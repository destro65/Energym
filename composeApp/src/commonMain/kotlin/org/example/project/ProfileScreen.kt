package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserInfo?,
    onBack: () -> Unit,
    onUpdateUser: (UserInfo, ByteArray?) -> Unit
) {
    var fbUrl by remember { mutableStateOf(userData?.fb_url ?: "") }
    var igUrl by remember { mutableStateOf(userData?.ig_url ?: "") }
    var tkUrl by remember { mutableStateOf(userData?.tk_url ?: "") }
    var waNum by remember { mutableStateOf(userData?.wa_num ?: "") }
    
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    if (showImagePicker) {
        ImagePicker(
            onImageSelected = {
                if (it.isNotEmpty()) {
                    selectedImageBytes = it
                }
                showImagePicker = false
            }
        )
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
                // APARTADO DE FOTO DE PERFIL
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageBytes != null) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Green)
                        } else if (!userData?.foto_url.isNullOrEmpty()) {
                            KamelImage(
                                resource = asyncPainterResource(data = userData?.foto_url ?: ""),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    IconButton(
                        onClick = { showImagePicker = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB39DDB))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar Foto", modifier = Modifier.size(20.dp), tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CAMPOS DE INFORMACIÓN (Solo lectura)
                ProfileInfoField(label = "Nombre Completo", value = userData?.nombre_completo ?: "No disponible")
                ProfileInfoField(label = "Email", value = userData?.email ?: "No disponible")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { ProfileInfoField(label = "Peso (kg)", value = userData?.peso ?: "0") }
                    Box(modifier = Modifier.weight(1f)) { ProfileInfoField(label = "Altura (cm)", value = userData?.altura ?: "0") }
                }

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                Text("Redes Sociales", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                // CAMPOS EDITABLES: REDES SOCIALES
                SocialInputField(value = fbUrl, onValueChange = { fbUrl = it }, label = "Facebook (URL)", icon = Icons.Default.Facebook)
                SocialInputField(value = igUrl, onValueChange = { igUrl = it }, label = "Instagram (URL)", icon = Icons.Default.CameraAlt)
                SocialInputField(value = tkUrl, onValueChange = { tkUrl = it }, label = "TikTok (URL)", icon = Icons.Default.MusicNote)
                SocialInputField(value = waNum, onValueChange = { waNum = it }, label = "WhatsApp (Número ej: 5939...)", icon = Icons.Default.Chat)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        userData?.let {
                            val updatedUser = it.copy(
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFB39DDB),
            unfocusedBorderColor = Color.Gray
        ),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFFB39DDB)) }
    )
}
