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

    private val TOKEN_KEY = "jwt_token"
    private val LAST_DECREMENT_KEY = "last_decrement_date"
    private val LAST_KNOWN_DAYS_KEY = "last_known_days"

    override fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    override fun saveSession(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    override fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    override fun clearSession() {
        prefs.edit().clear().apply()
    }

    override fun getLastDecrementDate(): String? {
        return prefs.getString(LAST_DECREMENT_KEY, null)
    }

    override fun saveLastDecrementDate(date: String) {
        prefs.edit().putString(LAST_DECREMENT_KEY, date).apply()
    }

    override fun getLastKnownDays(): Int {
        return prefs.getInt(LAST_KNOWN_DAYS_KEY, 0)
    }

    override fun saveLastKnownDays(days: Int) {
        prefs.edit().putInt(LAST_KNOWN_DAYS_KEY, days).apply()
    }
}

@Composable
actual fun rememberSessionManager(): SessionManager {
    val context = LocalContext.current
    return remember { AndroidSessionManager(context) }
}
