package com.soundinteractionapp.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R

/**
 * 頭像區域元件
 * 顯示用戶頭像、名稱和帳號資訊
 */
@Composable
fun AvatarSection(
    displayName: String,
    account: String,
    photoUrl: String,
    isAnonymous: Boolean,
    isLoading: Boolean,
    defaultAvatars: List<Int>,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 頭像顯示區域
        Box(
            modifier = Modifier.size(128.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, Color(0xFF673AB7), CircleShape)
                    .clickable(enabled = !isAnonymous) { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                // 顯示頭像圖片
                AvatarImage(
                    photoUrl = photoUrl,
                    isAnonymous = isAnonymous,
                    defaultAvatars = defaultAvatars
                )

                // 載入中遮罩
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White
                        )
                    }
                }
            }

            // 編輯按鈕（僅非訪客顯示）
            if (!isAnonymous) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF673AB7))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "編輯頭像",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 顯示名稱
        Text(
            text = displayName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF673AB7)
        )

        // 顯示帳號（僅非訪客顯示）
        if (!isAnonymous) {
            Text(
                text = account,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 頭像圖片元件
 * 根據狀態顯示不同的頭像
 */
@Composable
private fun AvatarImage(
    photoUrl: String,
    isAnonymous: Boolean,
    defaultAvatars: List<Int>
) {
    when {
        // 訪客模式
        isAnonymous -> {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "訪客頭像",
                modifier = Modifier.size(60.dp)
            )
        }
        // 有設定頭像
        photoUrl.isNotEmpty() -> {
            val avatarResId = photoUrl.toIntOrNull()
            if (avatarResId != null && defaultAvatars.contains(avatarResId)) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "頭像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "預設頭像",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        // 預設頭像
        else -> {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "預設頭像",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}