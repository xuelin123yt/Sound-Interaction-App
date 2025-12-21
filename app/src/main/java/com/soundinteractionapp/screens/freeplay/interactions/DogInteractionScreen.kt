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
import com.soundinteractionapp.utils.VolumeKeys
import kotlinx.coroutines.delay
import kotlin.random.Random

// 狗狗資料類別
data class DogAsset(
    val frames: List<Int>,
    val soundKey: String,
    val volumeKey: String,  // ✅ 新增音量鍵
    val name: String
)

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun DogInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    var isNavigating by remember { mutableStateOf(false) }

    val farDogSize = 130.dp
    val nearDogSize = 180.dp
    val farDogTopOffset = 180.dp
    val nearDogBottomOffset = 10.dp

    val allDogs = remember {
        listOf(
            DogAsset(
                frames = listOf(R.drawable.dog1_1, R.drawable.dog1_2, R.drawable.dog1_3, R.drawable.dog1_4),
                soundKey = "dog_bark1",
                volumeKey = VolumeKeys.FREEPLAY_DOG1,  // ✅ 黃金獵犬
                name = "Golden Retriever"
            ),
            DogAsset(
                frames = listOf(R.drawable.dog2_1, R.drawable.dog2_2, R.drawable.dog2_3),
                soundKey = "dog_bark2",
                volumeKey = VolumeKeys.FREEPLAY_DOG2,  // ✅ 柴犬
                name = "Shiba"
            ),
            DogAsset(
                frames = listOf(R.drawable.dog3_1, R.drawable.dog3_2, R.drawable.dog3_3),
                soundKey = "dog_bark3",
                volumeKey = VolumeKeys.FREEPLAY_DOG3,  // ✅ 哈士奇
                name = "Husky"
            )
        )
    }

    var nearDog by remember { mutableStateOf(allDogs[0]) }
    var farDog by remember { mutableStateOf(allDogs[1]) }

    fun getNextUniqueDog(currentDog: DogAsset, otherDog: DogAsset): DogAsset {
        val currentIndex = allDogs.indexOf(currentDog)
        var nextIndex = (currentIndex + 1) % allDogs.size
        if (allDogs[nextIndex] == otherDog) {
            nextIndex = (nextIndex + 1) % allDogs.size
        }
        return allDogs[nextIndex]
    }

    val backgroundResId = R.raw.dogbackground

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()

        val farYPx = with(density) { farDogTopOffset.toPx().toInt() }
        val nearDogSizePx = with(density) { nearDogSize.toPx() }
        val bottomPaddingPx = with(density) { nearDogBottomOffset.toPx() }
        val nearYPx = (screenHeightPx - nearDogSizePx - bottomPaddingPx).toInt()

        VideoBackground(videoResId = backgroundResId)

        // 遠處的狗 (較慢)
        RotatingDog(
            dogData = farDog,
            soundManager = soundManager,
            yOffsetPx = farYPx,
            dogDisplaySize = farDogSize,
            startFromRight = true,
            moveDuration = 8000,
            onCycleComplete = { farDog = getNextUniqueDog(farDog, nearDog) }
        )

        // 近處的狗 (較快)
        RotatingDog(
            dogData = nearDog,
            soundManager = soundManager,
            yOffsetPx = nearYPx,
            dogDisplaySize = nearDogSize,
            startFromRight = false,
            moveDuration = 4000,
            onCycleComplete = { nearDog = getNextUniqueDog(nearDog, farDog) }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart)
        ) {
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    soundManager.playSFX("cancel")
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
fun RotatingDog(
    dogData: DogAsset,
    soundManager: SoundManager,
    yOffsetPx: Int,
    dogDisplaySize: androidx.compose.ui.unit.Dp,
    startFromRight: Boolean,
    moveDuration: Int,
    onCycleComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val dogSizePx = with(density) { dogDisplaySize.toPx() }

    val startX = if (startFromRight) screenWidthPx else -dogSizePx
    val endX = if (startFromRight) -dogSizePx else screenWidthPx

    if (dogData.frames.isEmpty()) return

    // 幀動畫
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(dogData) {
        currentFrameIndex = 0
        while(true) {
            delay(130)
            currentFrameIndex = (currentFrameIndex + 1) % dogData.frames.size
        }
    }

    // 移動動畫
    val xOffsetAnim = remember { Animatable(startX) }
    LaunchedEffect(key1 = startFromRight, key2 = moveDuration, key3 = dogData) {
        xOffsetAnim.snapTo(startX)
        delay(Random.nextLong(0, 500))
        while(true) {
            xOffsetAnim.animateTo(
                targetValue = endX,
                animationSpec = tween(durationMillis = moveDuration, easing = LinearEasing)
            )
            xOffsetAnim.snapTo(startX)
            onCycleComplete()
            delay(Random.nextLong(1000, 3000))
        }
    }

    // 點擊互動狀態
    var isTapped by remember { mutableStateOf(false) }
    val tapScale = animateFloatAsState(if (isTapped) 1.25f else 1.0f, tween(100), label = "tap")
    val interactionSource = remember { MutableInteractionSource() }

    val directionScaleX = if (startFromRight) -1f else 1f

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffsetAnim.value.toInt(), yOffsetPx) }
            .size(dogDisplaySize)
            .scale(tapScale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                Log.d("DogDebug", "Clicked on dog: ${dogData.name}, Key: ${dogData.soundKey}, Volume: ${dogData.volumeKey}")
                isTapped = true

                try {
                    // ✅ 使用各自的音量鍵
                    soundManager.playSFX(dogData.soundKey, dogData.volumeKey)
                } catch (e: Exception) {
                    Log.e("DogDebug", "Error playing sound: ${e.message}")
                }
            }
    ) {
        // 自動復原點擊狀態
        if (isTapped) { LaunchedEffect(Unit) { delay(150); isTapped = false } }

        Image(
            painter = painterResource(id = dogData.frames[currentFrameIndex % dogData.frames.size]),
            contentDescription = dogData.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = directionScaleX, scaleY = 1f)
        )
    }
}