package com.soundinteractionapp.utils

/**
 * 音量設定鍵值常數
 * 用於 SoundManager 的 playMusic() 和 playSFX() 函數
 * 避免打錯字造成音量設定無效
 */
object VolumeKeys {
    // 模式一:自由玩耍
    const val FREEPLAY_CAT1 = "freeplay_cat1"
    const val FREEPLAY_CAT2 = "freeplay_cat2"
    const val FREEPLAY_CAT3 = "freeplay_cat3"
    const val FREEPLAY_DOG1 = "freeplay_dog1"  // ✅ 3 個狗狗
    const val FREEPLAY_DOG2 = "freeplay_dog2"
    const val FREEPLAY_DOG3 = "freeplay_dog3"
    const val FREEPLAY_BIRD = "freeplay_bird"
    const val FREEPLAY_PIANO = "freeplay_piano"
    const val FREEPLAY_DRUM = "freeplay_drum"
    const val FREEPLAY_BELL = "freeplay_bell"

    // 模式二：放鬆模式
    const val RELAX_RAIN = "relax_rain"
    const val RELAX_OCEAN = "relax_ocean"
    const val RELAX_WIND = "relax_wind"

    // 關卡一
    const val LEVEL1_EASY = "level1_easy"
    const val LEVEL1_MEDIUM = "level1_medium"
    const val LEVEL1_HARD = "level1_hard"
    const val LEVEL1_HIT = "level1_hit"

    // 關卡二
    const val LEVEL2_EASY = "level2_easy"
    const val LEVEL2_MEDIUM = "level2_medium"
    const val LEVEL2_HARD = "level2_hard"
    const val LEVEL2_HIT = "level2_hit"

    // 關卡三
    const val LEVEL3_MUSIC = "level3_music"
    const val LEVEL3_EFFECT = "level3_effect"

    // 關卡四
    const val LEVEL4_PREVIEW = "level4_preview"
    const val LEVEL4_SONG1 = "level4_song1"
    const val LEVEL4_SONG2 = "level4_song2"
    const val LEVEL4_SONG3 = "level4_song3"
    const val LEVEL4_SONG4 = "level4_song4"
    const val LEVEL4_SONG5 = "level4_song5"
    const val LEVEL4_HIT = "level4_hit"
    const val LEVEL4_MISS = "level4_miss"
}