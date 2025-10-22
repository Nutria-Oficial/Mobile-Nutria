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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import okhttp3.Response;
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

    private RecyclerView recyclerViewProdutos;
    private ComparacaoAdapter comparacaoAdapter;

    private Integer idUsuario = 1;
    private ProdutoAPI produtoApi;
    private OkHttpClient client;
    private Retrofit retrofit;

    private String credenciais = "";

    private static final int FRAGMENT_CONTAINER_ID = R.id.nav_host_fragment_activity_main;

    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000; // 60 segundos

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
        // ------------------------------------

        credenciais = Credentials.basic("nutria", "nutria123");

        // Configura OkHttpClient
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

        // Configura Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api-spring-mongodb.onrender.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        produtoApi = retrofit.create(ProdutoAPI.class);

        // --- Uso da função iniciandoServidor (A Boa Prática) ---
        // Chama o servidor para acordá-lo e, em seguida, busca os produtos.
        iniciandoServidor(() -> buscarProdutoDoUsuario(idUsuario));
        // -------------------------------------------------------


        // Simulação de transição (Removida ou mantida dependendo da sua necessidade, está comentada)
        /*
        if (botaoTesteTransicao != null) {
            botaoTesteTransicao.setOnClickListener(v -> {
                // ... lógica de transição simulada ...
            });
        }
        */

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
        if (btnEscolherTabelas != null) {
            btnEscolherTabelas.setOnClickListener(v -> {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, new ComparacaoParte2Fragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });
        }
    }

    /**
     * Tenta "acordar" o servidor de backend hospedado no Render (ou similar)
     * para evitar a latência do "cold start" na primeira chamada de API real.
     * Só executa o health check se a última tentativa for há mais de JANELA_WAKE_MS.
     * @param proximoPasso A ação a ser executada na UI thread após a tentativa de wake-up.
     */
    private void iniciandoServidor(Runnable proximoPasso) {
        long agora = System.currentTimeMillis();
        if (agora - ultimoWakeMs < JANELA_WAKE_MS) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }

        // Garante que o Fragment ainda está anexado antes de usar getActivity()
        if (getActivity() == null) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }

        new Thread(() -> {
            boolean ok = false;
            // O health check deve ser para o mesmo serviço que a API principal
            String healthCheckUrl = "https://api-spring-mongodb.onrender.com/actuator/health"; // Ajustado para o URL base do Retrofit

            for (int tent = 1; tent <= 3 && !ok; tent++) {
                try {
                    Request req = new Request.Builder()
                            .url(healthCheckUrl)
                            // A autorização pode ser necessária até para o health check dependendo da configuração
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                        Log.d("WakeUp", "Tentativa " + tent + ": " + (ok ? "SUCESSO" : "FALHA") + " | Código: " + (resp != null ? resp.code() : "N/A"));
                    }
                } catch (Exception e) {
                    Log.e("WakeUp", "Erro na tentativa " + tent + ": " + e.getMessage());
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            // Volta para a UI thread para executar o próximo passo (buscar dados)
            requireActivity().runOnUiThread(() -> { if (proximoPasso != null) proximoPasso.run(); });
        }).start();
    }

    private void buscarProdutoDoUsuario(Integer idUsuario) {
        // ... (Corpo da função mantido, agora é chamada após tentar acordar o servidor)
        produtoApi.buscarProdutosComMaisDeUmaTabela(idUsuario).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GetProdutoDTO>> call, @NonNull retrofit2.Response<List<GetProdutoDTO>> response) {
                if (getActivity() == null || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {

                    List<GetProdutoDTO> listaRetorno = response.body();

                    if (recyclerViewProdutos != null && !listaRetorno.isEmpty()) {

                        // --- Lógica de integração do Adapter ---
                        comparacaoAdapter = new ComparacaoAdapter(listaRetorno);

                        comparacaoAdapter.setOnItemClickListener(produto -> {
                            Log.d("Comparacao", "Produto selecionado: " + produto.getNome());

                            // Transição visual: Oculta a lista e exibe o item selecionado
                            textViewSelecionarProduto1.setVisibility(View.GONE);
                            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
                            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
                            iconeTabela.setVisibility(View.VISIBLE);
                            btnEscolherTabelas.setVisibility(View.VISIBLE);
                            demonstracaoItem2.setVisibility(View.VISIBLE);
                            textViewSelecionarProduto2.setVisibility(View.VISIBLE);

                            nomeProdutoSelecionado.setText(produto.getNome());
                            if (binding != null) {
                                binding.searchBar.setText(produto.getNome());
                            }
                        });

                        recyclerViewProdutos.setAdapter(comparacaoAdapter);

                        // 4. Mostrar a lista e ocultar a UI de "Escolha seu produto"
                        recyclerViewProdutos.setVisibility(View.VISIBLE);
                        demonstracaoItem1.setVisibility(View.GONE);
                        textViewSelecionarProduto1.setVisibility(View.GONE);
                        //botaoTesteTransicao.setVisibility(View.GONE);

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
        if (botaoTesteTransicao != null) {
            botaoTesteTransicao.setVisibility(View.VISIBLE);
        }
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