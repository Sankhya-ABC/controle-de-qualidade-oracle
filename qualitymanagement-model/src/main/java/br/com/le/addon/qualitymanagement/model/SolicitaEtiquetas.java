package br.com.le.addon.qualitymanagement.model;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class SolicitaEtiquetas {
    String tipoDestinatario;

    String identificador;

    String idServico;

    Integer qtdEtiquetas;

    String tipoEntCorreios;

    String numeroServico;

    String descServico;

    String cepOrigem;

    String cepDestino;

    BigDecimal nuemb;

    public String getTipoDestinatario() {
        return this.tipoDestinatario;
    }

    public void setTipoDestinatario(String tipoDestinatario) {
        this.tipoDestinatario = tipoDestinatario;
    }

    public String getIdentificador() {
        return this.identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getIdServico() {
        return this.idServico;
    }

    public void setIdServico(String idServico) {
        this.idServico = idServico;
    }

    public Integer getQtdEtiquetas() {
        return this.qtdEtiquetas;
    }

    public void setQtdEtiquetas(Integer qtdEtiquetas) {
        this.qtdEtiquetas = qtdEtiquetas;
    }

    public String getTipoEntCorreios() {
        return this.tipoEntCorreios;
    }

    public void setTipoEntCorreios(String tipoEntCorreios) {
        this.tipoEntCorreios = tipoEntCorreios;
    }

    public String getNumeroServico() {
        return this.numeroServico;
    }

    public void setNumeroServico(String numeroServico) {
        this.numeroServico = numeroServico;
    }

    public String getCepOrigem() {
        return this.cepOrigem;
    }

    public void setCepOrigem(String cepOrigem) {
        this.cepOrigem = cepOrigem;
    }

    public String getCepDestino() {
        return this.cepDestino;
    }

    public void setCepDestino(String cepDestino) {
        this.cepDestino = cepDestino;
    }

    public String getDescServico() {
        return this.descServico;
    }

    public void setDescServico(String descServico) {
        this.descServico = descServico;
    }

    public BigDecimal getNuemb() {
        return this.nuemb;
    }

    public void setNuemb(BigDecimal nuemb) {
        this.nuemb = nuemb;
    }

    public void setParametros(BigDecimal nunota, Integer qtdEtiquetas) throws Exception {
        System.out.println("parametros etiqueta");
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        NativeSql sql = new NativeSql(jdbc);
        sql.appendSql("SELECT REMETENTE.CEP CEP_REMETENTE, ");
        sql.appendSql("            NVL(DESTTRAY.CEP,DESTINATARIO.CEP) CEP_DESTINATARIO, ");
        sql.appendSql("\t\t        REMETENTE.CGC AS CNPJ_CPF, ");
        sql.appendSql("\t\t        NVL(NOTA.AD_NUMSERV, (SELECT AD_NUMSERV FROM SIGCFG WHERE ROWNUM <= 1)) AS NUMSERVICO, ");
        sql.appendSql("\t\t        (SELECT NUEMB FROM SIGCFG WHERE ROWNUM <= 1) AS NUEMB, ");
        sql.appendSql("\t\t        NVL((SELECT SERV.DESCRICAO FROM SIGSER SERV WHERE SERV.NUMSERV = NOTA.AD_NUMSERV), ");
        sql.appendSql("\t\t            (SELECT SERV.DESCRICAO FROM SIGSER SERV WHERE SERV.NUMSERV = (SELECT NUMSERV FROM SIGCFG WHERE ROWNUM <= 1) AND ROWNUM <= 1)) AS DESCSERVICO, ");
        sql.appendSql("\t\t        NVL((SELECT SERV.IDSERVICO FROM SIGSER SERV WHERE SERV.NUMSERV = NOTA.AD_NUMSERV), ");
        sql.appendSql("\t\t            (SELECT SERV.IDSERVICO FROM SIGSER SERV WHERE SERV.NUMSERV = (SELECT NUMSERV FROM SIGCFG WHERE ROWNUM <= 1) AND ROWNUM <= 1)) AS IDSERVICO ");
        sql.appendSql("\t\t FROM   TGFCAB NOTA, ");
        sql.appendSql("\t\t TSIEMP REMETENTE, ");
        sql.appendSql("\t\t TGFPAR DESTINATARIO, ");
        sql.appendSql("     (SELECT  ENDENTREGA.CEP, ");
        sql.appendSql("              CAB.NUNOTA NUNOTATRAY ");
        sql.appendSql("         FROM AD_FBTPEDEND ENDENTREGA, ");
        sql.appendSql("              TGFVAR LIG, ");
        sql.appendSql("              AD_FBTPED PEDTRAY, ");
        sql.appendSql("              TGFCAB CAB ");
        sql.appendSql("        WHERE PEDTRAY.PEDIDOID = ENDENTREGA.PEDIDOID ");
        sql.appendSql("          AND PEDTRAY.NUNOTA  = LIG.NUNOTAORIG ");
        sql.appendSql("          AND LIG.NUNOTAORIG =  CAB.NUNOTA ");
        sql.appendSql("          AND LIG.SEQUENCIA = 1 ");
        sql.appendSql("          AND ENDENTREGA.TIPO = 'Entrega') DESTTRAY ");
        sql.appendSql("\t\t WHERE NOTA.NUNOTA = DESTTRAY.NUNOTATRAY (+) ");
        sql.appendSql("\t\t AND   NOTA.CODEMP = REMETENTE.CODEMP ");
        sql.appendSql("\t\t AND   NOTA.CODPARC = DESTINATARIO.CODPARC ");
        sql.appendSql("     AND   NOTA.NUNOTA = " + nunota);
        System.out.println("sql: " + sql);
        ResultSet rset = sql.executeQuery();
        setQtdEtiquetas(qtdEtiquetas);
        while (rset.next()) {
            String cepDestinatario = rset.getString("CEP_DESTINATARIO");
            String cepDestinatarioFormatado = cepDestinatario.replaceAll("-", "");
            System.out.println("cepDestinatarioFormatado: " + cepDestinatarioFormatado);
            setNumeroServico(rset.getString("NUMSERVICO"));
            setNuemb(rset.getBigDecimal("NUEMB"));
            setDescServico(rset.getString("DESCSERVICO"));
            setCepOrigem(rset.getString("CEP_REMETENTE"));
            setCepDestino(cepDestinatarioFormatado);
            setIdentificador(rset.getString("CNPJ_CPF"));
            setIdServico(rset.getString("IDSERVICO"));
        }
    }

    public static void main(String[] args) {
        String cepDestinatario = "03426-020";
        System.out.println(cepDestinatario.replaceAll("-", ""));
    }
}
