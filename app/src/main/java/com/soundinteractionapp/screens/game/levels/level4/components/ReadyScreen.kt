package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap

@Composable
fun ReadyScreen(
    beatmap: Beatmap,  // ✅ 加上 beatmap 參數
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = beatmap.title,  // ✅ 改用 beatmap.title
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = beatmap.description,  // ✅ 改用 beatmap.description
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF69B4)
            ),
            modifier = Modifier.size(width = 200.dp, height = 60.dp)
        ) {
            Text("開始遊戲", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF666666)
            ),
            modifier = Modifier.size(width = 200.dp, height = 50.dp)
        ) {
            Text("返回")
        }
    }
}