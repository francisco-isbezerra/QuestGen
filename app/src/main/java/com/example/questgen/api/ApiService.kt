package com.example.questgen.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ══════════════════════════════════════════════════════════════════
    // AUTENTICAÇÃO
    // ══════════════════════════════════════════════════════════════════

    /**
     * Autentica o usuário com e-mail + senha.
     * POST login.php  [email, senha]
     */
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("senha") senha: String
    ): Call<LoginResponse>

    /**
     * Recarrega dados frescos do perfil pelo ID (GET, rota do mesmo login.php).
     * GET  login.php?usuario_id=X
     */
    @GET("login.php")
    fun getUsuario(
        @Query("usuario_id") usuarioId: Int
    ): Call<LoginResponse>

    /**
     * Cadastra um novo usuário.
     * POST cadastro.php  [nome, email, senha]
     */
    @FormUrlEncoded
    @POST("cadastro.php")
    fun cadastrarUsuario(
        @Field("nome")  nome: String,
        @Field("email") email: String,
        @Field("senha") senha: String
    ): Call<CadastroResponse>

    // ══════════════════════════════════════════════════════════════════
    // JOGOS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Lista todos os jogos disponíveis (GameSelectionActivity).
     * GET buscar_jogos.php
     */
    @GET("buscar_jogos.php")
    fun buscarJogos(): Call<List<JogoResponse>>

    // ══════════════════════════════════════════════════════════════════
    // DESAFIOS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Função principal para buscar desafios (Modo Geral / Detalhes).
     * POST buscar_desafio.php [usuario_id, jogo_id]
     */
    @FormUrlEncoded
    @POST("buscar_desafio.php")
    fun buscarDesafio(
        @Field("usuario_id") usuarioId: Int,
        @Field("jogo_id")    jogoId: Int
    ): Call<DesafioResponse>

    /**
     * Modo Home (jogo_id = 0): retorna perfil do usuário + desafio EM_CURSO.
     * Retorna HomeResponse (perfil + desafio nullable via LEFT JOIN).
     * POST buscar_desafio.php  [usuario_id, jogo_id=0]
     */
    @FormUrlEncoded
    @POST("buscar_desafio.php")
    fun buscarDesafioHome(
        @Field("usuario_id") usuarioId: Int,
        @Field("jogo_id")    jogoId: Int = 0
    ): Call<HomeResponse>

    /**
     * Modo Detalhes (jogo_id > 0): busca desafio ativo ou sorteia novo.
     * Retorna DesafioResponse com statusDesafio (null = desafio novo).
     * POST buscar_desafio.php  [usuario_id, jogo_id]
     */
    @FormUrlEncoded
    @POST("buscar_desafio.php")
    fun buscarDesafioJogo(
        @Field("usuario_id") usuarioId: Int,
        @Field("jogo_id")    jogoId: Int
    ): Call<DesafioResponse>

    /**
     * Aceita um desafio → insere em USUARIO_DESAFIO com STATUS = 'EM_CURSO'.
     * POST aceitar_desafio.php  [usuario_id, desafio_id]
     */
    @FormUrlEncoded
    @POST("aceitar_desafio.php")
    fun aceitarDesafio(
        @Field("usuario_id") usuarioId: Int,
        @Field("desafio_id") desafioId: Int
    ): Call<AcaoResponse>

    /**
     * Reivindica recompensa → STATUS = 'REIVINDICADO' + soma GameCoins.
     * POST reivindicar_recompensa.php  [usuario_id, desafio_id, recompensa]
     */
    @FormUrlEncoded
    @POST("reivindicar_recompensa.php")
    fun reivindicarRecompensa(
        @Field("usuario_id") usuarioId: Int,
        @Field("desafio_id") desafioId: Int,
        @Field("recompensa") recompensa: Int
    ): Call<AcaoResponse>

    // ══════════════════════════════════════════════════════════════════
    // LOJA
    // ══════════════════════════════════════════════════════════════════

    /**
     * Lista produtos da loja.
     * GET buscar_loja.php
     */
    @GET("buscar_loja.php")
    fun buscarLoja(): Call<List<LojaResponse>>

    // ══════════════════════════════════════════════════════════════════
    // RANKING
    // ══════════════════════════════════════════════════════════════════

    /**
     * Ranking global de jogadores.
     * GET ranking.php
     */
    @GET("ranking.php")
    fun getRanking(): Call<List<RankingResponse>>
}