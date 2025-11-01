package com.bea.nutria.ui.Ingrediente;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IngredientesRegistradosFragment extends Fragment {

    private static final String TAG = "IngredientesFragment";
    private static final String url = "https://api-spring-mongodb.onrender.com";

    private List<IngredienteResponse> listaIngredientes;
    private IngredienteAdapter adapter;
    private EditText editPesquisar;
    private ImageView iconeBuscar;
    private ConexaoAPI apiManager;
    private IngredienteAPI ingredienteApi;
    private RecyclerView recyclerViewIngredientes;
    private IngredienteSharedViewModel sharedViewModel;

    // componentes de paginação
    private ImageView btnPaginaAnterior;
    private ImageView btnProximaPagina;
    private TextView tvNumeroPagina;
    private ProgressBar progressBar;

    private int paginaAtual = 0; // API começa em 1
    private boolean isCarregando = false;
    private boolean estaBuscandoPorNome = false;
    private boolean primeiraCarregamento = true;

    public IngredientesRegistradosFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredientes_registrados, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(IngredienteSharedViewModel.class);
        listaIngredientes = new ArrayList<>();

        apiManager = new ConexaoAPI(url);
        ingredienteApi = apiManager.getApi(IngredienteAPI.class);

        // inicializar componentes
        editPesquisar = view.findViewById(R.id.editPesquisar);
        iconeBuscar = view.findViewById(R.id.iconeBuscar);
        recyclerViewIngredientes = view.findViewById(R.id.recyclerViewIngredientes);
        btnPaginaAnterior = view.findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = view.findViewById(R.id.btnProximaPagina);
        tvNumeroPagina = view.findViewById(R.id.tvNumeroPagina);
        progressBar = view.findViewById(R.id.progressBarIngredientes);

        adapter = new IngredienteAdapter(getContext(), new ArrayList<>());
        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredientes.setAdapter(adapter);

        // atualizar número da página
        atualizarNumeroPagina();

        // observar ingredientes selecionados
        sharedViewModel.getIngredientesSelecionados().observe(getViewLifecycleOwner(), selecionados -> {
            if (selecionados != null) {
                adapter.restaurarSelecao(selecionados);
                // Se estiver na página 1, recarregar para mostrar os selecionados no topo
                if (paginaAtual == 0 && !estaBuscandoPorNome) {
                    carregarPagina(paginaAtual);
                }
            }
        });

        // observar novos ingredientes adicionados (registrados)
        sharedViewModel.getNovoIngredienteAdicionado().observe(getViewLifecycleOwner(), novoIngrediente -> {
            if (novoIngrediente != null) {
                // Adiciona aos selecionados
                List<IngredienteResponse> selecionados = sharedViewModel.getIngredientesSelecionados().getValue();
                if (selecionados == null) {
                    selecionados = new ArrayList<>();
                }

                // Verifica se já não está na lista
                boolean jaExiste = false;
                for (IngredienteResponse ing : selecionados) {
                    if (ing.getId().equals(novoIngrediente.getId())) {
                        jaExiste = true;
                        break;
                    }
                }

                if (!jaExiste) {
                    selecionados.add(0, novoIngrediente);
                    sharedViewModel.setIngredientesSelecionados(selecionados);
                }

                // Se estiver na página 1, adiciona no topo da lista visível
                if (paginaAtual == 0) {
                    listaIngredientes.add(0, novoIngrediente);
                    adapter.atualizarLista(listaIngredientes);
                    recyclerViewIngredientes.scrollToPosition(0);
                }
            }
        });

        // listener para mudanças no adapter
        adapter.setOnIngredienteChangeListener(new IngredienteAdapter.OnIngredienteChangeListener() {
            @Override
            public void onIngredienteAdicionado(IngredienteResponse ingrediente) {
                sharedViewModel.setIngredientesSelecionados(adapter.getListaSelecionados());

                // Se estiver na página 1, move para o topo
                if (paginaAtual == 0) {
                    moverIngredienteParaTopo(ingrediente);
                    adapter.atualizarLista(listaIngredientes);
                    recyclerViewIngredientes.scrollToPosition(0);
                } else {
                    // Se estiver em outra página, remove da lista atual
                    removerIngredienteDaLista(ingrediente);
                    adapter.atualizarLista(listaIngredientes);
                }
            }

            @Override
            public void onIngredienteRemovido(IngredienteResponse ingrediente) {
                sharedViewModel.setIngredientesSelecionados(adapter.getListaSelecionados());

                // Se estiver na página 1, pode precisar remover do topo
                if (paginaAtual == 0) {
                    adapter.atualizarLista(listaIngredientes);
                } else {
                    // Se estava em outra página e foi removido, adiciona de volta
                    listaIngredientes.add(ingrediente);
                    adapter.atualizarLista(listaIngredientes);
                }
            }
        });

        // botões de paginação
        btnPaginaAnterior.setOnClickListener(v -> {
            if (paginaAtual >= 1 && !isCarregando && !estaBuscandoPorNome) {
                paginaAtual--;
                atualizarNumeroPagina();
                carregarPagina(paginaAtual);
            }
        });

        btnProximaPagina.setOnClickListener(v -> {
            if (!isCarregando && !estaBuscandoPorNome) {
                paginaAtual++;
                atualizarNumeroPagina();
                carregarPagina(paginaAtual);
            }
        });

        // iniciar servidor e carregar primeira página
        apiManager.iniciarServidor(requireActivity(), () -> carregarPagina(paginaAtual));

        // busca conforme digita
        editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim();
                if (texto.isEmpty()) {
                    estaBuscandoPorNome = false;
                    carregarPagina(paginaAtual);
                }
            }
        });

        // busca exata ao clicar no ícone
        iconeBuscar.setOnClickListener(v -> {
            String texto = editPesquisar.getText().toString().trim();
            if (!texto.isEmpty()) {
                buscarPorNomeExato(texto);
            }
        });

        return view;
    }

    private void carregarPagina(int pagina) {
        if (isCarregando) return;

        isCarregando = true;
        estaBuscandoPorNome = false;

        // mostrar loading e limpar lista
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewIngredientes.setVisibility(View.GONE);
        listaIngredientes.clear();
        adapter.atualizarLista(listaIngredientes);

        ingredienteApi.getAllIngredientes(pagina + 1).enqueue(new Callback<PaginatedResponse>() {
            @Override
            public void onResponse(Call<PaginatedResponse> call, Response<PaginatedResponse> response) {
                isCarregando = false;
                progressBar.setVisibility(View.GONE);
                recyclerViewIngredientes.setVisibility(View.VISIBLE);

                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse paginatedResponse = response.body();
                    List<IngredienteResponse> listaDoBanco = paginatedResponse.getContent();

                    if ((listaDoBanco == null || listaDoBanco.isEmpty()) && pagina > 0) {
                        // se não tem dados, volta para página anterior
                        paginaAtual--;
                        atualizarNumeroPagina();
                        return;
                    }

                    // desabilitar botão próxima se estiver na última página
                    btnProximaPagina.setAlpha(paginatedResponse.isLast() ? 0.3f : 1.0f);
                    btnProximaPagina.setEnabled(!paginatedResponse.isLast());

                    listaIngredientes.clear();

                    // Se for a primeira página, adicionar ingredientes selecionados no topo
                    if (pagina == 0) {
                        List<IngredienteResponse> selecionados = sharedViewModel.getIngredientesSelecionados().getValue();

                        if (selecionados != null && !selecionados.isEmpty()) {
                            // Adiciona os selecionados primeiro
                            listaIngredientes.addAll(selecionados);
                        }

                        // Adiciona os ingredientes do banco, verificando duplicatas
                        if (listaDoBanco != null) {
                            for (IngredienteResponse ingBanco : listaDoBanco) {
                                boolean jaAdicionado = false;

                                // Verifica se já está nos selecionados
                                if (selecionados != null) {
                                    for (IngredienteResponse ingSel : selecionados) {
                                        if (ingBanco.getId().equals(ingSel.getId())) {
                                            jaAdicionado = true;
                                            break;
                                        }
                                    }
                                }

                                // Se não está duplicado, adiciona
                                if (!jaAdicionado) {
                                    listaIngredientes.add(ingBanco);
                                }
                            }
                        }
                    } else {
                        // Em outras páginas, adiciona a lista do banco mas remove os selecionados
                        List<IngredienteResponse> selecionados = sharedViewModel.getIngredientesSelecionados().getValue();

                        if (listaDoBanco != null) {
                            for (IngredienteResponse ingBanco : listaDoBanco) {
                                boolean estaSelecionado = false;

                                // Verifica se está nos selecionados
                                if (selecionados != null) {
                                    for (IngredienteResponse ingSel : selecionados) {
                                        if (ingBanco.getId().equals(ingSel.getId())) {
                                            estaSelecionado = true;
                                            break;
                                        }
                                    }
                                }

                                // Só adiciona se NÃO estiver selecionado
                                if (!estaSelecionado) {
                                    listaIngredientes.add(ingBanco);
                                }
                            }
                        }
                    }

                    adapter.atualizarLista(listaIngredientes);
                    recyclerViewIngredientes.scrollToPosition(0);

                    Log.d(TAG, "Página " + (paginaAtual + 1) + " de " + paginatedResponse.getTotalPages() +
                            " com " + listaIngredientes.size() + " ingredientes");
                } else {
                    Log.e(TAG, "Erro na resposta: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse> call, Throwable throwable) {
                isCarregando = false;
                progressBar.setVisibility(View.GONE);
                recyclerViewIngredientes.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    Log.e(TAG, "Falha ao carregar página", throwable);
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void buscarPorNomeExato(String nome) {
        if (isCarregando) return;

        isCarregando = true;
        estaBuscandoPorNome = true;

        // mostrar loading
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewIngredientes.setVisibility(View.GONE);

        ingredienteApi.getIngredientesPorNome(nome).enqueue(new Callback<List<IngredienteResponse>>() {
            @Override
            public void onResponse(Call<List<IngredienteResponse>> call, Response<List<IngredienteResponse>> response) {
                isCarregando = false;
                progressBar.setVisibility(View.GONE);
                recyclerViewIngredientes.setVisibility(View.VISIBLE);

                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<IngredienteResponse> resultados = response.body();
                    listaIngredientes.clear();
                    listaIngredientes.addAll(resultados);
                    adapter.atualizarLista(listaIngredientes);

                    if (resultados.isEmpty()) {
                        Log.d(TAG, "Nenhum ingrediente encontrado com o nome: " + nome);
                    }
                } else {
                    Log.e(TAG, "Erro na busca: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<IngredienteResponse>> call, Throwable throwable) {
                isCarregando = false;
                progressBar.setVisibility(View.GONE);
                recyclerViewIngredientes.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void moverIngredienteParaTopo(IngredienteResponse ingrediente) {
        for (int i = 0; i < listaIngredientes.size(); i++) {
            if (listaIngredientes.get(i).getId().equals(ingrediente.getId())) {
                listaIngredientes.remove(i);
                break;
            }
        }
        listaIngredientes.add(0, ingrediente);
    }

    private void removerIngredienteDaLista(IngredienteResponse ingrediente) {
        for (int i = 0; i < listaIngredientes.size(); i++) {
            if (listaIngredientes.get(i).getId().equals(ingrediente.getId())) {
                listaIngredientes.remove(i);
                break;
            }
        }
    }

    private void atualizarNumeroPagina() {
        tvNumeroPagina.setText(String.valueOf(paginaAtual + 1));

        // desabilitar botão anterior se estiver na primeira página
        btnPaginaAnterior.setAlpha(paginaAtual > 0 ? 1.0f : 0.3f);
        btnPaginaAnterior.setEnabled(paginaAtual > 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Só recarrega se não for o primeiro carregamento
        if (!primeiraCarregamento && adapter != null && !estaBuscandoPorNome) {
            carregarPagina(paginaAtual);
        } else {
            primeiraCarregamento = false;
        }
    }
}