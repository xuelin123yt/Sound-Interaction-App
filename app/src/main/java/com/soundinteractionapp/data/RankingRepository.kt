package com.soundinteractionapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RankingRepository {
    private val _scores = MutableStateFlow(ScoreEntry())
    val scores: StateFlow<ScoreEntry> = _scores.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        listenToUserScores()
    }

    private fun listenToUserScores() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("user_scores").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val entry = snapshot.toObject(ScoreEntry::class.java)
                    if (entry != null) _scores.value = entry
                }
            }
    }

    fun updateHighScore(scoreId: Int, newScore: Int) {
        val current = _scores.value
        var isUpdated = false
        var updatedEntry = current

        when (scoreId) {
            11 -> if (newScore > current.level1Easy) { updatedEntry = current.copy(level1Easy = newScore); isUpdated = true }
            12 -> if (newScore > current.level1Normal) { updatedEntry = current.copy(level1Normal = newScore); isUpdated = true }
            13 -> if (newScore > current.level1Hard) { updatedEntry = current.copy(level1Hard = newScore); isUpdated = true }
            2 -> if (newScore > current.level2Score) { updatedEntry = current.copy(level2Score = newScore); isUpdated = true }
        }

        if (isUpdated) {
            _scores.value = updatedEntry
            uploadToCloud(updatedEntry)
        }
    }

    private fun uploadToCloud(entry: ScoreEntry) {
        val userId = auth.currentUser?.uid ?: return
        repoScope.launch {
            try {
                db.collection("user_scores").document(userId).set(entry, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("RankingRepo", "上傳失敗", e)
            }
        }
    }
}