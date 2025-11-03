package com.bea.nutria.ui.Comparacao;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NutrienteUtils {

    public static String formatDoubleToString(Double value) {
        if (value == null) {
            return "0.0";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);

        if (value == value.intValue()) {
            return String.valueOf(value.intValue());
        }

        return df.format(value);
    }
}