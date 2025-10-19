package com.bea.nutria.ui.Cadastro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.R;
import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.model.Usuario;
import com.bea.nutria.ui.FotoPerfil.FotoPerfilActivity;
import com.bea.nutria.MainActivity;
import com.bea.nutria.ui.Login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText editNome, editEmail, editSenha, editTelefone, editEmpresa;
    private Button btnCadastrar;
    private TextView btnEntrar;

    private UsuarioAPI usuarioAPI;
    private FirebaseAuth auth;

    private String urlFotoSelecionada = null;

    private final ActivityResultLauncher<Intent> fotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                btnCadastrar.setEnabled(true);
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    urlFotoSelecionada = result.getData().getStringExtra("urlFoto"); // pode vir null
                } else {
                    urlFotoSelecionada = null;
                }
                iniciandoServidor();
            });

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private String credenciais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.edit_nome);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        editTelefone = findViewById(R.id.edit_telefone);
        editEmpresa = findViewById(R.id.edit_empresa);
        btnCadastrar = findViewById(R.id.btn_cadastrar);
        btnEntrar = findViewById(R.id.btn_proximo);

        // Firebase Auth
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        credenciais = Credentials.basic("nutria", "nutria123");
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .callTimeout(100, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
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
                .baseUrl("https://api-spring-aql.onrender.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        usuarioAPI = retrofit.create(UsuarioAPI.class);

        btnEntrar.setOnClickListener(v ->
                startActivity(new Intent(CadastroActivity.this, LoginActivity.class)));

        btnCadastrar.setOnClickListener(v -> {
            btnCadastrar.setEnabled(false);
            solicitarFotoPrimeiro();
        });
    }

    private void solicitarFotoPrimeiro() {
        String nome = getText(editNome);
        String email = getText(editEmail);
        String senha = getText(editSenha);

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome, e-mail e senha", Toast.LENGTH_SHORT).show();
            btnCadastrar.setEnabled(true);
            return;
        }

        Intent i = new Intent(CadastroActivity.this, FotoPerfilActivity.class);
        i.putExtra("nome", nome);
        fotoLauncher.launch(i);
    }

    private void iniciandoServidor() {
        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url("https://api-spring-aql.onrender.com/actuator/health")
                        .header("Authorization", credenciais)
                        .build();
                Call call = okHttpClient.newCall(req);
                Response resp = call.execute();
                if (resp != null) resp.close();
            } catch (Exception ignored) {

            }
            runOnUiThread(this::efetivarCadastro);
        }).start();
    }

    private void efetivarCadastro() {
        String nome = getText(editNome);
        String email = getText(editEmail);
        String senhaNormal = getText(editSenha);
        String telefone = getText(editTelefone);
        String empresa = getText(editEmpresa);

        // BCrypt em background pra não travar UI
        new Thread(() -> {
            String senhaHasheada = BCrypt.withDefaults().hashToString(10, senhaNormal.toCharArray());

            Usuario body = new Usuario(nome, email, senhaHasheada, telefone, empresa, urlFotoSelecionada);

            usuarioAPI.cadastrarUsuario(body).enqueue(new retrofit2.Callback<Usuario>() {
                @Override
                public void onResponse(retrofit2.Call<Usuario> call, retrofit2.Response<Usuario> response) {
                    runOnUiThread(() -> {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(CadastroActivity.this, "Erro API: " + response.code(), Toast.LENGTH_SHORT).show();
                            btnCadastrar.setEnabled(true);
                            return;
                        }
                        criarNoFirebaseAuthentication(email, senhaNormal);
                    });
                }

                @Override
                public void onFailure(retrofit2.Call<Usuario> call, Throwable t) {
                    runOnUiThread(() -> {
                        String msg = (t instanceof java.net.SocketTimeoutException)
                                ? "Servidor demorou a responder (timeout). Tente novamente."
                                : "Erro de rede: " + (t.getMessage() == null ? "desconhecido" : t.getMessage());
                        Toast.makeText(CadastroActivity.this, msg, Toast.LENGTH_LONG).show();
                        btnCadastrar.setEnabled(true);
                    });
                }
            });
        }).start();
    }

    private void criarNoFirebaseAuthentication(String email, String senhaPura) {
        auth.createUserWithEmailAndPassword(email, senhaPura)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cadastro concluído!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Erro desconhecido";
                        Toast.makeText(this, "Erro Firebase Auth: " + msg, Toast.LENGTH_LONG).show();
                        btnCadastrar.setEnabled(true);
                    }
                });
    }

    private String getText(TextInputEditText input) {
        return (input != null && input.getText() != null) ? input.getText().toString().trim() : "";
    }
}
