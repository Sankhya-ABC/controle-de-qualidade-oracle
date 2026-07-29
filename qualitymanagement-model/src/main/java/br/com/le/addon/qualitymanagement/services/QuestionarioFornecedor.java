package br.com.le.addon.qualitymanagement.services;

import br.com.le.addon.qualitymanagement.utils.AnexoEmail;
import br.com.le.addon.qualitymanagement.utils.AnexoQuestionarioUtil;
import br.com.le.addon.qualitymanagement.utils.DocumentosQuestionarioUtil;
import br.com.le.addon.qualitymanagement.utils.EnviarEmailUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

public class QuestionarioFornecedor {

    public static void enviaQuestionario(String idQuest, String codFornec, String idQualif) throws Exception {
        validarCampoObrigatorio(idQuest, "ID do questionario");
        validarCampoObrigatorio(codFornec, "Codigo do fornecedor");
        validarCampoObrigatorio(idQualif, "ID da qualificacao");

        String empresa = getParametroObrigatorio("NOMEEMPQLF");

        String emailFornec = buscarEmailFornecedor(codFornec);

        if (isEmpty(emailFornec)) {
            throw new Exception(
                "Fornecedor " + codFornec + " nao possui e-mail cadastrado para envio do questionario.");
        }

        List<String> documentos = DocumentosQuestionarioUtil.listarDescricoes(idQuest);
        String blocoDocumentosHtml = DocumentosQuestionarioUtil.montarHtmlDocumentos(documentos);

        AnexoEmail anexo = AnexoQuestionarioUtil.carregarAnexoQuestionario(idQuest);

        System.out.println("===== ENVIO QUESTIONARIO FORNECEDOR =====");
        System.out.println("CODFORNEC: " + codFornec);
        System.out.println("IDQUEST: " + idQuest);
        System.out.println("IDQUALIF: " + idQualif);
        System.out.println("EMAIL FORNEC: " + emailFornec);
        System.out.println("ANEXO: " + anexo.getFileName());
        System.out.println("DOCUMENTOS LISTADOS: " + documentos.size());
        System.out.println("========================================");

        EnviarEmailUtil.enviarQuestionarioComAnexo(
            emailFornec,
            empresa,
            blocoDocumentosHtml,
            Collections.singletonList(anexo)
        );
    }

    public static void enviaNotificacao(BigDecimal codFornec, String mensagem) throws Exception {
        if (codFornec == null) {
            throw new Exception("Codigo do fornecedor nao informado.");
        }

        validarCampoObrigatorio(mensagem, "Mensagem");

        String emailFornec = buscarEmailFornecedor(String.valueOf(codFornec));

        if (isEmpty(emailFornec)) {
            throw new Exception("Fornecedor " + codFornec + " nao possui email cadastrado para notificacao.");
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
            throw new Exception("Parametro obrigatorio nao configurado: " + chave);
        }

        return valor.toString();
    }

    private static void validarCampoObrigatorio(String valor, String nomeCampo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(nomeCampo + " nao informado.");
        }
    }

    private static boolean isEmpty(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
