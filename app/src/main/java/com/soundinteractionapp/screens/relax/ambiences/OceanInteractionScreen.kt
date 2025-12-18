package com.soundinteractionapp.screens.relax.ambiences

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun OceanInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    // 🔥 防止快速點擊返回按鈕
    var isNavigating by remember { mutableStateOf(false) }

    val audioPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.wave_sound).apply {
                isLooping = true
                setVolume(0.6f, 0.6f)
            }
        } catch (e: Exception) { null }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.ocean_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            audioPlayer?.release()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
            audioPlayer?.start()
        } else {
            exoPlayer.pause()
            if (audioPlayer?.isPlaying == true) {
                audioPlayer.pause()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isPlaying) isPlaying = true
                }
        ) {
            if (!isPlaying) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "點擊畫面感受海浪",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                ),
                modifier = Modifier.height(50.dp),
                enabled = !isNavigating
            ) {
                Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (isPlaying) {
            Button(
                onClick = { isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                Text("暫停海浪")
            }
        }
    }
}