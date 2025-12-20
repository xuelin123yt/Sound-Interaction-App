package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import kotlin.math.roundToInt

/**
 * 歌曲卡片 - 支援鎖定狀態顯示（鎖頭在右側，標題和內容始終顯示）
 */
@Composable
fun SongCard(
    beatmap: Beatmap,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelect: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    // ✅ 鎖頭大小動畫
    val lockSize by animateDpAsState(
        targetValue = if (isSelected && isLocked) 36.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "lockSize"
    )

    val themeColor = getThemeColorById(beatmap.id)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .zIndex(if (isSelected) 10f else 0f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .clickable(onClick = onSelect),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 背景層
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isLocked) {
                                Color.Gray.copy(alpha = if (isSelected) 0.4f else 0.25f)
                            } else {
                                themeColor.copy(alpha = if (isSelected) 0.6f else 0.35f)
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 16.dp)
                        .background(
                            if (isLocked) {
                                Color.Gray.copy(alpha = if (isSelected) 0.15f else 0.08f)
                            } else {
                                themeColor.copy(alpha = if (isSelected) 0.2f else 0.10f)
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                if (isSelected && !isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        themeColor.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        themeColor.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                }

                // ✅ 內容層：標題和描述始終顯示在左側
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左側：文字內容
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.height(if (isSelected) 28.dp else 24.dp)) {
                            MarqueeText(
                                text = beatmap.title,
                                style = TextStyle(
                                    fontSize = if (isSelected) 20.sp else 17.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                marqueeSpeed = if (isSelected) 40.dp else 30.dp
                            )
                        }

                        if (beatmap.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(0.dp))
                            Text(
                                text = beatmap.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) {
                                    Color.White.copy(alpha = 0.95f)
                                } else {
                                    Color.White.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }

                    // 右側：鎖頭圖標（僅在鎖定時顯示）
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "已鎖定",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(lockSize)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 根據 beatmap ID 返回對應的主題色
 */
fun getThemeColorById(id: Int): Color {
    return when (id) {
        1 -> Color(0xFF64D8FF)  // 天空藍
        2 -> Color(0xFFC9A227)  // 霧金黃
        3 -> Color(0xFF607D8B)  // 冷調藍灰
        4 -> Color(0xFF2E7D6B)  // 深冷綠
        5 -> Color(0xFF64D8FF)  // 天空藍（第五首）
        else -> Color(0xFF64D8FF)
    }
}

/**
 * 右上角玩法說明按鈕
 */
@Composable
fun GameInstructionsButton(
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF64D8FF).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "玩法說明",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    if (showDialog) {
        GameInstructionsDialog(onDismiss = { showDialog = false })
    }
}

/**
 * 玩法說明對話框
 */
@Composable
fun GameInstructionsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🎮 遊戲玩法",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64D8FF),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(
                        color = Color(0xFF64D8FF).copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InstructionItem(
                        number = "1",
                        title = "跟著節奏，準備打擊",
                        description = "音樂開始後，音符會依節奏出現在畫面上，請留意節拍與接近判定線的時機"
                    )

                    InstructionItem(
                        number = "2",
                        title = "在正確時機點擊",
                        description = "當音符進入判定範圍時，點擊對應按鍵或畫面。點擊的準確度將決定判定結果（Perfect／Good／Miss）"
                    )

                    InstructionItem(
                        number = "3",
                        title = "累積連擊與高分",
                        description = "連續成功打擊可累積連擊數並提升分數，錯失音符將中斷連擊。完成歌曲後，系統會依表現給予評價。"
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64D8FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "我知道了",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    Color(0xFF64D8FF).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64D8FF)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * 右下角操作按鈕組 - ✅ 支援主題配色和鎖定狀態
 */
@Composable
fun GameActionButtons(
    onStartGame: () -> Unit,
    onShowExample: () -> Unit,
    modifier: Modifier = Modifier,
    isGameStartEnabled: Boolean = true,
    themeColor: Color = Color(0xFF64D8FF)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isGameStartEnabled) {
            // ✅ 解鎖狀態：兩個按鈕都使用 100% 主題色實色
            Button(
                onClick = onShowExample,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor  // ✅ 100% 實色
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "遊玩範例",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = onStartGame,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor  // ✅ 100% 實色
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "開始遊戲",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            // ✅ 鎖定狀態：兩個按鈕都使用灰色 30% 透明度
            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "遊玩範例",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "開始遊戲",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 跑馬燈效果的文字 Composable
 */
@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    marqueeSpeed: Dp = 30.dp,
    delay: Int = 1000,
    space: Dp = 10.dp
) {
    val density = LocalDensity.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val textWidth = textLayoutResult?.size?.width ?: 0
    val isMarqueeEnabled = textLayoutResult?.didOverflowWidth ?: false

    val scrollOffsetDp = if (isMarqueeEnabled) {
        val totalDistanceDp = with(density) {
            textWidth.toDp() + space.coerceAtLeast(0.dp)
        }
        val duration = ((totalDistanceDp.value / marqueeSpeed.value) * 1000)
            .toInt()
            .coerceAtLeast(100)

        rememberInfiniteTransition(label = "MarqueeTransition").animateFloat(
            initialValue = 0f,
            targetValue = -totalDistanceDp.value,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    delayMillis = delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "MarqueeOffset"
        ).value.dp
    } else {
        0.dp
    }

    Layout(
        content = {
            Text(
                text = text,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                onTextLayout = { result ->
                    textLayoutResult = result
                }
            )

            if (isMarqueeEnabled) {
                Text(
                    text = text,
                    style = style,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    ) { measurables, constraints ->

        val primaryTextPlaceable = measurables[0].measure(Constraints())
        val actualTextWidth = primaryTextPlaceable.width
        val containerWidth = constraints.maxWidth
        val height = primaryTextPlaceable.height
        val offsetPx = with(density) { scrollOffsetDp.toPx() }.roundToInt()

        layout(containerWidth, height) {
            primaryTextPlaceable.placeRelative(offsetPx, 0)

            if (measurables.size > 1) {
                val secondaryTextPlaceable = measurables[1].measure(Constraints())
                val spacePx = with(density) { space.toPx() }.roundToInt()
                val secondTextX = offsetPx + actualTextWidth + spacePx
                secondaryTextPlaceable.placeRelative(secondTextX, 0)
            }
        }
    }
}