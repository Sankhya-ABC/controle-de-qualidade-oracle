package br.com.le.addon.qualitymanagement.services;

import br.com.le.addon.qualitymanagement.utils.AuthQuestionarioUtil;
import br.com.le.addon.qualitymanagement.utils.EnviarEmailUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;

public class QuestionarioFornecedor {

    public static void enviaQuestionario(String idQuest, String codFornec, String idQualif) throws Exception {
        validarCampoObrigatorio(idQuest, "ID do questionário");
        validarCampoObrigatorio(codFornec, "Código do fornecedor");
        validarCampoObrigatorio(idQualif, "ID da qualificação");

        String loginBase64 = getParametroObrigatorio("LOGINQLF");
        String senhaBase64 = getParametroObrigatorio("PSWQLD");
        String urlQuestionario = getParametroObrigatorio("URLQUALIDADE");
        String empresa = getParametroObrigatorio("NOMEEMPQLF");

        String emailFornec = buscarEmailFornecedor(codFornec);

        if (isEmpty(emailFornec)) {
            throw new Exception("Fornecedor " + codFornec + " nao possui e-mail cadastrado para envio do questionario.");
        }

        String emailBase64 = toBase64(emailFornec);
        String codQuestBase64 = toBase64(idQuest);
        String codQualifBase64 = toBase64(idQualif);

        String auth = AuthQuestionarioUtil.montarAuth(
            emailBase64,
            codQualifBase64,
            codQuestBase64,
            loginBase64,
            senhaBase64
        );

        String urlFinal = String.valueOf(urlQuestionario) + "?" + auth;

        System.out.println("===== ENVIO QUESTIONARIO FORNECEDOR =====");
        System.out.println("CODFORNEC: " + codFornec);
        System.out.println("IDQUEST: " + idQuest);
        System.out.println("IDQUALIF: " + idQualif);
        System.out.println("EMAIL FORNEC: " + emailFornec);
        System.out.println("URL FINAL: " + urlFinal);
        System.out.println("========================================");

        EnviarEmailUtil.enviarQuestionario(emailFornec, urlFinal, empresa);
    }

    public static void enviaNotificacao(BigDecimal codFornec, String mensagem) throws Exception {
        if (codFornec == null) {
            throw new Exception("Código do fornecedor nao informado.");
        }

        validarCampoObrigatorio(mensagem, "Mensagem");

        String emailFornec = buscarEmailFornecedor(String.valueOf(codFornec));

        if (isEmpty(emailFornec)) {
            throw new Exception("Fornecedor " + codFornec + " nao possui email cadastrado para notificação.");
        }

        EnviarEmailUtil.EnviarNotificacaoFornec(emailFornec, mensagem);
    }

    private static String buscarEmailFornecedor(String codFornec) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);

            sql.appendSql(" SELECT ");
            sql.appendSql("     EMAILQUESTIONARIO");
            sql.appendSql(" FROM TGFPAR ");
            sql.appendSql(" WHERE CODPARC = :CODPARC ");

            sql.setNamedParameter("CODPARC", new BigDecimal(codFornec));

            rset = sql.executeQuery();

            if (!rset.next()) {
                throw new Exception("Fornecedor nao encontrado na TGFPAR. CODPARC: " + codFornec);
            }

            String email = rset.getString("EMAILQUESTIONARIO");

            System.out.println("Email localizado para o fornecedor " + codFornec + ": " + email);

            return email;

        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (Exception ignored) {
                }
            }

            if (sql != null) {
                NativeSql.releaseResources(sql);
            }

            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }

    private static String getParametroObrigatorio(String chave) throws Exception {
        Object valor = ParameterUtils.getParameter(chave);

        if (valor == null || valor.toString().trim().isEmpty()) {
            throw new Exception("Parâmetro obrigatório não configurado: " + chave);
        }

        return valor.toString();
    }

    private static String toBase64(String valor) {
        if (valor == null) {
            valor = "";
        }

        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static void validarCampoObrigatorio(String valor, String nomeCampo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(nomeCampo + " não informado.");
        }
    }

    private static boolean isEmpty(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
