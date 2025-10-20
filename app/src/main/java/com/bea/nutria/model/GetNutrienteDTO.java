package com.bea.nutria.model;

public class GetNutrienteDTO {
    String nutriente;
    double total;
    double porcao;
    double valorDiario;

    public GetNutrienteDTO(){

    }
    public GetNutrienteDTO(String nutriente, double total, double porcao, double valorDiario) {
        this.nutriente = nutriente;
        this.total = total;
        this.porcao = porcao;
        this.valorDiario = valorDiario;
    }

    public String getNutriente() {
        return nutriente;
    }

    public void setNutriente(String nutriente) {
        this.nutriente = nutriente;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getPorcao() {
        return porcao;
    }

    public void setPorcao(double porcao) {
        this.porcao = porcao;
    }

    public double getValorDiario() {
        return valorDiario;
    }

    public void setValorDiario(double valorDiario) {
        this.valorDiario = valorDiario;
    }
}
