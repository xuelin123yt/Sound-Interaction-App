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
fun OceanInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val context = LocalContext.current
    var isPressing by remember { mutableStateOf(false) }
    var bgIndex by remember { mutableIntStateOf(0) }
    var isNavigating by remember { mutableStateOf(false) }

    val videoList = remember {
        listOf(R.raw.oceanbackground1, R.raw.oceanbackground2, R.raw.oceanbackground3)
    }

    // ✅ 进入时暂停 BGM，离开时恢复
    DisposableEffect(Unit) {
        soundManager.pauseBGM()
        onDispose {
            soundManager.resumeBGM()
        }
    }

    // ✅ 取得音量設定
    val oceanVolume = remember {
        derivedStateOf {
            val detailVolume = soundManager.getRelaxVolume("ocean")
            val masterVolume = soundManager.masterVolume
            val sfxVolume = soundManager.sfxVolume
            val isMuted = soundManager.isMasterMuted || soundManager.isSfxMuted

            if (isMuted) 0f else (masterVolume * sfxVolume * detailVolume)
        }
    }

    val audioPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.wave_sound)?.apply {
                isLooping = true
                val vol = oceanVolume.value
                setVolume(vol, vol)
            } ?: run {
                println("DEBUG: MediaPlayer.create 回傳 null，請檢查 wave_sound 檔案")
                null
            }
        } catch (e: Exception) {
            println("DEBUG: 初始化失敗: ${e.message}")
            null
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }

    // ✅ 監聽音量變化並更新
    LaunchedEffect(oceanVolume.value) {
        audioPlayer?.let {
            val vol = oceanVolume.value
            it.setVolume(vol, vol)
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
                if (isPressing) {
                    player.start()
                } else {
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            } catch (e: IllegalStateException) {
                println("DEBUG: 播放器狀態錯誤: ${e.message}")
            }
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
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            soundManager.playSFX("cancel")
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    enabled = !isNavigating
                ) { Text("← 返回", color = Color.White) }

                Button(
                    onClick = {
                        soundManager.playSFX("options2")
                        bgIndex = (bgIndex + 1) % videoList.size
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                ) { Text("切換場景 (${bgIndex + 1}/3)") }
            }

            if (!isPressing) {
                Text(
                    text = "長按螢幕感受海浪...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}