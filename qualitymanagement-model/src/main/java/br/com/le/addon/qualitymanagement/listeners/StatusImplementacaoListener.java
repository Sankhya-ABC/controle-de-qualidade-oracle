package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.utils.StatusFaseRncUtil;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.studio.annotations.Listener;

@Listener(instanceNames = {"DadosImplementacao"})
public class StatusImplementacaoListener extends PersistenceEventAdapter {

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.marcarEmAndamento(event);
    }
}
