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

import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.api.TabelaAPI; // Mantido, mas não usado diretamente para busca de tabelas do produto
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.ui.Comparacao.TabelaAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparacaoParte2Fragment extends Fragment implements TabelaAdapter.OnTabelaClickListener {

    // Constantes para salvar estado (Mantido para compatibilidade)
    private static final String KEY_TEXT_MUDADO = "text_mudado";
    private static final String KEY_ITEMS_VISIVEL = "items_visivel";
    private static final String KEY_BUTTON_VISIVEL = "button_visivel";
    private static final String TAG = "ComparacaoP2Fragment";

    // Chave do argumento e variável para armazenar o ID do produto
    private static final String ARG_PRODUTO_ID = "produto_id";
    private Integer produtoId;

    // API e RecyclerView
    // TabelaAPI mantida, mas ProdutoAPI será usada para buscar a lista de tabelas
    private TabelaAPI tabelaApi;
    private ProdutoAPI produtoApi;
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

    // URL da API
    private static final String url = "https://api-spring-mongodb.onrender.com/";

    /**
     * MÉTODO FACTORY: Cria uma nova instância do fragment e empacota o ID do produto.
     */
    public static ComparacaoParte2Fragment newInstance(Integer produtoId) {
        ComparacaoParte2Fragment fragment = new ComparacaoParte2Fragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUTO_ID, produtoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            produtoId = getArguments().getInt(ARG_PRODUTO_ID, -1);
            if (produtoId == -1) {
                Log.e(TAG, "ID do Produto não encontrado nos argumentos!");
            } else {
                Log.d(TAG, "ID do Produto Recebido: " + produtoId);
            }
        }
        return inflater.inflate(R.layout.activity_comparacao_fragment_parte2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. Inicialização da API ---
        ConexaoAPI apiManager = new ConexaoAPI(url);
        tabelaApi = apiManager.getApi(TabelaAPI.class);
        produtoApi = apiManager.getApi(ProdutoAPI.class);

        // --- 2. Configurações de UI ---
        view.findViewById(R.id.voltar).setOnClickListener(v -> requireActivity().onBackPressed());

        subtitulo = view.findViewById(R.id.textViewSelecionarProduto3);
        listaItensPrincipalContainer = view.findViewById(R.id.listaItensPrincipal);
        botaoComparar = view.findViewById(R.id.btn_comparar);

        headerItem0 = view.findViewById(R.id.header_item);
        headerItem1 = view.findViewById(R.id.header_item1);

        if (headerItem0 != null) {
            nomeTabela1 = headerItem0.findViewById(R.id.textViewTitulo);
        }
        if (headerItem1 != null) {
            nomeTabela2 = headerItem1.findViewById(R.id.textViewTitulo3);
        }

        // --- 3. Configuração da RecyclerView ---
        if (listaItensPrincipalContainer != null) {
            recyclerViewTabelas = listaItensPrincipalContainer.findViewById(R.id.recyclerViewTabelas);
            if (recyclerViewTabelas != null) {
                recyclerViewTabelas.setLayoutManager(new LinearLayoutManager(getContext()));
            } else {
                Log.e(TAG, "Erro: RecyclerView não encontrada. Verifique o ID 'recyclerViewTabelas'.");
            }
        }

        // --- 4. Inicialização da API e Lógica de Busca ---
        if (produtoId != null && produtoId != -1) {
            // Inicia o servidor e, em seguida, busca TODAS as tabelas do produto
            apiManager.iniciarServidor(requireActivity(), () -> buscarTodasTabelasDoProduto(produtoId));
        } else {
            Toast.makeText(getContext(), "Erro: ID do produto inválido.", Toast.LENGTH_LONG).show();
        }

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
                    // TODO: Implementar navegação para a tela de comparação
                    Toast.makeText(getContext(), "Pronto para Comparar! Navegando...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Selecione duas tabelas para comparar.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 🚀 NOVO MÉTODO: Busca todas as tabelas de um produto em uma única chamada de API.
     */
    private void buscarTodasTabelasDoProduto(Integer produtoId) {
        if (produtoApi == null) {
            Log.e(TAG, "ProdutoAPI não inicializada.");
            return;
        }

        // ⚠️ Exibir um ProgressBar se você tiver um no layout
        // binding.progressBar.setVisibility(View.VISIBLE);

        produtoApi.buscarTodasTabelasDoProduto(produtoId).enqueue(new Callback<List<GetTabelaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<GetTabelaDTO>> call, @NonNull Response<List<GetTabelaDTO>> response) {
                // ⚠️ Esconder o ProgressBar

                if (response.isSuccessful() && response.body() != null) {
                    List<GetTabelaDTO> listaTabelas = response.body();

                    if (!listaTabelas.isEmpty()) {
                        setupAdapter(listaTabelas);
                    } else {
                        Toast.makeText(getContext(), "Nenhuma tabela encontrada para este produto.", Toast.LENGTH_LONG).show();
                        if (listaItensPrincipalContainer != null) listaItensPrincipalContainer.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Erro ao buscar tabelas: Código " + response.code());
                    Toast.makeText(getContext(), "Erro na resposta do servidor ao buscar tabelas.", Toast.LENGTH_LONG).show();
                    if (listaItensPrincipalContainer != null) listaItensPrincipalContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetTabelaDTO>> call, @NonNull Throwable t) {
                // ⚠️ Esconder o ProgressBar
                Log.e(TAG, "Falha de conexão: " + t.getMessage());
                Toast.makeText(getContext(), "Falha ao conectar-se à API para buscar tabelas.", Toast.LENGTH_LONG).show();
                if (listaItensPrincipalContainer != null) listaItensPrincipalContainer.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Configura o Adapter com a lista de resultados.
     */
    private void setupAdapter(List<GetTabelaDTO> listaTabelas) {
        if (!isAdded() || getContext() == null) return;

        tabelaAdapter = new TabelaAdapter(listaTabelas);
        tabelaAdapter.setOnTabelaClickListener(ComparacaoParte2Fragment.this);
        if (recyclerViewTabelas != null) {
            recyclerViewTabelas.setAdapter(tabelaAdapter);
            // Garante que a lista seja exibida após o carregamento
            recyclerViewTabelas.setVisibility(View.VISIBLE);
        }
        if (listaItensPrincipalContainer != null) {
            listaItensPrincipalContainer.setVisibility(View.VISIBLE);
        }
    }


    // Implementação da interface TabelaAdapter.OnTabelaClickListener
    @Override
    public void onEscolherTabelaClick(GetTabelaDTO tabela) {
        // ... Lógica de seleção (mantida) ...
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
        // ... Lógica de atualização de UI (mantida) ...
        if (tabelaSelecionada1 != null && nomeTabela1 != null) {
            nomeTabela1.setText(tabelaSelecionada1.getNomeTabela());
        }
        if (tabelaSelecionada2 != null && nomeTabela2 != null) {
            nomeTabela2.setText(tabelaSelecionada2.getNomeTabela());
        }

        if (tabelaSelecionada1 != null && tabelaSelecionada2 != null) {
            subtitulo.setText("Hora de comparar suas tabelas!");
            if (headerItem0 != null) headerItem0.setVisibility(View.VISIBLE);
            if (headerItem1 != null) headerItem1.setVisibility(View.VISIBLE);
            if (botaoComparar != null) botaoComparar.setVisibility(View.VISIBLE);

            if (recyclerViewTabelas != null) recyclerViewTabelas.setVisibility(View.GONE);
            if (listaItensPrincipalContainer != null) listaItensPrincipalContainer.setVisibility(View.GONE);

        } else if (tabelaSelecionada1 != null) {
            subtitulo.setText("Escolha a segunda tabela para comparação");
            if (headerItem0 != null) headerItem0.setVisibility(View.VISIBLE);
            if (headerItem1 != null) headerItem1.setVisibility(View.GONE);
            if (botaoComparar != null) botaoComparar.setVisibility(View.GONE);
        } else {
            subtitulo.setText("Escolha duas tabelas do produto");
            if (headerItem0 != null) headerItem0.setVisibility(View.GONE);
            if (headerItem1 != null) headerItem1.setVisibility(View.GONE);
            if (botaoComparar != null) botaoComparar.setVisibility(View.GONE);
        }
    }

    /**
     * ❌ REMOVIDO: A lógica de buscar IDs mockados e as chamadas sequenciais
     * (`getTableIdsForProduto`, `buscarTabelas`, `chamarSegundaTabela`) foram
     * removidas e substituídas pela única chamada `buscarTodasTabelasDoProduto`.
     */

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salvar o estado da UI (simplificado)
        outState.putBoolean(KEY_TEXT_MUDADO, subtitulo != null && subtitulo.getText().toString().contains("comparar"));
        outState.putBoolean(KEY_ITEMS_VISIVEL, headerItem0 != null && headerItem0.getVisibility() == View.VISIBLE);
        outState.putBoolean(KEY_BUTTON_VISIVEL, botaoComparar != null && botaoComparar.getVisibility() == View.VISIBLE);
    }
}