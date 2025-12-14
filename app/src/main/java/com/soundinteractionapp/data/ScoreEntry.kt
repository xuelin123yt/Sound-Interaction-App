package com.soundinteractionapp.data

data class ScoreEntry(
    // 關卡 1 的三種難度分數
    val level1Easy: Int = 0,
    val level1Normal: Int = 0,
    val level1Hard: Int = 0,

    // ✅ 關卡 2 的三種難度分數
    val level2Easy: Int = 0,    // 簡單 (天空之城)
    val level2Normal: Int = 0,  // 普通 (龍貓)
    val level2Hard: Int = 0,    // 困難 (Maria)

    // 關卡 3
    val level3Score: Int = 0
) {
    // 取得關卡 1 的總分
    val level1Total: Int
        get() = level1Easy + level1Normal + level1Hard

    // ✅ 取得關卡 2 的總分
    val level2Total: Int
        get() = level2Easy + level2Normal + level2Hard
}