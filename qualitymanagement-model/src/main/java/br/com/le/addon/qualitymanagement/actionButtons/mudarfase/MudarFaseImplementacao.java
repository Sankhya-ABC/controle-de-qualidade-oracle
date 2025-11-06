package br.com.le.addon.qualitymanagement.actionButtons.mudarfase;

import br.com.le.addon.qualitymanagement.services.AtualizaFases;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

/**
 * Botão de ação "Mudar Fase" para 08. Implementação
 */
@ActionButton(
    description = "Mudar Fase",
    instanceName = "Implementacao",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class MudarFaseImplementacao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        byte b;
        int i;
        Registro[] arrayOfRegistro;
        for (i = (arrayOfRegistro = ctx.getLinhas()).length, b = 0; b < i; ) {
            Registro linha = arrayOfRegistro[b];
            String rncid = linha.getCampo("RNCID").toString();
            String origem = linha.getCampo("ORIGEM").toString();
            try {
                AtualizaFases.atualizaFaseRnc(rncid, origem);
                ctx.setMensagemRetorno("Fase atualizada!");
            } catch (Exception e) {
                e.getMessage();
            }
            b++;
        }
    }
}

