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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<ProdutoItem> produtos = new ArrayList<>();
    private int usuarioId = -1;

    private static final String produtoUsuario = "https://api-spring-mongodb.onrender.com/produtos/usuario/%d?filtrar=false";
    private static final String BASE_URL_USUARIOS = "https://api-spring-aql.onrender.com/";
    private static final String userAuth = "nutria";
    private static final String passAuth = "nutria123";

    private UsuarioAPI usuarioAPI;

    private boolean isLoading = false;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_USUARIOS)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        usuarioAPI = retrofit.create(UsuarioAPI.class);

        showLoading(true);

        String emailAtual = getCurrentEmail();
        if (emailAtual == null) {
            showLoading(false);
            aplicarLista(new ArrayList<>());
            return;
        }

        resolverUsuarioIdDepoisCarregarProdutos(emailAtual.toLowerCase(Locale.ROOT));

        binding.editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filtro(s == null ? "" : s.toString());
                    toggleEmpty();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void resolverUsuarioIdDepoisCarregarProdutos(@NonNull String emailFinal) {
        usuarioAPI.buscarUsuario(emailFinal).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    Integer id = u.getId();

                    android.util.Log.d("Historico", "API usuario por email=" + emailFinal + " => id=" + id);

                    if (id != null && id > 0) {
                        prefs().edit()
                                .putString("email", emailFinal)
                                .putInt("usuario_id", id)
                                .apply();

                        usuarioId = id;
                        carregarProdutosUsuario(usuarioId);
                    } else {
                        showLoading(false);
                        aplicarLista(new ArrayList<>());
                    }
                } else {
                    showLoading(false);
                    aplicarLista(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                aplicarLista(new ArrayList<>());
            }
        });
    }

    private String getCurrentEmail() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String e = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                return (e != null && !e.trim().isEmpty()) ? e.trim() : null;
            }
        } catch (Throwable ignore) {}
        return null;
    }





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
                String basic = userAuth + ":" + passAuth;
                String auth = "Basic " + Base64.encodeToString(basic.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", auth);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                String body = readAll(is);

                List<ProdutoItem> lista = new ArrayList<>();
                if (!(code == 204 || code == 404 || body == null || body.trim().isEmpty())) {
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o == null) continue;
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

                mainHandler.post(() -> {
                    showLoading(false);
                    aplicarLista(lista);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    aplicarLista(new ArrayList<>());
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void aplicarLista(List<ProdutoItem> lista) {
        produtos.clear();
        if (lista != null) produtos.addAll(lista);
        List<String> nomes = new ArrayList<>();
        for (ProdutoItem p : produtos) nomes.add(p.nome);
        adapter.submit(nomes, produtos);
        toggleEmpty();
    }

    private void toggleEmpty() {
        if (binding == null) return;
        if (isLoading) {
            return;
        }
        boolean vazio = adapter == null || adapter.getItemCount() == 0;
        binding.rvHistorico.setVisibility(vazio ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(vazio ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean show) {
        isLoading = show;
        if (binding == null) return;
        binding.progresso.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvHistorico.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(View.GONE);
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private android.content.SharedPreferences prefs() {
        return requireContext().getSharedPreferences("nutria_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void onItemClick(int position) {
        if (position < 0 || position >= produtos.size()) return;
        ProdutoItem produto = produtos.get(position);
        Bundle args = new Bundle();
        args.putString("idProduto", produto.id);
        args.putString("nomeProduto", produto.nome);
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.action_navigation_historico_to_tabelaProdutoFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class ProdutoItem {
        String id;
        String nome;
        ProdutoItem(String id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }
}