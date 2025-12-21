package com.soundinteractionapp.utils

import androidx.compose.ui.graphics.Color

/**
 * 遊戲評分邏輯 - 難度優化版
 * 降低了等級門檻，讓使用者更容易獲得成就感。
 */
object GameScoreUtils {

    /**
     * 計算等級 (Rank) - 降低難度版本
     * @param score 玩家得分
     * @param maxScore 該難度的理論滿分
     */
    fun calculateRank(score: Int, maxScore: Int): String {
        if (score <= 0) return "-"
        if (maxScore == 0) return "C"

        val percentage = score.toFloat() / maxScore.toFloat()

        return when {
            // 原本需要 100%，現在 90% 以上就是最高榮譽 SSS
            percentage >= 0.90f -> "SSS"
            // 原本 95%，降至 80%
            percentage >= 0.80f -> "SS"
            // 原本 90%，降至 70%
            percentage >= 0.70f -> "S"
            // 原本 80%，降至 60%
            percentage >= 0.60f -> "A"
            // 原本 70%，降至 40%
            percentage >= 0.40f -> "B"
            // 低於 40% 才是 C
            else -> "C"
        }
    }

    /**
     * 根據等級取得對應顏色 (保持不變，或可微調顏色亮度)
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