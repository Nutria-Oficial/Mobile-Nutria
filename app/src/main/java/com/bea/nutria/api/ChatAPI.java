package com.bea.nutria.api;

import com.bea.nutria.ui.Chat.ChatRequest;
import com.bea.nutria.ui.Chat.ChatResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatAPI {
    @GET("chat/{id}")
    Call<String> pegarRespostaIA(@Path("id") Integer id);

    @GET("chat/{id}")
    Call<List<String>> listarChat(@Path("id") Integer id);

    @GET("chat/{id}")
    Call<Void> limparChat(@Path("id") Integer id);

    @POST("chatbot/")
    Call<ChatResponse> enviarMensagemPegarResposta(@Body ChatRequest chatRequest);
}
