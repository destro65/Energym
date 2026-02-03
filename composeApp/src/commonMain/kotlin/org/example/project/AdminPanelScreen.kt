package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
expect fun ImagePicker(
    modifier: Modifier = Modifier,
    label: String = "Seleccionar Foto",
    onImageSelected: (ByteArray) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminPanelScreen(adminName: String, onLogout: () -> Unit, onSwitchToUserView: () -> Unit) {
    val tabs = listOf("Dashboard", "Usuarios", "Tienda")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedUser by remember { mutableStateOf<UserInfo?>(null) }
    var userList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var articulosList by remember { mutableStateOf<List<Articulo>>(emptyList()) }
    var historyList by remember { mutableStateOf<List<SubscriptionHistory>>(emptyList()) }
    
    var isAdminLoading by remember { mutableStateOf(true) }
    var isArticulosLoading by remember { mutableStateOf(true) }

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddArticuloDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    fun refreshUsers() {
        scope.launch {
            isAdminLoading = true
            val token = sessionManager.getToken() ?: ""
            val users = apiService.getUsers(token)
            val history = apiService.getSubscriptionHistory(token)
            userList = users
            historyList = history
            isAdminLoading = false
        }
    }

    fun refreshArticulos() {
        scope.launch {
            isArticulosLoading = true
            articulosList = apiService.getArticulos()
            isArticulosLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshUsers(); refreshArticulos() }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = Color(0xFF220044),
            title = { Text("Cerrar Sesión", color = Color.White) },
            text = { Text("¿Deseas salir del panel de administración?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = { TextButton(onClick = { showLogoutConfirm = false; onLogout() }) { Text("SALIR", color = Color(0xFFE57373)) } },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("CANCELAR", color = Color.White) } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Panel Administrador", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF39006F), titleContentColor = Color.White, actionIconContentColor = Color.White),
                    actions = {
                        IconButton(onClick = onSwitchToUserView) { Icon(Icons.Default.SwitchAccount, "Vista Usuario", tint = Color.White) }
                        IconButton(onClick = { showLogoutConfirm = true }) { Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.White) }
                    }
                )
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color(0xFF39006F),
                    contentColor = Color.White,
                    indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(pagerState.currentPage, matchContentSize = true), color = Color(0xFFB39DDB)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.animateScrollToPage(index) } }, text = { Text(text = title) })
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF39006F))))) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> AdminDashboardTab(adminName, userList, historyList)
                    1 -> UserManagementTab(userList, isAdminLoading, { selectedUser = it }, { showAddUserDialog = true })
                    2 -> ShopManagementTab(articulosList, isArticulosLoading, {  }, { showAddArticuloDialog = true })
                }
            }
            
            AnimatedVisibility(visible = selectedUser != null) {
                selectedUser?.let { user ->
                    UserDetailView(
                        user = user, 
                        apiService = apiService,
                        token = sessionManager.getToken() ?: "",
                        adminName = adminName,
                        onDismiss = { selectedUser = null; refreshUsers() }
                    )
                }
            }
        }
    }

    if (showAddUserDialog) {
        CreateUserDialog(apiService, snackbarHostState, { showAddUserDialog = false }, { 
            refreshUsers()
            showAddUserDialog = false
        })
    }

    if (showAddArticuloDialog) {
        AddArticuloDialog({ showAddArticuloDialog = false }, { 
            refreshArticulos()
            showAddArticuloDialog = false
        })
    }
}

@Composable
fun AdminDashboardTab(adminName: String, userList: List<UserInfo>, historyList: List<SubscriptionHistory>) {
    val expiringUsers = userList.filter { it.rol == "normal" && it.dias_suscripcion in 1..4 }
    val usersLastMonth = remember(userList) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        userList.count { user ->
            try {
                val regDate = user.fecha_registro?.split(" ")?.get(0)?.let { LocalDate.parse(it) }
                if (regDate != null) regDate.daysUntil(now) <= 30 else false
            } catch (_: Exception) { false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("¡Bienvenido, $adminName!", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
             Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Estadística de Usuarios", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItemAdmin(Icons.Default.Group, "Total", userList.size.toString())
                    StatItemAdmin(Icons.Default.CalendarMonth, "Último Mes", usersLastMonth.toString())
                    StatItemAdmin(Icons.Default.AdminPanelSettings, "Admins", userList.count { it.rol == "admin" }.toString())
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Crecimiento de Usuarios", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Box(modifier = Modifier.padding(16.dp)) { UserGrowthLineChart(userList) }
        }
        if (expiringUsers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Alertas de Suscripción", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF39006F).copy(alpha = 0.2f)), border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFFB39DDB))
                        Spacer(modifier = Modifier.width(8.dp)); Text("Suscripciones por vencer (< 5 días)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    expiringUsers.forEach { user -> Text("• ${user.nombre_completo}: ${user.dias_suscripcion} días restantes", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp) }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Historial de Recargas", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (historyList.isEmpty()) { Text("No hay registros.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) } 
                else {
                    historyList.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.nombre_usuario, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Por: ${item.nombre_admin}", color = Color(0xFFB39DDB), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("+${item.dias_agregados} días", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.fecha_accion.split(" ")[0], color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        if (item != historyList.last()) HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserGrowthLineChart(userList: List<UserInfo>) {
    val spanishMonths = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val monthsData = remember(userList) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        (0..5).reversed().map { monthsBack ->
            val targetDate = now.minus(monthsBack, DateTimeUnit.MONTH)
            val monthIdx = targetDate.month
            val year = targetDate.year
            val count = userList.count { user ->
                try {
                    val regDate = user.fecha_registro?.split(" ")?.get(0)?.let { LocalDate.parse(it) }
                    if (regDate != null) regDate.month == monthIdx && regDate.year == year else false
                } catch (_: Exception) { false }
            }
            spanishMonths[monthIdx.number - 1] to count.toFloat()
        }
    }
    val maxCount = (monthsData.maxByOrNull { it.second }?.second ?: 0f).coerceAtLeast(1f)
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxHeight().padding(end = 8.dp).width(24.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
            Text(maxCount.toInt().toString(), color = Color.Gray, fontSize = 10.sp)
            Text("0", color = Color.Gray, fontSize = 10.sp); Spacer(modifier = Modifier.height(20.dp))
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val width = size.width; val height = size.height; val spacing = width / (monthsData.size - 1)
                val points = monthsData.mapIndexed { index, data -> Offset(index * spacing, height - (data.second / maxCount) * height * 0.8f) }
                if (points.isNotEmpty()) {
                    val strokePath = Path().apply { moveTo(points.first().x, points.first().y); points.forEach { lineTo(it.x, it.y) } }
                    drawPath(path = strokePath, color = Color(0xFFB39DDB), style = Stroke(width = 3.dp.toPx()))
                    points.forEach { point -> drawCircle(color = Color.White, radius = 4.dp.toPx(), center = point) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                monthsData.forEach { Text(it.first, color = Color.Gray, fontSize = 10.sp) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementTab(userList: List<UserInfo>, isLoading: Boolean, onUserClick: (UserInfo) -> Unit, onAddNew: () -> Unit) {
    var sortMode by remember { mutableStateOf("Suscripcion") }
    
    val sortedList = remember(userList, sortMode) {
        when(sortMode) {
            "Suscripcion" -> userList.sortedBy { it.dias_suscripcion }
            "Fecha" -> userList.sortedByDescending { it.fecha_registro }
            else -> userList.sortedBy { it.nombre_completo }
        }
    }

    val normalUsers = sortedList.filter { it.rol == "normal" }
    val adminUsers = sortedList.filter { it.rol == "admin" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Usuarios", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Button(onClick = onAddNew, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) {
                Icon(Icons.Default.PersonAdd, null, tint = Color.Black); Text("Nuevo", color = Color.Black)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = sortMode == "Suscripcion",
                onClick = { sortMode = "Suscripcion" },
                label = { Text("Días (Asc)") },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color(0xFFB39DDB))
            )
            FilterChip(
                selected = sortMode == "Nombre",
                onClick = { sortMode = "Nombre" },
                label = { Text("Nombre") },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color(0xFFB39DDB))
            )
            FilterChip(
                selected = sortMode == "Fecha",
                onClick = { sortMode = "Fecha" },
                label = { Text("Recientes") },
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedContainerColor = Color(0xFFB39DDB))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } } 
        else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                item { Text("Atletas", color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold) }
                items(normalUsers) { user -> UserListItem(user = user, onClick = { onUserClick(user) }) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Text("Administradores", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) }
                items(adminUsers) { user -> UserListItem(user = user, onClick = { onUserClick(user) }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(apiService: ApiService, snackbarHostState: SnackbarHostState, onDismiss: () -> Unit, onUserCreated: () -> Unit) {
    var nombre by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }; var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var fechaNac by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB39DDB), unfocusedBorderColor = Color.Gray, focusedLabelColor = Color(0xFFB39DDB), unfocusedLabelColor = Color.Gray)

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF220044), title = { Text("Nuevo Usuario", color = Color.White) },
        properties = DialogProperties(dismissOnClickOutside = false),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, textStyle = TextStyle(color = Color.White), colors = fieldColors, singleLine = true)
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email") }, 
                    textStyle = TextStyle(color = Color.White), 
                    colors = fieldColors, 
                    singleLine = true,
                    isError = email.isNotEmpty() && !email.contains("@")
                )
                if (email.isNotEmpty() && !email.contains("@")) {
                    Text("Email inválido (falta @)", color = Color.Red, fontSize = 10.sp)
                }
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), textStyle = TextStyle(color = Color.White), colors = fieldColors, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = peso, onValueChange = { if(it.all { c -> c.isDigit() }) peso = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f), textStyle = TextStyle(color = Color.White), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = altura, onValueChange = { if(it.all { c -> c.isDigit() }) altura = it }, label = { Text("Altura (cm)") }, modifier = Modifier.weight(1f), textStyle = TextStyle(color = Color.White), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(
                    value = fechaNac, 
                    onValueChange = { tfv -> 
                        val input = tfv.text
                        val digits = input.filter { it.isDigit() }.take(8)
                        val formatted = buildString {
                            if (digits.length >= 1) append(digits.substring(0, minOf(digits.length, 4)))
                            if (digits.length >= 5) {
                                append("-")
                                val m = digits.substring(4, minOf(digits.length, 6))
                                val mInt = m.toIntOrNull() ?: 0
                                if (m.length == 2 && mInt > 12) append("12")
                                else if (m.length == 2 && mInt == 0) append("01")
                                else append(m)
                            }
                            if (digits.length >= 7) {
                                append("-")
                                val d = digits.substring(6, minOf(digits.length, 8))
                                val dInt = d.toIntOrNull() ?: 0
                                if (d.length == 2 && dInt > 31) append("31")
                                else if (d.length == 2 && dInt == 0) append("01")
                                else append(d)
                            }
                        }
                        
                        var newSelection = tfv.selection.start
                        if (tfv.text.length < formatted.length) {
                           if (newSelection == 5 || newSelection == 8) newSelection++
                        }
                        
                        fechaNac = TextFieldValue(formatted, TextRange(newSelection.coerceAtMost(formatted.length)))
                    }, 
                    label = { Text("F. Nacimiento (AAAA-MM-DD)") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    textStyle = TextStyle(color = Color.White), 
                    colors = fieldColors, 
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.isBlank() || email.isBlank() || pass.isBlank()) { scope.launch { snackbarHostState.showSnackbar("Completa los campos") }; return@Button }
                if (!email.contains("@")) { scope.launch { snackbarHostState.showSnackbar("Email inválido") }; return@Button }
                scope.launch {
                    val res = apiService.register(nombre, email, pass, "normal", peso, altura, fechaNac.text, "Masculino")
                    if (res.error == null) onUserCreated() else res.error.let { snackbarHostState.showSnackbar(it) }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) { Text("Guardar", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.White) } }
    )
}

@Composable
fun UserListItem(user: UserInfo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                val fullUrl = getFullImageUrl(user.foto_url)
                if (fullUrl != null) KamelImage(resource = asyncPainterResource(data = fullUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.premio_constancia == 1) Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp).padding(end = 4.dp))
                    if (user.premio_fuerza == 1) Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFFE57373), modifier = Modifier.size(18.dp).padding(end = 4.dp))
                    if (user.premio_determinacion == 1) Icon(Icons.Default.Star, null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp).padding(end = 4.dp))
                    Text(user.nombre_completo, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("Suscripción: ${user.dias_suscripcion} días", color = if (user.dias_suscripcion < 5) Color(0xFFE57373) else Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailView(user: UserInfo, apiService: ApiService, token: String, adminName: String, onDismiss: () -> Unit) {
    var tempNombre by remember { mutableStateOf(user.nombre_completo) }; var tempPass by remember { mutableStateOf("") }
    var tempSuscripcionAdd by remember { mutableStateOf("") }; var premioConstancia by remember { mutableStateOf(user.premio_constancia == 1) }
    var premioFuerza by remember { mutableStateOf(user.premio_fuerza == 1) }; var premioDeterminacion by remember { mutableStateOf(user.premio_determinacion == 1) }
    var tempRecordWeight by remember { mutableStateOf(user.record_peso.toString()) }; var tempRecordTime by remember { mutableStateOf(user.record_tiempo.toString()) }
    var tempWeight by remember { mutableStateOf(user.peso ?: "") }; var tempHeight by remember { mutableStateOf(user.altura ?: "") }
    var tempGender by remember { mutableStateOf(user.genero ?: "Masculino") }
    var tempFechaNac by remember { mutableStateOf(TextFieldValue(user.fecha_nacimiento ?: "")) }
    var tempRol by remember { mutableStateOf(user.rol) }
    
    val ageCalculated = remember(tempFechaNac.text) { org.example.project.calcularEdad(tempFechaNac.text) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }; var isUpdating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White, 
        unfocusedTextColor = Color.White, 
        disabledTextColor = Color.White,
        focusedBorderColor = Color(0xFFB39DDB), 
        unfocusedBorderColor = Color.Gray, 
        focusedLabelColor = Color(0xFFB39DDB), 
        unfocusedLabelColor = Color.Gray
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f).clickable(enabled = false) {}, colors = CardDefaults.cardColors(containerColor = Color(0xFF220044))) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Editar Usuario", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }
                HorizontalDivider(color = Color(0xFFB39DDB), modifier = Modifier.padding(vertical = 8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                    
                    OutlinedTextField(value = user.email, onValueChange = {}, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, readOnly = true, enabled = true, singleLine = true)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempFechaNac, 
                            onValueChange = { tfv -> 
                                val input = tfv.text
                                val clean = input.replace("-", "").filter { it.isDigit() }.take(8)
                                val formatted = buildString {
                                    if (clean.isNotEmpty()) append(clean.substring(0, minOf(clean.length, 4)))
                                    if (clean.length >= 5) {
                                        append("-")
                                        val m = clean.substring(4, minOf(clean.length, 6))
                                        val mInt = m.toIntOrNull() ?: 0
                                        if (m.length == 2 && mInt > 12) append("12")
                                        else if (m.length == 2 && mInt == 0) append("01")
                                        else append(m)
                                    }
                                    if (clean.length >= 7) {
                                        append("-")
                                        val d = clean.substring(6, minOf(clean.length, 8))
                                        val dInt = d.toIntOrNull() ?: 0
                                        if (d.length == 2 && dInt > 31) append("31")
                                        else if (d.length == 2 && dInt == 0) append("01")
                                        else append(d)
                                    }
                                }
                                
                                var newSelection = tfv.selection.start
                                if (tfv.text.length < formatted.length) {
                                   if (newSelection == 5 || newSelection == 8) newSelection++
                                }

                                tempFechaNac = TextFieldValue(formatted, TextRange(newSelection.coerceAtMost(formatted.length)))
                            }, 
                            label = { Text("F. Nac (AAAA-MM-DD)") }, 
                            modifier = Modifier.weight(1.5f), 
                            colors = fieldColors, 
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(value = ageCalculated, onValueChange = {}, label = { Text("Edad") }, modifier = Modifier.weight(0.5f), colors = fieldColors, readOnly = true, enabled = true, singleLine = true)
                    }

                    OutlinedTextField(value = tempPass, onValueChange = { tempPass = it }, label = { Text("Nueva Contraseña (vacío para no cambiar)") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    
                    ExpandableSection("Avanzado (Rol del Usuario)") {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempRol = "normal" }) {
                                RadioButton(selected = tempRol == "normal", onClick = { tempRol = "normal" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB)))
                                Text("Usuario Normal", color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempRol = "admin" }) {
                                RadioButton(selected = tempRol == "admin", onClick = { tempRol = "admin" }, colors = RadioButtonDefaults.colors(selectedColor = Color.Red))
                                Text("Administrador", color = Color.White)
                            }
                        }
                    }

                    Text("Género", color = Color.White, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempGender = "Masculino" }) { RadioButton(selected = tempGender == "Masculino", onClick = { tempGender = "Masculino" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Masculino", color = Color.White) }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempGender = "Femenino" }) { RadioButton(selected = tempGender == "Femenino", onClick = { tempGender = "Femenino" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Femenino", color = Color.White) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedTextField(value = tempWeight, onValueChange = { if (it.all { char -> char.isDigit() }) tempWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = tempHeight, onValueChange = { if (it.all { char -> char.isDigit() }) tempHeight = it }, label = { Text("Altura (cm)") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    ExpandableSection("Retos y Récords") {
                        OutlinedTextField(value = tempRecordWeight, onValueChange = { if(it.all{c -> c.isDigit() || c == '.'}) tempRecordWeight = it }, label = { Text("Máximo Peso (kg)") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = tempRecordTime, onValueChange = { if(it.all{c -> c.isDigit()}) tempRecordTime = it }, label = { Text("Carrera (min)") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    ExpandableSection("Salón de la Fama") {
                        AwardSwitchRow("Premio Constancia", premioConstancia, user.fecha_premio_constancia) { premioConstancia = it }
                        AwardSwitchRow("Premio Fuerza", premioFuerza, user.fecha_premio_fuerza) { premioFuerza = it }
                        AwardSwitchRow("Premio Determinación", premioDeterminacion, user.fecha_premio_determinacion) { premioDeterminacion = it }
                    }
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text("Suscripción: ${user.dias_suscripcion} días", color = Color.White, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = tempSuscripcionAdd, onValueChange = { if(it.all{c -> c.isDigit()}) tempSuscripcionAdd = it }, label = { Text("Añadir Días") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Button(onClick = {
                        isUpdating = true
                        scope.launch {
                            val finalSub = user.dias_suscripcion + (tempSuscripcionAdd.toIntOrNull() ?: 0)
                            val updated = user.copy(nombre_completo = tempNombre, peso = tempWeight, altura = tempHeight, dias_suscripcion = finalSub, record_peso = tempRecordWeight.toDoubleOrNull() ?: 0.0, record_tiempo = tempRecordTime.toIntOrNull() ?: 0, premio_constancia = if(premioConstancia) 1 else 0, premio_fuerza = if(premioFuerza) 1 else 0, premio_determinacion = if(premioDeterminacion) 1 else 0, genero = tempGender, fecha_nacimiento = tempFechaNac.text, rol = tempRol)
                            if (apiService.updateAdminUser(token, updated, adminName, tempPass)) onDismiss()
                            isUpdating = false
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) { 
                        if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Guardar Cambios", color = Color.Black) 
                    }
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = false }, containerColor = Color(0xFF220044), title = { Text("Confirmar Eliminación", color = Color.White) }, text = { Text("¿Eliminar a ${user.nombre_completo} definitivamente?", color = Color.White.copy(alpha = 0.8f)) }, 
            confirmButton = { TextButton(onClick = { scope.launch { if(apiService.deleteUser(token, user.id_usuario)) onDismiss() } }) { Text("ELIMINAR", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCELAR", color = Color.White) } }
        )
    }
}

@Composable fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.White)
        }
        if (expanded) { Column(modifier = Modifier.padding(8.dp)) { content() } }
    }
}

@Composable fun AwardSwitchRow(label: String, checked: Boolean, date: String?, onCheckedChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFB39DDB), checkedTrackColor = Color(0xFF39006F)))
        }
        if (checked && !date.isNullOrEmpty()) { Text("Otorgado: $date", color = Color.Gray, fontSize = 10.sp) }
    }
}

@Composable fun ArticuloListItem(articulo: Articulo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                val fullUrl = getFullImageUrl(articulo.foto_url)
                if (fullUrl != null) KamelImage(resource = asyncPainterResource(data = fullUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.ShoppingBag, null, tint = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(articulo.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${articulo.precio} USD - Stock: ${articulo.stock}", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopManagementTab(articulos: List<Articulo>, isLoading: Boolean, onArticuloClick: (Articulo) -> Unit, onAddNew: () -> Unit) {
    val prods = articulos.filter { it.categoria == "Articulo" || it.categoria == "Suplemento" }
    val vest = articulos.filter { it.categoria == "Vestimenta" }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Tienda", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Button(onClick = onAddNew, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) {
                Icon(Icons.Default.Add, null, tint = Color.Black); Text("Nuevo", color = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } } 
        else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                if (prods.isNotEmpty()) {
                    item { Text("Artículos", color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold) }
                    items(prods) { articulo -> ArticuloListItem(articulo, onClick = { onArticuloClick(articulo) }) }
                }
                if (vest.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)); Text("Vestimenta", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) }
                    items(vest) { articulo -> ArticuloListItem(articulo, onClick = { onArticuloClick(articulo) }) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticuloDetailView(articulo: Articulo, apiService: ApiService, onRefresh: () -> Unit, onDismiss: () -> Unit) {
    var tempNombre by remember { mutableStateOf(articulo.nombre) }; var tempDesc by remember { mutableStateOf(articulo.descripcion ?: "") }
    var tempPrecio by remember { mutableStateOf(articulo.precio.toString()) }; var tempStock by remember { mutableStateOf(articulo.stock.toString()) }
    var tempCat by remember { mutableStateOf(articulo.categoria) }; var mainPhoto by remember { mutableStateOf(articulo.foto_url) }
    val allPhotos = remember(articulo) { (listOf(articulo.foto_url) + (articulo.fotos_adicionales ?: emptyList())).filterNotNull().distinct() }
    val fotosABorrar = remember { mutableStateListOf<String>() }; val newPhotos = remember { mutableStateListOf<ByteArray>() }
    var showDelConfirm by remember { mutableStateOf(false) }; var isUpdating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB39DDB), unfocusedBorderColor = Color.Gray, focusedLabelColor = Color(0xFFB39DDB), unfocusedLabelColor = Color.Gray)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f).clickable(enabled = false) {}, colors = CardDefaults.cardColors(containerColor = Color(0xFF220044))) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Editar Artículo", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (allPhotos.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(allPhotos) { url ->
                                val isDel = url in fotosABorrar; val full = getFullImageUrl(url)
                                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).border(2.dp, if (mainPhoto == url) Color(0xFFB39DDB) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { if (!isDel) mainPhoto = url }) {
                                    if (full != null) KamelImage(resource = asyncPainterResource(data = full), contentDescription = null, modifier = Modifier.fillMaxSize().then(if(isDel) Modifier.background(Color.Black.copy(alpha = 0.6f)) else Modifier), contentScale = ContentScale.Crop)
                                    IconButton(onClick = { if (isDel) fotosABorrar.remove(url) else fotosABorrar.add(url) }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(if(isDel) Color.Green else Color.Red, CircleShape)) { Icon(if(isDel) Icons.Default.Add else Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                }
                            }
                        }
                    }
                    Text("Agregar nuevas (${newPhotos.size})", color = Color.Gray, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(newPhotos) { index, _ -> Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color.White); IconButton(onClick = { newPhotos.removeAt(index) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Red, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) } } }
                        if (allPhotos.size + newPhotos.size < 5) item { AddPhotoPlaceholder { if(it.isNotEmpty()) newPhotos.add(it) } }
                    }
                    OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                    OutlinedTextField(value = tempDesc, onValueChange = { tempDesc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
                    Text("Categoría", color = Color.White, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempCat = "Articulo" }) { RadioButton(selected = tempCat == "Articulo", onClick = { tempCat = "Articulo" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Articulo", color = Color.White) }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tempCat = "Vestimenta" }) { RadioButton(selected = tempCat == "Vestimenta", onClick = { tempCat = "Vestimenta" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Vestimenta", color = Color.White) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = tempPrecio, onValueChange = { if(it.all{c -> c.isDigit() || c == '.'}) tempPrecio = it }, label = { Text("Precio") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = tempStock, onValueChange = { if(it.all{c -> c.isDigit()}) tempStock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Button(onClick = {
                        isUpdating = true
                        scope.launch {
                            val success = apiService.updateArticulo(articulo.id_articulo!!, tempNombre, tempDesc, tempPrecio.toDoubleOrNull() ?: 0.0, tempStock.toIntOrNull() ?: 0, tempCat, newPhotos.toList(), allPhotos.filter { it !in fotosABorrar }, mainPhoto)
                            if (success) { onRefresh(); onDismiss() }
                            isUpdating = false
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) { if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Guardar Cambios", color = Color.Black) }
                    TextButton(onClick = { showDelConfirm = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE57373))) { Text("Eliminar Artículo") }
                }
            }
        }
    }
    if (showDelConfirm) {
        AlertDialog(onDismissRequest = { showDelConfirm = false }, containerColor = Color(0xFF220044), title = { Text("Confirmar Eliminación", color = Color.White) }, text = { Text("¿Eliminar este artículo definitivamente?", color = Color.White.copy(alpha = 0.8f)) }, 
            confirmButton = { TextButton(onClick = { scope.launch { if(apiService.deleteArticulo(articulo.id_articulo!!)) { onRefresh(); onDismiss() } } }) { Text("ELIMINAR", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDelConfirm = false }) { Text("CANCELAR", color = Color.White) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticuloDialog(onDismiss: () -> Unit, onArticuloCreated: () -> Unit) {
    var nombre by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }; var tempStock by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Articulo") }
    val selectedPhotos = remember { mutableStateListOf<ByteArray>() }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope(); val apiService = remember { ApiService() }
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFB39DDB), unfocusedBorderColor = Color.Gray, focusedLabelColor = Color(0xFFB39DDB), unfocusedLabelColor = Color.Gray)

    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = Color(0xFF220044), 
        title = { Text("Nuevo Artículo", color = Color.White) },
        properties = DialogProperties(dismissOnClickOutside = false),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Imágenes (${selectedPhotos.size}/5)", color = Color.Gray, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(selectedPhotos) { index, _ -> Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))) { Box(modifier = Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, null, tint = Color.White) }; IconButton(onClick = { selectedPhotos.removeAt(index) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Red, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) } } }
                    if (selectedPhotos.size < 5) item { AddPhotoPlaceholder { if(it.isNotEmpty()) selectedPhotos.add(it) } }
                }
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, colors = fieldColors, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, colors = fieldColors)
                Text("Categoría", color = Color.White, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { categoria = "Articulo" }) { RadioButton(selected = categoria == "Articulo", onClick = { categoria = "Articulo" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Articulo", color = Color.White) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { categoria = "Vestimenta" }) { RadioButton(selected = categoria == "Vestimenta", onClick = { categoria = "Vestimenta" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFB39DDB))); Text("Vestimenta", color = Color.White) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = precio, onValueChange = { if(it.all{c -> c.isDigit() || c == '.'}) precio = it }, label = { Text("Precio") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = tempStock, onValueChange = { if(it.all{c -> c.isDigit()}) tempStock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.isBlank() || precio.isBlank()) return@Button
                isSaving = true
                scope.launch {
                    val res = apiService.createArticulo(nombre, desc, precio.toDoubleOrNull() ?: 0.0, tempStock.toIntOrNull() ?: 0, categoria, selectedPhotos.toList())
                    if (res) onArticuloCreated()
                    isSaving = false
                }
            }, enabled = !isSaving, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))) { 
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Guardar", color = Color.Black) 
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.White) } }
    )
}

@Composable
fun AddPhotoPlaceholder(onImageSelected: (ByteArray) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) ImagePicker { if(it.isNotEmpty()) onImageSelected(it); showPicker = false }
    Card(modifier = Modifier.size(80.dp).shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)).clickable { showPicker = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.3f))) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(24.dp)); Text("Añadir", color = Color.Gray, fontSize = 10.sp) } } }
}

@Composable fun StatItemAdmin(icon: ImageVector, label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(32.dp)); Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }
