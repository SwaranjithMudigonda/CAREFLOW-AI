package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 28
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseHeights = listOf(
            0.2f, 0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 0.85f, 0.5f,
            1.0f, 0.65f, 0.8f, 0.4f, 0.95f, 0.75f, 0.55f, 0.9f,
            0.6f, 0.85f, 0.45f, 0.7f, 0.95f, 0.5f, 0.75f, 0.35f,
            0.6f, 0.8f, 0.4f, 0.2f
        )

        baseHeights.forEachIndexed { index, baseFactor ->
            val animDuration = 600 + (index * 45) % 400
            val animProgress by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = if (isRecording && !isPaused) baseFactor else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )

            val currentHeight = if (isRecording && !isPaused) {
                (animProgress * 64).dp.coerceAtLeast(8.dp)
            } else if (isPaused) {
                (baseFactor * 24).dp.coerceAtLeast(6.dp)
            } else {
                8.dp
            }

            val barColor = if (isRecording && !isPaused) {
                if (index % 4 == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            } else if (isPaused) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(currentHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}
