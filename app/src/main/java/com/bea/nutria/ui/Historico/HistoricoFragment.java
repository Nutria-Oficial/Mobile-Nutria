package com.bea.nutria.ui.Historico;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.R;
import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.databinding.FragmentHistoricoBinding;
import com.bea.nutria.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HistoricoFragment extends Fragment implements HistoricoAdapter.OnItemClickListener {

    private FragmentHistoricoBinding binding;
    private HistoricoAdapter adapter;
    // O Handler para postar no thread principal
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<ProdutoItem> produtos = new ArrayList<>();
    private int usuarioId = -1;

    // Constantes
    private static final String produtoUsuario = "https://api-spring-mongodb.onrender.com/produtos/usuario/%d?filtrar=false";
    private static final String BASE_URL_USUARIOS = "https://api-spring-aql.onrender.com/";
    // **IMPORTANTE**: Estas credenciais devem ser definidas ou carregadas de um local seguro!
    private static final String userAuth = "";
    private static final String passAuth = "";

    private UsuarioAPI usuarioAPI;

    // controle de loading
    private boolean isLoading = false;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle Methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoricoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoricoAdapter(new ArrayList<>(), this);
        binding.rvHistorico.setAdapter(adapter);

        // Configuração do OkHttpClient com interceptor para Basic Auth
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Authorization", Credentials.basic(userAuth, passAuth))
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(req);
                })
                .build();

        // Configuração do Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_USUARIOS)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        usuarioAPI = retrofit.create(UsuarioAPI.class);

        showLoading(true); // começa com spinner

        // Tenta carregar o ID do usuário das SharedPreferences
        usuarioId = prefs().getInt("usuario_id", -1);
        if (usuarioId > 0) {
            carregarProdutosUsuario(usuarioId);
        } else {
            // Se não tiver ID, resolve o ID pelo e-mail
            resolverUsuarioIdDepoisCarregarProdutos();
        }

        // Listener para a caixa de pesquisa
        binding.editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException
                if (binding == null || adapter == null) return;

                adapter.filtro(s == null ? "" : s.toString());
                toggleEmpty();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Garante que o binding seja nullificado para evitar vazamento de memória
        binding = null;
    }

    // ---------------------------------------------------------------------------------------------
    // Data Loading and User Resolution
    // ---------------------------------------------------------------------------------------------

    /**
     * Tenta obter o ID do usuário usando o e-mail através da API Retrofit.
     */
    private void resolverUsuarioIdDepoisCarregarProdutos() {
        // Tenta obter o email salvo ou do Firebase
        String email = prefs().getString("email", null);
        if (email == null) {
            try {
                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                    email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            } catch (Throwable ignore) {
                // Ignora exceções ao tentar obter o usuário do Firebase
            }
        }

        if (email == null || email.trim().isEmpty()) {
            mainHandler.post(() -> {
                showLoading(false);
                aplicarLista(new ArrayList<>()); // Lista vazia
            });
            return;
        }

        final String emailFinal = email.trim().toLowerCase(Locale.ROOT);

        usuarioAPI.buscarUsuario(emailFinal).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                // Garante que o fragmento ainda esteja ativo
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    Integer id = u.getId();

                    if (id != null && id > 0) {
                        prefs().edit().putInt("usuario_id", id).apply();
                        usuarioId = id;
                        // Agora que temos o ID, carregamos os produtos
                        carregarProdutosUsuario(usuarioId);
                        return; // Sai após iniciar o carregamento dos produtos
                    }
                }

                // Em caso de resposta não bem-sucedida ou ID inválido
                showLoading(false);
                aplicarLista(new ArrayList<>());
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                // Garante que o fragmento ainda esteja ativo
                if (!isAdded()) return;

                showLoading(false);
                aplicarLista(new ArrayList<>());
            }
        });
    }

    /**
     * Carrega a lista de produtos do usuário usando uma requisição HttpURLConnection em background.
     */
    private void carregarProdutosUsuario(int idUsuario) {
        showLoading(true);
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlStr = String.format(Locale.US, produtoUsuario, idUsuario);
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(30000);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("User-Agent", "Android"); // Boa prática

                // Configuração da Basic Auth
                String basic = userAuth + ":" + passAuth;
                String auth = "Basic " + Base64.encodeToString(basic.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", auth);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                String body = readAll(is);

                List<ProdutoItem> lista = new ArrayList<>();
                // 204 (No Content) ou 404 (Not Found) ou corpo vazio/nulo
                if (!(code == 204 || code == 404 || body == null || body.trim().isEmpty())) {
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o == null) continue;

                        // Tenta obter o ID como String (_id) e depois como int (id)
                        String idStr = o.optString("_id", null);
                        if (idStr == null || idStr.trim().isEmpty() || "null".equalsIgnoreCase(idStr)) {
                            int idNum = o.optInt("id", -1);
                            if (idNum > 0) idStr = String.valueOf(idNum);
                        }
                        if (idStr == null || idStr.trim().isEmpty()) continue;

                        String nome = o.optString("nome", "Produto sem nome");
                        lista.add(new ProdutoItem(idStr, nome));
                    }
                }

                // Posta o resultado no thread principal
                mainHandler.post(() -> {
                    showLoading(false);
                    aplicarLista(lista);
                });
            } catch (Exception e) {
                // Em caso de erro, posta lista vazia no thread principal
                mainHandler.post(() -> {
                    showLoading(false);
                    aplicarLista(new ArrayList<>());
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // ---------------------------------------------------------------------------------------------
    // UI/Helper Methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Aplica a lista de produtos carregados ao adapter e atualiza a UI.
     */
    private void aplicarLista(List<ProdutoItem> lista) {
        // VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException
        if (binding == null || adapter == null) return;

        produtos.clear();
        if (lista != null) produtos.addAll(lista);

        List<String> nomes = new ArrayList<>();
        for (ProdutoItem p : produtos) nomes.add(p.nome);

        adapter.submit(nomes, produtos);
        toggleEmpty();
    }

    /**
     * Alterna a visibilidade da RecyclerView e da mensagem de lista vazia.
     */
    private void toggleEmpty() {
        // VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException
        if (binding == null) return;

        if (isLoading) {
            // loading já controla visibilidades
            return;
        }

        boolean vazio = adapter == null || adapter.getItemCount() == 0;
        binding.rvHistorico.setVisibility(vazio ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(vazio ? View.VISIBLE : View.GONE);
    }

    /**
     * Exibe ou esconde o spinner de progresso.
     */
    private void showLoading(boolean show) {
        isLoading = show;
        if (binding == null) return;
        binding.progresso.setVisibility(show ? View.VISIBLE : View.GONE);
        // enquanto carrega, esconde lista e imagem vazia
        binding.rvHistorico.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(View.GONE);
    }

    /**
     * Lê todo o conteúdo de um InputStream para uma String.
     */
    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    /**
     * Obtém as SharedPreferences.
     */
    private android.content.SharedPreferences prefs() {
        return requireContext().getSharedPreferences("nutria_prefs", Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------------------------------------------
    // Interface Implementation (HistoricoAdapter.OnItemClickListener)
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onItemClick(int position) {
        // VERIFICAÇÃO DE SEGURANÇA
        if (binding == null || position < 0 || position >= produtos.size()) return;

        ProdutoItem produto = produtos.get(position);
        Bundle args = new Bundle();
        args.putString("idProduto", produto.id);
        args.putString("nomeProduto", produto.nome);

        // Navegação
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.action_navigation_historico_to_tabelaProdutoFragment, args);
    }

    // ---------------------------------------------------------------------------------------------
    // Internal Class
    // ---------------------------------------------------------------------------------------------

    /**
     * Classe interna para representar um item de produto.
     */
    static class ProdutoItem {
        String id;
        String nome;
        ProdutoItem(String id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }
}