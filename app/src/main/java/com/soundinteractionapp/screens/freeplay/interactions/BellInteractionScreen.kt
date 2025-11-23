package com.soundinteractionapp.screens.freeplay.interactions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager

private data class BellItem(val name: String, val imageResId: Int, val soundResId: Int)

/**
 * 鈴鐺互動畫面
 */
@Composable
fun BellInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    val bellItems = remember {
        listOf(
            BellItem("按鈴", R.drawable.desk_bell, R.raw.desk_bell),
            BellItem("風鈴", R.drawable.wind_chime, R.raw.wind_chime),
            BellItem("搖鐘", R.drawable.hand_bell, R.raw.hand_bell),
            BellItem("聖誕鈴", R.drawable.jingle_bell, R.raw.jingle_bell)
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFBE4E7)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // 頂部返回按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // 鈴鐺區
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 佔滿剩餘高度
                    .padding(horizontal = 16.dp, vertical = 16.dp), // 增加一點底部間距
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bellItems.forEachIndexed { index, item ->
                    // 設定圖片大小：聖誕鈴 (index 3) 放大
                    val imageSizeDp = if (index == 3) 280.dp else 160.dp

                    BellImageButton(
                        imageSize = imageSizeDp,
                        imageResId = item.imageResId,
                        soundResId = item.soundResId,
                        soundManager = soundManager,
                        label = item.name
                    )
                }
            }
        }
    }
}

/**
 * 圖片鈴鐺按鈕 Composable。
 */
@Composable
fun RowScope.BellImageButton(
    imageSize: Dp,
    imageResId: Int,
    soundResId: Int,
    soundManager: SoundManager,
    label: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = tween(durationMillis = 50),
        label = "bellScale"
    )

    val alpha = animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        label = "bellAlpha"
    )

    // 使用 Column 並填滿高度，這樣我們可以控制對齊
    Column(
        modifier = Modifier
            .weight(1f) // 每個按鈕平分寬度
            .fillMaxHeight(), // 填滿垂直空間
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // 內容分散對齊 (雖然下面我們用 weight 控制)
    ) {
        // 1. 圖片容器 (佔據上方大部分空間，並垂直置中)
        Box(
            modifier = Modifier
                .weight(1f) // 佔據所有可用空間，將文字推到底部
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // 圖片在空間內垂直置中
        ) {
            Box(
                modifier = Modifier
                    .size(imageSize) // 這裡應用個別圖片大小
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            soundManager.playSound(soundResId)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 標籤文字 (固定在底部)
        Text(
            text = label,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}