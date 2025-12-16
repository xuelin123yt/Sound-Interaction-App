package com.soundinteractionapp.screens.game.levels.level4

enum class NoteType {
    CIRCLE,
    SLIDER,
    SPINNER
}

enum class HitResult {
    PERFECT,
    GREAT,
    GOOD,
    MISS
}

enum class CurveType {
    LINEAR,
    BEZIER,
    PERFECT,
    CATMULL
}

data class Note(
    val x: Float,
    val y: Float,
    val time: Long,
    val type: NoteType,
    val endTime: Long = 0,
    val curveType: CurveType = CurveType.LINEAR,
    val curvePoints: List<Pair<Float, Float>> = emptyList(),
    val slides: Int = 1,
    val length: Float = 0f,
    val comboColor: Int = 0
)

object LevelColors {
    val COMBO_COLORS = listOf(
        0xFFFF0000,  // 0 - 紅色
        0xFF00FF00,  // 1 - 綠色
        0xFF0000FF,  // 2 - 藍色
        0xFFFFFF00,  // 3 - 黃色
        0xFFFF00FF,  // 4 - 洋紅
        0xFF00FFFF,  // 5 - 青色
        0xFFFF8000,  // 6 - 橙色
        0xFF8000FF,  // 7 - 紫色
        0xFFFFFFFF   // 8 - 白色
    )
}