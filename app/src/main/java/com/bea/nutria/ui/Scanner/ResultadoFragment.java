package com.bea.nutria.ui.Scanner;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bea.nutria.R;
import com.bea.nutria.api.ScannerAPI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultadoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resultado, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageView voltar = v.findViewById(R.id.btVoltar);
        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        TextView tvPorcao = v.findViewById(R.id.tvPorcao);
        TextView tvPorcaoColuna = v.findViewById(R.id.tvPorcaoColuna);
        TableLayout table = v.findViewById(R.id.tableNutri);

        Bundle args = getArguments();
        String nome = args != null ? args.getString("nomeIngrediente", "Ingrediente") : "Ingrediente";
        String porcao = args != null ? args.getString("porcao", "") : "";
        Serializable ser = args != null ? args.getSerializable("nutrientes") : null;

        List<ScannerAPI.NutrienteDTO> nutrientes = new ArrayList<>();
        if (ser instanceof List) {
            try {
                nutrientes = (List<ScannerAPI.NutrienteDTO>) ser;
            } catch (Exception ignored) {
            }
        }

        // Log para debugggg
        Log.d("ResultadoFragment", "Nome: " + nome);
        Log.d("ResultadoFragment", "Porção: " + porcao);
        Log.d("ResultadoFragment", "Nutrientes: " + (nutrientes != null ? nutrientes.size() : "null"));

        tvTitulo.setText(TextUtils.isEmpty(nome) ? "Ingrediente" : nome);
        tvPorcao.setText("Porção aproximada do ingrediente");
        tvPorcaoColuna.setText(TextUtils.isEmpty(porcao) ? "" : porcao);

        // Limpa a tabela completamente
        table.removeAllViews();

        // Adiciona cabeçalho
        addHeader(table);

        if (nutrientes != null && !nutrientes.isEmpty()) {
            for (ScannerAPI.NutrienteDTO n : nutrientes) {
                addRow(table, safe(n.nome), safe(n.valor), safe(n.vd));
            }
        } else {
            Log.e("ResultadoFragment", "Lista de nutrientes vazia!");
            addRow(table, "Nenhum nutriente encontrado", "-", "-");
        }

        voltar.setOnClickListener(v1 -> requireActivity().onBackPressed());
    }

    private void addHeader(TableLayout table) {
        TableRow header = new TableRow(requireContext());
        header.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView c1 = cell("Item", true, 2);
        TextView c2 = cell("Valor", true, 1);
        TextView c3 = cell("%VD*", true, 1);
        c2.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        c3.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        header.addView(c1);
        header.addView(c2);
        header.addView(c3);
        table.addView(header);

        View divider = new View(requireContext());
        divider.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        ));
        divider.setBackgroundColor(0xFFE0E0E0);
        table.addView(divider);
    }

    private void addRow(TableLayout table, String item, String valor, String vd) {
        TableRow row = new TableRow(requireContext());
        row.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView c1 = cell(item, false, 2);
        TextView c2 = cell(valor, false, 1);
        TextView c3 = cell(vd, false, 1);

        c1.setMaxLines(1);
        c1.setEllipsize(android.text.TextUtils.TruncateAt.END);

        c2.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        c3.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        row.addView(c1);
        row.addView(c2);
        row.addView(c3);
        table.addView(row);

        View divider = new View(requireContext());
        divider.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        ));
        divider.setBackgroundColor(0xFFE0E0E0);
        table.addView(divider);
    }

    private TextView cell(String text, boolean bold, int weight) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(lp);
        tv.setTextSize(14);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}