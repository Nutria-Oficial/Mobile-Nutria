package com.bea.nutria.api;

import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.GetTabelaEAvaliacaoDTO;
import com.bea.nutria.model.ComparacaoNutrienteDTO;

import java.util.Map;
import java.util.List;

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

    // Busca de uma única tabela (método original)
    @GET("tabelas/{id}")
    Call<GetTabelaDTO> buscarTabela(@Path("id") Integer id);

    // NOVO MÉTODO: Busca a lista de comparação entre duas tabelas
    @GET("tabelas/{idTabela1}/{idTabela2}")
    Call<List<ComparacaoNutrienteDTO>> compararTabelas(
            @Path("idTabela1") Integer idTabela1,
            @Path("idTabela2") Integer idTabela2
    );
}
