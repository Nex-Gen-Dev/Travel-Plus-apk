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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.PackingItem
import com.example.ui.components.TravelTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val packingItems by viewModel.currentPackingItems.collectAsStateWithLifecycle()
    val preferredRetailer by viewModel.preferredRetailer.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Clothing") }

    val packedCount = packingItems.count { it.isPacked }
    val totalCount = packingItems.size
    val progressFraction = if (totalCount > 0) packedCount.toFloat() / totalCount else 0f

    val categories = listOf("All", "Essentials", "Clothing", "Electronics", "Toiletries", "Activity Gear", "Documents")
    val retailers = listOf("Amazon", "Target", "Best Buy", "Walmart", "Local Stores")

    Scaffold(
        topBar = {
            TravelTopHeader(
                title = "Shopping & Smart Packing",
                subtitle = "AI Gear Concierge for ${currentTrip?.destination ?: "Trip"}",
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Retailer Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛍️ Preferred Shopping Retailer",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = preferredRetailer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AI directs your 1-tap item searches to your favorite store first, with local store fallback for destination shopping.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(retailers) { ret ->
                                FilterChip(
                                    selected = preferredRetailer.equals(ret, ignoreCase = true),
                                    onClick = { viewModel.preferredRetailer.value = ret },
                                    label = { Text(ret, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Packing Progress Bar Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Packing Readiness",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$packedCount of $totalCount packed (${(progressFraction * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (progressFraction >= 1f) EmeraldGreen else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (progressFraction >= 1f) EmeraldGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Category Filter Row
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = if (cat == "All") selectedCategoryFilter == null else selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (cat == "All") null else cat
                            },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Filtered Packing Items
            val filteredList = if (selectedCategoryFilter != null) {
                packingItems.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
            } else {
                packingItems
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No items in this category",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+' to add your gear or customize your list.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    PackingItemRow(
                        item = item,
                        currentRetailer = preferredRetailer,
                        onToggle = { viewModel.togglePackingItem(item) },
                        onBuyClick = {
                            DeepLinkHelper.openShoppingSearch(
                                context,
                                item.name,
                                item.recommendedStore.ifBlank { preferredRetailer }
                            )
                        },
                        onDelete = { viewModel.deletePackingItem(item) }
                    )
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Packing Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item Name") },
                        placeholder = { Text("e.g., Swim Goggles, Portable Fan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Clothing", "Toiletries", "Electronics", "Essentials", "Activity Gear", "Documents")) { cat ->
                            FilterChip(
                                selected = newItemCategory == cat,
                                onClick = { newItemCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            viewModel.addCustomPackingItem(newItemName, newItemCategory)
                            newItemName = ""
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PackingItemRow(
    item: PackingItem,
    currentRetailer: String,
    onToggle: () -> Unit,
    onBuyClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPacked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isPacked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldGreen
                )
            )

            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (item.isPacked) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (item.isPacked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(" • ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = item.estimatedPrice,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Buy on Store Button
            Button(
                onClick = onBuyClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buy on ${item.recommendedStore.ifBlank { currentRetailer }}", fontSize = 10.sp)
            }
        }
    }
}
