package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(
    score: Int,
    maxCombo: Int,
    accuracy: Float,
    perfectCount: Int,
    greatCount: Int,
    goodCount: Int,
    missCount: Int,
    hasNextLevel: Boolean,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 標題
                Text(
                    text = "遊戲結束",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 最終分數
                Text(
                    text = "最終分數",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = score.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 準確率和最高連擊
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("準確率", "%.2f%%".format(accuracy))
                    StatItem("最高連擊", "${maxCombo}x")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 判定統計
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F0F1E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        JudgementRow("Perfect", perfectCount, Color(0xFFFFD700))
                        JudgementRow("Great", greatCount, Color(0xFF00FF00))
                        JudgementRow("Good", goodCount, Color(0xFF87CEEB))
                        JudgementRow("Miss", missCount, Color(0xFFFF0000))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按鈕組
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 下一關按鈕（僅在有下一關時顯示）
                    if (hasNextLevel) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(45.dp)
                        ) {
                            Text("下一關", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 重新開始按鈕
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(45.dp)
                    ) {
                        Text("重新開始", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // 離開按鈕
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF666666)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(45.dp)
                    ) {
                        Text("離開", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun JudgementRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = color
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}