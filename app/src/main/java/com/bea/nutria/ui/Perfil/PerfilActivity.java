package com.bea.nutria.ui.Perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.R;
import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.model.Usuario;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilActivity extends AppCompatActivity {

    private TextView nomeTopo, empresaTopo;
    private ImageView fotoPerfil, btnEditarFoto;

    private Usuario usuarioLogado;
    private UsuarioAPI api;

    private TextView tvNome;
    private TextInputLayout tilNome;
    private TextInputEditText inputNome;
    private ImageView btnEditNome, btnCancelNome, btnOkNome;

    private TextView tvTelefone;
    private TextInputLayout tilTelefone;
    private TextInputEditText inputTelefone;
    private ImageView btnEditTelefone, btnCancelarTelefone, btnOkTelefone;

    private TextView tvEmail;
    private TextInputLayout tilEmail;
    private TextInputEditText inputEmail;
    private ImageView btnEditEmail, btnCancelarEmail, btnOkEmail;

    private TextView tvSenha;
    private TextInputLayout tilSenha;
    private TextInputEditText inputSenha;
    private ImageView btnEditSenha, btnCancelarSenha, btnOkSenha;

    private View overlayCarregando;
    private TextView txtCarregando;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private OkHttpClient client;
    private Retrofit retrofit;
    private String credenciais;
    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //iniciando cloudinary
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            HashMap config = new HashMap();
            config.put("cloud_name", "dtvvd7xif");
            MediaManager.init(getApplicationContext(), config);
        }

        findViewById(R.id.voltar).setOnClickListener(v -> onBackPressed());

        credenciais = Credentials.basic("nutria", "nutria123");
        client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
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
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(UsuarioAPI.class);

        overlayCarregando = findViewById(R.id.loadingOverlay);
        txtCarregando = findViewById(R.id.txtLoading);

        nomeTopo = findViewById(R.id.nomeTopo);
        empresaTopo = findViewById(R.id.empresaTopo);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        btnEditarFoto = findViewById(R.id.btnEditarFoto);

        tvNome = findViewById(R.id.tvNome);
        tilNome = findViewById(R.id.tilNome);
        inputNome = findViewById(R.id.inputNome);
        btnEditNome = findViewById(R.id.btnEditNome);
        btnCancelNome = findViewById(R.id.btnCancelNome);
        btnOkNome = findViewById(R.id.btnOkNome);

        tvTelefone = findViewById(R.id.tvTelefone);
        tilTelefone = findViewById(R.id.tilTelefone);
        inputTelefone = findViewById(R.id.inputTelefone);
        btnEditTelefone = findViewById(R.id.btnEditTelefone);
        btnCancelarTelefone = findViewById(R.id.btnCancelTelefone);
        btnOkTelefone = findViewById(R.id.btnOkTelefone);

        tvEmail = findViewById(R.id.tvEmail);
        tilEmail = findViewById(R.id.tilEmail);
        inputEmail = findViewById(R.id.inputEmail);
        btnEditEmail = findViewById(R.id.btnEditEmail);
        btnCancelarEmail = findViewById(R.id.btnCancelEmail);
        btnOkEmail = findViewById(R.id.btnOkEmail);

        tvSenha = findViewById(R.id.tvSenha);
        tilSenha = findViewById(R.id.tilSenha);
        inputSenha = findViewById(R.id.inputSenha);
        btnEditSenha = findViewById(R.id.btnEditSenha);
        btnCancelarSenha = findViewById(R.id.btnCancelSenha);
        btnOkSenha = findViewById(R.id.btnOkSenha);

        //abrindo a galeria
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        //acorda e só então envia a foto
                        mostrarCarregando("Atualizando foto...");
                        iniciandoServidor(() -> atualizarFotoUsuario(imageUri));
                    }
                });

        //carregando o usuário
        String emailFromIntent = getIntent().getStringExtra("EMAIL_SESSAO");
        if (emailFromIntent != null) {
            getSharedPreferences("nutria_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("email", emailFromIntent.trim().toLowerCase())
                    .apply();
        }

        String emailLogado = getSharedPreferences("nutria_prefs", MODE_PRIVATE)
                .getString("email", null);


        mostrarCarregando("Carregando perfil...");
        iniciandoServidor(() -> carregarUsuario(emailLogado.trim().toLowerCase()));


        setListeners();
    }

    //mostra o overlay de carregamento com mensagem
    private void mostrarCarregando(String mensagem) {
        if (txtCarregando != null)
            txtCarregando.setText(mensagem == null ? "Carregando..." : mensagem);
        if (overlayCarregando != null) overlayCarregando.setVisibility(View.VISIBLE);
    }

    // esconde o overlay de carregamento
    private void esconderCarregando() {
        if (overlayCarregando != null) overlayCarregando.setVisibility(View.GONE);
    }

    //evita erro de timeout,pois começa a esquentar o render antes das chamadas
    private void iniciandoServidor(Runnable proximoPasso) {
        long agora = System.currentTimeMillis();
        if (agora - ultimoWakeMs < JANELA_WAKE_MS) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }
        new Thread(() -> {
            boolean ok = false;
            for (int tent = 1; tent <= 3 && !ok; tent++) {
                try {
                    Request req = new Request.Builder()
                            .url("https://api-spring-aql.onrender.com/actuator/health")
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                    }
                } catch (Exception ignore) {
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            runOnUiThread(() -> {
                if (proximoPasso != null) proximoPasso.run();
            });
        }).start();
    }

    private void carregarUsuario(String email) {
        api.buscarUsuario(email).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, retrofit2.Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usuarioLogado = response.body();
                    preencherDados();
                    esconderCarregando();
                } else {
                    int code = response.code();
                    esconderCarregando();
                    Toast.makeText(
                            PerfilActivity.this,
                            "Erro ao carregar usuário (" + code + ")\n",
                            Toast.LENGTH_LONG
                    ).show();
                }
                getSharedPreferences("nutria_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("usuario_id", usuarioLogado.getId() != null ? usuarioLogado.getId() : -1)
                        .apply();
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                esconderCarregando();
                Toast.makeText(PerfilActivity.this,
                        "Falha de conexão: " + (t.getMessage() == null ? "desconhecida" : t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencherDados() {
        if (usuarioLogado == null) return;

        //colocando os textos do usuario respectivo nos TVs
        nomeTopo.setText(nullToEmpty(usuarioLogado.getNome()));
        empresaTopo.setText(nullToEmpty(usuarioLogado.getEmpresa()));
        tvNome.setText(nullToEmpty(usuarioLogado.getNome()));
        tvTelefone.setText(nullToEmpty(usuarioLogado.getTelefone()));
        tvEmail.setText(nullToEmpty(usuarioLogado.getEmail()));

        String urlFoto = usuarioLogado.getUrlFoto();
        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(this).load(urlFoto).placeholder(R.drawable.foto_padrao).circleCrop().into(fotoPerfil);
        } else {
            Glide.with(this).load(R.drawable.foto_padrao).circleCrop().into(fotoPerfil);
        }

        btnEditarFoto.setOnClickListener(v -> abrirGaleria());
    }

    private void setListeners() {
        //nome
        btnEditNome.setOnClickListener(v -> {
            tilNome.setHint("Digite seu nome");
            editarCampo(tilNome, tvNome, btnEditNome, btnCancelNome, btnOkNome, inputNome, tvNome.getText().toString());
        });
        btnCancelNome.setOnClickListener(v -> cancelarEdicao(tilNome, tvNome, btnEditNome, btnCancelNome, btnOkNome));
        btnOkNome.setOnClickListener(v -> salvarCampo("nome", getText(inputNome), tilNome, tvNome, btnEditNome, btnCancelNome, btnOkNome));

        //telefone
        btnEditTelefone.setOnClickListener(v -> {
            tilTelefone.setHint("Digite seu telefone");
            editarCampo(tilTelefone, tvTelefone, btnEditTelefone, btnCancelarTelefone, btnOkTelefone, inputTelefone, tvTelefone.getText().toString());
        });
        btnCancelarTelefone.setOnClickListener(v -> cancelarEdicao(tilTelefone, tvTelefone, btnEditTelefone, btnCancelarTelefone, btnOkTelefone));
        btnOkTelefone.setOnClickListener(v -> salvarCampo("telefone", getText(inputTelefone), tilTelefone, tvTelefone, btnEditTelefone, btnCancelarTelefone, btnOkTelefone));

        //email
        btnEditEmail.setOnClickListener(v -> {
            tilEmail.setHint("Digite seu e-mail");
            editarCampo(tilEmail, tvEmail, btnEditEmail, btnCancelarEmail, btnOkEmail, inputEmail, tvEmail.getText().toString());
        });
        btnCancelarEmail.setOnClickListener(v -> cancelarEdicao(tilEmail, tvEmail, btnEditEmail, btnCancelarEmail, btnOkEmail));
        btnOkEmail.setOnClickListener(v -> salvarCampo("email", getText(inputEmail), tilEmail, tvEmail, btnEditEmail, btnCancelarEmail, btnOkEmail));

        //senha
        btnEditSenha.setOnClickListener(v -> {
            tilSenha.setHint("Digite sua nova senha");
            editarCampo(tilSenha, tvSenha, btnEditSenha, btnCancelarSenha, btnOkSenha, inputSenha, "");
        });
        btnCancelarSenha.setOnClickListener(v -> cancelarEdicao(tilSenha, tvSenha, btnEditSenha, btnCancelarSenha, btnOkSenha));
        btnOkSenha.setOnClickListener(v -> salvarCampo("senha", getText(inputSenha), tilSenha, tvSenha, btnEditSenha, btnCancelarSenha, btnOkSenha));
    }

    private void editarCampo(TextInputLayout til, TextView tv, ImageView edit, ImageView cancel, ImageView ok, TextInputEditText input, String valorAtual) {
        til.setVisibility(View.VISIBLE);//mostra o campo de texto
        tv.setVisibility(View.GONE); //esconde o texto atual
        edit.setVisibility(View.GONE); //esconde o icone de editar
        cancel.setVisibility(View.VISIBLE); //mostra o cancelar
        ok.setVisibility(View.VISIBLE); //mostra o check
        input.setText(valorAtual); //coloca o valor atual no campo
        input.requestFocus(); //foco no campo
    }

    private void cancelarEdicao(TextInputLayout til, TextView tv, ImageView edit, ImageView cancel, ImageView ok) {
        til.setError(null); //limpa possíveis erros de validação
        til.setVisibility(View.GONE); //esconde o campo de textp
        tv.setVisibility(View.VISIBLE); //mostra o texto atual
        edit.setVisibility(View.VISIBLE); //mostra o icone de editar
        cancel.setVisibility(View.GONE); //esconde o cancelar
        ok.setVisibility(View.GONE);//esconde o check
    }

    //salva o campo editado chamando o endpoint e atualiza a interface
    private void salvarCampo(String campo, String valor, TextInputLayout til, TextView tv,
                             ImageView edit, ImageView cancel, ImageView ok) {

        Integer id = (usuarioLogado == null) ? null : usuarioLogado.getId();
        if (id == null) {
            Toast.makeText(this, "nao foi possível identificar o usuário", Toast.LENGTH_LONG).show();
            return;
        }

        if (usuarioLogado == null) return;
        if (valor.trim().isEmpty()) {
            til.setError("Preencha o campo");
            return;
        }
        til.setError(null);

        //se o campo for senha, aplica o hash antes de enviar
        if ("senha".equals(campo)) {
            String senhaHasheada = at.favre.lib.crypto.bcrypt.BCrypt
                    .withDefaults()
                    .hashToString(10, valor.toCharArray());
            valor = senhaHasheada;
        }

        //corpo json no formato
        Map<String, String> body = Collections.singletonMap(campo, valor);

        Call<Usuario> call = null;
        final int idPath = id;

        //chama o endpoint correspondente
        switch (campo) {
            case "nome":
                call = api.atualizarNomeUsuario(idPath, body);
                break;
            case "telefone":
                call = api.atualizarTelefoneUsuario(idPath, body);
                break;
            case "email":
                call = api.atualizarEmailUsuario(idPath, body);
                break;
            case "senha":
                call = api.atualizarSenhaUsuario(idPath, body);
                break;
        }

        if (call == null) return;

        final Call<Usuario> chamadaFinal = call;

        mostrarCarregando("Atualizando...");
        iniciandoServidor(() -> chamadaFinal.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, retrofit2.Response<Usuario> response) {
                esconderCarregando();
                if (response.isSuccessful() && response.body() != null) {
                    Usuario retorno = response.body();

                    if (retorno.getId() == null) {
                        retorno.setId(usuarioLogado.getId());
                    }

                    switch (campo) {
                        case "nome":
                            usuarioLogado.setNome(getText(inputNome));
                            tv.setText(usuarioLogado.getNome());
                            nomeTopo.setText(usuarioLogado.getNome());
                            break;
                        case "telefone":
                            usuarioLogado.setTelefone(getText(inputTelefone));
                            tv.setText(nullToEmpty(usuarioLogado.getTelefone()));
                            break;
                        case "email":
                            usuarioLogado.setEmail(getText(inputEmail));
                            tv.setText(nullToEmpty(usuarioLogado.getEmail()));
                            getSharedPreferences("nutria_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("email", usuarioLogado.getEmail())
                                    .apply();
                            break;
                        case "senha":
                            tv.setText("Alterar senha");
                            break;
                    }

                    cancelarEdicao(til, tv, edit, cancel, ok);
                    Toast.makeText(PerfilActivity.this, "Atualizado com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PerfilActivity.this, "Erro ao atualizar (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                esconderCarregando();
                Toast.makeText(PerfilActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void atualizarFotoUsuario(Uri uri) {
        if (usuarioLogado == null) {
            esconderCarregando();
            return;
        }

        try {
            Glide.with(PerfilActivity.this)
                    .load(uri)
                    .placeholder(R.drawable.foto_padrao)
                    .circleCrop()
                    .into(fotoPerfil);

            mostrarCarregando("Atualizando foto...");

            com.cloudinary.android.MediaManager.get()
                    .upload(uri)
                    .unsigned("perfil_app")
                    .option("folder", "fotos")
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, java.util.Map resultData) {
                            String secureUrl = resultData != null ? (String) resultData.get("secure_url") : null;

                            if (secureUrl == null || secureUrl.isEmpty()) {
                                esconderCarregando();
                                Toast.makeText(PerfilActivity.this, "url vazia ", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<String, String> corpo = new java.util.HashMap<>();
                            corpo.put("foto", secureUrl);

                            api.atualizarFotoUsuario(usuarioLogado.getId(), corpo)
                                    .enqueue(new Callback<Usuario>() {
                                        @Override
                                        public void onResponse(Call<Usuario> call, retrofit2.Response<Usuario> response) {
                                            esconderCarregando();
                                            if (response.isSuccessful() && response.body() != null) {
                                                usuarioLogado = response.body();

                                                Glide.with(PerfilActivity.this)
                                                        .load(usuarioLogado.getUrlFoto() != null ? usuarioLogado.getUrlFoto() : secureUrl)
                                                        .placeholder(R.drawable.foto_padrao)
                                                        .circleCrop()
                                                        .into(fotoPerfil);

                                                Toast.makeText(PerfilActivity.this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PerfilActivity.this,
                                                        "Erro ao atualizar foto (" + response.code() + ")", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Usuario> call, Throwable t) {
                                            esconderCarregando();
                                            Toast.makeText(PerfilActivity.this, "Falha ao enviar imagem -" + t.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            esconderCarregando();
                            Toast.makeText(PerfilActivity.this, "Upload falhou", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        }
                    })
                    .dispatch(PerfilActivity.this);

        } catch (Exception e) {
            esconderCarregando();
            Toast.makeText(this, "Erro ao processar imagem - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
