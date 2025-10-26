package com.bea.nutria.api;

import com.bea.nutria.model.GetTabelaDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TabelaAPI {
    // ALTERAÇÃO CRUCIAL: Espera UM OBJETO (sem List)
    @GET("tabelas/{id}")
    Call<GetTabelaDTO> buscarTabela(@Path("id") Integer id); // <--- MUDANÇA AQUI
}