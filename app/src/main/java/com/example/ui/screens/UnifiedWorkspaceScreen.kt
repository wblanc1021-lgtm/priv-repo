package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterCastVault
import com.example.data.model.DeviceNode
import com.example.data.model.RecastProject
import com.example.data.model.SyncState
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import kotlin.math.cos
import kotlin.math.sin

enum class WorkspaceViewMode(val label: String) {
    UNIFIED("All-in-One"),
    PHONE("Mobile OS"),
    COMPUTER("Desktop Pro"),
    WEB("Web Cloud")
}

@Composable
fun UnifiedWorkspaceScreen(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    workspaceRepo: WorkspaceHandoffRepository,
    onOpenPortal: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToLiveCam: () -> Unit,
    onNavigateToResult: () -> Unit,
    configRepo: StudioConfigRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null
) {
    val context = LocalContext.current
    val localConfigRepo = remember(configRepo) { configRepo ?: StudioConfigRepository(context) }

    var viewMode by remember { mutableStateOf(WorkspaceViewMode.UNIFIED) }
    val devices by workspaceRepo.devices.collectAsState()
    val activeHandoff by workspaceRepo.activeHandoff.collectAsState()
    val syncState by workspaceRepo.syncState.collectAsState()
    val projectSyncStates by workspaceRepo.projectSyncStates.collectAsState()
    val latestRoomState = projectSyncStates.firstOrNull()

    val adminSession by localConfigRepo.adminSession.collectAsState()
    val subscriptionState by localConfigRepo.subscriptionState.collectAsState()
    val apiConfig by localConfigRepo.apiConfig.collectAsState()
    val connectionConfig by localConfigRepo.connectionConfig.collectAsState()

    var isTimelinePlaying by remember { mutableStateOf(true) }
    var timelineProgress by remember { mutableFloatStateOf(0.42f) }
    var virtualCamActive by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "handoffPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Next-Gen Spatial Studio Glass HUD Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF151929),
                            Color(0xFF1A142B),
                            Color(0xFF111522)
                        )
                    )
                )
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        GeminiSparkle(size = 22.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SPATIAL WORKSPACE 4.0",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AiStudioBadge(
                                    text = if (adminSession.isAdminAuthenticated) "ROOT ADMIN" else "TRI-MESH"
                                )
                            }
                            Text(
                                text = if (latestRoomState != null) {
                                    "Room DB • ${latestRoomState.deviceName} (${latestRoomState.lastModifiedFormatted.ifBlank { "Just now" }})"
                                } else {
                                    "Mobile • Desktop • Web Unified Handoff Mesh"
                                },
                                fontSize = 10.sp,
                                color = MutedIvory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Admin Console Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (adminSession.isAdminAuthenticated) GeminiCyan.copy(alpha = 0.18f)
                                    else CharcoalElevated
                                )
                                .border(
                                    1.dp,
                                    if (adminSession.isAdminAuthenticated) GeminiCyan else BorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onOpenConfigCenter?.invoke(ControlCenterTab.ADMIN) ?: onOpenPortal()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("workspace_admin_pill"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Login",
                                tint = GeminiCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (adminSession.isAdminAuthenticated) "Admin" else "Login",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = WarmIvory
                            )
                        }

                        // Sync Portal Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalElevated)
                                .border(
                                    1.dp,
                                    if (syncState == SyncState.SYNCED) GeminiGreen.copy(alpha = 0.6f) else GeminiAmber,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onOpenPortal() }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("workspace_sync_portal_pill"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState == SyncState.SYNCED) GeminiGreen else GeminiAmber)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (syncState == SyncState.SYNCED) "Mesh" else "Sync",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmIvory
                            )
                        }
                    }
                }

                // Quick Status Strip (Staged Subscription + API Model + Ping)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianBlack.copy(alpha = 0.65f))
                        .clickable {
                            onOpenConfigCenter?.invoke(ControlCenterTab.SUBSCRIPTION) ?: onOpenPortal()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GeminiAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${subscriptionState.stagedTier.title}: ${if (subscriptionState.isBillingImplemented) "ACTIVE" else "STAGED (Unbilled)"}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subscriptionState.isBillingImplemented) GeminiGreen else GeminiAmber
                        )
                    }

                    Text(
                        text = "${apiConfig.selectedGeminiModel} • ${connectionConfig.lastPingMs}ms QUIC",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GeminiCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Modern Segmented Glass Mode Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WorkspaceViewMode.values().forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(GeminiBlue, GeminiPurple))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .clickable { viewMode = mode }
                        .padding(vertical = 9.dp)
                        .testTag("workspace_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (mode) {
                                WorkspaceViewMode.UNIFIED -> Icons.Default.Dashboard
                                WorkspaceViewMode.PHONE -> Icons.Default.PhoneAndroid
                                WorkspaceViewMode.COMPUTER -> Icons.Default.Computer
                                WorkspaceViewMode.WEB -> Icons.Default.Language
                            },
                            contentDescription = null,
                            tint = if (isSelected) WarmIvory else MutedIvory,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) WarmIvory else MutedIvory
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Cross-Device Handoff Banner (if session beamed)
        AnimatedVisibility(visible = activeHandoff != null) {
            activeHandoff?.let { session ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .animatedRainbowBorder(strokeWidth = 1.5.dp, cornerRadius = 14.dp, glow = true)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(GeminiCyan.copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE HANDOFF BEAM ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = GeminiCyan
                                )
                            }
                            IconButton(
                                onClick = { workspaceRepo.dismissHandoff() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Dismiss", tint = MutedIvory, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${session.sourceDeviceName}  ➔  ${session.targetDeviceName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )

                        Text(
                            text = "Project: \"${session.projectTitle}\" • ${session.payloadPreview}",
                            fontSize = 10.sp,
                            color = MutedIvory
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    workspaceRepo.acceptHandoff { accepted ->
                                        onProjectChange(
                                            project.copy(
                                                title = accepted.projectTitle,
                                                characterStrength = accepted.characterStrength,
                                                qualityPreset = accepted.qualityPreset,
                                                voiceName = accepted.voiceName
                                            )
                                        )
                                        Toast.makeText(context, "Handoff Accepted! Project state synchronized.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept & Mirror State", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { workspaceRepo.dismissHandoff() },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalElevated, contentColor = MutedIvory),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Dismiss", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        val handleProjectChange: (RecastProject) -> Unit = { updated ->
            onProjectChange(updated)
            workspaceRepo.recordProjectEdit(updated)
        }

        // Main Viewport Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (viewMode) {
                WorkspaceViewMode.UNIFIED -> {
                    UnifiedAllInOneView(
                        project = project,
                        onProjectChange = handleProjectChange,
                        devices = devices,
                        workspaceRepo = workspaceRepo,
                        configRepo = localConfigRepo,
                        onOpenPortal = onOpenPortal,
                        onOpenConfigCenter = onOpenConfigCenter,
                        onNavigateToStudio = onNavigateToStudio,
                        onNavigateToLiveCam = onNavigateToLiveCam,
                        onNavigateToResult = onNavigateToResult,
                        virtualCamActive = virtualCamActive,
                        onVirtualCamToggle = { virtualCamActive = it }
                    )
                }
                WorkspaceViewMode.PHONE -> {
                    PhoneAppViewport(
                        project = project,
                        onProjectChange = handleProjectChange,
                        onNavigateToLiveCam = onNavigateToLiveCam,
                        onNavigateToStudio = onNavigateToStudio,
                        onBeamToComputer = {
                            val comp = devices.find { it.type == "computer" } ?: devices.first()
                            val phone = devices.find { it.isCurrentDevice } ?: devices.first()
                            workspaceRepo.sendHandoffBeam(phone, comp, project) {
                                Toast.makeText(context, "Beamed to ${comp.name}!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                WorkspaceViewMode.COMPUTER -> {
                    ComputerProWorkstationViewport(
                        project = project,
                        onProjectChange = handleProjectChange,
                        isPlaying = isTimelinePlaying,
                        onTogglePlay = { isTimelinePlaying = !isTimelinePlaying },
                        progress = timelineProgress,
                        onProgressChange = { timelineProgress = it },
                        virtualCamActive = virtualCamActive,
                        onVirtualCamToggle = { virtualCamActive = it },
                        onBeamToWeb = {
                            val web = devices.find { it.type == "web" } ?: devices.first()
                            val comp = devices.find { it.type == "computer" } ?: devices.first()
                            workspaceRepo.sendHandoffBeam(comp, web, project) {
                                Toast.makeText(context, "Beamed to ${web.name}!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                WorkspaceViewMode.WEB -> {
                    WebCloudPortalViewport(
                        project = project,
                        onOpenPortal = onOpenPortal
                    )
                }
            }
        }
    }
}

/**
 * High-framerate interactive 2026 Neural Holographic Viewfinder Canvas.
 * Replaces static placeholder icons with a live split-screen neural mesh, volumetric lighting,
 * facial landmark vectors, and audio spectrum telemetry.
 */
@Composable
fun NeuralHologramViewfinder(
    characterName: String,
    characterStrength: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var splitFraction by remember { mutableFloatStateOf(0.54f) }
    val infiniteTransition = rememberInfiniteTransition(label = "holoViewfinder")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holoPhase"
    )
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineY"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 12.dp else 18.dp))
            .background(ObsidianBlack)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(GeminiCyan, GeminiPurple, NeonOrange)),
                shape = RoundedCornerShape(if (compact) 12.dp else 18.dp)
            )
            .pointerInput(compact) {
                if (!compact) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        splitFraction = (change.position.x / size.width).coerceIn(0.1f, 0.9f)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val splitX = if (compact) w * 0.5f else w * splitFraction

            // Deep studio backdrop gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0B1021), Color(0xFF150E26), Color(0xFF070912))
                )
            )

            // Subtle perspective cyber grid
            val gridCols = 8
            for (i in 1 until gridCols) {
                val gx = (w / gridCols) * i
                drawLine(
                    color = GeminiBlue.copy(alpha = 0.08f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
            }
            val gridRows = 5
            for (j in 1 until gridRows) {
                val gy = (h / gridRows) * j
                drawLine(
                    color = GeminiPurple.copy(alpha = 0.08f),
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
            }

            // Volumetric Rim Glow behind silhouette
            val cx = w * 0.5f
            val cy = h * 0.45f
            val headRx = (if (compact) h * 0.24f else h * 0.22f)
            val headRy = headRx * 1.28f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GeminiCyan.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(cx - headRx * 0.4f, cy),
                    radius = headRx * 2.4f
                ),
                radius = headRx * 2.4f,
                center = Offset(cx - headRx * 0.4f, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GeminiPink.copy(alpha = 0.24f), Color.Transparent),
                    center = Offset(cx + headRx * 0.5f, cy),
                    radius = headRx * 2.4f
                ),
                radius = headRx * 2.4f,
                center = Offset(cx + headRx * 0.5f, cy)
            )

            // Left Half: Original Source Actor Silhouette
            clipRect(left = 0f, top = 0f, right = splitX, bottom = h) {
                // Shoulders
                drawRoundRect(
                    color = Color(0xFF1F293D),
                    topLeft = Offset(cx - headRx * 1.9f, cy + headRy * 0.78f),
                    size = Size(headRx * 3.8f, h * 0.5f),
                    cornerRadius = CornerRadius(headRx, headRx)
                )
                // Head
                drawOval(
                    color = Color(0xFF2B364F),
                    topLeft = Offset(cx - headRx, cy - headRy),
                    size = Size(headRx * 2f, headRy * 2f)
                )
            }

            // Right Half: Transformed Neural Recast Character with 468-Point Mesh
            clipRect(left = splitX, top = 0f, right = w, bottom = h) {
                // Recast Shoulders with Neon Rim
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF1E1B3A), Color(0xFF2D1948))
                    ),
                    topLeft = Offset(cx - headRx * 1.9f, cy + headRy * 0.78f),
                    size = Size(headRx * 3.8f, h * 0.5f),
                    cornerRadius = CornerRadius(headRx, headRx)
                )
                // Recast Head
                drawOval(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF283558), Color(0xFF3B1E54))
                    ),
                    topLeft = Offset(cx - headRx, cy - headRy),
                    size = Size(headRx * 2f, headRy * 2f)
                )
                // Glowing Recast Rim Contour
                drawOval(
                    color = GeminiCyan.copy(alpha = 0.85f),
                    topLeft = Offset(cx - headRx, cy - headRy),
                    size = Size(headRx * 2f, headRy * 2f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Neural Facial Landmark Triangulation Vectors
                val eyeY = cy - headRy * 0.15f
                val eyeOffsetX = headRx * 0.42f
                val noseY = cy + headRy * 0.18f
                val mouthY = cy + headRy * 0.48f

                val leftEye = Offset(cx - eyeOffsetX, eyeY)
                val rightEye = Offset(cx + eyeOffsetX, eyeY)
                val noseTip = Offset(cx, noseY + sin(phase) * 2f)
                val mouthLeft = Offset(cx - headRx * 0.36f, mouthY)
                val mouthRight = Offset(cx + headRx * 0.36f, mouthY)
                val chin = Offset(cx, cy + headRy * 0.88f)
                val browCenter = Offset(cx, cy - headRy * 0.42f)

                val meshPath = Path().apply {
                    moveTo(browCenter.x, browCenter.y)
                    lineTo(leftEye.x, leftEye.y)
                    lineTo(noseTip.x, noseTip.y)
                    lineTo(rightEye.x, rightEye.y)
                    close()
                    moveTo(leftEye.x, leftEye.y)
                    lineTo(mouthLeft.x, mouthLeft.y)
                    lineTo(noseTip.x, noseTip.y)
                    moveTo(rightEye.x, rightEye.y)
                    lineTo(mouthRight.x, mouthRight.y)
                    lineTo(noseTip.x, noseTip.y)
                    moveTo(mouthLeft.x, mouthLeft.y)
                    lineTo(chin.x, chin.y)
                    lineTo(mouthRight.x, mouthRight.y)
                    close()
                }
                drawPath(
                    path = meshPath,
                    color = GeminiCyan.copy(alpha = 0.55f),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                listOf(browCenter, leftEye, rightEye, noseTip, mouthLeft, mouthRight, chin).forEach { pt ->
                    drawCircle(color = GeminiCyan, radius = 2.6.dp.toPx(), center = pt)
                }
            }

            // Horizontal Laser Scanline
            val currentScanY = h * scanY
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, GeminiCyan.copy(alpha = 0.65f), GeminiPurple.copy(alpha = 0.65f), Color.Transparent)
                ),
                start = Offset(0f, currentScanY),
                end = Offset(w, currentScanY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Vertical Split Wipe Line
            drawLine(
                color = GeminiCyan,
                start = Offset(splitX, 0f),
                end = Offset(splitX, h),
                strokeWidth = if (compact) 1.5.dp.toPx() else 2.2.dp.toPx()
            )
            if (!compact) {
                drawCircle(
                    color = GeminiCyan,
                    radius = 11.dp.toPx(),
                    center = Offset(splitX, h * 0.5f)
                )
                drawCircle(
                    color = ObsidianBlack,
                    radius = 4.dp.toPx(),
                    center = Offset(splitX, h * 0.5f)
                )
            }
        }

        // Top HUD Overlays inside Viewfinder
        if (!compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack.copy(alpha = 0.78f))
                        .border(0.8.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RECAST: ${characterName.uppercase()} • ${characterStrength}%",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = GeminiCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack.copy(alpha = 0.78f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "60 FPS • 468 MESH PTS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GeminiGreen
                    )
                }
            }

            // Bottom drag hint
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ObsidianBlack.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "◄ DRAG LASER TO COMPARE SOURCE VS NEURAL RECAST ►",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory
                )
            }
        }
    }
}

// ---------------------------------------------------------
// PANE 1: ALL-IN-ONE MULTI-PANE STUDIO
// ---------------------------------------------------------
@Composable
fun UnifiedAllInOneView(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    devices: List<DeviceNode>,
    workspaceRepo: WorkspaceHandoffRepository,
    configRepo: StudioConfigRepository,
    onOpenPortal: () -> Unit,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)?,
    onNavigateToStudio: () -> Unit,
    onNavigateToLiveCam: () -> Unit,
    onNavigateToResult: () -> Unit,
    virtualCamActive: Boolean,
    onVirtualCamToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val adminSession by configRepo.adminSession.collectAsState()
    val subscriptionState by configRepo.subscriptionState.collectAsState()
    val apiConfig by configRepo.apiConfig.collectAsState()
    val connectionConfig by configRepo.connectionConfig.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento Card 0: Admin, Staged Subscription, API & Mesh Command Strip
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF141B2D), Color(0xFF1D1532))
                        )
                    )
                    .border(1.dp, GeminiCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = GeminiCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ROOT ADMIN & SYSTEM CONTROL DECK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = WarmIvory
                                )
                                Text(
                                    text = "${StudioConfigRepository.ROOT_ADMIN_EMAIL} • ${if (subscriptionState.isBillingImplemented) "Plan Verified" else "Staged Sandbox (Unbilled)"}",
                                    fontSize = 10.sp,
                                    color = GeminiCyan
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onOpenConfigCenter?.invoke(ControlCenterTab.ADMIN) ?: onOpenPortal()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiCyan,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("open_admin_deck_button")
                        ) {
                            Text("Admin & Config", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // 3 Quick Bento Pills: Subscription | API Keys | Connection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalElevated)
                                .clickable {
                                    onOpenConfigCenter?.invoke(ControlCenterTab.SUBSCRIPTION)
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("SUBSCRIPTION", fontSize = 8.sp, fontWeight = FontWeight.Black, color = GeminiAmber)
                                Text(
                                    text = subscriptionState.stagedTier.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (subscriptionState.isBillingImplemented) "Verified Live" else "Staged Test",
                                    fontSize = 9.sp,
                                    color = GeminiGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalElevated)
                                .clickable {
                                    onOpenConfigCenter?.invoke(ControlCenterTab.API_KEYS)
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("API & ENGINE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = GeminiPurple)
                                Text(
                                    text = apiConfig.selectedGeminiModel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = apiConfig.selectedVideoEngine,
                                    fontSize = 9.sp,
                                    color = GeminiCyan,
                                    maxLines = 1
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalElevated)
                                .clickable {
                                    onOpenConfigCenter?.invoke(ControlCenterTab.CONNECTION)
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("CONNECTION", fontSize = 8.sp, fontWeight = FontWeight.Black, color = GeminiCyan)
                                Text(
                                    text = "${connectionConfig.lastPingMs}ms QUIC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory
                                )
                                Text(
                                    text = "AES-256 Mesh",
                                    fontSize = 9.sp,
                                    color = GeminiGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Handoff Beam Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Cast, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ONE-TAP CROSS-DEVICE BEAM", fontSize = 11.sp, fontWeight = FontWeight.Black, color = WarmIvory)
                            Text("Instant state handoff across Mobile, PC & Web", fontSize = 10.sp, color = MutedIvory)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val phone = devices.find { it.isCurrentDevice } ?: devices.first()
                                val comp = devices.find { it.type == "computer" } ?: devices.last()
                                workspaceRepo.sendHandoffBeam(phone, comp, project) {
                                    Toast.makeText(context, "Beamed to ${comp.name}!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalElevated, contentColor = GeminiCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("To PC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val phone = devices.find { it.isCurrentDevice } ?: devices.first()
                                val web = devices.find { it.type == "web" } ?: devices.last()
                                workspaceRepo.sendHandoffBeam(phone, web, project) {
                                    Toast.makeText(context, "Beamed to ${web.name}!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalElevated, contentColor = GeminiAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("To Web", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Pane 1: Next-Gen Mobile Studio Dock with Live Neural Hologram Viewfinder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MOBILE OS 4.0 STUDIO DOCK (ACTIVE PHONE)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = WarmIvory)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonOrange.copy(alpha = 0.2f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text("60FPS HOLOGRAM", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GeminiCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live Animated Holographic Mini-Viewfinder
                        NeuralHologramViewfinder(
                            characterName = project.voiceName,
                            characterStrength = project.characterStrength,
                            compact = true,
                            modifier = Modifier.size(width = 124.dp, height = 84.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Cast: ${project.voiceName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = WarmIvory
                            )
                            Text(
                                text = "Neural Swap Strength: ${project.characterStrength}%",
                                fontSize = 10.sp,
                                color = GeminiCyan
                            )
                            Slider(
                                value = project.characterStrength.toFloat(),
                                onValueChange = { onProjectChange(project.copy(characterStrength = it.toLong())) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = GeminiCyan, activeTrackColor = GeminiCyan),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onNavigateToLiveCam,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Live Cam Recast", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalElevated, contentColor = WarmIvory),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Photo & Studio Lab", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Pane 2: Computer Pro Workstation Dock
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Computer, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMPUTER PRO WORKSTATION (DESKTOP NODE)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = WarmIvory)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GeminiCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("GPU ACCELERATED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GeminiCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hardware Node: Mac Studio M3 Pro • NVIDIA Cloud Tensor Bridge • 0.04s Keyframing",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Timeline: 00:05.12 / 00:15.00 • 3 Audio Tracks", fontSize = 10.sp, color = WarmIvory)
                                }
                                Text("Virtual Cam: Zoom/Meet", fontSize = 10.sp, color = if (virtualCamActive) GeminiGreen else Color.Gray)
                            }
                            LinearProgressIndicator(
                                progress = { 0.42f },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = GeminiCyan,
                                trackColor = CharcoalElevated
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = virtualCamActive,
                                onCheckedChange = onVirtualCamToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = GeminiCyan, checkedTrackColor = CharcoalElevated),
                                modifier = Modifier.size(width = 38.dp, height = 24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Broadcast Virtual Cam Driver", fontSize = 11.sp, color = WarmIvory)
                        }

                        Button(
                            onClick = onNavigateToResult,
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalElevated, contentColor = GeminiCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Timeline Compare", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Pane 3: Web Cloud Collaboration Dock
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, GeminiAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WEB CLOUD STUDIO (BROWSER PORTAL)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = WarmIvory)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GeminiAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("3 USERS LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GeminiAmber)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "URL: https://frameshift.cloud/studio/share/${project.id.ifBlank { "proj_99" }}",
                        fontSize = 10.sp,
                        color = GeminiAmber,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cloud Render Queue: 2 Variants Generated", fontSize = 10.sp, color = WarmIvory, fontWeight = FontWeight.Bold)
                            Text("Live Spectator Mode: WebRTC 1080p Bridge Active", fontSize = 9.sp, color = MutedIvory)
                        }
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Public review link copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = GeminiAmber, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PANE 2: NEXT-GEN MOBILE OS 4.0 VIEWPORT
// ---------------------------------------------------------
@Composable
fun PhoneAppViewport(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    onNavigateToLiveCam: () -> Unit,
    onNavigateToStudio: () -> Unit = {},
    onBeamToComputer: () -> Unit
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Uploaded_Portrait.jpg"
            onProjectChange(project.copy(primaryReferenceName = name, voiceName = name))
            Toast.makeText(context, "Loaded photo into Mobile Studio: $name", Toast.LENGTH_SHORT).show()
        }
    }

    val selfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onProjectChange(project.copy(primaryReferenceName = "Live Selfie", voiceName = "Live Selfie"))
            Toast.makeText(context, "Captured Live Selfie for instant recast!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF141826), Color(0xFF0E101A))
                        )
                    )
                    .animatedRainbowBorder(strokeWidth = 1.8.dp, cornerRadius = 24.dp, glow = true)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 2026 Titanium Dynamic Island Status Bezel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "9:41 • OS 4.0",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = WarmIvory
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ObsidianBlack)
                                .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GeminiGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NPU TENSOR LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = GeminiCyan
                            )
                        }
                        Text(
                            text = "5G UW • 99%",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GeminiGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive 60fps Holographic Split-Wipe Viewfinder
                    NeuralHologramViewfinder(
                        characterName = project.voiceName,
                        characterStrength = project.characterStrength,
                        compact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Instant Mobile Photo Upload & Camera Selfie Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CharcoalElevated,
                                contentColor = GeminiCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .testTag("mobile_upload_photo_button")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = { selfieLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CharcoalElevated,
                                contentColor = GeminiPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(1.dp, GeminiPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .testTag("mobile_selfie_button")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Snap Selfie", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Neural Strength & Lip-Sync Bento Control Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianBlack.copy(alpha = 0.7f))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "NEURAL IDENTITY BLEND STRENGTH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MutedIvory
                                )
                                Text(
                                    text = "${project.characterStrength}%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = GeminiCyan
                                )
                            }
                            Slider(
                                value = project.characterStrength.toFloat(),
                                onValueChange = { onProjectChange(project.copy(characterStrength = it.toLong())) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GeminiCyan,
                                    activeTrackColor = GeminiCyan
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Character Selector Pills
                    Text(
                        text = "SELECT 11LABS-TIER NEURAL CAST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CharacterCastVault.stockCharacters) { char ->
                            val isSelected = project.voiceName == char.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(listOf(GeminiBlue, GeminiPurple))
                                        } else {
                                            Brush.horizontalGradient(listOf(CharcoalElevated, CharcoalElevated))
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) GeminiCyan else BorderSubtle,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onProjectChange(
                                            project.copy(
                                                voiceName = char.name,
                                                characterStrength = char.recommendedStrength.toLong()
                                            )
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(char.avatarColorA)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = char.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = WarmIvory
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToLiveCam,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Full Live Cam", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = onBeamToComputer,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beam to Desktop", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// PANE 3: COMPUTER PRO WORKSTATION VIEWPORT
// ---------------------------------------------------------
@Composable
fun ComputerProWorkstationViewport(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    virtualCamActive: Boolean,
    onVirtualCamToggle: (Boolean) -> Unit,
    onBeamToWeb: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CharcoalCard)
                    .border(1.5.dp, GeminiCyan, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Computer Window Titlebar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                        }
                        Text(
                            text = "FrameShift Pro Workstation — \"${project.title.ifBlank { "Noir Alley Swap" }}\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                        Text("4K DCI • 60fps", fontSize = 10.sp, color = GeminiCyan, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Neural Hologram Monitor
                    NeuralHologramViewfinder(
                        characterName = project.voiceName,
                        characterStrength = project.characterStrength,
                        compact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timeline Scrubber & Transport
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiCyan)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = ObsidianBlack
                            )
                        }

                        Slider(
                            value = progress,
                            onValueChange = onProgressChange,
                            colors = SliderDefaults.colors(thumbColor = GeminiCyan, activeTrackColor = GeminiCyan),
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "00:06.18",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multi-Track Lanes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrackLaneItem("TRACK 1: 4K Neural Face & Body Mesh (Keyframed)", GeminiCyan, 0.78f)
                        TrackLaneItem("TRACK 2: 11Labs Voice Synthesis (${project.voiceName})", GeminiAmber, 0.85f)
                        TrackLaneItem("TRACK 3: Spatial Audio & Ambience Preserve", GeminiGreen, 0.62f)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBeamToWeb,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiAmber, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beam Master Timeline to Web Client", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackLaneItem(title: String, color: Color, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 9.sp, color = WarmIvory, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(90.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CharcoalElevated
        )
    }
}

// ---------------------------------------------------------
// PANE 4: WEB CLOUD PORTAL VIEWPORT
// ---------------------------------------------------------
@Composable
fun WebCloudPortalViewport(
    project: RecastProject,
    onOpenPortal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CharcoalCard)
                    .border(1.5.dp, GeminiAmber, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Browser Chrome Mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBlack)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "https://frameshift.cloud/studio/${project.id.ifBlank { "proj_active" }}",
                            fontSize = 11.sp,
                            color = WarmIvory,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MutedIvory, modifier = Modifier.size(14.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // WebRTC Stream Player Box with Live Hologram
                    NeuralHologramViewfinder(
                        characterName = project.voiceName,
                        characterStrength = project.characterStrength,
                        compact = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("LIVE COLLABORATION & DIRECTOR NOTES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MutedIvory)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("• [wblanc1021 (Root Admin)]: \"Approved 4K master cut & QUIC Zero-RTT sync.\"", fontSize = 10.sp, color = GeminiCyan, fontWeight = FontWeight.Bold)
                        Text("• [Alex VFX - 2m ago]: \"Voice synthesis matches the noir alley lighting cleanly.\"", fontSize = 10.sp, color = WarmIvory)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenPortal,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiAmber, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Full Multi-Device Login & Sync Portal", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
