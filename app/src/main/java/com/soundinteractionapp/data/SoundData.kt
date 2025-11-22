package com.soundinteractionapp.data

import androidx.compose.runtime.Composable

data class SoundData(
    val name: String,
    val resId: Int,
    val icon: @Composable () -> Unit
)