package com.soundinteractionapp.screens.freeplay.interactions

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.delay
import kotlin.random.Random

// 資料類別
data class CatAsset(
    val frames: List<Int>,
    val soundRes: Int,
    val name: String
)

/**
 * 專門用來播放背景影片的元件
 * 使用 ExoPlayer + AndroidView
 */
@OptIn(UnstableApi::class) // 標註使用 Media3 的 UI API
@Composable
fun VideoBackground(videoResId: Int) {
    val context = LocalContext.current

    // 初始化 ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 建立影片來源 URI (res/raw 資料夾)
            val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
            setMediaItem(MediaItem.fromUri(videoUri))

            // 設定循環播放與自動播放
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()

            // 設定靜音 (避免背景聲音干擾貓叫聲)
            volume = 0f
        }
    }

    // 當 Composable 被銷毀時，釋放播放器資源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 嵌入原生 View
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // 隱藏播放控制條
                // 設定縮放模式為 ZOOM (類似 ContentScale.Crop，填滿畫面不留黑邊)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun CatInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {

    // --- [調整區：這裡控制大小與位置] ---

    // 1. 位置設定
    val farCatTopOffset = 180.dp      // 遠處：距離頂部
    val nearCatBottomOffset = -10.dp   // 近處：距離底部 (貼底)

    // 2. 大小設定 (製造透視感：近大遠小)
    val farCatSize = 120.dp
    val nearCatSize = 200.dp

    // ------------------

    // 貓咪資料庫
    val allCats = remember {
        listOf(
            // --- 貓咪 1 (橘貓) ---
            CatAsset(
                frames = listOf(R.drawable.cat1_2, R.drawable.cat1_3, R.drawable.cat1_4),
                soundRes = R.raw.cat_meow,
                name = "Orange Cat"
            ),
            // --- 貓咪 2 (灰貓) ---
            CatAsset(
                frames = listOf(R.drawable.cat2_1, R.drawable.cat2_2, R.drawable.cat2_3),
                soundRes = R.raw.meow2,
                name = "Grey Cat"
            ),
            // --- 貓咪 3 (白貓) ---
            CatAsset(
                frames = listOf(R.drawable.cat3_1, R.drawable.cat3_2, R.drawable.cat3_3),
                soundRes = R.raw.meow3,
                name = "White Cat"
            )
        )
    }

    // --- 狀態管理：確保兩隻貓不同 ---
    var nearCatIndex by remember { mutableIntStateOf(0) }
    var farCatIndex by remember { mutableIntStateOf(1) }

    // 換貓邏輯
    fun getNextUniqueCat(currentIndex: Int, otherIndex: Int): Int {
        var next = (currentIndex + 1) % allCats.size
        if (next == otherIndex) {
            next = (next + 1) % allCats.size
        }
        return next
    }

    // 這裡指向你的影片檔案 (res/raw/catbackground.mp4)
    val backgroundResId = R.raw.catbackground

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()

        // 計算位置 (px)
        val farYPx = with(density) { farCatTopOffset.toPx().toInt() }

        val nearCatSizePx = with(density) { nearCatSize.toPx() }
        val bottomPaddingPx = with(density) { nearCatBottomOffset.toPx() }
        val nearYPx = (screenHeightPx - nearCatSizePx - bottomPaddingPx).toInt()

        // 1. 背景 (改用 VideoBackground)
        VideoBackground(videoResId = backgroundResId)

        // 2. 互動區

        // --- (A) 遠處的貓 (比較小) ---
        RotatingCat(
            catData = allCats[farCatIndex],
            soundManager = soundManager,
            yOffsetPx = farYPx,
            catDisplaySize = farCatSize,
            startFromRight = true,
            moveDuration = 6000,
            onCycleComplete = {
                farCatIndex = getNextUniqueCat(farCatIndex, nearCatIndex)
            }
        )

        // --- (B) 近處的貓 (比較大) ---
        RotatingCat(
            catData = allCats[nearCatIndex],
            soundManager = soundManager,
            yOffsetPx = nearYPx,
            catDisplaySize = nearCatSize,
            startFromRight = false,
            moveDuration = 4000,
            onCycleComplete = {
                nearCatIndex = getNextUniqueCat(nearCatIndex, farCatIndex)
            }
        )

        // 3. 返回按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.height(50.dp)
            ) {
                Text("← 返回", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * RotatingCat 元件 (保持不變)
 */
@Composable
fun RotatingCat(
    catData: CatAsset,
    soundManager: SoundManager,
    yOffsetPx: Int,
    catDisplaySize: androidx.compose.ui.unit.Dp,
    startFromRight: Boolean,
    moveDuration: Int,
    onCycleComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val catSizePx = with(density) { catDisplaySize.toPx() }

    val startX = if (startFromRight) screenWidthPx else -catSizePx
    val endX = if (startFromRight) -catSizePx else screenWidthPx

    // 安全性檢查
    if (catData.frames.isEmpty()) return

    // 動作動畫
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(catData) {
        currentFrameIndex = 0
        while(true) {
            delay(150)
            if (catData.frames.isNotEmpty()) {
                currentFrameIndex = (currentFrameIndex + 1) % catData.frames.size
            }
        }
    }

    // 移動動畫
    val xOffsetAnim = remember { Animatable(startX) }

    LaunchedEffect(key1 = startFromRight, key2 = moveDuration) {
        delay(Random.nextLong(0, 1000))
        while(true) {
            xOffsetAnim.animateTo(
                targetValue = endX,
                animationSpec = tween(durationMillis = moveDuration, easing = LinearEasing)
            )
            xOffsetAnim.snapTo(startX)
            onCycleComplete()
            delay(Random.nextLong(1000, 2500))
        }
    }

    // 點擊效果
    var isTapped by remember { mutableStateOf(false) }
    val tapScale = animateFloatAsState(if (isTapped) 1.2f else 1.0f, tween(100), label = "tap")

    val directionScaleX = if (startFromRight) -1f else 1f
    val finalScale = tapScale.value

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffsetAnim.value.toInt(), yOffsetPx) }
            .size(catDisplaySize)
            .scale(finalScale)
            .scale(scaleX = directionScaleX, scaleY = 1f)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    try {
                        soundManager.playSound(catData.soundRes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    isTapped = true
                })
            }
    ) {
        if (isTapped) { LaunchedEffect(Unit) { delay(150); isTapped = false } }

        val safeIndex = currentFrameIndex % catData.frames.size

        Image(
            painter = painterResource(id = catData.frames[safeIndex]),
            contentDescription = catData.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}