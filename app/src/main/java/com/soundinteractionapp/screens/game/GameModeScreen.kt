package com.soundinteractionapp.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.Screen

/**
 * 遊戲訓練模式 (Game Mode) 的 UI 介面內容。 (GameModeScreenContent)
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
                                onClick = { onNavigateToLevel(levelRoute) }
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
                // TODO: 加入獎勵徽章圖示或鎖定圖示
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
fun Level1FollowBeatScreen(onNavigateBack: () -> Unit) {
    GameLevelPlaceholderScreen(title = "關卡 1: 跟著拍拍手", onNavigateBack = onNavigateBack)
}

@Composable
fun Level2FindAnimalScreen(onNavigateBack: () -> Unit) {
    GameLevelPlaceholderScreen(title = "關卡 2: 找出小動物", onNavigateBack = onNavigateBack)
}

@Composable
fun Level3PitchScreen(onNavigateBack: () -> Unit) {
    GameLevelPlaceholderScreen(title = "關卡 3: 音階高低", onNavigateBack = onNavigateBack)
}

@Composable
fun Level4CompositionScreen(onNavigateBack: () -> Unit) {
    GameLevelPlaceholderScreen(title = "關卡 4: 創作小樂曲", onNavigateBack = onNavigateBack)
}