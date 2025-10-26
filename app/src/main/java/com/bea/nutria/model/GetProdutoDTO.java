package com.bea.nutria.model;

public class GetProdutoDTO {
    private Integer id; // Alterado de 'int' para 'Integer' para melhor manipulação de objetos e serialização
    private String nome;

    // ⚠️ SOLUÇÃO: Adicione este construtor vazio explicitamente
    public GetProdutoDTO() {
    }

    // Getter para 'id'
    public Integer getId() {
        return id;
    }

    // Setter para 'id'
    public void setId(Integer id) {
        this.id = id;
    }

    // Getter para 'nome'
    public String getNome() {
        return nome;
    }

    // Setter para 'nome'
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Se você tiver um construtor com parâmetros em outro lugar, mantenha-o,
    // mas o construtor vazio é CRÍTICO para o Gson/Jackson.
    /*
    // Exemplo de construtor com todos os campos:
    public GetProdutoDTO(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }
    */
}