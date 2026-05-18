# Módulo: RNC — Registro de Não Conformidade

## Visão Geral

Sistema completo de gestão do ciclo de vida de não conformidades. Desde registro inicial até conclusão, passando por análise de causa raiz, ações corretivas, verificação de eficácia e liberação de produtos. Suporta workflow de 10 fases com transições controladas, atribuição de responsáveis e notificações por email.

## Workflow de Fases

```
Fase 1:  REGISTRO DE NÃO CONFORMIDADE
         ↓
Fase 2:  AÇÕES IMEDIATAS (contenção)
         ↓
Fase 3:  CAUSA RAIZ (diagrama de Ishikawa)
         ↓
Fase 4:  AÇÕES CORRETIVAS (5W2H)
         ↓
Fase 5:  IMPLEMENTAÇÃO (execução + evidências)
         ↓
Fase 6:  VERIFICAÇÃO DE EFICÁCIA
         ↓
Fase 7:  REVISÃO DE DOCUMENTOS
         ↓
Fase 8:  RISCOS E OPORTUNIDADES (5W2H)
         ↓
Fase 9:  LIBERAÇÃO DE PRODUTOS
         ↓
Fase 10: CONCLUSÃO → STATUS = 'C' (Concluída)
```

### Regra Especial de Pulo de Fase

Se origem da RNC = **"RISCOS"** ou **"OPORTUNIDADES"**, pula da Fase 3 direto para Fase 5 (Implementação), ignorando Ações Corretivas.

### Navegação Entre Fases

| Ação | Comportamento |
|------|--------------|
| **Mudar Fase** | Avança para próxima fase (FASESNCID + 1). Ao chegar na 10, conclui RNC |
| **Voltar Fase** | Retorna para fase anterior (FASESNCID - 1). Bloqueado se RNC já concluída |
| **Cancelar RNC** | Marca STATUS='C' independente da fase atual |

---

## Tabelas do Banco

### Tabela Principal

#### TGQRNC — Registro de Não Conformidade

| Campo | Tipo | Função |
|-------|------|--------|
| `RNCID` (PK) | INTEGER | ID da RNC |
| `FASESNCID` (FK) | INTEGER | Fase atual (1-10) |
| `CODPARC` | INTEGER | Código do parceiro/empresa |
| `ORIGEM` | VARCHAR | Origem da NC |
| `TIPONC` | VARCHAR | Tipo da não conformidade |
| `DATAREGISTRO` | DATETIME | Data do registro |
| `DETALHAMENTO` | HTML | Detalhes da não conformidade |
| `STATUS` | VARCHAR | P=Pendente, A=Atrasada, C=Concluída, E=Cancelada |
| `PRIORIDADE` | INTEGER | 1=Simples, 2=Prioritário, 3=Crítico |
| `DATAPREVENCERRAR` | DATE | Previsão de encerramento |
| `PERCENTUALDESVIO` | DECIMAL | Percentual de desvio |
| `REINCIDENTE` | VARCHAR | Indicador de reincidência |
| `NCVINCULADA` | INTEGER | RNC vinculada (se reincidente) |
| `NAOPROCEDENTE` | CHECKBOX | Marca como não procedente |

#### TGQFASES — Definição das Fases

| Campo | Tipo | Função |
|-------|------|--------|
| `FASESNCID` (PK) | INTEGER | ID da fase (1-10) |
| `DESCFASE` | VARCHAR | Descrição da fase |

### Tabelas por Fase

#### TGQABRANGENCIA — Abrangência (5W2H)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDABRANGENCIA` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `OQUE` | VARCHAR | O quê? |
| `COMO` | VARCHAR | Como? |
| `ONDE` | VARCHAR | Onde? |
| `QUANDO` | VARCHAR | Quando? |
| `PORQUE` | VARCHAR | Por quê? |
| `QUANTO` | VARCHAR | Quanto? |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |
| `DATAPRAZO` | DATE | Prazo |

#### TGQACOESIMEDIATAS — Ações Imediatas (Fase 2)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDACOES` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `ACAODETALHE` | HTML | Detalhes da ação |
| `DATAPRAZO` | DATE | Prazo |
| `DATAACAO` | DATE | Data da ação |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |

#### TGQCAUSARAIZ — Causa Raiz / Ishikawa (Fase 3)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDCAUSARAIZ` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `MAODEOBRA` | VARCHAR | Mão de obra |
| `MAQUINA` | VARCHAR | Máquina |
| `METODO` | VARCHAR | Método |
| `MATERIAL` | VARCHAR | Material |
| `MEDIDA` | VARCHAR | Medida |
| `MEIOAMBIENTE` | VARCHAR | Meio ambiente |
| `DATAPRAZO` | DATE | Prazo |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |

> Usa **Diagrama de Ishikawa (6M)**: Mão de Obra, Máquina, Método, Material, Medida, Meio Ambiente

#### TGQACOESCORRETIVAS — Ações Corretivas / 5W2H (Fase 4)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDACOES` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `DETALHEACAO` | HTML | Detalhes da ação |
| `OQUE` | VARCHAR | O quê? |
| `COMO` | VARCHAR | Como? |
| `ONDE` | VARCHAR | Onde? |
| `QUANDO` | VARCHAR | Quando? |
| `PORQUE` | VARCHAR | Por quê? |
| `QUANTO` | VARCHAR | Quanto? |
| `DATAPRAZO` | DATE | Prazo |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |

#### TGQIMPLEMENTACAO — Implementação (Fase 5)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDIMP` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `DESCRICAO` | VARCHAR | Descrição |
| `DTIMPL` | DATE | Data da implementação |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |

#### TGQREVISAODOC — Revisão de Documentos (Fase 7)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDREVISAO` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `DESCRICAO` | VARCHAR | Descrição |
| `VALIDADO` | CHECKBOX | Aprovado |
| `REPROVADO` | CHECKBOX | Reprovado |
| `DATAREVISAO` | DATE | Data da revisão |

#### TGQRISCOSOPORT — Riscos e Oportunidades / 5W2H (Fase 8)

| Campo | Tipo | Função |
|-------|------|--------|
| `IDACOES` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `OQUE` | VARCHAR | O quê? |
| `COMO` | VARCHAR | Como? |
| `ONDE` | VARCHAR | Onde? |
| `QUANDO` | VARCHAR | Quando? |
| `PORQUE` | VARCHAR | Por quê? |
| `QUANTO` | VARCHAR | Quanto? |
| `DATAPRAZO` | DATE | Prazo |
| `NAOSEAPLICA` | CHECKBOX | Não se aplica |

### Tabelas de Suporte

#### TGQRESPRNC — Responsáveis por Fase

| Campo | Tipo | Função |
|-------|------|--------|
| `IDRESP` (PK) | INTEGER | ID |
| `RNCID` (FK) | INTEGER | RNC vinculada |
| `FASESNCID` (FK) | INTEGER | Fase |
| `CODPARC` | INTEGER | Parceiro responsável |
| `CODUSU` | INTEGER | Usuário responsável |
| `ENVIAREMAIL` | VARCHAR | Flag enviar email (S/N) |

**Instâncias por fase:**

| Instância | Fase |
|-----------|------|
| ResponsavelRNC | 1 — Registro |
| RespAcaoImediata | 2 — Ações Imediatas |
| RespCausaRaiz | 3 — Causa Raiz |
| RespAcoesCorretivas | 4 — Ações Corretivas |
| RespImplementacao | 5 — Implementação |
| RespVerifEficacia | 6 — Verificação Eficácia |
| RespRevisao | 7 — Revisão Documentos |
| RespRiscosOportunidades | 8 — Riscos/Oportunidades |
| RespLiberacao | 9 — Liberação |

#### TGQQUEMRNC — Participantes da Abrangência

| Campo | Tipo | Função |
|-------|------|--------|
| `ID` (PK) | INTEGER | ID |
| `IDABRANGENCIA` (FK) | INTEGER | Abrangência vinculada |
| `CODPARC` | INTEGER | Parceiro envolvido |

---

## Componentes Java

### Action Buttons — Mudar Fase (9 classes)

| Classe | Fase |
|--------|------|
| `MudarFaseRegistroNC` | 1 → 2 |
| `MudarFaseAcoesImediatas` | 2 → 3 |
| `MudarFaseCausaRaiz` | 3 → 4 (ou 5 se Riscos/Oportunidades) |
| `MudarFaseAcoesCorretivas` | 4 → 5 |
| `MudarFaseImplementacao` | 5 → 6 |
| `MudarFaseVerificacaoEficacia` | 6 → 7 |
| `MudarFaseRevisaoDocumentos` | 7 → 8 |
| `MudarFaseRiscosOportunidades` | 8 → 9 |
| `MudarFaseLiberacao` | 9 → 10 (Conclusão) |

Todos chamam `AtualizaFases.atualizaFaseRnc(rncId, origem)`.

### Action Buttons — Voltar Fase (8 classes)

| Classe | Fase |
|--------|------|
| `VoltarFaseAbrangencia` | → fase anterior |
| `VoltarFaseAcoesImediatas` | → fase anterior |
| `VoltarFaseCausaRaiz` | → fase anterior |
| `VoltarFaseAcoesCorretivas` | → fase anterior |
| `VoltarFaseImplementacao` | → fase anterior |
| `VoltarFaseVerificacaoEficacia` | → fase anterior |
| `VoltarFaseRevisaoDocumentos` | → fase anterior |
| `VoltarFaseRiscosOportunidades` | → fase anterior |
| `VoltarFaseLiberacao` | → fase anterior |

Todos chamam `AtualizaFases.retornaFaseRnc(rncId, status)`. Bloqueia se STATUS='C'.

### Action Buttons — Cancelar RNC (9 classes)

Cancelamento disponível em todas as fases. Chama `AtualizaFases.concluiFaseRnc(rncId)` → STATUS='C'.

### Action Buttons — Enviar Notificação (9 classes)

| Classe | Fase | Código Origem |
|--------|------|---------------|
| `EnviarNotificacaoResponsavelRNC` | 1 | A |
| `EnviarNotificacaoRespAcaoImediata` | 2 | B |
| `EnviarNotificacaoRespCausaRaiz` | 3 | C |
| `EnviarNotificacaoRespAcoesCorretivas` | 4 | D |
| `EnviarNotificacaoRespImplementacao` | 5 | E |
| `EnviarNotificacaoRespVerifEficacia` | 6 | F |
| `EnviarNotificacaoRespRevisao` | 7 | G |
| `EnviarNotificacaoRespRiscosOportunidades` | 8 | H |
| `EnviarNotificacaoRespLiberacao` | 9 | I |

Verifica flag `ENVIAREMAIL='S'` antes de enviar. Chama `NotificacaoAcoes.enviaNotificacao()`.

### Services

| Classe | Função |
|--------|--------|
| `AtualizaFases` | Avançar, retornar e concluir fases da RNC |
| `AtualizaFaseNaoConformidade` | Avanço simples de fase (FASESNCID + 1) |
| `NotificacaoAcoes` | Monta assunto/mensagem por fase e envia email via fila |
| `RegistroNaoConformeFornecedor` | Notifica fornecedor sobre RNC vinculada |
| `AtualizaDocumentos` | Gerencia documentos vinculados à RNC |

### Listeners

| Classe | Função |
|--------|--------|
| `QualidadeListener` | Stub (implementação vazia) |
| `ValidaOrigemFornecedorListener` | Valida origem quando RNC é de fornecedor |

---

## Métodos de Análise

### Diagrama de Ishikawa (6M) — Fase 3: Causa Raiz

```
         Mão de Obra    Máquina    Método
              \            |          /
               \           |         /
                +----- EFEITO ------+
               /           |         \
              /            |          \
         Material       Medida    Meio Ambiente
```

Campos na tabela `TGQCAUSARAIZ`: MAODEOBRA, MAQUINA, METODO, MATERIAL, MEDIDA, MEIOAMBIENTE

### 5W2H — Fases 4 e 8

| Pergunta | Campo |
|----------|-------|
| What? (O quê?) | OQUE |
| How? (Como?) | COMO |
| Where? (Onde?) | ONDE |
| When? (Quando?) | QUANDO |
| Why? (Por quê?) | PORQUE |
| How much? (Quanto?) | QUANTO |

Usado em `TGQACOESCORRETIVAS`, `TGQRISCOSOPORT` e `TGQABRANGENCIA`.

---

## Notificações por Email

### Fluxo

```
1. Usuário clica "Enviar Notificação" na fase
2. Verifica ENVIAREMAIL = 'S' em TGQRESPRNC
3. NotificacaoAcoes busca email do responsável em TGFPAR.EMAIL
4. Monta assunto baseado no código de origem (A-I)
5. EnviarEmailUtil.EnviarNotificacaoAcoes() insere na fila
6. MSDFilaMensagem processa envio (HTML, max 3 tentativas)
```

### Assuntos por Fase

| Código | Assunto do Email |
|--------|-----------------|
| A | Responsável pela RNC |
| B | Responsável por Ações Imediatas |
| C | Responsável por Causa Raiz |
| D | Responsável por Ações Corretivas |
| E | Responsável por Implementação |
| F | Responsável por Verificação de Eficácia |
| G | Responsável por Validação/Revisão |
| H | Responsável por Riscos e Oportunidades |
| I | Responsável por Liberação de Produtos |

---

## Fluxo de Dados Completo

```
1. REGISTRO
   Usuário → Tela RNC → INSERT TGQRNC (STATUS='P', FASESNCID=1)
   → INSERT TGQRESPRNC (responsáveis por fase)

2. CADA FASE
   Usuário preenche dados da fase → INSERT na tabela específica
   → Pode enviar notificação ao responsável
   → Clica "Mudar Fase"
   → AtualizaFases.atualizaFaseRnc()
   → UPDATE TGQRNC.FASESNCID = FASESNCID + 1

3. VOLTAR FASE (se necessário)
   → AtualizaFases.retornaFaseRnc()
   → Valida STATUS != 'C'
   → UPDATE TGQRNC.FASESNCID = FASESNCID - 1

4. CANCELAR (qualquer fase)
   → AtualizaFases.concluiFaseRnc()
   → UPDATE TGQRNC.STATUS = 'C'

5. CONCLUSÃO (fase 10)
   → Automático ao avançar da fase 9
   → UPDATE TGQRNC.STATUS = 'C'
```

---

## Arquivos do Módulo

```
qualitymanagement-model/src/main/java/br/com/le/addon/qualitymanagement/
├── actionButtons/
│   ├── mudarfase/
│   │   ├── AtualizaFaseBt.java
│   │   ├── MudarFaseRegistroNC.java
│   │   ├── MudarFaseAbrangencia.java
│   │   ├── MudarFaseAcoesImediatas.java
│   │   ├── MudarFaseCausaRaiz.java
│   │   ├── MudarFaseAcoesCorretivas.java
│   │   ├── MudarFaseImplementacao.java
│   │   ├── MudarFaseVerificacaoEficacia.java
│   │   ├── MudarFaseRevisaoDocumentos.java
│   │   ├── MudarFaseRiscosOportunidades.java
│   │   └── MudarFaseLiberacao.java
│   ├── voltarfase/
│   │   ├── VoltarFaseAbrangencia.java
│   │   ├── VoltarFaseAcoesImediatas.java
│   │   ├── VoltarFaseCausaRaiz.java
│   │   ├── VoltarFaseAcoesCorretivas.java
│   │   ├── VoltarFaseImplementacao.java
│   │   ├── VoltarFaseVerificacaoEficacia.java
│   │   ├── VoltarFaseRevisaoDocumentos.java
│   │   ├── VoltarFaseRiscosOportunidades.java
│   │   └── VoltarFaseLiberacao.java
│   ├── cancelarrnc/
│   │   ├── CancelarRncRegistro.java
│   │   ├── CancelarRncAbrangencia.java
│   │   ├── CancelarRncAcoesImediatas.java
│   │   ├── CancelarRncAcoesCorretivas.java
│   │   ├── CancelarRncCausaRaiz.java
│   │   ├── CancelarRncImplementacao.java
│   │   ├── CancelarRncVerificacaoEficacia.java
│   │   ├── CancelarRncRevisaoDocumentos.java
│   │   ├── CancelarRncRiscosOportunidades.java
│   │   └── CancelarRncLiberacao.java
│   ├── enviarnotificacao/
│   │   ├── EnviarNotificacaoResponsavelRNC.java
│   │   ├── EnviarNotificacaoRespAcaoImediata.java
│   │   ├── EnviarNotificacaoRespCausaRaiz.java
│   │   ├── EnviarNotificacaoRespAcoesCorretivas.java
│   │   ├── EnviarNotificacaoRespImplementacao.java
│   │   ├── EnviarNotificacaoRespVerifEficacia.java
│   │   ├── EnviarNotificacaoRespRevisao.java
│   │   ├── EnviarNotificacaoRespRiscosOportunidades.java
│   │   └── EnviarNotificacaoRespLiberacao.java
│   └── documentos/
│       ├── AprovacaoDocumentos.java
│       └── MarcarDocumentoRevisado.java
├── services/
│   ├── AtualizaFases.java
│   ├── AtualizaFaseNaoConformidade.java
│   ├── NotificacaoAcoes.java
│   ├── RegistroNaoConformeFornecedor.java
│   └── AtualizaDocumentos.java
├── listeners/
│   ├── QualidadeListener.java
│   └── ValidaOrigemFornecedorListener.java
└── utils/
    ├── EnviarEmailUtil.java
    ├── ValidaNumero.java
    └── AnexoEmail.java

datadictionary/
├── TABLE_TGQRNC.xml
├── TABLE_TGQFASES.xml
├── TABLE_TGQRESPRNC.xml
├── TABLE_TGQQUEMRNC.xml
├── TABLE_TGQABRANGENCIA.xml
├── TABLE_TGQACOESIMEDIATAS.xml
├── TABLE_TGQCAUSARAIZ.xml
├── TABLE_TGQACOESCORRETIVAS.xml
├── TABLE_TGQIMPLEMENTACAO.xml
├── TABLE_TGQREVISAODOC.xml
├── TABLE_TGQRISCOSOPORT.xml
└── TABLE_TGQRNCDET.xml
```
