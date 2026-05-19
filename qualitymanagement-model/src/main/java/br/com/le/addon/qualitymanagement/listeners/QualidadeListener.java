package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.studio.annotations.Listener;

@Listener(instanceNames = {"ConfigQualidade"})
public class QualidadeListener extends PersistenceEventAdapter {
    public void afterDelete(PersistenceEvent event) throws Exception {}

    public void afterInsert(PersistenceEvent event) throws Exception {}

    public void afterUpdate(PersistenceEvent event) throws Exception {
        getEtiqueta(event);
    }

    public void beforeCommit(TransactionContext arg0) throws Exception {}

    public void beforeDelete(PersistenceEvent event) throws Exception {}

    public void beforeInsert(PersistenceEvent event) throws Exception {}

    public void beforeUpdate(PersistenceEvent event) throws Exception {}

    public static void getEtiqueta(PersistenceEvent event) throws Exception {}
}
