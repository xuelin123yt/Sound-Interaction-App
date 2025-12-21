package com.soundinteractionapp.screens.relax

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
data class RelaxMenuItem(
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
fun textStrokeStyleRelax(fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight = FontWeight.Normal): TextStyle {
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
// 🎮 放鬆時光模式主畫面（太鼓風格）
// =====================================================
@Composable
fun RelaxScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToOceanInteraction: () -> Unit,
    onNavigateToRainInteraction: () -> Unit,
    onNavigateToWindInteraction: () -> Unit
) {
    var isNavigating by remember { mutableStateOf(false) }

    val menuItems = remember {
        listOf(
            RelaxMenuItem(
                id = 0,
                categoryName = "雨聲",
                icon = "🌧️",
                primaryColor = Color(0xFF81D4FA),
                secondaryColor = Color(0xFFB3E5FC),
                description = "舒緩的雨滴聲",
                soundResId = R.raw.rain_sound,
                onNavigate = onNavigateToRainInteraction
            ),
            RelaxMenuItem(
                id = 1,
                categoryName = "海浪",
                icon = "🌊",
                primaryColor = Color(0xFF4FC3F7),
                secondaryColor = Color(0xFF81D4FA),
                description = "平靜的海浪聲",
                soundResId = R.raw.wave_sound,
                onNavigate = onNavigateToOceanInteraction
            ),
            RelaxMenuItem(
                id = 2,
                categoryName = "微風",
                icon = "🍃",
                primaryColor = Color(0xFF81C784),
                secondaryColor = Color(0xFFA5D6A7),
                description = "輕柔的風聲",
                soundResId = R.raw.wind_sound,
                onNavigate = onNavigateToWindInteraction
            )
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F8FF)
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
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White,  // ✅ 新增這行
                        disabledContainerColor = Color(0xFF1976D2).copy(alpha = 0.7f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)  // ✅ 新增這行
                    ),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isNavigating
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)  // ✅ 加 tint
                    Spacer(Modifier.width(8.dp))
                    Text("返回", style = MaterialTheme.typography.titleMedium, color = Color.White)  // ✅ 加 color
                }

                Text(
                    "🌊 放鬆時光模式 🌊",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1976D2),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(100.dp))
            }

            // 橫向選單區域 - 置中對齊
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(menuItems) { index, item ->
                        RelaxMenuItemCard(
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
fun RelaxMenuItemCard(
    item: RelaxMenuItem,
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
            .height(340.dp)
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
            ExpandedContentRelax(item)
        } else {
            CollapsedContentRelax(item)
        }
    }
}

// =====================================================
// 📖 展開狀態內容
// =====================================================
@Composable
fun ExpandedContentRelax(item: RelaxMenuItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = item.icon,
            fontSize = 64.sp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.categoryName,
                style = textStrokeStyleRelax(22.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = textStrokeStyleRelax(14.sp),
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
fun CollapsedContentRelax(item: RelaxMenuItem) {
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
                style = textStrokeStyleRelax(20.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}