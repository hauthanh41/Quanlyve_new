package com.example.qlydatve.network.dto

import com.example.qlydatve.model.User
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email")     val email: String,
    @SerializedName("password")  val password: String,
    @SerializedName("phone")     val phone: String = ""
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user")  val user: User
)

data class MessageResponse(
    @SerializedName("message")    val message: String,
    @SerializedName("booking_id") val bookingId: Int? = null
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone")     val phone: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password")     val newPassword: String
)

data class HoldSeatRequest(
    @SerializedName("flight_id") val flightId: Int,
    @SerializedName("seat_id")   val seatId: Int
)
