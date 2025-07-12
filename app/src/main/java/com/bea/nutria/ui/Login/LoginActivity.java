package com.bea.nutria.ui.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.MainActivity;
import com.bea.nutria.R;
import com.bea.nutria.ui.Cadastro.CadastroActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private MaterialButton btnEntrar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        btnEntrar = findViewById(R.id.btn_entrar);

        TextView btnCadastrarSe = findViewById(R.id.btn_cadastrar_se);
        btnCadastrarSe.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnEntrar.setOnClickListener(v -> {
            fazerLogin();
        });
    }

    private void fazerLogin() {
        String email = getText(editEmail);
        String senha = getText(editSenha);

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Busca o usuário
        db.collection("perfil")
                .whereEqualTo("email", email)
                .whereEqualTo("senha", senha)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String nome = doc.getString("nome");
                        Toast.makeText(this, "Bem-vindo(a), " + nome + "!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getText(TextInputEditText input) {
        String valor = "";
        if (input != null && input.getText() != null) {
            valor = input.getText().toString().trim();
        }
        return valor;
    }
}
