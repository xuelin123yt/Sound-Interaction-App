package com.soundinteractionapp.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.SoundInteractionButton(
    soundName: String,
    icon: @Composable () -> Unit,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (isActive || isPressed) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "interactionScale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .scale(scale.value),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        color = Color.White
                    )
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = soundName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}