package com.bea.nutria;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.ui.Comparacao.TabelaAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparacaoParte2Fragment extends Fragment implements TabelaAdapter.OnTabelaClickListener {

    // Constantes para salvar estado
    private static final String KEY_TEXT_MUDADO = "text_mudado";
    private static final String KEY_ITEMS_VISIVEL = "items_visivel";
    private static final String KEY_BUTTON_VISIVEL = "button_visivel";
    private static final String TAG = "ComparacaoP2Fragment";

    // API e RecyclerView
    private TabelaAPI tabelaApi;
    private RecyclerView recyclerViewTabelas;
    private TabelaAdapter tabelaAdapter;

    // Elementos de UI
    private TextView subtitulo;
    private View listaItensPrincipalContainer;
    private ConstraintLayout headerItem1;
    private ConstraintLayout headerItem0;
    private TextView nomeTabela1;
    private TextView nomeTabela2;
    private MaterialButton botaoComparar;

    // Variáveis de estado para armazenar as tabelas selecionadas
    private GetTabelaDTO tabelaSelecionada1 = null;
    private GetTabelaDTO tabelaSelecionada2 = null;

    // NOVOS IDs MOCKADOS para forçar o carregamento das duas tabelas.
    // O backend está sendo tratado como se o ID do PRODUTO fosse '1' ou '2'
    private final List<Integer> MOCKED_TABLE_IDS = Arrays.asList(1, 2);
    private static final String url = "https://api-spring-mongodb.onrender.com/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_comparacao_fragment_parte2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. Inicialização da API ---
        ConexaoAPI apiManager = new ConexaoAPI(url);
        tabelaApi = apiManager.getApi(TabelaAPI.class);

        // --- 2. Configurações de UI ---
        view.findViewById(R.id.voltar).setOnClickListener(v -> requireActivity().onBackPressed());

        subtitulo = view.findViewById(R.id.textViewSelecionarProduto3);
        listaItensPrincipalContainer = view.findViewById(R.id.listaItensPrincipal);
        botaoComparar = view.findViewById(R.id.btn_comparar);

        // Referências aos Headers de seleção
        headerItem0 = view.findViewById(R.id.header_item);
        headerItem1 = view.findViewById(R.id.header_item1);

        // TextViews para exibir o nome da tabela selecionada
        if (headerItem0 != null) {
            nomeTabela1 = headerItem0.findViewById(R.id.textViewTitulo);
        }
        if (headerItem1 != null) {
            nomeTabela2 = headerItem1.findViewById(R.id.textViewTitulo3);
        }

        // --- 3. Configuração da RecyclerView ---
        if (listaItensPrincipalContainer != null) {
            // Verifique se o ID está correto. No log anterior estava 'recyclerViewListaTabelas'
            // Aqui estamos usando o ID que você forneceu no código, 'recyclerViewTabelas'
            recyclerViewTabelas = listaItensPrincipalContainer.findViewById(R.id.recyclerViewTabelas);
            if (recyclerViewTabelas != null) {
                recyclerViewTabelas.setLayoutManager(new LinearLayoutManager(getContext()));
            } else {
                Log.e(TAG, "Erro: RecyclerView não encontrada. Verifique o ID 'recyclerViewTabelas'.");
            }
        }

        // --- 4. Inicialização da API e Lógica de Comparação ---
        apiManager.iniciarServidor(requireActivity(), () -> buscarTabelas(MOCKED_TABLE_IDS));

        // Restaura o estado da UI (simplificado)
        if (savedInstanceState != null) {
            boolean textMudado = savedInstanceState.getBoolean(KEY_TEXT_MUDADO, false);
            boolean itemsVisivel = savedInstanceState.getBoolean(KEY_ITEMS_VISIVEL, false);
            boolean buttonVisivel = savedInstanceState.getBoolean(KEY_BUTTON_VISIVEL, false);

            if (textMudado) subtitulo.setText("Hora de comparar suas tabelas!");
            if (itemsVisivel) {
                if (headerItem0 != null) headerItem0.setVisibility(View.VISIBLE);
                if (headerItem1 != null) headerItem1.setVisibility(View.VISIBLE);
            }
            if (buttonVisivel) {
                if (botaoComparar != null) botaoComparar.setVisibility(View.VISIBLE);
            }
        } else {
            atualizarUISelecao(); // Estado inicial
        }

        // --- 5. Listener do botão Comparar ---
        if (botaoComparar != null) {
            botaoComparar.setOnClickListener(v -> {
                if (tabelaSelecionada1 != null && tabelaSelecionada2 != null) {
                    Toast.makeText(getContext(), "Pronto para Comparar!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Selecione duas tabelas para comparar.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Implementação da interface TabelaAdapter.OnTabelaClickListener
    @Override
    public void onEscolherTabelaClick(GetTabelaDTO tabela) {
        boolean tabelaJaSelecionada1 = tabelaSelecionada1 != null && tabela.getTabelaId().equals(tabelaSelecionada1.getTabelaId());
        boolean tabelaJaSelecionada2 = tabelaSelecionada2 != null && tabela.getTabelaId().equals(tabelaSelecionada2.getTabelaId());

        if (tabelaJaSelecionada1) {
            tabelaSelecionada1 = null;
            if (tabelaSelecionada2 != null) {
                tabelaSelecionada1 = tabelaSelecionada2;
                tabelaSelecionada2 = null;
            }
            Toast.makeText(getContext(), tabela.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();
        } else if (tabelaJaSelecionada2) {
            tabelaSelecionada2 = null;
            Toast.makeText(getContext(), tabela.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();
        } else if (tabelaSelecionada1 == null) {
            tabelaSelecionada1 = tabela;
        } else if (tabelaSelecionada2 == null) {
            tabelaSelecionada2 = tabela;
        } else {
            Toast.makeText(getContext(), "Máximo de duas tabelas selecionadas.", Toast.LENGTH_LONG).show();
        }

        atualizarUISelecao();

        if (tabelaSelecionada1 == null || tabelaSelecionada2 == null) {
            if (listaItensPrincipalContainer != null) {
                listaItensPrincipalContainer.setVisibility(View.VISIBLE);
            }
            if (recyclerViewTabelas != null) {
                recyclerViewTabelas.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Atualiza a UI com base nas tabelas selecionadas.
     */
    private void atualizarUISelecao() {
        if (tabelaSelecionada1 != null && nomeTabela1 != null) {
            nomeTabela1.setText(tabelaSelecionada1.getNomeTabela());
        }
        if (tabelaSelecionada2 != null && nomeTabela2 != null) {
            nomeTabela2.setText(tabelaSelecionada2.getNomeTabela());
        }

        if (tabelaSelecionada1 != null && tabelaSelecionada2 != null) {
            // 2 selecionadas
            subtitulo.setText("Hora de comparar suas tabelas!");
            headerItem0.setVisibility(View.VISIBLE);
            headerItem1.setVisibility(View.VISIBLE);
            botaoComparar.setVisibility(View.VISIBLE);

            // Ocultar a lista de seleção
            if (recyclerViewTabelas != null) recyclerViewTabelas.setVisibility(View.GONE);
            if (listaItensPrincipalContainer != null) listaItensPrincipalContainer.setVisibility(View.GONE);

        } else if (tabelaSelecionada1 != null) {
            // 1 selecionada
            subtitulo.setText("Escolha a segunda tabela para comparação");
            headerItem0.setVisibility(View.VISIBLE);
            headerItem1.setVisibility(View.GONE);
            botaoComparar.setVisibility(View.GONE);
        } else {
            // 0 selecionadas
            subtitulo.setText("Escolha duas tabelas do produto");
            headerItem0.setVisibility(View.GONE);
            headerItem1.setVisibility(View.GONE);
            botaoComparar.setVisibility(View.GONE);
        }
    }


    /**
     * Busca os dados da API para múltiplos IDs sequencialmente.
     * Esta é a adaptação para contornar a limitação da API que retorna apenas um objeto por ID.
     */
    private void buscarTabelas(List<Integer> tableIds) {
        if (tabelaApi == null || recyclerViewTabelas == null || tableIds.isEmpty()) {
            return;
        }

        List<GetTabelaDTO> listaFinalTabelas = new ArrayList<>();
        Integer firstId = tableIds.get(0);

        // Chamada para o primeiro ID (Ex: Tabela 1)
        tabelaApi.buscarTabela(firstId).enqueue(new Callback<GetTabelaDTO>() {
            @Override
            public void onResponse(@NonNull Call<GetTabelaDTO> call, @NonNull Response<GetTabelaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaFinalTabelas.add(response.body());
                    Log.d(TAG, "Tabela 1 carregada: " + response.body().getNomeTabela());

                    // Se houver um segundo ID, faça a chamada sequencial
                    if (tableIds.size() > 1) {
                        Integer secondId = tableIds.get(1);
                        chamarSegundaTabela(secondId, listaFinalTabelas);
                    } else {
                        // Se houver apenas 1 ID mockado, finalize aqui
                        setupAdapter(listaFinalTabelas);
                    }
                } else {
                    Log.e(TAG, "Erro ao carregar Tabela " + firstId + ": Código " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar Tabela 1.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetTabelaDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha na requisição da Tabela 1: " + t.getMessage());
                Toast.makeText(getContext(), "Erro de rede ao buscar Tabela 1.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Segunda chamada sequencial para o segundo ID mockado.
     */
    private void chamarSegundaTabela(Integer secondId, List<GetTabelaDTO> listaFinalTabelas) {
        tabelaApi.buscarTabela(secondId).enqueue(new Callback<GetTabelaDTO>() {
            @Override
            public void onResponse(@NonNull Call<GetTabelaDTO> call, @NonNull Response<GetTabelaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaFinalTabelas.add(response.body());
                    Log.d(TAG, "Tabela 2 carregada: " + response.body().getNomeTabela());
                } else {
                    Log.e(TAG, "Erro ao carregar Tabela " + secondId + ": Código " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar Tabela 2.", Toast.LENGTH_LONG).show();
                }

                // Finalize com a lista combinada, independentemente do sucesso da segunda chamada
                setupAdapter(listaFinalTabelas);
            }

            @Override
            public void onFailure(@NonNull Call<GetTabelaDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha na requisição da Tabela 2: " + t.getMessage());
                Toast.makeText(getContext(), "Erro de rede ao buscar Tabela 2.", Toast.LENGTH_LONG).show();

                // Finalize com a lista que tiver, mesmo se a Tabela 2 falhar
                setupAdapter(listaFinalTabelas);
            }
        });
    }

    /**
     * Configura o Adapter com a lista de resultados combinados.
     */
    private void setupAdapter(List<GetTabelaDTO> listaFinalTabelas) {
        if (!isAdded() || getContext() == null) return;

        if (!listaFinalTabelas.isEmpty()) {
            tabelaAdapter = new TabelaAdapter(listaFinalTabelas);
            tabelaAdapter.setOnTabelaClickListener(ComparacaoParte2Fragment.this);
            recyclerViewTabelas.setAdapter(tabelaAdapter);

            // Garante que a lista seja exibida após o carregamento
            if (listaItensPrincipalContainer != null) {
                listaItensPrincipalContainer.setVisibility(View.VISIBLE);
            }
            recyclerViewTabelas.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getContext(), "Nenhuma tabela pôde ser carregada.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // ... (lógica de onSaveInstanceState permanece a mesma) ...
    }
}