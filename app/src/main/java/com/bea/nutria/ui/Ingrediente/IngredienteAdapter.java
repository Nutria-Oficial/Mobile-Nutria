package com.bea.nutria.ui.Ingrediente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;

import java.util.ArrayList;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private ArrayList<String> lista;
    private Context context;
    private ArrayList<Boolean> estadoBotao;

    public IngredienteAdapter(Context context, ArrayList<String> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.context = context;

        estadoBotao = new ArrayList<>();
        for (int i = 0; i < this.lista.size(); i++) {
            estadoBotao.add(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!lista.isEmpty()) {
            String item = lista.get(position);
            holder.txtNomeIngrediente.setText(item);
        }

        boolean clicado = estadoBotao.get(position);
        configurarBotao(holder.btAddIngrediente, clicado);

        holder.btAddIngrediente.setOnClickListener(v -> {
            boolean novoEstado = !estadoBotao.get(position);
            estadoBotao.set(position, novoEstado);
            configurarBotao(holder.btAddIngrediente, novoEstado);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private void configurarBotao(Button botao, boolean clicado) {
        if (clicado) {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange));
            botao.setTextColor(ContextCompat.getColor(context, R.color.gray));
            botao.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray));
            botao.setText("Adicionado");
        } else {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange));
            botao.setTextColor(ContextCompat.getColor(context, R.color.white));
            botao.setBackgroundColor(ContextCompat.getColor(context, R.color.orange));
            botao.setText("Adicionar");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button btAddIngrediente;
        TextView txtNomeIngrediente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btAddIngrediente = itemView.findViewById(R.id.btAddIngrediente);
            txtNomeIngrediente = itemView.findViewById(R.id.txtNomeIngrediente);
        }
    }
}