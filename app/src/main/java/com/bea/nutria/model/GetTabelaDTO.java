package com.bea.nutria.model;

import java.util.List;

public class GetTabelaDTO {
    private Integer tabelaId;
    private String nomeTabela;
    private Double quantidadeTotal;
    private Double porcao;
    private String avaliacao;
    private List<NutrienteDTO> nutrientes;

    // Campo de estado da UI (transient para não ser serializado pelo GSON)
    private transient boolean isExpanded = false;

    public GetTabelaDTO() {
    }

    // --- Getters e Setters para GetTabelaDTO ---

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

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
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


    // =================================================================
    // Sub-classe para o array 'nutrientes'
    // =================================================================

    public static class NutrienteDTO {
        private String nutriente;
        private Double total;
        private Double porcao;
        private String valorDiario; // Mantido como String para lidar com "NaN"

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

        public String getValorDiario() {
            return valorDiario;
        }

        public void setValorDiario(String valorDiario) {
            this.valorDiario = valorDiario;
        }
    }
}