package com.bea.nutria.model;

public class ItemIngrediente {
    private Integer nCdIngrediente;
    private double iQuantidade;

    public ItemIngrediente(){}
    public ItemIngrediente(Integer nCdIngrediente, double iQuantidade) {
        this.nCdIngrediente = nCdIngrediente;
        this.iQuantidade = iQuantidade;
    }

    public Integer getnCdIngrediente() {
        return nCdIngrediente;
    }

    public void setnCdIngrediente(Integer nCdIngrediente) {
        this.nCdIngrediente = nCdIngrediente;
    }

    public double getiQuantidade() {
        return iQuantidade;
    }

    public void setiQuantidade(double iQuantidade) {
        this.iQuantidade = iQuantidade;
    }
}
