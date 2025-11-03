package com.bea.nutria.ui.Comparacao;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.model.GetTabelaComparacaoDTO;
import com.google.android.material.button.MaterialButton;

import com.bea.nutria.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TabelaViewHolder extends RecyclerView.ViewHolder {

    private final Map<String, NutrientTextViews> nutrientTextViewMap = new HashMap<>();

    public final View headerItem;
    public final MaterialButton btnSelecionarTabela;
    public final TextView textViewTitulo;

    private final ImageButton imageButtonSeta;
    private final View conteudoExpansivel;
    private final TextView tvTabelaTituloDetalhes;
    private final View cardBlocoTabela;

    public TabelaViewHolder(@NonNull View itemView) {
        super(itemView);

        this.headerItem = itemView.findViewById(R.id.header_item);

        this.conteudoExpansivel = itemView.findViewById(R.id.conteudo_expansivel);
        this.btnSelecionarTabela = conteudoExpansivel.findViewById(R.id.btn_selecionar_tabela);

        this.textViewTitulo = headerItem.findViewById(R.id.textViewTitulo);
        this.imageButtonSeta = headerItem.findViewById(R.id.imageButtonSeta);

        this.cardBlocoTabela = conteudoExpansivel.findViewById(R.id.cardBlocoTabela);
        this.tvTabelaTituloDetalhes = cardBlocoTabela.findViewById(R.id.tvTabelaTitulo);


        initializeNutrientMap();
    }

    private void initializeNutrientMap() {
        View container = cardBlocoTabela;

        nutrientTextViewMap.put("Valor Calórico", new NutrientTextViews(
                container.findViewById(R.id.tv_valor_calorico),
                container.findViewById(R.id.tv_vd_valor_calorico)
        ));

        nutrientTextViewMap.put("Proteína", new NutrientTextViews(
                container.findViewById(R.id.tv_proteina),
                container.findViewById(R.id.tv_vd_proteina)
        ));

        nutrientTextViewMap.put("Carboidrato", new NutrientTextViews(
                container.findViewById(R.id.tv_carboidrato),
                container.findViewById(R.id.tv_vd_carboidrato)
        ));

        nutrientTextViewMap.put("Açúcar Total", new NutrientTextViews(
                container.findViewById(R.id.tv_acucar_total),
                container.findViewById(R.id.tv_vd_acucar_total)
        ));

        nutrientTextViewMap.put("Fibra Alimentar", new NutrientTextViews(
                container.findViewById(R.id.tv_fibra_alimentar),
                container.findViewById(R.id.tv_vd_fibra_alimentar)
        ));

        nutrientTextViewMap.put("Gordura Total", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_total),
                container.findViewById(R.id.tv_vd_gordura_total)
        ));

        nutrientTextViewMap.put("Gordura Saturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_saturada),
                container.findViewById(R.id.tv_vd_gordura_saturada)
        ));

        nutrientTextViewMap.put("Gordura Monoinsaturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_monoinsaturada),
                container.findViewById(R.id.tv_vd_gordura_monoinsaturada)
        ));

        nutrientTextViewMap.put("Gordura Poli-Insaturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_poliinsaturada),
                container.findViewById(R.id.tv_vd_gordura_poliinsaturada)
        ));

        nutrientTextViewMap.put("Colesterol", new NutrientTextViews(
                container.findViewById(R.id.tv_colesterol),
                container.findViewById(R.id.tv_vd_colesterol)
        ));

        nutrientTextViewMap.put("Retinol/Vitamina A", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_a),
                container.findViewById(R.id.tv_vd_vitamina_a)
        ));

        nutrientTextViewMap.put("Tiamina", new NutrientTextViews(
                container.findViewById(R.id.tv_tiamina),
                container.findViewById(R.id.tv_vd_tiamina)
        ));

        nutrientTextViewMap.put("Riboflavina", new NutrientTextViews(
                container.findViewById(R.id.tv_riboflavina),
                container.findViewById(R.id.tv_vd_riboflavina)
        ));

        nutrientTextViewMap.put("Niacina", new NutrientTextViews(
                container.findViewById(R.id.tv_niacina),
                container.findViewById(R.id.tv_vd_niacina)
        ));

        nutrientTextViewMap.put("Vitamina B-6", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_b6),
                container.findViewById(R.id.tv_vd_vitamina_b6)
        ));

        nutrientTextViewMap.put("Ácido Fólico", new NutrientTextViews(
                container.findViewById(R.id.tv_acido_folico),
                container.findViewById(R.id.tv_vd_acido_folico)
        ));

        nutrientTextViewMap.put("Colina", new NutrientTextViews(
                container.findViewById(R.id.tv_colina),
                container.findViewById(R.id.tv_vd_colina)
        ));

        nutrientTextViewMap.put("Vitamina B-12", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_b12),
                container.findViewById(R.id.tv_vd_vitamina_b12)
        ));

        nutrientTextViewMap.put("Vitamina C", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_c),
                container.findViewById(R.id.tv_vd_vitamina_c)
        ));

        nutrientTextViewMap.put("Vitamina D", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_d),
                container.findViewById(R.id.tv_vd_vitamina_d)
        ));

        nutrientTextViewMap.put("Vitamina E", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_e),
                container.findViewById(R.id.tv_vd_vitamina_e)
        ));

        nutrientTextViewMap.put("Vitamina K", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_k),
                container.findViewById(R.id.tv_vd_vitamina_k)
        ));

        nutrientTextViewMap.put("Cálcio", new NutrientTextViews(
                container.findViewById(R.id.tv_calcio),
                container.findViewById(R.id.tv_vd_calcio)
        ));

        nutrientTextViewMap.put("Fósforo", new NutrientTextViews(
                container.findViewById(R.id.tv_fosforo),
                container.findViewById(R.id.tv_vd_fosforo)
        ));

        nutrientTextViewMap.put("Magnésio", new NutrientTextViews(
                container.findViewById(R.id.tv_magnesio),
                container.findViewById(R.id.tv_vd_magnesio)
        ));

        nutrientTextViewMap.put("Ferro", new NutrientTextViews(
                container.findViewById(R.id.tv_ferro),
                container.findViewById(R.id.tv_vd_ferro)
        ));

        nutrientTextViewMap.put("Zinco", new NutrientTextViews(
                container.findViewById(R.id.tv_zinco),
                container.findViewById(R.id.tv_vd_zinco)
        ));

        nutrientTextViewMap.put("Cobre", new NutrientTextViews(
                container.findViewById(R.id.tv_cobre),
                container.findViewById(R.id.tv_vd_cobre)
        ));

        nutrientTextViewMap.put("Selênio", new NutrientTextViews(
                container.findViewById(R.id.tv_selenio),
                container.findViewById(R.id.tv_vd_selenio)
        ));

        nutrientTextViewMap.put("Potássio", new NutrientTextViews(
                container.findViewById(R.id.tv_potassio),
                container.findViewById(R.id.tv_vd_potassio)
        ));

        nutrientTextViewMap.put("Sódio", new NutrientTextViews(
                container.findViewById(R.id.tv_sodio),
                container.findViewById(R.id.tv_vd_sodio)
        ));

        nutrientTextViewMap.put("Cafeína", new NutrientTextViews(
                container.findViewById(R.id.tv_cafeina),
                container.findViewById(R.id.tv_vd_cafeina)
        ));

        nutrientTextViewMap.put("Teobromina", new NutrientTextViews(
                container.findViewById(R.id.tv_teobromina),
                container.findViewById(R.id.tv_vd_teobromina)
        ));

        nutrientTextViewMap.put("Álcool", new NutrientTextViews(
                container.findViewById(R.id.tv_alcool),
                container.findViewById(R.id.tv_vd_alcool)
        ));

        nutrientTextViewMap.put("Água", new NutrientTextViews(
                container.findViewById(R.id.tv_agua),
                container.findViewById(R.id.tv_vd_agua)
        ));
    }

    public void bind(GetTabelaComparacaoDTO tabela) {

        textViewTitulo.setText(tabela.getNomeTabela());

        if (tvTabelaTituloDetalhes != null) {
            String detalhes = String.format(Locale.getDefault(),
                    "Detalhes Nutricionais (Porção: %.1fg)",
                    tabela.getPorcao());
            tvTabelaTituloDetalhes.setText(detalhes);
        }

        Map<String, GetTabelaComparacaoDTO.NutrienteDTO> dataLookup = new HashMap<>();
        List<GetTabelaComparacaoDTO.NutrienteDTO> nutrientes = tabela.getNutrientes();

        if (nutrientes != null) {
            for (GetTabelaComparacaoDTO.NutrienteDTO nutriente : nutrientes) {
                String key = cleanNutrientName(nutriente.getNutriente());
                dataLookup.put(key, nutriente);
            }
        }

        for (Map.Entry<String, NutrientTextViews> entry : nutrientTextViewMap.entrySet()) {
            String nutrientName = entry.getKey();
            NutrientTextViews tvs = entry.getValue();

            GetTabelaComparacaoDTO.NutrienteDTO data = dataLookup.get(nutrientName);

            String valorToDisplay = "-";
            String vdToDisplay = "-";

            String unit = getUnitForNutrient(nutrientName);

            if (data != null) {
                Double valor = data.getTotal();

                if (valor != null) {
                    valorToDisplay = String.format(Locale.getDefault(), "%.1f %s", valor, unit);
                } else {
                    valorToDisplay = "0.0 " + unit;
                }

                Object vd = data.getValorDiario();
                if (vd != null && !vd.toString().equalsIgnoreCase("NaN")) {
                    if (vd instanceof Double) {
                        vdToDisplay = String.format(Locale.getDefault(), "%.0f%%", (Double) vd * 100);
                    } else {
                        vdToDisplay = vd.toString();
                    }
                }
            }

            if (tvs.tvValor != null) tvs.tvValor.setText(valorToDisplay);
            if (tvs.tvVD != null) tvs.tvVD.setText(vdToDisplay);
        }

        if (tabela.isExpanded()) {
            conteudoExpansivel.setVisibility(View.VISIBLE);
            if (imageButtonSeta != null) imageButtonSeta.setRotation(180);
        } else {
            conteudoExpansivel.setVisibility(View.GONE);
            if (imageButtonSeta != null) imageButtonSeta.setRotation(0);
        }
    }

    private String cleanNutrientName(String name) {
        if (name == null) return "";
        int index = name.lastIndexOf('(');
        if (index > 0) {
            return name.substring(0, index).trim();
        }
        return name.trim();
    }

    private String getUnitForNutrient(String name) {
        if (name.equals("Valor Calórico")) return "kcal";

        if (name.equals("Proteína") || name.equals("Carboidrato") || name.equals("Açúcar Total") || name.equals("Fibra Alimentar") || name.equals("Gordura Total") || name.contains("Gordura") || name.equals("Álcool") || name.equals("Água"))
            return "g";

        if (name.equals("Colesterol") || name.equals("Tiamina") || name.equals("Riboflavina") || name.equals("Niacina") || name.equals("Vitamina B-6") || name.equals("Colina") || name.equals("Vitamina C") || name.equals("Vitamina E") || name.equals("Cálcio") || name.equals("Fósforo") || name.equals("Magnésio") || name.equals("Ferro") || name.equals("Zinco") || name.equals("Cobre") || name.equals("Potássio") || name.equals("Sódio") || name.equals("Cafeína") || name.equals("Teobromina"))
            return "mg";

        if (name.contains("Retinol/Vitamina A") || name.contains("Ácido Fólico") || name.contains("Vitamina B-12") || name.contains("Vitamina D") || name.contains("Vitamina K") || name.contains("Selênio"))
            return "μg";

        return "";
    }
}