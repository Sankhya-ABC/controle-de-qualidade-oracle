package br.com.le.addon.qualitymanagement.services;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cria linhas em RespostaFornecedor para cada pergunta do questionario vinculado
 * ao cabecalho QualificacaoFornecedor.
 */
public final class CarregaPerguntasQualificacao {

    private CarregaPerguntasQualificacao() {
    }

    /**
     * Usado pelo listener em QualificacaoFornecedor (insert/update).
     */
    public static void carregarAoSalvar(DynamicVO qualificacaoVo) throws Exception {
        if (qualificacaoVo == null) {
            return;
        }

        BigDecimal idQualif = qualificacaoVo.asBigDecimal("IDQUALIF");
        BigDecimal idQuest = qualificacaoVo.asBigDecimal("IDQUEST");

        if (idQualif == null) {
            System.out.println("[CarregaPerguntas] Ignorado: IDQUALIF nulo.");
            return;
        }

        if (idQuest == null) {
            System.out.println("[CarregaPerguntas] Ignorado: IDQUEST nulo para IDQUALIF=" + idQualif);
            return;
        }

        Set<BigDecimal> perguntasExistentes = carregarPerguntasExistentes(idQualif);
        List<BigDecimal> perguntasQuestionario = listarPerguntasDoQuestionario(idQuest);
        int criadas = 0;

        for (BigDecimal idPerg : perguntasQuestionario) {
            if (idPerg == null || perguntasExistentes.contains(idPerg)) {
                continue;
            }
            criarResposta(idQualif, idPerg, idQuest);
            perguntasExistentes.add(idPerg);
            criadas++;
        }

        System.out.println("[CarregaPerguntas] IDQUALIF=" + idQualif
            + " IDQUEST=" + idQuest
            + " respostas criadas=" + criadas);
    }

    private static Set<BigDecimal> carregarPerguntasExistentes(BigDecimal idQualif) throws Exception {
        Set<BigDecimal> existentes = carregarPerguntasExistentesJape(idQualif);

        if (existentes.isEmpty()) {
            existentes = carregarPerguntasExistentesSql(idQualif);
        }

        return existentes;
    }

    private static Set<BigDecimal> carregarPerguntasExistentesJape(BigDecimal idQualif) throws Exception {
        Set<BigDecimal> existentes = new HashSet<>();

        JapeWrapper dao = JapeFactory.dao("RespostaFornecedor");
        Collection<DynamicVO> registros = dao.find("IDQUALIF = ?", idQualif);

        if (registros == null || registros.isEmpty()) {
            return existentes;
        }

        for (DynamicVO vo : registros) {
            BigDecimal idPerg = vo.asBigDecimal("IDPERG");
            if (idPerg != null) {
                existentes.add(idPerg);
            }
        }

        return existentes;
    }

    private static Set<BigDecimal> carregarPerguntasExistentesSql(BigDecimal idQualif) throws Exception {
        Set<BigDecimal> existentes = new HashSet<>();

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT IDPERG ");
            sql.appendSql(" FROM TGQQUALIFRESP ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.setNamedParameter("IDQUALIF", idQualif);

            rset = sql.executeQuery();
            while (rset.next()) {
                BigDecimal idPerg = rset.getBigDecimal("IDPERG");
                if (idPerg != null) {
                    existentes.add(idPerg);
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

        return existentes;
    }

    private static List<BigDecimal> listarPerguntasDoQuestionario(BigDecimal idQuest) throws Exception {
        List<BigDecimal> perguntas = listarPerguntasDoQuestionarioJape(idQuest);

        if (perguntas.isEmpty()) {
            perguntas = listarPerguntasDoQuestionarioSql(idQuest);
        }

        return perguntas;
    }

    private static List<BigDecimal> listarPerguntasDoQuestionarioJape(BigDecimal idQuest) throws Exception {
        List<BigDecimal> perguntas = new ArrayList<>();

        try {
            JapeWrapper dao = JapeFactory.dao("PerguntaQuest");
            Collection<DynamicVO> registros = dao.find("IDQUEST = ?", idQuest);

            if (registros == null || registros.isEmpty()) {
                return perguntas;
            }

            for (DynamicVO vo : registros) {
                BigDecimal idPerg = vo.asBigDecimal("IDPERG");
                if (idPerg != null) {
                    perguntas.add(idPerg);
                }
            }
        } catch (Exception e) {
            System.out.println("[CarregaPerguntas] Jape PerguntaQuest indisponivel: " + e.getMessage());
        }

        return perguntas;
    }

    private static List<BigDecimal> listarPerguntasDoQuestionarioSql(BigDecimal idQuest) throws Exception {
        List<BigDecimal> perguntas = new ArrayList<>();

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT P.IDPERG ");
            sql.appendSql(" FROM TGQPERGQUEST P ");
            sql.appendSql(" INNER JOIN TGQQUESTQUALIF Q ON Q.IDQUEST = P.IDQUEST ");
            sql.appendSql(" WHERE P.IDQUEST = :IDQUEST ");
            sql.appendSql(" AND NVL(Q.INATIVO, 'N') = 'N' ");
            sql.appendSql(" ORDER BY NVL(P.ORDENACAO, P.IDPERG), P.IDPERG ");
            sql.setNamedParameter("IDQUEST", idQuest);

            rset = sql.executeQuery();
            while (rset.next()) {
                BigDecimal idPerg = rset.getBigDecimal("IDPERG");
                if (idPerg != null) {
                    perguntas.add(idPerg);
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

        return perguntas;
    }

    private static void criarResposta(BigDecimal idQualif, BigDecimal idPerg, BigDecimal idQuest) throws Exception {
        JapeWrapper dao = JapeFactory.dao("RespostaFornecedor");
        FluidCreateVO createVO = dao.create();
        createVO.set("IDQUALIF", idQualif);
        createVO.set("IDPERG", idPerg);
        createVO.set("IDQUEST", idQuest);
        createVO.save();
    }
}
