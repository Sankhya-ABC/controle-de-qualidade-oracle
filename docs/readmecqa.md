# Controle de Qualidade — Addon Sankhya

## Sobre o Projeto

Addon de **Controle de Qualidade** para plataforma Sankhya. Sistema completo de gestão da qualidade com módulos de Não Conformidade (RNC), Qualificação de Fornecedores, Gestão de Documentos, Gestão de Mudanças e Configurações.

| Propriedade | Valor |
|-------------|-------|
| **Group ID** | `br.com.le.addon.qualitymanagement` |
| **App Key** | `4df447b5-c512-457c-abba-bb60ffaf9104` |
| **Parceiro** | BP - ABC |
| **Plataforma Mínima** | 4.28 |
| **Banco de Dados** | Oracle |
| **Versão** | Auto-detectada via Git tags |

---

## Estrutura do Projeto

```
controle-de-qualidade-oracle/
├── qualitymanagement-model/     # Backend (Java: services, listeners, action buttons)
├── qualitymanagement-vc/        # Frontend (Visual Components)
├── datadictionary/              # 47 XMLs de definição de tabelas e menus
├── dbscripts/                   # Migrações (V1.xml, V2.xml)
├── docs/                        # Documentação dos módulos
├── build.gradle                 # Configuração de build
└── settings.gradle              # Definição dos módulos Gradle
```

### Pacotes Java (77 arquivos)

```
br.com.le.addon.qualitymanagement/
├── actionButtons/
│   ├── cancelarrnc/        (9 classes)  — Cancelamento de RNC por fase
│   ├── documentos/         (2 classes)  — Aprovação e revisão de documentos
│   ├── enviarnotificacao/  (9 classes)  — Notificações por email por fase
│   ├── fornecedores/       (1 classe)   — Envio de questionário ao fornecedor
│   ├── mudarfase/          (12 classes) — Avanço de fase (RNC + GM)
│   └── voltarfase/         (9 classes)  — Retorno de fase
├── businessRules/
├── callbacks/
├── dto/
├── jobs/
├── listeners/              (3 classes)  — Eventos automáticos
├── model/                  (8 classes)  — Entidades
├── services/               (8 classes)  — Lógica de negócio
└── utils/                  (3 classes)  — Utilitários (email, validação)
```

---

## Módulos

### 1. Registro de Não Conformidade (RNC)

Workflow de 10 fases para registro, análise e resolução de não conformidades.

**Fases:** Registro → Ações Imediatas → Causa Raiz (Ishikawa 6M) → Abrangência → Ações Corretivas (5W2H) → Revisão de Documentos → Riscos/Oportunidades → Implementação → Liberação de Produto → Verificação de Eficácia

**Funcionalidades:**
- Transição de fases (avançar/voltar/cancelar)
- Atribuição de responsáveis por fase
- Notificações por email em cada fase
- Prioridades com SLA (Simples, Prioritário, Crítico)
- Análise Ishikawa (6M) para causa raiz
- Análise 5W2H para ações corretivas e riscos

**Documentação:** [docs/RNC.md](RNC.md)

---

### 2. Qualificação de Fornecedores

Avaliação e qualificação de fornecedores via questionários digitais com pontuação automática.

**Fluxo:** Criar Questionário → Vincular ao Fornecedor → Enviar por Email → Fornecedor Responde → Pontuação Automática → Classificação (A/B/T)

**Funcionalidades:**
- Envio de questionários via URL com parâmetros Base64
- Tipos de pergunta: Texto, Numérico, Combo, Radio, Checkbox
- Cálculo automático de pontuação (Score = Total/Perguntas × 100)
- Classificação: A (80-100), B (50-79), T (≤49 — Reprovado)
- Monitoramento de validade de qualificações e certificados
- Notificações automáticas de vencimento

**Documentação:** [docs/QualificacaoFornecedor.md](QualificacaoFornecedor.md)

---

### 3. Gestão de Documentos

Controle de documentos com workflow de revisão/aprovação, versionamento e controle de impressão.

**Ciclo:** Pendente → Revisado → Aprovado

**Funcionalidades:**
- Versionamento de arquivos (BLOB)
- Workflow P → R → A com validação
- Histórico automático de status via trigger
- Controle de impressão (auditoria)
- Permissões por documento e por usuário (leitura, revisão, impressão, aprovação)
- Monitoramento agendado de validade de documentos
- Integração com RNC Fase 7 (Revisão de Documentos)

**Documentação:** [docs/Documentos.md](Documentos.md)

---

### 4. Gestão de Mudanças

Workflow de 5 fases para gerenciar mudanças organizacionais com questionários e aprovações.

**Fases:** Registro → Questionário de Avaliação → Ações (5W+H) + Aprovação → Avaliação de Riscos → Aprovação Final

**Funcionalidades:**
- Auto-populção de questionários nas fases 2, 4 e 5
- Duas rodadas de aprovação (fase 3 e fase 5)
- Assinatura eletrônica nas aprovações
- Definição de ações com análise 5W+H
- Atribuição de responsáveis por ação

**Documentação:** [docs/GestaoMudancas.md](GestaoMudancas.md)

---

### 5. Configurações e Parametrização

Núcleo de parametrização do sistema.

**Configura:**
- Controle de fornecedores (bloqueio de compras, notificações, prazos)
- Controle de documentos (alertas de vencimento, emails)
- Cadastros gerais (origens, tipos, processos RNC, processos GM)
- Fases de RNC (10) e Gestão de Mudanças (5)
- Critérios, índices e faixas de pontuação para qualificação
- Prioridades com SLA
- Permissões de documentos por usuário
- Parâmetros do sistema (URLs, templates de email, credenciais)

**Documentação:** [docs/Configuracoes.md](Configuracoes.md)

---

## Menu do Sistema

```
Controle de Qualidade
├── Gestão de Não Conformidades
│   ├── 01. Registro de Não Conformidade
│   ├── 02. Ações Imediatas
│   ├── 03. Causa Raiz
│   ├── 04. Abrangência
│   ├── 05. Ações Corretivas
│   ├── 06. Revisão de Documentos
│   ├── 07. Riscos e Oportunidades
│   ├── 08. Implementação
│   ├── 09. Liberação de Produto
│   └── 10. Verificação de Eficácia
│
├── Gestão de Fornecedores
│   └── Qualificação de Fornecedor
│
├── Gestão de Documentos
│   └── Controle de Documentos
│
├── Gestão de Mudanças
│   ├── 1. Cadastro
│   ├── 2. Questionário de Avaliação
│   ├── 3. Ações
│   ├── 4. Avaliação de Riscos
│   ├── 5. Aprovação Final
│   └── Consulta Gestão
│
├── Cadastro de Questionários
│
├── Configuração e Parametrização
│
└── Cadastros Gerais
```

---

## Tabelas do Banco (Prefixo TGQ)

### RNC

| Tabela | Função |
|--------|--------|
| TGQRNC | Registro principal de não conformidade |
| TGQRNCDET | Detalhes da RNC |
| TGQFASES | Definição das 10 fases |
| TGQRESPRNC | Responsáveis por fase |
| TGQQUEMRNC | Participantes da abrangência |
| TGQABRANGENCIA | Abrangência (5W2H) |
| TGQACOESIMEDIATAS | Ações imediatas |
| TGQCAUSARAIZ | Causa raiz (Ishikawa 6M) |
| TGQACOESCORRETIVAS | Ações corretivas (5W2H) |
| TGQIMPLEMENTACAO | Implementação |
| TGQREVISAODOC | Revisão de documentos |
| TGQRISCOSOPORT | Riscos e oportunidades |
| TGQLIBERPROD | Liberação de produto |
| TGQVEREFICACIA | Verificação de eficácia |

### Qualificação de Fornecedores

| Tabela | Função |
|--------|--------|
| TGQQUALIFFORN | Registro de qualificação |
| TGQQUESTQUALIF | Cadastro de questionários |
| TGQPERGQUEST | Perguntas do questionário |
| TGQPERGQUESTOPC | Opções de resposta |
| TGQQUALIFRESP | Respostas do fornecedor |
| TGQARQQUESTFORN | Arquivos do questionário |
| TGQCERTFORN | Certificados do fornecedor |

### Gestão de Documentos

| Tabela | Função |
|--------|--------|
| TGQCONTDOC | Controle de documentos |
| TGQARQDOC | Arquivos versionados |
| TGQHISTSTATUS | Histórico de status |
| TGQCTRLIMPRESSOS | Controle de impressão |
| TGQPERMDOC | Permissões por documento |
| TGQPERMISSAOUSUDOC | Permissões por usuário |

### Gestão de Mudanças

| Tabela | Função |
|--------|--------|
| TGQGESTAOMUDANCA | Registro de mudança |
| TGQFASESGM | Definição das 5 fases |
| TGQACOESMUDANCA | Ações da mudança (5W+H) |
| TGQQUEMGESTAOMUDANCA | Responsáveis por ação |
| TGQQUESTAVALIACAO | Questionários de avaliação |
| TGQPERGUNTASAVALIACAO | Respostas dos questionários |
| TGQRESPGESTAOMUDANCA | Aprovações (AM/AF) |

### Configuração

| Tabela | Função |
|--------|--------|
| TGQCONFIG | Configurações gerais |
| TGQCADASTROS | Cadastros gerais (origem, tipo, processo) |
| TGQPONTUACAO | Critérios de pontuação |
| TGQIQF | Índice de qualificação |
| TGQCRITERIOQF | Faixas de qualificação |
| TGQFASESORIGEM | Fases por origem |
| TGQPRIORIDADE | Prioridades com SLA |

---

## Ações Agendadas

| Classe | Função |
|--------|--------|
| `ValidacaoFornecSC` | Verifica qualificações e certificados vencendo |
| `ValidadeDocumentosSC` | Verifica documentos com validade expirando |

---

## Tecnologias

- **Linguagem:** Java 8
- **Build:** Gradle + Plugin Sankhya Addon Studio
- **Processamento:** Kotlin KSP (geração de código)
- **Banco:** Oracle (com suporte SQL Server nos scripts gerados)
- **Plataforma:** Sankhya ERP (WildFly)
- **Email:** Fila MSDFilaMensagem (HTML, max 3 tentativas)

---

## Build

```bash
# Com JAVA_HOME apontando para JDK (não JRE)
export JAVA_HOME="C:/Program Files/Eclipse Foundation/jdk-8.0.302.8-hotspot"
./gradlew build
```

---

## Documentação dos Módulos

| Documento | Módulo |
|-----------|--------|
| [RNC.md](RNC.md) | Registro de Não Conformidade |
| [QualificacaoFornecedor.md](QualificacaoFornecedor.md) | Qualificação de Fornecedores |
| [Documentos.md](Documentos.md) | Gestão de Documentos |
| [GestaoMudancas.md](GestaoMudancas.md) | Gestão de Mudanças |
| [Configuracoes.md](Configuracoes.md) | Configurações e Parametrização |
