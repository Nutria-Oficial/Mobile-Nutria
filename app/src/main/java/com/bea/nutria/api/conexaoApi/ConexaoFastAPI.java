package com.bea.nutria.api.conexaoApi;

import android.app.Activity;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConexaoFastAPI {

    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;
    private String baseUrl = "";

    private final OkHttpClient client;
    private final Retrofit retrofit;

    public ConexaoFastAPI(String url) {
        this.baseUrl = url;

        // OkHttpClient SEM autenticação (FastAPI não precisa)
        client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(req);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public <T> T getApi(Class<T> apiClass) {
        return retrofit.create(apiClass);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}