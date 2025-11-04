package br.com.le.addon.qualitymanagement.listeners;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.studio.annotations.Listener;

import java.math.BigDecimal;
import java.sql.ResultSet;

@Listener(instanceNames = {"LUCASTROCAR"})
public class ValidaOrigemFornecedorListener extends PersistenceEventAdapter {
    public void afterDelete(PersistenceEvent event) throws Exception {}

    public void afterInsert(PersistenceEvent event) throws Exception {
        validaOrigemFornec(event);
    }

    public void afterUpdate(PersistenceEvent event) throws Exception {
        validaOrigemFornec(event);
    }

    public void beforeCommit(TransactionContext arg0) throws Exception {}

    public void beforeDelete(PersistenceEvent event) throws Exception {}

    public void beforeInsert(PersistenceEvent event) throws Exception {}

    public void beforeUpdate(PersistenceEvent event) throws Exception {}

    public static String validaOrigemFornec(PersistenceEvent event) throws Exception {
        BigDecimal percDesvio = new BigDecimal(0);
        BigDecimal origemFornec = new BigDecimal(0);
        DynamicVO registro = (DynamicVO)event.getVo();
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sqlDadosOrigem = new NativeSql(jdbc);
        sqlDadosOrigem.appendSql(" SELECT COUNT(*) QTDE, nvl(R.PERCENTUALDESVIO,0) PERCENTUALDESVIO ");
        sqlDadosOrigem.appendSql(" FROM TGQRNC R, TGQCADASTROS O ");
        sqlDadosOrigem.appendSql(" WHERE RNCID =  " + registro.asBigDecimal("RNCID"));
        sqlDadosOrigem.appendSql(" AND R.ORIGEM = O.ID ");
        sqlDadosOrigem.appendSql(" AND R.ORIGEM = 1 ");
        sqlDadosOrigem.appendSql(" AND UPPER(O.DESCRICAO) LIKE 'FORNECEDOR%' ");
        sqlDadosOrigem.appendSql(" GROUP BY nvl(R.PERCENTUALDESVIO,0) ");
        ResultSet rsetrnc = sqlDadosOrigem.executeQuery();
        while (rsetrnc.next()) {
            origemFornec.add(rsetrnc.getBigDecimal("QTDE"));
            if (origemFornec.compareTo(new BigDecimal(0)) > 0 && percDesvio.compareTo(new BigDecimal(0)) > 0)
                return "Origem selecionada necessita do preenchimento do Percentual de Desvio. Favor verificar!";
        }
        return null;
    }
}
