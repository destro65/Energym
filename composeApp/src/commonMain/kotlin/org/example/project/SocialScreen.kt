package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen() {
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()
    var userList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filterMode by remember { mutableStateOf("Todos") }
    var selectedWinner by remember { mutableStateOf<UserInfo?>(null) }
    var winnerAwardType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken() ?: ""
        userList = apiService.getUsers(token)
        isLoading = false
    }

    val maleRanks = listOf("Debilucho", "Pecho de chifle", "Flaco", "Aceptable", "Medio Fuerte", "Fuerte", "Mamado", "Papiriqui")
    val femaleRanks = listOf("Iniciada", "Activa", "Estilizada", "Fit", "Atlética", "Potente", "Amazona", "Diosa")

    fun getRank(user: UserInfo): String {
        val ranks = if (user.genero == "Femenino") femaleRanks else maleRanks
        val isStrength = (user.record_peso >= user.record_tiempo)
        
        val targets = if (user.genero == "Femenino") {
            if (isStrength) listOf(5.0, 10.0, 18.0, 28.0, 40.0, 55.0, 75.0, 100.0) 
            else listOf(3.0, 8.0, 15.0, 25.0, 40.0, 55.0, 75.0, 100.0)
        } else {
            if (isStrength) listOf(10.0, 20.0, 35.0, 50.0, 65.0, 80.0, 90.0, 100.0)
            else listOf(5.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0, 100.0)
        }
        
        val record = if (isStrength) user.record_peso else user.record_tiempo.toDouble()
        var rankIdx = 0
        for (i in targets.indices) { if (record >= targets[i]) rankIdx = i else break }
        return ranks[rankIdx]
    }

    val filteredList = when(filterMode) {
        "Masculino" -> userList.filter { it.genero == "Masculino" }
        "Femenino" -> userList.filter { it.genero == "Femenino" }
        else -> userList
    }

    val winners = filteredList.filter { it.premio_constancia == 1 || it.premio_fuerza == 1 || it.premio_determinacion == 1 }
    val topElite = filteredList.sortedByDescending { it.record_peso + (it.record_tiempo * 2) }.take(5)

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF39006F))))) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Comunidad Energym", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Filtra y conoce a la élite del gimnasio", color = Color.Gray, fontSize = 14.sp)
                
                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", "Masculino", "Femenino").forEach { mode ->
                        FilterChip(selected = filterMode == mode, onClick = { filterMode = mode }, label = { Text(mode) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFB39DDB), selectedLabelColor = Color.Black, labelColor = Color.White))
                    }
                }
            }

            item { Text("Salón de la Fama", color = Color(0xFFB39DDB), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

            if (isLoading) {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFB39DDB)) } }
            } else if (winners.isEmpty()) {
                item { Text("No hay ganadores en esta categoría.", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            } else {
                items(winners) { user ->
                    if (user.premio_constancia == 1) AwardSocialCard(user, "Constancia", Icons.Default.EmojiEvents, Color(0xFFFFD700), getRank(user)) { selectedWinner = user; winnerAwardType = "Constancia" }
                    if (user.premio_fuerza == 1) AwardSocialCard(user, "Fuerza", Icons.Default.FitnessCenter, Color(0xFFE57373), getRank(user)) { selectedWinner = user; winnerAwardType = "Fuerza" }
                    if (user.premio_determinacion == 1) AwardSocialCard(user, "Determinación", Icons.Default.Star, Color(0xFF64B5F6), getRank(user)) { selectedWinner = user; winnerAwardType = "Determinación" }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)); Text("Top 5 Élite", color = Color(0xFFFFD700), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)), border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        topElite.forEachIndexed { index, user ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.width(30.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.nombre_completo, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(getRank(user), color = Color(0xFFB39DDB), fontSize = 11.sp)
                                }
                                Text("${user.record_peso.toInt()} kg / ${user.record_tiempo} min", color = Color.Gray, fontSize = 10.sp)
                            }
                            if (index < topElite.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
            item { DeveloperInfo() }
        }

        AnimatedVisibility(visible = selectedWinner != null, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
            selectedWinner?.let { WinnerDetailView(it, winnerAwardType) { selectedWinner = null } }
        }
    }
}

@Composable
fun AwardSocialCard(user: UserInfo, type: String, icon: ImageVector, color: Color, rank: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nombre_completo, color = Color.White, fontWeight = FontWeight.Bold)
                Text(rank, color = Color(0xFFB39DDB), fontSize = 12.sp)
            }
            Text("Premio $type", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WinnerDetailView(user: UserInfo, type: String, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth(0.85f).clickable(enabled = false) {}, colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    if (!user.foto_url.isNullOrEmpty()) KamelImage(resource = asyncPainterResource(getFullImageUrl(user.foto_url)!!), contentDescription = null, contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(60.dp).align(Alignment.Center))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.nombre_completo, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Premio a la $type", color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFB39DDB).copy(alpha = 0.1f))
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBadgeSocial(label = "Peso", value = "${user.peso} kg")
                    StatBadgeSocial(label = "Altura", value = "${user.altura} cm")
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!user.ig_url.isNullOrEmpty()) SocialIconSmall(Icons.Default.CameraAlt, Color(0xFFE4405F)) { uriHandler.openUri("https://instagram.com/${user.ig_url}") }
                    if (!user.fb_url.isNullOrEmpty()) SocialIconSmall(Icons.Default.Facebook, Color(0xFF1877F2)) { uriHandler.openUri("https://facebook.com/${user.fb_url}") }
                    if (!user.wa_num.isNullOrEmpty()) SocialIconSmall(Icons.Default.Chat, Color(0xFF25D366)) { uriHandler.openUri("https://wa.me/${user.wa_num}") }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) { Text("Cerrar", color = Color.Black) }
            }
        }
    }
}

@Composable
fun StatBadgeSocial(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun SocialIconSmall(icon: ImageVector, color: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun DeveloperInfo() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
             Text("Desarrollado por", color = Color.Gray, fontSize = 12.sp)
             Text("Harold Martinez", color = Color.White, fontWeight = FontWeight.Bold)
             Text("harol.lady@hotmail.com", color = Color.Gray, fontSize = 11.sp)
        }
    }
}
