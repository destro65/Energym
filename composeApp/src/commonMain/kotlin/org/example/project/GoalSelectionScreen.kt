package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GoalSelectionScreen(onGoalSelected: (String) -> Unit) {
    // Lista de objetivos con sus descripciones
    val goals = listOf(
        "Ganar Masa Muscular" to "Enfocado en hipertrofia y aumento de volumen muscular mediante levantamiento de pesas y superávit calórico.",
        "Perder Peso" to "Diseñado para crear un déficit calórico y quemar grasa corporal mediante cardio y entrenamiento de fuerza.",
        "Mejorar Resistencia" to "Entrenamientos aeróbicos y de alta intensidad (HIIT) para fortalecer el sistema cardiovascular y la resistencia física.",
        "Definición" to "Busca resaltar la musculatura reduciendo el porcentaje de grasa corporal manteniendo la masa muscular existente."
    )

    var showInfoDialog by remember { mutableStateOf(false) }
    var currentInfoTitle by remember { mutableStateOf("") }
    var currentInfoDescription by remember { mutableStateOf("") }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(currentInfoTitle, color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(currentInfoDescription, color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF333333),
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Entendido", color = Color(0xFFB39DDB))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color(0xFF39006F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color(0xFFB39DDB),
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "¿Cuál es tu objetivo?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            
            Text(
                "Personalizaremos tu experiencia",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(goals) { (goal, description) ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalSelected(goal) },
                        onInfoClick = {
                            currentInfoTitle = goal
                            currentInfoDescription = description
                            showInfoDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoalCard(goal: String, onClick: () -> Unit, onInfoClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFB39DDB).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = goal,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Seleccionar",
                    tint = Color(0xFFB39DDB)
                )
            }
        }
    }
}