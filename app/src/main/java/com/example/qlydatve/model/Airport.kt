package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Airport(
    @SerializedName("airport_id")   val id: Int = 0,
    @SerializedName("airport_code") val code: String = "",
    @SerializedName("airport_name") val name: String = "",
    @SerializedName("city")         val city: String = "",
    @SerializedName("country")      val country: String = ""
)
