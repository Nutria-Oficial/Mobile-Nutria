package com.bea.nutria.ui.Cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.R;
import com.bea.nutria.ui.FotoPerfil.FotoPerfilActivity;
import com.bea.nutria.ui.Login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextInputEditText editNome;
    private TextInputEditText editEmail;
    private TextInputEditText editSenha;
    private TextInputEditText editTelefone;
    private TextInputEditText editEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.edit_nome);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        editTelefone = findViewById(R.id.edit_telefone);
        editEmpresa = findViewById(R.id.edit_empresa);

        Button btnCadastrar = findViewById(R.id.btn_cadastrar);
        TextView btnEntrar = findViewById(R.id.btn_proximo);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        btnEntrar.setOnClickListener(v -> {
            startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
        });

        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    // cadastrar o profissional
    private void cadastrar() {
        String nome = getText(editNome);
        String email = getText(editEmail);
        String senha = getText(editSenha);
        String telefone = getText(editTelefone);
        String empresa = getText(editEmpresa);

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome, e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> profissional = new HashMap<>();
        profissional.put("nome", nome);
        profissional.put("email", email);
        profissional.put("senha", senha);
        profissional.put("telefone", telefone);
        profissional.put("empresa", empresa);

        db.collection("perfil")
                .add(profissional)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Profissional cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroActivity.this, FotoPerfilActivity.class);
                    intent.putExtra("nome", nome);
                    intent.putExtra("usuarioId", doc.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // pegar apenas o texto do EditText
    private String getText(TextInputEditText input) {
        String valor = "";
        if (input != null && input.getText() != null) {
            valor = input.getText().toString().trim();
        }
        return valor;
    }
}
