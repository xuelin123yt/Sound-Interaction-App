package com.soundinteractionapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.SoundData
import kotlinx.coroutines.delay

// =======================================================
// 自由探索模式 (Free Play)
// =======================================================

/**
 * 自由探索模式 (Free Play) 的 UI 介面內容。 (FreePlayScreenContent)
 */
@Composable
fun FreePlayScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToCatInteraction: () -> Unit,
    onNavigateToPianoInteraction: () -> Unit,
    onNavigateToDogInteraction: () -> Unit,
    onNavigateToBirdInteraction: () -> Unit,
    onNavigateToDrumInteraction: () -> Unit,
    onNavigateToOceanInteraction: () -> Unit,
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
                // 返回按鈕
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("← 返回模式選擇", style = MaterialTheme.typography.bodyLarge)
                }

                // 佔位 Spacer
                Spacer(modifier = Modifier.width(150.dp))
            }

            // 中間：9 個聲音互動按鈕 (3x3 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 佔據剩餘空間
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 模擬 3x3 網格
                repeat(3) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // 每個 Row 平均分配高度
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { colIndex ->
                            val buttonId = rowIndex * 3 + colIndex

                            // 獲取聲音數據
                            val soundData = getSoundInteractionData(buttonId)

                            // 聲音按鈕
                            SoundInteractionButton(
                                soundName = soundData.name,
                                icon = soundData.icon,
                                isActive = activeEffectButtonId == buttonId,
                                onClick = {
                                    when (buttonId) {
                                        0 -> onNavigateToCatInteraction() // 貓咪
                                        1 -> onNavigateToPianoInteraction() // 鋼琴
                                        // 2 -> onNavigateToOceanInteraction() // 海浪 (暫時關閉導航，改為只播放聲音)
                                        3 -> onNavigateToDogInteraction() // 狗狗
                                        4 -> onNavigateToDrumInteraction() // 爵士鼓
                                        // 5 -> 雨聲 (暫時只播放聲音)
                                        6 -> onNavigateToBirdInteraction() // 鳥兒
                                        7 -> onNavigateToBellInteraction() // 鈴鐺
                                        else -> {
                                            // 對於未開發完成的功能 (海浪、雨聲等)，只觸發視覺回饋和播放佔位聲音
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

// =======================================================
// 數據結構
// =======================================================

/** 根據 ID 獲取 Free Play 模式的聲音數據 */
@Composable
fun getSoundInteractionData(id: Int): SoundData {
    // 為了避免紅字錯誤，這裡將「海浪」和「雨聲」的資源暫時指向已存在的檔案 (如 cat_meow 或 desk_bell)
    // 只要 R.raw.xxx 檔案存在，紅字就會消失
    return when (id) {
        0 -> SoundData("貓咪", R.raw.cat_meow, { Text("🐾") })
        1 -> SoundData("鋼琴", R.raw.piano_c1, { Text("🎹") })

        // [修正] 海浪：暫時使用 cat_meow，避免 R.raw.wave_sound 紅字
        2 -> SoundData("海浪", R.raw.cat_meow, { Text("🌊") })

        3 -> SoundData("狗狗", R.raw.dog_barking, { Text("🐕") })
        4 -> SoundData("爵士鼓", R.raw.drum_cymbal_closed, { Text("🥁") })

        // [修正] 雨聲：暫時使用 cat_meow，避免 R.raw.rain_sound 紅字
        5 -> SoundData("雨聲", R.raw.cat_meow, { Text("🌧️") })

        6 -> SoundData("鳥兒", R.raw.bird_sound, { Text("🐦") })
        7 -> SoundData("鈴鐺", R.raw.desk_bell, { Text("🔔") })

        else -> SoundData("星星", R.raw.cat_meow, { Text("✨") })
    }
}