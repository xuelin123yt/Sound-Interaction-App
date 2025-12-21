package com.soundinteractionapp.screens.game.levels.level2

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.GameProgressManager
import com.soundinteractionapp.utils.GameScoreUtils
import com.soundinteractionapp.utils.VolumeKeys
import com.soundinteractionapp.screens.profile.models.AchievementManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.pow

// --- 狀態定義 ---
enum class Level2GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED, RESULT }

// --- 難度定義 ---
enum class Level2Difficulty(
    val label: String, val speed: Float, val color: Color, val scoreId: Int,
    val audioResId: Int, val maxScore: Int, val description: String,
    val bgResId: Int, val coverResId: Int, val previewStartTime: Int,
    val unlockKey: String, val rushThreshold: Int, val volumeKey: String
) {
    EASY("簡單模式", 0.004f, Color(0xFF4CAF50), 21, R.raw.castle_in_the_sky, 18400, "天空之城：適合初學者的經典旋律", R.drawable.bg_level2_easy, R.drawable.bg_level2_easy, 30000, "L2_EASY", 20, VolumeKeys.LEVEL2_EASY),
    NORMAL("普通模式", 0.006f, Color(0xFF2196F3), 22, R.raw.totoro, 48250, "龍貓：中等難度的鋼琴節奏挑戰", R.drawable.bg_level2_normal, R.drawable.bg_level2_normal, 15000, "L2_NORMAL", 40, VolumeKeys.LEVEL2_MEDIUM),
    HARD("困難模式", 0.010f, Color(0xFFE53935), 23, R.raw.maria, 52900, "Maria：極速點擊，考驗你的極限手速", R.drawable.bg_level2_hard, R.drawable.bg_level2_hard, 20000, "L2_HARD", 60, VolumeKeys.LEVEL2_HARD)
}

enum class LaneFeedbackType { NONE, HIT, MISS }
data class PianoTilePerspective(val id: String = UUID.randomUUID().toString(), val lane: Int, val logicalY: Float, val baseHeightRatio: Float, val targetTime: Long)

@Composable
fun Level2FollowBeatScreen(
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

    var isNavigating by remember { mutableStateOf(false) }

    // 核心透視參數
    val horizonRatio = -0.3f
    val spawnY = 0.4f
    val visualHitZoneStart = 0.88f
    val visualHitZoneEnd = 0.94f
    val noteTargetHeight = visualHitZoneEnd - visualHitZoneStart

    // 狀態管理
    var gameState by remember { mutableStateOf(Level2GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Level2Difficulty.EASY) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var hitCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }
    var countdownValue by remember { mutableIntStateOf(3) }
    var musicProgress by remember { mutableFloatStateOf(0f) }

    // UI 動畫
    val listState = rememberLazyListState()
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var optionsSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previousSelectedIndex by remember { mutableIntStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "rushAnim")
    val rushTimeOffsetY by infiniteTransition.animateFloat(0f, -8.dp.value, infiniteRepeatable(tween(400, easing = LinearOutSlowInEasing), RepeatMode.Reverse), label = "rush")

    // ✅ 添加衝刺火焰特效動畫
    val sprintFireComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sprint_fire))
    var isSprintActive by remember { mutableStateOf(false) }
    val sprintFireProgress by animateLottieCompositionAsState(
        composition = sprintFireComposition,
        isPlaying = isSprintActive,
        restartOnPlay = false,
        iterations = LottieConstants.IterateForever
    )

    // 玩法變數
    val tiles = remember { mutableStateListOf<PianoTilePerspective>() }
    val laneFeedback = remember { mutableStateListOf(LaneFeedbackType.NONE, LaneFeedbackType.NONE, LaneFeedbackType.NONE, LaneFeedbackType.NONE) }
    var nextNoteIndex by remember { mutableIntStateOf(0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val stopPreview = { previewPlayer?.stop(); previewPlayer?.release(); previewPlayer = null }

    // 選單列表邏輯
    val selectedIndex by remember { derivedStateOf {
        val info = listState.layoutInfo
        val center = info.viewportStartOffset + info.viewportSize.height / 2
        info.visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - center) }?.index ?: 0
    } }

    LaunchedEffect(selectedIndex) {
        if (gameState == Level2GameState.SELECTION) {
            if (selectedIndex != previousSelectedIndex) {
                optionsSoundPlayer?.release()
                optionsSoundPlayer = MediaPlayer.create(context, R.raw.options4)
                optionsSoundPlayer?.start()
            }
            selectedDifficulty = Level2Difficulty.values()[selectedIndex]
            stopPreview(); delay(400)
            try {
                val p = MediaPlayer.create(context, selectedDifficulty.audioResId)
                p.isLooping = true
                p.seekTo(selectedDifficulty.previewStartTime)
                p.start()
                previewPlayer = p
            } catch (e: Exception) {}
            previousSelectedIndex = selectedIndex
        }
    }

    // ✅ 監聽 combo 變化來控制火焰特效
    LaunchedEffect(combo, gameState) {
        if (gameState == Level2GameState.PLAYING) {
            val wasSprintActive = isSprintActive
            isSprintActive = combo >= selectedDifficulty.rushThreshold

            if (!wasSprintActive && isSprintActive) {
                Log.d("Level2Sprint", "🔥 進入衝刺模式! Combo: $combo")
            }
        } else {
            isSprintActive = false
        }
    }

    LaunchedEffect(gameState) {
        val isPlaying = gameState == Level2GameState.PLAYING
        onGameStateChanged(isPlaying)
    }

    // 遊戲流程與紀錄/解鎖邏輯
    LaunchedEffect(gameState) {
        if (gameState != Level2GameState.SELECTION) stopPreview()

        if (gameState == Level2GameState.COUNTDOWN) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, selectedDifficulty.audioResId)
            score = 0
            combo = 0
            maxCombo = 0
            hitCount = 0
            missCount = 0
            tiles.clear()
            nextNoteIndex = 0
            musicProgress = 0f
            countdownValue = 3
            delay(500)
            while (countdownValue > 0) { delay(1000); countdownValue-- }
            gameState = Level2GameState.PLAYING

            soundManager.playGameMusic(selectedDifficulty.audioResId, selectedDifficulty.volumeKey)
        }

        if (gameState == Level2GameState.FINISHED) {
            mediaPlayer?.pause()
            soundManager.stopMusic()

            rankingViewModel.updateHighScore(selectedDifficulty.scoreId, score)
            if (maxCombo > 0) {
                rankingViewModel.updateLevel2MaxCombo(maxCombo)
            }

            if (!isGuest) {
                if (selectedDifficulty == Level2Difficulty.EASY && score >= 11000) {
                    progressManager.unlockDifficulty(Level2Difficulty.NORMAL.unlockKey)
                } else if (selectedDifficulty == Level2Difficulty.NORMAL && score >= 20000) {
                    progressManager.unlockDifficulty(Level2Difficulty.HARD.unlockKey)
                }
            }
            gameState = Level2GameState.RESULT
        }
    }

    // 核心玩法:絕對時間同步
    LaunchedEffect(gameState) {
        if (gameState == Level2GameState.PLAYING && mediaPlayer != null) {
            val player = mediaPlayer!!
            val currentChart = when (selectedDifficulty) {
                Level2Difficulty.EASY -> Level2Charts.castle
                Level2Difficulty.NORMAL -> Level2Charts.totoro
                Level2Difficulty.HARD -> Level2Charts.maria
            }
            val travelTime = (0.5f / selectedDifficulty.speed) * 16L
            val audioLatencyOffset = -85L

            player.seekTo(0); player.start()
            while (isActive && gameState == Level2GameState.PLAYING) {
                val currentTime = player.currentPosition.toLong() + audioLatencyOffset
                musicProgress = currentTime.toFloat() / player.duration.toFloat()

                while (nextNoteIndex < currentChart.size && currentChart[nextNoteIndex].timeMs <= currentTime + travelTime) {
                    val note = currentChart[nextNoteIndex]
                    if (note.lane <= 3) tiles.add(PianoTilePerspective(lane = note.lane, logicalY = spawnY, baseHeightRatio = noteTargetHeight, targetTime = note.timeMs))
                    nextNoteIndex++
                }

                val iterator = tiles.listIterator()
                while (iterator.hasNext()) {
                    val tile = iterator.next()
                    val startTime = tile.targetTime - travelTime
                    val progress = (currentTime - startTime).toFloat() / travelTime.toFloat()
                    val newY = spawnY + (progress * 0.5f)
                    if (newY > 1.22f) {
                        iterator.remove()
                        combo = 0
                        missCount++
                        coroutineScope.launch {
                            laneFeedback[tile.lane] = LaneFeedbackType.MISS
                            delay(120)
                            laneFeedback[tile.lane] = LaneFeedbackType.NONE
                        }
                    } else {
                        iterator.set(tile.copy(logicalY = newY))
                    }
                }

                if (!player.isPlaying && tiles.isEmpty() && nextNoteIndex >= currentChart.size) {
                    gameState = Level2GameState.FINISHED
                }
                delay(12)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, gameState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (gameState == Level2GameState.SELECTION) {
                        previewPlayer?.pause()
                        Log.d("Level2", "Preview paused")
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (gameState == Level2GameState.SELECTION && previewPlayer != null) {
                        previewPlayer?.start()
                        Log.d("Level2", "Preview resumed")
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
    Box(Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Image(
            painter = painterResource(id = selectedDifficulty.bgResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(if (gameState == Level2GameState.SELECTION) 25.dp else 5.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.35f
        )

        // ✅ 衝刺火焰特效層
        if (gameState == Level2GameState.PLAYING && isSprintActive) {
            LottieAnimation(
                composition = sprintFireComposition,
                progress = { sprintFireProgress },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = size.height * 0.28f
                    }
                    .alpha(0.3f)
            )
        }

        if (gameState == Level2GameState.SELECTION) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(0.45f).fillMaxHeight(), Alignment.Center) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(Level2Difficulty.values()) { index, diff ->
                            val isLocked = if (diff == Level2Difficulty.EASY) false else !progressManager.isUnlocked(diff.unlockKey)
                            Level2DifficultyCard(diff, index == selectedIndex, isLocked) {
                                coroutineScope.launch { listState.animateScrollToItem(index) }
                            }
                        }
                    }
                }
                Box(Modifier.weight(0.55f).fillMaxHeight(), Alignment.Center) {
                    val isLocked = if (selectedDifficulty == Level2Difficulty.EASY) false else !progressManager.isUnlocked(selectedDifficulty.unlockKey)
                    Level2TrapezoidDisplay(
                        selectedDifficulty,
                        isLocked,
                        if(selectedDifficulty == Level2Difficulty.NORMAL) "需在簡單模式獲 11000 分" else "需在普通模式獲 20000 分"
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
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                enabled = !isNavigating
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            val startEnabled = if(selectedDifficulty == Level2Difficulty.EASY) true else progressManager.isUnlocked(selectedDifficulty.unlockKey)
            Level2ActionButtons(
                onStart = { gameState = Level2GameState.COUNTDOWN },
                isEnabled = startEnabled,
                themeColor = selectedDifficulty.color,
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp).fillMaxWidth(0.42f)
            )
        }

        if (gameState == Level2GameState.COUNTDOWN) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), Alignment.Center) {
                Text(
                    text = if (countdownValue > 0) "$countdownValue" else "GO!",
                    fontSize = 110.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (gameState == Level2GameState.PLAYING) {
            Box(Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = { tapOffset ->
                    val hY = size.height * horizonRatio
                    val vX = size.width / 2f
                    val dy = tapOffset.y - hY
                    if (dy > 0) {
                        val scale = (size.height - hY) / dy
                        val projectedX = vX + (tapOffset.x - vX) * scale
                        val tappedLane = (projectedX / (size.width / 4)).toInt().coerceIn(0, 3)
                        val target = tiles.firstOrNull { it.lane == tappedLane && it.logicalY in 0.86f..1.02f }
                        if (target != null) {
                            tiles.remove(target)
                            combo++
                            if (combo > maxCombo) maxCombo = combo
                            hitCount++

                            val isRush = combo >= selectedDifficulty.rushThreshold
                            score += if (isRush) 150 else 100

                            soundManager.playGameSFX("level2_piano_hit", VolumeKeys.LEVEL2_HIT)

                            coroutineScope.launch {
                                laneFeedback[tappedLane] = LaneFeedbackType.HIT
                                delay(120)
                                laneFeedback[tappedLane] = LaneFeedbackType.NONE
                            }
                        } else {
                            missCount++
                            combo = 0
                            coroutineScope.launch {
                                laneFeedback[tappedLane] = LaneFeedbackType.MISS
                                delay(120)
                                laneFeedback[tappedLane] = LaneFeedbackType.NONE
                            }
                        }
                    }
                })
            }) {
                Level2PianoCanvas(tiles, laneFeedback, horizonRatio)
            }

            Box(Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("SCORE: $score", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    val isRush = combo >= selectedDifficulty.rushThreshold
                    if (!isRush) {
                        Text("COMBO: $combo", fontSize = 32.sp, color = Color.White.copy(0.85f), fontWeight = FontWeight.ExtraBold)
                    }

                    if (isRush) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔥 衝刺 Time: $combo",
                            fontSize = 38.sp,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.alpha(0.7f).graphicsLayer { translationY = rushTimeOffsetY }
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

            LinearProgressIndicator(
                progress = { musicProgress.coerceIn(0f, 1f) },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(8.dp),
                color = selectedDifficulty.color
            )

            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    try { mediaPlayer?.stop() } catch(e:Exception){}
                    soundManager.stopMusic()
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(0.7f),
                    disabledContainerColor = Color.Red.copy(0.5f)
                ),
                enabled = !isNavigating,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Text("退出")
            }
        }

        if (gameState == Level2GameState.RESULT) {
            Level2ResultContent(
                score = score,
                maxScore = selectedDifficulty.maxScore,
                hitCount = hitCount,
                missCount = missCount,
                maxCombo = maxCombo,
                difficultyName = selectedDifficulty.label,
                onRetry = { gameState = Level2GameState.COUNTDOWN },
                onSelect = { gameState = Level2GameState.SELECTION },
                onExit = {
                    if (isNavigating) return@Level2ResultContent
                    isNavigating = true
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                isNavigating = isNavigating,
                rankingViewModel = rankingViewModel,
                soundManager = soundManager
            )
        }
    }
}

// --- UI 封裝組件 ---

@Composable
fun Level2DifficultyCard(diff: Level2Difficulty, isSelected: Boolean, isLocked: Boolean, onSelect: () -> Unit) {
    val cardBg = if (isLocked) Color.Gray else diff.color
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(82.dp).clickable { onSelect() }.zIndex(if (isSelected) 10f else 0f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg.copy(if (isSelected) 0.8f else 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else 2.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(diff.label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if(isLocked) Color.LightGray else Color.White)
                Text(diff.description, fontSize = 12.sp, color = Color.White.copy(0.7f))
            }
            if (isLocked) Icon(Icons.Default.Lock, null, tint = Color.White)
        }
    }
}

@Composable
fun Level2TrapezoidDisplay(diff: Level2Difficulty, isLocked: Boolean, hint: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Box(Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.55f).offset(x = 45.dp).drawWithContent {
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
        }) {
            Image(
                painter = painterResource(id = diff.coverResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = if (isLocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) }) else null
            )
            if (isLocked) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.75f)), Alignment.Center) {
                    Text(hint, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun Level2ActionButtons(onStart: () -> Unit, isEnabled: Boolean, themeColor: Color, modifier: Modifier) {
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
            if (isEnabled) "開始挑戰" else "尚未解鎖",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun Level2PianoCanvas(tiles: List<PianoTilePerspective>, laneFeedback: List<LaneFeedbackType>, horizonRatio: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val hY = size.height * horizonRatio
        val vX = size.width / 2f
        val laneW = size.width / 4

        for (i in 0..4) drawLine(Color.White.copy(0.15f), Offset(vX, hY), Offset(laneW * i, size.height), 2f)

        val pY1 = hY + (size.height - hY) * 0.88f.pow(2)
        val pY2 = hY + (size.height - hY) * 0.98f.pow(2)
        drawRect(Color.White.copy(0.1f), Offset(0f, pY1), Size(size.width, pY2 - pY1))

        laneFeedback.forEachIndexed { i, type ->
            if (type != LaneFeedbackType.NONE) {
                val color = if (type == LaneFeedbackType.HIT) Color.Cyan else Color.Red
                val path = Path().apply {
                    moveTo(vX, hY)
                    lineTo(laneW * i, size.height)
                    lineTo(laneW * (i + 1), size.height)
                    close()
                }
                drawPath(path, Brush.verticalGradient(listOf(color.copy(0f), color.copy(0.4f))))
            }
        }

        tiles.forEach { tile ->
            val yB = hY + (size.height - hY) * tile.logicalY.pow(2)
            val yT = hY + (size.height - hY) * (tile.logicalY - tile.baseHeightRatio).pow(2)
            fun getX(lY: Float, lIdx: Int) = vX + (lIdx * laneW - vX) * lY.pow(2)
            val path = Path().apply {
                moveTo(getX(tile.logicalY - tile.baseHeightRatio, tile.lane), yT)
                lineTo(getX(tile.logicalY - tile.baseHeightRatio, tile.lane + 1), yT)
                lineTo(getX(tile.logicalY, tile.lane + 1), yB)
                lineTo(getX(tile.logicalY, tile.lane), yB)
                close()
            }
            drawPath(path, Color(0xFF2196F3))
            drawPath(path, Color.White, style = Stroke(2f))
        }
    }
}

@Composable
fun Level2ResultContent(
    score: Int,
    maxScore: Int,
    hitCount: Int,
    missCount: Int,
    maxCombo: Int,
    difficultyName: String,
    onRetry: () -> Unit,
    onSelect: () -> Unit,
    onExit: () -> Unit,
    isNavigating: Boolean,
    rankingViewModel: RankingViewModel = viewModel(),
    soundManager: SoundManager // ✅ 新增參數
) {
    val rank = GameScoreUtils.calculateRank(score, maxScore)
    val rankColor = GameScoreUtils.getRankColor(rank)

    // ✅ 添加 Lottie 動畫
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    var playAnimation by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = playAnimation,
        restartOnPlay = true,
        iterations = 1
    )

    // ✅ 新增:用於追蹤「首次解鎖」的成就
    var newlyUnlockedAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // ✅ 結算時檢查成就並播放音效+動畫
    LaunchedEffect(Unit) {
        // 播放煙火音效
        soundManager.playSFX("fireworks")
        // 觸發動畫
        playAnimation = true

        coroutineScope.launch {
            try {
                val achievementManager = AchievementManager()
                val currentScores = rankingViewModel.scores.value

                val newlyUnlocked = achievementManager.checkAndUnlockAchievements(
                    scoreEntry = currentScores,
                    hasFeedback = false,
                    hasAvatar = false
                )

                val gameRelatedAchievements = mutableListOf<String>()

                // 成就 2: Combo Master／連擊大師
                if (2 in newlyUnlocked && currentScores.level2MaxCombo >= 100) {
                    gameRelatedAchievements.add("Combo Master／連擊大師")
                }

                // 成就 6: Mode Three Completionist／模式三完成者
                if (6 in newlyUnlocked) {
                    gameRelatedAchievements.add("Mode Three Completionist／模式三完成者")
                }

                if (gameRelatedAchievements.isNotEmpty()) {
                    newlyUnlockedAchievements = gameRelatedAchievements
                    Log.d("Level2Result", "🎉 新解鎖成就: $gameRelatedAchievements")
                }
            } catch (e: Exception) {
                Log.e("Level2Result", "❌ 檢查成就失敗", e)
            }
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(0.85f)),
        Alignment.Center
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
                Text("關卡二 結算", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.height(30.dp))
                Text(rank, fontSize = 100.sp, fontWeight = FontWeight.Black, color = rankColor)
                Text("最終得分: $score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Level2StatBubble("擊中", hitCount, Color(0xFF00E676))
                    Level2StatBubble("Max Combo", maxCombo, Color(0xFFFFD700))
                    Level2StatBubble("失誤", missCount, Color(0xFFFF5252))
                }

                // ✅ 顯示新解鎖的成就
                if (newlyUnlockedAchievements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    newlyUnlockedAchievements.forEach { achievementName ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏆", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "成就解鎖！",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        achievementName,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        if (achievementName != newlyUnlockedAchievements.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("重玩")
                    }
                    Button(
                        onClick = onSelect,
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

        // ✅ 煙火動畫覆蓋層
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun Level2StatBubble(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp).background(color.copy(0.2f), CircleShape).border(2.dp, color, CircleShape)
        ) {
            Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}