package com.bea.nutria;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.bea.nutria.databinding.FragmentAvaliacaoTabelaBinding;
import com.bea.nutria.databinding.FragmentVisualizarBinding;
import com.bea.nutria.model.GetNutrienteDTO;
import com.bea.nutria.model.GetTabelaEAvaliacaoDTO;

import java.util.ArrayList;
import java.util.Locale;


public class VisualizarFragment extends Fragment {
    private FragmentVisualizarBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentVisualizarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            ArrayList<String[]> tabelaDados = (ArrayList<String[]>) getArguments().getSerializable("tabela");
            if (tabelaDados != null){
                preencherDadosTabela(tabelaDados);
            }
        }
        NavController navController = NavHostFragment.findNavController(VisualizarFragment.this);
        navController.navigate(R.id.action_navigation_visualizar_to_navigation_avaliacao_tabela);
    }

    private void preencherDadosTabela(ArrayList<String[]> tabelaDados) {
        binding.tableLayout.removeAllViews();

        TableRow nomeTabela = new TableRow(getContext());
        TextView nome = new TextView(getContext());
        nomeTabela.addView(nome);
        binding.tableLayout.addView(nomeTabela);

        for (int i = 0; i < tabelaDados.size(); i++) {
            TableRow linha = new TableRow(getContext());

            for (int j = 0; j < tabelaDados.get(i).length; j++) {
                TextView valor = new TextView(getContext());

                valor.setText(tabelaDados.get(i)[j]);
                linha.addView(valor);
            }
            binding.tableLayout.addView(linha);
        }

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}