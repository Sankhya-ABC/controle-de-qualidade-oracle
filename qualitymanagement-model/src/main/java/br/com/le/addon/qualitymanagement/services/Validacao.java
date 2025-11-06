package br.com.le.addon.qualitymanagement.services;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Validacao {
    public static void ValidacaoDocumentos() throws Exception {
        String descNotific = null;
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sqlConfig = new NativeSql(jdbc);
        sqlConfig.appendSql(" SELECT TOP 1 QTDDIASDOC, EMAILNOTFY, CODUSU ");
        sqlConfig.appendSql(" FROM TGQCONFIG ");
        ResultSet rsetConfig = sqlConfig.executeQuery();
        rsetConfig.next();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT D.IDDOC, D.TITULODOC, CONVERT(VARCHAR, A.DATAVALIDADE, 103) VCTO ");
        sql.appendSql(" FROM TGQARQDOC A, TGQCONTDOC D ");
        sql.appendSql(" WHERE D.IDDOC  = A.IDDOCUMENTO ");
        sql.appendSql(" AND ( CONVERT(VARCHAR, A.DATAVALIDADE, 105)  <= CONVERT(VARCHAR, DATEADD(DAY, " + rsetConfig.getBigDecimal("QTDDIASDOC") + ", GETDATE()), 105)  ");
        sql.appendSql(" OR CONVERT(VARCHAR, A.DATAVALIDADE, 105)  <= CONVERT(VARCHAR, GETDATE(), 105))  ");
        ResultSet rset = sql.executeQuery();
        while (rset.next()) {
            NativeSql sqlAviso = new NativeSql(jdbc);
            sqlAviso.appendSql(" SELECT MAX(NUAVISO)+ 1 NUAVISO FROM TSIAVI ");
            ResultSet rsetAviso = sqlAviso.executeQuery();
            rsetAviso.next();
            descNotific = "O documento " + rset.getString("IDDOC") + " - " + rset.getString("TITULODOC") + " ira vencer (ou venceu) no dia " + rset.getString("VCTO");
            PreparedStatement pstm = jdbc.getPreparedStatement("INSERT INTO TSIAVI(NUAVISO,TITULO,DESCRICAO,IDENTIFICADOR, IMPORTANCIA,  CODUSU, TIPO, DHCRIACAO,CODUSUREMETENTE) VALUES(?, 'Documentos a Vencer', ?, 'PERSONALIZADO', 1, ?, 'P', GETDATE(), 0)");
            pstm.setBigDecimal(1, rsetAviso.getBigDecimal(1));
            pstm.setString(2, descNotific);
            pstm.setBigDecimal(3, rsetConfig.getBigDecimal("CODUSU"));
            pstm.executeUpdate();
        }
    }

    public static void ValidacaoFornecedores(BigDecimal codEmp) throws Exception {
        String descNotific = null;
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sqlConfig = new NativeSql(jdbc);
        if (codEmp.equals(BigDecimal.ZERO)) {
            sqlConfig.appendSql(" SELECT TOP 1 ISNULL(NOTIFICFORNEC,'N') NOTIFICFORNEC, EMAILNOTIFYQF, DIASRESPFORNEC ");
            sqlConfig.appendSql(" FROM TGQCONFIG ");
        } else {
            sqlConfig.appendSql(" SELECT ISNULL(NOTIFICFORNEC,'N') NOTIFICFORNEC, EMAILNOTIFYQF, DIASRESPFORNEC ");
            sqlConfig.appendSql(" FROM TGQCONFIG ");
            sqlConfig.appendSql(" WHERE CODEMP =  " + codEmp);
        }
        ResultSet rsetConfig = sqlConfig.executeQuery();
        rsetConfig.next();
        if (rsetConfig.getString("NOTIFICFORNEC").equals("S")) {
            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql(" SELECT Q.CODPARC, C.IDCERTFORN, C.DESCRCERT, C.DATAVALIDADE ");
            nativeSql.appendSql(" FROM TGQCERTFORN C, TGQQUALIFFORN Q ");
            nativeSql.appendSql(" WHERE C.IDQUALIFIC = Q.IDQUALIF ");
            nativeSql.appendSql(" AND CONVERT(VARCHAR, C.DATAVALIDADE, 105)  <= CONVERT(VARCHAR, GETDATE(), 105) ");
            ResultSet rset = nativeSql.executeQuery();
            descNotific = "Segue lista de documentos vencidos que devem ser enviados at" + rsetConfig.getBigDecimal("DIASRESPFORNEC") + " dias uteis da data de recebimento deste e-mail para mantermos a empresa qualificada." +
                "\n";
            while (rset.next())
                descNotific = String.valueOf(descNotific) + "\n" +
                    rset.getString("DESCRCERT");
            try {
                QuestionarioFornecedor.enviaNotificacao(rset.getBigDecimal("CODPARC"), descNotific);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT MAX(Q.IDQUALIF) ULTQUALIFIC, Q.CODPARC, P.NOMEPARC, MAX(Q.DATAVALIDADE) DATAVALIDADE ");
        sql.appendSql(" FROM TGQQUALIFFORN Q, TGFPAR P ");
        sql.appendSql(" WHERE Q.CODPARC = P.CODPARC   ");
        sql.appendSql(" AND CONVERT(VARCHAR, Q.DATAVALIDADE, 105)  >= CONVERT(VARCHAR, DATEADD(DAY, -10, GETDATE()), 105) ");
        sql.appendSql(" AND CONVERT(VARCHAR, Q.DATAVALIDADE, 105)  <= CONVERT(VARCHAR, GETDATE(), 105) ");
        sql.appendSql(" GROUP BY Q.CODPARC, P.NOMEPARC ");
        ResultSet rsetNotify = sql.executeQuery();
        while (rsetNotify.next()) {
            NativeSql sqlAviso = new NativeSql(jdbc);
            sqlAviso.appendSql(" SELECT MAX(NUAVISO)+ 1 NUAVISO FROM TSIAVI ");
            ResultSet rsetAviso = sqlAviso.executeQuery();
            rsetAviso.next();
            descNotific = "A qualificado Fornecedor " + rsetNotify.getBigDecimal("CODPARC") + " - " + rsetNotify.getString("NOMEPARC") + " irvencer (ou venceu) no dia " + rsetNotify.getString("DATAVALIDADE");
            PreparedStatement pstm = jdbc.getPreparedStatement("INSERT INTO TSIAVI(NUAVISO,TITULO,DESCRICAO,IDENTIFICADOR, IMPORTANCIA,  CODUSU, TIPO, DHCRIACAO,CODUSUREMETENTE) VALUES(?, 'QualificaVencer', ?, 'PERSONALIZADO', 1, ?, 'P', GETDATE(), 0)");
            pstm.setBigDecimal(1, rsetAviso.getBigDecimal(1));
            pstm.setString(2, descNotific);
            pstm.setBigDecimal(3, rsetConfig.getBigDecimal("CODUSU"));
            pstm.executeUpdate();
        }
    }
}
