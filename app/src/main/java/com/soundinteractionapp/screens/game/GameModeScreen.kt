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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.soundinteractionapp.R
import com.soundinteractionapp.Screen
import kotlin.math.absoluteValue
////////////////////////新增////////////////////////
import androidx.compose.material.icons.filled.EmojiEvents // 獎盃圖示
import androidx.compose.ui.window.Dialog
import com.soundinteractionapp.screens.game.levels.RankingDialogContent

////////////////////////新增////////////////////////

// =====================================================
// 🎵 簡易版 SoundManager (如果你的專案已有全域的，可直接引用)
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

        // 載入需要的音效
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
    val description: String, // 增加描述，讓畫面豐富一點
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// =====================================================
// 🎮 遊戲訓練模式主畫面 (Vertical Carousel 版本)
// =====================================================
@Composable
fun GameModeScreenContent(onNavigateBack: () -> Unit, onNavigateToLevel: (String) -> Unit) {
    val context = LocalContext.current
    // 初始化音效管理器
    val soundManager = remember { GameModeSoundManager(context) }

    var showRankingDialog by remember { mutableStateOf(false) }

    // 記得釋放資源
    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    // 定義關卡資料
    val levels = listOf(
        LevelData(1, "跟著按按鈕", "聽節奏，跟著按", Icons.Filled.PanTool, Color(0xFFFF7043), Screen.GameLevel1.route),
        LevelData(2, "找出小動物", "是誰在發出聲音？", Icons.Filled.Pets, Color(0xFF42A5F5), Screen.GameLevel2.route),
        LevelData(3, "聲控鳥飛行", "利用聲音控制鳥兒", Icons.Filled.GraphicEq, Color(0xFF66BB6A), Screen.GameLevel3.route),
        LevelData(4, "創作小樂曲", "自由發揮你的創意", Icons.Filled.MusicNote, Color(0xFFAB47BC), Screen.GameLevel4.route)
    )

    var currentIndex by remember { mutableStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {

            // -------------------------------------------------
            // 1. 頂部標題列 (保持原有設計)
            // -------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        soundManager.play(R.raw.cancel)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp)
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

                ////////////////////////新增////////////////////////
                // 【替換】右側：新增的排名圖示按鈕 (獎盃)
                IconButton(
                    onClick = { showRankingDialog = true }, // 點擊時呼叫導航
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents, // 使用獎盃圖示
                        contentDescription = "查看排名",
                        tint = MaterialTheme.colorScheme.primary, // 使用主題色
                        modifier = Modifier.size(32.dp)
                    )
                }
                ////////////////////////新增/////////////////////////
            }

            // -------------------------------------------------
            // 2. 垂直滾動選單區域
            // -------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 佔據剩餘空間
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                VerticalSwipeableCardCarousel(
                    soundManager = soundManager,
                    levels = levels,
                    currentIndex = currentIndex,
                    onIndexChange = { currentIndex = it },
                    onLevelClick = { route ->
                        soundManager.play(R.raw.options2) // 或是其他的確認音效
                        onNavigateToLevel(route)
                    }
                )

                ////////////////////////新增////////////////////////
                if (showRankingDialog) {
                    // 使用 Dialog 元件
                    Dialog(onDismissRequest = { showRankingDialog = false }) {
                        // 呼叫排名內容畫面，並傳遞關閉視窗的動作
                        RankingDialogContent(
                            onClose = { showRankingDialog = false } // 傳遞關閉自身的操作
                        )
                    }
                }
                ////////////////////////新增////////////////////////
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

    // 動畫補間，讓拖曳放開後平滑歸位
    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        finishedListener = { isAnimating = false }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() // 充滿整個區域
            .pointerInput(currentIndex) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (!isAnimating) {
                            // 向上拖曳 (下一頁)
                            if (offsetY < -100 && currentIndex < levels.size - 1) {
                                isAnimating = true
                                soundManager.play(R.raw.options2)
                                onIndexChange(currentIndex + 1)
                            }
                            // 向下拖曳 (上一頁)
                            else if (offsetY > 100 && currentIndex > 0) {
                                isAnimating = true
                                soundManager.play(R.raw.options2)
                                onIndexChange(currentIndex - 1)
                            }
                            // 無論是否換頁，位移量都歸零 (由 animatedOffset 處理動畫)
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (!isAnimating) {
                            // 限制最大拖曳距離，避免卡片飛太遠
                            offsetY = (offsetY + dragAmount * 0.7f).coerceIn(-300f, 300f)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 渲染卡片：只渲染當前、上一個、下一個，節省資源
        levels.forEachIndexed { index, level ->
            val indexOffset = index - currentIndex

            // 只顯示附近的卡片 (例如前後各 2 張)，避免渲染所有列表
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
    // 1. 稍微加大一點間距，讓卡片不要黏太緊 (原本 240f -> 改成 260f 或更多)
    val cardHeight = 220f
    val spacing = 260f

    val translationY = offset * spacing + dragOffset
    val scaleTarget = if (isCenter) 1f else 0.85f
    val scale by animateFloatAsState(scaleTarget, tween(300))
    val alpha = (1f - (offset.absoluteValue * 0.4f)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            // -------------------------------------------------------------
            // 🔥 重點修正：設定 Z-Index
            // 絕對值越小 (越接近 0)，層級越高。
            // 我們取負的絕對值，這樣 0 (中間) = 0 (最高)，1 或 -1 = -1 (較低)
            // -------------------------------------------------------------
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
        // ... (卡片內部內容保持不變) ...
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
