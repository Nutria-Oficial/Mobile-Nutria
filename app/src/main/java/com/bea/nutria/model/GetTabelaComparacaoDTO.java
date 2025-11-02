package com.bea.nutria.model;

import java.util.List;

public class GetTabelaComparacaoDTO {
    private Long tabelaId;
    private String nomeTabela;
    private Double quantidadeTotal;
    private Double porcao;

    private AvaliacaoDTO avaliacao;

    private List<NutrienteDTO> nutrientes;

    private transient boolean isExpanded = false;

    public GetTabelaComparacaoDTO() {
    }

    public Long getId() {
        return tabelaId;
    }

    public void setTabelaId(Long tabelaId) {
        this.tabelaId = tabelaId;
    }

    public Long getTabelaId() {
        return tabelaId;
    }

    public String getNomeTabela() {
        return nomeTabela;
    }

    public void setNomeTabela(String nomeTabela) {
        this.nomeTabela = nomeTabela;
    }

    public Double getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Double quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Double getPorcao() {
        return porcao;
    }

    public void setPorcao(Double porcao) {
        this.porcao = porcao;
    }

    public AvaliacaoDTO getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(AvaliacaoDTO avaliacao) {
        this.avaliacao = avaliacao;
    }

    public List<NutrienteDTO> getNutrientes() {
        return nutrientes;
    }

    public void setNutrientes(List<NutrienteDTO> nutrientes) {
        this.nutrientes = nutrientes;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public static class NutrienteDTO {
        private String nutriente;
        private Double total;
        private Double porcao;

        private Object valorDiario;

        public NutrienteDTO() {
        }

        public String getNutriente() {
            return nutriente;
        }

        public void setNutriente(String nutriente) {
            this.nutriente = nutriente;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }

        public Double getPorcao() {
            return porcao;
        }

        public void setPorcao(Double porcao) {
            this.porcao = porcao;
        }

        public Object getValorDiario() {
            return valorDiario;
        }

        public void setValorDiario(Object valorDiario) {
            this.valorDiario = valorDiario;
        }
    }
}