package br.com.le.addon.qualitymanagement.model;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.sql.ResultSet;

public class ServicoAdicional {
    String valorDeclarado;

    String catServicoAdicional;

    String avisoRecebimento;

    String eleicao;

    String entregaVizinho;

    String maoPropria;

    String valorDeclCorreios;

    String numVizinho;

    public String getValorDeclarado() {
        return this.valorDeclarado;
    }

    public void setValorDeclarado(String valorDeclarado) {
        this.valorDeclarado = valorDeclarado;
    }

    public String getCatServicoAdicional() {
        return this.catServicoAdicional;
    }

    public void setCatServicoAdicional(String codServicoAdional) {
        this.catServicoAdicional = codServicoAdional;
    }

    public String getAvisoRecebimento() {
        return this.avisoRecebimento;
    }

    public void setAvisoRecebimento(String avisoRecebimento) {
        this.avisoRecebimento = avisoRecebimento;
    }

    public String getEleicao() {
        return this.eleicao;
    }

    public void setEleicao(String eleicao) {
        this.eleicao = eleicao;
    }

    public String getEntregaVizinho() {
        return this.entregaVizinho;
    }

    public void setEntregaVizinho(String entregaVizinho) {
        this.entregaVizinho = entregaVizinho;
    }

    public String getMaoPropria() {
        return this.maoPropria;
    }

    public void setMaoPropria(String maoPropria) {
        this.maoPropria = maoPropria;
    }

    public String getValorDeclCorreios() {
        return this.valorDeclCorreios;
    }

    public void setValorDeclCorreios(String valorDecl) {
        this.valorDeclCorreios = valorDecl;
    }

    public String getNumVizinho() {
        return this.numVizinho;
    }

    public void setNumVizinho(String numVizinho) {
        this.numVizinho = numVizinho;
    }

    public void setParametros(String etiqueta) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT SERV.CATSERV,");
        sql.appendSql(" NVL(PLP.VALORDECLCORREIOS, 'N') AS VALORDECLCORREIOS,");
        sql.appendSql(" NVL(PLP.AVISORECCORREIOS, 'N') AS AVISORECCORREIOS,");
        sql.appendSql(" NVL(PLP.ELEICAOCORREIOS, 'N') AS ELEICAOCORREIOS,");
        sql.appendSql(" NVL(PLP.ENTRVIZCORREIOS, 'N') AS ENTRVIZCORREIOS,");
        sql.appendSql(" NVL(PLP.MAOPROPCORREIOS, 'N') AS MAOPROPCORREIOS,");
        sql.appendSql(" PLP.VALORDECLARADO AS VALORDECLARADO,");
        sql.appendSql(" PLP.NUMVIZINHO");
        sql.appendSql(" FROM   SIGPLP PLP,");
        sql.appendSql("        SIGSER SERV");
        sql.appendSql(" WHERE  PLP.ETIQUETA = '" + etiqueta + "'");
        sql.appendSql(" AND    SERV.NUMSERV = PLP.NUMSERV");
        ResultSet rset = sql.executeQuery();
        while (rset.next()) {
            setCatServicoAdicional(rset.getString("CATSERV"));
            setValorDeclCorreios(rset.getString("VALORDECLCORREIOS"));
            setAvisoRecebimento(rset.getString("AVISORECCORREIOS"));
            setEleicao(rset.getString("ELEICAOCORREIOS"));
            setEntregaVizinho(rset.getString("ENTRVIZCORREIOS"));
            setMaoPropria(rset.getString("MAOPROPCORREIOS"));
            setValorDeclarado(rset.getString("VALORDECLARADO"));
            setNumVizinho(rset.getString("NUMVIZINHO"));
        }
    }
}
