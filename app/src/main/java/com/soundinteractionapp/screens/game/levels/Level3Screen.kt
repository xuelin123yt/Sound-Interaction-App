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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.font.Font // ★ 引入 Font
import androidx.compose.ui.text.font.FontFamily // ★ 引入 FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
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
fun Level3PitchScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var birdY by remember { mutableStateOf(500f) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(180) }
    var isVictory by remember { mutableStateOf(false) }
    var obstacles by remember { mutableStateOf(floatArrayOf()) }
    var isPlaying by remember { mutableStateOf(true) }
    var permissionGranted by remember { mutableStateOf(false) }

    var showStartHint by remember { mutableStateOf(true) }

    // ★ 定義自訂字體 (假設檔名是 huninn.ttf)
    // 如果你的檔名不同，請修改 R.font.你的檔名
    val GameFont = FontFamily(Font(R.font.huninn))

    val scoreScale = remember { Animatable(1f) }

    LaunchedEffect(score) {
        if (score > 0) {
            scoreScale.snapTo(1f)
            scoreScale.animateTo(1.5f, animationSpec = tween(100))
            scoreScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
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

    // --- 過關音效 ---
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

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    // --- 動畫 ---
    val birdSprites = listOf(
        ImageBitmap.imageResource(id = R.drawable.bird_1),
        ImageBitmap.imageResource(id = R.drawable.bird_2),
        ImageBitmap.imageResource(id = R.drawable.bird_3)
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var frameCounter by remember { mutableIntStateOf(0) }

    // --- 權限 ---
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> permissionGranted = isGranted }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
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

    // --- 遊戲迴圈 ---
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            birdY = GameEngine.updateGame()
            obstacles = GameEngine.getObstacleData()
            val state = GameEngine.getGameState()

            val newScore = state[0].toInt()
            if (newScore > score) {
                soundPool.play(pipeSoundId, 0.3f, 0.3f, 1, 0, 1f)
            }
            score = newScore

            timeLeft = state[1].toInt()

            if (state[3] == 1.0f) {
                isVictory = true
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

        // 1. 背景影片
        VideoBackground(videoResId = R.raw.sky)

        // 2. 遊戲畫布
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
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
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "分數: $score",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = GameFont, // ★ 套用字體
                modifier = Modifier.scale(scoreScale.value)
            )
            Text(
                text = "時間: ${timeLeft}s",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = GameFont // ★ 套用字體
            )
        }

        // 開場提示
        if (showStartHint) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "對著麥克風發出聲音!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = GameFont, // ★ 套用字體
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black, offset = Offset(4f, 4f), blurRadius = 8f)
                    )
                )
            }
        }

        // 結算畫面
        if (isVictory) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "結束!",
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow,
                        fontFamily = GameFont // ★ 套用字體
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "最終分數: $score",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = GameFont // ★ 套用字體
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(width = 200.dp, height = 60.dp)
                    ) {
                        Text(
                            text = "回主選單",
                            fontSize = 20.sp,
                            fontFamily = GameFont // ★ 套用字體
                        )
                    }
                }
            }
        } else {
            Button(onClick = onNavigateBack, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(
                    text = "退出",
                    fontFamily = GameFont // ★ 套用字體
                )
            }
        }
    }
}