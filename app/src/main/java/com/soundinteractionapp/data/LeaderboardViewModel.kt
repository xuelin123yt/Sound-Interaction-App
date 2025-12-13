package com.soundinteractionapp.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.soundinteractionapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ 修改：預設 avatarResId 改為 R.drawable.user
data class LeaderboardItem(
    val rank: Int = 0,
    val name: String = "載入中...",
    val avatarResId: Int = R.drawable.user,
    val score: Int = 0
)

class LeaderboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 四個排行榜的資料狀態
    private val _totalRank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val totalRank = _totalRank.asStateFlow()

    private val _level1Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level1Rank = _level1Rank.asStateFlow()

    private val _level2Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level2Rank = _level2Rank.asStateFlow()

    private val _level3Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level3Rank = _level3Rank.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 啟動時或打開 Dialog 時呼叫此函式
    fun loadAllLeaderboards() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. 載入各個關卡的排行榜
            loadRankForField("level1Total", _level1Rank)
            loadRankForField("level2Score", _level2Rank)
            loadRankForField("level3Score", _level3Rank)

            // 2. 總排行榜 (特別處理)
            loadTotalRank()

            _isLoading.value = false
        }
    }

    private suspend fun loadRankForField(
        field: String,
        targetFlow: MutableStateFlow<List<LeaderboardItem>>
    ) {
        try {
            val scoreSnapshot = db.collection("user_scores")
                .orderBy(field, Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEachIndexed { index, doc ->
                val userId = doc.id
                val score = doc.getLong(field)?.toInt() ?: 0

                if (score > 0) {
                    val userProfile = fetchUserProfile(userId)
                    itemList.add(
                        LeaderboardItem(
                            rank = index + 1,
                            name = userProfile.first,
                            avatarResId = userProfile.second,
                            score = score
                        )
                    )
                }
            }
            targetFlow.value = itemList

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading $field: ${e.message}")
        }
    }

    private suspend fun loadTotalRank() {
        try {
            val scoreSnapshot = db.collection("user_scores")
                .orderBy("level1Total", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val l1 = doc.getLong("level1Total")?.toInt() ?: 0
                val l2 = doc.getLong("level2Score")?.toInt() ?: 0
                val l3 = doc.getLong("level3Score")?.toInt() ?: 0
                val total = l1 + l2 + l3

                if (total > 0) {
                    val userId = doc.id
                    val userProfile = fetchUserProfile(userId)

                    itemList.add(
                        LeaderboardItem(
                            name = userProfile.first,
                            avatarResId = userProfile.second,
                            score = total
                        )
                    )
                }
            }
            itemList.sortByDescending { it.score }
            val rankedList = itemList.mapIndexed { index, item -> item.copy(rank = index + 1) }

            _totalRank.value = rankedList

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading total: ${e.message}")
        }
    }

    // ✅ 修改：若抓不到圖片或格式錯誤，統一回傳 R.drawable.user
    private suspend fun fetchUserProfile(userId: String): Pair<String, Int> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val name = userDoc.getString("displayName") ?: "神秘玩家"
            val avatarStr = userDoc.getString("photoUrl")

            // 嘗試轉換，失敗或是 null 則使用預設 user 圖片
            val avatarId = avatarStr?.toIntOrNull() ?: R.drawable.user

            Pair(name, avatarId)
        } catch (e: Exception) {
            Pair("未知玩家", R.drawable.user)
        }
    }
}