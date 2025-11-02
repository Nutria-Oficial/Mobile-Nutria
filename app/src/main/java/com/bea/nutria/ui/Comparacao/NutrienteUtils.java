package com.bea.nutria.ui.Comparacao;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NutrienteUtils {

    /**
     * Formata um valor Double em uma String, tratando nulos e formatando para duas casas decimais.
     * @param value O valor Double (total ou porcao) do NutrienteDTO.
     * @return O valor formatado como String, ou "0.0" se for nulo.
     */
    public static String formatDoubleToString(Double value) {
        if (value == null) {
            return "0.0";
        }
        // Usando Locale.US para garantir o ponto como separador decimal.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);

        // Remove .0 ou ,0 no final se for um número inteiro.
        if (value == value.intValue()) {
            return String.valueOf(value.intValue());
        }

        return df.format(value);
    }
}
