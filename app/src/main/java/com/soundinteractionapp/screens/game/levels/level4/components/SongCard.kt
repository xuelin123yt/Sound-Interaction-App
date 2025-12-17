package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast

/**
 * 歌曲卡片 - 固定大小版本（移除直接點擊進入遊戲的邏輯）
 */
@Composable
fun SongCard(
    beatmap: Beatmap,
    isSelected: Boolean,
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

                // 背景層：主題色 + 毛玻璃效果
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            themeColor.copy(alpha = if (isSelected) 0.6f else 0.35f),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 16.dp)
                        .background(
                            themeColor.copy(alpha = if (isSelected) 0.2f else 0.10f),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                if (isSelected) {
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

                // 內容文字層
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
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
                            color = if (isSelected)
                                Color.White.copy(alpha = 0.95f)
                            else
                                Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
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
 * 玩法說明對話框 - 內容區域最大化
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
                // 標題 - 加大呼吸空間
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

                // 內容區域 - 佔最大空間
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InstructionItem(
                        number = "1",
                        title = "選擇歌曲",
                        description = "從左側列表選擇你想要挑戰的歌曲"
                    )

                    InstructionItem(
                        number = "2",
                        title = "跟隨節奏",
                        description = "音符會從上方落下，在正確時機點擊對應按鈕"
                    )

                    InstructionItem(
                        number = "3",
                        title = "獲得分數",
                        description = "Perfect > Great > Good > Miss，盡可能打出完美節奏！"
                    )
                }

                // 按鈕 - 極限壓縮
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

// 同時修改 InstructionItem，讓它超級緊湊：
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
 * 右下角操作按鈕組
 */
@Composable
fun GameActionButtons(
    onStartGame: () -> Unit,
    onShowExample: () -> Unit,
    modifier: Modifier = Modifier,
    isGameStartEnabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 遊玩範例按鈕
        OutlinedButton(
            onClick = onShowExample,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(
                2.dp,
                Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "遊玩範例",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // 開始遊戲按鈕
        Button(
            onClick = onStartGame,
            modifier = Modifier.weight(1f),
            enabled = isGameStartEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64D8FF),
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
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

/**
 * 根據 beatmap ID 返回對應的主題色
 */
private fun getThemeColorById(id: Int): Color {
    return when (id) {
        1 -> Color(0xFF64D8FF)  // 天空藍
        2 -> Color(0xFFC9A227)  // 霧金黃
        3 -> Color(0xFF607D8B)  // 冷調藍灰
        4 -> Color(0xFF2E7D6B)  // 深冷綠
        else -> Color(0xFF64D8FF)
    }
}