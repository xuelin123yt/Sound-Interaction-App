package com.soundinteractionapp.screens.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.settings.components.CategoryItem
import com.soundinteractionapp.screens.settings.sections.*

@Composable
fun SettingScreen(
    soundManager: SoundManager,
    onNavigateBack: () -> Unit,
    isLoggedIn: Boolean = false // 新增登入狀態參數
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
                modifier = Modifier.fillMaxSize()
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
                            VolumeSection(
                                soundManager = soundManager,
                                masterVolume = masterVolume,
                                musicVolume = musicVolume,
                                sfxVolume = sfxVolume,
                                isMasterMuted = isMasterMuted,
                                isMusicMuted = isMusicMuted,
                                isSfxMuted = isSfxMuted,
                                onMasterVolumeChange = { masterVolume = it; soundManager.masterVolume = it },
                                onMusicVolumeChange = { musicVolume = it; soundManager.musicVolume = it },
                                onSfxVolumeChange = { sfxVolume = it; soundManager.sfxVolume = it },
                                onMasterMuteToggle = {
                                    soundManager.toggleMasterMute()
                                    isMasterMuted = soundManager.isMasterMuted
                                },
                                onMusicMuteToggle = {
                                    soundManager.toggleMusicMute()
                                    isMusicMuted = soundManager.isMusicMuted
                                },
                                onSfxMuteToggle = {
                                    soundManager.toggleSfxMute()
                                    isSfxMuted = soundManager.isSfxMuted
                                },
                                onResetVolumes = {
                                    masterVolume = 1.0f
                                    musicVolume = 1.0f
                                    sfxVolume = 1.0f
                                    soundManager.masterVolume = 1.0f
                                    soundManager.musicVolume = 1.0f
                                    soundManager.sfxVolume = 1.0f

                                    if (soundManager.isMasterMuted) soundManager.toggleMasterMute()
                                    if (soundManager.isMusicMuted) soundManager.toggleMusicMute()
                                    if (soundManager.isSfxMuted) soundManager.toggleSfxMute()

                                    isMasterMuted = false
                                    isMusicMuted = false
                                    isSfxMuted = false
                                }
                            )
                        }
                        "顯示" -> {
                            DisplaySection()
                        }
                        "遊戲" -> {
                            GameSection()
                        }
                        "其他" -> {
                            OtherSection(
                                soundManager = soundManager,
                                isLoggedIn = isLoggedIn
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}