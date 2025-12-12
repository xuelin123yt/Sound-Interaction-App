package com.soundinteractionapp.screens.game.levels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.data.RankingViewModel

@Composable
fun RankingDialogContent(onClose: () -> Unit, rankingViewModel: RankingViewModel) {
    val scores by rankingViewModel.scores.collectAsState()

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("🏆 最高紀錄", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Divider()
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("🎵 關卡 1: 跟著按", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
                item { ScoreRow("簡單 (Easy)", scores.level1Easy, Color(0xFF81C784)) }
                item { ScoreRow("普通 (Normal)", scores.level1Normal, Color(0xFF4FC3F7)) }
                item { ScoreRow("困難 (Hard)", scores.level1Hard, Color(0xFFFF8A65)) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("總分", fontWeight = FontWeight.Bold)
                        Text("${scores.level1Total}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
                item { Text("🐶 關卡 2: 找出動物", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
                item { ScoreRow("最高分", scores.level2Score, Color.Gray) }
            }
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("關閉") }
        }
    }
}

@Composable
fun ScoreRow(levelName: String, score: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(levelName, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Text("$score 分", style = MaterialTheme.typography.bodyLarge, color = if (score > 0) color else Color.LightGray, fontWeight = FontWeight.Bold)
    }
}