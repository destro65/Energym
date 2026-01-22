package org.example.project

import androidx.compose.runtime.Composable

interface SessionManager {
    fun isLoggedIn(): Boolean
    fun saveSession(token: String)
    fun getToken(): String?       
    fun clearSession()
    fun getLastDecrementDate(): String?
    fun saveLastDecrementDate(date: String)
    fun getLastKnownDays(): Int
    fun saveLastKnownDays(days: Int)
}

@Composable
expect fun rememberSessionManager(): SessionManager
