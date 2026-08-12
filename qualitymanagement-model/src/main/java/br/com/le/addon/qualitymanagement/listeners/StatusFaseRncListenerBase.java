package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Logica compartilhada pelos listeners de fase da RNC: ao incluir um novo
 * registro, define STATUS = Em Andamento. Nao atua em alteracao (update).
 */
abstract class StatusFaseRncListenerBase extends PersistenceEventAdapter {

    private static final String STATUS_EM_ANDAMENTO = "E";

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        ((DynamicVO) event.getVo()).setProperty("STATUS", STATUS_EM_ANDAMENTO);
    }
}
