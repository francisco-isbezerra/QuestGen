package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta do buscar_desafio.php quando jogo_id > 0 (Modo Detalhes).
 *
 * JSON esperado:
 * {
 *   "status":        "Sucesso",
 *   "desafioId":     5,
 *   "jogoId":        2,
 *   "titulo":        "Marque 3 gols",
 *   "descricao":     "...",
 *   "recompensa":    100,
 *   "dificuldade":   3,
 *   "statusDesafio": "EM_CURSO"  ← null quando desafio novo (não aceito ainda)
 * }
 */
data class DesafioResponse(
    @SerializedName("desafioId")    val desafioId: Int,
    @SerializedName("jogoId")       val jogoId: Int,
    @SerializedName("titulo")       val titulo: String,
    @SerializedName("descricao")    val descricao: String,
    @SerializedName("recompensa")   val recompensa: Int,
    @SerializedName("dificuldade")  val dificuldade: Int,
    @SerializedName("statusDesafio")val status: String? = null
)