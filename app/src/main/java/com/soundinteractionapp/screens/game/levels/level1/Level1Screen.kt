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
import androidx.compose.ui.draw.alpha
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.GameProgressManager
import com.soundinteractionapp.utils.GameInputManager
import com.soundinteractionapp.utils.GameScoreUtils
import com.soundinteractionapp.utils.VolumeKeys
import com.soundinteractionapp.screens.profile.models.AchievementManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

// --- 狀態與難度定義 ---
enum class GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED, RESULT }

enum class Difficulty(
    val label: String,
    val speed: Float,
    val color: Color,
    val scoreId: Int,
    val musicResId: Int,
    val duration: Long,
    val chartData: List<Note>,
    val description: String,
    val bgResId: Int,
    val coverResId: Int,
    val previewStartTime: Int,
    val rushThreshold: Int
) {
    EASY(
        "簡單模式", 0.6f, Color(0xFF4CAF50), 11, R.raw.canon, 97000L,
        Level1Charts.LEVEL1_NORMAL_CHART,
        "卡農:適合初學者練習節奏感",
        R.drawable.bg_level1_easy, R.drawable.bg_level1_easy, 30000, 20
    ),
    NORMAL(
        "普通模式", 0.9f, Color(0xFF2196F3), 12, R.raw.fur_elise, 77000L,
        Level1Charts.LEVEL1_EASY_CHART,
        "獻給愛麗絲:挑戰更快的打擊反應",
        R.drawable.bg_level1_normal, R.drawable.bg_level1_normal, 15000, 40
    ),
    HARD(
        "困難模式", 1.2f, Color(0xFFE53935), 13, R.raw.rondo_alla_turca, 58000L,
        Level1Charts.LEVEL1_HARD_CHART,
        "土耳其進行曲:極致的手速考驗",
        R.drawable.bg_level1_hard, R.drawable.bg_level1_hard, 20000, 60
    );

    val maxScore: Int
        get() = Level1Charts.calculateMaxScore(chartData.size, rushThreshold)
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

    // 防抖與導航狀態
    var isNavigating by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    // 分數與統計
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var perfectCount by remember { mutableIntStateOf(0) }
    var goodCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }

    // 遊戲時間與反饋
    var feedbackText by remember { mutableStateOf("") }
    var countdownValue by remember { mutableIntStateOf(3) }
    var startTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    val currentNotes = remember { mutableStateListOf<Note>() }

    // 視覺特效
    var isCharacterStriking by remember { mutableStateOf(false) }
    var characterAnimJob: Job? by remember { mutableStateOf(null) }
    var currentEffectFrame by remember { mutableIntStateOf(-1) }
    var effectJob: Job? by remember { mutableStateOf(null) }
    var effectColor by remember { mutableStateOf(Color.White) }
    var trackBorderColor by remember { mutableStateOf(Color.White.copy(alpha = 0.5f)) }
    val penaltyFlashAlpha = remember { Animatable(0f) }

    // Rush 動畫
    val rushAnim = rememberInfiniteTransition(label = "rushAnim")
    val rushOffsetY by rushAnim.animateFloat(
        0f, -8.dp.value,
        infiniteRepeatable(tween(400, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "rush"
    )

    // 音效管理
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var optionsSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previousSelectedIndex by remember { mutableIntStateOf(0) }

    // 資源載入
    val charIdle = ImageBitmap.imageResource(id = R.drawable.character_1)
    val charHit = ImageBitmap.imageResource(id = R.drawable.character_2)
    val dogFrames = listOf(
        ImageBitmap.imageResource(id = R.drawable.prairie_dog1),
        ImageBitmap.imageResource(id = R.drawable.prairie_dog2),
        ImageBitmap.imageResource(id = R.drawable.prairie_dog3)
    )
    val dogHit = ImageBitmap.imageResource(id = R.drawable.prairie_dog4)
    val hitEffectBitmaps = listOf(
        R.drawable.hit_feedback_1, R.drawable.hit_feedback_2, R.drawable.hit_feedback_3,
        R.drawable.hit_feedback_4, R.drawable.hit_feedback_5, R.drawable.hit_feedback_6
    ).map { ImageBitmap.imageResource(context.resources, it) }

    val listState = rememberLazyListState()
    val perfectPhrases = remember { listOf("太棒了!", "太厲害了吧", "是個高手") }
    val goodPhrases = remember { listOf("差一點呀", "很接近了!", "你做得到的!") }

    val stopPreview = {
        previewPlayer?.stop()
        previewPlayer?.release()
        previewPlayer = null
    }

    // 選單切換邏輯
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
            selectedDifficulty = Difficulty.entries[selectedIndex]
            stopPreview()
            delay(400)
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

    // 軌道閃爍邏輯
    LaunchedEffect(trackBorderColor) {
        if (trackBorderColor != Color.White.copy(alpha = 0.5f)) {
            delay(200)
            trackBorderColor = Color.White.copy(alpha = 0.5f)
        }
    }

    // 遊戲流程控管
    LaunchedEffect(gameState) {
        onGameStateChanged(gameState == GameState.PLAYING)
        if (gameState != GameState.SELECTION) stopPreview()

        if (gameState == GameState.COUNTDOWN) {
            score = 0
            combo = 0
            maxCombo = 0
            perfectCount = 0
            goodCount = 0
            missCount = 0
            currentNotes.clear()
            currentNotes.addAll(selectedDifficulty.chartData.map { it.copy(isHit = false) })
            countdownValue = 3
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            gameState = GameState.PLAYING
            startTime = System.currentTimeMillis()
            soundManager.playGameMusic(
                selectedDifficulty.musicResId,
                when(selectedDifficulty) {
                    Difficulty.EASY -> VolumeKeys.LEVEL1_EASY
                    Difficulty.NORMAL -> VolumeKeys.LEVEL1_MEDIUM
                    Difficulty.HARD -> VolumeKeys.LEVEL1_HARD
                }
            )
        }

        if (gameState == GameState.FINISHED) {
            soundManager.stopMusic()
            rankingViewModel.updateHighScore(selectedDifficulty.scoreId, score)
            if (!isGuest) {
                if (selectedDifficulty == Difficulty.EASY && score >= 7500) {
                    progressManager.unlockDifficulty(Difficulty.NORMAL.label)
                } else if (selectedDifficulty == Difficulty.NORMAL && score >= 7000) {
                    progressManager.unlockDifficulty(Difficulty.HARD.label)
                }
            }
            gameState = GameState.RESULT
        }
    }

    // 時間更新與 Miss 判定
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            while (isActive) {
                currentTime = System.currentTimeMillis() - startTime
                currentNotes.forEach { note ->
                    if (!note.isHit && (currentTime - note.targetTime > 250)) {
                        note.isHit = true
                        combo = 0
                        missCount++
                        feedbackText = "Miss!"
                        effectColor = Color.Red
                        trackBorderColor = Color.Red
                    }
                }
                if (currentTime > selectedDifficulty.duration + 2000L) {
                    gameState = GameState.FINISHED
                }
                withFrameMillis { }
            }
        }
    }

    // 打擊監聽
    LaunchedEffect(Unit) {
        GameInputManager.keyEvents.collectLatest {
            if (gameState == GameState.PLAYING) {
                characterAnimJob?.cancel()
                characterAnimJob = launch {
                    isCharacterStriking = true
                    delay(100)
                    isCharacterStriking = false
                }
                launch {
                    soundManager.playGameSFX("hit_music", VolumeKeys.LEVEL1_HIT)
                }

                val judgeRadius = 120f
                val targetNote = currentNotes.firstOrNull { note ->
                    !note.isHit && abs((note.targetTime - currentTime) * selectedDifficulty.speed) <= judgeRadius
                }

                if (targetNote != null) {
                    val offset = abs((targetNote.targetTime - currentTime) * selectedDifficulty.speed) / judgeRadius
                    targetNote.isHit = true
                    val isRush = combo >= selectedDifficulty.rushThreshold

                    if (offset <= 0.65f) {
                        effectJob?.cancel()
                        effectJob = launch {
                            currentEffectFrame = 0
                            repeat(6) {
                                delay(30)
                                currentEffectFrame++
                            }
                            currentEffectFrame = -1
                        }

                        if (offset <= 0.35f) {
                            combo++
                            score += if (isRush) 150 else 100
                            perfectCount++
                            feedbackText = perfectPhrases.random()
                            effectColor = if (isRush) Color(0xFFFFD700) else Color(0xFF00E676)
                        } else {
                            combo++
                            score += if (isRush) 60 else 50
                            goodCount++
                            feedbackText = goodPhrases.random()
                            effectColor = Color.Cyan
                        }
                        if (combo > maxCombo) maxCombo = combo
                        trackBorderColor = effectColor
                    } else {
                        combo = 0
                        missCount++
                        score = (score - 10).coerceAtLeast(0)
                        feedbackText = "Miss!"
                        trackBorderColor = Color.Red
                        launch {
                            penaltyFlashAlpha.snapTo(0.3f)
                            penaltyFlashAlpha.animateTo(0f, tween(100))
                        }
                    }
                } else {
                    feedbackText = "揮空了"
                    score = (score - 1).coerceAtLeast(0)
                    launch {
                        penaltyFlashAlpha.snapTo(0.3f)
                        penaltyFlashAlpha.animateTo(0f, tween(200))
                    }
                }
            }
        }
    }

    // 生命週期管理
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, gameState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (gameState == GameState.SELECTION) {
                        previewPlayer?.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (gameState == GameState.SELECTION && previewPlayer != null) {
                        previewPlayer?.start()
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

    // UI 渲染
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A2E))) {
        Image(
            painter = painterResource(selectedDifficulty.bgResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(if (gameState == GameState.SELECTION) 25.dp else 5.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.35f
        )

        if (gameState == GameState.SELECTION) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(Difficulty.entries.toTypedArray()) { index, diff ->
                            val isLocked = if (diff == Difficulty.EASY) false
                            else !progressManager.isUnlocked(diff.label)
                            Level1DifficultyCard(diff, index == selectedIndex, isLocked) {
                                coroutineScope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                    }
                }
                Box(modifier = Modifier.weight(0.55f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    val isLocked = if (selectedDifficulty == Difficulty.EASY) false
                    else !progressManager.isUnlocked(selectedDifficulty.label)
                    Level1TrapezoidDisplay(
                        selectedDifficulty,
                        isLocked,
                        if(selectedDifficulty == Difficulty.NORMAL) "需在簡單模式獲 7500 分"
                        else "需在普通模式獲 7000 分"
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isNavigating) return@IconButton
                    isNavigating = true
                    stopPreview()
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Level1ActionButtons(
                onStart = { gameState = GameState.COUNTDOWN },
                isEnabled = (selectedDifficulty == Difficulty.EASY || progressManager.isUnlocked(selectedDifficulty.label)),
                themeColor = selectedDifficulty.color,
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp).fillMaxWidth(0.42f)
            )
        }

        if (gameState == GameState.COUNTDOWN) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
                Text(
                    text = if (countdownValue > 0) "$countdownValue" else "GO!",
                    fontSize = 110.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (gameState == GameState.PLAYING) {
            if (penaltyFlashAlpha.value > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = penaltyFlashAlpha.value)))
            }

            Box(Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "分數: $score",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    val isRush = combo >= selectedDifficulty.rushThreshold
                    if (!isRush) {
                        Text(
                            "COMBO: $combo",
                            fontSize = 32.sp,
                            color = Color.White.copy(0.85f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (isRush) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔥 衝刺 Time: $combo",
                            fontSize = 38.sp,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.alpha(0.7f).graphicsLayer { translationY = rushOffsetY }
                        )
                    }
                }

                Text(
                    "難度: ${selectedDifficulty.label}",
                    color = selectedDifficulty.color,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 65.dp)
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val adjY = (size.height / 2) + 130f
                val judgeLineX = 280f
                val speed = selectedDifficulty.speed

                drawLine(trackBorderColor, Offset(0f, adjY - 110f), Offset(size.width, adjY - 110f), 4f)
                drawLine(trackBorderColor, Offset(0f, adjY + 110f), Offset(size.width, adjY + 110f), 4f)
                drawLine(Color.White.copy(0.8f), Offset(judgeLineX, adjY - 160f), Offset(judgeLineX, adjY + 160f), 6f)

                val charImg = if (isCharacterStriking) charHit else charIdle
                val cW = charImg.width * 0.25f
                val cH = charImg.height * 0.25f
                drawImage(
                    charImg,
                    dstOffset = IntOffset((judgeLineX - cW * 0.7f - 70f).toInt(), (adjY - cH / 2 - 50f).toInt()),
                    dstSize = IntSize(cW.toInt(), cH.toInt())
                )

                currentNotes.forEach { note ->
                    val noteX = judgeLineX + (note.targetTime - currentTime) * speed
                    if (noteX in -150f..size.width + 150f) {
                        val dogImg = if (note.isHit) dogHit else dogFrames[((currentTime / 100) % 3).toInt()]
                        drawContext.canvas.save()
                        drawContext.canvas.translate(noteX, adjY)
                        drawContext.canvas.scale(-1f, 1f)
                        val dW = (dogImg.width * 0.22f).toInt()
                        val dH = (dogImg.height * 0.22f).toInt()
                        drawImage(dogImg, dstOffset = IntOffset(-dW/2, -dH/2), dstSize = IntSize(dW, dH))
                        drawContext.canvas.restore()

                        if (!note.isHit) {
                            drawCircle(Color.White, 8f, Offset(noteX, adjY))
                        }
                    }
                }

                if (currentEffectFrame in 0..5) {
                    val eff = hitEffectBitmaps[currentEffectFrame]
                    drawImage(
                        eff,
                        dstOffset = IntOffset((judgeLineX - eff.width).toInt(), (adjY - eff.height).toInt()),
                        dstSize = IntSize(eff.width * 2, eff.height * 2)
                    )
                }
            }

            Text(
                text = feedbackText,
                style = MaterialTheme.typography.headlineLarge,
                color = effectColor,
                modifier = Modifier.align(Alignment.Center).padding(top = 250.dp)
            )

            val progress = (currentTime.toFloat() / selectedDifficulty.duration.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(8.dp),
                color = selectedDifficulty.color,
                trackColor = Color.Black.copy(0.5f)
            )
        }

        if (gameState == GameState.RESULT) {
            GameResultContent(
                score, selectedDifficulty.maxScore, perfectCount, goodCount, missCount, maxCombo,
                { gameState = GameState.COUNTDOWN; countdownValue = 3 },
                { gameState = GameState.SELECTION },
                {
                    if(!isNavigating){
                        isNavigating = true
                        soundManager.playSFX("cancel")
                        onNavigateBack()
                    }
                },
                isNavigating,
                rankingViewModel
            )
        }
    }
}

// --- 輔助組件 ---

@Composable
fun GameResultContent(
    score: Int,
    maxScore: Int,
    perfectCount: Int,
    goodCount: Int,
    missCount: Int,
    maxCombo: Int,
    onRetry: () -> Unit,
    onSelectDifficulty: () -> Unit,
    onExit: () -> Unit,
    isNavigating: Boolean,
    rankingViewModel: RankingViewModel = viewModel()
) {
    val rank = GameScoreUtils.calculateRank(score, maxScore)
    val rankColor = GameScoreUtils.getRankColor(rank)
    var newlyUnlockedAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val achievementManager = AchievementManager()
                val currentScores = rankingViewModel.scores.value
                val newlyUnlocked = achievementManager.checkAndUnlockAchievements(
                    scoreEntry = currentScores,
                    hasFeedback = false,
                    hasAvatar = false
                )
            } catch (e: Exception) {
                Log.e("Level1Result", "檢查成就失敗", e)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
            border = BorderStroke(2.dp, Color.White.copy(0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "關卡一 結算", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = rank,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    color = rankColor
                )
                Text(text = "最終得分: $score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBubble("完美", perfectCount, Color(0xFF00E676))
                    StatBubble("Max Combo", maxCombo, Color(0xFFFFD700))
                    StatBubble("失誤", missCount, Color(0xFFFF5252))
                }

                if (newlyUnlockedAchievements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    newlyUnlockedAchievements.forEach { achievementName ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("成就解鎖！", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    Text(achievementName, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("重玩")
                    }
                    Button(
                        onClick = onSelectDifficulty,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("難度")
                    }
                    OutlinedButton(
                        onClick = onExit,
                        border = BorderStroke(1.dp, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isNavigating,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
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
            modifier = Modifier.size(60.dp)
                .background(color.copy(0.2f), CircleShape)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Level1DifficultyCard(
    diff: Difficulty,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .padding(horizontal = 20.dp)
            .clickable { onSelect() }
            .zIndex(if (isSelected) 10f else 0f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color.Gray.copy(0.3f)
            else diff.color.copy(if (isSelected) 0.8f else 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = diff.label,
                    color = if (isLocked) Color.LightGray else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = diff.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(0.7f)
                )
            }
            if (isLocked) {
                Icon(Icons.Default.Lock, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun Level1TrapezoidDisplay(
    diff: Difficulty,
    isLocked: Boolean,
    hint: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
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
                painter = painterResource(diff.coverResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = if (isLocked) 0.3f else 1f,
                colorFilter = if (isLocked) {
                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) })
                } else null
            )
            if (isLocked) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(0.75f)),
                    Alignment.Center
                ) {
                    Text(
                        text = hint,
                        color = Color.White,
                        modifier = Modifier.padding(32.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun Level1ActionButtons(
    onStart: () -> Unit,
    isEnabled: Boolean,
    themeColor: Color,
    modifier: Modifier
) {
    Button(
        onClick = onStart,
        enabled = isEnabled,
        modifier = modifier.height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
            disabledContainerColor = Color.Gray.copy(0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = if (isEnabled) "開始挑戰" else "尚未解鎖",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = Color.White
        )
    }
}