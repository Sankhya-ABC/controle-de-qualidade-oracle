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

    private static final String INST_CRITERIO = "CriterioQualific";
    private static final String INST_CONFIG = "ConfigQualidade";

    private CalculaPontuacaoQualificacao() {
    }

    /**
     * Usado pelo botao de acao. Exige ao menos uma resposta salva.
     */
    public static ResultadoPontuacao calcularEAtualizar(BigDecimal idQualif) throws Exception {
        if (idQualif == null) {
            throw new IllegalArgumentException("IDQUALIF nao informado.");
        }

        Map<BigDecimal, String> respostas = carregarRespostas(idQualif);
        return aplicarCalculo(idQualif, respostas);
    }

    /**
     * Usado pelo listener em RespostaFornecedor (insert/update/delete).
     * Inclui a linha do evento quando o banco ainda nao refletiu o insert/update.
     */
    public static void recalcularNoEvento(DynamicVO respostaVo, boolean registroExcluido) throws Exception {
        if (respostaVo == null) {
            return;
        }

        BigDecimal idQualif = respostaVo.asBigDecimal("IDQUALIF");
        if (idQualif == null) {
            System.out.println("[CalculaPontuacao] Evento ignorado: IDQUALIF nulo.");
            return;
        }

        Map<BigDecimal, String> respostas = carregarRespostas(idQualif);

        if (!registroExcluido) {
            adicionarResposta(respostas, respostaVo.asBigDecimal("IDPERG"), respostaVo.asString("RESPOSTA"));
        }

        aplicarCalculo(idQualif, respostas);
    }

    private static ResultadoPontuacao aplicarCalculo(BigDecimal idQualif, Map<BigDecimal, String> respostas)
        throws Exception {
        BigDecimal pontosFinais = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String resultadoIqf = "T";
        String qualificacao = null;

        if (respostas.isEmpty()) {
            qualificacao = resolverQualificacaoPorPontuacao(null, true);
            resultadoIqf = classificarIqf(pontosFinais);
        } else {
            BigDecimal pontosAcumulados = BigDecimal.ZERO;
            for (String resposta : respostas.values()) {
                pontosAcumulados = pontosAcumulados.add(pontosDaResposta(resposta));
            }

            BigDecimal qtdePerguntas = new BigDecimal(respostas.size());
            pontosFinais = pontosAcumulados
                .divide(qtdePerguntas, 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
            resultadoIqf = classificarIqf(pontosFinais);
            qualificacao = resolverQualificacaoPorPontuacao(pontosFinais, false);
        }

        atualizarQualificacao(idQualif, pontosFinais, resultadoIqf, qualificacao);

        System.out.println("[CalculaPontuacao] IDQUALIF=" + idQualif
            + " respostas=" + respostas.size()
            + " pontuacao=" + pontosFinais
            + " IQF=" + resultadoIqf
            + " qualificacao=" + qualificacao);

        return new ResultadoPontuacao(pontosFinais, resultadoIqf, qualificacao, respostas.size());
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

    private static void atualizarQualificacao(
        BigDecimal idQualif,
        BigDecimal pontosFinais,
        String resultadoIqf,
        String qualificacao
    ) throws Exception {
        JapeWrapper qualificacaoDAO = JapeFactory.dao("QualificacaoFornecedor");
        FluidUpdateVO updateVO = qualificacaoDAO.prepareToUpdateByPK(new Object[] { idQualif });
        updateVO.set("PONTUACAO", pontosFinais);
        updateVO.set("RESULTADOIQF", resultadoIqf);
        if (qualificacao != null && !qualificacao.isEmpty()) {
            updateVO.set("QUALIFICACAO", qualificacao);
        }
        updateVO.update();
    }

    /**
     * Resolve o valor do campo QUALIFICACAO (lista) conforme TGQCRITERIOQF / CriterioQualific da config.
     */
    private static String resolverQualificacaoPorPontuacao(BigDecimal pontuacao, boolean semRespostasPreenchidas)
        throws Exception {
        BigDecimal idConfig = resolverIdConfig();

        if (idConfig == null) {
            System.out.println("[CalculaPontuacao] IDCONFIG nao encontrado para criterios de qualificacao.");
            return semRespostasPreenchidas ? resolverValorListaQualificacao("E") : null;
        }

        String descricaoCriterio = buscarDescricaoCriterio(idConfig, pontuacao, semRespostasPreenchidas);

        if (descricaoCriterio == null || descricaoCriterio.isEmpty()) {
            System.out.println("[CalculaPontuacao] Nenhum criterio encontrado para pontuacao=" + pontuacao
                + " semRespostas=" + semRespostasPreenchidas);
            return semRespostasPreenchidas ? resolverValorListaQualificacao("E") : null;
        }

        return resolverValorListaQualificacao(descricaoCriterio);
    }

    private static BigDecimal resolverIdConfig() throws Exception {
        try {
            JapeWrapper dao = JapeFactory.dao(INST_CONFIG);
            Collection<DynamicVO> configs = dao.find("1 = 1");
            if (configs != null && !configs.isEmpty()) {
                return configs.iterator().next().asBigDecimal("IDCONFIG");
            }
        } catch (Exception e) {
            System.out.println("[CalculaPontuacao] Jape " + INST_CONFIG + ": " + e.getMessage());
        }

        return resolverIdConfigSql();
    }

    private static BigDecimal resolverIdConfigSql() throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT IDCONFIG FROM TGQCONFIG WHERE ROWNUM <= 1 ");
            rset = sql.executeQuery();
            if (rset.next()) {
                return rset.getBigDecimal("IDCONFIG");
            }
            return null;
        } finally {
            fecharRecursos(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static String buscarDescricaoCriterio(
        BigDecimal idConfig,
        BigDecimal pontuacao,
        boolean semRespostasPreenchidas
    ) throws Exception {
        String descricao = buscarDescricaoCriterioJape(idConfig, pontuacao, semRespostasPreenchidas);

        if (descricao == null || descricao.isEmpty()) {
            descricao = buscarDescricaoCriterioSql(idConfig, pontuacao, semRespostasPreenchidas);
        }

        return descricao;
    }

    private static String buscarDescricaoCriterioJape(
        BigDecimal idConfig,
        BigDecimal pontuacao,
        boolean semRespostasPreenchidas
    ) throws Exception {
        try {
            JapeWrapper dao = JapeFactory.dao(INST_CRITERIO);
            Collection<DynamicVO> criterios = dao.find("IDCONFIG = ?", idConfig);

            if (criterios == null || criterios.isEmpty()) {
                return null;
            }

            if (semRespostasPreenchidas) {
                for (DynamicVO criterio : criterios) {
                    BigDecimal min = criterio.asBigDecimal("PONTOSMIN");
                    BigDecimal max = criterio.asBigDecimal("PONTOSMAX");
                    if (isFaixaSemResposta(min, max)) {
                        return criterio.asString("DESCRICAO");
                    }
                }
            } else if (pontuacao != null) {
                DynamicVO melhor = null;
                BigDecimal menorFaixa = null;

                for (DynamicVO criterio : criterios) {
                    BigDecimal min = criterio.asBigDecimal("PONTOSMIN");
                    BigDecimal max = criterio.asBigDecimal("PONTOSMAX");

                    if (!pontuacaoEstaNaFaixa(pontuacao, min, max) || isFaixaSemResposta(min, max)) {
                        continue;
                    }

                    BigDecimal largura = max.subtract(min);
                    if (melhor == null || largura.compareTo(menorFaixa) < 0) {
                        melhor = criterio;
                        menorFaixa = largura;
                    }
                }

                if (melhor != null) {
                    return melhor.asString("DESCRICAO");
                }
            }
        } catch (Exception e) {
            System.out.println("[CalculaPontuacao] Jape " + INST_CRITERIO + ": " + e.getMessage());
        }

        return null;
    }

    private static String buscarDescricaoCriterioSql(
        BigDecimal idConfig,
        BigDecimal pontuacao,
        boolean semRespostasPreenchidas
    ) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);

            if (semRespostasPreenchidas) {
                sql.appendSql(" SELECT DESCRICAO ");
                sql.appendSql(" FROM TGQCRITERIOQF ");
                sql.appendSql(" WHERE IDCONFIG = :IDCONFIG ");
                sql.appendSql(" AND PONTOSMIN = 0 AND PONTOSMAX = 0 ");
                sql.appendSql(" ORDER BY ID ");
                sql.setNamedParameter("IDCONFIG", idConfig);
            } else {
                sql.appendSql(" SELECT DESCRICAO ");
                sql.appendSql(" FROM TGQCRITERIOQF ");
                sql.appendSql(" WHERE IDCONFIG = :IDCONFIG ");
                sql.appendSql(" AND :PONTUACAO BETWEEN PONTOSMIN AND PONTOSMAX ");
                sql.appendSql(" ORDER BY (PONTOSMAX - PONTOSMIN), PONTOSMIN ");
                sql.setNamedParameter("IDCONFIG", idConfig);
                sql.setNamedParameter("PONTUACAO", pontuacao);
            }

            rset = sql.executeQuery();
            if (rset.next()) {
                return rset.getString("DESCRICAO");
            }
            return null;
        } finally {
            fecharRecursos(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static boolean isFaixaSemResposta(BigDecimal min, BigDecimal max) {
        return min != null && max != null
            && min.compareTo(BigDecimal.ZERO) == 0
            && max.compareTo(BigDecimal.ZERO) == 0;
    }

    private static boolean pontuacaoEstaNaFaixa(BigDecimal pontuacao, BigDecimal min, BigDecimal max) {
        if (pontuacao == null || min == null || max == null) {
            return false;
        }
        return pontuacao.compareTo(min) >= 0 && pontuacao.compareTo(max) <= 0;
    }

    /**
     * Converte descricao do criterio no valor da lista QUALIFICACAO: R, Q, D ou E (XML do dicionario).
     */
    private static String resolverValorListaQualificacao(String descricaoOuCodigo) throws Exception {
        if (descricaoOuCodigo == null || descricaoOuCodigo.trim().isEmpty()) {
            return null;
        }

        String texto = descricaoOuCodigo.trim();
        String codigo = texto.toUpperCase();

        if (isCodigoQualificacaoValido(codigo)) {
            return codigo;
        }

        codigo = buscarOpcaoQualificacaoSql(texto);
        if (isCodigoQualificacaoValido(codigo)) {
            return codigo;
        }

        codigo = mapearDescricaoParaCodigo(texto);
        if (isCodigoQualificacaoValido(codigo)) {
            return codigo;
        }

        System.out.println("[CalculaPontuacao] Nao foi possivel mapear QUALIFICACAO para: " + texto);
        return null;
    }

    private static boolean isCodigoQualificacaoValido(String codigo) {
        return "R".equals(codigo) || "Q".equals(codigo) || "D".equals(codigo) || "E".equals(codigo);
    }

    private static String buscarOpcaoQualificacaoSql(String descricaoCriterio) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT O.OPCAO ");
            sql.appendSql(" FROM TDDCAM C, TDDOPC O ");
            sql.appendSql(" WHERE C.NUCAMPO = O.NUCAMPO ");
            sql.appendSql(" AND C.NOMETAB = 'TGQQUALIFFORN' ");
            sql.appendSql(" AND C.NOMECAMPO = 'QUALIFICACAO' ");
            sql.appendSql(" AND TRIM(O.OPCAO) = TRIM(:TEXTO) ");
            sql.setNamedParameter("TEXTO", descricaoCriterio);

            rset = sql.executeQuery();
            if (rset.next()) {
                return rset.getString("OPCAO");
            }
        } finally {
            fecharRecursos(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return mapearDescricaoParaCodigo(descricaoCriterio);
    }

    /**
     * Mapeamento alinhado ao XML: R=Requalificacao, Q=Qualificado, D=Desqualificado, E=Em Processo.
     */
    private static String mapearDescricaoParaCodigo(String descricao) {
        String upper = descricao.toUpperCase();

        if (upper.contains("PROCESSO")) {
            return "E";
        }
        if (upper.contains("DESQUALIFIC")) {
            return "D";
        }
        if (upper.contains("REQUALIF")) {
            return "R";
        }
        if (upper.equals("QUALIFICADO") || upper.startsWith("QUALIFICADO")) {
            return "Q";
        }

        return null;
    }

    private static void fecharRecursos(ResultSet rset, NativeSql sql) {
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
        return "NAO".equals(resposta) || "NO".equals(resposta) || "N".equals(resposta) || "NO".equals(resposta);
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
        private final String qualificacao;
        private final int quantidadeRespostas;

        public ResultadoPontuacao(
            BigDecimal pontuacao,
            String resultadoIqf,
            String qualificacao,
            int quantidadeRespostas
        ) {
            this.pontuacao = pontuacao;
            this.resultadoIqf = resultadoIqf;
            this.qualificacao = qualificacao;
            this.quantidadeRespostas = quantidadeRespostas;
        }

        public BigDecimal getPontuacao() {
            return pontuacao;
        }

        public String getResultadoIqf() {
            return resultadoIqf;
        }

        public String getQualificacao() {
            return qualificacao;
        }

        public int getQuantidadeRespostas() {
            return quantidadeRespostas;
        }
    }
}
