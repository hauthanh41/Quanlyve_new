package com.example.qlydatve.service

import com.example.qlydatve.model.User
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager

class UserService {
    private val api = RetrofitClient.api

    suspend fun getAllUsers(): Result<List<User>> = try {
        val response = api.getUsers(TokenManager.getBearerToken())
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Lỗi tải danh sách khách hàng"))
    } catch (e: Exception) {
        Result.failure(Exception("Không thể kết nối server"))
    }

    suspend fun deleteUser(id: Int): Result<String> = try {
        val response = api.deleteUser(TokenManager.getBearerToken(), id)
        if (response.isSuccessful) Result.success("Xóa khách hàng thành công")
        else Result.failure(Exception("Xóa thất bại"))
    } catch (e: Exception) {
        Result.failure(Exception("Không thể kết nối server"))
    }
}
