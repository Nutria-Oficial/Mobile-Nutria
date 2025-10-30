package com.bea.nutria.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Data Transfer Object (DTO) para representar o resultado da comparação entre
 * dois nutrientes de tabelas diferentes.
 * Utiliza um Map para armazenar os valores individuais da porção por tabela.
 */
public class ComparacaoNutrienteDTO {

    // Chaves esperadas no Map porcaoPorTabela
    private static final String KEY_TABELA_1 = "Teste1";
    private static final String KEY_TABELA_2 = "Teste2";

    @SerializedName("nutriente")
    private String nomeNutriente;

    // Map armazena os valores de porção para as duas tabelas, usando as chaves "Teste1" e "Teste2"
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

    /**
     * Método utilitário para obter o valor do nutriente da Tabela 1 (Produto 1).
     * @return O valor Double correspondente à chave "Teste1", ou null se não existir.
     */
    public Double getValorTabela1() {
        return porcaoPorTabela != null ? porcaoPorTabela.get(KEY_TABELA_1) : null;
    }

    /**
     * Método utilitário para obter o valor do nutriente da Tabela 2 (Produto 2).
     * @return O valor Double correspondente à chave "Teste2", ou null se não existir.
     */
    public Double getValorTabela2() {
        return porcaoPorTabela != null ? porcaoPorTabela.get(KEY_TABELA_2) : null;
    }
}
