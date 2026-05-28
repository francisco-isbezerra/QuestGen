package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta do buscar_jogos.php (GET)
 *
 * Campos alinhados com os aliases camelCase do SQL:
 *   JOGO_ID         AS jogoId
 *   JOGO_NOME       AS jogoNome
 *   JOGO_IMAGEM_URL AS jogoImagemUrl
 */
data class JogoResponse(
    @SerializedName("jogoId")       val jogoId: Int,
    @SerializedName("jogoNome")     val jogoNome: String,
    @SerializedName("jogoImagemUrl")val jogoImagemUrl: String
)
