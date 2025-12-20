package com.soundinteractionapp.screens.game.levels.level4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.BeatmapRegistry
import com.soundinteractionapp.screens.game.levels.level4.components.*

/**
 * Level 4 主入口 - 整合解鎖系統 + AUTO 模式
 */
@Composable
fun Level4Screen(
    onNavigateBack: () -> Unit,
    soundManager: SoundManager,
    authViewModel: AuthViewModel = viewModel(),
    rankingViewModel: RankingViewModel = viewModel()
) {
    var isNavigating by remember { mutableStateOf(false) }

    val beatmaps = remember { BeatmapRegistry.getAllBeatmaps() }
    var showSongSelection by remember { mutableStateOf(true) }
    var selectedBeatmapId by remember { mutableIntStateOf(beatmaps.firstOrNull()?.id ?: 1) }
    var isAutoMode by remember { mutableStateOf(false) }  // ✅ 新增：AUTO 模式標記

    // ✅ 觀察使用者狀態和分數
    val isGuest by remember { derivedStateOf { authViewModel.isAnonymous() } }
    val scoreEntry by rankingViewModel.scores.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        if (showSongSelection) {
            SongSelectionScreen(
                beatmaps = beatmaps,
                isGuest = isGuest,
                scoreEntry = scoreEntry,
                onSongSelected = { beatmapId, autoMode ->  // ✅ 接收 AUTO 模式參數
                    selectedBeatmapId = beatmapId
                    isAutoMode = autoMode
                    showSongSelection = false
                },
                onBack = {
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
                    isAutoMode = isAutoMode,  // ✅ 傳遞 AUTO 模式參數
                    onBack = {
                        soundManager.playSFX("cancel")
                        showSongSelection = true
                        isAutoMode = false  // ✅ 返回時重置
                    },
                    onNextLevel = {
                        if (BeatmapRegistry.hasNextBeatmap(selectedBeatmapId)) {
                            selectedBeatmapId += 1
                            isAutoMode = false  // ✅ 下一關時重置為正常模式
                        } else {
                            showSongSelection = true
                            isAutoMode = false
                        }
                    }
                )
            }
        }
    }
}