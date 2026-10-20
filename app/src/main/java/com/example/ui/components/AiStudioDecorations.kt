package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.StudioRainbowList
import com.example.ui.theme.WarmIvory
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Clean, non-spinning studio accent brush used for workspace indicators and subtle dividers.
 */
@Composable
fun rememberAnimatedRainbowBrush(
    durationMillis: Int = 4000,
    colors: List<Color> = listOf(
        GeminiCyan.copy(alpha = 0.65f),
        GeminiBlue.copy(alpha = 0.55f),
        NeonOrange.copy(alpha = 0.65f)
    )
): Brush {
    return Brush.horizontalGradient(colors = colors)
}

/**
 * Exclusive to the Login Portal (`AuthScreen`): renders the signature spinning rainbow border
 * with a soft glowing aura.
 */
@Composable
fun Modifier.loginSpinningRainbowBorder(
    strokeWidth: Dp = 1.8.dp,
    cornerRadius: Dp = 16.dp,
    durationMillis: Int = 4200,
    glow: Boolean = true,
    colors: List<Color> = StudioRainbowList
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "login_rainbow_border_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "login_border_angle"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "login_glow_pulse"
    )

    return this
        .drawBehind {
            if (glow) {
                val glowStroke = strokeWidth.toPx() * 3.2f
                val glowBrush = Brush.sweepGradient(
                    colors = colors.map { it.copy(alpha = it.alpha * glowPulse * 0.38f) },
                    center = center
                )
                rotate(angle) {
                    drawRoundRect(
                        brush = glowBrush,
                        topLeft = Offset(-glowStroke / 2, -glowStroke / 2),
                        size = Size(size.width + glowStroke, size.height + glowStroke),
                        cornerRadius = CornerRadius(
                            (cornerRadius + 4.dp).toPx(),
                            (cornerRadius + 4.dp).toPx()
                        ),
                        style = Stroke(width = glowStroke)
                    )
                }
            }
        }
        .drawWithCache {
            val strokePx = strokeWidth.toPx()
            val sweepBrush = Brush.sweepGradient(
                colors = colors,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            onDrawWithContent {
                drawContent()
                rotate(angle) {
                    drawRoundRect(
                        brush = sweepBrush,
                        topLeft = Offset(strokePx / 2, strokePx / 2),
                        size = Size(size.width - strokePx, size.height - strokePx),
                        cornerRadius = CornerRadius(
                            cornerRadius.toPx(),
                            cornerRadius.toPx()
                        ),
                        style = Stroke(width = strokePx)
                    )
                }
            }
        }
}

/**
 * Clean, static glassmorphic studio border used across all workspace screens and components
 * (replacing the spinning rainbow border outside of the Login screen).
 */
@Composable
fun Modifier.studioGlassBorder(
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 14.dp,
    accentColor: Color = GeminiCyan,
    elevatedGlow: Boolean = false
): Modifier {
    return this.drawWithCache {
        val strokePx = strokeWidth.toPx()
        val glassBrush = Brush.linearGradient(
            colors = if (elevatedGlow) {
                listOf(
                    Color.White.copy(alpha = 0.24f),
                    accentColor.copy(alpha = 0.42f),
                    BorderSubtle.copy(alpha = 0.85f),
                    accentColor.copy(alpha = 0.25f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.16f),
                    accentColor.copy(alpha = 0.26f),
                    BorderSubtle.copy(alpha = 0.9f),
                    Color(0xFF1F2430)
                )
            },
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
        onDrawWithContent {
            drawContent()
            drawRoundRect(
                brush = glassBrush,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(size.width - strokePx, size.height - strokePx),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * Workspace border modifier: mapped to clean, non-spinning studio glass border so all
 * internal screens, cards, and modals render with calm, modern precision glass edges.
 */
@Composable
fun Modifier.animatedRainbowBorder(
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 12.dp,
    durationMillis: Int = 4000,
    glow: Boolean = false,
    colors: List<Color> = StudioRainbowList
): Modifier {
    return this.studioGlassBorder(
        strokeWidth = strokeWidth.coerceAtMost(1.2.dp),
        cornerRadius = cornerRadius,
        accentColor = GeminiCyan,
        elevatedGlow = glow
    )
}

/**
 * Reusable Card container wrapped in a clean glassmorphic studio border.
 */
@Composable
fun AiStudioRainbowCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    strokeWidth: Dp = 1.dp,
    backgroundColor: Color = CharcoalCard,
    glow: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .studioGlassBorder(
                strokeWidth = strokeWidth,
                cornerRadius = cornerRadius,
                accentColor = GeminiCyan,
                elevatedGlow = glow
            )
    ) {
        content()
    }
}

/**
 * Modifier that renders a pure black background with a slowly flowing, whisper-subtle shifting
 * AI Studio rainbow micro-hexagon lattice. Large regions of the surface remain pure black while
 * ribbons of tiny rainbow hexagons gently drift across the viewport.
 */
@Composable
fun Modifier.flowingRainbowHexagonBackground(
    hexRadiusDp: Dp = 8.5.dp,
    maxPatternAlpha: Float = 0.17f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "hex_rainbow_flow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hex_flow_phase"
    )
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hex_color_shift"
    )

    return this
        .background(Color(0xFF020306))
        .drawBehind {
            drawFlowingMicroHexagonLattice(
                flowPhase = flowPhase,
                colorShift = colorShift,
                hexRadiusPx = hexRadiusDp.toPx(),
                maxAlpha = maxPatternAlpha
            )
        }
}

/**
 * Draws the subtle flowing AI Studio rainbow micro-hexagon honeycomb pattern over pure black,
 * keeping large areas of the screen completely black and illuminating only the drifting wave streams.
 */
private fun DrawScope.drawFlowingMicroHexagonLattice(
    flowPhase: Float,
    colorShift: Float,
    hexRadiusPx: Float,
    maxAlpha: Float
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    // Deep pitch-black base
    drawRect(color = Color(0xFF020306))

    // Very soft, barely-perceptible ambient under-glow along the flowing ribbon path
    val stream1Center = Offset(
        x = w * (0.5f + 0.36f * cos(flowPhase)),
        y = h * (0.32f + 0.22f * sin(flowPhase * 1.3f))
    )
    val stream2Center = Offset(
        x = w * (0.5f - 0.38f * sin(flowPhase * 0.85f)),
        y = h * (0.72f + 0.20f * cos(flowPhase))
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GeminiBlue.copy(alpha = 0.045f), Color.Transparent),
            center = stream1Center,
            radius = w * 0.52f
        ),
        radius = w * 0.52f,
        center = stream1Center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(GeminiViolet.copy(alpha = 0.04f), Color.Transparent),
            center = stream2Center,
            radius = w * 0.48f
        ),
        radius = w * 0.48f,
        center = stream2Center
    )

    val r = hexRadiusPx.coerceAtLeast(10f)
    val hexHeight = sqrt(3f) * r
    val horizStep = 1.5f * r
    val vertStep = hexHeight

    val cols = (w / horizStep).toInt() + 2
    val rows = (h / vertStep).toInt() + 2
    val strokePx = 0.7.dp.toPx()

    val rainbowPalette = listOf(
        GeminiBlue,
        GeminiCyan,
        GeminiGreen,
        GeminiAmber,
        GeminiCoral,
        GeminiPink,
        GeminiPurple,
        GeminiViolet
    )

    val hexPath = Path()

    for (col in 0..cols) {
        val cx = col * horizStep
        val yOffset = if (col % 2 == 1) vertStep * 0.5f else 0f
        val nx = cx / w

        for (row in 0..rows) {
            val cy = row * vertStep + yOffset
            if (cy < -r || cy > h + r) continue
            val ny = cy / h

            // Two flowing ribbon waves across normalized screen coordinates (nx, ny)
            // Keep ~55% of the screen completely black by thresholding the wave envelope
            val wavePrimary = sin(nx * 5.2f + ny * 3.8f - flowPhase) *
                cos(ny * 4.4f - nx * 2.6f + flowPhase * 0.7f)
            val waveSecondary = cos(nx * 4.0f - ny * 5.0f + flowPhase * 1.15f)

            val combinedWave = (wavePrimary * 0.65f + waveSecondary * 0.35f)
            // Only illuminate cells inside the flowing stream (combinedWave > 0.14f); keep rest pure black
            if (combinedWave <= 0.14f) continue

            val normalizedIntensity = ((combinedWave - 0.14f) / 0.86f).coerceIn(0f, 1f)
            val cellAlpha = (normalizedIntensity * maxAlpha).coerceIn(0.025f, maxAlpha)

            // Smooth shifting AI Studio rainbow color selection per micro-hexagon
            val huePos = ((nx * 1.4f + ny * 1.1f + colorShift * 2f) % 1f).let {
                if (it < 0f) it + 1f else it
            }
            val paletteFloatIdx = huePos * rainbowPalette.size
            val idxA = paletteFloatIdx.toInt() % rainbowPalette.size
            val idxB = (idxA + 1) % rainbowPalette.size
            val frac = paletteFloatIdx - paletteFloatIdx.toInt()
            val cA = rainbowPalette[idxA]
            val cB = rainbowPalette[idxB]

            val cellColor = Color(
                red = cA.red + (cB.red - cA.red) * frac,
                green = cA.green + (cB.green - cA.green) * frac,
                blue = cA.blue + (cB.blue - cA.blue) * frac,
                alpha = cellAlpha
            )

            hexPath.reset()
            val innerR = r * 0.88f
            for (v in 0 until 6) {
                val angleRad = (PI / 3.0 * v).toFloat()
                val vx = cx + innerR * cos(angleRad)
                val vy = cy + innerR * sin(angleRad)
                if (v == 0) {
                    hexPath.moveTo(vx, vy)
                } else {
                    hexPath.lineTo(vx, vy)
                }
            }
            hexPath.close()

            drawPath(
                path = hexPath,
                color = cellColor,
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * Fullscreen ambient background for both the Android App and Webpage viewports:
 * Pure black backdrop with a slowly flowing, subtle shifting AI Studio rainbow micro-hexagon pattern
 * that flows around the screen while keeping large portions of the screen pure black.
 */
@Composable
fun AiStudioAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingRainbowHexagonBackground(
                hexRadiusDp = 8.5.dp,
                maxPatternAlpha = 0.17f
            )
    ) {
        content()
    }
}

/**
 * Precision 4-pointed Star emblem with subtle pulse.
 */
@Composable
fun GeminiSparkle(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_sparkle_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val s = this.size.width * pulse
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        val path = Path().apply {
            val r = s / 2f
            val indent = r * 0.22f
            moveTo(center.x, center.y - r)
            cubicTo(center.x, center.y - indent, center.x + indent, center.y, center.x + r, center.y)
            cubicTo(center.x + indent, center.y, center.x, center.y + indent, center.x, center.y + r)
            cubicTo(center.x, center.y + indent, center.x - indent, center.y, center.x - r, center.y)
            cubicTo(center.x - indent, center.y, center.x, center.y - indent, center.x, center.y - r)
            close()
        }

        val brush = if (tint != null) {
            Brush.linearGradient(listOf(tint, tint))
        } else {
            Brush.linearGradient(
                colors = listOf(GeminiCyan, GeminiBlue, NeonOrange),
                start = Offset(center.x - s, center.y - s),
                end = Offset(center.x + s, center.y + s)
            )
        }

        drawPath(path = path, brush = brush)

        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = s * 0.08f,
            center = center
        )
    }
}

/**
 * Clean Glassmorphic Neural Synthesis & Two-Way Conversion Progress Loader.
 */
@Composable
fun AiStudioThinkingLoader(
    stageText: String = "Synthesizing bidirectional facial topology & motion vectors...",
    progressFraction: Float? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "studio_loader_anim")

    val outerRingRot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ring_rot"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalElevated.copy(alpha = 0.96f))
            .studioGlassBorder(strokeWidth = 1.dp, cornerRadius = 16.dp, accentColor = GeminiCyan, elevatedGlow = true)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                rotate(outerRingRot, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, GeminiCyan, NeonOrange, GeminiCyan),
                            center = center
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            GeminiSparkle(size = 26.dp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GeminiCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TWO-WAY AI CONVERSION PIPELINE",
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = GeminiCyan
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stageText,
            fontSize = 13.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            color = WarmIvory,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BorderSubtle)
        ) {
            val progress = progressFraction ?: 1f
            val baseFillWidth = progress.coerceIn(0.05f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth(baseFillWidth)
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GeminiCyan, GeminiBlue, NeonOrange)
                        )
                    )
                    .drawBehind {
                        val highlightX = size.width * shimmerOffset
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = 10f,
                            center = Offset(highlightX, size.height / 2f)
                        )
                    }
            )
        }

        if (progressFraction != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bidirectional Neural Alignment Active",
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = MutedIvory
                )
                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = GeminiCyan
                )
            }
        }
    }
}

/**
 * Clean Glass Pill Badge with optional sparkle icon.
 */
@Composable
fun AiStudioBadge(
    text: String,
    modifier: Modifier = Modifier,
    iconSparkle: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalElevated)
            .studioGlassBorder(strokeWidth = 1.dp, cornerRadius = 20.dp, accentColor = GeminiCyan)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconSparkle) {
            GeminiSparkle(size = 11.dp)
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            color = WarmIvory,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Clean Glassmorphic Primary Action Button with subtle specular sheen (no spinning rainbow border).
 */
@Composable
fun AiStudioRainbowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingSparkle: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .background(
                Brush.horizontalGradient(
                    colors = if (enabled) {
                        listOf(Color(0xFF19283D), Color(0xFF231D38), Color(0xFF331F24))
                    } else {
                        listOf(CharcoalElevated, CharcoalCard)
                    }
                )
            )
            .studioGlassBorder(
                strokeWidth = 1.2.dp,
                cornerRadius = 12.dp,
                accentColor = if (enabled) NeonOrange else BorderSubtle,
                elevatedGlow = enabled
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingSparkle && enabled) {
                GeminiSparkle(size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                color = if (enabled) WarmIvory else MutedIvory
            )
        }
    }
}
