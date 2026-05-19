package br.com.le.addon.qualitymanagement.services;

import br.com.le.addon.qualitymanagement.utils.ValidaNumero;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CalculaPontuacaoQualificacao {

    private CalculaPontuacaoQualificacao() {
    }

    public static ResultadoPontuacao calcularEAtualizar(BigDecimal idQualif, BigDecimal idQuest) throws Exception {
        if (idQualif == null) {
            throw new IllegalArgumentException("IDQUALIF nao informado.");
        }

        Map<BigDecimal, String> respostas = carregarRespostas(idQualif, idQuest);

        if (respostas.isEmpty()) {
            throw new Exception("Nenhuma resposta encontrada para esta qualificacao.");
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

        return new ResultadoPontuacao(pontosFinais, resultadoIqf, respostas.size());
    }

    private static Map<BigDecimal, String> carregarRespostas(BigDecimal idQualif, BigDecimal idQuest) throws Exception {
        Map<BigDecimal, String> respostas = new LinkedHashMap<>();

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT IDPERG, UPPER(TRIM(RESPOSTA)) AS RESPOSTA ");
            sql.appendSql(" FROM TGQQUALIFRESP ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");

            if (idQuest != null) {
                sql.appendSql(" AND IDQUEST = :IDQUEST ");
            }

            sql.setNamedParameter("IDQUALIF", idQualif);
            if (idQuest != null) {
                sql.setNamedParameter("IDQUEST", idQuest);
            }

            rset = sql.executeQuery();
            while (rset.next()) {
                BigDecimal idPerg = rset.getBigDecimal("IDPERG");
                String resposta = rset.getString("RESPOSTA");
                if (idPerg != null) {
                    respostas.put(idPerg, resposta != null ? resposta : "");
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
                NativeSql.releaseResources(sql);
            }
            JdbcWrapper.closeSession(jdbc);
        }

        return respostas;
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
