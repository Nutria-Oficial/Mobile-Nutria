package com.bea.nutria.ui.Historico;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.databinding.FragmentHistoricoBinding;

import java.util.ArrayList;
import java.util.List;

public class HistoricoFragment extends Fragment {

    private FragmentHistoricoBinding binding;
    private HistoricoAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoricoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<String> produtos = new ArrayList<>();
         produtos.add("Arroz Integral");
         produtos.add("Feijão Preto");
         produtos.add("Presunto levissimo");

        adapter = new HistoricoAdapter(produtos); // adapter não-nulo
        binding.rvHistorico.setAdapter(adapter);

        // 3) Só depois de setar o adapter, conecta o TextWatcher
        binding.editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filtro(s == null ? "" : s.toString());
                    toggleEmpty();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        toggleEmpty();
    }

    private void toggleEmpty() {
        boolean isEmpty = adapter == null || adapter.getItemCount() == 0;
        binding.rvHistorico.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
