package com.bea.nutria.ui.Comparacao;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.bea.nutria.R;
import com.bea.nutria.model.GetTabelaDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Classe simples para armazenar o par de TextViews (Valor e Valor Diário)
class NutrientTextViews {
    public final TextView tvValor;
    public final TextView tvVD;

    public NutrientTextViews(TextView tvValor, TextView tvVD) {
        this.tvValor = tvValor;
        this.tvVD = tvVD;
    }
}

public class TabelaViewHolder extends RecyclerView.ViewHolder {

    // Mapeia o nome do nutriente da API para o par de TextViews no layout
    private final Map<String, NutrientTextViews> nutrientTextViewMap = new HashMap<>();

    // Componentes principais do item para acesso direto no Adapter
    public final View headerItem; // Alterado de ConstraintLayout para View (para simplificar)
    public final MaterialButton btnSelecionarTabela;
    public final TextView textViewTitulo;

    // Componentes internos para controle de estado
    private final ImageButton imageButtonSeta;
    private final View conteudoExpansivel;
    private final TextView tvTabelaTituloDetalhes; // Para o título da porção/detalhes
    private final View cardBlocoTabela; // Para encontrar o tvTabelaTitulo

    // O construtor agora recebe a View root do item_card_escolha_tabela.xml
    public TabelaViewHolder(@NonNull View itemView) {
        super(itemView);

        // 1. Inicializa os componentes principais (acessíveis no Adapter)
        this.headerItem = itemView.findViewById(R.id.header_item);

        // O bloco 'conteudo_expansivel' deve ser encontrado primeiro para buscar
        // componentes internos (como o botão e a tabela)
        this.conteudoExpansivel = itemView.findViewById(R.id.conteudo_expansivel);
        this.btnSelecionarTabela = conteudoExpansivel.findViewById(R.id.btn_selecionar_tabela);

        // Componentes internos do header
        this.textViewTitulo = headerItem.findViewById(R.id.textViewTitulo);
        this.imageButtonSeta = headerItem.findViewById(R.id.imageButtonSeta);

        // Componentes internos do bloco expansível
        this.cardBlocoTabela = conteudoExpansivel.findViewById(R.id.cardBlocoTabela);
        this.tvTabelaTituloDetalhes = cardBlocoTabela.findViewById(R.id.tvTabelaTitulo);


        // 2. Inicializa o mapa com a correspondência
        initializeNutrientMap();
    }

    /**
     * Inicializa o mapa, ligando os nomes exatos dos nutrientes da API (chave)
     * aos IDs dos pares de TextViews no XML (valor).
     */
    private void initializeNutrientMap() {
        // Agora busca no cardBlocoTabela, que é o container de todas as linhas da tabela
        View container = cardBlocoTabela;

        // --- MAPAS DE NUTRIENTES ---

        // VALOR CALÓRICO
        nutrientTextViewMap.put("Valor Calórico", new NutrientTextViews(
                container.findViewById(R.id.tv_valor_calorico),
                container.findViewById(R.id.tv_vd_valor_calorico)
        ));

        // PROTEÍNA
        nutrientTextViewMap.put("Proteína", new NutrientTextViews(
                container.findViewById(R.id.tv_proteina),
                container.findViewById(R.id.tv_vd_proteina)
        ));

        // CARBOIDRATO
        nutrientTextViewMap.put("Carboidrato", new NutrientTextViews(
                container.findViewById(R.id.tv_carboidrato),
                container.findViewById(R.id.tv_vd_carboidrato)
        ));

        // AÇÚCAR TOTAL
        nutrientTextViewMap.put("Açúcar Total", new NutrientTextViews(
                container.findViewById(R.id.tv_acucar_total),
                container.findViewById(R.id.tv_vd_acucar_total)
        ));

        // FIBRA ALIMENTAR
        nutrientTextViewMap.put("Fibra Alimentar", new NutrientTextViews(
                container.findViewById(R.id.tv_fibra_alimentar),
                container.findViewById(R.id.tv_vd_fibra_alimentar)
        ));

        // GORDURA TOTAL
        nutrientTextViewMap.put("Gordura Total", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_total),
                container.findViewById(R.id.tv_vd_gordura_total)
        ));

        // GORDURA SATURADA
        nutrientTextViewMap.put("Gordura Saturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_saturada),
                container.findViewById(R.id.tv_vd_gordura_saturada)
        ));

        // GORDURA MONOINSATURADA
        nutrientTextViewMap.put("Gordura Monoinsaturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_monoinsaturada),
                container.findViewById(R.id.tv_vd_gordura_monoinsaturada)
        ));

        // GORDURA POLI-INSATURADA
        nutrientTextViewMap.put("Gordura Poli-Insaturada", new NutrientTextViews(
                container.findViewById(R.id.tv_gordura_poliinsaturada),
                container.findViewById(R.id.tv_vd_gordura_poliinsaturada)
        ));

        // COLESTEROL
        nutrientTextViewMap.put("Colesterol", new NutrientTextViews(
                container.findViewById(R.id.tv_colesterol),
                container.findViewById(R.id.tv_vd_colesterol)
        ));

        // --- VITAMINAS ---

        // RETINOL/VITAMINA A
        nutrientTextViewMap.put("Retinol/Vitamina A", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_a),
                container.findViewById(R.id.tv_vd_vitamina_a)
        ));

        // TIAMINA
        nutrientTextViewMap.put("Tiamina", new NutrientTextViews(
                container.findViewById(R.id.tv_tiamina),
                container.findViewById(R.id.tv_vd_tiamina)
        ));

        // RIBOFLAVINA
        nutrientTextViewMap.put("Riboflavina", new NutrientTextViews(
                container.findViewById(R.id.tv_riboflavina),
                container.findViewById(R.id.tv_vd_riboflavina)
        ));

        // NIACINA
        nutrientTextViewMap.put("Niacina", new NutrientTextViews(
                container.findViewById(R.id.tv_niacina),
                container.findViewById(R.id.tv_vd_niacina)
        ));

        // VITAMINA B-6
        nutrientTextViewMap.put("Vitamina B-6", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_b6),
                container.findViewById(R.id.tv_vd_vitamina_b6)
        ));

        // ÁCIDO FÓLICO
        nutrientTextViewMap.put("Ácido Fólico", new NutrientTextViews(
                container.findViewById(R.id.tv_acido_folico),
                container.findViewById(R.id.tv_vd_acido_folico)
        ));

        // COLINA
        nutrientTextViewMap.put("Colina", new NutrientTextViews(
                container.findViewById(R.id.tv_colina),
                container.findViewById(R.id.tv_vd_colina)
        ));

        // VITAMINA B-12
        nutrientTextViewMap.put("Vitamina B-12", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_b12),
                container.findViewById(R.id.tv_vd_vitamina_b12)
        ));

        // VITAMINA C
        nutrientTextViewMap.put("Vitamina C", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_c),
                container.findViewById(R.id.tv_vd_vitamina_c)
        ));

        // VITAMINA D
        nutrientTextViewMap.put("Vitamina D", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_d),
                container.findViewById(R.id.tv_vd_vitamina_d)
        ));

        // VITAMINA E
        nutrientTextViewMap.put("Vitamina E", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_e),
                container.findViewById(R.id.tv_vd_vitamina_e)
        ));

        // VITAMINA K
        nutrientTextViewMap.put("Vitamina K", new NutrientTextViews(
                container.findViewById(R.id.tv_vitamina_k),
                container.findViewById(R.id.tv_vd_vitamina_k)
        ));

        // --- MINERAIS E OUTROS ---

        // CÁLCIO
        nutrientTextViewMap.put("Cálcio", new NutrientTextViews(
                container.findViewById(R.id.tv_calcio),
                container.findViewById(R.id.tv_vd_calcio)
        ));

        // FÓSFORO
        nutrientTextViewMap.put("Fósforo", new NutrientTextViews(
                container.findViewById(R.id.tv_fosforo),
                container.findViewById(R.id.tv_vd_fosforo)
        ));

        // MAGNÉSIO
        nutrientTextViewMap.put("Magnésio", new NutrientTextViews(
                container.findViewById(R.id.tv_magnesio),
                container.findViewById(R.id.tv_vd_magnesio)
        ));

        // FERRO
        nutrientTextViewMap.put("Ferro", new NutrientTextViews(
                container.findViewById(R.id.tv_ferro),
                container.findViewById(R.id.tv_vd_ferro)
        ));

        // ZINCO
        nutrientTextViewMap.put("Zinco", new NutrientTextViews(
                container.findViewById(R.id.tv_zinco),
                container.findViewById(R.id.tv_vd_zinco)
        ));

        // COBRE
        nutrientTextViewMap.put("Cobre", new NutrientTextViews(
                container.findViewById(R.id.tv_cobre),
                container.findViewById(R.id.tv_vd_cobre)
        ));

        // SELÊNIO
        nutrientTextViewMap.put("Selênio", new NutrientTextViews(
                container.findViewById(R.id.tv_selenio),
                container.findViewById(R.id.tv_vd_selenio)
        ));

        // POTÁSSIO
        nutrientTextViewMap.put("Potássio", new NutrientTextViews(
                container.findViewById(R.id.tv_potassio),
                container.findViewById(R.id.tv_vd_potassio)
        ));

        // SÓDIO
        nutrientTextViewMap.put("Sódio", new NutrientTextViews(
                container.findViewById(R.id.tv_sodio),
                container.findViewById(R.id.tv_vd_sodio)
        ));

        // CAFEÍNA
        nutrientTextViewMap.put("Cafeína", new NutrientTextViews(
                container.findViewById(R.id.tv_cafeina),
                container.findViewById(R.id.tv_vd_cafeina)
        ));

        // TEOBROMINA
        nutrientTextViewMap.put("Teobromina", new NutrientTextViews(
                container.findViewById(R.id.tv_teobromina),
                container.findViewById(R.id.tv_vd_teobromina)
        ));

        // ÁLCOOL
        nutrientTextViewMap.put("Álcool", new NutrientTextViews(
                container.findViewById(R.id.tv_alcool),
                container.findViewById(R.id.tv_vd_alcool)
        ));

        // ÁGUA
        nutrientTextViewMap.put("Água", new NutrientTextViews(
                container.findViewById(R.id.tv_agua),
                container.findViewById(R.id.tv_vd_agua)
        ));
    }

    /**
     * Preenche todas as 35 TextViews com os dados da lista de nutrientes.
     */
    public void bind(GetTabelaDTO tabela) {

        // 1. Configurar Título do Item
        textViewTitulo.setText(tabela.getNomeTabela());

        // 2. Configurar o título detalhado da tabela (se existir)
        if (tvTabelaTituloDetalhes != null) {
            String detalhes = String.format(Locale.getDefault(),
                    "Detalhes Nutricionais (Porção: %.1fg)",
                    tabela.getPorcao());
            tvTabelaTituloDetalhes.setText(detalhes);
        }

        // 3. Cria um mapa de pesquisa rápida dos nutrientes vindos da API
        Map<String, GetTabelaDTO.NutrienteDTO> dataLookup = new HashMap<>();
        List<GetTabelaDTO.NutrienteDTO> nutrientes = tabela.getNutrientes();

        if (nutrientes != null) {
            for (GetTabelaDTO.NutrienteDTO nutriente : nutrientes) {
                // Limpa o nome para corresponder à chave do nosso mapa (Ex: "Proteína (g)" -> "Proteína")
                String key = cleanNutrientName(nutriente.getNutriente());
                dataLookup.put(key, nutriente);
            }
        }

        // 4. Limpa todos os campos e preenche com os dados da API
        for (Map.Entry<String, NutrientTextViews> entry : nutrientTextViewMap.entrySet()) {
            String nutrientName = entry.getKey();
            NutrientTextViews tvs = entry.getValue();

            GetTabelaDTO.NutrienteDTO data = dataLookup.get(nutrientName);

            // Valores padrão se o nutriente não for encontrado na API
            String valorToDisplay = "-";
            String vdToDisplay = "-";

            // Unidade (usa a função utilitária)
            String unit = getUnitForNutrient(nutrientName);

            if (data != null) {
                // Preenche o Valor (Usando o 'total' da API para o valor na tabela)
                Double valor = data.getTotal();

                if (valor != null) {
                    valorToDisplay = String.format(Locale.getDefault(), "%.1f %s", valor, unit);
                } else {
                    valorToDisplay = "0.0 " + unit;
                }

                // Preenche o %VD
                Object vd = data.getValorDiario();
                if (vd != null && !vd.toString().equalsIgnoreCase("NaN")) {
                    if (vd instanceof Double) {
                        vdToDisplay = String.format(Locale.getDefault(), "%.0f%%", (Double) vd * 100);
                    } else {
                        vdToDisplay = vd.toString(); // Ex: '-' se for String
                    }
                }
            }

            // Aplica os textos nos TextViews
            if (tvs.tvValor != null) tvs.tvValor.setText(valorToDisplay);
            if (tvs.tvVD != null) tvs.tvVD.setText(vdToDisplay);
        }

        // 5. Lógica de expansão/colapso da UI
        if (tabela.isExpanded()) {
            conteudoExpansivel.setVisibility(View.VISIBLE);
            if (imageButtonSeta != null) imageButtonSeta.setRotation(180);
        } else {
            conteudoExpansivel.setVisibility(View.GONE);
            if (imageButtonSeta != null) imageButtonSeta.setRotation(0);
        }
    }

    /**
     * Remove a unidade do nome do nutriente da API para criar uma chave limpa.
     * Ex: "Proteína (g)" -> "Proteína"
     */
    private String cleanNutrientName(String name) {
        if (name == null) return "";
        int index = name.lastIndexOf('(');
        if (index > 0) {
            return name.substring(0, index).trim();
        }
        return name.trim();
    }

    /**
     * Retorna a unidade correta com base no nome do nutriente.
     */
    private String getUnitForNutrient(String name) {
        if (name.equals("Valor Calórico")) return "kcal";

        // G (gramas)
        if (name.equals("Proteína") || name.equals("Carboidrato") || name.equals("Açúcar Total") || name.equals("Fibra Alimentar") || name.equals("Gordura Total") || name.contains("Gordura") || name.equals("Álcool") || name.equals("Água"))
            return "g";

        // MG (miligramas)
        if (name.equals("Colesterol") || name.equals("Tiamina") || name.equals("Riboflavina") || name.equals("Niacina") || name.equals("Vitamina B-6") || name.equals("Colina") || name.equals("Vitamina C") || name.equals("Vitamina E") || name.equals("Cálcio") || name.equals("Fósforo") || name.equals("Magnésio") || name.equals("Ferro") || name.equals("Zinco") || name.equals("Cobre") || name.equals("Potássio") || name.equals("Sódio") || name.equals("Cafeína") || name.equals("Teobromina"))
            return "mg";

        // µG (microgramas)
        if (name.contains("Retinol/Vitamina A") || name.contains("Ácido Fólico") || name.contains("Vitamina B-12") || name.contains("Vitamina D") || name.contains("Vitamina K") || name.contains("Selênio"))
            return "μg";

        return "";
    }
}