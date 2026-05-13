package com.example.qlydatve.network.dto

import com.google.gson.annotations.SerializedName

// Request body gửi lên server — chỉ gồm các field server cần
data class FlightRequest(
    @SerializedName("flight_code")            val flightCode: String,
    @SerializedName("departure_airport_id")   val departureAirportId: Int,
    @SerializedName("arrival_airport_id")     val arrivalAirportId: Int,
    @SerializedName("airplane_id")            val airplaneId: Int,
    @SerializedName("departure_time")         val departureTime: String,
    @SerializedName("arrival_time")           val arrivalTime: String,
    @SerializedName("price")                  val price: Double,
    @SerializedName("status")                 val status: String = "AVAILABLE"
)
