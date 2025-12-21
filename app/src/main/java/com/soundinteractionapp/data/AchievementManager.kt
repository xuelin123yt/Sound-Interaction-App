package com.soundinteractionapp.screens.profile.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soundinteractionapp.data.ScoreEntry
import com.soundinteractionapp.utils.GameScoreUtils
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * 成就管理器
 * 負責檢查和解鎖成就
 */
class AchievementManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * 檢查並解鎖所有成就
     * @param scoreEntry 使用者的分數資料
     * @param hasFeedback 是否已提交過意見回饋
     * @param hasAvatar 是否已設定頭像
     * @return 新解鎖的成就 ID 列表
     */
    suspend fun checkAndUnlockAchievements(
        scoreEntry: ScoreEntry,
        hasFeedback: Boolean,
        hasAvatar: Boolean
    ): List<Int> {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.d("AchievementManager", "訪客模式,不檢查成就")
            return emptyList()
        }

        // ✅ 先讀取已解鎖的成就
        val alreadyUnlocked = loadUnlockedAchievements(currentUser.uid).keys

        val shouldUnlockIds = mutableListOf<Int>()

        // 檢查所有成就條件
        if (checkLevel1SSS(scoreEntry)) shouldUnlockIds.add(1)
        if (checkLevel2Combo(scoreEntry)) shouldUnlockIds.add(2)
        if (checkLevel3Score(scoreEntry)) shouldUnlockIds.add(3)
        if (checkLevel4NoMiss(scoreEntry)) shouldUnlockIds.add(4)
        if (checkLevel4TotalScore(scoreEntry)) shouldUnlockIds.add(5)
        if (checkModeThreeComplete(scoreEntry)) shouldUnlockIds.add(6)
        if (hasAvatar) shouldUnlockIds.add(7)
        if (hasFeedback) shouldUnlockIds.add(8)

        // ✅ 只儲存「新解鎖」的成就(排除已經解鎖的)
        val newlyUnlockedIds = shouldUnlockIds.filterNot { it in alreadyUnlocked }

        if (newlyUnlockedIds.isNotEmpty()) {
            saveUnlockedAchievements(currentUser.uid, newlyUnlockedIds)
            Log.d("AchievementManager", "🎉 新解鎖成就: $newlyUnlockedIds")
        } else {
            Log.d("AchievementManager", "沒有新成就解鎖")
        }

        return newlyUnlockedIds
    }

    /**
     * ✅ 檢查成就 1: 關卡一任意難度取得 SSS
     */
    private fun checkLevel1SSS(scoreEntry: ScoreEntry): Boolean {
        val difficulties = listOf(
            scoreEntry.level1Easy to 13400,    // 簡單模式滿分
            scoreEntry.level1Normal to 22900,  // 普通模式滿分
            scoreEntry.level1Hard to 32850     // 困難模式滿分
        )

        return difficulties.any { (score, maxScore) ->
            val rank = GameScoreUtils.calculateRank(score, maxScore)
            rank == "SSS"
        }
    }

    /**
     * ✅ 檢查成就 2: 關卡二達成 100 Combo
     */
    private fun checkLevel2Combo(scoreEntry: ScoreEntry): Boolean {
        return scoreEntry.level2MaxCombo >= 100
    }

    /**
     * ✅ 檢查成就 3: 關卡三達成 3000 分
     */
    private fun checkLevel3Score(scoreEntry: ScoreEntry): Boolean {
        return scoreEntry.level3Score >= 3000
    }

    /**
     * ✅ 檢查成就 4: 關卡四全程無 Miss
     */
    private fun checkLevel4NoMiss(scoreEntry: ScoreEntry): Boolean {
        return scoreEntry.level4HasNoMiss
    }

    /**
     * ✅ 檢查成就 5: 關卡四總分超過 30000
     */
    private fun checkLevel4TotalScore(scoreEntry: ScoreEntry): Boolean {
        return scoreEntry.level4Total >= 30000
    }

    /**
     * ✅ 修正：完成模式三所有關卡 - 嚴格判定「有意義的分數」
     */
    private fun checkModeThreeComplete(scoreEntry: ScoreEntry): Boolean {
        // ✅ 關卡一：任意難度達到 1000 分以上
        val hasLevel1 = scoreEntry.level1Easy >= 1000 ||
                scoreEntry.level1Normal >= 1000 ||
                scoreEntry.level1Hard >= 1000

        // ✅ 關卡二：任意難度達到 1000 分以上
        val hasLevel2 = scoreEntry.level2Easy >= 1000 ||
                scoreEntry.level2Normal >= 1000 ||
                scoreEntry.level2Hard >= 1000

        // ✅ 關卡三：達到 100 分以上(表示至少通過 1 個管道)
        val hasLevel3 = scoreEntry.level3Score >= 100

        // ✅ 關卡四：任意歌曲達到 1000 分以上
        val hasLevel4 = scoreEntry.level4Osu01 >= 1000 ||
                scoreEntry.level4Osu02 >= 1000 ||
                scoreEntry.level4Osu03 >= 1000 ||
                scoreEntry.level4Osu04 >= 1000 ||
                scoreEntry.level4Osu05 >= 1000

        val result = hasLevel1 && hasLevel2 && hasLevel3 && hasLevel4

        Log.d("AchievementManager", """
            ✅ 模式三完成者檢查:
            - Level1: $hasLevel1 (E:${scoreEntry.level1Easy} N:${scoreEntry.level1Normal} H:${scoreEntry.level1Hard})
            - Level2: $hasLevel2 (E:${scoreEntry.level2Easy} N:${scoreEntry.level2Normal} H:${scoreEntry.level2Hard})
            - Level3: $hasLevel3 (${scoreEntry.level3Score})
            - Level4: $hasLevel4 (${scoreEntry.level4Total})
            → 結果: $result
        """.trimIndent())

        return result
    }

    /**
     * 🔥 儲存已解鎖的成就到 Firebase
     */
    private suspend fun saveUnlockedAchievements(userId: String, unlockedIds: List<Int>) {
        try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 建立成就資料 Map
            val achievementData = mutableMapOf<String, Any>()
            unlockedIds.forEach { id ->
                achievementData["achievement_$id"] = mapOf(
                    "unlocked" to true,
                    "unlockedDate" to currentDate
                )
            }

            firestore.collection("user_achievements")
                .document(userId)
                .set(achievementData, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Log.d("AchievementManager", "✅ 成就已儲存到 Firebase: $unlockedIds")
        } catch (e: Exception) {
            Log.e("AchievementManager", "❌ 儲存成就失敗", e)
        }
    }

    /**
     * 📥 從 Firebase 讀取已解鎖的成就
     */
    suspend fun loadUnlockedAchievements(userId: String): Map<Int, String> {
        return try {
            val doc = firestore.collection("user_achievements")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val unlockedMap = mutableMapOf<Int, String>()
                for (i in 1..8) {
                    val achievementData = doc.get("achievement_$i") as? Map<*, *>
                    if (achievementData != null && achievementData["unlocked"] == true) {
                        val date = achievementData["unlockedDate"] as? String ?: ""
                        unlockedMap[i] = date
                    }
                }
                Log.d("AchievementManager", "📥 成功讀取成就: $unlockedMap")
                unlockedMap
            } else {
                Log.d("AchievementManager", "📥 該使用者尚無成就資料")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("AchievementManager", "❌ 讀取成就失敗", e)
            emptyMap()
        }
    }
}