package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity tracking the synchronization state of projects across paired devices.
 * Contains 'last_modified' timestamp, 'device_id' tracking, sync status, video fidelity parameters,
 * and two-way AI conversion direction.
 */
@Entity(tableName = "project_sync_states")
data class ProjectSyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "project_id")
    val projectId: String,

    @ColumnInfo(name = "device_id")
    val deviceId: String,

    @ColumnInfo(name = "device_name")
    val deviceName: String = "Pixel 9 Pro Mobile Studio",

    @ColumnInfo(name = "device_type")
    val deviceType: String = "phone", // "phone", "computer", "web"

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_modified_formatted")
    val lastModifiedFormatted: String = "",

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_PUSH", "PENDING_PULL", "BEAMED", "CONFLICT"

    @ColumnInfo(name = "project_title")
    val projectTitle: String = "Untitled Recast",

    @ColumnInfo(name = "character_strength")
    val characterStrength: Long = 85L,

    @ColumnInfo(name = "quality_preset")
    val qualityPreset: String = "high",

    @ColumnInfo(name = "resolution")
    val resolution: String = "1080p",

    @ColumnInfo(name = "frame_rate")
    val frameRate: Long = 60L,

    @ColumnInfo(name = "compression_level")
    val compressionLevel: Long = 22L,

    @ColumnInfo(name = "conversion_direction")
    val conversionDirection: String = "forward_recast",

    @ColumnInfo(name = "voice_name")
    val voiceName: String = "Aura Noir",

    @ColumnInfo(name = "remote_version")
    val remoteVersion: Long = 1L,

    @ColumnInfo(name = "local_version")
    val localVersion: Long = 1L,

    @ColumnInfo(name = "is_conflict")
    val isConflict: Boolean = false,

    @ColumnInfo(name = "payload_summary")
    val payloadSummary: String = ""
)
