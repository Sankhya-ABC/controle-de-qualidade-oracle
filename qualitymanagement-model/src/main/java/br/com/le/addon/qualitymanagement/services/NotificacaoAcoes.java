package br.com.le.addon.qualitymanagement.services;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.le.addon.qualitymanagement.utils.EnviarEmailUtil;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class NotificacaoAcoes {
    public static String enviaNotificacao(String rncId, String codParc, String enviarEmail) throws Exception {
        String mensagem = null;
        String assunto = null;
        String emailParc = null;
        String retorno = null;
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            System.out.println("enviarEmail: " + enviarEmail);
            if (enviarEmail.equals("S")) {
                NativeSql sqlParc = new NativeSql(jdbc);
                sqlParc.appendSql(" SELECT EMAIL , NOMEPARC ");
                sqlParc.appendSql(" FROM TGFPAR ");
                sqlParc.appendSql(" WHERE CODPARC =  " + codParc);
                ResultSet rsetParc = sqlParc.executeQuery();
                rsetParc.next();
                emailParc = rsetParc.getString("EMAIL");
                System.out.println("emailParc: " + emailParc);
                if (emailParc != null) {
                    NativeSql sqlRnc = new NativeSql(jdbc);
                    sqlRnc.appendSql(" SELECT CASE RESP.ORIGEM ");
                    sqlRnc.appendSql(" WHEN 'A' THEN 'Responspela RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'B' THEN 'Responspelas AImediatas da RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'C' THEN 'Responspelas Causas Raizes da RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'D' THEN 'Responspelas ACorretivas da RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'E' THEN 'Responspelas Implementada RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'F' THEN 'Responspelas Verificade Eficacia da RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'G' THEN 'Responspelas Validada RNC n' || RNC.RNCID ");
                    sqlRnc.appendSql(" WHEN 'H' THEN 'Responspelos Riscos e Oportunidades da RNC n' || RNC.RNCID  ");
                    sqlRnc.appendSql(" WHEN 'I' THEN 'Responspelas Liberade Produtos da RNC n' || RNC.RNCID END ASSUNTO, ");
                    sqlRnc.appendSql(" CASE RESP.ORIGEM   ");
                    sqlRnc.appendSql(" WHEN 'A' THEN 'Olvocfoi adicionado como responspara o Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO ");
                    sqlRnc.appendSql(" WHEN 'B' THEN 'Olvocfoi adicionado como responspara AImediatas do Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'C' THEN 'Olvocfoi adicionado como responspara Causas Raizes do Registro de NConformidade '|| RNC.RNCID ||' - ' || RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'D' THEN 'Olvocfoi adicionado como responspara ACorretivas do Registro de NConformidade '|| RNC.RNCID || ' - '|| RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'E' THEN 'Olvocfoi adicionado como responspara Implementado Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'F' THEN 'Olvocfoi adicionado como responspara Verificade Eficacia do Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'G' THEN 'Olvocfoi adicionado como responspara Validado Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO ");
                    sqlRnc.appendSql(" WHEN 'H' THEN 'Olvocfoi adicionado como responspara Riscos e Oportunidades do Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO  ");
                    sqlRnc.appendSql(" WHEN 'I' THEN 'Olvocfoi adicionado como responspara Liberade Produtos do Registro de NConformidade '|| RNC.RNCID || ' - ' || RNC.DETALHAMENTO  END MENSAGEM ");
                    sqlRnc.appendSql(" FROM TGQRNC RNC, TGQRESPRNC RESP  ");
                    sqlRnc.appendSql(" WHERE RNC.RNCID =  " + rncId);
                    ResultSet rsetRnc = sqlRnc.executeQuery();
                    rsetRnc.next();
                    assunto = rsetRnc.getString("ASSUNTO");
                    mensagem = rsetRnc.getString("MENSAGEM");
                    EnviarEmailUtil.EnviarNotificacaoAcoes(emailParc, mensagem, assunto);
                    retorno = "Enviado";
                } else {
                    retorno = "NEnviado";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
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
