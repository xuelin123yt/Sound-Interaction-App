package com.soundinteractionapp.screens.game

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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import com.soundinteractionapp.R
import com.soundinteractionapp.Screen
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.screens.game.levels.RankingDialogContent

// =====================================================
// 📦 資料模型
// =====================================================
data class GameMenuItem(
    val id: Int,
    val categoryName: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String,
    val route: String
)

// =====================================================
// 🎨 文字描邊效果 (遊戲模式專用)
// =====================================================
fun gameTextStrokeStyle(fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight = FontWeight.Normal): TextStyle {
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
// 🎮 遊戲訓練模式主畫面（太鼓風格）
// =====================================================
@Composable
fun GameModeScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToLevel: (String) -> Unit,
    rankingViewModel: RankingViewModel,
    soundManager: SoundManager
) {
    var isNavigating by remember { mutableStateOf(false) }
    var showRankingDialog by remember { mutableStateOf(false) }

    val menuItems = remember {
        listOf(
            GameMenuItem(
                id = 1,
                categoryName = "料理鼠王",
                icon = "✋",
                primaryColor = Color(0xFFFF7043),
                secondaryColor = Color(0xFFFFAB91),
                description = "聽節奏，打老鼠",
                route = Screen.GameLevel1.route
            ),
            GameMenuItem(
                id = 2,
                categoryName = "鋼琴演奏",
                icon = "🐾",
                primaryColor = Color(0xFF42A5F5),
                secondaryColor = Color(0xFF90CAF9),
                description = "利用鋼琴創造舞台",
                route = Screen.GameLevel2.route
            ),
            GameMenuItem(
                id = 3,
                categoryName = "聲控飛行",
                icon = "🐦",
                primaryColor = Color(0xFF66BB6A),
                secondaryColor = Color(0xFFA5D6A7),
                description = "利用聲音控制鳥兒",
                route = Screen.GameLevel3.route
            ),
            GameMenuItem(
                id = 4,
                categoryName = "音樂節奏",
                icon = "🎵",
                primaryColor = Color(0xFFAB47BC),
                secondaryColor = Color(0xFFCE93D8),
                description = "隨著音樂一起消散吧",
                route = Screen.GameLevel4.route
            )
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF9E6)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 頂部導航列
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 左側返回按鈕
                Button(
                    onClick = {
                        if (isNavigating) return@Button
                        isNavigating = true
                        soundManager.playSFX("cancel")
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE65100),
                        contentColor = Color.White,  // ✅ 新增這行
                        disabledContainerColor = Color(0xFFE65100).copy(alpha = 0.7f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)  // ✅ 新增這行
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .align(Alignment.CenterStart),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isNavigating
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)  // ✅ 加 tint
                    Spacer(Modifier.width(8.dp))
                    Text("返回", style = MaterialTheme.typography.titleMedium, color = Color.White)  // ✅ 加 color
                }

                // 中央標題
                Text(
                    "🎮 遊戲訓練模式 🎮",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE65100),
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // 右側排名按鈕
                IconButton(
                    onClick = {
                        if (!isNavigating) {
                            showRankingDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "查看排名",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 橫向選單區域 - 置中對齊
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
                            .height(350.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(menuItems) { index, item ->
                            GameMenuItemCard(
                                item = item,
                                isSelected = selectedIndex == index,
                                onClick = {
                                    if (selectedIndex == index) {
                                        // 點擊已選中項目 -> 進入關卡
                                        if (!isNavigating) {
                                            isNavigating = true
                                            soundManager.playSFX("options2")
                                            onNavigateToLevel(item.route)
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
                    text = if (selectedIndex != null) "再次點擊開始挑戰" else "點擊項目展開詳細資訊",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // 排名對話框
    if (showRankingDialog) {
        Dialog(onDismissRequest = { showRankingDialog = false }) {
            RankingDialogContent(
                onClose = { showRankingDialog = false },
                rankingViewModel = rankingViewModel
            )
        }
    }
}

// =====================================================
// 🎴 單一選單項目卡片
// =====================================================
@Composable
fun GameMenuItemCard(
    item: GameMenuItem,
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
            GameExpandedContent(item)
        } else {
            GameCollapsedContent(item)
        }
    }
}

// =====================================================
// 📖 展開狀態內容
// =====================================================
@Composable
fun GameExpandedContent(item: GameMenuItem) {
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
                text = "關卡 ${item.id}",
                style = gameTextStrokeStyle(16.sp, FontWeight.Bold),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.categoryName,
                style = gameTextStrokeStyle(22.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = gameTextStrokeStyle(14.sp),
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
fun GameCollapsedContent(item: GameMenuItem) {
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
                style = gameTextStrokeStyle(20.sp, FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}