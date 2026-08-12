package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.studio.annotations.Listener;

/**
 * Ao incluir um novo registro nas fases da RNC (Causa Raiz, Acoes Imediatas,
 * Abrangencia, Acoes Corretivas, Implementacao), define STATUS = Em Andamento.
 * Nao atua em alteracao (update), somente em inclusao (insert).
 */
@Listener(instanceNames = {
    "DadosCausaRaiz",
    "DadosAcoesImediatas",
    "DadosAbrangencia",
    "DadosAcoesCorretivas",
    "DadosImplementacao"
})
public class StatusFaseRncListener extends PersistenceEventAdapter {

    private static final String STATUS_EM_ANDAMENTO = "E";

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        ((DynamicVO) event.getVo()).setProperty("STATUS", STATUS_EM_ANDAMENTO);
    }
}
