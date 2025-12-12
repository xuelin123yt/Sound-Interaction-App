package com.soundinteractionapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset as GeometryOffset
import androidx.compose.ui.draw.scale
import com.soundinteractionapp.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5FF))
            .padding(horizontal = 60.dp)
            .scale(0.9f), // 整體縮小 10%
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 左側：置中 LOGO + 標題 + 副標題
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // LOGO
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "樂之聲 Logo",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 標題縮小
            Text(
                text = "樂之聲",
                fontSize = 48.sp,   // ← 64 → 48
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    ),
                    shadow = Shadow(
                        color = Color.White,
                        offset = GeometryOffset(0f, 0f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "專為心智障礙孩童設計的音樂互動 App",
                fontSize = 16.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // 右側按鈕
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Text("登入帳號", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("註冊帳號", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onGuestLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF673AB7), Color(0xFF9C27B0))
                    )
                )
            ) {
                Text("訪客登入", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            }
        }
    }
}