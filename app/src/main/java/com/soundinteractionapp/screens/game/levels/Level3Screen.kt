package com.soundinteractionapp.screens.game.levels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.soundinteractionapp.GameEngine
import com.soundinteractionapp.R

@Composable
fun Level3PitchScreen(onNavigateBack: () -> Unit) {
    var birdY by remember { mutableStateOf(500f) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(180) }
    var isVictory by remember { mutableStateOf(false) }
    var obstacles by remember { mutableStateOf(floatArrayOf()) }
    var isPlaying by remember { mutableStateOf(true) }

    // --- 動畫變數 ---
    val birdSprites = listOf(
        ImageBitmap.imageResource(id = R.drawable.bird_1),
        ImageBitmap.imageResource(id = R.drawable.bird_2),
        ImageBitmap.imageResource(id = R.drawable.bird_3)
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameCounter by remember { mutableIntStateOf(0) }

    // --- 載入管子圖片 ---
    val pipeTopS = ImageBitmap.imageResource(id = R.drawable.pipe_top_s)
    val pipeTopM = ImageBitmap.imageResource(id = R.drawable.pipe_top_m)
    val pipeTopL = ImageBitmap.imageResource(id = R.drawable.pipe_top_l)
    val pipeBottomS = ImageBitmap.imageResource(id = R.drawable.pipe_top_s)
    val pipeBottomM = ImageBitmap.imageResource(id = R.drawable.pipe_top_m)
    val pipeBottomL = ImageBitmap.imageResource(id = R.drawable.pipe_top_l)

    LaunchedEffect(Unit) {
        GameEngine.initGame()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            birdY = GameEngine.updateGame()
            obstacles = GameEngine.getObstacleData()
            val state = GameEngine.getGameState()
            score = state[0].toInt()
            timeLeft = state[1].toInt()

            if (state[3] == 1.0f) {
                isVictory = true
                isPlaying = false
            }

            frameCounter++
            if (frameCounter % 5 == 0) {
                currentFrameIndex = (currentFrameIndex + 1) % birdSprites.size
            }

            delay(16)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF87CEEB))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        GameEngine.flap()
                    })
                }
        ) {
            val scaleFactor = size.height / 2000f

            scale(scale = scaleFactor, pivot = Offset.Zero) {

                val pipeWidth = 350f

                // 遍歷管子
                for (i in obstacles.indices step 3) {
                    if (i + 2 < obstacles.size) {
                        val pipeX = obstacles[i]
                        val gapY = obstacles[i+1]
                        val gapHeight = obstacles[i+2]

                        // === 畫上管 (保持比例) ===
                        val gapTopY = gapY - gapHeight / 2
                        // 根據上方空間選擇圖片
                        val topImage = when {
                            gapTopY < 400 -> pipeTopS
                            gapTopY < 800 -> pipeTopM
                            else -> pipeTopL
                        }
                        // ★ 關鍵：計算保持比例後的高度
                        // 高度 = 圖片原高 * (目標寬度 / 圖片原寬)
                        val drawnHeightTop = topImage.height * (pipeWidth / topImage.width)

                        drawImage(
                            image = topImage,
                            // ★ 關鍵：Y 座標要往上推，讓管口剛好對齊 gapTopY
                            dstOffset = IntOffset(pipeX.toInt(), (gapTopY - drawnHeightTop).toInt()),
                            dstSize = IntSize(pipeWidth.toInt(), drawnHeightTop.toInt())
                        )

                        // === 畫下管 (保持比例) ===
                        val gapBottomY = gapY + gapHeight / 2
                        val bottomSpace = 2000 - gapBottomY
                        // 根據下方空間選擇圖片
                        val bottomImage = when {
                            bottomSpace < 400 -> pipeBottomS
                            bottomSpace < 800 -> pipeBottomM
                            else -> pipeBottomL
                        }
                        // ★ 關鍵：計算保持比例後的高度
                        val drawnHeightBottom = bottomImage.height * (pipeWidth / bottomImage.width)

                        drawImage(
                            image = bottomImage,
                            // Y 座標直接從 gapBottomY 開始畫
                            dstOffset = IntOffset(pipeX.toInt(), gapBottomY.toInt()),
                            dstSize = IntSize(pipeWidth.toInt(), drawnHeightBottom.toInt())
                        )
                    }
                }

                // === 畫鳥 (加大尺寸) ===
                // ★ 修改這裡：把顯示尺寸加大到 130 (原本是 80)
                val visualBirdSize = 250
                drawImage(
                    image = birdSprites[currentFrameIndex],
                    // 為了讓中心點對齊，偏移量要改成新的尺寸的一半
                    dstOffset = IntOffset((300f - visualBirdSize/2).toInt(), (birdY - visualBirdSize/2).toInt()),
                    dstSize = IntSize(visualBirdSize, visualBirdSize)
                )

                // 畫地板
                drawRect(
                    color = Color(0xFFD2B48C),
                    topLeft = Offset(0f, 2000f),
                    size = Size(size.width / scaleFactor, 200f)
                )
            }
        }

        // HUD
        Row(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("分數: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (score < 0) Color.Red else Color.White)
            Text("時間: ${timeLeft}s", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (isVictory) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("🎉 恭喜過關！") },
                text = { Text("時間到！\n最終分數: $score") },
                confirmButton = { Button(onClick = onNavigateBack) { Text("回主選單") } }
            )
        }

        Button(onClick = onNavigateBack, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) { Text("退出") }
    }
}