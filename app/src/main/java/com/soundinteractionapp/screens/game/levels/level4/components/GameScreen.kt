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
import androidx.navigation.NavController
import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.BeatmapRegistry
import com.soundinteractionapp.screens.game.levels.level4.logic.*
import com.soundinteractionapp.screens.game.levels.level4.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// ✅ 加入缺少的 imports
import com.soundinteractionapp.screens.game.levels.level4.HitResult
import com.soundinteractionapp.screens.game.levels.level4.NoteType

/**
 * 遊戲主畫面
 */
@Composable
fun GameScreen(
    navController: NavController,
    beatmap: Beatmap,
    onBack: () -> Unit,
    onNextLevel: () -> Unit
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
    var hitSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicDuration by remember { mutableLongStateOf(0L) }
    var pausedPosition by remember { mutableIntStateOf(0) }
    var audioOffset by remember { mutableIntStateOf(0) }
    var activeNotes by remember { mutableStateOf<List<ActiveNote>>(emptyList()) }
    var hitEffects by remember { mutableStateOf<List<HitEffect>>(emptyList()) }
    var missEffects by remember { mutableStateOf<List<HitEffect>>(emptyList()) }
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
        try {
            hitSoundPlayer?.reset()
            hitSoundPlayer = MediaPlayer.create(context, R.raw.osu_hit_sound)
            hitSoundPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (gameState == GameState.PLAYING) {
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
            mediaPlayer?.seekTo(pausedPosition)
            mediaPlayer?.start()
        }
    }

    LaunchedEffect(beatmap.id) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, beatmap.audioResId)
            mediaPlayer?.isLooping = false
            musicDuration = mediaPlayer?.duration?.toLong() ?: 0L
            mediaPlayer?.setOnCompletionListener {
                gameState = GameState.FINISHED
            }
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

        while (isActive && gameState == GameState.PLAYING) {
            val rawTime = (mediaPlayer?.currentPosition ?: 0).toLong()
            currentTime = rawTime + audioOffset

            val loopResult = GameLoopHelper.updateGameLoop(
                currentTime = currentTime,
                activeNotes = activeNotes,
                noteCounter = noteCounter,
                isTouching = isTouching,
                touchPosition = touchPosition,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                beatmap = beatmap,
                onScoreUpdate = { result, comboValue ->
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
                },
                onMiss = { notePosition ->
                    missCount++
                    combo = 0
                    missEffects = missEffects + HitEffect(
                        notePosition,
                        System.currentTimeMillis(),
                        HitResult.MISS
                    )
                },
                onSliderReverse = {
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
            hitSoundPlayer?.release()
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
                    currentTime = currentTime,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    beatmap = beatmap,
                    onTouchEvent = { offset, isStart, isEnd ->
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
                                    playHitSound()

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
                                        playHitSound()

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
                    noteCounter = 0
                    pausedPosition = 0
                    mediaPlayer?.seekTo(0)
                },
                onExit = {
                    mediaPlayer?.release()
                    onBack()
                }
            )
        }

        if (showPauseDialog) {
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