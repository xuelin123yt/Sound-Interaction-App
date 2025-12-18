package com.soundinteractionapp.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import kotlin.math.absoluteValue

// =====================================================
// 📦 資料結構
// =====================================================
data class FreePlayItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val soundResId: Int,
    val onNavigate: () -> Unit
)

// =====================================================
// 🎮 自由探索模式主畫面（修復版）
// =====================================================
@Composable
fun FreePlayScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToCatInteraction: () -> Unit,
    onNavigateToPianoInteraction: () -> Unit,
    onNavigateToDogInteraction: () -> Unit,
    onNavigateToBirdInteraction: () -> Unit,
    onNavigateToDrumInteraction: () -> Unit,
    onNavigateToBellInteraction: () -> Unit
) {
    // 🔥 防止快速點擊導致白屏
    var isNavigating by remember { mutableStateOf(false) }

    // 定義資料
    val items = listOf(
        FreePlayItem(0, "貓咪", "可愛的喵喵聲", "🐾", Color(0xFFFFCC80), R.raw.cat_meow, onNavigateToCatInteraction),
        FreePlayItem(1, "狗狗", "忠誠的汪汪聲", "🐕", Color(0xFFA1887F), R.raw.dog_barking, onNavigateToDogInteraction),
        FreePlayItem(2, "鳥兒", "清脆的啾啾聲", "🐦", Color(0xFF81D4FA), R.raw.bird_sound, onNavigateToBirdInteraction),
        FreePlayItem(3, "鋼琴", "優美的琴聲", "🎹", Color(0xFF9FA8DA), R.raw.piano_c1, onNavigateToPianoInteraction),
        FreePlayItem(4, "爵士鼓", "動感的節奏", "🥁", Color(0xFFEF9A9A), R.raw.drum_cymbal_closed, onNavigateToDrumInteraction),
        FreePlayItem(5, "鈴鐺", "響亮的叮噹聲", "🔔", Color(0xFFFFF59D), R.raw.desk_bell, onNavigateToBellInteraction)
    )

    var currentIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. 頂部導航列
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
                        soundManager.playSound(R.raw.cancel)
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
                    "自由探索模式",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(100.dp))
            }

            // 2. 垂直滾動區域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                VerticalFreePlayCarousel(
                    soundManager = soundManager,
                    items = items,
                    currentIndex = currentIndex,
                    onIndexChange = { currentIndex = it },
                    onItemClick = { item ->
                        if (isNavigating) return@VerticalFreePlayCarousel
                        soundManager.playSound(item.soundResId)
                        item.onNavigate()
                    }
                )
            }
        }
    }
}

// =====================================================
// ↕️ 垂直輪播邏輯
// =====================================================
@Composable
fun VerticalFreePlayCarousel(
    soundManager: SoundManager,
    items: List<FreePlayItem>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onItemClick: (FreePlayItem) -> Unit
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
            .fillMaxSize()
            .pointerInput(currentIndex) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (!isAnimating) {
                            if (offsetY < -100 && currentIndex < items.size - 1) {
                                isAnimating = true
                                soundManager.playSound(R.raw.options2)
                                onIndexChange(currentIndex + 1)
                            } else if (offsetY > 100 && currentIndex > 0) {
                                isAnimating = true
                                soundManager.playSound(R.raw.options2)
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
        items.forEachIndexed { index, item ->
            val indexOffset = index - currentIndex
            if (indexOffset in -2..2) {
                FreePlayCard(
                    item = item,
                    offset = indexOffset,
                    dragOffset = animatedOffset,
                    isCenter = indexOffset == 0,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

// =====================================================
// 🃏 單張卡片 UI
// =====================================================
@Composable
fun FreePlayCard(
    item: FreePlayItem,
    offset: Int,
    dragOffset: Float,
    isCenter: Boolean,
    onClick: () -> Unit
) {
    val spacing = 260f
    val translationY = offset * spacing + dragOffset
    val scale by animateFloatAsState(if (isCenter) 1f else 0.85f, tween(300))
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
        elevation = CardDefaults.cardElevation(if (isCenter) 10.dp else 2.dp)
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
                            listOf(item.color.copy(0.3f), item.color.copy(0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (isCenter) onClick() },
                    enabled = isCenter,
                    colors = ButtonDefaults.buttonColors(containerColor = item.color),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("進入互動", color = Color.Black)
                }
            }
        }
    }
}