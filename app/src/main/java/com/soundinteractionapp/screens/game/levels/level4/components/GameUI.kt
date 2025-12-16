package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.game.levels.level4.models.GameState
import kotlinx.coroutines.delay

@Composable
fun GameUI(
    score: Int,
    combo: Int,
    gameState: GameState,
    judgementText: String?,
    judgementColor: Color,
    currentTime: Long,
    totalDuration: Long,
    onPause: () -> Unit,
    onJudgementDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 頂部分數和暫停按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "分數",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = score.toString().padStart(8, '0'),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (gameState == GameState.PLAYING) {
                IconButton(onClick = onPause) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "暫停",
                        tint = Color.White
                    )
                }
            }
        }

        // 中央判定文字
        judgementText?.let { text ->
            LaunchedEffect(text) {
                delay(500)
                onJudgementDismiss()
            }
        }

        // 底部音樂進度條和 Combo（並排顯示）
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 時間和 Combo 並排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // 左側：當前時間
                Text(
                    text = formatTime(currentTime),
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )

                // 中間：Combo（如果有的話）
                if (combo > 0 && gameState == GameState.PLAYING) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${combo}x",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "COMBO",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // 右側：總時長
                Text(
                    text = formatTime(totalDuration),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 進度條
            LinearProgressIndicator(
                progress = (currentTime.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = Color(0xFFFF69B4),
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}