package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Helper function for the bounce animation
private fun BounceInterpolator(): Easing {
    return Easing { fraction ->
        val n1 = 7.5625f
        val d1 = 2.75f
        var t = fraction
        when {
            t < 1 / d1 -> n1 * t * t
            t < 2 / d1 -> {
                t -= 1.5f / d1
                n1 * t * t + 0.75f
            }
            t < 2.5 / d1 -> {
                t -= 2.25f / d1
                n1 * t * t + 0.9375f
            }
            else -> {
                t -= 2.625f / d1
                n1 * t * t + 0.984375f
            }
        }
    }
}

@Composable
actual fun SplashScreen(onSplashFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val soundPlayer = rememberSoundPlayer("splash_sound1")

    // Transición para animaciones infinitas
    val infiniteTransition = rememberInfiniteTransition()

    // Animación de pulso para el logo, ahora más sutil
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f, // Pulso más sutil para reducir el "zoom"
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación para la onda de choque
    val shockwave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(key1 = true) {
        visible = true
        soundPlayer.play()
        delay(3500L)
        onSplashFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF673AB7), Color.Black),
                    radius = 1000f
                )
            )
    ) {
        // Círculos de fondo y ONDA DE CHOQUE
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension / 2

            // Onda de choque
            drawCircle(
                color = Color.White.copy(alpha = (1 - shockwave) * 0.2f), // Se desvanece al expandirse
                radius = radius * shockwave,
                style = Stroke(width = (4.dp.toPx() * (1 - shockwave))) // Se hace más delgada
            )

            // Sombra para el primer círculo
            drawCircle(
                color = Color(0xFF4A148C).copy(alpha = 0.08f),
                radius = radius * (scale - 0.1f),
                center = center,
                style = Stroke(width = 8.dp.toPx())
            )
            // Círculo original 1
            drawCircle(
                color = Color(0xFFB39DDB).copy(alpha = 0.1f),
                radius = radius * (scale - 0.1f),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Animación de entrada mejorada
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = tween(1200, easing = BounceInterpolator())) + // Más duración
                    fadeIn(animationSpec = tween(800, easing = LinearOutSlowInEasing)) +
                    expandIn(expandFrom = Alignment.Center) { it }
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Logo principal (los anillos han sido eliminados)
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo de Energym",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp) // Tamaño del logo del splash
                        .scale(scale)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}