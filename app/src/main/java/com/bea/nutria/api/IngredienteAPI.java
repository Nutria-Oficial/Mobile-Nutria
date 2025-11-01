package com.bea.nutria.api;

import com.bea.nutria.ui.Ingrediente.Ingrediente;
import com.bea.nutria.ui.Ingrediente.IngredienteRequest;
import com.bea.nutria.ui.Ingrediente.IngredienteResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IngredienteAPI {
    @GET("ingredientes")
    Call<List<IngredienteResponse>> getAllIngredientes(int pagina);

    @GET("ingredientes/{id}")
    Call<Ingrediente> getIngredienteById(@Path("id") Integer id);

    @POST("ingredientes")
    Call<IngredienteResponse> criarIngrediente(@Body IngredienteRequest ingrediente);
}
