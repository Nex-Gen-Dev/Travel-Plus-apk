package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destination: String,
    val country: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val durationDays: Int = 3,
    val vibe: String = "Balanced Explorer",
    val budgetLevel: String = "Moderate ($$)",
    val totalEstimatedBudget: Double = 1200.0,
    val currency: String = "USD",
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val departureCity: String = "New York (JFK)"
)

enum class ActivityCategory(val label: String, val iconName: String) {
    FLIGHT("Flight", "flight"),
    HOTEL("Hotel / Stay", "hotel"),
    ACTIVITY("Sightseeing", "attractions"),
    RESTAURANT("Dining & Food", "restaurant"),
    TRANSIT("Transport / Ride", "directions_car"),
    RELAXATION("Leisure & Beach", "beach_access"),
    SHOPPING("Shopping", "shopping_bag"),
    SCENIC("Viewpoint / Nature", "landscape")
}

@Entity(tableName = "itinerary_items")
data class ItineraryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val dayNumber: Int,
    val timeSlot: String, // "09:00 AM", "Morning", "01:30 PM", etc.
    val title: String,
    val description: String,
    val category: String = "ACTIVITY",
    val locationName: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val estimatedCost: Double = 0.0,
    val isCompleted: Boolean = false,
    val reservationUrl: String = "",
    val notes: String = ""
)
