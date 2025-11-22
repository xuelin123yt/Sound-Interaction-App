package com.soundinteractionapp.screens.freeplay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.components.SoundInteractionButton
import com.soundinteractionapp.data.SoundData
import com.soundinteractionapp.R
import kotlinx.coroutines.delay

@Composable
fun FreePlayScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToInteraction: (String) -> Unit // 改成通用導航
) {
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
                Text(
                    "自由探索模式 - 選擇關卡",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 佔位，保持對齊
                Spacer(modifier = Modifier.width(100.dp).height(50.dp))
            }

            // 9 個聲音互動按鈕 (3x3)
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
                                    // 根據 buttonId 導航到不同畫面
                                    val route = getInteractionRoute(buttonId)
                                    if (route != null) {
                                        onNavigateToInteraction(route)
                                    } else {
                                        // 佔位按鈕:只有視覺回饋
                                        activeEffectButtonId = buttonId
                                    }
                                }
                            )

                            LaunchedEffect(activeEffectButtonId) {
                                if (activeEffectButtonId != null) {
                                    delay(200)
                                    activeEffectButtonId = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 根據 ID 返回路由
fun getInteractionRoute(id: Int): String? {
    return when (id) {
        0 -> "interaction/cat"
        1 -> "interaction/dog"
        2 -> "interaction/bird"
        3 -> "interaction/piano"
        4 -> "interaction/drum"
        5 -> "interaction/bell"
        6 -> "interaction/rain"
        7 -> "interaction/ocean"
        8 -> "interaction/wind"
        else -> null
    }
}

// 獲取聲音數據
@Composable
fun getSoundInteractionData(id: Int): SoundData {
    return when (id) {
        0 -> SoundData("貓咪", R.raw.cat_meow, { Text("🐾") })
        1 -> SoundData("狗狗", 0, { Text("🐶") })
        2 -> SoundData("小鳥", 0, { Text("🐦") })
        3 -> SoundData("鋼琴", 0, { Text("🎹") })
        4 -> SoundData("鼓", 0, { Text("🥁") })
        5 -> SoundData("鈴鐺", 0, { Text("🔔") })
        6 -> SoundData("雨聲", 0, { Text("🌧️") })
        7 -> SoundData("海浪", 0, { Text("🌊") })
        8 -> SoundData("風聲", 0, { Text("💨") })
        else -> SoundData("開發中", 0, { Text("🛠️") })
    }
}