package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object AdminPanel : Screen()
    object GoalSelection : Screen()
    object Home : Screen()
    object Profile : Screen()
}

@OptIn(ExperimentalAnimationApi::class) // <- ANOTACIÓN AÑADIDA
@Composable
@Preview
fun App() {
    val sessionManager = rememberSessionManager()

    val initialScreen = if (sessionManager.isLoggedIn()) Screen.Home else Screen.Splash
    var currentScreen by remember { mutableStateOf(initialScreen) }
    var userGoal by remember { mutableStateOf("Mantenimiento") }

    MaterialTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        ) { targetScreen ->
            when (targetScreen) {
                is Screen.Splash -> SplashScreen(
                    onSplashFinished = { currentScreen = Screen.Login }
                )
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { isAdmin ->
                        if (isAdmin) {
                            currentScreen = Screen.AdminPanel
                        } else {
                            sessionManager.saveSession()
                            currentScreen = Screen.GoalSelection
                        }
                    }
                )
                is Screen.AdminPanel -> AdminPanelScreen(
                    onLogout = { currentScreen = Screen.Login }
                )
                is Screen.GoalSelection -> GoalSelectionScreen(
                    onGoalSelected = { goal ->
                        userGoal = goal
                        currentScreen = Screen.Home
                    }
                )
                is Screen.Home -> HomeScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        currentScreen = Screen.Login
                    },
                    onNavigateToProfile = { currentScreen = Screen.Profile },
                    onChangeGoal = { currentScreen = Screen.GoalSelection },
                    userRank = "Debilucho",
                    nextRank = "Pecho de chifle",
                    currentPoints = 350,
                    nextRankPoints = 1000,
                    isSubscriptionActive = true,
                    subscriptionDaysRemaining = 15,
                    userGoal = userGoal
                )
                is Screen.Profile -> ProfileScreen(
                    onBack = { currentScreen = Screen.Home },
                    onLogout = {
                        sessionManager.clearSession()
                        currentScreen = Screen.Login
                    }
                )
            }
        }
    }
}