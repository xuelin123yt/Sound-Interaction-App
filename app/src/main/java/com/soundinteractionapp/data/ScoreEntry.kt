package com.soundinteractionapp.data

data class ScoreEntry(
    // 關卡 1 的三種難度分數
    val level1Easy: Int = 0,
    val level1Normal: Int = 0,
    val level1Hard: Int = 0,

    // 關卡 2
    val level2Score: Int = 0,

    // ✅ 新增：關卡 3 分數
    val level3Score: Int = 0
) {
    // 取得關卡 1 的總分
    val level1Total: Int
        get() = level1Easy + level1Normal + level1Hard
}