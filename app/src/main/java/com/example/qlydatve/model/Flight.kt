package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Flight(
    @SerializedName("flight_id")         val id: Int = 0,
    @SerializedName("flight_code")       val flightNumber: String = "",
    @SerializedName("departure_airport") val departureAirport: String = "",
    @SerializedName("arrival_airport")   val arrivalAirport: String = "",
    @SerializedName("airplane_name")     val airplaneName: String = "",
    @SerializedName("departure_time")    val departureTime: String = "",
    @SerializedName("arrival_time")      val arrivalTime: String = "",
    @SerializedName("price")             val price: Double = 0.0,
    @SerializedName("status")            val status: String = "AVAILABLE",
    // For create/update requests — server also returns these in GET response
    @SerializedName("departure_airport_id") val departureAirportId: Int = 0,
    @SerializedName("arrival_airport_id")   val arrivalAirportId: Int = 0,
    @SerializedName("airplane_id")          val airplaneId: Int = 0
)
