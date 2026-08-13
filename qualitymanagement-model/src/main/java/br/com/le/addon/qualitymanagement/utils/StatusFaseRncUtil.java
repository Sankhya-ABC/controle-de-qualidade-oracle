package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logica compartilhada pelos listeners de fase da RNC (Causa Raiz, Acoes
 * Imediatas, Abrangencia, Acoes Corretivas, Implementacao):
 * - RN001: ao incluir um novo registro, define STATUS = Em Andamento.
 * - RN002/RN005: ao marcar o checkbox CONCLUIDO = S, define STATUS = Concluida.
 *
 * Cada marcacao e feita em dois momentos: no before*, tentando popular o VO
 * antes do INSERT/UPDATE ser gerado; e no after*, com um UPDATE direto pela
 * PK, garantindo o valor independente de o framework incorporar ou nao a
 * mudanca feita no VO durante o before*. Todo o fluxo eh logado com o prefixo
 * [StatusFaseRnc] para permitir confirmar em log se o listener foi chamado.
 */
public final class StatusFaseRncUtil {

    private static final Logger LOG = Logger.getLogger(StatusFaseRncUtil.class.getName());

    private static final String STATUS_EM_ANDAMENTO = "E";
    private static final String STATUS_CONCLUIDA = "C";
    private static final String CONCLUIDO_SIM = "S";

    private StatusFaseRncUtil() {
    }

    public static void marcarEmAndamentoAntesInserir(PersistenceEvent event) throws Exception {
        LOG.info("[StatusFaseRnc] beforeInsert chamado");
        try {
            ((DynamicVO) event.getVo()).setProperty("STATUS", STATUS_EM_ANDAMENTO);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[StatusFaseRnc] erro no beforeInsert", e);
        }
    }

    public static void confirmarEmAndamentoAposInserir(
        PersistenceEvent event,
        String tabela,
        String campoId
    ) throws Exception {
        LOG.info("[StatusFaseRnc] afterInsert chamado tabela=" + tabela);
        try {
            atualizarStatus(event, tabela, campoId, STATUS_EM_ANDAMENTO);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[StatusFaseRnc] erro no afterInsert tabela=" + tabela, e);
        }
    }

    public static void marcarConcluidaAntesAlterar(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        String concluido = vo.asString("CONCLUIDO");
        LOG.info("[StatusFaseRnc] beforeUpdate chamado CONCLUIDO=" + concluido);
        try {
            if (CONCLUIDO_SIM.equals(concluido)) {
                vo.setProperty("STATUS", STATUS_CONCLUIDA);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[StatusFaseRnc] erro no beforeUpdate", e);
        }
    }

    public static void confirmarConcluidaAposAlterar(
        PersistenceEvent event,
        String tabela,
        String campoId
    ) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        String concluido = vo.asString("CONCLUIDO");
        LOG.info("[StatusFaseRnc] afterUpdate chamado tabela=" + tabela + " CONCLUIDO=" + concluido);
        if (!CONCLUIDO_SIM.equals(concluido)) {
            return;
        }
        try {
            atualizarStatus(event, tabela, campoId, STATUS_CONCLUIDA);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[StatusFaseRnc] erro no afterUpdate tabela=" + tabela, e);
        }
    }

    private static void atualizarStatus(
        PersistenceEvent event,
        String tabela,
        String campoId,
        String status
    ) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        Object idValor = vo.asBigDecimal(campoId);
        LOG.info("[StatusFaseRnc] executando UPDATE " + tabela + " SET STATUS='" + status
            + "' WHERE " + campoId + "=" + idValor);
        NativeSql sql = new NativeSql(event.getJdbcWrapper());
        try {
            sql.appendSql(" UPDATE " + tabela + " ");
            sql.appendSql(" SET STATUS = :STATUS ");
            sql.appendSql(" WHERE " + campoId + " = :ID ");
            sql.setNamedParameter("STATUS", status);
            sql.setNamedParameter("ID", idValor);
            sql.executeUpdate();
            LOG.info("[StatusFaseRnc] UPDATE concluido " + tabela + " " + campoId + "=" + idValor);
        } finally {
            NativeSql.releaseResources(sql);
        }
    }
}
