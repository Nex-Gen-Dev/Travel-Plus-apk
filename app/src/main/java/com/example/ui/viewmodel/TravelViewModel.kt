package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AIProviderEngine
import com.example.data.local.TravelPlusDatabase
import com.example.data.models.*
import com.example.data.repository.TravelRepository
import com.example.data.updater.DownloadState
import com.example.data.updater.GitHubRelease
import com.example.data.updater.GitHubUpdateManager
import com.example.data.updater.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TravelViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("travel_plus_prefs", Application.MODE_PRIVATE)

    private val database = TravelPlusDatabase.getDatabase(application, viewModelScope)
    private val aiEngine = AIProviderEngine(database.aiModelConfigDao())
    val repository = TravelRepository(database, aiEngine)
    val updateManager = GitHubUpdateManager(application)

    val aiEngineStatus = aiEngine.engineStatus

    // Global OpenRouter Key & Model state
    private val _openRouterApiKey = MutableStateFlow(prefs.getString("openrouter_api_key", "") ?: "")
    val openRouterApiKey: StateFlow<String> = _openRouterApiKey.asStateFlow()

    private val _selectedAIModel = MutableStateFlow(prefs.getString("openrouter_model_id", "google/gemini-2.0-flash-001") ?: "google/gemini-2.0-flash-001")
    val selectedAIModel: StateFlow<String> = _selectedAIModel.asStateFlow()

    // GitHub Updates State
    val currentAppVersion: String = updateManager.getCurrentVersionName()

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult>(UpdateCheckResult.Idle)
    val updateCheckResult: StateFlow<UpdateCheckResult> = _updateCheckResult.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _activeReleaseForDialog = MutableStateFlow<GitHubRelease?>(null)
    val activeReleaseForDialog: StateFlow<GitHubRelease?> = _activeReleaseForDialog.asStateFlow()

    private val _repoOwner = MutableStateFlow(updateManager.getRepoOwner())
    val repoOwner: StateFlow<String> = _repoOwner.asStateFlow()

    private val _repoName = MutableStateFlow(updateManager.getRepoName())
    val repoName: StateFlow<String> = _repoName.asStateFlow()

    private val _autoCheckUpdates = MutableStateFlow(updateManager.isAutoCheckEnabled())
    val autoCheckUpdates: StateFlow<Boolean> = _autoCheckUpdates.asStateFlow()

    val allTrips: StateFlow<List<Trip>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiConfigs: StateFlow<List<AIModelConfig>> = repository.allConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<TravelDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Restore last selected trip from preferences
    private val _selectedTripId = MutableStateFlow<Long?>(
        prefs.getLong("last_active_trip_id", -1L).takeIf { it > 0 }
    )
    val selectedTripId: StateFlow<Long?> = _selectedTripId.asStateFlow()

    val currentTrip: StateFlow<Trip?> = combine(allTrips, _selectedTripId) { trips, selectedId ->
        if (selectedId != null) {
            trips.find { it.id == selectedId } ?: trips.firstOrNull()
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
                content = "👋 Welcome to **Travel Plus AI Concierge**!\n\nI'm your all-in-one trip planner. Tell me where you'd like to travel (e.g. *'Plan a 4-day Paris culinary & romantic getaway'* or *'Add a beach relaxation day to my trip'*)."
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

    // Active bottom navigation tab with persistence
    private val _activeTab = MutableStateFlow(prefs.getString("last_active_tab", "ITINERARY") ?: "ITINERARY")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    init {
        // Initialize AI engine with saved key
        aiEngine.globalApiKey = _openRouterApiKey.value
        aiEngine.preferredModelId = _selectedAIModel.value

        // Auto-check for updates on app startup if enabled
        if (updateManager.isAutoCheckEnabled()) {
            checkForUpdates(isManualCheck = false)
        }
    }

    fun setGlobalOpenRouterKey(key: String) {
        val cleanKey = key.trim()
        _openRouterApiKey.value = cleanKey
        prefs.edit().putString("openrouter_api_key", cleanKey).apply()
        aiEngine.globalApiKey = cleanKey
    }

    fun setSelectedAIModel(modelId: String) {
        val clean = modelId.trim()
        _selectedAIModel.value = clean
        prefs.edit().putString("openrouter_model_id", clean).apply()
        aiEngine.preferredModelId = clean
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        prefs.edit().putString("last_active_tab", tab).apply()
    }

    fun selectTrip(tripId: Long) {
        _selectedTripId.value = tripId
        prefs.edit().putLong("last_active_trip_id", tripId).apply()
    }

    fun createNewTrip(
        destination: String,
        startDate: String = "Upcoming",
        endDate: String = "",
        durationDays: Int = 4,
        vibe: String = "Balanced Explorer",
        budget: Double = 1200.0,
        departureCity: String = "New York (JFK)"
    ) {
        viewModelScope.launch {
            val newTrip = Trip(
                destination = destination.trim(),
                country = extractCountryFromDestination(destination),
                startDate = startDate,
                endDate = endDate,
                durationDays = durationDays,
                vibe = vibe,
                totalEstimatedBudget = budget,
                departureCity = departureCity,
                summary = "$durationDays-day $vibe adventure in $destination."
            )
            val newTripId = repository.createTripWithItinerary(newTrip, emptyList())
            selectTrip(newTripId)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
            val remaining = allTrips.value.filter { it.id != trip.id }
            if (remaining.isNotEmpty()) {
                selectTrip(remaining.first().id)
            } else {
                _selectedTripId.value = null
            }
        }
    }

    fun saveBooking(doc: TravelDocument) {
        val tripId = currentTrip.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertDocument(doc.copy(tripId = tripId))
        }
    }

    fun updateDocument(doc: TravelDocument) {
        viewModelScope.launch {
            repository.updateDocument(doc)
        }
    }

    fun updateDocumentPhoto(doc: TravelDocument, photoUri: String) {
        viewModelScope.launch {
            repository.updateDocument(doc.copy(photoUri = photoUri))
        }
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

    private fun extractCountryFromDestination(dest: String): String {
        return when {
            dest.contains("Japan", ignoreCase = true) || dest.contains("Tokyo", ignoreCase = true) || dest.contains("Kyoto", ignoreCase = true) -> "Japan"
            dest.contains("France", ignoreCase = true) || dest.contains("Paris", ignoreCase = true) -> "France"
            dest.contains("Italy", ignoreCase = true) || dest.contains("Rome", ignoreCase = true) || dest.contains("Amalfi", ignoreCase = true) -> "Italy"
            dest.contains("Spain", ignoreCase = true) || dest.contains("Barcelona", ignoreCase = true) -> "Spain"
            dest.contains("UK", ignoreCase = true) || dest.contains("London", ignoreCase = true) || dest.contains("England", ignoreCase = true) -> "United Kingdom"
            dest.contains("Iceland", ignoreCase = true) || dest.contains("Reykjavik", ignoreCase = true) -> "Iceland"
            dest.contains("USA", ignoreCase = true) || dest.contains("Maui", ignoreCase = true) || dest.contains("New York", ignoreCase = true) -> "United States"
            dest.contains("Mexico", ignoreCase = true) || dest.contains("Cancun", ignoreCase = true) -> "Mexico"
            dest.contains("Thailand", ignoreCase = true) || dest.contains("Bangkok", ignoreCase = true) -> "Thailand"
            dest.contains("Australia", ignoreCase = true) || dest.contains("Sydney", ignoreCase = true) -> "Australia"
            dest.contains("UAE", ignoreCase = true) || dest.contains("Dubai", ignoreCase = true) -> "United Arab Emirates"
            dest.contains("Switzerland", ignoreCase = true) || dest.contains("Alps", ignoreCase = true) -> "Switzerland"
            dest.contains(",") -> dest.substringAfterLast(",").trim()
            else -> "International"
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

    // ==========================================
    // GitHub Releases & In-App Updates
    // ==========================================

    fun checkForUpdates(isManualCheck: Boolean = false) {
        viewModelScope.launch {
            _updateCheckResult.value = UpdateCheckResult.Checking
            val result = updateManager.checkForUpdates(
                owner = _repoOwner.value,
                repo = _repoName.value,
                forceCheck = isManualCheck
            )
            _updateCheckResult.value = result

            if (result is UpdateCheckResult.UpdateAvailable) {
                val dismissedTag = updateManager.getDismissedTag()
                // If manual check, or user hasn't dismissed this specific version yet, show dialog
                if (isManualCheck || dismissedTag != result.release.tagName) {
                    _activeReleaseForDialog.value = result.release
                    _showUpdateDialog.value = true
                }
            }
        }
    }

    fun openUpdateDialogForRelease(release: GitHubRelease) {
        _activeReleaseForDialog.value = release
        _showUpdateDialog.value = true
    }

    fun dismissUpdateDialog(rememberLater: Boolean = true) {
        if (rememberLater) {
            _activeReleaseForDialog.value?.let { release ->
                updateManager.setDismissedTag(release.tagName)
            }
        }
        _showUpdateDialog.value = false
    }

    fun startDownloadAndInstall(release: GitHubRelease) {
        viewModelScope.launch {
            updateManager.downloadApk(release) { state ->
                _downloadState.value = state
                if (state is DownloadState.ReadyToInstall) {
                    updateManager.installApk(state.file)
                }
            }
        }
    }

    fun installDownloadedApk(file: File) {
        updateManager.installApk(file)
    }

    fun setRepoConfiguration(owner: String, repo: String) {
        val cleanOwner = owner.trim()
        val cleanRepo = repo.trim()
        updateManager.setRepoOwner(cleanOwner)
        updateManager.setRepoName(cleanRepo)
        _repoOwner.value = cleanOwner
        _repoName.value = cleanRepo
        // Reset check state on repo change
        _updateCheckResult.value = UpdateCheckResult.Idle
    }

    fun setAutoCheckEnabled(enabled: Boolean) {
        updateManager.setAutoCheckEnabled(enabled)
        _autoCheckUpdates.value = enabled
    }

    fun simulateDemoUpdate() {
        val demoRelease = updateManager.createDemoRelease()
        val result = UpdateCheckResult.UpdateAvailable(
            release = demoRelease,
            currentVersion = currentAppVersion,
            isNewer = true
        )
        _updateCheckResult.value = result
        _activeReleaseForDialog.value = demoRelease
        _showUpdateDialog.value = true
    }
}

