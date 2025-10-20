package com.bea.nutria.api;

import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.GetTabelaEAvaliacaoDTO;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TabelaAPI {
    @POST("/tabelas/{idUsuario}")
    Call<GetTabelaDTO> criarTabela(@Path("idUsuario") Integer idUsuario, @Body Map<String, Object> tabela);

    @POST("/tabelas/{idUsuario}/{idProduto}")
    Call<GetTabelaDTO> adicionarTabela(@Path("idUsuario") Integer idUsuario, @Path("idProduto") Integer idProduto, @Body Map<String, Object> tabela);

    @GET("/tabelas/{idTabela}")
    Call<GetTabelaEAvaliacaoDTO> buscarTabelaComAvaliacao(@Path("idTabela") Integer idTabela);

}
