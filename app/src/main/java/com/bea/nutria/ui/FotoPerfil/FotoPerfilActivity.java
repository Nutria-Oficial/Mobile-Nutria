package com.bea.nutria.ui.FotoPerfil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.MainActivity;
import com.bea.nutria.R;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FotoPerfilActivity extends AppCompatActivity {

    private final String cloudName="dtvvd7xif";
    private final String uploadPreset="perfil_app";
    private final String folder="fotosAjustadas";
    private String url;

    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private ImageView fotoPerfil;
    private ImageView btnAddFoto;
    private TextView boasVindas;
    private TextView pular;
    private MaterialButton btnProximo;

    private Uri imagemSelecionadaUri = null;
    private String nomeUsuario, usuarioId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_perfil);

        initCloudinary();

        setGallery();
        checkPermissions();

        fotoPerfil = findViewById(R.id.foto_padrao);
        btnAddFoto = findViewById(R.id.add_foto);
        boasVindas = findViewById(R.id.nome);
        pular      = findViewById(R.id.pular);
        btnProximo = findViewById(R.id.btn_proximo);
        db         = FirebaseFirestore.getInstance();

        nomeUsuario = getIntent().getStringExtra("nome");
        usuarioId   = getIntent().getStringExtra("usuarioId");

        if (TextUtils.isEmpty(usuarioId) || TextUtils.isEmpty(nomeUsuario)) {
            Toast.makeText(this, "Erro: usuário não encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        boasVindas.setText("Olá, " + nomeUsuario + "!");

        btnAddFoto.setOnClickListener(this::openGallery);
        btnProximo.setOnClickListener(v -> salvarPerfil());
        pular.setOnClickListener(v -> atualizarUrl(null));
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            MediaManager.init(this, config);
        }
    }

    private void checkPermissions() {
        requestPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                r -> {}
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(new String[]{ Manifest.permission.READ_MEDIA_IMAGES });
        } else {
            requestPermissions.launch(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE });
        }
    }

    private void setGallery() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            imagemSelecionadaUri = imageUri;
                            fotoPerfil.setImageURI(imagemSelecionadaUri);
                            fotoPerfil.setClipToOutline(true);
                        }
                    }
                }
        );
    }

    public void openGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void salvarPerfil() {
        if (imagemSelecionadaUri != null) {
            uploadImageSDK(imagemSelecionadaUri);
        } else {
            atualizarUrl(null);
        }
    }

    private void uploadImageSDK(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .unsigned(uploadPreset)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        runOnUiThread(() ->
                                Toast.makeText(FotoPerfilActivity.this, "Enviando imagem...", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        url = (String) resultData.get("secure_url");
                        runOnUiThread(() -> {
                            atualizarUrl(url);
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        android.util.Log.e("CloudinaryUpload",
                                "reqId=" + requestId +
                                        " code=" + error.getCode() +
                                        " desc=" + error.getDescription());
                        runOnUiThread(() ->
                                Toast.makeText(FotoPerfilActivity.this,
                                        "Falha no upload (" + error.getCode() + "): " + error.getDescription(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        android.util.Log.w("CloudinaryUpload", "Reagendado: " + error.getDescription());
                    }
                })
                .dispatch(this);
    }

    private void atualizarUrl(String urlImagem) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("imagem", urlImagem);

        db.collection("perfil").document(usuarioId)
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Perfil salvo!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Não foi possível salvar o perfil.", Toast.LENGTH_SHORT).show());
    }
}
