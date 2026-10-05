package com.alfred.kitabalhuda.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alfred.kitabalhuda.ui.theme.TealAccent
import kotlin.math.sin

/**
 * Compose visualizer drawing 16 radial bars around a circular boundary.
 * Port of AudioBarsRadialView to Jetpack Compose Canvas.
 */
@Composable
fun RadialAudioBars(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    barColor: Color = TealAccent,
    barCount: Int = 16,
    content: @Composable (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadialAudioBarsTransition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing)
        ),
        label = "RadialAudioBarsProgress"
    )

    // Pre-calculate fixed phases and amplitudes per bar
    val barPhases = remember(barCount) {
        FloatArray(barCount) { it * (360f / barCount) * (Math.PI.toFloat() / 180f) }
    }
    val barSpeeds = remember(barCount) {
        floatArrayOf(
            2.4f, 3.1f, 2.8f, 3.5f,
            2.2f, 3.0f, 2.7f, 3.3f,
            2.5f, 3.2f, 2.9f, 3.4f,
            2.3f, 3.1f, 2.6f, 3.6f
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val minDim = minOf(cx, cy)
                if (minDim <= 0f) return@Canvas

                val barWidth = 3.dp.toPx()
                val barMaxHeight = 7.dp.toPx()
                val innerRadius = minDim - barMaxHeight - 2.dp.toPx()
                val angleStep = 360f / barCount

                for (i in 0 until barCount) {
                    val phase = barPhases[i]
                    val speed = barSpeeds[i % barSpeeds.size]
                    val rawSin = sin(animationProgress * speed + phase)
                    val normalized = (0.35f + 0.65f * ((rawSin + 1f) / 2f)).coerceIn(0.2f, 1f)
                    val currentHeight = barMaxHeight * normalized

                    rotate(degrees = angleStep * i, pivot = Offset(cx, cy)) {
                        val path = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = cx - barWidth / 2f,
                                    top = cy - innerRadius - currentHeight,
                                    right = cx + barWidth / 2f,
                                    bottom = cy - innerRadius,
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            )
                        }
                        drawPath(
                            path = path,
                            color = barColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
        content?.invoke()
    }
}
