package com.bea.nutria.api;

import java.io.Serializable;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ScannerAPI {

    class NutrienteDTO implements Serializable {
        public String nome;
        public String valor;
        public String vd;
    }

    class ScannerResultadoDTO implements Serializable {
        public String nomeIngrediente;
        public String porcao;
        public List<NutrienteDTO> nutrientes;
    }

    @Multipart
    @POST("scanner/")  // sem barra inicial pra evitar duplicação na baseUrl
    Call<ScannerResultadoDTO> enviarScanner(
            @Part MultipartBody.Part file,
            @Query("nome_ingrediente") String nomeIngrediente  // só esse param, como o backend pede
    );
}
