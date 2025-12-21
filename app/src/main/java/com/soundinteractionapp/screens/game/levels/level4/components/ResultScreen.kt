package com.soundinteractionapp.screens.game.levels.level4.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.soundinteractionapp.screens.profile.models.AchievementManager
import kotlinx.coroutines.launch

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
    isAutoMode: Boolean = false,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
    rankingViewModel: RankingViewModel = viewModel(),
    soundManager: com.soundinteractionapp.SoundManager
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val progress by animateLottieCompositionAsState(composition = composition, iterations = 1)

    // ✅ 用於追蹤「首次解鎖」的成就
    var newlyUnlockedAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // ✅ 遊戲結束時儲存分數並檢查成就
    LaunchedEffect(Unit) {
        val scoreId = 40 + beatmapId
        Log.d("ResultScreen", "遊戲結束 - beatmapId=$beatmapId, scoreId=$scoreId, score=$score, missCount=$missCount, isAutoMode=$isAutoMode")

        if (!isAutoMode && score > 0) {
            // 1. 上傳分數
            rankingViewModel.updateHighScore(scoreId, score)

            // 2. 檢查「無 Miss」成就
            if (missCount == 0) {
                Log.d("ResultScreen", "✅ 達成無 Miss！標記成就...")
                rankingViewModel.markLevel4NoMiss()
            }

            // 3. 延遲檢查成就（確保分數已同步到 ViewModel）
            kotlinx.coroutines.delay(500)

            // 4. 檢查成就解鎖
            coroutineScope.launch {
                try {
                    val achievementManager = AchievementManager()
                    val currentScores = rankingViewModel.scores.value

                    // ✅ 檢查並解鎖成就（hasAvatar 和 hasFeedback 可設為 false，因為這裡只關注遊戲成就）
                    val newlyUnlocked = achievementManager.checkAndUnlockAchievements(
                        scoreEntry = currentScores,
                        hasFeedback = false,
                        hasAvatar = false
                    )

                    // ✅ 篩選出本次遊戲相關的成就
                    val gameRelatedAchievements = mutableListOf<String>()

                    // 成就 4: Flawless Finish／無瑕結束
                    if (4 in newlyUnlocked && missCount == 0) {
                        gameRelatedAchievements.add("Flawless Finish／無瑕結束")
                    }

                    // 成就 5: Perfect Performance／完美演出
                    if (5 in newlyUnlocked && currentScores.level4Total >= 30000) {
                        gameRelatedAchievements.add("Perfect Performance／完美演出")
                    }

                    // 成就 6: Mode Three Completionist／模式三完成者
                    if (6 in newlyUnlocked) {
                        gameRelatedAchievements.add("Mode Three Completionist／模式三完成者")
                    }

                    if (gameRelatedAchievements.isNotEmpty()) {
                        newlyUnlockedAchievements = gameRelatedAchievements
                        Log.d("ResultScreen", "🎉 新解鎖成就: $gameRelatedAchievements")
                    }
                } catch (e: Exception) {
                    Log.e("ResultScreen", "❌ 檢查成就失敗", e)
                }
            }
        }

        soundManager.playSFX("fireworks")
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xCC000000)).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAutoMode) "AUTO 模式結束" else "遊戲結束",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAutoMode) Color(0xFF64B5F6) else Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "最終分數", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                Text(text = score.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))

                if (isAutoMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "（演示模式,不計入排行榜）", fontSize = 12.sp, color = Color(0xFF64B5F6).copy(alpha = 0.7f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("準確率", "%.2f%%".format(accuracy))
                    StatItem("最高連擊", "${maxCombo}x")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1E)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        JudgementRow("Perfect", perfectCount, Color(0xFFFFD700))
                        JudgementRow("Great", greatCount, Color(0xFF00FF00))
                        JudgementRow("Good", goodCount, Color(0xFF87CEEB))
                        JudgementRow("Miss", missCount, Color(0xFFFF5252))
                    }
                }

                // ✅ 顯示新解鎖的成就
                if (newlyUnlockedAchievements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    newlyUnlockedAchievements.forEach { achievementName ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏆", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "成就解鎖！",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        achievementName,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        if (achievementName != newlyUnlockedAchievements.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (hasNextLevel && !isAutoMode) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.85f).height(45.dp)
                        ) {
                            Text(text = "下一關", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f).height(45.dp)
                    ) {
                        Text(text = if (isAutoMode) "再來一次" else "重新開始", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f).height(45.dp)
                    ) {
                        Text(text = "離開", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

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
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun JudgementRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = color)
        Text(text = count.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}