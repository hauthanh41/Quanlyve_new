package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

// Request body for creating a booking with tickets
data class CreateBookingRequest(
    @SerializedName("tickets") val tickets: List<TicketRequest>
)

data class TicketRequest(
    @SerializedName("passenger")    val passenger: PassengerRequest,
    @SerializedName("flight_id")    val flightId: Int,
    @SerializedName("seat_id")      val seatId: Int,
    @SerializedName("ticket_price") val ticketPrice: Double
)

data class PassengerRequest(
    @SerializedName("full_name")        val fullName: String,
    @SerializedName("gender")           val gender: String = "MALE",
    @SerializedName("date_of_birth")    val dateOfBirth: String = "",
    @SerializedName("passport_number")  val passportNumber: String = "",
    @SerializedName("nationality")      val nationality: String = "Vietnamese"
)
