package org.example.project

import androidx.compose.runtime.Composable

interface SessionManager {
    fun isLoggedIn(): Boolean
    fun saveSession()
    fun clearSession()
}

@Composable
expect fun rememberSessionManager(): SessionManager
