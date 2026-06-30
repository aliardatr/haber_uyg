package com.example.haber_portali.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Herhangi bir Compose elemanına .shimmerEfekti() yazarak uygulayabilirsin
fun Modifier.shimmerEfekti(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Karanlık moda uygun koyu gri tonları
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2C2C2C), // Koyu Gri
            Color(0xFF4A4A4A), // Parlayan Açık Gri
            Color(0xFF2C2C2C)  // Koyu Gri
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    this.background(brush)
}