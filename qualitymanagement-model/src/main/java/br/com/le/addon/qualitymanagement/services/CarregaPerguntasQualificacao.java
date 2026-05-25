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
import java.util.Objects;
import java.util.Set;

/**
 * Fluxo:
 * 1. QualificacaoFornecedor (pai) informa IDQUALIF e IDQUEST.
 * 2. Questionarios (TGQQUESTQUALIF) valida o questionario pelo IDQUEST.
 * 3. PerguntasQuestionario (TGQPERGQUEST, filha de Questionarios) fornece os IDPERG.
 * 4. RespostaFornecedor (TGQQUALIFRESP) recebe PK (IDQUALIF, IDPERG).
 */
public final class CarregaPerguntasQualificacao {

    private static final String INST_QUESTIONARIO = "Questionarios";
    private static final String INST_PERGUNTA = "PerguntasQuestionario";
    private static final String INST_RESPOSTA = "RespostaFornecedor";

    private CarregaPerguntasQualificacao() {
    }

    public static void carregarAoSalvar(DynamicVO qualificacaoVo, JdbcWrapper jdbc) throws Exception {
        carregarAoSalvar(qualificacaoVo, jdbc, false);
    }

    public static void carregarAoSalvar(DynamicVO qualificacaoVo, JdbcWrapper jdbc, boolean apenasInclusao)
        throws Exception {
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

        if (!questionarioAtivo(idQuest)) {
            System.out.println("[CarregaPerguntas] Questionario inativo ou nao encontrado. IDQUEST=" + idQuest);
            return;
        }

        if (apenasInclusao && contarRespostas(jdbc, idQualif) > 0) {
            System.out.println("[CarregaPerguntas] Ignorado no insert: ja existem respostas para IDQUALIF=" + idQualif);
            return;
        }

        Set<BigDecimal> perguntasExistentes = carregarIdPergExistentes(idQualif);
        List<BigDecimal> idPergsQuestionario = listarIdPergDoQuestionario(idQuest);
        int criadas = 0;

        for (BigDecimal idPerg : idPergsQuestionario) {
            if (idPerg == null || perguntasExistentes.contains(idPerg)) {
                continue;
            }
            criarResposta(idQualif, idPerg);
            perguntasExistentes.add(idPerg);
            criadas++;
        }

        System.out.println("[CarregaPerguntas] IDQUALIF=" + idQualif
            + " IDQUEST=" + idQuest
            + " perguntas no questionario=" + idPergsQuestionario.size()
            + " respostas criadas=" + criadas);
    }

    /**
     * Ao trocar IDQUEST no cabecalho: remove todas as respostas da qualificacao
     * e recria conforme as perguntas do novo questionario.
     */
    public static void carregarAoTrocarQuestionario(DynamicVO qualificacaoVo, JdbcWrapper jdbc) throws Exception {
        if (qualificacaoVo == null) {
            return;
        }

        BigDecimal idQualif = qualificacaoVo.asBigDecimal("IDQUALIF");
        BigDecimal idQuest = qualificacaoVo.asBigDecimal("IDQUEST");

        if (idQualif == null) {
            System.out.println("[CarregaPerguntas] Troca questionario ignorada: IDQUALIF nulo.");
            return;
        }

        if (idQuest == null) {
            System.out.println("[CarregaPerguntas] Troca questionario ignorada: IDQUEST nulo para IDQUALIF=" + idQualif);
            return;
        }

        if (!questionarioAtivo(idQuest)) {
            System.out.println("[CarregaPerguntas] Questionario inativo ou nao encontrado. IDQUEST=" + idQuest);
            return;
        }

        int excluidas = excluirRespostasPorQualificacao(idQualif, jdbc);
        List<BigDecimal> idPergsQuestionario = listarIdPergDoQuestionario(idQuest);
        int criadas = 0;

        for (BigDecimal idPerg : idPergsQuestionario) {
            if (idPerg == null) {
                continue;
            }
            criarResposta(idQualif, idPerg);
            criadas++;
        }

        System.out.println("[CarregaPerguntas] Troca questionario IDQUALIF=" + idQualif
            + " IDQUEST=" + idQuest
            + " respostas excluidas=" + excluidas
            + " respostas criadas=" + criadas);
    }

    private static int excluirRespostasPorQualificacao(BigDecimal idQualif, JdbcWrapper jdbc) throws Exception {
        if (jdbc != null) {
            int excluidas = excluirRespostasPorQualificacaoSql(idQualif, jdbc);
            if (contarRespostas(jdbc, idQualif) == 0) {
                return excluidas;
            }
        }
        return excluirRespostasPorQualificacaoJape(idQualif);
    }

    private static int excluirRespostasPorQualificacaoJape(BigDecimal idQualif) throws Exception {
        int excluidas = 0;
        JapeWrapper dao = JapeFactory.dao(INST_RESPOSTA);
        Collection<DynamicVO> registros = dao.find("IDQUALIF = ?", idQualif);

        if (registros == null || registros.isEmpty()) {
            return 0;
        }

        for (DynamicVO vo : registros) {
            dao.delete(vo);
            excluidas++;
        }
        return excluidas;
    }

    private static int excluirRespostasPorQualificacaoSql(BigDecimal idQualif, JdbcWrapper jdbc) throws Exception {
        if (jdbc == null) {
            return excluirRespostasPorQualificacaoJape(idQualif);
        }

        int qtde = contarRespostas(jdbc, idQualif);
        if (qtde == 0) {
            return 0;
        }

        NativeSql sql = null;
        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" DELETE FROM TGQQUALIFRESP ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.setNamedParameter("IDQUALIF", idQualif);
            sql.executeUpdate();
            return qtde;
        } finally {
            if (sql != null) {
                NativeSql.releaseResources(sql);
            }
        }
    }

    private static boolean questionarioAtivo(BigDecimal idQuest) throws Exception {
        if (questionarioAtivoJape(idQuest)) {
            return true;
        }
        return questionarioAtivoSql(idQuest);
    }

    private static boolean questionarioAtivoJape(BigDecimal idQuest) throws Exception {
        try {
            JapeWrapper dao = JapeFactory.dao(INST_QUESTIONARIO);
            Collection<DynamicVO> registros = dao.find("IDQUEST = ? AND NVL(INATIVO, 'N') = 'N'", idQuest);
            return registros != null && !registros.isEmpty();
        } catch (Exception e) {
            System.out.println("[CarregaPerguntas] Jape " + INST_QUESTIONARIO + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean questionarioAtivoSql(BigDecimal idQuest) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT 1 ");
            sql.appendSql(" FROM TGQQUESTQUALIF ");
            sql.appendSql(" WHERE IDQUEST = :IDQUEST ");
            sql.appendSql(" AND NVL(INATIVO, 'N') = 'N' ");
            sql.setNamedParameter("IDQUEST", idQuest);

            rset = sql.executeQuery();
            return rset.next();
        } finally {
            fecharSql(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static List<BigDecimal> listarIdPergDoQuestionario(BigDecimal idQuest) throws Exception {
        List<BigDecimal> idPergs = listarIdPergDoQuestionarioJape(idQuest);

        if (idPergs.isEmpty()) {
            idPergs = listarIdPergDoQuestionarioSql(idQuest);
        }

        return idPergs;
    }

    /**
     * PerguntasQuestionario (filha) filtrada pelo IDQUEST do cabecalho.
     */
    private static List<BigDecimal> listarIdPergDoQuestionarioJape(BigDecimal idQuest) throws Exception {
        List<BigDecimal> idPergs = new ArrayList<>();

        try {
            JapeWrapper dao = JapeFactory.dao(INST_PERGUNTA);
            Collection<DynamicVO> registros = dao.find("IDQUEST = ?", idQuest);

            if (registros == null || registros.isEmpty()) {
                return idPergs;
            }

            for (DynamicVO vo : registros) {
                BigDecimal idPerg = vo.asBigDecimal("IDPERG");
                if (idPerg != null) {
                    idPergs.add(idPerg);
                }
            }
        } catch (Exception e) {
            System.out.println("[CarregaPerguntas] Jape " + INST_PERGUNTA + ": " + e.getMessage());
        }

        return idPergs;
    }

    private static List<BigDecimal> listarIdPergDoQuestionarioSql(BigDecimal idQuest) throws Exception {
        List<BigDecimal> idPergs = new ArrayList<>();

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT IDPERG ");
            sql.appendSql(" FROM TGQPERGQUEST ");
            sql.appendSql(" WHERE IDQUEST = :IDQUEST ");
            sql.appendSql(" ORDER BY NVL(ORDENACAO, IDPERG), IDPERG ");
            sql.setNamedParameter("IDQUEST", idQuest);

            rset = sql.executeQuery();
            while (rset.next()) {
                BigDecimal idPerg = rset.getBigDecimal("IDPERG");
                if (idPerg != null) {
                    idPergs.add(idPerg);
                }
            }
        } finally {
            fecharSql(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return idPergs;
    }

    private static Set<BigDecimal> carregarIdPergExistentes(BigDecimal idQualif) throws Exception {
        Set<BigDecimal> existentes = new HashSet<>();
        existentes.addAll(carregarIdPergExistentesJape(idQualif));
        existentes.addAll(carregarIdPergExistentesSql(idQualif));
        return existentes;
    }

    private static Set<BigDecimal> carregarIdPergExistentesJape(BigDecimal idQualif) throws Exception {
        Set<BigDecimal> existentes = new HashSet<>();

        JapeWrapper dao = JapeFactory.dao(INST_RESPOSTA);
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

    private static Set<BigDecimal> carregarIdPergExistentesSql(BigDecimal idQualif) throws Exception {
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
            fecharSql(rset, sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return existentes;
    }

    /**
     * PK RespostaFornecedor: IDQUALIF + IDPERG (sem IDQUEST no insert).
     */
    private static void criarResposta(BigDecimal idQualif, BigDecimal idPerg) throws Exception {
        JapeWrapper dao = JapeFactory.dao(INST_RESPOSTA);
        FluidCreateVO createVO = dao.create();
        createVO.set("IDQUALIF", idQualif);
        createVO.set("IDPERG", idPerg);
        createVO.save();
    }

    private static int contarRespostas(JdbcWrapper jdbc, BigDecimal idQualif) throws Exception {
        if (jdbc == null) {
            return carregarIdPergExistentesSql(idQualif).size();
        }

        NativeSql sql = null;
        ResultSet rset = null;

        try {
            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT COUNT(1) AS QTDE ");
            sql.appendSql(" FROM TGQQUALIFRESP ");
            sql.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sql.setNamedParameter("IDQUALIF", idQualif);

            rset = sql.executeQuery();
            if (rset.next()) {
                return rset.getInt("QTDE");
            }
            return 0;
        } finally {
            fecharSql(rset, sql);
        }
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

    public static boolean idQuestionarioAlterado(DynamicVO vo, DynamicVO oldVo) {
        if (vo == null) {
            return false;
        }
        BigDecimal idQuest = vo.asBigDecimal("IDQUEST");
        if (idQuest == null) {
            return false;
        }
        if (oldVo == null) {
            return true;
        }
        return !Objects.equals(idQuest, oldVo.asBigDecimal("IDQUEST"));
    }
}
