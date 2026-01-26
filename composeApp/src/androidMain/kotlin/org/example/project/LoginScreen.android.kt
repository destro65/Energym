package org.example.project

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LoginScreen(onLoginSuccess: (user: UserInfo) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()

    fun performLogin() {
        if (isLoading) return
        if (email.isBlank() || password.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Por favor, completa todos los campos") }
            return
        }
        isLoading = true
        scope.launch {
            val response = apiService.login(email, password)
            if (response.token != null && response.user != null) {
                sessionManager.saveSession(response.token)
                onLoginSuccess(response.user)
            } else {
                val errorMsg = response.error ?: "Credenciales inválidas"
                snackbarHostState.showSnackbar(errorMsg)
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { 
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF39006F)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logobn),
                    contentDescription = "Logo de Energym",
                    modifier = Modifier.size(180.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Energym Login",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.LightGray) },
                    readOnly = isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFB39DDB),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFB39DDB)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color.LightGray) },
                    readOnly = isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        focusManager.clearFocus()
                        performLogin() 
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFB39DDB),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFB39DDB)
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { performLogin() },
                    modifier = Modifier.height(50.dp).fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Entrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
