package com.soundinteractionapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.components.AppModeButton

@Composable
fun WelcomeScreenContent(
    onNavigateToFreePlay: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側：大標題區
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SoundJoy",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "音樂互動訓練 App",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 右側：模式按鈕區
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                AppModeButton(
                    text = "自由探索模式",
                    description = "提供安全的聲音探索環境",
                    onClick = onNavigateToFreePlay,
                    color = MaterialTheme.colorScheme.tertiary
                )

                AppModeButton(
                    text = "遊戲訓練模式",
                    description = "透過遊戲提升專注力與認知",
                    onClick = onNavigateToGame,
                    color = MaterialTheme.colorScheme.primary
                )

                AppModeButton(
                    text = "放鬆模式",
                    description = "情緒調節與安撫焦慮",
                    onClick = onNavigateToRelax,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}