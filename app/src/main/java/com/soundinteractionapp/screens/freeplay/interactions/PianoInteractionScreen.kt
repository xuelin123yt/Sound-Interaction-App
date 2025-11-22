package com.soundinteractionapp

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

// 確保 R 類別可以被識別
import com.soundinteractionapp.R

/**
 * 鋼琴互動畫面，提供 8 個琴鍵用於音階探索。
 * 目標：音高概念、手眼協調訓練。
 */
@Composable
fun PianoInteractionScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {

    // 8 個琴鍵的資源列表 (C4 到 C5)
    // 修正：使用您在 res/raw 中實際的檔案名稱（piano_c1.wav, piano_d1.wav, ...）
    val pianoNotes = remember {
        listOf(
            R.raw.piano_c1, // 假設 C4 對應您的 C1.wav
            R.raw.piano_d1, // 假設 D4 對應您的 D1.wav
            R.raw.piano_e1,
            R.raw.piano_f1,
            R.raw.piano_g1,
            R.raw.piano_a1,
            R.raw.piano_b1,
            R.raw.piano_c2  // 假設 C5 對應您的 C2.wav
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

        // 使用 Column 堆疊：返回按鈕 -> 鋼琴鍵盤
        Column(modifier = Modifier.fillMaxSize()) {

            // 頂部返回按鈕 (放在最上層)
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

            // 鍵盤區 (佔據剩餘所有空間)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 佔滿剩餘高度
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 創建 8 個白鍵
                pianoNotes.forEachIndexed { index, resId ->
                    PianoKey(
                        label = when(index) {
                            0 -> "C"
                            1 -> "D"
                            2 -> "E"
                            3 -> "F"
                            4 -> "G"
                            5 -> "A"
                            6 -> "B"
                            7 -> "C"
                            else -> ""
                        },
                        noteResId = resId,
                        soundManager = soundManager
                    )
                }
            }
        }
    }
}

/**
 * 單個鋼琴鍵 Composable。
 */
@Composable
fun RowScope.PianoKey(
    label: String,
    noteResId: Int,
    soundManager: SoundManager
) {
    // 按鈕的狀態：是否被按壓 (用於視覺回饋)
    var isPressed by remember { mutableStateOf(false) }

    // 鍵盤的顏色：按下時變深，鬆開時恢復白色
    val keyColor = if (isPressed) Color(0xFFCCCCCC) else Color.White

    // 按鍵點擊時的互動邏輯
    val tapDetector = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onPress = { offset ->
                isPressed = true // 按下時變色
                soundManager.playSound(noteResId) // 播放聲音
                try {
                    awaitRelease() // 等待釋放
                } finally {
                    isPressed = false // 釋放時恢復
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .weight(1f) // 均勻分佈寬度
            .fillMaxHeight()
            .padding(horizontal = 4.dp), // 鍵與鍵之間的間距
        colors = CardDefaults.cardColors(containerColor = keyColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 0.dp else 4.dp),
        shape = MaterialTheme.shapes.extraSmall // 讓琴鍵看起來更像方塊
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(tapDetector), // 應用手勢偵測
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
