package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicGold
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    waveformPoints: List<Float>,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    playbackProgress: Float = 0f,
    height: Dp = 64.dp,
    activeColor: Color = EmeraldPrimary,
    inactiveColor: Color = Color(0xFF273833),
    playedColor: Color = IslamicGold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val points = if (waveformPoints.isNotEmpty()) {
        waveformPoints
    } else {
        List(40) { index -> 0.3f + (0.4f * (index % 5) / 5f) }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val count = points.size
        val barWidth = (size.width / (count * 1.6f)).coerceIn(2.dp.toPx(), 8.dp.toPx())
        val totalBarSpace = size.width / count

        for (i in 0 until count) {
            val progress = i.toFloat() / count.toFloat()
            val rawAmp = points[i]

            // Dynamic bounce if currently playing
            val dynamicFactor = if (isPlaying) {
                1.0f + (0.25f * sin(waveAnim + (i * 0.4f)))
            } else {
                1.0f
            }

            val barHeight = (size.height * rawAmp * dynamicFactor).coerceIn(4.dp.toPx(), size.height * 0.95f)
            val startX = (i * totalBarSpace) + ((totalBarSpace - barWidth) / 2f)
            val startY = (size.height - barHeight) / 2f

            val color = when {
                progress <= playbackProgress -> playedColor
                isPlaying -> activeColor
                else -> inactiveColor
            }

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.95f),
                        color.copy(alpha = 0.5f)
                    ),
                    startY = startY,
                    endY = startY + barHeight
                ),
                topLeft = Offset(startX, startY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
