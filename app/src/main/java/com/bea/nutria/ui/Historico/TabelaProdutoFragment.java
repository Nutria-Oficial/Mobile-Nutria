package com.bea.nutria.ui.Historico;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bea.nutria.R;
import com.bea.nutria.model.Linha;
import com.bea.nutria.model.Tabela;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabelaProdutoFragment extends Fragment {

    public TabelaProdutoFragment() { super(R.layout.fragment_tabela_produto); }

    private ViewPager2 pager;
    private TabLayout tabDots;
    private TextView tvResumoAvaliacao;
    private TextView tvTitulo;
    private View loadingOverlay;
    private View cardConteudo;

    private TabelasPagerAdapter pagerAdapter;
    private final List<String> avaliacoes = new ArrayList<>();

    private String idProduto;
    private String nomeProduto;

    private static final String urlBase = "https://api-spring-mongodb.onrender.com/produtos/";
    private static final String user = "nutria";
    private static final String password = "nutria123";

    private final Locale LOCALE_PTBR = new Locale("pt", "BR"); //segue o padrao br
    private final DecimalFormat DF2 = (DecimalFormat) NumberFormat.getNumberInstance(LOCALE_PTBR);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DF2.applyPattern("#0.00"); //todo numero com 2 casas

        pager= view.findViewById(R.id.pagerTabelas);
        tabDots= view.findViewById(R.id.tabDots);
        tvResumoAvaliacao= view.findViewById(R.id.tvResumoAvaliacao);
        tvTitulo= view.findViewById(R.id.tvTitulo);
        loadingOverlay    = view.findViewById(R.id.loadingOverlay);
        cardConteudo      = view.findViewById(R.id.cardConteudo);

        if (getArguments() != null) {
            idProduto   = getArguments().getString("idProduto");
            nomeProduto = getArguments().getString("nomeProduto", "Produto");
        }
        if (nomeProduto != null) tvTitulo.setText(nomeProduto);
        if (idProduto == null || idProduto.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Produto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        View btnVoltar = view.findViewById(R.id.voltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> {
                NavController nav = NavHostFragment.findNavController(this);
                nav.navigateUp();
            });
        }

        pagerAdapter = new TabelasPagerAdapter(
                requireContext(),
                new ArrayList<>(),
                () -> navegarParaAdicionarTabela(nomeProduto)
        );
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(1);

        new TabLayoutMediator(tabDots, pager, (tab, position) -> { }).attach();

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                atualizarAvaliacao(position);
            }
        });

        showLoading(true);
        carregarTabelasPorProduto(idProduto);
    }

    // empurra o usuário pra tela de criar tabela
    private void navegarParaAdicionarTabela(@NonNull String nomeProduto) {
        Bundle args = new Bundle();
        args.putString("nomeProduto", nomeProduto);
        args.putString("idProduto", idProduto);
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.action_tabelaProdutoFragment_to_navigation_tabela, args);
    }

    private void carregarTabelasPorProduto(String id) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlStr = urlBase + URLEncoder.encode(id, "UTF-8");
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                String basic = user + ":" + password;
                String auth = "Basic " + Base64.encodeToString(
                        basic.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", auth);

                int code = conn.getResponseCode();
                InputStream stream = (code >= 200 && code < 300)
                        ? new BufferedInputStream(conn.getInputStream())
                        : new BufferedInputStream(conn.getErrorStream());
                String body = readAll(stream);

                if (code < 200 || code >= 300) {
                    Log.e("TabelaProduto", "HTTP " + code + " body=" + body);
                    postUi(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Erro HTTP " + code, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                if (body == null || body.trim().isEmpty()) {
                    postUi(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Resposta vazia", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                JSONArray arr = new JSONArray(body);

                List<Tabela> novasTabelas = new ArrayList<>();
                List<String> novasAvaliacoes = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;

                    String nomeTabela  = o.optString("nomeTabela", "Tabela Nutricional");
                    String porcaoTexto = resolvePorcaoTexto(o);

                    List<Linha> linhas = new ArrayList<>();
                    JSONArray ns = o.optJSONArray("nutrientes");
                    if (ns != null) {
                        for (int k = 0; k < ns.length(); k++) {
                            JSONObject jn = ns.optJSONObject(k);
                            if (jn == null) continue;

                            String nome = jn.optString("nutriente", "");
                            String valorFmt = formatAnyNumberToTwoDecimals(anyToString(jn.opt("porcao")));
                            String vdFmt    = formatAnyNumberToTwoDecimals(anyToString(jn.opt("valorDiario")));

                            linhas.add(new Linha(nome, valorFmt, vdFmt));
                        }
                    }

                    long idEstavel = o.has("tabelaId")
                            ? (long) o.optInt("tabelaId", i) * 31L + i
                            : i;

                    novasTabelas.add(new Tabela(nomeTabela, porcaoTexto, linhas, idEstavel));

                    String avText = "";
                    Object avObj = o.opt("avaliacao");
                    if (avObj == null) avObj = o.opt("avaliacaoTexto");

                    if (avObj instanceof JSONObject) {
                        avText = normalizeAvaliacao((JSONObject) avObj);
                    } else if (avObj instanceof String) {
                        String raw = ((String) avObj).trim();
                        if (raw.startsWith("{") && raw.endsWith("}")) {
                            try { avText = normalizeAvaliacao(new JSONObject(raw)); }
                            catch (Exception ignore) { avText = normalizeAvaliacao(raw); }
                        } else {
                            avText = normalizeAvaliacao(raw);
                        }
                    }

                    novasAvaliacoes.add(avText);
                }

                postUi(() -> {
                    aplicarTabelas(novasTabelas, novasAvaliacoes);
                    showLoading(false);
                });

            } catch (Exception e) {
                Log.e("TabelaProduto", "Falha carregando tabelas", e);
                postUi(() -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Falha: " + (e.getMessage() == null ? "desconhecida" : e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private String resolvePorcaoTexto(JSONObject tabela) {
        String porcaoTxt = tabela.optString("porcaoTexto", null);
        if (porcaoTxt != null && !porcaoTxt.trim().isEmpty()) return porcaoTxt;
        String p = anyToString(tabela.opt("porcao"));
        if (!p.isEmpty()) return "Porção " + formatAnyNumberToTwoDecimals(p);
        return "Porção";
    }

    private void aplicarTabelas(@NonNull List<Tabela> tabelas,
                                @NonNull List<String> novasAvaliacoes) {
        avaliacoes.clear();
        avaliacoes.addAll(novasAvaliacoes != null ? novasAvaliacoes : new ArrayList<>());
        pagerAdapter.submit(tabelas);
        atualizarAvaliacao(0);
    }

    // mostra o textinho da avaliação
    private void atualizarAvaliacao(int position) {
        if (pagerAdapter == null || tvResumoAvaliacao == null) return;
        if (position >= pagerAdapter.getRealCount()) {
            tvResumoAvaliacao.setText("");
            return;
        }
        String txt = "";
        if (position >= 0 && position < avaliacoes.size()) {
            String a = avaliacoes.get(position);
            if (a != null) txt = a.trim();
        }
        if (txt.isEmpty() || "null".equalsIgnoreCase(txt)) {
            tvResumoAvaliacao.setText("Opa! Parece que essa tabela não tem uma avaliação");
        } else {
            tvResumoAvaliacao.setText(txt);
        }
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        if (cardConteudo != null)   cardConteudo.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private void postUi(Runnable r) {
        if (isAdded()) requireActivity().runOnUiThread(r);
    }

    // lê a resposta
    private String readAll(InputStream is) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // transforma qualquer coisa em string
    private String anyToString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    // pega o primeiro número e deixa bonitinho com 2 casas
    private String formatAnyNumberToTwoDecimals(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        Pattern p = Pattern.compile("(-?\\d+(?:[\\.,]\\d+)?)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            String numStr = m.group(1).replace(',', '.');
            try {
                double v = Double.parseDouble(numStr);
                String fmt = DF2.format(v);
                return s.substring(0, m.start()) + fmt + s.substring(m.end());
            } catch (NumberFormatException ignore) {
                return s;
            }
        }
        return s;
    }

    //pega o melhor texto de um JSON de avaliação
    private String normalizeAvaliacao(JSONObject obj) {
        if (obj == null) return "";
        String t = obj.optString("texto", null);
        if (t != null && !t.trim().isEmpty()) {
            return normalizeAvaliacao(t);
        }
        String best = "";
        int bestLen = 0;
        for (Iterator<String> it = obj.keys(); it.hasNext();) {
            String k = it.next();
            if ("classificacao".equalsIgnoreCase(k) || "pontuacao".equalsIgnoreCase(k)) continue;
            Object v = obj.opt(k);
            if (v instanceof String) {
                String sv = ((String) v).trim();
                if (sv.length() > bestLen) {
                    best = sv;
                    bestLen = sv.length();
                }
            }
        }
        return normalizeAvaliacao(best);
    }

    // tira aspas e converte \n em quebra real
    private String normalizeAvaliacao(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.replace("\\n", "\n");
        if ("null".equalsIgnoreCase(s)) return "";
        return s;
    }
}
