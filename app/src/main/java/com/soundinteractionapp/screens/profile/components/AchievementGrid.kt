package com.soundinteractionapp.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.profile.models.Achievement

/**
 * 成就網格模式
 * 以圖片網格方式顯示所有成就
 */
@Composable
fun AchievementGrid(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 使用 chunked 將成就分成每排 4 個
            achievements.chunked(4).forEach { rowAchievements ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowAchievements.forEach { achievement ->
                        AchievementGridItem(
                            achievement = achievement,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 填充空白空間（當最後一排不足 4 個時）
                    repeat(4 - rowAchievements.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 成就網格項目
 * 單個成就的網格顯示
 */
@Composable
private fun AchievementGridItem(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 成就圖示
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = achievement.iconResId),
                contentDescription = achievement.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (achievement.isUnlocked)
                            Color(0xFF673AB7)
                        else
                            Color.LightGray,
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop,
                colorFilter = if (!achievement.isUnlocked) {
                    ColorFilter.tint(Color.Gray.copy(alpha = 0.3f))
                } else null,
                alpha = if (achievement.isUnlocked) 1f else 0.4f
            )

            // 未解鎖顯示鎖頭
            if (!achievement.isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "未解鎖",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 顯示解鎖日期或狀態
        Text(
            text = if (achievement.isUnlocked)
                achievement.unlockedDate
            else
                "未解鎖",
            fontSize = 10.sp,
            color = if (achievement.isUnlocked)
                Color(0xFF673AB7)
            else
                Color.Gray,
            fontWeight = if (achievement.isUnlocked)
                FontWeight.Medium
            else
                FontWeight.Normal
        )
    }
}