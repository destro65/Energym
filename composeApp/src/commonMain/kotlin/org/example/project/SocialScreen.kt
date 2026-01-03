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

// Data class para los premios y ganadores actualizada con peso y altura
data class Award(
    val title: String, 
    val winner: String, 
    val icon: ImageVector, 
    val userImage: @Composable () -> Unit, 
    val description: String, 
    val weight: String,
    val height: String,
    val socials: Map<String, String>
)

data class SocialLink(val name: String, val icon: @Composable () -> Unit, val url: String)

@Composable
fun SocialScreen() {
    var selectedWinner by remember { mutableStateOf<Award?>(null) }

    // Datos actualizados con peso, altura y redes (Facebook e Instagram)
    val awards = listOf(
        Award("Premio a la Constancia", "Ana Pérez", Icons.Default.EmojiEvents, 
            userImage = { UserImagePlaceholder(tint = Color(0xFFF48FB1)) },
            description = "Ana ha demostrado una asistencia casi perfecta durante todo el mes, ¡un ejemplo de disciplina!", 
            weight = "62 kg", height = "1.65 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com")
        ),
        Award("Premio a la Determinación", "Carlos Ruiz", Icons.Default.Star, 
            userImage = { UserImagePlaceholder(tint = Color(0xFF81D4FA)) },
            description = "Carlos superó sus propios límites, aumentando su resistencia en un 20%. ¡Pura determinación!", 
            weight = "78 kg", height = "1.80 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com")
        ),
        Award("Premio a la Fuerza", "Luisa Gómez", Icons.Default.FitnessCenter, 
            userImage = { UserImagePlaceholder(tint = Color(0xFFC5E1A5)) },
            description = "Luisa rompió el récord del gimnasio en levantamiento de peso muerto. ¡Una fuerza imparable!", 
            weight = "70 kg", height = "1.72 m",
            socials = mapOf("Instagram" to "https://instagram.com", "Facebook" to "https://facebook.com")
        )
    )

    val appSocialLinks = listOf(
        SocialLink("Instagram", { Icon(Icons.Default.Share, null, tint = Color.White) }, "https://instagram.com/energym"),
        SocialLink("Facebook", { Icon(Icons.Default.Share, null, tint = Color.White) }, "https://facebook.com/energym")
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- CAPA 1: Contenido Principal ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF39006F))
                    )
                )
        ) {
            // Lista principal con scroll
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        "Salón de la Fama - Energym",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "Reconocimientos mensuales a nuestros atletas más destacados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp).align(Alignment.CenterHorizontally)
                    )
                }

                items(awards) { award ->
                    AwardCard(award, onClick = { selectedWinner = award })
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Síguenos para futuros sorteos",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(appSocialLinks) { link ->
                    SocialCard(link)
                }

                item {
                    DeveloperInfo()
                }
            }
        }

        // --- CAPA 2: Detalle del Ganador (Overlay) ---
        AnimatedVisibility(
            visible = selectedWinner != null,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            selectedWinner?.let {
                WinnerDetailView(award = it, onDismiss = { selectedWinner = null })
            }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = award.icon, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(award.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("Ganador: ${award.winner}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 44.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            award.userImage()
        }
    }
}

@Composable
fun WinnerDetailView(award: Award, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() }, // Click en el fondo para cerrar
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                }

                award.userImage()
                Spacer(modifier = Modifier.height(16.dp))
                Text(award.winner, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(award.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFD700))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sección de Estadísticas Físicas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    award.socials.forEach { (name, url) ->
                        IconButton(
                            onClick = { uriHandler.openUri(url) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            // Usamos iconos genéricos de share/person para simular FB/Insta
                            val icon = if (name == "Facebook") Icons.Default.Facebook else Icons.Default.CameraAlt 
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DeveloperInfo() {
    Spacer(modifier = Modifier.height(32.dp))
    Card(
        elevation = CardDefaults.cardElevation(12.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A148C), Color(0xFF7B1FA2), Color(0xFFAB47BC)) // Gradiente púrpura vibrante
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Dorado
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CEREBRO DEL PROYECTO",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD700),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Programador Z",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                Divider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(100.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("harol.lady@hotmail.com", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("0993347400", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun UserImagePlaceholder(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(
        modifier = modifier.size(50.dp).clip(CircleShape).background(tint.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, "Foto de usuario", tint = tint, modifier = Modifier.size(30.dp))
    }
}

@Composable
fun SocialCard(link: SocialLink) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(link.url) },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            link.icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(link.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}