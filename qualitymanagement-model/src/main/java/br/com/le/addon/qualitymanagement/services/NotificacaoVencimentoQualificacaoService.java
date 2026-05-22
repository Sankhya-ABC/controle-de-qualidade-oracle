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
import java.util.List;

/**
 * Consulta VW_VENC_QUALIF_FORN e enfileira e-mails de vencimento (fornecedor e empresa).
 */
public final class NotificacaoVencimentoQualificacaoService {

    private static final String VIEW_VENCIMENTO = "VW_VENC_QUALIF_FORN";
    private static final String TABELA_LOG = "TGQLOGNOTIFVENC";

    private static final String TIPO_VENCIMENTO_HOJE = "VENCIMENTO_HOJE";

    private static final SimpleDateFormat FMT_DATA = new SimpleDateFormat("dd/MM/yyyy");

    private NotificacaoVencimentoQualificacaoService() {
    }

    public static void processarNotificacoesPendentes() throws Exception {
        List<RegistroVencimento> registros = listarRegistrosParaEnvio();
        int enviados = 0;
        int ignorados = 0;

        for (RegistroVencimento registro : registros) {
            if (jaNotificadoHoje(registro.idQualif, registro.tipoNotificacao)) {
                ignorados++;
                continue;
            }

            boolean algumEmailEnfileirado = enviarNotificacoes(registro);
            if (algumEmailEnfileirado) {
                registrarEnvio(registro.idQualif, registro.tipoNotificacao);
                enviados++;
            } else {
                System.out.println("[NotifVenc] Nenhum e-mail valido para IDQUALIF="
                    + registro.idQualif + " CODPARC=" + registro.codParc);
            }
        }

        System.out.println("[NotifVenc] Processamento concluido. Registros view="
            + registros.size() + " enviados=" + enviados + " ja notificados hoje=" + ignorados);
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
            sql.appendSql("     CODPARC, ");
            sql.appendSql("     NOMEPARC, ");
            sql.appendSql("     EMAIL_FORNECEDOR, ");
            sql.appendSql("     EMAIL_NOTIFICACAO_EMPRESA, ");
            sql.appendSql("     NOME_DOCUMENTO, ");
            sql.appendSql("     DATAVALIDADE, ");
            sql.appendSql("     DIAS_RESTANTES, ");
            sql.appendSql("     TIPO_NOTIFICACAO ");
            sql.appendSql(" FROM ");
            sql.appendSql(VIEW_VENCIMENTO);
            sql.appendSql(" WHERE ENVIAR_NOTIFICACAO = 'S' ");

            rset = sql.executeQuery();
            while (rset.next()) {
                RegistroVencimento reg = new RegistroVencimento();
                reg.idQualif = rset.getBigDecimal("IDQUALIF");
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
                lista.add(reg);
            }
        } finally {
            fecharSql(rset, sql);
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }

        return lista;
    }

    private static boolean enviarNotificacoes(RegistroVencimento registro) throws Exception {
        String assunto = resolverAssunto(registro.tipoNotificacao);
        String mensagem = EnviarEmailUtil.montarMensagemVencimento(
            registro.tipoNotificacao,
            registro.nomeDocumento,
            formatarData(registro.dataValidade),
            registro.diasRestantes
        );

        boolean enviou = false;

        if (temEmail(registro.emailFornecedor)) {
            EnviarEmailUtil.enviarHtmlNaFila(registro.emailFornecedor.trim(), assunto, mensagem);
            enviou = true;
            System.out.println("[NotifVenc] Fornecedor IDQUALIF=" + registro.idQualif
                + " " + registro.nomeParc + " email=" + registro.emailFornecedor);
        }

        if (temEmail(registro.emailEmpresa)) {
            String emailEmpresa = registro.emailEmpresa.trim();
            if (!emailEmpresa.equalsIgnoreCase(
                registro.emailFornecedor != null ? registro.emailFornecedor.trim() : "")) {
                EnviarEmailUtil.enviarHtmlNaFila(emailEmpresa, assunto, mensagem);
                enviou = true;
                System.out.println("[NotifVenc] Empresa IDQUALIF=" + registro.idQualif
                    + " email=" + emailEmpresa);
            }
        }

        return enviou;
    }

    private static String resolverAssunto(String tipoNotificacao) {
        if (TIPO_VENCIMENTO_HOJE.equals(tipoNotificacao)) {
            return "Vencimento de qualificacao - documento vence hoje";
        }
        return "Aviso de vencimento - qualificacao de fornecedor";
    }

    private static boolean jaNotificadoHoje(BigDecimal idQualif, String tipoNotificacao) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT 1 ");
            sql.appendSql(" FROM ");
            sql.appendSql(TABELA_LOG);
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.appendSql(" AND TIPO_NOTIFICACAO = :TIPO_NOTIFICACAO ");
            sql.appendSql(" AND TRUNC(DTNOTIF) = TRUNC(SYSDATE) ");

            sql.setNamedParameter("IDQUALIF", idQualif);
            sql.setNamedParameter("TIPO_NOTIFICACAO", tipoNotificacao);

            rset = sql.executeQuery();
            return rset.next();
        } finally {
            fecharSql(rset, sql);
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }

    private static void registrarEnvio(BigDecimal idQualif, String tipoNotificacao) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" INSERT INTO ");
            sql.appendSql(TABELA_LOG);
            sql.appendSql(" (IDLOG, IDQUALIF, TIPO_NOTIFICACAO, DTNOTIF, DHENVIO) ");
            sql.appendSql(" VALUES ( ");
            sql.appendSql(" (SELECT NVL(MAX(IDLOG), 0) + 1 FROM ");
            sql.appendSql(TABELA_LOG);
            sql.appendSql("), ");
            sql.appendSql(" :IDQUALIF, :TIPO_NOTIFICACAO, TRUNC(SYSDATE), SYSDATE ) ");

            sql.setNamedParameter("IDQUALIF", idQualif);
            sql.setNamedParameter("TIPO_NOTIFICACAO", tipoNotificacao);
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

    private static final class RegistroVencimento {
        private BigDecimal idQualif;
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
