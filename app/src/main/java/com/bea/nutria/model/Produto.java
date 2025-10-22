package com.bea.nutria.api;

import com.bea.nutria.model.Tabela;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Produto {
    @SerializedName("_id")
    private String id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("porcao")
    private String porcao; // se vier, beleza. senão, ignora.

    @SerializedName("tabelas")
    private List<Tabela> tabelas;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getPorcao() { return porcao; }
    public List<Tabela> getTabelas() { return tabelas; }
}