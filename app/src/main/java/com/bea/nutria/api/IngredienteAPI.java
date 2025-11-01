package com.bea.nutria.api;

import com.bea.nutria.ui.Ingrediente.Ingrediente;
import com.bea.nutria.ui.Ingrediente.IngredienteRequest;
import com.bea.nutria.ui.Ingrediente.IngredienteResponse;
import com.bea.nutria.ui.Ingrediente.PaginatedResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IngredienteAPI {

    @GET("ingredientes")
    Call<PaginatedResponse> getAllIngredientes(@Query("pagina") int pagina);

    @GET("ingredientes/nomeExato/{nome}")
    Call<List<IngredienteResponse>> getIngredientesPorNome(@Path("nome") String nome);

    @GET("ingredientes/{id}")
    Call<Ingrediente> getIngredienteById(@Path("id") Integer id);

    @POST("ingredientes")
    Call<IngredienteResponse> criarIngrediente(@Body IngredienteRequest ingrediente);
}