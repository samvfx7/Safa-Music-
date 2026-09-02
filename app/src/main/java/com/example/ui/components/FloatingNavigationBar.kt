package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FloatingPillNavigationBar(
    screens: List<Screen>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = pillShape,
            color = Color.Transparent,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = pillShape,
                    spotColor = EmeraldPrimary.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.6f)
                )
                .clip(pillShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkSurfaceElevated.copy(alpha = 0.94f),
                            Color(0xFF0A1411).copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                GlassBorder,
                                EmeraldPrimary.copy(alpha = 0.25f),
                                GlassBorder
                            )
                        )
                    ),
                    shape = pillShape
                )
                .testTag("bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route

                    FloatingPillNavItem(
                        screen = screen,
                        isSelected = isSelected,
                        onClick = { onNavigate(screen.route) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingPillNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val activeIndicatorColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldPrimary.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_bg_color"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldLight else TextSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldLight else TextSecondary.copy(alpha = 0.8f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "text_color"
    )

    val verticalPadding by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "item_padding"
    )

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(activeIndicatorColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = EmeraldPrimary),
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = isSelected
                this.contentDescription = screen.title
            }
            .padding(vertical = verticalPadding, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = screen.title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontSize = if (isSelected) 10.5.sp else 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
