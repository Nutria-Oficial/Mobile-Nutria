package com.bea.nutria.api;

import com.bea.nutria.model.GetProdutoDTO;
// Importação necessária para o novo método, assumindo que GetTabelaComparacaoDTO está em outro pacote.
import com.bea.nutria.model.GetTabelaComparacaoDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProdutoAPI {

    @GET("produtos/usuario/{id}?filtrar=true")
    Call<List<GetProdutoDTO>> buscarProdutosComMaisDeUmaTabela(@Path("id") Integer id);

    @GET("produtos/{id}")
    Call<List<GetTabelaComparacaoDTO>> buscarTodasTabelasDoProduto(@Path("id") Integer produtoId);
}