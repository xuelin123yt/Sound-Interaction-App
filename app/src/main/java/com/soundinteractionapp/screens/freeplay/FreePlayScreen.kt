package com.soundinteractionapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.SoundData
import kotlinx.coroutines.delay

@Composable
fun FreePlayScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToCatInteraction: () -> Unit,
    onNavigateToPianoInteraction: () -> Unit,
    onNavigateToDogInteraction: () -> Unit,
    onNavigateToBirdInteraction: () -> Unit,
    onNavigateToDrumInteraction: () -> Unit,
    onNavigateToOceanInteraction: () -> Unit, // 海浪導航參數
    onNavigateToBellInteraction: () -> Unit
) {
    // 狀態管理：追蹤當前啟動視覺效果的按鈕 ID
    var activeEffectButtonId by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 頂部控制列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("← 返回模式選擇", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.width(150.dp))
            }

            // 中間：9 個聲音互動按鈕
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { colIndex ->
                            val buttonId = rowIndex * 3 + colIndex
                            val soundData = getSoundInteractionData(buttonId)

                            SoundInteractionButton(
                                soundName = soundData.name,
                                icon = soundData.icon,
                                isActive = activeEffectButtonId == buttonId,
                                onClick = {
                                    // 導航邏輯
                                    when (buttonId) {
                                        0 -> onNavigateToCatInteraction()
                                        1 -> onNavigateToPianoInteraction()

                                        // === [關鍵修改] 讓按鈕 2 跳轉到海浪畫面 ===
                                        2 -> onNavigateToOceanInteraction()

                                        3 -> onNavigateToDogInteraction()
                                        4 -> onNavigateToDrumInteraction()
                                        // 5 -> 雨聲 (尚未實作)
                                        6 -> onNavigateToBirdInteraction()
                                        7 -> onNavigateToBellInteraction()
                                        else -> {
                                            // 其他尚未實作的功能，只播放聲音
                                            activeEffectButtonId = buttonId
                                            soundManager.playSound(soundData.resId)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 視覺效果重置
            LaunchedEffect(activeEffectButtonId) {
                if (activeEffectButtonId != null) {
                    delay(200)
                    activeEffectButtonId = null
                }
            }
        }
    }
}

// === [關鍵修改] 更新資料來源，加入 wave_sound ===
@Composable
fun getSoundInteractionData(id: Int): SoundData {
    return when (id) {
        0 -> SoundData("貓咪", R.raw.cat_meow, { Text("🐾") })
        1 -> SoundData("鋼琴", R.raw.piano_c1, { Text("🎹") })

        // ID 2: 海浪
        2 -> SoundData("海浪", R.raw.wave_sound, { Text("🌊") })

        3 -> SoundData("狗狗", R.raw.dog_barking, { Text("🐕") })
        4 -> SoundData("爵士鼓", R.raw.drum_cymbal_closed, { Text("🥁") })

        // ID 5: 雨聲 (暫時共用檔案避免紅字)
        5 -> SoundData("雨聲", R.raw.wave_sound, { Text("🌧️") })

        6 -> SoundData("鳥兒", R.raw.bird_sound, { Text("🐦") })
        7 -> SoundData("鈴鐺", R.raw.desk_bell, { Text("🔔") })

        else -> SoundData("星星", R.raw.cat_meow, { Text("✨") })
    }
}