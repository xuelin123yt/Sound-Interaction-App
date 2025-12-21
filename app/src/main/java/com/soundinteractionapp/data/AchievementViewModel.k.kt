package com.soundinteractionapp.screens.profile.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 成就系統的 ViewModel
 * 負責載入和更新成就狀態
 */
class AchievementViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val achievementManager = AchievementManager()
    private val feedbackRepository = FeedbackRepository()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 載入並更新成就狀態
     * @param rankingViewModel 分數資料來源
     * @param hasAvatar 是否已設定頭像
     */
    fun loadAchievements(rankingViewModel: RankingViewModel, hasAvatar: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = auth.currentUser
                if (currentUser == null || currentUser.isAnonymous) {
                    Log.d("AchievementVM", "訪客模式，顯示預設成就列表")
                    _achievements.value = AchievementProvider.getAllAchievements()
                    _isLoading.value = false
                    return@launch
                }

                // 1. 取得分數資料
                val scoreEntry = rankingViewModel.scores.value

                // 2. 檢查是否有提交過意見回饋
                val hasFeedback = try {
                    val feedbackResult = feedbackRepository.getUserFeedback()
                    feedbackResult.isSuccess && feedbackResult.getOrNull()?.isNotEmpty() == true
                } catch (e: Exception) {
                    Log.e("AchievementVM", "檢查意見回饋失敗", e)
                    false
                }

                // 3. 檢查並解鎖成就
                achievementManager.checkAndUnlockAchievements(
                    scoreEntry = scoreEntry,
                    hasFeedback = hasFeedback,
                    hasAvatar = hasAvatar
                )

                // 4. 從 Firebase 讀取已解鎖的成就
                val unlockedMap = achievementManager.loadUnlockedAchievements(currentUser.uid)

                // 5. 更新成就列表
                val allAchievements = AchievementProvider.getAllAchievements()
                val updatedAchievements = allAchievements.map { achievement ->
                    val unlockedDate = unlockedMap[achievement.id]
                    if (unlockedDate != null) {
                        achievement.copy(isUnlocked = true, unlockedDate = unlockedDate)
                    } else {
                        achievement
                    }
                }

                _achievements.value = updatedAchievements

                Log.d("AchievementVM", "成就載入完成：${updatedAchievements.count { it.isUnlocked }}/${updatedAchievements.size} 已解鎖")
            } catch (e: Exception) {
                Log.e("AchievementVM", "載入成就失敗", e)
                _achievements.value = AchievementProvider.getAllAchievements()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 手動刷新成就狀態
     */
    fun refreshAchievements(rankingViewModel: RankingViewModel, hasAvatar: Boolean) {
        loadAchievements(rankingViewModel, hasAvatar)
    }
}