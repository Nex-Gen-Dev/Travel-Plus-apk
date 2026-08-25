package com.example.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.models.TravelDocument
import com.example.util.QRCodeGenerator

@Composable
fun BoardingPassCard(
    document: TravelDocument,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onUpdatePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFullScreenQR by remember { mutableStateOf(false) }
    var isScannerBrightnessOn by remember { mutableStateOf(false) }

    // Image Picker for Uploading Boarding Pass PDF/Image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onUpdatePhoto(it.toString()) }
    }

    // Toggle Screen Brightness for Airport Gate Scanner
    DisposableEffect(isScannerBrightnessOn) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness
        if (isScannerBrightnessOn) {
            val layoutParams = window?.attributes
            layoutParams?.screenBrightness = 1.0f // Max brightness
            window?.attributes = layoutParams
        }
        onDispose {
            if (isScannerBrightnessOn) {
                val layoutParams = window?.attributes
                layoutParams?.screenBrightness = originalBrightness ?: -1f
                window?.attributes = layoutParams
            }
        }
    }

    val flightNum = document.flightNumber.ifBlank { "AA 1084" }
    val airline = document.airlineOrProvider.ifBlank { "American Airlines" }
    val depCode = document.departureAirport.take(3).uppercase().ifBlank { "JFK" }
    val arrCode = document.arrivalAirport.take(3).uppercase().ifBlank { "HND" }
    val passenger = document.holderName.ifBlank { "TRAVELER / EXPLORER" }
    val seat = document.seatNumber.ifBlank { "14A" }
    val gate = document.gate.ifBlank { "B24" }
    val terminal = document.terminal.ifBlank { "T4" }
    val group = document.boardingGroup.ifBlank { "Group 2" }
    val pnr = document.confirmationCode.ifBlank { "PNR894" }
    val depTime = document.departureTime.ifBlank { "10:45 AM" }
    val qrData = document.qrBarcodeData.ifBlank { "M1$passenger $pnr $depCode$arrCode$flightNum $seat" }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Airline & Flight Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = airline,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Flight $flightNum • ${document.cabinClass}",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "PNR: $pnr",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Airport Codes & Flight Path
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = depCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = document.departureAirport.ifBlank { "New York (JFK)" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = depTime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Flight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "NON-STOP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = arrCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = document.arrivalAirport.ifBlank { "Tokyo (HND)" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = document.arrivalTime.ifBlank { "02:30 PM +1" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Ticket Notch Perforated Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // Gate, Seat, Terminal & Passenger Details Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PassDetailBlock(label = "PASSENGER", value = passenger)
                PassDetailBlock(label = "SEAT", value = seat, isHighlight = true)
                PassDetailBlock(label = "GATE", value = gate, isHighlight = true)
                PassDetailBlock(label = "TERM", value = terminal)
                PassDetailBlock(label = "GROUP", value = group)
            }

            // Attached Photo / PDF preview if user uploaded one
            if (document.photoUri.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = document.photoUri,
                        contentDescription = "Uploaded Boarding Pass Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }

            // Scannable High-Contrast QR Code & Barcode Block
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .clickable { isFullScreenQR = true },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val qrBitmap = remember(qrData) {
                        QRCodeGenerator.generateQRCodeBitmap(qrData, 360)
                    }

                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Boarding Pass QR Code for Gate Scanner",
                        modifier = Modifier.size(160.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val barcode1D = remember(qrData) {
                        QRCodeGenerator.generateBarcode1DBitmap(qrData, 500, 80)
                    }
                    Image(
                        bitmap = barcode1D,
                        contentDescription = "1D Gate Barcode",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(40.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TAP TO EXPAND FOR AIRPORT SCANNER",
                        color = Color(0xFF1E293B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Actions & Brightness Switch Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Airport Scanner Max Brightness toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isScannerBrightnessOn = !isScannerBrightnessOn }
                ) {
                    Icon(
                        if (isScannerBrightnessOn) Icons.Filled.BrightnessHigh else Icons.Outlined.BrightnessMedium,
                        contentDescription = null,
                        tint = if (isScannerBrightnessOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScannerBrightnessOn) "Scanner Brightness: ON" else "Scanner Brightness",
                        fontSize = 11.sp,
                        fontWeight = if (isScannerBrightnessOn) FontWeight.Bold else FontWeight.Normal,
                        color = if (isScannerBrightnessOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = "Upload Pass Photo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Details", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // Full Screen High-Contrast Scanner Mode Dialog
    if (isFullScreenQR) {
        Dialog(
            onDismissRequest = { isFullScreenQR = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$airline • Flight $flightNum",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "$depCode → $arrCode • Seat $seat • Gate $gate",
                                color = Color(0xFF475569),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = { isFullScreenQR = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }

                    // Extra Large High-Contrast QR Code for gate scanners
                    val fullQrBitmap = remember(qrData) {
                        QRCodeGenerator.generateQRCodeBitmap(qrData, 800)
                    }
                    Image(
                        bitmap = fullQrBitmap,
                        contentDescription = "Full Screen QR Code",
                        modifier = Modifier.size(280.dp)
                    )

                    // 1D Barcode
                    val fullBarcode = remember(qrData) {
                        QRCodeGenerator.generateBarcode1DBitmap(qrData, 700, 120)
                    }
                    Image(
                        bitmap = fullBarcode,
                        contentDescription = "Full Barcode",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HOLD PHONE DIRECTLY UNDER AIRPORT SCANNER",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { isFullScreenQR = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Done Scanning", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassDetailBlock(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 13.sp,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.SemiBold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontFamily = if (isHighlight) FontFamily.Monospace else FontFamily.Default
        )
    }
}
