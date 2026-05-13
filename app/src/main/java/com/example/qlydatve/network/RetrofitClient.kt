package com.example.qlydatve.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Change to your server IP when running on a real device
    // Use 10.0.2.2 for Android emulator pointing to localhost
//    private const val BASE_URL = "http://192.168.88.103:3000/api/"
    private const val BASE_URL = "http://10.0.2.2:3000/api/"


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Gson tùy chỉnh: parse cả string lẫn number thành Double
    // (MySQL DECIMAL trả về string qua JDBC/mysql2)
    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.java, JsonDeserializer { json, _, _ ->
            json.asString.toDoubleOrNull() ?: 0.0
        })
        .registerTypeAdapter(Double::class.javaObjectType, JsonDeserializer { json, _, _ ->
            json.asString.toDoubleOrNull() ?: 0.0
        })
        .registerTypeAdapter(Boolean::class.java, JsonDeserializer { json, _, _ ->
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber  -> json.asInt != 0
                else -> json.asString.equals("true", ignoreCase = true)
            }
        })
        .registerTypeAdapter(Boolean::class.javaObjectType, JsonDeserializer { json, _, _ ->
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber  -> json.asInt != 0
                else -> json.asString.equals("true", ignoreCase = true)
            }
        })
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
