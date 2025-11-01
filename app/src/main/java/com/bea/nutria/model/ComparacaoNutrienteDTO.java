package com.bea.nutria.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Data Transfer Object (DTO) para representar o resultado da comparação entre
 * dois nutrientes de tabelas diferentes.
 * Utiliza um Map para armazenar os valores individuais da porção por tabela.
 */
public class ComparacaoNutrienteDTO {

    // Chaves esperadas no Map porcaoPorTabela (Estas chaves fixas não são mais usadas
    // na lógica do Fragmento, que agora usa os nomes dinâmicos da tabela).

    @SerializedName("nutriente")
    private String nomeNutriente;

    // Map armazena os valores de porção para as duas tabelas
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
     * (Mantido por compatibilidade, mas o Fragmento agora usa getPorcaoPorTabela() diretamente)
     * @return O valor Double correspondente à chave "Teste1", ou null se não existir.
     */


    /**
     * Método utilitário para obter o valor do nutriente da Tabela 2 (Produto 2).
     * (Mantido por compatibilidade, mas o Fragmento agora usa getPorcaoPorTabela() diretamente)
     * @return O valor Double correspondente à chave "Teste2", ou null se não existir.
     */

}