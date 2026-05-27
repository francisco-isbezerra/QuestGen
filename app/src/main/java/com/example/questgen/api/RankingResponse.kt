package com.example.questgen.api

import com.google.gson.annotations.SerializedName

data class RankingResponse(
    @SerializedName("nome")      val nome: String,
    @SerializedName("gameCoins") val gameCoins: Int
)