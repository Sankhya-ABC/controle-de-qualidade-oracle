# Job: notificacao de vencimento (qualificacao fornecedor)

## Classe

`br.com.le.addon.qualitymanagement.jobs.NotificacaoVencimentoQualificacaoJob`

- **Frequencia:** `&10000` (10 segundos)
- **Servico:** `NotificacaoVencimentoQualifJobSP`

## Fluxo

1. Consulta `VW_VENC_QUALIF_FORN` onde `ENVIAR_NOTIFICACAO = 'S'`
2. Para cada registro, verifica se ja foi notificado hoje (`TGQLOGNOTIFVENC`)
3. Enfileira e-mail HTML (mesma fila `MSDFilaMensagem` do questionario):
   - **Fornecedor:** `EMAIL_FORNECEDOR` (campo `EMAILQUESTIONARIO` da TGFPAR)
   - **Empresa:** `EMAIL_NOTIFICACAO_EMPRESA` (campo `EMAILNOTIFYQF` da TGQCONFIG)
4. Grava log para nao reenviar no mesmo dia (evita duplicidade com job a cada 10s)

## Parametros Sankhya (HTML)

| Parametro | Quando usar |
|-----------|-------------|
| `HTMLEMAILVENC` | `TIPO_NOTIFICACAO = AVISO_VENCIMENTO` (proximo do vencimento) |
| `HTMLEMAILVENCHOJE` | `TIPO_NOTIFICACAO = VENCIMENTO_HOJE` (vence hoje) |

Placeholders: `{NOME_DOCUMENTO}`, `{DATA_VENCIMENTO}`, `{DIAS_RESTANTES}` (ou formato `[NOME_DOCUMENTO]` etc.).

Se o parametro estiver vazio, o codigo usa template padrao embutido.

## Deploy

1. Executar scripts V3 (view + tabela `TGQLOGNOTIFVENC`)
2. Publicar addon e habilitar o job no modulo Java / agendador Sankhya
3. Configurar `HTMLEMAILVENC` e `HTMLEMAILVENCHOJE` em homologacao

## Observacao

Em producao, avalie aumentar o intervalo do job (ex.: `&3600000` = 1 hora). Intervalo de 10 segundos e indicado para testes; o log diario impede reenvio repetido no mesmo dia.
