package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.AudioTrack
import com.example.data.model.RecastProject
import com.example.data.model.VoicePreset
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory

@Composable
fun AudioTimelinePanel(
    project: RecastProject,
    onProjectChange: (RecastProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val voicePresets = remember {
        listOf(
            VoicePreset("kore", "Kore", "Female", "Cinematic / Warm", "Natural conversational warm female actor"),
            VoicePreset("puck", "Puck", "Male", "Dynamic / Youthful", "Agile expressive delivery for action"),
            VoicePreset("fenrir", "Fenrir", "Male", "Deep / Resonant", "Gravelly theatrical trailer tone"),
            VoicePreset("aoede", "Aoede", "Female", "Melodic / Elegant", "Poised dramatic narrative clarity"),
            VoicePreset("charon", "Charon", "Male", "Authoritative", "Steady documentary anchor style"),
            VoicePreset("zephyr", "Zephyr", "Neutral", "Brisk / Modern", "Clean contemporary vocal presence")
        )
    }

    val audioModes = listOf(
        Pair("original", "Keep Original"),
        Pair("speech_to_speech", "Speech-to-Speech"),
        Pair("tts_replace", "TTS Script"),
        Pair("hybrid", "Hybrid Voice"),
        Pair("narration", "Narration Track")
    )

    var originalVolume by remember { mutableFloatStateOf(0.85f) }
    var voiceVolume by remember { mutableFloatStateOf(1.0f) }
    var ambientVolume by remember { mutableFloatStateOf(0.40f) }

    val context = LocalContext.current
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = java.util.Locale.US
                ttsEngine = engine
            }
        }
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    Column(
        modifier = modifier
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
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Voice Engine",
                    tint = NeonOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEURAL VOICE & AUDIO MIXER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = WarmIvory
                )
            }
            Text(
                text = "${project.lipSyncStrength}% Lip-Sync",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = NeonOrange
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audio Mode Selector
        Text(
            text = "AUDIO RECAST MODE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MutedIvory
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(audioModes) { (modeKey, modeTitle) ->
                val isSelected = project.voiceMode == modeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { onProjectChange(project.copy(voiceMode = modeKey)) },
                    label = { Text(modeTitle, fontSize = 11.sp) },
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

        if (project.voiceMode != "original") {
            Spacer(modifier = Modifier.height(14.dp))

            // Voice Presets
            Text(
                text = "NEURAL ACTOR VOICE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MutedIvory
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(voicePresets) { vp ->
                    val isSelected = project.voiceName == vp.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF331604) else CharcoalElevated)
                            .border(1.dp, if (isSelected) NeonOrange else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onProjectChange(project.copy(voiceName = vp.name)) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = vp.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonOrange else WarmIvory
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = vp.gender,
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                            Text(
                                text = vp.tone,
                                fontSize = 11.sp,
                                color = if (isSelected) WarmIvory else MutedIvory
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dialogue Input if TTS / Hybrid
            if (project.voiceMode == "tts_replace" || project.voiceMode == "hybrid") {
                Text(
                    text = "REPLACEMENT DIALOGUE SCRIPT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedIvory
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = project.ttsDialogue,
                    onValueChange = { onProjectChange(project.copy(ttsDialogue = it)) },
                    placeholder = {
                        Text(
                            "Type dialogue. Supports delivery tags like [intense], [calm], [whispers], [excited]...",
                            color = MutedIvory,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialogue_script_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonOrange,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory,
                        cursorColor = NeonOrange
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Quick Emotion Tag Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("[excited]", "[calm]", "[whispers]", "[intense]").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CharcoalElevated)
                                .clickable {
                                    val newText = if (project.ttsDialogue.isBlank()) "$tag " else "${project.ttsDialogue} $tag "
                                    onProjectChange(project.copy(ttsDialogue = newText))
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = tag, fontSize = 10.sp, color = NeonOrange, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Voice Synthesis Action
                Button(
                    onClick = {
                        val textToSpeak = project.ttsDialogue.ifBlank {
                            "Neural dialogue synthesis test for voice ${project.voiceName} in FrameShift Studio."
                        }.replace(Regex("\\[.*?\\]"), "").trim()
                        val pitch = when (project.voiceName.lowercase()) {
                            "fenrir", "charon" -> 0.75f
                            "kore", "aoede" -> 1.10f
                            "puck" -> 1.05f
                            else -> 0.95f
                        }
                        ttsEngine?.setPitch(pitch)
                        ttsEngine?.setSpeechRate(1.0f)
                        ttsEngine?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "voice_preview")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A1C0E),
                        contentColor = NeonOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("audition_timeline_voice_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audition Voice: ${project.voiceName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lip-Sync Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lip-Sync Conformity", fontSize = 12.sp, color = WarmIvory)
                Text("${project.lipSyncStrength}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = NeonOrange)
            }
            Slider(
                value = project.lipSyncStrength.toFloat(),
                onValueChange = { onProjectChange(project.copy(lipSyncStrength = it.toLong())) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonOrange,
                    activeTrackColor = NeonOrange,
                    inactiveTrackColor = CharcoalElevated
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-Track Timeline Mixer
        Text(
            text = "MULTI-TRACK AUDIO TIMELINE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MutedIvory
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Track 1: Original Production Audio
        TrackControlRow(
            trackName = "Track 1: Production Audio",
            volume = originalVolume,
            onVolumeChange = { originalVolume = it }
        )

        // Track 2: Neural Recast Voice
        TrackControlRow(
            trackName = "Track 2: Recast Voice (${project.voiceName})",
            volume = voiceVolume,
            onVolumeChange = { voiceVolume = it }
        )

        // Track 3: Ambient SFX / Foley
        TrackControlRow(
            trackName = "Track 3: Foley & Ambiance",
            volume = ambientVolume,
            onVolumeChange = { ambientVolume = it }
        )
    }
}

@Composable
private fun TrackControlRow(
    trackName: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (volume > 0.05f) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = null,
            tint = if (volume > 0.05f) NeonOrange else MutedIvory,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = trackName, fontSize = 11.sp, color = WarmIvory)
                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MutedIvory
                )
            }
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonOrange,
                    activeTrackColor = NeonOrange,
                    inactiveTrackColor = CharcoalElevated
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
