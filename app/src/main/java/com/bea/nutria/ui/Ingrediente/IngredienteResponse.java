package com.bea.nutria.ui.Ingrediente;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class IngredienteResponse implements Serializable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("nomeIngrediente")
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}