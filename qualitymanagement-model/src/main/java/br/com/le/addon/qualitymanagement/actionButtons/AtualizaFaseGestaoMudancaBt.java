package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.AtualizaFases;

public class AtualizaFaseGestaoMudancaBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String idGestao = linha.getCampo("IDGESTAO").toString();
            try {
                AtualizaFases.atualizaFaseGestao(idGestao);
                ctx.setMensagemRetorno("Fase atualizada!");
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}
