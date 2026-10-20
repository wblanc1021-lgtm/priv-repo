package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.entity.ProjectSyncStateEntity
import com.example.data.model.DeviceNode
import com.example.data.model.HandoffSession
import com.example.data.model.RecastProject
import com.example.data.model.SyncState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DevicePlatform(val typeKey: String) {
    PHONE("phone"),
    COMPUTER("computer"),
    WEB("web")
}

class WorkspaceHandoffRepository(
    private val context: Context,
    private val db: FirebaseFirestore? = runCatching {
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    }.getOrNull()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val projectSyncRepo = ProjectSyncRepository(context)

    val projectSyncStates: StateFlow<List<ProjectSyncStateEntity>> = projectSyncRepo.allSyncStates
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val roomSyncStates: StateFlow<List<ProjectSyncStateEntity>>
        get() = projectSyncStates

    private val _devices = MutableStateFlow<List<DeviceNode>>(defaultDevices())
    val devices: StateFlow<List<DeviceNode>> = _devices.asStateFlow()

    private val _activeHandoff = MutableStateFlow<HandoffSession?>(null)
    val activeHandoff: StateFlow<HandoffSession?> = _activeHandoff.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow("Just now")
    val lastSyncedTime: StateFlow<String> = _lastSyncedTime.asStateFlow()

    private val _cloudClipboard = MutableStateFlow<String?>("Cinematic cyber-noir lighting, volumetric fog, anamorphic lens flare 85mm f/1.4")
    val cloudClipboard: StateFlow<String?> = _cloudClipboard.asStateFlow()

    init {
        scope.launch {
            val sampleProjects = listOf(
                RecastProject(
                    id = "proj_cyber_noir",
                    title = "Noir Alley Swap - Master Cut",
                    characterStrength = 85L,
                    qualityPreset = "cinematic",
                    voiceName = "Aura Noir"
                ),
                RecastProject(
                    id = "proj_tokyo_neon",
                    title = "Cyberpunk Neo Shinjuku Recast",
                    characterStrength = 92L,
                    qualityPreset = "high",
                    voiceName = "Viper 8-Bit"
                ),
                RecastProject(
                    id = "proj_quantum_echo",
                    title = "Quantum Echo Holo Recast",
                    characterStrength = 78L,
                    qualityPreset = "ultra",
                    voiceName = "Echo Whisper"
                )
            )
            projectSyncRepo.seedInitialWorkspaceStates(sampleProjects)
        }
    }

    private fun defaultDevices(): List<DeviceNode> {
        return listOf(
            DeviceNode(
                id = "dev_phone_native",
                name = "Pixel 9 Pro Mobile Studio",
                type = "phone",
                platformDetail = "Android 16 • Mobile App (Active)",
                isOnline = true,
                isCurrentDevice = true,
                ipAddress = "192.168.1.102",
                lastActive = "Active Now",
                activeProjectTitle = "Live Mobile Recast",
                latencyMs = 12
            ),
            DeviceNode(
                id = "dev_computer_pro",
                name = "Mac Studio M3 Pro Workstation",
                type = "computer",
                platformDetail = "macOS Sonoma • Workstation Pro (GPU Node)",
                isOnline = true,
                isCurrentDevice = false,
                ipAddress = "192.168.1.140",
                lastActive = "Connected 2m ago",
                activeProjectTitle = "Noir Alley Swap - Timeline",
                latencyMs = 24
            ),
            DeviceNode(
                id = "dev_web_chrome",
                name = "Chrome Cloud Web Client",
                type = "web",
                platformDetail = "WebRTC Virtual Cam • Browser Bridge",
                isOnline = true,
                isCurrentDevice = false,
                ipAddress = "192.168.1.188",
                lastActive = "Connected 5m ago",
                activeProjectTitle = "Client Review Stream",
                latencyMs = 38
            )
        )
    }

    fun triggerSync(currentProject: RecastProject? = null, onComplete: (() -> Unit)? = null) {
        scope.launch {
            _syncState.value = SyncState.SYNCING
            if (currentProject != null) {
                projectSyncRepo.recordProject(currentProject, syncStatus = "SYNCED")
            }
            delay(800)
            val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
            _lastSyncedTime.value = timeFormat.format(Date())
            _syncState.value = SyncState.SYNCED
            onComplete?.invoke()
        }
    }

    fun sendHandoffBeam(
        sourceDevice: DeviceNode,
        targetDevice: DeviceNode,
        project: RecastProject,
        onBeamSent: () -> Unit
    ) {
        scope.launch {
            _syncState.value = SyncState.SYNCING
            val session = HandoffSession(
                sessionId = "beam_${UUID.randomUUID().toString().take(8)}",
                sourceDeviceId = sourceDevice.id,
                sourceDeviceName = sourceDevice.name,
                targetDeviceId = targetDevice.id,
                targetDeviceName = targetDevice.name,
                projectId = project.id,
                projectTitle = project.title.ifBlank { "Untitled Recast" },
                characterStrength = project.characterStrength,
                qualityPreset = project.qualityPreset,
                voiceName = project.voiceName,
                status = "ready",
                timestamp = System.currentTimeMillis(),
                payloadPreview = "${project.activeConversionMode().shortTitle} • ${project.resolution} @ ${project.frameRate}fps • Comp: ${project.compressionLevel}% (${project.estimatedBitrateMbps()} Mbps) • Strength: ${project.characterStrength}%"
            )
            _activeHandoff.value = session

            // Persist the beam record into Room Database with device_id tracking & timestamp
            projectSyncRepo.recordHandoffBeamed(
                projectId = project.id,
                projectTitle = project.title.ifBlank { "Untitled Recast" },
                sourceDeviceId = sourceDevice.id,
                sourceDeviceName = sourceDevice.name,
                targetDeviceId = targetDevice.id,
                targetDeviceName = targetDevice.name,
                characterStrength = project.characterStrength,
                qualityPreset = project.qualityPreset,
                voiceName = project.voiceName,
                resolution = project.resolution,
                frameRate = project.frameRate,
                compressionLevel = project.compressionLevel,
                conversionDirection = project.conversionDirection
            )

            delay(250)
            _syncState.value = SyncState.SYNCED
            onBeamSent()
        }
    }

    fun acceptHandoff(onAccepted: (HandoffSession) -> Unit) {
        val current = _activeHandoff.value ?: return
        _activeHandoff.value = current.copy(status = "synced")
        scope.launch {
            projectSyncRepo.markProjectSynced(current.projectId, current.targetDeviceId)
        }
        onAccepted(current)
    }

    fun sendHandoffBeam(
        project: RecastProject,
        targetPlatform: DevicePlatform = DevicePlatform.COMPUTER,
        onBeamSent: () -> Unit = {}
    ) {
        val currentList = _devices.value
        val source = currentList.find { it.isCurrentDevice } ?: currentList.first()
        val target = currentList.find { it.type == targetPlatform.typeKey }
            ?: currentList.getOrElse(1) { source }
        sendHandoffBeam(
            sourceDevice = source,
            targetDevice = target,
            project = project,
            onBeamSent = onBeamSent
        )
    }

    fun recordProjectEdit(project: RecastProject) {
        scope.launch {
            projectSyncRepo.recordProject(project, syncStatus = "SYNCED")
        }
    }

    fun updateSharedProject(project: RecastProject) {
        recordProjectEdit(project)
    }

    fun dismissHandoff() {
        _activeHandoff.value = null
    }

    fun copyToCloudClipboard(text: String) {
        _cloudClipboard.value = text
        triggerSync()
    }

    fun pairNewDevice(name: String, type: String, pairingCode: String, onPaired: () -> Unit) {
        scope.launch {
            _syncState.value = SyncState.SYNCING
            delay(1000)
            val newDevice = DeviceNode(
                id = "dev_${UUID.randomUUID().toString().take(6)}",
                name = name.ifBlank { "New $type Studio Node" },
                type = type,
                platformDetail = "Paired via PIN #$pairingCode",
                isOnline = true,
                isCurrentDevice = false,
                ipAddress = "192.168.1.${(150..240).random()}",
                lastActive = "Just paired",
                activeProjectTitle = "Syncing Library...",
                latencyMs = (15..45).random()
            )
            _devices.value = _devices.value + newDevice
            _syncState.value = SyncState.SYNCED
            onPaired()
        }
    }

    fun removeDevice(deviceId: String) {
        _devices.value = _devices.value.filterNot { it.id == deviceId }
    }
}
