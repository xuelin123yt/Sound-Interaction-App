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
    // 預設分數為 0
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
                    // 登出：清空並停止
                    Log.d("RankingRepo", "用戶登出，清空數據")
                    clearScoresAndStopListening()
                }
                currentUser.isAnonymous -> {
                    // ✅ 訪客修正：只停止雲端同步，但「保留」本地記憶體中的分數
                    stopListeningOnly()
                    Log.d("RankingRepo", "訪客模式：停止雲端同步，保留本地暫存分數")
                }
                else -> {
                    // 正式用戶：監聽雲端
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

    private fun listenToUserScores(userId: String) {
        stopListeningOnly()
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

        when (scoreId) {
            // Level 1
            11 -> if (newScore > current.level1Easy) { updatedEntry = current.copy(level1Easy = newScore); isUpdated = true }
            12 -> if (newScore > current.level1Normal) { updatedEntry = current.copy(level1Normal = newScore); isUpdated = true }
            13 -> if (newScore > current.level1Hard) { updatedEntry = current.copy(level1Hard = newScore); isUpdated = true }

            // Level 2
            21 -> if (newScore > current.level2Easy) { updatedEntry = current.copy(level2Easy = newScore); isUpdated = true }
            22 -> if (newScore > current.level2Normal) { updatedEntry = current.copy(level2Normal = newScore); isUpdated = true }
            23 -> if (newScore > current.level2Hard) { updatedEntry = current.copy(level2Hard = newScore); isUpdated = true }

            // Level 3
            3 -> if (newScore > current.level3Score) { updatedEntry = current.copy(level3Score = newScore); isUpdated = true }
        }

        if (isUpdated) {
            // 1. 更新本地 StateFlow (讓 UI 變更，包含訪客)
            _scores.value = updatedEntry

            // 2. 若是正式會員，則上傳雲端
            val currentUser = auth.currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                uploadToCloud(updatedEntry)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}