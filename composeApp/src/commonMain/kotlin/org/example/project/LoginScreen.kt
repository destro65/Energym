package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun LoginScreen(onLoginSuccess: (isAdmin: Boolean) -> Unit)
