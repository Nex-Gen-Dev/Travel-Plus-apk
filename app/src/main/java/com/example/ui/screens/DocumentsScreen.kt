package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DocumentType
import com.example.data.models.ReferenceDataStore
import com.example.data.models.TravelDocument
import com.example.ui.components.TravelTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val nationality by viewModel.selectedNationality.collectAsStateWithLifecycle()
    val visaDestination by viewModel.selectedVisaDestination.collectAsStateWithLifecycle()

    var showAddDocDialog by remember { mutableStateOf(false) }
    var newDocTitle by remember { mutableStateOf("") }
    var newDocType by remember { mutableStateOf(DocumentType.PASSPORT.name) }
    var newDocNumber by remember { mutableStateOf("") }
    var newDocHolder by remember { mutableStateOf("") }
    var newDocExpiry by remember { mutableStateOf("2030-08-15") }
    var newDocNotes by remember { mutableStateOf("") }

    val visaInfo = remember(visaDestination, nationality) {
        ReferenceDataStore.getVisaRequirement(visaDestination, nationality)
    }

    val advisoryInfo = remember(currentTrip?.country, currentTrip?.destination) {
        val search = currentTrip?.country ?: currentTrip?.destination ?: "Japan"
        ReferenceDataStore.travelAdvisories.find { advisory ->
            search.contains(advisory.country, ignoreCase = true) || advisory.country.contains(search, ignoreCase = true)
        } ?: ReferenceDataStore.travelAdvisories.first()
    }

    Scaffold(
        topBar = {
            TravelTopHeader(
                title = "Documents & Border Info",
                subtitle = "Encrypted On-Device Vault & Visa Rules",
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDocDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Document") }
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
            // 1. Passport 6-Month Rule & Validity Tracker
            val passportDoc = documents.find { it.docType == DocumentType.PASSPORT.name }
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Passport Validity Status",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "On-Device Encrypted",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (passportDoc != null) {
                            val (monthsLeft, isSafe) = viewModel.checkPassportValidity(passportDoc.expiryDate)
                            Text(
                                text = "Passport: ${passportDoc.documentNumber} (${passportDoc.holderName.ifBlank { "Primary Traveler" }})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Expires: ${passportDoc.expiryDate} (~$monthsLeft months remaining)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isSafe) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Passes 6-Month Rule! Safe for international border entry.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF14532D)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFFFE4E6),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Warning: Less than 6 months validity left. Many airlines will deny boarding!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF881337)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No passport logged yet. Add your passport expiry to activate automatic 6-month international entry checks.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 2. Visa Requirement Lookup
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🛂 Visa & Border Requirement Checker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant immigration status by nationality and destination",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nationality,
                                onValueChange = { viewModel.selectedNationality.value = it },
                                label = { Text("Passport Country") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = visaDestination,
                                onValueChange = { viewModel.selectedVisaDestination.value = it },
                                label = { Text("Destination") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = visaInfo.destinationCountry,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        color = if (visaInfo.status.contains("Free", true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = visaInfo.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (visaInfo.status.contains("Free", true)) Color(0xFF14532D) else Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Max Stay: ${visaInfo.maxStay}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = visaInfo.notes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { DeepLinkHelper.openWebUrl(context, visaInfo.officialPortalUrl) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Open Official Government Visa Portal", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Travel Advisory & Safety Feed
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ Travel Advisory (${advisoryInfo.country})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val levelColor = when (advisoryInfo.level) {
                                1 -> EmeraldGreen
                                2 -> AmberGold
                                3 -> Color(0xFFEA580C)
                                else -> CoralRed
                            }
                            Surface(
                                color = levelColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Level ${advisoryInfo.level}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = advisoryInfo.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = advisoryInfo.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "US Embassy: ${advisoryInfo.usEmbassyAddress}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Offline Boarding Pass & Trip Card (Airplane Mode Ready)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Offline Boarding Pass Card", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Works Offline", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PASSENGER", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Primary Explorer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CONFIRMATION", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("TP-849204", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("FROM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentTrip?.departureCity ?: "JFK / New York", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DESTINATION", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentTrip?.destination ?: "Tokyo (HND)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Simulated QR Code bar for offline scanning
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "||| | |||| | ||| ||||| || |||| || |||| |||",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp,
                                    letterSpacing = 4.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. Document Wallet List
            item {
                Text(
                    text = "Stored Travel Documents (${documents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(documents, key = { it.id }) { doc ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when (doc.docType) {
                                        DocumentType.PASSPORT.name -> Icons.Default.Badge
                                        DocumentType.INSURANCE.name -> Icons.Default.HealthAndSafety
                                        DocumentType.BOARDING_PASS.name -> Icons.Default.FlightTakeoff
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "No: ${doc.documentNumber.ifBlank { "N/A" }} • Exp: ${doc.expiryDate.ifBlank { "N/A" }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.deleteTravelDocument(doc) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Document Dialog
    if (showAddDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("Add Travel Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("Document Title (e.g. Allianz Insurance)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocNumber,
                        onValueChange = { newDocNumber = it },
                        label = { Text("Document / Policy Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocHolder,
                        onValueChange = { newDocHolder = it },
                        label = { Text("Holder Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocExpiry,
                        onValueChange = { newDocExpiry = it },
                        label = { Text("Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDocTitle.isNotBlank()) {
                            viewModel.addTravelDocument(
                                title = newDocTitle,
                                docType = newDocType,
                                documentNumber = newDocNumber,
                                holderName = newDocHolder,
                                expiryDate = newDocExpiry,
                                notes = newDocNotes
                            )
                            newDocTitle = ""
                            newDocNumber = ""
                            showAddDocDialog = false
                        }
                    }
                ) {
                    Text("Save to Encrypted Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
