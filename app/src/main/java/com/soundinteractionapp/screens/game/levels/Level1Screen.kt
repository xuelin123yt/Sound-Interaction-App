package com.soundinteractionapp.screens.game.levels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.utils.GameInputManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameMillis
import com.soundinteractionapp.data.RankingViewModel
import kotlin.math.abs

// 遊戲狀態
enum class GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED }

@Composable
fun Level1FollowBeatScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    rankingViewModel: RankingViewModel
) {
    // --- 狀態管理 ---
    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }

    var score by remember { mutableStateOf(0) }
    var combo by remember { mutableStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var countdownValue by remember { mutableStateOf(3) }

    // 視覺變數
    var trackBorderColor by remember { mutableStateOf(Color.White.copy(alpha = 0.5f)) }
    var effectColor by remember { mutableStateOf(Color.White) }
    val hitEffectScale = remember { Animatable(1f) }
    val palePink = Color(0xFFF48FB1)

    // 時間與譜面 (動態載入)
    var startTime by remember { mutableStateOf(0L) }
    var currentTime by remember { mutableStateOf(0L) }
    val currentNotes = remember { mutableStateListOf<Note>() }

    // 語錄
    val perfectPhrases = remember { listOf("太棒了!", "太厲害了吧", "是個高手") }
    val goodPhrases = remember { listOf("差一點呀", "很接近了!") }
    val missPhrases = remember { listOf("好可惜呀", "加把勁", "還是菜鳥呢") }

    // --- 軌道顏色還原 ---
    LaunchedEffect(trackBorderColor) {
        if (trackBorderColor != Color.White.copy(alpha = 0.5f)) {
            delay(200)
            trackBorderColor = Color.White.copy(alpha = 0.5f)
        }
    }

    // --- 遊戲流程控制 ---
    LaunchedEffect(gameState) {
        // 當倒數計時開始時，初始化遊戲
        if (gameState == GameState.COUNTDOWN) {
            score = 0
            combo = 0
            feedbackText = "Ready..."
            countdownValue = 3

            // ★ 1. 載入對應難度的譜面
            currentNotes.clear()
            // 使用 map copy 確保每次重玩都是新的狀態
            currentNotes.addAll(selectedDifficulty.chartData.map { it.copy(isHit = false) })

            delay(500)
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            feedbackText = "GO!"
            gameState = GameState.PLAYING

            // ★ 2. 播放對應難度的音樂
            soundManager.playMusic(selectedDifficulty.musicResId)

            startTime = System.currentTimeMillis()
        }

        // 當遊戲結束時
        if (gameState == GameState.FINISHED) {
            soundManager.stopMusic()
            feedbackText = "遊戲結束！"
            // 儲存分數 (使用該難度的 scoreId)
            rankingViewModel.onGameFinished(levelId = selectedDifficulty.scoreId, finalScore = score)
            delay(3000)
            onNavigateBack()
        }
    }

    // --- 遊戲邏輯迴圈 (Tick) ---
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            // 使用我們在 Difficulty 設定的 duration 作為結束時間，稍微加一點緩衝
            val gameDuration = selectedDifficulty.duration + 2000L

            while (isActive) {
                currentTime = System.currentTimeMillis() - startTime

                // Miss 判定
                currentNotes.forEach { note ->
                    if (!note.isHit && (currentTime - note.targetTime > 200)) {
                        note.isHit = true
                        combo = 0
                        feedbackText = missPhrases.random()
                        effectColor = Color.Red
                        trackBorderColor = Color.Red
                    }
                }

                // 結束判定
                if (currentTime > gameDuration) {
                    gameState = GameState.FINISHED
                }

                withFrameMillis { }
            }
        }
    }

    // --- 輸入監聽 ---
    LaunchedEffect(Unit) {
        GameInputManager.keyEvents.collectLatest {
            if (gameState == GameState.PLAYING) {
                val targetNote = currentNotes.firstOrNull { note ->
                    !note.isHit && abs(note.targetTime - currentTime) < 150
                }

                if (targetNote != null) {
                    val diff = abs(targetNote.targetTime - currentTime)
                    targetNote.isHit = true

                    if (diff < 60) {
                        score += 100
                        combo++
                        feedbackText = perfectPhrases.random()
                        effectColor = palePink
                        trackBorderColor = palePink
                        launch {
                            hitEffectScale.snapTo(1.5f)
                            hitEffectScale.animateTo(1f, tween(300))
                        }
                    } else {
                        score += 50
                        combo++
                        feedbackText = goodPhrases.random()
                        effectColor = Color.Cyan
                        trackBorderColor = Color.Cyan
                        launch {
                            hitEffectScale.snapTo(1.2f)
                            hitEffectScale.animateTo(1f, tween(150))
                        }
                    }
                }
            }
        }
    }

    // --- 畫面繪製 ---
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF222222)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // (A) 難度選擇選單
            if (gameState == GameState.SELECTION) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "請選擇難度",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // 動態產生三個按鈕
                    Difficulty.values().forEach { difficulty ->
                        Button(
                            onClick = {
                                selectedDifficulty = difficulty
                                gameState = GameState.COUNTDOWN
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = difficulty.color),
                            modifier = Modifier.width(220.dp).height(60.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(difficulty.label, fontSize = 24.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(onClick = onNavigateBack) {
                        Text("返回", color = Color.White)
                    }
                }
            }

            // (B) 倒數畫面
            if (gameState == GameState.COUNTDOWN) {
                Text(
                    text = if (countdownValue > 0) "$countdownValue" else "GO!",
                    fontSize = 120.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // (C) 遊戲進行畫面
            if (gameState == GameState.PLAYING) {
                // 1. 頂部資訊
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("難度: ${selectedDifficulty.label}", color = selectedDifficulty.color, fontSize = 18.sp)
                    Text("分數: $score", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("Combo: $combo", style = MaterialTheme.typography.displayMedium, color = Color.White)
                }

                // 2. 遊戲軌道
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerY = size.height / 2
                    val judgeLineX = 250f
                    // 根據難度取得速度
                    val currentSpeed = selectedDifficulty.speed

                    // 軌道背景
                    drawRect(
                        color = Color.White.copy(alpha = 0.1f),
                        topLeft = Offset(0f, centerY - 80f),
                        size = Size(size.width, 160f)
                    )
                    // 上下邊線
                    drawLine(trackBorderColor, Offset(0f, centerY - 80f), Offset(size.width, centerY - 80f), 4f)
                    drawLine(trackBorderColor, Offset(0f, centerY + 80f), Offset(size.width, centerY + 80f), 4f)

                    // 判定圈
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 60f,
                        center = Offset(judgeLineX, centerY),
                        style = Stroke(width = 4f)
                    )

                    // 打擊特效
                    if (hitEffectScale.value > 1.0f) {
                        drawCircle(
                            color = effectColor,
                            radius = 60f * hitEffectScale.value,
                            center = Offset(judgeLineX, centerY),
                            style = Stroke(width = 8f)
                        )
                    }

                    // 繪製音符
                    currentNotes.forEach { note ->
                        if (!note.isHit) {
                            val noteX = judgeLineX + (note.targetTime - currentTime) * currentSpeed

                            // 只畫畫面內的
                            if (noteX > -100 && noteX < size.width + 100) {
                                // 實心圓
                                drawCircle(
                                    color = Color(0xFFFF5252),
                                    radius = 40f,
                                    center = Offset(noteX, centerY)
                                )
                                // 空心圓框
                                drawCircle(
                                    color = Color.White,
                                    radius = 40f,
                                    center = Offset(noteX, centerY),
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }

                // 3. 語錄回饋
                Box(modifier = Modifier.align(Alignment.Center).padding(top = 220.dp)) {
                    Text(feedbackText, style = MaterialTheme.typography.headlineLarge, color = effectColor)
                }

                // ★ 4. 底部音樂進度條
                // 計算進度 0.0 ~ 1.0
                val progress = (currentTime.toFloat() / selectedDifficulty.duration.toFloat()).coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(12.dp),
                    color = selectedDifficulty.color, // 使用難度代表色
                    trackColor = Color.Gray.copy(alpha = 0.3f), // 軌道顏色
                )

                // 5. 退出按鈕 (在進度條上面一點點)
                Button(
                    onClick = {
                        soundManager.stopMusic()
                        onNavigateBack()
                    },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Text("退出")
                }
            }
        }
    }
}