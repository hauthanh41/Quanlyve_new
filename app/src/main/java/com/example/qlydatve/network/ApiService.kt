package com.example.qlydatve.network

import com.example.qlydatve.model.*
import com.example.qlydatve.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MessageResponse>

    // ── Flights ───────────────────────────────────────────────────────────
    @GET("flights")
    suspend fun getFlights(): Response<List<Flight>>

    @GET("flights")
    suspend fun searchFlights(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("date") date: String = ""
    ): Response<List<Flight>>

    @GET("flights/{id}")
    suspend fun getFlight(@Path("id") id: Int): Response<Flight>

    @POST("flights")
    suspend fun createFlight(
        @Header("Authorization") token: String,
        @Body body: FlightRequest
    ): Response<MessageResponse>

    @PUT("flights/{id}")
    suspend fun updateFlight(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: FlightRequest
    ): Response<MessageResponse>

    @DELETE("flights/{id}")
    suspend fun deleteFlight(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // ── Airports ──────────────────────────────────────────────────────────
    @GET("airports")
    suspend fun getAirports(): Response<List<Airport>>

    // ── Airplanes ─────────────────────────────────────────────────────────
    @GET("airplanes")
    suspend fun getAirplanes(): Response<List<Airplane>>

    // ── Seats ─────────────────────────────────────────────────────────────
    @GET("seats")
    suspend fun getSeats(
        @Query("airplane_id") airplaneId: Int,
        @Query("flight_id") flightId: Int
    ): Response<List<Seat>>

    @GET("seats/flight/{flightId}")
    suspend fun getSeatsByFlight(
        @Path("flightId") flightId: Int
    ): Response<List<Seat>>

    @PATCH("seats/hold")
    suspend fun holdSeat(
        @Header("Authorization") token: String,
        @Body body: HoldSeatRequest
    ): Response<MessageResponse>

    @PATCH("seats/release")
    suspend fun releaseSeat(
        @Header("Authorization") token: String,
        @Body body: HoldSeatRequest
    ): Response<MessageResponse>

    // ── Bookings ──────────────────────────────────────────────────────────
    @GET("bookings")
    suspend fun getBookings(
        @Header("Authorization") token: String
    ): Response<List<Booking>>

    @GET("bookings/{id}")
    suspend fun getBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Booking>

    @POST("bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body body: CreateBookingRequest
    ): Response<MessageResponse>

    @PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    @PATCH("bookings/{id}/confirm")
    suspend fun confirmBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // ── Users ─────────────────────────────────────────────────────────────
    @GET("users")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<User>>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: UpdateProfileRequest
    ): Response<MessageResponse>

    @PUT("users/{id}/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: ChangePasswordRequest
    ): Response<MessageResponse>

    // ── Payments ──────────────────────────────────────────────────────────
    @GET("payments")
    suspend fun getPayments(
        @Header("Authorization") token: String
    ): Response<List<Payment>>

    @POST("payments")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body body: PaymentRequest
    ): Response<MessageResponse>

    // ── Messages ──────────────────────────────────────────────────────────
    @GET("messages/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): Response<List<UserConversation>>

    @GET("messages/{userId}")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body body: SendMessageRequest
    ): Response<Message>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<List<AppNotification>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): Response<Map<String, Int>>

    @PATCH("notifications/read-all")
    suspend fun markAllRead(
        @Header("Authorization") token: String
    ): Response<MessageResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markRead(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>
}
