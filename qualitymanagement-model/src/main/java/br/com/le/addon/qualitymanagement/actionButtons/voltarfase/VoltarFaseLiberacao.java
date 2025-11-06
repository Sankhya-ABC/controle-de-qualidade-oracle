package br.com.le.addon.qualitymanagement.actionButtons.voltarfase;

import br.com.le.addon.qualitymanagement.services.AtualizaFases;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

/**
 * Botão de ação "Voltar Fase" para 09. Liberação de Produto
 */
@ActionButton(
    description = "Voltar Fase",
    instanceName = "Liberacao",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class VoltarFaseLiberacao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String rncid = linha.getCampo("RNCID").toString();
            String status = linha.getCampo("STATUS").toString();
            try {
                AtualizaFases.retornaFaseRnc(rncid, status);
                ctx.setMensagemRetorno("Fase atualizada!");
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}

