package com.soundinteractionapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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
                    // 用戶登出
                    Log.d("RankingRepo", "用戶登出")
                    clearScoresAndStopListening()
                }
                currentUser.isAnonymous -> {
                    // 訪客登入
                    Log.d("RankingRepo", "訪客登入")
                    clearScoresAndStopListening()
                }
                else -> {
                    // 正式用戶登入
                    Log.d("RankingRepo", "正式用戶登入: ${currentUser.uid}")
                    listenToUserScores(currentUser.uid)
                }
            }
        }

        // 初始化時檢查當前狀態
        val currentUser = auth.currentUser
        when {
            currentUser == null || currentUser.isAnonymous -> {
                _scores.value = ScoreEntry()
            }
            else -> {
                listenToUserScores(currentUser.uid)
            }
        }
    }

    /**
     * ✅ 清空分數並停止監聽
     */
    private fun clearScoresAndStopListening() {
        // 停止 Firestore 監聽
        scoreListener?.remove()
        scoreListener = null

        // ✅ 清空本地分數（這是關鍵！）
        _scores.value = ScoreEntry()
        Log.d("RankingRepo", "分數已清空，監聽已停止")
    }

    /**
     * 監聽指定用戶的分數變化
     */
    private fun listenToUserScores(userId: String) {
        // 先停止之前的監聽
        scoreListener?.remove()

        // 監聽路徑: user_scores/{userId}
        scoreListener = db.collection("user_scores").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RankingRepo", "讀取分數錯誤", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // 將雲端資料轉成我們的 ScoreEntry
                    val entry = snapshot.toObject(ScoreEntry::class.java)
                    if (entry != null) {
                        _scores.value = entry
                        Log.d("RankingRepo", "分數已同步: $entry")
                    }
                } else {
                    // 文件不存在，使用空分數
                    _scores.value = ScoreEntry()
                    Log.d("RankingRepo", "用戶無分數記錄，使用空分數")
                }
            }
    }

    /**
     * 更新最高分 (會比較舊分數，只有更高時才上傳)
     * @param scoreId 對應 Difficulty Enum 中的 ID (11, 12, 13, 2)
     */
    fun updateHighScore(scoreId: Int, newScore: Int) {
        val current = _scores.value
        var isUpdated = false
        var updatedEntry = current

        // 1. 判斷是否打破紀錄 (根據 scoreId 判斷是哪個難度)
        when (scoreId) {
            11 -> { // Level 1 簡單
                if (newScore > current.level1Easy) {
                    updatedEntry = current.copy(level1Easy = newScore)
                    isUpdated = true
                }
            }
            12 -> { // Level 1 普通
                if (newScore > current.level1Normal) {
                    updatedEntry = current.copy(level1Normal = newScore)
                    isUpdated = true
                }
            }
            13 -> { // Level 1 困難
                if (newScore > current.level1Hard) {
                    updatedEntry = current.copy(level1Hard = newScore)
                    isUpdated = true
                }
            }
            2 -> { // Level 2
                if (newScore > current.level2Score) {
                    updatedEntry = current.copy(level2Score = newScore)
                    isUpdated = true
                }
            }
        }

        // 2. 如果打破紀錄，更新本地並檢查是否需要上傳雲端
        if (isUpdated) {
            _scores.value = updatedEntry // 先更新本地，讓畫面變快 (訪客也會看到變化)

            val currentUser = auth.currentUser

            // ★★★ 關鍵判斷：只有「非 null」且「非訪客」才上傳 ★★★
            if (currentUser != null && !currentUser.isAnonymous) {
                uploadToCloud(updatedEntry)
                Log.d("RankingRepo", "正式會員，分數已上傳雲端")
            } else {
                Log.d("RankingRepo", "訪客模式，分數僅暫存本地，不上傳")
            }
        }
    }

    // 上傳到 Firebase
    private fun uploadToCloud(entry: ScoreEntry) {
        val userId = auth.currentUser?.uid ?: return // 沒登入就不上傳

        repoScope.launch {
            try {
                // 使用 merge，避免覆蓋掉未來可能新增的其他欄位
                db.collection("user_scores").document(userId)
                    .set(entry, SetOptions.merge())
                    .await()
                Log.d("RankingRepo", "分數上傳成功!")
            } catch (e: Exception) {
                Log.e("RankingRepo", "分數上傳失敗", e)
            }
        }
    }
}