package com.soundinteractionapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.soundinteractionapp.R
import com.soundinteractionapp.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // ✨ 淡入動畫控制
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,  // 淡入速度(毫秒)
            easing = LinearEasing
        ),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true  // 開始淡入
        delay(3000)  // 總停留時間(毫秒)

        // 導航到主畫面
        navController.navigate(Screen.Welcome.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // 啟動畫面
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo 圖片
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)  // 調整 Logo 大小
                    .graphicsLayer(alpha = alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App 名稱 - 加上漸層色和陰影
            Text(
                text = "樂之聲",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    ),
                    shadow = Shadow(
                        color = Color.White,
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier.graphicsLayer(alpha = alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 副標題
            Text(
                text = "探索聲音的樂趣",
                fontSize = 30.sp,
                color = Color.Gray.copy(alpha = alphaAnim.value)
            )
        }
    }
}