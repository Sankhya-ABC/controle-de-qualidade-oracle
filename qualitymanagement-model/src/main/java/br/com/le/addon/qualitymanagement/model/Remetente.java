package br.com.le.addon.qualitymanagement.model;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class Remetente {
    String razaoSocial;

    String logradouro;

    String numEnd;

    String complemento;

    String nomeBairro;

    String cep;

    String nomeCidade;

    String uf;

    String telefone;

    String fax;

    String email;

    String cnpjCpf;

    public String getRazaoSocial() {
        return this.razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getLogradouro() {
        return this.logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumEnd() {
        return this.numEnd;
    }

    public void setNumEnd(String numEnd) {
        this.numEnd = numEnd;
    }

    public String getComplemento() {
        return this.complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getNomeBairro() {
        return this.nomeBairro;
    }

    public void setNomeBairro(String nomeBairro) {
        this.nomeBairro = nomeBairro;
    }

    public String getCep() {
        return this.cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getNomeCidade() {
        return this.nomeCidade;
    }

    public void setNomeCidade(String nomeCidade) {
        this.nomeCidade = nomeCidade;
    }

    public String getUf() {
        return this.uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCnpjCpf() {
        return this.cnpjCpf;
    }

    public void setCnpjCpf(String cnpjCpf) {
        this.cnpjCpf = cnpjCpf;
    }

    public void setParametros(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql(" SELECT REMETENTE.RAZAOSOCIAL,");
        sql.appendSql(" ENDERECO.TIPO || ' ' || ENDERECO.NOMEEND AS LOGRADOURO,");
        sql.appendSql(" REMETENTE.NUMEND,");
        sql.appendSql(" REMETENTE.COMPLEMENTO,");
        sql.appendSql(" NVL(BAIRRO.DESCRICAOCORREIO, BAIRRO.NOMEBAI) AS NOME_BAIRRO,");
        sql.appendSql(" REMETENTE.CEP,");
        sql.appendSql(" NVL(CIDADE.DESCRICAOCORREIO, CIDADE.NOMECID) AS NOME_CIDADE,");
        sql.appendSql(" UF.UF,");
        sql.appendSql(" SUBSTR(REMETENTE.TELEFONE, 3) AS TELEFONE,");
        sql.appendSql(" REMETENTE.FAX,");
        sql.appendSql(" REMETENTE.EMAIL,");
        sql.appendSql(" REMETENTE.CGC AS CNPJ_CPF");
        sql.appendSql(" FROM   TGFCAB NOTA,");
        sql.appendSql(" TSIEMP REMETENTE,");
        sql.appendSql(" TSIEND ENDERECO,");
        sql.appendSql(" TSICID CIDADE,");
        sql.appendSql(" TSIBAI BAIRRO,");
        sql.appendSql(" TSIUFS UF");
        sql.appendSql(" WHERE  NOTA.NUNOTA = " + nunota);
        sql.appendSql(" AND    NOTA.CODEMP = REMETENTE.CODEMP");
        sql.appendSql(" AND    ENDERECO.CODEND = REMETENTE.CODEND");
        sql.appendSql(" AND    BAIRRO.CODBAI = REMETENTE.CODBAI");
        sql.appendSql(" AND    CIDADE.CODCID = REMETENTE.CODCID");
        sql.appendSql(" AND    UF.CODUF = CIDADE.UF");
        ResultSet rset = sql.executeQuery();
        while (rset.next()) {
            setRazaoSocial(rset.getString("RAZAOSOCIAL"));
            setLogradouro(rset.getString("LOGRADOURO"));
            setNumEnd(rset.getString("NUMEND"));
            setComplemento(rset.getString("COMPLEMENTO"));
            setNomeBairro(rset.getString("NOME_BAIRRO"));
            setCep(rset.getString("CEP"));
            setNomeCidade(rset.getString("NOME_CIDADE"));
            setUf(rset.getString("UF"));
            setTelefone(rset.getString("TELEFONE"));
            setFax(rset.getString("FAX"));
            setEmail(rset.getString("EMAIL"));
            setCnpjCpf(rset.getString("CNPJ_CPF"));
        }
    }
}
