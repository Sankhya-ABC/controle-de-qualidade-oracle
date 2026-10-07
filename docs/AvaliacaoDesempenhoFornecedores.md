# Dashboard: Avaliação de Desempenho de Fornecedores

## Visão Geral

Dashboard que avalia fornecedores e transportadoras com base em dois indicadores: prazo de entrega e percentual de desvio (não conformidades). Calcula uma pontuação média combinando ambos.

## Parâmetros de Entrada

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `PERIODO` | Período (data início/fim) | Sim | Filtra notas por `DTNEG` (data de negociação) |
| `CODPARC` | Parceiro | Não | Filtra por fornecedor/transportadora específico |
| `NUNOTA` | Número Único | Não | Filtra por nota fiscal específica (apenas no grid de notas) |

## Componentes do Dashboard

### 1. Grid — Lista de Fornecedores

Exibe todos os fornecedores e transportadoras com movimentação no período, mostrando três indicadores:

| Coluna | Descrição | Cálculo |
|--------|-----------|---------|
| **% Desvio** | Média do percentual de desvio das não conformidades | `SUM(TGQRNC.PERCENTUALDESVIO) / COUNT(*)` |
| **% Entrega** | Pontuação de cumprimento de prazo de entrega | `SUM(pontos por nota) / COUNT(*)` |
| **% Pontuação Média** | Média dos dois indicadores | `(% Desvio + % Entrega) / 2` |

Ao selecionar um fornecedor neste grid, atualiza o gráfico de produtos e o grid de notas.

### 2. Gráfico de Colunas — Produto

Detalha os indicadores por produto do fornecedor selecionado:

- **Eixo X:** Nome do produto
- **Barra vermelha:** % Entrega
- **Barra azul:** % Desvio

### 3. Grid — Relação de Notas por Fornecedores

Lista individual de cada nota do fornecedor selecionado com:

| Coluna | Descrição |
|--------|-----------|
| Nº Único [Nota] | Identificador da nota |
| Cód./Nome Empresa | Empresa da nota |
| Cód./Nome Parceiro | Fornecedor/transportadora |
| Cód./Nome Produto | Produto da nota |
| % Entrega | Pontuação de entrega daquela nota |
| Não Conformidade | ID da RNC vinculada (se houver) |
| % Desvio | Percentual de desvio da RNC (100 se não houver RNC) |
| Data Não Conformidade | Data de registro da RNC |
| Data Prevista NC | Data prevista para encerramento da RNC |

## Cálculos Detalhados

### % Entrega — Pontuação por prazo

Compara a data de negociação da nota (`TGFCAB.DTNEG`) com a data de início do pedido (`TGFITE.DTINICIO`):

| Condição | Pontos | Significado |
|----------|--------|-------------|
| `DTNEG - DTINICIO ≤ 0` | **100** | Entregou no prazo ou antes |
| `DTNEG - DTINICIO ≤ 2` | **60** | Atraso de até 2 dias |
| `DTNEG - DTINICIO > 2` | **20** | Atraso maior que 2 dias |

Pontuação final: média dos pontos de todas as notas do fornecedor no período.

### % Desvio — Não conformidades

Busca na tabela `TGQRNC` (via `TGQRNCDET`) o campo `PERCENTUALDESVIO` de cada não conformidade vinculada às notas de compra do fornecedor. Calcula a média dos percentuais.

- Quando não existe RNC vinculada na nota individual, assume **100** como desvio padrão.

### % Pontuação Média

```
Se ambos indicadores > 0:
    Pontuação Média = (% Desvio + % Entrega) / 2

Se apenas um indicador > 0:
    Pontuação Média = % Desvio + % Entrega  (o outro é zero)
```

Somente fornecedores com pelo menos um indicador > 0 aparecem no dashboard.

## Fontes de Dados

### Tabelas Sankhya (ERP)

| Tabela | Uso |
|--------|-----|
| `TGFPAR` | Cadastro de parceiros (filtra `FORNECEDOR = 'S'` e `TRANSPORTADORA = 'S'`) |
| `TGFCAB` | Cabeçalho de notas (filtra `TIPMOV = 'C'` para compras, ou via `CODPARCTRANSP` para transportadoras) |
| `TGFITE` | Itens da nota (data início do pedido) |
| `TGFVAR` | Variações — vincula pedido de compra à nota de entrada |
| `TGFPRO` | Cadastro de produtos |
| `TSIEMP` | Cadastro de empresas |

### Tabelas do Addon (Qualidade)

| Tabela | Uso |
|--------|-----|
| `TGQRNC` | Registro de não conformidade (`PERCENTUALDESVIO`, `DATAREGISTRO`, `DATAPREVENCERRAR`) |
| `TGQRNCDET` | Detalhe da RNC — vincula RNC à nota/produto/sequência |

## Observações

1. **Dashboard independente do IQF** — Não utiliza dados da tabela `TGQIQF` nem `TGQQUALIFFORN`. O cálculo de pontuação é próprio do dashboard e diferente do cálculo feito pelo `QualificacaoListener`.
2. **Dois públicos avaliados** — Fornecedores (`TGFPAR.FORNECEDOR = 'S'`) e transportadoras (`TGFPAR.TRANSPORTADORA = 'S'`) são avaliados separadamente via `UNION` e aparecem juntos no resultado.
3. **Vínculo pedido-nota** — Usa tabela `TGFVAR` para rastrear qual pedido (`TIPMOV = 'O'`) originou qual nota de compra (`TIPMOV = 'C'`).

## Arquivo

- Componente: `dashboards/236_component.xml`
