package br.com.le.addon.qualitymanagement.services;

import br.com.le.addon.qualitymanagement.utils.ValidaNumero;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CalculaPontuacaoQualificacao {

    private CalculaPontuacaoQualificacao() {
    }

    public static ResultadoPontuacao calcularEAtualizar(BigDecimal idQualif) throws Exception {
        if (idQualif == null) {
            throw new IllegalArgumentException("IDQUALIF nao informado.");
        }

        Map<BigDecimal, String> respostas = carregarRespostas(idQualif);

        if (respostas.isEmpty()) {
            throw new Exception(
                "Nenhuma resposta encontrada para IDQUALIF=" + idQualif
                    + ". Salve os registros na aba Respostas do Questionario e tente novamente."
            );
        }

        BigDecimal pontosAcumulados = BigDecimal.ZERO;
        for (String resposta : respostas.values()) {
            pontosAcumulados = pontosAcumulados.add(pontosDaResposta(resposta));
        }

        BigDecimal qtdePerguntas = new BigDecimal(respostas.size());
        BigDecimal pontosFinais = pontosAcumulados
            .divide(qtdePerguntas, 10, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP);

        String resultadoIqf = classificarIqf(pontosFinais);

        atualizarQualificacao(idQualif, pontosFinais, resultadoIqf);

        System.out.println("[CalculaPontuacao] IDQUALIF=" + idQualif
            + " respostas=" + respostas.size()
            + " pontuacao=" + pontosFinais
            + " IQF=" + resultadoIqf);

        return new ResultadoPontuacao(pontosFinais, resultadoIqf, respostas.size());
    }

  /**
     * Carrega respostas por IDQUALIF (PK composta IDQUALIF + IDPERG em TGQQUALIFRESP).
     * Usa Jape na transacao corrente; SQL nativo como fallback.
     */
    private static Map<BigDecimal, String> carregarRespostas(BigDecimal idQualif) throws Exception {
        Map<BigDecimal, String> respostas = carregarRespostasJape(idQualif);

        if (respostas.isEmpty()) {
            respostas = carregarRespostasSql(idQualif);
        }

        return respostas;
    }

    private static Map<BigDecimal, String> carregarRespostasJape(BigDecimal idQualif) throws Exception {
        Map<BigDecimal, String> respostas = new LinkedHashMap<>();

        JapeWrapper dao = JapeFactory.dao("RespostaFornecedor");
        Collection<DynamicVO> registros = dao.find("IDQUALIF = ?", idQualif);

        if (registros == null || registros.isEmpty()) {
            return respostas;
        }

        for (DynamicVO vo : registros) {
            adicionarResposta(respostas, vo.asBigDecimal("IDPERG"), vo.asString("RESPOSTA"));
        }

        return respostas;
    }

    private static Map<BigDecimal, String> carregarRespostasSql(BigDecimal idQualif) throws Exception {
        Map<BigDecimal, String> respostas = new LinkedHashMap<>();

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT IDPERG, RESPOSTA ");
            sql.appendSql(" FROM TGQQUALIFRESP ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.setNamedParameter("IDQUALIF", idQualif);

            rset = sql.executeQuery();
            while (rset.next()) {
                adicionarResposta(respostas, rset.getBigDecimal("IDPERG"), rset.getString("RESPOSTA"));
            }
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
            JdbcWrapper.closeSession(jdbc);
        }

        return respostas;
    }

    private static void adicionarResposta(Map<BigDecimal, String> respostas, BigDecimal idPerg, String resposta) {
        if (idPerg == null) {
            return;
        }
        String normalizada = normalizarResposta(resposta);
        if (!normalizada.isEmpty()) {
            respostas.put(idPerg, normalizada);
        }
    }

    private static String normalizarResposta(String resposta) {
        if (resposta == null) {
            return "";
        }
        return resposta.trim().toUpperCase();
    }

    private static void atualizarQualificacao(BigDecimal idQualif, BigDecimal pontosFinais, String resultadoIqf)
        throws Exception {
        JapeWrapper qualificacaoDAO = JapeFactory.dao("QualificacaoFornecedor");
        FluidUpdateVO updateVO = qualificacaoDAO.prepareToUpdateByPK(new Object[] { idQualif });
        updateVO.set("PONTUACAO", pontosFinais);
        updateVO.set("RESULTADOIQF", resultadoIqf);
        updateVO.update();
    }

    static BigDecimal pontosDaResposta(String resposta) {
        if (resposta == null || resposta.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (isRespostaPositiva(resposta)) {
            return BigDecimal.ONE;
        }

        if (isRespostaNegativa(resposta)) {
            return BigDecimal.ZERO;
        }

        if (ValidaNumero.isNumeric(resposta)) {
            return multiplicadorFaixa(new BigDecimal(resposta));
        }

        return BigDecimal.ZERO;
    }

    private static boolean isRespostaPositiva(String resposta) {
        return "SIM".equals(resposta) || "S".equals(resposta) || "YES".equals(resposta);
    }

    private static boolean isRespostaNegativa(String resposta) {
        return "NAO".equals(resposta) || "N?O".equals(resposta) || "N".equals(resposta) || "NO".equals(resposta);
    }

    private static BigDecimal multiplicadorFaixa(BigDecimal valor) {
        if (valor.compareTo(new BigDecimal(10)) < 0) {
            return new BigDecimal("0.15");
        }
        if (valor.compareTo(new BigDecimal(29)) <= 0) {
            return new BigDecimal("0.30");
        }
        if (valor.compareTo(new BigDecimal(49)) <= 0) {
            return new BigDecimal("0.45");
        }
        if (valor.compareTo(new BigDecimal(69)) <= 0) {
            return new BigDecimal("0.60");
        }
        if (valor.compareTo(new BigDecimal(89)) <= 0) {
            return new BigDecimal("0.75");
        }
        return new BigDecimal("0.90");
    }

    private static String classificarIqf(BigDecimal pontosFinais) {
        if (pontosFinais.compareTo(new BigDecimal(80)) >= 0) {
            return "A";
        }
        if (pontosFinais.compareTo(new BigDecimal(50)) >= 0) {
            return "B";
        }
        return "T";
    }

    public static final class ResultadoPontuacao {
        private final BigDecimal pontuacao;
        private final String resultadoIqf;
        private final int quantidadeRespostas;

        public ResultadoPontuacao(BigDecimal pontuacao, String resultadoIqf, int quantidadeRespostas) {
            this.pontuacao = pontuacao;
            this.resultadoIqf = resultadoIqf;
            this.quantidadeRespostas = quantidadeRespostas;
        }

        public BigDecimal getPontuacao() {
            return pontuacao;
        }

        public String getResultadoIqf() {
            return resultadoIqf;
        }

        public int getQuantidadeRespostas() {
            return quantidadeRespostas;
        }
    }
}
