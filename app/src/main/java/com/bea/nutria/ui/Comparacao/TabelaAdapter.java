package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.model.GetTabelaDTO;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaAdapter.TabelaViewHolder> {

    private final List<GetTabelaDTO> listaTabelas;
    private OnTabelaClickListener listener;
    private static final String TAG = "TabelaAdapter";

    // Interface para cliques no botão "Escolher Tabela"
    public interface OnTabelaClickListener {
        void onEscolherTabelaClick(GetTabelaDTO tabela);
    }

    public void setOnTabelaClickListener(OnTabelaClickListener listener) {
        this.listener = listener;
    }

    public TabelaAdapter(List<GetTabelaDTO> tabelas) {
        this.listaTabelas = tabelas;
    }

    @NonNull
    @Override
    public TabelaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            // Assumimos que 'item_card_comparacao.xml' é o layout de cada item da tabela.
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_card_escolha_tabela, parent, false);
            return new TabelaViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inflar o layout item_card_comparacao: " + e.getMessage());
            // Lançar exceção ou tratar erro de layout.
            return new TabelaViewHolder(new View(parent.getContext())); // Retorno dummy em caso de erro.
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TabelaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetTabelaDTO tabelaAtual = listaTabelas.get(position);

        // --- 1. Parte do Cabeçalho (Sempre Visível) ---
        if (holder.textViewTitulo != null) {
            // EXIBE O NOME DA TABELA: Ponto principal desta correção
            holder.textViewTitulo.setText(tabelaAtual.getNomeTabela());
        }

        // Exemplo: mostrar o ícone de tabela apenas se for uma tabela real
        if (holder.iconeTabela != null) {
            holder.iconeTabela.setVisibility(View.VISIBLE);
            // holder.iconeTabela.setImageResource(R.drawable.xlsx); // Assumindo este ícone
        }


        // Recupera o estado de expansão do item
        boolean isExpanded = tabelaAtual.isExpanded();

        // Ajusta a visibilidade do conteúdo expansível
        if (holder.conteudoExpansivel != null) {
            holder.conteudoExpansivel.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
        // Ajusta a rotação da seta (se o seu layout tiver um botão de seta)
        if (holder.imageButtonSeta != null) {
            holder.imageButtonSeta.setRotation(isExpanded ? 180 : 0);
        }

        // --- 2. Preenchimento de Detalhes Nutricionais ---
        // Preenche Valor Energético
        GetTabelaDTO.NutrienteDTO valorEnergetico = findNutriente(tabelaAtual.getNutrientes(), "Valor Calórico (kcal)");
        if (holder.tvValorEnergetico != null && valorEnergetico != null && valorEnergetico.getTotal() != null) {
            holder.tvValorEnergetico.setText(String.format(Locale.getDefault(), "%.0f kcal", valorEnergetico.getTotal()));
        }

        // Preenche Açúcares Totais
        GetTabelaDTO.NutrienteDTO acucaresTotais = findNutriente(tabelaAtual.getNutrientes(), "Açúcar Total (g)");
        if (holder.tvAcucares != null && acucaresTotais != null && acucaresTotais.getTotal() != null) {
            holder.tvAcucares.setText(String.format(Locale.getDefault(), "%.1f g", acucaresTotais.getTotal()));
        }

        // --- 3. Listeners de Clique ---

        // Listener para o cabeçalho (expandir/recolher)
        if (holder.headerItem != null) {
            holder.headerItem.setOnClickListener(v -> {
                tabelaAtual.setExpanded(!tabelaAtual.isExpanded()); // Inverte o estado
                notifyItemChanged(position); // Notifica o adapter
            });
        }

        // Listener para o botão "Escolher Tabela"
        if (holder.btnSelecionarTabela != null) {
            holder.btnSelecionarTabela.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEscolherTabelaClick(tabelaAtual);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaTabelas.size();
    }

    // Método auxiliar para encontrar um nutriente pelo nome
    private GetTabelaDTO.NutrienteDTO findNutriente(List<GetTabelaDTO.NutrienteDTO> nutrientes, String nomeNutriente) {
        if (nutrientes == null) return null;
        for (GetTabelaDTO.NutrienteDTO nutriente : nutrientes) {
            if (nutriente.getNutriente() != null && nutriente.getNutriente().equals(nomeNutriente)) {
                return nutriente;
            }
        }
        return null;
    }

    // =================================================================
    // ViewHolder
    // =================================================================

    public static class TabelaViewHolder extends RecyclerView.ViewHolder {
        // Componentes do Cabeçalho
        View headerItem; // O cabeçalho clicável (ConstraintLayout ou LinearLayout)
        TextView textViewTitulo; // ID que deve conter o nome da tabela
        ImageView iconeTabela; // O ícone xlsx, se existir no item_card_comparacao
        ImageButton imageButtonSeta; // Assumindo que você tem um botão/imagem para seta

        // Componentes do Conteúdo Expansível
        LinearLayout conteudoExpansivel;
        TextView tvValorEnergetico;
        TextView tvAcucares;
        MaterialButton btnSelecionarTabela;

        TabelaViewHolder(@NonNull View itemView) {
            super(itemView);

            // Referências que devem estar em R.layout.item_card_comparacao
            headerItem = itemView.findViewById(R.id.header_item);
            textViewTitulo = itemView.findViewById(R.id.textViewTitulo);
            iconeTabela = itemView.findViewById(R.id.imageView3); // Assumindo o ID do ícone XLSX no seu XML
            imageButtonSeta = itemView.findViewById(R.id.imageButtonSeta); // Assumindo ID para seta

            // Referências do Conteúdo Expansível
            conteudoExpansivel = itemView.findViewById(R.id.conteudo_expansivel);
            tvValorEnergetico = itemView.findViewById(R.id.tv_valor_energetico);
            tvAcucares = itemView.findViewById(R.id.tv_acucares);
            btnSelecionarTabela = itemView.findViewById(R.id.btn_selecionar_tabela);
        }
    }
}