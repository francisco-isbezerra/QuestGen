package com.example.questgen.api

import com.google.gson.annotations.SerializedName

data class DesafioResponse(
    @SerializedName("status")      val status: String,
    @SerializedName("descricao")   val descricao: String,
    @SerializedName("raridade")    val raridade: String,
    @SerializedName("recompensa")  val recompensa: Int
)