package com.hussain.namaztracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hussain.namaztracker.models.PrayerStatus
import com.hussain.namaztracker.models.StatsRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: SalahViewModel = viewModel()) {
    val allHistory by viewModel.allHistory.collectAsState()
    val insights by viewModel.statsInsights.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    
    // Map records for easy lookup: Date -> PrayerName -> Status
    val historyMap = remember(allHistory) {
        allHistory.groupBy { LocalDate.parse(it.date) }
            .mapValues { entry ->
                entry.value.associate { it.prayerName to PrayerStatus.valueOf(it.status) }
            }
    }

    val today = LocalDate.now()
    val startDate = today.minusDays(200) // Show last 6.5 months
    val endDate = today // Only up to today
    val dateList = remember {
        val list = mutableListOf<LocalDate>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            list.add(curr)
            curr = curr.plusDays(1)
        }
        list
    }

    var selectedDetail by remember { mutableStateOf<Triple<LocalDate, String, Offset>?>(null) }
    val mainScrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stats",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(mainScrollState)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // Scrollable Grid
                val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = dateList.size)
                
                LazyRow(
                    state = scrollState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateList) { date ->
                        val isToday = date == today

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                /*.background(
                                    if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    else Color.Transparent
                                )*/
                                .padding(horizontal = 2.dp)
                        ) {
                            // Date Header
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .widthIn(min = 28.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val label = if (date.dayOfMonth == 1 || date == dateList.first()) {
                                    date.format(DateTimeFormatter.ofPattern("MMM"))
                                } else {
                                    date.dayOfMonth.toString()
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isToday || date.dayOfMonth == 1 || date == dateList.first()) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isToday || date.dayOfMonth == 1 || date == dateList.first()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                            prayers.forEach { prayerName ->
                                val status = historyMap[date]?.get(prayerName) ?: PrayerStatus.UPCOMING
                                PrayerStatusBox(
                                    status = status,
                                    isFuture = date.isAfter(today),
                                    onClick = { offset ->
                                        selectedDetail = Triple(date, prayerName, offset)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Fixed Prayer Labels Column (Now on the right)
                Column(
                    modifier = Modifier.padding(top = 30.dp), // Date(24) + Spacing(6)
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val prayerIcons = listOf(
                        Icons.Outlined.Brightness2,
                        Icons.Outlined.WbSunny,
                        Icons.Outlined.Brightness7,
                        Icons.Outlined.Brightness5,
                        Icons.Outlined.Brightness2
                    )
                    prayerIcons.forEach { icon ->
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Streaks Section
        PremiumStreakSection(
            current = insights.currentStreak,
            best = insights.bestStreak
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Range Switcher
        TimeRangeSwitcher(
            selectedRange = selectedRange,
            onRangeSelected = { viewModel.setStatsRange(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Breakdown Card
        PremiumBreakdownCard(
            breakdown = insights.breakdown,
            range = selectedRange
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (selectedDetail != null) {
        val (date, prayer, offset) = selectedDetail!!
        val status = historyMap[date]?.get(prayer) ?: PrayerStatus.UPCOMING
        
        StatsDetailPopover(
            date = date,
            prayerName = prayer,
            status = status,
            anchorOffset = offset,
            onDismiss = { selectedDetail = null }
        )
    }
}

class SpeechBubbleShape(val arrowOffset: Float = 0.5f) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            val cornerRadius = with(density) { 12.dp.toPx() }
            val arrowWidth = with(density) { 12.dp.toPx() }
            val arrowHeight = with(density) { 8.dp.toPx() }
            
            // Main Bubble Rect
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - arrowHeight,
                    cornerRadius = CornerRadius(cornerRadius)
                )
            )
            
            // Arrow pointing down
            val arrowCenterX = size.width * arrowOffset
            moveTo(arrowCenterX - arrowWidth / 2, size.height - arrowHeight)
            lineTo(arrowCenterX, size.height)
            lineTo(arrowCenterX + arrowWidth / 2, size.height - arrowHeight)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun StatsDetailPopover(
    date: LocalDate,
    prayerName: String,
    status: PrayerStatus,
    anchorOffset: Offset,
    onDismiss: () -> Unit
) {
    val prayerIcon = when (prayerName) {
        "Fajr" -> Icons.Outlined.Brightness2
        "Dhuhr" -> Icons.Outlined.WbSunny
        "Asr" -> Icons.Outlined.Brightness7
        "Maghrib" -> Icons.Outlined.Brightness5
        "Isha" -> Icons.Outlined.Brightness2
        else -> Icons.Outlined.WbSunny
    }

    val statusLabel = when (status) {
        PrayerStatus.GROUP -> "In Jamaah"
        PrayerStatus.ALONE -> "On Time"
        PrayerStatus.LATE -> "Late"
        PrayerStatus.MISSED -> "Not Prayed"
        PrayerStatus.UPCOMING -> "Not Recorded"
    }

    var arrowOffset by remember { mutableFloatStateOf(0.5f) }

    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val preferredX = (anchorOffset.x - popupContentSize.width / 2).toInt()
                val x = preferredX.coerceIn(16, windowSize.width - popupContentSize.width - 16)
                val y = (anchorOffset.y - popupContentSize.height - 12).toInt()
                
                // Calculate where the arrow should be relative to the popup's width
                val relativeAnchorX = anchorOffset.x - x
                arrowOffset = (relativeAnchorX / popupContentSize.width).coerceIn(0.1f, 0.9f)
                
                return IntOffset(x, y)
            }
        },
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .wrapContentHeight(),
            shape = SpeechBubbleShape(arrowOffset),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = prayerIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = prayerName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badge
                Surface(
                    color = if (status == PrayerStatus.UPCOMING) 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else status.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        status.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = status.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (status == PrayerStatus.UPCOMING) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                    else status.color
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when(status) {
                        PrayerStatus.GROUP -> "Performed in Jamaah."
                        PrayerStatus.ALONE -> "Performed on time."
                        PrayerStatus.LATE -> "Performed late."
                        PrayerStatus.MISSED -> "Prayer missed."
                        PrayerStatus.UPCOMING -> "No record found."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PremiumStreakSection(current: Int, best: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$current",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "days",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Best Streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$best days",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (current < best) {
                    Text(
                        text = "${best - current} days to record",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeRangeSwitcher(
    selectedRange: StatsRange,
    onRangeSelected: (StatsRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatsRange.entries.forEach { range ->
                val isSelected = selectedRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onRangeSelected(range) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumBreakdownCard(breakdown: Map<PrayerStatus, Int>, range: StatsRange) {
    val total = breakdown.values.sum().toFloat().coerceAtLeast(1f)
    
    val statuses = listOf(
        PrayerStatus.GROUP to "In Jamaah",
        PrayerStatus.ALONE to "On Time",
        PrayerStatus.LATE to "Late",
        PrayerStatus.MISSED to "Not Prayed"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val title = when (range) {
                StatsRange.WEEKS -> "Last 7 Days Breakdown"
                StatsRange.MONTHS -> "Last 30 Days Breakdown"
                StatsRange.YEARS -> "Last Year Breakdown"
                StatsRange.ALL_TIME -> "All Time Breakdown"
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpressiveSegmentedProgressBar(breakdown = breakdown, statuses = statuses)

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed Legend
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                statuses.chunked(2).forEach { rowStatuses ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowStatuses.forEach { (status, label) ->
                            val count = breakdown[status] ?: 0
                            val percentage = (count / total * 100).toInt()
                            
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(status.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    status.icon?.let {
                                        Icon(
                                            imageVector = it,
                                            contentDescription = null,
                                            tint = status.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$percentage%",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = status.color
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${if (count == 1) "1 time" else "$count times"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Insight Hint
            val mostConsistent = statuses.maxByOrNull { breakdown[it.first] ?: 0 }
            if (mostConsistent != null && (breakdown[mostConsistent.first] ?: 0) > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your're most consistent with: ${mostConsistent.second}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerStatusBox(
    status: PrayerStatus,
    isFuture: Boolean,
    onClick: (Offset) -> Unit
) {
    val statusColor = when {
        isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        status == PrayerStatus.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant
        else -> status.color
    }
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    var boxOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(28.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                boxOffset = position + Offset(size.width / 2f, size.height / 2f)
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        statusColor,
                        statusColor.copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (status == PrayerStatus.UPCOMING) 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) 
                    else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = !isFuture) { 
                isPressed = true
                onClick(boxOffset)
                // Reset scale after a short delay
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isFuture && status != PrayerStatus.UPCOMING) {
            status.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
    
    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun ExpressiveSegmentedProgressBar(
    breakdown: Map<PrayerStatus, Int>,
    statuses: List<Pair<PrayerStatus, String>>
) {
    val totalCount = breakdown.values.sum()
    val total = totalCount.toFloat().coerceAtLeast(1f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (totalCount == 0) {
            // Placeholder track when no data is recorded
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        } else {
            statuses.forEach { (status, _) ->
                val count = breakdown[status] ?: 0
                val targetWeight = count / total
                
                val animatedWeight by animateFloatAsState(
                    targetValue = targetWeight,
                    animationSpec = tween(durationMillis = 800),
                    label = "${status.name}Weight"
                )

                if (animatedWeight > 0.001f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedWeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(status.color)
                    )
                }
            }
        }
    }
}

