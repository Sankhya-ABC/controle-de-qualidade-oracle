package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.services.CarregaPerguntasQualificacao;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.studio.annotations.Listener;

/**
 * Ao incluir QualificacaoFornecedor, cria RespostaFornecedor para cada pergunta do IDQUEST
 * (somente se ainda nao existir resposta).
 * Ao alterar IDQUEST, exclui respostas antigas e recria conforme o novo questionario.
 */
@Listener(instanceNames = {"QualificacaoFornecedor"})
public class QualificacaoFornecedorListener extends PersistenceEventAdapter {

    private static final ThreadLocal<Boolean> CARREGAR_PERGUNTAS_NO_UPDATE = new ThreadLocal<>();

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        CarregaPerguntasQualificacao.carregarAoSalvar(
            (DynamicVO) event.getVo(),
            event.getJdbcWrapper(),
            true
        );
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO oldVo = (DynamicVO) event.getOldVO();
        CARREGAR_PERGUNTAS_NO_UPDATE.set(CarregaPerguntasQualificacao.idQuestionarioAlterado(vo, oldVo));
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        try {
            if (!Boolean.TRUE.equals(CARREGAR_PERGUNTAS_NO_UPDATE.get())) {
                return;
            }
            CarregaPerguntasQualificacao.carregarAoTrocarQuestionario(
                (DynamicVO) event.getVo(),
                event.getJdbcWrapper()
            );
        } finally {
            CARREGAR_PERGUNTAS_NO_UPDATE.remove();
        }
    }
}
