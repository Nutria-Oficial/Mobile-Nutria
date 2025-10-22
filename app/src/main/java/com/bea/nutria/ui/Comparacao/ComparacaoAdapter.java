package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.util.Log; // Adicionado para Log
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

    private List<GetProdutoDTO> listaProdutosExibida;
    private final List<GetProdutoDTO> listaProdutosOriginal;
    private OnItemClickListener listener;
    private static final String TAG = "ComparacaoAdapter"; // Tag para Log

    public interface OnItemClickListener {
        void onItemClick(GetProdutoDTO produto);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ComparacaoAdapter(List<GetProdutoDTO> produtos) {
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
            // Este log será útil se o layout não puder ser inflado
            Log.e(TAG, "Erro ao inflar o layout item_card_comparacao: " + e.getMessage());
            throw e; // Lança a exceção para que ela apareça no logcat
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetProdutoDTO produtoAtual = listaProdutosExibida.get(position);

        // Verificação de NULO no holder antes de tentar usar o setText
        if (holder.txtNome != null) {
            holder.txtNome.setText(produtoAtual.getNome());
        } else {
            Log.e(TAG, "txtNome é NULL. O ID R.id.textViewTitulo está incorreto ou a View não existe.");
        }

        // Define o listener de clique
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(produtoAtual);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutosExibida.size();
    }

    // ... (Métodos filtro e updateList existentes) ...

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageView img;

        ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Verificação de ID: Se esta linha retornar null, a falha acontece no onBind.
            txtNome = itemView.findViewById(R.id.textViewTitulo);
            img = itemView.findViewById(R.id.imageView3);
        }
    }
}