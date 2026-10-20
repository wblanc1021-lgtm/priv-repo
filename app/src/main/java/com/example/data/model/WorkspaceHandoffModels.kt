package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DeviceNode(
    val id: String = "",
    val name: String = "",
    val type: String = "phone", // "phone", "computer", "web"
    val platformDetail: String = "Android 16 • Mobile App",
    val isOnline: Boolean = true,
    val isCurrentDevice: Boolean = false,
    val ipAddress: String = "192.168.1.105",
    val lastActive: String = "Just now",
    val activeProjectTitle: String? = null,
    val latencyMs: Int = 18,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HandoffSession(
    val sessionId: String = "",
    val sourceDeviceId: String = "",
    val sourceDeviceName: String = "",
    val targetDeviceId: String = "",
    val targetDeviceName: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val characterStrength: Long = 85L,
    val qualityPreset: String = "high",
    val voiceName: String = "Antoni",
    val status: String = "ready", // "beaming", "ready", "synced"
    val timestamp: Long = System.currentTimeMillis(),
    val payloadPreview: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class WorkspaceViewMode(val title: String, val subtitle: String, val iconName: String) {
    UNIFIED("Unified Studio", "All-in-One Multi-Pane", "Dashboard"),
    PHONE("Phone App", "Mobile Capture & Audition", "Smartphone"),
    COMPUTER("Computer Pro", "Workstation & Timeline", "Laptop"),
    WEB("Web Cloud", "Cloud Dashboard & Stream", "Language")
}

enum class SyncState(val label: String, val description: String) {
    SYNCED("Synced to Cloud", "Real-time Firestore link active"),
    SYNCING("Syncing Changes...", "Beaming parameters across devices"),
    OFFLINE("Offline / Cache", "Changes stored locally")
}
