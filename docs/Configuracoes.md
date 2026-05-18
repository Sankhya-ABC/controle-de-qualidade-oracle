# Módulo: Configurações e Parametrização

## Visão Geral

Núcleo de parametrização do sistema de Controle de Qualidade. Centraliza configurações de fornecedores, documentos, não conformidades, fases de processos, critérios de pontuação, índices de qualificação, cadastros gerais e prioridades.

---

## Tabelas do Banco

### TGQCONFIG — Configurações Gerais

Tabela principal de configuração. Geralmente um registro por empresa.

| Campo | Tipo | Grupo | Função |
|-------|------|-------|--------|
| `IDCONFIG` (PK) | INTEIRO | — | ID da configuração (auto-sequence) |
| `CODEMP` (FK) | INTEIRO | Geral | Código da empresa |
| `CONTROLEFORNECEDOR` | CHECKBOX | Fornecedor | Bloqueia compras com qualificação vencida |
| `NOTIFICFORNEC` | CHECKBOX | Fornecedor | Notificar fornecedor sobre vencimentos |
| `EMAILNOTIFYQF` | VARCHAR(100) | Fornecedor | Email para alertas de qualificação vencida |
| `DIASRESPFORNEC` | NÚMERO | Fornecedor | Prazo em dias para resposta do fornecedor |
| `INDICEPADRAOEMP` | INTEIRO | Fornecedor | Índice de qualificação padrão |
| `CONTROLEDOCUMENTOS` | CHECKBOX | Documentos | Ativa validação de documentos |
| `QTDDIASDOC` | INTEIRO | Documentos | Dias de antecedência para alerta de vencimento |
| `EMAILNOTFY` | VARCHAR(80) | Documentos | Email para notificações de documentos |
| `CODUSU` | INTEIRO | Documentos | Usuário para notificações (auto: usuário logado) |
| `CONTROLENAOCONFORMIDADES` | CHECKBOX | RNC | Ativa validação de não conformidades |

**Relacionamentos:**
- `IDCONFIG` → TGQIQF (Índices de Qualificação)
- `IDCONFIG` → TGQPONTUACAO (Critérios de Pontuação)
- `IDCONFIG` → TGQCRITERIOQF (Faixas de Qualificação)
- `CODEMP` → Empresa
- `CODUSU` → Usuário

**Uso nos Services:**
- `Validacao.ValidacaoDocumentos()` → lê QTDDIASDOC, EMAILNOTFY, CODUSU
- `Validacao.ValidacaoFornecedores()` → lê NOTIFICFORNEC, EMAILNOTIFYQF, DIASRESPFORNEC

---

### TGQCADASTROS — Cadastros Gerais

Classificações reutilizáveis para RNC e Gestão de Mudanças.

| Campo | Tipo | Função |
|-------|------|--------|
| `ID` (PK) | INTEIRO | ID do cadastro (auto-sequence) |
| `TIPOCADASTRO` | LISTA | Tipo do cadastro |
| `DESCRICAO` | TEXTO | Descrição |
| `PRAZODIAS` | INTEIRO | Prazo SLA em dias |

#### Valores de TIPOCADASTRO

| Valor | Descrição | Instância DWF | Uso |
|-------|-----------|---------------|-----|
| 1 | Origem RNC | Origem | De onde veio a NC (Fornecedor, Interno, Cliente) |
| 2 | Processo RNC | TipoCadastro | Processo associado (Produção, Qualidade, Entrega) |
| 3 | Tipo RNC | TipoRNC | Classificação da NC (Crítica, Maior, Menor) |
| 4 | Gestão de Mudanças | ProcessoGM | Tipo de processo de mudança |

---

### TGQPONTUACAO — Critérios de Pontuação

Define critérios e valores de pontos para qualificação de fornecedores.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDPONTOS` (PK) | INTEIRO | ID (auto-sequence) |
| `CRITERIO` | VARCHAR(150) | Descrição do critério |
| `PONTOS` | DECIMAL(38,2) | Valor em pontos |
| `IDCONFIG` (FK) | INTEIRO | Configuração vinculada |

---

### TGQIQF — Índice de Qualificação de Fornecedores

Define classificações e validades por faixa de resultado.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDIQF` (PK) | INTEIRO | ID (auto-sequence) |
| `IQF` | VARCHAR(150) | Descrição do índice |
| `RESULTADO` | VARCHAR(80) | Código do resultado (A, B, T) |
| `CLASSIFICACAO` | VARCHAR(10) | Classificação |
| `VALIDADE` | VARCHAR(50) | Período de validade |
| `IDCONFIG` (FK) | INTEIRO | Configuração vinculada |

---

### TGQCRITERIOQF — Faixas de Qualificação

Define faixas min/max de pontuação por nível de qualificação.

| Campo | Tipo | Função |
|-------|------|--------|
| `ID` (PK) | INTEIRO | ID (auto-sequence) |
| `DESCRICAO` | VARCHAR(100) | Descrição da faixa |
| `PONTOSMIN` | INTEIRO | Pontuação mínima |
| `PONTOSMAX` | INTEIRO | Pontuação máxima |
| `IDCONFIG` (FK) | INTEIRO | Configuração vinculada |

**Trigger TRG_INS_CONFIG:** Ao criar nova configuração, popula automaticamente com opções do campo QUALIFICACAO de TGQQUALIFFORN.

---

### TGQFASES — Fases de RNC

10 fases do processo de Registro de Não Conformidade.

| FASESNCID | Descrição |
|-----------|-----------|
| 1 | Registro de Não Conformidade |
| 2 | Ações Imediatas |
| 3 | Causa Raiz |
| 4 | Abrangência |
| 5 | Ações Corretivas |
| 6 | Revisão de Documentos |
| 7 | Riscos e Oportunidades |
| 8 | Implementação |
| 9 | Liberação de Produto |
| 10 | Verificação de Eficácia (Conclusão) |

---

### TGQFASESGM — Fases de Gestão de Mudanças

5 fases do processo de Gestão de Mudanças.

| FASESID | Descrição |
|---------|-----------|
| 1 | Gestão de Mudanças |
| 2 | Questionário de Avaliação da Mudança |
| 3 | Ações da Gestão de Mudança |
| 4 | Avaliação de Riscos das Ações |
| 5 | Aprovação da Proposta de Mudança |

---

### TGQFASESORIGEM — Fases por Origem

Vincula fases a cadastros genéricos com SLAs diferenciados.

| Campo | Tipo | Função |
|-------|------|--------|
| `FASESID` | INTEIRO | Identificador da fase |
| `ID` (FK) | INTEIRO | Cadastro vinculado (TGQCADASTROS.ID) |
| `DIAS` | INTEIRO | Prazo SLA em dias |

---

### TGQPRIORIDADE — Prioridades

Define prioridades com prazos para RNC.

| Campo | Tipo | Função |
|-------|------|--------|
| `IDPRIORIDADE` (PK) | INTEIRO | ID (auto-sequence) |
| `DESCRICAO` | TEXTO | Descrição da prioridade |
| `PRAZO` | INTEIRO | Valor do prazo |
| `TIPOPRAZO` | LISTA | D=Dia(s), S=Semana(s), M=Mês(es), A=Ano(s) |

---

## Parâmetros do Sistema

Configurados via interface de Parâmetros do Sankhya (`ParameterUtils.getParameter()`).

| Parâmetro | Tipo | Função | Usado em |
|-----------|------|--------|----------|
| `LOGINQLD` | Base64 | Login para URL do questionário | QuestionarioFornecedor |
| `PSWQLD` | Base64 | Senha para URL do questionário | QuestionarioFornecedor |
| `URLQUALIDADE` | URL | URL base do formulário de questionário | QuestionarioFornecedor |
| `NOMEEMPQLF` | Texto | Nome da empresa nos emails | QuestionarioFornecedor, RegistroNaoConformeFornecedor |
| `HTMLEMAILQUEST` | HTML | Template email questionário (`{URL}`, `{EMPRESA}`) | EnviarEmailUtil |
| `HTMLRNCFORNEC` | HTML | Template email RNC (`{RNC}`, `{DETALHAMENTO}`, `{EMPRESA}`) | EnviarEmailUtil |

---

## Triggers de Configuração

### TRG_INS_CONFIG (V2.xml)

- **Tabela:** TGQCONFIG
- **Evento:** AFTER INSERT
- **Ação:** Popula TGQCRITERIOQF automaticamente com opções do campo QUALIFICACAO de TGQQUALIFFORN

```sql
INSERT INTO TGQCRITERIOQF (ID, DESCRICAO, IDCONFIG)
SELECT ROWNUM, O.OPCAO, :NEW.IDCONFIG
FROM TDDCAM C, TDDOPC O
WHERE C.NUCAMPO = O.NUCAMPO
  AND C.NOMETAB = 'TGQQUALIFFORN'
  AND C.NOMECAMPO = 'QUALIFICACAO'
```

---

## Telas (Instâncias DWF)

| Instância | Tabela | Descrição |
|-----------|--------|-----------|
| `ConfigQualidade` | TGQCONFIG | Configurações e Parametrizações |
| `TipoCadastro` | TGQCADASTROS | Cadastros Gerais (todos os tipos) |
| `Origem` | TGQCADASTROS | Origens RNC (TIPOCADASTRO=1) |
| `TipoRNC` | TGQCADASTROS | Tipos RNC (TIPOCADASTRO=3) |
| `ProcessoGM` | TGQCADASTROS | Processos Gestão Mudanças (TIPOCADASTRO=4) |
| `IndiceQualificacao` | TGQIQF | Índices de Qualificação |
| `PontuacaoFornec` | TGQPONTUACAO | Critérios de Pontuação |
| `CriterioQualific` | TGQCRITERIOQF | Faixas de Qualificação |
| `CadastroFases` | TGQFASES | Fases de RNC |
| `FasesGestaoMudanca` | TGQFASESGM | Fases de Gestão de Mudanças |
| `FasesOrigem` | TGQFASESORIGEM | Fases por Origem |
| `Prioridades` | TGQPRIORIDADE | Cadastro de Prioridades |
| `PermissaoDocUsuario` | TGQPERMISSAOUSUDOC | Permissões de Documentos por Usuário |

---

## Navegação no Menu

```
Controle de Qualidade
└── Configuração e Parametrização
    ├── Configurações (ConfigQualidade)
    ├── Cadastros Gerais (TipoCadastro)
    ├── Prioridades (Prioridades)
    ├── Fases RNC (CadastroFases)
    ├── Fases Gestão de Mudanças (FasesGestaoMudanca)
    └── Permissões de Documentos (PermissaoDocUsuario)
```

---

## Relacionamentos entre Tabelas

```
TGQCONFIG (raiz)
├── CODEMP → Empresa
├── CODUSU → Usuário
├── IDCONFIG → TGQCRITERIOQF (faixas min/max)
├── IDCONFIG → TGQPONTUACAO (critérios + pontos)
├── IDCONFIG → TGQIQF (índices A/B/T + validade)
└── IDCONFIG → TGQPERMISSAOUSUDOC (permissões documentos)

TGQCADASTROS (classificações)
├── TIPOCADASTRO=1 → TGQRNC.ORIGEM
├── TIPOCADASTRO=2 → TGQRNC (processo)
├── TIPOCADASTRO=3 → TGQRNC.TIPONC
├── TIPOCADASTRO=4 → TGQGESTAOMUDANCA.PROCESSO
└── ID → TGQFASESORIGEM.ID (SLA por origem)

TGQFASES → TGQRNC.FASESNCID (10 fases)
TGQFASESGM → TGQGESTAOMUDANCA.FASESID (5 fases)
TGQPRIORIDADE → TGQRNC.PRIORIDADE
```

---

## Fluxo de Qualificação (Config → Execução)

```
1. CONFIGURAR
   TGQCONFIG → define parâmetros de fornecedor e documentos
   TGQCRITERIOQF → define faixas (ex: 80-100=A, 50-79=B, ≤49=T)
   TGQIQF → define índices com validade
   TGQPONTUACAO → define critérios com valores

2. EXECUTAR
   Fornecedor responde questionário → TGQQUALIFRESP
   QualificacaoListener calcula pontuação automática
   Score = (Total Pontos / Nº Perguntas) × 100
   Classifica: A (≥80), B (50-79), T (≤49)
   Atualiza TGQQUALIFFORN.RESULTADOIQF + PONTUACAO

3. MONITORAR
   ValidacaoFornecSC (agendado)
   → Lê NOTIFICFORNEC, DIASRESPFORNEC de TGQCONFIG
   → Verifica TGQQUALIFFORN.DATAVALIDADE
   → Verifica TGQCERTFORN.DATAVALIDADE
   → Envia notificações + cria alertas TSIAVI
```

---

## Checklist de Configuração Inicial

- [ ] Criar registro em TGQCONFIG com parâmetros da empresa
- [ ] Cadastrar Origens RNC (TIPOCADASTRO=1)
- [ ] Cadastrar Tipos RNC (TIPOCADASTRO=3)
- [ ] Cadastrar Processos RNC (TIPOCADASTRO=2)
- [ ] Cadastrar Processos Gestão de Mudanças (TIPOCADASTRO=4)
- [ ] Configurar Prioridades com prazos
- [ ] Definir Faixas de Qualificação em TGQCRITERIOQF (auto-populado pela trigger)
- [ ] Definir Índices de Qualificação em TGQIQF
- [ ] Definir Critérios de Pontuação em TGQPONTUACAO
- [ ] Configurar Parâmetros do Sistema: LOGINQLD, PSWQLD, URLQUALIDADE, NOMEEMPQLF
- [ ] Configurar Templates de Email: HTMLEMAILQUEST, HTMLRNCFORNEC
- [ ] Agendar ValidacaoFornecSC e ValidadeDocumentosSC como Jobs
- [ ] Configurar Permissões de Documentos por Usuário

---

## Notas Importantes

1. **TGQCONFIG** geralmente tem um registro por empresa (via CODEMP). Suporta multi-empresa.
2. **TRG_INS_CONFIG** popula TGQCRITERIOQF automaticamente — não precisa inserir manualmente.
3. **Fases são estruturais** — não devem ser alteradas após uso em produção.
4. **Pontuação é calculada automaticamente** pelo QualificacaoListener, não permite entrada manual.
5. **Parâmetros do Sistema** devem ser configurados via interface Sankhya, não direto no banco.
6. **LOGINQLD/PSWQLD** são Base64 (obfuscação, não criptografia).

---

## Arquivos do Módulo

```
datadictionary/
├── TABLE_TGQCONFIG.xml
├── TABLE_TGQCADASTROS.xml
├── TABLE_TGQPONTUACAO.xml
├── TABLE_TGQIQF.xml
├── TABLE_TGQCRITERIOQF.xml
├── TABLE_TGQFASES.xml
├── TABLE_TGQFASESGM.xml
├── TABLE_TGQFASESORIGEM.xml
├── TABLE_TGQPRIORIDADE.xml
├── TABLE_TGQPERMISSAOUSUDOC.xml
└── MENU_br.com.sankhya.qualidade.FrameBuilder.xml

dbscripts/
├── V1.xml (CREATE TABLE, INSERT fases padrão, sequences)
└── V2.xml (TRG_INS_CONFIG)

qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── services/
│   ├── Validacao.java (lê TGQCONFIG para validações)
│   └── AtualizaFases.java (usa TGQFASES/TGQFASESGM)
└── listeners/
    └── QualificacaoListener.java (usa critérios de pontuação)
```
