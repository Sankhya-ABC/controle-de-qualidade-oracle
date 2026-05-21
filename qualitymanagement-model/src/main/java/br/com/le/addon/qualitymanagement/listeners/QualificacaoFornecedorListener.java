package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.services.CarregaPerguntasQualificacao;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.studio.annotations.Listener;

/**
 * Ao salvar QualificacaoFornecedor com Id. Questionario, cria automaticamente
 * as linhas em RespostaFornecedor para cada pergunta do questionario.
 */
@Listener(instanceNames = {"QualificacaoFornecedor"})
public class QualificacaoFornecedorListener extends PersistenceEventAdapter {

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        CarregaPerguntasQualificacao.carregarAoSalvar((DynamicVO) event.getVo());
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        CarregaPerguntasQualificacao.carregarAoSalvar((DynamicVO) event.getVo());
    }
}
