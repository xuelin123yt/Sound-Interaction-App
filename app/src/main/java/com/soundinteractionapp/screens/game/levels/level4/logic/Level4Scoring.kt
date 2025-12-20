package com.soundinteractionapp.screens.game.levels.level4.logic

import com.soundinteractionapp.screens.game.levels.level4.HitResult

object Level4Scoring {
    // ✅ 大幅降低基礎分數
    const val SCORE_PERFECT = 100  // 從 300 降到 100
    const val SCORE_GREAT = 50     // 從 100 降到 50
    const val SCORE_GOOD = 20      // 從 50 降到 20
    const val SCORE_MISS = 0

    // ✅ 降低連擊加成
    fun calculateComboMultiplier(combo: Int) = 1f + (combo / 20f) * 0.05f  // 從 10/0.1 改成 20/0.05

    fun calculateScore(hitResult: HitResult, combo: Int): Int {
        val baseScore = when (hitResult) {
            HitResult.PERFECT -> SCORE_PERFECT
            HitResult.GREAT -> SCORE_GREAT
            HitResult.GOOD -> SCORE_GOOD
            HitResult.MISS -> SCORE_MISS
        }
        return (baseScore * calculateComboMultiplier(combo)).toInt()
    }

    fun calculateAccuracy(p: Int, g: Int, good: Int, m: Int): Float {
        val total = p + g + good + m
        if (total == 0) return 0f
        // ✅ 更新準確率計算以匹配新分數
        return ((p * 100 + g * 50 + good * 20).toFloat() / (total * 100f)) * 100f
    }
}