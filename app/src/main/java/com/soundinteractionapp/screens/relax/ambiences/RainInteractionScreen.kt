package com.soundinteractionapp.screens.relax.ambiences

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager

@OptIn(UnstableApi::class)
@Composable
fun RainInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val context = LocalContext.current
    var isPressing by remember { mutableStateOf(false) }
    var bgIndex by remember { mutableIntStateOf(0) }
    var isNavigating by remember { mutableStateOf(false) }

    val videoList = remember {
        listOf(R.raw.rainbackground1, R.raw.rainbackground2, R.raw.rainbackground3)
    }

    val audioPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.rain_sound)?.apply {
                isLooping = true
                setVolume(0.8f, 0.8f)
            }
        } catch (e: Exception) { null }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }

    LaunchedEffect(bgIndex) {
        val videoUri = Uri.parse("android.resource://${context.packageName}/${videoList[bgIndex]}")
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    LaunchedEffect(isPressing) {
        audioPlayer?.let { player ->
            try {
                if (isPressing) player.start() else if (player.isPlaying) player.pause()
            } catch (e: Exception) { }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            audioPlayer?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            isPressing = true
                            tryAwaitRelease()
                        } finally {
                            isPressing = false
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { if (!isNavigating) { isNavigating = true; onNavigateBack() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) { Text("← 返回", color = Color.White) }

                Button(
                    onClick = { bgIndex = (bgIndex + 1) % videoList.size },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                ) { Text("切換場景 (${bgIndex + 1}/3)") }
            }

            if (!isPressing) {
                Text(
                    text = "長按螢幕感受雨聲...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}