package br.com.le.addon.qualitymanagement.services;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;

public class AtualizaDocumentos {
    public static void aprovaDocumento(BigDecimal idDoc) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQCONTDOC ");
        query.append(" set STATUS = 'A' where IDDOC = ");
        query.append(idDoc);
        sql.appendSql(query.toString());
        sql.executeUpdate();
    }

    public static void documentoRevisado(BigDecimal idDoc) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        StringBuilder query = new StringBuilder();
        query.append(" update TGQCONTDOC ");
        query.append(" set STATUS = 'R' where IDDOC = ");
        query.append(idDoc);
        sql.appendSql(query.toString());
        sql.executeUpdate();
    }
}
