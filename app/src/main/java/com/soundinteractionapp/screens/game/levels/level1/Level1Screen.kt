package com.soundinteractionapp.screens.game.levels.level1

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.utils.GameInputManager
import com.soundinteractionapp.utils.GameProgressManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameMillis
import kotlin.math.abs

// 遊戲狀態
enum class GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED, RESULT }

@Composable
fun Level1FollowBeatScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    rankingViewModel: RankingViewModel
) {
    val context = LocalContext.current
    val progressManager = remember { GameProgressManager(context) }

    // ✅ 檢查是否為訪客
    val auth = FirebaseAuth.getInstance()
    val isGuest = auth.currentUser?.isAnonymous == true

    // --- 狀態管理 ---
    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var countdownValue by remember { mutableIntStateOf(3) }

    // 衝刺 Time 計數器
    var perfectStreak by remember { mutableIntStateOf(0) }

    // 統計數據
    var perfectCount by remember { mutableIntStateOf(0) }
    var goodCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }

    // 視覺變數
    var trackBorderColor by remember { mutableStateOf(Color.White.copy(alpha = 0.5f)) }
    var effectColor by remember { mutableStateOf(Color.White) }

    // 懲罰閃光
    val penaltyFlashAlpha = remember { Animatable(0f) }

    val hitEffectScale = remember { Animatable(1f) }

    // 完美特效改成綠色
    val perfectColor = Color(0xFF00E676)
    val goldColor = Color(0xFFFFD700)

    // 衝刺 Time 動畫
    val infiniteTransition = rememberInfiniteTransition(label = "rushTimeAnim")
    val rushTimeOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rushTimeOffsetY"
    )

    // 時間與譜面
    var startTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    val currentNotes = remember { mutableStateListOf<Note>() }

    // 語錄
    val perfectPhrases = remember { listOf("太棒了!", "太厲害了吧", "是個高手") }
    val goodPhrases = remember { listOf("差一點呀", "很接近了!") }
    val missPhrases = remember { listOf("好可惜呀", "加把勁", "還是菜鳥呢") }

    LaunchedEffect(trackBorderColor) {
        if (trackBorderColor != Color.White.copy(alpha = 0.5f)) {
            delay(200)
            trackBorderColor = Color.White.copy(alpha = 0.5f)
        }
    }

    // --- 遊戲流程控制 ---
    LaunchedEffect(gameState) {
        if (gameState == GameState.COUNTDOWN) {
            score = 0
            combo = 0
            perfectStreak = 0
            perfectCount = 0
            goodCount = 0
            missCount = 0
            feedbackText = "Ready..."
            countdownValue = 3
            currentNotes.clear()
            currentNotes.addAll(selectedDifficulty.chartData.map { it.copy(isHit = false) })
            delay(500)
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            feedbackText = "GO!"
            gameState = GameState.PLAYING
            soundManager.playMusic(selectedDifficulty.musicResId)
            startTime = System.currentTimeMillis()
        }

        if (gameState == GameState.FINISHED) {
            soundManager.stopMusic()

            // ✅ 只有非訪客才處理解鎖邏輯
            if (!isGuest) {
                // 解鎖條件 (8500 / 14000)
                if (selectedDifficulty == Difficulty.EASY && score >= 8500) {
                    progressManager.unlockDifficulty(Difficulty.NORMAL.label)
                } else if (selectedDifficulty == Difficulty.NORMAL && score >= 14000) {
                    progressManager.unlockDifficulty(Difficulty.HARD.label)
                }
            }

            rankingViewModel.onGameFinished(levelId = selectedDifficulty.scoreId, finalScore = score)
            gameState = GameState.RESULT
        }
    }

    // --- 遊戲邏輯迴圈 ---
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            val gameDuration = selectedDifficulty.duration + 2000L
            while (isActive) {
                currentTime = System.currentTimeMillis() - startTime
                currentNotes.forEach { note ->
                    if (!note.isHit && (currentTime - note.targetTime > 200)) {
                        note.isHit = true
                        combo = 0
                        perfectStreak = 0
                        missCount++
                        feedbackText = missPhrases.random()
                        effectColor = Color.Red
                        trackBorderColor = Color.Red
                    }
                }
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
                    combo++

                    val rushThreshold = when (selectedDifficulty) {
                        Difficulty.EASY -> 20
                        Difficulty.NORMAL -> 40
                        Difficulty.HARD -> 60
                    }
                    val isRushTime = combo >= rushThreshold

                    if (diff < 60) {
                        // --- PERFECT ---
                        perfectStreak++
                        score += if (isRushTime) 150 else 100
                        perfectCount++
                        feedbackText = perfectPhrases.random()

                        // 一般完美用綠色，衝刺時用金色
                        effectColor = if (isRushTime) goldColor else perfectColor
                        trackBorderColor = if (isRushTime) goldColor else perfectColor

                        launch {
                            hitEffectScale.snapTo(1.5f)
                            hitEffectScale.animateTo(1f, tween(300))
                        }
                    } else {
                        // --- GOOD ---
                        perfectStreak = 0
                        score += if (isRushTime) 60 else 50
                        goodCount++
                        feedbackText = goodPhrases.random()
                        effectColor = Color.Cyan
                        trackBorderColor = Color.Cyan
                        launch {
                            hitEffectScale.snapTo(1.2f)
                            hitEffectScale.animateTo(1f, tween(150))
                        }
                    }
                } else {
                    score = (score - 1).coerceAtLeast(0)
                    launch {
                        penaltyFlashAlpha.snapTo(0.3f)
                        penaltyFlashAlpha.animateTo(0f, tween(200))
                    }
                }
            }
        }
    }

    // --- 主要畫面佈局 ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        // (A) 難度選擇選單
        if (gameState == GameState.SELECTION) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎵 選擇挑戰難度", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 32.dp))

                Difficulty.values().forEach { difficulty ->
                    val isUnlocked = progressManager.isUnlocked(difficulty.label)
                    DifficultySelectionCard(
                        difficulty = difficulty,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                selectedDifficulty = difficulty
                                gameState = GameState.COUNTDOWN
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ 只有非訪客才顯示解鎖條件
                if (!isGuest) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 解鎖條件", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Text("• 簡單 > 8500分 解鎖普通", color = Color(0xFFFFD54F), fontSize = 15.sp)
                            Text("• 普通 > 14000分 解鎖困難", color = Color(0xFFFFD54F), fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    // ✅ 訪客顯示提示訊息
                    Surface(
                        color = Color(0xFF1E88E5).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👤 訪客模式", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Text("• 訪客模式無法紀錄分數", color = Color(0xFF90CAF9), fontSize = 15.sp)
                            Text("• 登入帳號才會保存遊戲分數", color = Color(0xFF90CAF9), fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                OutlinedButton(onClick = onNavigateBack, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))) {
                    Text("返回主選單", color = Color.White)
                }
            }
        }

        // (B) 倒數畫面
        if (gameState == GameState.COUNTDOWN) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Text(text = if (countdownValue > 0) "$countdownValue" else "GO!", fontSize = 120.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // (C) 遊戲進行畫面
        if (gameState == GameState.PLAYING) {

            if (penaltyFlashAlpha.value > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = penaltyFlashAlpha.value)))
            }

            Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("難度: ${selectedDifficulty.label}", color = selectedDifficulty.color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("分數: $score", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)

                val rushThreshold = when (selectedDifficulty) {
                    Difficulty.EASY -> 20
                    Difficulty.NORMAL -> 40
                    Difficulty.HARD -> 60
                }
                val isRushTime = combo >= rushThreshold

                if (isRushTime) {
                    Text(
                        text = "🔥 衝刺 Time: $combo",
                        style = MaterialTheme.typography.displayMedium,
                        color = goldColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.graphicsLayer { translationY = rushTimeOffsetY }
                    )
                } else {
                    Text(text = "Combo: $combo", style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2
                val judgeLineX = 250f
                val currentSpeed = selectedDifficulty.speed

                drawRect(Color.White.copy(alpha = 0.05f), Offset(0f, centerY - 80f), Size(size.width, 160f))
                drawLine(trackBorderColor, Offset(0f, centerY - 80f), Offset(size.width, centerY - 80f), 4f)
                drawLine(trackBorderColor, Offset(0f, centerY + 80f), Offset(size.width, centerY + 80f), 4f)

                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 60f, center = Offset(judgeLineX, centerY), style = Stroke(width = 4f))

                if (hitEffectScale.value > 1.0f) {
                    drawCircle(color = effectColor, radius = 60f * hitEffectScale.value, center = Offset(judgeLineX, centerY), style = Stroke(width = 8f))
                }

                currentNotes.forEach { note ->
                    if (!note.isHit) {
                        val noteX = judgeLineX + (note.targetTime - currentTime) * currentSpeed
                        if (noteX > -100 && noteX < size.width + 100) {
                            drawCircle(Color(0xFFFF5252), 40f, Offset(noteX, centerY))
                            drawCircle(Color.White, 40f, Offset(noteX, centerY), style = Stroke(width = 4f))
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.Center).padding(top = 220.dp)) {
                Text(feedbackText, style = MaterialTheme.typography.headlineLarge, color = effectColor, fontWeight = FontWeight.Bold)
            }

            val progress = (currentTime.toFloat() / selectedDifficulty.duration.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(8.dp),
                color = selectedDifficulty.color,
                trackColor = Color.Black.copy(alpha = 0.5f),
            )

            Button(
                onClick = {
                    soundManager.stopMusic()
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Text("退出")
            }
        }

        // (D) 結算畫面
        if (gameState == GameState.RESULT) {
            GameResultContent(
                score = score,
                maxScore = selectedDifficulty.maxScore,
                perfectCount = perfectCount,
                goodCount = goodCount,
                missCount = missCount,
                perfectColor = perfectColor,
                onRetry = { gameState = GameState.COUNTDOWN },
                onSelectDifficulty = { gameState = GameState.SELECTION },
                onExit = onNavigateBack
            )
        }
    }
}

// 結算畫面
@Composable
fun GameResultContent(
    score: Int,
    maxScore: Int,
    perfectCount: Int,
    goodCount: Int,
    missCount: Int,
    perfectColor: Color,
    onRetry: () -> Unit,
    onSelectDifficulty: () -> Unit,
    onExit: () -> Unit
) {
    val rank = calculateRank(score, maxScore)
    val rankColor = getRankColor(rank)

    val infiniteTransition = rememberInfiniteTransition(label = "rankBounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("遊戲結算", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.graphicsLayer {
                        translationY = offsetY
                        scaleX = scale
                        scaleY = scale
                    }
                ) {
                    Text(text = rank, fontSize = 100.sp, fontWeight = FontWeight.Black, color = rankColor, style = MaterialTheme.typography.displayLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("分數: $score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBubble("完美", perfectCount, perfectColor)
                    StatBubble("很好", goodCount, Color.Cyan)
                    StatBubble("失誤", missCount, Color(0xFFFF5252))
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重玩")
                    }
                    Button(onClick = onSelectDifficulty, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.List, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("選擇難度")
                    }
                    OutlinedButton(onClick = onExit, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)), border = BorderStroke(1.dp, Color(0xFFE53935)), shape = RoundedCornerShape(12.dp)) {
                        Text("離開")
                    }
                }
            }
        }
    }
}

@Composable
fun StatBubble(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp).background(color.copy(alpha = 0.2f), CircleShape).border(2.dp, color, CircleShape)
        ) {
            Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

fun calculateRank(score: Int, maxScore: Int): String {
    val percentage = if (maxScore > 0) score.toFloat() / maxScore.toFloat() else 0f
    return when {
        score >= maxScore -> "SSS"
        percentage >= 0.95f -> "SS"
        percentage >= 0.90f -> "S"
        percentage >= 0.80f -> "A"
        percentage >= 0.70f -> "B"
        else -> "C"
    }
}

fun getRankColor(rank: String): Color {
    return when (rank) {
        "SSS" -> Color(0xFFFFD700)
        "SS" -> Color(0xFFFFEB3B)
        "S" -> Color(0xFFFFA726)
        "A" -> Color(0xFF66BB6A)
        "B" -> Color(0xFF42A5F5)
        else -> Color(0xFFBDBDBD)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelectionCard(difficulty: Difficulty, isUnlocked: Boolean, onClick: () -> Unit) {
    val containerColor = if (isUnlocked) difficulty.color.copy(alpha = 0.9f) else Color.DarkGray.copy(alpha = 0.6f)
    val contentColor = if (isUnlocked) Color.White else Color.LightGray
    val borderColor = if (isUnlocked) difficulty.color else Color.Gray.copy(alpha = 0.5f)
    val icon: ImageVector = if (isUnlocked) Icons.Filled.MusicNote else Icons.Filled.Lock

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp).then(if (!isUnlocked) Modifier.blur(1.dp) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 8.dp else 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Text(text = if (isUnlocked) difficulty.label else "${difficulty.label} (未解鎖)", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}