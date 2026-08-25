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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TravelTopHeader
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

    var departureCity by remember { mutableStateOf(currentTrip?.departureCity ?: "New York (JFK)") }
    var destinationCity by remember { mutableStateOf(currentTrip?.destination ?: "Tokyo, Japan") }

    // Road trip & Fuel tool states
    var tripDistanceMiles by remember { mutableStateOf("450") }
    var vehicleMpg by remember { mutableStateOf("28") }
    var gasPricePerGallon by remember { mutableStateOf("3.65") }

    // Toll estimator state
    var selectedRouteType by remember { mutableStateOf("Major Interstate & Turnpike") }

    Scaffold(
        topBar = {
            TravelTopHeader(
                title = "Booking & Transport Hub",
                subtitle = "Direct Deep-Links & Logistics Companion",
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
            // Section 1: Flights Hub
            item {
                BookingSectionCard(
                    title = "✈️ Flights Search & Booking",
                    subtitle = "Compare real-time rates across top flight engines"
                ) {
                    OutlinedTextField(
                        value = departureCity,
                        onValueChange = { departureCity = it },
                        label = { Text("From (Origin)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.FlightTakeoff, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = destinationCity,
                        onValueChange = { destinationCity = it },
                        label = { Text("To (Destination)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.FlightLand, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                DeepLinkHelper.openFlightSearch(context, departureCity, destinationCity)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Google Flights", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                        FilledTonalButton(
                            onClick = {
                                DeepLinkHelper.openSkyscannerFlight(context, destinationCity)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Skyscanner", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Section 2: Hotel & Lodging Hub
            item {
                BookingSectionCard(
                    title = "🏨 Hotel & Lodging Finder",
                    subtitle = "Explore verified stays with best price guarantees"
                ) {
                    Text(
                        text = "Destination: $destinationCity",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                DeepLinkHelper.openHotelBooking(context, destinationCity, "Booking.com")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Booking.com", fontSize = 12.sp)
                        }
                        FilledTonalButton(
                            onClick = {
                                DeepLinkHelper.openHotelBooking(context, destinationCity, "Expedia")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Expedia", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                DeepLinkHelper.openHotelBooking(context, destinationCity, "Airbnb")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Airbnb Homes", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                DeepLinkHelper.openCarRentalSearch(context, destinationCity)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rental Cars", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Section 3: Ride-Hailing & Public Transit Quick Links
            item {
                BookingSectionCard(
                    title = "🚗 Ride-Hailing & Public Transit",
                    subtitle = "Instant transport connections in ${currentTrip?.destination ?: "destination"}"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF000000),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    DeepLinkHelper.openUberRide(context, destinationCity)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = "Uber", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Uber Ride", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Request now", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }

                        Surface(
                            color = Color(0xFFFF00BF),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    DeepLinkHelper.openLyftRide(context, destinationCity)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = "Lyft", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Lyft Ride", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Open app", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = {
                            DeepLinkHelper.openTransitDirections(context, destinationCity)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DirectionsSubway, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore Subway, Metro & Transit Routes")
                    }
                }
            }

            // Section 4: Road Trip Fuel & Toll Cost Estimator
            item {
                BookingSectionCard(
                    title = "⛽ Road Trip Fuel & Toll Cost Estimator",
                    subtitle = "Calculate highway fuel stops and estimated road toll fees"
                ) {
                    val dist = tripDistanceMiles.toDoubleOrNull() ?: 450.0
                    val mpg = vehicleMpg.toDoubleOrNull() ?: 28.0
                    val pricePerGal = gasPricePerGallon.toDoubleOrNull() ?: 3.65

                    val totalGallons = if (mpg > 0) dist / mpg else 0.0
                    val totalFuelCost = totalGallons * pricePerGal
                    val estimatedTolls = when (selectedRouteType) {
                        "Major Interstate & Turnpike" -> dist * 0.055
                        "Scenic Byways (No Tolls)" -> 0.0
                        else -> dist * 0.035
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tripDistanceMiles,
                            onValueChange = { tripDistanceMiles = it },
                            label = { Text("Distance (mi)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = vehicleMpg,
                            onValueChange = { vehicleMpg = it },
                            label = { Text("MPG") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gasPricePerGallon,
                            onValueChange = { gasPricePerGallon = it },
                            label = { Text("$/Gallon") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Est. Fuel Cost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("$${"%.2f".format(totalFuelCost)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${"%.1f".format(totalGallons)} gal total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text("Est. Tolls", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("$${"%.2f".format(estimatedTolls)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                Text(selectedRouteType.take(15) + "...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text("Total Driving", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("$${"%.2f".format(totalFuelCost + estimatedTolls)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                                Text("Fuel + Tolls", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            DeepLinkHelper.openMapsSearch(context, "Gas stations along route to $destinationCity")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Find Nearby Cheap Gas Stations on Maps")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
