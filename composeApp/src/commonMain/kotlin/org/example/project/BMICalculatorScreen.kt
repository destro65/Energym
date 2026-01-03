package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow

@Composable
fun BMICalculatorScreen(onBack: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }
    var bmiCategory by remember { mutableStateOf("") }

    fun calculateBMI() {
        val w = weight.toDoubleOrNull()
        val h = height.toDoubleOrNull()

        if (w != null && h != null && h > 0) {
            val heightInMeters = if (h > 3) h / 100 else h
            val bmi = w / heightInMeters.pow(2)
            bmiResult = bmi
            bmiCategory = when {
                bmi < 18.5 -> "Bajo peso"
                bmi < 24.9 -> "Peso normal"
                bmi < 29.9 -> "Sobrepeso"
                else -> "Obesidad"
            }
        }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Calculate,
                contentDescription = null,
                tint = Color(0xFFB39DDB),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFFB39DDB),
                    unfocusedIndicatorColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFFB39DDB),
                    focusedLabelColor = Color(0xFFB39DDB),
                    unfocusedLabelColor = Color(0xFFB39DDB),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Altura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFFB39DDB),
                    unfocusedIndicatorColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFFB39DDB),
                    focusedLabelColor = Color(0xFFB39DDB),
                    unfocusedLabelColor = Color(0xFFB39DDB),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { calculateBMI() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB39DDB),
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (bmiResult != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Tu IMC es",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            String.format("%.1f", bmiResult),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color(0xFFB39DDB),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            bmiCategory,
                            style = MaterialTheme.typography.headlineSmall,
                            color = when(bmiCategory) {
                                "Peso normal" -> Color.Green
                                "Bajo peso" -> Color.Yellow
                                else -> Color.Red
                            }
                        )
                    }
                }
            }
        }
    }
}