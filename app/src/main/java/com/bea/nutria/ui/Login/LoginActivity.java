package com.bea.nutria.ui.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.MainActivity;
import com.bea.nutria.R;
import com.bea.nutria.ui.Cadastro.CadastroActivity;
import com.bea.nutria.ui.Perfil.PerfilActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth objAutenticar;
    private TextInputEditText editEmail, editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        objAutenticar = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);


        findViewById(R.id.btn_cadastrar_se)
                .setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));

        findViewById(R.id.btn_proximo)
                .setOnClickListener(v -> fazerLogin());

        findViewById(R.id.esqueceuSenha)
                .setOnClickListener(v -> showPasswordReset());
    }

    private void showPasswordReset() {
        Context ctx = this;
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_esqueceu_senha, null);

        TextInputEditText editEmail = view.findViewById(R.id.email_esqueceu);
        ProgressBar progress        = view.findViewById(R.id.progress);
        MaterialButton btnCancelar  = view.findViewById(R.id.cancelar);
        MaterialButton btnEnviar    = view.findViewById(R.id.enviar);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(view)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // deixa o card com cantos visíveis
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        //botao de cancelar da um dismiss
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        //enviar esqueceu senha
        btnEnviar.setOnClickListener(v -> {
            String email = safeText(editEmail);
            if (email.isEmpty()) {
                editEmail.setError("Digite seu e-mail");
                editEmail.requestFocus();
                return;
            }

            progress.setVisibility(View.VISIBLE);
            btnEnviar.setEnabled(false);
            btnCancelar.setEnabled(false);

            //Busca no firebase o email para enviar o email de redefinição de senha
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progress.setVisibility(View.GONE);
                        btnEnviar.setEnabled(true);
                        btnCancelar.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(ctx, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            String msg = "Erro ao enviar e-mail";
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                msg = task.getException().getMessage();
                            }
                            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void fazerLogin() {
        String email = safeText(editEmail);
        String senha = safeText(editSenha);

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        objAutenticar.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getSharedPreferences("nutria_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("email", email.trim().toLowerCase())
                                    .apply();

                           startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String excecao = "Usuário/Senha inválidos";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                excecao = "Usuário não cadastrado";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "E-mail e senha não correspondem";
                            } catch (Exception ignored) { }
                            Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String safeText(TextInputEditText input) {
        return (input != null && input.getText() != null)
                ? input.getText().toString().trim()
                : "";
    }
}
