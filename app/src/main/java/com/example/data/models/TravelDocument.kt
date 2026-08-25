package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DocumentType(val title: String, val iconName: String = "badge") {
    BOARDING_PASS("Boarding Pass", "flight_takeoff"),
    FLIGHT_BOOKING("Flight Reservation", "flight"),
    HOTEL_CONFIRMATION("Hotel / Lodging", "hotel"),
    CAR_RENTAL("Rental Car", "directions_car"),
    ACTIVITY_BOOKING("Tour / Activity", "local_activity"),
    PASSPORT("Passport", "badge"),
    VISA("Visa / eVisa", "description"),
    INSURANCE("Travel Insurance", "health_and_safety"),
    DRIVER_LICENSE("Driver's License / ID", "credit_card"),
    OTHER("Other Document", "receipt_long")
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
    val confirmationCode: String = "", // PNR / Booking reference
    val qrBarcodeData: String = "", // Scannable barcode / QR payload
    // Rich Boarding Pass & Logistics Fields
    val flightNumber: String = "", // e.g. "AA 1084"
    val airlineOrProvider: String = "", // e.g. "American Airlines", "Booking.com", "Hertz"
    val departureAirport: String = "", // e.g. "JFK - New York"
    val arrivalAirport: String = "", // e.g. "HND - Tokyo Haneda"
    val departureTime: String = "", // e.g. "10:45 AM"
    val arrivalTime: String = "", // e.g. "02:30 PM +1"
    val departureDate: String = "", // e.g. "Oct 14, 2026"
    val gate: String = "", // e.g. "B24"
    val terminal: String = "", // e.g. "T4"
    val seatNumber: String = "", // e.g. "14A"
    val boardingGroup: String = "", // e.g. "Group 2"
    val cabinClass: String = "Economy", // Economy, Premium, Business, First
    val pricePaid: String = "", // e.g. "$450.00"
    val createdAt: Long = System.currentTimeMillis()
)
