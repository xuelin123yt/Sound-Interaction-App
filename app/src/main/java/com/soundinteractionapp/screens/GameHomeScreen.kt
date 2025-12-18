package com.soundinteractionapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset as GeometryOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.ProfileViewModel
import kotlin.math.absoluteValue

// =====================================================
// 🏠 主畫面 GameHomeScreen
// =====================================================
@Composable
fun GameHomeScreen(
    soundManager: SoundManager,
    onNavigateToFreePlay: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    // 登出狀態控制
    var isLoggingOut by remember { mutableStateOf(false) }

    // 黑屏動畫（700ms 淡入）
    val blackAlpha by animateFloatAsState(
        targetValue = if (isLoggingOut) 1f else 0f,
        animationSpec = tween(700),
        finishedListener = {
            if (isLoggingOut) {
                onLogout()
            }
        }
    )

    var currentIndex by remember { mutableStateOf(0) } // 改為 0，預設顯示第一張卡片（模式一）

    // 定義卡片資料 (包含排行榜)
    val modes = listOf(
        ModeData(
            title = "自由探索",
            subtitle = "模式一",
            description = "自由觸碰螢幕,探索各種聲音與互動",
            iconResId = R.drawable.music_01,
            color = Color(0xFF8C7AE6),
            onClick = {
                soundManager.playSFX("options3")
                onNavigateToFreePlay()
            }
        ),
        ModeData(
            title = "放鬆時光",
            subtitle = "模式二",
            description = "聆聽舒緩音樂,放鬆身心享受時光",
            iconResId = R.drawable.music_02,
            color = Color(0xFF4FC3F7),
            onClick = {
                soundManager.playSFX("options3")
                onNavigateToRelax()
            }
        ),
        ModeData(
            title = "音樂遊戲",
            subtitle = "模式三",
            description = "跟著節奏玩遊戲,訓練反應能力",
            iconResId = R.drawable.music_03,
            color = Color(0xFFFF9800),
            onClick = {
                soundManager.playSFX("options3")
                onNavigateToGame()
            }
        ),
        ModeData(
            title = "榮譽榜",
            subtitle = "排行榜",
            description = "查看大家的分數排行，挑戰最高榮譽",
            iconResId = R.drawable.music_01,
            color = Color(0xFFFFD700),
            buttonText = "查看排行",
            onClick = {
                soundManager.playSFX("options3")
                onNavigateToLeaderboard()
            }
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F4F4))
        ) {
            TopInfoBar(
                soundManager = soundManager,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
                onLogoutStart = {
                    soundManager.playSFX("cancel")
                    soundManager.stopBgm()
                    isLoggingOut = true
                },
                authViewModel = authViewModel
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側 Logo 區塊
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "樂之聲 Logo",
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "樂之聲",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFFEC4899)
                                    )
                                ),
                                letterSpacing = (-1.5).sp,
                                shadow = Shadow(
                                    color = Color.White,
                                    offset = GeometryOffset(0f, 0f),
                                    blurRadius = 6f
                                )
                            ),
                            maxLines = 1,
                            softWrap = false
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "讓每位孩子都能聽見快樂的聲音\n專為心智障礙孩童設計的音樂互動 App",
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                // 右側卡片輪播區塊
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SwipeableCardCarousel(
                        soundManager = soundManager,
                        modes = modes,
                        currentIndex = currentIndex,
                        onIndexChange = { currentIndex = it }
                    )
                }
            }
        }

        // 黑屏遮罩層（登出時淡入）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = blackAlpha }
                .background(Color.Black)
        )
    }
}

// =====================================================
// 🔝 上方資訊欄
// =====================================================
@Composable
fun TopInfoBar(
    soundManager: SoundManager,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutStart: () -> Unit,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    val isAnonymous = authViewModel.isAnonymous()
    val userProfile by profileViewModel.userProfile.collectAsState()

    val defaultAvatars = remember {
        listOf(
            R.drawable.avatar_01, R.drawable.avatar_02, R.drawable.avatar_03, R.drawable.avatar_04,
            R.drawable.avatar_05, R.drawable.avatar_06, R.drawable.avatar_07, R.drawable.avatar_08,
            R.drawable.avatar_09, R.drawable.avatar_10, R.drawable.avatar_11, R.drawable.avatar_12,
            R.drawable.avatar_13, R.drawable.avatar_14, R.drawable.avatar_15, R.drawable.avatar_16,
            R.drawable.avatar_17, R.drawable.avatar_18, R.drawable.avatar_19, R.drawable.avatar_20,
            R.drawable.avatar_21, R.drawable.avatar_22, R.drawable.avatar_23, R.drawable.avatar_24
        )
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    // 齒輪旋轉動畫
    val infiniteTransition = rememberInfiniteTransition(label = "gearRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gearRotate"
    )

    var clickBoost by remember { mutableStateOf(0f) }
    val boostRotation by animateFloatAsState(
        targetValue = clickBoost,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 300f
        ),
        finishedListener = { clickBoost = 0f }
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "樂之聲",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7),
                modifier = Modifier.weight(1f)
            )

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE8EAF6))
                        .clickable {
                            soundManager.playSFX("settings")
                            showDropdownMenu = !showDropdownMenu
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val avatarResId = userProfile.photoUrl.toIntOrNull()
                    val finalAvatar = if (avatarResId != null && defaultAvatars.contains(avatarResId)) {
                        avatarResId
                    } else {
                        R.drawable.user
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFF673AB7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(finalAvatar),
                            contentDescription = "頭像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isAnonymous) "訪客" else userProfile.displayName,
                        fontSize = 14.sp,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "下拉",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    modifier = Modifier.width(180.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.user),
                                    "個人資料",
                                    Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("個人資料", fontSize = 14.sp)
                            }
                        },
                        onClick = {
                            soundManager.playSFX("settings")
                            showDropdownMenu = false
                            onNavigateToProfile()
                        }
                    )
                    Divider(color = Color(0xFFE0E0E0))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.logout),
                                    "登出",
                                    Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("登出", fontSize = 14.sp, color = Color(0xFFE57373))
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            onLogoutStart()
                        }
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // 設定按鈕（齒輪圖示）
            Image(
                painter = painterResource(id = R.drawable.setting),
                contentDescription = "設定",
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = rotation + boostRotation }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        soundManager.playSFX("settings")
                        clickBoost += 720f
                        onNavigateToSettings()
                    }
            )
        }
    }
}

// =====================================================
// 📌 卡片輪播
// =====================================================
@Composable
fun SwipeableCardCarousel(
    soundManager: SoundManager,
    modes: List<ModeData>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        finishedListener = { isAnimating = false }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (!isAnimating) {
                            if (offsetX > 80 && currentIndex > 0) {
                                isAnimating = true
                                soundManager.playSFX("options2")
                                onIndexChange(currentIndex - 1)
                            } else if (offsetX < -80 && currentIndex < modes.size - 1) {
                                isAnimating = true
                                soundManager.playSFX("options2")
                                onIndexChange(currentIndex + 1)
                            }
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!isAnimating)
                            offsetX = (offsetX + dragAmount).coerceIn(-200f, 200f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        modes.forEachIndexed { index, mode ->
            val offset = index - currentIndex
            if (offset != 0) {
                ModeCardSwiper(mode, offset, animatedOffset, false)
            }
        }

        modes.getOrNull(currentIndex)?.let {
            ModeCardSwiper(it, 0, animatedOffset, true)
        }
    }
}

// =====================================================
// 🃏 卡片
// =====================================================
@Composable
fun ModeCardSwiper(
    mode: ModeData,
    offset: Int,
    dragOffset: Float,
    isCenter: Boolean
) {
    val scale by animateFloatAsState(if (isCenter) 1f else 0.8f, tween(300))
    val translationX = offset * 180f + dragOffset
    val rotationY = (translationX / 25f).coerceIn(-20f, 20f)
    val alpha = if (offset.absoluteValue > 1) 0f else (1f - offset.absoluteValue * 0.5f)

    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val iconBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isCenter) -8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconBounce"
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(240.dp)
            .graphicsLayer {
                this.translationX = translationX
                this.rotationY = rotationY
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(if (isCenter) 16.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer { translationY = iconBounce }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(mode.color.copy(0.25f), mode.color.copy(0.08f))
                        )
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(mode.color.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = mode.iconResId),
                        contentDescription = mode.title,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(mode.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(mode.subtitle, fontSize = 11.sp, color = mode.color, textAlign = TextAlign.Center)

            Spacer(Modifier.height(6.dp))
            Text(
                mode.description,
                fontSize = 10.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { if (isCenter) mode.onClick() },
                enabled = isCenter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = mode.color,
                    disabledContainerColor = mode.color.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(34.dp)
            ) {
                Text(
                    mode.buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCenter) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// =====================================================
// 📦 資料類別
// =====================================================
data class ModeData(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconResId: Int,
    val color: Color,
    val buttonText: String = "進入遊戲",
    val onClick: () -> Unit
)