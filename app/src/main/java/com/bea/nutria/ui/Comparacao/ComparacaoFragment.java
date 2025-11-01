package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.databinding.FragmentComparacaoBinding;
import com.bea.nutria.model.GetProdutoDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparacaoFragment extends Fragment {

    private FragmentComparacaoBinding binding;

    private View demonstracaoItem1;
    private TextView textViewSelecionarProduto1;
    private Button botaoTesteTransicao;
    private View demonstracaoItemSelecionado;
    private TextView nomeProdutoSelecionado;

    private TextView textViewSelecionarProduto2;
    private View iconeTabela;
    private View btnEscolherTabelas;

    // Referência para a barra de pesquisa
    private EditText searchBar;

    // Referência para a ProgressBar
    private ProgressBar progressBarLoading;

    private RecyclerView recyclerViewProdutos;
    private ComparacaoAdapter comparacaoAdapter;

    private static final String TAG = "ComparacaoP1Fragment";


    private Integer idUsuario = 1;

    // Variável para armazenar o ID do produto selecionado
    private Integer produtoSelecionadoId = null;

    // Armazena o objeto do produto que foi removido para poder reinserir na lista
    private GetProdutoDTO produtoRemovidoAnteriormente = null;

    private ProdutoAPI produtoApi;
    private ConexaoAPI apiManager;

    private static final String url = "https://api-spring-mongodb.onrender.com/";

    private static final int FRAGMENT_CONTAINER_ID = R.id.nav_host_fragment_activity_main;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComparacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // --- Inicialização do Gerenciador de API ---
        apiManager = new ConexaoAPI(url);
        produtoApi = apiManager.getApi(ProdutoAPI.class);
        // ------------------------------------------

        // Referências dos elementos de UI
        demonstracaoItem1 = view.findViewById(R.id.View_demonstracaoItem_1);
        textViewSelecionarProduto1 = view.findViewById(R.id.textViewSelecionarProduto1);
        // botaoTesteTransicao = view.findViewById(R.id.botaoTesteTransicao);
        demonstracaoItemSelecionado = view.findViewById(R.id.View_demonstracaoItem_selecionado);
        nomeProdutoSelecionado = view.findViewById(R.id.textViewNomeProdutoSelecionado);
        textViewSelecionarProduto2 = view.findViewById(R.id.textViewSelecionarProduto2);
        iconeTabela = view.findViewById(R.id.imageViewIconeTabela);
        btnEscolherTabelas = view.findViewById(R.id.btn_escolherTabelas);

        // Referência para a ProgressBar
        progressBarLoading = view.findViewById(R.id.progress_bar_loading);

        // Referência para a barra de pesquisa
        searchBar = view.findViewById(R.id.search_bar);

        // --- Configuração da RecyclerView ---
        View listaItensIncluded = view.findViewById(R.id.listaItens);
        if (listaItensIncluded != null) {
            recyclerViewProdutos = listaItensIncluded.findViewById(R.id.recyclerViewListaProdutos);
            if (recyclerViewProdutos != null) {
                recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(getContext()));
            } else {
                Log.e("ComparacaoFragment", "Erro: recyclerViewListaProdutos não encontrado.");
            }
        } else {
            Log.e("ComparacaoFragment", "Erro: listaItens (include) não encontrado.");
        }
        // -----------------------------------

        // --- Uso da função iniciandoServidor (A Boa Prática) ---
        progressBarLoading.setVisibility(View.VISIBLE);
        apiManager.iniciarServidor(requireActivity(), () -> buscarProdutoDoUsuario(idUsuario));
        // -------------------------------------------------------

        // Configurar o listener da barra de pesquisa
        setupSearchFunctionality();

        // Fecha o teclado ao tocar fora da EditText
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View currentFocus = requireActivity().getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    Rect outRect = new Rect();
                    currentFocus.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        currentFocus.clearFocus();
                        hideKeyboard(); // Usa o novo método auxiliar
                    }
                }
            }
            return false;
        });

        // Abre ComparacaoParte2Fragment
        if (btnEscolherTabelas != null) {
            btnEscolherTabelas.setOnClickListener(v -> {
                if (produtoSelecionadoId != null) {

                    // AÇÃO CORRIGIDA: Força a barra de pesquisa a perder o foco
                    // e esconde o teclado antes da transição.
                    if (searchBar != null) {
                        searchBar.clearFocus();
                    }
                    hideKeyboard();
                    // --------------------------------------------------------

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    ComparacaoParte2Fragment nextFragment =
                            ComparacaoParte2Fragment.newInstance(produtoSelecionadoId);


                    fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, nextFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    Log.w("ComparacaoFragment", "Tentativa de transição sem produto selecionado.");
                    // Opcional: Adicionar um Toast de aviso aqui.
                }
            });
        }
    }

    /**
     * Configura o TextWatcher para a funcionalidade de pesquisa/filtragem.
     */
    private void setupSearchFunctionality() {
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (comparacaoAdapter != null) {
                        comparacaoAdapter.filter(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }
    }


    private void buscarProdutoDoUsuario(Integer idUsuario) {
        produtoApi.buscarProdutosComMaisDeUmaTabela(idUsuario).enqueue(new Callback<List<GetProdutoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<GetProdutoDTO>> call, @NonNull Response<List<GetProdutoDTO>> response) {
                if (getActivity() == null || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {

                    List<GetProdutoDTO> listaRetorno = response.body();

                    if (recyclerViewProdutos != null && !listaRetorno.isEmpty()) {

                        // --- Lógica de integração do Adapter ---
                        comparacaoAdapter = new ComparacaoAdapter(listaRetorno);

                        comparacaoAdapter.setOnItemClickListener(produto -> {
                            Log.d("Comparacao", "Produto selecionado: " + produto.getNome());

                            // AÇÃO REQUERIDA: FECHAR O TECLADO AO CLICAR NO ITEM
                            hideKeyboard();
                            // ------------------------------------

                            if (comparacaoAdapter != null) {

                                if (produtoRemovidoAnteriormente != null) {
                                    comparacaoAdapter.addItem(produtoRemovidoAnteriormente);
                                    produtoRemovidoAnteriormente = null;
                                }

                                comparacaoAdapter.removeItem(produto);
                                produtoRemovidoAnteriormente = produto;
                            }

                            produtoSelecionadoId = produto.getId();

                            // Transição visual
                            textViewSelecionarProduto1.setVisibility(View.GONE);
                            demonstracaoItem1.setVisibility(View.GONE);

                            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
                            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
                            iconeTabela.setVisibility(View.VISIBLE);
                            btnEscolherTabelas.setVisibility(View.VISIBLE);

                            textViewSelecionarProduto2.setVisibility(View.VISIBLE);

                            nomeProdutoSelecionado.setText(produto.getNome());

                        });

                        recyclerViewProdutos.setAdapter(comparacaoAdapter);
                        recyclerViewProdutos.setVisibility(View.VISIBLE);

                    } else {
                        Log.d("API:", "Lista de produtos vazia ou RecyclerView não inicializada.");
                        tratarListaVaziaOuErro();
                    }
                } else {
                    Log.e("API", "Erro na resposta da API: " + response.code());
                    tratarListaVaziaOuErro();
                }

                // Oculta a ProgressBar
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetProdutoDTO>> call, @NonNull Throwable t) {
                Log.e("API:", "Falha na requisição: " + t.getMessage());
                if (getActivity() != null) {
                    // Oculta a ProgressBar na falha
                    if (progressBarLoading != null) {
                        progressBarLoading.setVisibility(View.GONE);
                    }
                    getActivity().runOnUiThread(ComparacaoFragment.this::tratarListaVaziaOuErro);
                }
            }
        });
    }

    /**
     * Centraliza a lógica de exibição em caso de lista vazia ou erro de API.
     */
    private void tratarListaVaziaOuErro() {
        if (recyclerViewProdutos != null) {
            recyclerViewProdutos.setVisibility(View.GONE);
        }
        // Mostra a UI de "Escolha seu produto"
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);
        if (botaoTesteTransicao != null) {
            botaoTesteTransicao.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Esconde o teclado virtual
     */
    private void hideKeyboard() {
        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resetEstado();
    }

    /**
     *  Restaura o Fragment para o estado inicial.
     */
    private void resetEstado() {
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);

        demonstracaoItemSelecionado.setVisibility(View.GONE);
        nomeProdutoSelecionado.setVisibility(View.GONE);
        iconeTabela.setVisibility(View.GONE);
        btnEscolherTabelas.setVisibility(View.GONE);
        textViewSelecionarProduto2.setVisibility(View.GONE);

        // Reseta os IDs e o produto removido
        produtoSelecionadoId = null;

        if (comparacaoAdapter != null && produtoRemovidoAnteriormente != null) {
            comparacaoAdapter.addItem(produtoRemovidoAnteriormente);
        }
        produtoRemovidoAnteriormente = null;

        if (recyclerViewProdutos != null) {
            recyclerViewProdutos.setVisibility(View.VISIBLE);
        }

        nomeProdutoSelecionado.setText("");

        // Limpa a SearchBar
        if (searchBar != null) {
            searchBar.setText("");
        }

        // Esconde o teclado
        hideKeyboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


}