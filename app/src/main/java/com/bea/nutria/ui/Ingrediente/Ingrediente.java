package com.bea.nutria.ui.Ingrediente;

public class Ingrediente {
    private Integer id;
    private String nomeIngrediente;
    private double caloria;
    private double carboidrato;
    private double acucar;
    private double proteina;
    private double gorduraTotal;
    private double gorduraSaturada;
    private double sodio;
    private double fibra;
    private double agua;
    private double gorduraMonoinsaturada;
    private double gorduraPoliinsaturada;
    private double colesterol;
    private double alcool;
    private double vitaminaB6;
    private double vitaminaB12;
    private double vitaminaC;
    private double vitaminaD;
    private double vitaminaE;
    private double vitaminaK;
    private double teobromina;
    private double cafeina;
    private double colina;
    private double calcio;
    private double fosforo;
    private double magnesio;
    private double potassio;
    private double ferro;
    private double zinco;
    private double cobre;
    private double selenio;
    private double retinol;
    private double tiamina;
    private double riboflavina;
    private double niacina;
    private double folato;

    public Ingrediente() {
    }

    public Ingrediente(Integer id, String nomeIngrediente, double caloria, double carboidrato, double acucar,
                              double proteina, double gorduraTotal, double gorduraSaturada, double sodio,
                              double fibra, double agua, double gorduraMonoinsaturada, double gorduraPoliinsaturada,
                              double colesterol, double alcool, double vitaminaB6, double vitaminaB12,
                              double vitaminaC, double vitaminaD, double vitaminaE, double vitaminaK,
                              double teobromina, double cafeina, double colina, double calcio, double fosforo,
                              double magnesio, double potassio, double ferro, double zinco, double cobre,
                              double selenio, double retinol, double tiamina, double riboflavina,
                              double niacina, double folato) {
        this.id = id;
        this.nomeIngrediente = nomeIngrediente;
        this.caloria = caloria;
        this.carboidrato = carboidrato;
        this.acucar = acucar;
        this.proteina = proteina;
        this.gorduraTotal = gorduraTotal;
        this.gorduraSaturada = gorduraSaturada;
        this.sodio = sodio;
        this.fibra = fibra;
        this.agua = agua;
        this.gorduraMonoinsaturada = gorduraMonoinsaturada;
        this.gorduraPoliinsaturada = gorduraPoliinsaturada;
        this.colesterol = colesterol;
        this.alcool = alcool;
        this.vitaminaB6 = vitaminaB6;
        this.vitaminaB12 = vitaminaB12;
        this.vitaminaC = vitaminaC;
        this.vitaminaD = vitaminaD;
        this.vitaminaE = vitaminaE;
        this.vitaminaK = vitaminaK;
        this.teobromina = teobromina;
        this.cafeina = cafeina;
        this.colina = colina;
        this.calcio = calcio;
        this.fosforo = fosforo;
        this.magnesio = magnesio;
        this.potassio = potassio;
        this.ferro = ferro;
        this.zinco = zinco;
        this.cobre = cobre;
        this.selenio = selenio;
        this.retinol = retinol;
        this.tiamina = tiamina;
        this.riboflavina = riboflavina;
        this.niacina = niacina;
        this.folato = folato;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeIngrediente() {
        return nomeIngrediente;
    }

    public void setNomeIngrediente(String nomeIngrediente) {
        this.nomeIngrediente = nomeIngrediente;
    }

    public double getCaloria() {
        return caloria;
    }

    public void setCaloria(double caloria) {
        this.caloria = caloria;
    }

    public double getCarboidrato() {
        return carboidrato;
    }

    public void setCarboidrato(double carboidrato) {
        this.carboidrato = carboidrato;
    }

    public double getAcucar() {
        return acucar;
    }

    public void setAcucar(double acucar) {
        this.acucar = acucar;
    }

    public double getProteina() {
        return proteina;
    }

    public void setProteina(double proteina) {
        this.proteina = proteina;
    }

    public double getGorduraTotal() {
        return gorduraTotal;
    }

    public void setGorduraTotal(double gorduraTotal) {
        this.gorduraTotal = gorduraTotal;
    }

    public double getGorduraSaturada() {
        return gorduraSaturada;
    }

    public void setGorduraSaturada(double gorduraSaturada) {
        this.gorduraSaturada = gorduraSaturada;
    }

    public double getSodio() {
        return sodio;
    }

    public void setSodio(double sodio) {
        this.sodio = sodio;
    }

    public double getFibra() {
        return fibra;
    }

    public void setFibra(double fibra) {
        this.fibra = fibra;
    }

    public double getAgua() {
        return agua;
    }

    public void setAgua(double agua) {
        this.agua = agua;
    }

    public double getGorduraMonoinsaturada() {
        return gorduraMonoinsaturada;
    }

    public void setGorduraMonoinsaturada(double gorduraMonoinsaturada) {
        this.gorduraMonoinsaturada = gorduraMonoinsaturada;
    }

    public double getGorduraPoliinsaturada() {
        return gorduraPoliinsaturada;
    }

    public void setGorduraPoliinsaturada(double gorduraPoliinsaturada) {
        this.gorduraPoliinsaturada = gorduraPoliinsaturada;
    }

    public double getColesterol() {
        return colesterol;
    }

    public void setColesterol(double colesterol) {
        this.colesterol = colesterol;
    }

    public double getAlcool() {
        return alcool;
    }

    public void setAlcool(double alcool) {
        this.alcool = alcool;
    }

    public double getVitaminaB6() {
        return vitaminaB6;
    }

    public void setVitaminaB6(double vitaminaB6) {
        this.vitaminaB6 = vitaminaB6;
    }

    public double getVitaminaB12() {
        return vitaminaB12;
    }

    public void setVitaminaB12(double vitaminaB12) {
        this.vitaminaB12 = vitaminaB12;
    }

    public double getVitaminaC() {
        return vitaminaC;
    }

    public void setVitaminaC(double vitaminaC) {
        this.vitaminaC = vitaminaC;
    }

    public double getVitaminaD() {
        return vitaminaD;
    }

    public void setVitaminaD(double vitaminaD) {
        this.vitaminaD = vitaminaD;
    }

    public double getVitaminaE() {
        return vitaminaE;
    }

    public void setVitaminaE(double vitaminaE) {
        this.vitaminaE = vitaminaE;
    }

    public double getVitaminaK() {
        return vitaminaK;
    }

    public void setVitaminaK(double vitaminaK) {
        this.vitaminaK = vitaminaK;
    }

    public double getTeobromina() {
        return teobromina;
    }

    public void setTeobromina(double teobromina) {
        this.teobromina = teobromina;
    }

    public double getCafeina() {
        return cafeina;
    }

    public void setCafeina(double cafeina) {
        this.cafeina = cafeina;
    }

    public double getColina() {
        return colina;
    }

    public void setColina(double colina) {
        this.colina = colina;
    }

    public double getCalcio() {
        return calcio;
    }

    public void setCalcio(double calcio) {
        this.calcio = calcio;
    }

    public double getFosforo() {
        return fosforo;
    }

    public void setFosforo(double fosforo) {
        this.fosforo = fosforo;
    }

    public double getMagnesio() {
        return magnesio;
    }

    public void setMagnesio(double magnesio) {
        this.magnesio = magnesio;
    }

    public double getPotassio() {
        return potassio;
    }

    public void setPotassio(double potassio) {
        this.potassio = potassio;
    }

    public double getFerro() {
        return ferro;
    }

    public void setFerro(double ferro) {
        this.ferro = ferro;
    }

    public double getZinco() {
        return zinco;
    }

    public void setZinco(double zinco) {
        this.zinco = zinco;
    }

    public double getCobre() {
        return cobre;
    }

    public void setCobre(double cobre) {
        this.cobre = cobre;
    }

    public double getSelenio() {
        return selenio;
    }

    public void setSelenio(double selenio) {
        this.selenio = selenio;
    }

    public double getRetinol() {
        return retinol;
    }

    public void setRetinol(double retinol) {
        this.retinol = retinol;
    }

    public double getTiamina() {
        return tiamina;
    }

    public void setTiamina(double tiamina) {
        this.tiamina = tiamina;
    }

    public double getRiboflavina() {
        return riboflavina;
    }

    public void setRiboflavina(double riboflavina) {
        this.riboflavina = riboflavina;
    }

    public double getNiacina() {
        return niacina;
    }

    public void setNiacina(double niacina) {
        this.niacina = niacina;
    }

    public double getFolato() {
        return folato;
    }

    public void setFolato(double folato) {
        this.folato = folato;
    }
}
