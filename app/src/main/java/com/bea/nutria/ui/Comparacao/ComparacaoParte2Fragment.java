package com.bea.nutria.ui.Comparacao;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager; // Importação necessária
import androidx.fragment.app.FragmentTransaction; // Importação necessária
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.model.GetTabelaDTO;
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

    // Elementos de expansão para as tabelas selecionadas
    private ImageButton seta1;
    private ImageButton seta2;
    private ConstraintLayout conteudoExpansivel1;
    private ConstraintLayout conteudoExpansivel2;

    // NOVO: Ícones de seleção para os headers
    private ImageView iconSelecionado1;
    private ImageView iconSelecionado2;

    // Variáveis de estado para a expansão
    private boolean isTabela1Expanded = false;
    private boolean isTabela2Expanded = false;

    // Variáveis de estado para armazenar as tabelas selecionadas
    private GetTabelaDTO tabelaSelecionada1 = null;
    private GetTabelaDTO tabelaSelecionada2 = null;

    // URL da API
    private static final String url = "https://api-spring-mongodb.onrender.com/";

    private static final int FRAGMENT_CONTAINER_ID = R.id.nav_host_fragment_activity_main;

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

        headerItem0 = view.findViewById(R.id.header_item); // Header Tabela 1
        headerItem1 = view.findViewById(R.id.header_item1); // Header Tabela 2

        // Inicialização dos elementos de expansão
        conteudoExpansivel1 = view.findViewById(R.id.conteudo_expansivel_tabela1);
        conteudoExpansivel2 = view.findViewById(R.id.conteudo_expansivel_tabela2);

        // Inicialização dos elementos da Tabela 1 (Header 0)
        if (headerItem0 != null) {
            nomeTabela1 = headerItem0.findViewById(R.id.textViewTitulo);
            seta1 = headerItem0.findViewById(R.id.imageButtonSeta);
            iconSelecionado1 = headerItem0.findViewById(R.id.icon_selecionado1); // NOVO
            headerItem0.setOnClickListener(v -> toggleHeaderExpansion(1));
        }

        // Inicialização dos elementos da Tabela 2 (Header 1)
        if (headerItem1 != null) {
            nomeTabela2 = headerItem1.findViewById(R.id.textViewTitulo3);

            // Tenta corrigir o erro de ID no Java
            int seta2Id = getResources().getIdentifier("imageButtonSeta2", "id", requireContext().getPackageName());
            if (seta2Id != 0) {
                seta2 = headerItem1.findViewById(seta2Id);
            } else {
                Log.e(TAG, "ID 'imageButtonSeta2' não encontrado. Verifique se o XML foi atualizado.");
                seta2 = headerItem1.findViewById(R.id.imageButtonSeta);
            }

            iconSelecionado2 = headerItem1.findViewById(R.id.icon_selecionado2); // NOVO

            headerItem1.setOnClickListener(v -> toggleHeaderExpansion(2));
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
                    // *** LÓGICA ATUALIZADA PARA INICIAR ComparacaoParte3Fragment ***

                    // Cria uma nova instância do fragmento de destino, passando os IDs E OS NOMES
                    Fragment nextFragment = ComparacaoParte3Fragment.newInstance(
                            tabelaSelecionada1.getTabelaId(),
                            tabelaSelecionada2.getTabelaId(),
                            // NOVO: Passando os nomes
                            tabelaSelecionada1.getNomeTabela(),
                            tabelaSelecionada2.getNomeTabela()
                    );

                    // Usa o FragmentManager para iniciar a transação
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    // Substitui o fragmento atual pelo novo.
                    // Certifique-se de que o ID do container (R.id.fragment_container, por exemplo) está correto no seu layout da Activity.
                    // Adiciona a transação à Back Stack para que o usuário possa voltar.
                    fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, nextFragment); // Substitua 'R.id.fragment_container' pelo ID do seu container de fragmentos
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    // Removido o Intent, pois estamos navegando entre Fragments, não Activities
                } else {
                    Toast.makeText(getContext(), "Selecione duas tabelas para comparar.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Alterna o estado de expansão de uma das tabelas selecionadas no cabeçalho.
     */
    private void toggleHeaderExpansion(int tableIndex) {
        ImageButton seta;
        ConstraintLayout conteudo;
        boolean isExpanded;

        if (tableIndex == 1) {
            if (tabelaSelecionada1 == null) return;
            isExpanded = !isTabela1Expanded;
            isTabela1Expanded = isExpanded;
            seta = seta1;
            conteudo = conteudoExpansivel1;
        } else if (tableIndex == 2) {
            if (tabelaSelecionada2 == null) return;
            isExpanded = !isTabela2Expanded;
            isTabela2Expanded = isExpanded;
            seta = seta2;
            conteudo = conteudoExpansivel2;
        } else {
            return;
        }

        if (conteudo != null) {
            conteudo.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        // Rotação animada da seta
        if (seta != null) {
            seta.animate().rotation(isExpanded ? 180f : 0f).setDuration(200).start();
        }
    }


    /**
     * Busca todas as tabelas de um produto em uma única chamada de API.
     */
    private void buscarTodasTabelasDoProduto(Integer produtoId) {
        if (produtoApi == null) {
            Log.e(TAG, "ProdutoAPI não inicializada.");
            return;
        }

        produtoApi.buscarTodasTabelasDoProduto(produtoId).enqueue(new Callback<List<GetTabelaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<GetTabelaDTO>> call, @NonNull Response<List<GetTabelaDTO>> response) {
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
        // O fragmento implementa a interface OnTabelaClickListener
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
    public void onEscolherTabelaClick(GetTabelaDTO tabela, int position) {
        // CORREÇÃO DE LÓGICA: Usar uma variável para armazenar a tabela que foi deselecionada,
        // garantindo que a adição/remoção na lista visível (Adapter) seja feita corretamente.

        // 1. Variáveis de estado
        boolean itemRemovidoDaListaVisivel = false;
        GetTabelaDTO tabelaParaReAdicionar = null;

        // VERIFICAÇÃO DE ID (CRUCIAL): Garante que a tabela tenha um ID para comparação
        if (tabela.getTabelaId() == null) {
            Toast.makeText(getContext(), "Erro: Tabela sem ID.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Tenta deselecionar (e re-adicionar à lista principal)
        // A. Tabela clicada é a Tabela 1?
        if (tabelaSelecionada1 != null && tabela.getTabelaId().equals(tabelaSelecionada1.getTabelaId())) {
            tabelaParaReAdicionar = tabelaSelecionada1;

            // Move a Tabela 2 para o slot da Tabela 1, se houver, para evitar buracos
            if (tabelaSelecionada2 != null) {
                tabelaSelecionada1 = tabelaSelecionada2;
                tabelaSelecionada2 = null;
            } else {
                tabelaSelecionada1 = null;
            }
            Toast.makeText(getContext(), tabelaParaReAdicionar.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();

            // B. Tabela clicada é a Tabela 2?
        } else if (tabelaSelecionada2 != null && tabela.getTabelaId().equals(tabelaSelecionada2.getTabelaId())) {
            tabelaParaReAdicionar = tabelaSelecionada2;
            tabelaSelecionada2 = null;
            Toast.makeText(getContext(), tabelaParaReAdicionar.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();

            // 3. Tenta selecionar (e remover da lista principal)
            // C. Seleciona no Slot 1
        } else if (tabelaSelecionada1 == null) {
            tabelaSelecionada1 = tabela;
            itemRemovidoDaListaVisivel = true; // Marca para remover da lista visível

            // D. Seleciona no Slot 2 (Este é o ponto que estava falhando na sua lógica anterior)
        } else if (tabelaSelecionada2 == null) {
            tabelaSelecionada2 = tabela;
            itemRemovidoDaListaVisivel = true; // Marca para remover da lista visível

            // E. Máximo atingido
        } else {
            Toast.makeText(getContext(), "Máximo de duas tabelas selecionadas.", Toast.LENGTH_LONG).show();
            return; // Não faz nada se já houver duas selecionadas
        }

        // 4. Executa a ação na lista visível (Adapter)

        if (tabelaParaReAdicionar != null) {
            // Se deselecionamos, re-adiciona o item à lista visível
            if (tabelaAdapter != null) {
                tabelaAdapter.addItem(tabelaParaReAdicionar);
            }
        } else if (itemRemovidoDaListaVisivel) {
            // Se selecionamos um novo item, remove ele da lista visível
            if (tabelaAdapter != null) {
                // A posição é o índice atual do item na lista visível antes de ser removido
                tabelaAdapter.removeItem(position);
            }
        }

        // 5. Atualiza a UI para refletir a nova seleção/deseleção, incluindo a visibilidade do ícone
        atualizarUISelecao();
    }

    /**
     * Atualiza a UI com base nas tabelas selecionadas, incluindo a visibilidade dos ícones de seleção.
     */
    private void atualizarUISelecao() {
        // Resetar o estado de expansão e visibilidade do conteúdo ao mudar a seleção
        isTabela1Expanded = false;
        isTabela2Expanded = false;
        if (conteudoExpansivel1 != null) conteudoExpansivel1.setVisibility(View.GONE);
        if (conteudoExpansivel2 != null) conteudoExpansivel2.setVisibility(View.GONE);
        if (seta1 != null) seta1.setRotation(0);
        if (seta2 != null) seta2.setRotation(0);

        // --- Tabela 1 ---
        if (tabelaSelecionada1 != null) {
            if (nomeTabela1 != null) nomeTabela1.setText(tabelaSelecionada1.getNomeTabela());
            if (iconSelecionado1 != null) iconSelecionado1.setVisibility(View.VISIBLE); // EXIBE O ÍCONE
        } else {
            if (nomeTabela1 != null) nomeTabela1.setText(""); // Limpa se for nulo
            if (iconSelecionado1 != null) iconSelecionado1.setVisibility(View.GONE); // OCULTA O ÍCONE
        }

        // --- Tabela 2 ---
        if (tabelaSelecionada2 != null) {
            if (nomeTabela2 != null) nomeTabela2.setText(tabelaSelecionada2.getNomeTabela());
            if (iconSelecionado2 != null) iconSelecionado2.setVisibility(View.VISIBLE); // EXIBE O ÍCONE
        } else {
            if (nomeTabela2 != null) nomeTabela2.setText(""); // Limpa se for nulo
            if (iconSelecionado2 != null) iconSelecionado2.setVisibility(View.GONE); // OCULTA O ÍCONE
        }

        // --- Lógica de Visibilidade Geral ---

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salvar o estado da UI (simplificado)
        outState.putBoolean(KEY_TEXT_MUDADO, subtitulo != null && subtitulo.getText().toString().contains("comparar"));
        outState.putBoolean(KEY_ITEMS_VISIVEL, headerItem0 != null && headerItem0.getVisibility() == View.VISIBLE);
        outState.putBoolean(KEY_BUTTON_VISIVEL, botaoComparar != null && botaoComparar.getVisibility() == View.VISIBLE);
    }
}
