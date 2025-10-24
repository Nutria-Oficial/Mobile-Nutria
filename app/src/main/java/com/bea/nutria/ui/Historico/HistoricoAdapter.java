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

    public interface OnItemClickListener { void onItemClick(int position); }

    private final OnItemClickListener listener;
    private final List<String> nomes = new ArrayList<>();
    private final List<String> nomesOriginais = new ArrayList<>();
    private final List<HistoricoFragment.ProdutoItem> produtos = new ArrayList<>();

    public HistoricoAdapter(List<String> nomesIniciais, OnItemClickListener listener) {
        if (nomesIniciais != null) {
            nomes.addAll(nomesIniciais);
            nomesOriginais.addAll(nomesIniciais);
        }
        this.listener = listener;
    }

    public void submit(List<String> novosNomes, List<HistoricoFragment.ProdutoItem> novosProdutos) {
        nomes.clear();
        nomesOriginais.clear();
        produtos.clear();

        if (novosNomes != null) {
            nomes.addAll(novosNomes);
            nomesOriginais.addAll(novosNomes);
        }
        if (novosProdutos != null) {
            produtos.addAll(novosProdutos);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtNome.setText(nomes.get(position));
        holder.img.setImageResource(R.drawable.imagem_item_historico);
    }

    @Override
    public int getItemCount() { return nomes.size(); }

    public void filtro(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        nomes.clear();

        if (q.isEmpty()) {
            nomes.addAll(nomesOriginais);
        } else {
            for (String s : nomesOriginais) {
                if (s.toLowerCase(Locale.ROOT).contains(q)) nomes.add(s);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageView img;
        ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.nomeProdutoHistorico);
            img = itemView.findViewById(R.id.imgProdutoHistorico);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) listener.onItemClick(pos);
            });
        }
    }
}
