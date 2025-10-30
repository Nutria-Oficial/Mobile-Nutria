package com.bea.nutria.ui.Ingrediente;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IngredientesRegistradosFragment extends Fragment {

    private List<IngredienteResponse> listaIngredientes;
    private IngredienteAdapter adapter;
    private EditText editPesquisar;
    private ImageView iconeBuscar;
    private ConexaoAPI apiManager;
    private IngredienteAPI ingredienteApi;
    private RecyclerView recyclerViewIngredientes;
    private IngredienteSharedViewModel sharedViewModel;

    private static final String TAG = "IngredientesFragment";
    private static final String url = "https://api-spring-mongodb.onrender.com";

    public IngredientesRegistradosFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredientes_registrados, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(IngredienteSharedViewModel.class);

        listaIngredientes = new ArrayList<>();

        apiManager = new ConexaoAPI(url);
        ingredienteApi = apiManager.getApi(IngredienteAPI.class);

        editPesquisar = view.findViewById(R.id.editPesquisar);
        iconeBuscar = view.findViewById(R.id.iconeBuscar);
        recyclerViewIngredientes = view.findViewById(R.id.recyclerViewIngredientes);

        adapter = new IngredienteAdapter(getContext(), new ArrayList<>());
        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredientes.setAdapter(adapter);

        sharedViewModel.getIngredientesSelecionados().observe(getViewLifecycleOwner(), selecionados -> {
            if (selecionados != null) {
                adapter.restaurarSelecao(selecionados);
            }
        });

        // observar novos ingredientes criados
        sharedViewModel.getNovoIngredienteAdicionado().observe(getViewLifecycleOwner(), novoIngrediente -> {
            if (novoIngrediente != null) {
                // adicionar no início da lista principal
                listaIngredientes.add(0, novoIngrediente);

                // atualizar o adapter com o novo ingrediente no topo
                atualizarListaExibida();

                // scroll para o topo para mostrar o novo ingrediente
                recyclerViewIngredientes.scrollToPosition(0);
            }
        });

        adapter.setOnIngredienteChangeListener(new IngredienteAdapter.OnIngredienteChangeListener() {
            @Override
            public void onIngredienteAdicionado(IngredienteResponse ingrediente) {
                sharedViewModel.setIngredientesSelecionados(adapter.getListaSelecionados());

                // mover ingrediente para o topo da lista principal
                moverIngredienteParaTopo(ingrediente);

                // atualizar a exibição
                atualizarListaExibida();

                // scroll para o topo
                recyclerViewIngredientes.scrollToPosition(0);
            }

            @Override
            public void onIngredienteRemovido(IngredienteResponse ingrediente) {
                sharedViewModel.setIngredientesSelecionados(adapter.getListaSelecionados());

                // atualizar a exibição para reordenar
                atualizarListaExibida();
            }
        });

        apiManager.iniciarServidor(requireActivity(), () -> getAllIngredientes());

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

        iconeBuscar.setOnClickListener(v -> {
            String texto = editPesquisar.getText().toString();
            filtrarIngredientesExata(texto);
        });

        return view;
    }

    private void getAllIngredientes() {
        ingredienteApi.getAllIngredientes().enqueue(new Callback<List<IngredienteResponse>>() {
            @Override
            public void onResponse(Call<List<IngredienteResponse>> call, Response<List<IngredienteResponse>> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<IngredienteResponse> listaCompleta = response.body();

                    listaIngredientes.clear();
                    listaIngredientes.addAll(listaCompleta);

                    atualizarListaExibida();
                } else {
                    Log.e(TAG, "Erro na resposta: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<IngredienteResponse>> call, Throwable throwable) {
                if (getActivity() != null) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void moverIngredienteParaTopo(IngredienteResponse ingrediente) {
        // remover o ingrediente da posição atual
        for (int i = 0; i < listaIngredientes.size(); i++) {
            if (listaIngredientes.get(i).getId().equals(ingrediente.getId())) {
                listaIngredientes.remove(i);
                break;
            }
        }

        // adicionar no topo
        listaIngredientes.add(0, ingrediente);
    }

    private void atualizarListaExibida() {
        String textoBusca = editPesquisar.getText().toString();

        if (textoBusca.isEmpty()) {
            // sem filtro, mostrar os primeiros 50
            List<IngredienteResponse> listaInicial = new ArrayList<>();
            int limite = Math.min(listaIngredientes.size(), 50);
            for (int i = 0; i < limite; i++) {
                listaInicial.add(listaIngredientes.get(i));
            }
            adapter.atualizarLista(listaInicial);
        } else {
            // reaplica o filtro atual
            filtrarIngredientesParcial(textoBusca);
        }
    }

    private void filtrarIngredientesParcial(String texto) {
        List<IngredienteResponse> listaTemporaria = new ArrayList<>();

        if (texto.isEmpty()) {
            int limite = Math.min(listaIngredientes.size(), 50);
            for (int i = 0; i < limite; i++) {
                listaTemporaria.add(listaIngredientes.get(i));
            }
        } else {
            String textoBusca = texto.toLowerCase().trim();
            int contador = 0;

            for (IngredienteResponse ingrediente : listaIngredientes) {
                if (ingrediente.getNomeIngrediente().toLowerCase().startsWith(textoBusca)) {
                    listaTemporaria.add(ingrediente);
                    contador++;

                    if (contador >= 50) {
                        break;
                    }
                }
            }
        }

        adapter.atualizarLista(listaTemporaria);
    }

    private void filtrarIngredientesExata(String texto) {
        List<IngredienteResponse> listaTemporaria = new ArrayList<>();

        if (!texto.isEmpty()) {
            String textoBusca = texto.trim();

            for (IngredienteResponse ingrediente : listaIngredientes) {
                if (ingrediente.getNomeIngrediente().equalsIgnoreCase(textoBusca)) {
                    listaTemporaria.add(ingrediente);
                    break;
                }
            }
        }

        adapter.atualizarLista(listaTemporaria);
    }

    @Override
    public void onResume() {
        super.onResume();
        // reordenar a lista quando voltar para a tela
        if (adapter != null && listaIngredientes != null && !listaIngredientes.isEmpty()) {
            atualizarListaExibida();
        }
    }
}