package com.soundinteractionapp.screens.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 使用者資訊卡片
 * 顯示所有個人資訊項目
 */
@Composable
fun UserInfoCard(
    account: String,
    displayName: String,
    bio: String,
    createdAt: String,
    isAnonymous: Boolean,
    onEditName: () -> Unit,
    onEditBio: () -> Unit,
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 非訪客顯示完整資訊
            if (!isAnonymous) {
                InfoItem(
                    icon = Icons.Default.AccountCircle,
                    title = "帳號",
                    value = account,
                    onClick = null
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                InfoItem(
                    icon = Icons.Default.Person,
                    title = "暱稱",
                    value = displayName,
                    onClick = onEditName
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                InfoItem(
                    icon = Icons.Default.Info,
                    title = "關於我",
                    value = bio.ifEmpty { "點擊設定關於我" },
                    onClick = onEditBio
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                InfoItem(
                    icon = Icons.Default.Lock,
                    title = "變更密碼",
                    value = "點擊修改",
                    onClick = onChangePassword
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 所有用戶都顯示註冊日期
            InfoItem(
                icon = Icons.Default.DateRange,
                title = "註冊日期",
                value = createdAt.ifEmpty { "未知" },
                onClick = null
            )
        }
    }
}

/**
 * 資訊項目元件
 * 顯示單個資訊項目
 */
@Composable
fun InfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF673AB7),
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                value,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                "編輯",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}