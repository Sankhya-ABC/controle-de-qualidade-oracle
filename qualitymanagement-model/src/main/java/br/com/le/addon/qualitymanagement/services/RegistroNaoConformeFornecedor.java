package br.com.le.addon.qualitymanagement.services;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import br.com.snkcps.qualidade.utils.EnviarEmailUtil;
import java.sql.ResultSet;

public class RegistroNaoConformeFornecedor {
    public static void enviaRegNaoConformidade(String idRnc, String codFornec, String detalhamento) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sqlFornec = new NativeSql(jdbc);
        sqlFornec.appendSql(" SELECT EMAILQUESTIONARIO ");
        sqlFornec.appendSql(" FROM TGFPAR ");
        sqlFornec.appendSql(" WHERE CODPARC =  " + codFornec);
        ResultSet rsetFornec = sqlFornec.executeQuery();
        rsetFornec.next();
        String emailFornec = rsetFornec.getString("EMAILQUESTIONARIO");
        String empresa = (String)ParameterUtils.getParameter("NOMEEMPQLF");
        EnviarEmailUtil.enviarRncFornecedor(emailFornec, empresa, detalhamento, idRnc);
    }
}
