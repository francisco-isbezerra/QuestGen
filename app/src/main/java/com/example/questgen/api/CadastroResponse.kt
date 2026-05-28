package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta do cadastro.php
 * Sucesso: { "status": "Sucesso", "mensagem": "...", "usuarioNome": "João" }
 * Erro:    { "status": "Erro",    "mensagem": "E-mail já cadastrado." }
 */
data class CadastroResponse(
    @SerializedName("status")      val status: String,
    @SerializedName("mensagem")    val mensagem: String,
    @SerializedName("usuarioNome") val usuarioNome: String? = null
)
