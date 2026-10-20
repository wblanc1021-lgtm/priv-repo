package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room SQLite entity storing comprehensive user login information, credentials metadata,
 * identity provider links (Google, Apple, X, Email), and session audit telemetry.
 */
@Serializable
@Entity(tableName = "user_login_records")
data class UserLoginRecordEntity(
    @PrimaryKey
    val recordId: String,
    val userId: String,
    val provider: String,
    val email: String,
    val displayName: String,
    val handle: String,
    val encryptedCredential: String = "",
    val maskedCredentialPreview: String = "••••••••",
    val rememberMe: Boolean = true,
    val isOwnerAdmin: Boolean = false,
    val deviceNode: String = "Android Studio Node",
    val loginCount: Int = 1,
    val firstLoginEpochMs: Long = System.currentTimeMillis(),
    val lastLoginEpochMs: Long = System.currentTimeMillis(),
    val lastLoginFormatted: String = "",
    val sessionStatus: String = "ACTIVE"
)
