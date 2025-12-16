package com.soundinteractionapp.screens.game.levels.level4.models

import androidx.compose.ui.geometry.Offset
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.HitResult

enum class GameState {
    READY,
    PLAYING,
    PAUSED,
    FINISHED
}

data class ActiveNote(
    val note: Note,
    var isHit: Boolean = false,
    val noteNumber: Int = 1,
    var sliderProgress: Float = 0f,
    var sliderCompleted: Boolean = false,
    var sliderFollowing: Boolean = false,
    var sliderStartTime: Long = 0L,
    var followTime: Long = 0L,
    var sliderCompleteTime: Long = 0L,
    var isMissed: Boolean = false,
    var missTime: Long = 0L,
    var lastSlideIndex: Int = 0,
    var hasPlayedReverseSound: Boolean = false
)

data class HitEffect(
    val position: Offset,
    val startTime: Long,
    val hitResult: HitResult
)

data class TimingPoint(
    val time: Long,
    val beatLength: Double,
    val volume: Int,
    val isKiai: Boolean = false
)