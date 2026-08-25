package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DocumentType(val title: String) {
    PASSPORT("Passport"),
    VISA("Visa / eVisa"),
    INSURANCE("Travel Insurance"),
    BOARDING_PASS("Boarding Pass"),
    DRIVER_LICENSE("Driver's License / ID"),
    HOTEL_CONFIRMATION("Hotel Booking"),
    OTHER("Other Document")
}

@Entity(tableName = "travel_documents")
data class TravelDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long? = null,
    val title: String,
    val docType: String = DocumentType.PASSPORT.name,
    val documentNumber: String = "",
    val holderName: String = "",
    val issueCountry: String = "",
    val issueDate: String = "",
    val expiryDate: String = "", // YYYY-MM-DD
    val notes: String = "",
    val photoUri: String = "",
    val confirmationCode: String = "",
    val qrBarcodeData: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
