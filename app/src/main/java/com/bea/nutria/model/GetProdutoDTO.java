package com.bea.nutria.model;

public class GetProdutoDTO {
    private String nome;

    // ⚠️ SOLUÇÃO: Adicione este construtor vazio explicitamente
    public GetProdutoDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Se você tiver um construtor com parâmetros em outro lugar, mantenha-o,
    // mas o construtor vazio é CRÍTICO para o Gson.
    // Exemplo:
    /*
    public GetProdutoDTO(String nome) {
        this.nome = nome;
    }
    */
}