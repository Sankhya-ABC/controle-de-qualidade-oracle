# Módulo: Gestão de Documentos

## Visão Geral

Sistema de controle de documentos com workflow de revisão e aprovação, versionamento de arquivos, controle de impressão, permissões por usuário e monitoramento de validade. Integra com RNC na Fase 7 (Revisão de Documentos).

## Ciclo de Vida do Documento

```
1. CRIAÇÃO → STATUS = 'P' (Pendente)
   ↓
2. UPLOAD DE ARQUIVOS (versionamento)
   ↓
3. REVISÃO → Botão "Marcar Documento Revisado" → STATUS = 'R'
   ↓
4. APROVAÇÃO → Botão "Aprovação de Documentos" → STATUS = 'A'
   ↓
5. MONITORAMENTO → Validação agendada de validade
```

### Regras de Transição

| De | Para | Validação |
|----|------|-----------|
| P (Pendente) | R (Revisado) | Apenas documentos pendentes podem ser revisados |
| R (Revisado) | A (Aprovado) | Apenas documentos revisados podem ser aprovados |

Cada mudança de status gera registro automático em `TGQHISTSTATUS` via trigger.

---

## Tabelas do Banco

### TGQCONTDOC — Controle de Documentos (Principal)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDDOC` (PK) | NUMBER(10) | ID do documento (auto-sequence) |
| `TITULODOC` | VARCHAR2(150) | Título do documento |
| `CODDOC` | VARCHAR2(20) | Código identificador |
| `STATUS` | VARCHAR2(50) | P=Pendente, R=Revisado, A=Aprovado |

### TGQARQDOC — Arquivos do Documento (Versionamento)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDARQ` (PK) | NUMBER(10) | ID do arquivo |
| `IDDOCUMENTO` (FK) | NUMBER(10) | Documento vinculado |
| `ARQUIVO` | BLOB | Conteúdo binário do arquivo |
| `VERSAO` | NUMBER(10) | Número da versão |
| `DATACRIACAO` | DATE | Data de criação (auto — trigger) |
| `DATAVALIDADE` | DATE | Data de validade |
| `COMENTARIO` | CLOB | Comentários sobre versão |
| `CODUSU` | NUMBER(38) | Usuário (auto — trigger) |

### TGQHISTSTATUS — Histórico de Status

| Campo | Tipo | Função |
|-------|------|--------|
| `IDHIST` (PK) | NUMBER(10) | ID do histórico |
| `IDDOCUMENTO` (FK) | NUMBER(10) | Documento vinculado |
| `STATUS` | VARCHAR2(20) | Status registrado (P, R, A, E) |
| `DATASTATUS` | DATE | Data da mudança (auto — trigger) |
| `CODUSU` | NUMBER(38) | Usuário que alterou (auto — trigger) |

### TGQCTRLIMPRESSOS — Controle de Impressão

| Campo | Tipo | Função |
|-------|------|--------|
| `IDCONTROLE` (PK) | NUMBER(10) | ID do controle |
| `IDDOCUMENTO` (FK) | NUMBER(10) | Documento impresso |
| `VERSAO` | NUMBER(10) | Versão impressa |
| `DATAIMPRESSAO` | DATE | Data da impressão (auto) |
| `QTDECOPIAS` | NUMBER(10) | Quantidade de cópias |
| `CODUSU` | NUMBER(38) | Usuário que imprimiu (auto) |

### TGQPERMDOC — Permissões por Documento

| Campo | Tipo | Função |
|-------|------|--------|
| `CODUSU` (PK) | NUMBER | Usuário |
| `IDDOC` (FK) | NUMBER | Documento |
| `IDCONFIG` (FK) | NUMBER | Configuração |
| `LEITURA` | VARCHAR2(10) | Permissão de leitura |
| `REVISAO` | VARCHAR2(10) | Permissão de revisão |
| `IMPRESSAO` | VARCHAR2(10) | Permissão de impressão |
| `APROVACAO` | VARCHAR2(10) | Permissão de aprovação |

### TGQPERMISSAOUSUDOC — Permissões Globais por Usuário

| Campo | Tipo | Função |
|-------|------|--------|
| `IDPERMISSAO` (PK) | NUMBER(10) | ID |
| `IDCONFIG` (FK) | NUMBER(10) | Configuração |
| `CODUSU` | NUMBER(10) | Usuário |
| `PERMITEREVISAO` | VARCHAR2(1) | Pode revisar documentos |
| `PERMITEAPROVACAO` | VARCHAR2(1) | Pode aprovar documentos |

### TGQREVISAODOC — Revisão de Documentos (Integração RNC)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDREVISAO` (PK) | NUMBER(10) | ID da revisão |
| `RNCID` (FK) | NUMBER(10) | RNC vinculada |
| `DESCRICAO` | VARCHAR2(4000) | Descrição da revisão |
| `VALIDADO` | VARCHAR2(2) | Checkbox aprovado |
| `REPROVADO` | VARCHAR2(2) | Checkbox reprovado |
| `DATAREVISAO` | DATE | Data da revisão (auto) |
| `CODUSU` | NUMBER(38) | Revisor (auto) |
| `NAOSEAPLICA` | VARCHAR2(2) | Não se aplica |

---

## Triggers

### TRG_INS_UPD_DOC_QLD — Histórico Automático

- **Tabela:** TGQCONTDOC
- **Evento:** AFTER INSERT OR UPDATE
- **Ação:** Insere registro em TGQHISTSTATUS com status atual, data e usuário logado

### TRG_INS_UPD_TGQARQDOC — Auto-preenchimento de Arquivos

- **Tabela:** TGQARQDOC
- **Evento:** BEFORE INSERT OR UPDATE
- **Ação:** Preenche DATACRIACAO = SYSDATE e CODUSU = usuário logado

---

## Componentes Java

### Action Buttons

#### AprovacaoDocumentos — Aprovar Documento

- **Instância:** ControleDocumentos
- **Validação:** STATUS deve ser 'R' (Revisado)
- **Ação:** `AtualizaDocumentos.aprovaDocumento(idDoc)` → STATUS = 'A'
- **Erro:** "Apenas documentos revisados podem ser aprovados!"

#### MarcarDocumentoRevisado — Marcar como Revisado

- **Instância:** ControleDocumentos
- **Validação:** STATUS deve ser 'P' (Pendente)
- **Ação:** `AtualizaDocumentos.documentoRevisado(idDoc)` → STATUS = 'R'
- **Erro:** "Apenas documentos pendentes podem ser aprovados!"

#### MudarFaseRevisaoDocumentos — Avançar Fase (RNC)

- **Instância:** RevisaoDocumentos
- **Ação:** `AtualizaFases.atualizaFaseRnc(rncid, origem)` → próxima fase

#### VoltarFaseRevisaoDocumentos — Voltar Fase (RNC)

- **Instância:** RevisaoDocumentos
- **Validação:** STATUS != 'C' (não pode voltar se concluída)
- **Ação:** `AtualizaFases.retornaFaseRnc(rncid, status)`

#### CancelarRncRevisaoDocumentos — Cancelar RNC

- **Instância:** RevisaoDocumentos
- **Ação:** `AtualizaFases.concluiFaseRnc(rncid)` → STATUS = 'C'

### Services

| Classe | Método | Função |
|--------|--------|--------|
| `AtualizaDocumentos` | `aprovaDocumento(idDoc)` | UPDATE STATUS = 'A' |
| `AtualizaDocumentos` | `documentoRevisado(idDoc)` | UPDATE STATUS = 'R' |
| `Validacao` | `ValidacaoDocumentos()` | Verifica validade e cria alertas TSIAVI |

### Ação Agendada

#### ValidadeDocumentosSC

- **Tipo:** ScheduledAction
- **Ação:** Chama `Validacao.ValidacaoDocumentos()`
- **Lógica:**
  1. Busca `QTDDIASDOC` em TGQCONFIG (dias antes do vencimento)
  2. Consulta TGQARQDOC com DATAVALIDADE <= SYSDATE + QTDDIASDOC
  3. Cria alertas TSIAVI: "O documento {ID} - {TITULO} irá vencer (ou venceu) no dia {DATA}"

---

## Permissões

### Dois Níveis de Controle

**Nível 1 — Por Documento (TGQPERMDOC):**
Controle granular por documento + usuário. Campos: Leitura, Revisão, Impressão, Aprovação.

**Nível 2 — Global por Usuário (TGQPERMISSAOUSUDOC):**
Controle geral do que o usuário pode fazer. Campos: Permite Revisão, Permite Aprovação.

---

## Integração com RNC

Módulo de Documentos integra na **Fase 7 do RNC** (Revisão de Documentos):

- Tabela `TGQREVISAODOC` vincula revisão à RNC via `RNCID`
- Botões de Mudar/Voltar/Cancelar fase disponíveis na tela RevisaoDocumentos
- Responsável pela revisão definido em `TGQRESPRNC` (instância RespRevisao)

---

## Configurações (TGQCONFIG)

| Campo | Função |
|-------|--------|
| `CONTROLEDOCUMENTOS` | Habilita/desabilita módulo de documentos |
| `QTDDIASDOC` | Dias de antecedência para notificação de vencimento |
| `EMAILNOTFY` | Email para notificações de documentos |

---

## Telas (Instâncias DWF)

| Instância | Descrição | Sub-formulários |
|-----------|-----------|----------------|
| `ControleDocumentos` | Tela principal de documentos | ListaArquivos, ControleImpressao, HistoricoStatus |
| `RevisaoDocumentos` | Revisão na fase RNC | DadosRevisao, ConsultaRespRNC, ConsultaDetalhe |
| `PermissaoDoc` | Gerenciamento de permissões | — |
| `DocumentosEvidencias` | Evidências de RNC | — |

---

## Navegação no Menu

```
Controle de Qualidade
└── Gestão de Documentos
    ├── Controle de Documentos (ControleDocumentos)
    └── 06. Revisão de Documentos (RevisaoDocumentos)
```

---

## Fluxo de Dados Completo

```
1. CRIAÇÃO
   Usuário → Tela ControleDocumentos
   → INSERT TGQCONTDOC (STATUS='P')
   → TRIGGER: INSERT TGQHISTSTATUS (STATUS='P')

2. UPLOAD DE ARQUIVO
   Usuário anexa arquivo
   → INSERT TGQARQDOC (VERSAO, ARQUIVO, DATAVALIDADE)
   → TRIGGER: auto-preenche DATACRIACAO, CODUSU

3. REVISÃO
   Botão "Marcar Documento Revisado"
   → Valida STATUS = 'P'
   → AtualizaDocumentos.documentoRevisado()
   → UPDATE TGQCONTDOC.STATUS = 'R'
   → TRIGGER: INSERT TGQHISTSTATUS (STATUS='R')

4. APROVAÇÃO
   Botão "Aprovação de Documentos"
   → Valida STATUS = 'R'
   → AtualizaDocumentos.aprovaDocumento()
   → UPDATE TGQCONTDOC.STATUS = 'A'
   → TRIGGER: INSERT TGQHISTSTATUS (STATUS='A')

5. IMPRESSÃO (quando ocorre)
   → INSERT TGQCTRLIMPRESSOS (VERSAO, QTDECOPIAS)
   → Auto-preenche DATAIMPRESSAO, CODUSU

6. MONITORAMENTO DE VALIDADE
   ValidadeDocumentosSC (agendado)
   → Busca QTDDIASDOC em TGQCONFIG
   → Consulta documentos vencendo
   → INSERT TSIAVI (alertas do sistema)
```

---

## Arquivos do Módulo

```
qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── actionButtons/
│   ├── documentos/
│   │   ├── AprovacaoDocumentos.java
│   │   └── MarcarDocumentoRevisado.java
│   ├── mudarfase/
│   │   └── MudarFaseRevisaoDocumentos.java
│   ├── voltarfase/
│   │   └── VoltarFaseRevisaoDocumentos.java
│   ├── cancelarrnc/
│   │   └── CancelarRncRevisaoDocumentos.java
│   └── ValidadeDocumentosSC.java
└── services/
    ├── AtualizaDocumentos.java
    └── Validacao.java (método ValidacaoDocumentos)

datadictionary/
├── TABLE_TGQCONTDOC.xml
├── TABLE_TGQARQDOC.xml
├── TABLE_TGQHISTSTATUS.xml
├── TABLE_TGQCTRLIMPRESSOS.xml
├── TABLE_TGQPERMDOC.xml
├── TABLE_TGQPERMISSAOUSUDOC.xml
└── TABLE_TGQREVISAODOC.xml

dbscripts/
├── V1.xml (criação de tabelas e sequences)
└── V2.xml (triggers TRG_INS_UPD_DOC_QLD, TRG_INS_UPD_TGQARQDOC)
```
