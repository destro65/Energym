package org.example.project

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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

    // --- ANIMACIONES ---
    var startAnimations by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "logoPulse"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnimations = true
    }

    fun performLogin() {
        if (isLoading) return
        if (email.isBlank() || password.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Por favor, completa todos los campos") }
            return
        }
        isLoading = true
        scope.launch {
            try {
                val response = apiService.login(email, password)
                if (response.token != null && response.user != null) {
                    sessionManager.saveSession(response.token)
                    sessionManager.saveUserData(response.user)
                    onLoginSuccess(response.user)
                } else {
                    val errorMsg = response.error ?: "Credenciales inválidas"
                    isLoading = false
                    snackbarHostState.showSnackbar(errorMsg)
                }
            } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("Error de conexión")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A0033), Color.Black, Color(0xFF39006F))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- LOGO ANIMADO ---
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = scaleIn(tween(1000, easing = OvershootInterpolator().toEasing())) + fadeIn()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Halo de luz detrás del logo
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(pulseScale * 1.2f)
                                .background(Color(0xFFB39DDB).copy(alpha = 0.1f), CircleShape)
                        )
                        Image(
                            painter = painterResource(R.drawable.logobn),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(150.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- TÍTULO ANIMADO ---
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(tween(1000, delayMillis = 200))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ENERGYM",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "ENTRENA • SUPERA • LOGRA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                color = Color(0xFFB39DDB)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // --- FORMULARIO CON ENTRADA ESCALONADA ---
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(1000, delayMillis = 400))
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Mail, null, tint = Color(0xFFB39DDB)) },
                        readOnly = isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFFB39DDB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = startAnimations,
                    enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(1000, delayMillis = 600))
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFB39DDB)) },
                        readOnly = isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            focusManager.clearFocus()
                            performLogin() 
                        }),
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color(0xFFB39DDB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- BOTÓN CON BRILLO ---
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(1000, delayMillis = 800))
                ) {
                    Button(
                        onClick = { performLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer {
                                shadowElevation = 8.dp.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = true
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            disabledContainerColor = Color(0xFF6200EE).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "INICIAR SESIÓN",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private class OvershootInterpolator(private val tension: Float = 2f) {
    fun toEasing() = Easing { x ->
        val t = x - 1.0f
        t * t * ((tension + 1) * t + tension) + 1.0f
    }
}
