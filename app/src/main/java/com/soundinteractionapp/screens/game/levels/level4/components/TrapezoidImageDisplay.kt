package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap

/**
 * 斜切梯形圖片顯示區 - 歌曲名稱顯示「前一首」歌曲的主題色
 */
@Composable
fun TrapezoidImageDisplay(
    beatmap: Beatmap,
    isLocked: Boolean = false,
    unlockHintText: String = ""
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        val alignment = when (beatmap.id) {
            1 -> Alignment.Center
            2 -> Alignment.Center
            3 -> Alignment.CenterEnd
            4 -> Alignment.Center
            else -> Alignment.Center
        }

        // ✅ 獲取「前一首」歌曲的主題色
        val previousSongId = if (beatmap.id > 1) beatmap.id - 1 else beatmap.id
        val themeColor = getThemeColorById(previousSongId)

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.50f)
                .offset(x = 40.dp)
                .drawWithContent {
                    val width = size.width
                    val height = size.height
                    val skewAmount = height * 0.3f

                    val trapezoidPath = Path().apply {
                        moveTo(skewAmount, 0f)
                        lineTo(width + skewAmount, 0f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    clipPath(trapezoidPath) {
                        this@drawWithContent.drawContent()
                    }

                    // 白色外邊框
                    drawPath(
                        path = trapezoidPath,
                        color = Color.White.copy(alpha = 0.5f),
                        style = Stroke(width = 5f)
                    )

                    // 白色內邊框
                    val innerPath = Path().apply {
                        moveTo(skewAmount + 12f, 12f)
                        lineTo(width + skewAmount - 12f, 12f)
                        lineTo(width - 12f, height - 12f)
                        lineTo(12f, height - 12f)
                        close()
                    }

                    drawPath(
                        path = innerPath,
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = 2f)
                    )
                }
        ) {
            // 歌曲封面圖片
            Image(
                painter = painterResource(id = beatmap.coverImageResId),
                contentDescription = "${beatmap.title} 封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = alignment,
                colorFilter = if (isLocked) {
                    ColorFilter.colorMatrix(ColorMatrix().apply {
                        setToSaturation(0.3f)
                    })
                } else null
            )

            // ✅ 鎖定遮罩 - 深色半透明背景
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ 使用 AnnotatedString 讓歌曲名稱顯示「前一首」的主題色
                    val styledText = buildAnnotatedString {
                        val parts = unlockHintText.split("「", "」")

                        if (parts.size >= 3) {
                            // 前面的文字（白色）
                            append(parts[0])
                            append("「")

                            // 歌曲名稱（前一首的主題色）
                            withStyle(style = SpanStyle(color = themeColor, fontWeight = FontWeight.ExtraBold)) {
                                append(parts[1])
                            }

                            // 後面的文字（白色）
                            append("」")
                            append(parts[2])
                        } else {
                            // 如果沒有「」符號，整段顯示白色
                            append(unlockHintText)
                        }
                    }

                    Text(
                        text = styledText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}