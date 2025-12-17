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

data class LeaderboardItem(
    val rank: Int = 0,
    val name: String = "載入中...",
    val avatarResId: Int = R.drawable.user,
    val score: Int = 0
)

class LeaderboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // ========== ✅ 新增關卡四的排行榜 ==========
    private val _totalRank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val totalRank = _totalRank.asStateFlow()

    private val _level1Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level1Rank = _level1Rank.asStateFlow()

    private val _level2Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level2Rank = _level2Rank.asStateFlow()

    private val _level3Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level3Rank = _level3Rank.asStateFlow()

    private val _level4Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level4Rank = _level4Rank.asStateFlow()  // ✅ 新增

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 啟動時或打開 Dialog 時呼叫此函式
    fun loadAllLeaderboards() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. 載入各個關卡的排行榜
            loadRankForField("level1Total", _level1Rank)
            loadLevel2Rank()  // 關卡2特殊處理
            loadRankForField("level3Score", _level3Rank)
            loadLevel4Rank()  // ✅ 新增：關卡4特殊處理

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
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id
                val score = doc.getLong(field)?.toInt() ?: 0

                if (score > 0) {
                    val userProfile = fetchUserProfile(userId)
                    itemList.add(
                        LeaderboardItem(
                            name = userProfile.first,
                            avatarResId = userProfile.second,
                            score = score
                        )
                    )
                }
            }

            itemList.sortByDescending { it.score }
            val rankedList = itemList.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

            targetFlow.value = rankedList
            Log.d("Leaderboard", "$field 排行榜載入成功: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading $field: ${e.message}")
        }
    }

    // 關卡2排行榜（需要計算三個難度總分）
    private suspend fun loadLevel2Rank() {
        try {
            val scoreSnapshot = db.collection("user_scores")
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id

                val level2Easy = doc.getLong("level2Easy")?.toInt() ?: 0
                val level2Normal = doc.getLong("level2Normal")?.toInt() ?: 0
                val level2Hard = doc.getLong("level2Hard")?.toInt() ?: 0
                val level2Total = level2Easy + level2Normal + level2Hard

                if (level2Total > 0) {
                    val userProfile = fetchUserProfile(userId)
                    itemList.add(
                        LeaderboardItem(
                            name = userProfile.first,
                            avatarResId = userProfile.second,
                            score = level2Total
                        )
                    )
                }
            }

            itemList.sortByDescending { it.score }
            val rankedList = itemList.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

            _level2Rank.value = rankedList
            Log.d("Leaderboard", "關卡2排行榜載入成功: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading level2: ${e.message}")
        }
    }

    // ========== ✅ 新增：關卡4排行榜（需要計算四個譜面總分） ==========
    private suspend fun loadLevel4Rank() {
        try {
            val scoreSnapshot = db.collection("user_scores")
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id

                // 計算關卡4的四個譜面總分
                val level4Osu01 = doc.getLong("level4Osu01")?.toInt() ?: 0
                val level4Osu02 = doc.getLong("level4Osu02")?.toInt() ?: 0
                val level4Osu03 = doc.getLong("level4Osu03")?.toInt() ?: 0
                val level4Osu04 = doc.getLong("level4Osu04")?.toInt() ?: 0
                val level4Total = level4Osu01 + level4Osu02 + level4Osu03 + level4Osu04

                if (level4Total > 0) {
                    val userProfile = fetchUserProfile(userId)
                    itemList.add(
                        LeaderboardItem(
                            name = userProfile.first,
                            avatarResId = userProfile.second,
                            score = level4Total
                        )
                    )
                }
            }

            itemList.sortByDescending { it.score }
            val rankedList = itemList.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

            _level4Rank.value = rankedList
            Log.d("Leaderboard", "關卡4排行榜載入成功: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading level4: ${e.message}")
        }
    }

    // ========== ✅ 修改：總排行榜加入關卡4 ==========
    private suspend fun loadTotalRank() {
        try {
            val scoreSnapshot = db.collection("user_scores")
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id

                // 關卡1總分
                val l1 = doc.getLong("level1Total")?.toInt() ?: 0

                // 關卡2總分（三個難度相加）
                val l2Easy = doc.getLong("level2Easy")?.toInt() ?: 0
                val l2Normal = doc.getLong("level2Normal")?.toInt() ?: 0
                val l2Hard = doc.getLong("level2Hard")?.toInt() ?: 0
                val l2 = l2Easy + l2Normal + l2Hard

                // 關卡3分數
                val l3 = doc.getLong("level3Score")?.toInt() ?: 0

                // ✅ 關卡4總分（四個譜面相加）
                val l4Osu01 = doc.getLong("level4Osu01")?.toInt() ?: 0
                val l4Osu02 = doc.getLong("level4Osu02")?.toInt() ?: 0
                val l4Osu03 = doc.getLong("level4Osu03")?.toInt() ?: 0
                val l4Osu04 = doc.getLong("level4Osu04")?.toInt() ?: 0
                val l4 = l4Osu01 + l4Osu02 + l4Osu03 + l4Osu04

                // ✅ 總分 = 關卡1 + 關卡2 + 關卡3 + 關卡4
                val total = l1 + l2 + l3 + l4

                if (total > 0) {
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
            val rankedList = itemList.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

            _totalRank.value = rankedList
            Log.d("Leaderboard", "總排行榜載入成功: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading total: ${e.message}")
        }
    }

    private suspend fun fetchUserProfile(userId: String): Pair<String, Int> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val name = userDoc.getString("displayName") ?: "神秘玩家"
            val avatarStr = userDoc.getString("photoUrl")

            val avatarId = avatarStr?.toIntOrNull() ?: R.drawable.user

            Pair(name, avatarId)
        } catch (e: Exception) {
            Pair("未知玩家", R.drawable.user)
        }
    }
}