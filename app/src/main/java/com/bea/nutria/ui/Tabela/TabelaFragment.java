package com.bea.nutria.ui.Tabela;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bea.nutria.AvaliacaoTabelaFragment;
import com.bea.nutria.R;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.databinding.FragmentTabelaBinding;
import com.bea.nutria.model.GetNutrienteDTO;
import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.ItemIngrediente;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TabelaFragment extends Fragment {

    private FragmentTabelaBinding binding;
    private int porcaoAtual= 0;
    private String tipoMedida = "";
    private Integer idTabela = 0;
    private List<ItemIngrediente> ingredienteList = new ArrayList<>();
    private OkHttpClient client;
    private Retrofit retrofit;
    private String credenciais = Credentials.basic("nutria", "nutria123");
    private TabelaAPI api;
    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTabelaBinding.inflate(inflater, container, false);

        setCheckBoxListener(binding.checkBox);
        setCheckBoxListener(binding.checkBox2);
        setCheckBoxListener(binding.checkBox3);
        setCheckBoxListener(binding.checkBox4);

        credenciais = Credentials.basic("nutria", "nutria123");
        client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Authorization", credenciais)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(req);
                })
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl("https://api-spring-mongodb.onrender.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(TabelaAPI.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            return WindowInsetsCompat.CONSUMED;
        });

        binding.editValor.setText(String.valueOf(porcaoAtual));
        binding.editValor.setInputType(InputType.TYPE_CLASS_NUMBER);


        binding.btnAumentar.setOnClickListener(v -> {
            atualizarPorcao(porcaoAtual + 1);
        });
        binding.btnDiminuir.setOnClickListener(v -> {
            if (porcaoAtual > 0){
                atualizarPorcao(porcaoAtual - 1);
            }
        });

        binding.editValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    porcaoAtual = Integer.parseInt(charSequence.toString());
                }catch (NumberFormatException exception){
                    porcaoAtual = 0;
                }
            }
        });

        binding.button.setOnClickListener(v -> {
            if (validarTodosCamposObrigatorios()) {

                //adicionar métodos para verificar se todos os campos foram preenchidos
                Map<String, Object> novaTabela = new HashMap<>();

                //ingredientes mockados
                List<Map<String, Number>> ingredientes = new ArrayList<>();
                Map<String, Number> ingrediente1 = new HashMap<>();
                ingrediente1.put("nCdIngrediente", 1);
                ingrediente1.put("iQuantidade", 200.0);

                Map<String, Number> ingrediente2 = new HashMap<>();
                ingrediente2.put("nCdIngrediente", 2);
                ingrediente2.put("iQuantidade", 150.5);

                ingredientes.add(ingrediente1);
                ingredientes.add(ingrediente2);

                novaTabela.put("nomeProduto", binding.nomeProdutoLayout.getEditText().getText());
                novaTabela.put("nomeTabela", binding.nomeTabelaLayout.getEditText().getText());
                novaTabela.put("tipoMedida", tipoMedida);
                novaTabela.put("porcao", getPorcaoAtual());
                novaTabela.put("ingredientes", ingredientes);

                if (getArguments() != null) {
                    Integer idProduto = getArguments().getInt("idProduto");
                    iniciandoServidor(() -> adicionarTabela(1, idProduto, novaTabela));
                } else {
                    iniciandoServidor(() -> criarTabela(1, novaTabela));
                }
            }else {
                Toast.makeText(getContext(),
                        "Por favor, todos os campos devem ser preenchidos e ao menos um ingrediente adicionado",
                        Toast.LENGTH_LONG).show();
            }

        });

        binding.btnNovo.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putInt("idTabela", idTabela);

            NavController navController = NavHostFragment.findNavController(TabelaFragment.this);
            navController.navigate(R.id.action_tabela_to_avaliacao_tabela, result);
        });


    }
    public void atualizarPorcao(int novoValor){
        porcaoAtual = novoValor;
        binding.editValor.setText(String.valueOf(porcaoAtual));
    }
    public int getPorcaoAtual(){
        return porcaoAtual;
    }
    private void setCheckBoxListener(CheckBox checkBox){
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (isChecked) {
                if (checkBox != binding.checkBox) binding.checkBox.setChecked(false);
                if (checkBox != binding.checkBox2) binding.checkBox2.setChecked(false);
                if (checkBox != binding.checkBox3) binding.checkBox3.setChecked(false);
                if (checkBox != binding.checkBox4) binding.checkBox4.setChecked(false);

                tipoMedida = checkBox.getText().toString();
            }
            else {
                if (tipoMedida.equals(checkBox.getText().toString())){
                    tipoMedida = "";
                }
            }
        });
    }
    private void criarTabela(Integer usuarioLogado, Map<String,Object> tabela) {
        mostrarCarregando(true);
        api.criarTabela(usuarioLogado, tabela).enqueue(new Callback<GetTabelaDTO>() {
            @Override
            public void onResponse(Call<GetTabelaDTO> call, retrofit2.Response<GetTabelaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetTabelaDTO tabelaCriada = response.body();
                    preencherDadosTabela(tabelaCriada);
                    mostrarCarregando(false);
                    binding.tableLayout.setVisibility(View.VISIBLE);
                    binding.btnNovo.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Tabela adicionada com sucesso!", Toast.LENGTH_SHORT).show();

                } else {
                    int code = response.code();
                    mostrarCarregando(false);
                    Toast.makeText(
                            getContext(),
                            "Erro ao carregar usuário (" + code + ")\n",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<GetTabelaDTO> call, Throwable t) {
                mostrarCarregando(false);
                Toast.makeText(getContext(),
                        "Falha de conexão: " + (t.getMessage() == null ? "desconhecida" : t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void adicionarTabela(Integer usuarioLogado, Integer idProduto, Map<String,Object> tabela) {
        mostrarCarregando(true);
        api.adicionarTabela(usuarioLogado, idProduto, tabela).enqueue(new Callback<GetTabelaDTO>() {
            @Override
            public void onResponse(Call<GetTabelaDTO> call, retrofit2.Response<GetTabelaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetTabelaDTO tabelaCriada = response.body();
                    preencherDadosTabela(tabelaCriada);
                    mostrarCarregando(false);
                    binding.tableLayout.setVisibility(View.VISIBLE);
                    binding.btnNovo.setVisibility(View.VISIBLE);

                    Toast.makeText(getContext(), "Tabela adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    int code = response.code();
                    mostrarCarregando(false);
                    Toast.makeText(
                            getContext(),
                            "Erro ao carregar usuário (" + code + ")\n",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<GetTabelaDTO> call, Throwable t) {
                mostrarCarregando(false);
                Toast.makeText(getContext(),
                        "Falha de conexão: " + (t.getMessage() == null ? "desconhecida" : t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencherDadosTabela(GetTabelaDTO tabela) {
        idTabela = tabela.getTabelaId();
        binding.tableLayout.removeAllViews();

        TableRow nomeTabela = new TableRow(getContext());
        TextView nome = new TextView(getContext());
        nomeTabela.addView(nome);
        binding.tableLayout.addView(nomeTabela);

        TableRow nomeColuna = new TableRow(getContext());
        TextView coluna1 = new TextView(getContext());
        TextView coluna2 = new TextView(getContext());
        TextView coluna3 = new TextView(getContext());

        coluna1.setText("Item");
        coluna2.setText("Valor");
        coluna3.setText("%VD*");

        nomeColuna.addView(coluna1);
        nomeColuna.addView(coluna2);
        nomeColuna.addView(coluna3);
        binding.tableLayout.addView(nomeColuna);

        for (GetNutrienteDTO nutrienteDados : tabela.getNutrientes()){
            TableRow nutrientesInformacao = new TableRow(getContext());
            TextView nutriente = new TextView(getContext());
            TextView porcao = new TextView(getContext());
            TextView vd = new TextView(getContext());

            nutriente.setText(nutrienteDados.getNutriente());
            porcao.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getPorcao()));
            vd.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getValorDiario())+"%");

            nutrientesInformacao.addView(nutriente);
            nutrientesInformacao.addView(porcao);
            nutrientesInformacao.addView(vd);
            binding.tableLayout.addView(nutrientesInformacao);

        }

    }

    private void iniciandoServidor(Runnable proximoPasso) {
        long agora = System.currentTimeMillis();
        if (agora - ultimoWakeMs < JANELA_WAKE_MS) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }
        new Thread(() -> {
            boolean ok = false;
            for (int tent = 1; tent <= 3 && !ok; tent++) {
                try {
                    Request req = new Request.Builder()
                            .url("https://api-spring-mongodb.onrender.com")
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                    }
                } catch (Exception ignore) {
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            if (isAdded()){
                requireActivity().runOnUiThread(() -> {
                    if (proximoPasso != null) proximoPasso.run();
                });
            }
        }).start();
    }
    private void mostrarCarregando(boolean carregando) {
        if (carregando) {
            binding.layoutCarregando.setVisibility(View.VISIBLE);
        } else {
            binding.layoutCarregando.setVisibility(View.GONE);
        }
    }
    private boolean validarCampoObrigatorio(TextInputLayout layout, TextInputEditText edit) {
        String texto = edit.getText().toString().trim();

        if (texto.isEmpty()) {
            layout.setError("Campo obrigatório");
            layout.setErrorEnabled(true);
            return false;
        } else {
            layout.setError(null);
            layout.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validarTodosCamposObrigatorios() {
        boolean nomeProdutoValido = validarCampoObrigatorio(binding.nomeProdutoLayout, binding.nomeProdutoEdit);
        boolean nomeTabelaValido = validarCampoObrigatorio(binding.nomeTabelaLayout, binding.nomeTabelaEdit);
        boolean checkBoxSelecionada = binding.checkBox.isChecked() || binding.checkBox2.isChecked() || binding.checkBox3.isChecked() || binding.checkBox4.isChecked();

        //verificar se tem ingredientes adicionados
        return nomeProdutoValido && nomeTabelaValido && checkBoxSelecionada && !binding.editValor.getText().equals("0");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
