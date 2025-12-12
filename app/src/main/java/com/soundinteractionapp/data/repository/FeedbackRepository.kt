package com.soundinteractionapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Feedback(
    val title: String = "",
    val description: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val timestamp: String = ""
)

class FeedbackRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun submitFeedback(title: String, description: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("使用者未登入"))
            } else {
                val feedback = Feedback(
                    title = title,
                    description = description,
                    userEmail = currentUser.email ?: "未提供",
                    userId = currentUser.uid,
                    timestamp = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                )

                firestore.collection("feedback")
                    .add(feedback)
                    .await()

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFeedback(): Result<List<Feedback>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("使用者未登入"))
            } else {
                val snapshot = firestore.collection("feedback")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val feedbackList = snapshot.documents.mapNotNull {
                    it.toObject(Feedback::class.java)
                }
                Result.success(feedbackList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}