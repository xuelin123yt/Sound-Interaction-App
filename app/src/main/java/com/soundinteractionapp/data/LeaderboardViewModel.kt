package com.soundinteractionapp.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.soundinteractionapp.R

data class LeaderboardItem(
    val rank: Int = 0,
    val name: String = "載入中...",
    val avatarResId: Int = R.drawable.user,
    val score: Int = 0
)

class LeaderboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalRank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val totalRank = _totalRank.asStateFlow()

    private val _level1Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level1Rank = _level1Rank.asStateFlow()

    private val _level2Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level2Rank = _level2Rank.asStateFlow()

    private val _level3Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level3Rank = _level3Rank.asStateFlow()

    private val _level4Rank = MutableStateFlow<List<LeaderboardItem>>(emptyList())
    val level4Rank = _level4Rank.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ✅ 頭像映射表（字串編號 → 實際資源 ID）
    private val avatarMap = mapOf(
        "1" to R.drawable.avatar_01, "2" to R.drawable.avatar_02,
        "3" to R.drawable.avatar_03, "4" to R.drawable.avatar_04,
        "5" to R.drawable.avatar_05, "6" to R.drawable.avatar_06,
        "7" to R.drawable.avatar_07, "8" to R.drawable.avatar_08,
        "9" to R.drawable.avatar_09, "10" to R.drawable.avatar_10,
        "11" to R.drawable.avatar_11, "12" to R.drawable.avatar_12,
        "13" to R.drawable.avatar_13, "14" to R.drawable.avatar_14,
        "15" to R.drawable.avatar_15, "16" to R.drawable.avatar_16,
        "17" to R.drawable.avatar_17, "18" to R.drawable.avatar_18,
        "19" to R.drawable.avatar_19, "20" to R.drawable.avatar_20,
        "21" to R.drawable.avatar_21, "22" to R.drawable.avatar_22,
        "23" to R.drawable.avatar_23, "24" to R.drawable.avatar_24
    )

    // ✅ 修復：整個函數用 try-catch 包裹，防止崩潰
    fun loadAllLeaderboards() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("LeaderboardVM", "開始載入排行榜...")

                // ✅ 修復：一次性讀取所有數據，避免多次查詢
                val scoreSnapshot = db.collection("user_scores")
                    .get()
                    .await()

                Log.d("LeaderboardVM", "✅ 成功取得 ${scoreSnapshot.size()} 筆分數資料")

                // ✅ 建立用戶資料快取，避免重複查詢
                val userCache = mutableMapOf<String, Pair<String, Int>>()

                // ✅ 預先載入所有用戶資料
                scoreSnapshot.documents.forEach { doc ->
                    val userId = doc.id
                    if (!userCache.containsKey(userId)) {
                        userCache[userId] = fetchUserProfile(userId)
                    }
                }

                Log.d("LeaderboardVM", "✅ 成功載入 ${userCache.size} 個用戶資料")

                // ✅ 處理各關卡排行榜（使用快取資料）
                processLevel1Rank(scoreSnapshot, userCache)
                processLevel2Rank(scoreSnapshot, userCache)
                processLevel3Rank(scoreSnapshot, userCache)
                processLevel4Rank(scoreSnapshot, userCache)
                processTotalRank(scoreSnapshot, userCache)

                Log.d("LeaderboardVM", "✅ 所有排行榜載入完成")

            } catch (e: Exception) {
                Log.e("LeaderboardVM", "❌ 載入排行榜失敗: ${e.message}", e)
                // 發生錯誤時清空所有列表
                _totalRank.value = emptyList()
                _level1Rank.value = emptyList()
                _level2Rank.value = emptyList()
                _level3Rank.value = emptyList()
                _level4Rank.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ 關卡1排行榜
    private fun processLevel1Rank(
        scoreSnapshot: com.google.firebase.firestore.QuerySnapshot,
        userCache: Map<String, Pair<String, Int>>
    ) {
        try {
            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id
                val score = doc.getLong("level1Total")?.toInt() ?: 0

                if (score > 0) {
                    val userProfile = userCache[userId] ?: Pair("未知玩家", R.drawable.user)
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

            _level1Rank.value = rankedList
            Log.d("LeaderboardVM", "關卡1排行榜: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "關卡1處理失敗: ${e.message}")
            _level1Rank.value = emptyList()
        }
    }

    // ✅ 關卡2排行榜（三個難度總分）
    private fun processLevel2Rank(
        scoreSnapshot: com.google.firebase.firestore.QuerySnapshot,
        userCache: Map<String, Pair<String, Int>>
    ) {
        try {
            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id
                val level2Easy = doc.getLong("level2Easy")?.toInt() ?: 0
                val level2Normal = doc.getLong("level2Normal")?.toInt() ?: 0
                val level2Hard = doc.getLong("level2Hard")?.toInt() ?: 0
                val level2Total = level2Easy + level2Normal + level2Hard

                if (level2Total > 0) {
                    val userProfile = userCache[userId] ?: Pair("未知玩家", R.drawable.user)
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
            Log.d("LeaderboardVM", "關卡2排行榜: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "關卡2處理失敗: ${e.message}")
            _level2Rank.value = emptyList()
        }
    }

    // ✅ 關卡3排行榜
    private fun processLevel3Rank(
        scoreSnapshot: com.google.firebase.firestore.QuerySnapshot,
        userCache: Map<String, Pair<String, Int>>
    ) {
        try {
            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id
                val score = doc.getLong("level3Score")?.toInt() ?: 0

                if (score > 0) {
                    val userProfile = userCache[userId] ?: Pair("未知玩家", R.drawable.user)
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

            _level3Rank.value = rankedList
            Log.d("LeaderboardVM", "關卡3排行榜: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "關卡3處理失敗: ${e.message}")
            _level3Rank.value = emptyList()
        }
    }

    // ✅ 關卡4排行榜（四個譜面總分）
    private fun processLevel4Rank(
        scoreSnapshot: com.google.firebase.firestore.QuerySnapshot,
        userCache: Map<String, Pair<String, Int>>
    ) {
        try {
            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id
                val level4Osu01 = doc.getLong("level4Osu01")?.toInt() ?: 0
                val level4Osu02 = doc.getLong("level4Osu02")?.toInt() ?: 0
                val level4Osu03 = doc.getLong("level4Osu03")?.toInt() ?: 0
                val level4Osu04 = doc.getLong("level4Osu04")?.toInt() ?: 0
                val level4Total = level4Osu01 + level4Osu02 + level4Osu03 + level4Osu04

                if (level4Total > 0) {
                    val userProfile = userCache[userId] ?: Pair("未知玩家", R.drawable.user)
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
            Log.d("LeaderboardVM", "關卡4排行榜: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "關卡4處理失敗: ${e.message}")
            _level4Rank.value = emptyList()
        }
    }

    // ✅ 總排行榜（所有關卡總分）
    private fun processTotalRank(
        scoreSnapshot: com.google.firebase.firestore.QuerySnapshot,
        userCache: Map<String, Pair<String, Int>>
    ) {
        try {
            val itemList = mutableListOf<LeaderboardItem>()

            scoreSnapshot.documents.forEach { doc ->
                val userId = doc.id

                // 關卡1
                val l1 = doc.getLong("level1Total")?.toInt() ?: 0

                // 關卡2（三個難度）
                val l2Easy = doc.getLong("level2Easy")?.toInt() ?: 0
                val l2Normal = doc.getLong("level2Normal")?.toInt() ?: 0
                val l2Hard = doc.getLong("level2Hard")?.toInt() ?: 0
                val l2 = l2Easy + l2Normal + l2Hard

                // 關卡3
                val l3 = doc.getLong("level3Score")?.toInt() ?: 0

                // 關卡4（四個譜面）
                val l4Osu01 = doc.getLong("level4Osu01")?.toInt() ?: 0
                val l4Osu02 = doc.getLong("level4Osu02")?.toInt() ?: 0
                val l4Osu03 = doc.getLong("level4Osu03")?.toInt() ?: 0
                val l4Osu04 = doc.getLong("level4Osu04")?.toInt() ?: 0
                val l4 = l4Osu01 + l4Osu02 + l4Osu03 + l4Osu04

                // 總分
                val total = l1 + l2 + l3 + l4

                if (total > 0) {
                    val userProfile = userCache[userId] ?: Pair("未知玩家", R.drawable.user)
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
            Log.d("LeaderboardVM", "總排行榜: ${rankedList.size} 筆")

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "總排行榜處理失敗: ${e.message}")
            _totalRank.value = emptyList()
        }
    }

    // ✅ 取得用戶資料（修復頭像資源 ID 轉換）
    private suspend fun fetchUserProfile(userId: String): Pair<String, Int> {
        return try {
            val userDoc = db.collection("users")
                .document(userId)
                .get()
                .await()

            val name = userDoc.getString("displayName") ?: "神秘玩家"
            val photoUrl = userDoc.getString("photoUrl") ?: ""

            // ✅ 關鍵修復：正確轉換頭像資源 ID
            val avatarId = when {
                // 如果 photoUrl 是資源 ID（數字很大，如 2131230745）
                photoUrl.toIntOrNull()?.let { it > 100000 } == true -> {
                    val resourceId = photoUrl.toInt()
                    // 驗證資源 ID 是否有效
                    if (avatarMap.values.contains(resourceId)) {
                        resourceId
                    } else {
                        R.drawable.user
                    }
                }
                // 如果 photoUrl 是編號字串（如 "11", "5"）
                avatarMap.containsKey(photoUrl) -> {
                    avatarMap[photoUrl]!!
                }
                // 否則使用預設頭像
                else -> R.drawable.user
            }

            Log.d("LeaderboardVM", "用戶 $userId: name=$name, photoUrl=$photoUrl, avatarId=$avatarId")
            Pair(name, avatarId)

        } catch (e: Exception) {
            Log.e("LeaderboardVM", "取得用戶資料失敗 ($userId): ${e.message}")
            Pair("未知玩家", R.drawable.user)
        }
    }
}