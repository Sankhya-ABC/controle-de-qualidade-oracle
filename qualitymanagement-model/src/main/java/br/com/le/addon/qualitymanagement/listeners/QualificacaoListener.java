package br.com.le.addon.qualitymanagement.listeners;

import br.com.le.addon.qualitymanagement.utils.ValidaNumero;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.studio.annotations.Listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recalcula PONTUACAO e RESULTADOIQF em TGQQUALIFFORN a cada alteracao em TGQQUALIFRESP.
 */
@Listener(instanceNames = {"RespostaFornecedor"})
public class QualificacaoListener extends PersistenceEventAdapter {

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        calcPontuacao(event, false);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        calcPontuacao(event, false);
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        calcPontuacao(event, true);
    }

    /**
     * @param registroExcluido true no afterDelete (linha ja removida do banco)
     */
    static void calcPontuacao(PersistenceEvent event, boolean registroExcluido) throws Exception {
        DynamicVO registro = (DynamicVO) event.getVo();
        BigDecimal idQualif = registro.asBigDecimal("IDQUALIF");

        if (idQualif == null) {
            System.out.println("[QualificacaoListener] IDQUALIF nulo no VO; pontuacao nao atualizada.");
            return;
        }

        System.out.println("[QualificacaoListener] CalcPontuacao IDQUALIF=" + idQualif
            + " excluido=" + registroExcluido
            + " IDPERG=" + registro.asBigDecimal("IDPERG")
            + " RESPOSTA=" + registro.asString("RESPOSTA"));

        Map<BigDecimal, String> respostasPorPergunta = carregarRespostas(idQualif);

        if (!registroExcluido) {
            aplicarRespostaDoEvento(respostasPorPergunta, registro);
        }

        BigDecimal pontosAcumulados = BigDecimal.ZERO;
        for (String resposta : respostasPorPergunta.values()) {
            pontosAcumulados = pontosAcumulados.add(pontosDaResposta(resposta));
        }

        BigDecimal qtdePerguntas = new BigDecimal(respostasPorPergunta.size());
        BigDecimal pontosFinais = BigDecimal.ZERO;
        String statusPontuacao = "T";

        if (qtdePerguntas.compareTo(BigDecimal.ZERO) > 0) {
            pontosFinais = pontosAcumulados
                .divide(qtdePerguntas, 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
            statusPontuacao = classificarIqf(pontosFinais);
        }

        System.out.println("[QualificacaoListener] respostas=" + respostasPorPergunta.size()
            + " pontos=" + pontosFinais + " IQF=" + statusPontuacao);

        atualizarQualificacao(idQualif, pontosFinais, statusPontuacao);
    }

    /**
     * Le respostas ja persistidas. Em afterInsert a linha nova pode ainda nao aparecer no SELECT
     * (outra sessao JDBC); por isso aplicarRespostaDoEvento complementa com o VO do evento.
     */
    private static Map<BigDecimal, String> carregarRespostas(BigDecimal idQualif) throws Exception {
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
            sql.setNamedParameter("IDQUALIF", idQualif);

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

    private static void aplicarRespostaDoEvento(Map<BigDecimal, String> respostas, DynamicVO registro) {
        BigDecimal idPerg = registro.asBigDecimal("IDPERG");
        if (idPerg == null) {
            return;
        }

        String resposta = registro.asString("RESPOSTA");
        if (resposta == null) {
            respostas.remove(idPerg);
            return;
        }

        respostas.put(idPerg, resposta.trim().toUpperCase());
    }

    private static void atualizarQualificacao(BigDecimal idQualif, BigDecimal pontosFinais, String statusPontuacao)
        throws Exception {
        JapeWrapper qualificacaoDAO = JapeFactory.dao("QualificacaoFornecedor");
        FluidUpdateVO updateVO = qualificacaoDAO.prepareToUpdateByPK(new Object[] { idQualif });
        updateVO.set("PONTUACAO", pontosFinais);
        updateVO.set("RESULTADOIQF", statusPontuacao);
        updateVO.update();
        System.out.println("[QualificacaoListener] TGQQUALIFFORN atualizado IDQUALIF=" + idQualif);
    }

    static BigDecimal pontosDaResposta(String resposta) {
        if (resposta == null || resposta.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if ("SIM".equals(resposta)) {
            return BigDecimal.ONE;
        }

        if (ValidaNumero.isNumeric(resposta)) {
            return multiplicadorFaixa(new BigDecimal(resposta));
        }

        return BigDecimal.ZERO;
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
}
