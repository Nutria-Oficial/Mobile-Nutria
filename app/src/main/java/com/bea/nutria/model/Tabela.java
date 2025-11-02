package com.bea.nutria.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Tabela implements Parcelable {
    private final String titulo;
    private final String porcaoTexto;
    private final List<Linha> linhas;
    private final long idEstavel;

    public Tabela(String titulo, String porcaoTexto, List<Linha> linhas, long idEstavel) {
        this.titulo = titulo;
        this.porcaoTexto = porcaoTexto;
        this.linhas = linhas;
        this.idEstavel = idEstavel;
    }

    protected Tabela(Parcel in) {
        titulo = in.readString();
        porcaoTexto = in.readString();
        linhas = new ArrayList<>();
        in.readList(linhas, Linha.class.getClassLoader());
        idEstavel = in.readLong();
    }

    public static final Creator<Tabela> CREATOR = new Creator<Tabela>() {
        @Override
        public Tabela createFromParcel(Parcel in) {
            return new Tabela(in);
        }

        @Override
        public Tabela[] newArray(int size) {
            return new Tabela[size];
        }
    };

    public String getTitulo() {
        return titulo;
    }

    public String getPorcaoTexto() {
        return porcaoTexto;
    }

    public List<Linha> getLinhas() {
        return linhas;
    }

    public long getIdEstavel() {
        return idEstavel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(porcaoTexto);
        dest.writeList(linhas);
        dest.writeLong(idEstavel);
    }
}
