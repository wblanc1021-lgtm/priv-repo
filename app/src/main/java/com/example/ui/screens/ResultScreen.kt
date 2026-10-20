package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.data.model.RecastVariant
import com.example.ui.components.RecastComparisonPlayer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory

import androidx.compose.runtime.collectAsState
import com.example.data.repository.DevicePlatform
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiViolet

@Composable
fun ResultScreen(
    project: RecastProject,
    variants: List<RecastVariant>,
    onBackToStudio: () -> Unit,
    onSaveProject: () -> Unit,
    onRegenerate: () -> Unit,
    onNavigateToVariants: () -> Unit,
    onNavigateToVoice: () -> Unit,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    onNavigateToLiveCam: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedBitrate by remember { mutableStateOf("15 Mbps (High)") }
    var isFavorited by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToStudio,
                    modifier = Modifier.size(36.dp).testTag("result_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = WarmIvory
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiSparkle(size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RECAST RENDER COMPLETE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = GeminiCyan
                        )
                        Text(
                            text = "${project.title} • Seed ${project.seed}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MutedIvory
                        )
                    }
                }

                IconButton(
                    onClick = {
                        isFavorited = !isFavorited
                        Toast.makeText(context, if (isFavorited) "Saved to Favorites" else "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorited) GeminiAmber else WarmIvory
                    )
                }
            }
        }

        // Unified System Integration Hub Banner
        if (configRepo != null && workspaceRepo != null) {
            item {
                SystemIntegrationHubBanner(
                    moduleName = "Compare & Master",
                    project = project,
                    configRepo = configRepo,
                    workspaceRepo = workspaceRepo,
                    onOpenConfigCenter = onOpenConfigCenter,
                    primaryBridgeLabel = "Send to Live Cam",
                    onPrimaryBridgeClick = {
                        workspaceRepo.updateSharedProject(project)
                        configRepo.recordSystemEvent("Compare & Master", "Promoted '${project.primaryReferenceName}' to Live Cam Virtual Stream")
                        onNavigateToLiveCam?.invoke()
                    },
                    secondaryBridgeLabel = "Takes (${variants.size})",
                    onSecondaryBridgeClick = onNavigateToVariants
                )
            }
        }

        // Comparison Player with Synchronized Scrubber
        item {
            RecastComparisonPlayer(
                characterName = project.primaryReferenceName.ifBlank { "Elena" },
                sourceTitle = project.sourceVideoName
            )
        }

        // Secondary Navigation: Voice Panel & Variant Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Voice Studio Button
                Button(
                    onClick = onNavigateToVoice,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("navigate_voice_studio_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalCard,
                        contentColor = WarmIvory
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Voice Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Variants Grid Button
                Button(
                    onClick = onNavigateToVariants,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("navigate_variants_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalCard,
                        contentColor = WarmIvory
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Takes Grid (${variants.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export & Download Options Card with Rainbow Border
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCard)
                    .animatedRainbowBorder(strokeWidth = 1.4.dp, cornerRadius = 12.dp, glow = false)
                    .padding(14.dp)
            ) {
                Text(
                    text = "MASTER EXPORT (H.264 MP4)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmIvory,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("TARGET BITRATE", fontSize = 10.sp, color = MutedIvory, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("8 Mbps (Standard)", "15 Mbps (High)", "30 Mbps (Master Pro)")) { bitrate ->
                        val sel = selectedBitrate == bitrate
                        FilterChip(
                            selected = sel,
                            onClick = { selectedBitrate = bitrate },
                            label = { Text(bitrate, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GeminiCyan,
                                selectedLabelColor = ObsidianBlack,
                                containerColor = CharcoalElevated,
                                labelColor = WarmIvory
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = sel,
                                borderColor = if (sel) GeminiCyan else BorderSubtle
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AiStudioRainbowButton(
                        text = "Download MP4",
                        onClick = {
                            Toast.makeText(context, "Exporting ${project.resolution} MP4 at $selectedBitrate...", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_video_button")
                    )

                    OutlinedButton(
                        onClick = onSaveProject,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_project_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = WarmIvory, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Project", fontSize = 13.sp, color = WarmIvory)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X (Twitter) & Apple Ecosystem Direct Export Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            configRepo?.recordSystemEvent(
                                "Result Export",
                                "Exported '${project.title}' (${project.resolution} ${project.frameRate}fps) for X (Twitter) Studio"
                            )
                            Toast.makeText(
                                context,
                                "Formatted & queued ${project.resolution} ${project.frameRate}fps master for X (Twitter)!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("result_export_x_button")
                    ) {
                        Text(
                            text = "𝕏 Share to X Studio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            configRepo?.recordSystemEvent(
                                "Result Export",
                                "Packaged '${project.title}' in Apple ProRes 4444 / iCloud Drive format"
                            )
                            Toast.makeText(
                                context,
                                "Packaged Apple ProRes 4444 master for iCloud / Final Cut!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("result_export_apple_button")
                    ) {
                        Text(
                            text = "Apple ProRes / iCloud",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                    }
                }
            }
        }

        // Reproduce / Regenerate with same inputs Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onRegenerate() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Regenerate Shot",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmIvory
                        )
                        Text(
                            text = "Re-render with same settings & reference weights",
                            fontSize = 11.sp,
                            color = MutedIvory
                        )
                    }
                }

                Text(
                    text = "RE-RUN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonOrange
                )
            }
        }
    }
}
