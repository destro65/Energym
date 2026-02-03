package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserInfo?,
    onBack: () -> Unit,
    onUpdateUser: (UserInfo, ByteArray?) -> Unit
) {
    val apiService = remember { ApiService() }
    val sessionManager = rememberSessionManager()
    val scope = rememberCoroutineScope()

    var fbUrl by remember(userData) { mutableStateOf(userData?.fb_url ?: "") }
    var igUrl by remember(userData) { mutableStateOf(userData?.ig_url ?: "") }
    var tkUrl by remember(userData) { mutableStateOf(userData?.tk_url ?: "") }
    var waNum by remember(userData) { mutableStateOf(userData?.wa_num ?: "") }
    var peso by remember(userData) { mutableStateOf(userData?.peso ?: "") }
    var altura by remember(userData) { mutableStateOf(userData?.altura ?: "") }
    
    var currentFotoUrl by remember(userData) { mutableStateOf(userData?.foto_url ?: "") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    var showSelectionDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showPhotoGallery by remember { mutableStateOf(false) }
    var userPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            containerColor = Color(0xFF220044),
            title = { Text("Cambiar Foto", color = Color.White) },
            text = { Text("¿Deseas subir una foto nueva o elegir una anterior?", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { 
                    showSelectionDialog = false
                    showImagePicker = true 
                }) { Text("NUEVA", color = Color(0xFFB39DDB)) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSelectionDialog = false
                    scope.launch {
                        val token = sessionManager.getToken() ?: ""
                        userPhotos = apiService.getUserPhotos(token, userData?.id_usuario ?: 0)
                        showPhotoGallery = true
                    }
                }) { Text("GALERÍA", color = Color.White) }
            }
        )
    }

    if (showImagePicker) {
        ImagePicker(
            onImageSelected = {
                if (it.isNotEmpty()) {
                    selectedImageBytes = it
                    currentFotoUrl = ""
                }
                showImagePicker = false
            }
        )
    }

    if (showPhotoGallery) {
        Dialog(onDismissRequest = { showPhotoGallery = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tus Fotos Anteriores", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (userPhotos.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No tienes fotos guardadas", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(userPhotos) { url ->
                                val fullUrl = getFullImageUrl(url)
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, if (currentFotoUrl == url) Color(0xFFB39DDB) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable {
                                            currentFotoUrl = url
                                            selectedImageBytes = null
                                            showPhotoGallery = false
                                        }
                                ) {
                                    if (fullUrl != null) {
                                        KamelImage(
                                            resource = asyncPainterResource(data = fullUrl),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { showPhotoGallery = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))
                    ) { Text("Cerrar", color = Color.Black) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF39006F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF39006F)))),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFB39DDB), CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageBytes != null) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Green)
                        } else if (currentFotoUrl.isNotEmpty()) {
                            val fullUrl = getFullImageUrl(currentFotoUrl)
                            if (fullUrl != null) {
                                KamelImage(
                                    resource = asyncPainterResource(data = fullUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    IconButton(
                        onClick = { showSelectionDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB39DDB))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar Foto", modifier = Modifier.size(20.dp), tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileInfoField(label = "Nombre Completo", value = userData?.nombre_completo ?: "No disponible")
                ProfileInfoField(label = "Email", value = userData?.email ?: "No disponible")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = peso,
                        onValueChange = { if(it.all { c -> c.isDigit() || c == '.' }) peso = it },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = altura,
                        onValueChange = { if(it.all { c -> c.isDigit() }) altura = it },
                        label = { Text("Altura (cm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                ProfileInfoField(label = "Edad", value = calcularEdad(userData?.fecha_nacimiento))

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                Text("Redes Sociales", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                SocialInputField(value = fbUrl, onValueChange = { fbUrl = it }, label = "Facebook (URL)", icon = Icons.Default.Facebook)
                SocialInputField(value = igUrl, onValueChange = { igUrl = it }, label = "Instagram (URL)", icon = Icons.Default.CameraAlt)
                SocialInputField(value = tkUrl, onValueChange = { tkUrl = it }, label = "TikTok (URL)", icon = Icons.Default.MusicNote)
                SocialInputField(value = waNum, onValueChange = { waNum = it }, label = "WhatsApp (Número)", icon = Icons.Default.Chat)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        userData?.let {
                            val updatedUser = it.copy(
                                foto_url = currentFotoUrl,
                                peso = peso,
                                altura = altura,
                                fb_url = fbUrl,
                                ig_url = igUrl,
                                tk_url = tkUrl,
                                wa_num = waNum
                            )
                            onUpdateUser(updatedUser, selectedImageBytes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Text("Guardar Cambios")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.LightGray,
            unfocusedTextColor = Color.LightGray,
            focusedBorderColor = Color.DarkGray,
            unfocusedBorderColor = Color.DarkGray,
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Composable
fun SocialInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFB39DDB),
            unfocusedBorderColor = Color.Gray
        ),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFFB39DDB)) }
    )
}
