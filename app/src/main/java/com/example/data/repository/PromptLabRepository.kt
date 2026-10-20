package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.PromptExperiment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PromptLabRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeExperiments(): Flow<List<PromptExperiment>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/prompts"
        emitAll(
            db.collection("users").document(uid).collection("prompts")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PromptExperiment::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveExperiment(experiment: PromptExperiment) {
        val uid = requireUserId()
        val expId = if (experiment.id.isNotBlank()) experiment.id else "exp_${System.currentTimeMillis()}"
        val docRef = db.collection("users").document(uid).collection("prompts").document(expId)
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "id" to expId,
                    "userId" to uid,
                    "title" to experiment.title,
                    "systemInstruction" to experiment.systemInstruction,
                    "promptText" to experiment.promptText,
                    "modelA" to experiment.modelA,
                    "modelB" to experiment.modelB,
                    "temperature" to experiment.temperature,
                    "topP" to experiment.topP,
                    "thinkingLevel" to experiment.thinkingLevel,
                    "version" to experiment.version,
                    "responseA" to experiment.responseA,
                    "responseB" to experiment.responseB,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "title" to experiment.title,
                    "systemInstruction" to experiment.systemInstruction,
                    "promptText" to experiment.promptText,
                    "modelA" to experiment.modelA,
                    "modelB" to experiment.modelB,
                    "temperature" to experiment.temperature,
                    "topP" to experiment.topP,
                    "thinkingLevel" to experiment.thinkingLevel,
                    "version" to (experiment.version + 1), // Iteration version increment
                    "responseA" to experiment.responseA,
                    "responseB" to experiment.responseB,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun rollbackToVersion(experiment: PromptExperiment, previousPromptText: String, previousSystemInstruction: String) {
        saveExperiment(
            experiment.copy(
                promptText = previousPromptText,
                systemInstruction = previousSystemInstruction
            )
        )
    }
}
