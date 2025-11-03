package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.model.GetTabelaComparacaoDTO;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaViewHolder> {

    private final List<GetTabelaComparacaoDTO> listaTabelas;
    private OnTabelaClickListener listener;
    private static final String TAG = "TabelaAdapter";

    public interface OnTabelaClickListener {
        void onEscolherTabelaClick(GetTabelaComparacaoDTO tabela, int position);
    }

    public void setOnTabelaClickListener(OnTabelaClickListener listener) {
        this.listener = listener;
    }

    public TabelaAdapter(List<GetTabelaComparacaoDTO> tabelas) {
        this.listaTabelas = tabelas;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < listaTabelas.size()) {
            return listaTabelas.get(position).getTabelaId();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public TabelaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_card_escolha_tabela,
                    parent,
                    false
            );
            return new TabelaViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inflar o layout: " + e.getMessage());
            throw new RuntimeException("Erro ao criar ViewHolder", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TabelaViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GetTabelaComparacaoDTO tabelaAtual = listaTabelas.get(position);

        holder.bind(corrigirTabelaTextos(tabelaAtual));

        if (holder.headerItem != null) {
            holder.headerItem.setOnClickListener(v -> {
                tabelaAtual.setExpanded(!tabelaAtual.isExpanded());
                notifyItemChanged(position);
            });
        }

        if (holder.btnSelecionarTabela != null) {
            holder.btnSelecionarTabela.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEscolherTabelaClick(tabelaAtual, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaTabelas.size();
    }

    private String corrigirTextoCodificado(String texto) {
        if (texto == null) return null;

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

    private GetTabelaComparacaoDTO corrigirTabelaTextos(GetTabelaComparacaoDTO tabela) {
        if (tabela == null) return null;

        if (tabela.getNomeTabela() != null) {
            tabela.setNomeTabela(corrigirTextoCodificado(tabela.getNomeTabela()));
        }

        return tabela;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < listaTabelas.size()) {
            listaTabelas.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addItem(GetTabelaComparacaoDTO tabela) {
        listaTabelas.add(tabela);
        notifyItemInserted(listaTabelas.size() - 1);
    }
}