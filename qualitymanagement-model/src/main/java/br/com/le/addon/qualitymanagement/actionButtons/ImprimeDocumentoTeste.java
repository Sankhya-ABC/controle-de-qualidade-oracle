package br.com.le.addon.qualitymanagement.actionButtons;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class ImprimeDocumentoTeste {
    public InputStream imprimeDocumento(BigDecimal idDoc) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        NativeSql sql = null;
        ResultSet rset = null;
        InputStream arquivo = null;
        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT DOC.ARQUIVO");
            sql.appendSql(" FROM TGQARQDOC DOC");
            sql.setNamedParameter("IDARQ", idDoc);
            rset = sql.executeQuery();
            while (rset.next())
                arquivo = rset.getBinaryStream(1);
        } catch (Exception e) {
            Exception ee = new Exception("Erro ao imprimir o documento");
            ee.printStackTrace();
            throw ee;
        } finally {
            jdbc.closeSession();
        }
        return null;
    }
}
