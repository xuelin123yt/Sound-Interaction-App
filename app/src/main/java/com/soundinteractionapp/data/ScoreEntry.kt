package com.soundinteractionapp.data

data class ScoreEntry(
    // 關卡 1 的三種難度分數
    val level1Easy: Int = 0,
    val level1Normal: Int = 0,
    val level1Hard: Int = 0,

    // 關卡 2 的三種難度分數
    val level2Easy: Int = 0,    // 簡單 (天空之城)
    val level2Normal: Int = 0,  // 普通 (龍貓)
    val level2Hard: Int = 0,    // 困難 (Maria)

    // 關卡 3
    val level3Score: Int = 0,

    // ========== ✅ 新增：關卡 4 的四個譜面分數 ==========
    val level4Osu01: Int = 0,   // OSU_01 譜面
    val level4Osu02: Int = 0,   // OSU_02 譜面
    val level4Osu03: Int = 0,   // OSU_03 譜面
    val level4Osu04: Int = 0    // OSU_04 譜面
) {
    // 取得關卡 1 的總分
    val level1Total: Int
        get() = level1Easy + level1Normal + level1Hard

    // 取得關卡 2 的總分
    val level2Total: Int
        get() = level2Easy + level2Normal + level2Hard

    // ========== ✅ 新增：取得關卡 4 的總分 ==========
    val level4Total: Int
        get() = level4Osu01 + level4Osu02 + level4Osu03 + level4Osu04
}