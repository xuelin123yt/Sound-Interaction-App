package com.soundinteractionapp.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import kotlin.math.roundToInt

@Composable
fun SettingScreen(
    soundManager: SoundManager,
    onNavigateBack: () -> Unit
) {
    // ✅ 使用 soundManager 的實際值,確保同步
    var masterVolume by remember { mutableFloatStateOf(soundManager.masterVolume) }
    var musicVolume by remember { mutableFloatStateOf(soundManager.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(soundManager.sfxVolume) }

    var isMasterMuted by remember { mutableStateOf(soundManager.isMasterMuted) }
    var isMusicMuted by remember { mutableStateOf(soundManager.isMusicMuted) }
    var isSfxMuted by remember { mutableStateOf(soundManager.isSfxMuted) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
                        text = "音量設定",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 主音量設定
            VolumeControlCard(
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
            VolumeControlCard(
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
            VolumeControlCard(
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VolumeControlCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 標題列
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ✅ 點擊圖標切換靜音
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) Color(0xFF999999) else Color(0xFF333333)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 音量百分比顯示
            Text(
                text = if (isMuted) "靜音" else "${(volume * 100).roundToInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) Color(0xFF999999) else iconColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 音量滑桿
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                enabled = !isMuted,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    activeTrackColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    inactiveTrackColor = if (isMuted) Color(0xFFEEEEEE) else iconColor.copy(alpha = 0.3f),
                    disabledThumbColor = Color(0xFFCCCCCC),
                    disabledActiveTrackColor = Color(0xFFCCCCCC),
                    disabledInactiveTrackColor = Color(0xFFEEEEEE)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 音量標籤
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "靜音",
                    fontSize = 12.sp,
                    color = if (isMuted) Color(0xFFCCCCCC) else Color(0xFF999999)
                )
                Text(
                    text = "最大",
                    fontSize = 12.sp,
                    color = if (isMuted) Color(0xFFCCCCCC) else Color(0xFF999999)
                )
            }
        }
    }
}