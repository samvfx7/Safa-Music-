package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanStage
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StageProgressIndicator(
    currentStage: ScanStage,
    completedStages: Set<ScanStage>,
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        ScanStage.READING_FILE,
        ScanStage.ANALYZING_AUDIO,
        ScanStage.FINDING_LYRICS,
        ScanStage.GEMINI_ASSESSMENT,
        ScanStage.SAVING_RESULT
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_active_stage")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F1715))
            .border(1.dp, Color(0xFF1E2E2A), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Analysis Pipeline Stages",
            style = MaterialTheme.typography.labelMedium,
            color = IslamicGold,
            fontWeight = FontWeight.Bold
        )

        stages.forEachIndexed { index, stage ->
            val isCompleted = completedStages.contains(stage)
            val isCurrent = currentStage == stage
            val isPending = !isCompleted && !isCurrent

            val circleColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> EmeraldPrimary
                    isCurrent -> IslamicGold
                    else -> Color(0xFF263330)
                },
                label = "color"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(if (isCurrent) pulseScale else 1.0f)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPending) Color.Gray else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isCurrent -> TextPrimary
                            isCompleted -> Color(0xFFA7F3D0)
                            else -> TextSecondary
                        }
                    )
                }

                Text(
                    text = when {
                        isCompleted -> "Done"
                        isCurrent -> "In progress"
                        else -> "Queued"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isCompleted -> EmeraldPrimary
                        isCurrent -> IslamicGold
                        else -> Color(0xFF6B7280)
                    }
                )
            }

            if (index < stages.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 11.dp)
                        .width(2.dp)
                        .height(8.dp)
                        .background(if (isCompleted) EmeraldDark else Color(0xFF263330))
                )
            }
        }
    }
}
