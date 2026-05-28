package com.example.questgen.api

import com.google.gson.annotations.SerializedName

/**
 * Resposta do buscar_loja.php
 * Representa um item na vitrine da LojaActivity.
 */
data class LojaResponse(
    @SerializedName("PRODUTO_ID")     val produtoId: Int,
    @SerializedName("PRODUTO_NOME")   val produtoNome: String,
    @SerializedName("PRODUTO_PRECO")  val produtoPreco: Int,
    @SerializedName("PRODUTO_IMAGEM") val produtoImagem: String
)
