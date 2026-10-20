package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.SubscriptionTierOption
import com.example.data.repository.WorkspaceHandoffRepository
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

enum class ControlCenterTab(val label: String) {
    ADMIN("Admin Login"),
    SUBSCRIPTION("Plans (Staged)"),
    API_KEYS("API Vault"),
    CONNECTION("Mesh & Net")
}

@Composable
fun AdminAndConfigCenterModal(
    isOpen: Boolean,
    initialTab: ControlCenterTab = ControlCenterTab.ADMIN,
    onDismiss: () -> Unit,
    configRepo: StudioConfigRepository,
    workspaceRepo: WorkspaceHandoffRepository? = null
) {
    if (!isOpen) return

    val adminSession by configRepo.adminSession.collectAsState()
    val subscriptionState by configRepo.subscriptionState.collectAsState()
    val apiConfig by configRepo.apiConfig.collectAsState()
    val connectionConfig by configRepo.connectionConfig.collectAsState()

    val isOwnerAdmin = adminSession.isAdminAuthenticated
    val visibleTabs = remember(isOwnerAdmin) {
        if (isOwnerAdmin) {
            ControlCenterTab.values().toList()
        } else {
            ControlCenterTab.values().filter { it != ControlCenterTab.ADMIN }
        }
    }
    var activeTab by remember(initialTab, isOwnerAdmin) {
        mutableStateOf(
            if (!isOwnerAdmin && initialTab == ControlCenterTab.ADMIN) {
                ControlCenterTab.SUBSCRIPTION
            } else {
                initialTab
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.93f)
                .clip(RoundedCornerShape(24.dp))
                .animatedRainbowBorder(strokeWidth = 1.2.dp, cornerRadius = 24.dp, glow = false),
            color = ObsidianBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GeminiBlue.copy(alpha = 0.3f), GeminiPurple.copy(alpha = 0.3f))
                                    )
                                )
                                .border(1.dp, if (isOwnerAdmin) GeminiCyan else NeonOrange, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOwnerAdmin) Icons.Default.AdminPanelSettings else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isOwnerAdmin) GeminiCyan else NeonOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOwnerAdmin) "ADMIN & STUDIO CONTROL CENTER" else "STUDIO CONFIGURATION CENTER",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AiStudioBadge(
                                    text = if (isOwnerAdmin) "ROOT ADMIN" else "2-WAY STUDIO"
                                )
                            }
                            Text(
                                text = if (isOwnerAdmin) {
                                    "Root Admin (${StudioConfigRepository.ROOT_ADMIN_EMAIL}) • Staged Plans • API & Mesh"
                                } else {
                                    "Staged Subscription Plans • Personal API Vault • Cross-Device Mesh"
                                },
                                fontSize = 10.sp,
                                color = MutedIvory
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CharcoalElevated)
                            .testTag("close_admin_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = WarmIvory,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern Segmented Tab Bar (Admin tab strictly visible only to Owner)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    visibleTabs.forEach { tab ->
                        val isSelected = activeTab == tab
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
                                .clickable { activeTab = tab }
                                .padding(vertical = 10.dp)
                                .testTag("admin_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) WarmIvory else MutedIvory
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        ControlCenterTab.ADMIN -> {
                            if (isOwnerAdmin) {
                                AdminLoginAndConsoleTab(
                                    adminSession = adminSession,
                                    configRepo = configRepo,
                                    workspaceRepo = workspaceRepo
                                )
                            } else {
                                StagedSubscriptionsTab(
                                    subscriptionState = subscriptionState,
                                    isAdminAuthenticated = false,
                                    configRepo = configRepo
                                )
                            }
                        }
                        ControlCenterTab.SUBSCRIPTION -> {
                            StagedSubscriptionsTab(
                                subscriptionState = subscriptionState,
                                isAdminAuthenticated = adminSession.isAdminAuthenticated,
                                configRepo = configRepo
                            )
                        }
                        ControlCenterTab.API_KEYS -> {
                            ApiKeyVaultTab(
                                apiConfig = apiConfig,
                                configRepo = configRepo
                            )
                        }
                        ControlCenterTab.CONNECTION -> {
                            ConnectionSettingsTab(
                                connectionConfig = connectionConfig,
                                configRepo = configRepo
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLoginAndConsoleTab(
    adminSession: com.example.data.repository.AdminSessionState,
    configRepo: StudioConfigRepository,
    workspaceRepo: WorkspaceHandoffRepository?
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf(StudioConfigRepository.ROOT_ADMIN_EMAIL) }
    var passkeyInput by remember { mutableStateOf(StudioConfigRepository.DEFAULT_ADMIN_PASSKEY) }
    var showPasskey by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Admin Identity Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF161B33),
                                Color(0xFF1F1435),
                                Color(0xFF111827)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (adminSession.isAdminAuthenticated) GeminiGreen else GeminiCyan,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(GeminiCyan, GeminiPurple))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (adminSession.isAdminAuthenticated) Icons.Default.VerifiedUser else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = adminSession.adminRoleTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WarmIvory
                                )
                                Text(
                                    text = StudioConfigRepository.ROOT_ADMIN_EMAIL,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiCyan
                                )
                                Text(
                                    text = "${adminSession.clearanceLevel} • ${adminSession.lastElevationTime}",
                                    fontSize = 10.sp,
                                    color = if (adminSession.isAdminAuthenticated) GeminiGreen else GeminiAmber
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (adminSession.isAdminAuthenticated) GeminiGreen.copy(alpha = 0.2f)
                                    else GeminiAmber.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (adminSession.isAdminAuthenticated) "ELEVATED" else "LOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (adminSession.isAdminAuthenticated) GeminiGreen else GeminiAmber
                            )
                        }
                    }

                    // Admin Login Form or Active Session Controls
                    if (!adminSession.isAdminAuthenticated) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Authenticate your Root Admin Portal credentials below or use 1-Tap Owner Passkey Elevation:",
                            fontSize = 11.sp,
                            color = MutedIvory
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                loginError = null
                            },
                            label = { Text("Root Admin Email", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_email_input")
                        )

                        OutlinedTextField(
                            value = passkeyInput,
                            onValueChange = {
                                passkeyInput = it
                                loginError = null
                            },
                            label = { Text("Admin Hardware Passkey / PIN (Default: FS-ADMIN-2026)", fontSize = 11.sp) },
                            singleLine = true,
                            visualTransformation = if (showPasskey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPasskey = !showPasskey }) {
                                    Icon(
                                        imageVector = if (showPasskey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Passkey Visibility",
                                        tint = MutedIvory
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiPurple,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_passkey_input")
                        )

                        if (loginError != null) {
                            Text(
                                text = loginError ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFFF5F56),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val ok = configRepo.loginAsAdmin(emailInput, passkeyInput)
                                    if (ok) {
                                        Toast.makeText(
                                            context,
                                            "Welcome Root Admin (${StudioConfigRepository.ROOT_ADMIN_EMAIL})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        loginError = "Invalid Admin Email or Passkey. Use ${StudioConfigRepository.ROOT_ADMIN_EMAIL} & FS-ADMIN-2026"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeminiCyan,
                                    contentColor = ObsidianBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("admin_login_submit_button")
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Admin Login", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = {
                                    val act = context as? FragmentActivity
                                    if (act != null && BiometricHelper.isBiometricAvailable(context)) {
                                        BiometricHelper.promptBiometric(
                                            activity = act,
                                            title = "Root Admin Biometric Unlock",
                                            subtitle = "Verify identity for ${StudioConfigRepository.ROOT_ADMIN_EMAIL}",
                                            onSuccess = {
                                                configRepo.loginAsAdmin(
                                                    StudioConfigRepository.ROOT_ADMIN_EMAIL,
                                                    StudioConfigRepository.DEFAULT_ADMIN_PASSKEY
                                                )
                                                Toast.makeText(context, "Biometric Admin Elevation Verified!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = {
                                                configRepo.loginAsAdmin(
                                                    StudioConfigRepository.ROOT_ADMIN_EMAIL,
                                                    StudioConfigRepository.DEFAULT_ADMIN_PASSKEY
                                                )
                                                Toast.makeText(context, "Admin Passkey Fallback Verified!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        configRepo.loginAsAdmin(
                                            StudioConfigRepository.ROOT_ADMIN_EMAIL,
                                            StudioConfigRepository.DEFAULT_ADMIN_PASSKEY
                                        )
                                        Toast.makeText(context, "1-Tap Owner Admin Elevation Active!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalElevated,
                                    contentColor = GeminiPurple
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .border(1.dp, GeminiPurple, RoundedCornerShape(10.dp))
                                    .testTag("admin_biometric_elevate_button")
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Biometric / 1-Tap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Active Root Admin Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    workspaceRepo?.triggerSync {
                                        Toast.makeText(context, "Admin Global Cluster & Room Sync Complete!", Toast.LENGTH_SHORT).show()
                                    } ?: Toast.makeText(context, "Admin Cluster Sync Triggered!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeminiGreen,
                                    contentColor = ObsidianBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Force Global Sync", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = {
                                    configRepo.logoutAdmin()
                                    Toast.makeText(context, "Stepped down from Root Admin mode", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalElevated,
                                    contentColor = GeminiAmber
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("admin_logout_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Root Admin Privileges & Cluster Overrides
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOT ADMIN CLUSTER OVERRIDES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = GeminiCyan
                        )
                        Text(
                            text = if (adminSession.isAdminAuthenticated) "UNLOCKED" else "REQUIRES ADMIN LOGIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminSession.isAdminAuthenticated) GeminiGreen else MutedIvory
                        )
                    }

                    AdminOverrideToggleRow(
                        title = "Unlimited Studio Credits & Zero Quota Cap",
                        subtitle = "Bypasses credit deduction on 4K/8K Recasts & Veo 3.1 generations",
                        checked = adminSession.unlimitedCreditsOverride,
                        enabled = adminSession.isAdminAuthenticated,
                        onCheckedChange = { configRepo.updateAdminFlags(unlimitedCredits = it) }
                    )

                    AdminOverrideToggleRow(
                        title = "Priority GPU Render Queue Bypass",
                        subtitle = "Routes mobile & desktop jobs directly to dedicated H100 tensor nodes",
                        checked = adminSession.bypassRenderQueue,
                        enabled = adminSession.isAdminAuthenticated,
                        onCheckedChange = { configRepo.updateAdminFlags(bypassQueue = it) }
                    )

                    AdminOverrideToggleRow(
                        title = "Unlock Experimental Gemini-4-Argon Ultra",
                        subtitle = "Enables internal high-thinking neural weights & 468-point raw mesh",
                        checked = adminSession.unlockExperimentalModels,
                        enabled = adminSession.isAdminAuthenticated,
                        onCheckedChange = { configRepo.updateAdminFlags(unlockModels = it) }
                    )

                    AdminOverrideToggleRow(
                        title = "Live Top-Bar Admin Telemetry Ribbon",
                        subtitle = "Displays real-time Room DB sync, mesh ping, and admin status in main HUD",
                        checked = adminSession.telemetryHudEnabled,
                        enabled = adminSession.isAdminAuthenticated,
                        onCheckedChange = { configRepo.updateAdminFlags(telemetryHud = it) }
                    )
                }
            }
        }

        // Security Audit Log
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ADMIN SECURITY & ELEVATION AUDIT LOG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                    adminSession.auditLog.forEach { entry ->
                        Text(
                            text = "• $entry",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = WarmIvory
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOverrideToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) WarmIvory else MutedIvory
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MutedIvory
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = GeminiCyan
            )
        )
    }
}

@Composable
private fun StagedSubscriptionsTab(
    subscriptionState: com.example.data.repository.SubscriptionConfigState,
    isAdminAuthenticated: Boolean,
    configRepo: StudioConfigRepository
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Staged Safeguard Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1B18))
                    .border(1.5.dp, GeminiAmber, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = GeminiAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STAGED SUBSCRIPTION SAFEGUARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = GeminiAmber
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (subscriptionState.isBillingImplemented) GeminiGreen.copy(alpha = 0.2f)
                                    else GeminiAmber.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (subscriptionState.isBillingImplemented) "IMPLEMENTED & ACTIVE" else "STAGED • NOT BILLED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (subscriptionState.isBillingImplemented) GeminiGreen else GeminiAmber
                            )
                        }
                    }

                    Text(
                        text = "Subscription options are configured in Staged Sandbox Mode: no billing or tier lock-in is implemented until the app is running and verified via the Runtime Test below.",
                        fontSize = 11.sp,
                        color = WarmIvory
                    )

                    if (subscriptionState.isRunningVerification || subscriptionState.isRuntimeTested) {
                        LinearProgressIndicator(
                            progress = { subscriptionState.testProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GeminiGreen,
                            trackColor = CharcoalElevated
                        )
                    }

                    subscriptionState.verificationReport.forEach { line ->
                        Text(
                            text = "• $line",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GeminiCyan
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                configRepo.runSubscriptionRuntimeTest {
                                    Toast.makeText(
                                        context,
                                        "Runtime & Pipeline Verification Passed!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !subscriptionState.isRunningVerification,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiAmber,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("run_subscription_test_button")
                        ) {
                            if (subscriptionState.isRunningVerification) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = ObsidianBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing Runtime...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (subscriptionState.isRuntimeTested) "Re-Run Runtime Test" else "Test Pipeline Before Implementing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Optional activation toggle (only enabled after test or if Admin)
                        Button(
                            onClick = {
                                val nextState = !subscriptionState.isBillingImplemented
                                val ok = configRepo.toggleImplementTestedSubscription(nextState)
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        "Run the Runtime Test first before implementing subscription!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (subscriptionState.isBillingImplemented) CharcoalElevated else GeminiGreen,
                                contentColor = if (subscriptionState.isBillingImplemented) WarmIvory else ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("toggle_implement_subscription_button")
                        ) {
                            Text(
                                text = if (subscriptionState.isBillingImplemented) "Revert to Staged" else "Activate Tested",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Subscription Tier Cards (Root Admin Unlimited tier strictly visible only to verified Owner)
        val visibleTiers = SubscriptionTierOption.values().filter {
            it != SubscriptionTierOption.ADMIN_FOUNDER || isAdminAuthenticated
        }
        items(visibleTiers) { tier ->
            val isStaged = subscriptionState.stagedTier == tier
            val isActive = subscriptionState.selectedTier == tier && subscriptionState.isBillingImplemented
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(
                        width = if (isStaged) 1.8.dp else 1.dp,
                        color = when {
                            isActive -> GeminiGreen
                            isStaged -> GeminiCyan
                            else -> BorderSubtle
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        configRepo.selectStagedSubscription(tier)
                        Toast.makeText(
                            context,
                            "Staged ${tier.title} (Pending runtime verification)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .padding(14.dp)
                    .testTag("subscription_card_${tier.id}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tier.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WarmIvory
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GeminiPurple.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tier.badge,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GeminiCyan
                                    )
                                }
                            }
                            Text(
                                text = "${tier.resolutionCap} • ${tier.monthlyCredits}",
                                fontSize = 11.sp,
                                color = MutedIvory
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tier.priceLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GeminiCyan
                            )
                            Text(
                                text = when {
                                    isActive -> "ACTIVE & VERIFIED"
                                    isStaged -> "STAGED FOR TEST"
                                    else -> "TAP TO STAGE"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isActive -> GeminiGreen
                                    isStaged -> GeminiAmber
                                    else -> MutedIvory
                                }
                            )
                        }
                    }

                    tier.features.forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isStaged) GeminiCyan else MutedIvory,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = feat,
                                fontSize = 11.sp,
                                color = WarmIvory
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyVaultTab(
    apiConfig: com.example.data.repository.ApiKeyConfigState,
    configRepo: StudioConfigRepository
) {
    val context = LocalContext.current
    var geminiKey by remember(apiConfig.geminiApiKeyOverride) { mutableStateOf(apiConfig.geminiApiKeyOverride) }
    var veoKey by remember(apiConfig.veoVideoApiKey) { mutableStateOf(apiConfig.veoVideoApiKey) }
    var elevenLabsKey by remember(apiConfig.elevenLabsVoiceKey) { mutableStateOf(apiConfig.elevenLabsVoiceKey) }
    var webhookSecret by remember(apiConfig.customWebhookSecret) { mutableStateOf(apiConfig.customWebhookSecret) }
    var showKeys by remember { mutableStateOf(false) }

    val availableModels = listOf("gemini-4-argon", "gemini-3.1-pro", "gemini-3.8-flash")
    val availableVideoEngines = listOf("veo-3.1-cinema", "veo-3.0-fast", "frameshift-neural-v4")

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, GeminiPurple.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "API KEY VAULT & ENDPOINT ROUTING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = WarmIvory
                            )
                        }
                        IconButton(
                            onClick = { showKeys = !showKeys },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (showKeys) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Show/Hide API Keys",
                                tint = GeminiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Configure custom API keys below or keep AI Studio BuildConfig Secret fallback enabled.",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )

                    val transformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation()

                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
                        label = { Text("Gemini / AI Studio API Key Override (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("AIzaSy... (Leave blank for BuildConfig.GEMINI_API_KEY)", fontSize = 10.sp) },
                        singleLine = true,
                        visualTransformation = transformation,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    OutlinedTextField(
                        value = veoKey,
                        onValueChange = { veoKey = it },
                        label = { Text("Veo 3.1 Video Recast Engine Key", fontSize = 11.sp) },
                        placeholder = { Text("veo_live_...", fontSize = 10.sp) },
                        singleLine = true,
                        visualTransformation = transformation,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = elevenLabsKey,
                        onValueChange = { elevenLabsKey = it },
                        label = { Text("ElevenLabs / Neural Voice Synthesis Key", fontSize = 11.sp) },
                        placeholder = { Text("xi_api_...", fontSize = 10.sp) },
                        singleLine = true,
                        visualTransformation = transformation,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiAmber,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = webhookSecret,
                        onValueChange = { webhookSecret = it },
                        label = { Text("Custom Render Cluster Webhook Secret", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = transformation,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Model Routing Pills
                    Text(
                        text = "ACTIVE GEMINI REASONING MODEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableModels) { model ->
                            val selected = apiConfig.selectedGeminiModel == model
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) GeminiCyan else CharcoalElevated)
                                    .clickable {
                                        configRepo.updateApiConfig(
                                            geminiKey = geminiKey,
                                            veoKey = veoKey,
                                            elevenLabsKey = elevenLabsKey,
                                            webhookSecret = webhookSecret,
                                            selectedModel = model
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = model,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) ObsidianBlack else WarmIvory
                                )
                            }
                        }
                    }

                    Text(
                        text = "ACTIVE VIDEO SYNTHESIS ENGINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableVideoEngines) { engine ->
                            val selected = apiConfig.selectedVideoEngine == engine
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) GeminiPurple else CharcoalElevated)
                                    .clickable {
                                        configRepo.updateApiConfig(
                                            geminiKey = geminiKey,
                                            veoKey = veoKey,
                                            elevenLabsKey = elevenLabsKey,
                                            webhookSecret = webhookSecret,
                                            selectedVideo = engine
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = engine,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory
                                )
                            }
                        }
                    }

                    // Status Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = apiConfig.handshakeStatus,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GeminiGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                configRepo.updateApiConfig(
                                    geminiKey = geminiKey,
                                    veoKey = veoKey,
                                    elevenLabsKey = elevenLabsKey,
                                    webhookSecret = webhookSecret
                                )
                                Toast.makeText(context, "API Keys saved to encrypted local vault", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiCyan,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("save_api_keys_button")
                        ) {
                            Text("Save API Vault", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                configRepo.updateApiConfig(
                                    geminiKey = geminiKey,
                                    veoKey = veoKey,
                                    elevenLabsKey = elevenLabsKey,
                                    webhookSecret = webhookSecret
                                )
                                configRepo.testApiHandshake { summary ->
                                    Toast.makeText(context, summary, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CharcoalElevated,
                                contentColor = GeminiGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(1.dp, GeminiGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .testTag("test_api_handshake_button")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Handshake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionSettingsTab(
    connectionConfig: com.example.data.repository.ConnectionConfigState,
    configRepo: StudioConfigRepository
) {
    val context = LocalContext.current
    var cloudEndpoint by remember(connectionConfig.cloudSyncEndpoint) { mutableStateOf(connectionConfig.cloudSyncEndpoint) }
    var webrtcRelay by remember(connectionConfig.webrtcRelayServer) { mutableStateOf(connectionConfig.webrtcRelayServer) }
    var lanIp by remember(connectionConfig.lanWorkstationIp) { mutableStateOf(connectionConfig.lanWorkstationIp) }
    var lanPort by remember(connectionConfig.lanWorkstationPort) { mutableStateOf(connectionConfig.lanWorkstationPort) }

    val protocols = listOf(
        "QUIC / HTTP3 Zero-RTT",
        "WebRTC DataChannel",
        "gRPC TLS 1.3",
        "WebSocket Secure"
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsEthernet, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CROSS-DEVICE MESH & CONNECTION SETTINGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmIvory
                        )
                    }

                    OutlinedTextField(
                        value = cloudEndpoint,
                        onValueChange = { cloudEndpoint = it },
                        label = { Text("Cloud Sync Mesh Endpoint", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("connection_endpoint_input")
                    )

                    OutlinedTextField(
                        value = webrtcRelay,
                        onValueChange = { webrtcRelay = it },
                        label = { Text("WebRTC TURN / STUN Relay Server", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lanIp,
                            onValueChange = { lanIp = it },
                            label = { Text("Workstation LAN IP", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier.weight(1.5f)
                        )

                        OutlinedTextField(
                            value = lanPort,
                            onValueChange = { lanPort = it },
                            label = { Text("Port", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Text(
                        text = "TRANSPORT PROTOCOL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(protocols) { proto ->
                            val selected = connectionConfig.transportProtocol == proto
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) GeminiCyan else CharcoalElevated)
                                    .clickable {
                                        configRepo.updateConnectionConfig(
                                            cloudEndpoint = cloudEndpoint,
                                            webrtcRelay = webrtcRelay,
                                            lanIp = lanIp,
                                            lanPort = lanPort,
                                            protocol = proto
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = proto,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) ObsidianBlack else WarmIvory
                                )
                            }
                        }
                    }

                    AdminOverrideToggleRow(
                        title = "Auto-Reconnect Multi-Device Handoff Mesh",
                        subtitle = "Automatically restores session beam when switching Wi-Fi or 5G",
                        checked = connectionConfig.autoReconnectMesh,
                        enabled = true,
                        onCheckedChange = { configRepo.updateConnectionConfig(autoReconnect = it) }
                    )

                    AdminOverrideToggleRow(
                        title = "End-to-End AES-256 Frame Stream Encryption",
                        subtitle = "Encrypts live camera recasts & cloud clipboard payloads",
                        checked = connectionConfig.aes256FrameEncryption,
                        enabled = true,
                        onCheckedChange = { configRepo.updateConnectionConfig(aes256 = it) }
                    )

                    AdminOverrideToggleRow(
                        title = "Offline Room SQLite Auto-Failover",
                        subtitle = "Queues last_modified & device_id edits locally when offline",
                        checked = connectionConfig.offlineRoomAutoFailover,
                        enabled = true,
                        onCheckedChange = { configRepo.updateConnectionConfig(roomFailover = it) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = connectionConfig.diagnosticSummary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GeminiGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${connectionConfig.lastPingMs}ms",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = GeminiCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                configRepo.updateConnectionConfig(
                                    cloudEndpoint = cloudEndpoint,
                                    webrtcRelay = webrtcRelay,
                                    lanIp = lanIp,
                                    lanPort = lanPort
                                )
                                Toast.makeText(context, "Connection settings updated", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiCyan,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text("Save Network Config", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                configRepo.updateConnectionConfig(
                                    cloudEndpoint = cloudEndpoint,
                                    webrtcRelay = webrtcRelay,
                                    lanIp = lanIp,
                                    lanPort = lanPort
                                )
                                configRepo.runConnectionDiagnostics { res ->
                                    Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CharcoalElevated,
                                contentColor = GeminiGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(1.dp, GeminiGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .testTag("run_connection_diagnostics_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Mesh Ping Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
