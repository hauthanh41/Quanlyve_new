package com.example.qlydatve.network.dto

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("booking_id")      val bookingId: Int,
    @SerializedName("payment_method")  val paymentMethod: String,
    @SerializedName("amount")          val amount: Double
)
