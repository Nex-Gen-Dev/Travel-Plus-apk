package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AIEngineStatus
import com.example.ui.theme.*

@Composable
fun FailoverStatusBar(
    status: AIEngineStatus,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status !is AIEngineStatus.Idle,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        when (status) {
            is AIEngineStatus.Generating -> {
                Surface(
                    color = IceBlue,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Querying Priority #${status.attemptNumber}: ${status.modelName}...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Navy900
                        )
                    }
                }
            }
            is AIEngineStatus.SwitchingFailover -> {
                Surface(
                    color = LightAmber,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Failover Switch",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Auto-Failover: ${status.failedModel} rate-limited",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = "Seamlessly switching to -> ${status.nextModel}",
                                fontSize = 11.sp,
                                color = Navy800
                            )
                        }
                    }
                }
            }
            is AIEngineStatus.Completed -> {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generated via ${status.usedModel} in ${status.responseTimeMs}ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }
            is AIEngineStatus.Error -> {
                Surface(
                    color = Color(0xFFFFE4E6),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = CoralRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.message,
                            fontSize = 12.sp,
                            color = Color(0xFF881337)
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun TravelTopHeader(
    title: String,
    subtitle: String? = null,
    onSettingsClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
                if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.Outlined.Tune,
                            contentDescription = "AI Engine Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (category.uppercase()) {
        "FLIGHT" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.Flight)
        "HOTEL" -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), Icons.Default.Hotel)
        "RESTAURANT" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Restaurant)
        "SCENIC" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Landscape)
        "TRANSIT" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.DirectionsCar)
        "RELAXATION" -> Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), Icons.Default.BeachAccess)
        "SHOPPING" -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), Icons.Default.ShoppingBag)
        else -> Triple(Color(0xFFE2E8F0), Color(0xFF334155), Icons.Default.Attractions)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
