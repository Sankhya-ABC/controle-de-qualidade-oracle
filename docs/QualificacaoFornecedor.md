# Módulo: Qualificação de Fornecedor

## Visão Geral

Sistema de avaliação e qualificação de fornecedores via questionários digitais. Envia questionários por email, coleta respostas, calcula pontuação automática e classifica fornecedores.

## Fluxo Principal

```
1. Cria Questionário (perguntas + tipos de resposta)
2. Cria Registro de Qualificação (vincula fornecedor + questionário)
3. Envia Questionário por email (botão de ação)
4. Fornecedor responde via link com parâmetros Base64
5. Sistema calcula pontuação automaticamente
6. Classifica fornecedor: A (80-100), B (50-79), T (≤49)
7. Validação agendada notifica qualificações vencendo
```

## Componentes

### Action Buttons

| Classe | Função |
|--------|--------|
| `EnviarQuestionarioFornecedor` | Envia questionário ao fornecedor por email |
| `ValidacaoFornecSC` | Ação agendada que verifica vencimentos de qualificações e certificados |

### Services

| Classe | Função |
|--------|--------|
| `QuestionarioFornecedor` | Monta URL com params Base64, busca email do fornecedor em `TGFPAR.EMAILQUESTIONARIO`, envia via fila de email |
| `RegistroNaoConformeFornecedor` | Notifica fornecedor sobre registro de não conformidade |
| `Validacao.ValidacaoFornecedores()` | Verifica certificados vencidos e qualificações expirando em 10 dias, cria alertas no sistema |

### Listeners

| Classe | Função |
|--------|--------|
| `QualificacaoListener` | Calcula pontuação automática após inserção de respostas do fornecedor |
| `ValidaOrigemFornecedorListener` | Valida origem de RNC vinculada a fornecedor |

### Utilitários

| Classe | Função |
|--------|--------|
| `EnviarEmailUtil` | Envia emails HTML via fila `MSDFilaMensagem` (max 3 tentativas) |

## Algoritmo de Pontuação

Executado automaticamente pelo `QualificacaoListener` ao inserir respostas:

1. **Respostas "SIM":** 1 ponto cada
2. **Respostas numéricas** (por faixa de valor):

| Faixa | Pontos |
|-------|--------|
| < 10 | 0.15 |
| 10 - 29 | 0.30 |
| 30 - 49 | 0.45 |
| 50 - 69 | 0.60 |
| 70 - 89 | 0.75 |
| ≥ 90 | 0.90 |

3. **Score final** = `(Total Pontos / Nº Perguntas) × 100`

4. **Classificação:**

| Score | Classificação | Resultado |
|-------|--------------|-----------|
| 80 - 100 | A | Aprovado |
| 50 - 79 | B | Bom |
| ≤ 49 | T | Reprovado |

## Tabelas do Banco

| Tabela | Função |
|--------|--------|
| `TGQQUESTQUALIF` | Cadastro de questionários |
| `TGQPERGQUEST` | Perguntas do questionário (tipos: T=Texto, N=Numérico, C=Combo, R=Radio, CK=Checkbox) |
| `TGQPERGQUESTOPC` | Opções de resposta para perguntas de múltipla escolha |
| `TGQQUALIFFORN` | Registro de qualificação do fornecedor |
| `TGQQUALIFRESP` | Respostas do fornecedor ao questionário |
| `TGQARQQUESTFORN` | Arquivos anexos ao questionário |
| `TGQCERTFORN` | Certificados do fornecedor |
| `TGQCONFIG` | Configurações gerais do módulo |
| `TGQPONTUACAO` | Critérios de pontuação |
| `TGQIQF` | Índice de qualificação (IQF) |
| `TGQCRITERIOQF` | Faixas min/max de pontuação por critério |

## Campos Chave — TGQQUALIFFORN

| Campo | Valores |
|-------|---------|
| `QUALIFICACAO` | R = Requalificação, Q = Qualificado, D = Desqualificado, E = Em Processo |
| `TIPOFORNECIMENTO` | M = Matéria Prima, T = Transporte, E = Embalagem, Q = Químicos Controlados, S = Serviços |
| `FORMAQUALIF` | A = Ambos, C = Certificado, Q = Questionário |
| `SITUACAO` | P = Pendente, C = Concluído, CA = Cancelado |
| `RESULTADOIQF` | A, B, T (calculado automaticamente pelo listener) |
| `PONTUACAO` | Score numérico (0-100) |

## Parâmetros do Sistema

| Parâmetro | Função |
|-----------|--------|
| `LOGINQLD` | Login codificado em Base64 para composição da URL |
| `PSWQLD` | Senha codificada em Base64 para composição da URL |
| `URLQUALIDADE` | URL base do formulário de questionário |
| `NOMEEMPQLF` | Nome da empresa exibido nos emails |
| `HTMLEMAILQUEST` | Template HTML do email de questionário (placeholders: `{URL}`, `{EMPRESA}`) |
| `HTMLRNCFORNEC` | Template HTML do email de RNC (placeholders: `{RNC}`, `{DETALHAMENTO}`, `{EMPRESA}`) |

## Configurações (TGQCONFIG)

| Campo | Função |
|-------|--------|
| `CONTROLEFORNECEDOR` | Bloqueia compras quando qualificação está vencida |
| `NOTIFICFORNEC` | Ativa notificação ao fornecedor sobre vencimentos |
| `EMAILNOTIFYQF` | Email destinatário para alertas de qualificação vencida |
| `DIASRESPFORNEC` | Prazo em dias para resposta do fornecedor |
| `INDICEPADRAOEMP` | Índice de qualificação padrão da empresa |

## Fluxo de Dados

```
1. CRIAR QUALIFICAÇÃO
   Usuário → Tela QualificacaoFornecedor → INSERT TGQQUALIFFORN

2. ENVIAR QUESTIONÁRIO
   Botão "Enviar Questionario" → EnviarQuestionarioFornecedor
   → QuestionarioFornecedor.enviaQuestionario()
   → SELECT TGFPAR.EMAILQUESTIONARIO
   → Monta URL Base64: {URLQUALIDADE}?{email}?{idQualif}?{idQuest}?{login}?{senha}?
   → EnviarEmailUtil.enviarQuestionario()
   → INSERT MSDFilaMensagem (fila de email)

3. FORNECEDOR RESPONDE
   Link no email → Formulário externo
   → INSERT TGQQUALIFRESP (respostas)

4. PONTUAÇÃO AUTOMÁTICA
   INSERT TGQQUALIFRESP → QualificacaoListener.afterInsert()
   → calcPontuacao()
   → UPDATE TGQQUALIFFORN.PONTUACAO
   → UPDATE TGQQUALIFFORN.RESULTADOIQF

5. VALIDAÇÃO AGENDADA
   Scheduler → ValidacaoFornecSC → Validacao.ValidacaoFornecedores()
   → CHECK TGQQUALIFFORN.DATAVALIDADE (vence em 10 dias?)
   → CHECK TGQCERTFORN.DATAVALIDADE (certificados vencidos?)
   → EnviarEmailUtil.EnviarNotificacaoFornec()
   → INSERT TSIAVI (alertas do sistema)
```

## Arquivos do Módulo

```
qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── actionButtons/
│   ├── fornecedores/
│   │   └── EnviarQuestionarioFornecedor.java
│   └── ValidacaoFornecSC.java
├── services/
│   ├── QuestionarioFornecedor.java
│   ├── RegistroNaoConformeFornecedor.java
│   └── Validacao.java
├── listeners/
│   ├── QualificacaoListener.java
│   └── ValidaOrigemFornecedorListener.java
└── utils/
    └── EnviarEmailUtil.java

datadictionary/
├── TABLE_TGQQUALIFFORN.xml
├── TABLE_TGQQUESTQUALIF.xml
├── TABLE_TGQQUALIFRESP.xml
├── TABLE_TGQPERGQUEST.xml
├── TABLE_TGQPERGQUESTOPC.xml
├── TABLE_TGQARQQUESTFORN.xml
├── TABLE_TGQCERTFORN.xml
├── TABLE_TGQCONFIG.xml
├── TABLE_TGQPONTUACAO.xml
├── TABLE_TGQIQF.xml
└── TABLE_TGQCRITERIOQF.xml

dbscripts/
├── V1.xml  (schema inicial)
└── V2.xml  (atualizações)
```