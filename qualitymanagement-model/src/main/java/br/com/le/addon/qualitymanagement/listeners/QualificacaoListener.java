package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.le.addon.qualitymanagement.utils.ValidaNumero;
import br.com.sankhya.studio.annotations.Listener;

import java.math.BigDecimal;
import java.sql.ResultSet;

@Listener(instanceNames = {"ConfigQualidade"})
public class QualificacaoListener extends PersistenceEventAdapter {
    public void afterDelete(PersistenceEvent event) throws Exception {}

    public void afterInsert(PersistenceEvent event) throws Exception {
        calcPontuacao(event);
    }

    public void afterUpdate(PersistenceEvent event) throws Exception {}

    public void beforeCommit(TransactionContext arg0) throws Exception {}

    public void beforeDelete(PersistenceEvent event) throws Exception {}

    public void beforeInsert(PersistenceEvent event) throws Exception {}

    public void beforeUpdate(PersistenceEvent event) throws Exception {}

    public static void calcPontuacao(PersistenceEvent event) throws Exception {
        System.out.println("CalcPontuacao");
        BigDecimal pontos = new BigDecimal(0);
        BigDecimal pontosFinais = new BigDecimal(0);
        BigDecimal valor = new BigDecimal(0);
        BigDecimal qtdePerguntas = new BigDecimal(0);
        String statusPontuacao = "";
        DynamicVO registro = (DynamicVO)event.getVo();
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sqlResp = new NativeSql(jdbc);
        sqlResp.appendSql(" SELECT UPPER(RESPOSTA) RESPOSTA, count(*) QTDE ");
        sqlResp.appendSql(" FROM TGQQUALIFRESP ");
        sqlResp.appendSql(" WHERE IDQUALIF =  " + registro.asBigDecimal("IDQUALIF"));
        sqlResp.appendSql(" GROUP BY RESPOSTA ");
        ResultSet rsetFornec = sqlResp.executeQuery();
        while (rsetFornec.next()) {
            qtdePerguntas.add(rsetFornec.getBigDecimal("QTDE"));
            if (rsetFornec.getString("RESPOSTA").equals("SIM"))
                pontos = rsetFornec.getBigDecimal("QTDE").multiply(BigDecimal.ONE);
            if (ValidaNumero.isNumeric(rsetFornec.getString("RESPOSTA"))) {
                valor = new BigDecimal(rsetFornec.getString("RESPOSTA"));
                if (valor.compareTo(new BigDecimal(10)) < 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.15"));
                if (valor.compareTo(new BigDecimal(10)) >= 0 && valor.compareTo(new BigDecimal(29)) <= 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.30"));
                if (valor.compareTo(new BigDecimal(30)) >= 0 && valor.compareTo(new BigDecimal(49)) <= 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.45"));
                if (valor.compareTo(new BigDecimal(50)) >= 0 && valor.compareTo(new BigDecimal(69)) <= 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.60"));
                if (valor.compareTo(new BigDecimal(70)) >= 0 && valor.compareTo(new BigDecimal(89)) <= 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.75"));
                if (valor.compareTo(new BigDecimal(90)) >= 0)
                    pontos = rsetFornec.getBigDecimal("QTDE").multiply(new BigDecimal("0.90"));
            }
            System.out.println("Pontuacao: " + pontos);
            pontosFinais = pontos.divide(qtdePerguntas).multiply(new BigDecimal(100));
            if (pontosFinais.compareTo(new BigDecimal(80)) >= 0 && pontosFinais.compareTo(new BigDecimal(100)) <= 0)
                statusPontuacao = "A";
            if (pontosFinais.compareTo(new BigDecimal(50)) >= 0 && pontosFinais.compareTo(new BigDecimal(79)) <= 0)
                statusPontuacao = "B";
            if (pontosFinais.compareTo(new BigDecimal(49)) <= 0)
                statusPontuacao = "T";
            JapeWrapper qualificacaoDAO = JapeFactory.dao("QualificacaoFornecedor");
            FluidUpdateVO updateVO = qualificacaoDAO.prepareToUpdateByPK(new Object[] { registro.asBigDecimal("IDQUALIF") });
            updateVO.set("PONTUACAO", pontosFinais);
            updateVO.set("RESULTADOIQF", statusPontuacao);
            updateVO.update();
        }
    }
}
