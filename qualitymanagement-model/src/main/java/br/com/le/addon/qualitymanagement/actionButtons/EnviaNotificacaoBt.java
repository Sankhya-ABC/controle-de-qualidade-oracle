package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.NotificacaoAcoes;

public class EnviaNotificacaoBt implements AcaoRotinaJava {
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
                    ctx.setMensagemRetorno("Notificaenviada!");
                } else {
                    ctx.setMensagemRetorno("Notificanenviada! Verificar o cadastro de email.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            b++;
        }
    }
}
