package com.soundinteractionapp

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.soundinteractionapp.data.SoundSettingsViewModel

class SoundManager(
    private val context: Context,
    private var soundSettingsViewModel: SoundSettingsViewModel? = null
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sound_settings", Context.MODE_PRIVATE)

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
        }

    var isMasterMuted: Boolean = prefs.getBoolean("master_muted", false)
        set(value) {
            field = value
            prefs.edit().putBoolean("master_muted", field).apply()
            updateBgmVolume()
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
        }

    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmResId: Int? = null
    private var wasBgmPlayingBeforePause = false
    private var bgmPausePosition = 0

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val soundReadyMap = mutableMapOf<Int, Boolean>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(15)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                soundReadyMap[sampleId] = true
                android.util.Log.d("SoundManager", "✅ Sound loaded: ID=$sampleId")
            } else {
                android.util.Log.e("SoundManager", "❌ Sound load failed: ID=$sampleId, status=$status")
            }
        }

        try {
            android.util.Log.d("SoundManager", "========== 載入音效 ==========")

            loadSound("perfect", R.raw.sfx_perfect)
            loadSound("good", R.raw.sfx_good)
            loadSound("miss", R.raw.sfx_miss)
            loadSound("settings", R.raw.settings)
            loadSound("cancel", R.raw.cancel)
            loadSound("options", R.raw.options)
            loadSound("options2", R.raw.options2)
            loadSound("options3", R.raw.options3)
            loadSound("options4", R.raw.options4)
            loadSound("osu_hit", R.raw.osu_hit_sound)
            loadSound("osu_miss", R.raw.osu_miss_sound)
            loadSound("fireworks", R.raw.fireworks)
            loadSound("dog_bark1", R.raw.dog_bark1)
            loadSound("dog_bark2", R.raw.dog_bark3)
            loadSound("dog_bark3", R.raw.dog_bark2)
            loadSound("bird_single", R.raw.bird_sound)
            loadSound("level2_piano_hit", R.raw.level2_piano_hit)
            loadSound("level3_correct", R.raw.pipe_music)
            loadSound("level3_wrong", R.raw.pipe_music)
            loadSound("level3_hit", R.raw.pipe_music)

            android.util.Log.d("SoundManager", "⚠️ hit_music 將使用 MediaPlayer 播放")
            android.util.Log.d("SoundManager", "========== 完成 ==========")
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "❌ 載入錯誤", e)
            e.printStackTrace()
        }
    }

    private fun loadSound(key: String, resId: Int) {
        try {
            val soundId = soundPool.load(context, resId, 1)
            soundMap[key] = soundId
            soundReadyMap[soundId] = false
            android.util.Log.d("SoundManager", "📋 Loading: $key -> ID=$soundId")
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "❌ Failed: $key", e)
        }
    }

    fun setSoundSettingsViewModel(viewModel: SoundSettingsViewModel) {
        this.soundSettingsViewModel = viewModel
    }

    fun toggleMasterMute() {
        isMasterMuted = !isMasterMuted
    }

    fun toggleMusicMute() {
        isMusicMuted = !isMusicMuted
    }

    fun toggleSfxMute() {
        isSfxMuted = !isSfxMuted
    }

    private fun updateBgmVolume() {
        val finalVolume = if (isMasterMuted || isMusicMuted) 0f else masterVolume * musicVolume
        bgmPlayer?.setVolume(finalVolume, finalVolume)
    }

    private fun getVolumeByKey(key: String?): Float {
        if (key == null) return 1f
        val settings = soundSettingsViewModel?.soundSettings?.value ?: return 1f

        return when (key) {
            "freeplay_cat1" -> settings.freePlay.cat1Volume
            "freeplay_cat2" -> settings.freePlay.cat2Volume
            "freeplay_cat3" -> settings.freePlay.cat3Volume
            "freeplay_dog1" -> settings.freePlay.dog1Volume
            "freeplay_dog2" -> settings.freePlay.dog2Volume
            "freeplay_dog3" -> settings.freePlay.dog3Volume
            "freeplay_bird" -> settings.freePlay.birdVolume
            "freeplay_piano" -> settings.freePlay.pianoVolume
            "freeplay_drum" -> settings.freePlay.drumVolume
            "freeplay_bell" -> settings.freePlay.bellVolume
            "relax_rain" -> settings.relax.rainVolume
            "relax_ocean" -> settings.relax.oceanVolume
            "relax_wind" -> settings.relax.windVolume
            "level1_easy" -> settings.game.level1.easyMusicVolume
            "level1_medium" -> settings.game.level1.mediumMusicVolume
            "level1_hard" -> settings.game.level1.hardMusicVolume
            "level1_hit" -> settings.game.level1.hitSoundVolume
            "level2_easy" -> settings.game.level2.easyMusicVolume
            "level2_medium" -> settings.game.level2.mediumMusicVolume
            "level2_hard" -> settings.game.level2.hardMusicVolume
            "level2_hit" -> settings.game.level2.hitSoundVolume
            "level3_music" -> settings.game.level3.musicVolume
            "level3_effect" -> settings.game.level3.effectVolume
            "level4_preview" -> settings.game.level4.previewVolume
            "level4_song1" -> settings.game.level4.song1Volume
            "level4_song2" -> settings.game.level4.song2Volume
            "level4_song3" -> settings.game.level4.song3Volume
            "level4_song4" -> settings.game.level4.song4Volume
            "level4_song5" -> settings.game.level4.song5Volume
            "level4_hit" -> settings.game.level4.hitSoundVolume
            "level4_miss" -> settings.game.level4.missSoundVolume
            else -> 1f
        }
    }

    fun playSFX(name: String, volumeKey: String? = null) {
        val soundId = soundMap[name]
        if (soundId != null && soundId != 0) {
            val detailVolume = getVolumeByKey(volumeKey)
            val finalVolume = if (isMasterMuted || isSfxMuted) 0f else masterVolume * sfxVolume * detailVolume
            soundPool.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        }
    }

    /**
     * ✅ 播放遊戲音效 (不受設定頁面 SFX 音量影響，只受詳細音量影響)
     * 如果是 hit_music 則改用 MediaPlayer 處理高品質 WAV 檔案
     */
    fun playGameSFX(name: String, volumeKey: String) {
        android.util.Log.d("SoundManager", "========== playGameSFX ==========")
        android.util.Log.d("SoundManager", "Sound: $name, VolumeKey: $volumeKey")

        // ✅ 如果是 hit_music，改用 MediaPlayer
        if (name == "hit_music") {
            android.util.Log.d("SoundManager", "🎵 Using MediaPlayer for hit_music")
            playLevel1HitSoundMP(volumeKey)
            return
        }

        // 其他音效繼續使用 SoundPool
        val soundId = soundMap[name]

        if (soundId == null || soundId == 0) {
            android.util.Log.e("SoundManager", "❌ Sound ID not found")
            return
        }

        if (soundReadyMap[soundId] != true) {
            android.util.Log.w("SoundManager", "⚠️ Sound not ready: $name")
            return
        }

        val detailVolume = getVolumeByKey(volumeKey)
        val finalVolume = if (isMasterMuted) 0f else masterVolume * detailVolume

        android.util.Log.d("SoundManager", "Volume: $finalVolume")

        try {
            val streamId = soundPool.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
            android.util.Log.d("SoundManager", "✅ Played (streamId: $streamId)")
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "❌ Play failed", e)
        }
    }

    /**
     * ✅ 使用 MediaPlayer 播放關卡一打擊音效
     * 專門處理高品質 WAV 檔案
     */
    private fun playLevel1HitSoundMP(volumeKey: String) {
        try {
            android.util.Log.d("SoundManager", "=== MediaPlayer 播放 hit_music ===")

            val detailVolume = getVolumeByKey(volumeKey)
            val finalVolume = if (isMasterMuted) 0f else masterVolume * detailVolume

            android.util.Log.d("SoundManager", "Master: $masterVolume, Detail: $detailVolume")
            android.util.Log.d("SoundManager", "Final Volume: $finalVolume")

            val mp = MediaPlayer.create(context, R.raw.hit_music)

            if (mp == null) {
                android.util.Log.e("SoundManager", "❌ MediaPlayer.create() 回傳 null!")
                android.util.Log.e("SoundManager", "可能是檔案不存在或格式不支援")
                return
            }

            mp.setVolume(finalVolume, finalVolume)
            mp.setOnCompletionListener {
                it.release()
                android.util.Log.d("SoundManager", "🔊 Sound completed and released")
            }
            mp.setOnErrorListener { _, what, extra ->
                android.util.Log.e("SoundManager", "❌ MediaPlayer error: what=$what, extra=$extra")
                true
            }
            mp.start()

            android.util.Log.d("SoundManager", "✅ MediaPlayer started!")

        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "❌ MediaPlayer exception", e)
            e.printStackTrace()
        }
    }

    fun playOsuHit() {
        playGameSFX("osu_hit", "level4_hit")
    }

    fun playOsuMiss() {
        playGameSFX("osu_miss", "level4_miss")
    }

    fun playSound(resId: Int, volumeKey: String? = null) {
        try {
            val detailVolume = getVolumeByKey(volumeKey)
            val mp = MediaPlayer.create(context, resId)
            val finalVolume = if (isMasterMuted || isSfxMuted) 0f else masterVolume * sfxVolume * detailVolume
            mp?.setVolume(finalVolume, finalVolume)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ✅ 修復：將 isLooping 改為 true，支持循環播放
    fun playGameMusic(resId: Int, volumeKey: String) {
        stopMusic()
        val detailVolume = getVolumeByKey(volumeKey)
        bgmPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true  // ✅ 改為 true，支持循環播放
            val finalVolume = if (isMasterMuted) 0f else masterVolume * detailVolume
            setVolume(finalVolume, finalVolume)
            start()
            // ✅ 移除 OnCompletionListener，因為現在是循環播放
        }
    }

    fun playBgm(resId: Int) {
        if (currentBgmResId == resId && bgmPlayer?.isPlaying == true) return
        if (currentBgmResId == resId && bgmPlayer != null) {
            bgmPlayer?.start()
            return
        }
        stopBgm()
        try {
            bgmPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                val finalVolume = if (isMasterMuted || isMusicMuted) 0f else masterVolume * musicVolume
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

    fun release() {
        stopBgm()
        soundPool.release()
    }

    fun getRelaxVolume(type: String): Float = getVolumeByKey("relax_$type")
    fun getLevel4SongVolume(songIndex: Int): Float = getVolumeByKey("level4_song$songIndex")
}