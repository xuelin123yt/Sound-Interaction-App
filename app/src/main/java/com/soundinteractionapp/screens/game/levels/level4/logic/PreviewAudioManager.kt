package com.soundinteractionapp.screens.game.levels.level4.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.soundinteractionapp.SoundManager
import kotlinx.coroutines.*

/**
 * 試聽音樂管理器
 * - 支援淡入淡出效果
 * - 自動循環播放
 * - 歌曲切換時平滑過渡
 * - ✅ 支援各別歌曲音量控制
 */
class PreviewAudioManager(
    private val context: Context,
    private val soundManager: SoundManager  // ✅ 新增：接收 SoundManager
) {

    private var currentPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var currentBeatmapId: Int? = null
    private var previewStartTime: Int = 0
    private var pausedPosition: Int = 0
    private var isPaused: Boolean = false

    private val TAG = "PreviewAudioManager"

    // 淡入淡出參數
    private val FADE_DURATION = 300L
    private val FADE_STEPS = 15

    /**
     * 播放試聽音樂
     * @param audioResId 音頻資源 ID
     * @param startTime 試聽起始時間（毫秒）
     * @param beatmapId 譜面 ID（用於判斷是否切換歌曲）
     */
    fun playPreview(audioResId: Int, startTime: Int, beatmapId: Int) {
        if (currentBeatmapId == beatmapId && currentPlayer?.isPlaying == true) {
            Log.d(TAG, "Already playing beatmap $beatmapId")
            return
        }

        if (currentBeatmapId != beatmapId) {
            Log.d(TAG, "Switching from beatmap $currentBeatmapId to $beatmapId")
            stopWithFadeOut {
                startNewPreview(audioResId, startTime, beatmapId)
            }
        } else {
            startNewPreview(audioResId, startTime, beatmapId)
        }
    }

    /**
     * 開始播放新的試聽音樂
     * ✅ 根據歌曲 ID 使用對應的音量設定
     */
    private fun startNewPreview(audioResId: Int, startTime: Int, beatmapId: Int) {
        try {
            currentPlayer?.release()
            previewStartTime = startTime

            // ✅ 根據 beatmapId 獲取對應的試聽音量
            val songVolume = soundManager.getLevel4SongVolume(beatmapId)
            val finalVolume = if (soundManager.isMasterMuted) {
                0f
            } else {
                soundManager.masterVolume * songVolume
            }

            Log.d(TAG, "Starting preview - beatmapId=$beatmapId, songVolume=$songVolume, finalVolume=$finalVolume")

            currentPlayer = MediaPlayer.create(context, audioResId)?.apply {
                setVolume(0f, 0f)  // 初始音量為 0（準備淡入）
                seekTo(startTime)
                isLooping = false

                setOnCompletionListener { mp ->
                    Log.d(TAG, "Preview completed, restarting from $previewStartTime")
                    try {
                        mp.seekTo(previewStartTime)
                        mp.start()
                        fadeIn(finalVolume)  // ✅ 重新淡入
                    } catch (e: Exception) {
                        Log.e(TAG, "Error restarting preview", e)
                    }
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    true
                }

                start()
                currentBeatmapId = beatmapId
                fadeIn(finalVolume)  // ✅ 開始淡入
            }

            if (currentPlayer == null) {
                Log.e(TAG, "Failed to create MediaPlayer for resource $audioResId")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting preview", e)
            e.printStackTrace()
        }
    }

    /**
     * 淡入效果
     * ✅ 支援動態最大音量
     */
    private fun fadeIn(targetVolume: Float) {
        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val stepDuration = FADE_DURATION / FADE_STEPS
                val volumeStep = targetVolume / FADE_STEPS

                for (i in 0..FADE_STEPS) {
                    val volume = volumeStep * i
                    currentPlayer?.setVolume(volume, volume)
                    delay(stepDuration)
                }

                currentPlayer?.setVolume(targetVolume, targetVolume)
                Log.d(TAG, "Fade in completed to $targetVolume")
            } catch (e: CancellationException) {
                Log.d(TAG, "Fade in cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error during fade in", e)
            }
        }
    }

    /**
     * 淡出並停止
     */
    private fun stopWithFadeOut(onComplete: () -> Unit = {}) {
        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val stepDuration = FADE_DURATION / FADE_STEPS

                // ✅ 獲取當前音量作為起始點
                val currentVolume = try {
                    // MediaPlayer 沒有 getVolume()，所以使用預設值
                    currentBeatmapId?.let { id ->
                        val songVolume = soundManager.getLevel4SongVolume(id)
                        soundManager.masterVolume * songVolume
                    } ?: 0.65f
                } catch (e: Exception) {
                    0.65f
                }

                val volumeStep = currentVolume / FADE_STEPS

                for (i in FADE_STEPS downTo 0) {
                    val volume = volumeStep * i
                    currentPlayer?.setVolume(volume, volume)
                    delay(stepDuration)
                }

                currentPlayer?.apply {
                    if (isPlaying) stop()
                    release()
                }
                currentPlayer = null
                currentBeatmapId = null

                Log.d(TAG, "Fade out completed")
                onComplete()

            } catch (e: CancellationException) {
                Log.d(TAG, "Fade out cancelled")
                currentPlayer?.release()
                currentPlayer = null
                currentBeatmapId = null
            } catch (e: Exception) {
                Log.e(TAG, "Error during fade out", e)
                currentPlayer?.release()
                currentPlayer = null
                currentBeatmapId = null
            }
        }
    }

    /**
     * 立即停止（無淡出效果）
     */
    fun stopImmediately() {
        fadeJob?.cancel()
        currentPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping player", e)
            }
            release()
        }
        currentPlayer = null
        currentBeatmapId = null
        Log.d(TAG, "Stopped immediately")
    }

    /**
     * 暫停（保存當前位置）
     */
    fun pause() {
        currentPlayer?.let { player ->
            if (player.isPlaying) {
                pausedPosition = player.currentPosition
                player.pause()
                isPaused = true
                fadeJob?.cancel()
                Log.d(TAG, "Paused at position: $pausedPosition")
            }
        }
    }

    /**
     * 恢復播放（從暫停位置繼續）
     * ✅ 恢復時重新計算音量
     */
    fun resume() {
        currentPlayer?.let { player ->
            if (isPaused) {
                try {
                    // ✅ 恢復時重新計算音量
                    currentBeatmapId?.let { id ->
                        val songVolume = soundManager.getLevel4SongVolume(id)
                        val finalVolume = if (soundManager.isMasterMuted) {
                            0f
                        } else {
                            soundManager.masterVolume * songVolume
                        }

                        player.seekTo(pausedPosition)
                        player.start()
                        isPaused = false
                        fadeIn(finalVolume)
                        Log.d(TAG, "Resumed from position: $pausedPosition with volume: $finalVolume")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error resuming", e)
                }
            }
        }
    }

    /**
     * 釋放資源
     */
    fun release() {
        fadeJob?.cancel()
        currentPlayer?.release()
        currentPlayer = null
        currentBeatmapId = null
        Log.d(TAG, "Released")
    }

    /**
     * 檢查是否正在播放
     */
    fun isPlaying(): Boolean {
        return currentPlayer?.isPlaying == true
    }
}