package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Travel Plus brand logo component.
 * Features a glowing gradient ring, pulsing compass glow, orbiting flight particle,
 * and a stylized supersonic aircraft silhouette.
 */
@Composable
fun TravelPlusLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TravelPlusLogoAnimation")

    // Rotation of outer orbit ring
    val ringRotation by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "RingRotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Breathing pulse for center core
    val coreScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CoreScale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // Glowing aura pulse
    val auraAlpha by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AuraAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0.4f) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val emeraldColor = EmeraldGreen

    Box(
        modifier = modifier
            .size(size)
            .padding(size * 0.05f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius = canvasWidth / 2f

            // 1. Soft radial background aura glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = auraAlpha * 0.4f),
                        emeraldColor.copy(alpha = auraAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer Dashed Orbital Ring
            val orbitRadius = radius * 0.82f
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.8f),
                        emeraldColor,
                        OceanBlue,
                        primaryColor.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = orbitRadius,
                center = center,
                style = Stroke(
                    width = canvasWidth * 0.045f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(canvasWidth * 0.12f, canvasWidth * 0.06f), ringRotation * 2f)
                )
            )

            // 3. Orbiting Travel Star/Satellite
            val angleRad = (ringRotation * (PI / 180.0)).toFloat()
            val orbitX = center.x + orbitRadius * cos(angleRad)
            val orbitY = center.y + orbitRadius * sin(angleRad)

            // Outer glow for orbiting point
            drawCircle(
                color = emeraldColor.copy(alpha = 0.6f),
                radius = canvasWidth * 0.065f,
                center = Offset(orbitX, orbitY)
            )
            drawCircle(
                color = Color.White,
                radius = canvasWidth * 0.035f,
                center = Offset(orbitX, orbitY)
            )

            // 4. Inner Compass / Globe Nodes
            val innerRingRadius = radius * 0.52f
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = canvasWidth * 0.02f)
            )

            // 5. Stylized Central Aircraft Silhouette
            val planeScale = (canvasWidth * 0.38f) * coreScale
            drawStylizedAirplane(
                center = center,
                size = planeScale,
                primaryColor = primaryColor,
                accentColor = emeraldColor
            )
        }
    }
}

/**
 * Draws an elegant supersonic aircraft shape with gradient wings & cockpit accent.
 */
private fun DrawScope.drawStylizedAirplane(
    center: Offset,
    size: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val path = Path().apply {
        // Nose point
        moveTo(center.x, center.y - size * 0.58f)
        // Right fuselage & wing tip
        lineTo(center.x + size * 0.12f, center.y - size * 0.15f)
        lineTo(center.x + size * 0.55f, center.y + size * 0.18f)
        lineTo(center.x + size * 0.46f, center.y + size * 0.28f)
        lineTo(center.x + size * 0.14f, center.y + size * 0.18f)
        // Tail right
        lineTo(center.x + size * 0.22f, center.y + size * 0.48f)
        lineTo(center.x, center.y + size * 0.38f)
        // Tail left
        lineTo(center.x - size * 0.22f, center.y + size * 0.48f)
        lineTo(center.x - size * 0.14f, center.y + size * 0.18f)
        // Left wing tip & fuselage
        lineTo(center.x - size * 0.46f, center.y + size * 0.28f)
        lineTo(center.x - size * 0.55f, center.y + size * 0.18f)
        lineTo(center.x - size * 0.12f, center.y - size * 0.15f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, accentColor, primaryColor),
            startY = center.y - size * 0.6f,
            endY = center.y + size * 0.5f
        )
    )

    // Center jet stream flare
    drawCircle(
        color = accentColor,
        radius = size * 0.08f,
        center = Offset(center.x, center.y + size * 0.38f)
    )
}

/**
 * Universal Animated Travel Plus Loading Indicator used across all screens.
 */
@Composable
fun TravelPlusLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    size: Dp = 56.dp
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TravelPlusLogo(
            size = size,
            animated = true
        )
        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Full-Screen Animated Splash Screen shown during app cold start / initial sync.
 */
@Composable
fun TravelPlusSplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "SplashAlpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "SplashScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        // Allow user to appreciate the brand animation briefly
        kotlinx.coroutines.delay(1200)
        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkNavy
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle ambient background gradient
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DeepTeal.copy(alpha = 0.35f),
                            OceanBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension * 0.55f
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .padding(24.dp)
            ) {
                // Branded Animated Logo
                TravelPlusLogo(
                    size = 120.dp,
                    animated = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Title
                Text(
                    text = "TRAVEL PLUS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "YOUR INTELLIGENT JOURNEY COMPANION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
