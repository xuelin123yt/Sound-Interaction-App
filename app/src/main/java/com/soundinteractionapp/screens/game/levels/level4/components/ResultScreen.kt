package com.soundinteractionapp.screens.game.levels.level4.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.soundinteractionapp.R
import com.soundinteractionapp.data.RankingViewModel

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
    beatmapId: Int,
    isAutoMode: Boolean = false,  // ✅ 新增：AUTO 模式標記
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
    rankingViewModel: RankingViewModel = viewModel(),
    soundManager: com.soundinteractionapp.SoundManager
) {
    // ✅ Lottie 動畫設定（使用 JSON 格式）- 只播放一次
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // ✅ 遊戲結束時儲存分數（AUTO 模式不儲存）
    LaunchedEffect(Unit) {
        val scoreId = 40 + beatmapId
        Log.d("ResultScreen", "遊戲結束 - beatmapId=$beatmapId, scoreId=$scoreId, score=$score, isAutoMode=$isAutoMode")

        // ✅ 只在非 AUTO 模式且分數大於 0 時儲存
        if (!isAutoMode && score > 0) {
            rankingViewModel.updateHighScore(scoreId, score)
        }

        // 🎵 播放煙火音效
        soundManager.playSFX("fireworks")
    }

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
                // ✅ 標題顯示 AUTO 模式
                Text(
                    text = if (isAutoMode) "AUTO 模式結束" else "遊戲結束",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAutoMode) Color(0xFF64B5F6) else Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                // ✅ AUTO 模式提示
                if (isAutoMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "（演示模式，不計入排行榜）",
                        fontSize = 12.sp,
                        color = Color(0xFF64B5F6).copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("準確率", "%.2f%%".format(accuracy))
                    StatItem("最高連擊", "${maxCombo}x")
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        JudgementRow("Miss", missCount, Color(0xFFFF5252))  // ✅ 使用更亮的紅色
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ✅ 下一關按鈕 - AUTO 模式不顯示
                    if (hasNextLevel && !isAutoMode) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(45.dp)
                        ) {
                            Text(
                                text = "下一關",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // ✅ 重新開始按鈕（AUTO 模式顯示為「再來一次」）
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(45.dp)
                    ) {
                        Text(
                            text = if (isAutoMode) "再來一次" else "重新開始",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // ✅ 離開按鈕
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(45.dp)
                    ) {
                        Text(
                            text = "離開",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 🎉 最上層全螢幕慶祝動畫（播放一次）
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
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