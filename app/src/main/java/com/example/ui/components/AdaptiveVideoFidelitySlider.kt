package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.WarmIvory
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Represents a coordinated processing quality tier on the custom Adaptive Video Fidelity Slider.
 */
enum class ProcessingQualityTier(
    val key: String,
    val label: String,
    val shortLabel: String,
    val defaultResolution: String,
    val defaultFrameRate: Long,
    val defaultCompression: Long,
    val neuralPassDescription: String,
    val targetProgress: Float,
    val accentColor: Color
) {
    DRAFT(
        key = "draft",
        label = "Draft Preview",
        shortLabel = "DRAFT",
        defaultResolution = "480p",
        defaultFrameRate = 24L,
        defaultCompression = 68L,
        neuralPassDescription = "Single-pass fast proxy • Rapid identity preview",
        targetProgress = 0.15f,
        accentColor = GeminiAmber
    ),
    STANDARD(
        key = "standard",
        label = "Balanced HD",
        shortLabel = "BALANCED",
        defaultResolution = "720p",
        defaultFrameRate = 30L,
        defaultCompression = 42L,
        neuralPassDescription = "Dual-pass optical flow • Real-time social stream",
        targetProgress = 0.45f,
        accentColor = GeminiCyan
    ),
    HIGH(
        key = "high",
        label = "Studio Pro FHD",
        shortLabel = "STUDIO PRO",
        defaultResolution = "1080p",
        defaultFrameRate = 60L,
        defaultCompression = 20L,
        neuralPassDescription = "468-pt temporal mesh lock + HDRI relighting",
        targetProgress = 0.76f,
        accentColor = NeonOrange
    ),
    MAX(
        key = "max",
        label = "Cinema 4K Master",
        shortLabel = "4K MASTER",
        defaultResolution = "4K",
        defaultFrameRate = 60L,
        defaultCompression = 8L,
        neuralPassDescription = "Sub-pixel alpha hair matte • Lossless 10-bit mastering",
        targetProgress = 0.96f,
        accentColor = GeminiGreen
    );

    companion object {
        fun fromPreset(preset: String): ProcessingQualityTier {
            return entries.find { it.key.equals(preset, ignoreCase = true) } ?: HIGH
        }
    }
}

private val FIDELITY_RESOLUTIONS = listOf("480p", "720p", "1080p", "1440p", "4K")
private val FIDELITY_FRAMERATES = listOf(24L, 30L, 48L, 60L, 120L)

/**
 * Computes a unified 0f..1f processing fidelity score from resolution, frame rate, compression, and quality preset.
 */
fun computeUnifiedFidelityFraction(
    resolution: String,
    frameRate: Long,
    compressionLevel: Long,
    qualityPreset: String
): Float {
    val resIndex = FIDELITY_RESOLUTIONS.indexOfFirst { it.equals(resolution, ignoreCase = true) }
        .let { if (it == -1) 2 else it }
    val resScore = resIndex / 4f // 0.0 .. 1.0

    val fpsIndex = FIDELITY_FRAMERATES.indexOf(frameRate).let {
        if (it == -1) 3 else it
    }
    val fpsScore = fpsIndex / 4f // 0.0 .. 1.0

    val compQualityScore = (1f - (compressionLevel.coerceIn(0L, 90L) / 90f)).coerceIn(0f, 1f)

    val presetScore = when (qualityPreset.lowercase()) {
        "draft" -> 0.2f
        "standard" -> 0.5f
        "high" -> 0.8f
        "max" -> 1.0f
        else -> 0.75f
    }

    return (resScore * 0.35f + fpsScore * 0.20f + compQualityScore * 0.25f + presetScore * 0.20f)
        .coerceIn(0.05f, 1.0f)
}

/**
 * Custom Adaptive Video Fidelity Slider UI Component.
 *
 * Allows users to interactively adjust video processing quality before triggering the AI character swap.
 * - Features a custom-drawn interactive Canvas spectrum slider with magnetic quality tiers (`Draft`, `Balanced`, `Studio Pro`, `4K Master`)
 * - Automatically adapts its layout via `BoxWithConstraints` across Compact (phones), Medium (foldables/tablets), and Expanded (desktop/workstation) viewports
 * - Provides fine-grained sliders for Resolution (`480p`–`4K`), Frame Rate (`24`–`120 FPS`), and Compression (`0%`–`90%`) with real-time bitrate and render cost telemetry.
 */
@Composable
fun AdaptiveVideoFidelitySlider(
    resolution: String,
    frameRate: Long,
    compressionLevel: Long,
    qualityPreset: String,
    onQualityParametersChange: (
        newResolution: String,
        newFrameRate: Long,
        newCompression: Long,
        newQualityPreset: String
    ) -> Unit,
    modifier: Modifier = Modifier,
    characterStrength: Long = 88L,
    onCharacterStrengthChange: ((Long) -> Unit)? = null,
    title: String = "PRE-SWAP VIDEO FIDELITY & QUALITY SLIDER",
     initiallyExpandedFineTuning: Boolean = false
) {
    var showFineTuningSliders by remember { mutableStateOf(initiallyExpandedFineTuning) }

    val activeTier = ProcessingQualityTier.fromPreset(qualityPreset)
    val rawFidelityFraction = computeUnifiedFidelityFraction(
        resolution = resolution,
        frameRate = frameRate,
        compressionLevel = compressionLevel,
        qualityPreset = qualityPreset
    )
    val animatedFraction by animateFloatAsState(
        targetValue = rawFidelityFraction,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f),
        label = "fidelitySpectrumFraction"
    )
    val fidelityPercent = (rawFidelityFraction * 100f).roundToInt().coerceIn(5, 100)

    val telemetryProject = remember(resolution, frameRate, compressionLevel, qualityPreset, characterStrength) {
        RecastProject(
            resolution = resolution,
            frameRate = frameRate,
            compressionLevel = compressionLevel,
            qualityPreset = qualityPreset,
            characterStrength = characterStrength
        )
    }

    // Apply a continuous 0f..1f scrub along the master quality spectrum
    val applyMasterSpectrumFraction: (Float) -> Unit = { fraction ->
        val clamped = fraction.coerceIn(0f, 1f)
        when {
            clamped < 0.28f -> {
                val comp = (80L - (clamped / 0.28f * 20f).roundToInt()).coerceIn(55L, 85L)
                onQualityParametersChange("480p", 24L, comp, "draft")
            }
            clamped < 0.56f -> {
                val local = (clamped - 0.28f) / 0.28f
                val fps = if (local > 0.5f) 30L else 24L
                val comp = (54L - (local * 18f).roundToInt()).coerceIn(32L, 54L)
                onQualityParametersChange("720p", fps, comp, "standard")
            }
            clamped < 0.84f -> {
                val local = (clamped - 0.56f) / 0.28f
                val res = if (local > 0.65f) "1440p" else "1080p"
                val fps = if (local > 0.35f) 60L else 30L
                val comp = (30L - (local * 14f).roundToInt()).coerceIn(14L, 30L)
                onQualityParametersChange(res, fps, comp, "high")
            }
            else -> {
                val local = ((clamped - 0.84f) / 0.16f).coerceIn(0f, 1f)
                val fps = if (local > 0.7f) 120L else 60L
                val comp = (12L - (local * 10f).roundToInt()).coerceIn(2L, 12L)
                onQualityParametersChange("4K", fps, comp, "max")
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF101726),
                        CharcoalCard
                    )
                )
            )
            .studioGlassBorder(
                strokeWidth = 1.dp,
                cornerRadius = 14.dp,
                accentColor = activeTier.accentColor,
                elevatedGlow = false
            )
            .padding(12.dp)
            .testTag("adaptive_video_fidelity_component")
    ) {
        val isExpandedViewport = maxWidth >= 560.dp
        val isMediumViewport = maxWidth >= 400.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header Row: Title, Active Quality Score Badge & Fine-Tune Toggle
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeTier.accentColor.copy(alpha = 0.15f))
                            .border(1.dp, activeTier.accentColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Processing Quality",
                            tint = activeTier.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = WarmIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeTier.neuralPassDescription,
                            fontSize = 9.sp,
                            fontFamily = InterFontFamily,
                            color = MutedIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Live Quality Index Pill + Fine-Tune Expander Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(activeTier.accentColor.copy(alpha = 0.18f))
                            .border(1.dp, activeTier.accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${activeTier.shortLabel} • $fidelityPercent%",
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = activeTier.accentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { showFineTuningSliders = !showFineTuningSliders }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("toggle_fidelity_fine_tune_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Fine-tune fidelity parameters",
                                tint = GeminiCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            if (isMediumViewport) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showFineTuningSliders) "Hide Params" else "Fine-Tune",
                                    fontSize = 9.sp,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeminiCyan
                                )
                            }
                            Icon(
                                imageVector = if (showFineTuningSliders) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = GeminiCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 2. Interactive Custom Canvas Master Fidelity Spectrum Track
            CustomFidelitySpectrumTrack(
                fraction = animatedFraction,
                activeColor = activeTier.accentColor,
                resolution = resolution,
                frameRate = frameRate,
                compressionLevel = compressionLevel,
                onFractionChange = applyMasterSpectrumFraction
            )

            // 3. Stepped Processing Quality Tier Selector Pills (Draft / Balanced / Studio Pro / 4K Master)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProcessingQualityTier.entries.forEach { tier ->
                    val isSelected = activeTier == tier
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) tier.accentColor.copy(alpha = 0.18f)
                                else CharcoalElevated.copy(alpha = 0.75f)
                            )
                            .border(
                                width = if (isSelected) 1.2.dp else 1.dp,
                                color = if (isSelected) tier.accentColor else BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onQualityParametersChange(
                                    tier.defaultResolution,
                                    tier.defaultFrameRate,
                                    tier.defaultCompression,
                                    tier.key
                                )
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("fidelity_tier_${tier.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tier.shortLabel,
                                fontSize = 9.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) tier.accentColor else WarmIvory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tier.defaultResolution}@${tier.defaultFrameRate}p",
                                fontSize = 8.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (isSelected) WarmIvory else MutedIvory,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 4. Adaptive Telemetry Readout Strip (Resolution, Frame Rate, Compression, Bitrate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianBlack.copy(alpha = 0.7f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryMetricItem(
                    label = "OUTPUT RES",
                    value = "$resolution (${telemetryProject.resolutionDimensionsLabel()})",
                    color = GeminiCyan
                )
                TelemetryMetricItem(
                    label = "CADENCE",
                    value = "$frameRate FPS",
                    color = NeonOrange
                )
                TelemetryMetricItem(
                    label = "COMPRESSION",
                    value = "$compressionLevel% (${String.format(Locale.US, "%.1f", telemetryProject.estimatedBitrateMbps())} Mbps)",
                    color = GeminiGreen
                )
                if (isExpandedViewport) {
                    TelemetryMetricItem(
                        label = "CODEC PIPELINE",
                        value = telemetryProject.recommendedCodecLabel(),
                        color = WarmIvory
                    )
                }
            }

            // 5. Expandable / Adaptive Multi-Column Fine-Tuning Sliders (Resolution, Frame Rate, Compression, Swap Blend)
            AnimatedVisibility(
                visible = showFineTuningSliders || isExpandedViewport,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalElevated.copy(alpha = 0.65f))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isExpandedViewport) {
                        // Wide / Tablet / Workstation 2-Column Rack
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FineTuneResolutionSlider(
                                resolution = resolution,
                                dimensionsLabel = telemetryProject.resolutionDimensionsLabel(),
                                onResolutionChange = { newRes ->
                                    onQualityParametersChange(newRes, frameRate, compressionLevel, qualityPreset)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FineTuneFrameRateSlider(
                                frameRate = frameRate,
                                onFrameRateChange = { newFps ->
                                    onQualityParametersChange(resolution, newFps, compressionLevel, qualityPreset)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Compact Vertical Stack
                        FineTuneResolutionSlider(
                            resolution = resolution,
                            dimensionsLabel = telemetryProject.resolutionDimensionsLabel(),
                            onResolutionChange = { newRes ->
                                onQualityParametersChange(newRes, frameRate, compressionLevel, qualityPreset)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        FineTuneFrameRateSlider(
                            frameRate = frameRate,
                            onFrameRateChange = { newFps ->
                                onQualityParametersChange(resolution, newFps, compressionLevel, qualityPreset)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FineTuneCompressionSlider(
                        compressionLevel = compressionLevel,
                        codecLabel = telemetryProject.activeCodecLabel(),
                        bitrateMbps = telemetryProject.estimatedBitrateMbps(),
                        onCompressionChange = { newComp ->
                            onQualityParametersChange(resolution, frameRate, newComp, qualityPreset)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (onCharacterStrengthChange != null) {
                        FineTuneSwapStrengthSlider(
                            characterStrength = characterStrength,
                            onStrengthChange = onCharacterStrengthChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas interactive spectrum slider with waveform density bars, magnetic tier nodes, and glowing thumb.
 */
@Composable
private fun CustomFidelitySpectrumTrack(
    fraction: Float,
    activeColor: Color,
    resolution: String,
    frameRate: Long,
    compressionLevel: Long,
    onFractionChange: (Float) -> Unit
) {
    val clampedFraction = fraction.coerceIn(0.05f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianBlack.copy(alpha = 0.85f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .semantics {
                contentDescription =
                    "Adaptive Video Fidelity Slider: $resolution at $frameRate FPS, $compressionLevel percent compression"
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    onFractionChange((offset.x / width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    onFractionChange((change.position.x / width).coerceIn(0f, 1f))
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("adaptive_video_fidelity_slider")
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            val w = size.width
            val h = size.height
            val centerY = h * 0.52f
            val activeX = (w * clampedFraction).coerceIn(0f, w)

            // 1. Background Spectrum Density Bars (28 vertical telemetry ticks)
            val barCount = 28
            val spacing = w / barCount
            for (i in 0 until barCount) {
                val barProgress = i.toFloat() / (barCount - 1)
                val barX = i * spacing + spacing * 0.5f
                val normalizedHeight = 0.25f + 0.55f * barProgress
                val barHeight = h * normalizedHeight
                val isLit = barProgress <= clampedFraction

                val barColor = when {
                    !isLit -> Color(0xFF232C3D)
                    barProgress < 0.33f -> GeminiAmber.copy(alpha = 0.85f)
                    barProgress < 0.66f -> GeminiCyan.copy(alpha = 0.9f)
                    barProgress < 0.86f -> NeonOrange.copy(alpha = 0.95f)
                    else -> GeminiGreen
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX - 2.dp.toPx(), centerY - barHeight / 2f),
                    size = Size(4.dp.toPx(), barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // 2. Base Track Line
            drawLine(
                color = Color(0xFF2B3548),
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3. Active Gradient Track Line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(GeminiAmber, GeminiCyan, NeonOrange, GeminiGreen),
                    startX = 0f,
                    endX = w
                ),
                start = Offset(0f, centerY),
                end = Offset(activeX, centerY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 4. Stepped Tier Stop Nodes along the track
            ProcessingQualityTier.entries.forEach { tier ->
                val nodeX = w * tier.targetProgress
                val reached = clampedFraction >= tier.targetProgress - 0.04f
                drawCircle(
                    color = if (reached) tier.accentColor else Color(0xFF3A465E),
                    radius = 5.dp.toPx(),
                    center = Offset(nodeX, centerY)
                )
                drawCircle(
                    color = ObsidianBlack,
                    radius = 2.dp.toPx(),
                    center = Offset(nodeX, centerY)
                )
            }

            // 5. Glowing Interactive Thumb Handle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(activeX, centerY),
                    radius = 18.dp.toPx()
                ),
                radius = 18.dp.toPx(),
                center = Offset(activeX, centerY)
            )
            drawCircle(
                color = activeColor,
                radius = 10.dp.toPx(),
                center = Offset(activeX, centerY)
            )
            drawCircle(
                color = ObsidianBlack,
                radius = 4.5.dp.toPx(),
                center = Offset(activeX, centerY)
            )
            drawCircle(
                color = WarmIvory,
                radius = 10.dp.toPx(),
                center = Offset(activeX, centerY),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            )
        }
    }
}

@Composable
private fun TelemetryMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            color = MutedIvory.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FineTuneResolutionSlider(
    resolution: String,
    dimensionsLabel: String,
    onResolutionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val idx = FIDELITY_RESOLUTIONS.indexOfFirst { it.equals(resolution, ignoreCase = true) }
        .let { if (it == -1) 2 else it }

    Column(modifier = modifier) {
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Resolution ($dimensionsLabel)",
                    fontSize = 10.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory
                )
            }
            Text(
                text = resolution,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = GeminiCyan
            )
        }
        Slider(
            value = idx.toFloat(),
            onValueChange = { raw ->
                val next = FIDELITY_RESOLUTIONS[raw.roundToInt().coerceIn(0, FIDELITY_RESOLUTIONS.lastIndex)]
                if (next != resolution) onResolutionChange(next)
            },
            valueRange = 0f..FIDELITY_RESOLUTIONS.lastIndex.toFloat(),
            steps = FIDELITY_RESOLUTIONS.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = GeminiCyan,
                activeTrackColor = GeminiCyan,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adaptive_resolution_slider")
        )
    }
}

@Composable
private fun FineTuneFrameRateSlider(
    frameRate: Long,
    onFrameRateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val idx = FIDELITY_FRAMERATES.indexOf(frameRate).let {
        if (it == -1) {
            FIDELITY_FRAMERATES.indices.minByOrNull { i -> abs(FIDELITY_FRAMERATES[i] - frameRate) } ?: 3
        } else it
    }

    Column(modifier = modifier) {
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Frame Rate Cadence",
                    fontSize = 10.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory
                )
            }
            Text(
                text = "$frameRate FPS",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = NeonOrange
            )
        }
        Slider(
            value = idx.toFloat(),
            onValueChange = { raw ->
                val next = FIDELITY_FRAMERATES[raw.roundToInt().coerceIn(0, FIDELITY_FRAMERATES.lastIndex)]
                if (next != frameRate) onFrameRateChange(next)
            },
            valueRange = 0f..FIDELITY_FRAMERATES.lastIndex.toFloat(),
            steps = FIDELITY_FRAMERATES.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adaptive_framerate_slider")
        )
    }
}

@Composable
private fun FineTuneCompressionSlider(
    compressionLevel: Long,
    codecLabel: String,
    bitrateMbps: Float,
    onCompressionChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        compressionLevel <= 25L -> GeminiGreen
        compressionLevel <= 55L -> GeminiAmber
        else -> NeonOrange
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Compression ($codecLabel)",
                    fontSize = 10.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory
                )
            }
            Text(
                text = "$compressionLevel% • ~${String.format(Locale.US, "%.1f", bitrateMbps)} Mbps",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Slider(
            value = compressionLevel.toFloat(),
            onValueChange = { raw ->
                onCompressionChange(raw.roundToInt().toLong().coerceIn(0L, 90L))
            },
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adaptive_compression_slider")
        )
    }
}

@Composable
private fun FineTuneSwapStrengthSlider(
    characterStrength: Long,
    onStrengthChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = NeonOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Neural Identity Swap Blend",
                    fontSize = 10.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory
                )
            }
            Text(
                text = "$characterStrength%",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = NeonOrange
            )
        }
        Slider(
            value = characterStrength.toFloat(),
            onValueChange = { raw ->
                onStrengthChange(raw.roundToInt().toLong().coerceIn(10L, 100L))
            },
            valueRange = 10f..100f,
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = ObsidianBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adaptive_swap_strength_slider")
        )
    }
}
