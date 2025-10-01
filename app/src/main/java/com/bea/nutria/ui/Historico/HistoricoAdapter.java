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

    private List<String> nomes; //lista exibida
    private List<String> nomeOriginal; //cópia da lista completa (sem filtro)

    public HistoricoAdapter(List<String> nomes) {
        this.nomes = new ArrayList<>(nomes);
        this.nomeOriginal = new ArrayList<>(nomes);
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
        String nome = nomes.get(position);
        holder.txtNome.setText(nome);
        holder.img.setImageResource(R.drawable.imagem_item_historico);
    }

    @Override
    public int getItemCount() {
        return nomes.size();
    }

    public void filtro(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        nomes.clear();

        if (q.isEmpty()) {
            nomes.addAll(nomeOriginal); // sem busca fica na lista original
        } else {
            for (String s : nomeOriginal) {
                if (s.toLowerCase(Locale.ROOT).contains(q)) {
                    nomes.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageView img;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.nomeProdutoHistorico);
            img = itemView.findViewById(R.id.imgProdutoHistorico);
        }
    }
}
