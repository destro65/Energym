package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun SocialScreen() {
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()
    val uriHandler = LocalUriHandler.current
    var userList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedWinner by remember { mutableStateOf<UserInfo?>(null) }
    var winnerAwardType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken() ?: ""
        userList = apiService.getUsers(token) 
        isLoading = false
    }

    val winners = userList.filter { 
        it.premio_constancia == 1 || it.premio_fuerza == 1 || it.premio_determinacion == 1 
    }

    val appSocialLinks = listOf(
        SocialLink("Instagram", { Icon(Icons.Default.Share, null, tint = Color.White) }, "https://instagram.com/energym"),
        SocialLink("Facebook", { Icon(Icons.Default.Share, null, tint = Color.White) }, "https://facebook.com/energym")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF39006F))))
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()){
                        Text("Salón de la Fama - Energym", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        Text("Reconocimientos a nuestros atletas más destacados.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 24.dp))
                    }
                }

                if (isLoading) {
                    item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFB39DDB)) } }
                } else if (winners.isEmpty()) {
                    item { Text("Aún no hay ganadores este mes.", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                } else {
                    items(winners) { user ->
                        if (user.premio_constancia == 1) AwardUserCard(user, "Premio a la Constancia", Icons.Default.EmojiEvents, Color(0xFFFFD700), user.fecha_premio_constancia ?: "") { selectedWinner = user; winnerAwardType = "Constancia" }
                        if (user.premio_fuerza == 1) AwardUserCard(user, "Premio a la Fuerza", Icons.Default.FitnessCenter, Color(0xFFE57373), user.fecha_premio_fuerza ?: "") { selectedWinner = user; winnerAwardType = "Fuerza" }
                        if (user.premio_determinacion == 1) AwardUserCard(user, "Premio a la Determinación", Icons.Default.Star, Color(0xFF64B5F6), user.fecha_premio_determinacion ?: "") { selectedWinner = user; winnerAwardType = "Determinación" }
                    }
                }

                item { 
                    Text("Síguenos", style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                }
                items(appSocialLinks) { link -> SocialCard(link) }
                item { DeveloperInfo() }
            }
        }

        AnimatedVisibility(
            visible = selectedWinner != null,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            selectedWinner?.let { user -> 
                WinnerDetailView(
                    user = user, 
                    awardType = winnerAwardType,
                    onDismiss = { selectedWinner = null }
                ) 
            }
        }
    }
}

@Composable
fun AwardUserCard(user: UserInfo, title: String, icon: ImageVector, iconColor: Color, date: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("Atleta: ${user.nombre_completo}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 44.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Desde", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(date, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                if (!user.foto_url.isNullOrEmpty()) {
                    KamelImage(resource = asyncPainterResource(data = user.foto_url!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun WinnerDetailView(user: UserInfo, awardType: String, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val awardTitle = when(awardType) {
        "Constancia" -> "Premio a la Constancia"
        "Fuerza" -> "Premio a la Fuerza"
        else -> "Premio a la Determinación"
    }
    val awardIcon = when(awardType) {
        "Constancia" -> Icons.Default.EmojiEvents
        "Fuerza" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Star
    }
    val awardColor = when(awardType) {
        "Constancia" -> Color(0xFFFFD700)
        "Fuerza" -> Color(0xFFE57373)
        else -> Color(0xFF64B5F6)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Cerrar", tint = Color.White) }
                }
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    if (!user.foto_url.isNullOrEmpty()) {
                        KamelImage(resource = asyncPainterResource(data = user.foto_url!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.nombre_completo, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(awardIcon, null, tint = awardColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(awardTitle, style = MaterialTheme.typography.titleMedium, color = awardColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBadge(label = "Peso", value = "${user.peso} kg", icon = Icons.Default.MonitorWeight)
                    StatBadge(label = "Altura", value = "${user.altura} cm", icon = Icons.Default.Height)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFB39DDB).copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Atleta ejemplar de Energym reconocido por su excelente desempeño y compromiso con el entrenamiento.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                
                // SECCIÓN DE REDES SOCIALES DINÁMICA
                val hasSocials = !user.fb_url.isNullOrEmpty() || !user.ig_url.isNullOrEmpty() || !user.tk_url.isNullOrEmpty() || !user.wa_num.isNullOrEmpty()
                
                if (hasSocials) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Conecta con el atleta:", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!user.fb_url.isNullOrEmpty()) {
                            SocialIconButton(icon = Icons.Default.Facebook, color = Color(0xFF1877F2)) {
                                safeOpenSocialUri(uriHandler, user.fb_url!!, "facebook")
                            }
                        }
                        if (!user.ig_url.isNullOrEmpty()) {
                            SocialIconButton(icon = Icons.Default.CameraAlt, color = Color(0xFFE4405F)) {
                                safeOpenSocialUri(uriHandler, user.ig_url!!, "instagram")
                            }
                        }
                        if (!user.tk_url.isNullOrEmpty()) {
                            SocialIconButton(icon = Icons.Default.MusicNote, color = Color.White) {
                                safeOpenSocialUri(uriHandler, user.tk_url!!, "tiktok")
                            }
                        }
                        if (!user.wa_num.isNullOrEmpty()) {
                            // Icono de teléfono/burbuja clásico para WhatsApp
                            SocialIconButton(icon = Icons.Default.Chat, color = Color(0xFF25D366)) {
                                val cleanNum = user.wa_num!!.filter { it.isDigit() }
                                safeOpenSocialUri(uriHandler, cleanNum, "whatsapp")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función para abrir URIs de forma segura y evitar crashes por URLs malformadas o protocolos faltantes
fun safeOpenSocialUri(uriHandler: UriHandler, input: String, platform: String) {
    if (input.isBlank()) return
    
    val formattedUrl = when (platform) {
        "whatsapp" -> "https://wa.me/${input.filter { it.isDigit() }}"
        "tiktok" -> {
            if (input.startsWith("http")) input
            else if (input.startsWith("@")) "https://www.tiktok.com/$input"
            else "https://www.tiktok.com/@$input"
        }
        "instagram" -> {
            if (input.startsWith("http")) input
            else "https://www.instagram.com/$input"
        }
        "facebook" -> {
            if (input.startsWith("http")) input
            else "https://www.facebook.com/$input"
        }
        else -> {
            if (input.startsWith("http")) input else "https://$input"
        }
    }
    
    try {
        uriHandler.openUri(formattedUrl)
    } catch (e: Exception) {
        println("Error abriendo social URI: ${e.message}")
    }
}

@Composable
fun SocialIconButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(color.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFB39DDB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DeveloperInfo() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
             Text("El cerebro del proyecto", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
             Text("Harold Martinez", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
             HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
             Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("0993347400", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
             }
             Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("harol.lady@hotmail.com", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
             }
        }
    }
}

@Composable
fun SocialCard(link: SocialLink) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(link.url) },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            link.icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(link.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

data class SocialLink(val name: String, val icon: @Composable () -> Unit, val url: String)
