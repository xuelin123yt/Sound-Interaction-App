package com.soundinteractionapp.screens.game.levels.level1

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.withFrameMillis
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.GameProgressManager
import com.soundinteractionapp.utils.GameInputManager
import com.soundinteractionapp.utils.GameScoreUtils
import com.soundinteractionapp.utils.VolumeKeys
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

// --- 狀態定義 ---
enum class GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED, RESULT }

// --- 難度配置 ---
enum class Difficulty(
    val label: String, val speed: Float, val color: Color, val scoreId: Int,
    val musicResId: Int, val duration: Long, val maxScore: Int,
    val chartData: List<Note>, val description: String,
    val bgResId: Int, val coverResId: Int,
    val previewStartTime: Int,
    val rushThreshold: Int
) {
    EASY("簡單模式", 0.6f, Color(0xFF4CAF50), 11, R.raw.canon, 97000L, 13400, Level1Charts.LEVEL1_NORMAL_CHART, "旋律優美，適合初學者練習節奏感", R.drawable.bg_level1_easy, R.drawable.bg_level1_easy, 30000, 20),
    NORMAL("普通模式", 0.9f, Color(0xFF2196F3), 12, R.raw.fur_elise, 77000L, 22900, Level1Charts.LEVEL1_EASY_CHART, "經典名曲，挑戰更快的打擊反應", R.drawable.bg_level1_normal, R.drawable.bg_level1_normal, 15000, 40),
    HARD("困難模式", 1.2f, Color(0xFFE53935), 13, R.raw.rondo_alla_turca, 58000L, 32850, Level1Charts.LEVEL1_HARD_CHART, "土耳其進行曲，極致的手速考驗", R.drawable.bg_level1_hard, R.drawable.bg_level1_hard, 20000, 60)
}

@Composable
fun Level1FollowBeatScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    rankingViewModel: RankingViewModel,
    onGameStateChanged: (isPlaying: Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val progressManager = remember { GameProgressManager(context) }
    val auth = FirebaseAuth.getInstance()
    val isGuest = auth.currentUser?.isAnonymous == true
    val coroutineScope = rememberCoroutineScope()

    // ✅ 防抖狀態
    var isNavigating by remember { mutableStateOf(false) }

    // 核心數據
    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var countdownValue by remember { mutableIntStateOf(3) }
    var startTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    val currentNotes = remember { mutableStateListOf<Note>() }

    // 統計數據
    var perfectCount by remember { mutableIntStateOf(0) }
    var goodCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }
    var perfectStreak by remember { mutableIntStateOf(0) }

    // 音效
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var optionsSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previousSelectedIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    // 視覺效果
    var isCharacterStriking by remember { mutableStateOf(false) }
    var characterAnimJob: Job? by remember { mutableStateOf(null) }
    var currentEffectFrame by remember { mutableIntStateOf(-1) }
    var effectJob: Job? by remember { mutableStateOf(null) }
    var effectColor by remember { mutableStateOf(Color.White) }
    var trackBorderColor by remember { mutableStateOf(Color.White.copy(alpha = 0.5f)) }
    val penaltyFlashAlpha = remember { Animatable(0f) }
    val perfectColor = Color(0xFF00E676)
    val goldColor = Color(0xFFFFD700)

    val rushAnim = rememberInfiniteTransition(label = "rushAnim")
    val rushOffsetY by rushAnim.animateFloat(0f, -10f, infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "rush")

    // 載入角色圖片
    val charIdle = ImageBitmap.imageResource(id = R.drawable.character_1)
    val charHit = ImageBitmap.imageResource(id = R.drawable.character_2)

    // ✅ 載入土撥鼠圖片
    val dog1 = ImageBitmap.imageResource(id = R.drawable.prairie_dog1)
    val dog2 = ImageBitmap.imageResource(id = R.drawable.prairie_dog2)
    val dog3 = ImageBitmap.imageResource(id = R.drawable.prairie_dog3)
    val dog4 = ImageBitmap.imageResource(id = R.drawable.prairie_dog4)
    val dogFrames = remember { listOf(dog1, dog2, dog3) }

    val hitEffectBitmaps = remember {
        listOf(R.drawable.hit_feedback_1, R.drawable.hit_feedback_2, R.drawable.hit_feedback_3,
            R.drawable.hit_feedback_4, R.drawable.hit_feedback_5, R.drawable.hit_feedback_6)
            .map { ImageBitmap.imageResource(context.resources, it) }
    }

    val perfectPhrases = remember { listOf("太棒了!", "太厲害了吧", "是個高手") }
    val goodPhrases = remember { listOf("差一點呀", "很接近了!", "你做得到的!") }

    val stopPreview = { previewPlayer?.stop(); previewPlayer?.release(); previewPlayer = null }

    // --- 1. 軌道閃爍邏輯 ---
    LaunchedEffect(trackBorderColor) {
        if (trackBorderColor != Color.White.copy(alpha = 0.5f)) {
            delay(200)
            trackBorderColor = Color.White.copy(alpha = 0.5f)
        }
    }

    // --- 2. 選單滾動偵測 ---
    val selectedIndex by remember { derivedStateOf {
        val info = listState.layoutInfo
        val center = info.viewportStartOffset + info.viewportSize.height / 2
        info.visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - center) }?.index ?: 0
    } }

    LaunchedEffect(selectedIndex) {
        if (gameState == GameState.SELECTION) {
            if (selectedIndex != previousSelectedIndex) {
                try {
                    optionsSoundPlayer?.release()
                    optionsSoundPlayer = MediaPlayer.create(context, R.raw.options4)
                    optionsSoundPlayer?.start()
                } catch (e: Exception) {}
            }
            selectedDifficulty = Difficulty.values()[selectedIndex]
            stopPreview(); delay(400)
            try {
                val p = MediaPlayer.create(context, selectedDifficulty.musicResId)
                p.isLooping = true
                p.seekTo(selectedDifficulty.previewStartTime)
                p.start()
                previewPlayer = p
            } catch (e: Exception) {}
            previousSelectedIndex = selectedIndex
        }
    }

    // ✅ 通知 MainActivity 遊戲狀態變化
    LaunchedEffect(gameState) {
        val isPlaying = gameState == GameState.PLAYING
        onGameStateChanged(isPlaying)
    }

    // --- 3. 遊戲流程控管 ---
    LaunchedEffect(gameState) {
        if (gameState != GameState.SELECTION) stopPreview()

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
            while (countdownValue > 0) { delay(1000); countdownValue-- }
            feedbackText = "GO!"
            gameState = GameState.PLAYING

            // ✅ 正確寫法：根據難度傳入對應的 VolumeKey
            val volumeKey = when(selectedDifficulty) {
                Difficulty.EASY -> VolumeKeys.LEVEL1_EASY
                Difficulty.NORMAL -> VolumeKeys.LEVEL1_MEDIUM
                Difficulty.HARD -> VolumeKeys.LEVEL1_HARD
            }
            soundManager.playGameMusic(selectedDifficulty.musicResId, volumeKey)
            startTime = System.currentTimeMillis()
        }

        if (gameState == GameState.FINISHED) {
            soundManager.stopMusic()
            rankingViewModel.updateHighScore(selectedDifficulty.scoreId, score)

            if (!isGuest) {
                if (selectedDifficulty == Difficulty.EASY && score >= 8500) {
                    progressManager.unlockDifficulty(Difficulty.NORMAL.label)
                } else if (selectedDifficulty == Difficulty.NORMAL && score >= 14000) {
                    progressManager.unlockDifficulty(Difficulty.HARD.label)
                }
            }
            gameState = GameState.RESULT
        }
    }

    // --- 4. 遊戲時間更新與 MISS 判定 ---
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

// --- 5. 核心打擊監聽 ---
    LaunchedEffect(Unit) {
        GameInputManager.keyEvents.collectLatest {
            if (gameState == GameState.PLAYING) {
                // ========== 第 1 步：立即觸發角色動畫 ==========
                characterAnimJob?.cancel()
                characterAnimJob = launch {
                    isCharacterStriking = true
                    delay(100)
                    isCharacterStriking = false
                }

                // ========== 第 2 步：非阻塞播放音效 ==========
                launch {
                    try {
                        soundManager.playGameSFX("hit_music", VolumeKeys.LEVEL1_HIT)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // ========== 第 3 步：判定邏輯 ==========
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

                    val isRushTime = combo >= selectedDifficulty.rushThreshold

                    if (offsetPercentage <= 0.65f) {
                        // ========== 立即觸發打擊特效動畫 ==========
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

    // ✅ 新增：監聽 App 生命週期，處理預覽音樂
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, gameState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (gameState == GameState.SELECTION) {
                        previewPlayer?.pause()
                        Log.d("Level1", "Preview paused")
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (gameState == GameState.SELECTION && previewPlayer != null) {
                        previewPlayer?.start()
                        Log.d("Level1", "Preview resumed")
                    }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    stopPreview()
                    optionsSoundPlayer?.release()
                    optionsSoundPlayer = null
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopPreview()
            optionsSoundPlayer?.release()
            optionsSoundPlayer = null
        }
    }

    // --- UI 渲染 ---
    Box(Modifier.fillMaxSize().background(Color(0xFF1A1A2E))) {
        // 背景
        Image(
            painter = painterResource(id = selectedDifficulty.bgResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(if (gameState == GameState.SELECTION) 25.dp else 5.dp),
            contentScale = ContentScale.Crop,
            alpha = if (gameState == GameState.SELECTION) 0.35f else 0.2f
        )

        if (gameState == GameState.SELECTION) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(0.45f).fillMaxHeight(), Alignment.Center) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(Difficulty.values()) { index, diff ->
                            val isLocked = if (diff == Difficulty.EASY) false else !progressManager.isUnlocked(diff.label)
                            Level1DifficultyCard(diff, index == selectedIndex, isLocked) {
                                coroutineScope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                    }
                }
                Box(Modifier.weight(0.55f).fillMaxHeight(), Alignment.Center) {
                    val isLocked = if (selectedDifficulty == Difficulty.EASY) false else !progressManager.isUnlocked(selectedDifficulty.label)
                    Level1TrapezoidDisplay(
                        selectedDifficulty,
                        isLocked,
                        if(selectedDifficulty == Difficulty.NORMAL) "需在簡單模式獲 8500 分" else "需在普通模式獲 14000 分"
                    )
                }
            }

            // ✅ 返回按鈕加入防抖
            IconButton(
                onClick = {
                    if (isNavigating) return@IconButton
                    isNavigating = true
                    stopPreview()
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                enabled = !isNavigating
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Level1ActionButtons(
                onStart = { gameState = GameState.COUNTDOWN },
                isEnabled = (if(selectedDifficulty == Difficulty.EASY) true else progressManager.isUnlocked(selectedDifficulty.label)),
                themeColor = selectedDifficulty.color,
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp).fillMaxWidth(0.42f)
            )
        }

        if (gameState == GameState.COUNTDOWN) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), Alignment.Center) {
                Text(
                    text = if (countdownValue > 0) "$countdownValue" else "GO!",
                    fontSize = 120.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (gameState == GameState.PLAYING) {
            if (penaltyFlashAlpha.value > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = penaltyFlashAlpha.value)))
            }

            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("難度: ${selectedDifficulty.label}", color = selectedDifficulty.color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("分數: $score", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)

                val isRushTime = combo >= selectedDifficulty.rushThreshold

                if (isRushTime) {
                    Text(
                        text = "🔥 衝刺 Time: $combo",
                        style = MaterialTheme.typography.displayMedium,
                        color = goldColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.graphicsLayer { translationY = rushOffsetY }
                    )
                } else {
                    Text(
                        text = "Combo: $combo",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Canvas(Modifier.fillMaxSize()) {
                val adjustedCenterY = (size.height / 2) + 130f
                val judgeLineX = 280f
                val currentSpeed = selectedDifficulty.speed
                val judgeLineHeight = 320f
                val trackHeight = 110f

                drawRect(Color.White.copy(alpha = 0.05f), Offset(0f, adjustedCenterY - trackHeight), Size(size.width, trackHeight * 2))
                drawLine(trackBorderColor, Offset(0f, adjustedCenterY - trackHeight), Offset(size.width, adjustedCenterY - trackHeight), 4f)
                drawLine(trackBorderColor, Offset(0f, adjustedCenterY + trackHeight), Offset(size.width, adjustedCenterY + trackHeight), 4f)

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

                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(judgeLineX, adjustedCenterY - (judgeLineHeight / 2)),
                    end = Offset(judgeLineX, adjustedCenterY + (judgeLineHeight / 2)),
                    strokeWidth = 6f
                )

                // ✅ 土撥鼠音符（取代紅色圓球）
                currentNotes.forEach { note ->
                    val noteX = judgeLineX + (note.targetTime - currentTime) * currentSpeed

                    if (noteX > -150 && noteX < size.width + 150) {
                        // 決定顯示哪張圖
                        val dogImage = if (note.isHit) {
                            dog4  // ✅ 被打中顯示受擊圖
                        } else {
                            // 快速切換 1→2→3 循環（每 100ms 換一張）
                            val frameIndex = ((currentTime / 100) % 3).toInt()
                            dogFrames[frameIndex]
                        }

                        // 設定土撥鼠大小
                        val dogScale = 0.22f
                        val dogW = dogImage.width * dogScale
                        val dogH = dogImage.height * dogScale

                        // ✅ 繪製翻轉的土撥鼠（面向左邊）
                        drawContext.canvas.save()
                        drawContext.canvas.translate(noteX, adjustedCenterY)
                        drawContext.canvas.scale(-1f, 1f)
                        drawImage(
                            image = dogImage,
                            dstOffset = IntOffset(
                                (-dogW / 2).toInt(),
                                (-dogH / 2).toInt()
                            ),
                            dstSize = IntSize(dogW.toInt(), dogH.toInt())
                        )
                        drawContext.canvas.restore()

                        // ✅ 標記土撥鼠中心點（白色點，被打中後不顯示）
                        if (!note.isHit) {
                            drawCircle(Color.White, 8f, Offset(noteX, adjustedCenterY))
                        }
                    }
                }

                if (currentEffectFrame in 0..5) {
                    val effectImage = hitEffectBitmaps[currentEffectFrame]
                    val effectScale = 2.0f
                    val effectW = effectImage.width * effectScale
                    val effectH = effectImage.height * effectScale
                    drawImage(
                        image = effectImage,
                        dstOffset = IntOffset((judgeLineX - effectW / 2).toInt(), (adjustedCenterY - effectH / 2).toInt()),
                        dstSize = IntSize(effectW.toInt(), effectH.toInt())
                    )
                }
            }

            Box(modifier = Modifier.align(Alignment.Center).padding(top = 250.dp)) {
                Text(feedbackText, style = MaterialTheme.typography.headlineLarge, color = effectColor, fontWeight = FontWeight.Bold)
            }

            val progress = (currentTime.toFloat() / selectedDifficulty.duration.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(8.dp),
                color = selectedDifficulty.color,
                trackColor = Color.Black.copy(alpha = 0.5f)
            )

            // ✅ 退出按鈕加入防抖
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    soundManager.stopMusic()
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.7f),
                    disabledContainerColor = Color.Red.copy(alpha = 0.5f)
                ),
                enabled = !isNavigating,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Text("退出")
            }
        }

        if (gameState == GameState.RESULT) {
            GameResultContent(
                score = score,
                maxScore = selectedDifficulty.maxScore,
                perfectCount = perfectCount,
                goodCount = goodCount,
                missCount = missCount,
                onRetry = { gameState = GameState.COUNTDOWN },
                onSelectDifficulty = { gameState = GameState.SELECTION },
                onExit = {
                    if (isNavigating) return@GameResultContent
                    isNavigating = true
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                isNavigating = isNavigating
            )
        }
    }
}

// --- 其餘組件 ---

@Composable
fun Level1DifficultyCard(diff: Difficulty, isSelected: Boolean, isLocked: Boolean, onSelect: () -> Unit) {
    val elev by animateDpAsState(if (isSelected) 16.dp else 4.dp, label = "")
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).zIndex(if (isSelected) 10f else 0f)) {
        Card(
            modifier = Modifier.fillMaxWidth().height(82.dp).clickable { onSelect() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = elev)
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier.fillMaxSize().background(
                        if (isLocked) Color.Gray.copy(0.25f) else diff.color.copy(if (isSelected) 0.65f else 0.35f),
                        RoundedCornerShape(20.dp)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = diff.label,
                            fontSize = if (isSelected) 22.sp else 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = diff.description,
                            fontSize = 12.sp,
                            color = Color.White.copy(0.7f),
                            maxLines = 1
                        )
                    }
                    if (isLocked) Icon(Icons.Default.Lock, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
fun Level1TrapezoidDisplay(diff: Difficulty, isLocked: Boolean, hint: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.55f)
                .offset(x = 45.dp)
                .drawWithContent {
                    val skew = size.height * 0.3f
                    val path = Path().apply {
                        moveTo(skew, 0f)
                        lineTo(size.width + skew, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    clipPath(path) { this@drawWithContent.drawContent() }
                    drawPath(path, Color.White.copy(0.5f), style = Stroke(6f))
                }
        ) {
            Image(
                painter = painterResource(id = diff.coverResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = if (isLocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.15f) }) else null
            )
            if (isLocked) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.75f)),
                    Alignment.Center
                ) {
                    Text(
                        text = hint,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Level1ActionButtons(onStart: () -> Unit, isEnabled: Boolean, themeColor: Color, modifier: Modifier) {
    Button(
        onClick = onStart,
        enabled = isEnabled,
        modifier = modifier.height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
            disabledContainerColor = Color.Gray.copy(0.35f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = if (isEnabled) "開始挑戰" else "尚未解鎖",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun GameResultContent(
    score: Int,
    maxScore: Int,
    perfectCount: Int,
    goodCount: Int,
    missCount: Int,
    onRetry: () -> Unit,
    onSelectDifficulty: () -> Unit,
    onExit: () -> Unit,
    isNavigating: Boolean
) {
    val rank = GameScoreUtils.calculateRank(score, maxScore)
    val rankColor = GameScoreUtils.getRankColor(rank)

    val infiniteTransition = rememberInfiniteTransition(label = "rankBounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
        Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
            border = BorderStroke(2.dp, Color.White.copy(0.1f)),
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
                    Text(
                        text = rank,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Black,
                        color = rankColor,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("分數: $score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBubble("完美", perfectCount, Color(0xFF00E676))
                    StatBubble("很好", goodCount, Color.Cyan)
                    StatBubble("失誤", missCount, Color(0xFFFF5252))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("重玩")
                    }
                    Button(
                        onClick = onSelectDifficulty,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.List, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("選擇難度")
                    }
                    OutlinedButton(
                        onClick = onExit,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935),
                            disabledContentColor = Color(0xFFE53935).copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE53935)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isNavigating
                    ) {
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
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(0.2f), CircleShape)
                .border(2.dp, color, CircleShape)
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}