package com.soundinteractionapp.screens.game.levels.level4.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
    rankingViewModel: RankingViewModel = viewModel()
) {
    // ✅ Lottie 動畫設定（使用 JSON 格式）- 只播放一次
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti) // confetti.json
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1 // 只播放一次
    )

    // ✅ 遊戲結束時儲存分數（只執行一次）
    LaunchedEffect(Unit) {
        val scoreId = 40 + beatmapId
        Log.d("ResultScreen", "遊戲結束 - beatmapId=$beatmapId, scoreId=$scoreId, score=$score")

        if (score > 0) {
            rankingViewModel.updateHighScore(scoreId, score)
        }
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
                Text(
                    text = "遊戲結束",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                        JudgementRow("Miss", missCount, Color(0xFFFF0000))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
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

        // 🎉 最上層全螢幕慶祝動畫（播放一次）
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = ContentScale.FillBounds, // 強制拉伸填滿螢幕
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