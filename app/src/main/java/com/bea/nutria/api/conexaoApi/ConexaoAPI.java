package com.bea.nutria.api.conexaoApi;

import android.app.Activity;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConexaoAPI {

    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;
    private static String BASE_URL = "";

    private final String credenciais = Credentials.basic("nutria", "nutria123");

    private final OkHttpClient client;
    private final Retrofit retrofit;

    public ConexaoAPI(String url) {
        BASE_URL = url;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d("API_LOG", message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
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
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public <T> T getApi(Class<T> apiClass) {
        return retrofit.create(apiClass);
    }

    public void iniciarServidor(Activity activity, Runnable proximoPasso) {
        long agora = System.currentTimeMillis();
        if (agora - ultimoWakeMs < JANELA_WAKE_MS) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }

        if (activity == null) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }

        new Thread(() -> {
            boolean ok = false;
            String healthCheckUrl = BASE_URL + "actuator/health";

            for (int tent = 1; tent <= 3 && !ok; tent++) {
                try {
                    Request req = new Request.Builder()
                            .url(healthCheckUrl)
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                        Log.d("WakeUp", "Tentativa " + tent + ": " + (ok ? "SUCESSO" : "FALHA") + " | Código: " + (resp != null ? resp.code() : "N/A"));

                        if (resp != null && !resp.isSuccessful() && resp.body() != null) {
                            String errorBody = resp.body().string();
                            Log.e("WakeUp", "Erro na resposta: " + errorBody);
                        }                    }
                } catch (Exception e) {
                    Log.e("WakeUp", "Erro na tentativa " + tent + ": " + e.getMessage());
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            activity.runOnUiThread(() -> {
                if (proximoPasso != null) proximoPasso.run();
            });
        }).start();
    }
    public void iniciandoServidor(Fragment fragment, Runnable proximoPasso) {
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
                            .url("https://api-spring-mongodb.onrender.com")
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                        Log.d("WakeUp", "Tentativa " + tent + ": " + (ok ? "SUCESSO" : "FALHA") + " | Código: " + (resp != null ? resp.code() : "N/A"));

                        if (resp != null && !resp.isSuccessful() && resp.body() != null) {
                            String errorBody = resp.body().string();
                            Log.e("WakeUp", "Erro na resposta: " + errorBody);
                        }                    }
                } catch (Exception ignore) {
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            if (fragment.isAdded()){
                fragment.requireActivity().runOnUiThread(() -> {
                    if (proximoPasso != null) proximoPasso.run();
                });
            }
        }).start();
    }
}
