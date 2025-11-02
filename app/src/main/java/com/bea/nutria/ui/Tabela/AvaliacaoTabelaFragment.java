package com.bea.nutria.ui.Tabela;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bea.nutria.R;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.databinding.FragmentAvaliacaoTabelaBinding;
import com.bea.nutria.model.GetNutrienteDTO;
import com.bea.nutria.model.GetTabelaEAvaliacaoDTO;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

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
            result.putInt("idTabela", idTabela);
            result.putSerializable("tabela", tabelaDados);
            result.putString("tabelaNome", binding.tvTabelaTitulo.getText().toString());
            result.putString("tabelaPorcao", binding.tvPorcaoColuna.getText().toString());
            result.putString("tabelaPorcaoEmbalagem", binding.tvPorcaoEmbalagemColuna.getText().toString());

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

    private void preencherDadosTabela(GetTabelaEAvaliacaoDTO tabela) {
        tabelaDados.clear();
        binding.tableLayout.removeAllViews();

        binding.tvTabelaTitulo.setText(corrigirTextoCodificado(tabela.getNomeTabela()));
        binding.tvPorcaoColuna.setText(String.format("%s%s", binding.tvPorcaoColuna.getText(), tabela.getPorcao()));
        binding.tvPorcaoEmbalagemColuna.setText(String.format("%s%s", binding.tvPorcaoEmbalagemColuna.getText(), porcaoEmbalagemAtual));


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
        modificarTextStyleColuna(3,coluna3);

        nomeColuna.addView(coluna1);
        nomeColuna.addView(coluna2);
        nomeColuna.addView(coluna3);
        binding.tableLayout.addView(nomeColuna);
        adicionarValoresTabelaDados(binding.tableLayout.getChildAt(0));

        View linha = new View(requireContext());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,1);

        linha.setLayoutParams(params);
        linha.setBackgroundColor(Color.parseColor("#E0E0E0"));

        binding.tableLayout.addView(linha);

        for (GetNutrienteDTO nutrienteDados : tabela.getNutrientes()){
            TableRow nutrientesInformacao = new TableRow(getContext());
            nutrientesInformacao.setPadding(padding, padding, padding, padding);

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
            adicionarValoresTabelaDados(nutrientesInformacao);

            View novaLinha = new View(requireContext());
            TableLayout.LayoutParams novoParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1
            );
            novaLinha.setLayoutParams(novoParams);
            novaLinha.setBackgroundColor(Color.parseColor("#E0E0E0"));
            binding.tableLayout.addView(novaLinha);

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
    private void mostrarCarregando(boolean carregando) {
        if (!carregando) {
            binding.layoutCarregando.setVisibility(View.GONE);
            binding.scrollView.setVisibility(View.VISIBLE);
        }
    }

    private void adicionarValoresTabelaDados(View linhaTabela){
        if (!(linhaTabela instanceof TableRow)){
            return;
        }
        TableRow linha = (TableRow) linhaTabela;

        String[] valores = new String[linha.getChildCount()];

        for (int i = 0; i < linha.getChildCount(); i++) {
            View child = linha.getChildAt(i);
            if (child instanceof  TextView){
                valores[i] = ((TextView) child).getText().toString();
            }
            else {
                valores[i] = "";
            }
        }
        tabelaDados.add(valores);
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
            case 'A':
                binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_a);
                break;
            case 'B':
                binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_b);
                break;
            case 'C':
                binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_c);
                break;
            case 'D':
                binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_d);
                break;
            case 'E':
                binding.avaliacao.setImageResource(R.drawable.ic_avaliacao_e);
                break;
            default:
                Log.w("Avaliacao", "Classificação desconhecida: " + letter);
                break;
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