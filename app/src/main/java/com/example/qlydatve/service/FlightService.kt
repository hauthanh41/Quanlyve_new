package com.example.qlydatve.service

import com.example.qlydatve.model.Airport
import com.example.qlydatve.model.Airplane
import com.example.qlydatve.model.Flight
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.network.dto.FlightRequest
import org.json.JSONObject

class FlightService {

    private val api = RetrofitClient.api

    suspend fun getAllFlights(): Result<List<Flight>> = safeCall {
        val r = api.getFlights()
        if (r.isSuccessful) r.body() ?: emptyList()
        else throw Exception(parseError(r.errorBody()?.string()) ?: "Lỗi tải danh sách chuyến bay")
    }

    suspend fun searchFlights(from: String, to: String, date: String = ""): Result<List<Flight>> = safeCall {
        val r = api.searchFlights(from, to, date)
        if (r.isSuccessful) r.body() ?: emptyList()
        else throw Exception(parseError(r.errorBody()?.string()) ?: "Lỗi tìm kiếm")
    }

    suspend fun getAirports(): Result<List<Airport>> = safeCall {
        val r = api.getAirports()
        if (r.isSuccessful) r.body() ?: emptyList()
        else throw Exception("Lỗi tải sân bay")
    }

    suspend fun getAirplanes(): Result<List<Airplane>> = safeCall {
        val r = api.getAirplanes()
        if (r.isSuccessful) r.body() ?: emptyList()
        else throw Exception("Lỗi tải máy bay")
    }

    suspend fun addFlight(flight: Flight): Result<String> = safeCall {
        val body = flight.toRequest()
        val r = api.createFlight(TokenManager.getBearerToken(), body)
        if (r.isSuccessful) r.body()?.message ?: "Thêm chuyến bay thành công"
        else throw Exception(parseError(r.errorBody()?.string()) ?: "Thêm chuyến bay thất bại (${r.code()})")
    }

    suspend fun updateFlight(flight: Flight): Result<String> = safeCall {
        val body = flight.toRequest()
        val r = api.updateFlight(TokenManager.getBearerToken(), flight.id, body)
        if (r.isSuccessful) r.body()?.message ?: "Cập nhật thành công"
        else throw Exception(parseError(r.errorBody()?.string()) ?: "Cập nhật thất bại (${r.code()})")
    }

    suspend fun deleteFlight(id: Int): Result<String> = safeCall {
        val r = api.deleteFlight(TokenManager.getBearerToken(), id)
        if (r.isSuccessful) "Xóa thành công"
        else throw Exception(parseError(r.errorBody()?.string()) ?: "Xóa thất bại (${r.code()})")
    }

    // Convert Flight model → FlightRequest DTO
    private fun Flight.toRequest() = FlightRequest(
        flightCode = flightNumber,
        departureAirportId = departureAirportId,
        arrivalAirportId = arrivalAirportId,
        airplaneId = airplaneId,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        price = price,
        status = status
    )

    // Parse {"message": "..."} from error body
    private fun parseError(body: String?): String? = try {
        body?.let { JSONObject(it).optString("message").ifBlank { null } }
    } catch (_: Exception) { null }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
