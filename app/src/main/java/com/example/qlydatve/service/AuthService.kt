package com.example.qlydatve.service

import com.example.qlydatve.model.User
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.network.dto.LoginRequest
import com.example.qlydatve.network.dto.RegisterRequest

class AuthService {

    private val api = RetrofitClient.api

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                TokenManager.saveToken(body.token)
                Result.success(body.user)
            } else {
                Result.failure(Exception("Email hoặc mật khẩu không đúng"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    suspend fun register(fullName: String, email: String, password: String, phone: String): Result<String> {
        return try {
            val response = api.register(RegisterRequest(fullName, email, password, phone))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Đăng ký thành công")
            } else {
                Result.failure(Exception("Email đã tồn tại"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối server"))
        }
    }

    fun logout() = TokenManager.clearToken()
}
