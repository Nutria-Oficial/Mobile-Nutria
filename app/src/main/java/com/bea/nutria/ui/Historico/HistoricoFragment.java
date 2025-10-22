package com.bea.nutria.ui.Historico;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentHistoricoBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment implements HistoricoAdapter.OnItemClickListener {

    private FragmentHistoricoBinding binding;
    private HistoricoAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<ProdutoItem> produtos = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoricoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoricoAdapter(new ArrayList<>(), this);
        binding.rvHistorico.setAdapter(adapter);

        // Busca produtos do usuário
        carregarProdutosUsuario();

        // Filtro local
        binding.editPesquisar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filtro(s == null ? "" : s.toString());
                    toggleEmpty();
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void carregarProdutosUsuario() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api-spring-mongodb.onrender.com/produtos/usuario/1?filtrar=false");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(30000);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                if (code >= 200 && code < 300) {
                    parseProdutos(sb.toString());
                } else {
                    postToast("Erro HTTP " + code);
                }
            } catch (IOException e) {
                postToast("Falha de rede: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void parseProdutos(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            List<ProdutoItem> lista = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.optString("_id", "");
                String nome = obj.optString("nome", "Produto sem nome");
                lista.add(new ProdutoItem(id, nome));
            }
            mainHandler.post(() -> aplicarLista(lista));
        } catch (JSONException e) {
            postToast("Erro ao ler JSON: " + e.getMessage());
        }
    }

    private void aplicarLista(List<ProdutoItem> lista) {
        this.produtos = lista != null ? lista : new ArrayList<>();
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

    private void postToast(String msg) {
        mainHandler.post(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onItemClick(int position) {
        if (position < 0 || position >= produtos.size()) return;
        ProdutoItem produto = produtos.get(position);

        Bundle args = new Bundle();
        args.putString("idProduto", produto.id);

        Fragment destino = new TabelaProdutoFragment();
        destino.setArguments(args);

        FragmentTransaction ft = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.nav_host_fragment_content_main, destino);
        ft.addToBackStack(null);
        ft.commit();
    }

    static class ProdutoItem {
        String id;
        String nome;
        ProdutoItem(String id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
