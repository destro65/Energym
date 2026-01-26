package org.example.project

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onChangeGoal: () -> Unit,
    onNavigateToAdminPanel: (() -> Unit)?,
    userRank: String,
    nextRank: String,
    currentPoints: Int,
    nextRankPoints: Int,
    isSubscriptionActive: Boolean,
    subscriptionDaysRemaining: Int?,
    userGoal: String,
    userData: UserInfo?
) {
    val tabs = listOf("Dashboard", "Rutinas", "Tienda", "Social")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTabIndex = pagerState.currentPage
    val scope = rememberCoroutineScope()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRankInfoDialog by remember { mutableStateOf(false) }

    val maleRanks = listOf("Debilucho", "Pecho de chifle", "Flaco", "Aceptable", "Medio Fuerte", "Fuerte", "Mamado", "Papiriqui")
    val femaleRanks = listOf("Iniciada", "Activa", "Estilizada", "Fit", "Atlética", "Potente", "Amazona", "Diosa")

    val gender = userData?.genero ?: "Masculino"
    val isMale = gender == "Masculino"
    val currentRanks = if (isMale) maleRanks else femaleRanks

    val xpData = remember(userData, userGoal) {
        val isStrengthGoal = userGoal == "Ganar Masa Muscular"
        
        // ESCALAS AJUSTADAS: Masculino 100 max / Femenino 80 max
        val strengthTargets = if (isMale) 
            listOf(10.0, 20.0, 35.0, 50.0, 65.0, 80.0, 90.0, 100.0) 
        else 
            listOf(5.0, 10.0, 18.0, 28.0, 40.0, 52.0, 65.0, 80.0)
            
        val cardioTargets = if (isMale) 
            listOf(5.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0, 100.0) 
        else 
            listOf(3.0, 8.0, 15.0, 25.0, 38.0, 52.0, 65.0, 80.0)

        val targets = if (isStrengthGoal) strengthTargets else cardioTargets
        val currentRecord = if (isStrengthGoal) (userData?.record_peso ?: 0.0) else (userData?.record_tiempo?.toDouble() ?: 0.0)
        
        var currentRankIndex = 0
        for (i in targets.indices) {
            if (currentRecord >= targets[i]) currentRankIndex = i
            else break
        }
        val nextTarget = if (currentRankIndex < targets.size - 1) targets[currentRankIndex + 1] else targets.last()
        
        object {
            val rankIndex = currentRankIndex
            val rankName = currentRanks[currentRankIndex]
            val nextRankName = if (currentRankIndex < currentRanks.size - 1) currentRanks[currentRankIndex + 1] else "Máximo Nivel"
            val overallProgress = (currentRecord / targets.last()).coerceIn(0.0, 1.0).toFloat()
            val record = currentRecord
            val unit = if (isStrengthGoal) "kg" else "min"
            val xpLabel = if (currentRankIndex < currentRanks.size - 1) "Faltan ${(nextTarget - currentRecord).toInt()} ${if (isStrengthGoal) "kg" else "min"} para $nextRankName" else "¡Has alcanzado la cima!"
            val targetsList = targets
        }
    }

    var showAwardNotification by remember { mutableStateOf(false) }
    LaunchedEffect(userData) {
        userData?.let {
            if (it.premio_constancia == 1 || it.premio_fuerza == 1 || it.premio_determinacion == 1) {
                showAwardNotification = true
            }
        }
    }

    if (showAwardNotification) {
        AlertDialog(
            onDismissRequest = { showAwardNotification = false },
            containerColor = Color(0xFF39006F),
            title = { Text("¡RECONOCIMIENTO!", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("¡Felicidades! Has sido reconocido en el Salón de la Fama por tu esfuerzo excepcional.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = { TextButton(onClick = { showAwardNotification = false }) { Text("¡GENIAL!", color = Color(0xFFB39DDB)) } }
        )
    }

    BackHandler { showLogoutDialog = true }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres salir de Energym?", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF220044),
            confirmButton = { TextButton(onClick = { showLogoutDialog = false; onLogout() }) { Text("Salir", color = Color(0xFFE57373)) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar", color = Color.White) } }
        )
    }

    if (showRankInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRankInfoDialog = false },
            title = { Text("Escala de Poder", color = Color.White) },
            text = {
                Column {
                    Text("Basado en tu objetivo de $userGoal", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    currentRanks.forEachIndexed { index, rank ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, null, tint = if (xpData.rankName == rank) Color(0xFFB39DDB) else Color.DarkGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${index + 1}. $rank", color = if (xpData.rankName == rank) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.6f))
                            }
                            Text(text = "${xpData.targetsList[index].toInt()} ${xpData.unit}", color = if (xpData.rankName == rank) Color(0xFFB39DDB) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            containerColor = Color(0xFF220044),
            confirmButton = { TextButton(onClick = { showRankInfoDialog = false }) { Text("Entendido", color = Color(0xFFB39DDB)) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).border(1.dp, Color(0xFFB39DDB).copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(R.drawable.logobn), contentDescription = null, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Energym", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF39006F), titleContentColor = Color.White, actionIconContentColor = Color.White),
                actions = {
                    if (onNavigateToAdminPanel != null) { IconButton(onClick = onNavigateToAdminPanel) { Icon(Icons.Default.AdminPanelSettings, "Admin", tint = Color(0xFFB39DDB)) } }
                    IconButton(onClick = onNavigateToProfile) { Icon(Icons.Default.Settings, "Perfil") }
                    IconButton(onClick = { showLogoutDialog = true }) { Icon(Icons.Default.Logout, "Salir") }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF39006F)) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = { Icon(when(index) { 0 -> Icons.Default.Dashboard; 1 -> Icons.Default.FitnessCenter; 2 -> Icons.Default.Storefront; else -> Icons.Default.Groups }, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFB39DDB), unselectedIconColor = Color.White.copy(alpha = 0.6f), selectedTextColor = Color(0xFFB39DDB), unselectedTextColor = Color.White.copy(alpha = 0.6f), indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF39006F))))) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> DashboardContent(
                        rankName = xpData.rankName,
                        rankIndex = xpData.rankIndex,
                        nextRank = xpData.nextRankName,
                        progress = xpData.overallProgress,
                        xpLabel = xpData.xpLabel,
                        recordValue = "${xpData.record} ${xpData.unit}",
                        isSubscriptionActive = isSubscriptionActive,
                        subscriptionDaysRemaining = subscriptionDaysRemaining,
                        userGoal = userGoal,
                        onChangeGoal = onChangeGoal,
                        onShowRankInfo = { showRankInfoDialog = true },
                        userData = userData
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
    rankName: String, rankIndex: Int, nextRank: String, progress: Float, xpLabel: String, recordValue: String,
    isSubscriptionActive: Boolean, subscriptionDaysRemaining: Int?, userGoal: String,
    onChangeGoal: () -> Unit, onShowRankInfo: () -> Unit, userData: UserInfo?
) {
    val weight = userData?.peso?.toDoubleOrNull() ?: 0.0
    val heightCm = userData?.altura?.toDoubleOrNull() ?: 0.0
    val age = userData?.edad ?: 0
    val gender = userData?.genero ?: "Masculino"
    val imc = if (heightCm > 0) weight / (heightCm / 100).pow(2) else 0.0
    val bodyFat = if (imc > 0 && age > 0) (1.20 * imc) + (0.23 * age) - (10.8 * (if(gender == "Masculino") 1 else 0)) - 5.4 else 0.0
    val muscleMassEstimate = weight - (weight * (bodyFat / 100))

    var infoDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }
    if (infoDialogData != null) {
        AlertDialog(onDismissRequest = { infoDialogData = null }, title = { Text(infoDialogData!!.first, color = Color.White, fontWeight = FontWeight.Bold) }, text = { Text(infoDialogData!!.second, color = Color.White.copy(alpha = 0.8f)) }, containerColor = Color(0xFF220044), confirmButton = { TextButton(onClick = { infoDialogData = null }) { Text("Cerrar", color = Color(0xFFB39DDB)) } })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Bienvenido,", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                Text(userData?.nombre_completo?.split(" ")?.firstOrNull() ?: "Atleta", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Row(modifier = Modifier.clickable { onChangeGoal() }, verticalAlignment = Alignment.CenterVertically) {
                    Text(userGoal, color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Edit, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(14.dp).padding(start = 4.dp))
                }
            }
            Box(modifier = Modifier.size(70.dp).border(2.dp, Color(0xFFB39DDB), CircleShape).padding(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                if (!userData?.foto_url.isNullOrEmpty()) KamelImage(resource = asyncPainterResource(getFullImageUrl(userData!!.foto_url)!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        Text("Tu Estado Físico", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhysicalCard(Modifier.weight(1f), "IMC", String.format("%.1f", imc), if (imc < 25) Color(0xFF81C784) else Color(0xFFFFD54F), Icons.Default.AccessibilityNew) { infoDialogData = "IMC" to "Tu Índice de Masa Corporal actual es ${String.format("%.1f", imc)}." }
            PhysicalCard(Modifier.weight(1f), "% Grasa", String.format("%.1f%%", bodyFat), Color(0xFF64B5F6), Icons.Default.Percent) { infoDialogData = "Grasa" to "Tu porcentaje de grasa estimado es ${String.format("%.1f%%", bodyFat)}." }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhysicalCard(Modifier.weight(1f), "Masa Muscular", String.format("%.1f kg", muscleMassEstimate), Color(0xFFB39DDB), Icons.Default.MonitorWeight) { infoDialogData = "Masa" to "Tu masa muscular estimada es ${String.format("%.1f kg", muscleMassEstimate)}." }
            PhysicalCard(Modifier.weight(1f), "Rango", rankName, Color(0xFFFFD700), Icons.Default.EmojiEvents, chartType = "bar", currentLevel = rankIndex) { onShowRankInfo() }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Récord Actual", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(recordValue, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color(0xFFB39DDB), trackColor = Color.White.copy(alpha = 0.1f))
                Text(xpLabel, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (isSubscriptionActive) {
            Spacer(modifier = Modifier.height(32.dp))
            val subColor = if ((subscriptionDaysRemaining ?: 0) <= 3) Color(0xFFE57373) else Color(0xFF81C784)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = subColor.copy(alpha = 0.1f)), border = BorderStroke(1.dp, subColor.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, tint = subColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Suscripción activa: $subscriptionDaysRemaining días restantes", color = Color.White, fontSize = 13.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PhysicalCard(modifier: Modifier, title: String, value: String, accentColor: Color, icon: ImageVector, chartType: String = "line", currentLevel: Int = 0, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), shape = RoundedCornerShape(20.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (chartType == "line") Sparkline(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp), color = accentColor)
            else LevelBarChart(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(50.dp).padding(horizontal = 12.dp, vertical = 8.dp), color = accentColor, currentLevel = currentLevel)
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Text(title, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun Sparkline(modifier: Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val width = size.width; val height = size.height; val points = listOf(0.8f, 0.6f, 0.9f, 0.5f, 0.7f, 0.4f, 0.2f)
        val path = Path(); val xStep = width / (points.size - 1)
        points.forEachIndexed { index, y ->
            val xPos = index * xStep; val yPos = height * y
            if (index == 0) path.moveTo(xPos, yPos) else path.lineTo(xPos, yPos)
        }
        drawPath(path = path, color = color.copy(alpha = 0.3f), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun LevelBarChart(modifier: Modifier, color: Color, currentLevel: Int) {
    Canvas(modifier = modifier) {
        val numBars = 8; val spacing = 4.dp.toPx(); val barWidth = (size.width - (spacing * (numBars - 1))) / numBars
        for (i in 0 until numBars) {
            val isActive = i <= currentLevel; val barHeight = size.height * ((i + 1).toFloat() / numBars)
            val x = i * (barWidth + spacing); val y = size.height - barHeight
            drawRect(color = if (isActive) color else color.copy(alpha = 0.1f), topLeft = Offset(x, y), size = Size(barWidth, barHeight))
        }
    }
}
