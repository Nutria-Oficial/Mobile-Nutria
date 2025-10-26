package com.bea.nutria.ui.Tabela;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.ui.Ingrediente.IngredienteResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaAdapter.ViewHolder> {

    private Context context;
    private List<IngredienteResponse> listaIngredientes;
    private Map<String, Integer> quantidades;
    private OnItemRemovedListener listener;

    public interface OnItemRemovedListener {
        void onItemRemoved(IngredienteResponse ingrediente, int newCount);
    }

    public TabelaAdapter(Context context, List<IngredienteResponse> lista) {
        this.context = context;
        this.listaIngredientes = lista;
        this.quantidades = new HashMap<>();

        for (IngredienteResponse ing : lista) {
            quantidades.put(ing.getId().toString(), 100);
        }
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredienteResponse ingrediente = listaIngredientes.get(position);

        holder.txtNome.setText(ingrediente.getNomeIngrediente());
        holder.btAdd.setText("Remover");
        holder.btAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                context.getResources().getColor(android.R.color.holo_red_dark)
        ));

        holder.btAdd.setOnClickListener(v -> {
            int posicaoAtual = holder.getAdapterPosition();
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                IngredienteResponse ingredienteRemovido = listaIngredientes.get(posicaoAtual); // salvar o ingrediente antes de remover
                listaIngredientes.remove(posicaoAtual);
                quantidades.remove(ingrediente.getId());
                notifyItemRemoved(posicaoAtual);
                notifyItemRangeChanged(posicaoAtual, listaIngredientes.size());

                if (listener != null) {
                    listener.onItemRemoved(ingredienteRemovido, listaIngredientes.size()); // passar o ingrediente
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaIngredientes.size();
    }

    public Map<String, Integer> getQuantidades() {
        return quantidades;
    }

    public List<IngredienteResponse> getIngredientes() {
        return listaIngredientes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        Button btAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomeIngrediente);
            btAdd = itemView.findViewById(R.id.btAddIngrediente);
        }
    }
}