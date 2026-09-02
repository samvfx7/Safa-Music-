package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassificationStatus

@Composable
fun ClassificationBadge(
    status: ClassificationStatus,
    modifier: Modifier = Modifier,
    confidence: Float? = null,
    showConfidence: Boolean = false,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) 8.dp else 12.dp)

    Row(
        modifier = modifier
            .testTag("classification_badge_${status.id}")
            .clip(shape)
            .background(status.containerColor.copy(alpha = 0.85f))
            .border(1.dp, status.color.copy(alpha = 0.4f), shape)
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(if (compact) 6.dp else 8.dp)
                .clip(CircleShape)
                .background(status.color)
        )

        Text(
            text = status.displayName,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = status.color
        )

        if (showConfidence && confidence != null && confidence > 0f) {
            val percent = (confidence * 100).toInt()
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall,
                color = status.color.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
