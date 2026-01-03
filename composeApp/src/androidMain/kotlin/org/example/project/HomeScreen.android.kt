package org.example.project

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onChangeGoal: () -> Unit,
    userRank: String,
    nextRank: String,
    currentPoints: Int,
    nextRankPoints: Int,
    isSubscriptionActive: Boolean,
    subscriptionDaysRemaining: Int?,
    userGoal: String
) {
    val tabs = listOf("Dashboard", "Rutinas", "Tienda", "Social")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTabIndex = pagerState.currentPage
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRankInfoDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    BackHandler {
        showLogoutDialog = true
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres salir de Energym?", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF39006F),
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Salir", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }

    if (showRankInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRankInfoDialog = false },
            title = { Text("Niveles de Energym", color = Color.White) },
            text = {
                Column {
                    Text("Escala de poder:", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 8.dp))
                    val ranks = listOf("Debilucho", "Pecho de chifle", "Flaco", "Aceptable", "Medio Fuerte", "Fuerte", "Mamado", "Papiriqui")
                    ranks.forEachIndexed { index, rank ->
                        Text("${index + 1}. $rank", color = if (userRank == rank) Color(0xFFB39DDB) else Color.Gray)
                    }
                }
            },
            containerColor = Color(0xFF333333),
            confirmButton = {
                TextButton(onClick = { showRankInfoDialog = false }) {
                    Text("Entendido", color = Color(0xFFB39DDB))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Energym", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(R.drawable.logobn),
                                contentDescription = "Logo BN",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF39006F),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { uriHandler.openUri("https://wa.me/5930993347400") }) {
                            Icon(Icons.Default.Phone, contentDescription = "Ayuda WhatsApp")
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración y Perfil")
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF39006F),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFFB39DDB)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { 
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = title, maxLines = 1) },
                            selectedContentColor = Color(0xFFB39DDB),
                            unselectedContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF39006F))
                    )
                )
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DashboardContent(
                        userRank, nextRank, currentPoints, nextRankPoints, isSubscriptionActive, subscriptionDaysRemaining, userGoal, onChangeGoal,
                        onShowRankInfo = { showRankInfoDialog = true } 
                    )
                    1 -> RoutinesScreen(userGoal = userGoal, onBack = {})
                    2 -> ShopScreen()
                    3 -> SocialScreen()
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    userRank: String,
    nextRank: String,
    currentPoints: Int,
    nextRankPoints: Int,
    isSubscriptionActive: Boolean,
    subscriptionDaysRemaining: Int?,
    userGoal: String,
    onChangeGoal: () -> Unit,
    onShowRankInfo: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "¡Hola, Atleta!",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onChangeGoal() }
                    ) {
                        Text(
                            "Objetivo: $userGoal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFB39DDB)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Cambiar Objetivo", 
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de usuario",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) { 
                    IconButton(
                        onClick = onShowRankInfo,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Información de Rangos",
                            tint = Color.Gray
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 24.dp) 
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents, 
                                contentDescription = null, 
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Rango Actual", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(userRank, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$currentPoints XP", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB39DDB))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Siguiente: $nextRank", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("${nextRankPoints - currentPoints} XP restantes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = currentPoints.toFloat() / nextRankPoints.toFloat(),
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFB39DDB),
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSubscriptionActive) {
                                Text("Suscripción: ACTIVO", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                if (subscriptionDaysRemaining != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$subscriptionDaysRemaining días restantes", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            } else {
                                Text("Suscripción: INACTIVO", style = MaterialTheme.typography.titleLarge, color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Mi Progreso Semanal",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Objetivo Semanal", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = 0.7f,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFB39DDB),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("70% Completado", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItemHome("Peso", "75 kg")
                StatItemHome("IMC", "22.5") 
                StatItemHome("Racha", "5 días")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItemHome(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}