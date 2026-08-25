package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.DocumentType
import com.example.data.models.TravelDocument

enum class TravelBookingPortal(
    val title: String,
    val category: String,
    val baseUrl: String,
    val iconName: String
) {
    GOOGLE_FLIGHTS("Google Flights", "FLIGHT", "https://www.google.com/travel/flights", "flight"),
    SKYSCANNER("Skyscanner", "FLIGHT", "https://www.skyscanner.com", "flight_takeoff"),
    BOOKING_COM("Booking.com", "HOTEL", "https://www.booking.com", "hotel"),
    EXPEDIA("Expedia", "HOTEL", "https://www.expedia.com", "apartment"),
    AIRBNB("Airbnb", "HOTEL", "https://www.airbnb.com", "home"),
    RENTALCARS("Rentalcars", "CAR", "https://www.rentalcars.com", "directions_car")
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppTravelBrowser(
    initialUrl: String,
    initialPortal: TravelBookingPortal = TravelBookingPortal.GOOGLE_FLIGHTS,
    departureQuery: String = "",
    destinationQuery: String = "",
    onClose: () -> Unit,
    onSaveBooking: (TravelDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPortal by remember { mutableStateOf(initialPortal) }
    var currentUrl by remember { mutableStateOf(buildInitialUrl(initialPortal, initialUrl, departureQuery, destinationQuery)) }
    var pageTitle by remember { mutableStateOf(currentPortal.title) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showAddBookingDialog by remember { mutableStateOf(false) }

    // Intercept hardware back button to navigate webview back first
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close Browser")
                        }

                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = canGoBack
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = canGoForward
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        IconButton(onClick = { webViewInstance?.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload")
                        }

                        // URL / Title Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = "Secure",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pageTitle.ifBlank { currentPortal.title },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quick Capture / Add to Bookings Button
                        FilledTonalButton(
                            onClick = { showAddBookingDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Linear progress indicator while loading
                    if (isLoading) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }

                    // Portal Quick-Switch Chips Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TravelBookingPortal.entries.forEach { portal ->
                            val isSelected = portal == currentPortal
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (currentPortal != portal) {
                                        currentPortal = portal
                                        val newUrl = buildInitialUrl(portal, portal.baseUrl, departureQuery, destinationQuery)
                                        currentUrl = newUrl
                                        webViewInstance?.loadUrl(newUrl)
                                    }
                                },
                                label = {
                                    Text(
                                        text = portal.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (portal) {
                                        TravelBookingPortal.GOOGLE_FLIGHTS -> "✈️"
                                        TravelBookingPortal.SKYSCANNER -> "🛫"
                                        TravelBookingPortal.BOOKING_COM -> "🏨"
                                        TravelBookingPortal.EXPEDIA -> "🏖️"
                                        TravelBookingPortal.AIRBNB -> "🏡"
                                        TravelBookingPortal.RENTALCARS -> "🚗"
                                    }
                                    Text(icon, fontSize = 12.sp)
                                }
                            )
                        }
                    }
                }
            }

            // Embedded WebView Core
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportZoom(true)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                // Modern Mobile User Agent
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    url?.let { currentUrl = it }
                                    pageTitle = view?.title ?: ""
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress / 100f
                                    if (newProgress >= 100) isLoading = false
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                    title?.let { pageTitle = it }
                                }
                            }

                            loadUrl(currentUrl)
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                    }
                )
            }
        }
    }

    // Quick Capture to Bookings Dialog
    if (showAddBookingDialog) {
        QuickSaveBookingDialog(
            portal = currentPortal,
            currentUrl = currentUrl,
            destinationQuery = destinationQuery,
            departureQuery = departureQuery,
            onDismiss = { showAddBookingDialog = false },
            onConfirmSave = { doc ->
                onSaveBooking(doc)
                showAddBookingDialog = false
            }
        )
    }
}

@Composable
fun QuickSaveBookingDialog(
    portal: TravelBookingPortal,
    currentUrl: String,
    destinationQuery: String,
    departureQuery: String,
    onDismiss: () -> Unit,
    onConfirmSave: (TravelDocument) -> Unit
) {
    val defaultDocType = when (portal.category) {
        "FLIGHT" -> DocumentType.FLIGHT_BOOKING.name
        "HOTEL" -> DocumentType.HOTEL_CONFIRMATION.name
        "CAR" -> DocumentType.CAR_RENTAL.name
        else -> DocumentType.ACTIVITY_BOOKING.name
    }

    var title by remember {
        mutableStateOf(
            when (portal) {
                TravelBookingPortal.GOOGLE_FLIGHTS, TravelBookingPortal.SKYSCANNER -> "Flight: $departureQuery → $destinationQuery"
                TravelBookingPortal.BOOKING_COM, TravelBookingPortal.EXPEDIA, TravelBookingPortal.AIRBNB -> "Stay in $destinationQuery"
                TravelBookingPortal.RENTALCARS -> "Car Rental in $destinationQuery"
            }
        )
    }
    var provider by remember { mutableStateOf(portal.title) }
    var confirmationCode by remember { mutableStateOf("CONF-${(100000..999999).random()}") }
    var flightOrRoomNumber by remember { mutableStateOf(if (portal.category == "FLIGHT") "AA 1084" else "Deluxe Suite") }
    var dates by remember { mutableStateOf("Upcoming Dates") }
    var pricePaid by remember { mutableStateOf("$350.00") }
    var notes by remember { mutableStateOf("Booked via in-app portal ($currentUrl)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BookmarkAdded, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to My Bookings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Save this ${portal.title} reservation into your Bookings tab:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Booking Title / Route") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = { Text("Provider") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmationCode,
                        onValueChange = { confirmationCode = it },
                        label = { Text("Confirmation #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = flightOrRoomNumber,
                        onValueChange = { flightOrRoomNumber = it },
                        label = { Text(if (portal.category == "FLIGHT") "Flight #" else "Room / Vehicle") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pricePaid,
                        onValueChange = { pricePaid = it },
                        label = { Text("Price / Total") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Details") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newDoc = TravelDocument(
                        title = title,
                        docType = defaultDocType,
                        airlineOrProvider = provider,
                        confirmationCode = confirmationCode,
                        flightNumber = if (portal.category == "FLIGHT") flightOrRoomNumber else "",
                        pricePaid = pricePaid,
                        notes = notes,
                        departureAirport = departureQuery,
                        arrivalAirport = destinationQuery,
                        departureDate = dates,
                        qrBarcodeData = confirmationCode
                    )
                    onConfirmSave(newDoc)
                }
            ) {
                Text("Add to Bookings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun buildInitialUrl(
    portal: TravelBookingPortal,
    customUrl: String,
    departure: String,
    destination: String
): String {
    if (customUrl.isNotBlank() && customUrl.startsWith("http") && customUrl != portal.baseUrl) {
        return customUrl
    }
    val cleanDest = destination.trim().replace(" ", "+")
    val cleanDep = departure.trim().replace(" ", "+")

    return when (portal) {
        TravelBookingPortal.GOOGLE_FLIGHTS -> {
            if (cleanDest.isNotBlank()) "https://www.google.com/travel/flights?q=flights+from+$cleanDep+to+$cleanDest"
            else "https://www.google.com/travel/flights"
        }
        TravelBookingPortal.SKYSCANNER -> {
            if (cleanDest.isNotBlank()) "https://www.skyscanner.com/transport/flights/everywhere/$cleanDest"
            else "https://www.skyscanner.com"
        }
        TravelBookingPortal.BOOKING_COM -> {
            if (cleanDest.isNotBlank()) "https://www.booking.com/searchresults.html?ss=$cleanDest"
            else "https://www.booking.com"
        }
        TravelBookingPortal.EXPEDIA -> {
            if (cleanDest.isNotBlank()) "https://www.expedia.com/Hotels?destination=$cleanDest"
            else "https://www.expedia.com"
        }
        TravelBookingPortal.AIRBNB -> {
            if (cleanDest.isNotBlank()) "https://www.airbnb.com/s/$cleanDest/homes"
            else "https://www.airbnb.com"
        }
        TravelBookingPortal.RENTALCARS -> {
            "https://www.rentalcars.com/"
        }
    }
}
