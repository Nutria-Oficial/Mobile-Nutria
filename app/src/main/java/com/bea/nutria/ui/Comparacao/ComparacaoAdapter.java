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

    // Lista de dados atualmente exibida (filtrada ou completa)
    private final List<GetProdutoDTO> listaProdutosExibida;
    // Cópia da lista de dados original (COMPLETA), que serve como fonte de dados para o filtro
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
        this.listaProdutosOriginal = new ArrayList<>(produtos); // Fonte de verdade
        this.listaProdutosExibida = new ArrayList<>(produtos); // Lista visível
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
     * MÉTODO DE FILTRO: Aplica a lógica de pesquisa.
     *
     * @param text O texto digitado na barra de pesquisa.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        listaProdutosExibida.clear(); // Limpa a lista atual

        if (text.isEmpty()) {
            // Se o texto estiver vazio, mostra a lista completa original
            listaProdutosExibida.addAll(listaProdutosOriginal);
        } else {
            // Converte o texto para minúsculas para pesquisa case-insensitive
            String filterPattern = text.toLowerCase(Locale.getDefault()).trim();

            for (GetProdutoDTO item : listaProdutosOriginal) {
                // Filtra pelo nome do produto
                if (item.getNome().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                    listaProdutosExibida.add(item);
                }
            }
        }
        // Notifica o RecyclerView para redesenhar com a nova lista filtrada
        notifyDataSetChanged();
    }

    /**
     * Método para adicionar um item de volta à lista.
     * O produto é adicionado à lista Original e o filtro é re-aplicado.
     *
     * @param produto O produto a ser adicionado.
     */
    public void addItem(GetProdutoDTO produto) {
        // 1. Adiciona à lista ORIGINAL (fonte de verdade) se ainda não estiver lá
        if (!listaProdutosOriginal.contains(produto)) {
            listaProdutosOriginal.add(produto);
        }

        // 2. Re-aplica o filtro para atualizar a lista de exibição
        // Isso garante que o item adicionado apareça (ou não) dependendo do texto na SearchBar.
        filter(getLastSearchText());
    }

    /**
     * Método para remover um item.
     * O produto é removido da lista Original e o filtro é re-aplicado/atualizado.
     *
     * @param produto O produto a ser removido.
     */
    public void removeItem(GetProdutoDTO produto) {
        // 1. Remove da lista ORIGINAL (fonte de verdade)
        listaProdutosOriginal.remove(produto);

        // 2. Remove da lista de exibição (se o filtro já não o fez) e notifica a remoção
        int position = listaProdutosExibida.indexOf(produto);
        if (position != -1) {
            listaProdutosExibida.remove(position);
            notifyItemRemoved(position);
        }

        // Observação: Não precisamos chamar 'filter' aqui, pois a remoção já foi notificada
        // e o Fragmento lida com o estado. Se o fragmento não lidasse com o estado,
        // precisaríamos chamar 'filter(getLastSearchText());'
    }

    /**
     * Método auxiliar para simular o texto de pesquisa mais recente.
     * (Idealmente, o Adapter não deveria depender de uma variável externa para isso,
     * mas é um workaround prático se o Fragment não puder fornecer o texto de pesquisa no addItem/removeItem).
     * Nota: Mantenho a chamada ao filter no addItem, removendo a necessidade de ler o SearchBar aqui.
     */
    private String getLastSearchText() {
        // Como o Fragment chama 'filter' no TextWatcher, podemos confiar que a lista está
        // no estado correto, mas se houver uma implementação mais complexa de 'addItem'
        // que precise do último texto de busca, ela deveria ser passada como parâmetro.
        // Por enquanto, retorno vazio para mostrar a lista completa se não houver filtro ativo.
        return "";
    }

    /**
     * Permite que o Fragment acesse a lista de produtos atualizada (a exibida).
     */
    public List<GetProdutoDTO> getListaProdutosExibida() {
        return listaProdutosExibida;
    }


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