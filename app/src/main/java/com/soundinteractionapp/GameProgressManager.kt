package com.soundinteractionapp

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class GameProgressManager(context: Context) {
    // 建立一個存檔空間，名稱叫 "game_progress"
    private val prefs: SharedPreferences = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)

    // 取得當前使用者 ID
    private fun getCurrentUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 如果是訪客，返回 "guest"
        // 如果是已登入使用者，返回他的 uid
        return if (currentUser?.isAnonymous == true) {
            "guest"
        } else {
            currentUser?.uid ?: "guest"
        }
    }

    // 生成帶有使用者 ID 的 key
    private fun getUnlockKey(difficultyLabel: String): String {
        val userId = getCurrentUserId()
        return "unlock_${userId}_${difficultyLabel}"
    }

    // 檢查某個難度是否已解鎖
    fun isUnlocked(difficultyLabel: String): Boolean {
        // "簡單" 永遠是解鎖的，不用檢查
        if (difficultyLabel == "簡單") return true

        // 訪客模式：所有難度都解鎖
        if (getCurrentUserId() == "guest") return true

        // 其他難度去讀取紀錄，預設是 false (鎖住)
        return prefs.getBoolean(getUnlockKey(difficultyLabel), false)
    }

    // 解鎖某個難度 (當分數達標時呼叫)
    fun unlockDifficulty(difficultyLabel: String) {
        prefs.edit().putBoolean(getUnlockKey(difficultyLabel), true).apply()
    }

    // 清除當前使用者的所有進度 (可選功能)
    fun clearProgress() {
        val userId = getCurrentUserId()
        prefs.edit()
            .remove("unlock_${userId}_普通")
            .remove("unlock_${userId}_困難")
            .apply()
    }

    // 清除所有使用者的進度 (用於測試或重置)
    fun clearAllProgress() {
        prefs.edit().clear().apply()
    }
}