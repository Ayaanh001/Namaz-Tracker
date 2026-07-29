package com.hussain.namaztracker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qibla Direction",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Compass UI
        Box(
            modifier = Modifier
                .size(285.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Static Outer Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = onSurface.copy(alpha = 0.1f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Static Heading Indicator (Top)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier.size(width = 2.dp, height = 8.dp),
                    color = onSurface.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {}
            }

            // Rotating Compass
            val bearingRotation by animateFloatAsState(targetValue = -uiState.bearing, label = "bearing")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(bearingRotation),
                contentAlignment = Alignment.Center
            ) {
                CompassFace(textMeasurer, onSurface)
                QiblaNeedle(qiblaDirection = uiState.qiblaDirection)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.hasLocation) {
            Text(
                text = uiState.locationName,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${uiState.qiblaDirection.toInt()}° to Kaaba",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.getDefault(), "%.0f km from Mecca", uiState.distance),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                )
            }
        } else {
            Text(
                text = uiState.locationName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CompassFace(textMeasurer: androidx.compose.ui.text.TextMeasurer, color: Color) {
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = color
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        
        // Draw 360 degree markers
        for (i in 0 until 360 step 5) {
            val isMajor = i % 30 == 0
            val isCardinal = i % 90 == 0
            
            val markerLength = if (isCardinal) 15.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
            val angle = Math.toRadians(i.toDouble())
            
            val start = Offset(
                (center.x + (radius - markerLength) * sin(angle)).toFloat(),
                (center.y - (radius - markerLength) * cos(angle)).toFloat()
            )
            val end = Offset(
                (center.x + radius * sin(angle)).toFloat(),
                (center.y - radius * cos(angle)).toFloat()
            )
            
            drawLine(
                color = if (isCardinal) color else color.copy(alpha = 0.5f),
                start = start,
                end = end,
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw N, E, S, W labels
            if (isCardinal) {
                val label = when (i) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    270 -> "W"
                    else -> ""
                }
                val textLayoutResult = textMeasurer.measure(label, labelStyle)
                val textOffset = Offset(
                    (center.x + (radius - 30.dp.toPx()) * sin(angle) - textLayoutResult.size.width / 2).toFloat(),
                    (center.y - (radius - 30.dp.toPx()) * cos(angle) - textLayoutResult.size.height / 2).toFloat()
                )
                drawText(textLayoutResult, topLeft = textOffset)
            }
        }
    }
}

@Composable
fun QiblaNeedle(qiblaDirection: Float) {
    val greenColor = Color(0xFF4CAF50)
    Canvas(modifier = Modifier.fillMaxSize()) {
        rotate(qiblaDirection) {
            val radius = size.minDimension / 2
            val hubRadius = 12.dp.toPx()
            val gap = 6.dp.toPx()
            val arrowLength = radius * 0.45f
            val arrowWidthAngle = 25f // Degrees
            
            // 1. Center Circle (Hub)
            drawCircle(
                color = greenColor,
                radius = hubRadius,
                center = center
            )
            
            // 2. Qibla Arrow (Cut from circle effect)
            val startDist = hubRadius + gap
            val endDist = startDist + arrowLength
            
            val path = Path().apply {
                // Tip of the arrow
                moveTo(center.x, center.y - endDist)
                
                // Right base point (on an arc)
                val angleRad = Math.toRadians(arrowWidthAngle.toDouble() / 2)
                val bx1 = (center.x + startDist * sin(angleRad)).toFloat()
                val by1 = (center.y - startDist * cos(angleRad)).toFloat()
                
                // Left base point (on an arc)
                val bx2 = (center.x - startDist * sin(angleRad)).toFloat()
                val by2 = (center.y - startDist * cos(angleRad)).toFloat()
                
                lineTo(bx1, by1)
                
                // Draw arc at the base to match the hub's curvature
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        center.x - startDist,
                        center.y - startDist,
                        center.x + startDist,
                        center.y + startDist
                    ),
                    startAngleDegrees = 270f + arrowWidthAngle / 2,
                    sweepAngleDegrees = -arrowWidthAngle,
                    forceMoveTo = false
                )
                
                lineTo(center.x, center.y - endDist)
                close()
            }
            
            drawPath(
                path = path,
                color = greenColor
            )
            
            // Center pin detail
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}
