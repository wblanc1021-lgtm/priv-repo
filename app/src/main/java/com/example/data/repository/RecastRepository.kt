package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.crypto.CryptoManager
import com.example.data.model.RecastProject
import com.example.data.model.UserProfile
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
import org.json.JSONArray
import org.json.JSONObject

class RecastRepository(
    private val db: FirebaseFirestore,
    private val context: Context,
    private val cryptoManager: CryptoManager = CryptoManager()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        context.applicationContext
    )

    private val auth = Firebase.auth
    private val prefs = context.getSharedPreferences("frameshift_offline_cache", Context.MODE_PRIVATE)

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfileCreated(displayName: String, email: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.ifBlank { "Director" },
                    "email" to email,
                    "credits" to 100L,
                    "tier" to "pro",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeProjects(): Flow<List<RecastProject>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects"
        emitAll(
            db.collection("users").document(uid).collection("projects")
                .snapshots()
                .map { snapshot ->
                    val projects = snapshot.toObjects(RecastProject::class.java)
                    // Update encrypted local cache for offline mode
                    cacheProjectsLocally(projects)
                    projects
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    // If network fails, emit encrypted offline cached projects
                    val cached = getCachedProjectsLocally()
                    if (cached.isNotEmpty()) {
                        emit(cached)
                    } else {
                        throw error
                    }
                }
        )
    }

    suspend fun saveProject(project: RecastProject) {
        val uid = requireUserId()
        val projId = if (project.id.isNotBlank()) project.id else "proj_${System.currentTimeMillis()}"
        val docRef = db.collection("users").document(uid).collection("projects").document(projId)
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                // CREATE
                val payload = mapOf(
                    "id" to projId,
                    "userId" to uid,
                    "title" to project.title,
                    "sourceVideoName" to project.sourceVideoName,
                    "sourceVideoDuration" to project.sourceVideoDuration,
                    "sourceVideoResolution" to project.sourceVideoResolution,
                    "primaryReferenceName" to project.primaryReferenceName,
                    "secondaryReferencesCount" to project.secondaryReferencesCount,
                    "characterStrength" to project.characterStrength,
                    "facePriority" to project.facePriority,
                    "identityConsistency" to project.identityConsistency,
                    "lightingAdaptation" to project.lightingAdaptation,
                    "motionFidelity" to project.motionFidelity,
                    "edgeRefinement" to project.edgeRefinement,
                    "temporalConsistency" to project.temporalConsistency,
                    "qualityPreset" to project.qualityPreset,
                    "resolution" to project.resolution,
                    "variantsCount" to project.variantsCount,
                    "seed" to project.seed,
                    "protectBackground" to project.protectBackground,
                    "voiceMode" to project.voiceMode,
                    "voiceName" to project.voiceName,
                    "ttsDialogue" to project.ttsDialogue,
                    "lipSyncStrength" to project.lipSyncStrength,
                    "status" to project.status,
                    "resultVideoTitle" to project.resultVideoTitle,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                // UPDATE: affectedKeys allows updating non-immutable fields
                val updates = mapOf(
                    "title" to project.title,
                    "sourceVideoName" to project.sourceVideoName,
                    "sourceVideoDuration" to project.sourceVideoDuration,
                    "sourceVideoResolution" to project.sourceVideoResolution,
                    "primaryReferenceName" to project.primaryReferenceName,
                    "secondaryReferencesCount" to project.secondaryReferencesCount,
                    "characterStrength" to project.characterStrength,
                    "facePriority" to project.facePriority,
                    "identityConsistency" to project.identityConsistency,
                    "lightingAdaptation" to project.lightingAdaptation,
                    "motionFidelity" to project.motionFidelity,
                    "edgeRefinement" to project.edgeRefinement,
                    "temporalConsistency" to project.temporalConsistency,
                    "qualityPreset" to project.qualityPreset,
                    "resolution" to project.resolution,
                    "variantsCount" to project.variantsCount,
                    "seed" to project.seed,
                    "protectBackground" to project.protectBackground,
                    "voiceMode" to project.voiceMode,
                    "voiceName" to project.voiceName,
                    "ttsDialogue" to project.ttsDialogue,
                    "lipSyncStrength" to project.lipSyncStrength,
                    "status" to project.status,
                    "resultVideoTitle" to project.resultVideoTitle,
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

    suspend fun deleteProject(projectId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("projects").document(projectId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Encrypted Offline Cache via AES-256
    private fun cacheProjectsLocally(projects: List<RecastProject>) {
        try {
            val jsonArray = JSONArray()
            projects.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("userId", p.userId)
                    put("title", p.title)
                    put("sourceVideoName", p.sourceVideoName)
                    put("characterStrength", p.characterStrength)
                    put("qualityPreset", p.qualityPreset)
                    put("status", p.status)
                    put("resultVideoTitle", p.resultVideoTitle)
                }
                jsonArray.put(obj)
            }
            val encrypted = cryptoManager.encrypt(jsonArray.toString())
            prefs.edit().putString("cached_projects", encrypted).apply()
        } catch (e: Exception) {
            // Non-fatal
        }
    }

    fun getCachedProjectsLocally(): List<RecastProject> {
        val encrypted = prefs.getString("cached_projects", null) ?: return emptyList()
        return try {
            val decrypted = cryptoManager.decrypt(encrypted)
            if (decrypted.isBlank()) return emptyList()
            val array = JSONArray(decrypted)
            val list = mutableListOf<RecastProject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecastProject(
                        id = obj.optString("id"),
                        userId = obj.optString("userId"),
                        title = obj.optString("title"),
                        sourceVideoName = obj.optString("sourceVideoName"),
                        characterStrength = obj.optLong("characterStrength", 85L),
                        qualityPreset = obj.optString("qualityPreset", "high"),
                        status = obj.optString("status", "draft"),
                        resultVideoTitle = obj.optString("resultVideoTitle")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
