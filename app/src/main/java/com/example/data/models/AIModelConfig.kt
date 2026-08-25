package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_model_configs")
data class AIModelConfig(
    @PrimaryKey
    val modelId: String,
    val displayName: String,
    val provider: String = "OpenRouter",
    val apiKey: String = "",
    val isFreeTier: Boolean = true,
    val priority: Int = 1, // Lower number = higher priority
    val isEnabled: Boolean = true,
    val contextLength: String = "128k",
    val description: String = ""
)

sealed class AIEngineStatus {
    object Idle : AIEngineStatus()
    data class Generating(val modelName: String, val attemptNumber: Int) : AIEngineStatus()
    data class SwitchingFailover(val failedModel: String, val nextModel: String, val reason: String) : AIEngineStatus()
    data class Completed(val usedModel: String, val responseTimeMs: Long) : AIEngineStatus()
    data class Error(val message: String) : AIEngineStatus()
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isFailoverSwitched: Boolean = false,
    val parsedItineraryTrip: Trip? = null,
    val parsedItems: List<ItineraryItem> = emptyList()
)
