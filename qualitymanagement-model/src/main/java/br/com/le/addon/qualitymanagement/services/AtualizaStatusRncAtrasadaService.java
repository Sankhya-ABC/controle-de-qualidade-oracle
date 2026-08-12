package br.com.le.addon.qualitymanagement.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Avalia TGQRNC.DATAPREVENCERRAR de todos os registros (entidade ConsultaRNC) e marca
 * como Atrasada (STATUS = 'A') quando a data atual for superior a data prevista de
 * encerramento. Registros ja Concluidos ('C') ou Cancelados ('E') nao sao alterados.
 */
public final class AtualizaStatusRncAtrasadaService {

    private static final String STATUS_ATRASADA = "A";

    private AtualizaStatusRncAtrasadaService() {
    }

    public static void atualizarRncsAtrasadas() throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" UPDATE TGQRNC ");
            sql.appendSql(" SET STATUS = :STATUS_ATRASADA ");
            sql.appendSql(" WHERE DATAPREVENCERRAR IS NOT NULL ");
            sql.appendSql(" AND TRUNC(DATAPREVENCERRAR) < TRUNC(SYSDATE) ");
            sql.appendSql(" AND (STATUS IS NULL OR STATUS NOT IN ('C', 'E', :STATUS_ATRASADA)) ");

            sql.setNamedParameter("STATUS_ATRASADA", STATUS_ATRASADA);

            sql.executeUpdate();
            System.out.println("[AtualizaStatusRncAtrasada] Atualizacao de RNCs atrasadas concluida.");
        } finally {
            if (sql != null) {
                NativeSql.releaseResources(sql);
            }
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }
}
