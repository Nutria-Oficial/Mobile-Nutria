package com.bea.nutria.ui.Historico;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.model.Linha;
import com.bea.nutria.model.Tabela;

import java.util.ArrayList;
import java.util.List;

public class TabelasPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAddClickListener {
        void onAddClick();
    }

    private static final int VT_TABELA = 0;
    private static final int VT_ADD = 1;

    private final Context context;
    private final LayoutInflater inflater;
    private final OnAddClickListener onAddClick;
    private List<Tabela> tabelas;

    public TabelasPagerAdapter(@NonNull Context context,
                               @NonNull List<Tabela> tabelas,
                               @NonNull OnAddClickListener onAddClick) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.tabelas = new ArrayList<>(tabelas);
        this.onAddClick = onAddClick;
    }

    /** Atualiza a lista de tabelas e redesenha o pager. */
    public void submit(List<Tabela> novas) {
        if (novas == null) novas = new ArrayList<>();
        this.tabelas = new ArrayList<>(novas);
        notifyDataSetChanged();
    }

    /** Quantas tabelas reais existem (sem contar a página ADD). */
    public int getRealCount() {
        return tabelas != null ? tabelas.size() : 0;
    }

    /** Retorna a tabela da posição, ou null se fora do range. */
    public Tabela getTabelaAt(int pos) {
        return (tabelas != null && pos >= 0 && pos < tabelas.size()) ? tabelas.get(pos) : null;
    }

    @Override
    public int getItemViewType(int position) {
        return position < getRealCount() ? VT_TABELA : VT_ADD;
    }

    @Override
    public int getItemCount() {
        // Sempre 1 a mais por causa da página final de "adicionar"
        return getRealCount() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VT_TABELA) {
            View v = inflater.inflate(R.layout.item_pagina_tabela, parent, false);
            return new TabelaVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_pagina_add, parent, false);
            return new AddVH(v, onAddClick);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TabelaVH && position < getRealCount()) {
            ((TabelaVH) holder).bind(getTabelaAt(position));
        }
        // Página ADD não precisa de bind extra
    }

    /* -------------------- ViewHolders -------------------- */

    static class TabelaVH extends RecyclerView.ViewHolder {
        TabelaVH(@NonNull View itemView) {
            super(itemView);
        }

        void bind(@NonNull Tabela tabela) {
            TextView titulo = itemView.findViewById(R.id.tvTabelaTitulo);
            TextView porcao = itemView.findViewById(R.id.tvPorcaoColuna);
            TableLayout table = itemView.findViewById(R.id.tableNutri);

            if (titulo != null) titulo.setText(tabela.getTitulo());
            if (porcao != null) porcao.setText(tabela.getPorcaoTexto());
            if (table == null) return;

            // Limpa e remonta a tabela
            table.removeAllViews();

            // Cabeçalho
            table.addView(criaHeaderRow());
            table.addView(criaDivisor());

            // Linhas de conteúdo
            List<Linha> linhas = tabela.getLinhas();
            if (linhas != null) {
                for (int i = 0; i < linhas.size(); i++) {
                    table.addView(criaLinha(linhas.get(i)));
                    if (i < linhas.size() - 1) table.addView(criaDivisor());
                }
            }
        }

        private TableRow criaHeaderRow() {
            TableRow tr = new TableRow(itemView.getContext());
            tr.setPadding(dp(12), dp(12), dp(12), dp(12));

            TextView c1 = novoTv("Item", 2f, Gravity.START, true);
            TextView c2 = novoTv("Valor", 1f, Gravity.END, true);
            TextView c3 = novoTv("%VD*", 1f, Gravity.END, true);

            tr.addView(c1);
            tr.addView(c2);
            tr.addView(c3);
            return tr;
        }

        private TableRow criaLinha(@NonNull Linha l) {
            TableRow tr = new TableRow(itemView.getContext());
            tr.setPadding(dp(12), dp(12), dp(12), dp(12));

            TextView c1 = novoTv(l.getNome(), 2f, Gravity.START, false);
            c1.setMaxLines(1);
            c1.setEllipsize(TextUtils.TruncateAt.END);

            TextView c2 = novoTv(l.getValor(), 1f, Gravity.END, false);
            TextView c3 = novoTv(l.getVd(), 1f, Gravity.END, false);

            tr.addView(c1);
            tr.addView(c2);
            tr.addView(c3);
            return tr;
        }

        private View criaDivisor() {
            View v = new View(itemView.getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, dp(1));
            v.setLayoutParams(lp);
            v.setBackgroundColor(Color.parseColor("#E0E0E0"));
            return v;
        }

        private TextView novoTv(String texto, float peso, int gravidade, boolean bold) {
            TextView tv = new TextView(itemView.getContext());
            tv.setText(texto != null ? texto : "");
            tv.setGravity(gravidade);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, peso);
            tv.setLayoutParams(lp);
            return tv;
        }

        private int dp(int v) {
            float d = itemView.getResources().getDisplayMetrics().density;
            return (int) (v * d);
        }
    }

    static class AddVH extends RecyclerView.ViewHolder {
        AddVH(@NonNull View itemView, @NonNull final OnAddClickListener onAddClick) {
            super(itemView);
            View clickable = itemView.findViewById(R.id.imgAddTabela);
            if (clickable == null) clickable = itemView; // fallback
            View finalClickable = clickable;
            finalClickable.setOnClickListener(v -> onAddClick.onAddClick());
            // Se quiser acionar também no clique de qualquer lugar:
            itemView.setOnClickListener(v -> onAddClick.onAddClick());
        }
    }
}
