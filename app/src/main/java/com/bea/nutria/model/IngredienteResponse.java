package com.bea.nutria.model;

public class IngredienteResponse {
    private Integer id;
    private String nomeIngrediente;

    public IngredienteResponse(Integer id, String nomeIngrediente) {
        this.id = id;
        this.nomeIngrediente = nomeIngrediente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeIngrediente() {
        return nomeIngrediente;
    }

    public void setNomeIngrediente(String nomeIngrediente) {
        this.nomeIngrediente = nomeIngrediente;
    }
}
