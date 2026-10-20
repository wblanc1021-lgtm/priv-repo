package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioRainbowList
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay

enum class PreviewDisplayMode(val label: String) {
    SPLIT("Split Slider"),
    TRANSFORMED("Recast Full"),
    SIDE_BY_SIDE("Side-by-Side"),
    FACIAL_MESH("Neural Mesh")
}

data class PresetCharacterOption(
    val id: String,
    val name: String,
    val role: String,
    val accentColor: Color,
    val initialStrength: Long
)

/**
 * High-performance interactive Live Transformation Preview canvas.
 * Allows instant photo upload, character selection, real-time before/after wipe slider,
 * neural landmark tracking, and parameter tweaking with live visual feedback.
 */
@Composable
fun LiveTransformationPreviewCard(
    modifier: Modifier = Modifier,
    sourceClipTitle: String,
    activeCharacterName: String,
    customReferenceBitmap: Bitmap?,
    characterStrength: Long,
    onStrengthChange: (Long) -> Unit,
    qualityPreset: String,
    onQualityPresetChange: (String) -> Unit,
    conversionDirection: String = "forward_recast",
    onToggleConversionPolarity: (() -> Unit)? = null,
    videoResolution: String = "1080p",
    videoFrameRate: Long = 60L,
    videoCompressionLevel: Long = 18L,
    onVideoQualityParametersChange: ((String, Long, Long, String) -> Unit)? = null,
    onUploadPhotoClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onGenerateRecast: () -> Unit,
    onBeamToWorkspace: () -> Unit
) {
    var displayMode by remember { mutableStateOf(PreviewDisplayMode.SPLIT) }
    var splitFraction by remember { mutableFloatStateOf(0.52f) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentFrame by remember { mutableLongStateOf(24L) }
    var showFacialMesh by remember { mutableStateOf(true) }
    var lightingVectorMode by remember { mutableStateOf("Cyber Cyan") }
    var isExpandedControls by remember { mutableStateOf(false) }
    val activeConvMode = com.example.data.model.TwoWayConversionMode.fromId(conversionDirection)

    val presetCharacters = remember {
        listOf(
            PresetCharacterOption("elena", "Elena (Sci-Fi Detective)", "Cyberpunk Noir", GeminiCyan, 88L),
            PresetCharacterOption("marcus", "Marcus (Neo-Noir Protagonist)", "Moody Rim-Lit", NeonOrange, 82L),
            PresetCharacterOption("aria", "Aria (Cyborg Operative)", "Studio Hyperreal", GeminiPurple, 95L),
            PresetCharacterOption("kael", "Kael (Street Runner)", "Volumetric Gold", GeminiAmber, 78L)
        )
    }

    // Animation frame loop ~30 fps
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(33)
            currentFrame = if (currentFrame >= 300L) 1L else currentFrame + 1
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hudGlow")
    val laserGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalCard)
            .animatedRainbowBorder(strokeWidth = 1.2.dp, cornerRadius = 16.dp, glow = isPlaying)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
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
                        .background(Color(0xFF142030))
                        .border(1.dp, GeminiCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GeminiCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TWO-WAY AI CONVERSION PREVIEW",
                            fontSize = 13.sp,
                            fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            color = WarmIvory
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AiStudioBadge(text = "BIDIRECTIONAL")
                    }
                    Text(
                        text = "${activeConvMode.title} (${activeConvMode.directionBadge}) • $videoResolution @ ${videoFrameRate}fps",
                        fontSize = 10.sp,
                        fontFamily = com.example.ui.theme.InterFontFamily,
                        color = MutedIvory
                    )
                }
            }

            // Quick Play/Pause for animation simulation
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CharcoalElevated)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Preview" else "Play Preview",
                    tint = if (isPlaying) GeminiAmber else GeminiGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Bidirectional Conversion Polarity Strip (Source ⇄ Target)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E1522))
                .border(1.dp, GeminiCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACTIVE POLARITY: ${activeConvMode.directionBadge.uppercase()}",
                    fontSize = 9.sp,
                    fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = GeminiCyan
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (activeConvMode) {
                        com.example.data.model.TwoWayConversionMode.FORWARD_RECAST ->
                            "$sourceClipTitle (Motion) → ${activeCharacterName.substringBefore(" (")} (Cast)"
                        com.example.data.model.TwoWayConversionMode.INVERSE_DRIVE ->
                            "${activeCharacterName.substringBefore(" (")} (Still) ← $sourceClipTitle (Driver)"
                        com.example.data.model.TwoWayConversionMode.DUAL_SWAP ->
                            "$sourceClipTitle ⇄ ${activeCharacterName.substringBefore(" (")} (Mutual Swap)"
                        com.example.data.model.TwoWayConversionMode.REVERSE_EXTRACT ->
                            "$sourceClipTitle → Extracted 3D Identity & Voice Rig"
                    },
                    fontSize = 10.sp,
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory,
                    maxLines = 1
                )
            }

            if (onToggleConversionPolarity != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF17263B))
                        .border(1.dp, GeminiCyan.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .clickable { onToggleConversionPolarity() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("preview_invert_polarity_button")
                ) {
                    Text(
                        text = "⇄ Flip Direction",
                        fontSize = 10.sp,
                        fontFamily = com.example.ui.theme.PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GeminiCyan
                    )
                }
            }
        }

        // Quick Photo Upload & Preset Character Selector Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CharcoalElevated)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHOOSE OR UPLOAD REFERENCE PHOTO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = GeminiCyan
                )

                if (customReferenceBitmap != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Custom Photo Active", fontSize = 9.sp, color = GeminiGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons Row: Upload + Selfie + Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary One-Tap Upload Button
                Button(
                    onClick = onUploadPhotoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E1C38),
                        contentColor = GeminiPurple
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .border(1.dp, GeminiPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .testTag("upload_photo_button")
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Camera Selfie Button
                Button(
                    onClick = onCameraClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A1C12),
                        contentColor = NeonOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, NeonOrange.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .testTag("camera_selfie_button")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Snap Selfie", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Preset Characters Quick Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(presetCharacters) { preset ->
                    val isSelected = activeCharacterName == preset.name && customReferenceBitmap == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) preset.accentColor.copy(alpha = 0.25f) else ObsidianBlack)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) preset.accentColor else BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectPreset(preset.name)
                                onStrengthChange(preset.initialStrength)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(preset.accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.name.substringBefore(" ("),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) WarmIvory else MutedIvory
                            )
                        }
                    }
                }
            }
        }

        // View Mode Selector (Split / Full Recast / Side-by-Side / Mesh)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalElevated)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PreviewDisplayMode.values().forEach { mode ->
                val isSelected = displayMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) NeonOrange else Color.Transparent)
                        .clickable { displayMode = mode }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) ObsidianBlack else WarmIvory
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // LIVE TRANSFORMATION CANVAS
        // -------------------------------------------------------------
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianBlack)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .pointerInput(displayMode) {
                    if (displayMode == PreviewDisplayMode.SPLIT) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                            splitFraction = newFraction
                        }
                    }
                }
                .testTag("live_transformation_canvas")
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (displayMode) {
                    PreviewDisplayMode.SPLIT -> {
                        val splitX = width * splitFraction
                        // 1. Draw Left: Original Source Shot
                        drawSourceShotScene(
                            width = width,
                            height = height,
                            clipRight = splitX,
                            frame = currentFrame
                        )

                        // 2. Draw Right: Transformed Recast Scene
                        drawTransformedRecastScene(
                            width = width,
                            height = height,
                            clipLeft = splitX,
                            frame = currentFrame,
                            characterStrength = characterStrength,
                            lightingMode = lightingVectorMode,
                            hasCustomPhoto = customReferenceBitmap != null
                        )

                        // 3. Draw Laser Divider Line & Handle
                        drawLine(
                            color = NeonOrange.copy(alpha = laserGlowAlpha),
                            start = Offset(splitX, 0f),
                            end = Offset(splitX, height),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(splitX, 0f),
                            end = Offset(splitX, height),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Split Handle Knob
                        drawCircle(
                            color = NeonOrange,
                            radius = 14.dp.toPx(),
                            center = Offset(splitX, height / 2)
                        )
                        drawCircle(
                            color = ObsidianBlack,
                            radius = 6.dp.toPx(),
                            center = Offset(splitX, height / 2)
                        )
                    }

                    PreviewDisplayMode.TRANSFORMED -> {
                        drawTransformedRecastScene(
                            width = width,
                            height = height,
                            clipLeft = 0f,
                            frame = currentFrame,
                            characterStrength = characterStrength,
                            lightingMode = lightingVectorMode,
                            hasCustomPhoto = customReferenceBitmap != null
                        )
                    }

                    PreviewDisplayMode.SIDE_BY_SIDE -> {
                        val midX = width / 2f
                        drawSourceShotScene(width = width, height = height, clipRight = midX, frame = currentFrame)
                        drawTransformedRecastScene(
                            width = width,
                            height = height,
                            clipLeft = midX,
                            frame = currentFrame,
                            characterStrength = characterStrength,
                            lightingMode = lightingVectorMode,
                            hasCustomPhoto = customReferenceBitmap != null
                        )
                        drawLine(color = BorderSubtle, start = Offset(midX, 0f), end = Offset(midX, height), strokeWidth = 2.dp.toPx())
                    }

                    PreviewDisplayMode.FACIAL_MESH -> {
                        drawTransformedRecastScene(
                            width = width,
                            height = height,
                            clipLeft = 0f,
                            frame = currentFrame,
                            characterStrength = characterStrength,
                            lightingMode = lightingVectorMode,
                            hasCustomPhoto = customReferenceBitmap != null
                        )
                        drawNeuralFacialMesh(width = width, height = height, frame = currentFrame)
                    }
                }

                // If Facial Mesh overlay is toggled on during SPLIT mode
                if (showFacialMesh && displayMode == PreviewDisplayMode.SPLIT) {
                    val splitX = width * splitFraction
                    drawNeuralFacialMesh(width = width, height = height, frame = currentFrame, clipLeft = splitX)
                }
            }

            // Top HUD Overlay (Timecode & Fidelity Telemetry Badges)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FRAME ${currentFrame.toString().padStart(3, '0')} • $videoResolution",
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GeminiCyan.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${videoFrameRate} FPS • ${videoCompressionLevel}% COMP",
                            fontSize = 8.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${activeConvMode.shortTitle.uppercase()} • ${characterStrength}%",
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange
                        )
                    }
                }
            }

            // Bottom Canvas Labels: Dynamic based on Two-Way Conversion Mode
            if (displayMode == PreviewDisplayMode.SPLIT) {
                val (leftLabel, rightLabel) = when (activeConvMode) {
                    com.example.data.model.TwoWayConversionMode.FORWARD_RECAST ->
                        "◀ SOURCE FOOTAGE" to "NEURAL RECAST ▶"
                    com.example.data.model.TwoWayConversionMode.INVERSE_DRIVE ->
                        "◀ MOTION DRIVER VIDEO" to "ANIMATED STILL PHOTO ▶"
                    com.example.data.model.TwoWayConversionMode.DUAL_SWAP ->
                        "◀ ACTOR A SOURCE" to "ACTOR B DUAL SWAP ▶"
                    com.example.data.model.TwoWayConversionMode.REVERSE_EXTRACT ->
                        "◀ INPUT PERFORMANCE" to "EXTRACTED 3D IDENTITY RIG ▶"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = leftLabel,
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = rightLabel,
                            fontSize = 9.sp,
                            fontFamily = com.example.ui.theme.JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange
                        )
                    }
                }
            }
        }

        // Live Character Strength Slider Control (Realtime Transformation)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CharcoalElevated)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHARACTER TRANSFORMATION STRENGTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory
                    )
                }
                Text(
                    text = "$characterStrength%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeonOrange
                )
            }

            Slider(
                value = characterStrength.toFloat(),
                onValueChange = { onStrengthChange(it.toLong()) },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonOrange,
                    activeTrackColor = NeonOrange,
                    inactiveTrackColor = ObsidianBlack
                ),
                modifier = Modifier.testTag("live_strength_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtle Blend (10%)", fontSize = 9.sp, color = MutedIvory)
                Text("Default (85%)", fontSize = 9.sp, color = GeminiAmber)
                Text("Complete Morph (100%)", fontSize = 9.sp, color = NeonOrange, fontWeight = FontWeight.Bold)
            }
        }

        // Toggle Expandable Controls (Mesh, Lighting, Presets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalElevated)
                .clickable { isExpandedControls = !isExpandedControls }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExpandedControls) "Hide Neural Shading & Vector Mesh" else "Show Neural Shading & Vector Mesh (${lightingVectorMode})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory
                )
            }
            Text(
                text = if (isExpandedControls) "COLLAPSE ▲" else "EXPAND ▼",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = GeminiCyan
            )
        }

        AnimatedVisibility(visible = isExpandedControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalElevated)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Neural Mesh overlay toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show 468-Point Facial Landmark Wireframe", fontSize = 11.sp, color = WarmIvory)
                    Switch(
                        checked = showFacialMesh,
                        onCheckedChange = { showFacialMesh = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonOrange,
                            checkedTrackColor = Color(0xFF3B1D0E)
                        )
                    )
                }

                // Lighting Adaptation Vector Preset
                Text("LIGHTING VECTOR ADAPTATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiAmber)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cyber Cyan", "Warm Amber", "Studio Rim").forEach { mode ->
                        val isSelected = lightingVectorMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GeminiAmber.copy(alpha = 0.2f) else ObsidianBlack)
                                .border(1.dp, if (isSelected) GeminiAmber else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { lightingVectorMode = mode }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GeminiAmber else MutedIvory
                            )
                        }
                    }
                }
            }
        }

        // Custom Adaptive Video Fidelity Slider right before triggering the AI character swap
        AdaptiveVideoFidelitySlider(
            resolution = videoResolution,
            frameRate = videoFrameRate,
            compressionLevel = videoCompressionLevel,
            qualityPreset = qualityPreset,
            characterStrength = characterStrength,
            onCharacterStrengthChange = onStrengthChange,
            onQualityParametersChange = { newRes, newFps, newComp, newPreset ->
                onQualityPresetChange(newPreset)
                onVideoQualityParametersChange?.invoke(newRes, newFps, newComp, newPreset)
            },
            title = "PRE-SWAP VIDEO FIDELITY SLIDER"
        )

        // Primary Bottom Action Bar: "Generate Full Recast" & "Beam to Workspace"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBeamToWorkspace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalElevated,
                    contentColor = GeminiCyan
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(44.dp)
                    .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Beam to Node", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            AiStudioRainbowButton(
                text = "RECAST WITH THIS CAST",
                onClick = onGenerateRecast,
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
                    .testTag("generate_from_live_preview_button")
            )
        }
    }
}

// -------------------------------------------------------------------------
// CANVAS PROCEDURAL DRAWING HELPERS
// -------------------------------------------------------------------------

private fun DrawScope.drawSourceShotScene(
    width: Float,
    height: Float,
    clipRight: Float,
    frame: Long
) {
    val clipWidth = clipRight.coerceIn(0f, width)
    if (clipWidth <= 0f) return

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0F1D),
            Color(0xFF0F1829),
            Color(0xFF060912)
        )
    )

    drawRect(
        brush = bgGradient,
        topLeft = Offset.Zero,
        size = Size(clipWidth, height)
    )

    // Cinematic rainy street light beams
    val beamOffset = ((frame * 2) % width.toInt()).toFloat()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GeminiBlue.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(width * 0.25f, height * 0.4f),
            radius = width * 0.35f
        ),
        radius = width * 0.35f,
        center = Offset(width * 0.25f, height * 0.4f)
    )

    // Source silhouette / subject outline
    val centerX = width * 0.32f
    val centerY = height * 0.55f
    if (centerX < clipWidth) {
        drawCircle(
            color = Color(0xFF1E2838),
            radius = height * 0.22f,
            center = Offset(centerX, centerY)
        )
        // Jaw & Head shape
        drawOval(
            color = Color(0xFF2B3A4F),
            topLeft = Offset(centerX - height * 0.14f, centerY - height * 0.26f),
            size = Size(height * 0.28f, height * 0.34f)
        )
        // Rim lighting
        drawArc(
            color = GeminiCyan.copy(alpha = 0.5f),
            startAngle = 180f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(centerX - height * 0.14f, centerY - height * 0.26f),
            size = Size(height * 0.28f, height * 0.34f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
    }
}

private fun DrawScope.drawTransformedRecastScene(
    width: Float,
    height: Float,
    clipLeft: Float,
    frame: Long,
    characterStrength: Long,
    lightingMode: String,
    hasCustomPhoto: Boolean
) {
    val renderWidth = (width - clipLeft).coerceAtLeast(0f)
    if (renderWidth <= 0f) return

    val rimColor = when (lightingMode) {
        "Warm Amber" -> GeminiAmber
        "Studio Rim" -> ChampagneGold
        else -> GeminiCyan
    }

    val strengthFraction = (characterStrength / 100f).coerceIn(0.1f, 1f)

    // Background: High-tech studio with volumetric rim lighting
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A0A26),
            Color(0xFF261036),
            Color(0xFF0F0517)
        )
    )

    drawRect(
        brush = bgGradient,
        topLeft = Offset(clipLeft, 0f),
        size = Size(renderWidth, height)
    )

    // Neural bloom ambient light
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(rimColor.copy(alpha = 0.3f * strengthFraction), Color.Transparent),
            center = Offset(width * 0.65f, height * 0.45f),
            radius = width * 0.45f
        ),
        radius = width * 0.45f,
        center = Offset(width * 0.65f, height * 0.45f)
    )

    // Transformed Head & Facial Structure
    val centerX = width * 0.65f
    val centerY = height * 0.52f
    if (centerX >= clipLeft - 100f) {
        // Shoulders & Chest
        drawOval(
            color = Color(0xFF2C1640),
            topLeft = Offset(centerX - height * 0.28f, centerY + height * 0.08f),
            size = Size(height * 0.56f, height * 0.45f)
        )

        // Face & Jawline with Neural Cast
        val faceColor = if (hasCustomPhoto) Color(0xFF4A2B4C) else Color(0xFF381F48)
        drawOval(
            color = faceColor,
            topLeft = Offset(centerX - height * 0.15f, centerY - height * 0.25f),
            size = Size(height * 0.30f, height * 0.36f)
        )

        // Neural Recast Rim Glow (Reflects lighting adaptation vector)
        drawArc(
            color = rimColor.copy(alpha = 0.85f * strengthFraction),
            startAngle = -45f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - height * 0.15f, centerY - height * 0.25f),
            size = Size(height * 0.30f, height * 0.36f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
        )

        // Sharp jawline & cheekbone highlight lines
        drawLine(
            color = rimColor.copy(alpha = 0.7f),
            start = Offset(centerX - height * 0.1f, centerY + height * 0.02f),
            end = Offset(centerX, centerY + height * 0.11f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = rimColor.copy(alpha = 0.7f),
            start = Offset(centerX + height * 0.1f, centerY + height * 0.02f),
            end = Offset(centerX, centerY + height * 0.11f),
            strokeWidth = 2.dp.toPx()
        )

        // Eyes highlight
        val eyeSpacing = height * 0.06f
        val eyeY = centerY - height * 0.06f
        drawCircle(color = rimColor, radius = 3.dp.toPx(), center = Offset(centerX - eyeSpacing, eyeY))
        drawCircle(color = rimColor, radius = 3.dp.toPx(), center = Offset(centerX + eyeSpacing, eyeY))
    }
}

private fun DrawScope.drawNeuralFacialMesh(
    width: Float,
    height: Float,
    frame: Long,
    clipLeft: Float = 0f
) {
    val centerX = if (clipLeft > 0f) width * 0.65f else width * 0.5f
    val centerY = height * 0.52f
    val r = height * 0.14f

    if (centerX < clipLeft - 50f) return

    val meshColor = GeminiCyan.copy(alpha = 0.45f)
    val pointColor = NeonOrange.copy(alpha = 0.8f)

    // Draw landmark points
    val points = listOf(
        Offset(centerX, centerY - r * 1.1f),           // Forehead
        Offset(centerX - r * 0.6f, centerY - r * 0.8f),// Left Brow
        Offset(centerX + r * 0.6f, centerY - r * 0.8f),// Right Brow
        Offset(centerX - r * 0.45f, centerY - r * 0.4f), // Left Eye
        Offset(centerX + r * 0.45f, centerY - r * 0.4f), // Right Eye
        Offset(centerX, centerY - r * 0.2f),           // Nose bridge
        Offset(centerX, centerY + r * 0.1f),           // Nose tip
        Offset(centerX - r * 0.35f, centerY + r * 0.45f),// Left Mouth
        Offset(centerX + r * 0.35f, centerY + r * 0.45f),// Right Mouth
        Offset(centerX, centerY + r * 0.55f),          // Lower Lip
        Offset(centerX, centerY + r * 0.95f),          // Chin
        Offset(centerX - r * 0.85f, centerY + r * 0.2f),// Left Jaw
        Offset(centerX + r * 0.85f, centerY + r * 0.2f) // Right Jaw
    )

    // Connect points into wireframe triangles
    val connections = listOf(
        0 to 1, 0 to 2, 1 to 3, 2 to 4, 3 to 5, 4 to 5,
        5 to 6, 6 to 7, 6 to 8, 7 to 9, 8 to 9, 9 to 10,
        1 to 11, 11 to 7, 11 to 10, 2 to 12, 12 to 8, 12 to 10
    )

    connections.forEach { (a, b) ->
        val p1 = points[a]
        val p2 = points[b]
        if (p1.x >= clipLeft || p2.x >= clipLeft) {
            drawLine(
                color = meshColor,
                start = p1,
                end = p2,
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }

    // Points
    points.forEach { pt ->
        if (pt.x >= clipLeft) {
            drawCircle(
                color = pointColor,
                radius = 2.5.dp.toPx(),
                center = pt
            )
        }
    }
}
