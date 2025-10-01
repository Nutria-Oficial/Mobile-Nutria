package com.bea.nutria.ui.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.MainActivity;
import com.bea.nutria.R;
import com.bea.nutria.ui.Cadastro.CadastroActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth objAutenticar;
    private TextInputEditText editEmail;
    private TextInputEditText editSenha;
    private Button btnEntrar;
    private TextView btnCadastrarSe;
    private TextView esqueceuSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        objAutenticar = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        btnEntrar = findViewById(R.id.btn_proximo);
        btnCadastrarSe = findViewById(R.id.btn_cadastrar_se);
        esqueceuSenha = findViewById(R.id.esqueceuSenha);

        btnCadastrarSe.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class))
        );

        btnEntrar.setOnClickListener(v -> fazerLogin());

        esqueceuSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordReset();
            }

            private void showPasswordReset() {
                //alert dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setTitle("Recuperar senha");
                alert.setMessage("Digite seu email para recuperar a senha: ");
                //colocar um edittext para digitar o email
                EditText editText = new EditText(LoginActivity.this);
                alert.setView(editText);
                //botao positive: enviar
                alert.setPositiveButton("Enviar", (dialog, wich) -> {
                    String email = editText.getText().toString();
                    objAutenticar.sendPasswordResetEmail(email);
                });
                alert.show();
            }
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
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish(); // opcional, fecha a tela de login
                        } else {
                            String excecao = "Usuário/Senha inválidos";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                excecao = "Usuário não cadastrado";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "E-mail e senha não correspondem";
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
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
