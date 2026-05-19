package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.services.CalculaPontuacaoQualificacao;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.studio.annotations.Listener;

/**
 * Recalcula PONTUACAO e RESULTADOIQF automaticamente ao salvar respostas,
 * usando a mesma regra do botao Calcula Pontuacao.
 */
@Listener(instanceNames = {"RespostaFornecedor"})
public class QualificacaoListener extends PersistenceEventAdapter {

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        CalculaPontuacaoQualificacao.recalcularNoEvento((DynamicVO) event.getVo(), false);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        CalculaPontuacaoQualificacao.recalcularNoEvento((DynamicVO) event.getVo(), false);
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        CalculaPontuacaoQualificacao.recalcularNoEvento((DynamicVO) event.getVo(), true);
    }
}
