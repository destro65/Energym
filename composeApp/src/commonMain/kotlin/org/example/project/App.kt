package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.*

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object AdminPanel : Screen()
    object GoalSelection : Screen()
    object Home : Screen()
    object Profile : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    val sessionManager = rememberSessionManager()
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService() }

    var currentScreen by remember { mutableStateOf<Screen>(if (sessionManager.isLoggedIn()) Screen.Splash else Screen.Splash) }
    var isAdminUser by remember { mutableStateOf(false) }
    var currentAdminName by remember { mutableStateOf("") } 
    
    var userGoal by remember { mutableStateOf("Ganar Masa Muscular") }
    var loggedUser by remember { mutableStateOf<UserInfo?>(null) }

    // Al arrancar, si hay token, recuperamos al usuario real de la BDD
    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) {
            val token = sessionManager.getToken() ?: ""
            val users = apiService.getUsers(token)
            if (users.isNotEmpty()) {
                // Buscamos al usuario que corresponde al token (o el admin si es el caso)
                // Nota: Idealmente el login debería devolver el ID guardado, por ahora buscamos consistencia
                val me = users.firstOrNull { it.email.isNotEmpty() } // Intento de recuperación básica
                if (me != null) {
                    loggedUser = me
                    isAdminUser = (me.rol == "admin")
                    currentAdminName = me.nombre_completo
                    delay(1000)
                    currentScreen = if (isAdminUser) Screen.AdminPanel else Screen.Home
                } else {
                    currentScreen = Screen.Login
                }
            } else {
                currentScreen = Screen.Login
            }
        } else {
            // Si no hay sesión, el splash termina y va a login (manejado por SplashScreen)
        }
    }

    // LÓGICA DE RESTA DIARIA AUTOMÁTICA
    LaunchedEffect(currentScreen) {
        if (sessionManager.isLoggedIn() && (currentScreen is Screen.Home || currentScreen is Screen.AdminPanel)) {
            val token = sessionManager.getToken() ?: ""
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val lastCheck = sessionManager.getLastDecrementDate()
            
            if (today != lastCheck) {
                val success = apiService.decrementSubscriptions(token)
                if (success) {
                    sessionManager.saveLastDecrementDate(today)
                }
            }
        }
    }

    MaterialTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                is Screen.Splash -> SplashScreen(
                    onSplashFinished = { 
                        if (!sessionManager.isLoggedIn()) {
                            currentScreen = Screen.Login 
                        }
                    }
                )
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { user ->
                        loggedUser = user
                        isAdminUser = (user.rol == "admin")
                        currentAdminName = user.nombre_completo
                        sessionManager.saveLastKnownDays(user.dias_suscripcion)
                        if (isAdminUser) {
                            currentScreen = Screen.AdminPanel
                        } else {
                            currentScreen = Screen.GoalSelection
                        }
                    }
                )
                is Screen.AdminPanel -> AdminPanelScreen(
                    adminName = currentAdminName,
                    onLogout = { 
                        sessionManager.clearSession()
                        loggedUser = null
                        currentScreen = Screen.Login 
                    },
                    onSwitchToUserView = { 
                        currentScreen = Screen.Home
                    }
                )
                is Screen.GoalSelection -> GoalSelectionScreen(
                    onGoalSelected = { goal ->
                        userGoal = goal
                        currentScreen = Screen.Home
                    }
                )
                is Screen.Home -> {
                    // Sincronización en segundo plano para detectar cambios del admin
                    LaunchedEffect(Unit) {
                        while(true) {
                            val token = sessionManager.getToken() ?: ""
                            if (token.isNotEmpty()) {
                                val users = apiService.getUsers(token)
                                val me = users.find { it.id_usuario == loggedUser?.id_usuario }
                                if (me != null) {
                                    loggedUser = me
                                }
                            }
                            delay(30000) // Poll cada 30 segundos
                        }
                    }

                    HomeScreen(
                        onLogout = {
                            sessionManager.clearSession()
                            loggedUser = null
                            currentScreen = Screen.Login
                        },
                        onNavigateToProfile = { currentScreen = Screen.Profile },
                        onChangeGoal = { currentScreen = Screen.GoalSelection },
                        onNavigateToAdminPanel = if (isAdminUser) { { currentScreen = Screen.AdminPanel } } else null,
                        userRank = "Debilucho",
                        nextRank = "Pecho de chifle",
                        currentPoints = 350,
                        nextRankPoints = 1000,
                        isSubscriptionActive = (loggedUser?.dias_suscripcion ?: 0) > 0,
                        subscriptionDaysRemaining = loggedUser?.dias_suscripcion,
                        userGoal = userGoal,
                        userData = loggedUser
                    )
                }
                is Screen.Profile -> ProfileScreen(
                    userData = loggedUser,
                    onBack = { currentScreen = Screen.Home },
                    onUpdateUser = { updatedUser, imageBytes ->
                        scope.launch {
                            val token = sessionManager.getToken() ?: ""
                            val newFotoUrl = apiService.updateUser(token, updatedUser, imageBytes)
                            if (newFotoUrl != null) {
                                loggedUser = updatedUser.copy(foto_url = newFotoUrl)
                                currentScreen = Screen.Home
                            }
                        }
                    }
                )
            }
        }
    }
}
