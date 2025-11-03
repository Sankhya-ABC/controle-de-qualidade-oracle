package br.com.le.addon.qualitymanagement.model;


public class BuscaCliente {
    String idContrato;

    String idCartaoPostagem;

    String usuario;

    String senha;

    public String getIdContrato() {
        return this.idContrato;
    }

    public void setIdContrato(String idContrato) {
        this.idContrato = idContrato;
    }

    public String getIdCartaoPostagem() {
        return this.idCartaoPostagem;
    }

    public void setIdCartaoPostagem(String idCartaoPostagem) {
        this.idCartaoPostagem = idCartaoPostagem;
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
}
