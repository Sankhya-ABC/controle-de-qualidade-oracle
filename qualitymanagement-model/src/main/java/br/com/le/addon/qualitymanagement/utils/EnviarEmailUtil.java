package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import com.sankhya.util.BigDecimalUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class EnviarEmailUtil {

    /**
     * @deprecated Usar {@link #enviarQuestionarioComAnexo}. Mantido apenas para compatibilidade com codigo legado.
     */
    @Deprecated
    public static void enviarQuestionario(String emailFornec, String urlQuestionario, String empresa) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            String msgmHtml = (String) ParameterUtils.getParameter("HTMLEMAILQUEST");
            String mensagem = msgmHtml
                .replace("{URL}", urlQuestionario != null ? urlQuestionario : "")
                .replace("{EMPRESA}", empresa != null ? empresa : "");
            String assunto = "Qualificacao de Fornecedor";
            Collection<AnexoEmail> anexos = new ArrayList<>();
            criarEmailNaFila(dwf, emailFornec, assunto, mensagem, null, anexos);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    public static void enviarQuestionarioComAnexo(
        String emailFornec,
        String empresa,
        String blocoDocumentosHtml,
        Collection<AnexoEmail> anexos
    ) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            String msgmHtml = (String) ParameterUtils.getParameter("HTMLEMAILQUEST");
            if (msgmHtml == null || msgmHtml.trim().isEmpty()) {
                msgmHtml = templatePadraoQuestionario();
            }
            String mensagem = msgmHtml
                .replace("{EMPRESA}", empresa != null ? empresa : "")
                .replace("{DOCUMENTOS}", blocoDocumentosHtml != null ? blocoDocumentosHtml : "");
            String assunto = "Qualificacao de Fornecedor";
            Collection<AnexoEmail> anexosEnvio = anexos != null ? anexos : new ArrayList<AnexoEmail>();
            criarEmailNaFila(dwf, emailFornec, assunto, mensagem, null, anexosEnvio);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static String templatePadraoQuestionario() {
        return "<p>Prezado(a) fornecedor(a),</p>"
            + "<p>Estamos encaminhando em anexo o questionario de qualificacao para preenchimento e retorno.</p>"
            + "<p>A empresa {EMPRESA} solicita tambem o envio dos documentos obrigatorios vinculados ao processo "
            + "de qualificacao cadastral.</p>"
            + "<p><strong>Documentos solicitados:</strong></p>"
            + "{DOCUMENTOS}"
            + "<p>Pedimos que o questionario preenchido e os documentos solicitados sejam enviados dentro do prazo "
            + "estabelecido para continuidade do processo de qualificacao/homologacao do fornecedor.</p>"
            + "<p>Em caso de duvidas, favor entrar em contato com nossa equipe responsavel.</p>"
            + "<p>Atenciosamente,</p>";
    }

    public static void enviarRncFornecedor(String emailFornec, String empresa, String detalhamento, String numRnc) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            String msgmHtml = (String)ParameterUtils.getParameter("HTMLRNCFORNEC");
            String mensagem = msgmHtml.replace("{RNC}", numRnc).replace("{DETALHAMENTO}", detalhamento).replace("{EMPRESA}", empresa);
            String assunto = "Qualificacao de Fornecedor";
            Collection<AnexoEmail> anexos = new ArrayList<>();
            criarEmailNaFila(dwf, emailFornec, assunto, mensagem, null, anexos);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    public static void EnviarNotificacaoFornec(String emailFornec, String mensagem) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            String assunto = "Atualizade Certificados";
            Collection<AnexoEmail> anexos = new ArrayList<>();
            criarEmailNaFila(dwf, emailFornec, assunto, mensagem, null, anexos);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    public static void EnviarNotificacaoAcoes(String emailParc, String mensagem, String assunto) throws Exception {
        enviarHtmlNaFila(emailParc, assunto, mensagem);
    }

    public static void enviarHtmlNaFila(String destinatario, String assunto, String mensagemHtml) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            Collection<AnexoEmail> anexos = new ArrayList<>();
            criarEmailNaFila(dwf, destinatario, assunto, mensagemHtml, null, anexos);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    public static String montarMensagemVencimento(
        String tipoNotificacao,
        String nomeDocumento,
        String dataVencimento,
        BigDecimal diasRestantes
    ) throws Exception {
        boolean venceHoje = "VENCIMENTO_HOJE".equals(tipoNotificacao);
        String parametro = venceHoje ? "HTMLEMAILVENCHOJE" : "HTMLEMAILVENC";
        String template = (String) ParameterUtils.getParameter(parametro);

        if (template == null || template.trim().isEmpty()) {
            template = venceHoje ? templatePadraoVencimentoHoje() : templatePadraoVencimentoAviso();
        }

        String diasRestantesTexto = diasRestantes != null ? diasRestantes.toPlainString() : "";

        return template
            .replace("{NOME_DOCUMENTO}", valorSeguro(nomeDocumento))
            .replace("{DATA_VENCIMENTO}", valorSeguro(dataVencimento))
            .replace("{DIAS_RESTANTES}", diasRestantesTexto)
            .replace("[NOME_DOCUMENTO]", valorSeguro(nomeDocumento))
            .replace("[DATA_VENCIMENTO]", valorSeguro(dataVencimento))
            .replace("[DIAS_RESTANTES]", diasRestantesTexto);
    }

    private static String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private static String templatePadraoVencimentoAviso() {
        return "<p>Prezado(a) fornecedor(a),</p>"
            + "<p>Informamos que o documento/questionario abaixo esta proximo do vencimento conforme "
            + "parametrizacao definida no sistema.</p>"
            + "<p><strong>Detalhes do vencimento:</strong></p>"
            + "<ul>"
            + "<li>Documento/Questionario: {NOME_DOCUMENTO}</li>"
            + "<li>Data de vencimento: {DATA_VENCIMENTO}</li>"
            + "<li>Dias restantes para vencimento: {DIAS_RESTANTES}</li>"
            + "</ul>"
            + "<p>Solicitamos que realize a atualizacao ou renovacao do documento/questionario dentro do prazo "
            + "para evitar pendencias em seu cadastro.</p>"
            + "<p>Em caso de duvidas, favor entrar em contato com nossa equipe responsavel.</p>"
            + "<p>Atenciosamente,</p>";
    }

    private static String templatePadraoVencimentoHoje() {
        return "<p>Prezado(a) fornecedor(a),</p>"
            + "<p>Informamos que o documento/questionario abaixo venceu na data de hoje.</p>"
            + "<p><strong>Detalhes do vencimento:</strong></p>"
            + "<ul>"
            + "<li>Documento/Questionario: {NOME_DOCUMENTO}</li>"
            + "<li>Data de vencimento: {DATA_VENCIMENTO}</li>"
            + "</ul>"
            + "<p>Solicitamos a regularizacao o mais breve possivel para evitar possiveis bloqueios de "
            + "fornecimento e restricoes em seu cadastro junto a nossa empresa.</p>"
            + "<p>Em caso de duvidas ou para envio da documentacao atualizada, favor entrar em contato com "
            + "nossa equipe responsavel.</p>"
            + "<p>Atenciosamente,</p>";
    }

    private static void criarEmailNaFila(EntityFacade dwfFacade, String destinatario, String assunto, String mensagem, BigDecimal codSMTP, Collection<AnexoEmail> anexos) throws Exception {
        DynamicVO filaVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("MSDFilaMensagem");
        filaVO.setProperty("EMAIL", destinatario);
        filaVO.setProperty("CODCON", BigDecimal.ZERO);
        filaVO.setProperty("CODMSG", null);
        filaVO.setProperty("STATUS", "Pendente");
        filaVO.setProperty("TIPOENVIO", "E");
        filaVO.setProperty("MAXTENTENVIO", BigDecimalUtil.valueOf(3L));
        filaVO.setProperty("ASSUNTO", assunto);
        filaVO.setProperty("MIMETYPE", "text/html");
        filaVO.setProperty("MENSAGEM", mensagem.toCharArray());
        filaVO.setProperty("TIPODOC", "E");
        filaVO.setProperty("CODSMTP", codSMTP);
        BigDecimal codFila = ((DynamicVO)dwfFacade.createEntity("MSDFilaMensagem", (EntityVO)filaVO)
            .getValueObject()).asBigDecimal("CODFILA");
        for (AnexoEmail anexo : anexos) {
            DynamicVO anexoVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("AnexoMensagem");
            anexoVO.setProperty("ANEXO", anexo.getData());
            anexoVO.setProperty("NOMEARQUIVO", anexo.getFileName());
            anexoVO.setProperty("TIPO", anexo.getMimeType());
            BigDecimal nuAnexo = ((DynamicVO)dwfFacade
                .createEntity("AnexoMensagem", (EntityVO)anexoVO).getValueObject())
                .asBigDecimal("NUANEXO");
            ((FluidCreateVO)((FluidCreateVO)JapeFactory.dao("AnexoPorMensagem").create().set("CODFILA", codFila))
                .set("NUANEXO", nuAnexo)).save();
        }
    }
}
