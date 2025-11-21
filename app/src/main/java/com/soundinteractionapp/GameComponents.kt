

package com.soundinteractionapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 遊戲訓練模式 (Game Mode) 的 UI 介面內容。 (GameModeScreenContent)
 * 包含四個關卡的入口按鈕。
 */
@Composable
fun FreePlayScreenContent(onNavigateBack: () -> Unit, soundManager: SoundManager) { // 接收 SoundManager
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 頂部：返回按鈕
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
                    Text("← 返回模式選擇", style = MaterialTheme.typography.bodyLarge)
                }
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
                            // 聲音按鈕
                            SoundInteractionButton(
                                soundName = when (rowIndex * 3 + colIndex) {
                                    0 -> "貓咪"
                                    1 -> "鋼琴"
                                    2 -> "海浪"
                                    3 -> "狗狗"
                                    4 -> "鼓聲"
                                    5 -> "雨聲"
                                    6 -> "鳥叫"
                                    7 -> "鈴鐺"
                                    else -> "星星"
                                },
                                onClick = {
                                    // TODO: 在這裡使用 soundManager.playSound()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 自由探索模式中的單個聲音互動按鈕 (高對比度、大尺寸)。
 */
@Composable
fun RowScope.SoundInteractionButton(soundName: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .weight(1f) // 每個按鈕平均分配寬度
            .fillMaxHeight()
            .padding(8.dp), // 內部間隔
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = soundName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            // TODO: 在這裡加入視覺回饋 (動畫/圖案)
        }
    }
}

// =======================================================
// 遊戲訓練模式 (Game Mode)
// =======================================================

/**
 * 遊戲訓練模式 (Game Mode) 的 UI 介面內容。 (GameModeScreenContent)
 * 包含四個關卡的入口按鈕。
 */
@Composable
fun GameModeScreenContent(onNavigateBack: () -> Unit, onNavigateToLevel: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 頂部：返回按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("← 返回模式選擇", style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    "遊戲訓練模式 - 選擇關卡",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 佔位，保持對齊
                Spacer(modifier = Modifier.width(100.dp).height(50.dp))
            }

            // 中間：關卡選擇區 (2x2 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 模擬 2x2 關卡網格
                repeat(2) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // 每個 Row 平均分配高度
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) { colIndex ->
                            val levelIndex = rowIndex * 2 + colIndex

                            // 決定關卡路由
                            val levelRoute = when (levelIndex) {
                                0 -> Screen.GameLevel1.route
                                1 -> Screen.GameLevel2.route
                                2 -> Screen.GameLevel3.route
                                else -> Screen.GameLevel4.route
                            }

                            GameLevelButton(
                                levelNumber = levelIndex + 1,
                                levelTitle = when (levelIndex) {
                                    0 -> "跟著拍拍手"
                                    1 -> "找出小動物"
                                    2 -> "音階高低"
                                    else -> "創作小樂曲"
                                },
                                onClick = { onNavigateToLevel(levelRoute) } // 修改：導航到特定關卡
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 遊戲模式中的單個關卡按鈕 (2x2 網格)。 (GameLevelButton)
 */
@Composable
fun RowScope.GameLevelButton(levelNumber: Int, levelTitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .weight(1f) // 每個按鈕平均分配寬度
            .fillMaxHeight()
            .padding(16.dp), // 內部間隔
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "關卡 ${levelNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// =======================================================
// 獨立關卡骨架 (Placeholder Screens)
// =======================================================

/**
 * 遊戲關卡的通用骨架畫面。
 */
@Composable
fun GameLevelPlaceholderScreen(title: String, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("關卡邏輯實作中...", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 32.dp)) {
                Text("返回關卡選擇")
            }
        }
    }
}

@Composable
fun Level1FollowBeatScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    GameLevelPlaceholderScreen(title = "關卡 1: 跟著拍拍手", onNavigateBack = onNavigateBack)
}

@Composable
fun Level2FindAnimalScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    GameLevelPlaceholderScreen(title = "關卡 2: 找出小動物", onNavigateBack = onNavigateBack)
}

@Composable
fun Level3PitchHighLowScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    GameLevelPlaceholderScreen(title = "關卡 3: 音階高低", onNavigateBack = onNavigateBack)
}

@Composable
fun Level4ComposeTuneScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    GameLevelPlaceholderScreen(title = "關卡 4: 創作小樂曲", onNavigateBack = onNavigateBack)
}