package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.ReferenceDataStore
import com.example.data.models.TripExpense
import com.example.ui.components.TravelTopHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.DeepLinkHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyScreen(
    viewModel: TravelViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsStateWithLifecycle()
    val expenses by viewModel.currentExpenses.collectAsStateWithLifecycle()

    var sourceAmt by remember { mutableStateOf("100") }
    var fromCurrencyCode by remember { mutableStateOf("USD") }
    var toCurrencyCode by remember { mutableStateOf("EUR") }

    // Tip calculator state
    var billAmount by remember { mutableStateOf("85.00") }
    var tipPercent by remember { mutableStateOf(18) }

    // Add Expense dialog state
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("Food & Dining") }

    val totalSpent = expenses.sumOf { it.amount }
    val tripBudget = currentTrip?.totalEstimatedBudget ?: 1500.0
    val budgetProgress = if (tripBudget > 0) (totalSpent / tripBudget).toFloat().coerceIn(0f, 1f) else 0f

    val fromRate = ReferenceDataStore.currencies.find { it.code == fromCurrencyCode }?.rateAgainstUSD ?: 1.0
    val toRate = ReferenceDataStore.currencies.find { it.code == toCurrencyCode }?.rateAgainstUSD ?: 1.0
    val parsedSource = sourceAmt.toDoubleOrNull() ?: 100.0
    val convertedResult = (parsedSource / fromRate) * toRate

    Scaffold(
        topBar = {
            TravelTopHeader(
                title = "Money & Cards",
                subtitle = "Currency Exchange, Budget & Travel Rewards",
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Expense") }
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
            // 1. Live Currency Converter Card
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
                            Text(
                                text = "💱 Real-Time Currency Converter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Offline Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = sourceAmt,
                                onValueChange = { sourceAmt = it },
                                label = { Text("Amount ($fromCurrencyCode)") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    val tmp = fromCurrencyCode
                                    fromCurrencyCode = toCurrencyCode
                                    toCurrencyCode = tmp
                                }
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = "Swap",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Convert To", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = toCurrencyCode,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sourceAmt.ifBlank { "0" }} $fromCurrencyCode =",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${"%.2f".format(convertedResult)} $toCurrencyCode",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Quick Currency Pills
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("EUR", "JPY", "GBP", "CAD", "AUD", "CHF", "THB", "MXN")) { code ->
                                FilterChip(
                                    selected = toCurrencyCode == code,
                                    onClick = { toCurrencyCode = code },
                                    label = { Text(code, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tipping Guide & Tip Calculator
            item {
                val currentDestCurrency = ReferenceDataStore.currencies.find { it.code == toCurrencyCode } ?: ReferenceDataStore.currencies.first()
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
                            text = "🍽️ Tipping Culture Guide (${currentDestCurrency.code})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDestCurrency.tipCulture,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val billVal = billAmount.toDoubleOrNull() ?: 85.0
                        val tipAmount = billVal * (tipPercent / 100.0)
                        val grandTotal = billVal + tipAmount

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = billAmount,
                                onValueChange = { billAmount = it },
                                label = { Text("Bill Amount") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tip: $tipPercent%", fontSize = 11.sp)
                                Slider(
                                    value = tipPercent.toFloat(),
                                    onValueChange = { tipPercent = it.toInt() },
                                    valueRange = 0f..25f,
                                    steps = 4
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tip: $${"%.2f".format(tipAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Grand Total: $${"%.2f".format(grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }
            }

            // 3. Trip Budget & Expense Tracker
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
                                text = "📊 Trip Budget & Spending Tracker",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Spent So Far", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(totalSpent)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Trip Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(tripBudget)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (budgetProgress > 0.9f) CoralRed else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Logged Expenses (${expenses.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (expenses.isEmpty()) {
                            Text(
                                text = "No expenses logged yet. Tap '+ Log Expense' below.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            expenses.forEach { exp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(exp.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("$${"%.2f".format(exp.amount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(
                                        onClick = { viewModel.deleteExpense(exp) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Travel Credit Card Matcher & Deep Links
            item {
                Text(
                    text = "💳 Recommended Travel Credit Cards",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cards with 0% Foreign Transaction Fees & Lounge Access",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(ReferenceDataStore.creditCards) { card ->
                Card(
                    shape = RoundedCornerShape(16.dp),
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
                            Text(card.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(card.bestForVibe, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Annual Fee: ${card.annualFee} • ${card.foreignTxFee}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = card.rewardHighlights,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🎁 Welcome Bonus: ${card.welcomeBonus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { DeepLinkHelper.openWebUrl(context, card.applyDeepLink) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Learn More & Apply on ${card.issuer}", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Log Trip Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Expense Title (e.g., Dinner at Bistro)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Food & Dining", "Hotels", "Flights", "Activities", "Transport", "Shopping")) { cat ->
                            FilterChip(
                                selected = expenseCategory == cat,
                                onClick = { expenseCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (expenseTitle.isNotBlank() && amt > 0) {
                            viewModel.addExpense(expenseTitle, amt, expenseCategory)
                            expenseTitle = ""
                            expenseAmount = ""
                            showAddExpenseDialog = false
                        }
                    }
                ) {
                    Text("Log Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
