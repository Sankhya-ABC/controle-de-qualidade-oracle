package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Logica compartilhada pelos listeners de fase da RNC (Causa Raiz, Acoes
 * Imediatas, Abrangencia, Acoes Corretivas, Implementacao):
 * - RN001: ao incluir um novo registro, define STATUS = Em Andamento.
 * - RN002/RN005: ao marcar o checkbox CONCLUIDO = S, define STATUS = Concluida.
 *
 * Cada marcacao e feita em dois momentos: no before*, tentando popular o VO
 * antes do INSERT/UPDATE ser gerado; e no after*, com um UPDATE direto pela
 * PK, garantindo o valor independente de o framework incorporar ou nao a
 * mudanca feita no VO durante o before*.
 */
public final class StatusFaseRncUtil {

    private static final String STATUS_EM_ANDAMENTO = "E";
    private static final String STATUS_CONCLUIDA = "C";
    private static final String CONCLUIDO_SIM = "S";

    private StatusFaseRncUtil() {
    }

    public static void marcarEmAndamentoAntesInserir(PersistenceEvent event) throws Exception {
        ((DynamicVO) event.getVo()).setProperty("STATUS", STATUS_EM_ANDAMENTO);
    }

    public static void confirmarEmAndamentoAposInserir(
        PersistenceEvent event,
        String tabela,
        String campoId
    ) throws Exception {
        atualizarStatus(event, tabela, campoId, STATUS_EM_ANDAMENTO);
    }

    public static void marcarConcluidaAntesAlterar(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        if (CONCLUIDO_SIM.equals(vo.asString("CONCLUIDO"))) {
            vo.setProperty("STATUS", STATUS_CONCLUIDA);
        }
    }

    public static void confirmarConcluidaAposAlterar(
        PersistenceEvent event,
        String tabela,
        String campoId
    ) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        if (!CONCLUIDO_SIM.equals(vo.asString("CONCLUIDO"))) {
            return;
        }
        atualizarStatus(event, tabela, campoId, STATUS_CONCLUIDA);
    }

    private static void atualizarStatus(
        PersistenceEvent event,
        String tabela,
        String campoId,
        String status
    ) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        NativeSql sql = new NativeSql(event.getJdbcWrapper());
        try {
            sql.appendSql(" UPDATE " + tabela + " ");
            sql.appendSql(" SET STATUS = :STATUS ");
            sql.appendSql(" WHERE " + campoId + " = :ID ");
            sql.setNamedParameter("STATUS", status);
            sql.setNamedParameter("ID", vo.asBigDecimal(campoId));
            sql.executeUpdate();
        } finally {
            NativeSql.releaseResources(sql);
        }
    }
}
