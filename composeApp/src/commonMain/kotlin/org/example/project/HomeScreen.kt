package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun HomeScreen(
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
)
