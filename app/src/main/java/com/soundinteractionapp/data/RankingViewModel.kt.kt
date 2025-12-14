package com.soundinteractionapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {
    // 初始化 Repository
    private val repository = RankingRepository()

    // 將 Repository 的分數狀態暴露給 UI 觀察
    val scores: StateFlow<ScoreEntry> = repository.scores

    /**
     * ✅ [關鍵修正] 新增這個函式！
     * Level 2 的 UI 就是在呼叫這個函式，加上去後錯誤就會消失。
     */
    fun updateHighScore(scoreId: Int, newScore: Int) {
        viewModelScope.launch {
            repository.updateHighScore(scoreId, newScore)
        }
    }

    /**
     * [保留] Level 1 舊有的呼叫方式 (相容性)
     */
    fun onGameFinished(levelId: Int, finalScore: Int) {
        updateHighScore(levelId, finalScore)
    }
}