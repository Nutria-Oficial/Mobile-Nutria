package com.bea.nutria.ui.Ingrediente;

import android.content.Context;
import android.os.Bundle;
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
import java.util.List;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private List<IngredienteResponse> lista;
    private Context context;
    private List<IngredienteResponse> ingredientesSelecionados;
    private OnIngredienteChangeListener listener;

    public void adicionarIngrediente(IngredienteResponse ingredienteCriado) {
        lista.add(ingredienteCriado);
        notifyDataSetChanged();
    }

    public interface OnIngredienteChangeListener {
        void onIngredienteAdicionado(IngredienteResponse ingrediente);
        void onIngredienteRemovido(IngredienteResponse ingrediente);
    }

    public IngredienteAdapter(Context context, List<IngredienteResponse> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.context = context;
        this.ingredientesSelecionados = new ArrayList<>();
    }

    public void setOnIngredienteChangeListener(OnIngredienteChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= lista.size()) {
            return;
        }

        IngredienteResponse ingrediente = lista.get(position);
        holder.txtNomeIngrediente.setText(ingrediente.getNomeIngrediente());

        boolean estaSelecionado = estaNaListaSelecionados(ingrediente);
        configurarBotao(holder.btAddIngrediente, estaSelecionado);

        holder.btAddIngrediente.setOnClickListener(v -> {
            if (estaSelecionado) {
                removerDaSelecao(ingrediente);
                configurarBotao(holder.btAddIngrediente, false);

                if (listener != null) {
                    listener.onIngredienteRemovido(ingrediente);
                }
            } else {
                ingredientesSelecionados.add(ingrediente);
                configurarBotao(holder.btAddIngrediente, true);

                if (listener != null) {
                    listener.onIngredienteAdicionado(ingrediente);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private boolean estaNaListaSelecionados(IngredienteResponse ingrediente) {
        for (IngredienteResponse selecionado : ingredientesSelecionados) {
            if (selecionado.getId().equals(ingrediente.getId())) {
                return true;
            }
        }
        return false;
    }

    private void removerDaSelecao(IngredienteResponse ingrediente) {
        for (int i = 0; i < ingredientesSelecionados.size(); i++) {
            if (ingredientesSelecionados.get(i).getId().equals(ingrediente.getId())) {
                ingredientesSelecionados.remove(i);
                break;
            }
        }
    }

    private void configurarBotao(Button botao, boolean selecionado) {
        if (selecionado) {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
            botao.setTextColor(ContextCompat.getColor(context, R.color.gray));
            botao.setText("Adicionado");
        } else {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange));
            botao.setTextColor(ContextCompat.getColor(context, R.color.white));
            botao.setText("Adicionar");
        }
    }

    public void atualizarLista(List<IngredienteResponse> novaLista) {
        this.lista = novaLista != null ? novaLista : new ArrayList<>();

        // selecionados primeiro vão ficar em cima
        List<IngredienteResponse> listaOrdenada = new ArrayList<>();
        List<IngredienteResponse> naoSelecionados = new ArrayList<>();

        for (IngredienteResponse ing : this.lista) {
            if (estaNaListaSelecionados(ing)) {
                listaOrdenada.add(ing);
            } else {
                naoSelecionados.add(ing);
            }
        }

        listaOrdenada.addAll(naoSelecionados);
        this.lista = listaOrdenada;

        notifyDataSetChanged();
    }

    // restaurar seleção do ViewModel --> pegar selecionados e restaurar
    public void restaurarSelecao(List<IngredienteResponse> selecionados) {
        this.ingredientesSelecionados.clear();
        if (selecionados != null) {
            this.ingredientesSelecionados.addAll(selecionados);
        }
        notifyDataSetChanged();
    }

    // obter lista de selecionados
    public List<IngredienteResponse> getListaSelecionados() {
        return new ArrayList<>(ingredientesSelecionados);
    }

    public Bundle getIngredientesSelecionados() {
        Bundle bundle = new Bundle();
        for (int i = 0; i < ingredientesSelecionados.size(); i++) {
            bundle.putSerializable("ingrediente_" + i, ingredientesSelecionados.get(i));
        }
        bundle.putInt("total", ingredientesSelecionados.size());
        return bundle;
    }

    public void limparSelecao() {
        ingredientesSelecionados.clear();
        notifyDataSetChanged();
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