package com.example.qlydatve.service

import com.example.qlydatve.model.AppNotification
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager

class NotificationService {
    private val api = RetrofitClient.api

    suspend fun getNotifications(): Result<List<AppNotification>> = try {
        val r = api.getNotifications(TokenManager.getBearerToken())
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Lỗi tải thông báo"))
    } catch (e: Exception) { Result.failure(Exception("Không thể kết nối server")) }

    suspend fun getUnreadCount(): Int = try {
        val r = api.getUnreadCount(TokenManager.getBearerToken())
        if (r.isSuccessful) r.body()?.get("count") ?: 0
        else 0
    } catch (e: Exception) { 0 }

    suspend fun markAllRead(): Unit = try {
        api.markAllRead(TokenManager.getBearerToken())
        Unit
    } catch (_: Exception) {}

    suspend fun markRead(id: Int): Unit = try {
        api.markRead(TokenManager.getBearerToken(), id)
        Unit
    } catch (_: Exception) {}
}
