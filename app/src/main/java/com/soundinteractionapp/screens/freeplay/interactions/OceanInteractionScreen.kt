package com.soundinteractionapp.screens.freeplay.interactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager

/**
 * 海浪互動畫面 (佔位版本)
 */
@Composable
fun OceanInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val backgroundResId = R.drawable.catbackground

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 中央內容
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "🌊",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "海浪互動",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "功能開發中...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 返回按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.height(50.dp)
            ) {
                Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}