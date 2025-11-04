package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.le.addon.qualitymanagement.services.QuestionarioFornecedor;

public class EnviaQuestionarioFornecBt implements AcaoRotinaJava {
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String idQuest = linha.getCampo("IDQUEST").toString();
            String codFornec = linha.getCampo("CODPARC").toString();
            String idQualif = linha.getCampo("IDQUALIF").toString();
            System.out.println("idQualif: " + idQualif);
            try {
                QuestionarioFornecedor.enviaQuestionario(idQuest, codFornec, idQualif);
                ctx.setMensagemRetorno("Questionario enviado!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            b++;
        }
    }
}
