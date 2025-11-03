package com.bea.nutria.ui.Comparacao;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.model.GetTabelaComparacaoDTO;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparacaoParte2Fragment extends Fragment implements TabelaAdapter.OnTabelaClickListener {

    private static final String KEY_TEXT_MUDADO = "text_mudado";
    private static final String KEY_ITEMS_VISIVEL = "items_visivel";
    private static final String KEY_BUTTON_VISIVEL = "button_visivel";
    private static final String TAG = "ComparacaoP2Fragment";

    private static final String ARG_PRODUTO_ID = "produto_id";
    private Integer produtoId;

    private TabelaAPI tabelaApi;
    private ProdutoAPI produtoApi;

    private RecyclerView recyclerViewTabelas;
    private TabelaAdapter tabelaAdapter;

    private TextView subtitulo;
    private View listaItensPrincipalContainer;
    private ConstraintLayout headerItem0;
    private ConstraintLayout headerItem1;
    private TextView nomeTabela1;
    private TextView nomeTabela2;
    private MaterialButton botaoComparar;

    private ProgressBar progressBarLoading;

    private ImageButton seta1;
    private ImageButton seta2;
    private ConstraintLayout conteudoExpansivel1;
    private ConstraintLayout conteudoExpansivel2;

    private ImageView iconSelecionado1;
    private ImageView iconSelecionado2;

    private boolean isTabela1Expanded = false;
    private boolean isTabela2Expanded = false;

    private GetTabelaComparacaoDTO tabelaSelecionada1 = null;
    private GetTabelaComparacaoDTO tabelaSelecionada2 = null;

    private static final String url = "https://api-spring-mongodb.onrender.com/";

    private static final int FRAGMENT_CONTAINER_ID = R.id.nav_host_fragment_activity_main;

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

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        ConexaoAPI apiManager = new ConexaoAPI(url);
        tabelaApi = apiManager.getApi(TabelaAPI.class);
        produtoApi = apiManager.getApi(ProdutoAPI.class);

        progressBarLoading = view.findViewById(R.id.progress_bar_loading2);

        view.findViewById(R.id.voltar).setOnClickListener(v -> requireActivity().onBackPressed());

        subtitulo = view.findViewById(R.id.textViewSelecionarProduto3);
        listaItensPrincipalContainer = view.findViewById(R.id.listaItensPrincipal);
        botaoComparar = view.findViewById(R.id.btn_comparar);

        headerItem0 = view.findViewById(R.id.header_item);
        headerItem1 = view.findViewById(R.id.header_item1);

        conteudoExpansivel1 = view.findViewById(R.id.conteudo_expansivel_tabela1);
        conteudoExpansivel2 = view.findViewById(R.id.conteudo_expansivel_tabela2);

        if (headerItem0 != null) {
            nomeTabela1 = headerItem0.findViewById(R.id.textViewTitulo);
            seta1 = headerItem0.findViewById(R.id.imageButtonSeta);
            iconSelecionado1 = headerItem0.findViewById(R.id.icon_selecionado1);
        }

        if (headerItem1 != null) {
            nomeTabela2 = headerItem1.findViewById(R.id.textViewTitulo3);

            int seta2Id = getResources().getIdentifier("imageButtonSeta2", "id", requireContext().getPackageName());
            if (seta2Id != 0) {
                seta2 = headerItem1.findViewById(seta2Id);
            } else {
                Log.e(TAG, "ID 'imageButtonSeta2' não encontrado. Verifique se o XML foi atualizado.");
                seta2 = headerItem1.findViewById(R.id.imageButtonSeta);
            }

            iconSelecionado2 = headerItem1.findViewById(R.id.icon_selecionado2);
        }

        if (headerItem0 != null) {
            headerItem0.setOnClickListener(v -> {
                hideKeyboard();
            });
        }

        if (headerItem1 != null) {
            headerItem1.setOnClickListener(v -> {
                hideKeyboard();
            });
        }

        if (listaItensPrincipalContainer != null) {
            recyclerViewTabelas = listaItensPrincipalContainer.findViewById(R.id.recyclerViewTabelas);
            if (recyclerViewTabelas != null) {
                recyclerViewTabelas.setLayoutManager(new LinearLayoutManager(getContext()));
            } else {
                Log.e(TAG, "Erro: RecyclerView não encontrada. Verifique o ID 'recyclerViewTabelas'.");
            }
        }

        if (produtoId != null && produtoId != -1) {

            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.VISIBLE);
            }
            if (listaItensPrincipalContainer != null) {
                listaItensPrincipalContainer.setVisibility(View.GONE);
            }

            apiManager.iniciarServidor(requireActivity(), () -> buscarTodasTabelasDoProduto(produtoId));
        } else {
            Toast.makeText(getContext(), "Erro: ID do produto inválido.", Toast.LENGTH_LONG).show();
            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.GONE);
            }
        }

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
            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.GONE);
            }
        } else {
            atualizarUISelecao();
        }

        if (botaoComparar != null) {
            botaoComparar.setOnClickListener(v -> {
                if (tabelaSelecionada1 != null && tabelaSelecionada2 != null) {

                    Fragment nextFragment = ComparacaoParte3Fragment.newInstance(
                            tabelaSelecionada1.getTabelaId(),
                            tabelaSelecionada2.getTabelaId(),
                            corrigirTextoCodificado(tabelaSelecionada1.getNomeTabela()),
                            corrigirTextoCodificado(tabelaSelecionada2.getNomeTabela())
                    );

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, nextFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                } else {
                    Toast.makeText(getContext(), "Selecione duas tabelas para comparar.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewTabelas = null;
        tabelaAdapter = null;
        subtitulo = null;
        listaItensPrincipalContainer = null;
        headerItem0 = null;
        headerItem1 = null;
        nomeTabela1 = null;
        nomeTabela2 = null;
        botaoComparar = null;
        progressBarLoading = null;
        seta1 = null;
        seta2 = null;
        conteudoExpansivel1 = null;
        conteudoExpansivel2 = null;
        iconSelecionado1 = null;
        iconSelecionado2 = null;
    }

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

        if (seta != null) {
            seta.animate().rotation(isExpanded ? 180f : 0f).setDuration(200).start();
        }
    }

    private void buscarTodasTabelasDoProduto(Integer produtoId) {
        if (produtoApi == null) {
            Log.e(TAG, "ProdutoAPI não inicializada.");
            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.GONE);
            }
            return;
        }

        produtoApi.buscarTodasTabelasDoProduto(produtoId).enqueue(new Callback<List<GetTabelaComparacaoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<GetTabelaComparacaoDTO>> call, @NonNull Response<List<GetTabelaComparacaoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetTabelaComparacaoDTO> listaTabelas = response.body();

                    if (!listaTabelas.isEmpty()) {
                        setupAdapter(listaTabelas);
                    } else {
                        Toast.makeText(getContext(), "Nenhuma tabela encontrada para este produto.", Toast.LENGTH_LONG).show();
                        if (listaItensPrincipalContainer != null)
                            listaItensPrincipalContainer.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Erro ao buscar tabelas: Código " + response.code());
                    Toast.makeText(getContext(), "Erro na resposta do servidor ao buscar tabelas.", Toast.LENGTH_LONG).show();
                    if (listaItensPrincipalContainer != null)
                        listaItensPrincipalContainer.setVisibility(View.GONE);
                }

                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetTabelaComparacaoDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha de conexão: " + t.getMessage());
                Toast.makeText(getContext(), "Falha ao conectar-se à API para buscar tabelas.", Toast.LENGTH_LONG).show();
                if (listaItensPrincipalContainer != null)
                    listaItensPrincipalContainer.setVisibility(View.GONE);

                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupAdapter(List<GetTabelaComparacaoDTO> listaTabelas) {
        if (!isAdded() || getContext() == null || recyclerViewTabelas == null) return;

        tabelaAdapter = new TabelaAdapter(listaTabelas);
        tabelaAdapter.setOnTabelaClickListener(ComparacaoParte2Fragment.this);

        recyclerViewTabelas.setAdapter(tabelaAdapter);
        recyclerViewTabelas.setVisibility(View.VISIBLE);

        if (listaItensPrincipalContainer != null) {
            listaItensPrincipalContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEscolherTabelaClick(GetTabelaComparacaoDTO tabela, int position) {

        boolean itemRemovidoDaListaVisivel = false;
        GetTabelaComparacaoDTO tabelaParaReAdicionar = null;

        hideKeyboard();

        if (tabela.getTabelaId() == null) {
            Toast.makeText(getContext(), "Erro: Tabela sem ID.", Toast.LENGTH_LONG).show();
            return;
        }

        if (tabelaSelecionada1 != null && tabela.getTabelaId().equals(tabelaSelecionada1.getTabelaId())) {
            tabelaParaReAdicionar = tabelaSelecionada1;

            if (tabelaSelecionada2 != null) {
                tabelaSelecionada1 = tabelaSelecionada2;
                tabelaSelecionada2 = null;
            } else {
                tabelaSelecionada1 = null;
            }
            Toast.makeText(getContext(), tabelaParaReAdicionar.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();

        } else if (tabelaSelecionada2 != null && tabela.getTabelaId().equals(tabelaSelecionada2.getTabelaId())) {
            tabelaParaReAdicionar = tabelaSelecionada2;
            tabelaSelecionada2 = null;
            Toast.makeText(getContext(), tabelaParaReAdicionar.getNomeTabela() + " deselecionada.", Toast.LENGTH_SHORT).show();

        } else if (tabelaSelecionada1 == null) {
            tabelaSelecionada1 = tabela;
            itemRemovidoDaListaVisivel = true;

        } else if (tabelaSelecionada2 == null) {
            tabelaSelecionada2 = tabela;
            itemRemovidoDaListaVisivel = true;

        } else {
            Toast.makeText(getContext(), "Máximo de duas tabelas selecionadas.", Toast.LENGTH_LONG).show();
            return;
        }

        if (tabelaAdapter != null) {
            if (tabelaParaReAdicionar != null) {
                tabelaAdapter.addItem(tabelaParaReAdicionar);
            } else if (itemRemovidoDaListaVisivel) {
                tabelaAdapter.removeItem(position);
            }

            tabelaAdapter.notifyDataSetChanged();

            if (recyclerViewTabelas != null) {
                recyclerViewTabelas.scrollToPosition(0);
            }
        }

        atualizarUISelecao();
    }

    private void atualizarUISelecao() {
        isTabela1Expanded = false;
        isTabela2Expanded = false;
        if (conteudoExpansivel1 != null) conteudoExpansivel1.setVisibility(View.GONE);
        if (conteudoExpansivel2 != null) conteudoExpansivel2.setVisibility(View.GONE);
        if (seta1 != null) seta1.setRotation(0);
        if (seta2 != null) seta2.setRotation(0);

        if (tabelaSelecionada1 != null) {
            if (nomeTabela1 != null) nomeTabela1.setText(tabelaSelecionada1.getNomeTabela());
            if (iconSelecionado1 != null) iconSelecionado1.setVisibility(View.VISIBLE);
        } else {
            if (nomeTabela1 != null) nomeTabela1.setText("");
            if (iconSelecionado1 != null) iconSelecionado1.setVisibility(View.GONE);
        }

        if (tabelaSelecionada2 != null) {
            if (nomeTabela2 != null) nomeTabela2.setText(tabelaSelecionada2.getNomeTabela());
            if (iconSelecionado2 != null) iconSelecionado2.setVisibility(View.VISIBLE);
        } else {
            if (nomeTabela2 != null) nomeTabela2.setText("");
            if (iconSelecionado2 != null) iconSelecionado2.setVisibility(View.GONE);
        }

        if (tabelaSelecionada1 != null && tabelaSelecionada2 != null) {
            subtitulo.setText("Hora de comparar suas tabelas!");
            if (headerItem0 != null) headerItem0.setVisibility(View.VISIBLE);
            if (headerItem1 != null) headerItem1.setVisibility(View.VISIBLE);
            if (botaoComparar != null) botaoComparar.setVisibility(View.VISIBLE);

            if (recyclerViewTabelas != null) recyclerViewTabelas.setVisibility(View.GONE);
            if (listaItensPrincipalContainer != null)
                listaItensPrincipalContainer.setVisibility(View.GONE);

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
        outState.putBoolean(KEY_TEXT_MUDADO, subtitulo != null && subtitulo.getText().toString().contains("comparar"));
        outState.putBoolean(KEY_ITEMS_VISIVEL, headerItem0 != null && headerItem0.getVisibility() == View.VISIBLE);
        outState.putBoolean(KEY_BUTTON_VISIVEL, botaoComparar != null && botaoComparar.getVisibility() == View.VISIBLE);
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            View currentFocus = getActivity().getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }
        }
    }

    private String corrigirTextoCodificado(String texto) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean houveCorrecao = false;

            for (int i = 0; i < texto.length();) {
                char c = texto.charAt(i);

                if (c == '\\' && i + 3 < texto.length() && texto.charAt(i + 1) == 'x') {
                    String hex = texto.substring(i + 2, i + 4);
                    try {
                        int valor = Integer.parseInt(hex, 16);
                        out.write(valor);
                        i += 4;
                        houveCorrecao = true;
                    } catch (NumberFormatException e) {
                        out.write((byte) c);
                        i++;
                    }
                } else {
                    out.write((byte) c);
                    i++;
                }
            }

            if (!houveCorrecao) {
                return texto;
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return texto;
        }
    }
}