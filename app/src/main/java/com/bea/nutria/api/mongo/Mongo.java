package com.bea.nutria.api.mongo;

import android.app.Activity;
import android.util.Log;

import com.bea.nutria.api.ProdutoAPI; // Mantemos o import para o método getProdutoApi, mas é opcional

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Mongo {

    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000; // 60 segundos
    private static final String BASE_URL = "https://api-spring-mongodb.onrender.com/";

    private final String credenciais = Credentials.basic("nutria", "nutria123");

    private final OkHttpClient client;
    private final Retrofit retrofit;

    public Mongo() {
        // 1. Configura OkHttpClient
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

        // 2. Configura Retrofit (apenas a instância base)
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Retorna uma instância da interface Retrofit especificada.
     * Agora aceita qualquer interface (ProdutoAPI, UsuarioAPI, etc.).
     *
     * @param apiClass A interface da API (e.g., ProdutoAPI.class).
     * @param <T> O tipo da interface da API.
     * @return Uma instância da interface da API.
     */
    public <T> T getApi(Class<T> apiClass) {
        return retrofit.create(apiClass);
    }

    /**
     * Tenta "acordar" o servidor de backend hospedado no Render (ou similar).
     * Só executa o health check se a última tentativa for há mais de JANELA_WAKE_MS.
     * @param activity A Activity para garantir que o 'proximoPasso' seja executado na UI thread.
     * @param proximoPasso A ação a ser executada na UI thread após a tentativa de wake-up.
     */
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
                    }
                } catch (Exception e) {
                    Log.e("WakeUp", "Erro na tentativa " + tent + ": " + e.getMessage());
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            // Volta para a UI thread para executar o próximo passo
            activity.runOnUiThread(() -> { if (proximoPasso != null) proximoPasso.run(); });
        }).start();
    }
}