package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.data.repository.DevicePlatform
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory

/**
 * Unified System Integration Hub Banner connecting all screens to:
 * - StudioConfigRepository (Root Admin wblanc1021@gmail.com, Staged Subscriptions, API Keys, Connection Mesh, System Bus)
 * - WorkspaceHandoffRepository & ProjectSyncRepository (Room SQLite last_modified + device_id tracking & Handoff Beams)
 * - Active RecastProject state across Studio, Veo & Nano, Prompt Lab, Voice, Live Cam, Result, and Vault
 */
@Composable
fun SystemIntegrationHubBanner(
    moduleName: String,
    project: RecastProject,
    configRepo: StudioConfigRepository,
    workspaceRepo: WorkspaceHandoffRepository,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    primaryBridgeLabel: String? = null,
    onPrimaryBridgeClick: (() -> Unit)? = null,
    secondaryBridgeLabel: String? = null,
    onSecondaryBridgeClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val adminSession by configRepo.adminSession.collectAsState()
    val subscriptionState by configRepo.subscriptionState.collectAsState()
    val apiConfig by configRepo.apiConfig.collectAsState()
    val connectionConfig by configRepo.connectionConfig.collectAsState()
    val systemBus by configRepo.systemBus.collectAsState()
    val roomSyncStates by workspaceRepo.roomSyncStates.collectAsState()

    val currentRoomEntry = roomSyncStates.find { it.projectId == project.id } ?: roomSyncStates.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF121826), CharcoalCard)
                )
            )
            .border(1.dp, GeminiCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("system_integration_hub_${moduleName.lowercase().replace(" ", "_")}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Module Identity + Root Admin / Staged Subscription / AI Model Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(GeminiGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYSTEM BUS • ${moduleName.uppercase()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = GeminiCyan
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Two-Way Conversion Direction Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalElevated)
                        .border(1.dp, NeonOrange.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.activeConversionMode().badge,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange
                    )
                }

                // Owner-Only Admin Badge (strictly visible ONLY to wblanc1021@gmail.com)
                if (adminSession.isAdminAuthenticated) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiCyan.copy(alpha = 0.18f))
                            .border(1.dp, GeminiCyan, RoundedCornerShape(6.dp))
                            .clickable { onOpenConfigCenter?.invoke(ControlCenterTab.ADMIN) }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ROOT ADMIN",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmIvory
                        )
                    }
                }

                // Staged / Active Plan Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalElevated)
                        .border(
                            1.dp,
                            if (subscriptionState.isBillingImplemented) GeminiGreen else GeminiAmber,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onOpenConfigCenter?.invoke(ControlCenterTab.SUBSCRIPTION) }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (subscriptionState.isBillingImplemented) {
                            "LIVE: ${subscriptionState.selectedTier.resolutionCap}"
                        } else {
                            "STAGED: ${subscriptionState.stagedTier.resolutionCap}"
                        },
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (subscriptionState.isBillingImplemented) GeminiGreen else GeminiAmber
                    )
                }
            }
        }

        // Row 2: Live Linked Project + Fidelity + Room SQLite Sync (last_modified + device_id)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianBlack.copy(alpha = 0.65f))
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${project.title} • ${project.resolution} @ ${project.frameRate}fps • Comp ${project.compressionLevel}% (${project.estimatedBitrateMbps()} Mbps)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${project.activeConversionMode().shortTitle} • Room ID: ${currentRoomEntry?.deviceId ?: workspaceRepo.projectSyncRepo.currentDeviceId} • Mod: ${currentRoomEntry?.lastModifiedFormatted ?: "Just now"}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MutedIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${connectionConfig.transportProtocol.substringBefore(" ")} ${connectionConfig.lastPingMs}ms",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GeminiGreen
                )
                Text(
                    text = if (adminSession.isAdminAuthenticated && adminSession.unlimitedCreditsOverride) {
                        "∞ CREDITS"
                    } else {
                        "${systemBus.creditsRemaining} CR"
                    },
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = GeminiViolet
                )
            }
        }

        // Row 3: 1-Tap Cross-System Integration Bridges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Sync Project to Room SQLite + Shared Workspace
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CharcoalElevated)
                    .border(1.dp, GeminiGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        workspaceRepo.updateSharedProject(project)
                        configRepo.recordSystemEvent(
                            moduleName,
                            "Synced '${project.title}' to Room DB (${workspaceRepo.projectSyncRepo.currentDeviceId})"
                        )
                        Toast.makeText(
                            context,
                            "Synced '${project.title}' to Room DB & Multi-Device Mesh",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("hub_sync_room_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = GeminiGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sync Room",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory,
                    maxLines = 1
                )
            }

            // 2. Beam Active State to Workstation / Web
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CharcoalElevated)
                    .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        workspaceRepo.sendHandoffBeam(
                            project = project,
                            targetPlatform = DevicePlatform.COMPUTER
                        ) {
                            configRepo.recordSystemEvent(
                                moduleName,
                                "Beamed '${project.title}' from $moduleName to Workstation"
                            )
                            Toast.makeText(
                                context,
                                "Beamed '${project.title}' to Pro Workstation!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("hub_beam_workstation_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = GeminiCyan,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Beam to PC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory,
                    maxLines = 1
                )
            }

            // 3. Optional Module-Specific Primary Bridge Action
            if (primaryBridgeLabel != null && onPrimaryBridgeClick != null) {
                Row(
                    modifier = Modifier
                        .weight(1.25f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GeminiBlue, GeminiPurple))
                        )
                        .border(1.dp, GeminiCyan, RoundedCornerShape(8.dp))
                        .clickable { onPrimaryBridgeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("hub_primary_bridge_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WarmIvory,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = primaryBridgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = WarmIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 4. Optional Secondary Bridge Action
            if (secondaryBridgeLabel != null && onSecondaryBridgeClick != null) {
                Row(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalElevated)
                        .border(1.dp, GeminiAmber.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onSecondaryBridgeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("hub_secondary_bridge_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = GeminiAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = secondaryBridgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
