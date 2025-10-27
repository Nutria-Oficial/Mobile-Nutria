package com.bea.nutria.ui.Comparacao;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton; // Adicionado
import android.widget.ImageView; // Adicionado

import com.bea.nutria.R;
import com.google.android.material.button.MaterialButton; // Adicionado
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout; // Adicionado
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.databinding.ItemCardEscolhaTabelaBinding; // ⚠️ Ajustado o nome do binding
import com.bea.nutria.model.GetTabelaDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// O ViewHolder agora estende RecyclerView.ViewHolder e usa o Binding
public class TabelaViewHolder extends RecyclerView.ViewHolder {

    private final ItemCardEscolhaTabelaBinding binding; // ⚠️ Referência de Binding
    private final Map<String, TextView> nutrientTextViewMap = new HashMap<>();

    // Componentes principais do item para acesso direto no Adapter
    public final ConstraintLayout headerItem;
    public final MaterialButton btnSelecionarTabela;
    public final TextView textViewTitulo;

    public TabelaViewHolder(@NonNull ItemCardEscolhaTabelaBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        // Referências para o Adapter (para cliques e manipulação básica)
        this.headerItem = binding.headerItem;

        // Acesso ao botão de seleção (mantido com findViewById no container, conforme solicitado)
        this.btnSelecionarTabela = binding.conteudoExpansivel.findViewById(R.id.btn_selecionar_tabela);

        // Acesso ao TextView do Título no Header (mantido com findViewById no container)
        this.textViewTitulo = binding.headerItem.findViewById(R.id.textViewTitulo);

        // Inicializa o mapa com a correspondência
        initializeNutrientMap();
    }

    /**
     * Inicializa o mapa, ligando os nomes exatos dos nutrientes da API (chave)
     * aos IDs dos TextViews no XML (valor), usando findViewById.
     */
    private void initializeNutrientMap() {
        // Nomes de Nutrientes devem corresponder EXATAMENTE aos valores da API
        // Todos os mapeamentos agora usam findViewById no container `conteudoExpansivel`.
        View container = binding.conteudoExpansivel;

        nutrientTextViewMap.put("Valor Calórico", container.findViewById(R.id.tv_valor_calorico));
        nutrientTextViewMap.put("Proteína", container.findViewById(R.id.tv_proteina));
        nutrientTextViewMap.put("Carboidrato", container.findViewById(R.id.tv_carboidrato));
        nutrientTextViewMap.put("Açúcar Total", container.findViewById(R.id.tv_acucar_total));
        nutrientTextViewMap.put("Fibra Alimentar", container.findViewById(R.id.tv_fibra_alimentar));
        nutrientTextViewMap.put("Gordura Total", container.findViewById(R.id.tv_gordura_total));
        nutrientTextViewMap.put("Gordura Saturada", container.findViewById(R.id.tv_gordura_saturada));
        nutrientTextViewMap.put("Gordura Monoinsaturada", container.findViewById(R.id.tv_gordura_monoinsaturada));
        nutrientTextViewMap.put("Gordura Poli-Insaturada", container.findViewById(R.id.tv_gordura_poliinsaturada));
        nutrientTextViewMap.put("Colesterol", container.findViewById(R.id.tv_colesterol));

        // Vitaminas
        nutrientTextViewMap.put("Retinol/Vitamina A", container.findViewById(R.id.tv_vitamina_a));
        nutrientTextViewMap.put("Tiamina", container.findViewById(R.id.tv_tiamina));
        nutrientTextViewMap.put("Riboflavina", container.findViewById(R.id.tv_riboflavina));
        nutrientTextViewMap.put("Niacina", container.findViewById(R.id.tv_niacina));
        nutrientTextViewMap.put("Vitamina B-6", container.findViewById(R.id.tv_vitamina_b6));
        nutrientTextViewMap.put("Ácido Fólico", container.findViewById(R.id.tv_acido_folico));
        nutrientTextViewMap.put("Colina", container.findViewById(R.id.tv_colina));
        nutrientTextViewMap.put("Vitamina B-12", container.findViewById(R.id.tv_vitamina_b12));
        nutrientTextViewMap.put("Vitamina C", container.findViewById(R.id.tv_vitamina_c));
        nutrientTextViewMap.put("Vitamina D", container.findViewById(R.id.tv_vitamina_d));
        nutrientTextViewMap.put("Vitamina E", container.findViewById(R.id.tv_vitamina_e));
        nutrientTextViewMap.put("Vitamina K", container.findViewById(R.id.tv_vitamina_k));

        // Minerais e Outros
        nutrientTextViewMap.put("Cálcio", container.findViewById(R.id.tv_calcio));
        nutrientTextViewMap.put("Fósforo", container.findViewById(R.id.tv_fosforo));
        nutrientTextViewMap.put("Magnésio", container.findViewById(R.id.tv_magnesio));
        nutrientTextViewMap.put("Ferro", container.findViewById(R.id.tv_ferro));
        nutrientTextViewMap.put("Zinco", container.findViewById(R.id.tv_zinco));
        nutrientTextViewMap.put("Cobre", container.findViewById(R.id.tv_cobre));
        nutrientTextViewMap.put("Selênio", container.findViewById(R.id.tv_selenio));
        nutrientTextViewMap.put("Potássio", container.findViewById(R.id.tv_potassio));
        nutrientTextViewMap.put("Sódio", container.findViewById(R.id.tv_sodio));

        // Nutrientes com valorDiario tipicamente "NaN" ou 0
        nutrientTextViewMap.put("Cafeína", container.findViewById(R.id.tv_cafeina));
        nutrientTextViewMap.put("Teobromina", container.findViewById(R.id.tv_teobromina));
        nutrientTextViewMap.put("Álcool", container.findViewById(R.id.tv_alcool));
        nutrientTextViewMap.put("Água", container.findViewById(R.id.tv_agua));
    }

    /**
     * Preenche todas as 35 TextViews com os dados da lista de nutrientes.
     * Deve ser chamado pelo Adapter.
     */
    public void bind(GetTabelaDTO tabela) {

        // 1. Configurar Título do Item
        textViewTitulo.setText(tabela.getNomeTabela());

        // 2. Criar um mapa de pesquisa rápida dos nutrientes
        Map<String, GetTabelaDTO.NutrienteDTO> dataLookup = new HashMap<>();
        List<GetTabelaDTO.NutrienteDTO> nutrientes = tabela.getNutrientes();

        if (nutrientes != null) {
            for (GetTabelaDTO.NutrienteDTO nutriente : nutrientes) {
                // Remove a unidade (se houver) para garantir que a chave corresponda
                String key = cleanNutrientName(nutriente.getNutriente());
                dataLookup.put(key, nutriente);
            }
        }

        // 3. Iterar e preencher os 35 TextViews
        for (Map.Entry<String, TextView> entry : nutrientTextViewMap.entrySet()) {
            String nutrientName = entry.getKey();
            TextView textView = entry.getValue();

            GetTabelaDTO.NutrienteDTO data = dataLookup.get(nutrientName);

            // Valor padrão se o nutriente não for encontrado ou não tiver dado
            String valueToDisplay = "N/A";

            // Unidade que será adicionada
            String unit = getUnitForNutrient(nutrientName);

            if (data != null && data.getTotal() != null) {
                // Usa o método utilitário para formatar o valor total (Double)
                valueToDisplay = NutrienteUtils.formatDoubleToString(data.getTotal());
                valueToDisplay += " " + unit;
            } else if (data != null && data.getTotal() == null) {
                // Se o valor for null, exibe "0.0" com a unidade
                valueToDisplay = "0.0 " + unit;
            }

            // Garante que o TextView existe antes de tentar definir o texto
            if (textView != null) {
                textView.setText(valueToDisplay);
            }
        }

        // 4. Lógica de expansão/colapso da UI (Visibilidade e Rotação da Seta)
        // 🚨 CORREÇÃO APLICADA AQUI: Usando findViewById no container 'headerItem'
        ImageButton seta = binding.headerItem.findViewById(R.id.imageButtonSeta);

        if (tabela.isExpanded()) {
            binding.conteudoExpansivel.setVisibility(View.VISIBLE);
            if (seta != null) seta.setRotation(180);
        } else {
            binding.conteudoExpansivel.setVisibility(View.GONE);
            if (seta != null) seta.setRotation(0);
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
        return name;
    }

    /**
     * Retorna a unidade correta com base no nome do nutriente.
     */
    private String getUnitForNutrient(String name) {
        if (name.contains("Valor Calórico")) return "kcal";
        if (name.endsWith(" (g)")) return "g";
        if (name.endsWith(" (mg)")) return "mg";
        if (name.endsWith(" (μg)")) return "μg";

        // Mapeamento explícito caso a API não envie a unidade no nome:
        if (name.equals("Proteína") || name.equals("Carboidrato") || name.equals("Açúcar Total") || name.equals("Fibra Alimentar") || name.equals("Gordura Total") || name.equals("Álcool") || name.equals("Água") || name.contains("Gordura")) return "g";
        if (name.equals("Colesterol") || name.equals("Tiamina") || name.equals("Riboflavina") || name.equals("Niacina") || name.equals("Vitamina B-6") || name.equals("Colina") || name.equals("Vitamina B-12") || name.equals("Vitamina C") || name.equals("Vitamina E") || name.equals("Cálcio") || name.equals("Fósforo") || name.equals("Magnésio") || name.equals("Ferro") || name.equals("Zinco") || name.equals("Cobre") || name.equals("Potássio") || name.equals("Sódio") || name.equals("Cafeína") || name.equals("Teobromina")) return "mg";
        if (name.contains("Retinol/Vitamina A") || name.contains("Ácido Fólico") || name.contains("Vitamina B-12") || name.contains("Vitamina D") || name.contains("Vitamina K") || name.contains("Selênio")) return "μg";

        return "";
    }
}
