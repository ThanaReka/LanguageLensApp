package com.example.languagelens.data

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val output: String?
)



