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
fun WindInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val context = LocalContext.current

    // 1. 狀態管理
    var isPressing by remember { mutableStateOf(false) } // 追蹤長按狀態
    var bgIndex by remember { mutableIntStateOf(0) }    // 追蹤背景索引
    var isNavigating by remember { mutableStateOf(false) }

    // 2. 微風背景清單 (請確認 R.raw 有這些檔案)
    val videoList = remember {
        listOf(
            R.raw.windbackground1,
            R.raw.windbackground2,
            R.raw.windbackground3
        )
    }

    // 3. 風聲播放器 (長按時播放)
    val audioPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.wind_sound).apply {
                isLooping = true
                setVolume(0.7f, 0.7f)
            }
        } catch (e: Exception) {
            null
        }
    }

    // 4. 影片播放器 (背景動態影片)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }

    // 5. 監控：切換影片
    LaunchedEffect(bgIndex) {
        val videoUri = Uri.parse("android.resource://${context.packageName}/${videoList[bgIndex]}")
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // 6. 監控：長按音效開關
    LaunchedEffect(isPressing) {
        if (isPressing) {
            audioPlayer?.start()
        } else {
            if (audioPlayer?.isPlaying == true) {
                audioPlayer.pause()
            }
        }
    }

    // 7. 資源釋放
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            audioPlayer?.release()
        }
    }

    // 8. 介面佈局
    Box(
        modifier = Modifier
            .fillMaxSize()
            // 偵測全螢幕長按
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
        // 底層影片
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

        // UI 頂層覆蓋
        Box(modifier = Modifier.fillMaxSize()) {

            // --- 上方按鈕列 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween // 讓兩個按鈕分開在左右兩邊
            ) {
                // 左上：返回按鈕
                Button(
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Text("← 返回", color = Color.White)
                }

                // 🔥 右上：切換場景按鈕
                Button(
                    onClick = {
                        bgIndex = (bgIndex + 1) % videoList.size
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                ) {
                    Text("切換場景 (${bgIndex + 1}/3)")
                }
            }

            // 中間提示文字
            if (!isPressing) {
                Text(
                    text = "長按螢幕感受微風...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}