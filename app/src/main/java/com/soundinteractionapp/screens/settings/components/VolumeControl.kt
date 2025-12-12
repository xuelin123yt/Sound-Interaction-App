package com.soundinteractionapp.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun HorizontalVolumeControl(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側圖標和標題
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMuted)
                                Color(0xFFEEEEEE)
                            else
                                iconColor.copy(alpha = 0.15f)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onMuteToggle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = if (isMuted) "取消靜音" else "靜音",
                        tint = if (isMuted) Color(0xFF999999) else iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMuted) Color(0xFF999999) else Color(0xFF333333)
                )
            }

            // 中間滑桿
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                enabled = !isMuted,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    activeTrackColor = if (isMuted) Color(0xFFCCCCCC) else iconColor,
                    inactiveTrackColor = if (isMuted) Color(0xFFEEEEEE) else iconColor.copy(alpha = 0.3f),
                    disabledThumbColor = Color(0xFFCCCCCC),
                    disabledActiveTrackColor = Color(0xFFCCCCCC),
                    disabledInactiveTrackColor = Color(0xFFEEEEEE)
                )
            )

            // 右側百分比
            Text(
                text = if (isMuted) "0" else "${(volume * 100).roundToInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) Color(0xFF999999) else iconColor,
                modifier = Modifier.width(50.dp)
            )
        }
    }
}