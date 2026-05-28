package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta genérica usada por:
 * - aceitar_desafio.php    → { "status": "Sucesso"|"Erro", "mensagem": "..." }
 * - reivindicar_recompensa.php → { "status": "Sucesso"|"Erro", "mensagem": "...", "novosCoins": 350 }
 */
data class AcaoResponse(
    @SerializedName("status")    val status: String,
    @SerializedName("mensagem")  val mensagem: String,
    @SerializedName("novosCoins")val novosCoins: Int? = null
)
