package com.soundinteractionapp.screens.game.levels

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.soundinteractionapp.GameEngine
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.screens.profile.models.AchievementManager
import com.soundinteractionapp.utils.VolumeKeys

@OptIn(UnstableApi::class)
@Composable
fun VideoBackground(videoResId: Int) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun Level3PitchScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    rankingViewModel: RankingViewModel = viewModel()
) {
    val context = LocalContext.current
    var isNavigating by remember { mutableStateOf(false) }

    var birdY by remember { mutableFloatStateOf(500f) }
    var score by remember { mutableIntStateOf(0) }
    var currentHp by remember { mutableIntStateOf(100) }
    var isGameOver by remember { mutableStateOf(false) }

    var obstacles by remember { mutableStateOf(floatArrayOf()) }
    var isPlaying by remember { mutableStateOf(true) }

    var permissionGranted by remember { mutableStateOf(false) }
    var showStartHint by remember { mutableStateOf(true) }
    val maxHp = 100

    val GameFont = FontFamily(Font(R.font.huninn))
    val scoreScale = remember { Animatable(1f) }

    LaunchedEffect(score) {
        if (score > 0) {
            scoreScale.snapTo(1f)
            scoreScale.animateTo(1.5f, animationSpec = tween(100))
            scoreScale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
        }
    }

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            rankingViewModel.onGameFinished(levelId = 3, finalScore = score)
            Log.d("Level3", "遊戲結束,嘗試更新分數: $score")
            soundManager.stopMusic()
        }
    }

    // ✅ 背景音樂處理
    val musicList = listOf(R.raw.music1, R.raw.music2, R.raw.music3)
    val randomMusicResId = remember { musicList.random() }

    DisposableEffect(Unit) {
        // ✅ 使用 playGameMusic() 播放背景音樂（現在支持循環播放）
        soundManager.playGameMusic(randomMusicResId, VolumeKeys.LEVEL3_MUSIC)

        onDispose {
            soundManager.stopMusic()
        }
    }

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    val birdSprites = listOf(
        ImageBitmap.imageResource(id = R.drawable.bird_1),
        ImageBitmap.imageResource(id = R.drawable.bird_2),
        ImageBitmap.imageResource(id = R.drawable.bird_3)
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameCounter by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            permissionGranted = true
        }
        GameEngine.initGame()
        delay(3000)
        showStartHint = false
    }

    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) return@LaunchedEffect
        launch(Dispatchers.IO) {
            val sampleRate = 44100
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                val buffer = ShortArray(bufferSize)
                audioRecord.startRecording()
                try {
                    while (isPlaying) {
                        val readCount = audioRecord.read(buffer, 0, bufferSize)
                        if (readCount > 0) GameEngine.processAudio(buffer, readCount)
                    }
                } catch (e: Exception) {
                    Log.e("Mic", "Error: ${e.message}")
                } finally {
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            birdY = GameEngine.updateGame()
            obstacles = GameEngine.getObstacleData()
            val state = GameEngine.getGameState()
            val newScore = state[0].toInt()
            val gameOverFlag = state[2] > 0.5f
            val newHp = if (state.size >= 5) state[4].toInt() else currentHp

            if (newScore > score) {
                soundManager.playGameSFX("level3_correct", VolumeKeys.LEVEL3_EFFECT)
            }

            if (newHp < currentHp) {
                soundManager.playGameSFX("level3_hit", VolumeKeys.LEVEL3_EFFECT)
            }

            score = newScore
            currentHp = newHp

            if (gameOverFlag) {
                isGameOver = true
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
        VideoBackground(videoResId = R.raw.sky)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.height / 2000f

            scale(scale = scaleFactor, pivot = Offset.Zero) {
                val pipeWidth = 300f
                val rimHeight = 80f
                val rimOverhang = 20f

                val pipeBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF558946), Color(0xFF96E668), Color(0xFF558946)),
                    startX = 0f, endX = pipeWidth
                )
                val rimBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF558946), Color(0xFFAAFFA0), Color(0xFF558946)),
                    startX = -rimOverhang, endX = pipeWidth + rimOverhang
                )
                val borderColor = Color(0xFF2F4F2F)

                for (i in obstacles.indices step 3) {
                    if (i + 2 < obstacles.size) {
                        val pipeX = obstacles[i]
                        val gapY = obstacles[i+1]
                        val gapHeight = obstacles[i+2]

                        val topPipeBottom = gapY - gapHeight / 2
                        if (topPipeBottom > 0) {
                            drawRect(brush = pipeBrush, topLeft = Offset(pipeX, 0f), size = Size(pipeWidth, topPipeBottom - rimHeight))
                            drawRect(color = borderColor, topLeft = Offset(pipeX, 0f), size = Size(pipeWidth, topPipeBottom - rimHeight), style = Stroke(width = 6f))
                            drawRect(brush = rimBrush, topLeft = Offset(pipeX - rimOverhang, topPipeBottom - rimHeight), size = Size(pipeWidth + rimOverhang * 2, rimHeight))
                            drawRect(color = borderColor, topLeft = Offset(pipeX - rimOverhang, topPipeBottom - rimHeight), size = Size(pipeWidth + rimOverhang * 2, rimHeight), style = Stroke(width = 6f))
                        }

                        val bottomPipeTop = gapY + gapHeight / 2
                        drawRect(brush = pipeBrush, topLeft = Offset(pipeX, bottomPipeTop + rimHeight), size = Size(pipeWidth, 2000f - (bottomPipeTop + rimHeight)))
                        drawRect(color = borderColor, topLeft = Offset(pipeX, bottomPipeTop + rimHeight), size = Size(pipeWidth, 2000f - (bottomPipeTop + rimHeight)), style = Stroke(width = 6f))
                        drawRect(brush = rimBrush, topLeft = Offset(pipeX - rimOverhang, bottomPipeTop), size = Size(pipeWidth + rimOverhang * 2, rimHeight))
                        drawRect(color = borderColor, topLeft = Offset(pipeX - rimOverhang, bottomPipeTop), size = Size(pipeWidth + rimOverhang * 2, rimHeight), style = Stroke(width = 6f))
                    }
                }

                val visualBirdSize = 260
                drawImage(
                    image = birdSprites[currentFrameIndex],
                    dstOffset = IntOffset((300f - visualBirdSize/2).toInt(), (birdY - visualBirdSize/2).toInt()),
                    dstSize = IntSize(visualBirdSize, visualBirdSize)
                )

                drawRect(color = Color(0xFFDED895), topLeft = Offset(0f, 2000f), size = Size(size.width / scaleFactor, 200f))
                drawRect(color = Color(0xFF73BF2E), topLeft = Offset(0f, 2000f), size = Size(size.width / scaleFactor, 20f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "分數: $score",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = GameFont,
                modifier = Modifier.scale(scoreScale.value),
                style = TextStyle(shadow = Shadow(Color.Black, Offset(2f, 2f), 4f))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HP",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GameFont,
                    modifier = Modifier.padding(end = 8.dp),
                    style = TextStyle(shadow = Shadow(Color.Black, Offset(2f, 2f), 4f))
                )

                Box(
                    modifier = Modifier.width(200.dp).height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.5f))
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                ) {
                    val hpFraction = (currentHp / maxHp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(hpFraction)
                            .background(if (currentHp < 30) Color(0xFFFF4444) else Color(0xFF44FF44))
                    )
                    Text(
                        text = "$currentHp/$maxHp",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                        fontFamily = GameFont,
                        style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 2f))
                    )
                }
            }
        }

        if (showStartHint) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "對著麥克風發出聲音!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = GameFont,
                    style = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(4f, 4f), blurRadius = 8f))
                )
            }
        }

        if (isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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

                                if (3 in newlyUnlocked && currentScores.level3Score >= 3000) {
                                    gameRelatedAchievements.add("Voice Flight Ace／聲控飛行高手")
                                }

                                if (gameRelatedAchievements.isNotEmpty()) {
                                    newlyUnlockedAchievements = gameRelatedAchievements
                                    android.util.Log.d("Level3Result", "🎉 新解鎖成就: $gameRelatedAchievements")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Level3Result", "❌ 檢查成就失敗", e)
                            }
                        }
                    }

                    // ✅ 所有元素縮小 20%（原尺寸 × 0.8）
                    Text(
                        text = "遊戲結束!",
                        fontSize = 48.sp,  // 原 60sp × 0.8
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4444),
                        fontFamily = GameFont
                    )
                    Spacer(modifier = Modifier.height(19.dp))  // 原 24dp × 0.8
                    Text(
                        text = "血量耗盡",
                        fontSize = 22.sp,  // 原 28sp × 0.8
                        color = Color.Gray,
                        fontFamily = GameFont,
                        modifier = Modifier.padding(bottom = 13.dp)  // 原 16dp × 0.8
                    )
                    Text(
                        text = "最終分數: $score",
                        fontSize = 32.sp,  // 原 40sp × 0.8
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = GameFont
                    )

                    // ✅ 顯示新解鎖的成就（縮小 20%）
                    if (newlyUnlockedAchievements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(19.dp))  // 原 24dp × 0.8

                        newlyUnlockedAchievements.forEach { achievementName ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                modifier = Modifier.width(280.dp)  // 原 350dp × 0.8
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),  // 原 12dp × 0.8
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏆", fontSize = 19.sp)  // 原 24sp × 0.8
                                    Spacer(modifier = Modifier.width(10.dp))  // 原 12dp × 0.8
                                    Column {
                                        Text(
                                            "成就解鎖！",
                                            fontSize = 13.sp,  // 原 16sp × 0.8
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                        Text(
                                            achievementName,
                                            fontSize = 11.sp,  // 原 14sp × 0.8
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            if (achievementName != newlyUnlockedAchievements.last()) {
                                Spacer(modifier = Modifier.height(6.dp))  // 原 8dp × 0.8
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(38.dp))  // 原 48dp × 0.8

                    Button(
                        onClick = {
                            if (isNavigating) return@Button
                            isNavigating = true
                            soundManager.playSFX("cancel")
                            onNavigateBack()
                        },
                        modifier = Modifier.size(width = 160.dp, height = 48.dp),  // 原 200dp × 60dp × 0.8
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            disabledContainerColor = Color.DarkGray.copy(alpha = 0.7f)
                        ),
                        enabled = !isNavigating
                    ) {
                        Text(text = "回主選單", fontSize = 16.sp, fontFamily = GameFont)  // 原 20sp × 0.8
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    if (isNavigating) return@Button
                    isNavigating = true
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                },
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                enabled = !isNavigating,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            ) {
                Text(text = "退出", fontFamily = GameFont)
            }
        }
    }
}