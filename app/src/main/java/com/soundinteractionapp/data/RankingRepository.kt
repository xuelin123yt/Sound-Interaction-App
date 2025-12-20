package com.soundinteractionapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RankingRepository {
    private val _scores = MutableStateFlow(ScoreEntry())
    val scores: StateFlow<ScoreEntry> = _scores.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repoScope = CoroutineScope(Dispatchers.IO)
    private var scoreListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser

            when {
                currentUser == null -> {
                    Log.d("RankingRepo", "用戶登出，清空數據")
                    clearScoresAndStopListening()
                }
                currentUser.isAnonymous -> {
                    stopListeningOnly()
                    Log.d("RankingRepo", "訪客模式：停止雲端同步，保留本地暫存分數")
                }
                else -> {
                    Log.d("RankingRepo", "正式用戶登入: ${currentUser.uid}")
                    listenToUserScores(currentUser.uid)
                }
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            listenToUserScores(currentUser.uid)
        }
    }

    private fun stopListeningOnly() {
        scoreListener?.remove()
        scoreListener = null
    }

    private fun clearScoresAndStopListening() {
        stopListeningOnly()
        _scores.value = ScoreEntry()
    }

    /**
     * ✅ 修復：從雲端拉取數據時，對每個分數欄位取最大值
     * 確保本地已有的更高分數不會被雲端舊數據覆蓋
     */
    private fun listenToUserScores(userId: String) {
        stopListeningOnly()
        scoreListener = db.collection("user_scores").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RankingRepo", "讀取分數錯誤", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val cloudEntry = snapshot.toObject(ScoreEntry::class.java)
                    if (cloudEntry != null) {
                        val localEntry = _scores.value

                        // ✅ 對每個欄位取最大值，確保保留最高分
                        val mergedEntry = ScoreEntry(
                            level1Easy = maxOf(localEntry.level1Easy, cloudEntry.level1Easy),
                            level1Normal = maxOf(localEntry.level1Normal, cloudEntry.level1Normal),
                            level1Hard = maxOf(localEntry.level1Hard, cloudEntry.level1Hard),

                            level2Easy = maxOf(localEntry.level2Easy, cloudEntry.level2Easy),
                            level2Normal = maxOf(localEntry.level2Normal, cloudEntry.level2Normal),
                            level2Hard = maxOf(localEntry.level2Hard, cloudEntry.level2Hard),

                            level3Score = maxOf(localEntry.level3Score, cloudEntry.level3Score),

                            level4Osu01 = maxOf(localEntry.level4Osu01, cloudEntry.level4Osu01),
                            level4Osu02 = maxOf(localEntry.level4Osu02, cloudEntry.level4Osu02),
                            level4Osu03 = maxOf(localEntry.level4Osu03, cloudEntry.level4Osu03),
                            level4Osu04 = maxOf(localEntry.level4Osu04, cloudEntry.level4Osu04),
                            level4Osu05 = maxOf(localEntry.level4Osu05, cloudEntry.level4Osu05)
                        )

                        _scores.value = mergedEntry

                        Log.d("RankingRepo", "📥 雲端數據已合併（保留最高分）")
                        Log.d("RankingRepo", "   Level4 OSU_01: 本地=${localEntry.level4Osu01}, 雲端=${cloudEntry.level4Osu01}, 最終=${mergedEntry.level4Osu01}")
                        Log.d("RankingRepo", "   Level4 OSU_02: 本地=${localEntry.level4Osu02}, 雲端=${cloudEntry.level4Osu02}, 最終=${mergedEntry.level4Osu02}")
                        Log.d("RankingRepo", "   Level4 OSU_03: 本地=${localEntry.level4Osu03}, 雲端=${cloudEntry.level4Osu03}, 最終=${mergedEntry.level4Osu03}")
                        Log.d("RankingRepo", "   Level4 OSU_04: 本地=${localEntry.level4Osu04}, 雲端=${cloudEntry.level4Osu04}, 最終=${mergedEntry.level4Osu04}")
                        Log.d("RankingRepo", "   Level4 OSU_05: 本地=${localEntry.level4Osu05}, 雲端=${cloudEntry.level4Osu05}, 最終=${mergedEntry.level4Osu05}")
                        Log.d("RankingRepo", "   Level4 Total: ${mergedEntry.level4Total}")

                        // ✅ 如果合併後的數據與雲端不同，需要更新雲端
                        if (mergedEntry != cloudEntry) {
                            Log.d("RankingRepo", "☁️ 本地有更高分數，更新雲端...")
                            syncMergedScoresToCloud(mergedEntry)
                        }
                    }
                }
            }
    }

    /**
     * ✅ 核心邏輯：只記錄歷史最高分
     * 只有當 newScore > current.score 時才更新
     */
    fun updateHighScore(scoreId: Int, newScore: Int) {
        val current = _scores.value
        var isUpdated = false
        var updatedEntry = current
        var fieldName = ""

        Log.d("RankingRepo", "=" .repeat(60))
        Log.d("RankingRepo", "📊 updateHighScore 被呼叫: scoreId=$scoreId, newScore=$newScore")

        when (scoreId) {
            // Level 1
            11 -> if (newScore > current.level1Easy) {
                updatedEntry = current.copy(level1Easy = newScore)
                fieldName = "level1Easy"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level1 Easy: ${current.level1Easy} → $newScore")
            }
            12 -> if (newScore > current.level1Normal) {
                updatedEntry = current.copy(level1Normal = newScore)
                fieldName = "level1Normal"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level1 Normal: ${current.level1Normal} → $newScore")
            }
            13 -> if (newScore > current.level1Hard) {
                updatedEntry = current.copy(level1Hard = newScore)
                fieldName = "level1Hard"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level1 Hard: ${current.level1Hard} → $newScore")
            }

            // Level 2
            21 -> if (newScore > current.level2Easy) {
                updatedEntry = current.copy(level2Easy = newScore)
                fieldName = "level2Easy"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level2 Easy: ${current.level2Easy} → $newScore")
            }
            22 -> if (newScore > current.level2Normal) {
                updatedEntry = current.copy(level2Normal = newScore)
                fieldName = "level2Normal"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level2 Normal: ${current.level2Normal} → $newScore")
            }
            23 -> if (newScore > current.level2Hard) {
                updatedEntry = current.copy(level2Hard = newScore)
                fieldName = "level2Hard"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level2 Hard: ${current.level2Hard} → $newScore")
            }

            // Level 3
            3 -> if (newScore > current.level3Score) {
                updatedEntry = current.copy(level3Score = newScore)
                fieldName = "level3Score"
                isUpdated = true
                Log.d("RankingRepo", "✅ Level3: ${current.level3Score} → $newScore")
            }

            // ========== ✅ Level 4 的五個譜面 ==========
            41 -> if (newScore > current.level4Osu01) {
                updatedEntry = current.copy(level4Osu01 = newScore)
                fieldName = "level4Osu01"
                isUpdated = true
                Log.d("RankingRepo", "✅ OSU_01: ${current.level4Osu01} → $newScore")
            } else {
                Log.d("RankingRepo", "⚠️ OSU_01: $newScore ≤ ${current.level4Osu01}，不更新")
            }

            42 -> if (newScore > current.level4Osu02) {
                updatedEntry = current.copy(level4Osu02 = newScore)
                fieldName = "level4Osu02"
                isUpdated = true
                Log.d("RankingRepo", "✅ OSU_02: ${current.level4Osu02} → $newScore")
            } else {
                Log.d("RankingRepo", "⚠️ OSU_02: $newScore ≤ ${current.level4Osu02}，不更新")
            }

            43 -> if (newScore > current.level4Osu03) {
                updatedEntry = current.copy(level4Osu03 = newScore)
                fieldName = "level4Osu03"
                isUpdated = true
                Log.d("RankingRepo", "✅ OSU_03: ${current.level4Osu03} → $newScore")
            } else {
                Log.d("RankingRepo", "⚠️ OSU_03: $newScore ≤ ${current.level4Osu03}，不更新")
            }

            44 -> if (newScore > current.level4Osu04) {
                updatedEntry = current.copy(level4Osu04 = newScore)
                fieldName = "level4Osu04"
                isUpdated = true
                Log.d("RankingRepo", "✅ OSU_04: ${current.level4Osu04} → $newScore")
            } else {
                Log.d("RankingRepo", "⚠️ OSU_04: $newScore ≤ ${current.level4Osu04}，不更新")
            }

            45 -> if (newScore > current.level4Osu05) {
                updatedEntry = current.copy(level4Osu05 = newScore)
                fieldName = "level4Osu05"
                isUpdated = true
                Log.d("RankingRepo", "✅ OSU_05: ${current.level4Osu05} → $newScore")
            } else {
                Log.d("RankingRepo", "⚠️ OSU_05: $newScore ≤ ${current.level4Osu05}，不更新")
            }
        }

        if (isUpdated) {
            // 1. 更新本地 StateFlow
            _scores.value = updatedEntry
            Log.d("RankingRepo", "💾 本地狀態已更新")
            Log.d("RankingRepo", "   Level4 Total: ${updatedEntry.level4Total}")

            // 2. 若是正式會員，則上傳雲端
            val currentUser = auth.currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                uploadScoreToCloud(fieldName, newScore, updatedEntry)
            }
        }

        Log.d("RankingRepo", "=" .repeat(60))
    }

    /**
     * ✅ 修復：上傳完整的分數數據到雲端
     * 確保所有欄位都正確同步，特別是總分
     */
    private fun uploadScoreToCloud(fieldName: String, newScore: Int, entry: ScoreEntry) {
        val userId = auth.currentUser?.uid ?: return
        repoScope.launch {
            try {
                // ✅ 改為上傳完整的分數數據，確保總分正確
                val dataMap = mapOf(
                    "level1Easy" to entry.level1Easy,
                    "level1Normal" to entry.level1Normal,
                    "level1Hard" to entry.level1Hard,
                    "level1Total" to entry.level1Total,

                    "level2Easy" to entry.level2Easy,
                    "level2Normal" to entry.level2Normal,
                    "level2Hard" to entry.level2Hard,
                    "level2Total" to entry.level2Total,

                    "level3Score" to entry.level3Score,

                    "level4Osu01" to entry.level4Osu01,
                    "level4Osu02" to entry.level4Osu02,
                    "level4Osu03" to entry.level4Osu03,
                    "level4Osu04" to entry.level4Osu04,
                    "level4Osu05" to entry.level4Osu05,
                    "level4Total" to entry.level4Total
                )

                db.collection("user_scores").document(userId)
                    .set(dataMap)
                    .await()

                Log.d("RankingRepo", "☁️ ✅ 分數上傳成功")
                Log.d("RankingRepo", "   $fieldName = $newScore")
                Log.d("RankingRepo", "   Level4 Total = ${entry.level4Total}")
            } catch (e: Exception) {
                Log.e("RankingRepo", "☁️ ❌ 上傳失敗", e)
            }
        }
    }

    /**
     * ✅ 新增：將合併後的完整分數同步到雲端
     */
    private fun syncMergedScoresToCloud(entry: ScoreEntry) {
        val userId = auth.currentUser?.uid ?: return
        repoScope.launch {
            try {
                val dataMap = mapOf(
                    "level1Easy" to entry.level1Easy,
                    "level1Normal" to entry.level1Normal,
                    "level1Hard" to entry.level1Hard,
                    "level1Total" to entry.level1Total,

                    "level2Easy" to entry.level2Easy,
                    "level2Normal" to entry.level2Normal,
                    "level2Hard" to entry.level2Hard,
                    "level2Total" to entry.level2Total,

                    "level3Score" to entry.level3Score,

                    "level4Osu01" to entry.level4Osu01,
                    "level4Osu02" to entry.level4Osu02,
                    "level4Osu03" to entry.level4Osu03,
                    "level4Osu04" to entry.level4Osu04,
                    "level4Osu05" to entry.level4Osu05,
                    "level4Total" to entry.level4Total
                )

                db.collection("user_scores").document(userId)
                    .set(dataMap)
                    .await()

                Log.d("RankingRepo", "☁️ ✅ 合併後的分數已同步到雲端")
            } catch (e: Exception) {
                Log.e("RankingRepo", "☁️ ❌ 同步失敗", e)
            }
        }
    }
}