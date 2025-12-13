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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.utils.GameInputManager
import com.soundinteractionapp.GameProgressManager
import kotlinx.coroutines.Job
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
    val auth = FirebaseAuth.getInstance()
    val isGuest = auth.currentUser?.isAnonymous == true

    // --- 1. 載入圖片資源 ---
    val charIdle = ImageBitmap.imageResource(id = R.drawable.character_1)
    val charHit = ImageBitmap.imageResource(id = R.drawable.character_2)

    val hitEffectsIds = remember {
        listOf(
            R.drawable.hit_feedback_1,
            R.drawable.hit_feedback_2,
            R.drawable.hit_feedback_3,
            R.drawable.hit_feedback_4,
            R.drawable.hit_feedback_5,
            R.drawable.hit_feedback_6
        )
    }
    val hitEffectBitmaps = hitEffectsIds.map { ImageBitmap.imageResource(id = it) }

    // --- 狀態管理 ---
    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var countdownValue by remember { mutableIntStateOf(3) }

    // 視覺狀態
    var isCharacterStriking by remember { mutableStateOf(false) }
    var characterAnimJob: Job? by remember { mutableStateOf(null) }

    var currentEffectFrame by remember { mutableIntStateOf(-1) }
    var effectJob: Job? by remember { mutableStateOf(null) }

    // 統計數據
    var perfectStreak by remember { mutableIntStateOf(0) }
    var perfectCount by remember { mutableIntStateOf(0) }
    var goodCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }

    // 視覺變數
    var trackBorderColor by remember { mutableStateOf(Color.White.copy(alpha = 0.5f)) }
    var effectColor by remember { mutableStateOf(Color.White) }
    val penaltyFlashAlpha = remember { Animatable(0f) }

    val perfectColor = Color(0xFF00E676)
    val goldColor = Color(0xFFFFD700)

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

    var startTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    val currentNotes = remember { mutableStateListOf<Note>() }

    val perfectPhrases = remember { listOf("太棒了!", "太厲害了吧", "是個高手") }
    val goodPhrases = remember { listOf("差一點呀", "很接近了!","你做得到的!") }

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
            if (!isGuest) {
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

    // --- 遊戲邏輯 ---
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            val gameDuration = selectedDifficulty.duration + 2000L
            while (isActive) {
                currentTime = System.currentTimeMillis() - startTime
                currentNotes.forEach { note ->
                    if (!note.isHit && (currentTime - note.targetTime > 250)) {
                        note.isHit = true
                        combo = 0
                        perfectStreak = 0
                        missCount++
                        feedbackText = "錯過了呀"
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

                // 1. 播放音效
                try {
                    soundManager.playSound(R.raw.hit_music)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. 播放揮棒動畫
                characterAnimJob?.cancel()
                characterAnimJob = launch {
                    isCharacterStriking = true
                    delay(100)
                    isCharacterStriking = false
                }

                // 3. 判定邏輯
                val baseJudgeRadius = 120f
                val targetNote = currentNotes.firstOrNull { note ->
                    if (note.isHit) return@firstOrNull false
                    val pixelDistance = abs((note.targetTime - currentTime) * selectedDifficulty.speed)
                    pixelDistance <= baseJudgeRadius
                }

                if (targetNote != null) {
                    val pixelDistance = abs((targetNote.targetTime - currentTime) * selectedDifficulty.speed)
                    val offsetPercentage = pixelDistance / baseJudgeRadius
                    targetNote.isHit = true

                    val rushThreshold = when (selectedDifficulty) {
                        Difficulty.EASY -> 20
                        Difficulty.NORMAL -> 40
                        Difficulty.HARD -> 60
                    }
                    val isRushTime = combo >= rushThreshold

                    if (offsetPercentage <= 0.65f) {
                        // 打中：特效
                        effectJob?.cancel()
                        effectJob = launch {
                            currentEffectFrame = 0
                            while (currentEffectFrame < 5) {
                                delay(30)
                                currentEffectFrame++
                            }
                            delay(30)
                            currentEffectFrame = -1
                        }

                        if (offsetPercentage <= 0.35f) {
                            combo++
                            perfectStreak++
                            score += if (isRushTime) 150 else 100
                            perfectCount++
                            feedbackText = perfectPhrases.random()
                            effectColor = if (isRushTime) goldColor else perfectColor
                            trackBorderColor = if (isRushTime) goldColor else perfectColor
                        } else {
                            combo++
                            perfectStreak = 0
                            score += if (isRushTime) 60 else 50
                            goodCount++
                            feedbackText = goodPhrases.random()
                            effectColor = Color.Cyan
                            trackBorderColor = Color.Cyan
                        }
                    } else {
                        // Miss (按太偏)
                        combo = 0
                        perfectStreak = 0
                        missCount++
                        score = (score - 10).coerceAtLeast(0)
                        feedbackText = "錯過了"
                        effectColor = Color.Gray
                        trackBorderColor = Color.Red
                        launch {
                            penaltyFlashAlpha.snapTo(0.3f)
                            penaltyFlashAlpha.animateTo(0f, tween(100))
                        }
                    }
                } else {
                    // 空揮
                    feedbackText = "揮空了呦"
                    effectColor = Color.Gray
                    score = (score - 1).coerceAtLeast(0)
                    launch {
                        penaltyFlashAlpha.snapTo(0.3f)
                        penaltyFlashAlpha.animateTo(0f, tween(200))
                    }
                }
            }
        }
    }

    // --- 畫面佈局 ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        // (A) 難度選擇
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

            // UI 往下移
            Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

            // --- Canvas 繪圖 ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 核心：所有物件往下移
                val adjustedCenterY = (size.height / 2) + 130f

                val judgeLineX = 280f
                val currentSpeed = selectedDifficulty.speed

                val noteRadius = 80f
                val judgeLineHeight = 320f
                val trackHeight = 110f

                // 1. 背景軌道
                drawRect(Color.White.copy(alpha = 0.05f), Offset(0f, adjustedCenterY - trackHeight), Size(size.width, trackHeight * 2))
                drawLine(trackBorderColor, Offset(0f, adjustedCenterY - trackHeight), Offset(size.width, adjustedCenterY - trackHeight), 4f)
                drawLine(trackBorderColor, Offset(0f, adjustedCenterY + trackHeight), Offset(size.width, adjustedCenterY + trackHeight), 4f)

                // 2. 繪製角色
                val charImage = if (isCharacterStriking) charHit else charIdle

                val charScale = 0.25f
                val charW = charImage.width * charScale
                val charH = charImage.height * charScale

                val charX = judgeLineX - (charW * 0.7f) - 70f
                val charY = adjustedCenterY - (charH / 2) - 50f

                drawImage(
                    image = charImage,
                    dstOffset = IntOffset(charX.toInt(), charY.toInt()),
                    dstSize = IntSize(charW.toInt(), charH.toInt())
                )

                // 3. 判定線
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(judgeLineX, adjustedCenterY - (judgeLineHeight / 2)),
                    end = Offset(judgeLineX, adjustedCenterY + (judgeLineHeight / 2)),
                    strokeWidth = 6f
                )

                // 4. 音符
                currentNotes.forEach { note ->
                    if (!note.isHit) {
                        val noteX = judgeLineX + (note.targetTime - currentTime) * currentSpeed
                        if (noteX > -150 && noteX < size.width + 150) {
                            drawCircle(Color(0xFFFF5252), noteRadius, Offset(noteX, adjustedCenterY))
                            drawCircle(Color.White, noteRadius, Offset(noteX, adjustedCenterY), style = Stroke(width = 6f))
                            drawCircle(Color.White, 8f, Offset(noteX, adjustedCenterY))
                        }
                    }
                }

                // 5. 特效 (修正處：只使用一個 correct 的 dstOffset)
                if (currentEffectFrame in 0..5) {
                    val effectImage = hitEffectBitmaps[currentEffectFrame]
                    val effectScale = 2.0f
                    val effectW = effectImage.width * effectScale
                    val effectH = effectImage.height * effectScale

                    drawImage(
                        image = effectImage,
                        dstOffset = IntOffset(
                            (judgeLineX - effectW / 2).toInt(),
                            (adjustedCenterY - effectH / 2).toInt()
                        ),
                        dstSize = IntSize(effectW.toInt(), effectH.toInt())
                    )
                }
            }

            // 文字下移
            Box(modifier = Modifier.align(Alignment.Center).padding(top = 250.dp)) {
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

        // (D) 結算
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

// ... 下方保持 GameResultContent 與 DifficultySelectionCard (省略以節省空間) ...
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