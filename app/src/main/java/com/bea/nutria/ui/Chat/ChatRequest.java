package com.bea.nutria.ui.Chat;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    @SerializedName("cPrompt")
    private String pergunta;
    @SerializedName("nCdUser")
    private int idUser;

    public ChatRequest(){};

    public ChatRequest(String cPrompt, int nCdUser) {
        this.pergunta = cPrompt;
        this.idUser = nCdUser;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
}
