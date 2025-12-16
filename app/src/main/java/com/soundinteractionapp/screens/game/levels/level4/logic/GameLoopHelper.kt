package com.soundinteractionapp.screens.game.levels.level4.logic

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.soundinteractionapp.screens.game.levels.level4.HitResult
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.models.ActiveNote

object GameLoopHelper {

    private const val TAG = "GameLoopHelper"

    // ✅ 音效延遲補償（毫秒）- 可根據設備調整
    private const val AUDIO_ANTICIPATION_MS = 30f

    data class GameLoopResult(
        val activeNotes: List<ActiveNote>,
        val noteCounter: Int
    )

    fun updateGameLoop(
        currentTime: Long,
        activeNotes: List<ActiveNote>,
        noteCounter: Int,
        isTouching: Boolean,
        touchPosition: Offset?,
        screenWidth: Float,
        screenHeight: Float,
        beatmap: Beatmap,
        onScoreUpdate: (HitResult, Int) -> Unit,
        onMiss: (Offset) -> Unit,
        onSliderReverse: () -> Unit
    ): GameLoopResult {
        var currentCounter = noteCounter
        var updatedNotes = activeNotes

        // 生成新音符
        val newNotes = beatmap.notes.filter { note ->
            val appearTime = note.time - beatmap.preempt

            val shouldAppear = currentTime >= appearTime &&
                    currentTime < appearTime + 50

            val notAlreadyExists = updatedNotes.none {
                it.note.time == note.time &&
                        it.note.x == note.x &&
                        it.note.y == note.y
            }

            shouldAppear && notAlreadyExists
        }.map {
            currentCounter++
            ActiveNote(it, noteNumber = currentCounter)
        }

        if (newNotes.isNotEmpty()) {
            updatedNotes = updatedNotes + newNotes
        }

        // 更新現有音符
        updatedNotes = updatedNotes.mapNotNull { activeNote ->
            val timeUntilHit = activeNote.note.time - currentTime

            // 檢測快速連打
            val isQuickTap = updatedNotes.any { other ->
                other != activeNote &&
                        other.isHit &&
                        !other.isMissed &&
                        kotlin.math.abs(other.note.time - activeNote.note.time) < 500L
            }

            val effectiveHitWindow = if (isQuickTap) {
                beatmap.hitWindowGood + beatmap.quickTapGrace
            } else {
                beatmap.hitWindowGood
            }

            // ✅ 更新 Slider 進度 - 優化折返音效觸發時機
            if (activeNote.note.type == NoteType.SLIDER &&
                activeNote.isHit &&
                !activeNote.sliderCompleted &&
                activeNote.sliderStartTime > 0L) {

                val sliderDuration = activeNote.note.endTime - activeNote.note.time

                // 安全檢查：如果 sliderDuration 無效，直接標記為完成並記錄錯誤
                if (sliderDuration <= 0) {
                    Log.e(TAG, "Invalid slider duration: endTime=${activeNote.note.endTime}, " +
                            "time=${activeNote.note.time}, duration=$sliderDuration. " +
                            "Marking slider as completed to prevent crash.")
                    activeNote.sliderCompleted = true
                    activeNote.sliderCompleteTime = currentTime
                    activeNote.sliderProgress = 1f
                    return@mapNotNull activeNote
                }

                val elapsed = currentTime - activeNote.sliderStartTime

                activeNote.sliderProgress = (elapsed.toFloat() / sliderDuration.toFloat()).coerceIn(0f, 1f)

                // ✅ 檢測滑條折返並提前觸發音效（補償延遲）
                if (activeNote.note.slides > 1) {
                    val totalSlides = activeNote.note.slides
                    val progressPerSlide = 1f / totalSlides

                    // ✅ 計算預判進度（提前觸發音效）
                    val anticipationProgress = if (sliderDuration > 0) {
                        activeNote.sliderProgress + (AUDIO_ANTICIPATION_MS / sliderDuration.toFloat())
                    } else {
                        activeNote.sliderProgress
                    }

                    val currentSlide = (anticipationProgress / progressPerSlide).toInt().coerceIn(0, totalSlides - 1)

                    // ✅ 當到達新的折返點時觸發音效
                    if (currentSlide > activeNote.lastSlideIndex && currentSlide > 0) {
                        activeNote.lastSlideIndex = currentSlide
                        onSliderReverse()

                        Log.d(TAG, "Slider reverse: slide=$currentSlide/$totalSlides, " +
                                "progress=${activeNote.sliderProgress}, " +
                                "anticipated=$anticipationProgress")
                    }
                }

                if (isTouching && touchPosition != null) {
                    val isFollowing = NoteHandler.checkSliderFollowing(
                        touchPosition = touchPosition,
                        note = activeNote.note,
                        sliderProgress = activeNote.sliderProgress,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )

                    if (isFollowing) {
                        activeNote.sliderFollowing = true
                        activeNote.followTime += 16
                    } else {
                        activeNote.sliderFollowing = false
                    }
                }
            }

            // 決定是否移除音符
            val shouldRemove = when {
                activeNote.note.type == NoteType.CIRCLE &&
                        activeNote.isHit &&
                        (currentTime - activeNote.note.time > 100) -> true

                activeNote.note.type == NoteType.CIRCLE &&
                        !activeNote.isHit &&
                        !activeNote.isMissed &&
                        timeUntilHit < -effectiveHitWindow -> {
                    onMiss(Offset(activeNote.note.x, activeNote.note.y))
                    activeNote.isMissed = true
                    activeNote.missTime = currentTime
                    false
                }

                activeNote.note.type == NoteType.CIRCLE &&
                        activeNote.isMissed &&
                        (currentTime - activeNote.missTime > 300) -> true

                activeNote.note.type == NoteType.SLIDER &&
                        !activeNote.isHit &&
                        !activeNote.isMissed &&
                        timeUntilHit < -effectiveHitWindow -> {
                    onMiss(Offset(activeNote.note.x, activeNote.note.y))
                    activeNote.isMissed = true
                    activeNote.missTime = currentTime
                    false
                }

                activeNote.note.type == NoteType.SLIDER &&
                        activeNote.isMissed &&
                        (currentTime - activeNote.missTime > 300) -> true

                activeNote.note.type == NoteType.SLIDER &&
                        activeNote.sliderCompleted &&
                        (currentTime - activeNote.sliderCompleteTime > 300) -> true

                activeNote.note.type == NoteType.SLIDER &&
                        activeNote.isHit &&
                        !activeNote.sliderCompleted &&
                        currentTime > activeNote.note.endTime + 500 -> {
                    // 添加安全檢查
                    val sliderDuration = activeNote.note.endTime - activeNote.note.time
                    if (sliderDuration > 0) {
                        if (!activeNote.sliderCompleted) {
                            activeNote.sliderCompleted = true
                            activeNote.sliderCompleteTime = currentTime
                            onMiss(Offset(activeNote.note.x, activeNote.note.y))
                        }
                    } else {
                        Log.w(TAG, "Slider with invalid duration detected during timeout check. " +
                                "Forcing completion.")
                        activeNote.sliderCompleted = true
                        activeNote.sliderCompleteTime = currentTime
                    }
                    false
                }

                else -> false
            }

            if (shouldRemove) null else activeNote
        }

        return GameLoopResult(
            activeNotes = updatedNotes,
            noteCounter = currentCounter
        )
    }
}