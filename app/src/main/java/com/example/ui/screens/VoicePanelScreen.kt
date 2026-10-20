package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.AudioTimelinePanel
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import java.util.Locale

@Composable
fun VoicePanelScreen(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    onBack: () -> Unit,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    onNavigateToLiveCam: (() -> Unit)? = null
) {
    val context = LocalContext.current
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp).testTag("voice_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WarmIvory)
                }
                Spacer(modifier = Modifier.width(6.dp))
                GeminiSparkle(size = 22.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEURAL VOICE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = WarmIvory
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "STUDIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "High-fidelity speech conversion & dialogue synchronization",
                        fontSize = 11.sp,
                        color = MutedIvory
                    )
                }
            }

            AiStudioBadge(text = "TTS & STS")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (configRepo != null && workspaceRepo != null) {
                item {
                    val apiCfg by configRepo.apiConfig.collectAsState()
                    SystemIntegrationHubBanner(
                        moduleName = "Neural Voice",
                        project = project,
                        configRepo = configRepo,
                        workspaceRepo = workspaceRepo,
                        onOpenConfigCenter = onOpenConfigCenter,
                        primaryBridgeLabel = "Audition Voice",
                        onPrimaryBridgeClick = {
                            val line = project.ttsDialogue.ifBlank {
                                "Optical neural mesh synchronized for ${project.primaryReferenceName} using ${project.voiceName} voice profile."
                            }
                            ttsEngine?.speak(line, TextToSpeech.QUEUE_FLUSH, null, "voice_audition_utt")
                            configRepo.recordSystemEvent("Voice Studio", "Auditioned '${project.voiceName}' TTS synthesis")
                            Toast.makeText(context, "Speaking: ${project.voiceName}", Toast.LENGTH_SHORT).show()
                        },
                        secondaryBridgeLabel = "Live Cam STS",
                        onSecondaryBridgeClick = {
                            workspaceRepo.updateSharedProject(project)
                            onNavigateToLiveCam?.invoke()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Connected ElevenLabs / AI Studio Voice Vault Status Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = GeminiViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "VOICE ENGINE ROUTING: ${if (apiCfg.elevenLabsVoiceKey.isNotBlank()) "Custom ElevenLabs Vault Key" else "AI Studio Neural TTS Bridge"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Active Voice: ${project.voiceName} • Lip-Sync Lock: ${project.lipSyncStrength}%",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GeminiGreen
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val line = project.ttsDialogue.ifBlank {
                                    "Testing live lip-sync and acoustic timbre for ${project.primaryReferenceName}."
                                }
                                ttsEngine?.speak(line, TextToSpeech.QUEUE_FLUSH, null, "voice_preview_btn")
                                Toast.makeText(context, "Playing Live TTS Audition...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiCyan,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test TTS", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                AudioTimelinePanel(
                    project = project,
                    onProjectChange = { updated ->
                        onProjectChange(updated)
                        workspaceRepo?.updateSharedProject(updated)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AiStudioRainbowButton(
            text = "APPLY & SYNC VOICE CONFIGURATION",
            onClick = {
                workspaceRepo?.updateSharedProject(project)
                configRepo?.recordSystemEvent(
                    "Voice Studio",
                    "Committed voice '${project.voiceName}' (${project.lipSyncStrength}% Lip-Sync) to '${project.title}'"
                )
                Toast.makeText(context, "Voice settings synced to Studio, Room DB & Multi-Device Mesh!", Toast.LENGTH_SHORT).show()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("apply_voice_settings_button")
        )
    }
}
