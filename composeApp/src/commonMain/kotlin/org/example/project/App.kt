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
    object Notifications : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    val sessionManager = rememberSessionManager()
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var isAdminUser by remember { mutableStateOf(false) }
    var currentAdminName by remember { mutableStateOf("") } 
    var userGoal by remember { mutableStateOf("Ganar Masa Muscular") }
    var loggedUser by remember { mutableStateOf<UserInfo?>(null) }

    // Validación de sesión al iniciar
    LaunchedEffect(Unit) {
        val savedUser = sessionManager.getUserData()
        if (sessionManager.isLoggedIn()) {
            val token = sessionManager.getToken() ?: ""
            val users = apiService.getUsers(token)
            
            // Buscamos al usuario que coincida con el guardado o el primero si no hay guardado
            val me = if (savedUser != null) {
                users.find { it.id_usuario == savedUser.id_usuario } ?: users.firstOrNull()
            } else {
                users.firstOrNull()
            }

            if (me != null) {
                loggedUser = me
                sessionManager.saveUserData(me)
                isAdminUser = (me.rol == "admin")
                currentAdminName = me.nombre_completo
                delay(1000)
                currentScreen = if (isAdminUser) Screen.AdminPanel else Screen.Home
            } else {
                sessionManager.clearSession()
                currentScreen = Screen.Login
            }
        }
    }

    // Lógica de resta diaria automática
    LaunchedEffect(currentScreen) {
        if (sessionManager.isLoggedIn() && (currentScreen is Screen.Home || currentScreen is Screen.AdminPanel)) {
            val token = sessionManager.getToken() ?: ""
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val lastCheck = sessionManager.getLastDecrementDate()
            if (today != lastCheck) {
                val success = apiService.decrementSubscriptions(token)
                if (success) sessionManager.saveLastDecrementDate(today)
            }
        }
    }

    MaterialTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500)) },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                is Screen.Splash -> SplashScreen(onSplashFinished = { if (!sessionManager.isLoggedIn()) currentScreen = Screen.Login })
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { user ->
                        loggedUser = user
                        sessionManager.saveUserData(user)
                        isAdminUser = (user.rol == "admin")
                        currentAdminName = user.nombre_completo
                        currentScreen = if (isAdminUser) Screen.AdminPanel else Screen.GoalSelection
                    }
                )
                is Screen.AdminPanel -> AdminPanelScreen(
                    adminName = currentAdminName,
                    onLogout = { sessionManager.clearSession(); loggedUser = null; currentScreen = Screen.Login },
                    onSwitchToUserView = { currentScreen = Screen.Home }
                )
                is Screen.GoalSelection -> GoalSelectionScreen(onGoalSelected = { goal -> userGoal = goal; currentScreen = Screen.Home })
                is Screen.Home -> {
                    LaunchedEffect(Unit) {
                        while(true) {
                            val token = sessionManager.getToken() ?: ""
                            if (token.isNotEmpty() && loggedUser != null) {
                                val users = apiService.getUsers(token)
                                val me = users.find { it.id_usuario == loggedUser?.id_usuario }
                                if (me != null) {
                                    loggedUser = me
                                    sessionManager.saveUserData(me)
                                }
                            }
                            delay(30000)
                        }
                    }
                    HomeScreen(
                        onLogout = { sessionManager.clearSession(); loggedUser = null; currentScreen = Screen.Login },
                        onNavigateToProfile = { currentScreen = Screen.Profile },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications },
                        onChangeGoal = { currentScreen = Screen.GoalSelection },
                        onNavigateToAdminPanel = if (isAdminUser) { { currentScreen = Screen.AdminPanel } } else null,
                        userRank = "", nextRank = "", currentPoints = 0, nextRankPoints = 0,
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
                            val response = apiService.updateUser(token, updatedUser, imageBytes)
                            if (response.error == null) {
                                loggedUser = if (response.foto_url != null) updatedUser.copy(foto_url = response.foto_url) else updatedUser
                                sessionManager.saveUserData(loggedUser!!)
                                currentScreen = Screen.Home
                            }
                        }
                    }
                )
                is Screen.Notifications -> NotificationScreen(
                    userData = loggedUser,
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
