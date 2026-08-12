package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.utils.StatusFaseRncUtil;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.studio.annotations.Listener;

@Listener(instanceNames = {"DadosAbrangencia"})
public class StatusAbrangenciaListener extends PersistenceEventAdapter {

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.marcarEmAndamentoAntesInserir(event);
    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.confirmarEmAndamentoAposInserir(event, "TGQABRANGENCIA", "IDABRANGENCIA");
    }
}
