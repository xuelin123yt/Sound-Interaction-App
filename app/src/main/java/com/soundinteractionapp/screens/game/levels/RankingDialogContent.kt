package com.soundinteractionapp.screens.game.levels

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.utils.GameScoreUtils
import com.soundinteractionapp.screens.game.levels.level1.Difficulty
import com.soundinteractionapp.screens.game.levels.level2.Level2Difficulty

@Composable
fun RankingDialogContent(onClose: () -> Unit, rankingViewModel: RankingViewModel) {
    val scores by rankingViewModel.scores.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "rankBounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 650.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("🏆 歷史最高紀錄", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Divider()

            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Level 1
                item { SectionTitle("🎵 關卡 1: 料理鼠王") }
                item {
                    ScoreRowWithRank("簡單", scores.level1Easy, Difficulty.EASY.maxScore, Color(0xFF81C784), scale)
                    ScoreRowWithRank("普通", scores.level1Normal, Difficulty.NORMAL.maxScore, Color(0xFF4FC3F7), scale)
                    ScoreRowWithRank("困難", scores.level1Hard, Difficulty.HARD.maxScore, Color(0xFFFF8A65), scale)
                }
                item { TotalScoreRow(scores.level1Total); Divider(modifier = Modifier.padding(top = 8.dp)) }

                // Level 2
                item { SectionTitle("🎹 關卡 2: 鋼琴演奏") }
                item {
                    ScoreRowWithRank("簡單 (天空之城)", scores.level2Easy, Level2Difficulty.EASY.maxScore, Color(0xFF4CAF50), scale)
                    ScoreRowWithRank("普通 (龍貓)", scores.level2Normal, Level2Difficulty.NORMAL.maxScore, Color(0xFF2196F3), scale)
                    ScoreRowWithRank("困難 (Maria)", scores.level2Hard, Level2Difficulty.HARD.maxScore, Color(0xFFE53935), scale)
                }
                item { TotalScoreRow(scores.level2Total); Divider(modifier = Modifier.padding(top = 8.dp)) }

                // Level 3
                item { SectionTitle("🎤 關卡 3: 聲控飛行") }
                item {
                    ScoreRowNoRank("最高分", scores.level3Score, Color(0xFFE91E63))
                }
                item { Divider(modifier = Modifier.padding(top = 8.dp)) }

                // ========== ✅ Level 4: 補上第五首 ==========
                item { SectionTitle("🎵 關卡 4: 音樂節奏") }
                item {
                    ScoreRowNoRank("哆啦A夢 主題曲", scores.level4Osu01, Color(0xFF81D4FA))
                    ScoreRowNoRank("神魔之塔 主題曲（夜）", scores.level4Osu02, Color(0xFFFFCC80))
                    ScoreRowNoRank("Ib 記憶", scores.level4Osu05, Color(0xFFB39DDB))
                    ScoreRowNoRank("打上花火", scores.level4Osu04, Color(0xFFFFAB91))
                    ScoreRowNoRank("能看見海的街道", scores.level4Osu03, Color(0xFFA5D6A7))
                }
                item { TotalScoreRow(scores.level4Total); Divider(modifier = Modifier.padding(top = 8.dp)) }
            }

            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
                Text("關閉", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF555555))
}

@Composable
fun TotalScoreRow(total: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("總分", fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("$total", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
    }
}

@Composable
fun ScoreRowWithRank(levelName: String, score: Int, maxScore: Int, color: Color, animScale: Float) {
    val rank = GameScoreUtils.calculateRank(score, maxScore)
    val rankColor = GameScoreUtils.getRankColor(rank)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(levelName, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$score 分", style = MaterialTheme.typography.bodyLarge, color = if (score > 0) color else Color.LightGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            if (score > 0) {
                Text(
                    text = rank,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = rankColor,
                    modifier = Modifier.graphicsLayer { scaleX = animScale; scaleY = animScale }
                )
            } else {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun ScoreRowNoRank(levelName: String, score: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(levelName, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Text(
            text = if (score > 0) "$score 分" else "-",
            style = MaterialTheme.typography.bodyLarge,
            color = if (score > 0) color else Color.LightGray,
            fontWeight = FontWeight.Bold
        )
    }
}