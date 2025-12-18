package com.soundinteractionapp.screens.game.levels.level4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.BeatmapRegistry
import com.soundinteractionapp.screens.game.levels.level4.components.*

/**
 * Level 4 主入口 - 加入防抖機制
 */
@Composable
fun Level4Screen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager
) {
    // ✅ 防抖狀態
    var isNavigating by remember { mutableStateOf(false) }

    val beatmaps = remember { BeatmapRegistry.getAllBeatmaps() }
    var showSongSelection by remember { mutableStateOf(true) }
    var selectedBeatmapId by remember { mutableIntStateOf(beatmaps.firstOrNull()?.id ?: 1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        if (showSongSelection) {
            SongSelectionScreen(
                beatmaps = beatmaps,
                onSongSelected = { beatmapId ->
                    selectedBeatmapId = beatmapId
                    showSongSelection = false
                },
                onBack = {
                    // ✅ 加入防抖檢查
                    if (isNavigating) return@SongSelectionScreen
                    isNavigating = true
                    soundManager.playSFX("cancel")
                    onNavigateBack()
                }
            )
        } else {
            val beatmap = BeatmapRegistry.getBeatmap(selectedBeatmapId)
            if (beatmap != null) {
                GameScreen(
                    beatmap = beatmap,
                    soundManager = soundManager,
                    onBack = {
                        soundManager.playSFX("cancel")
                        showSongSelection = true
                    },
                    onNextLevel = {
                        if (BeatmapRegistry.hasNextBeatmap(selectedBeatmapId)) {
                            selectedBeatmapId += 1
                        } else {
                            showSongSelection = true
                        }
                    }
                )
            }
        }
    }
}