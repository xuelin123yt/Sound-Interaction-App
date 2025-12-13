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
                    Log.d("RankingRepo", "用戶登出")
                    clearScoresAndStopListening()
                }
                currentUser.isAnonymous -> {
                    Log.d("RankingRepo", "訪客登入")
                    clearScoresAndStopListening()
                }
                else -> {
                    Log.d("RankingRepo", "正式用戶登入: ${currentUser.uid}")
                    listenToUserScores(currentUser.uid)
                }
            }
        }

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

    private fun clearScoresAndStopListening() {
        scoreListener?.remove()
        scoreListener = null
        _scores.value = ScoreEntry()
        Log.d("RankingRepo", "分數已清空，監聽已停止")
    }

    private fun listenToUserScores(userId: String) {
        scoreListener?.remove()
        scoreListener = db.collection("user_scores").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RankingRepo", "讀取分數錯誤", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val entry = snapshot.toObject(ScoreEntry::class.java)
                    if (entry != null) {
                        _scores.value = entry
                        Log.d("RankingRepo", "分數已同步: $entry")
                    }
                } else {
                    _scores.value = ScoreEntry()
                    Log.d("RankingRepo", "用戶無分數記錄，使用空分數")
                }
            }
    }

    /**
     * 更新最高分
     * @param scoreId 11=L1易, 12=L1中, 13=L1難, 2=L2, 3=L3 (新增)
     */
    fun updateHighScore(scoreId: Int, newScore: Int) {
        val current = _scores.value
        var isUpdated = false
        var updatedEntry = current

        // 1. 判斷是否打破紀錄
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
            // ✅ 新增：Level 3 邏輯
            3 -> {
                if (newScore > current.level3Score) {
                    updatedEntry = current.copy(level3Score = newScore)
                    isUpdated = true
                }
            }
        }

        // 2. 如果打破紀錄，更新本地並檢查是否需要上傳雲端
        if (isUpdated) {
            _scores.value = updatedEntry // 先更新本地

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

    private fun uploadToCloud(entry: ScoreEntry) {
        val userId = auth.currentUser?.uid ?: return
        repoScope.launch {
            try {
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