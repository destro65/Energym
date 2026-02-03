package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// --- Data Classes ---
data class RadarItem(val name: String, val lastTrained: String, val icon: ImageVector)
data class Exercise(val name: String, val details: String, val videoUrl: String? = null)
data class ExercisePart(val partName: String, val variants: List<Exercise>)

// --- Lógica de Datos ---
fun getRadarItemsForGoal(goal: String): List<RadarItem> {
    return when (goal) {
        "Ganar Masa Muscular", "Definición" -> listOf(
            RadarItem("Pecho", "Entrenar", Icons.Default.AccessibilityNew),
            RadarItem("Espalda", "Entrenar", Icons.Default.Accessibility),
            RadarItem("Piernas", "Entrenar", Icons.AutoMirrored.Filled.DirectionsRun),
            RadarItem("Hombro", "Entrenar", Icons.Default.AccessibilityNew),
            RadarItem("Brazos", "Entrenar", Icons.Default.FitnessCenter)
        )
        "Perder Peso" -> listOf(
            RadarItem("Cardio HIIT", "Entrenar", Icons.Default.Bolt),
            RadarItem("Cardio LISS", "Entrenar", Icons.AutoMirrored.Filled.DirectionsWalk),
            RadarItem("Fuerza", "Entrenar", Icons.Default.FitnessCenter),
            RadarItem("Full Body", "Entrenar", Icons.Default.Accessibility)
        )
        else -> emptyList()
    }
}

fun getDietForGoal(goal: String): List<Exercise> {
    return when (goal) {
        "Ganar Masa Muscular" -> listOf(
            Exercise("Desayuno", "Avena con agua o leche, 2 huevos cocidos y 1 plátano (alimento barato y energético)."),
            Exercise("Almuerzo", "Arroz blanco (buena porción), lentejas o frijoles y pollo, hígado o carne molida económica."),
            Exercise("Merienda", "Pan con queso o huevo, y una fruta de temporada (guayaba, naranja o lo que esté barato)."),
            Exercise("Cena", "Yuca, camote o papa cocida con huevo frito o queso y ensalada de repollo.")
        )
        "Perder Peso" -> listOf(
            Exercise("Desayuno", "Té o café sin azúcar y 2 huevos revueltos con cebolla y tomate (perico)."),
            Exercise("Almuerzo", "Sopa de vegetales con granos y una pieza pequeña de proteína. Mucha agua."),
            Exercise("Merienda", "Una fruta cítrica o un puñado de maní (cacahuetes) tostados."),
            Exercise("Cena", "Ensalada de atún (en lata) con cebolla, tomate y limón. Evitar el pan.")
        )
        "Definición" -> listOf(
            Exercise("Desayuno", "3 claras de huevo y 1 huevo entero revueltos. Un poco de avena en agua."),
            Exercise("Almuerzo", "Pollo a la plancha o sudado, ensalada verde grande y solo 4 cucharadas de arroz."),
            Exercise("Merienda", "Un huevo duro o un vaso de yogur natural casero sin azúcar."),
            Exercise("Cena", "Pescado económico o pollo con espinacas o brócoli al vapor.")
        )
        else -> listOf(Exercise("Dieta Sugerida", "Prioriza alimentos naturales comprados en plaza: huevos, granos, tubérculos y vegetales."))
    }
}

fun getRoutinePartsForGoal(goal: String, itemName: String): List<ExercisePart> {
    return when (itemName) {
        "Pecho" -> listOf(
            ExercisePart("Pecho Alto", listOf(
                Exercise("Press Inclinado con Barra", "4x10 - Fuerza en la parte superior."),
                Exercise("Press Inclinado con Mancuernas", "4x12 - Control y rango de movimiento."),
                Exercise("Flexiones con pies en silla", "4 al fallo - Excelente para casa.")
            )),
            ExercisePart("Pecho Medio", listOf(
                Exercise("Press de Banca Plano", "4x8 - El ejercicio rey del pecho."),
                Exercise("Press en Máquina Sentado", "4x12 - Tensión constante."),
                Exercise("Flexiones clásicas", "4 al fallo - Control corporal.")
            )),
            ExercisePart("Pecho Bajo", listOf(
                Exercise("Fondos en paralelas", "4x10 - Enfoque inferior y tríceps."),
                Exercise("Press Declinado", "4x10 - Para dar forma a la base."),
                Exercise("Flexiones con manos en banco", "4x15 - Variación ligera.")
            )),
            ExercisePart("Cierre de Pecho", listOf(
                Exercise("Contractor (Pec Deck)", "3x15 - Bombeo final."),
                Exercise("Aperturas con mancuernas", "3x12 - Estiramiento de fibras."),
                Exercise("Cruce de poleas", "3x15 - Máxima contracción.")
            ))
        )
        "Espalda" -> listOf(
            ExercisePart("Amplitud", listOf(
                Exercise("Dominadas", "4 al fallo - Espalda ancha."),
                Exercise("Jalón al pecho", "4x12 - Variante controlada."),
                Exercise("Jalón agarre cerrado", "4x12 - Enfoque central.")
            )),
            ExercisePart("Grosor", listOf(
                Exercise("Remo con barra", "4x10 - Potencia de tracción."),
                Exercise("Remo con mancuerna", "4x10 por brazo - Unilateral."),
                Exercise("Remo en polea baja", "4x12 - Control de escápulas.")
            )),
            ExercisePart("Espalda Baja", listOf(
                Exercise("Peso muerto", "4x8 - Fuerza total."),
                Exercise("Extensiones lumbares", "3x15 - Salud de columna."),
                Exercise("Buenos días", "3x12 - Flexibilidad y fuerza.")
            )),
            ExercisePart("Remate Posterior", listOf(
                Exercise("Facepull", "3x15 - Hombro posterior y espalda alta."),
                Exercise("Pájaros", "3x15 - Detalle posterior."),
                Exercise("Pull-over polea alta", "3x15 - Aislamiento dorsal.")
            ))
        )
        "Piernas" -> listOf(
            ExercisePart("Cuádriceps", listOf(
                Exercise("Sentadilla con barra", "4x10 - Piernas fuertes."),
                Exercise("Prensa de piernas", "4x12 - Volumen seguro."),
                Exercise("Zancadas (Lunges)", "4x12 - Estabilidad.")
            )),
            ExercisePart("Femorales", listOf(
                Exercise("Peso muerto rumano", "4x12 - Estiramiento isquios."),
                Exercise("Curl femoral acostado", "4x12 - Aislamiento."),
                Exercise("Zancada lateral", "3x12 - Variante funcional.")
            )),
            ExercisePart("Glúteos", listOf(
                Exercise("Hip Thrust", "4x10 - Poder en glúteo."),
                Exercise("Patada de glúteo", "4x15 - Aislamiento."),
                Exercise("Puente de glúteo", "4x20 - Resistencia.")
            )),
            ExercisePart("Pantorrillas", listOf(
                Exercise("Elevación talones de pie", "4x20 - Gemelos fuertes."),
                Exercise("Elevación talones sentado", "4x20 - Sóleo."),
                Exercise("Puntillas en prensa", "4x20 - Volumen gemelo.")
            ))
        )
        "Hombro" -> listOf(
            ExercisePart("Hombro Frontal", listOf(
                Exercise("Press Militar", "4x10 - Hombros redondos."),
                Exercise("Press Arnold", "4x10 - Rango completo."),
                Exercise("Elevación frontal", "3x12 - Aislamiento frontal.")
            )),
            ExercisePart("Hombro Lateral", listOf(
                Exercise("Elevaciones laterales", "4x15 - Anchura lateral."),
                Exercise("Laterales en polea", "4x12 - Tensión continua."),
                Exercise("Remo al mentón", "4x12 - Trapezio y hombro.")
            )),
            ExercisePart("Hombro Posterior", listOf(
                Exercise("Pájaros con mancuernas", "4x15 - Salud de hombro."),
                Exercise("Facepull en polea", "4x15 - Postura correcta."),
                Exercise("Posterior en máquina", "4x15 - Bombeo.")
            )),
            ExercisePart("Trapecio", listOf(
                Exercise("Encogimientos mancuerna", "4x12 - Cuello fuerte."),
                Exercise("Encogimientos barra", "4x10 - Pesado."),
                Exercise("Paseo del granjero", "3x1 min - Fuerza de agarre.")
            ))
        )
        "Brazos" -> listOf(
            ExercisePart("Bíceps (Masa)", listOf(
                Exercise("Curl con barra", "4x10 - Construcción básica."),
                Exercise("Curl con mancuernas", "4x12 - Supinación."),
                Exercise("Curl en banco predicador", "4x10 - Sin balanceo.")
            )),
            ExercisePart("Bíceps (Detalle)", listOf(
                Exercise("Curl Martillo", "3x12 - Grosor de brazo."),
                Exercise("Curl concentrado", "3x12 - Pico del bíceps."),
                Exercise("Curl polea baja", "3x12 - Tensión final.")
            )),
            ExercisePart("Tríceps (Extensión)", listOf(
                Exercise("Press Francés", "4x10 - Tríceps largo."),
                Exercise("Extensión tras nuca", "4x12 - Estiramiento."),
                Exercise("Fondos entre bancos", "4x15 - Económico y efectivo.")
            )),
            ExercisePart("Tríceps (Aislamiento)", listOf(
                Exercise("Extensión polea alta", "4x15 - Bombeo."),
                Exercise("Patada de tríceps", "3x15 - Definición."),
                Exercise("Extensión con cuerda", "4x15 - Separación muscular.")
            ))
        )
        else -> listOf(
            ExercisePart("General", listOf(Exercise("Actividad", "3x12"), Exercise("Variante 2", "3x12"), Exercise("Variante 3", "3x12")))
        )
    }
}

@Composable
fun RoutinesScreen(userGoal: String, onBack: () -> Unit) {
    var selectedItem by remember { mutableStateOf<RadarItem?>(null) }
    var showDiet by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var showRecoveryInfoDialog by remember { mutableStateOf(false) }

    val radarItems = getRadarItemsForGoal(userGoal)

    if (showRecoveryInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryInfoDialog = false },
            title = { Text("Información", color = Color.White) },
            text = { Text("La alimentación sencilla y el descanso son tan importantes como el entrenamiento mismo.", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF333333),
            confirmButton = { TextButton(onClick = { showRecoveryInfoDialog = false }) { Text("ENTENDIDO", color = Color(0xFFB39DDB)) } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(brush = Brush.radialGradient(colors = listOf(Color(0xFF220044), Color.Black)))
        ) {
             Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(text = "RUTINAS: $userGoal", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showRecoveryInfoDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
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

                // --- BOTÓN CENTRAL: DIETA ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(95.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF2E7D32)))).border(2.dp, Color.White, CircleShape).clickable { showDiet = true }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Restaurant, "Dieta", tint = Color.White, modifier = Modifier.size(32.dp))
                        Text("DIETA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                radarItems.forEachIndexed { index, item ->
                    val angleStep = 360f / radarItems.size
                    val itemAngleRad = (angleStep * index + rotationAngle) * (PI / 180.0)
                    val visualRadius = maxRadius * 0.75f
                    val itemX = (visualRadius * cos(itemAngleRad)).toFloat()
                    val itemY = (visualRadius * sin(itemAngleRad)).toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset { IntOffset(itemX.roundToInt(), itemY.roundToInt()) }.clickable { selectedItem = item }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(65.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)).border(1.dp, Color(0xFFB39DDB), CircleShape)
                        ) {
                            Icon(item.icon, item.name, tint = Color(0xFFB39DDB), modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- Detalle de Rutina ---
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300))
        ) {
            selectedItem?.let { item ->
                val parts = getRoutinePartsForGoal(userGoal, item.name)
                RoutinePartsDetailView(
                    itemName = item.name,
                    parts = parts,
                    onDismiss = { selectedItem = null }
                )
            }
        }

        // --- Detalle de Dieta ---
        AnimatedVisibility(
            visible = showDiet,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300))
        ) {
            val diet = getDietForGoal(userGoal)
            DietDetailView(
                diet = diet,
                onDismiss = { showDiet = false }
            )
        }
    }
}

@Composable
fun RoutinePartsDetailView(itemName: String, parts: List<ExercisePart>, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f).clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0033)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Grupo: $itemName", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Personaliza tu rutina eligiendo variantes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }
                HorizontalDivider(color = Color(0xFFB39DDB).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(parts) { part ->
                        ExercisePartItem(part)
                    }
                }
            }
        }
    }
}

@Composable
fun ExercisePartItem(part: ExercisePart) {
    var selectedIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = part.partName, color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            part.variants.forEachIndexed { index, exercise ->
                val isSelected = selectedIndex == index
                Card(
                    modifier = Modifier.weight(1f).clickable { selectedIndex = index },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF39006F) else Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFB39DDB) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Opción ${index + 1}", color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = exercise.name, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, minLines = 2)
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = part.variants[selectedIndex].details, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DietDetailView(diet: List<Exercise>, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f).clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF002211)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Plan Nutricional", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Accesible y nutritivo", style = MaterialTheme.typography.bodySmall, color = Color(0xFF81C784))
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }
                HorizontalDivider(color = Color(0xFF81C784).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(diet) { meal ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RestaurantMenu, null, tint = Color(0xFF81C784), modifier = Modifier.padding(end = 12.dp))
                                Column {
                                    Text(meal.name, color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(meal.details, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "* Usa alimentos de temporada y granos para ahorrar. Evita refrescos y azúcar.",
                    color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String)
