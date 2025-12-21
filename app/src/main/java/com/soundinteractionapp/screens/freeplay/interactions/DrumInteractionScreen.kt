package com.soundinteractionapp.screens.freeplay.interactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.utils.VolumeKeys

/**
 * 爵士鼓組件數據類別 - 基於圖片原始比例的百分比定位
 */
data class DrumComponentData(
    val name: String,
    val soundResId: Int,
    val widthPercent: Float,
    val heightPercent: Float,
    val centerXPercent: Float,  // 中心點 X 百分比
    val centerYPercent: Float   // 中心點 Y 百分比
)

@Composable
fun DrumInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    var isNavigating by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        soundManager.pauseBGM()
        onDispose {
            soundManager.resumeBGM()
        }
    }

    // ✅ 基於圖片內容的百分比定位（中心點）- 針對 16:9 (1920x1080) 精確優化
    val drumComponents = remember {
        listOf(
            // Hi-Hat (左下金色小鈸)
            DrumComponentData("Hi-Hat", R.raw.drum_cymbal_closed,
                0.14f, 0.22f, 0.19f, 0.48f),
            // Crash Cymbal (左上大鈸)
            DrumComponentData("Crash Cymbal", R.raw.drum_cymbal_hard,
                0.20f, 0.26f, 0.24f, 0.18f),
            // Snare (前方左側白色小鼓)
            DrumComponentData("Snare", R.raw.drum_snare_hard,
                0.16f, 0.20f, 0.36f, 0.63f),
            // Tom 1 (中左上方小鼓) - 正方形
            DrumComponentData("Tom 1", R.raw.drum_tom_hi_hard,
                0.14f, 0.14f, 0.42f, 0.30f),
            // Tom 2 (中右上方小鼓) - 正方形
            DrumComponentData("Tom 2", R.raw.drum_tom_mid_soft,
                0.14f, 0.14f, 0.56f, 0.30f),
            // Kick (中間大鼓)
            DrumComponentData("Kick", R.raw.drum_bass_hard,
                0.13f, 0.24f, 0.52f, 0.70f),
            // Floor Tom (右下大鼓) - 往右一點
            DrumComponentData("Floor Tom", R.raw.drum_tom_lo_soft,
                0.15f, 0.20f, 0.70f, 0.66f),
            // Ride (右上大鈸) - 範圍大一點
            DrumComponentData("Ride", R.raw.drum_cymbal_hard,
                0.25f, 0.32f, 0.80f, 0.24f)
        )
    }

    var tappedDrumId by remember { mutableStateOf<Int?>(null) }

    // 取得圖片原始尺寸
    val context = LocalContext.current
    val imageRatio = remember {
        val drawable = context.resources.getDrawable(R.drawable.drum_background, null)
        drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val screenRatio = screenWidth / screenHeight

        // ✅ 計算圖片實際顯示區域（考慮 Crop 裁切）
        val (imageDisplayWidth, imageDisplayHeight, imageOffsetX, imageOffsetY) = remember(screenWidth, screenHeight) {
            if (screenRatio > imageRatio) {
                // 螢幕較寬，圖片寬度填滿，高度會被裁切
                val displayWidth = screenWidth
                val displayHeight = screenWidth / imageRatio
                val offsetX = 0.dp
                val offsetY = (screenHeight - displayHeight) / 2
                listOf(displayWidth, displayHeight, offsetX, offsetY)
            } else {
                // 螢幕較高，圖片高度填滿，寬度會被裁切
                val displayHeight = screenHeight
                val displayWidth = screenHeight * imageRatio
                val offsetX = (screenWidth - displayWidth) / 2
                val offsetY = 0.dp
                listOf(displayWidth, displayHeight, offsetX, offsetY)
            }
        }

        Image(
            painter = painterResource(id = R.drawable.drum_background),
            contentDescription = "爵士鼓背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        drumComponents.forEachIndexed { index, data ->
            DrumPad(
                id = index,
                data = data,
                imageDisplayWidth = imageDisplayWidth,
                imageDisplayHeight = imageDisplayHeight,
                imageOffsetX = imageOffsetX,
                imageOffsetY = imageOffsetY,
                isTapped = tappedDrumId == index,
                soundManager = soundManager,
                onTap = { tappedDrumId = index }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    }

    LaunchedEffect(tappedDrumId) {
        if (tappedDrumId != null) {
            delay(150)
            tappedDrumId = null
        }
    }
}

@Composable
fun DrumPad(
    id: Int,
    data: DrumComponentData,
    imageDisplayWidth: Dp,
    imageDisplayHeight: Dp,
    imageOffsetX: Dp,
    imageOffsetY: Dp,
    isTapped: Boolean,
    soundManager: SoundManager,
    onTap: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 50),
        label = "drumPadScale"
    )

    // ✅ 根據圖片實際顯示區域計算位置
    val actualWidth = imageDisplayWidth * data.widthPercent
    val actualHeight = imageDisplayHeight * data.heightPercent
    val centerX = imageOffsetX + imageDisplayWidth * data.centerXPercent
    val centerY = imageOffsetY + imageDisplayHeight * data.centerYPercent
    val actualOffsetX = centerX - actualWidth / 2
    val actualOffsetY = centerY - actualHeight / 2

    Box(
        modifier = Modifier
            .offset(x = actualOffsetX, y = actualOffsetY)
            .size(actualWidth, actualHeight)
            .scale(scale)
            // ✅ 移除淡紫色背景
            // .background(Color(0x4DBA68C8))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        soundManager.playSound(data.soundResId, VolumeKeys.FREEPLAY_DRUM)
                        onTap()
                    }
                )
            }
    )
}