package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Logica compartilhada pelos listeners de fase da RNC (Causa Raiz, Acoes
 * Imediatas, Abrangencia, Acoes Corretivas, Implementacao): ao incluir um
 * novo registro, define STATUS = Em Andamento.
 *
 * A marcacao e feita em dois momentos: no beforeInsert, tentando popular o
 * VO antes do INSERT ser gerado; e no afterInsert, com um UPDATE direto pela
 * PK ja gerada, garantindo o valor independente de o framework incorporar ou
 * nao a mudanca feita no VO durante o beforeInsert.
 */
public final class StatusFaseRncUtil {

    private static final String STATUS_EM_ANDAMENTO = "E";

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
        DynamicVO vo = (DynamicVO) event.getVo();
        NativeSql sql = new NativeSql(event.getJdbcWrapper());
        try {
            sql.appendSql(" UPDATE " + tabela + " ");
            sql.appendSql(" SET STATUS = :STATUS ");
            sql.appendSql(" WHERE " + campoId + " = :ID ");
            sql.setNamedParameter("STATUS", STATUS_EM_ANDAMENTO);
            sql.setNamedParameter("ID", vo.asBigDecimal(campoId));
            sql.executeUpdate();
        } finally {
            NativeSql.releaseResources(sql);
        }
    }
}
