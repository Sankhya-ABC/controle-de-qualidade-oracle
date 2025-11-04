package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.le.addon.qualitymanagement.services.Validacao;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;

public class ValidacaoFornecSC implements ScheduledAction {
    public void onTime(ScheduledActionContext ctx) {
        try {
            Validacao.ValidacaoFornecedores(BigDecimal.ZERO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
