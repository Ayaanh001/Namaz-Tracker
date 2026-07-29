package com.hussain.namaztracker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    val colorScheme = MaterialTheme.colorScheme

    // Check if aligned with Qibla (within 2 degrees)
    val isAligned = remember(uiState.bearing, uiState.qiblaDirection) {
        val diff = abs((uiState.bearing + uiState.qiblaDirection + 360) % 360)
        diff < 2f || diff > 358f
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose {
            viewModel.stopListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        colorScheme.background
                    ),
                    center = Offset.Unspecified,
                    radius = 1000f
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Qibla Direction",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.weight(1.2f))

            // Group Compass and Info together
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Compass Container with Glow
                Box(
                    modifier = Modifier.size(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Glow when aligned
                    val glowAlpha by animateFloatAsState(
                        targetValue = if (isAligned) 0.15f else 0f,
                        animationSpec = tween(500),
                        label = "glow"
                    )
                    
                    if (glowAlpha > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(colorScheme.primary, Color.Transparent),
                                    center = center,
                                    radius = size.minDimension / 1.5f
                                ),
                                alpha = glowAlpha
                            )
                        }
                    }

                    // Static Outer Ring (True North Indicator)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = colorScheme.onSurface.copy(alpha = 0.05f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        
                        // Top Marker (Device Heading)
                        rotate(-90f) {
                            drawPath(
                                path = Path().apply {
                                    moveTo(size.width, size.height / 2)
                                    lineTo(size.width - 12.dp.toPx(), size.height / 2 - 6.dp.toPx())
                                    lineTo(size.width - 12.dp.toPx(), size.height / 2 + 6.dp.toPx())
                                    close()
                                },
                                color = colorScheme.primary
                            )
                        }
                    }

                    // Rotating Compass Face
                    val bearingRotation by animateFloatAsState(
                        targetValue = -uiState.bearing,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "bearing"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(bearingRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        CompassFace(textMeasurer, colorScheme.onSurface)
                        QiblaNeedle(qiblaDirection = uiState.qiblaDirection, isAligned = isAligned)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Alignment Status & Info
                if (uiState.hasLocation) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusColor by animateColorAsState(
                            targetValue = if (isAligned) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.4f),
                            label = "statusColor"
                        )
                        
                        Text(
                            text = if (isAligned) "Perfectly Aligned" else "Rotate Device",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${uiState.qiblaDirection.toInt()}°",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = colorScheme.onBackground
                                )
                            )
                            Text(
                                text = "to Kaaba",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Surface(
                            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f km from Mecca", uiState.distance),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom spacing for navigation bar safety
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CompassFace(textMeasurer: androidx.compose.ui.text.TextMeasurer, color: Color) {
    val labelStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        color = color.copy(alpha = 0.8f)
    )
    val degreeStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 8.sp,
        color = color.copy(alpha = 0.3f)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        val innerRadius = radius - 20.dp.toPx()
        
        // Inner thin circle
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = innerRadius,
            style = Stroke(width = 0.5.dp.toPx())
        )

        for (i in 0 until 360 step 2) {
            val angle = Math.toRadians(i.toDouble())
            val isCardinal = i % 90 == 0
            val isMajor = i % 30 == 0
            val isMinor = i % 10 == 0
            
            val startRadius = when {
                isCardinal -> radius - 16.dp.toPx()
                isMajor -> radius - 12.dp.toPx()
                isMinor -> radius - 8.dp.toPx()
                else -> radius - 4.dp.toPx()
            }
            
            val start = Offset(
                (center.x + startRadius * sin(angle)).toFloat(),
                (center.y - startRadius * cos(angle)).toFloat()
            )
            val end = Offset(
                (center.x + radius * sin(angle)).toFloat(),
                (center.y - radius * cos(angle)).toFloat()
            )
            
            drawLine(
                color = if (isCardinal) color else color.copy(alpha = 0.2f),
                start = start,
                end = end,
                strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            if (isCardinal) {
                val label = when (i) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    270 -> "W"
                    else -> ""
                }
                val textLayoutResult = textMeasurer.measure(label, labelStyle)
                val labelRadius = innerRadius - 12.dp.toPx()
                val textOffset = Offset(
                    (center.x + labelRadius * sin(angle) - textLayoutResult.size.width / 2).toFloat(),
                    (center.y - labelRadius * cos(angle) - textLayoutResult.size.height / 2).toFloat()
                )
                drawText(textLayoutResult, topLeft = textOffset)
            } else if (isMajor) {
                val textLayoutResult = textMeasurer.measure(i.toString(), degreeStyle)
                val labelRadius = innerRadius - 10.dp.toPx()
                val textOffset = Offset(
                    (center.x + labelRadius * sin(angle) - textLayoutResult.size.width / 2).toFloat(),
                    (center.y - labelRadius * cos(angle) - textLayoutResult.size.height / 2).toFloat()
                )
                drawText(textLayoutResult, topLeft = textOffset)
            }
        }
    }
}

@Composable
fun QiblaNeedle(qiblaDirection: Float, isAligned: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    val needleColor by animateColorAsState(
        targetValue = if (isAligned) primaryColor else secondaryColor,
        animationSpec = tween(300),
        label = "needleColor"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        rotate(qiblaDirection) {
            val radius = size.minDimension / 2
            val needleLength = radius * 0.75f
            val needleWidth = 14.dp.toPx()
            
            // Needle Path (Diamond/Tapered shape)
            val path = Path().apply {
                moveTo(center.x, center.y - needleLength) // Tip
                lineTo(center.x + needleWidth / 2, center.y) // Right middle
                lineTo(center.x, center.y + needleWidth) // Bottom tip
                lineTo(center.x - needleWidth / 2, center.y) // Left middle
                close()
            }
            
            // Draw needle with a subtle gradient
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(needleColor, needleColor.copy(alpha = 0.7f)),
                    startY = center.y - needleLength,
                    endY = center.y + needleWidth
                )
            )
            
            // Decorative center line
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(center.x, center.y - needleLength + 10.dp.toPx()),
                end = Offset(center.x, center.y + needleWidth - 4.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )

            // Center Hub
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
