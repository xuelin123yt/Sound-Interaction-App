package com.soundinteractionapp.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager

// =====================================================
// 📦 資料模型
// =====================================================
data class TaikoMenuItem(
    val id: Int,
    val categoryName: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String,
    val soundResId: Int,
    val onNavigate: () -> Unit
)

// =====================================================
// 🎨 文字描邊效果
// =====================================================
fun textStrokeStyle(fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight = FontWeight.Normal): TextStyle {
    return TextStyle(
        fontSize = fontSize,
        fontWeight = fontWeight,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(3f, 3f),
            blurRadius = 1f
        )
    )
}

// =====================================================
// 🎮 自由探索模式主畫面（太鼓風格）
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
    var isNavigating by remember { mutableStateOf(false) }

    val menuItems = remember {
        listOf(
            TaikoMenuItem(
                id = 0,
                categoryName = "貓咪",
                icon = "🐾",
                primaryColor = Color(0xFFFFCC80),
                secondaryColor = Color(0xFFFFE0B2),
                description = "可愛的喵喵聲",
                soundResId = R.raw.cat_meow,
                onNavigate = onNavigateToCatInteraction
            ),
            TaikoMenuItem(
                id = 1,
                categoryName = "狗狗",
                icon = "🐕",
                primaryColor = Color(0xFFA1887F),
                secondaryColor = Color(0xFFBCAAA4),
                description = "忠誠的汪汪聲",
                soundResId = R.raw.dog_barking,
                onNavigate = onNavigateToDogInteraction
            ),
            TaikoMenuItem(
                id = 2,
                categoryName = "鳥兒",
                icon = "🐦",
                primaryColor = Color(0xFF81D4FA),
                secondaryColor = Color(0xFFB3E5FC),
                description = "清脆的啾啾聲",
                soundResId = R.raw.bird_sound,
                onNavigate = onNavigateToBirdInteraction
            ),
            TaikoMenuItem(
                id = 3,
                categoryName = "鋼琴",
                icon = "🎹",
                primaryColor = Color(0xFF9FA8DA),
                secondaryColor = Color(0xFFC5CAE9),
                description = "優美的琴聲",
                soundResId = R.raw.piano_c1,
                onNavigate = onNavigateToPianoInteraction
            ),
            TaikoMenuItem(
                id = 4,
                categoryName = "爵士鼓",
                icon = "🥁",
                primaryColor = Color(0xFFEF9A9A),
                secondaryColor = Color(0xFFFFCDD2),
                description = "動感的節奏",
                soundResId = R.raw.drum_cymbal_closed,
                onNavigate = onNavigateToDrumInteraction
            ),
            TaikoMenuItem(
                id = 5,
                categoryName = "鈴鐺",
                icon = "🔔",
                primaryColor = Color(0xFFFFF59D),
                secondaryColor = Color(0xFFFFF9C4),
                description = "響亮的叮噹聲",
                soundResId = R.raw.desk_bell,
                onNavigate = onNavigateToBellInteraction
            )
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF5E6)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 頂部導航列
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
                        soundManager.playSFX("cancel")
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        disabledContainerColor = Color(0xFFD32F2F).copy(alpha = 0.7f)
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
                    "🥁 自由探索模式 🥁",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD32F2F),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(100.dp))
            }

            // 橫向選單區域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(400.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(menuItems) { index, item ->
                            TaikoMenuItemCard(
                                item = item,
                                isSelected = selectedIndex == index,
                                onClick = {
                                    if (selectedIndex == index) {
                                        // 點擊已選中項目 -> 進入互動
                                        if (!isNavigating) {
                                            isNavigating = true
                                            item.onNavigate()
                                        }
                                    } else {
                                        // 點擊其他項目 -> 展開
                                        soundManager.playSFX("options2")
                                        selectedIndex = index
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 提示文字
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedIndex != null) "再次點擊進入互動" else "點擊項目展開詳細資訊",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =====================================================
// 🎴 單一選單項目卡片
// =====================================================
@Composable
fun TaikoMenuItemCard(
    item: TaikoMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 280.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "width"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(400.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        item.primaryColor,
                        item.secondaryColor
                    )
                )
            )
            .border(
                width = 4.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        if (isSelected) {
            ExpandedContent(item)
        } else {
            CollapsedContent(item)
        }
    }
}

// =====================================================
// 📖 展開狀態內容
// =====================================================
@Composable
fun ExpandedContent(item: TaikoMenuItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = item.icon,
            fontSize = 80.sp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.categoryName,
                style = textStrokeStyle(26.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = textStrokeStyle(15.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// =====================================================
// 📝 收合狀態內容
// =====================================================
@Composable
fun CollapsedContent(item: TaikoMenuItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.icon,
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        item.categoryName.forEach { char ->
            Text(
                text = char.toString(),
                style = textStrokeStyle(20.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}