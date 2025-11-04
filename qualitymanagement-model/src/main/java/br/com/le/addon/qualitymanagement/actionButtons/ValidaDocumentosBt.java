package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.le.addon.qualitymanagement.services.Validacao;

public class ValidaDocumentosBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        try {
            Validacao.ValidacaoDocumentos();
            ctx.setMensagemRetorno("Usunotificado!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
