package com.bea.nutria.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Linha implements Parcelable {
    private final String nome;
    private final String valor;
    private final String vd;

    public Linha(String nome, String valor, String vd) {
        this.nome = nome;
        this.valor = valor;
        this.vd = vd;
    }

    protected Linha(Parcel in) {
        nome = in.readString();
        valor = in.readString();
        vd = in.readString();
    }

    public static final Creator<Linha> CREATOR = new Creator<Linha>() {
        @Override
        public Linha createFromParcel(Parcel in) {
            return new Linha(in);
        }

        @Override
        public Linha[] newArray(int size) {
            return new Linha[size];
        }
    };

    public String getNome() {
        return nome;
    }

    public String getValor() {
        return valor;
    }

    public String getVd() {
        return vd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
        dest.writeString(valor);
        dest.writeString(vd);
    }
}
