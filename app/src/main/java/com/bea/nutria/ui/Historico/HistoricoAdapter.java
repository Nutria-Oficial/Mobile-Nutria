package com.bea.nutria.ui.Historico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final List<String> nomes = new ArrayList<>();
    private final List<HistoricoFragment.ProdutoItem> produtosOriginais = new ArrayList<>();
    private final List<HistoricoFragment.ProdutoItem> produtosFiltrados = new ArrayList<>();
    private final OnItemClickListener listener;

    public HistoricoAdapter(List<String> nomesIniciais, OnItemClickListener listener) {
        if (nomesIniciais != null) nomes.addAll(nomesIniciais);
        this.listener = listener;
    }

    public void submit(List<String> nomesNovos, List<HistoricoFragment.ProdutoItem> produtosNovos) {
        nomes.clear();
        produtosOriginais.clear();
        produtosFiltrados.clear();

        if (nomesNovos != null) nomes.addAll(nomesNovos);
        if (produtosNovos != null) {
            produtosOriginais.addAll(produtosNovos);
            produtosFiltrados.addAll(produtosNovos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoricoFragment.ProdutoItem p = produtosFiltrados.get(position);
        holder.txtNome.setText(p.nome);
        holder.img.setImageResource(R.drawable.imagem_item_historico);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return produtosFiltrados.size();
    }

    public void filtro(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        produtosFiltrados.clear();

        if (q.isEmpty()) {
            produtosFiltrados.addAll(produtosOriginais);
        } else {
            for (HistoricoFragment.ProdutoItem p : produtosOriginais) {
                if (p.nome.toLowerCase(Locale.ROOT).contains(q)) {
                    produtosFiltrados.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageView img;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.nomeProdutoHistorico);
            img = itemView.findViewById(R.id.imgProdutoHistorico);
        }
    }
}
