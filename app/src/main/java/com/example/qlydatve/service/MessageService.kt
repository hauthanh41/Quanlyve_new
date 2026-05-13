package com.example.qlydatve.service

import com.example.qlydatve.model.Message
import com.example.qlydatve.model.SendMessageRequest
import com.example.qlydatve.model.UserConversation
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager

class MessageService {
    private val api = RetrofitClient.api

    suspend fun getConversations(): Result<List<UserConversation>> = try {
        val r = api.getConversations(TokenManager.getBearerToken())
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Lỗi tải danh sách hội thoại"))
    } catch (e: Exception) { Result.failure(Exception("Không thể kết nối server")) }

    suspend fun getMessages(userId: Int): Result<List<Message>> = try {
        val r = api.getMessages(TokenManager.getBearerToken(), userId)
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Lỗi tải tin nhắn"))
    } catch (e: Exception) { Result.failure(Exception("Không thể kết nối server")) }

    suspend fun sendMessage(content: String, receiverId: Int? = null): Result<Message> = try {
        val r = api.sendMessage(
            TokenManager.getBearerToken(),
            SendMessageRequest(receiverId = receiverId, content = content)
        )
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Gửi thất bại (${r.code()}): ${r.errorBody()?.string()}"))
    } catch (e: Exception) { Result.failure(Exception("Không thể kết nối server: ${e.message}")) }
}
