package com.example.languagelens.data

import android.util.Log
import com.example.languagelens.BuildConfig

class GeminiRepository(private val apiService: GeminiApiService) {
    private val API_KEY =  BuildConfig.GEMINI_API_KEY

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