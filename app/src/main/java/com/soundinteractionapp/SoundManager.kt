package com.soundinteractionapp

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {

    // --- SharedPreferences 儲存音量設定 ---
    private val prefs: SharedPreferences = context.getSharedPreferences("sound_settings", Context.MODE_PRIVATE)

    // --- 音量設定 (0.0f ~ 1.0f) ---
    var masterVolume: Float = prefs.getFloat("master_volume", 1.0f)
        set(value) {
            field = value.coerceIn(0f, 1f)
            prefs.edit().putFloat("master_volume", field).apply()
            updateBgmVolume()
        }

    var musicVolume: Float = prefs.getFloat("music_volume", 0.8f)
        set(value) {
            field = value.coerceIn(0f, 1f)
            prefs.edit().putFloat("music_volume", field).apply()
            updateBgmVolume()
        }

    var sfxVolume: Float = prefs.getFloat("sfx_volume", 1.0f)
        set(value) {
            field = value.coerceIn(0f, 1f)
            prefs.edit().putFloat("sfx_volume", field).apply()
            // ✅ 音效音量改變時,更新 SoundPool 的音量
            updateSoundPoolVolume()
        }

    // --- 靜音狀態 ---
    var isMasterMuted: Boolean = prefs.getBoolean("master_muted", false)
        set(value) {
            field = value
            prefs.edit().putBoolean("master_muted", field).apply()
            updateBgmVolume()
            updateSoundPoolVolume() // ✅ 主音量靜音也要更新音效
        }

    var isMusicMuted: Boolean = prefs.getBoolean("music_muted", false)
        set(value) {
            field = value
            prefs.edit().putBoolean("music_muted", field).apply()
            updateBgmVolume()
        }

    var isSfxMuted: Boolean = prefs.getBoolean("sfx_muted", false)
        set(value) {
            field = value
            prefs.edit().putBoolean("sfx_muted", field).apply()
            updateSoundPoolVolume() // ✅ 音效靜音要更新音量
        }

    // --- 背景音樂 (BGM) 用的 MediaPlayer ---
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmResId: Int? = null

    // ✅ 新增：記錄 BGM 是否因為 App 暫停而被暫停，以及暫停時的播放位置
    private var wasBgmPlayingBeforePause = false
    private var bgmPausePosition = 0

    // --- 打擊音效 (SFX) 用的 SoundPool ---
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    // ✅ 記錄當前音效音量,用於實時調整
    private var currentSfxVolume: Float = 1.0f

    init {
        // 初始化 SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // 預先載入音效
        try {
            loadSound("perfect", R.raw.sfx_perfect)
            loadSound("good", R.raw.sfx_good)
            loadSound("miss", R.raw.sfx_miss)
            loadSound("settings", R.raw.settings)
            loadSound("cancel", R.raw.cancel)
            loadSound("options2", R.raw.options2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ✅ 初始化時計算音效音量
        updateSoundPoolVolume()
    }

    private fun loadSound(key: String, resId: Int) {
        soundMap[key] = soundPool.load(context, resId, 1)
    }

    /**
     * 切換靜音狀態的方法
     */
    fun toggleMasterMute() {
        isMasterMuted = !isMasterMuted
    }

    fun toggleMusicMute() {
        isMusicMuted = !isMusicMuted
    }

    fun toggleSfxMute() {
        isSfxMuted = !isSfxMuted
    }

    /**
     * 更新 BGM 音量
     */
    private fun updateBgmVolume() {
        val finalVolume = if (isMasterMuted || isMusicMuted) {
            0f
        } else {
            masterVolume * musicVolume
        }
        bgmPlayer?.setVolume(finalVolume, finalVolume)
    }

    /**
     * ✅ 更新 SoundPool 的音效音量
     */
    private fun updateSoundPoolVolume() {
        currentSfxVolume = if (isMasterMuted || isSfxMuted) {
            0f
        } else {
            masterVolume * sfxVolume
        }
    }

    /**
     * 播放預先載入的短音效 (使用 SoundPool)
     */
    fun playSFX(name: String) {
        val soundId = soundMap[name]
        if (soundId != null && soundId != 0) {
            // ✅ 重新計算音量以確保使用最新值
            val finalVolume = if (isMasterMuted || isSfxMuted) {
                0f
            } else {
                masterVolume * sfxVolume
            }
            soundPool.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        }
    }

    /**
     * 直接用資源 ID 播放聲音
     */
    fun playSound(resId: Int) {
        try {
            val mp = MediaPlayer.create(context, resId)
            // ✅ 重新計算音量以確保使用最新值
            val finalVolume = if (isMasterMuted || isSfxMuted) {
                0f
            } else {
                masterVolume * sfxVolume
            }
            mp?.setVolume(finalVolume, finalVolume)
            mp?.setOnCompletionListener {
                it.release()
            }
            mp?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放關卡音樂 (不循環)
     */
    fun playMusic(resId: Int) {
        stopMusic()
        bgmPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = false
            val finalVolume = if (isMasterMuted || isMusicMuted) {
                0f
            } else {
                masterVolume * musicVolume
            }
            setVolume(finalVolume, finalVolume)
            start()
            setOnCompletionListener {
                it.release()
                bgmPlayer = null
            }
        }
    }

    /**
     * 播放背景音樂 (從 res/raw 資料夾,使用資源 ID)
     * 如果正在播放相同的 BGM,不做處理
     */
    fun playBgm(resId: Int) {
        // 如果正在播放相同的 BGM,不做處理
        if (currentBgmResId == resId && bgmPlayer?.isPlaying == true) {
            return
        }

        // ✅ 如果是相同的 BGM 但已暫停，恢復播放
        if (currentBgmResId == resId && bgmPlayer != null) {
            bgmPlayer?.start()
            return
        }

        stopBgm()

        try {
            bgmPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                val finalVolume = if (isMasterMuted || isMusicMuted) {
                    0f
                } else {
                    masterVolume * musicVolume
                }
                setVolume(finalVolume, finalVolume)
                start()
            }
            currentBgmResId = resId
            bgmPausePosition = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止背景音樂
     */
    fun stopBgm() {
        bgmPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        bgmPlayer = null
        currentBgmResId = null
        wasBgmPlayingBeforePause = false
        bgmPausePosition = 0
    }

    /**
     * 暫停背景音樂
     */
    fun pauseBgm() {
        bgmPlayer?.pause()
    }

    /**
     * 恢復背景音樂
     */
    fun resumeBgm() {
        bgmPlayer?.start()
    }

    /**
     * 停止關卡音樂
     */
    fun stopMusic() {
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.stop()
        }
        bgmPlayer?.release()
        bgmPlayer = null
    }

    /**
     * ✅ 新增：暫停所有音樂 (當 App 進入後台時)
     */
    fun pauseAllAudio() {
        bgmPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    bgmPausePosition = player.currentPosition
                    wasBgmPlayingBeforePause = true
                    player.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                wasBgmPlayingBeforePause = false
            }
        }
    }

    /**
     * ✅ 新增：恢復所有音樂 (當 App 回到前台時)
     */
    fun resumeAllAudio() {
        if (wasBgmPlayingBeforePause && bgmPlayer != null) {
            try {
                bgmPlayer?.apply {
                    seekTo(bgmPausePosition)
                    start()
                }
                wasBgmPlayingBeforePause = false
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果恢復失敗，嘗試重新播放當前的 BGM
                currentBgmResId?.let { resId ->
                    stopBgm()
                    playBgm(resId)
                }
            }
        }
    }

    /**
     * ✅ 新增：停止所有音樂 (當 App 被停止時)
     */
    fun stopAllAudio() {
        stopBgm()
        wasBgmPlayingBeforePause = false
        bgmPausePosition = 0
    }

    /**
     * 釋放所有資源
     */
    fun release() {
        stopBgm()
        soundPool.release()
    }
}