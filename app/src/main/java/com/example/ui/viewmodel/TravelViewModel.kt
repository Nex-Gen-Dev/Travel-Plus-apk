package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AIProviderEngine
import com.example.data.local.TravelPlusDatabase
import com.example.data.models.*
import com.example.data.repository.TravelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TravelViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TravelPlusDatabase.getDatabase(application, viewModelScope)
    private val aiEngine = AIProviderEngine(database.aiModelConfigDao())
    val repository = TravelRepository(database, aiEngine)

    val aiEngineStatus = aiEngine.engineStatus

    val allTrips: StateFlow<List<Trip>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiConfigs: StateFlow<List<AIModelConfig>> = repository.allConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<TravelDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTripId = MutableStateFlow<Long?>(null)
    val selectedTripId: StateFlow<Long?> = _selectedTripId.asStateFlow()

    val currentTrip: StateFlow<Trip?> = combine(allTrips, _selectedTripId) { trips, selectedId ->
        if (selectedId != null) {
            trips.find { it.id == selectedId }
        } else {
            trips.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentItineraryItems: StateFlow<List<ItineraryItem>> = _selectedTripId.flatMapLatest { tripId ->
        val id = tripId ?: allTrips.value.firstOrNull()?.id
        if (id != null) {
            repository.getItineraryForTrip(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPackingItems: StateFlow<List<PackingItem>> = _selectedTripId.flatMapLatest { tripId ->
        val id = tripId ?: allTrips.value.firstOrNull()?.id
        if (id != null) {
            repository.getPackingForTrip(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentExpenses: StateFlow<List<TripExpense>> = _selectedTripId.flatMapLatest { tripId ->
        val id = tripId ?: allTrips.value.firstOrNull()?.id
        if (id != null) {
            repository.getExpensesForTrip(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat State for Conversational AI Planning
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                content = "👋 Welcome to **Travel Plus**! I'm your all-in-one AI travel concierge.\n\nWhere would you like to travel? Tell me your dream destination, dates, budget, or vibe (e.g. *'5 days in Tokyo for foodie exploration'* or *'Surprise me with a romantic 4-day European getaway'*)."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Currency converter state
    val sourceAmount = MutableStateFlow("100")
    val sourceCurrency = MutableStateFlow("USD")
    val targetCurrency = MutableStateFlow("EUR")

    // Visa lookup state
    val selectedNationality = MutableStateFlow("United States")
    val selectedVisaDestination = MutableStateFlow("Japan")

    // Selected Retailer preference
    val preferredRetailer = MutableStateFlow("Amazon") // Amazon, Target, Best Buy, Local

    // Active bottom navigation tab
    private val _activeTab = MutableStateFlow("ITINERARY")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun selectTrip(tripId: Long) {
        _selectedTripId.value = tripId
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isGenerating.value) return

        val userMsg = ChatMessage(
            sender = "user",
            content = userText
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (responseContent, usedModel) = aiEngine.executeWithFailover(
                    prompt = userText,
                    conversationHistory = _chatMessages.value
                )

                val (parsedTrip, parsedItems) = aiEngine.parseItineraryFromResponse(
                    destinationHint = extractDestinationHint(userText),
                    aiResponse = responseContent
                )

                val assistantMsg = ChatMessage(
                    sender = "assistant",
                    content = responseContent,
                    modelUsed = usedModel,
                    parsedItineraryTrip = parsedTrip,
                    parsedItems = parsedItems
                )

                _chatMessages.value = _chatMessages.value + assistantMsg

                // If user requested a new trip or comprehensive plan, auto-create/update the active trip
                if (userText.contains("day", ignoreCase = true) || userText.contains("trip", ignoreCase = true) || userText.contains("itinerary", ignoreCase = true) || parsedItems.isNotEmpty()) {
                    val newTripId = repository.createTripWithItinerary(parsedTrip, parsedItems)
                    _selectedTripId.value = newTripId
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "assistant",
                    content = "I encountered an issue connecting to the AI models: ${e.message}. Using offline backup engine to assist your trip."
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun lockCurrentTrip(isLocked: Boolean) {
        val trip = currentTrip.value ?: return
        viewModelScope.launch {
            repository.updateTrip(trip.copy(isLocked = isLocked))
        }
    }

    fun togglePackingItem(item: PackingItem) {
        viewModelScope.launch {
            repository.togglePackingItem(item)
        }
    }

    fun addCustomPackingItem(name: String, category: String, estimatedPrice: String = "$10 - $20") {
        val tripId = currentTrip.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertPackingItem(
                PackingItem(
                    tripId = tripId,
                    name = name,
                    category = category,
                    recommendedStore = preferredRetailer.value,
                    estimatedPrice = estimatedPrice,
                    isAiSuggested = false
                )
            )
        }
    }

    fun deletePackingItem(item: PackingItem) {
        viewModelScope.launch {
            repository.deletePackingItem(item)
        }
    }

    fun addTravelDocument(
        title: String,
        docType: String,
        documentNumber: String,
        holderName: String,
        expiryDate: String,
        notes: String = ""
    ) {
        val tripId = currentTrip.value?.id
        viewModelScope.launch {
            repository.insertDocument(
                TravelDocument(
                    tripId = tripId,
                    title = title,
                    docType = docType,
                    documentNumber = documentNumber,
                    holderName = holderName,
                    expiryDate = expiryDate,
                    notes = notes
                )
            )
        }
    }

    fun deleteTravelDocument(doc: TravelDocument) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        val tripId = currentTrip.value?.id ?: return
        viewModelScope.launch {
            repository.insertExpense(
                TripExpense(
                    tripId = tripId,
                    title = title,
                    amount = amount,
                    category = category
                )
            )
        }
    }

    fun deleteExpense(expense: TripExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateAIConfigKey(modelId: String, newApiKey: String) {
        viewModelScope.launch {
            val existing = aiConfigs.value.find { it.modelId == modelId }
            if (existing != null) {
                repository.updateAIConfig(existing.copy(apiKey = newApiKey))
            }
        }
    }

    fun toggleAIConfigEnabled(modelId: String) {
        viewModelScope.launch {
            val existing = aiConfigs.value.find { it.modelId == modelId }
            if (existing != null) {
                repository.updateAIConfig(existing.copy(isEnabled = !existing.isEnabled))
            }
        }
    }

    fun moveAIConfigPriority(modelId: String, moveUp: Boolean) {
        viewModelScope.launch {
            val list = aiConfigs.value.sortedBy { it.priority }.toMutableList()
            val index = list.indexOfFirst { it.modelId == modelId }
            if (index == -1) return@launch

            if (moveUp && index > 0) {
                val temp = list[index]
                list[index] = list[index - 1]
                list[index - 1] = temp
            } else if (!moveUp && index < list.size - 1) {
                val temp = list[index]
                list[index] = list[index + 1]
                list[index + 1] = temp
            }

            val updatedList = list.mapIndexed { i, item -> item.copy(priority = i + 1) }
            repository.saveAIConfigs(updatedList)
        }
    }

    fun checkPassportValidity(expiryDateStr: String): Pair<Int, Boolean> {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = sdf.parse(expiryDateStr) ?: return Pair(12, true)
            val now = Calendar.getInstance().time
            val diffMillis = expiry.time - now.time
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            val remainingMonths = diffDays / 30
            val isSafe = remainingMonths >= 6
            Pair(remainingMonths, isSafe)
        } catch (e: Exception) {
            Pair(12, true)
        }
    }

    private fun extractDestinationHint(prompt: String): String {
        val destKeywords = listOf(
            "Tokyo", "Paris", "Rome", "London", "Barcelona", "Kyoto", "New York", "Bangkok",
            "Amalfi Coast", "Swiss Alps", "Maui", "Reykjavik", "Cancun", "Sydney", "Dubai",
            "Florence", "Bali", "Amsterdam", "Singapore", "Lisbon", "Santorini"
        )
        for (keyword in destKeywords) {
            if (prompt.contains(keyword, ignoreCase = true)) {
                return keyword
            }
        }
        return ""
    }
}
