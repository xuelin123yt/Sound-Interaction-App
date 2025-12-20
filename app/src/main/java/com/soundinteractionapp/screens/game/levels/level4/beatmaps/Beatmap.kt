package com.soundinteractionapp.screens.game.levels.level4.beatmaps

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

object BeatmapRegistry {
    private val beatmaps = mutableMapOf<Int, Beatmap>()

    init {
        register(OSU_01)
        register(OSU_02)
        register(OSU_03)
        register(OSU_04)
        register(OSU_05)
    }

    private fun register(beatmap: Beatmap) {
        beatmaps[beatmap.id] = beatmap
    }

    fun getBeatmap(id: Int): Beatmap? = beatmaps[id]
    fun getAllBeatmaps(): List<Beatmap> = beatmaps.values.sortedBy { it.id }
    fun getBeatmapCount(): Int = beatmaps.size
    fun hasNextBeatmap(currentId: Int): Boolean = beatmaps.containsKey(currentId + 1)
}