package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packing_items")
data class PackingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val name: String,
    val category: String, // "Clothing", "Toiletries", "Electronics", "Documents", "Activity Gear", "Health"
    val quantity: Int = 1,
    val isPacked: Boolean = false,
    val recommendedStore: String = "Amazon",
    val estimatedPrice: String = "$15 - $30",
    val searchQuery: String = "",
    val isAiSuggested: Boolean = true
)
