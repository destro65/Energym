package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
actual fun SplashScreen(onSplashFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val soundPlayer = rememberSoundPlayer("splash_sound1")
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    // Animación de escala para el logo y el resplandor
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineHighlightEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // Rotaciones encontradas para los anillos
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotFast"
    )

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotSlow"
    )

    LaunchedEffect(Unit) {
        visible = true
        soundPlayer.play()
        delay(4000L) // Un poco más de tiempo para apreciar el arte
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0033), Color.Black, Color(0xFF39006F))
                )
            )
    ) {
        // --- TEXTO SUPERIOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1500)) + slideInVertically(initialOffsetY = { -50 })
            ) {
                Text(
                    "ENERGYM",
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 12.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFB39DDB),
                            offset = Offset(0f, 0f),
                            blurRadius = 30f
                        )
                    )
                )
            }
        }

        // --- NÚCLEO CENTRAL (Anillos + Logo) ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Resplandor de fondo pulsante (Detrás de todo)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = scale * 1.5f
                        scaleY = scale * 1.5f
                        alpha = 0.15f
                    }
                    .background(Color(0xFFB39DDB), CircleShape)
                    .blur(60.dp)
            )

            // Anillos de Energía
            Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                // Anillo exterior discontinuo
                Canvas(modifier = Modifier.fillMaxSize().rotate(rotationFast)) {
                    drawCircle(
                        color = Color(0xFFB39DDB).copy(alpha = 0.5f),
                        radius = size.minDimension / 2,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(40f, 40f), 0f)
                        )
                    )
                }
                // Anillo medio sólido
                Canvas(modifier = Modifier.fillMaxSize(0.88f).rotate(rotationSlow)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                // Anillo interno de partículas/puntos
                Canvas(modifier = Modifier.fillMaxSize(0.75f).rotate(rotationFast * 0.5f)) {
                    drawCircle(
                        color = Color(0xFFB39DDB).copy(alpha = 0.3f),
                        radius = size.minDimension / 2,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 20f), 0f)
                        )
                    )
                }
            }

            // Logo Principal
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = tween(1500, easing = FastOutSlowInEasing)) + fadeIn(tween(1000))
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(170.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .border(2.dp, Brush.linearGradient(listOf(Color.White, Color(0xFFB39DDB))), CircleShape)
                )
            }
        }

        // --- DETALLE INFERIOR ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(2000, delayMillis = 1000))
            ) {
                Text(
                    "TU TRANSFORMACIÓN COMIENZA AQUÍ",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

// Easing personalizado para un pulso más orgánico
private val SineHighlightEasing = Easing { x ->
    kotlin.math.sin(x * Math.PI.toFloat()).toFloat()
}