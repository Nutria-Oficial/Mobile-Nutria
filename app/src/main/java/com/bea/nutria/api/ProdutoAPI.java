package com.bea.nutria.api;

import com.bea.nutria.model.GetProdutoDTO;
// Importação necessária para o novo método, assumindo que GetTabelaDTO está em outro pacote.
import com.bea.nutria.model.GetTabelaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProdutoAPI {

    // Método anterior (manter)
    @GET("produtos/usuario/{id}?filtrar=false")
    Call<List<GetProdutoDTO>> buscarProdutosComMaisDeUmaTabela(@Path("id") Integer id);

    // ✨ NOVO MÉTODO: Para buscar todas as tabelas de um produto por produtoId
    @GET("produtos/{id}")
    Call<List<GetTabelaDTO>> buscarTodasTabelasDoProduto(@Path("id") Integer produtoId);
}