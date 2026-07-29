package com.hussain.namaztracker.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hussain.namaztracker.Screen
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalHazeApi::class)
@Composable
fun FloatingBottomNavigation(
    screens: List<Screen>,
    currentRoute: String?,
    hazeState: HazeState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalFloatingToolbar(
            modifier = Modifier
                .clip(FloatingToolbarDefaults.ContainerShape)
                .hazeEffect(
                    state = hazeState,
                    block = fun HazeEffectScope.() {
                        style = HazeDefaults.style(
                            backgroundColor = Color.Transparent,
                            blurRadius = 24.dp,
                            noiseFactor = -1f,
                        )
                        blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                    }
                )
                .zIndex(1000f)
                .animateContentSize(
                    MaterialTheme.motionScheme.fastSpatialSpec()
                ),
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f),
            ),
            expanded = true,
        ) {
            screens.forEach { screen ->
                val isSelected = currentRoute == screen.route
                
                TonalToggleButton(
                    checked = isSelected,
                    onCheckedChange = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onNavigate(screen.route)
                    },
                    shapes = ToggleButtonDefaults.shapes(
                        shape = CircleShape,
                        checkedShape = RoundedCornerShape(14.dp)
                    ),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        label = "iconScale"
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(iconScale)
                        )
                        
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(
                                MaterialTheme.motionScheme.fastSpatialSpec()
                            ),
                            exit = fadeOut() + shrinkHorizontally(
                                MaterialTheme.motionScheme.fastSpatialSpec()
                            )
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.1.sp,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
