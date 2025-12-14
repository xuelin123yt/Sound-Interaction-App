package com.soundinteractionapp.utils

import androidx.compose.ui.graphics.Color

/**
 * 遊戲評分邏輯的唯一真理中心 (Single Source of Truth)
 * 所有關卡結算、排行榜顯示，都必須呼叫這裡的函式，確保標準一致。
 */
object GameScoreUtils {

    /**
     * 計算等級 (Rank)
     * @param score 玩家得分
     * @param maxScore 該難度的理論滿分
     */
    fun calculateRank(score: Int, maxScore: Int): String {
        if (score == 0) return "-"
        if (maxScore == 0) return "C" // 防呆

        val percentage = score.toFloat() / maxScore.toFloat()

        return when {
            score >= maxScore -> "SSS"  // 滿分 (Perfect)
            percentage >= 0.95f -> "SS" // 95% 以上
            percentage >= 0.90f -> "S"  // 90% 以上
            percentage >= 0.80f -> "A"  // 80% 以上
            percentage >= 0.70f -> "B"  // 70% 以上
            else -> "C"                 // 低於 70%
        }
    }

    /**
     * 根據等級取得對應顏色
     */
    fun getRankColor(rank: String): Color {
        return when (rank) {
            "SSS" -> Color(0xFFFFD700) // 金色
            "SS" -> Color(0xFFFFEB3B)  // 黃色
            "S" -> Color(0xFFFFA726)   // 橘色
            "A" -> Color(0xFF66BB6A)   // 綠色
            "B" -> Color(0xFF42A5F5)   // 藍色
            else -> Color(0xFFBDBDBD)  // 灰色
        }
    }
}

