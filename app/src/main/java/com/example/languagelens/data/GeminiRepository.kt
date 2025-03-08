package com.example.languagelens.data

import android.util.Log

class GeminiRepository(private val apiService: GeminiApiService) {
    private val API_KEY = "AIzaSyBvobBq8j6oSMht_2izXjHAEo8M9_Qk91U" // 🔥 Keep this secure

    suspend fun detectTextFromImage(base64Image: String): String {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                )
            )

            val response = apiService.detectObjects(API_KEY, request)

            if (response.isSuccessful) {
                response.body()?.candidates?.firstOrNull()?.output ?: "No text detected"
            } else {
                val errorMessage = response.errorBody()?.string()
                Log.e("GeminiRepository", "API error: $errorMessage")  // 🔍 Print full error
                "API error: $errorMessage"
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error detecting text: ${e.message}", e)
            "Error processing image"
        }
    }
}