# Módulo: Cadastro de Questionários

## Visão Geral

Sistema flexível de criação e gerenciamento de questionários reutilizáveis. Serve como catálogo central de questionários consumido por dois módulos: **Qualificação de Fornecedores** (respostas externas via email) e **Gestão de Mudanças** (respostas internas Sim/Não). Suporta 5 tipos de resposta e categorização por origem.

---

## Ciclo de Vida do Questionário

```
1. CRIAÇÃO
   Cadastra questionário (descrição + origem)
   → Adiciona perguntas (texto + tipo de resposta + ordenação)
   → Adiciona opções (para Combo/Radio/Checkbox)

2. USO EM QUALIFICAÇÃO DE FORNECEDOR (ORIGEM=1)
   Vincula ao fornecedor → Envia por email (URL Base64)
   → Fornecedor responde → TGQQUALIFRESP
   → Pontuação automática → Classificação A/B/T

3. USO EM GESTÃO DE MUDANÇAS (ORIGEM=2,3,4)
   Avanço de fase → Auto-popula questionários
   → Usuário responde Sim/Não + Observações → TGQPERGUNTASAVALIACAO
```

---

## Categorização por Origem

| ORIGEM | Descrição | Módulo | Fase |
|--------|-----------|--------|------|
| 1 | Fornecedor | Qualificação de Fornecedores | — |
| 2 | Gestão de Mudanças — Avaliação | Gestão de Mudanças | Fase 2 |
| 3 | Gestão de Mudanças — Riscos | Gestão de Mudanças | Fase 4 |
| 4 | Gestão de Mudanças — Finalização | Gestão de Mudanças | Fase 5 |

---

## Tipos de Resposta (TPRESP)

| Código | Tipo | Descrição | Opções |
|--------|------|-----------|--------|
| T | Texto | Resposta aberta em texto livre | — |
| N | Número | Valor numérico (usado no cálculo de pontuação) | — |
| C | Combo | Seleção dropdown | TGQPERGQUESTOPC |
| R | Radio | Escolha única entre opções | TGQPERGQUESTOPC |
| CK | Checkbox | Múltipla seleção | TGQPERGQUESTOPC |

**Nota:** Para Gestão de Mudanças, todas as respostas são Sim/Não + Observação, independente do tipo configurado.

---

## Tabelas do Banco

### TGQQUESTQUALIF — Cadastro de Questionários

| Campo | Tipo | Função |
|-------|------|--------|
| `IDQUEST` (PK) | NUMBER(10) | ID do questionário (auto-sequence) |
| `DESCRICAO` | VARCHAR2(100) | Descrição do questionário |
| `ORIGEM` | NUMBER(38) | Categoria: 1=Fornecedor, 2=Avaliação, 3=Riscos, 4=Finalização |
| `INATIVO` | VARCHAR2(1) | Marca como inativo |
| `DTCRIACAO` | DATE | Data de criação (auto) |
| `DTALTER` | DATE | Última alteração (auto — trigger) |
| `CODUSU` | NUMBER(38) | Usuário criador (auto: logado) |

### TGQPERGQUEST — Perguntas do Questionário

| Campo | Tipo | Função |
|-------|------|--------|
| `IDPERG` (PK) | NUMBER(10) | ID da pergunta (auto-sequence) |
| `IDQUEST` (FK) | NUMBER(10) | Questionário vinculado |
| `PERGUNTA` | VARCHAR2(350) | Texto da pergunta |
| `TPRESP` | VARCHAR2(100) | Tipo de resposta: T, N, C, R, CK |
| `ORDENACAO` | NUMBER(10) | Ordem de exibição |
| `DTCRIACAO` | DATE | Data de criação (auto) |
| `DTALTER` | DATE | Última alteração (auto — trigger) |

### TGQPERGQUESTOPC — Opções de Resposta

| Campo | Tipo | Função |
|-------|------|--------|
| `IDPERG` (PK, FK) | NUMBER(10) | Pergunta vinculada |
| `OPCAO` (PK) | VARCHAR2(20) | Texto/valor da opção |
| `IDOPC` | NUMBER(10) | Ordem da opção |

Usado quando TPRESP = C (Combo), R (Radio) ou CK (Checkbox).

---

## Tabelas de Resposta

### TGQQUALIFRESP — Respostas do Fornecedor

Armazena respostas individuais do fornecedor ao questionário.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDQUALIF` (PK, FK) | NUMBER(10) | Qualificação vinculada |
| `IDPERG` (PK, FK) | NUMBER(10) | Pergunta respondida |
| `IDQUEST` (FK) | NUMBER | Questionário |
| `RESPOSTA` | VARCHAR2(4000) | Resposta (texto ou valor numérico) |
| `DTCRIACAO` | DATE | Data da resposta (auto) |

**Pontuação automática** via `QualificacaoListener.afterInsert()`:
- "SIM" → 1 ponto
- Numérico: <10→0.15, 10-29→0.30, 30-49→0.45, 50-69→0.60, 70-89→0.75, ≥90→0.90
- Score = (Total / Nº Perguntas) × 100
- Classificação: A (≥80), B (50-79), T (≤49)

### TGQQUESTAVALIACAO — Avaliações de Gestão de Mudanças

Vincula questionários a processos de mudança por fase.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDAVALIACAO` (PK) | NUMBER(10) | ID da avaliação (SEQ_TGQQUESTAVALIACAO) |
| `IDGESTAO` (FK) | NUMBER(10) | Mudança vinculada |
| `IDQUEST` (FK) | NUMBER(10) | Questionário vinculado (read-only) |
| `ORIGEM` | NUMBER(10) | 2=Avaliação, 3=Riscos, 4=Finalização |
| `CODUSU` | NUMBER(38) | Usuário (auto: logado, read-only) |
| `DATAINCLUSAO` | DATE | Data de criação (auto, read-only) |

**Auto-populado** por `QuestionarioGestaoMudanca.criaQuestionariosGestao()` ao avançar para fases 2, 4 ou 5.

### TGQPERGUNTASAVALIACAO — Respostas de Avaliação

Respostas Sim/Não para questionários de Gestão de Mudanças.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDAVALIACAO` (PK, FK) | NUMBER(10) | Avaliação vinculada |
| `IDPERGUNTA` (PK, FK) | NUMBER(10) | Pergunta (read-only) |
| `SIM` | VARCHAR2(1) | Checkbox Sim |
| `NAO` | VARCHAR2(1) | Checkbox Não |
| `OBSERVACAO` | VARCHAR2(4000) | Comentários |
| `DATA` | DATE | Data da resposta (auto, read-only) |
| `CODUSU` | NUMBER(38) | Usuário (auto: logado, read-only) |

---

## Triggers

### TRG_INS_UPD_TGQQUESTQUALIF

- **Tabela:** TGQQUESTQUALIF
- **Evento:** BEFORE INSERT OR UPDATE
- **Ação:** Auto-preenche DTALTER = SYSDATE

### TRG_INS_UPD_TGQPERGQUEST

- **Tabela:** TGQPERGQUEST
- **Evento:** BEFORE INSERT OR UPDATE
- **Ação:** Auto-preenche DTALTER = SYSDATE

---

## Componentes Java

### Services

#### QuestionarioFornecedor — Envio para Fornecedor

| Método | Função |
|--------|--------|
| `enviaQuestionario(idQuest, codFornec, idQualif)` | Monta URL Base64 e envia email ao fornecedor |
| `enviaNotificacao(codFornec, mensagem)` | Envia notificação genérica ao fornecedor |

**Fluxo de envio:**
1. Busca email em `TGFPAR.EMAILQUESTIONARIO`
2. Codifica parâmetros em Base64: email, idQualif, idQuest, login, senha
3. Monta URL: `{URLQUALIDADE}?{email}?{idQualif}?{idQuest}?{login}?{senha}?`
4. Envia via `EnviarEmailUtil.enviarQuestionario()` (template HTMLEMAILQUEST)

#### QuestionarioGestaoMudanca — Auto-Populção para Mudanças

| Método | Função |
|--------|--------|
| `criaQuestionariosGestao(fase, idGestao)` | Cria avaliações e perguntas automaticamente |

**Algoritmo:**
```
SE fase IN (2, 4, 5):
  BUSCA questionários ativos com ORIGEM != 1 e INATIVO = 'N'
  QUE ainda não foram vinculados a este IDGESTAO
  
  PARA CADA questionário:
    INSERT TGQQUESTAVALIACAO (SEQ_TGQQUESTAVALIACAO.NEXTVAL)
    
    PARA CADA pergunta do questionário:
      INSERT TGQPERGUNTASAVALIACAO (avaliação + pergunta)
```

### Action Buttons

| Classe | Instância | Função |
|--------|-----------|--------|
| `EnviarQuestionarioFornecedor` | QualificacaoFornecedor | Botão "Enviar Questionário ao Fornecedor" |
| `MudarFaseMudancaQuestionario` | MudancaQuestionarioAvaliacao | Botão "Mudar Fase" (dispara auto-populção) |

### Listeners

| Classe | Evento | Função |
|--------|--------|--------|
| `QualificacaoListener` | afterInsert em TGQQUALIFRESP | Calcula pontuação e classificação automática |

---

## Telas (Instâncias DWF)

### Cadastro de Questionários

| Instância | Descrição |
|-----------|-----------|
| `Questionarios` | Tela principal de cadastro |
| `PerguntasQuestionario` | Perguntas e ordenação |
| `OpcaoPergunta` | Opções de resposta |
| `ArquivosQuestionarios` | Anexos do questionário |

### Respostas de Fornecedor

| Instância | Descrição |
|-----------|-----------|
| `RespostaFornecedor` | Respostas do fornecedor ao questionário |

### Respostas de Gestão de Mudanças

| Instância | Fase | Modo |
|-----------|------|------|
| `QuestionarioAvaliacao` | 2 — Avaliação | Edição |
| `ConsQuestionarioAvaliacao` | 2 — Avaliação | Consulta |
| `AvaliacaoDeRiscos` | 4 — Riscos | Edição |
| `ConsAvaliacaoDeRiscos` | 4 — Riscos | Consulta |
| `AprovacaoFinal` | 5 — Finalização | Edição |
| `ConsAprovacaoFinal` | 5 — Finalização | Consulta |
| `PerguntaAvaliacao` | 2 | Perguntas individuais |
| `PerguntasRiscos` | 4 | Perguntas individuais |
| `PerguntasAprovacaoFinal` | 5 | Perguntas individuais |

---

## Integração com Outros Módulos

### Qualificação de Fornecedores

```
TGQQUESTQUALIF (ORIGEM=1)
    ↓ vincula via
TGQQUALIFFORN.IDQUEST
    ↓ envio por email
QuestionarioFornecedor.enviaQuestionario()
    ↓ respostas
TGQQUALIFRESP
    ↓ cálculo automático
QualificacaoListener → PONTUACAO + RESULTADOIQF (A/B/T)
```

### Gestão de Mudanças

```
TGQQUESTQUALIF (ORIGEM=2,3,4)
    ↓ auto-populado ao mudar fase
QuestionarioGestaoMudanca.criaQuestionariosGestao()
    ↓ cria
TGQQUESTAVALIACAO (vincula questionário à mudança)
    ↓ cria
TGQPERGUNTASAVALIACAO (uma linha por pergunta)
    ↓ usuário responde
SIM/NAO + OBSERVACAO
```

---

## Parâmetros do Sistema

| Parâmetro | Função |
|-----------|--------|
| `LOGINQLD` | Login Base64 para URL do questionário |
| `PSWQLD` | Senha Base64 para URL do questionário |
| `URLQUALIDADE` | URL base do formulário externo |
| `NOMEEMPQLF` | Nome da empresa nos emails |
| `HTMLEMAILQUEST` | Template HTML do email (`{URL}`, `{EMPRESA}`) |

---

## Navegação no Menu

```
Controle de Qualidade
└── Cadastro de Questionários (Questionarios)
```

---

## Fluxo de Dados Completo

```
1. CADASTRO
   Tela Questionarios
   → INSERT TGQQUESTQUALIF (descrição, origem, status)
   → INSERT TGQPERGQUEST (perguntas, tipo resposta, ordenação)
   → INSERT TGQPERGQUESTOPC (opções para C/R/CK)

2. ENVIO AO FORNECEDOR (ORIGEM=1)
   Botão EnviarQuestionarioFornecedor
   → QuestionarioFornecedor.enviaQuestionario()
   → Busca TGFPAR.EMAILQUESTIONARIO
   → Codifica params Base64
   → EnviarEmailUtil → INSERT MSDFilaMensagem
   → Fornecedor recebe email com link

3. RESPOSTA DO FORNECEDOR
   Fornecedor acessa link → preenche respostas
   → INSERT TGQQUALIFRESP (idQualif + idPerg + resposta)
   → QualificacaoListener.afterInsert()
   → Calcula pontuação por faixa
   → UPDATE TGQQUALIFFORN.PONTUACAO
   → UPDATE TGQQUALIFFORN.RESULTADOIQF (A/B/T)

4. AUTO-POPULÇÃO GESTÃO DE MUDANÇAS (ORIGEM=2,3,4)
   AtualizaFases.atualizaFaseGestao() → fase 2, 4 ou 5
   → QuestionarioGestaoMudanca.criaQuestionariosGestao()
   → SELECT questionários ativos (ORIGEM != 1, INATIVO = 'N')
   → INSERT TGQQUESTAVALIACAO (vincula à mudança)
   → INSERT TGQPERGUNTASAVALIACAO (uma por pergunta)

5. RESPOSTA GESTÃO DE MUDANÇAS
   Usuário preenche na tela da fase
   → UPDATE TGQPERGUNTASAVALIACAO (SIM/NAO + OBSERVACAO)
```

---

## Constraints e Foreign Keys

```sql
FK_PERGQUEST:    TGQPERGQUEST.IDQUEST    → TGQQUESTQUALIF.IDQUEST
FK_TGQPERGQUESTOPC: TGQPERGQUESTOPC.IDPERG → TGQPERGQUEST.IDPERG
```

---

## Arquivos do Módulo

```
qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── services/
│   ├── QuestionarioFornecedor.java
│   └── QuestionarioGestaoMudanca.java
├── actionButtons/
│   ├── fornecedores/
│   │   └── EnviarQuestionarioFornecedor.java
│   └── mudarfase/
│       └── MudarFaseMudancaQuestionario.java
└── listeners/
    └── QualificacaoListener.java

datadictionary/
├── TABLE_TGQQUESTQUALIF.xml
├── TABLE_TGQPERGQUEST.xml
├── TABLE_TGQPERGQUESTOPC.xml
├── TABLE_TGQQUESTAVALIACAO.xml
├── TABLE_TGQPERGUNTASAVALIACAO.xml
└── TABLE_TGQQUALIFRESP.xml

dbscripts/
├── V1.xml (tabelas, sequences, FKs, triggers)
└── V2.xml (atualizações)
```
