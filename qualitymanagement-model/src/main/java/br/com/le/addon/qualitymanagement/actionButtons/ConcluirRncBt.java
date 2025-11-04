package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.AtualizaFases;

public class ConcluirRncBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String rncid = linha.getCampo("RNCID").toString();
            try {
                AtualizaFases.concluiFaseRnc(rncid);
                ctx.setMensagemRetorno("RNC Concluida!");
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}
