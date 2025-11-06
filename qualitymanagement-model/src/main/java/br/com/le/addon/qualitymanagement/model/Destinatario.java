package br.com.le.addon.qualitymanagement.model;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class Destinatario {
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

    String numNota;

    String serie;

    public String toString() {
        return "Destinatario [razaoSocial=" + this.razaoSocial + ", logradouro=" + this.logradouro + ", numEnd=" + this.numEnd +
            ", complemento=" + this.complemento + ", nomeBairro=" + this.nomeBairro + ", cep=" + this.cep + ", nomeCidade=" +
            this.nomeCidade + ", uf=" + this.uf + ", telefone=" + this.telefone + ", fax=" + this.fax + ", email=" + this.email +
            ", cnpjCpf=" + this.cnpjCpf + ", numNota=" + this.numNota + ", serie=" + this.serie + "]";
    }

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

    public String getNumNota() {
        return this.numNota;
    }

    public void setNumNota(String numNota) {
        this.numNota = numNota;
    }

    public String getSerie() {
        return this.serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public void setParametros(BigDecimal nunota, BigDecimal pedidoTray) throws Exception {
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        System.out.println("pedidoTray: " + pedidoTray);
        if (pedidoTray != null) {
            System.out.println("If pedido tray");
            NativeSql sqlTray = new NativeSql(jdbc);
            sqlTray.appendSql(" SELECT ENDDEST.NOME  AS RAZAOSOCIAL,");
            sqlTray.appendSql(" NOTA.NUMNOTA,");
            sqlTray.appendSql(" NOTA.SERIENOTA,");
            sqlTray.appendSql(" ENDDEST.ENDERECO AS LOGRADOURO,");
            sqlTray.appendSql(" ENDDEST.NUMERO AS NUMEND,");
            sqlTray.appendSql(" ENDDEST.COMPLEMENTO,");
            sqlTray.appendSql(" ENDDEST.BAIRRO AS NOME_BAIRRO,");
            sqlTray.appendSql(" ENDDEST.CEP,");
            sqlTray.appendSql(" ENDDEST.CIDADE AS NOME_CIDADE,");
            sqlTray.appendSql(" ENDDEST.ESTADO AS UF,");
            sqlTray.appendSql(" SUBSTR(DESTINATARIO.TELEFONE, 3) AS TELEFONE,");
            sqlTray.appendSql(" DESTINATARIO.FAX,");
            sqlTray.appendSql(" DESTINATARIO.EMAIL,");
            sqlTray.appendSql(" DESTINATARIO.CGC_CPF AS CNPJ_CPF");
            sqlTray.appendSql(" FROM   TGFCAB NOTA,");
            sqlTray.appendSql(" TGFPAR DESTINATARIO,");
            sqlTray.appendSql(" AD_FBTPEDEND ENDDEST");
            sqlTray.appendSql(" WHERE NOTA.NUNOTA = " + nunota);
            sqlTray.appendSql(" AND NOTA.CODPARC = DESTINATARIO.CODPARC");
            sqlTray.appendSql(" AND ENDDEST.TIPO  = 'Entrega'");
            sqlTray.appendSql(" AND NOTA.NUMPEDIDO2 = ENDDEST.PEDIDOID");
            ResultSet rset = sqlTray.executeQuery();
            while (rset.next()) {
                setRazaoSocial(rset.getString("RAZAOSOCIAL"));
                setNumNota(rset.getString("NUMNOTA"));
                setSerie(rset.getString("SERIENOTA"));
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
        } else {
            System.out.println("Else pedido sankhya ");
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT DESTINATARIO.NOMEPARC AS RAZAOSOCIAL,");
            sql.appendSql(" NOTA.NUMNOTA,");
            sql.appendSql(" NOTA.SERIENOTA,");
            sql.appendSql(" ENDERECO.TIPO + ' ' + ENDERECO.NOMEEND AS LOGRADOURO,");
            sql.appendSql(" DESTINATARIO.NUMEND,");
            sql.appendSql(" trim(DESTINATARIO.COMPLEMENTO) AS COMPLEMENTO,");
            sql.appendSql(" ISNULL(BAIRRO.DESCRICAOCORREIO, BAIRRO.NOMEBAI) AS NOME_BAIRRO,");
            sql.appendSql(" DESTINATARIO.CEP,");
            sql.appendSql(" ISNULL(CIDADE.DESCRICAOCORREIO, CIDADE.NOMECID) AS NOME_CIDADE,");
            sql.appendSql(" UF.UF,");
            sql.appendSql(" SUBSTRING(DESTINATARIO.TELEFONE, 3, LEN(DESTINATARIO.TELEFONE)) AS TELEFONE,");
            sql.appendSql(" ISNULL(DESTINATARIO.FAX,SUBSTRING(DESTINATARIO.TELEFONE, 3, LEN(DESTINATARIO.TELEFONE))) AS FAX,");
            sql.appendSql(" DESTINATARIO.EMAIL,");
            sql.appendSql(" DESTINATARIO.CGC_CPF AS CNPJ_CPF");
            sql.appendSql(" FROM   TGFCAB NOTA,");
            sql.appendSql(" TGFPAR DESTINATARIO,");
            sql.appendSql(" TSIEND ENDERECO,");
            sql.appendSql(" TSICID CIDADE,");
            sql.appendSql(" TSIBAI BAIRRO,");
            sql.appendSql(" TSIUFS UF");
            sql.appendSql(" WHERE  NOTA.NUNOTA = " + nunota);
            sql.appendSql(" AND    NOTA.CODPARC = DESTINATARIO.CODPARC");
            sql.appendSql(" AND    ENDERECO.CODEND = DESTINATARIO.CODEND");
            sql.appendSql(" AND    BAIRRO.CODBAI = DESTINATARIO.CODBAI");
            sql.appendSql(" AND    CIDADE.CODCID = DESTINATARIO.CODCID");
            sql.appendSql(" AND    UF.CODUF = CIDADE.UF");
            ResultSet rset = sql.executeQuery();
            while (rset.next()) {
                setRazaoSocial(rset.getString("RAZAOSOCIAL"));
                setNumNota(rset.getString("NUMNOTA"));
                setSerie(rset.getString("SERIENOTA"));
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
                System.out.println("nomeDest: " + rset.getString("RAZAOSOCIAL"));
            }
        }
    }
}
