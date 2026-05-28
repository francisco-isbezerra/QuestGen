package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta unificada do login.php (POST por email + senha).
 *
 * Sucesso: {
 *   "status": "Sucesso",
 *   "mensagem": "Login realizado com sucesso!",
 *   "usuarioId": 3,
 *   "usuarioNome": "João",
 *   "usuarioEmail": "joao@email.com",
 *   "gameCoins": 150,
 *   "winRate": 72,
 *   "rankNome": "Ouro"
 * }
 *
 * Erro: {
 *   "status": "Erro",
 *   "mensagem": "E-mail ou senha incorretos.",
 *   "usuarioId": null, ...
 * }
 */
data class LoginResponse(
    @SerializedName("status")      val status: String       = "",
    @SerializedName("mensagem")    val mensagem: String     = "",
    @SerializedName("usuarioId")   val usuarioId: Int?      = null,
    @SerializedName("usuarioNome") val usuarioNome: String? = null,
    @SerializedName("usuarioEmail")val usuarioEmail: String? = null,
    @SerializedName("gameCoins")   val gameCoins: Int?      = null,
    @SerializedName("winRate")     val winRate: Int?        = null,
    @SerializedName("rankNome")    val rankNome: String?    = null
)