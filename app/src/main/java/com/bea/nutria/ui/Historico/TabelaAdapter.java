package com.bea.nutria.ui.Historico;

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

import java.util.List;

public class TabelaAdapter extends RecyclerView.Adapter<TabelaAdapter.TabelaVH> {

    private final List<Tabela> tabelas;

    public TabelaAdapter(@NonNull List<Tabela> tabelas) {
        this.tabelas = tabelas;
    }

    @NonNull
    @Override
    public TabelaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pagina_tabela, parent, false);
        return new TabelaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TabelaVH holder, int position) {
        Tabela t = tabelas.get(position);
        holder.bind(t);
    }

    @Override
    public int getItemCount() {
        return tabelas == null ? 0 : tabelas.size();
    }

    static class TabelaVH extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final TextView tvPorcao;
        private final TableLayout table;

        TabelaVH(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTabelaTitulo);
            tvPorcao = itemView.findViewById(R.id.tvPorcaoColuna);
            table    = itemView.findViewById(R.id.tableNutri);
        }

        void bind(@NonNull Tabela tabela) {
            if (tvTitulo != null) tvTitulo.setText(tabela.getTitulo());
            if (tvPorcao != null) tvPorcao.setText(tabela.getPorcaoTexto());

            if (table == null) return;

            table.removeAllViews();

            table.addView(headerRow());
            table.addView(divider());

            List<Linha> linhas = tabela.getLinhas();
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

            TextView c1 = tv(nullSafe(l.getNome()), 2f, Gravity.START, false);
            c1.setMaxLines(1);
            c1.setEllipsize(TextUtils.TruncateAt.END);

            tr.addView(c1);
            tr.addView(tv(nullSafe(l.getValor()), 1f, Gravity.END, false));
            tr.addView(tv(nullSafe(l.getVd()),    1f, Gravity.END, false));
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

        private TextView tv(String txt, float weight, int gravity, boolean bold) {
            TextView t = new TextView(itemView.getContext());
            t.setText(txt == null ? "" : txt);
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

        private String nullSafe(String s) {
            return s == null ? "" : s;
        }
    }
}
