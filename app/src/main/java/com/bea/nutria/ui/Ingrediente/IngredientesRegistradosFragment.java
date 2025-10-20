package com.bea.nutria.ui.Ingrediente;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bea.nutria.R;

import java.util.ArrayList;

public class IngredientesRegistradosFragment extends Fragment {

    private ArrayList<String> listaIngredientes;
    private ArrayList<String> listaFiltrada;
    private IngredienteAdapter adapter;
    private EditText editPesquisar;
    private ImageView iconeBuscar;

    public IngredientesRegistradosFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredientes_registrados, container, false);

        editPesquisar = view.findViewById(R.id.editPesquisar);
        iconeBuscar = view.findViewById(R.id.iconeBuscar);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewIngredientes);

        // lista de ingredientes - teste
        listaIngredientes = new ArrayList<>();
        listaIngredientes.add("Arroz");
        listaIngredientes.add("Feijão");
        listaIngredientes.add("Macarrão");
        listaIngredientes.add("Farinha");
        listaIngredientes.add("Açúcar");
        listaIngredientes.add("Sal");
        listaIngredientes.add("Óleo");
        listaIngredientes.add("Leite");
        listaIngredientes.add("Ovos");
        listaIngredientes.add("Queijo");
        listaIngredientes.add("Frango");
        listaIngredientes.add("Carne moída");
        listaIngredientes.add("Batata");
        listaIngredientes.add("Cenoura");
        listaIngredientes.add("Tomate");
        listaIngredientes.add("Cebola");
        listaIngredientes.add("Alho");
        listaIngredientes.add("Pimentão");

        listaFiltrada = new ArrayList<>(listaIngredientes);

        adapter = new IngredienteAdapter(getContext(), listaFiltrada);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // pesquisa enquanto digita (parcial)
        editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filtrarIngredientesParcial(s.toString());
            }
        });

        // pesquisa exata quando clica na lupa
        iconeBuscar.setOnClickListener(v -> {
            String texto = editPesquisar.getText().toString();
            filtrarIngredientesExata(texto);
        });

        return view;
    }

    //retorna todos que começam com o texto digitado
    private void filtrarIngredientesParcial(String texto) {
        listaFiltrada.clear();

        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaIngredientes);
        } else {
            String textoBusca = texto.toLowerCase();
            for (String ingrediente : listaIngredientes) {
                if (ingrediente.toLowerCase().startsWith(textoBusca)) {
                    listaFiltrada.add(ingrediente);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    // retorna apenas ingredientes com o nome idêntico ao da pesquisa
    private void filtrarIngredientesExata(String texto) {
        listaFiltrada.clear();

        for (String ingrediente : listaIngredientes) {
            if (ingrediente.equalsIgnoreCase(texto)) {
                listaFiltrada.add(ingrediente);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
