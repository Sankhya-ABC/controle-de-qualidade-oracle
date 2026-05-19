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

@Listener(instanceNames = {"RespostaFornecedor"})
public class QualificacaoListener extends PersistenceEventAdapter {

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        calcPontuacao(event);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        calcPontuacao(event);
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        calcPontuacao(event);
    }

    public static void calcPontuacao(PersistenceEvent event) throws Exception {
        DynamicVO registro = (DynamicVO) event.getVo();
        BigDecimal idQualif = registro.asBigDecimal("IDQUALIF");

        if (idQualif == null) {
            System.out.println("[QualificacaoListener] IDQUALIF nulo, pontuacao ignorada.");
            return;
        }

        System.out.println("[QualificacaoListener] CalcPontuacao IDQUALIF=" + idQualif);

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();

        NativeSql sqlResp = null;
        ResultSet rset = null;

        try {
            sqlResp = new NativeSql(jdbc);
            sqlResp.appendSql(" SELECT UPPER(TRIM(RESPOSTA)) AS RESPOSTA ");
            sqlResp.appendSql(" FROM TGQQUALIFRESP ");
            sqlResp.appendSql(" WHERE IDQUALIF = :IDQUALIF ");
            sqlResp.setNamedParameter("IDQUALIF", idQualif);

            rset = sqlResp.executeQuery();

            BigDecimal pontosAcumulados = BigDecimal.ZERO;
            BigDecimal qtdePerguntas = BigDecimal.ZERO;

            while (rset.next()) {
                qtdePerguntas = qtdePerguntas.add(BigDecimal.ONE);
                String resposta = rset.getString("RESPOSTA");
                pontosAcumulados = pontosAcumulados.add(pontosDaResposta(resposta));
            }

            BigDecimal pontosFinais = BigDecimal.ZERO;
            String statusPontuacao = "T";

            if (qtdePerguntas.compareTo(BigDecimal.ZERO) > 0) {
                pontosFinais = pontosAcumulados
                    .divide(qtdePerguntas, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
                statusPontuacao = classificarIqf(pontosFinais);
            }

            System.out.println("[QualificacaoListener] pontos=" + pontosFinais
                + " IQF=" + statusPontuacao + " perguntas=" + qtdePerguntas);

            JapeWrapper qualificacaoDAO = JapeFactory.dao("QualificacaoFornecedor");
            FluidUpdateVO updateVO = qualificacaoDAO.prepareToUpdateByPK(new Object[] { idQualif });
            updateVO.set("PONTUACAO", pontosFinais);
            updateVO.set("RESULTADOIQF", statusPontuacao);
            updateVO.update();

        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (Exception ignored) {
                }
            }
            if (sqlResp != null) {
                NativeSql.releaseResources(sqlResp);
            }
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static BigDecimal pontosDaResposta(String resposta) {
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
