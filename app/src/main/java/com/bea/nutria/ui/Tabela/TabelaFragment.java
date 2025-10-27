package com.bea.nutria.ui.Tabela;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.AvaliacaoTabelaFragment;
import com.bea.nutria.R;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentTabelaBinding;
import com.bea.nutria.model.GetNutrienteDTO;
import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.ItemIngrediente;
import com.bea.nutria.ui.Ingrediente.IngredienteResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import okhttp3.Credentials;
import okhttp3.OkHttpClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import com.bea.nutria.ui.Ingrediente.IngredienteSharedViewModel;

import java.util.ArrayList;

public class TabelaFragment extends Fragment {

    private FragmentTabelaBinding binding;
    private int porcaoAtual= 0;
    private int porcaoEmbalagemAtual= 0;
    private String tipoMedida = "";
    private Integer idTabela = 0;
    private List<ItemIngrediente> ingredienteList = new ArrayList<>();

    private TabelaAPI api;
    private ConexaoAPI conexaoAPI;
    private TabelaAdapter adapter;
    private IngredienteSharedViewModel sharedViewModel;
    private static final String TAG = "TabelaFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTabelaBinding.inflate(inflater, container, false);

        setCheckBoxListener(binding.checkBox);
        setCheckBoxListener(binding.checkBox2);
        setCheckBoxListener(binding.checkBox3);
        setCheckBoxListener(binding.checkBox4);

        conexaoAPI = new ConexaoAPI("https://api-spring-mongodb.onrender.com");
        api = conexaoAPI.getApi(TabelaAPI.class);


        // Inicializar ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(IngredienteSharedViewModel.class);

        //setupRecyclerView();
        observarIngredientes();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> WindowInsetsCompat.CONSUMED);


        binding.porcao.setText(String.valueOf(porcaoAtual));
        binding.porcao.setInputType(InputType.TYPE_CLASS_NUMBER);


        binding.btnAumentar.setOnClickListener(v -> {
            atualizarPorcao(porcaoAtual + 1);
        });
        binding.btnDiminuir.setOnClickListener(v -> {
            if (porcaoAtual > 0){
                atualizarPorcao(porcaoAtual - 1);
            }
        });

        binding.porcao.addTextChangedListener(new TextWatcher() {
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
        binding.porcaoEmbalagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    porcaoEmbalagemAtual = Integer.parseInt(charSequence.toString());
                }catch (NumberFormatException exception){
                    porcaoEmbalagemAtual = 0;
                }
            }
        });

        binding.btnIngredientes.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(TabelaFragment.this);
            navController.navigate(R.id.action_tabela_to_ingrediente);
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
                    conexaoAPI.iniciandoServidor(TabelaFragment.this,() -> adicionarTabela(1, idProduto, novaTabela));
                } else {
                    conexaoAPI.iniciandoServidor(TabelaFragment.this,() -> criarTabela(1, novaTabela));
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
        binding.porcao.setText(String.valueOf(porcaoAtual));
    }
    public int getPorcaoAtual(){
        return porcaoAtual;
    }
    public int getPorcaoEmbalagemAtual(){
        return porcaoEmbalagemAtual;
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

        binding.tvTabelaTitulo.setText(tabela.getNomeTabela());
        binding.tvPorcaoColuna.setText(getPorcaoAtual());
        binding.tvPorcaoEmbalagemColuna.setText(getPorcaoEmbalagemAtual());

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

        for (GetNutrienteDTO nutrienteDados : tabela.getNutrientes()){
            TableRow nutrientesInformacao = new TableRow(getContext());
            TextView nutriente = new TextView(getContext());
            TextView porcaoNutriente = new TextView(getContext());
            TextView vd = new TextView(getContext());

            nutriente.setText(nutrienteDados.getNutriente());
            modificarTextStyleValores(1,nutriente);
            porcaoNutriente.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getPorcao()));
            modificarTextStyleValores(2,porcaoNutriente);
            vd.setText(String.format(Locale.forLanguageTag("pt-BR"),"%.2f", nutrienteDados.getValorDiario())+"%");
            modificarTextStyleValores(3,vd);

            nutrientesInformacao.addView(nutriente);
            nutrientesInformacao.addView(porcaoNutriente);
            nutrientesInformacao.addView(vd);
            binding.tableLayout.addView(nutrientesInformacao);

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
        boolean checkBoxSelecionado = binding.checkBox.isChecked() || binding.checkBox2.isChecked() || binding.checkBox3.isChecked() || binding.checkBox4.isChecked();

        //verificar se tem ingredientes adicionados
        return nomeProdutoValido && nomeTabelaValido && checkBoxSelecionado && !binding.porcao.getText().equals("0") && !binding.porcaoEmbalagem.getText().equals("0");
    }    
//    private void setupRecyclerView() {
//        adapter = new TabelaAdapter(getContext(), new ArrayList<>());
//
//        // atualizar ViewModel quando remover
//        adapter.setOnItemRemovedListener((ingrediente, newCount) -> {
////            binding.selecionados.setText(String.valueOf(newCount));
//            sharedViewModel.removerIngrediente(ingrediente); // atualizar ViewModel
//        });
//
//        binding.ingredientesSelecionados.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.ingredientesSelecionados.setAdapter(adapter);
//    }

    private void observarIngredientes() {
        sharedViewModel.getIngredientesSelecionados().observe(getViewLifecycleOwner(), selecionados -> {
            LinearLayout container = binding.layoutIngredientes;
            container.removeAllViews();

            if (selecionados != null && !selecionados.isEmpty()){
                for (IngredienteResponse response : selecionados){
                    mostrarIngredientesNaTela(response, container);
                }
            }
        });
    }
    private void mostrarIngredientesNaTela(IngredienteResponse ingrediente, LinearLayout container){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_ingrediente_tabela,container, false);
        TextView textView = view.findViewById(R.id.txtNomeIngrediente);
        EditText editText = view.findViewById(R.id.txtNomeIngrediente);
        ImageView btnRemover = view.findViewById(R.id.btnRemover);

        textView.setText(ingrediente.getNomeIngrediente());
        btnRemover.setOnClickListener(v -> {
            sharedViewModel.removerIngrediente(ingrediente);
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {

                }catch (NumberFormatException exception){
                }
            }
        });
        container.addView(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}