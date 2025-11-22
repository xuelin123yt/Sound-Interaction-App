package com.soundinteractionapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlin.random.Random
import kotlinx.coroutines.delay

// 確保 R 類別可以被識別
import com.soundinteractionapp.R

/**
 * 狗狗互動畫面，包含單一可點擊、會單向連續移動且具有連續動畫的狗狗。
 * 這是 CatInteractionScreen 的複製品，用於狗狗功能。
 */
@Composable
fun DogInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {

    // 隨機狗叫聲資源列表 (目前只使用一個檔案: R.raw.dog_barking)
    val dogSoundResources = remember {
        listOf(
            R.raw.dog_barking // <-- 使用狗狗的叫聲資源
        )
    }

    // 狗狗動畫幀資源列表 (請確保這些檔案存在於 res/drawable 目錄中: dog1_1.png, dog1_2.png, ...)
    val dogFrames = remember {
        listOf(
            R.drawable.dog1_1, // <-- 更新為 dog1_X 命名
            R.drawable.dog1_2,
            R.drawable.dog1_3,
            R.drawable.dog1_4
        )
    }

    // 背景圖片資源 ID
    // TODO: 請在 res/drawable 放入您的背景圖片 dog_background.jpg 或 .png
    val backgroundResId = R.drawable.dog_background

    // 使用 Box 進行堆疊：背景 -> 狗狗 -> 返回按鈕
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. 背景圖片 (放在最底層)
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = "狗狗互動背景",
            contentScale = ContentScale.Crop, // 確保圖片填滿整個橫向螢幕
            modifier = Modifier.fillMaxSize()
        )

        // 2. 互動區：單一狗狗
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp) // 避免與返回按鈕重疊
        ) {
            MovingDog(
                dogFrames = dogFrames, // 傳遞動畫幀
                soundManager = soundManager,
                dogSoundResources = dogSoundResources
            )
        }

        // 3. 頂部返回按鈕 (放在最上層)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.height(50.dp)
            ) {
                Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * 會單向連續移動且可互動的單一狗狗 Composable，具有逐幀動畫。
 */
@Composable
fun MovingDog(
    dogFrames: List<Int>,
    soundManager: SoundManager,
    dogSoundResources: List<Int>
) {
    // 獲取螢幕尺寸（DP）
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // 獲取密度並進行轉換
    val density = LocalDensity.current

    // 狗狗圖片的固定尺寸
    val dogSize = 250.dp

    // 將 DP 尺寸轉換為 Int
    val dogSizeInt = with(density) { dogSize.toPx().toInt() }
    val screenWidthInt = with(density) { screenWidthDp.dp.toPx().toInt() }

    // Y 軸固定在中央偏下的位置 (距離頂部約 100dp)
    val fixedYOffsetDp = 100.dp
    val fixedYOffsetInt = with(density) { fixedYOffsetDp.toPx().toInt() }

    // 移動的目標 X 座標範圍 (從 0 到最右邊界)
    // Animatable 使用 Float，因此轉換為 Float
    val maxX = screenWidthInt.toFloat() - dogSizeInt.toFloat()

    // 1. 動作動畫狀態 (逐幀動畫)
    var currentFrameIndex by remember { mutableStateOf(0) }

    // 啟動逐幀動畫循環
    LaunchedEffect(Unit) {
        while(true) {
            delay(100) // 每 100 毫秒切換一幀
            currentFrameIndex = (currentFrameIndex + 1) % dogFrames.size
        }
    }

    // 2. 移動動畫狀態 (使用 Animatable 實現單向移動和瞬間重置)

    // 狀態：追蹤當前 X 座標。 Animatable 預設為 Float
    val xOffset = remember { Animatable(0f) }

    // 控制單向循環的邏輯 (使用 Animatable 確保到達邊緣和瞬間重置)
    LaunchedEffect(key1 = Unit) {
        while(true) {
            // 步驟 A: 動畫到最右側 (maxX)
            xOffset.animateTo(
                targetValue = maxX,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing) // 3秒移動
            )

            // 步驟 B: 瞬間重置回左側 (0)
            xOffset.snapTo(0f)

            // 步驟 C: 短暫等待後再次啟動
            delay(50)
        }
    }

    // 點擊回饋狀態 (縮放動畫)
    var isTapped by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (isTapped) 1.2f else 1.0f, // 點擊時放大 20%
        animationSpec = tween(durationMillis = 100),
        label = "dogTapScale"
    )

    // 點擊區域和動畫
    Box(
        modifier = Modifier
            .size(dogSize)
            // 應用移動動畫位置 (將 Float 轉換為 IntOffset)
            .offset { IntOffset(xOffset.value.toInt(), fixedYOffsetInt) }
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // 1. 播放單一狗叫聲
                        val soundId = dogSoundResources.random()
                        soundManager.playSound(soundId)

                        // 2. 觸發視覺回饋 (縮放動畫)
                        isTapped = true
                    }
                )
            }
    ) {
        // 應用 tap 結束後的視覺效果重置
        LaunchedEffect(isTapped) {
            if (isTapped) {
                delay(100)
                isTapped = false
            }
        }

        // 狗狗的圖片 (載入當前幀)
        Image(
            painter = painterResource(id = dogFrames[currentFrameIndex]), // 使用逐幀動畫的圖片
            contentDescription = "互動狗狗",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // 顯示狗狗名字，方便識別
        Text(
            text = "Click Me!",
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = 30.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp
        )
    }
}