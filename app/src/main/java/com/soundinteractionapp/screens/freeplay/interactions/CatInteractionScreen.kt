package com.soundinteractionapp.screens.freeplay.interactions

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.game.levels.VideoBackground
import com.soundinteractionapp.utils.VolumeKeys
import kotlinx.coroutines.delay
import kotlin.random.Random

// 資料類別
data class CatAsset(
    val frames: List<Int>,
    val soundRes: Int,
    val volumeKey: String,  // ✅ 新增
    val name: String
)

/**
 * 專門用來播放背景影片的元件
 * 使用 ExoPlayer + AndroidView
 */
@OptIn(UnstableApi::class)
@Composable
fun CatInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    var isNavigating by remember { mutableStateOf(false) }

    val farCatTopOffset = 180.dp
    val nearCatBottomOffset = -10.dp
    val farCatSize = 120.dp
    val nearCatSize = 200.dp

    val allCats = remember {
        listOf(
            CatAsset(
                frames = listOf(R.drawable.cat1_2, R.drawable.cat1_3, R.drawable.cat1_4),
                soundRes = R.raw.meow1,
                volumeKey = VolumeKeys.FREEPLAY_CAT1,
                name = "Orange Cat"
            ),
            CatAsset(
                frames = listOf(R.drawable.cat2_1, R.drawable.cat2_2, R.drawable.cat2_3),
                soundRes = R.raw.meow2,
                volumeKey = VolumeKeys.FREEPLAY_CAT2,
                name = "Grey Cat"
            ),
            CatAsset(
                frames = listOf(R.drawable.cat3_1, R.drawable.cat3_2, R.drawable.cat3_3),
                soundRes = R.raw.meow3,
                volumeKey = VolumeKeys.FREEPLAY_CAT3,  // ⚠️ 如果這個常數不存在會出錯
                name = "White Cat"
            )
        )
    }

    var nearCatIndex by remember { mutableIntStateOf(0) }
    var farCatIndex by remember { mutableIntStateOf(1) }

    fun getNextUniqueCat(currentIndex: Int, otherIndex: Int): Int {
        var next = (currentIndex + 1) % allCats.size
        if (next == otherIndex) next = (next + 1) % allCats.size
        return next
    }

    val backgroundResId = R.raw.catbackground

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()
        val farYPx = with(density) { farCatTopOffset.toPx().toInt() }
        val nearCatSizePx = with(density) { nearCatSize.toPx() }
        val bottomPaddingPx = with(density) { nearCatBottomOffset.toPx() }
        val nearYPx = (screenHeightPx - nearCatSizePx - bottomPaddingPx).toInt()

        VideoBackground(videoResId = backgroundResId)

        RotatingCat(
            catData = allCats[farCatIndex],
            soundManager = soundManager,
            yOffsetPx = farYPx,
            catDisplaySize = farCatSize,
            startFromRight = true,
            moveDuration = 6000,
            onCycleComplete = { farCatIndex = getNextUniqueCat(farCatIndex, nearCatIndex) }
        )

        RotatingCat(
            catData = allCats[nearCatIndex],
            soundManager = soundManager,
            yOffsetPx = nearYPx,
            catDisplaySize = nearCatSize,
            startFromRight = false,
            moveDuration = 4000,
            onCycleComplete = { nearCatIndex = getNextUniqueCat(nearCatIndex, farCatIndex) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    soundManager.playSFX("cancel")
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
            .pointerInput(catData) {  // ✅ 改成 catData，當貓咪改變時重新建立監聽器
                detectTapGestures(onTap = {
                    try {
                        soundManager.playSound(catData.soundRes, catData.volumeKey)
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