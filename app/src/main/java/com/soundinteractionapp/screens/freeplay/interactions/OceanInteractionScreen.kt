package com.soundinteractionapp.screens.freeplay.interactions

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

    // 狀態：是否正在播放
    var isPlaying by remember { mutableStateOf(false) }

    // --- 1. 背景音效播放器 (wave_sound.mp3) ---
    // 因為影片被靜音了，我們加回這個播放器來負責「好聽的海浪聲」
    val audioPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.wave_sound).apply {
                isLooping = true // 循環播放
                setVolume(0.6f, 0.6f) // 設定音量
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- 2. 影片播放器 (ExoPlayer - 負責畫面) ---
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.ocean_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ONE // 影片循環
            volume = 0f // 【關鍵修改】將影片設為靜音
            prepare()
        }
    }

    // --- 3. 生命週期管理 & 同步控制 ---
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()     // 釋放影片
            audioPlayer?.release()  // 釋放音樂
        }
    }

    // 當 isPlaying 改變時，同時控制「影片」和「音樂」
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

    // --- 4. 畫面 UI ---
    Box(modifier = Modifier.fillMaxSize()) {

        // (A) 影片層 (無聲背景)
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

        // (B) 透明互動層
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isPlaying) {
                        // 【關鍵修改】移除鳥叫聲
                        // 這裡現在什麼都不做，或者你希望點擊也能暫停？
                        // 目前保持空白，只讓右下角按鈕負責暫停
                    } else {
                        // 尚未播放時，點擊 -> 開始
                        isPlaying = true
                    }
                }
        ) {
            // 提示文字
            if (!isPlaying) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "點擊畫面感受海浪",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // (C) 返回按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.height(50.dp)
            ) {
                Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // (D) 暫停按鈕
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