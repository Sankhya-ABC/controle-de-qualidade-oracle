package br.com.le.addon.qualitymanagement.services;

import br.com.le.addon.qualitymanagement.utils.EnviarEmailUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta VW_VENC_QUALIF_FORN, agrupa por qualificacao e envia um e-mail com todos os vencimentos do dia.
 */
public final class NotificacaoVencimentoQualificacaoService {

    public static final String VW_VENC_QUALIF_FORN = "VW_VENC_QUALIF_FORN";
    public static final String TGQLOGNOTIFVENC = "TGQLOGNOTIFVENC";

    private static final SimpleDateFormat FMT_DATA = new SimpleDateFormat("dd/MM/yyyy");

    private NotificacaoVencimentoQualificacaoService() {
    }

    public static void processarNotificacoesPendentes() throws Exception {
        Map<BigDecimal, List<RegistroVencimento>> porQualificacao = agruparPorQualificacao(listarRegistrosParaEnvio());
        int enviados = 0;
        int ignorados = 0;

        for (List<RegistroVencimento> grupo : porQualificacao.values()) {
            List<RegistroVencimento> pendentes = filtrarNaoNotificadosHoje(grupo);
            if (pendentes.isEmpty()) {
                ignorados += grupo.size();
                continue;
            }

            RegistroVencimento cabecalho = pendentes.get(0);
            boolean algumEmailEnfileirado = enviarNotificacoesAgrupadas(cabecalho, pendentes);
            if (algumEmailEnfileirado) {
                for (RegistroVencimento registro : pendentes) {
                    registrarEnvio(registro);
                }
                enviados++;
                System.out.println("[NotifVenc] E-mail agrupado IDQUALIF=" + cabecalho.idQualif
                    + " itens=" + pendentes.size());
            } else {
                System.out.println("[NotifVenc] Nenhum e-mail valido para IDQUALIF="
                    + cabecalho.idQualif + " CODPARC=" + cabecalho.codParc);
            }
        }

        System.out.println("[NotifVenc] Processamento concluido. Qualificacoes="
            + porQualificacao.size() + " e-mails enviados=" + enviados + " itens ignorados=" + ignorados);
    }

    private static Map<BigDecimal, List<RegistroVencimento>> agruparPorQualificacao(List<RegistroVencimento> registros) {
        Map<BigDecimal, List<RegistroVencimento>> mapa = new LinkedHashMap<>();
        for (RegistroVencimento registro : registros) {
            if (registro.idQualif == null) {
                continue;
            }
            List<RegistroVencimento> grupo = mapa.get(registro.idQualif);
            if (grupo == null) {
                grupo = new ArrayList<>();
                mapa.put(registro.idQualif, grupo);
            }
            grupo.add(registro);
        }
        return mapa;
    }

    private static List<RegistroVencimento> filtrarNaoNotificadosHoje(List<RegistroVencimento> grupo) throws Exception {
        List<RegistroVencimento> pendentes = new ArrayList<>();
        for (RegistroVencimento registro : grupo) {
            if (!jaNotificadoHoje(registro)) {
                pendentes.add(registro);
            }
        }
        return pendentes;
    }

    private static List<RegistroVencimento> listarRegistrosParaEnvio() throws Exception {
        List<RegistroVencimento> lista = new ArrayList<>();
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT ");
            sql.appendSql("     IDQUALIF, ");
            sql.appendSql("     IDREGISTRO, ");
            sql.appendSql("     TIPO_VENCIMENTO, ");
            sql.appendSql("     CODPARC, ");
            sql.appendSql("     NOMEPARC, ");
            sql.appendSql("     EMAIL_FORNECEDOR, ");
            sql.appendSql("     EMAIL_NOTIFICACAO_EMPRESA, ");
            sql.appendSql("     NOME_DOCUMENTO, ");
            sql.appendSql("     DATAVALIDADE, ");
            sql.appendSql("     DIAS_RESTANTES, ");
            sql.appendSql("     TIPO_NOTIFICACAO ");
            sql.appendSql(" FROM VW_VENC_QUALIF_FORN ");
            sql.appendSql(" WHERE ENVIAR_NOTIFICACAO = 'S' ");
            sql.appendSql(" AND TIPO_NOTIFICACAO = 'AVISO_VENCIMENTO' ");
            sql.appendSql(" ORDER BY IDQUALIF, TIPO_VENCIMENTO, IDREGISTRO ");

            rset = sql.executeQuery();
            while (rset.next()) {
                lista.add(lerRegistro(rset));
            }
        } finally {
            fecharSql(rset, sql);
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }

        return lista;
    }

    private static RegistroVencimento lerRegistro(ResultSet rset) throws Exception {
        RegistroVencimento reg = new RegistroVencimento();
        reg.idQualif = rset.getBigDecimal("IDQUALIF");
        reg.idRegistro = rset.getBigDecimal("IDREGISTRO");
        reg.tipoVencimento = rset.getString("TIPO_VENCIMENTO");
        reg.codParc = rset.getBigDecimal("CODPARC");
        reg.nomeParc = rset.getString("NOMEPARC");
        reg.emailFornecedor = rset.getString("EMAIL_FORNECEDOR");
        reg.emailEmpresa = rset.getString("EMAIL_NOTIFICACAO_EMPRESA");
        reg.nomeDocumento = rset.getString("NOME_DOCUMENTO");
        reg.dataValidade = rset.getTimestamp("DATAVALIDADE");
        if (reg.dataValidade == null) {
            java.sql.Date dt = rset.getDate("DATAVALIDADE");
            if (dt != null) {
                reg.dataValidade = new Timestamp(dt.getTime());
            }
        }
        reg.diasRestantes = rset.getBigDecimal("DIAS_RESTANTES");
        reg.tipoNotificacao = rset.getString("TIPO_NOTIFICACAO");
        return reg;
    }

    private static boolean enviarNotificacoesAgrupadas(
        RegistroVencimento cabecalho,
        List<RegistroVencimento> itens
    ) throws Exception {
        String assunto = "Aviso de vencimento - qualificacao de fornecedor";
        String listaHtml = montarHtmlListaVencimentos(itens);
        String mensagem = EnviarEmailUtil.montarMensagemVencimento(listaHtml);

        boolean enviou = false;

        if (temEmail(cabecalho.emailFornecedor)) {
            EnviarEmailUtil.enviarHtmlNaFila(cabecalho.emailFornecedor.trim(), assunto, mensagem);
            enviou = true;
            System.out.println("[NotifVenc] Fornecedor IDQUALIF=" + cabecalho.idQualif
                + " " + cabecalho.nomeParc + " itens=" + itens.size()
                + " email=" + cabecalho.emailFornecedor);
        }

        if (temEmail(cabecalho.emailEmpresa)) {
            String emailEmpresa = cabecalho.emailEmpresa.trim();
            if (!emailEmpresa.equalsIgnoreCase(
                cabecalho.emailFornecedor != null ? cabecalho.emailFornecedor.trim() : "")) {
                EnviarEmailUtil.enviarHtmlNaFila(emailEmpresa, assunto, mensagem);
                enviou = true;
                System.out.println("[NotifVenc] Empresa IDQUALIF=" + cabecalho.idQualif
                    + " itens=" + itens.size() + " email=" + emailEmpresa);
            }
        }

        return enviou;
    }

    static String montarHtmlListaVencimentos(List<RegistroVencimento> itens) {
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\" style=\"border-collapse:collapse;\">");
        html.append("<thead><tr>");
        html.append("<th>Documento</th>");
        html.append("<th>Tipo</th>");
        html.append("<th>Data de vencimento</th>");
        html.append("<th>Dias restantes</th>");
        html.append("</tr></thead><tbody>");

        for (RegistroVencimento item : itens) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(valorSeguro(item.nomeDocumento))).append("</td>");
            html.append("<td>").append(escapeHtml(descricaoTipoVencimento(item.tipoVencimento))).append("</td>");
            html.append("<td>").append(escapeHtml(formatarData(item.dataValidade))).append("</td>");
            html.append("<td>").append(item.diasRestantes != null ? item.diasRestantes.toPlainString() : "")
                .append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        return html.toString();
    }

    private static String descricaoTipoVencimento(String tipo) {
        if ("CERTIFICADO".equals(tipo)) {
            return "Certificado";
        }
        if ("ARQUIVO_QUESTIONARIO".equals(tipo)) {
            return "Arquivo questionario";
        }
        return valorSeguro(tipo);
    }

    private static boolean jaNotificadoHoje(RegistroVencimento registro) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT 1 ");
            sql.appendSql(" FROM TGQLOGNOTIFVENC ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.appendSql(" AND TIPO_NOTIFICACAO = :TIPO_NOTIFICACAO ");
            sql.appendSql(" AND TRUNC(DTNOTIF) = TRUNC(SYSDATE) ");
            sql.appendSql(" AND TIPO_VENCIMENTO = :TIPO_VENCIMENTO ");
            sql.appendSql(" AND IDREGISTRO = :IDREGISTRO ");

            sql.setNamedParameter("IDQUALIF", registro.idQualif);
            sql.setNamedParameter("TIPO_NOTIFICACAO", registro.tipoNotificacao);
            sql.setNamedParameter("TIPO_VENCIMENTO", registro.tipoVencimento);
            sql.setNamedParameter("IDREGISTRO", registro.idRegistro);

            rset = sql.executeQuery();
            return rset.next();
        } finally {
            fecharSql(rset, sql);
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }

    private static void registrarEnvio(RegistroVencimento registro) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" INSERT INTO TGQLOGNOTIFVENC ");
            sql.appendSql(" (IDLOG, IDQUALIF, CODPARC, TIPO_NOTIFICACAO, TIPO_VENCIMENTO, IDREGISTRO, ");
            sql.appendSql("  EMAIL_FORNEC, EMAIL_EMPRESA, DTNOTIF, DHENVIO) ");
            sql.appendSql(" VALUES ( ");
            sql.appendSql(" (SELECT NVL(MAX(IDLOG), 0) + 1 FROM TGQLOGNOTIFVENC), ");
            sql.appendSql(" :IDQUALIF, :CODPARC, :TIPO_NOTIFICACAO, :TIPO_VENCIMENTO, :IDREGISTRO, ");
            sql.appendSql(" :EMAIL_FORNEC, :EMAIL_EMPRESA, TRUNC(SYSDATE), SYSDATE ) ");

            sql.setNamedParameter("IDQUALIF", registro.idQualif);
            sql.setNamedParameter("CODPARC", registro.codParc);
            sql.setNamedParameter("TIPO_NOTIFICACAO", registro.tipoNotificacao);
            sql.setNamedParameter("TIPO_VENCIMENTO", registro.tipoVencimento);
            sql.setNamedParameter("IDREGISTRO", registro.idRegistro);
            sql.setNamedParameter("EMAIL_FORNEC", registro.emailFornecedor);
            sql.setNamedParameter("EMAIL_EMPRESA", registro.emailEmpresa);
            sql.executeUpdate();
        } finally {
            if (sql != null) {
                NativeSql.releaseResources(sql);
            }
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }

    private static String formatarData(Timestamp data) {
        if (data == null) {
            return "";
        }
        synchronized (FMT_DATA) {
            return FMT_DATA.format(data);
        }
    }

    private static String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private static String escapeHtml(String texto) {
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static boolean temEmail(String email) {
        return email != null && !email.trim().isEmpty();
    }

    private static void fecharSql(ResultSet rset, NativeSql sql) {
        if (rset != null) {
            try {
                rset.close();
            } catch (Exception ignored) {
            }
        }
        if (sql != null) {
            NativeSql.releaseResources(sql);
        }
    }

    static final class RegistroVencimento {
        private BigDecimal idQualif;
        private BigDecimal idRegistro;
        private String tipoVencimento;
        private BigDecimal codParc;
        private String nomeParc;
        private String emailFornecedor;
        private String emailEmpresa;
        private String nomeDocumento;
        private Timestamp dataValidade;
        private BigDecimal diasRestantes;
        private String tipoNotificacao;
    }
}
