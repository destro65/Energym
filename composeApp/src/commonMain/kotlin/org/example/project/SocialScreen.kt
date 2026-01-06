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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- DATA CLASSES ---
data class Award(
    val title: String,
    val winner: String,
    val icon: ImageVector,
    val userImage: @Composable () -> Unit,
    val description: String,
    val weight: String,
    val height: String,
    val socials: Map<String, String>,
    val date: String
)

data class SocialLink(val name: String, val icon: @Composable () -> Unit, val url: String)

// --- PANTALLA PRINCIPAL ---
@Composable
fun SocialScreen() {
    var selectedWinner by remember { mutableStateOf<Award?>(null) }

    val awards = listOf(
        Award("Premio a la Constancia", "Ana Pérez", Icons.Default.EmojiEvents,
            userImage = { UserImagePlaceholder(tint = Color(0xFFF48FB1)) },
            description = "Ana ha demostrado una asistencia casi perfecta durante todo el mes, ¡un ejemplo de disciplina!",
            weight = "62 kg", height = "1.65 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com"),
            date = "01/06/2024"
        ),
        Award("Premio a la Determinación", "Carlos Ruiz", Icons.Default.Star,
            userImage = { UserImagePlaceholder(tint = Color(0xFF81D4FA)) },
            description = "Carlos superó sus propios límites, aumentando su resistencia en un 20%. ¡Pura determinación!",
            weight = "78 kg", height = "1.80 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com"),
            date = "01/06/2024"
        ),
        Award("Premio a la Fuerza", "Luisa Gómez", Icons.Default.FitnessCenter,
            userImage = { UserImagePlaceholder(tint = Color(0xFFC5E1A5)) },
            description = "Luisa rompió el récord del gimnasio en levantamiento de peso muerto. ¡Una fuerza imparable!",
            weight = "70 kg", height = "1.72 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com"),
            date = "01/06/2024"
        )
    )

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
                        Text("Reconocimientos mensuales a nuestros atletas más destacados.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 24.dp))
                    }
                }
                items(awards) { award -> AwardCard(award, onClick = { selectedWinner = award }) }
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
            selectedWinner?.let { WinnerDetailView(award = it, onDismiss = { selectedWinner = null }) }
        }
    }
}

@Composable
fun AwardCard(award: Award, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = award.icon, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(award.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("Ganador: ${award.winner}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 44.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Fecha", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(award.date, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            award.userImage()
        }
    }
}

@Composable
fun WinnerDetailView(award: Award, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
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
                award.userImage()
                Spacer(modifier = Modifier.height(16.dp))
                Text(award.winner, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(award.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBadge(label = "Peso", value = award.weight, icon = Icons.Default.MonitorWeight)
                    StatBadge(label = "Altura", value = award.height, icon = Icons.Default.Height)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFB39DDB).copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(award.description, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Conecta con ${award.winner.split(" ")[0]}:", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    award.socials.forEach { (name, url) ->
                        IconButton(
                            onClick = { uriHandler.openUri(url) },
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            val icon = if (name == "Facebook") Icons.Default.Groups else Icons.Default.CameraAlt
                            Icon(icon, name, tint = Color(0xFFB39DDB), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
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
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp) // Añadido padding superior
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
             Text("El cerebro del proyecto", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
             Text("Harold Martinez", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
             Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
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
fun UserImagePlaceholder(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(
        modifier = modifier.size(80.dp).clip(CircleShape).background(tint.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "Foto de usuario", tint = tint, modifier = Modifier.size(40.dp))
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
