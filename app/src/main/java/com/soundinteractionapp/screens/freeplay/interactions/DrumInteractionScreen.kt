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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager

/**
 * 爵士鼓組件數據類別
 */
data class DrumComponentData(
    val name: String,
    val soundResId: Int,
    val sizeW: Dp,
    val sizeH: Dp,
    val offsetX: Dp,
    val offsetY: Dp
)

/**
 * 爵士鼓互動畫面，提供 8 個精確定位的鼓面和鈸。
 */
@Composable
fun DrumInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    var isNavigating by remember { mutableStateOf(false) }

    val drumSounds = remember {
        listOf(
            R.raw.drum_cymbal_closed, R.raw.drum_snare_hard, R.raw.drum_bass_hard,
            R.raw.drum_tom_lo_soft, R.raw.drum_tom_hi_hard, R.raw.drum_tom_mid_soft,
            R.raw.drum_cymbal_hard, R.raw.drum_cymbal_hard
        )
    }

    val drumComponents = remember {
        listOf(
            DrumComponentData("Hi-Hat", drumSounds[0], 150.dp, 110.dp, 15.dp, 120.dp),
            DrumComponentData("Snare", drumSounds[1], 120.dp, 120.dp, 170.dp, 170.dp),
            DrumComponentData("Tom 1", drumSounds[4], 100.dp, 90.dp, 210.dp, 70.dp),
            DrumComponentData("Tom 2", drumSounds[5], 100.dp, 90.dp, 330.dp, 70.dp),
            DrumComponentData("Ride", drumSounds[6], 200.dp, 150.dp, 440.dp, 20.dp),
            DrumComponentData("Floor Tom", drumSounds[3], 150.dp, 120.dp, 390.dp, 180.dp),
            DrumComponentData("Kick", drumSounds[2], 110.dp, 120.dp, 290.dp, 200.dp),
            DrumComponentData("Crash Cymbal", drumSounds[7], 180.dp, 130.dp, 100.dp, 20.dp)
        )
    }

    var tappedDrumId by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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

/**
 * 爵士鼓的單一可點擊鼓面/鈸 (Drum Pad)
 */
@Composable
fun DrumPad(
    id: Int,
    data: DrumComponentData,
    isTapped: Boolean,
    soundManager: SoundManager,
    onTap: () -> Unit
) {
    // 視覺回饋：脈衝動畫
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 1.05f else 1.0f, // 被敲擊時放大 5%
        animationSpec = tween(durationMillis = 50),
        label = "drumPadScale"
    )

    // 移除白色覆蓋層，將顏色設為完全透明
    val colorOverlay by animateColorAsState(
        targetValue = Color.Transparent, // 永遠透明
        animationSpec = tween(durationMillis = 100),
        label = "drumColor"
    )

    Box(
        modifier = Modifier
            .offset(x = data.offsetX, y = data.offsetY) // 精確定位
            .size(data.sizeW, data.sizeH) // 設定點擊區尺寸
            .scale(scale) // 應用縮放
            .background(color = colorOverlay, shape = MaterialTheme.shapes.extraLarge)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // 播放聲音
                        soundManager.playSound(data.soundResId)
                        // 觸發視覺狀態
                        onTap()
                    }
                )
            }
    )
    /*
    // 提示：可以取消註釋下面這段程式碼，方便調試點擊區域是否準確
    {
        Text(
            text = data.name,
            modifier = Modifier.align(Alignment.Center),
            color = if (isTapped) Color.Black else Color.White,
            fontSize = 12.sp
        )
    }
    */
}