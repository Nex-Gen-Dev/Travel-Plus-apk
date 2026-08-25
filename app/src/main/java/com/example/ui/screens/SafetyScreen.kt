package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.ReferenceDataStore
import com.example.ui.components.TravelTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()

    val currentCountry = currentTrip?.country?.ifBlank { "Japan" } ?: "Japan"
    val emergencyInfo = remember(currentCountry) {
        ReferenceDataStore.emergencyDirectory.find {
            currentCountry.contains(it.country, ignoreCase = true) || it.country.contains(currentCountry, ignoreCase = true)
        } ?: ReferenceDataStore.emergencyDirectory.first()
    }

    val advisory = remember(currentCountry) {
        ReferenceDataStore.travelAdvisories.find {
            currentCountry.contains(it.country, ignoreCase = true) || it.country.contains(currentCountry, ignoreCase = true)
        } ?: ReferenceDataStore.travelAdvisories.first()
    }

    Scaffold(
        topBar = {
            TravelTopHeader(
                title = "Safety & Emergency Hub",
                subtitle = "Local Hotlines, Embassies & SOS Dispatch",
                onSettingsClick = onNavigateToSettings
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
            // 1. One-Tap Emergency SOS Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFE4E6)
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
                                Icon(Icons.Default.Sos, contentDescription = null, tint = CoralRed, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Emergency SOS & Live Share",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF881337)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Immediately dispatch your current destination, trip dates, hotel address, and full itinerary via SMS / WhatsApp to your emergency contacts.",
                            fontSize = 12.sp,
                            color = Color(0xFF4C0519)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                DeepLinkHelper.shareEmergencySOS(
                                    context = context,
                                    tripSummary = currentTrip?.summary ?: "Trip to ${currentTrip?.destination}",
                                    destination = currentTrip?.destination ?: "International Travel",
                                    hotelAddress = "Hotel Gracery Shinjuku, Tokyo"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1-Tap Share Itinerary & SOS Alert", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Direct Emergency Hotlines
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
                            text = "🚨 Local Emergency Hotlines (${emergencyInfo.country})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Country Calling Code: ${emergencyInfo.dialCode} • Universal: ${emergencyInfo.generalEmergency}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EmergencyCallButton(
                                label = "Police",
                                number = emergencyInfo.police,
                                icon = Icons.Default.LocalPolice,
                                color = ElectricBlue,
                                modifier = Modifier.weight(1f),
                                onCall = { DeepLinkHelper.dialEmergency(context, emergencyInfo.police) }
                            )
                            EmergencyCallButton(
                                label = "Ambulance",
                                number = emergencyInfo.ambulance,
                                icon = Icons.Default.MedicalServices,
                                color = CoralRed,
                                modifier = Modifier.weight(1f),
                                onCall = { DeepLinkHelper.dialEmergency(context, emergencyInfo.ambulance) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EmergencyCallButton(
                                label = "Fire Dept",
                                number = emergencyInfo.fire,
                                icon = Icons.Default.LocalFireDepartment,
                                color = AmberGold,
                                modifier = Modifier.weight(1f),
                                onCall = { DeepLinkHelper.dialEmergency(context, emergencyInfo.fire) }
                            )
                            if (emergencyInfo.touristPolice.isNotBlank()) {
                                EmergencyCallButton(
                                    label = "Tourist Police",
                                    number = emergencyInfo.touristPolice,
                                    icon = Icons.Default.Shield,
                                    color = EmeraldGreen,
                                    modifier = Modifier.weight(1f),
                                    onCall = { DeepLinkHelper.dialEmergency(context, emergencyInfo.touristPolice) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Embassy Information
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
                            text = "🏛️ Embassy Contact Details (${advisory.country})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("US Embassy Address:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(advisory.usEmbassyAddress, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("24/7 Consular Emergency Phone: ${advisory.emergencyHotline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { DeepLinkHelper.dialEmergency(context, advisory.emergencyHotline) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Embassy", fontSize = 12.sp)
                            }
                            FilledTonalButton(
                                onClick = { DeepLinkHelper.openMapsSearch(context, advisory.usEmbassyAddress) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Directions", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyCallButton(
    label: String,
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onCall: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onCall() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                Text(number, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
