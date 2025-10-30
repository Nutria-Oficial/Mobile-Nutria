package com.bea.nutria;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.databinding.FragmentAvaliacaoTabelaBinding;
import com.bea.nutria.model.GetNutrienteDTO;
import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.GetTabelaEAvaliacaoDTO;
import com.bea.nutria.ui.Tabela.TabelaFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link AvaliacaoTabelaFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class AvaliacaoTabelaFragment extends Fragment {

    private FragmentAvaliacaoTabelaBinding binding;
    private ConexaoAPI conexaoAPI;
    private TabelaAPI api;
    private Double porcaoEmbalagemAtual;
    private ArrayList<String[]> tabelaDados = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAvaliacaoTabelaBinding.inflate(inflater, container, false);

        conexaoAPI = new ConexaoAPI("https://api-spring-mongodb.onrender.com");
        api = conexaoAPI.getApi(TabelaAPI.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert getArguments() != null;
        Integer idTabela = getArguments().getInt("idTabela");
        porcaoEmbalagemAtual = getArguments().getDouble("porcaoEmbalagem");
        conexaoAPI.iniciandoServidor(AvaliacaoTabelaFragment.this,() -> buscarTabelaAvaliacao(idTabela));

        binding.btnVisualizar.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putSerializable("tabela", tabelaDados);

            NavController navController = NavHostFragment.findNavController(AvaliacaoTabelaFragment.this);
            navController.navigate(R.id.action_navigation_avaliacao_tabela_to_navigation_visualizar, result);
        });
        binding.btnVoltar.setOnClickListener(v ->{
            NavController navController = NavHostFragment.findNavController(AvaliacaoTabelaFragment.this);
            navController.navigate(R.id.action_avaliacao_tabela_to_tabela);
        });
        binding.avaliacao.setOnClickListener(v -> {
            mostrarLegenda();
        });
    }

    private void buscarTabelaAvaliacao(Integer tabelaId) {
        mostrarCarregando(true);
        api.buscarTabelaComAvaliacao(tabelaId).enqueue(new Callback<GetTabelaEAvaliacaoDTO>() {
            @Override
            public void onResponse(Call<GetTabelaEAvaliacaoDTO> call, retrofit2.Response<GetTabelaEAvaliacaoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetTabelaEAvaliacaoDTO tabelaEncontrada = response.body();
                    preencherDadosTabela(tabelaEncontrada);
                    binding.avaliacaoComentarios.setText(tabelaEncontrada.getAvaliacao().getComentarios());

                    mostrarCarregando(false);
                    binding.cardConteudo.setVisibility(View.VISIBLE);
                    mudarCorAvaliacao(tabelaEncontrada.getAvaliacao().getClassificacao());

                } else {
                    int code = response.code();
                    Toast.makeText(
                            getContext(),
                            "Erro ao carregar usuário (" + code + ")\n",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<GetTabelaEAvaliacaoDTO> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Falha de conexão: " + (t.getMessage() == null ? "desconhecida" : t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

//    private void preencherDadosTabela(GetTabelaEAvaliacaoDTO tabela) {
//        tabelaDados.clear();
//        binding.tableLayout.removeAllViews();
//
//        TableRow nomeTabela = new TableRow(getContext());
//        TextView nome = new TextView(getContext());
//        nomeTabela.addView(nome);
//        binding.tableLayout.addView(nomeTabela);
//        adicionarValoresTabelaDados(binding.tableLayout.getChildAt(0));
//
//        TableRow nomeColuna = new TableRow(getContext());
//        TextView coluna1 = new TextView(getContext());
//        TextView coluna2 = new TextView(getContext());
//        TextView coluna3 = new TextView(getContext());
//
//        coluna1.setText(tabela.getNomeTabela());
//        coluna2.setText("Poção " + tabela.getPorcao()+"g");
//        coluna3.setText("%VD*");
//
//        nomeColuna.addView(coluna1);
//        nomeColuna.addView(coluna2);
//        nomeColuna.addView(coluna3);
//        binding.tableLayout.addView(nomeColuna);
//        adicionarValoresTabelaDados(binding.tableLayout.getChildAt(1));
//
//        for (GetNutrienteDTO nutrienteDados : tabela.getNutrientes()){
//            TableRow nutrientesInformacao = new TableRow(getContext());
//            TextView nutriente = new TextView(getContext());
//            TextView porcao = new TextView(getContext());
//            TextView vd = new TextView(getContext());
//
//            nutriente.setText(nutrienteDados.getNutriente());
//            porcao.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getPorcao()));
//            vd.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getValorDiario())+"%");
//
//            nutrientesInformacao.addView(nutriente);
//            nutrientesInformacao.addView(porcao);
//            nutrientesInformacao.addView(vd);
//            binding.tableLayout.addView(nutrientesInformacao);
//            adicionarValoresTabelaDados(binding.tableLayout.getChildAt(1));
//
//        }
//
//    }
    private void preencherDadosTabela(GetTabelaEAvaliacaoDTO tabela) {
        tabelaDados.clear();
        binding.tableLayout.removeAllViews();

        binding.tvTabelaTitulo.setText(tabela.getNomeTabela());
        binding.tvPorcaoColuna.setText(String.valueOf(tabela.getPorcao()));
        binding.tvPorcaoEmbalagemColuna.setText(String.valueOf(porcaoEmbalagemAtual));


        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        TableRow nomeColuna = new TableRow(getContext());
        nomeColuna.setPadding(padding, padding, padding, padding);

        TextView coluna1 = new TextView(getContext());
        TextView coluna2 = new TextView(getContext());
        TextView coluna3 = new TextView(getContext());

        coluna1.setText("Nutriente");
        modificarTextStyleColuna(1,coluna1);
        coluna2.setText("Valor");
        modificarTextStyleColuna(2,coluna2);
        coluna3.setText("%VD*");
        modificarTextStyleValores(3,coluna3);

        nomeColuna.addView(coluna1);
        nomeColuna.addView(coluna2);
        nomeColuna.addView(coluna3);
        binding.tableLayout.addView(nomeColuna);
        adicionarValoresTabelaDados(binding.tableLayout.getChildAt(0));

        for (GetNutrienteDTO nutrienteDados : tabela.getNutrientes()){
            TableRow nutrientesInformacao = new TableRow(getContext());
            TextView nutriente = new TextView(getContext());
            TextView porcaoNutriente = new TextView(getContext());
            TextView vd = new TextView(getContext());

            nutriente.setText(nutrienteDados.getNutriente());
            modificarTextStyleValores(1,nutriente);
            porcaoNutriente.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getPorcao()));
            modificarTextStyleValores(2,porcaoNutriente);
            if (nutrienteDados.getValorDiario() == null){
                vd.setText("NaN");
            }
            else {
                vd.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f%%", nutrienteDados.getValorDiario()));
            }
            modificarTextStyleValores(3,vd);

            nutrientesInformacao.addView(nutriente);
            nutrientesInformacao.addView(porcaoNutriente);
            nutrientesInformacao.addView(vd);
            binding.tableLayout.addView(nutrientesInformacao);
            adicionarValoresTabelaDados(binding.tableLayout.getChildAt(1));
        }

    }
    private void mostrarCarregando(boolean carregando) {
        if (carregando) {
            binding.layoutCarregando.setVisibility(View.VISIBLE);
            binding.layoutInformacoes.setVisibility(View.GONE);
        } else {
            binding.layoutCarregando.setVisibility(View.GONE);
            binding.layoutInformacoes.setVisibility(View.VISIBLE);
        }
    }

    private void adicionarValoresTabelaDados(View linhaTabela){
        TableRow linha = (TableRow) linhaTabela;

        String[] valores = new String[linha.getChildCount()];

        for (int i = 0; i < linha.getChildCount(); i++) {
            TextView valor = (TextView) linha.getChildAt(i);
            valores[i] = valor.getText().toString();
        }
        tabelaDados.add(valores);
    }
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
    private void modificarTextStyleColuna(int coluna,TextView textView){
        if(coluna == 1){
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }
        else {
            textView.setGravity(Gravity.END);
        }
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_semibold);
        textView.setTypeface(typeface);
        textView.setMaxLines(1);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,1f
        );
        textView.setLayoutParams(textParams);
    }
    private void modificarTextStyleValores(int coluna, TextView textView){
        if(coluna == 1){
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }
        else {
            textView.setGravity(Gravity.END);
        }
        textView.setMaxLines(1);
        TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,1f
        );
        textView.setLayoutParams(textParams);
    }
    private void mudarCorAvaliacao(Character letter) {
        switch (letter){
            case 'A': binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_a);
            case 'B': binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_b);
            case 'C': binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_c);
            case 'D': binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_d);
            case 'E': binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_e);
        }
    }
    private void mostrarLegenda(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.card_avaliacao_legenda, null);

        ImageView btnfechar = view.findViewById(R.id.btnFechar);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnfechar.setOnClickListener(v -> dialog.dismiss());
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}