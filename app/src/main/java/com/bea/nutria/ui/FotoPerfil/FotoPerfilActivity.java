package com.bea.nutria.ui.FotoPerfil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.R;
import com.google.android.material.button.MaterialButton;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class FotoPerfilActivity extends AppCompatActivity {

    private final String cloudName = "dtvvd7xif";
    private final String uploadPreset = "perfil_app";
    private final String folder = "fotosAjustadas";

    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private ImageView fotoPerfil;
    private ImageView btnAddFoto;
    private TextView boasVindas;
    private TextView pular;
    private MaterialButton btnProximo;

    private Uri imagemSelecionadaUri = null;
    private String nomeUsuario;

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
        pular = findViewById(R.id.pular);
        btnProximo = findViewById(R.id.btn_proximo);

        nomeUsuario = getIntent().getStringExtra("nome");
        if (!TextUtils.isEmpty(nomeUsuario)) {
            boasVindas.setText("Olá, " + nomeUsuario + "!");
        }

        btnAddFoto.setOnClickListener(v -> openGallery());
        btnProximo.setOnClickListener(v -> {
            if (imagemSelecionadaUri != null) {
                uploadImageSDK(imagemSelecionadaUri);
            } else {
                // usuário não escolheu foto retorna sem URL
                finalizarComUrl(null);
            }
        });
        pular.setOnClickListener(v -> finalizarComUrl(null));
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
                r -> {
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES});
        } else {
            requestPermissions.launch(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageSDK(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .unsigned(uploadPreset)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        runOnUiThread(() -> Toast.makeText(FotoPerfilActivity.this, "Enviando imagem...", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        runOnUiThread(() -> finalizarComUrl(url));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() ->
                                Toast.makeText(FotoPerfilActivity.this,
                                        "Falha no upload (" + error.getCode() + "): " + error.getDescription(),
                                        Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch(this);
    }

    private void finalizarComUrl(String urlPublica) {
        Intent data = new Intent();
        data.putExtra("urlFoto", urlPublica);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
