package com.soundinteractionapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box

/**
 * 專為無障礙設計的大尺寸模式按鈕 Composable。 (AppModeButton)
 */
@Composable
fun AppModeButton(
    text: String,
    description: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp), // 縮小最小高度到 72dp
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(16.dp) // 縮小內部間距
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge, // 縮小主標題文字
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall, // 縮小輔助文字
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 應用程式的歡迎/登入畫面內容，專為橫向螢幕設計。 (WelcomeScreenContent)
 */
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
            // 左側：大標題區 (佔 40% 寬度)
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SoundJoy",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "音樂互動訓練 App",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 右側：模式按鈕區 (佔 60% 寬度)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                // 讓所有按鈕和間距均勻分佈
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. 自由探索模式按鈕
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

                // 2. 放鬆模式按鈕
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