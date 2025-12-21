package com.soundinteractionapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {
    private val repository = RankingRepository()

    val scores: StateFlow<ScoreEntry> = repository.scores

    /**
     * 更新最高分
     */
    fun updateHighScore(scoreId: Int, newScore: Int) {
        viewModelScope.launch {
            repository.updateHighScore(scoreId, newScore)
        }
    }

    /**
     * ✅ 新增：更新關卡二的最高 Combo
     */
    fun updateLevel2MaxCombo(newCombo: Int) {
        viewModelScope.launch {
            repository.updateLevel2MaxCombo(newCombo)
        }
    }

    /**
     * ✅ 新增：標記關卡四已達成「無 Miss」
     */
    fun markLevel4NoMiss() {
        viewModelScope.launch {
            repository.markLevel4NoMiss()
        }
    }

    /**
     * [保留] Level 1/3 舊有的呼叫方式 (相容性)
     */
    fun onGameFinished(levelId: Int, finalScore: Int) {
        updateHighScore(levelId, finalScore)
    }
}