package br.com.le.addon.qualitymanagement.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class AtualizaFases {
    public static void atualizaFaseRnc(String nuRNC, String origem) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        BigDecimal novaFase = new BigDecimal(0);
        NativeSql sqlOrigem = new NativeSql(jdbc);
        sqlOrigem.appendSql(" SELECT count(*) QTDE ");
        sqlOrigem.appendSql(" FROM TGQCADASTROS ");
        sqlOrigem.appendSql(" WHERE ID = " + Integer.valueOf(origem));
        sqlOrigem.appendSql(" AND ( UPPER(DESCRICAO) LIKE '%RISCOS%' ");
        sqlOrigem.appendSql(" OR UPPER(DESCRICAO) LIKE '%OPORTUNIDADES%') ");
        ResultSet rsetOrigem = sqlOrigem.executeQuery();
        rsetOrigem.next();
        if (rsetOrigem.getBigDecimal("QTDE").equals(BigDecimal.ZERO)) {
            NativeSql sqlFase = new NativeSql(jdbc);
            sqlFase.appendSql(" SELECT FASESNCID ");
            sqlFase.appendSql(" FROM TGQFASES ");
            sqlFase.appendSql(" WHERE FASESNCID = (SELECT F.FASESNCID + 1 ");
            sqlFase.appendSql(" FROM TGQFASES F, TGQRNC R ");
            sqlFase.appendSql(" WHERE F.FASESNCID = R.FASESNCID ");
            sqlFase.appendSql(" AND R.RNCID =  " + Integer.valueOf(nuRNC) + ")");
            ResultSet rsetFase = sqlFase.executeQuery();
            rsetFase.next();
            if (rsetFase != null) {
                novaFase = rsetFase.getBigDecimal("FASESNCID");
            } else {
                novaFase = new BigDecimal(10);
            }
        } else {
            novaFase = new BigDecimal(5);
        }
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQRNC ");
        query.append(" set FASESNCID = " + novaFase);
        query.append(" where RNCID = " + Integer.valueOf(nuRNC));
        sql.appendSql(query.toString());
        sql.executeUpdate();
        if (novaFase.intValue() == 10) {
            NativeSql upd = new NativeSql(jdbc);
            StringBuilder qryUpd = new StringBuilder();
            qryUpd.append(" update TGQRNC ");
            qryUpd.append(" set STATUS = 'C'");
            qryUpd.append(" where RNCID = " + Integer.valueOf(nuRNC));
            upd.appendSql(qryUpd.toString());
            upd.executeUpdate();
        }
    }

    public static void concluiFaseRnc(String nuRNC) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql upd = new NativeSql(jdbc);
        StringBuilder qryUpd = new StringBuilder();
        qryUpd.append(" update TGQRNC ");
        qryUpd.append(" set STATUS = 'C'");
        qryUpd.append(" where RNCID = " + Integer.valueOf(nuRNC));
        upd.appendSql(qryUpd.toString());
        upd.executeUpdate();
    }

    public static void atualizaFaseGestao(String idGestao) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        BigDecimal novaFase = new BigDecimal(0);
        NativeSql sqlFase = new NativeSql(jdbc);
        sqlFase.appendSql(" SELECT FASESID ");
        sqlFase.appendSql(" FROM TGQFASESGM ");
        sqlFase.appendSql(" WHERE FASESID = (SELECT F.FASESID + 1 ");
        sqlFase.appendSql(" FROM TGQFASESGM F, TGQGESTAOMUDANCA G ");
        sqlFase.appendSql(" WHERE F.FASESID = G.FASESID ");
        sqlFase.appendSql(" AND G.IDGESTAO =  " + Integer.valueOf(idGestao) + ")");
        ResultSet rsetFase = sqlFase.executeQuery();
        rsetFase.next();
        novaFase = rsetFase.getBigDecimal("FASESID");
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQGESTAOMUDANCA ");
        query.append(" set FASESID = " + novaFase);
        query.append(" where IDGESTAO = " + Integer.valueOf(idGestao));
        sql.appendSql(query.toString());
        sql.executeUpdate();
        System.out.println("idGestao: " + idGestao + " novaFase: " + novaFase);
        QuestionarioGestaoMudanca.criaQuestionariosGestao(novaFase, idGestao);
    }

    public static void retornaFaseRnc(String nuRNC, String status) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        BigDecimal novaFase = new BigDecimal(0);
        NativeSql sqlFase = new NativeSql(jdbc);
        sqlFase.appendSql(" SELECT FASESNCID ");
        sqlFase.appendSql(" FROM TGQFASES ");
        sqlFase.appendSql(" WHERE FASESNCID = (SELECT F.FASESNCID - 1 ");
        sqlFase.appendSql(" FROM TGQFASES F, TGQRNC R ");
        sqlFase.appendSql(" WHERE F.FASESNCID = R.FASESNCID ");
        sqlFase.appendSql(" AND R.RNCID =  " + Integer.valueOf(nuRNC) + ")");
        ResultSet rsetFase = sqlFase.executeQuery();
        rsetFase.next();
        if (rsetFase != null) {
            novaFase = rsetFase.getBigDecimal("FASESNCID");
        } else {
            novaFase = new BigDecimal(1);
        }
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQRNC ");
        query.append(" set FASESNCID = " + novaFase);
        query.append(" where RNCID = " + Integer.valueOf(nuRNC));
        sql.appendSql(query.toString());
        sql.executeUpdate();
        if (status.equals(Character.valueOf('C')))
            throw new Exception("Essa RNC jfoi concluida, npodervoltar para a fase anterior.");
    }
}
