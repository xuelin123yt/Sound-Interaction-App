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

// 這是給 UI 顯示用的資料結構
data class LeaderboardItem(
    val rank: Int = 0,
    val name: String = "載入中...",
    val avatarResId: Int = R.drawable.avatar_01, // 預設頭像
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
            // 根據截圖，欄位名稱分別是: level1Total, level2Score, level3Score
            loadRankForField("level1Total", _level1Rank)
            loadRankForField("level2Score", _level2Rank) // 雖然目前可能沒資料，但寫好邏輯
            loadRankForField("level3Score", _level3Rank)

            // 2. 總排行榜 (特別處理)
            // 因為 Firestore 沒有存 "grandTotal"，我們暫時用 level1Total 的前 20 名來計算
            // 最佳解法是在上傳分數時，多算一個 grandTotal 欄位存進去
            loadTotalRank()

            _isLoading.value = false
        }
    }

    private suspend fun loadRankForField(
        field: String,
        targetFlow: MutableStateFlow<List<LeaderboardItem>>
    ) {
        try {
            // 步驟 A: 抓取分數 (只抓前 20 名)
            val scoreSnapshot = db.collection("user_scores")
                .orderBy(field, Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val itemList = mutableListOf<LeaderboardItem>()

            // 步驟 B: 針對每一筆分數，去抓取使用者的名字
            scoreSnapshot.documents.forEachIndexed { index, doc ->
                val userId = doc.id // 取得 User ID
                val score = doc.getLong(field)?.toInt() ?: 0

                if (score > 0) { // 只顯示分數大於 0 的人
                    // 根據 User ID 去 users 集合抓名字
                    val userProfile = fetchUserProfile(userId)

                    itemList.add(
                        LeaderboardItem(
                            rank = index + 1,
                            name = userProfile.first, // 名字
                            avatarResId = userProfile.second, // 頭像 ID
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

    // 總排行榜邏輯 (目前先簡單加總，未來建議資料庫加欄位)
    private suspend fun loadTotalRank() {
        try {
            // 這裡我們先抓取 level1 分數最高的人，然後把他們的三個分數加總
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
            // 在記憶體中重新排序總分
            itemList.sortByDescending { it.score }
            // 重新標記名次
            val rankedList = itemList.mapIndexed { index, item -> item.copy(rank = index + 1) }

            _totalRank.value = rankedList

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error loading total: ${e.message}")
        }
    }

    // 輔助函式：去 users 集合抓個資
    private suspend fun fetchUserProfile(userId: String): Pair<String, Int> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val name = userDoc.getString("displayName") ?: "神秘玩家"
            // 假設你的 photoUrl 存的是 String 格式的 Resource ID (例如 "213123...")
            val avatarStr = userDoc.getString("photoUrl")
            val avatarId = avatarStr?.toIntOrNull() ?: R.drawable.avatar_01
            Pair(name, avatarId)
        } catch (e: Exception) {
            Pair("未知玩家", R.drawable.avatar_01)
        }
    }
}