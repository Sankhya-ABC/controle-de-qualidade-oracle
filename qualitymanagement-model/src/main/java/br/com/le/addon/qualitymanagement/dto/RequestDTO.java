package br.com.le.addon.qualitymanagement.dto;

import lombok.Data;

@Data
public class RequestDTO {

    private String nome;
    private String documento;

    public RequestDTO() {}

    public RequestDTO(String nome, String documento) {
        this.nome = nome;
        this.documento = documento;
    }

}
