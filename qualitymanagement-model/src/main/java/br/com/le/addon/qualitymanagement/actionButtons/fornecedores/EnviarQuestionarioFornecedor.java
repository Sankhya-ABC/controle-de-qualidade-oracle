package br.com.le.addon.qualitymanagement.actionButtons.fornecedores;

import br.com.le.addon.qualitymanagement.services.QuestionarioFornecedor;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

@ActionButton(
    description = "Enviar Questionario ao Fornecedor",
    instanceName = "QualificacaoFornecedor",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM
)
public class EnviarQuestionarioFornecedor implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();

        if (linhas == null || linhas.length == 0) {
            ctx.setMensagemRetorno("Nenhum registro selecionado.");
            return;
        }

        for (Registro linha : linhas) {
            String idQuest = getCampoObrigatorio(linha, "IDQUEST");
            String codFornec = getCampoObrigatorio(linha, "CODPARC");
            String idQualif = getCampoObrigatorio(linha, "IDQUALIF");

            System.out.println("===== BOTÃO ENVIAR QUESTIONÁRIO =====");
            System.out.println("IDQUEST: " + idQuest);
            System.out.println("CODPARC: " + codFornec);
            System.out.println("IDQUALIF: " + idQualif);
            System.out.println("====================================");

            QuestionarioFornecedor.enviaQuestionario(idQuest, codFornec, idQualif);
        }

        ctx.setMensagemRetorno("E-mail enfileirado com questionario em anexo.");
    }

    private String getCampoObrigatorio(Registro linha, String campo) throws Exception {
        Object valor = linha.getCampo(campo);

        if (valor == null || valor.toString().trim().isEmpty()) {
            throw new Exception("Campo obrigatório não informado: " + campo);
        }

        return valor.toString();
    }
}
