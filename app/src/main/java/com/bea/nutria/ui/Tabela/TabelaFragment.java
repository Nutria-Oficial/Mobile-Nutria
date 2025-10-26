package com.bea.nutria.ui.Tabela;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentTabelaBinding;
import com.bea.nutria.ui.Ingrediente.IngredienteSharedViewModel;

import java.util.ArrayList;

public class TabelaFragment extends Fragment {

    private FragmentTabelaBinding binding;
    private TabelaAdapter adapter;
    private IngredienteSharedViewModel sharedViewModel;
    private static final String TAG = "TabelaFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTabelaBinding.inflate(inflater, container, false);

        // Inicializar ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(IngredienteSharedViewModel.class);

        setupRecyclerView();
        observarIngredientes();
        setupListeners();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new TabelaAdapter(getContext(), new ArrayList<>());

        // atualizar ViewModel quando remover
        adapter.setOnItemRemovedListener((ingrediente, newCount) -> {
            binding.selecionados.setText(String.valueOf(newCount));
            sharedViewModel.removerIngrediente(ingrediente); // atualizar ViewModel
        });

        binding.ingredientesSelecionados.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ingredientesSelecionados.setAdapter(adapter);
    }

    private void observarIngredientes() {
        sharedViewModel.getIngredientesSelecionados().observe(getViewLifecycleOwner(), selecionados -> {
            if (selecionados != null) {
                Log.d(TAG, "Ingredientes recebidos do ViewModel: " + selecionados.size());
                binding.selecionados.setText(String.valueOf(selecionados.size()));

                adapter = new TabelaAdapter(getContext(), new ArrayList<>(selecionados));
                adapter.setOnItemRemovedListener((ingrediente, newCount) -> {
                    binding.selecionados.setText(String.valueOf(newCount));
                    sharedViewModel.removerIngrediente(ingrediente);
                });
                binding.ingredientesSelecionados.setAdapter(adapter);
            } else {
                binding.selecionados.setText("0");
            }
        });
    }

    private void setupListeners() {
        binding.btIngredientes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_tabela_to_ingrediente)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}