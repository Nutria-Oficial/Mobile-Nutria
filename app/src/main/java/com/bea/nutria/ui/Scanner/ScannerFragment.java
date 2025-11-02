package com.bea.nutria.ui.Scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.text.Normalizer;

import android.util.Log;
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
import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.api.ScannerAPI;
import com.bea.nutria.api.ScannerClient;
import com.bea.nutria.ui.Ingrediente.Ingrediente;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 42;
    private static final String TAG = "ScannerFragment";

    private FrameLayout previewContainer;
    private PreviewView previewView;
    private LifecycleCameraController cameraController;
    private ImageButton btnTirarFoto;
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
        RequestBody requestFile = RequestBody.create(foto, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", foto.getName(), requestFile);

        ScannerAPI scannerAPI = ScannerClient.createScannerService();
        Call<ScannerAPI.ScannerResponseDTO> call = scannerAPI.enviarScanner(body, nomeIngrediente);

        call.enqueue(new Callback<ScannerAPI.ScannerResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScannerAPI.ScannerResponseDTO> call,
                                   @NonNull Response<ScannerAPI.ScannerResponseDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    ScannerAPI.ScannerResponseDTO dto = response.body();

                    Log.d(TAG, "========== SCANNER RESPONSE ==========");
                    Log.d(TAG, "Message: " + dto.message);
                    Log.d(TAG, "ID novo: " + dto.id_novo);
                    Log.d(TAG, "======================================");

                    if (dto.id_novo != null) {
                        // Busca pelo ID com retry
                        buscarDetalhesIngredientePorId(dto.id_novo);
                    } else {
                        mostrarCarregando(false);
                        Toast.makeText(requireContext(),
                                "Erro: ID do ingrediente não retornado pelo servidor",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    mostrarCarregando(false);
                    Log.e(TAG, "Erro no servidor: " + response.code());
                    Toast.makeText(requireContext(),
                            "Erro no servidor: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScannerAPI.ScannerResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarCarregando(false);
                Log.e(TAG, "Falha no envio do scanner", t);
                Toast.makeText(requireContext(),
                        "Falha ao enviar: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buscarDetalhesIngredientePorId(Integer idIngrediente) {
        buscarComRetry(idIngrediente, 0, 5); // 5 tentativas
    }

    private void buscarComRetry(Integer idIngrediente, int tentativa, int maxTentativas) {
        if (tentativa >= maxTentativas) {
            mostrarCarregando(false);
            Toast.makeText(requireContext(),
                    "Não foi possível recuperar o ingrediente após " + maxTentativas + " tentativas. " +
                            "ID: " + idIngrediente,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Delay progressivo: 2s, 4s, 6s, 8s, 10s
        long delay = (tentativa + 1) * 2000L;

        Log.d(TAG, "Aguardando " + (delay / 1000) + "s antes da tentativa " + (tentativa + 1) + " de " + maxTentativas);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Iniciando tentativa " + (tentativa + 1) + " - ID: " + idIngrediente);

            // Agora usa o ScannerClient que já tem autenticação configurada
            IngredienteAPI ingredienteAPI = ScannerClient.createIngredienteService();
            Call<Ingrediente> call = ingredienteAPI.getIngredienteById(idIngrediente);

            Log.d(TAG, "URL da requisição: " + call.request().url());

            call.enqueue(new Callback<Ingrediente>() {
                @Override
                public void onResponse(@NonNull Call<Ingrediente> call,
                                       @NonNull Response<Ingrediente> response) {
                    if (!isAdded()) return;

                    Log.d(TAG, "========== RESPOSTA GET BY ID (Tentativa " + (tentativa + 1) + ") ==========");
                    Log.d(TAG, "Status code: " + response.code());
                    Log.d(TAG, "URL: " + call.request().url());

                    if (response.isSuccessful() && response.body() != null) {
                        mostrarCarregando(false);
                        Ingrediente ingrediente = response.body();

                        Log.d(TAG, "✓ Ingrediente recebido com sucesso!");
                        Log.d(TAG, "ID: " + ingrediente.getId());
                        Log.d(TAG, "Nome: " + ingrediente.getNomeIngrediente());
                        Log.d(TAG, "Calorias: " + ingrediente.getCaloria());
                        Log.d(TAG, "========================================");

                        abrirResultadoFragment(ingrediente);
                    } else if (response.code() == 404) {
                        // Ingrediente ainda não está disponível, tentar novamente
                        Log.w(TAG, "✗ Ingrediente ainda não disponível (404), tentando novamente...");
                        Log.d(TAG, "========================================");
                        buscarComRetry(idIngrediente, tentativa + 1, maxTentativas);
                    } else if (response.code() == 401) {
                        // Erro de autenticação
                        mostrarCarregando(false);
                        Log.e(TAG, "✗ Erro de autenticação (401)!");
                        Log.e(TAG, "Verifique as credenciais no ScannerClient");
                        Log.e(TAG, "========================================");
                        Toast.makeText(requireContext(),
                                "Erro de autenticação. Verifique as credenciais da API.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "✗ Erro na resposta!");
                        Log.e(TAG, "Status code: " + response.code());

                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "sem corpo de erro";
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler error body", e);
                        }

                        Log.e(TAG, "========================================");

                        // Tentar novamente se for erro de servidor (5xx)
                        if (response.code() >= 500) {
                            buscarComRetry(idIngrediente, tentativa + 1, maxTentativas);
                        } else {
                            mostrarCarregando(false);
                            Toast.makeText(requireContext(),
                                    "Erro ao buscar ingrediente: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Ingrediente> call, @NonNull Throwable t) {
                    if (!isAdded()) return;

                    Log.e(TAG, "========== FALHA NA REQUISIÇÃO (Tentativa " + (tentativa + 1) + ") ==========");
                    Log.e(TAG, "URL: " + call.request().url());
                    Log.e(TAG, "Erro: " + t.getMessage(), t);
                    Log.e(TAG, "=========================================");

                    // Tentar novamente em caso de falha de rede
                    buscarComRetry(idIngrediente, tentativa + 1, maxTentativas);
                }
            });
        }, delay);
    }

    private void abrirResultadoFragment(Ingrediente ing) {
        Bundle b = new Bundle();
        b.putString("nomeIngrediente", ing.getNomeIngrediente() != null ? ing.getNomeIngrediente() : "Ingrediente");
        b.putString("porcao", "100g");

        // Converter todos os campos do Ingrediente para NutrienteDTO
        ArrayList<ScannerAPI.NutrienteDTO> nutrientesDTO = new ArrayList<>();

        // Mapa com nome do nutriente -> valor
        LinkedHashMap<String, Double> nutrientes = new LinkedHashMap<>();
        nutrientes.put("Calorias", ing.getCaloria());
        nutrientes.put("Carboidratos", ing.getCarboidrato());
        nutrientes.put("Açúcar", ing.getAcucar());
        nutrientes.put("Proteína", ing.getProteina());
        nutrientes.put("Gordura Total", ing.getGorduraTotal());
        nutrientes.put("Gordura Saturada", ing.getGorduraSaturada());
        nutrientes.put("Gordura Monoinsaturada", ing.getGorduraMonoinsaturada());
        nutrientes.put("Gordura Poliinsaturada", ing.getGorduraPoliinsaturada());
        nutrientes.put("Colesterol", ing.getColesterol());
        nutrientes.put("Sódio", ing.getSodio());
        nutrientes.put("Fibra", ing.getFibra());
        nutrientes.put("Água", ing.getAgua());
        nutrientes.put("Álcool", ing.getAlcool());
        nutrientes.put("Cálcio", ing.getCalcio());
        nutrientes.put("Fósforo", ing.getFosforo());
        nutrientes.put("Magnésio", ing.getMagnesio());
        nutrientes.put("Potássio", ing.getPotassio());
        nutrientes.put("Ferro", ing.getFerro());
        nutrientes.put("Zinco", ing.getZinco());
        nutrientes.put("Cobre", ing.getCobre());
        nutrientes.put("Selênio", ing.getSelenio());
        nutrientes.put("Vitamina B6", ing.getVitaminaB6());
        nutrientes.put("Vitamina B12", ing.getVitaminaB12());
        nutrientes.put("Vitamina C", ing.getVitaminaC());
        nutrientes.put("Vitamina D", ing.getVitaminaD());
        nutrientes.put("Vitamina E", ing.getVitaminaE());
        nutrientes.put("Vitamina K", ing.getVitaminaK());
        nutrientes.put("Retinol", ing.getRetinol());
        nutrientes.put("Tiamina", ing.getTiamina());
        nutrientes.put("Riboflavina", ing.getRiboflavina());
        nutrientes.put("Niacina", ing.getNiacina());
        nutrientes.put("Folato", ing.getFolato());
        nutrientes.put("Colina", ing.getColina());
        nutrientes.put("Teobromina", ing.getTeobromina());
        nutrientes.put("Cafeína", ing.getCafeina());

        // Converter para DTOs, ignorando valores zero
        for (Map.Entry<String, Double> entry : nutrientes.entrySet()) {
            double valor = entry.getValue();
            if (valor != 0.0) {
                ScannerAPI.NutrienteDTO dto = new ScannerAPI.NutrienteDTO();
                dto.nome = entry.getKey();
                dto.valor = formatarValor(entry.getKey(), valor);
                dto.vd = calcularVD(entry.getKey(), valor);
                nutrientesDTO.add(dto);
            }
        }

        Log.d(TAG, "Total de nutrientes com valor: " + nutrientesDTO.size());
        b.putSerializable("nutrientes", nutrientesDTO);

        ResultadoFragment fragment = new ResultadoFragment();
        fragment.setArguments(b);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    private String formatarValor(String nomeNutriente, double valor) {
        if (nomeNutriente.equals("Calorias")) {
            return String.format("%.0f kcal", valor);
        } else if (nomeNutriente.contains("Vitamina") ||
                nomeNutriente.equals("Selênio") ||
                nomeNutriente.equals("Folato") ||
                nomeNutriente.equals("Retinol")) {
            return String.format("%.2f µg", valor);
        } else if (nomeNutriente.equals("Colesterol") ||
                nomeNutriente.equals("Sódio") ||
                nomeNutriente.equals("Cálcio") ||
                nomeNutriente.equals("Fósforo") ||
                nomeNutriente.equals("Magnésio") ||
                nomeNutriente.equals("Potássio") ||
                nomeNutriente.equals("Ferro") ||
                nomeNutriente.equals("Zinco") ||
                nomeNutriente.equals("Cobre")) {
            return String.format("%.2f mg", valor);
        } else {
            return String.format("%.2f g", valor);
        }
    }

    private String calcularVD(String nomeNutriente, double valor) {
        double vd = 0;

        switch (nomeNutriente) {
            case "Calorias":
                vd = (valor / 2000) * 100;
                break;
            case "Carboidratos":
                vd = (valor / 300) * 100;
                break;
            case "Proteína":
                vd = (valor / 75) * 100;
                break;
            case "Gordura Total":
                vd = (valor / 55) * 100;
                break;
            case "Gordura Saturada":
                vd = (valor / 22) * 100;
                break;
            case "Fibra":
                vd = (valor / 25) * 100;
                break;
            case "Sódio":
                vd = (valor / 2400) * 100;
                break;
            case "Cálcio":
                vd = (valor / 1000) * 100;
                break;
            case "Ferro":
                vd = (valor / 14) * 100;
                break;
            case "Vitamina C":
                vd = (valor / 45) * 100;
                break;
            case "Vitamina D":
                vd = (valor / 5) * 100;
                break;
            default:
                return "-";
        }

        return vd > 0 ? String.format("%.0f%%", vd) : "-";
    }

    private void mostrarCarregando(boolean mostrar) {
        if (progressBarEnvio != null)
            progressBarEnvio.setVisibility(mostrar ? View.VISIBLE : View.GONE);
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