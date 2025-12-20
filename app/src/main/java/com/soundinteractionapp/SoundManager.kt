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
            loadSound("options", R.raw.options)
            loadSound("options2", R.raw.options2)
            loadSound("options3", R.raw.options3)
            loadSound("osu_hit", R.raw.osu_hit_sound)
            loadSound("osu_miss", R.raw.osu_miss_sound)
            loadSound("fireworks", R.raw.fireworks)

            loadSound("dog_bark1", R.raw.dog_bark1)
            loadSound("dog_bark2", R.raw.dog_bark3)
            loadSound("dog_bark3", R.raw.dog_bark2)

            loadSound("bird_single", R.raw.bird_sound)
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
     * ✅ 舊版打擊音效方法（保留相容性）
     */
    fun playHitSound() {
        playSFX("osu_hit")
    }

    /**
     * ✅ OSU 遊戲專用音效
     */
    fun playOsuHit() {
        playSFX("osu_hit")
    }

    fun playOsuMiss() {
        playSFX("osu_miss")
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

    /**
     * ✅ 新增：直接播放音效（不受設定頁面的 SFX/Master 音量滑桿影響）
     * 用途：給狗狗互動使用，之後可改為接收「模式一總音量」
     */
    fun playDirectSound(name: String) {
        val soundId = soundMap[name]
        if (soundId != null && soundId != 0) {
            // 這裡直接設定 1.0f (最大聲)，完全忽略 masterVolume 和 sfxVolume
            // 如果你希望「靜音開關」還是有效，可以加個判斷；
            // 如果希望連靜音都無視，就直接傳入 1.0f

            // 這裡我寫保留「靜音開關」的功能，但「音量滑桿」無效
            // 如果你想連靜音開關都不管，把 if 去掉直接用 1.0f 即可
            val finalVolume = if (isMasterMuted || isSfxMuted) 0f else 1.0f

            soundPool.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        }
    }
}