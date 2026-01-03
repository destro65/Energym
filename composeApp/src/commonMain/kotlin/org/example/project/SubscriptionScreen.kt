package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    val plans = listOf(
        Plan("Básico", "$19.99/mes", listOf("Acceso al gimnasio", "Taquillas estándar", "1 Clase grupal/mes"), false),
        Plan("Energym Pro", "$39.99/mes", listOf("Acceso ilimitado", "Taquillas premium", "Clases ilimitadas", "Acceso a Spa"), true),
        Plan("Elite", "$59.99/mes", listOf("Todo lo de Pro", "Entrenador personal", "Nutricionista", "Parking gratuito"), false)
    )

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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.widthIn(max = 800.dp)
        ) {
            item {
                Text(
                    "Elige tu nivel de energía",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(plans) { plan ->
                PlanCard(plan)
            }
        }
    }
}

@Composable
fun PlanCard(plan: Plan) {
    val containerColor = if (plan.isRecommended) Color(0xFF39006F) else Color.White.copy(alpha = 0.1f)
    val borderColor = if (plan.isRecommended) Color(0xFFB39DDB) else Color.Transparent
    val borderWidth = if (plan.isRecommended) 2.dp else 0.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (plan.isRecommended) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFB39DDB), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RECOMENDADO", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(plan.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(plan.price, style = MaterialTheme.typography.titleLarge, color = Color(0xFFB39DDB))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                plan.features.forEach { feature ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFB39DDB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(feature, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: Subscribe logic */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.isRecommended) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.2f),
                    contentColor = if (plan.isRecommended) Color.Black else Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (plan.isRecommended) "Suscribirse Ahora" else "Elegir Plan")
            }
        }
    }
}

data class Plan(val name: String, val price: String, val features: List<String>, val isRecommended: Boolean)