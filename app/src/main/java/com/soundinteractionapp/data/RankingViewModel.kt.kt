package com.soundinteractionapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RankingViewModel(private val repository: RankingRepository) : ViewModel() {

    val scores: StateFlow<ScoreEntry> = repository.scores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScoreEntry()
        )

    fun onGameFinished(levelId: Int, finalScore: Int) {
        repository.updateHighScore(levelId, finalScore)
    }
}