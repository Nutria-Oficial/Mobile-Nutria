package com.bea.nutria.model;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private Integer id;

    private Integer idUsuario;

    private Integer indiceChat;

    private List<String> listaUsuario = new ArrayList<>();

    private List<String> listaBot = new ArrayList<>();
    public Chat(){
    }

    public Chat(Integer id, Integer idUsuario, Integer indiceChat, List<String> listaUsuario, List<String> listaBot) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.indiceChat = indiceChat;
        this.listaUsuario = listaUsuario;
        this.listaBot = listaBot;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIndiceChat() {
        return indiceChat;
    }

    public void setIndiceChat(Integer indiceChat) {
        this.indiceChat = indiceChat;
    }

    public List<String> getListaUsuario() {
        return listaUsuario;
    }

    public void setListaUsuario(List<String> listaUsuario) {
        this.listaUsuario = listaUsuario;
    }

    public List<String> getListaBot() {
        return listaBot;
    }

    public void setListaBot(List<String> listaBot) {
        this.listaBot = listaBot;
    }
}
