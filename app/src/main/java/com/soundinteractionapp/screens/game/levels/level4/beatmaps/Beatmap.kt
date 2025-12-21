package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

interface Beatmap {
    val id: Int
    val title: String
    val description: String
    val audioResId: Int
    val coverImageResId: Int
    val backgroundImageResId: Int
    val sliderMultiplier: Float
    val preempt: Long
    val fadeIn: Long
    val hitWindowPerfect: Long
    val hitWindowGreat: Long
    val hitWindowGood: Long
    val quickTapGrace: Long
    val offset: Long
    val audioLeadIn: Long
    val timingPoints: List<TimingPoint>
    val notes: List<Note>

    // ✅ 試聽音樂起始位置（毫秒）
    // 預設從 40 秒開始，各譜面可以覆寫這個值
    // 設為 0 則從頭開始播放
    val previewStartTime: Int get() = 40000

    fun getBPM(): Double
}

/**
 * ✅ 譜面註冊中心 - 管理所有關卡4的歌曲
 */
object BeatmapRegistry {
    private val beatmaps = mutableMapOf<Int, Beatmap>()

    init {
        // ✅ 註冊所有譜面
        // 確保這些 OSU_01 ~ OSU_05 物件在你的專案中已定義
        register(OSU_01)  // 歌曲1 - 哆啦A夢
        register(OSU_02)  // 歌曲2 - 神魔之塔
        register(OSU_03)  // 歌曲3 - Ib 記憶
        register(OSU_04)  // 歌曲4 - 打上花火
        register(OSU_05)  // 歌曲5 - 海的街道
    }

    private fun register(beatmap: Beatmap) {
        beatmaps[beatmap.id] = beatmap
    }

    fun getBeatmap(id: Int): Beatmap? = beatmaps[id]
    fun getAllBeatmaps(): List<Beatmap> = beatmaps.values.sortedBy { it.id }
    fun getBeatmapCount(): Int = beatmaps.size
    fun hasNextBeatmap(currentId: Int): Boolean = beatmaps.containsKey(currentId + 1)
}

/**
 * ✅ 確保每個 OSU_XX 物件都有正確的 audioResId
 *
 * 範例結構（你需要根據實際專案調整）：
 *
 * object OSU_01 : Beatmap {
 *     override val id = 1
 *     override val title = "哆啦A夢"
 *     override val audioResId = R.raw.level4_doraemon  // ✅ 確保這個資源存在
 *     // ... 其他屬性
 * }
 *
 * object OSU_02 : Beatmap {
 *     override val id = 2
 *     override val title = "神魔之塔"
 *     override val audioResId = R.raw.level4_tos  // ✅ 確保這個資源存在
 *     // ... 其他屬性
 * }
 *
 * object OSU_03 : Beatmap {
 *     override val id = 3
 *     override val title = "Ib 記憶"
 *     override val audioResId = R.raw.level4_ib  // ✅ 確保這個資源存在
 *     // ... 其他屬性
 * }
 *
 * object OSU_04 : Beatmap {
 *     override val id = 4
 *     override val title = "打上花火"
 *     override val audioResId = R.raw.level4_fireworks  // ✅ 確保這個資源存在
 *     // ... 其他屬性
 * }
 *
 * object OSU_05 : Beatmap {
 *     override val id = 5
 *     override val title = "海的街道"
 *     override val audioResId = R.raw.level4_ocean_street  // ✅ 確保這個資源存在
 *     // ... 其他屬性
 * }
 */