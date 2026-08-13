package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.utils.StatusFaseRncUtil;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.studio.annotations.Listener;

@Listener(instanceNames = {"DadosAbrangencia"})
public class StatusAbrangenciaListener extends PersistenceEventAdapter {

    private static final String TABELA = "TGQABRANGENCIA";
    private static final String CAMPO_ID = "IDABRANGENCIA";

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.marcarEmAndamentoAntesInserir(event);
    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.confirmarEmAndamentoAposInserir(event, TABELA, CAMPO_ID);
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.marcarConcluidaAntesAlterar(event);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        StatusFaseRncUtil.confirmarConcluidaAposAlterar(event, TABELA, CAMPO_ID);
    }
}
