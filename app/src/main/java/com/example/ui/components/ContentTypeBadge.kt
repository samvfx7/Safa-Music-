package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentStatus
import com.example.data.model.ContentType

@Composable
fun ContentTypeBadge(
    contentType: ContentType,
    modifier: Modifier = Modifier,
    status: ContentStatus? = null,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) 8.dp else 12.dp)

    Row(
        modifier = modifier
            .testTag("content_type_badge_${contentType.id}")
            .clip(shape)
            .background(contentType.badgeContainerColor)
            .border(1.dp, contentType.badgeColor.copy(alpha = 0.45f), shape)
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 3.dp else 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        Text(
            text = contentType.badgeIcon,
            fontSize = if (compact) 11.sp else 13.sp
        )

        Text(
            text = contentType.displayName,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentType.badgeColor
        )

        if (status != null && status != ContentStatus.UNKNOWN && !compact) {
            Text(
                text = "• ${status.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = contentType.badgeColor.copy(alpha = 0.75f),
                fontSize = 10.sp
            )
        }
    }
}
