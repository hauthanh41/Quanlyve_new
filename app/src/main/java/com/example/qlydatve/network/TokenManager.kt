package com.example.qlydatve.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREF_NAME = "airline_prefs"
    private const val KEY_TOKEN = "jwt_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getBearerToken(): String = "Bearer ${getToken() ?: ""}"

    fun clearToken() = prefs.edit().remove(KEY_TOKEN).apply()

    fun isLoggedIn(): Boolean = getToken() != null
}
