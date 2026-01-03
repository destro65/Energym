package org.example.project

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidSessionManager(private val context: Context) : SessionManager {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("energym_session", Context.MODE_PRIVATE)
    }

    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    override fun saveSession() {
        prefs.edit().putBoolean("is_logged_in", true).apply()
    }

    override fun clearSession() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
}

@Composable
actual fun rememberSessionManager(): SessionManager {
    val context = LocalContext.current
    return remember { AndroidSessionManager(context) }
}