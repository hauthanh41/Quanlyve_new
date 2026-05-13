package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")   val id: Int = 0,
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email")     val email: String = "",
    @SerializedName("phone")     val phone: String = "",
    @SerializedName("role")      val role: String = "CUSTOMER"
)
