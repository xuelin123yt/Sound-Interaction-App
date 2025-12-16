package com.soundinteractionapp.screens.game.levels.level4.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap

/**
 * 斜切梯形圖片顯示區 - 使用歌曲封面圖（coverImageResId）並根據不同歌曲調整裁切位置
 */
@Composable
fun TrapezoidImageDisplay(beatmap: Beatmap) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 根據不同歌曲調整裁切位置
        val alignment = when (beatmap.id) {
            1 -> Alignment.Center          // OSU_01: 中
            2 -> Alignment.Center          // OSU_02: 中
            3 -> Alignment.CenterEnd       // OSU_03: 中偏右
            4 -> Alignment.Center          // OSU_04: 中
            else -> Alignment.Center
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.50f)  // 從 0.65f 改為 0.50f，上下更窄
                .offset(x = 40.dp)
                .drawWithContent {
                    val width = size.width
                    val height = size.height
                    val skewAmount = height * 0.3f

                    // 定義平行四邊形路徑
                    val trapezoidPath = Path().apply {
                        moveTo(skewAmount, 0f)
                        lineTo(width + skewAmount, 0f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    // 裁切並繪製內容
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
            // 歌曲封面圖片 - 使用 Crop 填滿空間（避免上下留白）
            Image(
                painter = painterResource(id = beatmap.coverImageResId),
                contentDescription = "${beatmap.title} 封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,  // 改回 Crop 填滿整個梯形
                alignment = alignment
            )
        }
    }
}