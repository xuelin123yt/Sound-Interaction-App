package com.soundinteractionapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RankingViewModel(private val repository: RankingRepository = RankingRepository()) :
    ViewModel() {

    // ✅ 直接使用 Repository 的 scores，不需要額外的邏輯
    val scores: StateFlow<ScoreEntry> = repository.scores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScoreEntry()
        )

    /**
     * 遊戲結束時更新分數
     */
    fun onGameFinished(levelId: Int, finalScore: Int) {
        repository.updateHighScore(levelId, finalScore)
    }
}