package com.example.data.repository

import com.example.data.ai.AIProviderEngine
import com.example.data.local.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

class TravelRepository(
    private val db: TravelPlusDatabase,
    val aiEngine: AIProviderEngine
) {
    val allTrips: Flow<List<Trip>> = db.tripDao().getAllTrips()
    val allConfigs: Flow<List<AIModelConfig>> = db.aiModelConfigDao().getAllConfigs()
    val allDocuments: Flow<List<TravelDocument>> = db.travelDocumentDao().getAllDocuments()

    fun getItineraryForTrip(tripId: Long): Flow<List<ItineraryItem>> =
        db.itineraryItemDao().getItemsForTrip(tripId)

    fun getPackingForTrip(tripId: Long): Flow<List<PackingItem>> =
        db.packingItemDao().getPackingItemsForTrip(tripId)

    fun getExpensesForTrip(tripId: Long): Flow<List<TripExpense>> =
        db.tripExpenseDao().getExpensesForTrip(tripId)

    suspend fun getTripById(id: Long): Trip? = db.tripDao().getTripById(id)

    suspend fun createTripWithItinerary(trip: Trip, items: List<ItineraryItem>): Long {
        val newTripId = db.tripDao().insertTrip(trip)
        val itemsWithTripId = items.map { it.copy(tripId = newTripId) }
        db.itineraryItemDao().insertItems(itemsWithTripId)
        generateSmartPackingForTrip(newTripId, trip.destination, trip.vibe)
        return newTripId
    }

    suspend fun updateTrip(trip: Trip) = db.tripDao().updateTrip(trip)
    suspend fun deleteTrip(trip: Trip) = db.tripDao().deleteTrip(trip)

    suspend fun insertItineraryItem(item: ItineraryItem) = db.itineraryItemDao().insertItem(item)
    suspend fun updateItineraryItem(item: ItineraryItem) = db.itineraryItemDao().updateItem(item)
    suspend fun deleteItineraryItem(item: ItineraryItem) = db.itineraryItemDao().deleteItem(item)

    suspend fun togglePackingItem(item: PackingItem) =
        db.packingItemDao().updatePackingItem(item.copy(isPacked = !item.isPacked))

    suspend fun insertPackingItem(item: PackingItem) = db.packingItemDao().insertPackingItem(item)
    suspend fun deletePackingItem(item: PackingItem) = db.packingItemDao().deletePackingItem(item)

    suspend fun insertDocument(doc: TravelDocument) = db.travelDocumentDao().insertDocument(doc)
    suspend fun updateDocument(doc: TravelDocument) = db.travelDocumentDao().updateDocument(doc)
    suspend fun deleteDocument(doc: TravelDocument) = db.travelDocumentDao().deleteDocument(doc)

    suspend fun insertExpense(expense: TripExpense) = db.tripExpenseDao().insertExpense(expense)
    suspend fun deleteExpense(expense: TripExpense) = db.tripExpenseDao().deleteExpense(expense)

    suspend fun updateAIConfig(config: AIModelConfig) = db.aiModelConfigDao().updateConfig(config)
    suspend fun saveAIConfigs(configs: List<AIModelConfig>) = db.aiModelConfigDao().insertConfigs(configs)

    suspend fun generateSmartPackingForTrip(tripId: Long, destination: String, vibe: String) {
        val dest = destination.lowercase()
        val isTropical = dest.contains("bali") || dest.contains("hawaii") || dest.contains("cancun") || dest.contains("phuket") || dest.contains("miami")
        val isAlpine = dest.contains("alps") || dest.contains("swiss") || dest.contains("aspen") || dest.contains("iceland")
        val isAsia = dest.contains("japan") || dest.contains("tokyo") || dest.contains("thailand") || dest.contains("singapore") || dest.contains("korea")
        val isEurope = dest.contains("paris") || dest.contains("italy") || dest.contains("rome") || dest.contains("london") || dest.contains("spain")

        val items = mutableListOf<PackingItem>()

        // Essentials
        items.add(PackingItem(tripId = tripId, name = "Passport & International ID", category = "Documents", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "Essential"))
        items.add(PackingItem(tripId = tripId, name = "RFID Blocking Travel Neck Wallet / Pouch", category = "Documents", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "$14.99"))

        // Electronics
        val adapterType = when {
            isAsia -> "Universal All-in-One Travel Adapter with PD Fast Charge"
            isEurope -> "Type C/E/F European Plug Adapter Set"
            else -> "Universal Travel Power Converter Adapter"
        }
        items.add(PackingItem(tripId = tripId, name = adapterType, category = "Electronics", quantity = 2, recommendedStore = "Amazon", estimatedPrice = "$19.99"))
        items.add(PackingItem(tripId = tripId, name = "20,000mAh Ultra-Slim Fast Power Bank", category = "Electronics", quantity = 1, recommendedStore = "Best Buy", estimatedPrice = "$39.99"))
        items.add(PackingItem(tripId = tripId, name = "Noise-Cancelling Travel Headphones", category = "Electronics", quantity = 1, recommendedStore = "Best Buy", estimatedPrice = "$99.00"))

        // Clothing & Activity Gear
        if (isTropical) {
            items.add(PackingItem(tripId = tripId, name = "Reef-Safe Mineral Sunscreen SPF 50+", category = "Toiletries", quantity = 2, recommendedStore = "Target", estimatedPrice = "$15.99"))
            items.add(PackingItem(tripId = tripId, name = "UV Protection Quick-Dry Swimwear / Rashguard", category = "Clothing", quantity = 2, recommendedStore = "Target", estimatedPrice = "$29.99"))
            items.add(PackingItem(tripId = tripId, name = "Waterproof Dry Bag (10L) for Island Tours", category = "Activity Gear", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "$16.99"))
            items.add(PackingItem(tripId = tripId, name = "Anti-Fog Snorkel & Mask Set", category = "Activity Gear", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "$24.99"))
        } else if (isAlpine) {
            items.add(PackingItem(tripId = tripId, name = "Thermal Base Layers (Merino Wool)", category = "Clothing", quantity = 2, recommendedStore = "Target", estimatedPrice = "$45.00"))
            items.add(PackingItem(tripId = tripId, name = "Waterproof Gore-Tex Hiking Boots", category = "Activity Gear", quantity = 1, recommendedStore = "Target", estimatedPrice = "$120.00"))
            items.add(PackingItem(tripId = tripId, name = "Insulated Windproof Ski / Alpine Jacket", category = "Clothing", quantity = 1, recommendedStore = "Target", estimatedPrice = "$149.00"))
            items.add(PackingItem(tripId = tripId, name = "Touchscreen Thermal Winter Gloves", category = "Clothing", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "$18.00"))
        } else {
            items.add(PackingItem(tripId = tripId, name = "Comfortable Walking Sneakers / City Trainers", category = "Clothing", quantity = 1, recommendedStore = "Target", estimatedPrice = "$65.00"))
            items.add(PackingItem(tripId = tripId, name = "Light Packable Rain Shell / Windbreaker", category = "Clothing", quantity = 1, recommendedStore = "Target", estimatedPrice = "$35.00"))
            items.add(PackingItem(tripId = tripId, name = "Anti-Theft Crossbody Bag (Pacsafe / Travelon)", category = "Essentials", quantity = 1, recommendedStore = "Amazon", estimatedPrice = "$42.00"))
        }

        // Toiletries & Meds
        items.add(PackingItem(tripId = tripId, name = "Travel Medicine Kit (Electrolytes, Motion Sickness, Pain)", category = "Toiletries", quantity = 1, recommendedStore = "Target", estimatedPrice = "$12.50"))
        items.add(PackingItem(tripId = tripId, name = "TSA-Approved Silicone Travel Bottles (3.4oz)", category = "Toiletries", quantity = 4, recommendedStore = "Amazon", estimatedPrice = "$9.99"))

        db.packingItemDao().insertPackingItems(items)
    }
}
