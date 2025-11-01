package com.bea.nutria.api;

import com.bea.nutria.ui.Chat.ChatRequest;
import com.bea.nutria.ui.Chat.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatAPIFast {
    //usando da fast api
    @POST("chatbot/")
    Call<ChatResponse> enviarMensagemPegarResposta(@Body ChatRequest chatRequest);
}
