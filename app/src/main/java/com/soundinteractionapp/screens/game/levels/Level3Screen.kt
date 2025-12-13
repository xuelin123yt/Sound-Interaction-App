package com.soundinteractionapp.screens.game.levels

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.MediaPlayer
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
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ 新增 import
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
import com.soundinteractionapp.data.RankingViewModel // ✅ 新增 import

// --- 背景影片組件 (保持不變) ---
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
    // ✅ 新增：注入 ViewModel (預設使用 viewModel() 獲取)
    rankingViewModel: RankingViewModel = viewModel()
) {
    val context = LocalContext.current

    // --- 遊戲狀態變數 ---
    var birdY by remember { mutableFloatStateOf(500f) }
    var score by remember { mutableIntStateOf(0) }
    var currentHp by remember { mutableIntStateOf(100) }
    var isGameOver by remember { mutableStateOf(false) }

    var obstacles by remember { mutableStateOf(floatArrayOf()) }
    var isPlaying by remember { mutableStateOf(true) }

    // 其他 UI 狀態
    var permissionGranted by remember { mutableStateOf(false) }
    var showStartHint by remember { mutableStateOf(true) }
    val maxHp = 100

    // 字體
    val GameFont = FontFamily(Font(R.font.huninn))
    val scoreScale = remember { Animatable(1f) }

    // 分數動畫
    LaunchedEffect(score) {
        if (score > 0) {
            scoreScale.snapTo(1f)
            scoreScale.animateTo(1.5f, animationSpec = tween(100))
            scoreScale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
        }
    }

    // ✅ 新增：監聽遊戲結束狀態並儲存分數
    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            // ID 3 代表關卡 3，會自動處理訪客/會員邏輯
            rankingViewModel.onGameFinished(levelId = 3, finalScore = score)
            Log.d("Level3", "遊戲結束，嘗試更新分數: $score")
        }
    }

    // --- 背景音樂 ---
    val musicList = listOf(R.raw.music1, R.raw.music2, R.raw.music3)
    val randomMusicResId = remember { musicList.random() }
    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, randomMusicResId)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.6f, 0.6f)
        mediaPlayer.start()
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // --- 音效 ---
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
    val pipeSoundId = remember { soundPool.load(context, R.raw.pipe_music, 1) }
    val hitSoundId = remember { soundPool.load(context, R.raw.pipe_music, 1) }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    // --- 鳥動畫 ---
    val birdSprites = listOf(
        ImageBitmap.imageResource(id = R.drawable.bird_1),
        ImageBitmap.imageResource(id = R.drawable.bird_2),
        ImageBitmap.imageResource(id = R.drawable.bird_3)
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameCounter by remember { mutableIntStateOf(0) }

    // --- 權限與初始化 ---
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

    // --- 錄音 ---
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
                } catch (e: Exception) { Log.e("Mic", "Error: ${e.message}") }
                finally { audioRecord.stop(); audioRecord.release() }
            }
        }
    }

    // --- 核心遊戲迴圈 ---
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            birdY = GameEngine.updateGame()
            obstacles = GameEngine.getObstacleData()
            val state = GameEngine.getGameState()
            val newScore = state[0].toInt()
            val gameOverFlag = state[2] > 0.5f
            val newHp = if (state.size >= 5) state[4].toInt() else currentHp

            if (newScore > score) {
                soundPool.play(pipeSoundId, 0.3f, 0.3f, 1, 0, 1f)
            }

            if (newHp < currentHp) {
                soundPool.play(hitSoundId, 1.0f, 1.0f, 1, 0, 0.6f)
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

                        // 上管
                        val topPipeBottom = gapY - gapHeight / 2
                        if (topPipeBottom > 0) {
                            drawRect(brush = pipeBrush, topLeft = Offset(pipeX, 0f), size = Size(pipeWidth, topPipeBottom - rimHeight))
                            drawRect(color = borderColor, topLeft = Offset(pipeX, 0f), size = Size(pipeWidth, topPipeBottom - rimHeight), style = Stroke(width = 6f))
                            drawRect(brush = rimBrush, topLeft = Offset(pipeX - rimOverhang, topPipeBottom - rimHeight), size = Size(pipeWidth + rimOverhang * 2, rimHeight))
                            drawRect(color = borderColor, topLeft = Offset(pipeX - rimOverhang, topPipeBottom - rimHeight), size = Size(pipeWidth + rimOverhang * 2, rimHeight), style = Stroke(width = 6f))
                        }

                        // 下管
                        val bottomPipeTop = gapY + gapHeight / 2
                        drawRect(brush = pipeBrush, topLeft = Offset(pipeX, bottomPipeTop + rimHeight), size = Size(pipeWidth, 2000f - (bottomPipeTop + rimHeight)))
                        drawRect(color = borderColor, topLeft = Offset(pipeX, bottomPipeTop + rimHeight), size = Size(pipeWidth, 2000f - (bottomPipeTop + rimHeight)), style = Stroke(width = 6f))
                        drawRect(brush = rimBrush, topLeft = Offset(pipeX - rimOverhang, bottomPipeTop), size = Size(pipeWidth + rimOverhang * 2, rimHeight))
                        drawRect(color = borderColor, topLeft = Offset(pipeX - rimOverhang, bottomPipeTop), size = Size(pipeWidth + rimOverhang * 2, rimHeight), style = Stroke(width = 6f))
                    }
                }

                // 畫鳥
                val visualBirdSize = 260
                drawImage(
                    image = birdSprites[currentFrameIndex],
                    dstOffset = IntOffset((300f - visualBirdSize/2).toInt(), (birdY - visualBirdSize/2).toInt()),
                    dstSize = IntSize(visualBirdSize, visualBirdSize)
                )

                // 畫地板
                drawRect(color = Color(0xFFDED895), topLeft = Offset(0f, 2000f), size = Size(size.width / scaleFactor, 200f))
                drawRect(color = Color(0xFF73BF2E), topLeft = Offset(0f, 2000f), size = Size(size.width / scaleFactor, 20f))
            }
        }

        // HUD
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

        // 結算畫面
        if (isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = "遊戲結束!", fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4444), fontFamily = GameFont)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "血量耗盡", fontSize = 28.sp, color = Color.Gray, fontFamily = GameFont, modifier = Modifier.padding(bottom = 16.dp))
                    Text(text = "最終分數: $score", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = GameFont)
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(width = 200.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(text = "回主選單", fontSize = 20.sp, fontFamily = GameFont)
                    }
                }
            }
        } else {
            Button(onClick = onNavigateBack, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(text = "退出", fontFamily = GameFont)
            }
        }
    }
}