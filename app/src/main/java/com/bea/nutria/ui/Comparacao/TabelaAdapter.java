package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.model.GetTabelaComparacaoDTO;

import java.util.List;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaViewHolder> {

    private final List<GetTabelaComparacaoDTO> listaTabelas;
    private OnTabelaClickListener listener;
    private static final String TAG = "TabelaAdapter";

    // Interface para cliques no botão "Escolher Tabela"
    public interface OnTabelaClickListener {
        void onEscolherTabelaClick(GetTabelaComparacaoDTO tabela, int position);
    }

    public void setOnTabelaClickListener(OnTabelaClickListener listener) {
        this.listener = listener;
    }

    public TabelaAdapter(List<GetTabelaComparacaoDTO> tabelas) {
        this.listaTabelas = tabelas;

        // ⭐️ 1. HABILITA IDs ESTÁVEIS
        setHasStableIds(true);
    }

    // ⭐️ 2. NOVO MÉTODO OBRIGATÓRIO: Retorna o ID único do item
    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < listaTabelas.size()) {
            // Se o GetTabelaComparacaoDTO tiver um campo 'id', use-o.
            // Exemplo assumindo que GetTabelaComparacaoDTO tem um método getId() que retorna long.
            return listaTabelas.get(position).getTabelaId();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public TabelaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_card_escolha_tabela,
                    parent,
                    false
            );
            return new TabelaViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inflar o layout: " + e.getMessage());
            throw new RuntimeException("Erro ao criar ViewHolder", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TabelaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetTabelaComparacaoDTO tabelaAtual = listaTabelas.get(position);

        // 1. Delegar todo o preenchimento dos 35 nutrientes ao ViewHolder
        holder.bind(tabelaAtual);

        // 2. Lógica de expansão/colapso no cabeçalho
        if (holder.headerItem != null) {
            holder.headerItem.setOnClickListener(v -> {
                tabelaAtual.setExpanded(!tabelaAtual.isExpanded()); // Inverte o estado

                // ⭐️ Recomenda-se usar getBindingAdapterPosition() se a posição for usada em lógica complexa
                // mas para notifyItemChanged, a 'position' anotada já funciona.
                notifyItemChanged(position);
            });
        }

        // 3. Listener para o botão "Escolher Tabela"
        if (holder.btnSelecionarTabela != null) {
            holder.btnSelecionarTabela.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEscolherTabelaClick(tabelaAtual, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaTabelas.size();
    }

    // NOVO MÉTODO: Remove um item da lista e notifica a RecyclerView
    public void removeItem(int position) {
        if (position >= 0 && position < listaTabelas.size()) {
            listaTabelas.remove(position);

            // ⭐️ O notifyItemRemoved(position) ainda é usado, mas com IDs estáveis,
            // a RecyclerView recalcula as posições de forma mais confiável.
            notifyItemRemoved(position);
        }
    }

    // NOVO MÉTODO: Adiciona um item de volta (usado na deseleção)
    public void addItem(GetTabelaComparacaoDTO tabela) {
        listaTabelas.add(tabela);
        notifyItemInserted(listaTabelas.size() - 1);
    }
}