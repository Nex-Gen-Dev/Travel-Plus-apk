package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.ItineraryItem
import com.example.data.models.Trip
import com.example.ui.components.CategoryBadge
import com.example.ui.components.FailoverStatusBar
import com.example.ui.components.TravelTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val allTrips by viewModel.allTrips.collectAsStateWithLifecycle()
    val itineraryItems by viewModel.currentItineraryItems.collectAsStateWithLifecycle()
    val aiStatus by viewModel.aiEngineStatus.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    var selectedDayFilter by remember { mutableStateOf<Int?>(null) }
    var showChatSheet by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    var showTripSelectorDialog by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "🏖️ Swap Day 2 for a relaxing beach & spa day",
        "🍣 Add authentic local foodie spots & sushi bar",
        "💰 Optimize itinerary for budget under $1,500",
        "🏛️ Include top historic landmarks & hidden gems",
        "☕ Find cozy specialty coffee shops nearby"
    )

    Scaffold(
        topBar = {
            Column {
                TravelTopHeader(
                    title = "Travel Plus AI",
                    subtitle = currentTrip?.destination ?: "Plan Your Dream Trip",
                    onSettingsClick = onNavigateToSettings,
                    actions = {
                        IconButton(onClick = { showTripSelectorDialog = true }) {
                            Icon(
                                Icons.Outlined.FolderSpecial,
                                contentDescription = "Select Trip",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                FailoverStatusBar(status = aiStatus)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showChatSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Concierge",
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = { Text("AI Concierge", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Trip Card
            item {
                if (currentTrip != null) {
                    TripHeroCard(
                        trip = currentTrip!!,
                        onLockToggle = { viewModel.lockCurrentTrip(!currentTrip!!.isLocked) },
                        onChatRefineClick = { showChatSheet = true }
                    )
                } else {
                    EmptyTripHero(onPlanClick = { showChatSheet = true })
                }
            }

            // Quick Refinement Prompt Chips
            item {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                    Text(
                        text = "Instant AI Refinements",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickPrompts) { prompt ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.sendMessage(prompt)
                                    showChatSheet = true
                                },
                                label = { Text(prompt, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            // Day Selector Tabs
            if (currentTrip != null && currentTrip!!.durationDays > 1) {
                item {
                    val daysCount = currentTrip!!.durationDays
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDayFilter == null,
                                onClick = { selectedDayFilter = null },
                                label = { Text("All Days") }
                            )
                        }
                        items((1..daysCount).toList()) { day ->
                            FilterChip(
                                selected = selectedDayFilter == day,
                                onClick = { selectedDayFilter = day },
                                label = { Text("Day $day") }
                            )
                        }
                    }
                }
            }

            // Itinerary Items Timeline
            val filteredItems = if (selectedDayFilter != null) {
                itineraryItems.filter { it.dayNumber == selectedDayFilter }
            } else {
                itineraryItems
            }

            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.FlightTakeoff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No activities yet",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask Travel Plus AI to draft your custom schedule!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val groupedByDay = filteredItems.groupBy { it.dayNumber }
                groupedByDay.forEach { (day, itemsForDay) ->
                    item {
                        DayHeader(dayNumber = day)
                    }
                    items(itemsForDay, key = { it.id }) { item ->
                        ItineraryItemCard(
                            item = item,
                            destinationCity = currentTrip?.destination ?: "",
                            onMapClick = {
                                DeepLinkHelper.openMapsSearch(
                                    context,
                                    item.locationName.ifBlank { "${item.title} ${currentTrip?.destination}" }
                                )
                            },
                            onUberClick = {
                                DeepLinkHelper.openUberRide(
                                    context,
                                    item.address.ifBlank { "${item.locationName} ${currentTrip?.destination}" }
                                )
                            },
                            onLyftClick = {
                                DeepLinkHelper.openLyftRide(
                                    context,
                                    item.address.ifBlank { "${item.locationName} ${currentTrip?.destination}" }
                                )
                            },
                            onBookClick = {
                                when (item.category.uppercase()) {
                                    "RESTAURANT" -> DeepLinkHelper.openRestaurantBooking(
                                        context,
                                        item.title,
                                        currentTrip?.destination ?: ""
                                    )
                                    "HOTEL" -> DeepLinkHelper.openHotelBooking(
                                        context,
                                        item.locationName.ifBlank { currentTrip?.destination ?: "" }
                                    )
                                    "FLIGHT" -> DeepLinkHelper.openFlightSearch(
                                        context,
                                        currentTrip?.departureCity ?: "NYC",
                                        currentTrip?.destination ?: "Tokyo"
                                    )
                                    else -> DeepLinkHelper.openMapsSearch(
                                        context,
                                        item.locationName.ifBlank { item.title }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // AI Chat & Refinement Bottom Sheet
    if (showChatSheet) {
        val chatListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showNewTripDialogInSheet by remember { mutableStateOf(false) }

        // Auto-scroll to bottom when new messages arrive
        LaunchedEffect(chatMessages.size, isGenerating) {
            if (chatMessages.isNotEmpty()) {
                chatListState.animateScrollToItem(chatMessages.size - 1)
            }
        }

        // Show scroll-to-bottom button when user scrolls up
        val showScrollToBottom by remember {
            derivedStateOf {
                val layoutInfo = chatListState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                if (totalItems == 0) false
                else {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleItem < totalItems - 1
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showChatSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .padding(horizontal = 16.dp)
            ) {
                // AI Concierge Header with Multi-Trip switcher & New Trip Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Travel Concierge",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (currentTrip != null) "Active: ${currentTrip!!.destination}" else "No trip selected",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = { showNewTripDialogInSheet = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Trip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { showChatSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Trips Quick Switcher Horizontal Bar
                if (allTrips.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allTrips.take(4).forEach { trip ->
                            val isSelected = trip.id == currentTrip?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectTrip(trip.id) },
                                label = {
                                    Text(
                                        text = trip.destination,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }

                // Chat Messages List with Scroll-to-bottom overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            ChatMessageBubble(msg = msg)
                        }
                        if (isGenerating) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.example.ui.components.TravelPlusLogo(
                                        size = 28.dp,
                                        animated = true
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "AI is drafting trip details & exploring places...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Floating Scroll-to-Bottom Arrow Button
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp, end = 8.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (chatMessages.isNotEmpty()) {
                                        chatListState.animateScrollToItem(chatMessages.size - 1)
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to bottom",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Quick Prompt Suggestion Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        SuggestionChip(
                            onClick = {
                                viewModel.sendMessage(prompt)
                            },
                            label = { Text(prompt, fontSize = 11.sp) }
                        )
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Ask to plan a trip, change dates, add places...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendMessage(chatInputText)
                                chatInputText = ""
                            }
                        },
                        enabled = !isGenerating && chatInputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isGenerating && chatInputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (!isGenerating && chatInputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showNewTripDialogInSheet) {
                CreateTripDialog(
                    onDismiss = { showNewTripDialogInSheet = false },
                    onConfirm = { dest, start, days, vibe, budget, dep ->
                        viewModel.createNewTrip(dest, start, "", days, vibe, budget, dep)
                        viewModel.sendMessage("Plan a comprehensive $days-day $vibe trip to $dest departing from $dep.")
                        showNewTripDialogInSheet = false
                    }
                )
            }
        }
    }

    // Trip Selector Dialog
    if (showTripSelectorDialog) {
        var showCreateTripDialogInSelector by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTripSelectorDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Trips", fontWeight = FontWeight.Bold)
                    FilledTonalButton(
                        onClick = { showCreateTripDialogInSelector = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New Trip", fontSize = 11.sp)
                    }
                }
            },
            text = {
                if (allTrips.isEmpty()) {
                    Text("No trips created yet. Tap '+ New Trip' to start planning!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allTrips) { trip ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (trip.id == currentTrip?.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectTrip(trip.id)
                                        showTripSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(trip.destination, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${trip.durationDays} Days • ${trip.vibe}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (trip.id == currentTrip?.id) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Active Trip",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTripSelectorDialog = false }) {
                    Text("Close")
                }
            }
        )

        if (showCreateTripDialogInSelector) {
            CreateTripDialog(
                onDismiss = { showCreateTripDialogInSelector = false },
                onConfirm = { dest, start, days, vibe, budget, dep ->
                    viewModel.createNewTrip(dest, start, "", days, vibe, budget, dep)
                    viewModel.sendMessage("Plan a comprehensive $days-day $vibe trip to $dest departing from $dep.")
                    showCreateTripDialogInSelector = false
                    showTripSelectorDialog = false
                }
            )
        }
    }
}

@Composable
fun CreateTripDialog(
    onDismiss: () -> Unit,
    onConfirm: (destination: String, startDate: String, durationDays: Int, vibe: String, budget: Double, departureCity: String) -> Unit
) {
    var destination by remember { mutableStateOf("") }
    var departureCity by remember { mutableStateOf("New York (JFK)") }
    var startDate by remember { mutableStateOf("Upcoming") }
    var durationDays by remember { mutableStateOf("4") }
    var vibe by remember { mutableStateOf("Balanced Explorer") }
    var budget by remember { mutableStateOf("1500") }

    val vibes = listOf("Balanced Explorer", "Culinary & Foodie", "Luxury & Relaxation", "Budget & Backpacking", "History & Culture", "Adventure & Nature")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plan a New Trip", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination (e.g. Paris, Tokyo, Maui)") },
                        placeholder = { Text("e.g. Rome, Italy") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = departureCity,
                        onValueChange = { departureCity = it },
                        label = { Text("Departure City / Airport") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationDays,
                            onValueChange = { durationDays = it },
                            label = { Text("Days") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = budget,
                            onValueChange = { budget = it },
                            label = { Text("Budget ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Text("Trip Vibe / Style:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(vibes) { v ->
                            val isSelected = vibe == v
                            FilterChip(
                                selected = isSelected,
                                onClick = { vibe = v },
                                label = { Text(v, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (destination.isNotBlank()) {
                        val days = durationDays.toIntOrNull() ?: 4
                        val bud = budget.toDoubleOrNull() ?: 1500.0
                        onConfirm(destination, startDate, days, vibe, bud, departureCity)
                    }
                },
                enabled = destination.isNotBlank()
            ) {
                Text("Start Planning")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TripHeroCard(
    trip: Trip,
    onLockToggle: () -> Unit,
    onChatRefineClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${trip.durationDays} Days • ${trip.vibe}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLockToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (trip.isLocked) Icons.Default.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock Itinerary",
                            tint = if (trip.isLocked) EmeraldGreen else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (trip.isLocked) {
                        Text(
                            text = "Locked",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = trip.destination,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (trip.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = trip.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Est. Budget",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$${trip.totalEstimatedBudget.toInt()} ${trip.currency}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Button(
                    onClick = onChatRefineClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refine with AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyTripHero(onPlanClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Active Trip Selected",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap below to chat with Travel Plus AI and create an itinerary instantly!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPlanClick) {
                Text("Start Planning with AI")
            }
        }
    }
}

@Composable
fun DayHeader(dayNumber: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$dayNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Day $dayNumber Schedule",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ItineraryItemCard(
    item: ItineraryItem,
    destinationCity: String,
    onMapClick: () -> Unit,
    onUberClick: () -> Unit,
    onLyftClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Time & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.timeSlot,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                CategoryBadge(category = item.category)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Description
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.locationName.isNotBlank() || item.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.locationName.ifBlank { item.address },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (item.estimatedCost > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Est. Cost: $${item.estimatedCost.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // One-Tap Deep Link Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onMapClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Maps", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = onUberClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Outlined.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uber/Lyft", fontSize = 11.sp)
                    }
                }

                val bookLabel = when (item.category.uppercase()) {
                    "RESTAURANT" -> "Book Table"
                    "HOTEL" -> "Book Stay"
                    "FLIGHT" -> "View Flights"
                    else -> "Explore"
                }

                Button(
                    onClick = onBookClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(bookLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: com.example.data.models.ChatMessage) {
    val isUser = msg.sender == "user"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = msg.content,
                    fontSize = 13.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (msg.modelUsed != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚡ Powered by ${msg.modelUsed}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
