package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Locale

enum class TwoWayConversionMode(
    val key: String,
    val shortTitle: String,
    val badge: String,
    val directionFormula: String,
    val description: String
) {
    FORWARD_RECAST(
        key = "forward_recast",
        shortTitle = "Forward Recast",
        badge = "VIDEO + PHOTO ➔ RECAST",
        directionFormula = "Source Video Motion + Target Reference Photo ➔ Recast Performance",
        description = "Transfers target character identity onto source footage while locking actor motion, lighting, and timing."
    ),
    INVERSE_DRIVE(
        key = "inverse_drive",
        shortTitle = "Inverse Photo Drive",
        badge = "PHOTO + MOTION ➔ VIDEO",
        directionFormula = "Static Portrait Photo + Driving Video/Audio ➔ Animated Scene",
        description = "Animates a still portrait or concept photo using motion vectors and facial visemes extracted from driver footage."
    ),
    DUAL_SWAP(
        key = "dual_swap",
        shortTitle = "Two-Way Role Swap",
        badge = "ACTOR A ⇄ ACTOR B",
        directionFormula = "Subject A Identity ⇄ Subject B Identity (Bidirectional Exchange)",
        description = "Simultaneously swaps identities, expressions, and voice timbres in both directions between two subjects."
    ),
    REVERSE_EXTRACT(
        key = "reverse_extract",
        shortTitle = "Reverse ID Extract",
        badge = "VIDEO ➔ PERSONA SHEET",
        directionFormula = "Source Video Footage ➔ Clean 3D Facial Mesh + Castable Reference + Voice Clone",
        description = "De-recasts and extracts a clean, reusable character reference sheet and voice print from raw video."
    );

    companion object {
        fun fromKey(key: String): TwoWayConversionMode {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: FORWARD_RECAST
        }

        fun fromId(id: String): TwoWayConversionMode = fromKey(id)
    }

    val title: String get() = shortTitle
    val directionBadge: String get() = badge
}

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val credits: Long = 100L,
    val tier: String = "pro",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RecastProject(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val sourceVideoName: String = "",
    val sourceVideoDuration: Long = 15L,
    val sourceVideoResolution: String = "1080p",
    val primaryReferenceName: String = "",
    val secondaryReferencesCount: Long = 0L,
    val characterStrength: Long = 85L,
    val facePriority: Boolean = true,
    val identityConsistency: Long = 90L,
    val lightingAdaptation: Long = 80L,
    val motionFidelity: Long = 95L,
    val edgeRefinement: Long = 85L,
    val temporalConsistency: Long = 90L,
    val qualityPreset: String = "high", // draft, standard, high, max
    val resolution: String = "1080p", // 480p, 720p, 1080p, 1440p, 4K
    val frameRate: Long = 60L, // 24, 30, 48, 60, 120
    val compressionLevel: Long = 22L, // 0% (Lossless) to 90% (Ultra-Light)
    val conversionDirection: String = "forward_recast", // forward_recast, inverse_drive, dual_swap, reverse_extract
    val variantsCount: Long = 1L,
    val seed: Long = 42L,
    val protectBackground: Boolean = true,
    val voiceMode: String = "original", // original, speech_to_speech, tts_replace, hybrid, narration
    val voiceName: String = "Kore",
    val ttsDialogue: String = "",
    val lipSyncStrength: Long = 90L,
    val status: String = "draft", // draft, processing, completed, failed
    val resultVideoTitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun activeConversionMode(): TwoWayConversionMode =
        TwoWayConversionMode.fromKey(conversionDirection)

    fun flippedPolarityProject(): RecastProject {
        val nextMode = when (activeConversionMode()) {
            TwoWayConversionMode.FORWARD_RECAST -> TwoWayConversionMode.INVERSE_DRIVE
            TwoWayConversionMode.INVERSE_DRIVE -> TwoWayConversionMode.FORWARD_RECAST
            TwoWayConversionMode.DUAL_SWAP -> TwoWayConversionMode.REVERSE_EXTRACT
            TwoWayConversionMode.REVERSE_EXTRACT -> TwoWayConversionMode.DUAL_SWAP
        }
        return copy(conversionDirection = nextMode.key)
    }

    fun estimatedBitrateMbps(): Float {
        val baseMbps = when (resolution.uppercase(Locale.ROOT)) {
            "480P" -> 6.0f
            "720P" -> 14.0f
            "1440P" -> 48.0f
            "4K" -> 95.0f
            else -> 26.0f // 1080p
        }
        val fpsMultiplier = (frameRate.coerceIn(24L, 120L).toFloat() / 30f).coerceIn(0.75f, 3.2f)
        val compressionFactor = (100f - compressionLevel.coerceIn(0L, 90L).toFloat() * 0.88f) / 100f
        return ((baseMbps * fpsMultiplier * compressionFactor) * 10f).toInt() / 10f
    }

    fun activeCodecLabel(): String {
        return when {
            compressionLevel <= 10L -> "ProRes 422 HQ • Lossless"
            compressionLevel <= 30L -> "AV1 10-Bit • Cinema Master"
            compressionLevel <= 60L -> "HEVC Main10 • Balanced"
            else -> "H.264 Fast • Low-Latency"
        }
    }

    fun recommendedCodecLabel(): String = activeCodecLabel()

    fun estimatedSizeMbPerMin(): Int {
        val mbps = estimatedBitrateMbps()
        return ((mbps * 60f) / 8f).toInt().coerceAtLeast(8)
    }

    fun resolutionDimensionsLabel(): String {
        return when (resolution.uppercase(Locale.ROOT)) {
            "480P" -> "854×480 SD"
            "720P" -> "1280×720 HD"
            "1440P" -> "2560×1440 QHD"
            "4K" -> "3840×2160 UHD"
            else -> "1920×1080 FHD"
        }
    }
}

@IgnoreExtraProperties
data class PromptExperiment(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val systemInstruction: String = "",
    val promptText: String = "",
    val modelA: String = "gemini-3.8-flash",
    val modelB: String = "gemini-4-argon",
    val temperature: Double = 0.7,
    val topP: Double = 0.95,
    val thinkingLevel: String = "high",
    val version: Long = 1L,
    val responseA: String = "",
    val responseB: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class RecastVariant(
    val id: String,
    val variantIndex: Int,
    val seed: Long,
    val previewLabel: String,
    val characterStrength: Int,
    val lightingStyle: String,
    val scoreFidelity: Float,
    val isFavorite: Boolean = false
)

data class VoicePreset(
    val id: String,
    val name: String,
    val gender: String,
    val tone: String,
    val previewDescription: String
)

data class AudioTrack(
    val name: String,
    val volume: Float = 0.8f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val offsetMs: Long = 0L
)
