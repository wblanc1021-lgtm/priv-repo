package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ProjectSyncStateEntity
import com.example.data.model.DeviceNode
import com.example.data.model.RecastProject
import com.example.data.model.SyncState
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LoginPortalModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    workspaceRepo: WorkspaceHandoffRepository,
    currentProject: RecastProject,
    onSignOut: () -> Unit,
    onOpenAdminConsole: (() -> Unit)? = null,
    onOpenSubscriptions: (() -> Unit)? = null
) {
    if (!isOpen) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser = Firebase.auth.currentUser
    val devices by workspaceRepo.devices.collectAsState()
    val syncState by workspaceRepo.syncState.collectAsState()
    val lastSyncedTime by workspaceRepo.lastSyncedTime.collectAsState()
    val cloudClipboard by workspaceRepo.cloudClipboard.collectAsState()
    val projectSyncStates by workspaceRepo.projectSyncStates.collectAsState()

    var showPairDialog by remember { mutableStateOf(false) }
    var newDeviceName by remember { mutableStateOf("") }
    var newDeviceType by remember { mutableStateOf("computer") }
    val generatedPairCode = remember { "FS-${(1000..9999).random()}" }

    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, BorderSubtle, RoundedCornerShape(20.dp)),
            color = ObsidianBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharcoalElevated)
                                .border(1.dp, NeonOrange, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LOGIN & SYNC PORTAL",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AiStudioBadge(text = "LIVE CLOUD")
                            }
                            Text(
                                text = "Cross-device session sync for Web, App & Computer",
                                fontSize = 11.sp,
                                color = MutedIvory
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MutedIvory
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile & Identity Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalCard)
                                .animatedRainbowBorder(strokeWidth = 1.dp, cornerRadius = 14.dp, glow = false)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(NeonOrange),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (currentUser?.displayName?.firstOrNull() ?: 'F').uppercase(),
                                                color = ObsidianBlack,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = currentUser?.displayName ?: "Root Admin • FrameShift Director",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = WarmIvory
                                            )
                                            Text(
                                                text = currentUser?.email ?: "wblanc1021@gmail.com",
                                                fontSize = 12.sp,
                                                color = GeminiCyan,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(GeminiGreen)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "Root Admin Verified • Google Identity",
                                                    fontSize = 10.sp,
                                                    color = GeminiGreen,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onSignOut()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CharcoalElevated,
                                            contentColor = NeonOrange
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (onOpenAdminConsole != null || onOpenSubscriptions != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (onOpenAdminConsole != null) {
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onOpenAdminConsole()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = GeminiCyan,
                                                    contentColor = ObsidianBlack
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text("Root Admin Login", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                        if (onOpenSubscriptions != null) {
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onOpenSubscriptions()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CharcoalElevated,
                                                    contentColor = GeminiAmber
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text("Staged Plans, API & Net", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Cloud Sync Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CharcoalElevated)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (syncState == SyncState.SYNCING) Icons.Default.Sync else Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = if (syncState == SyncState.SYNCING) GeminiAmber else GeminiGreen,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .then(
                                                        if (syncState == SyncState.SYNCING) Modifier.rotate(rotation) else Modifier
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = syncState.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (syncState == SyncState.SYNCING) GeminiAmber else GeminiGreen
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• Last synced: $lastSyncedTime",
                                                fontSize = 10.sp,
                                                color = MutedIvory
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.clickable {
                                                workspaceRepo.triggerSync {
                                                    Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Sync Now",
                                                tint = NeonOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Sync Now",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonOrange
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Connected Nodes Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONNECTED WORKSPACE NODES (${devices.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = WarmIvory
                            )

                            Button(
                                onClick = { showPairDialog = !showPairDialog },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalElevated,
                                    contentColor = GeminiCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLink,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showPairDialog) "Cancel Pairing" else "+ Link Device",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Pair Node expansion dialog
                    item {
                        AnimatedVisibility(visible = showPairDialog) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CharcoalElevated)
                                    .border(1.dp, GeminiCyan, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("PAIR NEW COMPUTER OR WEB WORKSPACE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WarmIvory)
                                    }

                                    Text(
                                        text = "Open frameshift.ai/workspace on your computer or browser and enter pairing PIN below:",
                                        fontSize = 11.sp,
                                        color = MutedIvory
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ObsidianBlack)
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ONE-TIME PAIRING PIN", fontSize = 9.sp, color = MutedIvory, fontWeight = FontWeight.Bold)
                                            Text(generatedPairCode, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = NeonOrange)
                                        }
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedPairCode))
                                            Toast.makeText(context, "PIN copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy PIN", tint = WarmIvory, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newDeviceName,
                                        onValueChange = { newDeviceName = it },
                                        label = { Text("Device Label (e.g. Studio Desktop RTX 4090)", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GeminiCyan,
                                            unfocusedBorderColor = BorderSubtle,
                                            focusedTextColor = WarmIvory,
                                            unfocusedTextColor = WarmIvory
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                workspaceRepo.pairNewDevice(
                                                    name = newDeviceName.ifBlank { "Desktop Workstation" },
                                                    type = "computer",
                                                    pairingCode = generatedPairCode
                                                ) {
                                                    showPairDialog = false
                                                    newDeviceName = ""
                                                    Toast.makeText(context, "Workstation paired successfully!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan, contentColor = ObsidianBlack),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Text("Pair Computer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                workspaceRepo.pairNewDevice(
                                                    name = newDeviceName.ifBlank { "Web Cloud Client" },
                                                    type = "web",
                                                    pairingCode = generatedPairCode
                                                ) {
                                                    showPairDialog = false
                                                    newDeviceName = ""
                                                    Toast.makeText(context, "Web Client paired successfully!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange, contentColor = ObsidianBlack),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Text("Pair Web Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Devices list items
                    items(devices) { dev ->
                        DeviceNodeItemCard(
                            device = dev,
                            onHandoffTo = {
                                val currentDev = devices.find { it.isCurrentDevice } ?: devices.first()
                                workspaceRepo.sendHandoffBeam(
                                    sourceDevice = currentDev,
                                    targetDevice = dev,
                                    project = currentProject
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Project beamed to ${dev.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    // Cloud Shared Clipboard
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SYNCHRONIZED CLOUD CLIPBOARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                                    }
                                    Text("Auto-Mirrored", fontSize = 9.sp, color = GeminiGreen)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cloudClipboard ?: "Empty cloud clipboard",
                                    fontSize = 12.sp,
                                    color = MutedIvory,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CharcoalElevated)
                                        .padding(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        cloudClipboard?.let {
                                            clipboardManager.setText(AnnotatedString(it))
                                            Toast.makeText(context, "Copied prompt to device clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CharcoalElevated,
                                        contentColor = WarmIvory
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Synced Prompt to Local Device", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Room Database Project Synchronization States
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = GeminiCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ROOM PERSISTENCE: PROJECT SYNC STATES (${projectSyncStates.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                            }
                            Text(
                                text = "Device ID & Timestamp Tracked",
                                fontSize = 9.sp,
                                color = GeminiGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (projectSyncStates.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalCard)
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No project sync records yet. Room database tracks every authoring and beam event.",
                                    fontSize = 11.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    } else {
                        items(projectSyncStates, key = { it.projectId }) { syncEntity ->
                            ProjectSyncStateCard(
                                entity = syncEntity,
                                onSyncNow = {
                                    workspaceRepo.triggerSync(currentProject) {
                                        Toast.makeText(context, "Synchronized ${syncEntity.projectTitle}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSyncStateCard(
    entity: ProjectSyncStateEntity,
    onSyncNow: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (entity.deviceType) {
                                "computer" -> Icons.Default.Computer
                                "web" -> Icons.Default.Language
                                else -> Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = when (entity.deviceType) {
                                "computer" -> GeminiCyan
                                "web" -> GeminiAmber
                                else -> NeonOrange
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entity.projectTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Device: ${entity.deviceName}",
                                fontSize = 10.sp,
                                color = MutedIvory
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${entity.deviceId})",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GeminiAmber
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (entity.syncStatus) {
                                "SYNCED" -> GeminiGreen.copy(alpha = 0.2f)
                                "BEAMED" -> NeonOrange.copy(alpha = 0.2f)
                                else -> GeminiAmber.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = entity.syncStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = when (entity.syncStatus) {
                            "SYNCED" -> GeminiGreen
                            "BEAMED" -> NeonOrange
                            else -> GeminiAmber
                        }
                    )
                }
            }

            // Timestamp and Version Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CharcoalElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MutedIvory,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "last_modified: ${entity.lastModifiedFormatted.ifBlank { "${entity.lastModified} ms" }}",
                        fontSize = 10.sp,
                        color = WarmIvory,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "v${entity.localVersion} / cloud-v${entity.remoteVersion}",
                    fontSize = 9.sp,
                    color = MutedIvory
                )
            }

            // Summary payload & Quick Sync action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.payloadSummary.ifBlank { "Strength: ${entity.characterStrength}% • Quality: ${entity.qualityPreset.uppercase()} • Voice: ${entity.voiceName}" },
                    fontSize = 10.sp,
                    color = MutedIvory,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onSyncNow,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = NeonOrange,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceNodeItemCard(
    device: DeviceNode,
    onHandoffTo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .border(
                width = 1.dp,
                color = if (device.isCurrentDevice) NeonOrange else BorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (device.type) {
                            "computer" -> Icons.Default.Computer
                            "web" -> Icons.Default.Language
                            else -> Icons.Default.PhoneAndroid
                        },
                        contentDescription = null,
                        tint = when (device.type) {
                            "computer" -> GeminiCyan
                            "web" -> GeminiAmber
                            else -> NeonOrange
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = WarmIvory
                        )
                        if (device.isCurrentDevice) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonOrange)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "THIS DEVICE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ObsidianBlack
                                )
                            }
                        }
                    }

                    Text(
                        text = "${device.platformDetail} • ${device.ipAddress}",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (device.isOnline) GeminiGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (device.isOnline) "Online (${device.latencyMs}ms)" else "Offline",
                            fontSize = 9.sp,
                            color = if (device.isOnline) GeminiGreen else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${device.lastActive}",
                            fontSize = 9.sp,
                            color = MutedIvory
                        )
                    }
                }
            }

            if (!device.isCurrentDevice) {
                Button(
                    onClick = onHandoffTo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalElevated,
                        contentColor = NeonOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Beam", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
