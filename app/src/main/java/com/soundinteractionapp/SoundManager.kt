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
            updateSoundPoolVolume()
        }

    // --- 靜音狀態 ---
    var isMasterMuted: Boolean = prefs.getBoolean("master_muted", false)
        set(value) {
            field = value
            prefs.edit().putBoolean("master_muted", field).apply()
            updateBgmVolume()
            updateSoundPoolVolume()
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
            updateSoundPoolVolume()
        }

    // --- 背景音樂 (BGM) 用的 MediaPlayer ---
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmResId: Int? = null

    private var wasBgmPlayingBeforePause = false
    private var bgmPausePosition = 0

    // --- 打擊音效 (SFX) 用的 SoundPool ---
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

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

            // ✅ 新增：預載遊戲打擊音效
            loadSound("hit", R.raw.osu_hit_sound)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
     * 更新 SoundPool 的音效音量
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
            val finalVolume = if (isMasterMuted || isSfxMuted) {
                0f
            } else {
                masterVolume * sfxVolume
            }
            soundPool.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        }
    }

    /**
     * ✅ 新增：專用於遊戲打擊音效的播放方法
     * - 使用 SoundPool 確保零延遲
     * - 可同時播放多個音效
     * - 適用於高頻率觸發場景
     */
    fun playHitSound() {
        playSFX("hit")
    }

    /**
     * 直接用資源 ID 播放聲音 (不建議用於高頻音效)
     */
    fun playSound(resId: Int) {
        try {
            val mp = MediaPlayer.create(context, resId)
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
     */
    fun playBgm(resId: Int) {
        if (currentBgmResId == resId && bgmPlayer?.isPlaying == true) {
            return
        }

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

    fun pauseBgm() {
        bgmPlayer?.pause()
    }

    fun resumeBgm() {
        bgmPlayer?.start()
    }

    fun stopMusic() {
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.stop()
        }
        bgmPlayer?.release()
        bgmPlayer = null
    }

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
                currentBgmResId?.let { resId ->
                    stopBgm()
                    playBgm(resId)
                }
            }
        }
    }

    fun stopAllAudio() {
        stopBgm()
        wasBgmPlayingBeforePause = false
        bgmPausePosition = 0
    }

    fun release() {
        stopBgm()
        soundPool.release()
    }
}