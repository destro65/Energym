package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// --- Data Class para la lista de usuarios ---
data class UserData(
    val id: Int,
    var username: String,
    var password: String, // Campo añadido
    var weight: String,
    var height: String,
    var social: String,
    var subscriptionDays: Int,
    var isHallOfFame: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminPanelScreen(onLogout: () -> Unit, onSwitchToUserView: () -> Unit) {
    val tabs = listOf("Dashboard", "Crear Usuario", "Gestión de Artículos")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTabIndex = pagerState.currentPage
    val scope = rememberCoroutineScope()
    
    var selectedUser by remember { mutableStateOf<UserData?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Panel de Administrador", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF39006F),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = onSwitchToUserView) { 
                            Icon(Icons.Default.SwitchAccount, "Cambiar a vista de usuario")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
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
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(text = title) }
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
                .background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF39006F))))
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> AdminDashboardTab(onUserClick = { user -> selectedUser = user })
                    1 -> CreateUserTab()
                    2 -> ShopManagementTab()
                }
            }
            
            AnimatedVisibility(visible = selectedUser != null) {
                selectedUser?.let {
                    UserDetailView(
                        user = it,
                        onDismiss = { selectedUser = null }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashboardTab(onUserClick: (UserData) -> Unit) {
    val users = remember {
        mutableStateListOf(
            UserData(1, "A", "A", "75", "180", "@user_a", 15, isHallOfFame = true),
            UserData(2, "user_test", "test", "82", "175", "@user_test", 0),
            UserData(3, "strong_user", "strong", "90", "185", "@strong_user", 30)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                 Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estadística de Usuarios", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItemAdmin(Icons.Default.Group, "Total de Usuarios", users.size.toString())
                        StatItemAdmin(Icons.Default.PersonAdd, "Nuevos (Mes)", "5")
                    }
                }
            }
        }

        item {
            Text("Usuarios Registrados", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }

        items(users) { user ->
            UserListItem(user = user, onClick = { onUserClick(user) })
        }
    }
}

@Composable
fun UserListItem(user: UserData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (user.isHallOfFame) {
                Icon(Icons.Default.Star, "Salón de la Fama", tint = Color(0xFFFFD700), modifier = Modifier.padding(end = 8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Suscripción: ${user.subscriptionDays} días", color = if (user.subscriptionDays > 0) Color.Green else Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailView(user: UserData, onDismiss: () -> Unit) {
    var tempUsername by remember { mutableStateOf(user.username) }
    var tempPassword by remember { mutableStateOf("") } // Vacío por seguridad
    var tempWeight by remember { mutableStateOf(user.weight) }
    var tempHeight by remember { mutableStateOf(user.height) }
    var tempSocial by remember { mutableStateOf(user.social) }
    var tempSubscriptionDays by remember { mutableStateOf("") }
    var tempIsHallOfFame by remember { mutableStateOf(user.isHallOfFame) }

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color(0xFFB39DDB),
        unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = Color(0xFFB39DDB),
        focusedLabelColor = Color(0xFFB39DDB),
        unfocusedLabelColor = Color.Gray
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.95f).clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Editar Usuario", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar", tint = Color.White) }
                }
                Divider(color = Color(0xFFB39DDB), modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = tempUsername, onValueChange = { tempUsername = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                    OutlinedTextField(value = tempPassword, onValueChange = { tempPassword = it }, label = { Text("Nueva Contraseña (opcional)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                    OutlinedTextField(value = tempWeight, onValueChange = { tempWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                    OutlinedTextField(value = tempHeight, onValueChange = { tempHeight = it }, label = { Text("Altura (cm)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                    OutlinedTextField(value = tempSocial, onValueChange = { tempSocial = it }, label = { Text("Red Social") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Añadir al Salón de la Fama", color = Color.White)
                        Switch(checked = tempIsHallOfFame, onCheckedChange = { tempIsHallOfFame = it })
                    }
                    
                    Text("Gestionar Suscripción", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = tempSubscriptionDays,
                            onValueChange = { tempSubscriptionDays = it },
                            label = { Text("Días") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = Color.White),
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                             val daysToAdd = tempSubscriptionDays.toIntOrNull() ?: 0
                             user.subscriptionDays += daysToAdd
                             tempSubscriptionDays = ""
                        }) {
                            Text("Añadir")
                        }
                    }
                     Text("Días restantes: ${user.subscriptionDays}", color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        user.username = tempUsername
                        if (tempPassword.isNotBlank()) { user.password = tempPassword }
                        user.weight = tempWeight
                        user.height = tempHeight
                        user.social = tempSocial
                        user.isHallOfFame = tempIsHallOfFame
                        onDismiss()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserTab() {
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }
    var newHeight by remember { mutableStateOf("") }
    var newSocial by remember { mutableStateOf("") }

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color(0xFFB39DDB),
        unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = Color(0xFFB39DDB),
        focusedLabelColor = Color(0xFFB39DDB),
        unfocusedLabelColor = Color.Gray
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Crear Nuevo Usuario", style = MaterialTheme.typography.titleLarge, color = Color.White)
                
                OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Nombre de usuario") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = newWeight, onValueChange = { newWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = newHeight, onValueChange = { newHeight = it }, label = { Text("Altura (cm)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = newSocial, onValueChange = { newSocial = it }, label = { Text("Red Social (opcional)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { /* Lógica para crear usuario */ }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
                    Icon(Icons.Default.Add, "Crear Usuario")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Usuario")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopManagementTab() {
    var itemName by remember { mutableStateOf("") }
    var itemValue by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color(0xFFB39DDB),
        unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = Color(0xFFB39DDB),
        focusedLabelColor = Color(0xFFB39DDB),
        unfocusedLabelColor = Color.Gray
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Añadir Nuevo Artículo", style = MaterialTheme.typography.titleLarge, color = Color.White)
                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nombre del artículo") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = itemValue, onValueChange = { itemValue = it }, label = { Text("Valor (ej: 19.99)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                OutlinedTextField(value = itemDescription, onValueChange = { itemDescription = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), maxLines = 3, textStyle = TextStyle(color = Color.White), colors = textFieldColors)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Lógica para añadir artículo */ }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
                    Icon(Icons.Default.AddShoppingCart, "Añadir Artículo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Artículo")
                }
            }
        }
    }
}
@Composable
fun StatItemAdmin(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}