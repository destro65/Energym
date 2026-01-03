package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// --- Data Classes para la nueva lógica ---
data class RadarItem(val name: String, val lastTrained: String, val icon: ImageVector)
data class Exercise(val name: String, val details: String, val videoUrl: String? = null)

// --- Lógica de Datos Dinámica ---
fun getRadarItemsForGoal(goal: String): List<RadarItem> {
    return when (goal) {
        "Ganar Masa Muscular", "Definición" -> listOf(
            RadarItem("Pecho", "Hace 3 días", Icons.Default.AccessibilityNew),
            RadarItem("Espalda", "Hace 5 días", Icons.Default.Accessibility),
            RadarItem("Piernas", "Ayer", Icons.Default.DirectionsRun),
            RadarItem("Hombro", "Hace 2 días", Icons.Default.AccessibilityNew),
            RadarItem("Brazos", "Hace 4 días", Icons.Default.FitnessCenter)
        )
        "Perder Peso", "Mejorar Resistencia" -> listOf(
            RadarItem("Cardio HIIT", "Hoy", Icons.Default.Bolt),
            RadarItem("Cardio LISS", "Ayer", Icons.Default.DirectionsWalk),
            RadarItem("Resistencia", "Hace 2 días", Icons.Default.Loop),
            RadarItem("Full Body", "Hace 3 días", Icons.Default.Accessibility)
        )
        else -> emptyList()
    }
}

fun getRoutineForGoal(goal: String, itemName: String): List<Exercise> {
    return when (goal) {
        "Ganar Masa Muscular" -> when (itemName) {
            "Pecho" -> listOf(
                Exercise("Press de Banca Plano", "4 series x 6-8 reps (al fallo)", "https://www.youtube.com/watch?v=TAH8RxOS0VI"),
                Exercise("Press Inclinado con Mancuernas", "3 series x 8-10 reps"),
                Exercise("Aperturas con Cable o Fondos", "3 series x 10-12 reps")
            )
            "Espalda" -> listOf(
                Exercise("Dominadas o Jalón al Pecho", "4 series al fallo o 8-10 reps"),
                Exercise("Remo con Barra", "4 series x 8-10 reps"),
                Exercise("Remo en Punta o Serrucho", "3 series x 10-12 reps por brazo")
            )
            "Piernas" -> listOf(
                Exercise("Sentadillas con Barra", "4 series x 6-8 reps (al fallo)"),
                Exercise("Prensa de Piernas", "3 series x 10-12 reps"),
                Exercise("Extensiones de Cuádriceps", "3 series x 12-15 reps"),
                Exercise("Curl Femoral", "3 series x 12-15 reps")
            )
            "Hombro" -> listOf(
                Exercise("Press Militar con Barra", "4 series x 6-8 reps"),
                Exercise("Elevaciones Laterales con Mancuerna", "4 series x 12-15 reps"),
                Exercise("Face Pulls (Tirón a la cara)", "3 series x 15-20 reps")
            )
            "Brazos" -> listOf(
                Exercise("Curl de Bíceps con Barra", "4 series x 8-10 reps"),
                Exercise("Press Francés o Fondos para Tríceps", "4 series x 8-10 reps"),
                Exercise("Curl Martillo con Mancuerna", "3 series x 10-12 reps"),
                Exercise("Extensiones de Tríceps en Polea", "3 series x 12-15 reps")
            )
            else -> listOf(Exercise("Ejercicio de Ejemplo", "3 series x 10 reps")) // Fallback
        }
        else -> emptyList() // Fallback para otros objetivos
    }
}

@Composable
fun RoutinesScreen(userGoal: String, onBack: () -> Unit) {
    var selectedItem by remember { mutableStateOf<RadarItem?>(null) }
    var videoUrlToPlay by remember { mutableStateOf<String?>(null) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var showRadarInfoDialog by remember { mutableStateOf(false) }
    var showRecoveryInfoDialog by remember { mutableStateOf(false) }

    val radarItems = getRadarItemsForGoal(userGoal)

    if (showRecoveryInfoDialog) { /* ... Diálogo ... */ }
    if (showRadarInfoDialog) { /* ... Diálogo ... */ }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- CONTENIDO DEL RADAR RESTAURADO ---
        Column(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.radialGradient(colors = listOf(Color(0xFF220044), Color.Black))
            )
        ) {
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Radar: $userGoal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showRecoveryInfoDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Información de Recuperación", tint = Color.Gray)
                }
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectDragGestures { _, dragAmount -> rotationAngle += dragAmount.x * 0.5f }
                },
                contentAlignment = Alignment.Center
            ) {
                val maxRadius = minOf(constraints.maxWidth, constraints.maxHeight) / 2f * 0.8f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color.Gray.copy(alpha = 0.1f), radius = maxRadius * 0.5f, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = Color.Gray.copy(alpha = 0.2f), radius = maxRadius, style = Stroke(width = 1.dp.toPx()))
                }

                radarItems.forEachIndexed { index, item ->
                    val angleStep = 360f / radarItems.size
                    val itemAngleRad = Math.toRadians((angleStep * index + rotationAngle).toDouble())
                    val visualRadius = maxRadius * 0.7f

                    val itemX = (visualRadius * cos(itemAngleRad)).toFloat()
                    val itemY = (visualRadius * sin(itemAngleRad)).toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset { IntOffset(itemX.roundToInt(), itemY.roundToInt()) }
                            .clickable { selectedItem = item }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, Color(0xFFB39DDB), CircleShape)
                        ) {
                            Icon(item.icon, item.name, tint = Color(0xFFB39DDB), modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = item.lastTrained, color = Color.Gray, fontSize = 10.sp)
                    }
                }

                IconButton(
                    onClick = { showRadarInfoDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "Información del Radar", 
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // --- VISTA DE DETALLE DE RUTINA ---
        AnimatedVisibility(
            visible = selectedItem != null && videoUrlToPlay == null, 
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300))
        ) {
            selectedItem?.let { item ->
                val routine = getRoutineForGoal(userGoal, item.name)
                RoutineDetailView(
                    itemName = item.name,
                    routine = routine,
                    onPlayVideo = { videoUrl -> videoUrlToPlay = videoUrl },
                    onDismiss = { selectedItem = null }
                )
            }
        }

        // --- VISTA DE REPRODUCTOR DE VIDEO ---
        AnimatedVisibility(visible = videoUrlToPlay != null) {
            videoUrlToPlay?.let {
                VideoPlayerScreen(url = it, onDismiss = { videoUrlToPlay = null })
            }
        }
    }
}

@Composable
fun RoutineDetailView(itemName: String, routine: List<Exercise>, onPlayVideo: (String) -> Unit, onDismiss: () -> Unit) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rutina: $itemName", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                }
                Divider(color = Color(0xFFB39DDB), modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(routine) { exercise ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFFB39DDB), modifier = Modifier.padding(end = 12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(exercise.details, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                                if (exercise.videoUrl != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onPlayVideo(exercise.videoUrl) }) {
                                        Icon(Icons.Default.PlayCircle, "Ver video", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(url: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() }, // Clic en el fondo para cerrar
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Este clickable vacío intercepta los clics para que no se propaguen al fondo
            modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            VideoPlayer(modifier = Modifier.fillMaxWidth(0.95f).aspectRatio(16f / 9f), url = url)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) {
                Text("Cerrar Video", color = Color.Black)
            }
        }
    }
}

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String)
