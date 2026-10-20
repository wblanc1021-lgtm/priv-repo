package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import com.example.data.gemini.GeminiService
import com.example.data.model.RecastProject
import com.example.data.model.RecastVariant
import com.example.data.repository.PromptLabRepository
import com.example.data.repository.RecastRepository
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.service.recast.RecastEngine
import com.example.ui.components.AdminAndConfigCenterModal
import com.example.ui.components.AiStudioAmbientBackground
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.LoginPortalModal
import com.example.ui.components.rememberAnimatedRainbowBrush
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GenerativeMediaStudioScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LiveSessionScreen
import com.example.ui.screens.PromptLabScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.UnifiedWorkspaceScreen
import com.example.ui.screens.VariantGridScreen
import com.example.ui.screens.VoicePanelScreen
import com.example.ui.screens.signOut
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.FrameShiftTheme
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val icon: ImageVector) {
    WORKSPACE("Workspace", Icons.Default.Devices),
    STUDIO("Studio", Icons.Default.Movie),
    GENERATIVE("Veo & Nano", Icons.Default.AutoAwesome),
    RESULT("Compare", Icons.Default.Splitscreen),
    VOICE("Voice", Icons.Default.RecordVoiceOver),
    LIVE("Live Cam", Icons.Default.Videocam),
    PROMPT_LAB("Prompt Lab", Icons.Default.Tune),
    VAULT("Vault", Icons.Default.Folder)
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FrameShiftTheme {
                AppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val user = currentUser!!
        val recastRepository = remember { RecastRepository(context) }
        val promptLabRepository = remember { PromptLabRepository(context) }
        val workspaceHandoffRepository = remember { WorkspaceHandoffRepository(context) }
        val studioConfigRepository = remember { StudioConfigRepository(context) }
        val geminiService = remember { GeminiService(configRepo = studioConfigRepository) }
        val recastEngine = remember {
            RecastEngine(
                configRepo = studioConfigRepository,
                workspaceRepo = workspaceHandoffRepository
            )
        }

        val adminSession by studioConfigRepository.adminSession.collectAsState()
        val subscriptionState by studioConfigRepository.subscriptionState.collectAsState()
        val apiConfig by studioConfigRepository.apiConfig.collectAsState()
        val connectionConfig by studioConfigRepository.connectionConfig.collectAsState()
        val systemBus by studioConfigRepository.systemBus.collectAsState()

        var showLoginPortalModal by remember { mutableStateOf(false) }
        var showAdminAndConfigModal by remember { mutableStateOf(false) }
        var showVariantGridSubScreen by remember { mutableStateOf(false) }
        var configModalInitialTab by remember { mutableStateOf(ControlCenterTab.SUBSCRIPTION) }

        LaunchedEffect(user.uid, user.email) {
            studioConfigRepository.syncWithFirebaseUser(user.email)
            recastRepository.ensureUserProfileCreated(
                displayName = user.displayName ?: "Studio Director",
                email = user.email ?: ""
            )
        }

        var currentScreen by remember { mutableStateOf(AppScreen.WORKSPACE) }

        var activeProject by remember {
            mutableStateOf(
                RecastProject(
                    id = "proj_${System.currentTimeMillis()}",
                    userId = user.uid,
                    title = "Neo-Noir Alley Recast",
                    sourceVideoName = "Cyberpunk Alley Run",
                    sourceVideoDuration = 12L,
                    sourceVideoResolution = "1080p",
                    primaryReferenceName = "Elena (Sci-Fi Detective)",
                    characterStrength = 85L,
                    qualityPreset = "high",
                    resolution = "1080p",
                    frameRate = 60L,
                    compressionLevel = 22L,
                    conversionDirection = "forward_recast",
                    status = "draft"
                )
            )
        }

        val generatedVariants = remember { mutableStateListOf<RecastVariant>() }

        val projectList by recastRepository.observeProjects()
            .catch { emit(emptyList()) }
            .collectAsState(initial = emptyList())

        AiStudioAmbientBackground {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GeminiSparkle(size = 20.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "FRAMESHIFT",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.6.sp,
                                                color = WarmIvory
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "2-WAY AI",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = GeminiCyan
                                            )
                                        }
                                        Text(
                                            text = "${activeProject.resolution} ${activeProject.frameRate}fps • ${if (adminSession.isAdminAuthenticated) "OWNER ADMIN" else activeProject.activeConversionMode().shortTitle}",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (adminSession.isAdminAuthenticated) GeminiCyan else MutedIvory
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Root Admin Pill — STRICTLY visible ONLY to verified owner (wblanc1021@gmail.com)
                                if (adminSession.isAdminAuthenticated) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GeminiCyan.copy(alpha = 0.16f))
                                            .border(1.dp, GeminiCyan.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                configModalInitialTab = ControlCenterTab.ADMIN
                                                showAdminAndConfigModal = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                            .testTag("top_admin_login_button"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Owner Admin Console",
                                            tint = GeminiCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "wblanc1021",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmIvory
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(5.dp))
                                }

                                // Plans / API / Connection Config Pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CharcoalElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable {
                                            configModalInitialTab = ControlCenterTab.SUBSCRIPTION
                                            showAdminAndConfigModal = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                        .testTag("top_config_center_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Subscriptions & API Settings",
                                        tint = GeminiAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Config",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarmIvory
                                    )
                                }

                                Spacer(modifier = Modifier.width(5.dp))

                                // Sync Portal Pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CharcoalElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { showLoginPortalModal = true }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                        .testTag("top_sync_portal_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = "Sync Portal",
                                        tint = GeminiGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sync",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarmIvory
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val credManager = CredentialManager.create(context)
                                        signOut(context, credManager, { currentUser = null }, scope)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Sign Out",
                                        tint = MutedIvory,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ObsidianBlack.copy(alpha = 0.94f),
                                titleContentColor = WarmIvory
                            )
                        )

                        // Clean Studio Glass Hairline Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            BorderSubtle,
                                            GeminiCyan.copy(alpha = 0.35f),
                                            NeonOrange.copy(alpha = 0.35f),
                                            BorderSubtle
                                        )
                                    )
                                )
                        )

                        // Live Root Admin & System Integration Bus Telemetry Ribbon
                        if (adminSession.isAdminAuthenticated && adminSession.telemetryHudEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0E1525))
                                    .clickable {
                                        configModalInitialTab = ControlCenterTab.ADMIN
                                        showAdminAndConfigModal = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GeminiGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ADMIN: ${StudioConfigRepository.ROOT_ADMIN_EMAIL} • [${systemBus.lastActiveModule}] ${systemBus.lastEventSummary}",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiCyan,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${if (subscriptionState.isBillingImplemented) "PLAN LIVE" else "STAGED"} • ${connectionConfig.lastPingMs}ms",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GeminiAmber
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    // Next-Gen Floating Glassmorphic Scrollable Pill Dock
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianBlack.copy(alpha = 0.92f))
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.2.dp)
                                .background(rememberAnimatedRainbowBrush(durationMillis = 5000))
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(AppScreen.values().toList()) { screen ->
                                val isSelected = currentScreen == screen
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(listOf(GeminiBlue, GeminiPurple))
                                            } else {
                                                Brush.horizontalGradient(listOf(CharcoalCard, CharcoalCard))
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) GeminiCyan else BorderSubtle,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { currentScreen = screen }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .testTag("nav_tab_${screen.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) WarmIvory else MutedIvory,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = screen.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                            color = if (isSelected) WarmIvory else MutedIvory
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (currentScreen != AppScreen.WORKSPACE || showVariantGridSubScreen) {
                        BackHandler {
                            if (showVariantGridSubScreen) {
                                showVariantGridSubScreen = false
                            } else {
                                currentScreen = AppScreen.WORKSPACE
                            }
                        }
                    }

                    val openConfigTab: (ControlCenterTab) -> Unit = { tab ->
                        configModalInitialTab = tab
                        showAdminAndConfigModal = true
                    }

                    val updateActiveAndSync: (RecastProject) -> Unit = { updated ->
                        activeProject = updated
                        workspaceHandoffRepository.updateSharedProject(updated)
                    }

                    when (currentScreen) {
                        AppScreen.WORKSPACE -> {
                            UnifiedWorkspaceScreen(
                                project = activeProject,
                                onProjectChange = updateActiveAndSync,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenPortal = { showLoginPortalModal = true },
                                onNavigateToStudio = { currentScreen = AppScreen.STUDIO },
                                onNavigateToLiveCam = { currentScreen = AppScreen.LIVE },
                                onNavigateToResult = { currentScreen = AppScreen.RESULT },
                                configRepo = studioConfigRepository,
                                onOpenConfigCenter = openConfigTab
                            )
                        }

                        AppScreen.STUDIO -> {
                            StudioScreen(
                                currentProject = activeProject,
                                onProjectChange = updateActiveAndSync,
                                onGenerationComplete = { proj, vars ->
                                    activeProject = proj
                                    workspaceHandoffRepository.updateSharedProject(proj)
                                    generatedVariants.clear()
                                    generatedVariants.addAll(vars)
                                    scope.launch {
                                        runCatching { recastRepository.saveProject(proj) }
                                    }
                                    showVariantGridSubScreen = false
                                    currentScreen = AppScreen.RESULT
                                },
                                geminiService = geminiService,
                                recastEngine = recastEngine,
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab,
                                onNavigateToLiveCam = { currentScreen = AppScreen.LIVE }
                            )
                        }

                        AppScreen.GENERATIVE -> {
                            GenerativeMediaStudioScreen(
                                geminiService = geminiService,
                                currentProject = activeProject,
                                onProjectChange = updateActiveAndSync,
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab,
                                onNavigateToStudio = { currentScreen = AppScreen.STUDIO }
                            )
                        }

                        AppScreen.RESULT -> {
                            if (showVariantGridSubScreen) {
                                VariantGridScreen(
                                    project = activeProject,
                                    variants = generatedVariants,
                                    onSelectVariant = { selectedVar ->
                                        val updated = activeProject.copy(
                                            seed = selectedVar.seed,
                                            characterStrength = selectedVar.characterStrength.toLong(),
                                            resultVideoTitle = "${activeProject.title} • ${selectedVar.previewLabel}"
                                        )
                                        updateActiveAndSync(updated)
                                        studioConfigRepository.recordSystemEvent(
                                            "Variant Grid",
                                            "Promoted Take #${selectedVar.variantIndex} (Seed ${selectedVar.seed}) as Master"
                                        )
                                    },
                                    onBack = { showVariantGridSubScreen = false }
                                )
                            } else {
                                ResultScreen(
                                    project = activeProject,
                                    variants = generatedVariants,
                                    onBackToStudio = { currentScreen = AppScreen.STUDIO },
                                    onSaveProject = {
                                        scope.launch {
                                            runCatching { recastRepository.saveProject(activeProject) }
                                            workspaceHandoffRepository.updateSharedProject(activeProject)
                                            studioConfigRepository.recordSystemEvent(
                                                "Compare & Master",
                                                "Saved '${activeProject.title}' to Cloud Vault & Room DB"
                                            )
                                            Toast.makeText(context, "Saved to Cloud Vault, Room DB & Offline Cache", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onRegenerate = { currentScreen = AppScreen.STUDIO },
                                    onNavigateToVariants = { showVariantGridSubScreen = true },
                                    onNavigateToVoice = { currentScreen = AppScreen.VOICE },
                                    configRepo = studioConfigRepository,
                                    workspaceRepo = workspaceHandoffRepository,
                                    onOpenConfigCenter = openConfigTab,
                                    onNavigateToLiveCam = { currentScreen = AppScreen.LIVE }
                                )
                            }
                        }

                        AppScreen.VOICE -> {
                            VoicePanelScreen(
                                project = activeProject,
                                onProjectChange = updateActiveAndSync,
                                onBack = { currentScreen = AppScreen.STUDIO },
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab,
                                onNavigateToLiveCam = { currentScreen = AppScreen.LIVE }
                            )
                        }

                        AppScreen.LIVE -> {
                            LiveSessionScreen(
                                characterName = activeProject.primaryReferenceName.ifBlank { "Elena (Sci-Fi Detective)" },
                                currentProject = activeProject,
                                onProjectChange = updateActiveAndSync,
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab
                            )
                        }

                        AppScreen.PROMPT_LAB -> {
                            PromptLabScreen(
                                geminiService = geminiService,
                                promptLabRepository = promptLabRepository,
                                currentProject = activeProject,
                                onProjectChange = updateActiveAndSync,
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab,
                                onNavigateToStudio = { currentScreen = AppScreen.STUDIO },
                                onNavigateToVeo = { currentScreen = AppScreen.GENERATIVE }
                            )
                        }

                        AppScreen.VAULT -> {
                            HistoryScreen(
                                projects = projectList,
                                onSelectProject = { p ->
                                    updateActiveAndSync(p)
                                    currentScreen = AppScreen.STUDIO
                                    Toast.makeText(context, "Loaded '${p.title}' into Studio & Room Sync", Toast.LENGTH_SHORT).show()
                                },
                                recastRepository = recastRepository,
                                activeProject = activeProject,
                                configRepo = studioConfigRepository,
                                workspaceRepo = workspaceHandoffRepository,
                                onOpenConfigCenter = openConfigTab
                            )
                        }
                    }
                }

                // Multi-Device Login & Cloud Sync Portal Modal
                LoginPortalModal(
                    isOpen = showLoginPortalModal,
                    onDismiss = { showLoginPortalModal = false },
                    workspaceRepo = workspaceHandoffRepository,
                    currentProject = activeProject,
                    onSignOut = {
                        val credManager = CredentialManager.create(context)
                        signOut(context, credManager, { currentUser = null }, scope)
                    },
                    onOpenAdminConsole = {
                        configModalInitialTab = ControlCenterTab.ADMIN
                        showAdminAndConfigModal = true
                    },
                    onOpenSubscriptions = {
                        configModalInitialTab = ControlCenterTab.SUBSCRIPTION
                        showAdminAndConfigModal = true
                    }
                )

                // Root Admin Login, Staged Subscriptions, API Keys & Connection Settings Modal
                AdminAndConfigCenterModal(
                    isOpen = showAdminAndConfigModal,
                    initialTab = configModalInitialTab,
                    onDismiss = { showAdminAndConfigModal = false },
                    configRepo = studioConfigRepository,
                    workspaceRepo = workspaceHandoffRepository
                )
            }
        }
    }
}
