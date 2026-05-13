package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message_id")  val id: Int = 0,
    @SerializedName("sender_id")   val senderId: Int = 0,
    @SerializedName("receiver_id") val receiverId: Int = 0,
    @SerializedName("content")     val content: String = "",
    @SerializedName("is_read")     val isRead: Boolean = false,
    @SerializedName("created_at")  val createdAt: String = ""
)

data class UserConversation(
    @SerializedName("user_id")      val userId: Int = 0,
    @SerializedName("full_name")    val fullName: String = "",
    @SerializedName("email")        val email: String = "",
    @SerializedName("last_message") val lastMessage: String = "",
    @SerializedName("last_time")    val lastTime: String = "",
    @SerializedName("unread_count") val unreadCount: Int = 0
)

data class SendMessageRequest(
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("content")     val content: String
)
