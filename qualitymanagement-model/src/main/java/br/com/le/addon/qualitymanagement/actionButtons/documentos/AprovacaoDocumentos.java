package br.com.le.addon.qualitymanagement.actionButtons.documentos;

import br.com.le.addon.qualitymanagement.services.AtualizaDocumentos;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

import java.math.BigDecimal;

/**
 * Botão de ação "Aprovação de Documentos" para Controle de Documentos
 */
@ActionButton(
    description = "Aprovação de Documentos",
    instanceName = "ControleDocumentos",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class AprovacaoDocumentos implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            BigDecimal documento = (BigDecimal)linha.getCampo("IDDOC");
            String status = (String)linha.getCampo("STATUS");
            try {
                if (status.equals("R")) {
                    AtualizaDocumentos.aprovaDocumento(documento);
                } else {
                    ctx.setMensagemRetorno("Apenas documentos revisados podem ser aprovados!");
                }
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}

