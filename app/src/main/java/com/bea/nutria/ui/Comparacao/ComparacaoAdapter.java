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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComparacaoAdapter extends RecyclerView.Adapter<ComparacaoAdapter.ProdutoViewHolder> {

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
        this.listaProdutosOriginal = new ArrayList<>(produtos);
        this.listaProdutosExibida = new ArrayList<>(produtos);
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
            String nomeCorrigido = corrigirTextoCodificado(produtoAtual.getNome());
            holder.txtNome.setText(nomeCorrigido);
        } else {
            Log.e(TAG, "txtNome é NULL. O ID R.id.textViewTitulo está incorreto ou a View não existe.");
        }

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

    private String corrigirTextoCodificado(String texto) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean houveCorrecao = false;

            for (int i = 0; i < texto.length();) {
                char c = texto.charAt(i);

                if (c == '\\' && i + 3 < texto.length() && texto.charAt(i + 1) == 'x') {
                    String hex = texto.substring(i + 2, i + 4);
                    try {
                        int valor = Integer.parseInt(hex, 16);
                        out.write(valor);
                        i += 4;
                        houveCorrecao = true;
                    } catch (NumberFormatException e) {
                        out.write((byte) c);
                        i++;
                    }
                } else {
                    out.write((byte) c);
                    i++;
                }
            }

            if (!houveCorrecao) {
                return texto;
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return texto;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        listaProdutosExibida.clear();

        if (text.isEmpty()) {
            listaProdutosExibida.addAll(listaProdutosOriginal);
        } else {
            String filterPattern = text.toLowerCase(Locale.getDefault()).trim();

            for (GetProdutoDTO item : listaProdutosOriginal) {
                if (item.getNome().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                    listaProdutosExibida.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addItem(GetProdutoDTO produto) {
        if (!listaProdutosOriginal.contains(produto)) {
            listaProdutosOriginal.add(produto);
        }

        filter(getLastSearchText());
    }

    public void removeItem(GetProdutoDTO produto) {
        listaProdutosOriginal.remove(produto);

        int position = listaProdutosExibida.indexOf(produto);
        if (position != -1) {
            listaProdutosExibida.remove(position);
            notifyItemRemoved(position);
        }
    }

    private String getLastSearchText() {
        return "";
    }

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