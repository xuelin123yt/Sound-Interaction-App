package com.soundinteractionapp.screens.game.levels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close // 使用 Close 或 ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 排名系統畫面 (Ranking Dialog)。
 * 作為彈出視窗的內容，顯示在遊戲選單之上。
 */
@Composable
fun RankingDialogContent(onClose: () -> Unit) { // 【修正】函數名稱與參數

    // 彈出式視窗的內容應該使用 Card 來實現圓角方型背景
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f) // 佔據螢幕寬度 90%
            .heightIn(min = 300.dp, max = 500.dp) // 設定高度範圍
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp), // 圓角
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 頂部欄位：模擬 TopAppBar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 標題 (中間)
                    Text(
                        text = "🏆 全球排行榜",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )

                    // 2. 關閉按鈕 (右側)
                    IconButton(onClick = onClose) { // 點擊時呼叫關閉
                        Icon(
                            imageVector = Icons.Filled.Close, // 使用 X 關閉圖示
                            contentDescription = "關閉",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 排名列表顯示區 (佔據剩餘空間)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 【TODO: 在此處實作 LazyColumn 顯示排名資料】
                Text(
                    "用戶排名列表將在此處展示...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}