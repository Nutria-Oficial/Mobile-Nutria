package com.bea.nutria.ui.Scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import java.text.Normalizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bea.nutria.R;
import com.bea.nutria.api.ScannerAPI;
import com.bea.nutria.api.ScannerClient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 42;

    private FrameLayout previewContainer;
    private PreviewView previewView;
    private LifecycleCameraController cameraController;
    private ImageButton btnTirarFoto;

    // overlay loading (ids existem no seu fragment_scanner.xml)
    private ProgressBar progressBarEnvio;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewContainer = view.findViewById(R.id.previewContainer);
        btnTirarFoto = view.findViewById(R.id.btnTirarFoto);
        progressBarEnvio = view.findViewById(R.id.progressBarEnvio);

        btnTirarFoto.setOnClickListener(v -> tirarFoto());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private void iniciarCamera() {
        previewView = new PreviewView(requireContext());
        previewView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        previewContainer.removeAllViews();
        previewContainer.addView(previewView);

        cameraController = new LifecycleCameraController(requireContext());
        cameraController.bindToLifecycle(getViewLifecycleOwner());
        cameraController.setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE);
        previewView.setController(cameraController);
    }

    private void tirarFoto() {
        if (cameraController == null) {
            Toast.makeText(requireContext(), "Câmera não inicializada", Toast.LENGTH_SHORT).show();
            return;
        }

        File fotoFile = new File(requireContext().getCacheDir(),
                "foto_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(fotoFile).build();

        cameraController.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        abrirPopupNome(fotoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(), "Erro ao salvar foto: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void abrirPopupNome(File fotoFile) {
        PopUpScanner dialog = PopUpScanner.newInstance();
        dialog.setOnNomeConfirmadoListener(nome -> {
            mostrarCarregando(true);
            File compactada = compactarImagem(fotoFile, 1280, 80);
            enviarParaServidor(compactada, normalizar(nome));
        });
        dialog.show(getParentFragmentManager(), "PopUpScanner");
    }

    private void enviarParaServidor(File foto, String nomeIngrediente) {
        // monta multipart
        RequestBody requestFile = RequestBody.create(foto, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", foto.getName(), requestFile);

        // cria chamada Retrofit
        ScannerAPI api = ScannerClient.createService();

        // dá um tempo humano para back-end respirar (timeouts do cliente ficam no OkHttp padrão)
        Call<ScannerAPI.ScannerResultadoDTO> call =
                api.enviarScanner(body, nomeIngrediente);

        call.enqueue(new Callback<ScannerAPI.ScannerResultadoDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScannerAPI.ScannerResultadoDTO> call,
                                   @NonNull Response<ScannerAPI.ScannerResultadoDTO> response) {
                if (!isAdded()) return;
                mostrarCarregando(false);

                if (response.isSuccessful() && response.body() != null) {
                    abrirResultadoFragment(response.body());
                } else {
                    Toast.makeText(requireContext(),
                            "Servidor: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScannerAPI.ScannerResultadoDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarCarregando(false);
                Toast.makeText(requireContext(),
                        "Falha: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirResultadoFragment(ScannerAPI.ScannerResultadoDTO dto) {
        Bundle b = new Bundle();
        b.putString("nomeIngrediente", dto.nomeIngrediente != null ? dto.nomeIngrediente : "");
        b.putString("porcao", dto.porcao != null ? dto.porcao : "");
        b.putSerializable("nutrientes",
                dto.nutrientes != null ? new ArrayList<>(dto.nutrientes) : null);

        ResultadoFragment fragment = new ResultadoFragment();
        fragment.setArguments(b);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void mostrarCarregando(boolean mostrar) {
        if (progressBarEnvio != null) progressBarEnvio.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private String normalizar(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.trim().toLowerCase();
    }

    private File compactarImagem(File input, int maxLado, int qualidadeJpeg) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(input.getAbsolutePath(), bounds);
            int w = bounds.outWidth, h = bounds.outHeight;

            int maior = Math.max(w, h);
            int inSample = 1;
            while (maior / inSample > maxLado) inSample *= 2;

            BitmapFactory.Options decode = new BitmapFactory.Options();
            decode.inSampleSize = inSample;
            Bitmap bmp = BitmapFactory.decodeFile(input.getAbsolutePath(), decode);
            if (bmp == null) return input;

            float ratio = Math.max((float) bmp.getWidth() / maxLado, (float) bmp.getHeight() / maxLado);
            Bitmap out = bmp;
            if (ratio > 1f) {
                int newW = Math.round(bmp.getWidth() / ratio);
                int newH = Math.round(bmp.getHeight() / ratio);
                out = Bitmap.createScaledBitmap(bmp, newW, newH, true);
                if (out != bmp) bmp.recycle();
            }

            File outFile = new File(requireContext().getCacheDir(),
                    "upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(outFile);
            out.compress(Bitmap.CompressFormat.JPEG, qualidadeJpeg, fos);
            fos.flush();
            fos.close();
            out.recycle();
            return outFile;
        } catch (Exception e) {
            return input;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamera();
            } else {
                Toast.makeText(requireContext(), "Permissão da câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        previewView = null;
        cameraController = null;
        super.onDestroyView();
    }
}
