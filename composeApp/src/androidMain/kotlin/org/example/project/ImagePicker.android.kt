package org.example.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ImagePicker(
    modifier: Modifier,
    label: String,
    onImageSelected: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                onImageSelected(bytes)
            }
            inputStream?.close()
        } else {
            // Si el usuario cancela, devolvemos un array vacío para cerrar el picker en el estado
            onImageSelected(ByteArray(0))
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
