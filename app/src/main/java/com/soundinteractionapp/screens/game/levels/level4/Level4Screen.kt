package com.soundinteractionapp.screens.game.levels.level4

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.soundinteractionapp.SoundManager  // ✅ 添加這個 import
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.BeatmapRegistry
import com.soundinteractionapp.screens.game.levels.level4.components.*
import kotlin.collections.firstOrNull

/**
 * Level 4 主入口 - 歌曲選擇畫面（更新版：支援返回功能）
 */
@Composable
fun Level4Screen(
    navController: NavController,
    soundManager: SoundManager  // ✅ 添加這個參數
) {
    val beatmaps = remember { BeatmapRegistry.getAllBeatmaps() }
    var showSongSelection by remember { mutableStateOf(true) }
    var selectedBeatmapId by remember { mutableIntStateOf(beatmaps.firstOrNull()?.id ?: 1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        if (showSongSelection) {
            // 歌曲選擇畫面
            SongSelectionScreen(
                beatmaps = beatmaps,
                onSongSelected = { beatmapId ->
                    selectedBeatmapId = beatmapId
                    showSongSelection = false
                },
                onBack = {
                    // 返回上一頁（遊戲模式選單）
                    navController.popBackStack()
                }
            )
        } else {
            // 遊戲畫面
            val beatmap = BeatmapRegistry.getBeatmap(selectedBeatmapId)
            if (beatmap != null) {
                GameScreen(
                    navController = navController,
                    beatmap = beatmap,
                    soundManager = soundManager,  // ✅ 傳入 soundManager
                    onBack = { showSongSelection = true },
                    onNextLevel = {
                        if (BeatmapRegistry.hasNextBeatmap(selectedBeatmapId)) {
                            selectedBeatmapId += 1
                            showSongSelection = false
                        } else {
                            showSongSelection = true
                        }
                    }
                )
            }
        }
    }
}