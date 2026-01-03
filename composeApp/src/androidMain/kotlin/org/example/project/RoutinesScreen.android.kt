package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
actual fun VideoPlayer(modifier: Modifier, url: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Extraemos el ID del video de la URL
    val videoId = url.substringAfter("v=")

    val youTubePlayerView = remember {
        YouTubePlayerView(context).apply {
            // Añadimos el observador del ciclo de vida
            lifecycleOwner.lifecycle.addObserver(this)

            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(videoId, 0f)
                }
            })
        }
    }

    AndroidView(modifier = modifier, factory = { youTubePlayerView })

    DisposableEffect(Unit) {
        onDispose {
            youTubePlayerView.release()
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
        }
    }
}