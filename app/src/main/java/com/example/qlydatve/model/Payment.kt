package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Payment(
    @SerializedName("payment_id")     val id: Int = 0,
    @SerializedName("booking_id")     val bookingId: Int = 0,
    @SerializedName("payment_method") val paymentMethod: String = "",
    @SerializedName("amount")         val amount: Double = 0.0,
    @SerializedName("payment_status") val status: String = "PAID",
    @SerializedName("payment_date")   val paymentDate: String = "",
    @SerializedName("full_name")      val fullName: String = ""
)
