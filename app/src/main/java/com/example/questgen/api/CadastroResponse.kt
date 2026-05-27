package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Mapeia o JSON retornado pelo cadastro.php.
 * Exemplo de resposta de sucesso:
 *   { "status": "Sucesso", "mensagem": "Usuário cadastrado com sucesso!" }
 * Exemplo de resposta de erro:
 *   { "status": "Erro", "mensagem": "E-mail já cadastrado." }
 */
data class CadastroResponse(
    @SerializedName("status")   val status: String,
    @SerializedName("mensagem") val mensagem: String
)
