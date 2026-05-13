package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Airplane(
    @SerializedName("airplane_id")          val id: Int = 0,
    @SerializedName("airplane_name")        val name: String = "",
    @SerializedName("airplane_code")        val code: String = "",
    @SerializedName("total_seats")          val totalSeats: Int = 0
)
