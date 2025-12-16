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
    fun getBPM(): Double
}

object BeatmapRegistry {
    private val beatmaps = mutableMapOf<Int, Beatmap>()

    init {
        register(OSU_01)
        register(OSU_02)
        register(OSU_03)
        register(OSU_04)
    }

    private fun register(beatmap: Beatmap) {
        beatmaps[beatmap.id] = beatmap
    }

    fun getBeatmap(id: Int): Beatmap? = beatmaps[id]
    fun getAllBeatmaps(): List<Beatmap> = beatmaps.values.sortedBy { it.id }
    fun getBeatmapCount(): Int = beatmaps.size
    fun hasNextBeatmap(currentId: Int): Boolean = beatmaps.containsKey(currentId + 1)
}