package com.bea.nutria.ui.Tabela;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Environment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentVisualizarBinding;
import com.bea.nutria.databinding.TelaDeslizavelBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class VisualizarFragment extends Fragment {
    private FragmentVisualizarBinding binding;
    private TelaDeslizavelBinding telaDeslizavelBinding;
    private boolean telaVisivel = false;
    private String csvTemporario = null;
    private int idTabela;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentVisualizarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            idTabela = getArguments().getInt("idTabela");
            ArrayList<String[]> tabelaDados = (ArrayList<String[]>) getArguments().getSerializable("tabela");
            String tabelaNome = getArguments().getString("tabelaNome");
            String tabelaPorcao = getArguments().getString("tabelaPorcao");
            String tabelaPorcaoEmbalagem = getArguments().getString("tabelaPorcaoEmbalagem");

            if (tabelaDados != null){
                preencherDadosTabela(tabelaDados,tabelaNome, tabelaPorcao, tabelaPorcaoEmbalagem);
            }
        }
        binding.btnOpcoes.setOnClickListener(v -> mostrarTelaDeslizavel());
        binding.btnVoltar.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt("idTabela", idTabela);
            if (getArguments() != null && getArguments().containsKey("nomeProduto")) {
                String nomeProduto = getArguments().getString("nomeProduto");
                String idProduto = getArguments().getString("idProduto");
                result.putString("nomeProduto", nomeProduto);
                result.putString("idProduto", idProduto);
                NavController navController = NavHostFragment.findNavController(VisualizarFragment.this);
                navController.navigate(R.id.action_navigation_visualizar_to_navigation_avaliacao_tabela, result);
            }
            else {
                NavController navController = NavHostFragment.findNavController(VisualizarFragment.this);
                navController.navigate(R.id.action_navigation_visualizar_to_navigation_avaliacao_tabela, result);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && csvTemporario != null) {
                try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                    outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                    outputStream.write(csvTemporario.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(getContext(), "CSV salvo com sucesso!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Erro ao salvar arquivo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void preencherDadosTabela(ArrayList<String[]> tabelaDados, String nomeTabela, String porcao, String porcaoEmbalagem) {
        binding.tableLayout.removeAllViews();

        binding.tvTabelaTitulo.setText(nomeTabela);
        binding.tvPorcaoColuna.setText(String.format("%s%s", binding.tvPorcaoColuna.getText(), porcao));
        binding.tvPorcaoEmbalagemColuna.setText(porcaoEmbalagem);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        for (int i = 0; i < tabelaDados.size(); i++) {
            TableRow linha = new TableRow(getContext());
            linha.setPadding(padding, padding, padding, padding);

            for (int j = 0; j < tabelaDados.get(i).length; j++) {
                TextView valor = new TextView(getContext());
                valor.setText(tabelaDados.get(i)[j]);

                if (i == 0){
                    modificarTextStyleColuna(j+1,valor);
                }
                else {
                    modificarTextStyleValores(j+1,valor);
                }
                linha.addView(valor);
            }
            binding.tableLayout.addView(linha);
            View divisor = new View(requireContext());
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,1);

            divisor.setLayoutParams(params);
            divisor.setBackgroundColor(Color.parseColor("#E0E0E0"));

            binding.tableLayout.addView(divisor);
        }

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
    public void mostrarTelaDeslizavel(){
        if (telaVisivel) return;

        telaDeslizavelBinding= TelaDeslizavelBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        View telaView = telaDeslizavelBinding.getRoot();

        View cardView = binding.cardViewConteudo;

        cardView.post(() -> {
            int alturaEmPx = cardView.getHeight();
            int[] posicao = new int[2];
            cardView.getLocationOnScreen(posicao);
            int posicaoTopo = posicao[1];

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    alturaEmPx
            );
            params.topMargin = posicaoTopo;
            params.gravity = Gravity.TOP;

            telaView.setTranslationX(300f);
            ((ViewGroup) binding.getRoot()).addView(telaView, params);

            telaView.animate().translationX(0f).setDuration(300).start();

            telaVisivel = true;

            telaDeslizavelBinding.btnFechar.setOnClickListener(v -> esconderTelaDeslizavel());
            telaDeslizavelBinding.btnExportar.setOnClickListener(v -> {
                try {
                    salvarTabelaComoCSV();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Erro ao exportar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }
    public void esconderTelaDeslizavel(){
        if (telaDeslizavelBinding == null) return;

        View telaView = telaDeslizavelBinding.getRoot();

        telaView.animate().translationX(telaView.getWidth()).setDuration(300).withEndAction(() -> {
            ((ViewGroup) binding.getRoot()).removeView(telaView);
            telaDeslizavelBinding = null;
            telaVisivel = false;
        });
    }
    private void salvarTabelaComoCSV() {
        String nomeArquivo = "Tabela_" + binding.tvTabelaTitulo.getText().toString().replaceAll("\\s+","_") + ".csv";

        StringBuilder conteudo = new StringBuilder();

        for (int i = 0; i < binding.tableLayout.getChildCount(); i++) {
            View linhaView = binding.tableLayout.getChildAt(i);
            if (linhaView instanceof TableRow) {
                TableRow linha = (TableRow) linhaView;
                for (int j = 0; j < linha.getChildCount(); j++) {
                    View cell = linha.getChildAt(j);
                    String campo = "";

                    if (cell instanceof TextView) {
                        campo = ((TextView) cell).getText().toString();
                    }
                    campo = campo.replace("\"", "\"\"");

                    conteudo.append('"').append(campo).append('"');

                    if (j < linha.getChildCount() - 1) {
                        conteudo.append(";");
                    }
                }
                conteudo.append("\n");
            }
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File arquivo = new File(downloadsDir, nomeArquivo);

            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
                fos.write(bom);
                fos.write(conteudo.toString().getBytes(StandardCharsets.UTF_8));
            }

            Toast.makeText(getContext(), "A tabela \"" + binding.tvTabelaTitulo.getText().toString() + "\" foi salva com sucesso!\nLocal: Downloads (Meus Arquivos)", Toast.LENGTH_LONG).show();

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    arquivo
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/csv");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(Intent.createChooser(intent, "Abrir arquivo com..."));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(),
                        "Nenhum aplicativo encontrado para abrir o arquivo CSV.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            abrirSeletorDeLocal(nomeArquivo, conteudo.toString());
        }
    }
    private void abrirSeletorDeLocal(String nomeArquivo, String conteudo) {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, nomeArquivo);
            startActivityForResult(intent, 1001);

            this.csvTemporario = conteudo;

        } catch (Exception e) {
            Toast.makeText(getContext(), "Não foi possível abrir o seletor de arquivos", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}