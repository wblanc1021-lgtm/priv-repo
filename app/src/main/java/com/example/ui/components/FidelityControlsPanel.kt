package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.data.model.TwoWayConversionMode
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.WarmIvory
import kotlin.math.roundToInt

private val RESOLUTION_STOPS = listOf("480p", "720p", "1080p", "1440p", "4K")
private val FRAMERATE_STOPS = listOf(24L, 30L, 48L, 60L, 120L)

/**
 * Adaptive Video Fidelity & Two-Way AI Conversion Slider Suite.
 * Designed with clean glassmorphic borders and smooth typography (Plus Jakarta Sans, Inter, JetBrains Mono)
 * to fit all platform views (Mobile Compact, Web Cloud, Computer Pro Workstation, and All-in-One Dock).
 */
@Composable
fun FidelityControlsPanel(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    modifier: Modifier = Modifier,
    platformLabel: String = "ADAPTIVE MULTI-VIEW"
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }
    val activeMode = project.activeConversionMode()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalCard)
            .studioGlassBorder(
                strokeWidth = 1.dp,
                cornerRadius = 16.dp,
                accentColor = GeminiCyan,
                elevatedGlow = false
            )
            .padding(14.dp)
            .testTag("video_fidelity_controls_panel")
    ) {
        val isWideLayout = maxWidth >= 520.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Bar with Live Telemetry Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, NeonOrange.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Video Fidelity & Two-Way Conversion Controls",
                            tint = NeonOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VIDEO FIDELITY & 2-WAY AI MASTERING",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = WarmIvory
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiCyan.copy(alpha = 0.14f))
                                    .border(1.dp, GeminiCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = platformLabel,
                                    fontSize = 8.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiCyan
                                )
                            }
                        }
                        Text(
                            text = "${project.resolution} (${project.resolutionDimensionsLabel()}) • ${project.frameRate} FPS • ${project.compressionLevel}% Comp • ${project.estimatedBitrateMbps()} Mbps",
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = MutedIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { isAdvancedExpanded = !isAdvancedExpanded },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Advanced Neural Controls",
                        tint = WarmIvory
                    )
                }
            }

            // 2. Two-Way AI Conversion Direction & Polarity Switcher
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack.copy(alpha = 0.65f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TWO-WAY AI CONVERSION DIRECTION",
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp,
                            color = GeminiCyan
                        )
                    }

                    // 1-Tap Flip Polarity Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GeminiBlue.copy(alpha = 0.35f), GeminiPurple.copy(alpha = 0.35f))
                                )
                            )
                            .border(1.dp, GeminiCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                onProjectChange(project.flippedPolarityProject())
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("flip_conversion_polarity_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Flip Conversion Polarity",
                            tint = GeminiCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⇄ Invert Polarity",
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                    }
                }

                // 4 Two-Way Conversion Mode Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TwoWayConversionMode.entries.forEach { mode ->
                        val isSelected = activeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) NeonOrange.copy(alpha = 0.18f)
                                    else CharcoalElevated
                                )
                                .border(
                                    width = if (isSelected) 1.2.dp else 1.dp,
                                    color = if (isSelected) NeonOrange else BorderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onProjectChange(project.copy(conversionDirection = mode.key))
                                }
                                .padding(horizontal = 6.dp, vertical = 7.dp)
                                .testTag("conversion_mode_${mode.key}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = mode.shortTitle,
                                    fontSize = 10.sp,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) WarmIvory else MutedIvory,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mode.badge,
                                    fontSize = 8.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = if (isSelected) NeonOrange else MutedIvory.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${activeMode.directionFormula} — ${activeMode.description}",
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = MutedIvory
                )
            }

            // 3. Quick Coordinated Mastering Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val quickProfiles = listOf(
                    Triple("Cinema 4K", "4K", Triple(24L, 8L, "max")),
                    Triple("Studio QHD", "1440p", Triple(60L, 16L, "high")),
                    Triple("Pro FHD", "1080p", Triple(60L, 22L, "high")),
                    Triple("Web Stream", "720p", Triple(30L, 52L, "standard"))
                )
                quickProfiles.forEach { (label, res, specs) ->
                    val (fps, comp, preset) = specs
                    val isMatched = project.resolution.equals(res, ignoreCase = true) &&
                        project.frameRate == fps &&
                        (project.compressionLevel - comp) in -5L..5L
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMatched) GeminiCyan.copy(alpha = 0.16f) else CharcoalElevated
                            )
                            .border(
                                1.dp,
                                if (isMatched) GeminiCyan else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onProjectChange(
                                    project.copy(
                                        resolution = res,
                                        frameRate = fps,
                                        compressionLevel = comp,
                                        qualityPreset = preset
                                    )
                                )
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isMatched) GeminiCyan else WarmIvory,
                                maxLines = 1
                            )
                            Text(
                                text = "$res • ${fps}fps",
                                fontSize = 8.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MutedIvory,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 4. Core Video Fidelity Sliders: Resolution, Frame Rate & Compression Level
            // Automatically adapts between 2-column rack on wide screens and vertical stack on compact screens
            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResolutionFidelitySliderBox(
                        project = project,
                        onProjectChange = onProjectChange,
                        modifier = Modifier.weight(1f)
                    )
                    FrameRateFidelitySliderBox(
                        project = project,
                        onProjectChange = onProjectChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                ResolutionFidelitySliderBox(
                    project = project,
                    onProjectChange = onProjectChange,
                    modifier = Modifier.fillMaxWidth()
                )
                FrameRateFidelitySliderBox(
                    project = project,
                    onProjectChange = onProjectChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Compression & Bitrate Fine-Tuning Slider
            CompressionFidelitySliderBox(
                project = project,
                onProjectChange = onProjectChange,
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Primary Character Swap / Conversion Strength Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalElevated.copy(alpha = 0.6f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Two-Way Identity & Motion Blend Strength",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory
                    )
                    Text(
                        text = "${project.characterStrength}%",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange
                    )
                }
                Slider(
                    value = project.characterStrength.toFloat(),
                    onValueChange = { onProjectChange(project.copy(characterStrength = it.toLong())) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonOrange,
                        activeTrackColor = NeonOrange,
                        inactiveTrackColor = ObsidianBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_strength_slider")
                )
            }

            // 6. Collapsible Advanced Neural & Optical Flow Tuning
            AnimatedVisibility(
                visible = isAdvancedExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Quality Preset Selector
                    Text(
                        text = "RENDER ENGINE QUALITY TIER",
                        fontSize = 10.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf("draft", "standard", "high", "max")
                        presets.forEach { preset ->
                            val isSelected = project.qualityPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { onProjectChange(project.copy(qualityPreset = preset)) },
                                label = {
                                    Text(
                                        text = preset.uppercase(),
                                        fontSize = 11.sp,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonOrange,
                                    selectedLabelColor = ObsidianBlack,
                                    containerColor = CharcoalElevated,
                                    labelColor = WarmIvory
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) NeonOrange else BorderSubtle
                                )
                            )
                        }
                    }

                    // Priority Switch (Face vs Full-Body)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Priority Topology Mapping",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmIvory
                            )
                            Text(
                                text = if (project.facePriority) {
                                    "Face priority (Strict 468-point facial contours & lip visemes)"
                                } else {
                                    "Full-body priority (Wardrobe, silhouette & skeletal motion match)"
                                },
                                fontSize = 11.sp,
                                fontFamily = InterFontFamily,
                                color = MutedIvory
                            )
                        }
                        Switch(
                            checked = project.facePriority,
                            onCheckedChange = { onProjectChange(project.copy(facePriority = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = NeonOrange,
                                uncheckedThumbColor = WarmIvory,
                                uncheckedTrackColor = CharcoalElevated
                            ),
                            modifier = Modifier.testTag("face_priority_switch")
                        )
                    }

                    FidelitySlider(
                        label = "Identity Consistency",
                        subtext = "Reference portrait lock across extreme head turns",
                        value = project.identityConsistency,
                        onValueChange = { onProjectChange(project.copy(identityConsistency = it)) }
                    )

                    FidelitySlider(
                        label = "Lighting Adaptation",
                        subtext = "Scene HDRI relighting vs reference shading",
                        value = project.lightingAdaptation,
                        onValueChange = { onProjectChange(project.copy(lightingAdaptation = it)) }
                    )

                    FidelitySlider(
                        label = "Motion Fidelity",
                        subtext = "Strict driving actor micro-expression transfer",
                        value = project.motionFidelity,
                        onValueChange = { onProjectChange(project.copy(motionFidelity = it)) }
                    )

                    FidelitySlider(
                        label = "Edge & Hair Refinement",
                        subtext = "Sub-pixel alpha matte & silhouette blending",
                        value = project.edgeRefinement,
                        onValueChange = { onProjectChange(project.copy(edgeRefinement = it)) }
                    )

                    FidelitySlider(
                        label = "Temporal Consistency",
                        subtext = "Inter-frame de-flicker & optical flow stabilization",
                        value = project.temporalConsistency,
                        onValueChange = { onProjectChange(project.copy(temporalConsistency = it)) }
                    )

                    // Variants Count & Background Protection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BATCH VARIANTS",
                                fontSize = 10.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MutedIvory
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1L, 2L, 4L).forEach { vCount ->
                                    val sel = project.variantsCount == vCount
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) NeonOrange else CharcoalElevated)
                                            .clickable { onProjectChange(project.copy(variantsCount = vCount)) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${vCount}x",
                                            fontSize = 11.sp,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) ObsidianBlack else WarmIvory
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Seed Lock",
                                tint = GeminiCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Seed: ${project.seed}",
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = WarmIvory
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CharcoalElevated)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable {
                                        onProjectChange(project.copy(seed = (1000..99999).random().toLong()))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Randomize",
                                    fontSize = 10.sp,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeminiCyan
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Protect Scene Background",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmIvory
                            )
                            Text(
                                text = "Strict neural matte lock on background environment",
                                fontSize = 11.sp,
                                fontFamily = InterFontFamily,
                                color = MutedIvory
                            )
                        }
                        Switch(
                            checked = project.protectBackground,
                            onCheckedChange = { onProjectChange(project.copy(protectBackground = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = GeminiCyan,
                                uncheckedThumbColor = WarmIvory,
                                uncheckedTrackColor = CharcoalElevated
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionFidelitySliderBox(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = RESOLUTION_STOPS.indexOfFirst {
        it.equals(project.resolution, ignoreCase = true)
    }.let { if (it == -1) 2 else it }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated.copy(alpha = 0.72f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = GeminiCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Resolution",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory
                    )
                    Text(
                        text = project.resolutionDimensionsLabel(),
                        fontSize = 10.sp,
                        fontFamily = InterFontFamily,
                        color = MutedIvory
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(GeminiCyan.copy(alpha = 0.16f))
                    .border(1.dp, GeminiCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = project.resolution,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = GeminiCyan
                )
            }
        }

        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { raw ->
                val idx = raw.roundToInt().coerceIn(0, RESOLUTION_STOPS.lastIndex)
                val nextRes = RESOLUTION_STOPS[idx]
                if (nextRes != project.resolution) {
                    onProjectChange(project.copy(resolution = nextRes))
                }
            },
            valueRange = 0f..RESOLUTION_STOPS.lastIndex.toFloat(),
            steps = RESOLUTION_STOPS.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = GeminiCyan,
                activeTrackColor = GeminiCyan,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_resolution_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RESOLUTION_STOPS.forEach { stop ->
                val selected = stop.equals(project.resolution, ignoreCase = true)
                Text(
                    text = stop,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) GeminiCyan else MutedIvory,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onProjectChange(project.copy(resolution = stop)) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FrameRateFidelitySliderBox(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = FRAMERATE_STOPS.indexOf(project.frameRate).let {
        if (it == -1) {
            FRAMERATE_STOPS.indices.minByOrNull { idx ->
                kotlin.math.abs(FRAMERATE_STOPS[idx] - project.frameRate)
            } ?: 3
        } else it
    }

    val cadenceDescription = when (project.frameRate) {
        24L -> "24p True Cinema Cadence"
        30L -> "30p Broadcast Standard"
        48L -> "48p High Frame Rate Motion"
        60L -> "60p Ultra-Smooth Studio"
        else -> "120p Master Slow-Mo / HFR"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated.copy(alpha = 0.72f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NeonOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Frame Rate",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory
                    )
                    Text(
                        text = cadenceDescription,
                        fontSize = 10.sp,
                        fontFamily = InterFontFamily,
                        color = MutedIvory
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonOrange.copy(alpha = 0.16f))
                    .border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${project.frameRate} FPS",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = NeonOrange
                )
            }
        }

        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { raw ->
                val idx = raw.roundToInt().coerceIn(0, FRAMERATE_STOPS.lastIndex)
                val nextFps = FRAMERATE_STOPS[idx]
                if (nextFps != project.frameRate) {
                    onProjectChange(project.copy(frameRate = nextFps))
                }
            },
            valueRange = 0f..FRAMERATE_STOPS.lastIndex.toFloat(),
            steps = FRAMERATE_STOPS.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_framerate_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FRAMERATE_STOPS.forEach { fps ->
                val selected = project.frameRate == fps
                Text(
                    text = "${fps}fps",
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) NeonOrange else MutedIvory,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onProjectChange(project.copy(frameRate = fps)) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CompressionFidelitySliderBox(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val compressionAccent = when {
        project.compressionLevel <= 25L -> GeminiGreen
        project.compressionLevel <= 60L -> GeminiAmber
        else -> NeonOrange
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated.copy(alpha = 0.72f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Compression & Bitrate Target",
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory
                )
                Text(
                    text = "${project.activeCodecLabel()} • Est. ~${project.estimatedSizeMbPerMin()} MB/min",
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = MutedIvory
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${project.estimatedBitrateMbps()} Mbps",
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GeminiCyan
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(compressionAccent.copy(alpha = 0.16f))
                        .border(1.dp, compressionAccent.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${project.compressionLevel}% COMP",
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = compressionAccent
                    )
                }
            }
        }

        Slider(
            value = project.compressionLevel.toFloat(),
            onValueChange = { raw ->
                onProjectChange(project.copy(compressionLevel = raw.roundToInt().toLong().coerceIn(0L, 90L)))
            },
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = compressionAccent,
                activeTrackColor = compressionAccent,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_compression_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val compStops = listOf(
                0L to "0% Lossless",
                22L to "22% Master",
                45L to "45% Balanced",
                75L to "75% Stream"
            )
            compStops.forEach { (level, label) ->
                val isNear = kotlin.math.abs(project.compressionLevel - level) <= 6L
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = if (isNear) FontWeight.Bold else FontWeight.Normal,
                    color = if (isNear) compressionAccent else MutedIvory,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onProjectChange(project.copy(compressionLevel = level)) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FidelitySlider(
    label: String,
    subtext: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory
                )
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = MutedIvory
                )
            }
            Text(
                text = "$value%",
                fontSize = 12.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = NeonOrange
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toLong()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Reusable compact Video Fidelity Slider Strip for Veo 3 Video Studio and Live Session screens.
 * Provides interactive sliders and quick-tap stops for Resolution, Frame Rate, and Compression/Bitrate.
 */
@Composable
fun VideoFidelityCompactStrip(
    resolution: String,
    frameRate: Long,
    compressionLevel: Long,
    onResolutionChange: (String) -> Unit,
    onFrameRateChange: (Long) -> Unit,
    onCompressionChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val resStops = listOf("480p", "720p", "1080p", "1440p", "4K")
    val fpsStops = listOf(24L, 30L, 48L, 60L, 120L)
    val resIdx = resStops.indexOfFirst { it.equals(resolution, ignoreCase = true) }.let { if (it == -1) 2 else it }
    val fpsIdx = fpsStops.indexOf(frameRate).let { if (it == -1) 3 else it }

    val dummyProject = RecastProject(
        resolution = resolution,
        frameRate = frameRate,
        compressionLevel = compressionLevel
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0E1522))
            .border(1.dp, GeminiCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = GeminiCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VIDEO FIDELITY SLIDERS",
                    fontSize = 11.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory
                )
            }
            Text(
                text = "$resolution @ ${frameRate}fps • ~${dummyProject.estimatedBitrateMbps()} Mbps",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = GeminiCyan
            )
        }

        // 1. Resolution Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Resolution (${dummyProject.resolutionDimensionsLabel()})",
                fontSize = 10.sp,
                fontFamily = InterFontFamily,
                color = MutedIvory
            )
            Text(
                text = resolution,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = GeminiCyan
            )
        }
        Slider(
            value = resIdx.toFloat(),
            onValueChange = { raw ->
                val next = resStops[raw.roundToInt().coerceIn(0, resStops.lastIndex)]
                if (next != resolution) onResolutionChange(next)
            },
            valueRange = 0f..resStops.lastIndex.toFloat(),
            steps = resStops.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = GeminiCyan,
                activeTrackColor = GeminiCyan,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Frame Rate Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Frame Rate Cadence",
                fontSize = 10.sp,
                fontFamily = InterFontFamily,
                color = MutedIvory
            )
            Text(
                text = "${frameRate} FPS",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = NeonOrange
            )
        }
        Slider(
            value = fpsIdx.toFloat(),
            onValueChange = { raw ->
                val next = fpsStops[raw.roundToInt().coerceIn(0, fpsStops.lastIndex)]
                if (next != frameRate) onFrameRateChange(next)
            },
            valueRange = 0f..fpsStops.lastIndex.toFloat(),
            steps = fpsStops.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Compression Level Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Compression (${dummyProject.activeCodecLabel()})",
                fontSize = 10.sp,
                fontFamily = InterFontFamily,
                color = MutedIvory
            )
            Text(
                text = "${compressionLevel}% Comp",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = GeminiGreen
            )
        }
        Slider(
            value = compressionLevel.toFloat(),
            onValueChange = { raw ->
                onCompressionChange(raw.roundToInt().toLong().coerceIn(0L, 90L))
            },
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = GeminiGreen,
                activeTrackColor = GeminiGreen,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

