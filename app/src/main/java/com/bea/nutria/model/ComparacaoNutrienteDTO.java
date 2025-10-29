package com.bea.nutria.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ComparacaoNutrienteDTO {

    @SerializedName("nutriente")
    private String nomeNutriente;

    // Map para armazenar os valores de porção para "Teste1" e "Teste2"
    @SerializedName("porcaoPorTabela")
    private Map<String, Double> porcaoPorTabela;

    @SerializedName("valorComparacao")
    private Double valorComparacao;

    public String getNomeNutriente() {
        return nomeNutriente;
    }

    public void setNomeNutriente(String nomeNutriente) {
        this.nomeNutriente = nomeNutriente;
    }

    public Map<String, Double> getPorcaoPorTabela() {
        return porcaoPorTabela;
    }

    public void setPorcaoPorTabela(Map<String, Double> porcaoPorTabela) {
        this.porcaoPorTabela = porcaoPorTabela;
    }

    public Double getValorComparacao() {
        return valorComparacao;
    }

    public void setValorComparacao(Double valorComparacao) {
        this.valorComparacao = valorComparacao;
    }
}
