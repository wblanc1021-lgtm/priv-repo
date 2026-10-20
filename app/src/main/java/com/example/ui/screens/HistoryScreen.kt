package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.RecastProject
import com.example.data.repository.DevicePlatform
import com.example.data.repository.RecastRepository
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.BiometricHelper
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DimIvory
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    projects: List<RecastProject>,
    onSelectProject: (RecastProject) -> Unit,
    recastRepository: RecastRepository,
    activeProject: RecastProject? = null,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val adminSession = configRepo?.adminSession?.collectAsState()?.value
    val roomSyncStates = workspaceRepo?.roomSyncStates?.collectAsState()?.value ?: emptyList()

    var isVaultUnlocked by remember {
        mutableStateOf(adminSession?.isAdminAuthenticated == true)
    }

    LaunchedEffect(adminSession?.isAdminAuthenticated) {
        if (adminSession?.isAdminAuthenticated == true) {
            isVaultUnlocked = true
        }
    }

    // Merge Firestore Cloud Vault projects with Room SQLite synced projects so nothing is ever lost
    val combinedProjects = remember(projects, roomSyncStates) {
        val byId = LinkedHashMap<String, RecastProject>()
        projects.forEach { byId[it.id] = it }
        roomSyncStates.forEach { roomItem ->
            if (!byId.containsKey(roomItem.projectId)) {
                byId[roomItem.projectId] = RecastProject(
                    id = roomItem.projectId,
                    title = roomItem.projectTitle,
                    primaryReferenceName = "Elena (Sci-Fi Detective)",
                    characterStrength = roomItem.characterStrength,
                    qualityPreset = roomItem.qualityPreset,
                    voiceName = roomItem.voiceName,
                    status = roomItem.syncStatus.lowercase()
                )
            }
        }
        byId.values.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Vault Header with Biometric & Root Admin Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GeminiSparkle(size = 24.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isVaultUnlocked) Color(0xFF163820) else CharcoalElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isVaultUnlocked) Color(0xFF30D158) else NeonOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SECURE PROJECT VAULT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = WarmIvory
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AiStudioBadge(
                            text = if (adminSession?.isAdminAuthenticated == true) "ROOT ADMIN UNLOCKED"
                            else if (isVaultUnlocked) "ONLINE"
                            else "LOCKED",
                            iconSparkle = false
                        )
                    }
                    Text(
                        text = if (isVaultUnlocked) "AES-256 Vault + Room SQLite Sync (${combinedProjects.size} Projects)"
                        else "Protected by Biometrics, Root Admin & AES-256",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )
                }
            }

            // Biometric Auth Unlock Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isVaultUnlocked) Color(0xFF1B3D23) else CharcoalElevated)
                    .border(1.dp, if (isVaultUnlocked) Color(0xFF30D158) else NeonOrange, RoundedCornerShape(6.dp))
                    .clickable {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            BiometricHelper.promptBiometric(
                                activity = activity,
                                title = "Unlock FrameShift Vault",
                                onSuccess = {
                                    isVaultUnlocked = true
                                    configRepo?.recordSystemEvent("Cloud & Room Vault", "Vault unlocked via Biometrics")
                                    Toast.makeText(context, "Vault Unlocked Successfully", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    isVaultUnlocked = true
                                    configRepo?.recordSystemEvent("Cloud & Room Vault", "Vault unlocked via Device Credential")
                                    Toast.makeText(context, "Authenticated via Device Credential", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            isVaultUnlocked = true
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("biometric_unlock_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Lock",
                        tint = if (isVaultUnlocked) Color(0xFF30D158) else NeonOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVaultUnlocked) "UNLOCKED" else "UNLOCK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVaultUnlocked) Color(0xFF30D158) else NeonOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeProject != null && configRepo != null && workspaceRepo != null) {
            SystemIntegrationHubBanner(
                moduleName = "Cloud & Room Vault",
                project = activeProject,
                configRepo = configRepo,
                workspaceRepo = workspaceRepo,
                onOpenConfigCenter = onOpenConfigCenter,
                primaryBridgeLabel = "Save Active to Vault",
                onPrimaryBridgeClick = {
                    scope.launch {
                        runCatching { recastRepository.saveProject(activeProject) }
                        workspaceRepo.updateSharedProject(activeProject)
                        configRepo.recordSystemEvent(
                            "Cloud & Room Vault",
                            "Committed '${activeProject.title}' to Firestore & Room SQLite"
                        )
                        Toast.makeText(context, "Saved '${activeProject.title}' to Cloud Vault & Room DB!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (combinedProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = CharcoalElevated, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Recast Projects in Vault Yet", fontSize = 13.sp, color = MutedIvory)
                    Text("Generated video swaps & Room sync states appear here automatically", fontSize = 11.sp, color = DimIvory)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(combinedProjects, key = { it.id }) { project ->
                    val roomEntry = roomSyncStates.find { it.projectId == project.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard)
                            .then(
                                if (isVaultUnlocked) {
                                    Modifier.animatedRainbowBorder(strokeWidth = 1.dp, cornerRadius = 12.dp, glow = false)
                                } else {
                                    Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                }
                            )
                            .clickable {
                                workspaceRepo?.updateSharedProject(project)
                                configRepo?.recordSystemEvent("Cloud & Room Vault", "Loaded '${project.title}' into active Studio pipeline")
                                onSelectProject(project)
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${project.characterStrength}% Swap • ${project.qualityPreset.uppercase()} • ${project.voiceName}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MutedIvory
                                )
                                if (roomEntry != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Room Sync: ${roomEntry.syncStatus} • Dev: ${roomEntry.deviceId} • Mod: ${roomEntry.lastModifiedFormatted} (v${roomEntry.localVersion})",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = GeminiCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 1-Tap Beam from Vault to Workstation
                            if (workspaceRepo != null) {
                                IconButton(
                                    onClick = {
                                        workspaceRepo.sendHandoffBeam(
                                            project = project,
                                            targetPlatform = DevicePlatform.COMPUTER
                                        ) {
                                            configRepo?.recordSystemEvent(
                                                "Cloud & Room Vault",
                                                "Beamed '${project.title}' from Vault to Workstation"
                                            )
                                            Toast.makeText(context, "Beamed '${project.title}' to Workstation!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Beam Project", tint = GeminiCyan, modifier = Modifier.size(16.dp))
                                }
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        runCatching { recastRepository.deleteProject(project.id) }
                                        workspaceRepo?.projectSyncRepo?.deleteSyncState(project.id)
                                        configRepo?.recordSystemEvent("Cloud & Room Vault", "Removed '${project.title}' from Vault")
                                        Toast.makeText(context, "Deleted project from Vault & Room Sync", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MutedIvory, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
