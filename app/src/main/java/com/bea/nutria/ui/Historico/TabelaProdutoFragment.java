package com.bea.nutria.ui.Historico;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bea.nutria.R;
import com.bea.nutria.model.Linha;
import com.bea.nutria.model.Tabela;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TabelaProdutoFragment extends Fragment {

    public TabelaProdutoFragment() {
        super(R.layout.fragment_tabela_produto);
    }

    private ViewPager2 pager;
    private TabLayout tabDots;
    private TextView tvResumoAvaliacao;
    private TextView tvTitulo;
    private TextView tvPorcaoTopo;

    private TabelasPagerAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String idProduto;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager = view.findViewById(R.id.pagerTabelas);
        tabDots = view.findViewById(R.id.tabDots);
        tvResumoAvaliacao = view.findViewById(R.id.tvResumoAvaliacao);
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvPorcaoTopo = view.findViewById(R.id.tvPorcao);

        adapter = new TabelasPagerAdapter(
                requireContext(),
                new ArrayList<>(),
                () -> Toast.makeText(requireContext(), "Adicionar nova tabela", Toast.LENGTH_SHORT).show()
        );
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(1);

        new TabLayoutMediator(tabDots, pager, (tab, position) -> { /* sem texto nos tabs */ }).attach();

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                atualizarAvaliacao(position);
            }
        });

        // Pega o id do produto via arguments
        if (getArguments() != null) {
            idProduto = getArguments().getString("idProduto");
        }
        if (idProduto == null || idProduto.isEmpty()) {
            Toast.makeText(requireContext(), "Produto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Busca sem Retrofit
        carregarProdutoSemRetrofit(idProduto);
    }

    private void carregarProdutoSemRetrofit(String id) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String base = "https://api-spring-mongodb.onrender.com/produtos/";
                String urlStr = base + URLEncoder.encode(id, "UTF-8");
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? new BufferedInputStream(conn.getInputStream())
                        : new BufferedInputStream(conn.getErrorStream());

                String body = readAll(is);
                if (code < 200 || code >= 300) {
                    postToast("Erro HTTP " + code);
                    return;
                }

                JSONObject json = new JSONObject(body);
                ProdutoDTO produto = parseProduto(json);

                mainHandler.post(() -> aplicarProdutoNaUI(produto));

            } catch (IOException | JSONException e) {
                postToast("Falha: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void aplicarProdutoNaUI(ProdutoDTO p) {
        if (p == null) {
            Toast.makeText(requireContext(), "Resposta inválida do servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        tvTitulo.setText(p.nome != null ? p.nome : "Produto");
        if (p.porcaoTopo != null && !p.porcaoTopo.isEmpty()) {
            tvPorcaoTopo.setText(p.porcaoTopo);
        }

        adapter.submit(p.tabelas);

        // Ajusta avaliação inicial: se não tem tabela, cai no “ADD”
        atualizarAvaliacao(0);
    }

    private void atualizarAvaliacao(int position) {
        if (position >= adapter.getRealCount()) {
            tvResumoAvaliacao.setText("Pronto para adicionar uma nova tabela nutricional?");
            return;
        }

        // TODO: se quiser avaliação específica por tabela, gere aqui com base nas linhas da tabela atual.
        String avaliacao = "Veja a avaliação resumida dividida em pontos bons e ruins (0 a 100):\n\n" +
                "✅ Pontos bons:\n" +
                "Proteínas (6,2g) – 75: Boa quantidade para lanches.\n" +
                "Cálcio (243mg) – 85: Ótima fonte.\n" +
                "Gorduras Totais (0,7g) – 90: Baixíssimo teor.\n" +
                "Sódio (124mg) – 80: Valor controlado.\n" +
                "Açúcares adicionados (2g) – 70: Moderado.\n" +
                "Valor energético (123 kcal) – 80: Calorias equilibradas.\n\n" +
                "⚠️ Pontos ruins:\n" +
                "Gorduras saturadas (24g) – 10: Muito alto.\n" +
                "Gorduras trans (0,3g) – 20: Deveria ser zero.\n" +
                "Açúcares totais (24g) – 25: Quase no limite diário.\n" +
                "Fibra alimentar (1g) – 30: Baixa quantidade.\n\n" +
                "📈 O que pode melhorar:\n" +
                "Reduzir gorduras saturadas e trans.\n" +
                "Diminuir açúcares totais.\n" +
                "Aumentar fibras.\n" +
                "Manter os bons níveis de proteínas, cálcio e gorduras totais.";
        tvResumoAvaliacao.setText(avaliacao);
    }

    // ---------- Helpers de rede/JSON ----------

    private void postToast(String msg) {
        mainHandler.post(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private ProdutoDTO parseProduto(JSONObject json) throws JSONException {
        ProdutoDTO dto = new ProdutoDTO();
        dto.id = json.optString("_id", "");
        dto.nome = json.optString("nome", "");
        // se existir uma porção geral do produto
        dto.porcaoTopo = json.optString("porcao", "");

        JSONArray arrTabelas = json.optJSONArray("tabelas");
        dto.tabelas = new ArrayList<>();

        if (arrTabelas != null) {
            for (int i = 0; i < arrTabelas.length(); i++) {
                JSONObject jt = arrTabelas.optJSONObject(i);
                if (jt == null) continue;

                String titulo = jt.optString("titulo", "Tabela Nutricional");
                String porcaoTexto = jt.optString("porcaoTexto", "Porção");
                long idEstavel = jt.optLong("idEstavel", criarIdEstavelHeuristico(dto.id, i));

                JSONArray arrLinhas = jt.optJSONArray("linhas");
                List<Linha> linhas = new ArrayList<>();
                if (arrLinhas != null) {
                    for (int k = 0; k < arrLinhas.length(); k++) {
                        JSONObject jl = arrLinhas.optJSONObject(k);
                        if (jl == null) continue;
                        String nome = jl.optString("nome", "");
                        String valor = jl.optString("valor", "");
                        String vd = jl.optString("vd", "");
                        linhas.add(new Linha(nome, valor, vd));
                    }
                }

                dto.tabelas.add(new Tabela(titulo, porcaoTexto, linhas, idEstavel));
            }
        }

        return dto;
    }

    private long criarIdEstavelHeuristico(String produtoId, int indexTabela) {
        // gera um id estável quando backend não mandar. Qualquer heurística determinística serve.
        long base = produtoId != null ? produtoId.hashCode() : 0;
        return (base & 0x7FFFFFFFL) * 31L + indexTabela;
    }

    // DTO interno só para transporte temporário
    static class ProdutoDTO {
        String id;
        String nome;
        String porcaoTopo;
        List<Tabela> tabelas;
    }
}
