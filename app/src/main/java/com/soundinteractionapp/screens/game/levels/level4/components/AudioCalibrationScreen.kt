package com.soundinteractionapp.screens.game.levels.level4.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.logic.AudioOffsetManager
import kotlinx.coroutines.delay

/**
 * 音訊校準畫面
 * 讓玩家手動調整音訊延遲以達到最佳遊戲體驗
 */
@Composable
fun AudioCalibrationScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current

    var isCalibrating by remember { mutableStateOf(false) }
    var currentOffset by remember { mutableIntStateOf(AudioOffsetManager.getCurrentOffset()) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // 視覺節拍動畫
    var beatProgress by remember { mutableFloatStateOf(0f) }
    val beatAnimation = rememberInfiniteTransition(label = "beat")

    // 校準統計
    var tapTimes by remember { mutableStateOf<List<Long>>(emptyList()) }
    var averageDeviation by remember { mutableFloatStateOf(0f) }

    val offsetInfo = AudioOffsetManager.getOffsetInfo()

    // 節拍動畫
    LaunchedEffect(isCalibrating) {
        if (isCalibrating) {
            while (isCalibrating) {
                beatProgress = 0f
                delay(50)
                beatProgress = 1f
                delay(550) // 600ms 間隔 (100 BPM)
            }
        }
    }

    // 初始化音樂播放器
    LaunchedEffect(Unit) {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.osu_hit_sound)
            mediaPlayer?.isLooping = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 標題列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }

                Text(
                    text = "音訊延遲校準",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = {
                    AudioOffsetManager.resetToDefaults()
                    currentOffset = AudioOffsetManager.getCurrentOffset()
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重置",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 當前設備資訊卡片
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "當前音訊輸出設備",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val deviceName = when (offsetInfo.currentDevice) {
                        AudioOffsetManager.AudioOutputDevice.SPEAKER -> "📱 手機喇叭"
                        AudioOffsetManager.AudioOutputDevice.BLUETOOTH -> "🎧 藍牙耳機"
                        AudioOffsetManager.AudioOutputDevice.WIRED_HEADSET -> "🎧 有線耳機"
                        else -> "❓ 未知設備"
                    }

                    Text(
                        text = deviceName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "當前總延遲",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${offsetInfo.currentTotalOffset} ms",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "全局調整",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${offsetInfo.globalOffset} ms",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 視覺節拍指示器
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val animatedRadius = if (isCalibrating) {
                            80f + (1f - beatProgress) * 40f
                        } else {
                            80f
                        }

                        val animatedAlpha = if (isCalibrating) {
                            beatProgress
                        } else {
                            0.3f
                        }

                        // 外圈
                        drawCircle(
                            color = Color(0xFF64B5F6).copy(alpha = animatedAlpha),
                            radius = animatedRadius,
                            style = Stroke(width = 8f)
                        )

                        // 內圈
                        drawCircle(
                            color = Color(0xFFFFD700).copy(
                                alpha = if (isCalibrating) 1f - beatProgress else 0.5f
                            ),
                            radius = 60f
                        )
                    }

                    if (!isCalibrating) {
                        Text(
                            text = "點擊開始\n校準",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 校準說明
            if (isCalibrating) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👆 跟隨圓圈節奏點擊畫面\n收集 10 次數據後會自動計算最佳延遲",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (tapTimes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "已收集: ${tapTimes.size}/10",
                        fontSize = 16.sp,
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 提示:\n" +
                                "• 戴上您平常玩遊戲時使用的耳機/喇叭\n" +
                                "• 在安靜的環境中進行校準\n" +
                                "• 盡量準確地跟隨節拍點擊",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 手動調整區域
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "手動微調",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentOffset -= 5
                                AudioOffsetManager.setGlobalOffset(currentOffset)
                            }
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "減少",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "$currentOffset ms",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )

                        IconButton(
                            onClick = {
                                currentOffset += 5
                                AudioOffsetManager.setGlobalOffset(currentOffset)
                            }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "增加",
                                tint = Color.White
                            )
                        }
                    }

                    Slider(
                        value = currentOffset.toFloat(),
                        onValueChange = {
                            currentOffset = it.toInt()
                            AudioOffsetManager.setGlobalOffset(currentOffset)
                        },
                        valueRange = -200f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFF64B5F6)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "音符更早",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "音符更晚",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 底部按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isCalibrating) {
                            isCalibrating = false
                            mediaPlayer?.pause()
                            tapTimes = emptyList()
                        } else {
                            isCalibrating = true
                            tapTimes = emptyList()
                            mediaPlayer?.seekTo(0)
                            mediaPlayer?.start()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCalibrating) Color(0xFFF44336) else Color(0xFF4CAF50)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        if (isCalibrating) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCalibrating) "停止" else "開始校準",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64B5F6)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "完成",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}