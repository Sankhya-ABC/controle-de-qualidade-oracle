package br.com.le.addon.qualitymanagement.actionButtons.enviarnotificacao;

import br.com.le.addon.qualitymanagement.services.NotificacaoAcoes;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

/**
 * Botão "Enviar Notificacao" para RespLiberacao (09. Liberação de Produto)
 */
@ActionButton(
    description = "Enviar Notificacao",
    instanceName = "RespLiberacao",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class EnviarNotificacaoRespLiberacao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        String retorno = null;
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String rncId = linha.getCampo("RNCID").toString();
            String codParc = linha.getCampo("CODPARC").toString();
            String enviarEmail = linha.getCampo("ENVIAREMAIL").toString();
            try {
                retorno = NotificacaoAcoes.enviaNotificacao(rncId, codParc, enviarEmail);
                if (retorno != null) {
                    ctx.setMensagemRetorno("Notificacao enviada!");
                } else {
                    ctx.setMensagemRetorno("Notificacao nao enviada! Verificar o cadastro de email.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            b++;
        }
    }
}

