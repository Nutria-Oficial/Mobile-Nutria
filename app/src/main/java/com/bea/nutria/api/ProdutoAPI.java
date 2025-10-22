package com.bea.nutria.api;

import com.bea.nutria.model.GetProdutoDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProdutoAPI {
    //Buscar o usuario pelo email
    @GET("produtos/usuario/{id}?filtrar=true")
    Call<List<GetProdutoDTO>> buscarProdutosComMaisDeUmaTabela(@Path("id") Integer id);
}
