package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Trip::class,
        ItineraryItem::class,
        PackingItem::class,
        TravelDocument::class,
        TripExpense::class,
        AIModelConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TravelPlusDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun itineraryItemDao(): ItineraryItemDao
    abstract fun packingItemDao(): PackingItemDao
    abstract fun travelDocumentDao(): TravelDocumentDao
    abstract fun tripExpenseDao(): TripExpenseDao
    abstract fun aiModelConfigDao(): AIModelConfigDao

    companion object {
        @Volatile
        private var INSTANCE: TravelPlusDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TravelPlusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelPlusDatabase::class.java,
                    "travel_plus_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: TravelPlusDatabase) {
            // Seed initial multi-provider AI model configs with free-tier priorities
            val defaultModels = listOf(
                AIModelConfig(
                    modelId = "meta-llama/llama-3.3-70b-instruct:free",
                    displayName = "Llama 3.3 70B (Free)",
                    provider = "OpenRouter",
                    apiKey = "",
                    isFreeTier = true,
                    priority = 1,
                    isEnabled = true,
                    contextLength = "128k",
                    description = "High accuracy Meta model on OpenRouter free tier. Ideal for complex multi-day planning."
                ),
                AIModelConfig(
                    modelId = "google/gemini-2.0-flash-exp:free",
                    displayName = "Gemini 2.0 Flash (Free)",
                    provider = "OpenRouter",
                    apiKey = "",
                    isFreeTier = true,
                    priority = 2,
                    isEnabled = true,
                    contextLength = "1M",
                    description = "Ultra-fast response model with massive context window. Primary failover tier."
                ),
                AIModelConfig(
                    modelId = "mistralai/mistral-small-24b-instruct-2501:free",
                    displayName = "Mistral Small 24B (Free)",
                    provider = "OpenRouter",
                    apiKey = "",
                    isFreeTier = true,
                    priority = 3,
                    isEnabled = true,
                    contextLength = "32k",
                    description = "Specialized European culture & destination reasoning. Secondary failover tier."
                ),
                AIModelConfig(
                    modelId = "deepseek/deepseek-r1:free",
                    displayName = "DeepSeek R1 (Free)",
                    provider = "OpenRouter",
                    apiKey = "",
                    isFreeTier = true,
                    priority = 4,
                    isEnabled = true,
                    contextLength = "64k",
                    description = "Deep reasoning AI for detailed logistics, budgeting & transit optimization."
                ),
                AIModelConfig(
                    modelId = "anthropic/claude-3.5-haiku",
                    displayName = "Claude 3.5 Haiku",
                    provider = "OpenRouter",
                    apiKey = "",
                    isFreeTier = false,
                    priority = 5,
                    isEnabled = true,
                    contextLength = "200k",
                    description = "High precision, concise travel agent response styling."
                )
            )
            db.aiModelConfigDao().insertConfigs(defaultModels)

            // Seed a starter sample trip (Tokyo Autumn Discovery)
            val tripId = db.tripDao().insertTrip(
                Trip(
                    destination = "Tokyo, Japan",
                    country = "Japan",
                    startDate = "Oct 12, 2026",
                    endDate = "Oct 16, 2026",
                    durationDays = 5,
                    vibe = "Cultural & Foodie Explorer",
                    budgetLevel = "Moderate ($$)",
                    totalEstimatedBudget = 1850.0,
                    currency = "USD",
                    summary = "5-day immersive exploration of Tokyo featuring historic temples in Asakusa, neon streets of Shinjuku, authentic Tsukiji outer market sushi, and Mount Fuji day tour.",
                    isLocked = false,
                    departureCity = "San Francisco (SFO)"
                )
            )

            // Seed day-by-day items
            val sampleItems = listOf(
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 1,
                    timeSlot = "10:30 AM",
                    title = "Flight Arrival at Haneda (HND)",
                    description = "Arrive at Haneda Airport, pick up Suica/Pasmo IC card at transit machine and pocket WiFi.",
                    category = "FLIGHT",
                    locationName = "Haneda Airport Terminal 3",
                    address = "Hanedakuko, Ota City, Tokyo 144-0041, Japan",
                    estimatedCost = 0.0,
                    notes = "Take Keikyu Airport Line direct to Shinagawa Station."
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 1,
                    timeSlot = "01:30 PM",
                    title = "Check-in at Hotel Gracery Shinjuku",
                    description = "Iconic Godzilla Road hotel located in central Kabukicho with easy access to JR Shinjuku Station.",
                    category = "HOTEL",
                    locationName = "Hotel Gracery Shinjuku",
                    address = "1-19-1 Kabukicho, Shinjuku City, Tokyo",
                    estimatedCost = 160.0,
                    reservationUrl = "https://www.booking.com"
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 1,
                    timeSlot = "05:00 PM",
                    title = "Shinjuku Omoide Yokocho Yakitori Alley",
                    description = "Atmospheric post-war alleyways lined with cozy counter-service skewers and local craft beers.",
                    category = "RESTAURANT",
                    locationName = "Omoide Yokocho",
                    address = "1-2 Nishishinjuku, Shinjuku City, Tokyo",
                    estimatedCost = 35.0
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 2,
                    timeSlot = "09:00 AM",
                    title = "Sensō-ji Temple & Nakamise-dori",
                    description = "Tokyo's oldest Buddhist temple founded in 645 AD. Browse traditional sweet stalls along the approach.",
                    category = "ACTIVITY",
                    locationName = "Sensō-ji Temple",
                    address = "2-3-1 Asakusa, Taito City, Tokyo",
                    estimatedCost = 0.0
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 2,
                    timeSlot = "01:00 PM",
                    title = "Tsukiji Outer Market Food Tour",
                    description = "Sample fresh tamagoyaki omelet, fresh king crab legs, wagyu skewers, and tuna nigiri.",
                    category = "RESTAURANT",
                    locationName = "Tsukiji Outer Market",
                    address = "4 Chome Tsukiji, Chuo City, Tokyo",
                    estimatedCost = 45.0
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 2,
                    timeSlot = "06:30 PM",
                    title = "Shibuya Sky Sunset Observatory",
                    description = "360-degree open-air rooftop observation deck over Shibuya Crossing and Tokyo skyline.",
                    category = "SCENIC",
                    locationName = "Shibuya Scramble Square",
                    address = "2-24-12 Shibuya, Tokyo",
                    estimatedCost = 22.0
                ),
                ItineraryItem(
                    tripId = tripId,
                    dayNumber = 3,
                    timeSlot = "08:30 AM",
                    title = "Mount Fuji & Lake Kawaguchiko Day Express",
                    description = "Scenic express train to Chureito Pagoda for postcard views of snowcapped Mount Fuji.",
                    category = "ACTIVITY",
                    locationName = "Lake Kawaguchiko",
                    address = "Fujikawaguchiko, Minamitsuru District, Yamanashi",
                    estimatedCost = 85.0
                )
            )
            db.itineraryItemDao().insertItems(sampleItems)

            // Seed packing items
            val samplePacking = listOf(
                PackingItem(tripId = tripId, name = "Type A/B Japan Power Plug Adapter", category = "Electronics", quantity = 2, isPacked = true, recommendedStore = "Amazon", estimatedPrice = "$12"),
                PackingItem(tripId = tripId, name = "Slip-on Walking Shoes (for temples)", category = "Clothing", quantity = 1, isPacked = true, recommendedStore = "Target", estimatedPrice = "$65"),
                PackingItem(tripId = tripId, name = "Coin Pouch (Japan is coin-heavy)", category = "Essentials", quantity = 1, isPacked = false, recommendedStore = "Amazon", estimatedPrice = "$9"),
                PackingItem(tripId = tripId, name = "Universal eSIM / Pocket WiFi Pass", category = "Electronics", quantity = 1, isPacked = false, recommendedStore = "Amazon", estimatedPrice = "$25"),
                PackingItem(tripId = tripId, name = "Light Rain Jacket / Windbreaker", category = "Clothing", quantity = 1, isPacked = false, recommendedStore = "Target", estimatedPrice = "$40"),
                PackingItem(tripId = tripId, name = "Travel Medicine Kit & Pain Relievers", category = "Toiletries", quantity = 1, isPacked = false, recommendedStore = "Target", estimatedPrice = "$15")
            )
            db.packingItemDao().insertPackingItems(samplePacking)

            // Seed sample document (Passport Tracker)
            db.travelDocumentDao().insertDocument(
                TravelDocument(
                    tripId = tripId,
                    title = "Primary US Passport",
                    docType = DocumentType.PASSPORT.name,
                    documentNumber = "E84920194",
                    holderName = "Traveler Explorer",
                    issueCountry = "United States",
                    issueDate = "2021-05-15",
                    expiryDate = "2031-05-15",
                    notes = "Valid for more than 6 months. Stored on-device encrypted."
                )
            )

            // Seed sample expense
            db.tripExpenseDao().insertExpense(
                TripExpense(
                    tripId = tripId,
                    title = "JR Rail Pass / Suica Card Initial Load",
                    amount = 50.0,
                    category = "Transport",
                    notes = "Loaded on Apple/Google Wallet"
                )
            )
        }
    }
}
