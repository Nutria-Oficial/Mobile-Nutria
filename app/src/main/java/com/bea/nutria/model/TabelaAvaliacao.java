package com.bea.nutria.model;

public class TabelaAvaliacao {
    private Character classificacao;

    private double pontuacao;

    private String comentarios;

    public TabelaAvaliacao(){

    }
    public TabelaAvaliacao(Character classificacao, double pontuacao, String comentarios) {
        this.classificacao = classificacao;
        this.pontuacao = pontuacao;
        this.comentarios = comentarios;
    }

    public Character getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(Character classificacao) {
        this.classificacao = classificacao;
    }

    public double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
