package com.soundinteractionapp.screens.freeplay.interactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.utils.VolumeKeys

/**
 * 爵士鼓組件數據類別 - 使用百分比定位
 */
data class DrumComponentData(
    val name: String,
    val soundResId: Int,
    val widthPercent: Float,    // 寬度百分比 (0-1)
    val heightPercent: Float,   // 高度百分比 (0-1)
    val offsetXPercent: Float,  // X 偏移百分比 (0-1)
    val offsetYPercent: Float   // Y 偏移百分比 (0-1)
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

    // ✅ 使用百分比定位，適應所有螢幕比例
    val drumComponents = remember {
        listOf(
            // Hi-Hat (左上)
            DrumComponentData("Hi-Hat", R.raw.drum_cymbal_closed,
                0.18f, 0.28f, 0.02f, 0.30f),
            // Snare (左中下)
            DrumComponentData("Snare", R.raw.drum_snare_hard,
                0.15f, 0.30f, 0.22f, 0.45f),
            // Tom 1 (中左上)
            DrumComponentData("Tom 1", R.raw.drum_tom_hi_hard,
                0.12f, 0.22f, 0.28f, 0.18f),
            // Tom 2 (中右上)
            DrumComponentData("Tom 2", R.raw.drum_tom_mid_soft,
                0.12f, 0.22f, 0.44f, 0.18f),
            // Ride (右上)
            DrumComponentData("Ride", R.raw.drum_cymbal_hard,
                0.22f, 0.35f, 0.72f, 0.05f),
            // Floor Tom (右下)
            DrumComponentData("Floor Tom", R.raw.drum_tom_lo_soft,
                0.18f, 0.30f, 0.62f, 0.48f),
            // Kick (中下)
            DrumComponentData("Kick", R.raw.drum_bass_hard,
                0.14f, 0.30f, 0.38f, 0.52f),
            // Crash Cymbal (左上方)
            DrumComponentData("Crash Cymbal", R.raw.drum_cymbal_hard,
                0.22f, 0.32f, 0.12f, 0.02f)
        )
    }

    var tappedDrumId by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

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
                screenWidth = screenWidth,
                screenHeight = screenHeight,
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
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    isTapped: Boolean,
    soundManager: SoundManager,
    onTap: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 50),
        label = "drumPadScale"
    )

    val colorOverlay by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = tween(durationMillis = 100),
        label = "drumColor"
    )

    // ✅ 根據螢幕尺寸計算實際位置和大小
    val actualWidth = screenWidth * data.widthPercent
    val actualHeight = screenHeight * data.heightPercent
    val actualOffsetX = screenWidth * data.offsetXPercent
    val actualOffsetY = screenHeight * data.offsetYPercent

    Box(
        modifier = Modifier
            .offset(x = actualOffsetX, y = actualOffsetY)
            .size(actualWidth, actualHeight)
            .scale(scale)
            .background(color = colorOverlay, shape = MaterialTheme.shapes.extraLarge)
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