package com.soundinteractionapp.screens.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import com.soundinteractionapp.R
import com.soundinteractionapp.Screen
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.screens.game.levels.RankingDialogContent
import kotlin.math.absoluteValue

// =====================================================
// 🎵 簡易版 SoundManager
// =====================================================
class GameModeSoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMap[R.raw.options2] = soundPool.load(context, R.raw.options2, 1)
        soundMap[R.raw.cancel] = soundPool.load(context, R.raw.cancel, 1)
    }

    fun play(soundResId: Int) {
        soundMap[soundResId]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

// =====================================================
// 📦 關卡資料結構
// =====================================================
data class LevelData(
    val id: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// =====================================================
// 🎮 遊戲訓練模式主畫面（修復版）
// =====================================================
@Composable
fun GameModeScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToLevel: (String) -> Unit,
    rankingViewModel: RankingViewModel
) {
    val context = LocalContext.current
    val soundManager = remember { GameModeSoundManager(context) }

    var showRankingDialog by remember { mutableStateOf(false) }

    // 🔥 防止快速點擊導致白屏
    var isNavigating by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val levels = listOf(
        LevelData(1, "跟著按按鈕", "聽節奏,跟著按", Icons.Filled.PanTool, Color(0xFFFF7043), Screen.GameLevel1.route),
        LevelData(2, "找出小動物", "是誰在發出聲音?", Icons.Filled.Pets, Color(0xFF42A5F5), Screen.GameLevel2.route),
        LevelData(3, "聲控鳥飛行", "利用聲音控制鳥兒", Icons.Filled.GraphicEq, Color(0xFF66BB6A), Screen.GameLevel3.route),
        LevelData(4, "創作小樂曲", "自由發揮你的創意", Icons.Filled.MusicNote, Color(0xFFAB47BC), Screen.GameLevel4.route)
    )

    var currentIndex by remember { mutableStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isNavigating) return@Button
                        isNavigating = true
                        soundManager.play(R.raw.cancel)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isNavigating
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("返回", style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    "選擇遊戲關卡",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        if (!isNavigating) {
                            showRankingDialog = true
                        }
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "查看排名",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                VerticalSwipeableCardCarousel(
                    soundManager = soundManager,
                    levels = levels,
                    currentIndex = currentIndex,
                    onIndexChange = { currentIndex = it },
                    onLevelClick = { route ->
                        if (isNavigating) return@VerticalSwipeableCardCarousel
                        soundManager.play(R.raw.options2)
                        onNavigateToLevel(route)
                    }
                )

                if (showRankingDialog) {
                    Dialog(onDismissRequest = { showRankingDialog = false }) {
                        RankingDialogContent(
                            onClose = { showRankingDialog = false },
                            rankingViewModel = rankingViewModel
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// ↕️ 垂直卡片輪播核心邏輯
// =====================================================
@Composable
fun VerticalSwipeableCardCarousel(
    soundManager: GameModeSoundManager,
    levels: List<LevelData>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onLevelClick: (String) -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        finishedListener = { isAnimating = false }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(currentIndex) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (!isAnimating) {
                            if (offsetY < -100 && currentIndex < levels.size - 1) {
                                isAnimating = true
                                soundManager.play(R.raw.options2)
                                onIndexChange(currentIndex + 1)
                            } else if (offsetY > 100 && currentIndex > 0) {
                                isAnimating = true
                                soundManager.play(R.raw.options2)
                                onIndexChange(currentIndex - 1)
                            }
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (!isAnimating) {
                            offsetY = (offsetY + dragAmount * 0.7f).coerceIn(-300f, 300f)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        levels.forEachIndexed { index, level ->
            val indexOffset = index - currentIndex

            if (indexOffset in -2..2) {
                LevelCardSwiper(
                    level = level,
                    offset = indexOffset,
                    dragOffset = animatedOffset,
                    isCenter = indexOffset == 0,
                    onClick = { onLevelClick(level.route) }
                )
            }
        }
    }
}

// =====================================================
// 🃏 單張關卡卡片 UI
// =====================================================
@Composable
fun LevelCardSwiper(
    level: LevelData,
    offset: Int,
    dragOffset: Float,
    isCenter: Boolean,
    onClick: () -> Unit
) {
    val spacing = 260f
    val translationY = offset * spacing + dragOffset
    val scaleTarget = if (isCenter) 1f else 0.85f
    val scale by animateFloatAsState(scaleTarget, tween(300))
    val alpha = (1f - (offset.absoluteValue * 0.4f)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .zIndex(-offset.absoluteValue.toFloat())
            .graphicsLayer {
                this.translationY = translationY
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
                this.rotationX = (translationY / 20f).coerceIn(-10f, 10f) * -1
                this.cameraDistance = 12 * density
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (isCenter) 12.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(level.color.copy(0.2f), level.color.copy(0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = level.icon,
                    contentDescription = null,
                    tint = level.color,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "關卡 ${level.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (isCenter) onClick() },
                    enabled = isCenter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = level.color
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("開始挑戰", color = Color.White)
                }
            }
        }
    }
}