package com.bea.nutria.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    @SerializedName(value = "id", alternate = {"idUsuario", "usuarioId", "ncdusuario"})
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String empresa;

    @SerializedName("foto")
    private String urlFoto;

    public Usuario(Integer id, String nome, String email, String senha, String telefone, String empresa, String urlFoto) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.empresa = empresa;
        this.urlFoto = urlFoto;
    }

    public Usuario(String nome, String email, String senha, String telefone, String empresa, String urlFoto) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.empresa = empresa;
        this.urlFoto = urlFoto;
    }

    public Usuario() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }
}
