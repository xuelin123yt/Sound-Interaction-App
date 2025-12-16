package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast

/**
 * 歌曲卡片 - 固定大小版本（只改變透明度和陰影）
 */
@Composable
fun SongCard(
    beatmap: Beatmap,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // ✅ 移除 scale 和 offset 動畫，保持固定大小
    // 只用透明度和陰影來強調選中狀態

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    // 根據 beatmap.id 獲取主題色
    val themeColor = getThemeColorById(beatmap.id)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)  // ✅ 移除垂直 padding
            .zIndex(if (isSelected) 10f else 0f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)  // ✅ 固定高度，不再 scale
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ============== 1. 背景層：主題色 + 毛玻璃效果 ==============

                // 主題色背景層
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            themeColor.copy(alpha = if (isSelected) 0.6f else 0.35f),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // 毛玻璃 (Blur) 效果層
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 16.dp)
                        .background(
                            themeColor.copy(alpha = if (isSelected) 0.2f else 0.10f),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // 選中時的邊框高亮
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

                // ============== 2. 內容文字層 ==============
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // 標題 - 使用 MarqueeText 實現跑馬燈效果
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

                    // 描述文字
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