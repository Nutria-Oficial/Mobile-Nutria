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

    private final LayoutInflater inflater;
    private final OnAddClickListener onAddClick;
    private final List<Tabela> tabelas = new ArrayList<>();

    public TabelasPagerAdapter(@NonNull Context ctx,
                               @NonNull List<Tabela> iniciais,
                               @NonNull OnAddClickListener onAddClick) {
        this.inflater = LayoutInflater.from(ctx);
        if (iniciais != null) tabelas.addAll(iniciais);
        this.onAddClick = onAddClick;
    }

    public void submit(@NonNull List<Tabela> novas) {
        tabelas.clear();
        if (novas != null) tabelas.addAll(novas);
        notifyDataSetChanged();
    }

    public int getRealCount() {
        return tabelas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < getRealCount() ? VT_TABELA : VT_ADD;
    }

    @Override
    public int getItemCount() {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        if (h instanceof TabelaVH && position < getRealCount()) {
            ((TabelaVH) h).bind(tabelas.get(position));
        }
    }

    static class TabelaVH extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvPorcao;
        final TableLayout table;

        TabelaVH(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTabelaTitulo);
            tvPorcao = itemView.findViewById(R.id.tvPorcaoColuna);
            table = itemView.findViewById(R.id.tableNutri);
        }

        void bind(@NonNull Tabela t) {
            if (tvTitulo != null) tvTitulo.setText(t.getTitulo());
            if (tvPorcao != null) tvPorcao.setText(t.getPorcaoTexto());
            if (table == null) return;

            table.removeAllViews();
            table.addView(headerRow());
            table.addView(divider());

            List<Linha> linhas = t.getLinhas();
            if (linhas != null) {
                for (int i = 0; i < linhas.size(); i++) {
                    table.addView(dataRow(linhas.get(i)));
                    if (i < linhas.size() - 1) table.addView(divider());
                }
            }
        }

        private TableRow headerRow() {
            TableRow tr = new TableRow(itemView.getContext());
            tr.setPadding(dp(12), dp(12), dp(12), dp(12));
            tr.addView(tv("Item", 2f, Gravity.START, true));
            tr.addView(tv("Valor", 1f, Gravity.END, true));
            tr.addView(tv("%VD*", 1f, Gravity.END, true));
            return tr;
        }

        private TableRow dataRow(@NonNull Linha l) {
            TableRow tr = new TableRow(itemView.getContext());
            tr.setPadding(dp(12), dp(12), dp(12), dp(12));

            TextView c1 = tv(ns(l.getNome()), 2f, Gravity.START, false);
            c1.setMaxLines(1);
            c1.setEllipsize(TextUtils.TruncateAt.END);

            tr.addView(c1);
            tr.addView(tv(ns(l.getValor()), 1f, Gravity.END, false));
            tr.addView(tv(ns(l.getVd()), 1f, Gravity.END, false));
            return tr;
        }

        private View divider() {
            View v = new View(itemView.getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, dp(1));
            v.setLayoutParams(lp);
            v.setBackgroundColor(Color.parseColor("#E0E0E0"));
            return v;
        }

        private TextView tv(String texto, float weight, int gravity, boolean bold) {
            TextView t = new TextView(itemView.getContext());
            t.setText(texto == null ? "" : texto);
            t.setGravity(gravity);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, weight);
            t.setLayoutParams(lp);
            return t;
        }

        private int dp(int v) {
            float d = itemView.getResources().getDisplayMetrics().density;
            return (int) (v * d);
        }

        private String ns(String s) {
            return s == null ? "" : s;
        }
    }

    static class AddVH extends RecyclerView.ViewHolder {
        AddVH(@NonNull View itemView, @NonNull OnAddClickListener onAddClick) {
            super(itemView);
            View add = itemView.findViewById(R.id.imgAddTabela);
            if (add == null) add = itemView;
            add.setOnClickListener(v -> onAddClick.onAddClick());
            itemView.setOnClickListener(v -> onAddClick.onAddClick());
        }
    }
}
