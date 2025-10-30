package com.bea.nutria.ui.Chat;

import com.google.gson.annotations.SerializedName;


public class ChatResponse {
    @SerializedName("Pergunta")
    private String pergunta;
    @SerializedName("Resposta")
    private String resposta;

    public ChatResponse() {
    }

    public ChatResponse(String pergunta, String resposta) {
        this.pergunta = pergunta;
        this.resposta = resposta;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
}
