package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DocumentType
import com.example.data.models.TravelDocument
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val allDocuments by viewModel.allDocuments.collectAsStateWithLifecycle()

    var departureCity by remember(currentTrip) { mutableStateOf(currentTrip?.departureCity ?: "New York (JFK)") }
    var destinationCity by remember(currentTrip) { mutableStateOf(currentTrip?.destination ?: "Tokyo, Japan") }

    // In-App Browser State
    var showInAppBrowser by remember { mutableStateOf(false) }
    var browserInitialPortal by remember { mutableStateOf(TravelBookingPortal.GOOGLE_FLIGHTS) }
    var browserInitialUrl by remember { mutableStateOf("") }

    // Add / Edit Booking Dialog
    var showAddBookingDialog by remember { mutableStateOf(false) }
    var editingDocument by remember { mutableStateOf<TravelDocument?>(null) }

    // Selected Category Filter for Bookings Hub
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // ALL, BOARDING_PASS, HOTEL, CAR, TOUR

    // Filter documents belonging to current trip or general
    val tripBookings = remember(allDocuments, currentTrip, selectedCategoryFilter) {
        val filtered = allDocuments.filter { doc ->
            currentTrip == null || doc.tripId == null || doc.tripId == currentTrip?.id
        }
        when (selectedCategoryFilter) {
            "BOARDING_PASS" -> filtered.filter { it.docType == DocumentType.BOARDING_PASS.name || it.docType == DocumentType.FLIGHT_BOOKING.name }
            "HOTEL" -> filtered.filter { it.docType == DocumentType.HOTEL_CONFIRMATION.name }
            "CAR" -> filtered.filter { it.docType == DocumentType.CAR_RENTAL.name }
            "TOUR" -> filtered.filter { it.docType == DocumentType.ACTIVITY_BOOKING.name }
            else -> filtered
        }
    }

    if (showInAppBrowser) {
        InAppTravelBrowser(
            initialUrl = browserInitialUrl,
            initialPortal = browserInitialPortal,
            departureQuery = departureCity,
            destinationQuery = destinationCity,
            onClose = { showInAppBrowser = false },
            onSaveBooking = { newDoc ->
                viewModel.saveBooking(newDoc)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TravelTopHeader(
                    title = "Bookings & Travel Hub",
                    subtitle = currentTrip?.destination ?: "Search & Manage Trip Logistics",
                    onSettingsClick = onNavigateToSettings
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddBookingDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Booking") },
                    text = { Text("Add Pass / Booking", fontWeight = FontWeight.Bold) }
                )
            },
            modifier = modifier
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // Section 1: In-App Search Portals Grid
                // ==========================================
                item {
                    Text(
                        text = "In-App Flight & Stay Portals",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = departureCity,
                                    onValueChange = { departureCity = it },
                                    label = { Text("From") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Outlined.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                OutlinedTextField(
                                    value = destinationCity,
                                    onValueChange = { destinationCity = it },
                                    label = { Text("To") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Outlined.FlightLand, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }

                            Text(
                                text = "Search directly inside the app without leaving:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick In-App Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.GOOGLE_FLIGHTS
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google Flights", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.SKYSCANNER
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Skyscanner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Hotel, Airbnb & Car Rental Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.BOOKING_COM
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🏨 Booking.com", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.EXPEDIA
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🏖️ Expedia", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.AIRBNB
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🏡 Airbnb", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        browserInitialPortal = TravelBookingPortal.RENTALCARS
                                        browserInitialUrl = ""
                                        showInAppBrowser = true
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🚗 Rentalcars", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // Section 2: Confirmed Bookings & Boarding Passes
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Bookings & Boarding Passes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "${tripBookings.size} Saved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Category Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterOptions = listOf(
                            Pair("ALL", "All"),
                            Pair("BOARDING_PASS", "✈️ Passes & Flights"),
                            Pair("HOTEL", "🏨 Hotels"),
                            Pair("CAR", "🚗 Car Rentals"),
                            Pair("TOUR", "🎟️ Tours & Activities")
                        )
                        filterOptions.forEach { (key, label) ->
                            val isSelected = selectedCategoryFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // Bookings & Passes List
                if (tripBookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Outlined.ConfirmationNumber,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Bookings Added Yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Search flights or stays above and tap 'Save', or add a boarding pass with high-contrast QR code.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddBookingDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add First Booking")
                                }
                            }
                        }
                    }
                } else {
                    items(tripBookings, key = { it.id }) { booking ->
                        if (booking.docType == DocumentType.BOARDING_PASS.name || booking.docType == DocumentType.FLIGHT_BOOKING.name) {
                            // Render authentic Boarding Pass with QR code
                            BoardingPassCard(
                                document = booking,
                                onDelete = { viewModel.deleteTravelDocument(booking) },
                                onEdit = { editingDocument = booking },
                                onUpdatePhoto = { photoUri -> viewModel.updateDocumentPhoto(booking, photoUri) }
                            )
                        } else {
                            // Render Hotel / Car / Tour Booking Card
                            BookingSummaryCard(
                                document = booking,
                                onDelete = { viewModel.deleteTravelDocument(booking) },
                                onEdit = { editingDocument = booking },
                                onUpdatePhoto = { photoUri -> viewModel.updateDocumentPhoto(booking, photoUri) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Booking Dialog
    if (showAddBookingDialog || editingDocument != null) {
        val docToEdit = editingDocument
        AddOrEditBookingDialog(
            existingDocument = docToEdit,
            destinationQuery = destinationCity,
            departureQuery = departureCity,
            onDismiss = {
                showAddBookingDialog = false
                editingDocument = null
            },
            onSave = { doc ->
                if (docToEdit != null) {
                    viewModel.updateDocument(doc)
                } else {
                    viewModel.saveBooking(doc)
                }
                showAddBookingDialog = false
                editingDocument = null
            }
        )
    }
}

@Composable
fun BookingSummaryCard(
    document: TravelDocument,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onUpdatePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (document.docType) {
        DocumentType.HOTEL_CONFIRMATION.name -> "🏨"
        DocumentType.CAR_RENTAL.name -> "🚗"
        DocumentType.ACTIVITY_BOOKING.name -> "🎟️"
        else -> "📋"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(categoryIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = document.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${document.airlineOrProvider.ifBlank { "Booking" }} • Ref: ${document.confirmationCode.ifBlank { "N/A" }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (document.pricePaid.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = document.pricePaid,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (document.notes.isNotBlank()) {
                Text(
                    text = document.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddOrEditBookingDialog(
    existingDocument: TravelDocument?,
    destinationQuery: String,
    departureQuery: String,
    onDismiss: () -> Unit,
    onSave: (TravelDocument) -> Unit
) {
    var docType by remember { mutableStateOf(existingDocument?.docType ?: DocumentType.BOARDING_PASS.name) }
    var title by remember { mutableStateOf(existingDocument?.title ?: if (docType == DocumentType.BOARDING_PASS.name) "Flight to $destinationQuery" else "Reservation in $destinationQuery") }
    var provider by remember { mutableStateOf(existingDocument?.airlineOrProvider ?: if (docType == DocumentType.BOARDING_PASS.name) "American Airlines" else "Booking.com") }
    var confirmationCode by remember { mutableStateOf(existingDocument?.confirmationCode ?: "CONF-${(100000..999999).random()}") }
    var flightNumber by remember { mutableStateOf(existingDocument?.flightNumber ?: "AA 1084") }
    var departureAirport by remember { mutableStateOf(existingDocument?.departureAirport ?: departureQuery) }
    var arrivalAirport by remember { mutableStateOf(existingDocument?.arrivalAirport ?: destinationQuery) }
    var seatNumber by remember { mutableStateOf(existingDocument?.seatNumber ?: "14A") }
    var gate by remember { mutableStateOf(existingDocument?.gate ?: "B24") }
    var terminal by remember { mutableStateOf(existingDocument?.terminal ?: "T4") }
    var departureTime by remember { mutableStateOf(existingDocument?.departureTime ?: "10:45 AM") }
    var arrivalTime by remember { mutableStateOf(existingDocument?.arrivalTime ?: "02:30 PM +1") }
    var passengerName by remember { mutableStateOf(existingDocument?.holderName ?: "TRAVELER / EXPLORER") }
    var pricePaid by remember { mutableStateOf(existingDocument?.pricePaid ?: "$450.00") }
    var notes by remember { mutableStateOf(existingDocument?.notes ?: "") }

    val docTypes = listOf(
        Pair(DocumentType.BOARDING_PASS.name, "✈️ Boarding Pass"),
        Pair(DocumentType.HOTEL_CONFIRMATION.name, "🏨 Hotel / Stay"),
        Pair(DocumentType.CAR_RENTAL.name, "🚗 Rental Car"),
        Pair(DocumentType.ACTIVITY_BOOKING.name, "🎟️ Activity / Tour")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingDocument != null) "Edit Booking Details" else "Add New Booking or Pass",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Type selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        docTypes.forEach { (key, label) ->
                            val isSelected = docType == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { docType = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = provider,
                            onValueChange = { provider = it },
                            label = { Text("Provider / Airline") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = confirmationCode,
                            onValueChange = { confirmationCode = it },
                            label = { Text("Confirmation # / PNR") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (docType == DocumentType.BOARDING_PASS.name || docType == DocumentType.FLIGHT_BOOKING.name) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = flightNumber,
                                onValueChange = { flightNumber = it },
                                label = { Text("Flight #") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = passengerName,
                                onValueChange = { passengerName = it },
                                label = { Text("Passenger Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = departureAirport,
                                onValueChange = { departureAirport = it },
                                label = { Text("From (Airport)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = arrivalAirport,
                                onValueChange = { arrivalAirport = it },
                                label = { Text("To (Airport)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = seatNumber,
                                onValueChange = { seatNumber = it },
                                label = { Text("Seat") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gate,
                                onValueChange = { gate = it },
                                label = { Text("Gate") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = terminal,
                                onValueChange = { terminal = it },
                                label = { Text("Terminal") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = departureTime,
                                onValueChange = { departureTime = it },
                                label = { Text("Dep Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = arrivalTime,
                                onValueChange = { arrivalTime = it },
                                label = { Text("Arr Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = pricePaid,
                        onValueChange = { pricePaid = it },
                        label = { Text("Cost / Price") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Gate Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (existingDocument ?: TravelDocument(title = title)).copy(
                        title = title,
                        docType = docType,
                        airlineOrProvider = provider,
                        confirmationCode = confirmationCode,
                        flightNumber = flightNumber,
                        holderName = passengerName,
                        departureAirport = departureAirport,
                        arrivalAirport = arrivalAirport,
                        seatNumber = seatNumber,
                        gate = gate,
                        terminal = terminal,
                        departureTime = departureTime,
                        arrivalTime = arrivalTime,
                        pricePaid = pricePaid,
                        notes = notes,
                        qrBarcodeData = confirmationCode
                    )
                    onSave(updated)
                }
            ) {
                Text(if (existingDocument != null) "Update" else "Save Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
