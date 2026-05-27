package com.example.questgen.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    /**
     * Busca dados do usuário pelo ID.
     * Exemplo: GET login.php?usuario_id=1
     */
    @GET("login.php")
    fun getUsuario(
        @Query("usuario_id") usuarioId: Int
    ): Call<LoginResponse>

    /**
     * Gera um desafio para o usuário em um determinado jogo.
     * Exemplo: GET gerar_desafio.php?usuario_id=1&jogo_id=1
     */
    @GET("gerar_desafio.php")
    fun gerarDesafio(
        @Query("usuario_id") usuarioId: Int,
        @Query("jogo_id")    jogoId: Int
    ): Call<DesafioResponse>

    /**
     * Busca o ranking global de jogadores.
     * Exemplo: GET ranking.php
     */
    @GET("ranking.php")
    fun getRanking(): Call<List<RankingResponse>>
}