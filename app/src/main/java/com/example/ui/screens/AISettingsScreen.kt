package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.models.AIModelConfig
import com.example.ui.components.FailoverStatusBar
import com.example.ui.components.TravelTopHeader
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
    val configs by viewModel.aiConfigs.collectAsStateWithLifecycle()
    val aiStatus by viewModel.aiEngineStatus.collectAsStateWithLifecycle()

    var editingConfig by remember { mutableStateOf<AIModelConfig?>(null) }
    var keyInputValue by remember { mutableStateOf("") }
    var globalKeyInput by remember { mutableStateOf("") }

    val sortedConfigs = configs.sortedBy { it.priority }

    Scaffold(
        topBar = {
            Column {
                TravelTopHeader(
                    title = "AI Engine & Failover",
                    subtitle = "Multi-Model OpenRouter Pipeline",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Architecture Info Card
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
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Failover Architecture",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Zero Rate-Limit Lockouts",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Requests start at Priority #1. If rate-limited (HTTP 429), quota exceeded, or timed out, Travel Plus seamlessly switches to #2, then #3, maintaining a continuous conversation.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                DeepLinkHelper.openWebUrl(context, "https://openrouter.ai/keys")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Get Free OpenRouter API Key", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Quick Global Key Setup Card
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
                            text = "🔑 Set Global OpenRouter Key",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Apply one key across all configured models with one click",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = globalKeyInput,
                            onValueChange = { globalKeyInput = it },
                            placeholder = { Text("sk-or-v1-...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (globalKeyInput.isNotBlank()) {
                                    sortedConfigs.forEach { cfg ->
                                        viewModel.updateAIConfigKey(cfg.modelId, globalKeyInput)
                                    }
                                    globalKeyInput = ""
                                }
                            },
                            enabled = globalKeyInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply to All Models")
                        }
                    }
                }
            }

            // Test Simulation Button
            item {
                Button(
                    onClick = {
                        viewModel.sendMessage("Generate a quick 3-day travel itinerary for Paris with failover test")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Live Failover Pipeline")
                }
            }

            // Prioritized Models List
            item {
                Text(
                    text = "Configured Model Priority Chain",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            itemsIndexed(sortedConfigs) { index, config ->
                ModelConfigCard(
                    config = config,
                    index = index,
                    totalCount = sortedConfigs.size,
                    onMoveUp = { viewModel.moveAIConfigPriority(config.modelId, moveUp = true) },
                    onMoveDown = { viewModel.moveAIConfigPriority(config.modelId, moveUp = false) },
                    onToggleEnabled = { viewModel.toggleAIConfigEnabled(config.modelId) },
                    onEditKey = {
                        editingConfig = config
                        keyInputValue = config.apiKey
                    }
                )
            }
        }
    }

    // Edit Model Key Dialog
    if (editingConfig != null) {
        AlertDialog(
            onDismissRequest = { editingConfig = null },
            title = { Text("Edit API Key: ${editingConfig!!.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Model ID: ${editingConfig!!.modelId}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = keyInputValue,
                        onValueChange = { keyInputValue = it },
                        label = { Text("OpenRouter API Key") },
                        placeholder = { Text("sk-or-v1-...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Keys are securely stored on your device Room database and sent directly to OpenRouter.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAIConfigKey(editingConfig!!.modelId, keyInputValue)
                        editingConfig = null
                    }
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingConfig = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ModelConfigCard(
    config: AIModelConfig,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleEnabled: () -> Unit,
    onEditKey: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (config.priority == 1) AmberGold else MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${config.priority}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.priority == 1) Navy900 else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = config.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = config.modelId,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { onToggleEnabled() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (config.apiKey.isNotBlank()) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (config.apiKey.isNotBlank()) "Key Configured (•••${config.apiKey.takeLast(4)})" else "Default / Global Key Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (config.apiKey.isNotBlank()) Color(0xFF14532D) else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                    }
                    TextButton(onClick = onEditKey) {
                        Text("Edit Key", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
