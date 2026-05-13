package com.example.qlydatve.service

import com.example.qlydatve.model.Booking
import com.example.qlydatve.model.CreateBookingRequest
import com.example.qlydatve.model.Seat
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.network.dto.HoldSeatRequest

class BookingService {

    private val api = RetrofitClient.api

    suspend fun getAllBookings(): Result<List<Booking>> {
        return try {
            val response = api.getBookings(TokenManager.getBearerToken())
            if (response.isSuccessful) Result.success(response.body() ?: emptyList())
            else Result.failure(Exception("Lỗi tải danh sách đặt vé"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun getSeats(airplaneId: Int, flightId: Int): Result<List<Seat>> {
        if (flightId <= 0) return Result.failure(Exception("ID chuyến bay không hợp lệ (ID=$flightId)"))
        return try {
            // Luôn dùng /seats/flight/:id — server tự tìm airplane và trả về ghế kèm trạng thái
            val response = api.getSeatsByFlight(flightId)
            if (response.isSuccessful) {
                val seats = response.body() ?: emptyList()
                if (seats.isEmpty()) Result.failure(Exception("Chuyến bay chưa có dữ liệu ghế"))
                else Result.success(seats)
            } else {
                Result.failure(Exception("Lỗi tải danh sách ghế (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<String> {
        return try {
            val response = api.createBooking(TokenManager.getBearerToken(), request)
            if (response.isSuccessful) {
                val body = response.body()
                val bookingId = body?.bookingId
                Result.success(if (bookingId != null) "BOOKING_ID:$bookingId" else "Đặt vé thành công")
            }
            else Result.failure(Exception("Đặt vé thất bại: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun cancelBooking(id: Int): Result<String> {
        return try {
            val response = api.cancelBooking(TokenManager.getBearerToken(), id)
            if (response.isSuccessful) Result.success("Hủy vé thành công")
            else Result.failure(Exception("Hủy vé thất bại"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun holdSeat(flightId: Int, seatId: Int): Result<String> {
        return try {
            val response = api.holdSeat(
                TokenManager.getBearerToken(),
                HoldSeatRequest(flightId, seatId)
            )
            if (response.isSuccessful) Result.success("Giữ ghế thành công")
            else {
                val code = response.code()
                if (code == 409) Result.failure(Exception("Ghế đã được đặt hoặc đang được giữ"))
                else Result.failure(Exception("Không thể giữ ghế"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun releaseSeat(flightId: Int, seatId: Int): Result<String> {
        return try {
            val response = api.releaseSeat(
                TokenManager.getBearerToken(),
                HoldSeatRequest(flightId, seatId)
            )
            if (response.isSuccessful) Result.success("Đã giải phóng ghế")
            else Result.failure(Exception("Lỗi giải phóng ghế"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }
}
