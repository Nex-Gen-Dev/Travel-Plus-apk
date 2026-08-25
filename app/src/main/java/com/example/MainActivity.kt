package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.UpdateAvailableDialog
import com.example.ui.screens.*
import com.example.ui.theme.TravelPlusTheme
import com.example.ui.viewmodel.TravelViewModel

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    ITINERARY("itinerary", "Itinerary", Icons.Outlined.Map, Icons.Filled.Map),
    BOOKING("booking", "Bookings", Icons.Outlined.Flight, Icons.Filled.Flight),
    SHOPPING("shopping", "Packing", Icons.Outlined.Checklist, Icons.Filled.Checklist),
    DOCUMENTS("documents", "Docs & Visa", Icons.Outlined.Badge, Icons.Filled.Badge),
    MONEY("money", "Money", Icons.Outlined.Paid, Icons.Filled.Paid),
    SAFETY("safety", "Safety & SOS", Icons.Outlined.Shield, Icons.Filled.Shield)
}

class MainActivity : ComponentActivity() {
    private val travelViewModel: TravelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelPlusTheme {
                TravelPlusApp(viewModel = travelViewModel)
            }
        }
    }
}

@Composable
fun TravelPlusApp(viewModel: TravelViewModel) {
    val activeTabStr by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentTab = remember(activeTabStr) {
        NavigationTab.entries.find { it.name.equals(activeTabStr, ignoreCase = true) || it.route.equals(activeTabStr, ignoreCase = true) } ?: NavigationTab.ITINERARY
    }
    var showSettingsScreen by remember { mutableStateOf(false) }

    // GitHub Updates State
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val activeRelease by viewModel.activeReleaseForDialog.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showSettingsScreen) {
            AISettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { showSettingsScreen = false }
            )
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setActiveTab(tab.name) },
                                icon = {
                                    Icon(
                                        if (isSelected) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        NavigationTab.ITINERARY -> ItineraryScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                        NavigationTab.BOOKING -> BookingScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                        NavigationTab.SHOPPING -> ShoppingScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                        NavigationTab.DOCUMENTS -> DocumentsScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                        NavigationTab.MONEY -> MoneyScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                        NavigationTab.SAFETY -> SafetyScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettingsScreen = true }
                        )
                    }
                }
            }
        }

        // Automatic / Prompted In-App Update Dialog
        if (showUpdateDialog && activeRelease != null) {
            UpdateAvailableDialog(
                release = activeRelease!!,
                currentVersion = viewModel.currentAppVersion,
                downloadState = downloadState,
                onUpdateClicked = {
                    viewModel.startDownloadAndInstall(activeRelease!!)
                },
                onLaterClicked = {
                    viewModel.dismissUpdateDialog(rememberLater = true)
                },
                onInstallFileClicked = { file ->
                    viewModel.installDownloadedApk(file)
                },
                onDismiss = {
                    viewModel.dismissUpdateDialog(rememberLater = false)
                }
            )
        }
    }
}


