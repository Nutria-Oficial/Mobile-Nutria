package com.bea.nutria.ui.Scanner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bea.nutria.R;
import com.bea.nutria.ui.Ingrediente.IngredienteFragment;

public class PopUpScanner extends DialogFragment {

    public interface OnNomeConfirmadoListener {
        void onNomeConfirmado(String nome);
    }

    private OnNomeConfirmadoListener listener;

    public static PopUpScanner newInstance() {
        return new PopUpScanner();
    }

    public void setOnNomeConfirmadoListener(OnNomeConfirmadoListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pop_up_scanner, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etNome = view.findViewById(R.id.nomeScanner);
        Button btnCadastrar = view.findViewById(R.id.btnCadastrarScanner);
        View btnFechar = view.findViewById(R.id.btnFechar);

        btnCadastrar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            if (nome.isEmpty()) {
                etNome.setError("Digite um nome");
            } else {
                if (listener != null) listener.onNomeConfirmado(nome);
                dismiss();
            }
        });

        btnFechar.setOnClickListener(v -> dismiss());
        return dialog;
    }
}
