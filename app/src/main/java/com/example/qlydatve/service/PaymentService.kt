package com.example.qlydatve.service

import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.network.dto.PaymentRequest

class PaymentService {

    private val api = RetrofitClient.api

    suspend fun pay(bookingId: Int, amount: Double, method: String = "BANKING"): Result<String> {
        return try {
            val response = api.createPayment(
                TokenManager.getBearerToken(),
                PaymentRequest(bookingId, method, amount)
            )
            if (response.isSuccessful) Result.success("Thanh toán thành công")
            else Result.failure(Exception("Thanh toán thất bại"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }
}
