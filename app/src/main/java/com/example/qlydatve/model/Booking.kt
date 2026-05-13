package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Booking(
    @SerializedName("booking_id")     val id: Int = 0,
    @SerializedName("user_id")        val userId: Int = 0,
    @SerializedName("full_name")      val fullName: String = "",
    @SerializedName("email")          val email: String = "",
    @SerializedName("booking_date")   val bookingDate: String = "",
    @SerializedName("total_amount")   val totalAmount: Double = 0.0,
    @SerializedName("booking_status") val status: String = "PENDING",
    @SerializedName("tickets")        val tickets: List<TicketDetail> = emptyList()
) {
    // Lấy thông tin từ vé đầu tiên để hiển thị trên HomeScreen
    val flightCode: String get() = tickets.firstOrNull()?.flightCode ?: ""
    val seatInfo: String get() = tickets.firstOrNull()?.seatNumber ?: ""
    val classType: String get() = tickets.firstOrNull()?.classType ?: ""
    // Các field này cần server trả về — dùng giá trị mặc định nếu chưa có
    val departureAirport: String get() = ""
    val arrivalAirport: String get() = ""
    val departureCity: String get() = ""
    val arrivalCity: String get() = ""
    val departureTime: String get() = ""
    val arrivalTime: String get() = ""
    val date: String get() = bookingDate.take(10)
}

data class TicketDetail(
    @SerializedName("ticket_id")      val id: Int = 0,
    @SerializedName("flight_code")    val flightCode: String = "",
    @SerializedName("seat_number")    val seatNumber: String = "",
    @SerializedName("class_type")     val classType: String = "",
    @SerializedName("passenger_name") val passengerName: String = "",
    @SerializedName("ticket_price")   val ticketPrice: Double = 0.0,
    @SerializedName("ticket_status")  val ticketStatus: String = "BOOKED"
)
