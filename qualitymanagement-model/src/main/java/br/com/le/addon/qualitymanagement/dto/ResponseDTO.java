package br.com.le.addon.qualitymanagement.dto;

import lombok.Data;

@Data
public class ResponseDTO {

    private String mensagem;
    private String codigoParceiro;

    public ResponseDTO() {}

    public ResponseDTO(String mensagem, String codigoParceiro) {
        this.mensagem = mensagem;
        this.codigoParceiro = codigoParceiro;
    }

}
