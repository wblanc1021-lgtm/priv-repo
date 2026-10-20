package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CharacterCastVault
import com.example.data.model.PreGeneratedCharacter
import com.example.data.model.RecastProject
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DimIvory
import com.example.ui.theme.GeminiAmber
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
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * ElevenLabs-style Pre-Generated Characters Vault Modal & Selector.
 * Allows users to browse, audition sample dialogue, inspect multi-angle stills,
 * and 1-click cast characters into the Recast Studio or Live Zoom/FaceTime meetings.
 */
@Composable
fun CharacterVaultDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentSelectedName: String,
    onSelectCharacter: (PreGeneratedCharacter) -> Unit,
    onLaunchLiveCamWithCharacter: ((PreGeneratedCharacter) -> Unit)? = null
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var currentlyPlayingCharacterId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Cinematic", "Sci-Fi", "Narration", "Corporate / Meeting")

    val filteredCharacters = remember(selectedCategory, searchQuery) {
        CharacterCastVault.stockCharacters.filter { char ->
            val matchesCategory = selectedCategory == "All" || char.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    char.name.contains(searchQuery, ignoreCase = true) ||
                    char.archetype.contains(searchQuery, ignoreCase = true) ||
                    char.accent.contains(searchQuery, ignoreCase = true) ||
                    char.tagline.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        currentlyPlayingCharacterId = null
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        currentlyPlayingCharacterId = null
                    }
                })
                ttsInstance = engine
            }
        }
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    // Real TextToSpeech & Audio Playback Handler
    LaunchedEffect(currentlyPlayingCharacterId) {
        val playingId = currentlyPlayingCharacterId
        if (playingId != null) {
            val char = CharacterCastVault.stockCharacters.find { it.id == playingId }
            if (char != null) {
                val spokenText = char.sampleDialogue.replace(Regex("\\[.*?\\]"), "").trim()
                val pitch = when {
                    char.name.contains("Adam", true) -> 0.76f
                    char.name.contains("Elena", true) -> 1.08f
                    char.name.contains("Freya", true) -> 1.18f
                    char.name.contains("Rachel", true) -> 1.02f
                    char.gender.equals("male", true) -> 0.88f
                    else -> 1.05f
                }
                val rate = when (char.category.lowercase()) {
                    "sci-fi" -> 1.06f
                    "corporate / meeting" -> 1.04f
                    else -> 0.96f
                }
                ttsInstance?.setPitch(pitch)
                ttsInstance?.setSpeechRate(rate)
                ttsInstance?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, char.id)
            }
        } else {
            ttsInstance?.stop()
        }
    }

    Dialog(
        onDismissRequest = {
            ttsInstance?.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianBlack)
                .animatedRainbowBorder(strokeWidth = 2.dp, cornerRadius = 16.dp, glow = true),
            color = ObsidianBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GeminiSparkle(size = 24.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PRE-GENERATED CHARACTER CAST",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = WarmIvory
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "11LABS TIER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Pre-rendered photoreal identities, voice models & multi-angle stills",
                                fontSize = 11.sp,
                                color = MutedIvory
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            ttsInstance?.stop()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WarmIvory)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, archetype, or accent (e.g. Rachel, Scottish, Baritone)...", color = MutedIvory, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        val isSel = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) Color(0xFF1E2840) else CharcoalElevated)
                                .border(
                                    1.dp,
                                    if (isSel) GeminiCyan else BorderSubtle,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) GeminiCyan else WarmIvory
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Characters List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCharacters) { char ->
                        val isSelected = currentSelectedName.contains(char.name, ignoreCase = true)
                        val isPlaying = currentlyPlayingCharacterId == char.id

                        CharacterCastCard(
                            character = char,
                            isSelected = isSelected,
                            isPlaying = isPlaying,
                            onTogglePlay = {
                                currentlyPlayingCharacterId = if (isPlaying) null else char.id
                                if (!isPlaying) {
                                    Toast.makeText(context, "Playing sample voice for ${char.name}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCast = {
                                onSelectCharacter(char)
                                Toast.makeText(context, "Cast ${char.name} as active persona", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            onLaunchLive = if (onLaunchLiveCamWithCharacter != null) {
                                {
                                    onLaunchLiveCamWithCharacter(char)
                                    onDismiss()
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Character Card with avatar canvas portrait, sample dialogue player,
 * and 1-click casting affordances.
 */
@Composable
fun CharacterCastCard(
    character: PreGeneratedCharacter,
    isSelected: Boolean,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onCast: () -> Unit,
    onLaunchLive: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = if (isSelected) GeminiCyan else BorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Character Stylized Avatar Portrait with Distinct Geometric Chroma
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(character.avatarColorA, character.avatarColorB)
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Stylized head silhouette
                        drawCircle(
                            color = Color.White.copy(alpha = 0.9f),
                            radius = 14.dp.toPx(),
                            center = Offset(w / 2f, h * 0.42f)
                        )
                        // Torso silhouette
                        drawCircle(
                            color = Color.White.copy(alpha = 0.65f),
                            radius = 24.dp.toPx(),
                            center = Offset(w / 2f, h * 1.15f)
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(GeminiGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(10.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmIvory
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CharcoalElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = character.gender.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = character.avatarColorA
                            )
                        }
                    }

                    Text(
                        text = character.archetype,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeminiCyan
                    )

                    Text(
                        text = "${character.accent} • ${character.lightingStyle}",
                        fontSize = 10.sp,
                        color = MutedIvory
                    )
                }
            }

            // Stills Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CharcoalElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${character.multiAngleStillsCount} Angles",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GeminiAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tagline
        Text(
            text = character.tagline,
            fontSize = 11.sp,
            color = WarmIvory.copy(alpha = 0.85f),
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Audio Dialogue Box with Play Button and Animated Waveform
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalElevated)
                .clickable { onTogglePlay() }
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) GeminiCyan else ObsidianBlack)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Audition Voice",
                        tint = if (isPlaying) ObsidianBlack else GeminiCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = character.voiceName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiAmber
                        )

                        // Animated Waveform bars when playing
                        if (isPlaying) {
                            Canvas(modifier = Modifier.size(width = 60.dp, height = 14.dp)) {
                                val barCount = 7
                                val barWidth = 4f
                                val spacing = 7f
                                for (i in 0 until barCount) {
                                    val h = (sin(wavePhase + i * 0.9f) * 0.5f + 0.5f) * size.height
                                    val x = i * spacing
                                    drawLine(
                                        color = GeminiCyan,
                                        start = Offset(x, (size.height - h) / 2f),
                                        end = Offset(x, (size.height + h) / 2f),
                                        strokeWidth = barWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "TAP TO LISTEN",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MutedIvory
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = character.sampleDialogue,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = if (isPlaying) GeminiCyan else WarmIvory,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cast Button
            Button(
                onClick = onCast,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) GeminiGreen else GeminiCyan,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("cast_character_${character.id}")
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSelected) "CURRENTLY CAST" else "CAST CHARACTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Test in Live Cam Button
            if (onLaunchLive != null && character.optimalForMeetings) {
                OutlinedButton(
                    onClick = onLaunchLive,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("live_test_${character.id}")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = GeminiViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Cam", fontSize = 11.sp, color = WarmIvory)
                }
            }
        }
    }
}
