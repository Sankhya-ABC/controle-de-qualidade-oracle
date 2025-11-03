package br.com.le.addon.qualitymanagement.model;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.sql.ResultSet;

public class ParametrosCorreiosXML {
    String codigoAdministrativo;

    String contrato;

    String codigoServico;

    String cartaoPostagem;

    String usuario;

    String senha;

    String urlCorreios;

    String tipoEnv;

    public String getCodigoAdministrativo() {
        return this.codigoAdministrativo;
    }

    public void setCodigoAdministrativo(String codigoAdministrativo) {
        this.codigoAdministrativo = codigoAdministrativo;
    }

    public String getContrato() {
        return this.contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

    public String getCodigoServico() {
        return this.codigoServico;
    }

    public void setCodigoServico(String codigoServico) {
        this.codigoServico = codigoServico;
    }

    public String getCartaoPostagem() {
        return this.cartaoPostagem;
    }

    public void setCartaoPostagem(String cartaoPostagem) {
        this.cartaoPostagem = cartaoPostagem;
    }

    public String getUsuario() {
        return this.usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getUrlCorreios() {
        return this.urlCorreios;
    }

    public void setUrlCorreios(String urlCorreios) {
        this.urlCorreios = urlCorreios;
    }

    public String getTipoEnv() {
        return this.tipoEnv;
    }

    public void setTipoEnv(String tipoEnv) {
        this.tipoEnv = tipoEnv;
    }

    public void setParametros() throws Exception {
        System.out.println("setParametros");
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT USUARIO, SENHA, CODADM, CONTRATO, CARPOST, NUMSERV, URL ");
        sql.appendSql(" FROM  SIGCFG WHERE ROWNUM = 1");
        ResultSet rset = sql.executeQuery();
        while (rset.next()) {
            setUsuario(rset.getString("USUARIO"));
            setSenha(rset.getString("SENHA"));
            setCodigoAdministrativo(rset.getString("CODADM"));
            setCartaoPostagem(rset.getString("CARPOST"));
            setContrato(rset.getString("CONTRATO"));
            setUrlCorreios(rset.getString("URL"));
            setTipoEnv(rset.getString("NUMSERV"));
        }
    }
}
