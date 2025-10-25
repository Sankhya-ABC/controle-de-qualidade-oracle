package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

/**
 * Exemplo de Bot�o de A��o no Addon.*

 * Para mais informa��es sobre como criar e utilizar Action Button,
 consulte a documenta��o oficial da Sankhya no link abaixo:
 <a href="https://developer.sankhya.com.br/docs/05_action_button">Bot�o de A��o</a>
  */

@ActionButton(
    description = "Iniciar Atendimento",
    instanceName = "TMP_Atendimento",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM)
public class ExemploActionButton implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhasSelecionadas = contexto.getLinhas(); //manipule os registros (linhas selecionadas)
        throw new UnsupportedOperationException("Implementar l�gica para o Bot�o de A��o para as linhas selecionadas: " + linhasSelecionadas.length + " linha(s).");
    }
}
