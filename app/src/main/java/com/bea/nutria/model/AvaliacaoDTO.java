package com.bea.nutria.model;

public class AvaliacaoDTO {

    private String classificacao;
    private Integer pontuacao;
    private String comentarios;

    public AvaliacaoDTO() {
    }

    public AvaliacaoDTO(String classificacao, Integer pontuacao, String comentarios) {
        this.classificacao = classificacao;
        this.pontuacao = pontuacao;
        this.comentarios = comentarios;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }

    public Integer getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Integer pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}