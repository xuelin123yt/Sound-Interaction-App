package com.soundinteractionapp.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.soundinteractionapp.BuildConfig
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import kotlin.math.roundToInt

@Composable
fun SettingScreen(
    soundManager: SoundManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // ✅ 使用 soundManager 的實際值,確保同步
    var masterVolume by remember { mutableFloatStateOf(soundManager.masterVolume) }
    var musicVolume by remember { mutableFloatStateOf(soundManager.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(soundManager.sfxVolume) }

    var isMasterMuted by remember { mutableStateOf(soundManager.isMasterMuted) }
    var isMusicMuted by remember { mutableStateOf(soundManager.isMusicMuted) }
    var isSfxMuted by remember { mutableStateOf(soundManager.isSfxMuted) }

    // 選中的分類
    var selectedCategory by remember { mutableStateOf("音量") }

    // 齒輪旋轉動畫
    val infiniteTransition = rememberInfiniteTransition(label = "gearRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gearRotate"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景漸層
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F5F5),
                            Color(0xFFE8EAF6)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 頂部標題列
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundManager.playSFX("cancel")
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color(0xFF673AB7),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Image(
                        painter = painterResource(id = R.drawable.setting),
                        contentDescription = "設定圖示",
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                rotationZ = rotation
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when (selectedCategory) {
                            "音量" -> "音量設定"
                            "顯示" -> "顯示設定"
                            "遊戲" -> "遊戲設定"
                            "其他" -> "其他設定"
                            else -> "設定"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
            }

            // 主要內容區域（左右分欄）
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 左側選單
                Surface(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    color = Color(0xFFEEEEEE)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 音量設定
                        CategoryItem(
                            icon = Icons.Default.VolumeUp,
                            isSelected = selectedCategory == "音量",
                            onClick = {
                                soundManager.playSFX("settings")
                                selectedCategory = "音量"
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 顯示設定
                        CategoryItem(
                            icon = Icons.Default.Image,
                            isSelected = selectedCategory == "顯示",
                            onClick = {
                                soundManager.playSFX("settings")
                                selectedCategory = "顯示"
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 遊戲設定
                        CategoryItem(
                            icon = Icons.Default.SportsEsports,
                            isSelected = selectedCategory == "遊戲",
                            onClick = {
                                soundManager.playSFX("settings")
                                selectedCategory = "遊戲"
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 其他設定
                        CategoryItem(
                            icon = Icons.Default.Info,
                            isSelected = selectedCategory == "其他",
                            onClick = {
                                soundManager.playSFX("settings")
                                selectedCategory = "其他"
                            }
                        )
                    }
                }

                // 右側內容區域
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 24.dp)
                ) {
                    // 根據選中的分類顯示內容
                    when (selectedCategory) {
                        "音量" -> {
                            // 主音量設定
                            HorizontalVolumeControl(
                                title = "主音量",
                                icon = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                iconColor = Color(0xFF673AB7),
                                volume = masterVolume,
                                isMuted = isMasterMuted,
                                onVolumeChange = { newVolume ->
                                    masterVolume = newVolume
                                    soundManager.masterVolume = newVolume
                                },
                                onMuteToggle = {
                                    soundManager.toggleMasterMute()
                                    isMasterMuted = soundManager.isMasterMuted
                                    soundManager.playSFX("settings")
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 音樂音量設定
                            HorizontalVolumeControl(
                                title = "音樂音量",
                                icon = if (isMusicMuted) Icons.Default.MusicOff else Icons.Default.MusicNote,
                                iconColor = Color(0xFF4FC3F7),
                                volume = musicVolume,
                                isMuted = isMusicMuted,
                                onVolumeChange = { newVolume ->
                                    musicVolume = newVolume
                                    soundManager.musicVolume = newVolume
                                },
                                onMuteToggle = {
                                    soundManager.toggleMusicMute()
                                    isMusicMuted = soundManager.isMusicMuted
                                    soundManager.playSFX("settings")
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 音效音量設定
                            HorizontalVolumeControl(
                                title = "音效音量",
                                icon = if (isSfxMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                iconColor = Color(0xFFFF9800),
                                volume = sfxVolume,
                                isMuted = isSfxMuted,
                                onVolumeChange = { newVolume ->
                                    sfxVolume = newVolume
                                    soundManager.sfxVolume = newVolume
                                },
                                onMuteToggle = {
                                    soundManager.toggleSfxMute()
                                    isSfxMuted = soundManager.isSfxMuted
                                    soundManager.playSFX("settings")
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 重置按鈕
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            soundManager.playSFX("settings")

                                            // 重置音量
                                            masterVolume = 1.0f
                                            musicVolume = 1.0f
                                            sfxVolume = 1.0f
                                            soundManager.masterVolume = 1.0f
                                            soundManager.musicVolume = 1.0f
                                            soundManager.sfxVolume = 1.0f

                                            // 取消所有靜音
                                            if (soundManager.isMasterMuted) soundManager.toggleMasterMute()
                                            if (soundManager.isMusicMuted) soundManager.toggleMusicMute()
                                            if (soundManager.isSfxMuted) soundManager.toggleSfxMute()

                                            isMasterMuted = false
                                            isMusicMuted = false
                                            isSfxMuted = false
                                        }
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "重置",
                                        tint = Color(0xFF673AB7),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "重置為預設值",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF673AB7)
                                    )
                                }
                            }
                        }
                        "顯示" -> {
                            // 顯示設定內容（暫時留空）
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "顯示設定（待開發）",
                                        fontSize = 18.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        }
                        "遊戲" -> {
                            // 遊戲設定內容（暫時留空）
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "遊戲設定（待開發）",
                                        fontSize = 18.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        }
                        "其他" -> {
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

                                    // 第二排：2 個成員（精確對齊到上面的中間位置）
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 56.dp),  // 精確對齊
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
                                            // 開啟 Email Intent
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:")
                                                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@soundinteraction.com")) // 替換成你的 Email
                                                putExtra(Intent.EXTRA_SUBJECT, "Sound Interaction App - 意見回饋")
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    """
                                                    請在此輸入您的意見或問題：
                                                    
                                                    
                                                    
                                                    ---
                                                    App 版本資訊：
                                                    Git Commit: ${BuildConfig.COMMIT_HASH}
                                                    Build Date: ${BuildConfig.BUILD_DATE}
                                                    """.trimIndent()
                                                )
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // 處理沒有郵件應用的情況
                                            }
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
                                                text = "點擊發送郵件給我們",
                                                fontSize = 14.sp,
                                                color = Color(0xFF666666)
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TeamMember(
    avatarRes: Int,
    name: String,
    accentColor: Color = Color(0xFF2196F3)
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 頭像容器（帶彩色邊框）
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 使用 AsyncImage 支持 GIF 動畫
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarRes)
                    .decoderFactory(
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoderDecoder.Factory()
                        } else {
                            GifDecoder.Factory()
                        }
                    )
                    .build(),
                contentDescription = name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        }

        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
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
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun CategoryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected)
                    Color(0xFFE1BEE7)
                else
                    Color.Transparent
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF673AB7) else Color(0xFF999999),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun HorizontalVolumeControl(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側圖標和標題
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMuted)
                                Color(0xFFEEEEEE)
                            else
                                iconColor.copy(alpha = 0.15f)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onMuteToggle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = if (isMuted) "取消靜音" else "靜音",
                        tint = if (isMuted) Color(0xFF999999) else iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMuted) Color(0xFF999999) else Color(0xFF333333)
                )
            }

            // 中間滑桿
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                enabled = !isMuted,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    activeTrackColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    inactiveTrackColor = if (isMuted) Color(0xFFEEEEEE) else iconColor.copy(alpha = 0.3f),
                    disabledThumbColor = Color(0xFFCCCCCC),
                    disabledActiveTrackColor = Color(0xFFCCCCCC),
                    disabledInactiveTrackColor = Color(0xFFEEEEEE)
                )
            )

            // 右側百分比
            Text(
                text = if (isMuted) "0" else "${(volume * 100).roundToInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) Color(0xFF999999) else iconColor,
                modifier = Modifier.width(50.dp)
            )
        }
    }
}