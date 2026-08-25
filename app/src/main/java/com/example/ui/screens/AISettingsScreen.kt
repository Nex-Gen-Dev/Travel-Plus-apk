package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.AIEngineStatus
import com.example.data.updater.DownloadState
import com.example.data.updater.GitHubRelease
import com.example.data.updater.UpdateCheckResult
import com.example.ui.components.FailoverStatusBar
import com.example.ui.components.TravelTopHeader
import com.example.ui.components.UpdateChangelogView
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    viewModel: TravelViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiStatus by viewModel.aiEngineStatus.collectAsStateWithLifecycle()
    val savedApiKey by viewModel.openRouterApiKey.collectAsStateWithLifecycle()
    val savedModel by viewModel.selectedAIModel.collectAsStateWithLifecycle()

    // Update State
    val updateResult by viewModel.updateCheckResult.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val repoOwner by viewModel.repoOwner.collectAsStateWithLifecycle()
    val repoName by viewModel.repoName.collectAsStateWithLifecycle()
    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsStateWithLifecycle()

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var keySavedSuccess by remember { mutableStateOf(false) }
    var selectedModel by remember(savedModel) { mutableStateOf(savedModel) }
    var showModelDropdown by remember { mutableStateOf(false) }
    var isCustomModel by remember { mutableStateOf(false) }

    var showRepoConfigDialog by remember { mutableStateOf(false) }
    var editRepoOwner by remember { mutableStateOf(repoOwner) }
    var editRepoName by remember { mutableStateOf(repoName) }

    val popularModels = listOf(
        Pair("google/gemini-2.0-flash-001", "Google Gemini 2.0 Flash (Fast & Smart)"),
        Pair("anthropic/claude-3.5-sonnet", "Anthropic Claude 3.5 Sonnet (Best Reasoning)"),
        Pair("openai/gpt-4o-mini", "OpenAI GPT-4o Mini (Efficient)"),
        Pair("meta-llama/llama-3.3-70b-instruct", "Meta Llama 3.3 70B (Open Weights)"),
        Pair("deepseek/deepseek-chat", "DeepSeek V3 (High Accuracy)"),
        Pair("custom", "Custom Model Identifier...")
    )

    Scaffold(
        topBar = {
            Column {
                TravelTopHeader(
                    title = "App Settings & AI Key",
                    subtitle = "OpenRouter Configuration & System Updates",
                    actions = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )
                FailoverStatusBar(status = aiStatus)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // Section 1: Global OpenRouter AI Key
            // ==========================================
            item {
                Text(
                    text = "OpenRouter AI Integration",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "OpenRouter API Key",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Powers all live AI trip planning & concierge features",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Status Indicator
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (savedApiKey.isNotBlank()) EmeraldGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (savedApiKey.isNotBlank()) "ACTIVE" else "NOT SET",
                                    color = if (savedApiKey.isNotBlank()) EmeraldGreen else AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // API Key Input
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                keySavedSuccess = false
                            },
                            label = { Text("OpenRouter API Key (sk-or-v1-...)") },
                            placeholder = { Text("sk-or-v1-xxxxxxxxxxxxxxxxxxxx") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            supportingText = {
                                Text(
                                    if (savedApiKey.isNotBlank()) "✅ Key configured & saved securely on-device."
                                    else "Paste your OpenRouter key here to enable live AI trip generation."
                                )
                            }
                        )

                        // Model Selector Dropdown / Selection
                        ExposedDropdownMenuBox(
                            expanded = showModelDropdown,
                            onExpandedChange = { showModelDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = popularModels.find { it.first == selectedModel }?.second ?: selectedModel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Preferred AI Model") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModelDropdown) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = showModelDropdown,
                                onDismissRequest = { showModelDropdown = false }
                            ) {
                                popularModels.forEach { (modelId, displayName) ->
                                    DropdownMenuItem(
                                        text = { Text(displayName, fontSize = 13.sp) },
                                        onClick = {
                                            if (modelId == "custom") {
                                                isCustomModel = true
                                            } else {
                                                isCustomModel = false
                                                selectedModel = modelId
                                                viewModel.setSelectedAIModel(modelId)
                                            }
                                            showModelDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isCustomModel) {
                            OutlinedTextField(
                                value = selectedModel,
                                onValueChange = {
                                    selectedModel = it
                                    viewModel.setSelectedAIModel(it)
                                },
                                label = { Text("Custom Model ID (e.g. mistralai/mistral-large)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Save & Test Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setGlobalOpenRouterKey(apiKeyInput)
                                    keySavedSuccess = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }

                            if (apiKeyInput.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        apiKeyInput = ""
                                        viewModel.setGlobalOpenRouterKey("")
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear")
                                }
                            }
                        }

                        if (keySavedSuccess) {
                            Text(
                                text = "✅ OpenRouter key saved successfully! The AI Concierge is ready to plan trips.",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ==========================================
            // Section 2: GitHub Releases & In-App Updates
            // ==========================================
            item {
                Text(
                    text = "App Updates & GitHub Releases",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Version",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "v${viewModel.currentAppVersion} (Production Build)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { viewModel.checkForUpdates(isManualCheck = true) },
                                enabled = updateResult !is UpdateCheckResult.Checking && downloadState !is DownloadState.Downloading
                            ) {
                                if (updateResult is UpdateCheckResult.Checking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Check for Updates")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Update Status Box
                        when (val res = updateResult) {
                            is UpdateCheckResult.UpdateAvailable -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🎉 New Version ${res.release.tagName} Available!",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Text(
                                            text = res.release.name.ifBlank { "Travel Plus Release" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Button(
                                            onClick = { viewModel.startDownloadAndInstall(res.release) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Download & Install Update")
                                        }
                                    }
                                }
                            }
                            is UpdateCheckResult.UpToDate -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("You are on the latest version of Travel Plus!", fontSize = 13.sp)
                                }
                            }
                            is UpdateCheckResult.Error -> {
                                Text(
                                    text = "Update check error: ${res.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                            else -> {}
                        }

                        // Auto check switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Check on App Launch", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Notify automatically when a new GitHub release is published", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoCheckUpdates,
                                onCheckedChange = { viewModel.setAutoCheckEnabled(it) }
                            )
                        }

                        // Target GitHub Repository
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRepoConfigDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Target GitHub Repository", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$repoOwner/$repoName", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                            }
                            IconButton(onClick = { showRepoConfigDialog = true }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Repo", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // ==========================================
            // Section 3: About & Publish Info
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Travel Plus • All-In-One Travel Super-App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Features in-app flight exploration (Google Flights, Skyscanner), live hotel and stay search (Booking.com, Expedia, Airbnb), digital scannable boarding passes with high-contrast QR & Aztec barcodes, offline currency converter, packing assistant, and conversational multi-trip AI concierge.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Edit Target Repository Dialog
    if (showRepoConfigDialog) {
        AlertDialog(
            onDismissRequest = { showRepoConfigDialog = false },
            title = { Text("Configure GitHub Repository") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specify the GitHub repository where releases and APKs are published:")
                    OutlinedTextField(
                        value = editRepoOwner,
                        onValueChange = { editRepoOwner = it },
                        label = { Text("Owner (e.g. your-username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRepoName,
                        onValueChange = { editRepoName = it },
                        label = { Text("Repository (e.g. travel-plus)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setRepoConfiguration(editRepoOwner, editRepoName)
                    showRepoConfigDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepoConfigDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
