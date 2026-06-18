package com.example.qlydatve.model

import com.google.gson.annotations.SerializedName

data class AppNotification(
    @SerializedName("id")         val id: Int = 0,
    @SerializedName("title")      val title: String = "",
    @SerializedName("body")       val body: String = "",
    @SerializedName("type")       val type: String = "BOOKING",
    @SerializedName("is_read")    val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)
