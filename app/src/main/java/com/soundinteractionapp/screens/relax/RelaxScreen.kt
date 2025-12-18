package com.soundinteractionapp.screens.relax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.components.SoundInteractionButton
import com.soundinteractionapp.data.SoundData
import kotlinx.coroutines.delay

@Composable
fun RelaxScreenContent(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    onNavigateToOceanInteraction: () -> Unit,
    onNavigateToRainInteraction: () -> Unit,
    onNavigateToWindInteraction: () -> Unit
) {
    var activeEffectButtonId by remember { mutableStateOf<Int?>(null) }

    // 🔥 關鍵修復：防止快速點擊返回按鈕
    var isNavigating by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 頂部控制列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // 🔥 防抖保護
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
                    enabled = !isNavigating  // 🔥 點擊後禁用按鈕
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("返回", style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    "放鬆時光模式",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(100.dp))
            }

            // 中間：3 個環境音互動按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 雨聲
                val rainData = SoundData("雨聲", R.raw.rain_sound) { Text("🌧️") }
                SoundInteractionButton(
                    soundName = rainData.name,
                    icon = rainData.icon,
                    isActive = activeEffectButtonId == 0,
                    onClick = {
                        if (isNavigating) return@SoundInteractionButton
                        soundManager.playSound(R.raw.rain_sound)
                        onNavigateToRainInteraction()
                    }
                )

                // 海浪
                val oceanData = SoundData("海浪", R.raw.wave_sound) { Text("🌊") }
                SoundInteractionButton(
                    soundName = oceanData.name,
                    icon = oceanData.icon,
                    isActive = activeEffectButtonId == 1,
                    onClick = {
                        if (isNavigating) return@SoundInteractionButton
                        soundManager.playSound(R.raw.wave_sound)
                        onNavigateToOceanInteraction()
                    }
                )

                // 微風
                val windData = SoundData("微風", R.raw.wind_sound) { Text("🍃") }
                SoundInteractionButton(
                    soundName = windData.name,
                    icon = windData.icon,
                    isActive = activeEffectButtonId == 2,
                    onClick = {
                        if (isNavigating) return@SoundInteractionButton
                        soundManager.playSound(R.raw.wind_sound)
                        onNavigateToWindInteraction()
                    }
                )
            }

            // 視覺效果重置
            LaunchedEffect(activeEffectButtonId) {
                if (activeEffectButtonId != null) {
                    delay(200)
                    activeEffectButtonId = null
                }
            }
        }
    }
}