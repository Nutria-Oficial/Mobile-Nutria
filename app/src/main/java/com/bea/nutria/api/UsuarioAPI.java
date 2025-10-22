package com.bea.nutria.api;

import com.bea.nutria.model.Usuario;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

//alteraçãoa teste
public interface UsuarioAPI {
    //Busca o usuario pelo email
    @GET("usuarios/{email}")
    Call<Usuario> buscarUsuario(@Path("email") String email);

    //Cadastra o usuario
    @POST("usuarios")
    Call<Usuario> cadastrarUsuario(@Body Usuario usuario);

    //Atualiza o email do usuario
    @PATCH("usuarios/{id}/email")
    Call<Usuario> atualizarEmailUsuario(@Path("id") Integer id, @Body Map<String, String> email);

    //Atualiza o telefone do usuario
    @PATCH("usuarios/{id}/telefone")
    Call<Usuario> atualizarTelefoneUsuario(@Path("id") Integer id, @Body Map<String, String> telefone);

    //Atualiza a senha do usuario
    @PATCH("usuarios/{id}/senha")
    Call<Usuario> atualizarSenhaUsuario(@Path("id") Integer id, @Body Map<String, String> senha);

    //Atualiza a foto do usuario
    @PATCH("usuarios/{id}/foto")
    Call<Usuario> atualizarFotoUsuario(@Path("id") Integer id, @Body Map<String, String> foto);

    //Atualiza o nome do usuario
    @PATCH("usuarios/{id}/nome")
    Call<Usuario> atualizarNomeUsuario(@Path("id") Integer id, @Body Map<String, String> nome);

    //Atualiza a empresa do usuario
    @PATCH("usuarios/{id}/empresa")
    Call<Usuario> atualizarEmpresaUsuario(@Path("id") Integer id, @Body Map<String, String> empresa);


}
