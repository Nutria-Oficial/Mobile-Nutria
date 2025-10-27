package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.databinding.ItemCardEscolhaTabelaBinding;
import com.bea.nutria.model.GetTabelaDTO;

import java.util.List;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaViewHolder> {

    private final List<GetTabelaDTO> listaTabelas;
    private OnTabelaClickListener listener;
    private static final String TAG = "TabelaAdapter";

    // Interface para cliques no botão "Escolher Tabela"
    public interface OnTabelaClickListener {
        // MUDANÇA: Agora passa a posição junto com o objeto Tabela
        void onEscolherTabelaClick(GetTabelaDTO tabela, int position);
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
        // USANDO VIEW BINDING para inflar o layout e passá-lo para o ViewHolder
        try {
            ItemCardEscolhaTabelaBinding binding = ItemCardEscolhaTabelaBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new TabelaViewHolder(binding);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inflar o layout: " + e.getMessage());
            throw new RuntimeException("Erro ao criar ViewHolder", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TabelaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetTabelaDTO tabelaAtual = listaTabelas.get(position);

        // 1. Delegar todo o preenchimento dos 35 nutrientes ao ViewHolder
        holder.bind(tabelaAtual);

        // 2. Lógica de expansão/colapso no cabeçalho
        if (holder.headerItem != null) {
            holder.headerItem.setOnClickListener(v -> {
                tabelaAtual.setExpanded(!tabelaAtual.isExpanded()); // Inverte o estado
                notifyItemChanged(position); // Notifica o adapter
            });
        }

        // 3. Listener para o botão "Escolher Tabela"
        if (holder.btnSelecionarTabela != null) {
            holder.btnSelecionarTabela.setOnClickListener(v -> {
                if (listener != null) {
                    // MUDANÇA: Passa a posição para o listener
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
            notifyItemRemoved(position);
        }
    }

    // NOVO MÉTODO: Adiciona um item de volta (usado na deseleção)
    // Para simplificar, adicionamos no final.
    public void addItem(GetTabelaDTO tabela) {
        listaTabelas.add(tabela);
        notifyItemInserted(listaTabelas.size() - 1);
    }
}
