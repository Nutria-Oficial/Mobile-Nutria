package com.bea.nutria.api;

import com.bea.nutria.model.GetTabelaDTO;
import com.bea.nutria.model.ComparacaoNutrienteDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface TabelaAPI {
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
