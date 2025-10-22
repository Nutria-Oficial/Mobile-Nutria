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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager; // Reintroduzido
import androidx.recyclerview.widget.RecyclerView;     // Reintroduzido

import com.bea.nutria.ComparacaoParte2Fragment;
import com.bea.nutria.R;
import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.databinding.FragmentComparacaoBinding;
import com.bea.nutria.model.GetProdutoDTO;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ComparacaoFragment extends Fragment {

    private FragmentComparacaoBinding binding;

    private View demonstracaoItem1;
    private TextView textViewSelecionarProduto1;
    private Button botaoTesteTransicao;
    private View demonstracaoItemSelecionado;
    private TextView nomeProdutoSelecionado;
    private View demonstracaoItem2;
    private TextView textViewSelecionarProduto2;
    private View iconeTabela;
    private View btnEscolherTabelas;

    // Variáveis da RecyclerView e Adapter (REINTRODUZIDAS)
    private RecyclerView recyclerViewProdutos;
    private ComparacaoAdapter comparacaoAdapter;

    private Integer idUsuario = 1;
    private ProdutoAPI produtoApi;
    private OkHttpClient client;
    private Retrofit retrofit;

    private String credenciais = "";

    // REMOVIDAS: ultimoWakeMs e JANELA_WAKE_MS

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

        // Referências dos elementos de UI
        demonstracaoItem1 = view.findViewById(R.id.View_demonstracaoItem_1);
        textViewSelecionarProduto1 = view.findViewById(R.id.textViewSelecionarProduto1);
        demonstracaoItemSelecionado = view.findViewById(R.id.View_demonstracaoItem_selecionado);
        nomeProdutoSelecionado = view.findViewById(R.id.textViewNomeProdutoSelecionado);
        demonstracaoItem2 = view.findViewById(R.id.view_demonstracaoItem2);
        textViewSelecionarProduto2 = view.findViewById(R.id.textViewSelecionarProduto2);
        iconeTabela = view.findViewById(R.id.imageViewIconeTabela);
        btnEscolherTabelas = view.findViewById(R.id.btn_escolherTabelas);
        botaoTesteTransicao = view.findViewById(R.id.botao_teste_transicao);

        // --- Configuração da RecyclerView (RESTAURADA E SEGURA) ---
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
        // --------------------------------------------------------

        // Credenciais: Assumindo "nutria" é o usuário e "nutria123" a senha (ajustado para Basic Auth)
        credenciais = Credentials.basic("nutria", "nutria123");

        // Configura o cliente OkHttp (mantido)
        client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Authorization", credenciais)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(req);
                })
                .build();

        // Configura Retrofit (base URL para o serviço MongoDB)
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api-spring-mongodb.onrender.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        produtoApi = retrofit.create(ProdutoAPI.class);

        // MUDANÇA CRÍTICA: Chama a API principal DIRETAMENTE (Sem o iniciandoServidor)
        buscarProdutoDoUsuario(idUsuario);

        // Simulação de transição
        botaoTesteTransicao.setOnClickListener(v -> {
            demonstracaoItem1.setVisibility(View.INVISIBLE);
            textViewSelecionarProduto1.setVisibility(View.GONE);
            botaoTesteTransicao.setVisibility(View.GONE);
            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
            iconeTabela.setVisibility(View.VISIBLE);
            btnEscolherTabelas.setVisibility(View.VISIBLE);
            iconeTabela.bringToFront();
            demonstracaoItem2.setVisibility(View.GONE);
            textViewSelecionarProduto2.setVisibility(View.GONE);
            nomeProdutoSelecionado.setText("Manteiga");

            // Novo: Oculta a lista de produtos (se estivesse visível)
            if (recyclerViewProdutos != null) {
                recyclerViewProdutos.setVisibility(View.GONE);
            }
        });

        // Fecha o teclado ao tocar fora (código mantido)
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View currentFocus = requireActivity().getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    Rect outRect = new Rect();
                    currentFocus.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        currentFocus.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                    }
                }
            }
            return false;
        });

        // Abre ComparacaoParte2Fragment
        btnEscolherTabelas.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, new ComparacaoParte2Fragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    // REMOVIDO: iniciandoServidor()

    private void buscarProdutoDoUsuario(Integer idUsuario) {
        // MUDAR: de Callback<GetProdutoDTO>
        // PARA: Callback<List<GetProdutoDTO>>
        produtoApi.buscarProdutosComMaisDeUmaTabela(idUsuario).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GetProdutoDTO>> call, @NonNull retrofit2.Response<List<GetProdutoDTO>> response) {
                if (getActivity() == null || binding == null) return; // Segurança de Fragmento

                if (response.isSuccessful() && response.body() != null) {

                    List<GetProdutoDTO> listaRetorno = response.body();

                    // Adicionada verificação de nulo no RecyclerViewProdutos
                    if (recyclerViewProdutos != null && !listaRetorno.isEmpty()) {

                        // --- Lógica de integração do Adapter (RESTAURADA) ---
                        comparacaoAdapter = new ComparacaoAdapter(listaRetorno);

                        comparacaoAdapter.setOnItemClickListener(produto -> {
                            Log.d("Comparacao", "Produto selecionado: " + produto.getNome());

                            // Transição visual: Oculta a lista e exibe o item selecionado
                            recyclerViewProdutos.setVisibility(View.GONE);
                            demonstracaoItem1.setVisibility(View.GONE);
                            textViewSelecionarProduto1.setVisibility(View.GONE);
                            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
                            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
                            iconeTabela.setVisibility(View.VISIBLE);
                            btnEscolherTabelas.setVisibility(View.VISIBLE);
                            demonstracaoItem2.setVisibility(View.VISIBLE);
                            textViewSelecionarProduto2.setVisibility(View.VISIBLE);

                            nomeProdutoSelecionado.setText(produto.getNome());
                            if (binding != null && binding.searchBar != null) {
                                binding.searchBar.setText(produto.getNome());
                            }
                        });

                        recyclerViewProdutos.setAdapter(comparacaoAdapter);

                        // 4. Mostrar a lista e ocultar a UI de "Escolha seu produto"
                        recyclerViewProdutos.setVisibility(View.VISIBLE);
                        demonstracaoItem1.setVisibility(View.GONE);
                        textViewSelecionarProduto1.setVisibility(View.GONE);
                        botaoTesteTransicao.setVisibility(View.GONE);

                    } else {
                        Log.d("API:", "Lista de produtos vazia ou RecyclerView não inicializada.");
                        tratarListaVaziaOuErro();
                    }
                } else {
                    Log.e("API", "Erro na resposta da API: " + response.code());
                    tratarListaVaziaOuErro();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetProdutoDTO>> call, @NonNull Throwable t) {
                Log.e("API:", "Falha na requisição: " + t.getMessage());
                // Garante que a UI seja atualizada na thread principal em caso de falha
                if (getActivity() != null) {
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
        botaoTesteTransicao.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetEstado();
    }

    /**
     * Restaura o Fragment para o estado inicial.
     */
    private void resetEstado() {
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);
        botaoTesteTransicao.setVisibility(View.VISIBLE);

        demonstracaoItemSelecionado.setVisibility(View.GONE);
        nomeProdutoSelecionado.setVisibility(View.GONE);
        iconeTabela.setVisibility(View.GONE);
        btnEscolherTabelas.setVisibility(View.GONE);
        demonstracaoItem2.setVisibility(View.GONE);
        textViewSelecionarProduto2.setVisibility(View.GONE);

        // RecyclerView (OCULTA)
        if (recyclerViewProdutos != null) {
            recyclerViewProdutos.setVisibility(View.GONE);
        }

        nomeProdutoSelecionado.setText("");

        // Limpa a SearchBar
        if (binding != null && binding.searchBar != null) {
            binding.searchBar.setText("");
        }

        // Esconde o teclado
        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}