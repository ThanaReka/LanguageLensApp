package com.example.languagelens.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun detectObjects(
        @Query("key") apiKey: String,  // ✅ Pass API key in query
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}



