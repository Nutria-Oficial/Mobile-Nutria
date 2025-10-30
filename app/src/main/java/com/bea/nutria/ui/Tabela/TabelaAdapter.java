package com.bea.nutria.ui.Tabela;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.ui.Ingrediente.IngredienteResponse;
import com.bea.nutria.ui.Ingrediente.QuantidadeViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaAdapter.ViewHolder> {

    private final Context context;
    private final List<IngredienteResponse> listaIngredientes;
    private Map<String, Double> quantidades;
    private OnItemRemovedListener listener;
    private final QuantidadeViewModel quantidadeViewModel;

    public interface OnItemRemovedListener {
        void onItemRemoved(IngredienteResponse ingrediente, int newCount);
    }

    public TabelaAdapter(Context context, List<IngredienteResponse> lista, QuantidadeViewModel quantidadeViewModel) {
        this.context = context;
        this.listaIngredientes = lista;
        this.quantidadeViewModel = quantidadeViewModel;
        this.quantidades = new HashMap<>();

        for (IngredienteResponse ing : lista) {
            quantidades.put(ing.getId().toString(), 100.0);
        }
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.listener = listener;
    }
//    public LiveData<String> getQuantidadeIngrediente(){
//        return quantidade;
//    }
//    public void setQuantidadeIngrediente(String valor){
//        quantidade.setValue(valor);
//    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente_tabela, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredienteResponse ingrediente = listaIngredientes.get(position);
        String id = ingrediente.getId().toString();

        holder.nomeIngrediente.setText(ingrediente.getNomeIngrediente());

        String valorAtual = quantidadeViewModel.getQuantidade(Integer.parseInt(id));

        if (valorAtual == null || valorAtual.isEmpty()){
            valorAtual = "100.0";
        }
        holder.txtQuantidade.setText(valorAtual);
//        holder.porcao.setText(String.valueOf(quantidades.getOrDefault(id,100.0)));
        holder.porcao.setText(valorAtual);


        if (holder.watcherAtual != null){
            holder.porcao.removeTextChangedListener(holder.watcherAtual);
        }
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String texto = s.toString();
                    if (texto.contains(",")){
                        texto = texto.replace(",", ".");
                    }
                    double quantidade = Double.parseDouble(texto);
                    quantidades.put(id, quantidade);
                    quantidadeViewModel.setQuantidade(Integer.parseInt(id), String.valueOf(quantidade));
                }catch (NumberFormatException numberFormatException){
                    quantidades.put(id, 0.0);
                    quantidadeViewModel.setQuantidade(Integer.parseInt(id), String.valueOf(0.0));
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };
        holder.porcao.addTextChangedListener(watcher);
        holder.watcherAtual = watcher;

        holder.txtQuantidade.setOnClickListener(v ->{
            holder.txtQuantidade.setVisibility(View.GONE);
            holder.porcao.setVisibility(View.VISIBLE);
            holder.porcao.requestFocus();
        });
        holder.porcao.setOnFocusChangeListener((v, hasFocus) ->{
            if (!hasFocus){
                String valor = holder.porcao.getText().toString().replace(",", ".");
                if (valor.isEmpty()) valor = "0";

                quantidadeViewModel.setQuantidade(Integer.parseInt(id),valor);
                quantidades.put(id, Double.parseDouble(valor));
                holder.txtQuantidade.setText(valor);

                holder.porcao.setVisibility(View.GONE);
                holder.txtQuantidade.setVisibility(View.VISIBLE);
            }
        });

        holder.btnRemover.setOnClickListener(v -> {
            int posicaoAtual = holder.getAdapterPosition();
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                IngredienteResponse ingredienteRemovido = listaIngredientes.get(posicaoAtual);
                listaIngredientes.remove(posicaoAtual);
                quantidades.remove(id);
                quantidadeViewModel.removerQuantidade(Integer.parseInt(id));
                notifyItemRemoved(posicaoAtual);
                notifyItemRangeChanged(posicaoAtual, listaIngredientes.size());

                if (listener != null) {
                    listener.onItemRemoved(ingredienteRemovido, listaIngredientes.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaIngredientes.size();
    }

    public Map<String, Double> getQuantidades() {
        return quantidades;
    }

    public List<IngredienteResponse> getIngredientes() {
        return listaIngredientes;
    }

    public void setIngredientes(List<IngredienteResponse> novaLista) {
        this.listaIngredientes.clear();
        this.listaIngredientes.addAll(novaLista);
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nomeIngrediente;
        TextView txtQuantidade;
        EditText porcao;
        ImageView btnRemover;
        TextWatcher watcherAtual;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeIngrediente = itemView.findViewById(R.id.nomeIngrediente);
            btnRemover = itemView.findViewById(R.id.btnRemover);
            porcao = itemView.findViewById(R.id.porcao);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
        }
    }
}