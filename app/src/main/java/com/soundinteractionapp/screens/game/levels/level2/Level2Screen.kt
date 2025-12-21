package com.soundinteractionapp.screens.game.levels.level2

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.GameProgressManager
import com.soundinteractionapp.screens.game.levels.level2.Level2Charts
import com.soundinteractionapp.screens.profile.models.AchievementManager
import com.soundinteractionapp.utils.GameScoreUtils
import com.soundinteractionapp.utils.VolumeKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.pow

enum class GameState { SELECTION, COUNTDOWN, PLAYING, FINISHED, RESULT }

enum class Level2Difficulty(
    val label: String,
    val speed: Float,
    val color: Color,
    val scoreId: Int,
    val maxScore: Int,
    val audioResId: Int,
    val volumeKey: String  // ✅ 新增音量鍵
) {
    EASY("簡單 (天空之城)", 0.004f, Color(0xFF4CAF50), 21, 18400, R.raw.castle_in_the_sky, VolumeKeys.LEVEL2_EASY),
    NORMAL("普通 (龍貓)", 0.006f, Color(0xFF2196F3), 22, 48250, R.raw.totoro, VolumeKeys.LEVEL2_MEDIUM),
    HARD("困難 (Maria)", 0.009f, Color(0xFFE53935), 23, 52900, R.raw.maria, VolumeKeys.LEVEL2_HARD)
}

enum class LaneFeedbackType { NONE, HIT, MISS }

data class PianoTilePerspective(
    val id: String = UUID.randomUUID().toString(),
    val lane: Int,
    val logicalY: Float,
    val baseHeightRatio: Float
)

@Composable
fun Level2FollowBeatScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    rankingViewModel: RankingViewModel
) {
    val context = LocalContext.current
    val progressManager = remember { GameProgressManager(context) }
    val auth = FirebaseAuth.getInstance()
    val isGuest = auth.currentUser?.isAnonymous == true

    var isNavigating by remember { mutableStateOf(false) }

    val spawnY = 0.4f
    val visualHitZoneStart = 0.88f
    val visualHitZoneEnd = 0.94f
    val noteTargetHeight = visualHitZoneEnd - visualHitZoneStart
    val manualOffset = -50L

    var gameState by remember { mutableStateOf(GameState.SELECTION) }
    var selectedDifficulty by remember { mutableStateOf(Level2Difficulty.EASY) }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var hitCount by remember { mutableIntStateOf(0) }
    var missCount by remember { mutableIntStateOf(0) }

    var musicProgress by remember { mutableFloatStateOf(0f) }
    val comboYOffset = remember { Animatable(0f) }
    var countdownValue by remember { mutableIntStateOf(3) }

    val goldColor = Color(0xFFFFD700)
    val infiniteTransition = rememberInfiniteTransition(label = "rushTimeAnim")
    val rushTimeOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rushTimeOffsetY"
    )

    val tiles = remember { mutableStateListOf<PianoTilePerspective>() }
    val laneFeedback = remember { mutableStateListOf(LaneFeedbackType.NONE, LaneFeedbackType.NONE, LaneFeedbackType.NONE, LaneFeedbackType.NONE) }
    val scope = rememberCoroutineScope()
    var nextNoteIndex by remember { mutableIntStateOf(0) }

    // ✅ 移除舊的 MediaPlayer 和 SoundPool，改用 SoundManager

    // ✅ 音樂播放控制
    LaunchedEffect(gameState) {
        if (gameState == GameState.COUNTDOWN) {
            score = 0; combo = 0; maxCombo = 0; hitCount = 0; missCount = 0
            countdownValue = 3; tiles.clear(); nextNoteIndex = 0; musicProgress = 0f
            delay(500)
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            gameState = GameState.PLAYING

            // ✅ 使用 playGameMusic() 播放背景音樂（不受設定頁面音樂音量影響）
            soundManager.playGameMusic(selectedDifficulty.audioResId, selectedDifficulty.volumeKey)
        }

        if (gameState == GameState.FINISHED) {
            // ✅ 停止音樂
            soundManager.stopMusic()

            rankingViewModel.updateHighScore(selectedDifficulty.scoreId, score)
            if (maxCombo > 0) {
                rankingViewModel.updateLevel2MaxCombo(maxCombo)
            }
            if (!isGuest) {
                if (selectedDifficulty == Level2Difficulty.EASY && score >= 11000) {
                    progressManager.unlockDifficulty(Level2Difficulty.NORMAL.label)
                } else if (selectedDifficulty == Level2Difficulty.NORMAL && score >= 20000) {
                    progressManager.unlockDifficulty(Level2Difficulty.HARD.label)
                }
            }
            delay(500)
            gameState = GameState.RESULT
        }
    }

    // ✅ 遊戲主循環（不需要修改音樂部分，因為已經在上面處理了）
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            val currentChart = when (selectedDifficulty) {
                Level2Difficulty.EASY -> Level2Charts.castle
                Level2Difficulty.NORMAL -> Level2Charts.totoro
                Level2Difficulty.HARD -> Level2Charts.maria
            }
            val hitZoneCenter = (visualHitZoneStart + visualHitZoneEnd) / 2
            val noteCenterAtSpawn = spawnY - (noteTargetHeight / 2)
            val distance = hitZoneCenter - noteCenterAtSpawn
            val fallTimeOffset = ((distance / selectedDifficulty.speed) * 16).toLong() + manualOffset

            // ✅ 使用系統時間來計算音樂進度（因為沒有 MediaPlayer 的 currentPosition）
            val startTime = System.currentTimeMillis()
            val songDuration = when (selectedDifficulty) {
                Level2Difficulty.EASY -> 95000L    // 天空之城約95秒
                Level2Difficulty.NORMAL -> 77000L  // 龍貓約77秒
                Level2Difficulty.HARD -> 58000L    // Maria約58秒
            }

            while (isActive && gameState == GameState.PLAYING) {
                val currentTime = System.currentTimeMillis() - startTime
                musicProgress = (currentTime.toFloat() / songDuration.toFloat()).coerceIn(0f, 1f)
                val targetSpawnTime = currentTime + fallTimeOffset

                while (nextNoteIndex < currentChart.size && currentChart[nextNoteIndex].timeMs <= targetSpawnTime) {
                    val note = currentChart[nextNoteIndex]
                    if (note.lane <= 3) {
                        tiles.add(PianoTilePerspective(lane = note.lane, logicalY = spawnY, baseHeightRatio = noteTargetHeight))
                    }
                    nextNoteIndex++
                }

                val iterator = tiles.listIterator()
                while (iterator.hasNext()) {
                    val tile = iterator.next()
                    val newY = tile.logicalY + selectedDifficulty.speed
                    if (newY > 1.2f) {
                        iterator.remove()
                        combo = 0
                        score = (score - 50).coerceAtLeast(0)
                        missCount++
                        scope.launch {
                            laneFeedback[tile.lane] = LaneFeedbackType.MISS
                            delay(100)
                            laneFeedback[tile.lane] = LaneFeedbackType.NONE
                        }
                    } else {
                        iterator.set(tile.copy(logicalY = newY))
                    }
                }

                // ✅ 遊戲結束判定（音樂時間到 + 沒有音符）
                if (currentTime >= songDuration && tiles.isEmpty()) {
                    gameState = GameState.FINISHED
                }

                delay(16L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))))) {
        if (gameState == GameState.SELECTION) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎹 鋼琴節奏", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(32.dp))

                Level2Difficulty.values().forEach { difficulty ->
                    val isUnlocked = if (isGuest) true else when (difficulty) {
                        Level2Difficulty.EASY -> true
                        else -> progressManager.isUnlocked(difficulty.label)
                    }
                    Level2DifficultySelectionCard(
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
                            Text("• 簡單 > 11000分 解鎖普通", color = Color(0xFFFFD54F), fontSize = 15.sp)
                            Text("• 普通 > 20000分 解鎖困難", color = Color(0xFFFFD54F), fontSize = 15.sp)
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF1E88E5).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👤 訪客模式", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Text("• 訪客模式資料僅保留於本次執行", color = Color(0xFF90CAF9), fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = {
                        if (isNavigating) return@OutlinedButton
                        isNavigating = true
                        soundManager.playSFX("cancel")
                        onNavigateBack()
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    enabled = !isNavigating
                ) {
                    Text("返回主選單")
                }
            }
        }

        if (gameState == GameState.COUNTDOWN) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdownValue > 0) "$countdownValue" else "GO!",
                    fontSize = 120.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (gameState == GameState.PLAYING || gameState == GameState.FINISHED) {
            Box(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures(onTap = { tapOffset ->
                        if (gameState == GameState.PLAYING) {
                            val maxWidth = size.width
                            val maxHeight = size.height
                            val horizonY = maxHeight * -0.3f
                            val vanishingPointX = maxWidth / 2f
                            val dy = tapOffset.y - horizonY

                            if (dy > 0) {
                                val scale = (maxHeight - horizonY) / dy
                                val projectedBottomX = vanishingPointX + (tapOffset.x - vanishingPointX) * scale
                                val tappedLane = (projectedBottomX / (maxWidth / 4)).toInt()

                                if (tappedLane in 0 until 4) {
                                    val targetTile = tiles.filter {
                                        it.lane == tappedLane &&
                                                it.logicalY > visualHitZoneStart &&
                                                (it.logicalY - it.baseHeightRatio) < visualHitZoneEnd
                                    }.maxByOrNull { it.logicalY }

                                    if (targetTile != null) {
                                        tiles.remove(targetTile)
                                        combo++
                                        if (combo > maxCombo) maxCombo = combo
                                        hitCount++
                                        val isRushTime = combo >= 40
                                        score += if (isRushTime) 150 else 100

                                        // ✅ 播放打擊音效（使用 playGameSFX，不受設定頁面 SFX 音量影響）
                                        soundManager.playGameSFX("level2_piano_hit", VolumeKeys.LEVEL2_HIT)

                                        scope.launch {
                                            laneFeedback[tappedLane] = LaneFeedbackType.HIT
                                            comboYOffset.snapTo(0f)
                                            comboYOffset.animateTo(-20f, tween(100))
                                            comboYOffset.animateTo(0f, tween(100))
                                            delay(50)
                                            laneFeedback[tappedLane] = LaneFeedbackType.NONE
                                        }
                                    } else {
                                        score = (score - 1).coerceAtLeast(0)
                                        missCount++
                                        scope.launch {
                                            laneFeedback[tappedLane] = LaneFeedbackType.MISS
                                            delay(150)
                                            laneFeedback[tappedLane] = LaneFeedbackType.NONE
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            ) {
                Level2CanvasContent(tiles, laneFeedback, visualHitZoneStart, visualHitZoneEnd)
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(modifier = Modifier.align(Alignment.TopEnd), horizontalAlignment = Alignment.End) {
                    Text("分數: $score", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("難度: ${selectedDifficulty.label}", color = selectedDifficulty.color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (combo > 0) {
                val isRushTime = combo >= 40
                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    if (isRushTime) {
                        Text(
                            "🔥 衝刺 Time: $combo",
                            style = MaterialTheme.typography.headlineLarge,
                            color = goldColor,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.offset(y = comboYOffset.value.dp).graphicsLayer { translationY = rushTimeOffsetY }
                        )
                    } else {
                        Text(
                            "Combo $combo",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = comboYOffset.value.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { musicProgress },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(6.dp),
                color = Color.Cyan,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    soundManager.stopMusic()
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                enabled = !isNavigating,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Text("退出")
            }
        }

        if (gameState == GameState.RESULT) {
            Level2GameResultContent(
                score, selectedDifficulty.maxScore, hitCount, missCount, maxCombo, selectedDifficulty.label,
                { gameState = GameState.COUNTDOWN },
                { gameState = GameState.SELECTION },
                {
                    if (isNavigating) return@Level2GameResultContent
                    isNavigating = true
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                isNavigating,
                rankingViewModel = rankingViewModel  // ✅ 加入這行
            )
        }
    }
}

@Composable
fun Level2GameResultContent(
    score: Int,
    maxScore: Int,
    hitCount: Int,
    missCount: Int,
    maxCombo: Int,
    difficultyName: String,
    onRetry: () -> Unit,
    onSelectDifficulty: () -> Unit,
    onExit: () -> Unit,
    isNavigating: Boolean,
    rankingViewModel: RankingViewModel = viewModel()  // ✅ 新增這行
) {
    val rank = GameScoreUtils.calculateRank(score, maxScore)
    val rankColor = GameScoreUtils.getRankColor(rank)
    val infiniteTransition = rememberInfiniteTransition(label = "rankBounce")
    val scale by infiniteTransition.animateFloat(1f, 1.1f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    // ✅ 新增：用於追蹤「首次解鎖」的成就
    var newlyUnlockedAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // ✅ 結算時檢查成就
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

                val gameRelatedAchievements = mutableListOf<String>()

                if (2 in newlyUnlocked && currentScores.level2MaxCombo >= 100) {
                    gameRelatedAchievements.add("Combo Master／連擊大師")
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.width(420.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("關卡結算 - $difficultyName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
                    Text(text = rank, fontSize = 100.sp, fontWeight = FontWeight.Black, color = rankColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("分數: $score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBubble("Hit", hitCount, Color(0xFF4FC3F7))
                    StatBubble("Max Combo", maxCombo, Color(0xFFFFD700))
                    StatBubble("Miss", missCount, Color(0xFFFF5252))
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
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("重玩")
                    }
                    Button(onClick = onSelectDifficulty, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.List, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("選難度")
                    }
                    OutlinedButton(
                        onClick = onExit,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
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
            modifier = Modifier.size(60.dp).background(color.copy(alpha = 0.2f), CircleShape).border(2.dp, color, CircleShape)
        ) {
            Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level2DifficultySelectionCard(difficulty: Level2Difficulty, isUnlocked: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp).then(if (!isUnlocked) Modifier.blur(1.dp) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) difficulty.color.copy(alpha = 0.9f) else Color.DarkGray.copy(alpha = 0.6f)
        ),
        border = BorderStroke(2.dp, if (isUnlocked) difficulty.color else Color.Gray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isUnlocked) Icons.Filled.MusicNote else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color.White else Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = if (isUnlocked) difficulty.label else "${difficulty.label} (未解鎖)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color.LightGray
            )
        }
    }
}

@Composable
fun Level2CanvasContent(
    tiles: List<PianoTilePerspective>,
    laneFeedback: List<LaneFeedbackType>,
    visualHitZoneStart: Float,
    visualHitZoneEnd: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = size.width
        val maxHeightPx = size.height
        val horizonYPx = maxHeightPx * -0.3f
        val vanishingPointX = maxWidthPx / 2f
        val bottomLaneWidth = maxWidthPx / 4

        val trackPath = Path().apply {
            moveTo(vanishingPointX, horizonYPx)
            lineTo(0f, maxHeightPx)
            lineTo(maxWidthPx, maxHeightPx)
            close()
        }
        drawPath(trackPath, color = Color.White.copy(alpha = 0.1f))

        for (i in 1 until 4) {
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(vanishingPointX, horizonYPx),
                end = Offset(bottomLaneWidth * i, maxHeightPx),
                strokeWidth = 2f
            )
        }

        laneFeedback.forEachIndexed { index, type ->
            if (type != LaneFeedbackType.NONE) {
                val glowColor = if (type == LaneFeedbackType.HIT) Color(0xFF4FC3F7) else Color(0xFFFF5252)
                val highlightPath = Path().apply {
                    moveTo(vanishingPointX, horizonYPx)
                    lineTo(bottomLaneWidth * index, maxHeightPx)
                    lineTo(bottomLaneWidth * (index + 1), maxHeightPx)
                    close()
                }
                drawPath(
                    path = highlightPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor.copy(alpha = 0.0f), glowColor.copy(alpha = 0.6f)),
                        startY = 0f,
                        endY = maxHeightPx
                    )
                )
            }
        }

        val startT = visualHitZoneStart.pow(2)
        val endT = visualHitZoneEnd.pow(2)
        val startY = horizonYPx + (maxHeightPx - horizonYPx) * startT
        val endY = horizonYPx + (maxHeightPx - horizonYPx) * endT
        val zonePath = Path().apply {
            moveTo(vanishingPointX + (0f - vanishingPointX) * startT, startY)
            lineTo(vanishingPointX + (maxWidthPx - vanishingPointX) * startT, startY)
            lineTo(vanishingPointX + (maxWidthPx - vanishingPointX) * endT, endY)
            lineTo(vanishingPointX + (0f - vanishingPointX) * endT, endY)
            close()
        }
        drawPath(zonePath, color = Color.Gray.copy(alpha = 0.3f))
        drawPath(zonePath, color = Color.Gray, style = Stroke(width = 3f))

        tiles.forEach { tile ->
            val bottomLogicalY = tile.logicalY
            val topLogicalY = tile.logicalY - tile.baseHeightRatio

            if (bottomLogicalY > 0f) {
                fun getProjectedPoint(logicalY: Float, laneIndex: Int, isLeft: Boolean): Offset {
                    val t = logicalY.coerceAtLeast(0f).pow(2)
                    val yPos = horizonYPx + (maxHeightPx - horizonYPx) * t
                    val targetBottomX = if (isLeft) laneIndex * bottomLaneWidth else (laneIndex + 1) * bottomLaneWidth
                    val xPos = vanishingPointX + (targetBottomX - vanishingPointX) * t
                    return Offset(xPos, yPos)
                }

                val p1 = getProjectedPoint(topLogicalY, tile.lane, true)
                val p2 = getProjectedPoint(topLogicalY, tile.lane, false)
                val p3 = getProjectedPoint(bottomLogicalY, tile.lane, false)
                val p4 = getProjectedPoint(bottomLogicalY, tile.lane, true)
                val notePath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }
                val baseColor = if (tile.lane in 1..2) Color(0xFF64B5F6) else Color(0xFF2196F3)
                drawPath(
                    path = notePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(baseColor.copy(alpha = 0.6f), baseColor),
                        startY = p1.y,
                        endY = p4.y
                    )
                )
                drawPath(path = notePath, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 3f))
                drawLine(color = Color.White, start = p4, end = p3, strokeWidth = 6f)
            }
        }
    }
}