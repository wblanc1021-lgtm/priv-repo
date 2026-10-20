package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.GeminiService
import com.example.data.model.CharacterCastVault
import com.example.data.model.PreGeneratedCharacter
import com.example.data.model.RecastProject
import com.example.data.model.RecastVariant
import com.example.service.recast.RecastEngine
import com.example.service.recast.RecastProgress
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.AiStudioThinkingLoader
import com.example.ui.components.AudioTimelinePanel
import com.example.ui.components.CharacterVaultDialog
import com.example.ui.components.FidelityControlsPanel
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.LiveTransformationPreviewCard
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DimIvory
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import androidx.compose.runtime.collectAsState
import com.example.data.repository.DevicePlatform
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.launch

@Composable
fun StudioScreen(
    currentProject: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    onGenerationComplete: (RecastProject, List<RecastVariant>) -> Unit,
    geminiService: GeminiService,
    recastEngine: RecastEngine,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    onNavigateToLiveCam: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var currentPipelineStage by remember { mutableStateOf("") }
    var pipelineFraction by remember { mutableFloatStateOf(0f) }
    var geminiAnalysisResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showConceptDialog by remember { mutableStateOf(false) }
    var showCharacterVaultModal by remember { mutableStateOf(false) }
    var customReferenceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Android Zero-Permission Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "User_Footage.mp4"
            onProjectChange(
                currentProject.copy(
                    sourceVideoName = fileName,
                    sourceVideoDuration = 15L,
                    sourceVideoResolution = "1080p (Imported)"
                )
            )
            Toast.makeText(context, "Loaded footage: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Android Zero-Permission Image Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                customReferenceBitmap = bitmap
                val name = "Imported Portrait"
                onProjectChange(currentProject.copy(primaryReferenceName = name))
                Toast.makeText(context, "Loaded reference photo", Toast.LENGTH_SHORT).show()
                isAnalyzing = true
                scope.launch {
                    val analysis = geminiService.analyzeCharacterReference(
                        bitmap = bitmap,
                        prompt = "Analyze casting readiness and facial structure of this imported photo for character swap."
                    )
                    geminiAnalysisResult = analysis
                    isAnalyzing = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Live Camera Selfie Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            customReferenceBitmap = bitmap
            val name = "Live Selfie (Camera)"
            onProjectChange(currentProject.copy(primaryReferenceName = name))
            Toast.makeText(context, "Captured camera selfie!", Toast.LENGTH_SHORT).show()
            isAnalyzing = true
            scope.launch {
                val analysis = geminiService.analyzeCharacterReference(
                    bitmap = bitmap,
                    prompt = "Analyze this live selfie photo for casting fidelity, lighting vector alignment, and recasting realism."
                )
                geminiAnalysisResult = analysis
                isAnalyzing = false
            }
        }
    }

    val sampleClips = listOf(
        Triple("Cyberpunk Alley Run", 12L, "1080p"),
        Triple("Dialogue Close-up Shot", 8L, "1440p"),
        Triple("Studio Stage Tracking", 15L, "1080p"),
        Triple("Cinematic Golden Hour Walk", 20L, "1080p")
    )

    val sampleCharacters = listOf(
        Pair("Elena (Sci-Fi Detective)", "Front angle, sharp jawline, high edge clarity"),
        Pair("Marcus (Neo-Noir Protagonist)", "45-degree angle, rim lit, stubble texture"),
        Pair("Aria (Cyborg Operative)", "Studio headshot, neutral expression, 8K"),
        Pair("Kael (Cyberpunk Runner)", "Dynamic low-angle, street illumination")
    )

    val (estSeconds, estCredits) = recastEngine.estimateGenerationTimeAndCredits(currentProject)

    val triggerRecastGeneration: () -> Unit = {
        isProcessing = true
        scope.launch {
            recastEngine.executeRecastPipeline(currentProject).collect { progress ->
                when (progress) {
                    is RecastProgress.Step -> {
                        currentPipelineStage = progress.stageName
                        pipelineFraction = progress.progressFraction
                    }
                    is RecastProgress.Completed -> {
                        isProcessing = false
                        onGenerationComplete(progress.project, progress.variants)
                    }
                    is RecastProgress.Failed -> {
                        isProcessing = false
                        currentPipelineStage = "Failed: ${progress.reason}"
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header with Two-Way AI Conversion Studio branding & smooth typography
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiSparkle(size = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TWO-WAY AI CONVERSION",
                                fontSize = 16.sp,
                                fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = WarmIvory
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STUDIO",
                                fontSize = 11.sp,
                                fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = GeminiCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Bidirectional Video ⇄ Identity Recast • Inverse Photo Drive • 3D Rig Extract",
                            fontSize = 11.sp,
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = MutedIvory
                        )
                    }
                }

                AiStudioBadge(text = "2-WAY ENGINE")
            }
        }

        // Unified System Integration Hub Banner & Cross-Module Bridges
        if (configRepo != null && workspaceRepo != null) {
            item {
                val systemBus by configRepo.systemBus.collectAsState()
                SystemIntegrationHubBanner(
                    moduleName = "Studio Pipeline",
                    project = currentProject,
                    configRepo = configRepo,
                    workspaceRepo = workspaceRepo,
                    onOpenConfigCenter = onOpenConfigCenter,
                    primaryBridgeLabel = "Pull Veo & Nano",
                    onPrimaryBridgeClick = {
                        val updated = currentProject.copy(
                            sourceVideoName = systemBus.lastSynthesizedVeoClip,
                            primaryReferenceName = systemBus.lastSynthesizedNanoPortrait
                        )
                        onProjectChange(updated)
                        workspaceRepo.updateSharedProject(updated)
                        configRepo.recordSystemEvent(
                            "Studio Pipeline",
                            "Imported Veo Clip '${systemBus.lastSynthesizedVeoClip}' & Nano Portrait '${systemBus.lastSynthesizedNanoPortrait}'"
                        )
                        Toast.makeText(
                            context,
                            "Pulled latest Veo Clip & Nano Portrait into Studio!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    secondaryBridgeLabel = "Pull Prompt Lab",
                    onSecondaryBridgeClick = {
                        geminiAnalysisResult = "Imported Optimized Prompt from Prompt Lab:\n\"${systemBus.lastOptimizedPrompt}\"\n\nRecommended Settings Applied: Strength 90%, High Preset."
                        val updated = currentProject.copy(characterStrength = 90L, qualityPreset = "high")
                        onProjectChange(updated)
                        workspaceRepo.updateSharedProject(updated)
                        configRepo.recordSystemEvent("Studio Pipeline", "Applied Prompt Lab optimization to '${updated.title}'")
                        Toast.makeText(context, "Applied Prompt Lab optimization!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Section 1: Source Video Selection
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. SOURCE VIDEO FOOTAGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                    }

                    // Video Analysis via Gemini 4 Argon
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalElevated)
                            .clickable {
                                isAnalyzing = true
                                scope.launch {
                                    val result = geminiService.analyzeVideoContent(
                                        "Footage: ${currentProject.sourceVideoName}, Duration: ${currentProject.sourceVideoDuration}s, Resolution: ${currentProject.sourceVideoResolution}"
                                    )
                                    geminiAnalysisResult = result
                                    isAnalyzing = false
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze with Argon", fontSize = 10.sp, color = WarmIvory)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Source video picker options
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Custom user video import via Android Photo Picker
                    item {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF141F30))
                                .border(1.2.dp, GeminiCyan, RoundedCornerShape(8.dp))
                                .clickable {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Import Video",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Device Camera / Gallery",
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }

                    items(sampleClips) { (clipTitle, dur, res) ->
                        val isSelected = currentProject.sourceVideoName == clipTitle
                        Box(
                            modifier = Modifier
                                .width(170.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF331604) else CharcoalElevated)
                                .border(1.dp, if (isSelected) NeonOrange else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    onProjectChange(
                                        currentProject.copy(
                                            sourceVideoName = clipTitle,
                                            sourceVideoDuration = dur,
                                            sourceVideoResolution = res
                                        )
                                    )
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = clipTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonOrange else WarmIvory
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$dur sec • $res • 60fps",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MutedIvory
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Character Reference Photo & Angle Consistency
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. CHARACTER REFERENCE ASSET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Pre-Generated 11Labs Vault Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CharcoalElevated)
                                .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable {
                                    showCharacterVaultModal = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GeminiSparkle(size = 12.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("11Labs Vault", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiCyan)
                            }
                        }

                        // Concept generation via Gemini Nano Banana 2.1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CharcoalElevated)
                                .clickable {
                                    isAnalyzing = true
                                    scope.launch {
                                        val result = geminiService.createOrEditImageWithNanoBanana(
                                            prompt = "Cinematic character portrait for ${currentProject.title}, studio rim lighting, 8k photorealistic",
                                            aspectRatio = "1:1",
                                            imageSize = "2K"
                                        )
                                        geminiAnalysisResult = result
                                        isAnalyzing = false
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create with Nano", fontSize = 10.sp, color = ChampagneGold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live Selfie with Device Camera
                    item {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF281C10))
                                .border(1.2.dp, GeminiAmber, RoundedCornerShape(8.dp))
                                .clickable { cameraLauncher.launch(null) }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Snap Selfie",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Live Camera Capture",
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }

                    // Import Photo from Gallery
                    item {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF20162A))
                                .border(1.2.dp, GeminiViolet, RoundedCornerShape(8.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = GeminiViolet, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Import Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiViolet
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Device Photo Picker",
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }

                    items(sampleCharacters) { (charName, desc) ->
                        val isSelected = currentProject.primaryReferenceName == charName
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF331604) else CharcoalElevated)
                                .border(1.dp, if (isSelected) NeonOrange else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    onProjectChange(
                                        currentProject.copy(
                                            primaryReferenceName = charName
                                        )
                                    )
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = charName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonOrange else WarmIvory
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }
                }

                // Visual Thumbnail if User Took Selfie or Imported Photo
                if (customReferenceBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131726))
                            .animatedRainbowBorder(strokeWidth = 1.2.dp, cornerRadius = 8.dp, glow = true)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = customReferenceBitmap!!.asImageBitmap(),
                            contentDescription = "Active Reference Portrait",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE CAST: ${currentProject.primaryReferenceName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiCyan
                            )
                            Text(
                                text = "Photo mesh loaded • Sub-millimeter temporal fidelity enabled",
                                fontSize = 10.sp,
                                color = MutedIvory
                            )
                        }
                    }
                }

                // Reference Quality Analysis Trigger via gemini-4-argon
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalElevated)
                        .clickable {
                            isAnalyzing = true
                            scope.launch {
                                val result = geminiService.analyzeCharacterReference(
                                    bitmap = customReferenceBitmap,
                                    prompt = "Analyze casting readiness for character: ${currentProject.primaryReferenceName} with swap strength ${currentProject.characterStrength}%"
                                )
                                geminiAnalysisResult = result
                                isAnalyzing = false
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reference Quality & Lighting Check (Argon)",
                            fontSize = 11.sp,
                            color = WarmIvory
                        )
                    }
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NeonOrange, strokeWidth = 2.dp)
                    } else {
                        Text("Run Check", fontSize = 10.sp, color = NeonOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2.5: Interactive Live Transformation Preview (Before/After Slider, Polarity Flip & Fidelity Telemetry)
        item {
            LiveTransformationPreviewCard(
                sourceClipTitle = currentProject.sourceVideoName,
                activeCharacterName = currentProject.primaryReferenceName,
                customReferenceBitmap = customReferenceBitmap,
                characterStrength = currentProject.characterStrength,
                onStrengthChange = { strength ->
                    onProjectChange(currentProject.copy(characterStrength = strength))
                },
                qualityPreset = currentProject.qualityPreset,
                onQualityPresetChange = { preset ->
                    onProjectChange(currentProject.copy(qualityPreset = preset))
                },
                conversionDirection = currentProject.conversionDirection,
                onToggleConversionPolarity = {
                    val nextMode = when (currentProject.conversionDirection) {
                        "forward_recast" -> "inverse_drive"
                        "inverse_drive" -> "dual_swap"
                        "dual_swap" -> "reverse_extract"
                        else -> "forward_recast"
                    }
                    val modeEnum = com.example.data.model.TwoWayConversionMode.fromId(nextMode)
                    onProjectChange(currentProject.copy(conversionDirection = nextMode))
                    Toast.makeText(
                        context,
                        "Switched Polarity: ${modeEnum.title} (${modeEnum.directionBadge})",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                videoResolution = currentProject.resolution,
                videoFrameRate = currentProject.frameRate,
                videoCompressionLevel = currentProject.compressionLevel,
                onVideoQualityParametersChange = { newRes, newFps, newComp, newPreset ->
                    onProjectChange(
                        currentProject.copy(
                            resolution = newRes,
                            frameRate = newFps,
                            compressionLevel = newComp,
                            qualityPreset = newPreset
                        )
                    )
                },
                onUploadPhotoClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCameraClick = {
                    cameraLauncher.launch(null)
                },
                onSelectPreset = { presetName ->
                    customReferenceBitmap = null
                    onProjectChange(currentProject.copy(primaryReferenceName = presetName))
                },
                onGenerateRecast = {
                    triggerRecastGeneration()
                },
                onBeamToWorkspace = {
                    workspaceRepo?.sendHandoffBeam(
                        project = currentProject,
                        targetPlatform = DevicePlatform.COMPUTER
                    ) {
                        configRepo?.recordSystemEvent(
                            "Studio Live Preview",
                            "Beamed '${currentProject.title}' (${currentProject.activeConversionMode().shortTitle} • ${currentProject.resolution}@${currentProject.frameRate}fps) to Workstation"
                        )
                    }
                    Toast.makeText(context, "Beamed active two-way conversion session to Workspace & Room DB", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Gemini Analysis Thinking State
        if (isAnalyzing) {
            item {
                AiStudioThinkingLoader(
                    stageText = "Gemini 4 Argon (High Thinking): Analyzing scene composition, lighting vectors, and facial geometry...",
                    progressFraction = null
                )
            }
        }

        // Gemini Analysis Diagnostic Card (if available) with rainbow flare
        if (geminiAnalysisResult != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .animatedRainbowBorder(strokeWidth = 1.5.dp, cornerRadius = 12.dp, glow = true)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GeminiSparkle(size = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GEMINI 4 ARGON REPORT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = GeminiCyan
                                )
                            }
                            Text(
                                text = "DISMISS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiAmber,
                                modifier = Modifier.clickable { geminiAnalysisResult = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = geminiAnalysisResult ?: "",
                            fontSize = 11.sp,
                            color = WarmIvory,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Section 3: Swap Fidelity & Quality Controls
        item {
            FidelityControlsPanel(
                project = currentProject,
                onProjectChange = onProjectChange
            )
        }

        // Section 4: Deep Voice & Audio Timeline
        item {
            AudioTimelinePanel(
                project = currentProject,
                onProjectChange = onProjectChange
            )
        }

        // Section 5: Generation Summary & Action
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCard)
                    .animatedRainbowBorder(strokeWidth = 1.2.dp, cornerRadius = 12.dp, glow = false)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TWO-WAY PIPELINE: ${currentProject.activeConversionMode().title.uppercase()}",
                            fontSize = 10.sp,
                            fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentProject.resolution} @ ${currentProject.frameRate}fps • Comp ${currentProject.compressionLevel}% (~${String.format(java.util.Locale.US, "%.1f", currentProject.estimatedBitrateMbps())} Mbps)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            color = WarmIvory
                        )
                        Text(
                            text = "Est. ~$estSeconds sec • $estCredits Credits • ${currentProject.variantsCount} Take(s) • ${currentProject.recommendedCodecLabel()}",
                            fontSize = 10.sp,
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = MutedIvory
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, Color(0xFF34D399).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentProject.activeConversionMode().directionBadge,
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Adaptive Video Fidelity Slider right before triggering the AI Character Swap
                com.example.ui.components.AdaptiveVideoFidelitySlider(
                    resolution = currentProject.resolution,
                    frameRate = currentProject.frameRate,
                    compressionLevel = currentProject.compressionLevel,
                    qualityPreset = currentProject.qualityPreset,
                    characterStrength = currentProject.characterStrength,
                    onCharacterStrengthChange = { newStrength ->
                        onProjectChange(currentProject.copy(characterStrength = newStrength))
                    },
                    onQualityParametersChange = { newRes, newFps, newComp, newPreset ->
                        onProjectChange(
                            currentProject.copy(
                                resolution = newRes,
                                frameRate = newFps,
                                compressionLevel = newComp,
                                qualityPreset = newPreset
                            )
                        )
                    },
                    title = "PRE-FLIGHT VIDEO FIDELITY & SWAP QUALITY"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // High-fidelity AI Studio Thinking Loader during Recasting
                if (isProcessing) {
                    AiStudioThinkingLoader(
                        stageText = currentPipelineStage.ifBlank { "Synthesizing ${currentProject.activeConversionMode().title} at ${currentProject.resolution} ${currentProject.frameRate}fps..." },
                        progressFraction = pipelineFraction,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Action Button with clean studio glass border & shimmer movement
                AiStudioRainbowButton(
                    text = if (isProcessing) "CONVERTING (${currentProject.resolution} @ ${currentProject.frameRate}FPS)..." else "EXECUTE ${currentProject.activeConversionMode().shortTitle.uppercase()} (${currentProject.resolution})",
                    onClick = { triggerRecastGeneration() },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_recast_button")
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ElevenLabs-Tier Character Cast Vault Dialog
    CharacterVaultDialog(
        isOpen = showCharacterVaultModal,
        onDismiss = { showCharacterVaultModal = false },
        currentSelectedName = currentProject.primaryReferenceName,
        onSelectCharacter = { char ->
            onProjectChange(
                currentProject.copy(
                    primaryReferenceName = char.name,
                    characterStrength = char.recommendedStrength.toLong()
                )
            )
            showCharacterVaultModal = false
        }
    )
}
