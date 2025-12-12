package com.soundinteractionapp.utils

import android.content.Context
import android.content.SharedPreferences

class GameProgressManager(context: Context) {
    // 建立一個存檔空間，名稱叫 "game_progress"
    private val prefs: SharedPreferences = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)

    // 檢查某個難度是否已解鎖
    fun isUnlocked(difficultyLabel: String): Boolean {
        // "簡單" 永遠是解鎖的，不用檢查
        if (difficultyLabel == "簡單") return true

        // 其他難度去讀取紀錄，預設是 false (鎖住)
        return prefs.getBoolean("unlock_${difficultyLabel}", false)
    }

    // 解鎖某個難度 (當分數達標時呼叫)
    fun unlockDifficulty(difficultyLabel: String) {
        prefs.edit().putBoolean("unlock_${difficultyLabel}", true).apply()
    }
}