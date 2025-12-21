package com.soundinteractionapp.data

/**
 * 音效設定總資料模型
 */
data class SoundSettings(
    val freePlay: FreePlaySounds = FreePlaySounds(),
    val relax: RelaxSounds = RelaxSounds(),
    val game: GameSounds = GameSounds()
)

/**
 * 模式一：自由玩耍音效
 */
data class FreePlaySounds(
    val cat1Volume: Float = 1f,
    val cat2Volume: Float = 1f,
    val cat3Volume: Float = 1f,
    val dog1Volume: Float = 1f,  // ✅ 拆分成 3 個
    val dog2Volume: Float = 1f,
    val dog3Volume: Float = 1f,
    val birdVolume: Float = 1f,
    val pianoVolume: Float = 1f,
    val drumVolume: Float = 1f,
    val bellVolume: Float = 1f
)

/**
 * 模式二：放鬆模式音效
 */
data class RelaxSounds(
    val rainVolume: Float = 1f,
    val oceanVolume: Float = 1f,
    val windVolume: Float = 1f
)

/**
 * 模式三：遊戲模式音效
 */
data class GameSounds(
    val level1: Level1Sounds = Level1Sounds(),
    val level2: Level2Sounds = Level2Sounds(),
    val level3: Level3Sounds = Level3Sounds(),
    val level4: Level4Sounds = Level4Sounds()
)

/**
 * 關卡一：料理鼠王音效
 */
data class Level1Sounds(
    val easyMusicVolume: Float = 1f,
    val mediumMusicVolume: Float = 1f,
    val hardMusicVolume: Float = 1f,
    val hitSoundVolume: Float = 1f
)

/**
 * 關卡二：鋼琴演奏音效
 */
data class Level2Sounds(
    val easyMusicVolume: Float = 1f,
    val mediumMusicVolume: Float = 1f,
    val hardMusicVolume: Float = 1f,
    val hitSoundVolume: Float = 1f
)

/**
 * 關卡三音效
 */
data class Level3Sounds(
    val musicVolume: Float = 1f,
    val effectVolume: Float = 1f
)

/**
 * 關卡四：音樂節奏音效
 */
data class Level4Sounds(
    val previewVolume: Float = 1f,
    val song1Volume: Float = 1f,
    val song2Volume: Float = 1f,
    val song3Volume: Float = 1f,
    val song4Volume: Float = 1f,
    val song5Volume: Float = 1f,
    val hitSoundVolume: Float = 1f,
    val missSoundVolume: Float = 1f
)