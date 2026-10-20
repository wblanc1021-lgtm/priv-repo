package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.ProjectSyncDao
import com.example.data.local.entity.ProjectSyncStateEntity
import com.example.data.model.RecastProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Repository abstracting Room Database access for cross-device project synchronization state.
 * Manages 'last_modified' timestamps, 'device_id' tracking, version resolution, video fidelity
 * parameters, and two-way AI conversion beams.
 */
class ProjectSyncRepository(
    private val projectSyncDao: ProjectSyncDao,
    private val context: Context
) {
    constructor(context: Context) : this(
        AppDatabase.getDatabase(context).projectSyncDao(),
        context.applicationContext
    )

    private val prefs = context.getSharedPreferences("frameshift_device_sync_prefs", Context.MODE_PRIVATE)

    val currentDeviceId: String by lazy {
        prefs.getString("device_id", null) ?: run {
            val newId = "dev_phone_${UUID.randomUUID().toString().take(6)}"
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }

    val currentDeviceName: String = "Pixel 9 Pro Mobile Studio"
    val currentDeviceType: String = "phone"

    val allSyncStates: Flow<List<ProjectSyncStateEntity>> = projectSyncDao.observeAllSyncStates()

    fun observeSyncState(projectId: String): Flow<ProjectSyncStateEntity?> =
        projectSyncDao.observeSyncState(projectId)

    fun observeSyncStatesByDevice(deviceId: String): Flow<List<ProjectSyncStateEntity>> =
        projectSyncDao.observeSyncStatesByDevice(deviceId)

    suspend fun getSyncState(projectId: String): ProjectSyncStateEntity? = withContext(Dispatchers.IO) {
        projectSyncDao.getSyncState(projectId)
    }

    suspend fun getAllSyncStates(): List<ProjectSyncStateEntity> = withContext(Dispatchers.IO) {
        projectSyncDao.getAllSyncStates()
    }

    /**
     * Records or updates the synchronization state when a project is authored or modified on a device.
     */
    suspend fun recordProjectModification(
        projectId: String,
        projectTitle: String,
        deviceId: String = currentDeviceId,
        deviceName: String = currentDeviceName,
        deviceType: String = currentDeviceType,
        characterStrength: Long = 85L,
        qualityPreset: String = "high",
        resolution: String = "1080p",
        frameRate: Long = 60L,
        compressionLevel: Long = 22L,
        conversionDirection: String = "forward_recast",
        voiceName: String = "Aura Noir",
        syncStatus: String = "SYNCED",
        payloadSummary: String = ""
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val formatted = formatTimestamp(now)
        val existing = projectSyncDao.getSyncState(projectId)
        val localVer = (existing?.localVersion ?: 0L) + 1L

        val entity = ProjectSyncStateEntity(
            projectId = projectId,
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            lastModified = now,
            lastModifiedFormatted = formatted,
            syncStatus = syncStatus,
            projectTitle = projectTitle.ifBlank { "Untitled Recast" },
            characterStrength = characterStrength,
            qualityPreset = qualityPreset,
            resolution = resolution,
            frameRate = frameRate,
            compressionLevel = compressionLevel,
            conversionDirection = conversionDirection,
            voiceName = voiceName,
            remoteVersion = existing?.remoteVersion ?: 1L,
            localVersion = localVer,
            isConflict = false,
            payloadSummary = payloadSummary.ifBlank {
                "Mod by $deviceName • $resolution ${frameRate}fps • Comp ${compressionLevel}% • Strength $characterStrength%"
            }
        )
        projectSyncDao.insertOrUpdate(entity)
    }

    /**
     * Convenience method to record state from a RecastProject.
     */
    suspend fun recordProject(
        project: RecastProject,
        deviceId: String = currentDeviceId,
        deviceName: String = currentDeviceName,
        deviceType: String = currentDeviceType,
        syncStatus: String = "SYNCED"
    ) {
        recordProjectModification(
            projectId = project.id,
            projectTitle = project.title,
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            characterStrength = project.characterStrength,
            qualityPreset = project.qualityPreset,
            resolution = project.resolution,
            frameRate = project.frameRate,
            compressionLevel = project.compressionLevel,
            conversionDirection = project.conversionDirection,
            voiceName = project.voiceName,
            syncStatus = syncStatus,
            payloadSummary = "${project.activeConversionMode().shortTitle} • ${project.resolution} @ ${project.frameRate}fps • Comp ${project.compressionLevel}% (${project.estimatedBitrateMbps()} Mbps)"
        )
    }

    /**
     * Records a handoff beam transfer event across paired devices.
     */
    suspend fun recordHandoffBeamed(
        projectId: String,
        projectTitle: String,
        sourceDeviceId: String,
        sourceDeviceName: String,
        targetDeviceId: String,
        targetDeviceName: String,
        characterStrength: Long,
        qualityPreset: String,
        voiceName: String,
        resolution: String = "1080p",
        frameRate: Long = 60L,
        compressionLevel: Long = 22L,
        conversionDirection: String = "forward_recast"
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val formatted = formatTimestamp(now)
        val existing = projectSyncDao.getSyncState(projectId)
        val localVer = (existing?.localVersion ?: 0L) + 1L

        val entity = ProjectSyncStateEntity(
            projectId = projectId,
            deviceId = targetDeviceId,
            deviceName = targetDeviceName,
            deviceType = when {
                targetDeviceId.contains("computer") -> "computer"
                targetDeviceId.contains("web") -> "web"
                else -> "phone"
            },
            lastModified = now,
            lastModifiedFormatted = formatted,
            syncStatus = "BEAMED",
            projectTitle = projectTitle,
            characterStrength = characterStrength,
            qualityPreset = qualityPreset,
            resolution = resolution,
            frameRate = frameRate,
            compressionLevel = compressionLevel,
            conversionDirection = conversionDirection,
            voiceName = voiceName,
            remoteVersion = existing?.remoteVersion ?: 1L,
            localVersion = localVer,
            isConflict = false,
            payloadSummary = "Beamed $sourceDeviceName ➔ $targetDeviceName • $resolution ${frameRate}fps (${compressionLevel}% Comp)"
        )
        projectSyncDao.insertOrUpdate(entity)
    }

    suspend fun markProjectSynced(projectId: String, deviceId: String = currentDeviceId) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        projectSyncDao.updateSyncStatus(
            projectId = projectId,
            status = "SYNCED",
            timestamp = now,
            formatted = formatTimestamp(now),
            deviceId = deviceId
        )
    }

    suspend fun deleteSyncState(projectId: String) = withContext(Dispatchers.IO) {
        projectSyncDao.deleteSyncState(projectId)
    }

    suspend fun seedInitialWorkspaceStates(projects: List<RecastProject>) = withContext(Dispatchers.IO) {
        val currentStates = projectSyncDao.getAllSyncStates()
        if (currentStates.isEmpty() && projects.isNotEmpty()) {
            val entities = projects.mapIndexed { index, p ->
                val (devId, devName, devType) = when (index % 3) {
                    0 -> Triple("dev_phone_native", "Pixel 9 Pro Mobile Studio", "phone")
                    1 -> Triple("dev_computer_pro", "Mac Studio M3 Pro Workstation", "computer")
                    else -> Triple("dev_web_chrome", "Chrome Cloud Web Client", "web")
                }
                val modTime = System.currentTimeMillis() - (index * 3600000L)
                ProjectSyncStateEntity(
                    projectId = p.id,
                    deviceId = devId,
                    deviceName = devName,
                    deviceType = devType,
                    lastModified = modTime,
                    lastModifiedFormatted = formatTimestamp(modTime),
                    syncStatus = "SYNCED",
                    projectTitle = p.title.ifBlank { "Project #${index + 1}" },
                    characterStrength = p.characterStrength,
                    qualityPreset = p.qualityPreset,
                    resolution = p.resolution,
                    frameRate = p.frameRate,
                    compressionLevel = p.compressionLevel,
                    conversionDirection = p.conversionDirection,
                    voiceName = p.voiceName,
                    remoteVersion = 1L,
                    localVersion = 1L,
                    isConflict = false,
                    payloadSummary = "${p.activeConversionMode().shortTitle} • ${p.resolution} ${p.frameRate}fps • Synced on $devName"
                )
            }
            projectSyncDao.insertAll(entities)
        }
    }

    private fun formatTimestamp(timestampMs: Long): String {
        val diff = System.currentTimeMillis() - timestampMs
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestampMs))
        }
    }
}
