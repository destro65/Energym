package org.example.project

import androidx.compose.runtime.Composable

// Definición para que commonMain sepa qué esperar de cada plataforma.

@Composable
expect fun rememberSoundPlayer(soundResName: String): SoundPlayer

expect class SoundPlayer {
    fun play()
    fun stop()
    fun release()
}