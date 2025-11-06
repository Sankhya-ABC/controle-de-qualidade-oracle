package br.com.le.addon.qualitymanagement.actionButtons.fornecedores;

import br.com.le.addon.qualitymanagement.services.QuestionarioFornecedor;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

/**
 * Botão de ação "Enviar Questionario ao Fornecedor" para Qualificação de Fornecedor
 */
@ActionButton(
    description = "Enviar Questionario ao Fornecedor",
    instanceName = "QualificacaoFornecedor",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class EnviarQuestionarioFornecedor implements AcaoRotinaJava {

    @Override
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

