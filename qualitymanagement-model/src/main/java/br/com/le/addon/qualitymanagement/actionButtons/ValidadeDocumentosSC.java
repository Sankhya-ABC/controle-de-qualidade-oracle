package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.le.addon.qualitymanagement.services.Validacao;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

public class ValidadeDocumentosSC implements ScheduledAction {
    public void onTime(ScheduledActionContext ctx) {
        try {
            Validacao.ValidacaoDocumentos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
