@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.hussain.namaztracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enum for Theme Selection
 */
enum class ThemeMode {
    AUTO, LIGHT, DARK;

    companion object {
        fun fromInt(value: Int): ThemeMode =
            entries.getOrNull(value) ?: AUTO
        
        fun fromString(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: AUTO
    }
}

enum class GroupPosition {
    Single, Top, Middle, Bottom
}

@Composable
fun GroupSurface(
    position: GroupPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = when (position) {
        GroupPosition.Single -> RoundedCornerShape(24.dp)
        GroupPosition.Top -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        GroupPosition.Bottom -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        GroupPosition.Middle -> RoundedCornerShape(8.dp)
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        content()
    }
}

/**
 * Themed Card Component using Material 3 Expressive APIs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCard(
    selectedTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(24.dp),
        color          = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header (Icon + Title)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            val options = ThemeMode.entries
            val count   = options.size

            // Connected Button Group
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedTheme == option

                    // Dynamic shapes for the connected group look
                    val shapes = when {
                        count == 1         -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        index == 0         -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else               -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }

                    ToggleButton(
                        modifier        = Modifier.weight(1f).height(44.dp),
                        checked         = isSelected,
                        onCheckedChange = { 
                            if (it) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onThemeChange(option)
                            }
                        },
                        shapes          = shapes,
                        colors          = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor   = MaterialTheme.colorScheme.onPrimary,
                            containerColor        = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor          = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = when (option) {
                                ThemeMode.AUTO  -> Icons.Default.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK  -> Icons.Default.DarkMode
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = when (option) {
                                ThemeMode.AUTO  -> "Auto"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK  -> "Dark"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 13.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── Setting tiles ─────────────────────────────────────────────────────────────

@Composable
fun SettingTile(
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: RoundedCornerShape
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = shape,
        color          = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(!checked) 
                }
                .padding(16.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis, maxLines = 2)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis, maxLines = 3)
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked       = checked,
                onCheckedChange = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(it) 
                },
                thumbContent  = {
                    Icon(
                        imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            )
        }
    }
}

@Composable
fun ClickableTile(
    icon: ImageVector? = null,
    painter: Painter? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    painterContainerColor: Color = Color.Transparent,
    title: String,
    subtitle: String = "",
    subtitleContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = shape,
        color          = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            if (icon != null || painter != null) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = if (painter != null) painterContainerColor else iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (painter != null) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis, maxLines = 1)
                
                if (subtitleContent != null) {
                    Box {
                        subtitleContent()
                    }
                } else if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis, maxLines = 2)
                }
            }

            if (trailingContent != null) {
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    trailingContent()
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
