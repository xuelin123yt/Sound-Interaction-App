package com.soundinteractionapp

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

    // 鼓組件音源列表 (使用您的 MP3 檔案名稱)
    val drumSounds = remember {
        listOf(
            R.raw.drum_cymbal_closed, // 0. Hi-Hat (腳踏鈸)
            R.raw.drum_snare_hard,    // 1. Snare (小鼓)
            R.raw.drum_bass_hard,     // 2. Kick Drum (大鼓)
            R.raw.drum_tom_lo_soft,   // 3. Floor Tom (落地鼓)
            R.raw.drum_tom_hi_hard,   // 4. Rack Tom 1 (高音 Tom)
            R.raw.drum_tom_mid_soft,  // 5. Rack Tom 2 (中音 Tom)
            R.raw.drum_cymbal_hard,   // 6. Ride Cymbal (疊音鈸)
            R.raw.drum_cymbal_hard    // 7. Crash Cymbal (左上墜擊鈸)
        )
    }

    // 鼓組件 UI 數據 - [使用您提供的精確位置數據]
    val drumComponents = remember {
        listOf(
            // 0. Hi-Hat Cymbal (左上角的鈸 - 調整位置和尺寸以匹配側視圖)
            DrumComponentData(name = "Hi-Hat", soundResId = drumSounds[0],
                sizeW = 150.dp, sizeH = 110.dp, offsetX = 15.dp, offsetY = 120.dp),

            // 1. Snare Drum (左前方的小鼓)
            DrumComponentData(name = "Snare", soundResId = drumSounds[1],
                sizeW = 120.dp, sizeH = 120.dp, offsetX = 170.dp, offsetY = 170.dp),

            // 2. Rack Tom 1 (後排左邊的 Tom)
            DrumComponentData(name = "Tom 1", soundResId = drumSounds[4],
                sizeW = 100.dp, sizeH = 90.dp, offsetX = 210.dp, offsetY = 70.dp),

            // 3. Rack Tom 2 (後排右邊的 Tom)
            DrumComponentData(name = "Tom 2", soundResId = drumSounds[5],
                sizeW = 100.dp, sizeH = 90.dp, offsetX = 330.dp, offsetY = 70.dp),

            // 4. Ride Cymbal (右上角的鈸)
            DrumComponentData(name = "Ride", soundResId = drumSounds[6],
                sizeW = 200.dp, sizeH = 150.dp, offsetX = 440.dp, offsetY = 20.dp),

            // 5. Floor Tom (右下方的落地鼓)
            DrumComponentData(name = "Floor Tom", soundResId = drumSounds[3],
                sizeW = 150.dp, sizeH = 120.dp, offsetX = 390.dp, offsetY = 180.dp),

            // 6. Kick Drum (中間的大鼓,底部點擊區)
            DrumComponentData(name = "Kick", soundResId = drumSounds[2],
                sizeW = 110.dp, sizeH = 120.dp, offsetX = 290.dp, offsetY = 200.dp),

            // 7. Crash Cymbal (左上方的墜擊鈸,新增的第 8 個組件)
            DrumComponentData(name = "Crash Cymbal", soundResId = drumSounds[7],
                sizeW = 180.dp, sizeH = 130.dp, offsetX = 100.dp, offsetY = 20.dp)
        )
    }

    // 狀態：追蹤哪個鼓組件被敲擊 (用於視覺回饋)
    var tappedDrumId by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. 背景圖片 (Drum Kit Image)
        Image(
            painter = painterResource(id = R.drawable.drum_background),
            contentDescription = "爵士鼓背景",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. 8 個鼓組件的可點擊區域 (Drum Pads)
        drumComponents.forEachIndexed { index, data ->
            DrumPad(
                id = index,
                data = data,
                isTapped = tappedDrumId == index,
                soundManager = soundManager,
                onTap = { tappedDrumId = index }
            )
        }

        // 3. 頂部返回按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
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

    // 視覺回饋重置：在短暫延遲後清除視覺效果
    LaunchedEffect(tappedDrumId) {
        if (tappedDrumId != null) {
            delay(150) // 視覺效果持續 150ms
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