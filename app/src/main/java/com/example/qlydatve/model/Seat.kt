package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Seat(
    @SerializedName("seat_id")      val id: Int = 0,
    @SerializedName("airplane_id")  val airplaneId: Int = 0,
    @SerializedName("seat_number")  val seatNumber: String = "",
    @SerializedName("class_type")   val classType: String = "ECONOMY",
    // Trạng thái ghế theo chuyến bay (từ flight_seats)
    @SerializedName("seat_status")  val seatStatus: String = "AVAILABLE",
    @SerializedName("flight_seat_id") val flightSeatId: Int? = null,
    // Server trả về is_available khi query kèm flight_id
    @SerializedName("is_available") val isAvailableRaw: Boolean? = null
) {
    val isAvailable: Boolean get() = isAvailableRaw ?: (seatStatus == "AVAILABLE")
}
