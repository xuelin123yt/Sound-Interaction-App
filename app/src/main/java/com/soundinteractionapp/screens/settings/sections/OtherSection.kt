package com.soundinteractionapp.screens.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.BuildConfig
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.repository.FeedbackRepository
import com.soundinteractionapp.screens.settings.components.FeedbackDialog
import com.soundinteractionapp.screens.settings.components.InfoCard
import com.soundinteractionapp.screens.settings.components.InfoRow
import com.soundinteractionapp.screens.settings.components.TeamMember
import kotlinx.coroutines.launch

@Composable
fun OtherSection(
    soundManager: SoundManager,
    isLoggedIn: Boolean = false
) {
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val feedbackRepository = remember { FeedbackRepository() }

    Column {
        // 開發團隊資訊卡片
        InfoCard(
            title = "開發團隊",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF2196F3)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 第一排：4 個成員
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TeamMember(
                        avatarRes = R.drawable.avatar_411312121,
                        name = "王奕翔",
                        accentColor = Color(0xFF673AB7)
                    )
                    TeamMember(
                        avatarRes = R.drawable.avatar_411322388,
                        name = "黃義祥",
                        accentColor = Color(0xFF2196F3)
                    )
                    TeamMember(
                        avatarRes = R.drawable.avatar_411322346,
                        name = "黃士豪",
                        accentColor = Color(0xFFFF9800)
                    )
                    TeamMember(
                        avatarRes = R.drawable.avatar_411312228,
                        name = "張佑先",
                        accentColor = Color(0xFF4CAF50)
                    )
                }

                // 第二排：2 個成員
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TeamMember(
                        avatarRes = R.drawable.avatar_411300467,
                        name = "李維駿",
                        accentColor = Color(0xFFE91E63)
                    )
                    TeamMember(
                        avatarRes = R.drawable.avatar_411303156,
                        name = "黃福恩",
                        accentColor = Color(0xFF00BCD4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 版本資訊卡片
        InfoCard(
            title = "版本資訊",
            icon = Icons.Default.Info,
            iconColor = Color(0xFF4CAF50)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Git Commit",
                    value = BuildConfig.COMMIT_HASH
                )
                Divider(color = Color(0xFFEEEEEE))
                InfoRow(
                    label = "Build Date",
                    value = BuildConfig.BUILD_DATE
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 問題與意見回饋卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        soundManager.playSFX("settings")
                        showFeedbackDialog = true
                    }
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "問題回饋",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "問題與意見回饋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isLoggedIn) "點擊填寫回饋表單" else "需要登入才能使用",
                            fontSize = 14.sp,
                            color = if (isLoggedIn) Color(0xFF666666) else Color(0xFFFF9800)
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // 顯示回饋對話框
    if (showFeedbackDialog) {
        FeedbackDialog(
            isLoggedIn = isLoggedIn,
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { title, content ->
                scope.launch {
                    try {
                        feedbackRepository.submitFeedback(title, content)
                        soundManager.playSFX("settings")
                        // Dialog 內部會自動顯示綠色成功提示並在 1.5 秒後關閉
                    } catch (e: Exception) {
                        soundManager.playSFX("cancel")
                        // 錯誤處理（如果需要可以添加錯誤提示）
                    }
                }
            }
        )
    }
}