package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 暫停對話框 - 優化效能版本
 * ✅ 移除漸層背景，改用純色
 * ✅ 按鈕文字強制白色
 */
@Composable
fun PauseDialog(
    score: Int,
    combo: Int,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)), // ✅ 改用純色，提升效能
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E2A3E)
            ),
            elevation = CardDefaults.cardElevation(32.dp),
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 標題
                Text(
                    text = "遊戲暫停",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.8.sp
                )

                // 分數和連擊顯示卡片
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 分數
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "分數",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                text = score.toString(),
                                fontSize = 47.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }

                        // 連擊
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "連擊",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                text = "${combo}x",
                                fontSize = 47.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF69B4)
                            )
                        }
                    }
                }

                // 三個按鈕橫向排列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // 繼續遊戲按鈕
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White // ✅ 強制白色
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            text = "繼續遊戲",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // ✅ 強制白色
                        )
                    }

                    // 再試一次按鈕
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White // ✅ 強制白色
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            text = "再試一次",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // ✅ 強制白色
                        )
                    }

                    // 離開遊戲按鈕
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White // ✅ 強制白色
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            text = "離開遊戲",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // ✅ 強制白色
                        )
                    }
                }
            }
        }
    }
}