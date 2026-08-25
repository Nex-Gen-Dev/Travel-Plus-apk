package com.example.data.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Double = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int = 2048,
    @Json(name = "top_p") val topP: Double = 0.95
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    val id: String?,
    val model: String?,
    val choices: List<OpenRouterChoice>?
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val index: Int?,
    val message: OpenRouterMessage?,
    @Json(name = "finish_reason") val finishReason: String?
)
