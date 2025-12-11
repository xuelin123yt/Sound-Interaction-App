package com.soundinteractionapp.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.profile.models.Achievement

/**
 * 成就展示主元件
 * 包含標題、切換按鈕和成就顯示
 */
@Composable
fun AchievementDisplay(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    // 成就顯示模式：true = 圖片模式, false = 列表模式
    var isGridMode by remember { mutableStateOf(true) }
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 標題與切換按鈕
        AchievementHeader(
            unlockedCount = unlockedCount,
            totalCount = totalCount,
            isGridMode = isGridMode,
            onToggleMode = { isGridMode = !isGridMode }
        )

        // 根據模式顯示不同的成就展示
        if (isGridMode) {
            AchievementGrid(achievements = achievements)
        } else {
            AchievementList(achievements = achievements)
        }
    }
}

/**
 * 成就展示標題
 * 包含計數和模式切換按鈕
 */
@Composable
private fun AchievementHeader(
    unlockedCount: Int,
    totalCount: Int,
    isGridMode: Boolean,
    onToggleMode: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：標題和計數
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "成就展示",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "($unlockedCount/$totalCount)",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // 右側：切換顯示模式按鈕
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = { if (!isGridMode) onToggleMode() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.GridView,
                    contentDescription = "圖片模式",
                    tint = if (isGridMode) Color(0xFF673AB7) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = { if (isGridMode) onToggleMode() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = "列表模式",
                    tint = if (!isGridMode) Color(0xFF673AB7) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}