package com.bea.nutria.model;

import java.util.List;

public class GetTabelaEAvaliacaoDTO {
    Integer tabelaId;
    String nomeTabela;
    double quantidadeTotal;
    double porcao;
    List<GetNutrienteDTO> nutrientes;
    TabelaAvaliacao avaliacao;

    public GetTabelaEAvaliacaoDTO(){

    }
    public GetTabelaEAvaliacaoDTO(Integer tabelaId, String nomeTabela, double quantidadeTotal, double porcao, List<GetNutrienteDTO> nutrientes, TabelaAvaliacao avaliacao) {
        this.tabelaId = tabelaId;
        this.nomeTabela = nomeTabela;
        this.quantidadeTotal = quantidadeTotal;
        this.porcao = porcao;
        this.nutrientes = nutrientes;
        this.avaliacao = avaliacao;
    }

    public Integer getTabelaId() {
        return tabelaId;
    }

    public void setTabelaId(Integer tabelaId) {
        this.tabelaId = tabelaId;
    }

    public String getNomeTabela() {
        return nomeTabela;
    }

    public void setNomeTabela(String nomeTabela) {
        this.nomeTabela = nomeTabela;
    }

    public double getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(double quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public double getPorcao() {
        return porcao;
    }

    public void setPorcao(double porcao) {
        this.porcao = porcao;
    }

    public List<GetNutrienteDTO> getNutrientes() {
        return nutrientes;
    }

    public void setNutrientes(List<GetNutrienteDTO> nutrientes) {
        this.nutrientes = nutrientes;
    }

    public TabelaAvaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(TabelaAvaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }
}
