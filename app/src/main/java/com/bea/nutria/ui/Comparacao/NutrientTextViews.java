package com.bea.nutria.ui.Comparacao;

import android.widget.TextView;

// Classe simples para armazenar o par de TextViews (Valor e Valor Diário)
public class NutrientTextViews {
    public final TextView tvValor;
    public final TextView tvVD;

    public NutrientTextViews(TextView tvValor, TextView tvVD) {
        this.tvValor = tvValor;
        this.tvVD = tvVD;
    }
}
