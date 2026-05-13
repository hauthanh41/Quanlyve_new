package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Passenger(
    @SerializedName("passenger_id")    val id: Int = 0,
    @SerializedName("full_name")       val fullName: String = "",
    @SerializedName("gender")          val gender: String = "MALE",
    @SerializedName("date_of_birth")   val dateOfBirth: String = "",
    @SerializedName("passport_number") val passportNumber: String = "",
    @SerializedName("nationality")     val nationality: String = "Vietnamese"
)
