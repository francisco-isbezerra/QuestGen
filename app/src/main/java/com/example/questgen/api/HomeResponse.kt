package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta do buscar_desafio.php quando jogo_id == 0 (Modo Home).
 *
 * Um único objeto com dados do perfil do usuário + desafio EM_CURSO via LEFT JOIN.
 * Os campos do desafio são nullable: se não há desafio ativo, serão null.
 *
 * JSON esperado:
 * {
 *   "status":       "Sucesso",
 *   "usuarioId":    1,
 *   "usuarioNome":  "João",
 *   "gameCoins":    150,
 *   "rankNome":     "Bronze I",
 *   "desafioId":    5,        ← null se sem desafio ativo
 *   "titulo":       "...",    ← null se sem desafio ativo
 *   "descricao":    "...",    ← null se sem desafio ativo
 *   "recompensa":   100,      ← null se sem desafio ativo
 *   "dificuldade":  3,        ← null se sem desafio ativo
 *   "statusDesafio":"EM_CURSO" ← null se sem desafio ativo
 * }
 */
data class HomeResponse(
    @SerializedName("status")       val status: String,
    @SerializedName("usuarioId")    val usuarioId: Int,
    @SerializedName("usuarioNome")  val usuarioNome: String,
    @SerializedName("gameCoins")    val gameCoins: Int,
    @SerializedName("rankNome")     val rankNome: String,
    // Campos do desafio — null quando não há desafio EM_CURSO
    @SerializedName("desafioId")    val desafioId: Int?    = null,
    @SerializedName("titulo")       val titulo: String?    = null,
    @SerializedName("descricao")    val descricao: String? = null,
    @SerializedName("recompensa")   val recompensa: Int?   = null,
    @SerializedName("dificuldade")  val dificuldade: Int?  = null,
    @SerializedName("statusDesafio")val statusDesafio: String? = null
) {
    /** Retorna true se há um desafio ativo para exibir no card da Home. */
    val temDesafioAtivo: Boolean get() = desafioId != null
}
