package org.example.project

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSoundPlayer(soundResName: String): SoundPlayer {
    val context = LocalContext.current
    val soundId = remember(soundResName) {
        context.resources.getIdentifier(soundResName, "raw", context.packageName)
    }
    
    val mediaPlayer = remember(soundId) {
        if (soundId != 0) {
            MediaPlayer.create(context, soundId)?.apply {
                // Configura un listener que se activa cuando el sonido termina
                setOnCompletionListener { mp ->
                    mp.release()
                }
            }
        } else {
            null
        }
    }

    return remember(mediaPlayer) {
        SoundPlayer(mediaPlayer)
    }
}

actual class SoundPlayer(private val player: MediaPlayer?) {
    actual fun play() {
        player?.start()
    }

    actual fun stop() {
        // Ya no es necesario que el stop libere los recursos,
        // se hará automáticamente al completar la reproducción.
        try {
            if (player?.isPlaying == true) {
                player.stop()
                player.prepare()
            }
        } catch (e: IllegalStateException) {
            // Ignorar el error si el player ya está en un estado inválido
        }
    }
    
    actual fun release() {
        // La liberación principal se hará en el onCompletionListener.
        // Este método se puede mantener por si se necesita una liberación manual.
        try {
            player?.release()
        } catch (e: Exception) {
            // Ignorar si ya ha sido liberado
        }
    }
}