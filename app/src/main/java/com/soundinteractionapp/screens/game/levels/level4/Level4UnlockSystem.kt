package com.soundinteractionapp.screens.game.levels.level4

import com.soundinteractionapp.data.ScoreEntry

/**
 * 關卡解鎖需求
 */
data class SongUnlockRequirement(
    val songId: Int,
    val requiredScore: Int,
    val requiredSongId: Int? // null 表示第一首歌，預設解鎖
)

/**
 * Level 4 解鎖系統核心
 */
object Level4UnlockSystem {

    // ✅ 解鎖規則配置
    private val UNLOCK_REQUIREMENTS = mapOf(
        1 to SongUnlockRequirement(1, 0, null),        // 第一首預設解鎖
        2 to SongUnlockRequirement(2, 10000, 1),       // 需要第一首達到 10000 分
        3 to SongUnlockRequirement(3, 4000, 2),        // 需要第二首達到 4000 分
        4 to SongUnlockRequirement(4, 6000, 3),        // 需要第三首達到 6000 分
        5 to SongUnlockRequirement(5, 6000, 4)         // 需要第四首達到 6000 分
    )

    /**
     * 檢查歌曲是否解鎖
     */
    fun isSongUnlocked(
        songId: Int,
        scoreEntry: ScoreEntry,
        isGuest: Boolean
    ): Boolean {
        // 訪客模式：全部解鎖
        if (isGuest) return true

        // 第一首歌：永遠解鎖
        if (songId == 1) return true

        // 獲取解鎖需求
        val requirement = UNLOCK_REQUIREMENTS[songId] ?: return false
        val requiredSongId = requirement.requiredSongId ?: return true

        // 獲取前置關卡的分數
        val previousScore = when (requiredSongId) {
            1 -> scoreEntry.level4Osu01
            2 -> scoreEntry.level4Osu02
            3 -> scoreEntry.level4Osu03
            4 -> scoreEntry.level4Osu04
            5 -> scoreEntry.level4Osu05
            else -> 0
        }

        // 檢查是否達到分數門檻
        return previousScore >= requirement.requiredScore
    }

    /**
     * 獲取解鎖需求資訊
     */
    fun getUnlockRequirement(songId: Int): SongUnlockRequirement? {
        return UNLOCK_REQUIREMENTS[songId]
    }

    /**
     * 獲取解鎖提示文字
     */
    fun getUnlockHintText(
        songId: Int,
        scoreEntry: ScoreEntry
    ): String {
        val requirement = UNLOCK_REQUIREMENTS[songId] ?: return ""
        val requiredSongId = requirement.requiredSongId ?: return "已解鎖"

        // 獲取前置關卡的分數
        val currentScore = when (requiredSongId) {
            1 -> scoreEntry.level4Osu01
            2 -> scoreEntry.level4Osu02
            3 -> scoreEntry.level4Osu03
            4 -> scoreEntry.level4Osu04
            5 -> scoreEntry.level4Osu05
            else -> 0
        }

        // 獲取前置關卡名稱
        val songName = when (requiredSongId) {
            1 -> "哆啦A夢 主題曲"
            2 -> "神魔之塔 主題曲（夜）"
            3 -> "Ib 記憶"
            4 -> "打上花火"
            5 -> "能看見海的街道"
            else -> "上一首歌"
        }

        val requiredScore = requirement.requiredScore

        return if (currentScore >= requiredScore) {
            "已解鎖"
        } else {
            "需要在「$songName」\n達到 $requiredScore 分才能解鎖\n\n目前分數：$currentScore"
        }
    }

    /**
     * 獲取所有歌曲的解鎖狀態
     */
    fun getAllUnlockStatus(
        scoreEntry: ScoreEntry,
        isGuest: Boolean
    ): Map<Int, Boolean> {
        return UNLOCK_REQUIREMENTS.keys.associateWith { songId ->
            isSongUnlocked(songId, scoreEntry, isGuest)
        }
    }
}