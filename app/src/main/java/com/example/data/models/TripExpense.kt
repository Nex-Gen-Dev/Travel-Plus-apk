package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_expenses")
data class TripExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val amount: Double,
    val currency: String = "USD",
    val category: String = "Food & Dining", // "Flights", "Hotels", "Food & Dining", "Activities", "Transport", "Shopping", "Misc"
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
