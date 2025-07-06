package com.bea.nutria.ui.Cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.MainActivity;
import com.bea.nutria.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {
    private TextInputEditText editNome, editEmail, editSenha, editTelefone, editEmpresa;
    private MaterialButton btnCadastrar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        editNome = findViewById(R.id.edit_nome);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        editTelefone = findViewById(R.id.edit_telefone);
        editEmpresa = findViewById(R.id.edit_empresa);
        btnCadastrar = findViewById(R.id.btn_cadastrar);

        btnCadastrar.setOnClickListener(v -> {
            cadastrar();
        });
    }
//Metodo para cadastrar o profissional
    private void cadastrar() {
        //Pegando os dados do EditText
        String nome = getText(editNome);
        String email = getText(editEmail);
        String senha = getText(editSenha);
        String telefone = getText(editTelefone);
        String empresa = getText(editEmpresa);
//Verificando se os campos estão vazios
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome, e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }
//Criando um HashMap para armazenar os dados do profissional
        Map<String, Object> profissional = new HashMap<>();
        profissional.put("nome", nome);
        profissional.put("email", email);
        profissional.put("senha", senha);
        profissional.put("telefone", telefone);
        profissional.put("empresa", empresa);

        //Cadastrando profissional
        db.collection("perfil")
                .add(profissional)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Profissional cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                })
                //Erro
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
//Metodo para pegar apenas o texto do EditText
    private String getText(TextInputEditText input) {
        String valor = "";
        if (input != null && input.getText() != null) {
            valor = input.getText().toString().trim();
        }
        return valor;
    }
}
