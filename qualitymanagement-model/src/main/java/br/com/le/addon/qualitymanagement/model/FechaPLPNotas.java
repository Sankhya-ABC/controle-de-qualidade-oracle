package br.com.le.addon.qualitymanagement.model;


import java.math.BigDecimal;

public class FechaPLPNotas {
    BigDecimal nunota;

    String etiquetaDigitoVerificador;

    String idPLP;

    String etiquetaSemDigVer;

    String numServico;

    BigDecimal pedidoTray;

    public BigDecimal getNunota() {
        return this.nunota;
    }

    public void setNunota(BigDecimal nunota) {
        this.nunota = nunota;
    }

    public String getEtiquetaDigitoVerificador() {
        return this.etiquetaDigitoVerificador;
    }

    public void setEtiquetaDigitoVerificador(String etiquetaDigitoVerificador) {
        this.etiquetaDigitoVerificador = etiquetaDigitoVerificador;
    }

    public String getIdPLP() {
        return this.idPLP;
    }

    public void setIdPLP(String idPLP) {
        this.idPLP = idPLP;
    }

    public String getEtiquetaSemDigVer() {
        return this.etiquetaSemDigVer;
    }

    public void setEtiquetaSemDigVer(String etiquetaSemDigVer) {
        this.etiquetaSemDigVer = etiquetaSemDigVer;
    }

    public String getNumServico() {
        return this.numServico;
    }

    public void setNumServico(String numServico) {
        this.numServico = numServico;
    }

    public BigDecimal getPedidoTray() {
        return this.pedidoTray;
    }

    public void setPedidoTray(BigDecimal pedidoTray) {
        this.pedidoTray = pedidoTray;
    }

    public String toString() {
        return "FechaPLPNotas [nunota=" + this.nunota + ", etiquetaDigitoVerificador=" + this.etiquetaDigitoVerificador +
            ", idPLP=" + this.idPLP + ", etiquetaSemDigVer=" + this.etiquetaSemDigVer + ", numServico=" + this.numServico +
            ", pedidoTray=" + this.pedidoTray + "]";
    }
}
