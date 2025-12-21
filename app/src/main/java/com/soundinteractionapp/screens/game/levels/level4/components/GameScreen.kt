package com.soundinteractionapp.screens.game.levels.level4.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.BeatmapRegistry
import com.soundinteractionapp.screens.game.levels.level4.logic.*
import com.soundinteractionapp.screens.game.levels.level4.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.soundinteractionapp.screens.game.levels.level4.HitResult
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import kotlin.math.abs

/**
 * 遊戲主畫面 - 支援 AUTO 模式
 */
@Composable
fun GameScreen(
    beatmap: Beatmap,
    onBack: () -> Unit,
    onNextLevel: () -> Unit,
    soundManager: SoundManager,
    isAutoMode: Boolean = false  // ✅ 新增：AUTO 模式參數
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        AudioOffsetManager.init(context)
    }

    var gameState by remember { mutableStateOf(GameState.READY) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var perfectCount by remember { mutableIntStateOf(0) }
    var greatCount by remember { mutableIntStateOf(0) }
    var goodCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }
    var countdownValue by remember { mutableIntStateOf(0) }
    var isCountingDown by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicDuration by remember { mutableLongStateOf(0L) }
    var pausedPosition by remember { mutableIntStateOf(0) }
    var audioOffset by remember { mutableIntStateOf(0) }
    var activeNotes by remember { mutableStateOf<List<ActiveNote>>(emptyList()) }
    var hitEffects by remember { mutableStateOf<List<HitEffect>>(emptyList()) }
    var missEffects by remember { mutableStateOf<List<HitEffect>>(emptyList()) }
    var starEffects by remember { mutableStateOf<List<StarEffect>>(emptyList()) }  // ✅ 星星特效
    var judgementText by remember { mutableStateOf<String?>(null) }
    var judgementColor by remember { mutableStateOf(Color.White) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var noteCounter by remember { mutableIntStateOf(0) }
    var isTouching by remember { mutableStateOf(false) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var screenWidth by remember { mutableStateOf(1080f) }
    var screenHeight by remember { mutableStateOf(1920f) }

    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            audioOffset = AudioOffsetManager.getCurrentOffset()
        }
    }

    fun playHitSound() {
        soundManager.playOsuHit()
    }

    fun playMissSound() {
        soundManager.playOsuMiss()
    }

    // ✅ 輔助函數：計算縮放和偏移
    fun calculateScaleAndOffset(): Pair<Pair<Float, Float>, Pair<Float, Float>> {
        val PLAY_FIELD_SCALE = 0.85f
        val ORIGINAL_WIDTH = 512f
        val ORIGINAL_HEIGHT = 384f

        val scaledWidth = ORIGINAL_WIDTH * PLAY_FIELD_SCALE
        val scaledHeight = ORIGINAL_HEIGHT * PLAY_FIELD_SCALE

        val offsetX = (ORIGINAL_WIDTH - scaledWidth) / 2f
        val offsetY = (ORIGINAL_HEIGHT - scaledHeight) / 2f

        val scaleX = (screenWidth / ORIGINAL_WIDTH) * PLAY_FIELD_SCALE
        val scaleY = (screenHeight / ORIGINAL_HEIGHT) * PLAY_FIELD_SCALE

        val screenOffsetX = offsetX * (screenWidth / ORIGINAL_WIDTH)
        val screenOffsetY = offsetY * (screenHeight / ORIGINAL_HEIGHT)

        return Pair(scaleX to scaleY, screenOffsetX to screenOffsetY)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (gameState == GameState.PLAYING) {  // ✅ 移除 isAutoMode 限制
                        gameState = GameState.PAUSED
                        pausedPosition = mediaPlayer?.currentPosition ?: 0
                        mediaPlayer?.pause()
                        showPauseDialog = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    audioOffset = AudioOffsetManager.getCurrentOffset()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // ✅ AUTO 模式自動點擊邏輯
    LaunchedEffect(currentTime, isAutoMode, activeNotes, gameState, isCountingDown) {
        // ✅ 只在 PLAYING 狀態且不在倒數計時時執行
        if (!isAutoMode || gameState != GameState.PLAYING || isCountingDown) return@LaunchedEffect

        activeNotes.forEach { activeNote ->
            if (activeNote.isHit || activeNote.isMissed) return@forEach

            val timeDiff = currentTime - activeNote.note.time

            // ✅ 在完美時機自動點擊（放寬到 30ms 誤差範圍，確保不會漏掉音符）
            if (timeDiff in -15L..15L) {
                when (activeNote.note.type) {
                    NoteType.CIRCLE -> {
                        playHitSound()
                        activeNote.isHit = true
                        perfectCount++
                        combo++
                        score += Level4Scoring.calculateScore(HitResult.PERFECT, combo)

                        // ✅ 產生星星特效
                        val (scale, screenOffset) = calculateScaleAndOffset()
                        val (scaleX, scaleY) = scale
                        val (screenOffsetX, screenOffsetY) = screenOffset
                        val starPos = Offset(
                            activeNote.note.x * scaleX + screenOffsetX,
                            activeNote.note.y * scaleY + screenOffsetY
                        )
                        starEffects = starEffects + StarEffect(starPos, System.currentTimeMillis())

                        hitEffects = hitEffects + HitEffect(
                            Offset(activeNote.note.x, activeNote.note.y),
                            System.currentTimeMillis(),
                            HitResult.PERFECT
                        )
                    }
                    NoteType.SLIDER -> {
                        if (!activeNote.isHit) {
                            playHitSound()
                            activeNote.isHit = true
                            activeNote.sliderStartTime = currentTime
                            activeNote.sliderProgress = 0f
                            activeNote.followTime = 0L

                            // ✅ 產生星星特效
                            val (scale, screenOffset) = calculateScaleAndOffset()
                            val (scaleX, scaleY) = scale
                            val (screenOffsetX, screenOffsetY) = screenOffset
                            val starPos = Offset(
                                activeNote.note.x * scaleX + screenOffsetX,
                                activeNote.note.y * scaleY + screenOffsetY
                            )
                            starEffects = starEffects + StarEffect(starPos, System.currentTimeMillis())
                        }
                    }
                    else -> {}
                }

                if (combo > maxCombo) maxCombo = combo
            }
        }

        // ✅ AUTO 模式自動跟隨滑條
        if (isAutoMode && !isCountingDown) {  // ✅ 倒數期間不更新滑條
            activeNotes.filter { it.note.type == NoteType.SLIDER && it.isHit && !it.sliderCompleted }.forEach { activeNote ->
                val elapsed = currentTime - activeNote.sliderStartTime
                val duration = activeNote.note.endTime - activeNote.note.time

                if (elapsed >= 0 && elapsed <= duration) {
                    val newProgress = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    activeNote.sliderProgress = newProgress
                    activeNote.sliderFollowing = true
                    activeNote.followTime = elapsed

                    // ✅ 折返音效由 GameLoopHelper 統一處理，這裡不需要重複檢測

                } else if (elapsed > duration && !activeNote.sliderCompleted) {
                    // ✅ 滑條結束
                    activeNote.sliderCompleted = true
                    activeNote.sliderCompleteTime = currentTime
                    playHitSound()

                    perfectCount++
                    combo++
                    score += Level4Scoring.calculateScore(HitResult.PERFECT, combo)

                    val endPos = NoteHandler.getSliderEndPosition(activeNote.note)
                    hitEffects = hitEffects + HitEffect(endPos, System.currentTimeMillis(), HitResult.PERFECT)

                    // ✅ 產生星星特效
                    val (scale, screenOffset) = calculateScaleAndOffset()
                    val (scaleX, scaleY) = scale
                    val (screenOffsetX, screenOffsetY) = screenOffset
                    val starPos = Offset(
                        endPos.x * scaleX + screenOffsetX,
                        endPos.y * scaleY + screenOffsetY
                    )
                    starEffects = starEffects + StarEffect(starPos, System.currentTimeMillis())

                    if (combo > maxCombo) maxCombo = combo
                }
            }
        }

        // ✅ 清理過期的星星特效（600ms 生命週期）
        starEffects = starEffects.filter {
            System.currentTimeMillis() - it.startTime < 600
        }
    }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            countdownValue = 3
            delay(1000)
            countdownValue = 2
            delay(1000)
            countdownValue = 1
            delay(1000)
            countdownValue = 0
            isCountingDown = false
            gameState = GameState.PLAYING

            // ✅ 恢復遊戲前重新計算 audioOffset
            audioOffset = AudioOffsetManager.getCurrentOffset()

            // ✅ 恢復遊戲時重新設定音量
            val songVolume = soundManager.getLevel4SongVolume(beatmap.id)
            val finalVolume = if (soundManager.isMasterMuted) {
                0f
            } else {
                soundManager.masterVolume * songVolume
            }
            mediaPlayer?.setVolume(finalVolume, finalVolume)

            android.util.Log.d("GameScreen", "Resume - beatmapId=${beatmap.id}, songVolume=$songVolume, finalVolume=$finalVolume")

            mediaPlayer?.seekTo(pausedPosition)
            mediaPlayer?.start()
        }
    }

    LaunchedEffect(beatmap.id) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, beatmap.audioResId)

            // ✅ 設定音量（試聽和遊戲共用同一個音量）
            val songVolume = soundManager.getLevel4SongVolume(beatmap.id)
            val finalVolume = if (soundManager.isMasterMuted) {
                0f
            } else {
                soundManager.masterVolume * songVolume
            }
            mediaPlayer?.setVolume(finalVolume, finalVolume)

            mediaPlayer?.isLooping = false
            musicDuration = mediaPlayer?.duration?.toLong() ?: 0L
            mediaPlayer?.setOnCompletionListener {
                gameState = GameState.FINISHED
            }

            android.util.Log.d("GameScreen", "Music loaded - beatmapId=${beatmap.id}, songVolume=$songVolume, finalVolume=$finalVolume")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            if (pausedPosition == 0) {
                noteCounter = 0
                activeNotes = emptyList()
            }
            mediaPlayer?.start()
        }

        while (isActive && gameState == GameState.PLAYING && !isCountingDown) {  // ✅ 倒數期間暫停遊戲循環
            val rawTime = (mediaPlayer?.currentPosition ?: 0).toLong()
            currentTime = rawTime + audioOffset

            val loopResult = GameLoopHelper.updateGameLoop(
                currentTime = currentTime,
                activeNotes = activeNotes,
                noteCounter = noteCounter,
                isTouching = if (isAutoMode) false else isTouching,  // ✅ AUTO 模式不接受觸控
                touchPosition = if (isAutoMode) null else touchPosition,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                beatmap = beatmap,
                onScoreUpdate = { result, comboValue ->
                    if (!isAutoMode) {  // ✅ 只在非 AUTO 模式計分
                        when (result) {
                            HitResult.PERFECT -> perfectCount++
                            HitResult.GREAT -> greatCount++
                            HitResult.GOOD -> goodCount++
                            HitResult.MISS -> {
                                missCount++
                                combo = 0
                            }
                        }
                        if (result != HitResult.MISS) {
                            combo = comboValue
                            score += Level4Scoring.calculateScore(result, comboValue)
                        }
                        if (combo > maxCombo) maxCombo = combo
                    }
                },
                onMiss = { notePosition ->
                    if (!isAutoMode) {  // ✅ AUTO 模式不會 MISS
                        missCount++
                        combo = 0
                        playMissSound()
                        missEffects = missEffects + HitEffect(
                            notePosition,
                            System.currentTimeMillis(),
                            HitResult.MISS
                        )
                    }
                },
                onSliderReverse = {
                    // ✅ AUTO 模式和手動模式都要播放折返音效
                    playHitSound()
                }
            )

            activeNotes = loopResult.activeNotes
            noteCounter = loopResult.noteCounter

            hitEffects = hitEffects.filter {
                System.currentTimeMillis() - it.startTime < 500
            }

            missEffects = missEffects.filter {
                System.currentTimeMillis() - it.startTime < 500
            }

            delay(16)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                screenWidth = constraints.maxWidth.toFloat()
                screenHeight = constraints.maxHeight.toFloat()

                GameCanvas(
                    activeNotes = activeNotes,
                    hitEffects = hitEffects,
                    missEffects = missEffects,
                    starEffects = if (isAutoMode) starEffects else emptyList(),  // ✅ 傳遞星星特效
                    currentTime = currentTime,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    beatmap = beatmap,
                    onTouchEvent = if (isAutoMode) { _, _, _ -> } else { offset, isStart, isEnd ->  // ✅ AUTO 模式禁用觸控
                        if (gameState != GameState.PLAYING) return@GameCanvas

                        if (isStart) {
                            isTouching = true
                            touchPosition = offset

                            NoteHandler.handleTap(
                                offset = offset,
                                activeNotes = activeNotes,
                                currentTime = currentTime,
                                screenWidth = screenWidth,
                                screenHeight = screenHeight,
                                beatmap = beatmap,
                                onHit = { activeNote, hitResult ->
                                    if (hitResult == HitResult.MISS) {
                                        playMissSound()
                                    } else {
                                        playHitSound()
                                    }

                                    when (hitResult) {
                                        HitResult.PERFECT -> {
                                            perfectCount++
                                            combo++
                                            judgementText = "PERFECT"
                                            judgementColor = Color(0xFFFFD700)
                                        }
                                        HitResult.GREAT -> {
                                            greatCount++
                                            combo++
                                            judgementText = "GREAT"
                                            judgementColor = Color(0xFF00FF00)
                                        }
                                        HitResult.GOOD -> {
                                            goodCount++
                                            combo++
                                            judgementText = "GOOD"
                                            judgementColor = Color(0xFF87CEEB)
                                        }
                                        HitResult.MISS -> {
                                            missCount++
                                            combo = 0
                                            judgementText = "MISS"
                                            judgementColor = Color(0xFFFF0000)
                                        }
                                    }

                                    if (combo > maxCombo) maxCombo = combo
                                    score += Level4Scoring.calculateScore(hitResult, combo)
                                    activeNote.isHit = true

                                    hitEffects = hitEffects + HitEffect(
                                        Offset(activeNote.note.x, activeNote.note.y),
                                        System.currentTimeMillis(),
                                        hitResult
                                    )
                                }
                            )

                            NoteHandler.handleSliderStart(
                                offset = offset,
                                activeNotes = activeNotes,
                                currentTime = currentTime,
                                screenWidth = screenWidth,
                                screenHeight = screenHeight,
                                beatmap = beatmap,
                                onHit = { activeNote, _ ->
                                    playHitSound()
                                    activeNote.isHit = true
                                    activeNote.sliderStartTime = currentTime
                                    activeNote.sliderProgress = 0f
                                    activeNote.followTime = 0L
                                }
                            )
                        }

                        if (!isEnd && isTouching) {
                            touchPosition = offset
                        }

                        if (isEnd) {
                            activeNotes.filter {
                                it.note.type == NoteType.SLIDER &&
                                        it.isHit &&
                                        !it.sliderCompleted
                            }.forEach { activeNote ->
                                NoteHandler.handleSliderRelease(
                                    activeNote = activeNote,
                                    currentTime = currentTime,
                                    onComplete = { hitResult, endPosition ->
                                        if (hitResult == HitResult.MISS) {
                                            playMissSound()
                                        } else {
                                            playHitSound()
                                        }

                                        when (hitResult) {
                                            HitResult.PERFECT -> {
                                                perfectCount++
                                                combo++
                                                judgementText = "PERFECT"
                                                judgementColor = Color(0xFFFFD700)
                                            }
                                            HitResult.GREAT -> {
                                                greatCount++
                                                combo++
                                                judgementText = "GREAT"
                                                judgementColor = Color(0xFF00FF00)
                                            }
                                            HitResult.GOOD -> {
                                                goodCount++
                                                combo++
                                                judgementText = "GOOD"
                                                judgementColor = Color(0xFF87CEEB)
                                            }
                                            HitResult.MISS -> {
                                                missCount++
                                                combo = 0
                                                judgementText = "MISS"
                                                judgementColor = Color(0xFFFF0000)
                                            }
                                        }

                                        if (combo > maxCombo) maxCombo = combo
                                        score += Level4Scoring.calculateScore(hitResult, combo)

                                        hitEffects = hitEffects + HitEffect(
                                            endPosition,
                                            System.currentTimeMillis(),
                                            hitResult
                                        )
                                    }
                                )
                            }

                            isTouching = false
                            touchPosition = null
                        }
                    }
                )
            }
        }

        if (isCountingDown && countdownValue > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownValue.toString(),
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
        }

        if (gameState == GameState.PLAYING) {
            GameUI(
                score = score,
                combo = combo,
                gameState = gameState,
                judgementText = judgementText,
                judgementColor = judgementColor,
                currentTime = currentTime,
                totalDuration = musicDuration,
                onPause = {
                    // ✅ AUTO 模式也可以暫停
                    gameState = GameState.PAUSED
                    pausedPosition = mediaPlayer?.currentPosition ?: 0
                    mediaPlayer?.pause()
                    showPauseDialog = true
                },
                onJudgementDismiss = { judgementText = null }
            )
        }

        if (gameState == GameState.READY) {
            ReadyScreen(
                beatmap = beatmap,
                onStart = {
                    audioOffset = AudioOffsetManager.getCurrentOffset()
                    gameState = GameState.PLAYING
                    pausedPosition = 0
                },
                onBack = onBack
            )
        }

        if (gameState == GameState.FINISHED) {
            val hasNextLevel = BeatmapRegistry.hasNextBeatmap(beatmap.id)

            ResultScreen(
                score = score,
                maxCombo = maxCombo,
                accuracy = Level4Scoring.calculateAccuracy(
                    perfectCount, greatCount, goodCount, missCount
                ),
                perfectCount = perfectCount,
                greatCount = greatCount,
                goodCount = goodCount,
                missCount = missCount,
                hasNextLevel = hasNextLevel,
                beatmapId = beatmap.id,
                isAutoMode = isAutoMode,  // ✅ 傳遞 AUTO 模式標記
                onNextLevel = {
                    gameState = GameState.READY
                    score = 0
                    combo = 0
                    maxCombo = 0
                    perfectCount = 0
                    greatCount = 0
                    goodCount = 0
                    missCount = 0
                    activeNotes = emptyList()
                    hitEffects = emptyList()
                    missEffects = emptyList()
                    starEffects = emptyList()
                    noteCounter = 0
                    pausedPosition = 0
                    mediaPlayer?.release()
                    onNextLevel()
                },
                onRetry = {
                    gameState = GameState.READY
                    score = 0
                    combo = 0
                    maxCombo = 0
                    perfectCount = 0
                    greatCount = 0
                    goodCount = 0
                    missCount = 0
                    activeNotes = emptyList()
                    hitEffects = emptyList()
                    missEffects = emptyList()
                    starEffects = emptyList()
                    noteCounter = 0
                    pausedPosition = 0
                    mediaPlayer?.seekTo(0)
                },
                onExit = {
                    mediaPlayer?.release()
                    onBack()
                },
                soundManager = soundManager
            )
        }

        if (showPauseDialog) {  // ✅ AUTO 模式也顯示暫停對話框
            PauseDialog(
                score = score,
                combo = combo,
                onResume = {
                    showPauseDialog = false
                    isCountingDown = true
                },
                onRetry = {
                    showPauseDialog = false
                    gameState = GameState.READY
                    score = 0
                    combo = 0
                    maxCombo = 0
                    perfectCount = 0
                    greatCount = 0
                    goodCount = 0
                    missCount = 0
                    activeNotes = emptyList()
                    hitEffects = emptyList()
                    missEffects = emptyList()
                    starEffects = emptyList()
                    noteCounter = 0
                    pausedPosition = 0
                    mediaPlayer?.seekTo(0)
                    mediaPlayer?.pause()
                },
                onExit = {
                    showPauseDialog = false
                    mediaPlayer?.release()
                    onBack()
                }
            )
        }
    }
}

// ✅ 星星特效數據類
data class StarEffect(
    val position: Offset,
    val startTime: Long
)