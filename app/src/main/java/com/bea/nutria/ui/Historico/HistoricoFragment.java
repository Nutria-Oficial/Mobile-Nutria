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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentHistoricoBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment implements HistoricoAdapter.OnItemClickListener {

    private FragmentHistoricoBinding binding;
    private HistoricoAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<ProdutoItem> produtos = new ArrayList<>();
    private int usuarioId = -1;

    // === MODO TESTE: force carregar produtos do usuário 1 ===
    private static final boolean FORCE_TEST_USER = true; // <<< DESLIGUE DEPOIS
    private static final int TEST_USER_ID = 1;

    // Endpoint de usuário por e-mail (ajuste se necessário)
    private static final String usuarioEmail = "https://api-spring-aql.onrender.com/usuarios/email/%s";
    // Endpoint de produtos por usuário
    private static final String produtoUsuario = "https://api-spring-mongodb.onrender.com/produtos/usuario/%d?filtrar=false";
    // Basic Auth, se o backend exigir
    private static final String userAuth = "nutria";
    private static final String passAuth = "nutria123";

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

        // Modo teste: ignora tudo e usa o ID 1
        if (FORCE_TEST_USER) {
            carregarProdutosUsuario(TEST_USER_ID);
        } else {
            // Fluxo normal: tenta pegar o id salvo, senão resolve por e-mail
            usuarioId = prefs().getInt("usuario_id", -1);
            if (usuarioId > 0) {
                carregarProdutosUsuario(usuarioId);
            } else {
                resolverUsuarioIdDepoisCarregarProdutos();
            }
        }

        // Filtro local
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

    private void resolverUsuarioIdDepoisCarregarProdutos() {
        String email = prefs().getString("email", null);
        if (email == null) {
            try {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                }
            } catch (Throwable ignore) {}
        }

        if (email == null || email.trim().isEmpty()) {
            // Sem e-mail: estado vazio
            mainHandler.post(() -> aplicarLista(new ArrayList<>()));
            return;
        }

        final String emailFinal = email.trim().toLowerCase(Locale.ROOT);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String encodedEmail = URLEncoder.encode(emailFinal, "UTF-8");
                String urlStr = String.format(Locale.US, usuarioEmail, encodedEmail);

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
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String body = readAll(is);
                if (code < 200 || code >= 300) {
                    mainHandler.post(() -> aplicarLista(new ArrayList<>()));
                    return;
                }

                JSONObject obj = new JSONObject(body);
                int id = obj.optInt("id", -1);
                if (id <= 0) {
                    JSONObject data = obj.optJSONObject("data");
                    if (data != null) id = data.optInt("id", -1);
                }

                if (id <= 0) {
                    mainHandler.post(() -> aplicarLista(new ArrayList<>()));
                    return;
                }

                final int resolvedId = id;
                prefs().edit().putInt("usuario_id", resolvedId).apply();

                mainHandler.post(() -> {
                    usuarioId = resolvedId;
                    carregarProdutosUsuario(usuarioId);
                });

            } catch (Exception e) {
                mainHandler.post(() -> aplicarLista(new ArrayList<>()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void carregarProdutosUsuario(int idUsuario) {
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

                // >>>>>>>>>> ADICIONE A AUTENTICAÇÃO SE O BACKEND EXIGE <<<<<<<<<<
                String basic = userAuth + ":" + passAuth;
                String auth = "Basic " + Base64.encodeToString(basic.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", auth);

                int code = conn.getResponseCode();

                // Debug opcional para você ver o que está rolando
                InputStream isDbg = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String bodyDbg = readAll(isDbg);
                android.util.Log.d("Historico", "GET produtos -> code=" + code + " body.head=" +
                        (bodyDbg == null ? "null" : bodyDbg.substring(0, Math.min(200, bodyDbg.length()))));
                // Reabre o stream porque já consumimos (ou reaproveite bodyDbg)
                // Como eu já li o body em bodyDbg, vou reutilizar ele abaixo em vez de ler de novo.

                if (code == 204 || code == 404) {
                    mainHandler.post(() -> aplicarLista(new ArrayList<>()));
                    return;
                }
                if (code < 200 || code >= 300) {
                    mainHandler.post(() -> aplicarLista(new ArrayList<>()));
                    return;
                }

                String body = bodyDbg;
                if (body == null || body.trim().isEmpty() || body.trim().equalsIgnoreCase("null")) {
                    mainHandler.post(() -> aplicarLista(new ArrayList<>()));
                    return;
                }

                List<ProdutoItem> lista = new ArrayList<>();
                try {
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o == null) continue;
                        String id = o.optString("_id", "");
                        String nome = o.optString("nome", "Produto sem nome");
                        lista.add(new ProdutoItem(id, nome));
                    }
                } catch (Exception ignore) {
                    lista.clear();
                }

                mainHandler.post(() -> aplicarLista(lista));
            } catch (Exception e) {
                android.util.Log.e("Historico", "Falha rede produtos: " + e.getMessage(), e);
                mainHandler.post(() -> aplicarLista(new ArrayList<>()));
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
        boolean vazio = adapter == null || adapter.getItemCount() == 0;
        binding.rvHistorico.setVisibility(vazio ? View.GONE : View.VISIBLE);
        binding.triaSemHistorico.setVisibility(vazio ? View.VISIBLE : View.GONE);
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
        ProdutoItem(String id, String nome) { this.id = id; this.nome = nome; }
    }
}
