package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import android.speech.tts.TextToSpeech
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterCastVault
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.CharacterVaultDialog
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.components.rememberAnimatedRainbowBrush
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DimIvory
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioRainbowList
import com.example.ui.theme.WarmIvory
import androidx.compose.runtime.collectAsState
import com.example.data.model.RecastProject
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.SystemIntegrationHubBanner
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sin

enum class MeetingPlatform(val displayName: String, val appTag: String) {
    ZOOM("Zoom Meetings", "Virtual Camera"),
    FACETIME("Apple FaceTime", "Continuity Feed"),
    GOOGLE_MEET("Google Meet", "WebRTC Direct"),
    TEAMS("Microsoft Teams", "UVC Driver"),
    OBS_STUDIO("OBS / Twitch", "Virtual Output")
}

@Composable
fun LiveSessionScreen(
    characterName: String = "Elena (Sci-Fi Detective)",
    currentProject: RecastProject? = null,
    onProjectChange: ((RecastProject) -> Unit)? = null,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null
) {
    val context = LocalContext.current
    var isLiveStreaming by remember { mutableStateOf(true) }
    var selectedPlatform by remember { mutableStateOf(MeetingPlatform.ZOOM) }
    var activeCharacter by remember(characterName) { mutableStateOf(characterName) }
    var liveSwapStrength by remember { mutableFloatStateOf((currentProject?.characterStrength ?: 90L).toFloat()) }
    var liveResolution by remember(currentProject?.resolution) { mutableStateOf(currentProject?.resolution ?: "1080p") }
    var liveFrameRate by remember(currentProject?.frameRate) { mutableLongStateOf(currentProject?.frameRate ?: 60L) }
    var liveCompression by remember(currentProject?.compressionLevel) { mutableLongStateOf(currentProject?.compressionLevel ?: 18L) }
    var isTwoWayCallBridge by remember { mutableStateOf(true) }
    var isMicMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isVoiceModulationEnabled by remember { mutableStateOf(true) }
    var voicePitchShift by remember { mutableFloatStateOf(1.0f) }
    var expressionMatchLevel by remember { mutableFloatStateOf(95f) }
    var showDeviceLinkDialog by remember { mutableStateOf(false) }
    var showMockMeetingDialog by remember { mutableStateOf(false) }
    var showCharacterVaultModal by remember { mutableStateOf(false) }
    var connectionMethod by remember { mutableStateOf("Wi-Fi WebRTC (Local Bridge)") }
    var frameTick by remember { mutableLongStateOf(0L) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                ttsEngine = engine
            }
        }
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    // Live frame ticker for animated face mesh and audio waveform
    LaunchedEffect(isLiveStreaming) {
        while (isLiveStreaming) {
            delay(33) // ~30 fps
            frameTick++
        }
    }

    val sampleCharacters = listOf(
        "Elena (Sci-Fi Detective)",
        "Marcus (Neo-Noir Executive)",
        "Aria (Cyborg Operative)",
        "Kael (Cyberpunk Runner)",
        "Dr. Vance (Futuristic Scientist)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Studio Live Meeting Header
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
                                text = "LIVE MEETING RECAST",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = WarmIvory
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRO CAM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Real-Time Zoom, FaceTime & Teams Virtual Webcam Bridge",
                            fontSize = 11.sp,
                            color = MutedIvory
                        )
                    }
                }

                // Live Stream status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isLiveStreaming) Color(0xFF132A1B) else CharcoalElevated)
                        .border(
                            1.dp,
                            if (isLiveStreaming) GeminiGreen else BorderSubtle,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveStreaming) GeminiGreen else Color(0xFFFF453A))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveStreaming) "LIVE • 60 FPS" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLiveStreaming) GeminiGreen else WarmIvory
                        )
                    }
                }
            }
        }

        if (currentProject != null && configRepo != null && workspaceRepo != null) {
            item {
                val connCfg by configRepo.connectionConfig.collectAsState()
                SystemIntegrationHubBanner(
                    moduleName = "Live Cam Bridge",
                    project = currentProject.copy(
                        primaryReferenceName = activeCharacter,
                        characterStrength = liveSwapStrength.toLong()
                    ),
                    configRepo = configRepo,
                    workspaceRepo = workspaceRepo,
                    onOpenConfigCenter = onOpenConfigCenter,
                    primaryBridgeLabel = "Sync Character to Studio",
                    onPrimaryBridgeClick = {
                        val updated = currentProject.copy(
                            primaryReferenceName = activeCharacter,
                            characterStrength = liveSwapStrength.toLong()
                        )
                        onProjectChange?.invoke(updated)
                        workspaceRepo.updateSharedProject(updated)
                        configRepo.recordSystemEvent(
                            "Live Cam Bridge",
                            "Synced Live Cam character '$activeCharacter' (${liveSwapStrength.toInt()}%) to Studio"
                        )
                        Toast.makeText(context, "Synced '$activeCharacter' (${liveSwapStrength.toInt()}%) to Studio & Room DB!", Toast.LENGTH_SHORT).show()
                    },
                    secondaryBridgeLabel = "Mesh (${connCfg.lastPingMs}ms)",
                    onSecondaryBridgeClick = {
                        onOpenConfigCenter?.invoke(ControlCenterTab.CONNECTION)
                    }
                )
            }
        }

        // Section 1: Meeting Platform Selector (Zoom, FaceTime, Google Meet, Teams, OBS)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET MEETING PLATFORM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedPlatform.appTag,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GeminiCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MeetingPlatform.values()) { platform ->
                        val isSelected = selectedPlatform == platform
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E2840) else CharcoalElevated)
                                .border(
                                    1.2.dp,
                                    if (isSelected) GeminiCyan else BorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedPlatform = platform
                                    Toast.makeText(context, "Configured for ${platform.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = platform.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GeminiCyan else WarmIvory
                                )
                                Text(
                                    text = platform.appTag,
                                    fontSize = 9.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Interactive Real-Time Viewfinder with Rainbow Border & Facial Mesh
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .animatedRainbowBorder(
                        strokeWidth = 2.dp,
                        cornerRadius = 14.dp,
                        glow = isLiveStreaming
                    )
            ) {
                if (isCameraOff) {
                    // Camera Privacy Off State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VideocamOff, contentDescription = null, tint = MutedIvory, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera Feed Muted", fontSize = 13.sp, color = MutedIvory)
                            Text("Tap video icon below to resume live recast", fontSize = 11.sp, color = DimIvory)
                        }
                    }
                } else {
                    // Live Viewfinder Canvas: Mesh, Reticle, Dynamic Lighting & Face Contours
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Dynamic atmospheric background gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF131726), Color(0xFF090A10))
                            )
                        )

                        // Subtle grid lines simulating studio NPU telemetry
                        val gridAlpha = 0.12f
                        for (i in 1..5) {
                            val y = h * (i / 6f)
                            drawLine(
                                color = GeminiCyan.copy(alpha = gridAlpha),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }
                        for (i in 1..5) {
                            val x = w * (i / 6f)
                            drawLine(
                                color = GeminiCyan.copy(alpha = gridAlpha),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1f
                            )
                        }

                        // Real-time animated target tracking face mesh
                        val sway = sin(frameTick * 0.08f) * 16f
                        val headX = w * 0.5f + sway
                        val headY = h * 0.42f

                        // Outer glowing aura around the recast character
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GeminiViolet.copy(alpha = 0.28f),
                                    GeminiCyan.copy(alpha = 0.14f),
                                    Color.Transparent
                                ),
                                center = Offset(headX, headY),
                                radius = 100.dp.toPx()
                            ),
                            radius = 100.dp.toPx(),
                            center = Offset(headX, headY)
                        )

                        // Facial silhouette contour
                        val headR = 56.dp.toPx()
                        drawCircle(
                            brush = Brush.sweepGradient(
                                StudioRainbowList,
                                center = Offset(headX, headY)
                            ),
                            radius = headR,
                            center = Offset(headX, headY),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 68-Point Landmark Mesh highlights
                        val eyeOffset = 26f
                        val eyeY = headY - 14f
                        // Eyes
                        drawCircle(color = GeminiCyan, radius = 5.dp.toPx(), center = Offset(headX - eyeOffset, eyeY))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(headX - eyeOffset, eyeY))
                        drawCircle(color = GeminiCyan, radius = 5.dp.toPx(), center = Offset(headX + eyeOffset, eyeY))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(headX + eyeOffset, eyeY))

                        // Eyebrow vectors
                        drawLine(GeminiViolet, Offset(headX - 38f, eyeY - 12f), Offset(headX - 14f, eyeY - 16f), 2.5f)
                        drawLine(GeminiViolet, Offset(headX + 14f, eyeY - 16f), Offset(headX + 38f, eyeY - 12f), 2.5f)

                        // Nose bridge & tip
                        drawLine(GeminiAmber, Offset(headX, eyeY), Offset(headX, headY + 8f), 2f)
                        drawCircle(color = GeminiAmber, radius = 4.dp.toPx(), center = Offset(headX, headY + 8f))

                        // Dynamic animated talking mouth (lip-sync movement)
                        val mouthOpen = (sin(frameTick * 0.2f) * 6f + 8f).coerceAtLeast(3f)
                        val mouthY = headY + 34f
                        val mouthPath = Path().apply {
                            moveTo(headX - 22f, mouthY)
                            cubicTo(headX - 10f, mouthY - mouthOpen, headX + 10f, mouthY - mouthOpen, headX + 22f, mouthY)
                            cubicTo(headX + 10f, mouthY + mouthOpen, headX - 10f, mouthY + mouthOpen, headX - 22f, mouthY)
                            close()
                        }
                        drawPath(
                            path = mouthPath,
                            brush = Brush.linearGradient(listOf(GeminiPink, GeminiCoral))
                        )

                        // Optical tracking corner brackets
                        val cornerSize = 22.dp.toPx()
                        val pad = 14.dp.toPx()
                        val bracketColor = GeminiCyan
                        // Top-Left
                        drawLine(bracketColor, Offset(pad, pad), Offset(pad + cornerSize, pad), 3.5f)
                        drawLine(bracketColor, Offset(pad, pad), Offset(pad, pad + cornerSize), 3.5f)
                        // Top-Right
                        drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad - cornerSize, pad), 3.5f)
                        drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad, pad + cornerSize), 3.5f)
                        // Bottom-Left
                        drawLine(bracketColor, Offset(pad, h - pad), Offset(pad + cornerSize, h - pad), 3.5f)
                        drawLine(bracketColor, Offset(pad, h - pad), Offset(pad, h - pad - cornerSize), 3.5f)
                        // Bottom-Right
                        drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad - cornerSize, h - pad), 3.5f)
                        drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - cornerSize), 3.5f)
                    }

                    // Top Left Viewfinder HUD details
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(GeminiCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE AVATAR: $activeCharacter",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GeminiCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "NEURAL BLEND: ${liveSwapStrength.toInt()}% • EXPR: ${expressionMatchLevel.toInt()}%",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = WarmIvory
                        )
                    }

                    // Top Right Telemetry
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC0B0D14))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${liveResolution} @ ${liveFrameRate}fps • Comp ${liveCompression}% • ${if (isTwoWayCallBridge) "2-WAY" else "1-WAY"}",
                            fontSize = 10.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GeminiGreen
                        )
                    }

                    // Bottom meeting controls bar (Mute, Video, Share, Link)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xDD121522))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic toggle
                        IconButton(
                            onClick = {
                                isMicMuted = !isMicMuted
                                Toast.makeText(context, if (isMicMuted) "Microphone Muted" else "Microphone Active (Neural Voice)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = if (isMicMuted) Color(0xFFFF453A) else GeminiGreen
                            )
                        }

                        // Video toggle
                        IconButton(
                            onClick = { isCameraOff = !isCameraOff },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = "Toggle Video",
                                tint = if (isCameraOff) Color(0xFFFF453A) else GeminiCyan
                            )
                        }

                        // Test Virtual Meeting Dialog Trigger
                        Button(
                            onClick = { showMockMeetingDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiViolet,
                                contentColor = WarmIvory
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test in Zoom", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Link to PC / Mac
                        IconButton(
                            onClick = { showDeviceLinkDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = "Link to PC/Mac",
                                tint = GeminiAmber
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Phone & Computer Linking Hub (The Killer Feature)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "COMPUTER & PHONE BRIDGE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = "Use phone camera as real-time Mac / PC Zoom webcam",
                                fontSize = 10.sp,
                                color = MutedIvory
                            )
                        }
                    }

                    AiStudioBadge(text = "DRIVER V2.4", iconSparkle = false)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connection status card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalElevated)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connected: MacBook Pro (M-Series)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                        }
                        Text("Stream: 1080p60 WebRTC • Latency: 22ms", fontSize = 10.sp, color = MutedIvory)
                    }

                    Button(
                        onClick = { showDeviceLinkDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2840),
                            contentColor = GeminiCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Pair / Link", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 4: Live Character Switcher
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
                    Text(
                        text = "SELECT LIVE MEETING RECAST CHARACTER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory,
                        letterSpacing = 1.sp
                    )

                    // 11Labs Vault Button with AI Studio Sparkle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { showCharacterVaultModal = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GeminiSparkle(size = 12.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("11Labs Vault", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Pre-generated 11Labs stock characters
                    items(CharacterCastVault.stockCharacters) { stockChar ->
                        val isSelected = activeCharacter.startsWith(stockChar.name, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .width(190.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E2844) else CharcoalElevated)
                                .border(
                                    1.2.dp,
                                    if (isSelected) GeminiCyan else BorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    activeCharacter = stockChar.name
                                    liveSwapStrength = stockChar.recommendedStrength.toFloat()
                                    Toast.makeText(context, "Live avatar swapped to ${stockChar.name} (${stockChar.archetype})", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp)
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
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(stockChar.avatarColorA, stockChar.avatarColorB)))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stockChar.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GeminiCyan else WarmIvory
                                        )
                                    }

                                    Text(
                                        text = stockChar.category.take(8),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stockChar.tagline,
                                    fontSize = 9.sp,
                                    color = MutedIvory,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SettingsVoice, contentDescription = null, tint = GeminiViolet, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stockChar.voiceName.take(18),
                                        fontSize = 8.sp,
                                        color = GeminiViolet
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Real-Time Neural Controls (Blend, Lip-Sync, Voice Modulation)
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
                    Text(
                        text = "TWO-WAY LIVE STREAM & FIDELITY CONTROLS",
                        fontSize = 10.sp,
                        fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MutedIvory,
                        letterSpacing = 0.8.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTwoWayCallBridge) Color(0xFF132A1B) else CharcoalElevated)
                            .border(1.dp, if (isTwoWayCallBridge) GeminiGreen else BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { isTwoWayCallBridge = !isTwoWayCallBridge }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isTwoWayCallBridge) "⇄ 2-Way Duplex Call Active" else "→ Outbound Cam Only",
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isTwoWayCallBridge) GeminiGreen else WarmIvory
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Adaptive Video Fidelity Sliders (Resolution, Frame Rate, Compression)
                com.example.ui.components.VideoFidelityCompactStrip(
                    resolution = liveResolution,
                    frameRate = liveFrameRate,
                    compressionLevel = liveCompression,
                    onResolutionChange = { res ->
                        liveResolution = res
                        if (currentProject != null && onProjectChange != null) {
                            val updated = currentProject.copy(resolution = res, sourceVideoResolution = res)
                            onProjectChange(updated)
                            workspaceRepo?.updateSharedProject(updated)
                        }
                    },
                    onFrameRateChange = { fps ->
                        liveFrameRate = fps
                        if (currentProject != null && onProjectChange != null) {
                            val updated = currentProject.copy(frameRate = fps)
                            onProjectChange(updated)
                            workspaceRepo?.updateSharedProject(updated)
                        }
                    },
                    onCompressionChange = { comp ->
                        liveCompression = comp
                        if (currentProject != null && onProjectChange != null) {
                            val updated = currentProject.copy(compressionLevel = comp)
                            onProjectChange(updated)
                            workspaceRepo?.updateSharedProject(updated)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Swap Strength
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Identity Replacement Strength", fontSize = 11.sp, color = WarmIvory)
                    Text("${liveSwapStrength.toInt()}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = GeminiCyan)
                }
                Slider(
                    value = liveSwapStrength,
                    onValueChange = { liveSwapStrength = it },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(thumbColor = GeminiCyan, activeTrackColor = GeminiCyan),
                    modifier = Modifier.fillMaxWidth()
                )

                // Expression Match
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Expression & Lip-Sync Match", fontSize = 11.sp, color = WarmIvory)
                    Text("${expressionMatchLevel.toInt()}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = GeminiViolet)
                }
                Slider(
                    value = expressionMatchLevel,
                    onValueChange = { expressionMatchLevel = it },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(thumbColor = GeminiViolet, activeTrackColor = GeminiViolet),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Voice Modulation Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsVoice, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Live Mic Voice Morphing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                            Text("Morphs voice to character timbre live in Zoom call", fontSize = 10.sp, color = MutedIvory)
                        }
                    }

                    Switch(
                        checked = isVoiceModulationEnabled,
                        onCheckedChange = { isVoiceModulationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = GeminiAmber
                        )
                    )
                }

                if (isVoiceModulationEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val phrase = "Live audio test: This is $activeCharacter speaking in real time through the FrameShift neural virtual camera bridge."
                            val pitch = when {
                                activeCharacter.contains("Adam", true) -> 0.76f
                                activeCharacter.contains("Elena", true) -> 1.08f
                                activeCharacter.contains("Freya", true) -> 1.18f
                                activeCharacter.contains("Rachel", true) -> 1.02f
                                activeCharacter.contains("Marcus", true) -> 0.82f
                                else -> 0.95f
                            }
                            ttsEngine?.setPitch(pitch)
                            ttsEngine?.setSpeechRate(1.0f)
                            ttsEngine?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "live_sample")
                            Toast.makeText(context, "Testing $activeCharacter voice timbre...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2840),
                            contentColor = GeminiAmber
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("test_live_voice_timbre_button")
                    ) {
                        Icon(Icons.Default.SettingsVoice, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Live Character Voice Timbre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 6: Action Start / Stop Stream Button with Rainbow Border
        item {
            AiStudioRainbowButton(
                text = if (isLiveStreaming) "BROADCASTING TO ${selectedPlatform.displayName.uppercase()}" else "START LIVE RECAST STREAM",
                onClick = {
                    isLiveStreaming = !isLiveStreaming
                    Toast.makeText(
                        context,
                        if (isLiveStreaming) "Live stream active for ${selectedPlatform.displayName}" else "Live stream paused",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("toggle_live_stream_button")
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal: Device Linking Dialog (Phone to Computer Pairing)
    if (showDeviceLinkDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiSparkle(size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link Phone to Computer Virtual Cam", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FrameShift allows your phone's neural processor to run character recast and transmit the 1080p60 stream directly into Zoom, FaceTime, or Meet on your Mac / PC.",
                        fontSize = 11.sp,
                        color = MutedIvory,
                        lineHeight = 16.sp
                    )

                    // Pairing Token Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, GeminiCyan, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("PAIRING TOKEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedIvory)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FS-RECAST-8842",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = GeminiCyan
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Pairing Token", "FS-RECAST-8842"))
                                        Toast.makeText(context, "Pairing token copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WarmIvory, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Connection Method Selector
                    Text("CONNECTION PROTOCOL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedIvory)
                    listOf("Wi-Fi WebRTC (Local Bridge - 24ms)", "USB-C Direct Tether (Zero-Buffer - 8ms)").forEach { method ->
                        val isSel = connectionMethod == method
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF1E2840) else CharcoalElevated)
                                .clickable { connectionMethod = method }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = method,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) GeminiCyan else WarmIvory
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeviceLinkDialog = false
                    Toast.makeText(context, "Virtual Camera driver linked to $connectionMethod", Toast.LENGTH_LONG).show()
                }) {
                    Text("Connect Virtual Camera", color = GeminiCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceLinkDialog = false }) {
                    Text("Close", color = MutedIvory)
                }
            },
            containerColor = CharcoalCard
        )
    }

    // Modal: Mock Interactive Meeting Simulation (Zoom / FaceTime style)
    if (showMockMeetingDialog) {
        AlertDialog(
            onDismissRequest = { showMockMeetingDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF3B30))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedPlatform.displayName} Simulation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                    }

                    Text("REC 04:12", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFF453A))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Meeting Gallery Grid: 2 Participants (You + Colleague)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Participant 1: YOU (Recast Avatar)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151928))
                                .border(1.5.dp, GeminiCyan, RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                drawCircle(
                                    color = GeminiViolet.copy(alpha = 0.35f),
                                    radius = 35.dp.toPx(),
                                    center = Offset(w / 2f, h * 0.45f)
                                )
                                drawCircle(
                                    color = GeminiCyan,
                                    radius = 28.dp.toPx(),
                                    center = Offset(w / 2f, h * 0.45f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Eye landmarks
                                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(w / 2f - 10f, h * 0.42f))
                                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(w / 2f + 10f, h * 0.42f))
                            }
                            Text(
                                text = "You ($activeCharacter)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmIvory,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                                    .background(Color(0xBB000000), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        // Participant 2: Colleague / Director
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C1D24))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                drawCircle(
                                    color = Color(0xFF333544),
                                    radius = 28.dp.toPx(),
                                    center = Offset(w / 2f, h * 0.45f)
                                )
                            }
                            Text(
                                text = "Sarah J. (Creative Director)",
                                fontSize = 9.sp,
                                color = MutedIvory,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                                    .background(Color(0xBB000000), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Colleagues see your live character recast in real time with 24ms latency. Voice modulation is mixed onto the microphone feed.",
                        fontSize = 11.sp,
                        color = MutedIvory,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMockMeetingDialog = false }) {
                    Text("Return to Studio", color = GeminiCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CharcoalCard
        )
    }

    // ElevenLabs-Tier Character Cast Vault Dialog
    CharacterVaultDialog(
        isOpen = showCharacterVaultModal,
        onDismiss = { showCharacterVaultModal = false },
        currentSelectedName = activeCharacter,
        onSelectCharacter = { char ->
            activeCharacter = char.name
            liveSwapStrength = char.recommendedStrength.toFloat()
            showCharacterVaultModal = false
            Toast.makeText(context, "Swapped live avatar to ${char.name}", Toast.LENGTH_SHORT).show()
        }
    )
}
