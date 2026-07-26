package com.hussain.namaztracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hussain.namaztracker.models.PrayerEntry
import com.hussain.namaztracker.models.PrayerStatus
import com.hussain.namaztracker.ui.components.PrayerCompletionBottomSheet
import com.hussain.namaztracker.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

val ParallelogramShape = GenericShape { size, _ ->
    moveTo(size.width * 0.25f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahScreen(viewModel: SalahViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPrayerForCompletion by remember { mutableStateOf<PrayerEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    
    val dateListState = rememberLazyListState(initialFirstVisibleItemIndex = 500)
    val coroutineScope = rememberCoroutineScope()
    val showGoToToday by remember {
        derivedStateOf {
            val visibleItems = dateListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf false
            val lastVisibleIndex = visibleItems.last().index
            // Only show if we've scrolled at least 3 items past today into the past
            lastVisibleIndex < 490
        }
    }

    val prayerData by viewModel.prayerData.collectAsState()
    val prayers = prayerData[selectedDate] ?: remember(selectedDate) {
        viewModel.getPrayersForDate(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Salah Tracker",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalDateStrip(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                listState = dateListState
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = showGoToToday,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            dateListState.animateScrollToItem(500)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    tonalElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to Today",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PRAYERS",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(prayers) { _, prayer ->
                PrayerCard(
                    prayer = prayer,
                    onClick = {
                        selectedPrayerForCompletion = prayer
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    if (showBottomSheet && selectedPrayerForCompletion != null) {
        PrayerCompletionBottomSheet(
            prayer = selectedPrayerForCompletion!!,
            date = selectedDate,
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
            onStatusSelected = { newStatus, applyToAll ->
                if (applyToAll) {
                    viewModel.updateAllPrayersStatus(
                        date = selectedDate,
                        newStatus = newStatus
                    )
                } else {
                    viewModel.updatePrayerStatus(
                        date = selectedDate,
                        prayerName = selectedPrayerForCompletion!!.name,
                        newStatus = newStatus
                    )
                }
                showBottomSheet = false
            }
        )
    }
}

@Composable
fun HorizontalDateStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    listState: LazyListState
) {
    val dates = remember {
        (-500..2).map { LocalDate.now().plusDays(it.toLong()) }
    }
    
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(dates) { _, date ->
            val isSelected = date == selectedDate
            DatePill(
                date = date,
                isSelected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DatePill(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isToday = date == LocalDate.now()
    
    val topSectionColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val bottomSectionColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val dayTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val isFirstOfMonth = date.dayOfMonth == 1
    
    val dayLabel = if (isFirstOfMonth) {
        date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    } else {
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    val dayLabelColor = when {
        isSelected -> dayTextColor
        isFirstOfMonth -> MaterialTheme.colorScheme.primary
        else -> dayTextColor
    }

    Column(
        modifier = Modifier
            .width(44.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(topSectionColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section: Day
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = dayLabelColor,
                    fontWeight = if (isFirstOfMonth) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp
                )
            )
        }

        // Bottom Section: Date Number (Edge-to-Edge with top rounding)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.6f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(bottomSectionColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            if (isToday) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerCard(
    prayer: PrayerEntry,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "prayerCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = prayer.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayer.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val statusColor = if (prayer.status == PrayerStatus.UPCOMING) 
                MaterialTheme.colorScheme.surfaceVariant 
                else prayer.status.color

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .clip(ParallelogramShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(statusColor, statusColor.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                prayer.status.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
