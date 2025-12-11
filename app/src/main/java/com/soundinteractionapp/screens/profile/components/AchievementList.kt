package com.soundinteractionapp.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.profile.models.Achievement

/**
 * 成就列表模式
 * 以垂直列表方式顯示所有成就
 */
@Composable
fun AchievementList(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        achievements.forEach { achievement ->
            AchievementListItem(achievement = achievement)
        }
    }
}

/**
 * 成就列表項目
 * 單個成就的列表顯示
 */
@Composable
private fun AchievementListItem(
    achievement: Achievement
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                Color.White
            else
                Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側：成就圖示
            AchievementIcon(
                achievement = achievement,
                modifier = Modifier.size(60.dp)
            )

            Spacer(Modifier.width(16.dp))

            // 中間：成就名稱和描述
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked)
                        Color.Black
                    else
                        Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // 右側：狀態和日期
            AchievementStatus(achievement = achievement)
        }
    }
}

/**
 * 成就圖示
 * 顯示成就的圖示和鎖定狀態
 */
@Composable
private fun AchievementIcon(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "未解鎖",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 成就狀態
 * 顯示成就的完成狀態和日期
 */
@Composable
private fun AchievementStatus(
    achievement: Achievement
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        if (achievement.isUnlocked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "已完成",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "已完成",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = achievement.unlockedDate,
                fontSize = 12.sp,
                color = Color.Gray
            )
        } else {
            Text(
                text = "未完成",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}