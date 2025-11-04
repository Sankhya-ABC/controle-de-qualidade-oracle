package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.studio.annotations.Listener;

/**
 * Exemplo de Listener customizados no Addon.*

 * Para mais informa��es sobre como criar e utilizar Listener,
 consulte a documenta��o oficial da Sankhya no link abaixo:
 * <a href="https://developer.sankhya.com.br/docs/07_listeners">Listeners: Reagindo a Eventos de Persist�ncia</a>
 */

@Listener(instanceNames = {"TMP_Atendimento"})
public class ExemploListener extends PersistenceEventAdapter {

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        throw new UnsupportedOperationException("Implementar l�gica antes da atualiza��o de Atendimento.");
    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        throw new UnsupportedOperationException("Implementar l�gica antes da exclus�o de Atendimento.");
    }

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        throw new UnsupportedOperationException("Implementar l�gica antes da inser��o de Atendimento.");
    }

}
