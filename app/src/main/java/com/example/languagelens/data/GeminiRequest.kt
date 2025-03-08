package com.example.languagelens.data

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val inlineData: InlineData
)

data class InlineData(
    val mimeType: String,
    val data: String // Base64 image data
)

