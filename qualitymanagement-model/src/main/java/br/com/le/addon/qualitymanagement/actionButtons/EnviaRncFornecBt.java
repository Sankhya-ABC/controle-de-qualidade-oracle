package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.RegistroNaoConformeFornecedor;

public class EnviaRncFornecBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String idRnc = linha.getCampo("RNCID").toString();
            String codFornec = linha.getCampo("CODPARC").toString();
            String detalhamento = linha.getCampo("DETALHAMENTO").toString();
            try {
                RegistroNaoConformeFornecedor.enviaRegNaoConformidade(idRnc, codFornec, detalhamento);
                ctx.setMensagemRetorno("NConformidade enviada!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            b++;
        }
    }
}
