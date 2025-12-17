package com.soundinteractionapp.screens.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import kotlin.math.roundToInt

data class Resolution(val width: Int, val height: Int) {
    override fun toString(): String = "${width}×${height}"
}

// 全域解析度狀態管理器
object ResolutionManager {
    var currentScale by mutableStateOf(1f)
    var renderWidth by mutableStateOf(0)
    var renderHeight by mutableStateOf(0)

    // 持久化設定
    private const val PREFS_NAME = "display_settings"
    private const val KEY_SCALE_PERCENTAGE = "scale_percentage"

    fun saveSettings(context: Context, percentage: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_SCALE_PERCENTAGE, percentage)
            apply()
        }
    }

    fun loadSettings(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCALE_PERCENTAGE, 100)
    }
}

@Composable
fun DisplaySection() {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // 計算裝置實際解析度（像素）
    val deviceWidth = configuration.screenWidthDp * configuration.densityDpi / 160
    val deviceHeight = configuration.screenHeightDp * configuration.densityDpi / 160
    val deviceResolution = Resolution(deviceWidth, deviceHeight)

    // 載入保存的百分比設定
    val savedPercentage = remember {
        ResolutionManager.loadSettings(context)
    }

    var scalePercentage by remember { mutableIntStateOf(savedPercentage) }

    // 計算實際渲染解析度
    val scale = scalePercentage / 100f
    val renderWidth = (deviceWidth * scale).roundToInt()
    val renderHeight = (deviceHeight * scale).roundToInt()
    val renderResolution = Resolution(renderWidth, renderHeight)

    // 更新全域狀態 + 保存設定
    LaunchedEffect(scalePercentage) {
        ResolutionManager.currentScale = scale
        ResolutionManager.renderWidth = renderWidth
        ResolutionManager.renderHeight = renderHeight
        ResolutionManager.saveSettings(context, scalePercentage)
    }

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
                .padding(24.dp)
        ) {
            Text(
                text = "解析度設定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "調整遊戲渲染解析度，數值越低效能越好",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 裝置資訊
            InfoRow(
                label = "裝置解析度",
                value = deviceResolution.toString(),
                isHighlight = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color(0xFFEEEEEE))

            Spacer(modifier = Modifier.height(24.dp))

            // 當前渲染解析度顯示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        scalePercentage >= 100 -> Color(0xFFE3F2FD) // 藍色 - 最高
                        scalePercentage >= 75 -> Color(0xFFE8F5E9)  // 綠色 - 高
                        scalePercentage >= 50 -> Color(0xFFFFF9C4)  // 黃色 - 中
                        else -> Color(0xFFFFE0B2)                    // 橘色 - 低
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "渲染解析度",
                                fontSize = 13.sp,
                                color = when {
                                    scalePercentage >= 100 -> Color(0xFF1976D2)
                                    scalePercentage >= 75 -> Color(0xFF388E3C)
                                    scalePercentage >= 50 -> Color(0xFFF57F17)
                                    else -> Color(0xFFE64A19)
                                },
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = renderResolution.toString(),
                                fontSize = 20.sp,
                                color = when {
                                    scalePercentage >= 100 -> Color(0xFF1565C0)
                                    scalePercentage >= 75 -> Color(0xFF2E7D32)
                                    scalePercentage >= 50 -> Color(0xFFF57F17)
                                    else -> Color(0xFFD84315)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "縮放比例",
                                fontSize = 13.sp,
                                color = when {
                                    scalePercentage >= 100 -> Color(0xFF1976D2)
                                    scalePercentage >= 75 -> Color(0xFF388E3C)
                                    scalePercentage >= 50 -> Color(0xFFF57F17)
                                    else -> Color(0xFFE64A19)
                                },
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$scalePercentage%",
                                fontSize = 20.sp,
                                color = when {
                                    scalePercentage >= 100 -> Color(0xFF1565C0)
                                    scalePercentage >= 75 -> Color(0xFF2E7D32)
                                    scalePercentage >= 50 -> Color(0xFFF57F17)
                                    else -> Color(0xFFD84315)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            scalePercentage >= 100 -> "✓ 使用裝置原生解析度，畫質最佳"
                            scalePercentage >= 75 -> "⬇ 解析度略微降低，效能小幅提升"
                            scalePercentage >= 50 -> "⬇ 解析度中等降低，效能明顯提升"
                            else -> "⬇ 解析度大幅降低，效能大幅提升"
                        },
                        fontSize = 11.sp,
                        color = when {
                            scalePercentage >= 100 -> Color(0xFF1976D2)
                            scalePercentage >= 75 -> Color(0xFF388E3C)
                            scalePercentage >= 50 -> Color(0xFFF57F17)
                            else -> Color(0xFFE64A19)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 解析度滑桿
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "解析度比例",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = "$scalePercentage%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = scalePercentage.toFloat(),
                    onValueChange = { scalePercentage = it.roundToInt() },
                    valueRange = 25f..100f,
                    steps = 14, // 25, 30, 35, ..., 95, 100 (每5%)
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF2196F3),
                        activeTrackColor = Color(0xFF2196F3),
                        inactiveTrackColor = Color(0xFFBBDEFB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 刻度標示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "25%",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = "50%",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = "75%",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = "100%",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 快速設定按鈕
            Text(
                text = "快速設定",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSettingButton(
                    label = "最高\n100%",
                    isSelected = scalePercentage == 100,
                    onClick = { scalePercentage = 100 },
                    modifier = Modifier.weight(1f)
                )
                QuickSettingButton(
                    label = "高\n75%",
                    isSelected = scalePercentage == 75,
                    onClick = { scalePercentage = 75 },
                    modifier = Modifier.weight(1f)
                )
                QuickSettingButton(
                    label = "中\n50%",
                    isSelected = scalePercentage == 50,
                    onClick = { scalePercentage = 50 },
                    modifier = Modifier.weight(1f)
                )
                QuickSettingButton(
                    label = "低\n25%",
                    isSelected = scalePercentage == 25,
                    onClick = { scalePercentage = 25 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 效能提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        scalePercentage >= 90 -> Color(0xFFE3F2FD)
                        scalePercentage >= 60 -> Color(0xFFFFF9C4)
                        else -> Color(0xFFFFE0B2)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            scalePercentage >= 90 -> "💡"
                            scalePercentage >= 60 -> "⚡"
                            else -> "🚀"
                        },
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            scalePercentage >= 90 -> "當前設定接近原生解析度，畫質最佳但效能需求較高"
                            scalePercentage >= 60 -> "當前設定平衡畫質與效能，適合多數裝置"
                            else -> "當前設定優先效能，適合配置較低的裝置或追求高幀率"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF795548)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) Color(0xFF2196F3) else Color(0xFF333333)
        )
    }
}

@Composable
private fun QuickSettingButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color(0xFF666666)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ✅ 應用解析度縮放的 Modifier
@Composable
fun Modifier.applyResolutionScale(): Modifier {
    val scale = ResolutionManager.currentScale

    // 只有在縮放比例不是 1.0 時才套用縮放
    return if (scale != 1f) {
        this.graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
        )
    } else {
        this
    }
}