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
import com.soundinteractionapp.utils.VolumeKeys

private data class BellItem(val name: String, val imageResId: Int, val soundResId: Int)

/**
 * 鈴鐺互動畫面
 */
@Composable
fun BellInteractionScreen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    var isNavigating by remember { mutableStateOf(false) }

    // ✅ 进入时暂停 BGM，离开时恢复
    DisposableEffect(Unit) {
        soundManager.pauseBGM()
        onDispose {
            soundManager.resumeBGM()
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        if (isNavigating) return@Button
                        isNavigating = true
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.height(50.dp),
                    enabled = !isNavigating
                ) {
                    Text("← 返回自由探索", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bellItems.forEachIndexed { index, item ->
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
    val scale = animateFloatAsState(if (isPressed) 0.9f else 1.0f, tween(50), label = "bellScale")
    val alpha = animateFloatAsState(if (isPressed) 0.7f else 1.0f, label = "bellAlpha")

    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            soundManager.playSound(soundResId, VolumeKeys.FREEPLAY_BELL)
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
        Text(
            text = label,
            fontSize = 20.sp,
            color = Color.Black,  // ✅ 強制黑色
            style = MaterialTheme.typography.titleMedium
        )
    }
}