package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.GeminiService
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.AiStudioThinkingLoader
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.components.rememberAnimatedRainbowBrush
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import androidx.compose.runtime.collectAsState
import com.example.data.model.RecastProject
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.launch

@Composable
fun GenerativeMediaStudioScreen(
    geminiService: GeminiService,
    currentProject: RecastProject? = null,
    onProjectChange: ((RecastProject) -> Unit)? = null,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    onNavigateToStudio: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedStudioTab by remember { mutableIntStateOf(0) } // 0: Veo Video Studio, 1: Nano Image Lab

    // Veo Generation States (Bidirectional Photo <-> Video + Fidelity Sliders)
    var veoMode by remember { mutableStateOf("text_to_video") } // "text_to_video", "animate_image", "extract_identity"
    var veoPrompt by remember {
        mutableStateOf("Cinematic Steadicam shot of a neon cyberpunk character walking through a rain-slicked Tokyo alleyway at dusk, anamorphic lens flare, 60fps volumetric lighting")
    }
    var veoAspectRatio by remember { mutableStateOf("16:9") } // Strictly "16:9" or "9:16"
    var veoResolution by remember(currentProject?.resolution) { mutableStateOf(currentProject?.resolution ?: "1080p") }
    var veoFrameRate by remember(currentProject?.frameRate) { mutableStateOf(currentProject?.frameRate ?: 60L) }
    var veoCompression by remember(currentProject?.compressionLevel) { mutableStateOf(currentProject?.compressionLevel ?: 18L) }
    var selectedPhotoTitle by remember { mutableStateOf("Elena (Sci-Fi Detective Portrait)") }
    var isGeneratingVeo by remember { mutableStateOf(false) }
    var veoResultText by remember { mutableStateOf<String?>(null) }

    // Nano Banana 2.1 States
    var nanoMode by remember { mutableStateOf("create") } // "create" vs "edit"
    var nanoPrompt by remember {
        mutableStateOf("Hyper-realistic neo-noir detective in obsidian trench coat with amber neon fiber optics, dramatic split rim lighting, 8k sharp focus")
    }
    var nanoAspectRatio by remember { mutableStateOf("1:1") }
    var nanoImageSize by remember { mutableStateOf("2K") }
    var selectedBaseImageTitle by remember { mutableStateOf("Studio Character Headshot") }
    var isGeneratingNano by remember { mutableStateOf(false) }
    var nanoResultText by remember { mutableStateOf<String?>(null) }

    val sampleStills = listOf(
        "Elena (Sci-Fi Detective Portrait)",
        "Marcus (Neo-Noir Alley Standoff)",
        "Aria (Cyborg Operative Studio)",
        "Kael (Neon Rooftop Horizon)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Studio Selector Tabs with AI Studio animated rainbow line
        val rainbowBrush = rememberAnimatedRainbowBrush(durationMillis = 3500)
        TabRow(
            selectedTabIndex = selectedStudioTab,
            containerColor = CharcoalCard,
            contentColor = WarmIvory,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedStudioTab])
                        .height(3.5.dp)
                        .background(rainbowBrush)
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedStudioTab == 0,
                onClick = { selectedStudioTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GeminiSparkle(size = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VEO 3 VIDEO STUDIO",
                            fontSize = 11.sp,
                            fontWeight = if (selectedStudioTab == 0) FontWeight.Black else FontWeight.Normal,
                            color = if (selectedStudioTab == 0) GeminiCyan else WarmIvory
                        )
                    }
                },
                modifier = Modifier.testTag("tab_veo_studio")
            )
            Tab(
                selected = selectedStudioTab == 1,
                onClick = { selectedStudioTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedStudioTab == 1) GeminiAmber else MutedIvory)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NANO 2.1 IMAGE LAB",
                            fontSize = 11.sp,
                            fontWeight = if (selectedStudioTab == 1) FontWeight.Black else FontWeight.Normal,
                            color = if (selectedStudioTab == 1) GeminiAmber else WarmIvory
                        )
                    }
                },
                modifier = Modifier.testTag("tab_nano_image_lab")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (currentProject != null && configRepo != null && workspaceRepo != null) {
                item {
                    val systemBus by configRepo.systemBus.collectAsState()
                    SystemIntegrationHubBanner(
                        moduleName = if (selectedStudioTab == 0) "Veo 3 Video" else "Nano 2.1 Lab",
                        project = currentProject,
                        configRepo = configRepo,
                        workspaceRepo = workspaceRepo,
                        onOpenConfigCenter = onOpenConfigCenter,
                        primaryBridgeLabel = "Sync Project Prompt",
                        onPrimaryBridgeClick = {
                            val syncedPrompt = "Cinematic shot of ${currentProject.primaryReferenceName} in ${currentProject.sourceVideoName} (${currentProject.characterStrength}% fidelity) • ${systemBus.lastOptimizedPrompt.take(75)}"
                            if (selectedStudioTab == 0) {
                                veoPrompt = syncedPrompt
                            } else {
                                nanoPrompt = syncedPrompt
                            }
                            configRepo.recordSystemEvent("Veo & Nano", "Loaded active project & Prompt Lab context into generator")
                            Toast.makeText(context, "Synced prompt from Active Studio Project & Prompt Lab!", Toast.LENGTH_SHORT).show()
                        },
                        secondaryBridgeLabel = "Open Studio",
                        onSecondaryBridgeClick = { onNavigateToStudio?.invoke() }
                    )
                }
            }

            if (selectedStudioTab == 0) {
                // ==================== VEO 3 VIDEO STUDIO ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VEO 3 VIDEO GENERATION",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Model: veo-3.1-fast-generate-preview",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonOrange
                                )
                            }

                            // Mode Selector (Text-to-Video vs Animate Photo)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CharcoalElevated)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (veoMode == "text_to_video") NeonOrange else Color.Transparent)
                                        .clickable { veoMode = "text_to_video" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Text-to-Video",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (veoMode == "text_to_video") ObsidianBlack else WarmIvory
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (veoMode == "animate_image") NeonOrange else Color.Transparent)
                                        .clickable { veoMode = "animate_image" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Animate Photo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (veoMode == "animate_image") ObsidianBlack else WarmIvory
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Photo Picker if Animate Image mode
                        if (veoMode == "animate_image") {
                            Text(
                                text = "UPLOAD / SELECT PHOTO TO ANIMATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedIvory
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sampleStills) { still ->
                                    val isSelected = selectedPhotoTitle == still
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF331604) else CharcoalElevated)
                                            .border(1.dp, if (isSelected) NeonOrange else BorderSubtle, RoundedCornerShape(6.dp))
                                        .clickable { selectedPhotoTitle = still }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = if (isSelected) NeonOrange else MutedIvory, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = still.substringBefore(" ("),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) NeonOrange else WarmIvory
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Veo Prompt
                        Text(
                            text = if (veoMode == "animate_image") "MOTION / ANIMATION PROMPT" else "VIDEO GENERATION PROMPT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = veoPrompt,
                            onValueChange = { veoPrompt = it },
                            placeholder = { Text("Describe the cinematic motion, camera movement, and action...", color = MutedIvory, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("veo_prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonOrange,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // STRICT ASPECT RATIO: 16:9 (Landscape) or 9:16 (Portrait)
                        Text(
                            text = "VEO ASPECT RATIO (MANDATORY 16:9 OR 9:16)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 16:9 Landscape Option
                            val is169 = veoAspectRatio == "16:9"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (is169) Color(0xFF331604) else CharcoalElevated)
                                    .border(1.5.dp, if (is169) NeonOrange else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { veoAspectRatio = "16:9" }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CropLandscape, contentDescription = null, tint = if (is169) NeonOrange else MutedIvory, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("16:9 Landscape", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (is169) NeonOrange else WarmIvory)
                                        Text("Widescreen Cinema", fontSize = 9.sp, color = MutedIvory)
                                    }
                                }
                            }

                            // 9:16 Portrait Option
                            val is916 = veoAspectRatio == "9:16"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (is916) Color(0xFF331604) else CharcoalElevated)
                                    .border(1.5.dp, if (is916) NeonOrange else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { veoAspectRatio = "9:16" }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CropPortrait, contentDescription = null, tint = if (is916) NeonOrange else MutedIvory, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("9:16 Portrait", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (is916) NeonOrange else WarmIvory)
                                        Text("Vertical Reels / Shorts", fontSize = 9.sp, color = MutedIvory)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Adaptive Video Fidelity Sliders (Resolution, Frame Rate, Compression)
                        com.example.ui.components.VideoFidelityCompactStrip(
                            resolution = veoResolution,
                            frameRate = veoFrameRate,
                            compressionLevel = veoCompression,
                            onResolutionChange = { res ->
                                veoResolution = res
                                if (currentProject != null && onProjectChange != null) {
                                    val updated = currentProject.copy(resolution = res, sourceVideoResolution = res)
                                    onProjectChange(updated)
                                    workspaceRepo?.updateSharedProject(updated)
                                }
                            },
                            onFrameRateChange = { fps ->
                                veoFrameRate = fps
                                if (currentProject != null && onProjectChange != null) {
                                    val updated = currentProject.copy(frameRate = fps)
                                    onProjectChange(updated)
                                    workspaceRepo?.updateSharedProject(updated)
                                }
                            },
                            onCompressionChange = { comp ->
                                veoCompression = comp
                                if (currentProject != null && onProjectChange != null) {
                                    val updated = currentProject.copy(compressionLevel = comp)
                                    onProjectChange(updated)
                                    workspaceRepo?.updateSharedProject(updated)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Studio Thinking Loader for Veo
                        if (isGeneratingVeo) {
                            AiStudioThinkingLoader(
                                stageText = "Google DeepMind Veo 3: Synthesizing temporal coherence at $veoResolution • ${veoFrameRate}fps • $veoCompression% compression ($veoAspectRatio)...",
                                progressFraction = null,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Generate Veo Video Action with clean glass studio border
                        AiStudioRainbowButton(
                            text = if (isGeneratingVeo) "RENDERING VEO 3 ($veoResolution @ ${veoFrameRate}FPS)..." else if (veoMode == "animate_image") "ANIMATE PHOTO → VIDEO ($veoResolution)" else "GENERATE VEO 3 VIDEO ($veoResolution @ ${veoFrameRate}FPS)",
                            onClick = {
                                isGeneratingVeo = true
                                veoResultText = null
                                scope.launch {
                                    val result = geminiService.generateVideoWithVeo(
                                        prompt = if (veoMode == "animate_image") "Animate photo [$selectedPhotoTitle] ($veoResolution @ ${veoFrameRate}fps, Comp $veoCompression%): $veoPrompt" else "$veoPrompt ($veoResolution @ ${veoFrameRate}fps, Comp $veoCompression%)",
                                        inputBitmap = null,
                                        aspectRatio = veoAspectRatio,
                                        resolution = veoResolution
                                    )
                                    veoResultText = result
                                    isGeneratingVeo = false
                                }
                            },
                            enabled = !isGeneratingVeo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_veo_video_button")
                        )
                    }
                }

                // Veo Video Result Viewfinder Card with Rainbow Border
                if (veoResultText != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCard)
                                .animatedRainbowBorder(strokeWidth = 1.5.dp, cornerRadius = 12.dp, glow = true)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VEO 3 GENERATION READY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                                }
                                Text(
                                    text = "$veoAspectRatio • $veoResolution",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonOrange
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Simulated Video Canvas Player with exact aspect ratio
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (veoAspectRatio == "9:16") 280.dp else 180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBlack)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFF261507), Color(0xFF0F0C0A))
                                        )
                                    )
                                    // Animated beam
                                    drawCircle(
                                        color = Color(0x33FF6A00),
                                        radius = size.width * 0.35f,
                                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(NeonOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = ObsidianBlack, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Veo 3 Rendered Take • $veoAspectRatio",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmIvory
                                    )
                                    Text(
                                        text = "60fps Latent Neural Flow",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MutedIvory
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = veoResultText ?: "",
                                fontSize = 11.sp,
                                color = WarmIvory,
                                lineHeight = 15.sp
                            )

                            if (currentProject != null && onProjectChange != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val clipName = "Veo 3 Master ($veoResolution $veoAspectRatio)"
                                        val updated = currentProject.copy(
                                            sourceVideoName = clipName,
                                            sourceVideoResolution = veoResolution
                                        )
                                        onProjectChange(updated)
                                        workspaceRepo?.updateSharedProject(updated)
                                        configRepo?.recordSynthesizedVeoClip(clipName)
                                        Toast.makeText(context, "Applied '$clipName' to Studio & Room DB!", Toast.LENGTH_SHORT).show()
                                        onNavigateToStudio?.invoke()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GeminiCyan,
                                        contentColor = ObsidianBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("apply_veo_to_studio_button")
                                ) {
                                    Text(
                                        text = "SET AS STUDIO SOURCE VIDEO & SYNC ROOM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==================== NANO BANANA 2.1 IMAGE LAB ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CREATE & EDIT IMAGES",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Model: gemini-nano-banana-2.1",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ChampagneGold
                                )
                            }

                            // Mode Switch (Create vs Edit)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CharcoalElevated)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (nanoMode == "create") ChampagneGold else Color.Transparent)
                                        .clickable { nanoMode = "create" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Create",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (nanoMode == "create") ObsidianBlack else WarmIvory
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (nanoMode == "edit") ChampagneGold else Color.Transparent)
                                        .clickable { nanoMode = "edit" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Edit Photo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (nanoMode == "edit") ObsidianBlack else WarmIvory
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // If Edit mode, select base image
                        if (nanoMode == "edit") {
                            Text(
                                text = "BASE PHOTO TO EDIT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedIvory
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sampleStills) { still ->
                                    val isSelected = selectedBaseImageTitle == still
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF332A04) else CharcoalElevated)
                                            .border(1.dp, if (isSelected) ChampagneGold else BorderSubtle, RoundedCornerShape(6.dp))
                                            .clickable { selectedBaseImageTitle = still }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = still.substringBefore(" ("),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ChampagneGold else WarmIvory
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Nano Prompt
                        Text(
                            text = if (nanoMode == "edit") "EDITING INSTRUCTION PROMPT" else "IMAGE CREATION TEXT PROMPT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nanoPrompt,
                            onValueChange = { nanoPrompt = it },
                            placeholder = { Text("E.g. Change lighting to dramatic tungsten rim light, add cybernetic visor...", color = MutedIvory, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nano_prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Aspect Ratio Affordances (1:1, 16:9, 9:16, 4:3, 3:4, 21:9)
                        Text(
                            text = "IMAGE ASPECT RATIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("1:1", "16:9", "9:16", "4:3", "3:4", "2:3", "3:2", "21:9")) { ratio ->
                                val isSel = nanoAspectRatio == ratio
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ChampagneGold else CharcoalElevated)
                                        .clickable { nanoAspectRatio = ratio }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = ratio,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) ObsidianBlack else WarmIvory
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Resolution Affordances (1K, 2K, 4K)
                        Text(
                            text = "RESOLUTION SIZE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1K", "2K", "4K").forEach { size ->
                                val isSel = nanoImageSize == size
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ChampagneGold else CharcoalElevated)
                                        .clickable { nanoImageSize = size }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = size,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) ObsidianBlack else WarmIvory
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Studio Thinking Loader for Nano Banana
                        if (isGeneratingNano) {
                            AiStudioThinkingLoader(
                                stageText = "Gemini Nano Banana 2.1: Synthesizing photoreal $nanoImageSize asset at $nanoAspectRatio...",
                                progressFraction = null,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Action Button with AI Studio Rainbow Border
                        AiStudioRainbowButton(
                            text = if (isGeneratingNano) "SYNTHESIZING WITH NANO 2.1..." else if (nanoMode == "edit") "EDIT IMAGE (NANO BANANA 2.1)" else "CREATE IMAGE (NANO BANANA 2.1)",
                            onClick = {
                                isGeneratingNano = true
                                nanoResultText = null
                                scope.launch {
                                    val result = geminiService.createOrEditImageWithNanoBanana(
                                        prompt = if (nanoMode == "edit") "Edit photo [$selectedBaseImageTitle]: $nanoPrompt" else nanoPrompt,
                                        inputBitmap = null,
                                        aspectRatio = nanoAspectRatio,
                                        imageSize = nanoImageSize
                                    )
                                    nanoResultText = result
                                    isGeneratingNano = false
                                }
                            },
                            enabled = !isGeneratingNano,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("synthesize_nano_image_button")
                        )
                    }
                }

                // Nano Image Result Card with Animated Rainbow Border
                if (nanoResultText != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCard)
                                .animatedRainbowBorder(strokeWidth = 1.5.dp, cornerRadius = 12.dp, glow = true)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SYNTHESIS SUCCESSFUL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                                }
                                Text(
                                    text = "$nanoAspectRatio • $nanoImageSize",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ChampagneGold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBlack)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF3B2A06), Color(0xFF0F0E0B)),
                                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                                            radius = size.width * 0.6f
                                        )
                                    )
                                    drawCircle(
                                        color = ChampagneGold,
                                        radius = 35.dp.toPx(),
                                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Synthesized Character Portrait",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmIvory
                                    )
                                    Text(
                                        text = "gemini-nano-banana-2.1 • $nanoImageSize",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MutedIvory
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = nanoResultText ?: "",
                                fontSize = 11.sp,
                                color = WarmIvory,
                                lineHeight = 15.sp
                            )

                            if (currentProject != null && onProjectChange != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val refName = "${selectedBaseImageTitle.substringBefore(" (")} (Nano 2.1 $nanoImageSize)"
                                        val updated = currentProject.copy(
                                            primaryReferenceName = refName
                                        )
                                        onProjectChange(updated)
                                        workspaceRepo?.updateSharedProject(updated)
                                        configRepo?.recordSynthesizedNanoPortrait(refName)
                                        Toast.makeText(context, "Set '$refName' as Studio Character Reference!", Toast.LENGTH_SHORT).show()
                                        onNavigateToStudio?.invoke()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ChampagneGold,
                                        contentColor = ObsidianBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("apply_nano_to_studio_button")
                                ) {
                                    Text(
                                        text = "USE AS STUDIO CHARACTER REFERENCE & SYNC",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
