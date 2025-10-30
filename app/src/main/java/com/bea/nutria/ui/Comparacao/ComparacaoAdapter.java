package com.bea.nutria.ui.Comparacao;
import android.util.Log;
import android.annotation.SuppressLint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.model.GetProdutoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComparacaoAdapter extends RecyclerView.Adapter<ComparacaoAdapter.ProdutoViewHolder> {

    // A lista de exibição AGORA PODE SER MODIFICADA (Mutable)
    private final List<GetProdutoDTO> listaProdutosExibida;
    private final List<GetProdutoDTO> listaProdutosOriginal;
    private OnItemClickListener listener;
    private static final String TAG = "ComparacaoAdapter";

    public interface OnItemClickListener {
        void onItemClick(GetProdutoDTO produto);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ComparacaoAdapter(List<GetProdutoDTO> produtos) {
        // Inicializa ambas as listas
        this.listaProdutosExibida = new ArrayList<>(produtos);
        this.listaProdutosOriginal = new ArrayList<>(produtos);
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_card_comparacao, parent, false);
            return new ProdutoViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inflar o layout item_card_comparacao: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetProdutoDTO produtoAtual = listaProdutosExibida.get(position);

        if (holder.txtNome != null) {
            holder.txtNome.setText(produtoAtual.getNome());
        } else {
            Log.e(TAG, "txtNome é NULL. O ID R.id.textViewTitulo está incorreto ou a View não existe.");
        }

        // Define o listener de clique
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Passa o produto completo para o Fragment
                listener.onItemClick(produtoAtual);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutosExibida.size();
    }

    /**
     * NOVO: Método para adicionar um item de volta à lista e notificar a RecyclerView.
     * O item é adicionado ao final da lista.
     * @param produto O produto a ser adicionado.
     */
    public void addItem(GetProdutoDTO produto) {
        if (!listaProdutosExibida.contains(produto)) {
            listaProdutosExibida.add(produto);
            // Notifica o adapter para animar a inserção no final
            notifyItemInserted(listaProdutosExibida.size() - 1);
        }
    }

    /**
     * Método para remover um item e notificar a RecyclerView.
     * @param produto O produto a ser removido.
     */
    public void removeItem(GetProdutoDTO produto) {
        int position = listaProdutosExibida.indexOf(produto);
        if (position != -1) {
            listaProdutosExibida.remove(position);
            // Notifica o adapter para animar a remoção
            notifyItemRemoved(position);
        }
    }

    /**
     * Permite que o Fragment acesse a lista de produtos atualizada.
     */
    public List<GetProdutoDTO> getListaProdutosExibida() {
        return listaProdutosExibida;
    }


    // ... (Métodos filtro e updateList existentes) ...

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageView img;

        ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.textViewTitulo);
            img = itemView.findViewById(R.id.imageView3);
        }
    }
}
