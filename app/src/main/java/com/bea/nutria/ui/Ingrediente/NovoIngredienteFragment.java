package com.bea.nutria.ui.Ingrediente;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.databinding.FragmentNovoIngredienteBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovoIngredienteFragment extends Fragment {

    private FragmentNovoIngredienteBinding binding;
    private ConexaoAPI apiManager;
    private IngredienteAPI ingredienteAPI;
    private IngredienteAdapter adapter;
    private static final String url = "https://api-spring-mongodb.onrender.com";


    public NovoIngredienteFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNovoIngredienteBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        configurarValidacao();
        configurarBotao();

        apiManager = new ConexaoAPI(url);
        ingredienteAPI = apiManager.getApi(IngredienteAPI.class);

        return view;
    }

    private void criarIngrediente(IngredienteRequest ingrediente) {
        ingredienteAPI.criarIngrediente(ingrediente).enqueue(new Callback<IngredienteResponse>() {
            @Override
            public void onResponse(Call<IngredienteResponse> call, Response<IngredienteResponse> response) {
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    IngredienteResponse ingredienteCriado = response.body();

                    IngredienteSharedViewModel sharedViewModel =
                            new ViewModelProvider(requireActivity()).get(IngredienteSharedViewModel.class);

                    // adicionar aos selecionados
                    List<IngredienteResponse> selecionados = sharedViewModel.getIngredientesSelecionados().getValue();
                    if (selecionados == null) {
                        selecionados = new ArrayList<>();
                    }
                    selecionados.add(ingredienteCriado);
                    sharedViewModel.setIngredientesSelecionados(selecionados);

                    // notificar que foi criado novo ingrediente
                    sharedViewModel.setNovoIngredienteAdicionado(ingredienteCriado);

                    Toast.makeText(getContext(),
                            "✓ " + ingredienteCriado.getNomeIngrediente() + " adicionado!",
                            Toast.LENGTH_SHORT).show();

                    limparCampos();
                } else {
                    Toast.makeText(getContext(),
                            "Erro ao adicionar ingrediente",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IngredienteResponse> call, Throwable throwable) {
                if (getActivity() != null) {
                    Toast.makeText(getContext(),
                            "Falha na conexão",
                            Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void configurarValidacao() {
        configurarValidacaoCampo(binding.layoutNomeIngrediente, binding.editNomeIngrediente);
        configurarValidacaoCampo(binding.layoutCaloria, binding.editCaloria);
        configurarValidacaoCampo(binding.layoutCarboidrato, binding.editCarboidrato);
        configurarValidacaoCampo(binding.layoutAcucar, binding.editAcucar);
        configurarValidacaoCampo(binding.layoutProteina, binding.editProteina);
        configurarValidacaoCampo(binding.layoutGordurasTotais, binding.editGordurasTotais);
        configurarValidacaoCampo(binding.layoutGordurasSaturadas, binding.editGordurasSaturadas);
        configurarValidacaoCampo(binding.layoutSodio, binding.editSodio);
        configurarValidacaoCampo(binding.layoutFibra, binding.editFibra);
    }

    private void configurarValidacaoCampo(TextInputLayout layout, TextInputEditText edit) {
        edit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layout, edit);
            }
        });
    }

    private boolean validarCampoObrigatorio(TextInputLayout layout, TextInputEditText edit) {
        String texto = edit.getText() != null ? edit.getText().toString().trim() : "";

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

    private void configurarBotao() {
        binding.btnAdicionarIngrediente.setOnClickListener(v -> {
            if (validarTodosCamposObrigatorios()) {
                adicionarIngrediente();
            } else {
                Toast.makeText(getContext(),
                        "Por favor, preencha todos os campos obrigatórios",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void adicionarIngrediente() {
        IngredienteRequest ingrediente = new IngredienteRequest();

        ingrediente.setNomeIngrediente(getText(binding.editNomeIngrediente));
        ingrediente.setCaloria(getDouble(binding.editCaloria));
        ingrediente.setCarboidrato(getDouble(binding.editCarboidrato));
        ingrediente.setAcucar(getDouble(binding.editAcucar));
        ingrediente.setProteina(getDouble(binding.editProteina));
        ingrediente.setGorduraTotal(getDouble(binding.editGordurasTotais));
        ingrediente.setGorduraSaturada(getDouble(binding.editGordurasSaturadas));
        ingrediente.setSodio(getDouble(binding.editSodio));
        ingrediente.setFibra(getDouble(binding.editFibra));
        ingrediente.setAgua(getDouble(binding.editAgua));
        ingrediente.setGorduraMonoinsaturada(getDouble(binding.editGordurasMono));
        ingrediente.setGorduraPoliinsaturada(getDouble(binding.editGordurasPoli));
        ingrediente.setColesterol(getDouble(binding.editColesterol));
        ingrediente.setAlcool(getDouble(binding.editAlcool));
        ingrediente.setVitaminaB6(getDouble(binding.editVitaminaB6));
        ingrediente.setVitaminaB12(getDouble(binding.editVitaminaB12));
        ingrediente.setVitaminaC(getDouble(binding.editVitaminaC));
        ingrediente.setVitaminaD(getDouble(binding.editVitaminaD));
        ingrediente.setVitaminaE(getDouble(binding.editVitaminaE));
        ingrediente.setVitaminaK(getDouble(binding.editVitaminaK));
        ingrediente.setTeobromina(getDouble(binding.editTeobromina));
        ingrediente.setCafeina(getDouble(binding.editCafeina));
        ingrediente.setColina(getDouble(binding.editColina));
        ingrediente.setCalcio(getDouble(binding.editCalcio));
        ingrediente.setFosforo(getDouble(binding.editFosforo));
        ingrediente.setMagnesio(getDouble(binding.editMagnesio));
        ingrediente.setPotassio(getDouble(binding.editPotassio));
        ingrediente.setFerro(getDouble(binding.editFerro));
        ingrediente.setZinco(getDouble(binding.editZinco));
        ingrediente.setCobre(getDouble(binding.editCobre));
        ingrediente.setSelenio(getDouble(binding.editSelenio));
        ingrediente.setRetinol(getDouble(binding.editRetinol));
        ingrediente.setTiamina(getDouble(binding.editTiamina));
        ingrediente.setRiboflavina(getDouble(binding.editRiboflavina));
        ingrediente.setNiacina(getDouble(binding.editNiacina));
        ingrediente.setFolato(getDouble(binding.editFolato));

        apiManager.iniciarServidor(requireActivity(), () -> criarIngrediente(ingrediente));
    }

    private String getText(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private double getDouble(TextInputEditText edit) {
        String texto = getText(edit);
        if (texto.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(texto);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean validarTodosCamposObrigatorios() {
        return validarCampoObrigatorio(binding.layoutNomeIngrediente, binding.editNomeIngrediente)
                && validarCampoObrigatorio(binding.layoutCaloria, binding.editCaloria)
                && validarCampoObrigatorio(binding.layoutCarboidrato, binding.editCarboidrato)
                && validarCampoObrigatorio(binding.layoutAcucar, binding.editAcucar)
                && validarCampoObrigatorio(binding.layoutProteina, binding.editProteina)
                && validarCampoObrigatorio(binding.layoutGordurasTotais, binding.editGordurasTotais)
                && validarCampoObrigatorio(binding.layoutGordurasSaturadas, binding.editGordurasSaturadas)
                && validarCampoObrigatorio(binding.layoutSodio, binding.editSodio)
                && validarCampoObrigatorio(binding.layoutFibra, binding.editFibra);
    }

    private void limparCampos() {
        binding.editNomeIngrediente.setText("");
        binding.editCaloria.setText("");
        binding.editCarboidrato.setText("");
        binding.editAcucar.setText("");
        binding.editProteina.setText("");
        binding.editGordurasTotais.setText("");
        binding.editGordurasSaturadas.setText("");
        binding.editSodio.setText("");
        binding.editFibra.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // evitar memory leak
    }
}