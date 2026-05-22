package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Lista documentos obrigatorios (QuestionarioDocumentosQa) e monta HTML para o e-mail.
 */
public final class DocumentosQuestionarioUtil {

    private static final String INST_DOCUMENTOS = "QuestionarioDocumentosQa";

    private DocumentosQuestionarioUtil() {
    }

    public static List<String> listarDescricoes(String idQuest) throws Exception {
        List<String> descricoes = new ArrayList<>();
        BigDecimal idQuestBd = new BigDecimal(idQuest.trim());

        try {
            JapeWrapper dao = JapeFactory.dao(INST_DOCUMENTOS);
            Collection<DynamicVO> registros = dao.find("IDQUEST = ?", idQuestBd);

            if (registros != null) {
                for (DynamicVO vo : registros) {
                    String descricao = vo.asString("DESCRICAO");
                    if (descricao != null && !descricao.trim().isEmpty()) {
                        descricoes.add(descricao.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DocumentosQuestionario] Jape " + INST_DOCUMENTOS + ": " + e.getMessage());
            descricoes = listarDescricoesSql(idQuestBd);
        }

        return descricoes;
    }

    public static String montarHtmlDocumentos(List<String> descricoes) {
        if (descricoes == null || descricoes.isEmpty()) {
            return "<p><em>Nenhum documento cadastrado para este questionario.</em></p>";
        }

        StringBuilder html = new StringBuilder("<ul>");
        for (String descricao : descricoes) {
            html.append("<li>").append(escaparHtml(descricao)).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private static List<String> listarDescricoesSql(BigDecimal idQuest) throws Exception {
        List<String> descricoes = new ArrayList<>();

        br.com.sankhya.jape.dao.JdbcWrapper jdbc =
            br.com.sankhya.modelcore.util.EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        br.com.sankhya.jape.sql.NativeSql sql = null;
        java.sql.ResultSet rset = null;

        try {
            sql = new br.com.sankhya.jape.sql.NativeSql(jdbc);
            sql.appendSql(" SELECT DESCRICAO ");
            sql.appendSql(" FROM TGQQUESTDOC ");
            sql.appendSql(" WHERE IDQUEST = :IDQUEST ");
            sql.appendSql(" ORDER BY IDDOCQST ");
            sql.setNamedParameter("IDQUEST", idQuest);

            rset = sql.executeQuery();
            while (rset.next()) {
                String descricao = rset.getString("DESCRICAO");
                if (descricao != null && !descricao.trim().isEmpty()) {
                    descricoes.add(descricao.trim());
                }
            }
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (Exception ignored) {
                }
            }
            if (sql != null) {
                br.com.sankhya.jape.sql.NativeSql.releaseResources(sql);
            }
            br.com.sankhya.jape.dao.JdbcWrapper.closeSession(jdbc);
        }

        return descricoes;
    }

    private static String escaparHtml(String texto) {
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
