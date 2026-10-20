package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DimIvory
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.StudioRainbowList
import kotlinx.coroutines.delay

@Composable
fun RecastComparisonPlayer(
    modifier: Modifier = Modifier,
    characterName: String = "Character Reference",
    sourceTitle: String = "Original Shot"
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var currentFrame by remember { mutableLongStateOf(42L) }
    val totalFrames = 450L
    var isPlaying by remember { mutableStateOf(false) }
    var isSideBySideMode by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(33) // ~30 fps
            currentFrame = if (currentFrame >= totalFrames) 1L else currentFrame + 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .animatedRainbowBorder(strokeWidth = 1.6.dp, cornerRadius = 12.dp, glow = isPlaying)
            .padding(12.dp)
    ) {
        // Player Header Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GeminiSparkle(size = 18.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "NEURAL RECAST COMPARISON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = GeminiCyan
                    )
                    Text(
                        text = "$sourceTitle  ➜  $characterName",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )
                }
            }

            IconButton(
                onClick = { isSideBySideMode = !isSideBySideMode },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("toggle_comparison_mode_button")
            ) {
                Icon(
                    imageVector = if (isSideBySideMode) Icons.Default.Splitscreen else Icons.Default.ViewColumn,
                    contentDescription = "Toggle View Mode",
                    tint = NeonOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Screen Canvas Display Area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianBlack)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            if (isSideBySideMode) {
                // Side-by-Side Dual View
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Color(0xFF141416))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCinematicScene(
                                isRecast = false,
                                frameNumber = currentFrame,
                                label = "SOURCE FOOTAGE"
                            )
                        }
                        Text(
                            text = "ORIGINAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedIvory,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxSize()
                            .background(NeonOrange)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Color(0xFF191817))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCinematicScene(
                                isRecast = true,
                                frameNumber = currentFrame,
                                label = characterName
                            )
                        }
                        Text(
                            text = "RECAST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Interactive Split Screen Scrubber
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val x = change.position.x
                                splitFraction = (x / widthPx).coerceIn(0.05f, 0.95f)
                            }
                        }
                ) {
                    val splitX = widthPx * splitFraction

                    // Draw Original on left side
                    drawCinematicScene(
                        isRecast = false,
                        frameNumber = currentFrame,
                        label = "SOURCE FOOTAGE"
                    )

                    // Draw Recast on right side with clipping
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF23170E), Color(0xFF17130F)),
                            startX = splitX,
                            endX = widthPx
                        ),
                        topLeft = Offset(splitX, 0f),
                        size = Size(widthPx - splitX, heightPx)
                    )

                    // Draw recast actor silhouette on right partition
                    drawRecastFigure(splitX, widthPx, heightPx, currentFrame)

                    // Split separator line with AI Studio rainbow gradient
                    drawLine(
                        brush = Brush.verticalGradient(StudioRainbowList),
                        start = Offset(splitX, 0f),
                        end = Offset(splitX, heightPx),
                        strokeWidth = 3.5.dp.toPx()
                    )

                    // Center handle circle with glowing rainbow ring
                    drawCircle(
                        color = Color.White,
                        radius = 12.dp.toPx(),
                        center = Offset(splitX, heightPx / 2f)
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(StudioRainbowList, center = Offset(splitX, heightPx / 2f)),
                        radius = 10.dp.toPx(),
                        center = Offset(splitX, heightPx / 2f)
                    )
                    drawCircle(
                        color = ObsidianBlack,
                        radius = 5.dp.toPx(),
                        center = Offset(splitX, heightPx / 2f)
                    )
                }

                // Split Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BEFORE (ORIGINAL)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory,
                        modifier = Modifier
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "AFTER (RECAST)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange,
                        modifier = Modifier
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Timecode & Frame overlay
            Text(
                text = String.format("FR %03d / %03d • 00:00:%02d", currentFrame, totalFrames, (currentFrame / 30)),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = WarmIvory,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0xDD0C0C0E), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frame-by-Frame Timeline Scrubber
        Slider(
            value = currentFrame.toFloat(),
            onValueChange = { currentFrame = it.toLong() },
            valueRange = 1f..totalFrames.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = NeonOrange,
                activeTrackColor = NeonOrange,
                inactiveTrackColor = CharcoalElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("frame_scrubber_slider")
        )

        // Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { currentFrame = (currentFrame - 1).coerceAtLeast(1L) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Frame",
                        tint = WarmIvory,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonOrange)
                        .testTag("playback_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = ObsidianBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { currentFrame = (currentFrame + 1).coerceAtMost(totalFrames) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Frame",
                        tint = WarmIvory,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "1080p60 • ProRES 422",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MutedIvory
            )
        }
    }
}

private fun DrawScope.drawCinematicScene(
    isRecast: Boolean,
    frameNumber: Long,
    label: String
) {
    val w = size.width
    val h = size.height

    // Background gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = if (isRecast) listOf(Color(0xFF24160D), Color(0xFF14100C)) else listOf(Color(0xFF181A1D), Color(0xFF0F1114))
        )
    )

    // Cinematic lighting beam
    val beamColor = if (isRecast) Color(0x33FF6A00) else Color(0x224A88E8)
    val path = Path().apply {
        moveTo(w * 0.2f, 0f)
        lineTo(w * 0.8f, 0f)
        lineTo(w * 0.9f, h)
        lineTo(w * 0.1f, h)
        close()
    }
    drawPath(path, beamColor)

    // Walking actor silhouette representation
    val animOffset = ((frameNumber % 60) - 30) * 1.5f
    val centerX = w * 0.5f + animOffset
    val centerY = h * 0.52f

    val figureColor = if (isRecast) Color(0xFFFFA040) else Color(0xFF708090)

    // Head
    drawCircle(
        color = figureColor,
        radius = h * 0.09f,
        center = Offset(centerX, centerY - h * 0.22f)
    )
    // Body torso
    drawOval(
        color = figureColor,
        topLeft = Offset(centerX - w * 0.08f, centerY - h * 0.12f),
        size = Size(w * 0.16f, h * 0.38f)
    )
}

private fun DrawScope.drawRecastFigure(splitX: Float, widthPx: Float, heightPx: Float, frameNumber: Long) {
    val animOffset = ((frameNumber % 60) - 30) * 1.5f
    val centerX = widthPx * 0.5f + animOffset
    val centerY = heightPx * 0.52f

    if (centerX > splitX - 40) {
        // High-contrast Recast Head with warm neon rim
        drawCircle(
            color = Color(0xFFFF7A00),
            radius = heightPx * 0.09f,
            center = Offset(centerX, centerY - heightPx * 0.22f)
        )
        // Recast Torso
        drawOval(
            color = Color(0xFFFF9500),
            topLeft = Offset(centerX - widthPx * 0.08f, centerY - heightPx * 0.12f),
            size = Size(widthPx * 0.16f, heightPx * 0.38f)
        )
    }
}
