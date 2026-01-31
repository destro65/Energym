package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onChangeGoal: () -> Unit,
    onNavigateToAdminPanel: (() -> Unit)? = null,
    userRank: String,
    nextRank: String,
    currentPoints: Int,
    nextRankPoints: Int,
    isSubscriptionActive: Boolean,
    subscriptionDaysRemaining: Int?,
    userGoal: String,
    userData: UserInfo? = null
)
