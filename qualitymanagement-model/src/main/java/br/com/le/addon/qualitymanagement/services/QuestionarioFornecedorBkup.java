package br.com.le.addon.qualitymanagement.services;


import java.util.Base64;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import br.com.le.addon.qualitymanagement.utils.EnviarEmailUtil;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class QuestionarioFornecedorBkup {
    public static void enviaQuestionario(String idQuest, String codFornec, String idQualif) throws Exception {
        String loginBase64 = (String)ParameterUtils.getParameter("LOGINQLD");
        String senhaBase64 = (String)ParameterUtils.getParameter("PSWQLD");
        String emailFornec = null;
        String emailBase64 = null;
        String codQuestBase64 = null;
        String codQualifBase64 = null;
        String auth = null;
        String urlQuestionario = (String)ParameterUtils.getParameter("URLQUALIDADE");
        String empresa = (String)ParameterUtils.getParameter("NOMEEMPQLF");
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        System.out.println("codFornec: " + codFornec);
        System.out.println("loginBase64: " + loginBase64);
        NativeSql sqlFornec = new NativeSql(jdbc);
        sqlFornec.appendSql(" SELECT EMAILQUESTIONARIO ");
        sqlFornec.appendSql(" FROM TGFPAR ");
        sqlFornec.appendSql(" WHERE CODPARC =  " + codFornec);
        ResultSet rsetFornec = sqlFornec.executeQuery();
        rsetFornec.next();
        System.out.println("email: " + rsetFornec.getString("EMAILQUESTIONARIO"));
        emailFornec = rsetFornec.getString("EMAILQUESTIONARIO");
        System.out.print("email fornec:" + emailFornec);
        emailBase64 = Base64.getEncoder().encodeToString(emailFornec.getBytes());
        codQuestBase64 = Base64.getEncoder().encodeToString(idQuest.getBytes());
        codQualifBase64 = Base64.getEncoder().encodeToString(idQualif.getBytes());
        auth = String.valueOf(emailBase64.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "")) + "?" + codQualifBase64.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "") + "?" + codQuestBase64.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "") + "?" + loginBase64.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "") + "?" + senhaBase64.replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "") + "?";
        urlQuestionario = String.valueOf(urlQuestionario) + "?" + auth;
        System.out.println("urlQuestionario: " + urlQuestionario);
        EnviarEmailUtil.enviarQuestionario(emailFornec, urlQuestionario, empresa);
    }

    public static void enviaNotificacao(BigDecimal codFornec, String mensagem) throws Exception {
        String emailFornec = null;
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
        emailFornec = rsetFornec.getString("EMAILQUESTIONARIO");
        EnviarEmailUtil.EnviarNotificacaoFornec(emailFornec, mensagem);
    }
}
