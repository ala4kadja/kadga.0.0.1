package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KadjaGlowGradientEnd
import com.example.ui.theme.KadjaGlowGradientStart
import kotlin.random.Random

@Composable
fun AudioVisualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barColor: Color = KadjaGlowGradientStart
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    
    val barHeights = List(barCount) { index ->
        if (isPlaying) {
            val anim by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (index * 90),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            anim
        } else {
            0.2f
        }
    }

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        barHeights.forEach { heightRatio ->
            Canvas(modifier = Modifier.width(3.dp).fillMaxHeight()) {
                val barHeight = size.height * heightRatio
                val topY = size.height - barHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, topY),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun PulsingMusicGlow(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.95f else 1f,
        targetValue = if (isPlaying) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.4f else 0.15f,
        targetValue = if (isPlaying) 0.85f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Canvas(modifier = modifier) {
        val radius = (size.minDimension / 2) * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    KadjaGlowGradientStart.copy(alpha = alpha),
                    KadjaGlowGradientEnd.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}
