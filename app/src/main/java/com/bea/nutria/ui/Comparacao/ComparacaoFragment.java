package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.ProdutoAPI;
import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.databinding.FragmentComparacaoBinding;
import com.bea.nutria.model.GetProdutoDTO;
import com.bea.nutria.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

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

    private EditText searchBar;

    private ProgressBar progressBarLoading;

    private RecyclerView recyclerViewProdutos;
    private ComparacaoAdapter comparacaoAdapter;

    private static final String TAG = "ComparacaoP1Fragment";

    private ConexaoAPI conexaoAPIUsuario;

    private UsuarioAPI usuarioAPI;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int usuarioId = -1;

    private Integer produtoSelecionadoId = null;

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

        apiManager = new ConexaoAPI(url);
        produtoApi = apiManager.getApi(ProdutoAPI.class);
        conexaoAPIUsuario = new ConexaoAPI("https://api-spring-aql.onrender.com/");
        usuarioAPI = conexaoAPIUsuario.getApi(UsuarioAPI.class);

        usuarioId = prefs().getInt("usuario_id", -1);
        if (usuarioId < 0) {
            resolverUsuarioId();
        }

        demonstracaoItem1 = view.findViewById(R.id.View_demonstracaoItem_1);
        textViewSelecionarProduto1 = view.findViewById(R.id.textViewSelecionarProduto1);
        demonstracaoItemSelecionado = view.findViewById(R.id.View_demonstracaoItem_selecionado);
        nomeProdutoSelecionado = view.findViewById(R.id.textViewNomeProdutoSelecionado);
        textViewSelecionarProduto2 = view.findViewById(R.id.textViewSelecionarProduto2);
        iconeTabela = view.findViewById(R.id.imageViewIconeTabela);
        btnEscolherTabelas = view.findViewById(R.id.btn_escolherTabelas);

        progressBarLoading = view.findViewById(R.id.progress_bar_loading);

        searchBar = view.findViewById(R.id.search_bar);

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

        progressBarLoading.setVisibility(View.VISIBLE);
        apiManager.iniciarServidor(requireActivity(), () -> buscarProdutoDoUsuario(usuarioId));

        setupSearchFunctionality();

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View currentFocus = requireActivity().getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    Rect outRect = new Rect();
                    currentFocus.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        currentFocus.clearFocus();
                        hideKeyboard();
                    }
                }
            }
            return false;
        });

        if (btnEscolherTabelas != null) {
            btnEscolherTabelas.setOnClickListener(v -> {
                if (produtoSelecionadoId != null) {

                    if (searchBar != null) {
                        searchBar.clearFocus();
                    }
                    hideKeyboard();

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    ComparacaoParte2Fragment nextFragment =
                            ComparacaoParte2Fragment.newInstance(produtoSelecionadoId);

                    fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, nextFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    Log.w("ComparacaoFragment", "Tentativa de transição sem produto selecionado.");
                }
            });
        }
    }

    private void setupSearchFunctionality() {
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (comparacaoAdapter != null) {
                        comparacaoAdapter.filter(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
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

                        comparacaoAdapter = new ComparacaoAdapter(listaRetorno);

                        comparacaoAdapter.setOnItemClickListener(produto -> {
                            Log.d("Comparacao", "Produto selecionado: " + produto.getNome());

                            hideKeyboard();

                            if (comparacaoAdapter != null) {

                                if (produtoRemovidoAnteriormente != null) {
                                    comparacaoAdapter.addItem(produtoRemovidoAnteriormente);
                                    produtoRemovidoAnteriormente = null;
                                }

                                comparacaoAdapter.removeItem(produto);
                                produtoRemovidoAnteriormente = produto;
                            }

                            produtoSelecionadoId = produto.getId();

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

                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetProdutoDTO>> call, @NonNull Throwable t) {
                Log.e("API:", "Falha na requisição: " + t.getMessage());
                if (getActivity() != null) {
                    if (progressBarLoading != null) {
                        progressBarLoading.setVisibility(View.GONE);
                    }
                    getActivity().runOnUiThread(ComparacaoFragment.this::tratarListaVaziaOuErro);
                }
            }
        });
    }

    private void tratarListaVaziaOuErro() {
        if (recyclerViewProdutos != null) {
            recyclerViewProdutos.setVisibility(View.GONE);
        }
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);
        if (botaoTesteTransicao != null) {
            botaoTesteTransicao.setVisibility(View.VISIBLE);
        }
    }

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

    private void resetEstado() {
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);

        demonstracaoItemSelecionado.setVisibility(View.GONE);
        nomeProdutoSelecionado.setVisibility(View.GONE);
        iconeTabela.setVisibility(View.GONE);
        btnEscolherTabelas.setVisibility(View.GONE);
        textViewSelecionarProduto2.setVisibility(View.GONE);

        produtoSelecionadoId = null;

        if (comparacaoAdapter != null && produtoRemovidoAnteriormente != null) {
            comparacaoAdapter.addItem(produtoRemovidoAnteriormente);
        }
        produtoRemovidoAnteriormente = null;

        if (recyclerViewProdutos != null) {
            recyclerViewProdutos.setVisibility(View.VISIBLE);
        }

        nomeProdutoSelecionado.setText("");

        if (searchBar != null) {
            searchBar.setText("");
        }

        hideKeyboard();
    }

    private void resolverUsuarioId() {
        String email = prefs().getString("email", null);
        if (email == null) {
            try {
                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                    email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            } catch (Throwable ignore) {}
        }

        if (email == null || email.trim().isEmpty()) {
            mainHandler.post(() -> {
                Toast.makeText(getContext(),
                        "Não foi possível identificar o usuário logado",
                        Toast.LENGTH_LONG).show();
            });
            return;
        }

        final String emailFinal = email.trim().toLowerCase(Locale.ROOT);

        usuarioAPI.buscarUsuario(emailFinal).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    Integer id = u.getId();

                    if (id != null && id > 0) {
                        prefs().edit().putInt("usuario_id", id).apply();
                        usuarioId = id;
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Usuário encontrado sem ID válido",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Falha de conexão: " + (t.getMessage() == null ? "desconhecida" : t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private android.content.SharedPreferences prefs() {
        return requireContext().getSharedPreferences("nutria_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}