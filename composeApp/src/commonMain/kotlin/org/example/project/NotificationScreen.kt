package org.example.project

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(userData: UserInfo?, onBack: () -> Unit) {
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()
    val scope = rememberCoroutineScope()
    
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshNotifications() {
        scope.launch {
            isLoading = true
            val token = sessionManager.getToken() ?: ""
            val userId = userData?.id_usuario ?: sessionManager.getUserData()?.id_usuario ?: 0
            if (userId > 0) {
                notifications = apiService.getNotifications(token, userId)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            refreshNotifications()
            delay(15000) 
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF39006F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF39006F))))
        ) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB39DDB))
            } else if (notifications.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, tint = Color.Gray, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes notificaciones", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = {
                                scope.launch {
                                    val token = sessionManager.getToken() ?: ""
                                    if (apiService.markNotificationAsRead(token, notification.id_notificacion)) {
                                        notifications = notifications.map { if (it.id_notificacion == notification.id_notificacion) it.copy(leida = 1) else it }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val token = sessionManager.getToken() ?: ""
                                    if (apiService.deleteNotification(token, notification.id_notificacion)) {
                                        notifications = notifications.filter { it.id_notificacion != notification.id_notificacion }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onMarkAsRead: () -> Unit, onDelete: () -> Unit) {
    val isRead = notification.leida == 1
    Card(
        modifier = Modifier.fillMaxWidth().clickable { if (!isRead) onMarkAsRead() },
        colors = CardDefaults.cardColors(containerColor = if (isRead) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isRead) Color.Gray.copy(alpha = 0.3f) else Color(0xFFB39DDB)), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when {
                        notification.titulo.contains("Suscripción", true) -> Icons.Default.CardMembership
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = if (isRead) Color.White.copy(alpha = 0.5f) else Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = notification.titulo, color = if (isRead) Color.LightGray else Color.White, fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold, fontSize = 16.sp)
                    Text(text = notification.fecha.split(" ").firstOrNull() ?: "", color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = notification.mensaje, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 18.sp)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) }
        }
    }
}
