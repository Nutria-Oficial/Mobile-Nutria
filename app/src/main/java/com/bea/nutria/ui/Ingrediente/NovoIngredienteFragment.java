package com.bea.nutria.ui.Ingrediente;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bea.nutria.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class NovoIngredienteFragment extends Fragment {
    private TextInputLayout layoutNome, layoutCaloria, layoutCarboidrato;
    private TextInputLayout layoutAcucar, layoutProteina, layoutGordurasTotais;
    private TextInputLayout layoutGordurasSaturadas, layoutSodio, layoutFibra;

    private TextInputEditText editNome, editCaloria, editCarboidrato;
    private TextInputEditText editAcucar, editProteina, editGordurasTotais;
    private TextInputEditText editGordurasSaturadas, editSodio, editFibra;

    private Button btnAdicionar;

    public NovoIngredienteFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novo_ingrediente, container, false);

        inicializarCampos(view);
        configurarValidacao();
        configurarBotao();

        return view;
    }

    private void inicializarCampos(View view) {
        layoutNome = view.findViewById(R.id.layoutNomeIngrediente);
        layoutCaloria = view.findViewById(R.id.layoutCaloria);
        layoutCarboidrato = view.findViewById(R.id.layoutCarboidrato);
        layoutAcucar = view.findViewById(R.id.layoutAcucar);
        layoutProteina = view.findViewById(R.id.layoutProteina);
        layoutGordurasTotais = view.findViewById(R.id.layoutGordurasTotais);
        layoutGordurasSaturadas = view.findViewById(R.id.layoutGordurasSaturadas);
        layoutSodio = view.findViewById(R.id.layoutSodio);
        layoutFibra = view.findViewById(R.id.layoutFibra);

        editNome = view.findViewById(R.id.editNomeIngrediente);
        editCaloria = view.findViewById(R.id.editCaloria);
        editCarboidrato = view.findViewById(R.id.editCarboidrato);
        editAcucar = view.findViewById(R.id.editAcucar);
        editProteina = view.findViewById(R.id.editProteina);
        editGordurasTotais = view.findViewById(R.id.editGordurasTotais);
        editGordurasSaturadas = view.findViewById(R.id.editGordurasSaturadas);
        editSodio = view.findViewById(R.id.editSodio);
        editFibra = view.findViewById(R.id.editFibra);

        btnAdicionar = view.findViewById(R.id.btnAdicionarIngrediente);
    }

    private void configurarValidacao() {
        // limpa erro quando o usuário começa a digitar
        editNome.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutNome, editNome);
            }
        });

        editCaloria.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutCaloria, editCaloria);
            }
        });

        editCarboidrato.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutCarboidrato, editCarboidrato);
            }
        });

        editAcucar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutAcucar, editAcucar);
            }
        });

        editProteina.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutProteina, editProteina);
            }
        });

        editGordurasTotais.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutGordurasTotais, editGordurasTotais);
            }
        });

        editGordurasSaturadas.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutGordurasSaturadas, editGordurasSaturadas);
            }
        });

        editSodio.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutSodio, editSodio);
            }
        });

        editFibra.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarCampoObrigatorio(layoutFibra, editFibra);
            }
        });
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

    private void configurarBotao() {
        btnAdicionar.setOnClickListener(v -> {
            if (validarTodosCamposObrigatorios()) {
                adicionarIngrediente();
            } else {
                Toast.makeText(getContext(),
                        "Por favor, preencha todos os campos obrigatórios",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validarTodosCamposObrigatorios() {
        boolean nomeValido = validarCampoObrigatorio(layoutNome, editNome);
        boolean caloriaValida = validarCampoObrigatorio(layoutCaloria, editCaloria);
        boolean carboidratoValido = validarCampoObrigatorio(layoutCarboidrato, editCarboidrato);
        boolean acucarValido = validarCampoObrigatorio(layoutAcucar, editAcucar);
        boolean proteinaValida = validarCampoObrigatorio(layoutProteina, editProteina);
        boolean gordurasTotaisValida = validarCampoObrigatorio(layoutGordurasTotais, editGordurasTotais);
        boolean gordurasSaturadasValida = validarCampoObrigatorio(layoutGordurasSaturadas, editGordurasSaturadas);
        boolean sodioValido = validarCampoObrigatorio(layoutSodio, editSodio);
        boolean fibraValida = validarCampoObrigatorio(layoutFibra, editFibra);

        return nomeValido && caloriaValida && carboidratoValido && acucarValido &&
                proteinaValida && gordurasTotaisValida && gordurasSaturadasValida &&
                sodioValido && fibraValida;
    }

    private void adicionarIngrediente() {
        Toast.makeText(getContext(), "Ingrediente adicionado com sucesso!", Toast.LENGTH_SHORT).show();

         limparCampos();
    }

    private void limparCampos() {
        editNome.setText("");
        editCaloria.setText("");
        editCarboidrato.setText("");
        editAcucar.setText("");
        editProteina.setText("");
        editGordurasTotais.setText("");
        editGordurasSaturadas.setText("");
        editSodio.setText("");
        editFibra.setText("");
    }
}