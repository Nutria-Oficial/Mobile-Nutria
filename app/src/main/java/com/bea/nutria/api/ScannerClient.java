package com.bea.nutria.api;

import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScannerClient {

    private static final String BASE_URL = "https://nutria-fast-api.koyeb.app/";

    public static ScannerAPI createService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ScannerAPI.class);
    }

    public static void enviarImagem(File file, String nomeIngrediente, android.content.Context context) {
        ScannerAPI api = createService();

        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<ScannerAPI.ScannerResultadoDTO> call = api.enviarScanner(body, nomeIngrediente);

        call.enqueue(new Callback<ScannerAPI.ScannerResultadoDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScannerAPI.ScannerResultadoDTO> call, @NonNull Response<ScannerAPI.ScannerResultadoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Scanner OK: " + response.body().nomeIngrediente, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Erro no servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("Scanner", "Erro " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScannerAPI.ScannerResultadoDTO> call, @NonNull Throwable t) {
                Toast.makeText(context, "Falha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Scanner", "Falha no envio", t);
            }
        });
    }
}
