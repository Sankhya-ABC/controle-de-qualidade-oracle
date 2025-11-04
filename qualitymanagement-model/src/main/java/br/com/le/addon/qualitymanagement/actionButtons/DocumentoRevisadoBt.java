package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.AtualizaDocumentos;

import java.math.BigDecimal;

public class DocumentoRevisadoBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            BigDecimal documento = (BigDecimal)linha.getCampo("IDDOC");
            String status = (String)linha.getCampo("STATUS");
            try {
                if (status.equals("P")) {
                    AtualizaDocumentos.documentoRevisado(documento);
                } else {
                    ctx.setMensagemRetorno("Apenas documentos pendentes podem ser aprovados!");
                }
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}
