package com.soundinteractionapp.screens.game.levels.level4.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*

/**
 * 試聽音樂管理器
 * - 支援淡入淡出效果
 * - 自動循環播放
 * - 歌曲切換時平滑過渡
 */
class PreviewAudioManager(private val context: Context) {

    private var currentPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var currentBeatmapId: Int? = null
    private var previewStartTime: Int = 0  // ✅ 記錄試聽起始位置
    private var pausedPosition: Int = 0    // ✅ 記錄暫停時的位置
    private var isPaused: Boolean = false  // ✅ 記錄是否已暫停

    private val TAG = "PreviewAudioManager"

    // 淡入淡出參數
    private val FADE_DURATION = 300L   // 淡入淡出時長（毫秒）- 0.3 秒
    private val FADE_STEPS = 15        // 音量變化步數
    private val MAX_VOLUME = 0.65f     // 最大音量（避免過大）

    /**
     * 播放試聽音樂
     * @param audioResId 音頻資源 ID
     * @param startTime 試聽起始時間（毫秒）
     * @param beatmapId 譜面 ID（用於判斷是否切換歌曲）
     */
    fun playPreview(audioResId: Int, startTime: Int, beatmapId: Int) {
        // 如果是同一首歌，不重複播放
        if (currentBeatmapId == beatmapId && currentPlayer?.isPlaying == true) {
            Log.d(TAG, "Already playing beatmap $beatmapId")
            return
        }

        // 切換到新歌曲
        if (currentBeatmapId != beatmapId) {
            Log.d(TAG, "Switching from beatmap $currentBeatmapId to $beatmapId")
            stopWithFadeOut {
                startNewPreview(audioResId, startTime, beatmapId)
            }
        } else {
            // 同一首歌但播放器已停止，重新播放
            startNewPreview(audioResId, startTime, beatmapId)
        }
    }

    /**
     * 開始播放新的試聽音樂
     */
    private fun startNewPreview(audioResId: Int, startTime: Int, beatmapId: Int) {
        try {
            // 釋放舊播放器
            currentPlayer?.release()

            // ✅ 記錄試聽起始位置
            previewStartTime = startTime

            // 創建新播放器
            currentPlayer = MediaPlayer.create(context, audioResId)?.apply {
                setVolume(0f, 0f)  // 初始音量為 0
                seekTo(startTime)
                isLooping = false   // ✅ 關閉自動循環，改用手動控制

                // ✅ 設置循環監聽器：播放結束後回到試聽起始位置
                setOnCompletionListener { mp ->
                    Log.d(TAG, "Preview completed, restarting from $previewStartTime")
                    try {
                        mp.seekTo(previewStartTime)
                        mp.start()
                        fadeIn()  // 重新淡入
                    } catch (e: Exception) {
                        Log.e(TAG, "Error restarting preview", e)
                    }
                }

                // 設置錯誤監聽器
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    true
                }

                start()
                currentBeatmapId = beatmapId

                // 開始淡入
                fadeIn()
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
     */
    private fun fadeIn() {
        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val stepDuration = FADE_DURATION / FADE_STEPS
                val volumeStep = MAX_VOLUME / FADE_STEPS

                for (i in 0..FADE_STEPS) {
                    val volume = volumeStep * i
                    currentPlayer?.setVolume(volume, volume)
                    delay(stepDuration)
                }

                // 確保最終音量正確
                currentPlayer?.setVolume(MAX_VOLUME, MAX_VOLUME)
                Log.d(TAG, "Fade in completed")
            } catch (e: CancellationException) {
                Log.d(TAG, "Fade in cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error during fade in", e)
            }
        }
    }

    /**
     * 淡出並停止
     * @param onComplete 淡出完成後的回調
     */
    private fun stopWithFadeOut(onComplete: () -> Unit = {}) {
        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val stepDuration = FADE_DURATION / FADE_STEPS
                val currentVolume = MAX_VOLUME
                val volumeStep = currentVolume / FADE_STEPS

                for (i in FADE_STEPS downTo 0) {
                    val volume = volumeStep * i
                    currentPlayer?.setVolume(volume, volume)
                    delay(stepDuration)
                }

                // 停止並釋放播放器
                currentPlayer?.apply {
                    if (isPlaying) stop()
                    release()
                }
                currentPlayer = null
                currentBeatmapId = null

                Log.d(TAG, "Fade out completed")

                // 執行回調
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
     */
    fun resume() {
        currentPlayer?.let { player ->
            if (isPaused) {
                try {
                    player.seekTo(pausedPosition)
                    player.start()
                    isPaused = false
                    fadeIn()
                    Log.d(TAG, "Resumed from position: $pausedPosition")
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