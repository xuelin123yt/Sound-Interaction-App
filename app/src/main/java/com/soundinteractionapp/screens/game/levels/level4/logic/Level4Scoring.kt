package com.soundinteractionapp.screens.game.levels.level4.logic

import com.soundinteractionapp.screens.game.levels.level4.HitResult

object Level4Scoring {
    const val SCORE_PERFECT = 300
    const val SCORE_GREAT = 100
    const val SCORE_GOOD = 50
    const val SCORE_MISS = 0

    fun calculateComboMultiplier(combo: Int) = 1f + (combo / 10f) * 0.1f

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
        return ((p * 300 + g * 100 + good * 50).toFloat() / (total * 300f)) * 100f
    }
}