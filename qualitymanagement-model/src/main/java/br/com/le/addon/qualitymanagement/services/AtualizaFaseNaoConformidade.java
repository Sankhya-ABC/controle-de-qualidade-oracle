package br.com.le.addon.qualitymanagement.services;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class AtualizaFaseNaoConformidade {
    public static void atualizaFase(String nuRNC) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        BigDecimal novaFase = new BigDecimal(0);
        NativeSql sqlFase = new NativeSql(jdbc);
        sqlFase.appendSql(" SELECT FASESNCID ");
        sqlFase.appendSql(" FROM TGQFASES ");
        sqlFase.appendSql(" WHERE FASESNCID = (SELECT F.FASESNCID + 1 ");
        sqlFase.appendSql(" FROM TGQFASES F, TGQRNC R ");
        sqlFase.appendSql(" WHERE F.FASESNCID = R.FASESNCID ");
        sqlFase.appendSql(" AND R.RNCID =  " + Integer.valueOf(nuRNC) + ")");
        ResultSet rsetFase = sqlFase.executeQuery();
        rsetFase.next();
        novaFase = rsetFase.getBigDecimal("FASESNCID");
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQRNC ");
        query.append(" set FASESNCID = " + novaFase);
        query.append(" where RNCID = " + Integer.valueOf(nuRNC));
        sql.appendSql(query.toString());
        sql.executeUpdate();
    }
}
