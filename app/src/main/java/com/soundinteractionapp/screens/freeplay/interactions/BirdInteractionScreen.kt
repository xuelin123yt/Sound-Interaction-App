package com.soundinteractionapp.screens.freeplay.interactions

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.game.levels.VideoBackground
import kotlinx.coroutines.delay
import kotlin.random.Random

// 鳥兒資料類別
data class BirdAsset(
    val frames: List<Int>,
    val name: String
)

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun BirdInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    var isNavigating by remember { mutableStateOf(false) }

    // 設定視覺大小 (遠小近大)
    val farBirdSize = 110.dp
    val nearBirdSize = 180.dp

    // 定義三種不同的鳥類資源
    val allBirds = remember {
        listOf(
            BirdAsset(
                frames = listOf(R.drawable.bird1_1, R.drawable.bird1_2, R.drawable.bird1_3, R.drawable.bird1_4),
                name = "Bird Type 1"
            ),
            BirdAsset(
                frames = listOf(R.drawable.bird2_1, R.drawable.bird2_2),
                name = "Bird Type 2"
            ),
            BirdAsset(
                frames = listOf(R.drawable.bird3_1, R.drawable.bird3_2),
                name = "Bird Type 3"
            )
        )
    }

    // 當前畫面上的兩隻鳥
    var nearBird by remember { mutableStateOf(allBirds[0]) }
    var farBird by remember { mutableStateOf(allBirds[1]) }

    // 取得下一隻不重複鳥類的邏輯
    fun getNextUniqueBird(currentBird: BirdAsset, otherBird: BirdAsset): BirdAsset {
        val currentIndex = allBirds.indexOf(currentBird)
        var nextIndex = (currentIndex + 1) % allBirds.size
        // 確保不會跟另一層的鳥重複
        if (allBirds[nextIndex] == otherBird) {
            nextIndex = (nextIndex + 1) % allBirds.size
        }
        return allBirds[nextIndex]
    }

    val backgroundResId = R.raw.birdbackground

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()

        // --- 修正：使用百分比計算高度，確保不跑出螢幕 ---
        // 遠處的鳥：位於螢幕上方 20%
        val farYPx = (screenHeightPx * 0.2f).toInt()
        // 近處的鳥：位於螢幕中間 50% (原 250dp 容易跑出去)
        val nearYPx = (screenHeightPx * 0.5f).toInt()

        // 背景影片
        VideoBackground(videoResId = backgroundResId)

        // 遠處的鳥 (較慢，從右向左飛)
        RotatingBird(
            birdData = farBird,
            soundManager = soundManager,
            yOffsetPx = farYPx,
            birdDisplaySize = farBirdSize,
            startFromRight = true,
            moveDuration = 9500,
            onCycleComplete = { farBird = getNextUniqueBird(farBird, nearBird) }
        )

        // 近處的鳥 (較快，從左向右飛)
        RotatingBird(
            birdData = nearBird,
            soundManager = soundManager,
            yOffsetPx = nearYPx,
            birdDisplaySize = nearBirdSize,
            startFromRight = false,
            moveDuration = 5500,
            onCycleComplete = { nearBird = getNextUniqueBird(nearBird, farBird) }
        )

        // 返回按鈕
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart)
        ) {
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.height(50.dp),
                enabled = !isNavigating
            ) {
                Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun RotatingBird(
    birdData: BirdAsset,
    soundManager: SoundManager,
    yOffsetPx: Int,
    birdDisplaySize: androidx.compose.ui.unit.Dp,
    startFromRight: Boolean,
    moveDuration: Int,
    onCycleComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val birdSizePx = with(density) { birdDisplaySize.toPx() }

    // 起點與終點
    val startX = if (startFromRight) screenWidthPx else -birdSizePx
    val endX = if (startFromRight) -birdSizePx else screenWidthPx

    if (birdData.frames.isEmpty()) return

    // 1. 幀動畫 (翅膀拍打頻率)
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(birdData) {
        currentFrameIndex = 0
        while(true) {
            delay(130) // 每秒約 8 幀
            currentFrameIndex = (currentFrameIndex + 1) % birdData.frames.size
        }
    }

    // 2. 水平移動動畫
    val xOffsetAnim = remember { Animatable(startX) }
    LaunchedEffect(key1 = startFromRight, key2 = birdData) {
        xOffsetAnim.snapTo(startX)
        delay(Random.nextLong(0, 800)) // 隨機出發時間
        while(true) {
            xOffsetAnim.animateTo(
                targetValue = endX,
                animationSpec = tween(durationMillis = moveDuration, easing = LinearEasing)
            )
            // 完成一趟後重置並切換種類
            xOffsetAnim.snapTo(startX)
            onCycleComplete()
            delay(Random.nextLong(1000, 3000))
        }
    }

    // 3. 點擊縮放狀態
    var isTapped by remember { mutableStateOf(false) }
    val tapScale = animateFloatAsState(if (isTapped) 1.25f else 1.0f, tween(100), label = "birdScale")
    val interactionSource = remember { MutableInteractionSource() }

    // 視覺翻轉：根據飛行方向決定圖片朝向
    val directionScaleX = if (startFromRight) -1f else 1f

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffsetAnim.value.toInt(), yOffsetPx) }
            .size(birdDisplaySize)
            .scale(tapScale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isTapped = true
                // 播放單一鳥叫聲
                soundManager.playSound(R.raw.bird_sound)
                Log.d("BirdInteraction", "Clicked ${birdData.name}")
            }
    ) {
        if (isTapped) { LaunchedEffect(Unit) { delay(150); isTapped = false } }

        Image(
            painter = painterResource(id = birdData.frames[currentFrameIndex % birdData.frames.size]),
            contentDescription = birdData.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = directionScaleX, scaleY = 1f)
        )
    }
}