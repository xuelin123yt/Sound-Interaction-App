package com.soundinteractionapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SoundSettingsViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("detailed_sound_settings", Context.MODE_PRIVATE)

    private val _soundSettings = MutableStateFlow(SoundSettings())
    val soundSettings: StateFlow<SoundSettings> = _soundSettings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _soundSettings.value = SoundSettings(
            freePlay = FreePlaySounds(
                cat1Volume = prefs.getFloat("freeplay_cat1", 1f),
                cat2Volume = prefs.getFloat("freeplay_cat2", 1f),
                cat3Volume = prefs.getFloat("freeplay_cat3", 1f),
                dog1Volume = prefs.getFloat("freeplay_dog1", 1f),  // ✅ 改成 dog1
                dog2Volume = prefs.getFloat("freeplay_dog2", 1f),  // ✅ 新增 dog2
                dog3Volume = prefs.getFloat("freeplay_dog3", 1f),  // ✅ 新增 dog3
                birdVolume = prefs.getFloat("freeplay_bird", 1f),
                pianoVolume = prefs.getFloat("freeplay_piano", 1f),
                drumVolume = prefs.getFloat("freeplay_drum", 1f),
                bellVolume = prefs.getFloat("freeplay_bell", 1f)
            ),
            relax = RelaxSounds(
                rainVolume = prefs.getFloat("relax_rain", 1f),
                oceanVolume = prefs.getFloat("relax_ocean", 1f),
                windVolume = prefs.getFloat("relax_wind", 1f)
            ),
            game = GameSounds(
                level1 = Level1Sounds(
                    easyMusicVolume = prefs.getFloat("level1_easy_music", 1f),
                    mediumMusicVolume = prefs.getFloat("level1_medium_music", 1f),
                    hardMusicVolume = prefs.getFloat("level1_hard_music", 1f),
                    hitSoundVolume = prefs.getFloat("level1_hit", 1f)
                ),
                level2 = Level2Sounds(
                    easyMusicVolume = prefs.getFloat("level2_easy_music", 1f),
                    mediumMusicVolume = prefs.getFloat("level2_medium_music", 1f),
                    hardMusicVolume = prefs.getFloat("level2_hard_music", 1f),
                    hitSoundVolume = prefs.getFloat("level2_hit", 1f)
                ),
                level3 = Level3Sounds(
                    musicVolume = prefs.getFloat("level3_music", 1f),
                    effectVolume = prefs.getFloat("level3_effect", 1f)
                ),
                level4 = Level4Sounds(
                    previewVolume = prefs.getFloat("level4_preview", 1f),
                    song1Volume = prefs.getFloat("level4_song1", 1f),
                    song2Volume = prefs.getFloat("level4_song2", 1f),
                    song3Volume = prefs.getFloat("level4_song3", 1f),
                    song4Volume = prefs.getFloat("level4_song4", 1f),
                    song5Volume = prefs.getFloat("level4_song5", 1f),
                    hitSoundVolume = prefs.getFloat("level4_hit", 1f),
                    missSoundVolume = prefs.getFloat("level4_miss", 1f)
                )
            )
        )
    }

    private fun saveSettings() {
        prefs.edit().apply {
            val settings = _soundSettings.value

            // 模式一
            putFloat("freeplay_cat1", settings.freePlay.cat1Volume)
            putFloat("freeplay_cat2", settings.freePlay.cat2Volume)
            putFloat("freeplay_cat3", settings.freePlay.cat3Volume)
            putFloat("freeplay_dog1", settings.freePlay.dog1Volume)  // ✅ 改成 dog1
            putFloat("freeplay_dog2", settings.freePlay.dog2Volume)  // ✅ 新增 dog2
            putFloat("freeplay_dog3", settings.freePlay.dog3Volume)  // ✅ 新增 dog3
            putFloat("freeplay_bird", settings.freePlay.birdVolume)

            // 模式二
            putFloat("relax_rain", settings.relax.rainVolume)
            putFloat("relax_ocean", settings.relax.oceanVolume)
            putFloat("relax_wind", settings.relax.windVolume)

            // 關卡一
            putFloat("level1_easy_music", settings.game.level1.easyMusicVolume)
            putFloat("level1_medium_music", settings.game.level1.mediumMusicVolume)
            putFloat("level1_hard_music", settings.game.level1.hardMusicVolume)
            putFloat("level1_hit", settings.game.level1.hitSoundVolume)

            // 關卡二
            putFloat("level2_easy_music", settings.game.level2.easyMusicVolume)
            putFloat("level2_medium_music", settings.game.level2.mediumMusicVolume)
            putFloat("level2_hard_music", settings.game.level2.hardMusicVolume)
            putFloat("level2_hit", settings.game.level2.hitSoundVolume)

            // 關卡三
            putFloat("level3_music", settings.game.level3.musicVolume)
            putFloat("level3_effect", settings.game.level3.effectVolume)

            // 關卡四
            putFloat("level4_preview", settings.game.level4.previewVolume)
            putFloat("level4_song1", settings.game.level4.song1Volume)
            putFloat("level4_song2", settings.game.level4.song2Volume)
            putFloat("level4_song3", settings.game.level4.song3Volume)
            putFloat("level4_song4", settings.game.level4.song4Volume)
            putFloat("level4_song5", settings.game.level4.song5Volume)
            putFloat("level4_hit", settings.game.level4.hitSoundVolume)
            putFloat("level4_miss", settings.game.level4.missSoundVolume)

            apply()
        }
    }

    // ========== 模式一更新函數 ==========
    fun updateCat1Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(cat1Volume = volume)
        )
        saveSettings()
    }

    fun updateCat2Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(cat2Volume = volume)
        )
        saveSettings()
    }

    fun updateCat3Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(cat3Volume = volume)
        )
        saveSettings()
    }

    // ✅ 新增 3 個狗狗更新函數
    fun updateDog1Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(dog1Volume = volume)
        )
        saveSettings()
    }

    fun updateDog2Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(dog2Volume = volume)
        )
        saveSettings()
    }

    fun updateDog3Volume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(dog3Volume = volume)
        )
        saveSettings()
    }

    fun updateBirdVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(birdVolume = volume)
        )
        saveSettings()
    }

    fun updatePianoVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(pianoVolume = volume)
        )
        saveSettings()
    }

    fun updateDrumVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(drumVolume = volume)
        )
        saveSettings()
    }

    fun updateBellVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            freePlay = _soundSettings.value.freePlay.copy(bellVolume = volume)
        )
        saveSettings()
    }

    // ========== 模式二更新函數 ==========
    fun updateRainVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            relax = _soundSettings.value.relax.copy(rainVolume = volume)
        )
        saveSettings()
    }

    fun updateOceanVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            relax = _soundSettings.value.relax.copy(oceanVolume = volume)
        )
        saveSettings()
    }

    fun updateWindVolume(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            relax = _soundSettings.value.relax.copy(windVolume = volume)
        )
        saveSettings()
    }

    // ========== 關卡一更新函數 ==========
    fun updateLevel1EasyMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level1 = _soundSettings.value.game.level1.copy(easyMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel1MediumMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level1 = _soundSettings.value.game.level1.copy(mediumMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel1HardMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level1 = _soundSettings.value.game.level1.copy(hardMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel1HitSound(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level1 = _soundSettings.value.game.level1.copy(hitSoundVolume = volume)
            )
        )
        saveSettings()
    }

    // ========== 關卡二更新函數 ==========
    fun updateLevel2EasyMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level2 = _soundSettings.value.game.level2.copy(easyMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel2MediumMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level2 = _soundSettings.value.game.level2.copy(mediumMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel2HardMusic(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level2 = _soundSettings.value.game.level2.copy(hardMusicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel2HitSound(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level2 = _soundSettings.value.game.level2.copy(hitSoundVolume = volume)
            )
        )
        saveSettings()
    }

    // ========== 關卡三更新函數 ==========
    fun updateLevel3Music(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level3 = _soundSettings.value.game.level3.copy(musicVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel3Effect(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level3 = _soundSettings.value.game.level3.copy(effectVolume = volume)
            )
        )
        saveSettings()
    }

    // ========== 關卡四更新函數 ==========
    fun updateLevel4Preview(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(previewVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4Song1(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(song1Volume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4Song2(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(song2Volume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4Song3(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(song3Volume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4Song4(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(song4Volume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4Song5(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(song5Volume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4HitSound(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(hitSoundVolume = volume)
            )
        )
        saveSettings()
    }

    fun updateLevel4MissSound(volume: Float) {
        _soundSettings.value = _soundSettings.value.copy(
            game = _soundSettings.value.game.copy(
                level4 = _soundSettings.value.game.level4.copy(missSoundVolume = volume)
            )
        )
        saveSettings()
    }
}