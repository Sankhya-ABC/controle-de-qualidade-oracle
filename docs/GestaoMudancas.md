# Módulo: Gestão de Mudanças

## Visão Geral

Sistema de workflow para gerenciar mudanças organizacionais. Módulo paralelo ao RNC, mas com foco proativo: registra situação atual, propõe mudança, avalia riscos via questionários e coleta aprovações. Possui 5 fases com integração automática de questionários.

**TIPOCADASTRO = 4** no sistema de cadastros.

## Workflow de Fases

```
Fase 1:  GESTÃO DE MUDANÇAS — Registro da mudança (situação, sugestão, justificativa)
         ↓
Fase 2:  QUESTIONÁRIO DE AVALIAÇÃO — Questionários automáticos (ORIGEM=2)
         ↓
Fase 3:  AÇÕES DA GESTÃO DE MUDANÇA — Definição de ações (5W+H) + Aprovação
         ↓
Fase 4:  AVALIAÇÃO DE RISCOS — Questionários de risco (ORIGEM=3)
         ↓
Fase 5:  APROVAÇÃO FINAL — Questionários finais (ORIGEM=4) + Aprovação definitiva
```

### Fases com Questionários Automáticos

Nas fases 2, 4 e 5, o sistema popula automaticamente os questionários cadastrados via `QuestionarioGestaoMudanca.criaQuestionariosGestao()`.

---

## Comparação com RNC

| Aspecto | Gestão de Mudanças | RNC |
|---------|-------------------|-----|
| Fases | 5 | 10 |
| Tabela de fases | TGQFASESGM | TGQFASES |
| Tabela principal | TGQGESTAOMUDANCA | TGQRNC |
| TIPOCADASTRO | 4 | 1, 2, 3 |
| Questionários | ORIGEM 2, 3, 4 | ORIGEM 1 |
| Aprovação | TGQRESPGESTAOMUDANCA | TGQRESPRNC |
| Foco | Proativo (implementar mudanças) | Reativo (resolver não conformidades) |

---

## Tabelas do Banco

### TGQGESTAOMUDANCA — Registro Principal

| Campo | Tipo | Função |
|-------|------|--------|
| `IDGESTAO` (PK) | NUMBER(10) | ID da mudança |
| `SITUACAOATUAL` | VARCHAR2(4000) | Descrição da situação atual |
| `SUGESTAOMUDANCA` | VARCHAR2(4000) | Mudança proposta |
| `JUSTIFICATIVA` | VARCHAR2(4000) | Justificativa |
| `FASESID` (FK) | NUMBER(10) | Fase atual (1-5) |
| `PROCESSO` (FK) | NUMBER(10) | Processo vinculado (TIPOCADASTRO=4) |
| `CODUSU` | NUMBER(38) | Usuário solicitante |
| `DTCRIACAO` | DATE | Data de criação (auto) |
| `DTALTER` | DATE | Última alteração (auto) |

### TGQFASESGM — Definição das Fases

| FASESID | Descrição |
|---------|-----------|
| 1 | Gestão de Mudanças |
| 2 | Questionário de Avaliação da Mudança |
| 3 | Ações da Gestão de Mudança |
| 4 | Avaliação de Riscos das Ações |
| 5 | Avaliação da Proposta de Mudança |

### TGQACOESMUDANCA — Ações da Mudança (5W+H)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDACOES` (PK) | NUMBER(10) | ID da ação |
| `IDGESTAO` (FK) | NUMBER(10) | Mudança vinculada |
| `OQUE` | VARCHAR2(4000) | O quê? |
| `COMO` | VARCHAR2(4000) | Como? |
| `ONDE` | VARCHAR2(4000) | Onde? |
| `PORQUE` | VARCHAR2(4000) | Por quê? |
| `QUANDO` | DATE | Quando? |
| `QUANTO` | FLOAT | Quanto? (custo) |
| `DATAACAO` | DATE | Data da ação |
| `DATAPRAZO` | DATE | Prazo |
| `NAOSEAPLICA` | VARCHAR2(2) | Não se aplica |
| `CODUSU` | NUMBER(38) | Usuário criador |

### TGQQUEMGESTAOMUDANCA — Responsáveis por Ação

| Campo | Tipo | Função |
|-------|------|--------|
| `ID` (PK) | NUMBER(10) | ID |
| `IDACOES` (FK) | NUMBER(10) | Ação vinculada |
| `CODPARC` (FK) | NUMBER(10) | Parceiro responsável |

### TGQQUESTAVALIACAO — Questionários de Avaliação

| Campo | Tipo | Função |
|-------|------|--------|
| `IDAVALIACAO` (PK) | NUMBER(10) | ID da avaliação (SEQ_TGQQUESTAVALIACAO) |
| `IDGESTAO` (FK) | NUMBER(10) | Mudança vinculada |
| `IDQUEST` (FK) | NUMBER(10) | Questionário vinculado |
| `ORIGEM` | NUMBER(10) | 2=Avaliação, 3=Riscos, 4=Finalização |
| `CODUSU` | NUMBER(38) | Usuário |
| `DATAINCLUSAO` | DATE | Data de criação |

### TGQPERGUNTASAVALIACAO — Respostas dos Questionários

| Campo | Tipo | Função |
|-------|------|--------|
| `IDAVALIACAO` (PK, FK) | NUMBER(10) | Avaliação vinculada |
| `IDPERGUNTA` (PK, FK) | NUMBER(10) | Pergunta vinculada |
| `SIM` | VARCHAR2(1) | Resposta Sim |
| `NAO` | VARCHAR2(1) | Resposta Não |
| `OBSERVACAO` | VARCHAR2(4000) | Observações |
| `DATA` | DATE | Data da resposta |
| `CODUSU` | NUMBER(38) | Usuário |

### TGQRESPGESTAOMUDANCA — Aprovações

| Campo | Tipo | Função |
|-------|------|--------|
| `IDGESTAO` (PK, FK) | NUMBER(10) | Mudança vinculada |
| `CODUSU` (PK) | NUMBER(38) | Aprovador |
| `APROVADO` | VARCHAR2(1) | Checkbox aprovado |
| `REPROVADO` | VARCHAR2(1) | Checkbox reprovado |
| `ASSINATURA` | VARCHAR2(50) | Assinatura eletrônica |
| `DATAAPROVACAO` | DATE | Data da aprovação (auto) |
| `OBSNAOAPROVADO` | VARCHAR2(4000) | Motivo da reprovação |
| `ORIGEM` | VARCHAR2(2) | AM=Aprovação Mudança (Fase 3), AF=Aprovação Final (Fase 5) |

---

## Integração com Questionários

### Tipos de Questionário por Fase

| ORIGEM | Fase | Uso |
|--------|------|-----|
| 1 | — | Fornecedor (não usado neste módulo) |
| 2 | Fase 2 | Avaliação da Mudança |
| 3 | Fase 4 | Avaliação de Riscos |
| 4 | Fase 5 | Finalização/Aprovação |

### Auto-Populção de Questionários

Ao avançar para fases 2, 4 ou 5, `QuestionarioGestaoMudanca.criaQuestionariosGestao()` executa:

```
SE fase IN (2, 4, 5):
  BUSCA questionários ativos (INATIVO='N') com ORIGEM != 1
  QUE ainda não foram respondidos para este IDGESTAO
  
  PARA CADA questionário:
    INSERT TGQQUESTAVALIACAO (nova avaliação)
    
    PARA CADA pergunta do questionário:
      INSERT TGQPERGUNTASAVALIACAO (registro de resposta vazio)
```

Previne duplicatas — questionários já respondidos não são recriados.

---

## Componentes Java

### Action Buttons — Mudar Fase (4 classes)

| Classe | Fase | Instância |
|--------|------|-----------|
| `MudarFaseMudancaQuestionario` | 2 → 3 | MudancaQuestionarioAvaliacao |
| `MudarFaseAcoesMudanca` | 3 → 4 | AcoesDaMudanca |
| `MudarFaseAvaliacaoRiscos` | 4 → 5 | MudancaAvaliacaoRisco |
| `MudarFaseAprovacaoFinal` | 5 → Conclusão | AvaliacaoPosMudanca |

Todos chamam `AtualizaFases.atualizaFaseGestao(idGestao)`.

### Services

| Classe | Método | Função |
|--------|--------|--------|
| `AtualizaFases` | `atualizaFaseGestao(idGestao)` | Avança fase e dispara criação de questionários |
| `QuestionarioGestaoMudanca` | `criaQuestionariosGestao(fase, idGestao)` | Popula questionários automaticamente nas fases 2, 4, 5 |

---

## Telas (Instâncias DWF)

### Telas Principais

| Instância | Fase | Descrição |
|-----------|------|-----------|
| `GestaoMudanca` | 1 | Cadastro da mudança |
| `MudancaQuestionarioAvaliacao` | 2 | Questionários de avaliação |
| `AcoesDaMudanca` | 3 | Ações + aprovação da mudança |
| `MudancaAvaliacaoRisco` | 4 | Questionários de risco |
| `AvaliacaoPosMudanca` | 5 | Questionários finais + aprovação definitiva |

### Telas de Consulta

| Instância | Descrição |
|-----------|-----------|
| `ConsultaGestao` | Pesquisa geral de mudanças |
| `ConsAcoesMudanca` | Consulta de ações |
| `ConsRespAprMudanca` | Consulta de aprovações |

---

## Aprovações

Duas rodadas de aprovação no workflow:

| Etapa | Fase | ORIGEM | Descrição |
|-------|------|--------|-----------|
| Aprovação da Mudança | 3 | `AM` | Após definir ações, antes de avaliar riscos |
| Aprovação Final | 5 | `AF` | Após avaliação de riscos, encerra processo |

Cada aprovação registra: aprovador (CODUSU), assinatura eletrônica, data e motivo de reprovação (se aplicável).

---

## Fluxo de Dados Completo

```
1. REGISTRO (Fase 1)
   Usuário → Tela GestaoMudanca
   → INSERT TGQGESTAOMUDANCA (FASESID=1)

2. AVALIAÇÃO (Fase 2)
   MudarFase → atualizaFaseGestao() → FASESID=2
   → criaQuestionariosGestao(2, idGestao)
   → INSERT TGQQUESTAVALIACAO (ORIGEM=2)
   → INSERT TGQPERGUNTASAVALIACAO (perguntas)
   → Usuário responde questionários (SIM/NAO/OBSERVACAO)

3. AÇÕES (Fase 3)
   MudarFase → FASESID=3
   → Usuário define ações em TGQACOESMUDANCA (5W+H)
   → Define responsáveis em TGQQUEMGESTAOMUDANCA
   → Coleta aprovações em TGQRESPGESTAOMUDANCA (ORIGEM='AM')

4. RISCOS (Fase 4)
   MudarFase → FASESID=4
   → criaQuestionariosGestao(4, idGestao)
   → INSERT TGQQUESTAVALIACAO (ORIGEM=3)
   → Usuário responde questionários de risco

5. APROVAÇÃO FINAL (Fase 5)
   MudarFase → FASESID=5
   → criaQuestionariosGestao(5, idGestao)
   → INSERT TGQQUESTAVALIACAO (ORIGEM=4)
   → Usuário responde questionários finais
   → Coleta aprovações finais em TGQRESPGESTAOMUDANCA (ORIGEM='AF')
   → MudarFaseAprovacaoFinal → Processo concluído
```

---

## Campos de Auditoria

| Campo | Preenchimento | Expressão |
|-------|---------------|-----------|
| `DTCRIACAO` | Auto no insert | `getDateTime()` |
| `DTALTER` | Auto no update | `getDateTime()` |
| `DATAAPROVACAO` | Auto na aprovação | `getDateTime()` |
| `CODUSU` | Auto por sessão | `$ctx_usuario_logado` |

---

## Navegação no Menu

```
Controle de Qualidade
└── Gestão de Mudanças
    ├── 1. Cadastro (GestaoMudanca)
    ├── 2. Questionário de Avaliação (MudancaQuestionarioAvaliacao)
    ├── 3. Ações (AcoesDaMudanca)
    ├── 4. Avaliação de Riscos (MudancaAvaliacaoRisco)
    ├── 5. Aprovação Final (AvaliacaoPosMudanca)
    └── Consulta Gestão (ConsultaGestao)
```

---

## Arquivos do Módulo

```
qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── actionButtons/mudarfase/
│   ├── MudarFaseMudancaQuestionario.java
│   ├── MudarFaseAcoesMudanca.java
│   ├── MudarFaseAvaliacaoRiscos.java
│   └── MudarFaseAprovacaoFinal.java
└── services/
    ├── AtualizaFases.java (método atualizaFaseGestao)
    └── QuestionarioGestaoMudanca.java

datadictionary/
├── TABLE_TGQGESTAOMUDANCA.xml
├── TABLE_TGQFASESGM.xml
├── TABLE_TGQACOESMUDANCA.xml
├── TABLE_TGQQUEMGESTAOMUDANCA.xml
├── TABLE_TGQQUESTAVALIACAO.xml
├── TABLE_TGQPERGUNTASAVALIACAO.xml
├── TABLE_TGQRESPGESTAOMUDANCA.xml
└── TABLE_TGQCADASTROS.xml

dbscripts/
└── V1.xml (definições de tabelas, sequences, triggers)
```
