package com.example.languagelens.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1/" // ✅ Base URL must end in `/`

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // ✅ Fix: Proper base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}


