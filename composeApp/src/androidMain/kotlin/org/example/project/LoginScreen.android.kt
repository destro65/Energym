package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LoginScreen(onLoginSuccess: (isAdmin: Boolean) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = Color(0xFF673AB7), contentColor = Color.White)
            }
        },
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
                    modifier = Modifier
                        .size(200.dp) 
                        .scale(scale) 
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Bienvenido a Energym",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.scale(scale)
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario") },
                            readOnly = isLoading,
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFFB39DDB),
                                unfocusedIndicatorColor = Color.Gray,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color(0xFFB39DDB),
                                focusedLabelColor = Color(0xFFB39DDB),
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            readOnly = isLoading,
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                               focusedIndicatorColor = Color(0xFFB39DDB),
                                unfocusedIndicatorColor = Color.Gray,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color(0xFFB39DDB),
                                focusedLabelColor = Color(0xFFB39DDB),
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        val buttonColor by animateColorAsState(
                            targetValue = if (isLoading) Color(0xFF512DA8) else Color(0xFF673AB7)
                        )

                        Button(
                            onClick = {
                                if (isLoading) return@Button

                                when {
                                    username == "Admin" && password == "Admin" -> { 
                                        isLoading = true
                                        scope.launch {
                                            delay(1000L)
                                            onLoginSuccess(true) // Es admin
                                        }
                                    }
                                    username == "A" && password == "A" -> {
                                        isLoading = true
                                        scope.launch {
                                            delay(1000L)
                                            onLoginSuccess(false) // Es usuario normal
                                        }
                                    }
                                    else -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Usuario o contraseña incorrectos")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.height(48.dp).fillMaxWidth().animateContentSize(),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            if (isLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Iniciando...")
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Iniciar sesión")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, "Iniciar sesión")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}