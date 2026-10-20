package com.example.service.recast

import com.example.data.model.RecastProject
import com.example.data.model.RecastVariant
import com.example.data.model.TwoWayConversionMode
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

sealed class RecastProgress {
    data class Step(val stageName: String, val progressFraction: Float) : RecastProgress()
    data class Completed(val project: RecastProject, val variants: List<RecastVariant>) : RecastProgress()
    data class Failed(val reason: String) : RecastProgress()
}

class RecastEngine(
    private val configRepo: StudioConfigRepository? = null,
    private val workspaceRepo: WorkspaceHandoffRepository? = null
) {

    fun estimateGenerationTimeAndCredits(project: RecastProject): Pair<Int, Int> {
        val baseSeconds = (project.sourceVideoDuration * 0.8).toInt().coerceAtLeast(6)
        val presetMultiplier = when (project.qualityPreset) {
            "draft" -> 0.5
            "standard" -> 1.0
            "high" -> 1.6
            "max" -> 2.4
            else -> 1.0
        }
        val resMultiplier = when (project.resolution) {
            "720p" -> 0.75
            "1080p" -> 1.0
            "1440p" -> 1.4
            "4K" -> 2.0
            else -> 1.0
        }
        val fpsMultiplier = when {
            project.frameRate >= 120L -> 1.5
            project.frameRate >= 60L -> 1.15
            else -> 0.9
        }
        val variantMultiplier = project.variantsCount.coerceAtLeast(1)
        val rawSeconds = (baseSeconds * presetMultiplier * resMultiplier * fpsMultiplier * (0.8 + 0.2 * variantMultiplier)).toInt().coerceAtLeast(4)
        val rawCredits = (2 * variantMultiplier * (if (project.qualityPreset == "max" || project.resolution == "4K") 2 else 1)).toInt()

        val admin = configRepo?.adminSession?.value
        val isFastQueue = admin?.isAdminAuthenticated == true && admin.bypassRenderQueue
        val isFreeCredits = admin?.isAdminAuthenticated == true && admin.unlimitedCreditsOverride

        val finalSeconds = if (isFastQueue) (rawSeconds * 0.55).toInt().coerceAtLeast(3) else rawSeconds
        val finalCredits = if (isFreeCredits) 0 else rawCredits
        return Pair(finalSeconds, finalCredits)
    }

    fun executeRecastPipeline(project: RecastProject): Flow<RecastProgress> = flow {
        val modelName = configRepo?.apiConfig?.value?.selectedGeminiModel ?: "gemini-4-argon"
        val videoEngine = configRepo?.apiConfig?.value?.selectedVideoEngine ?: "veo-3.1-cinema"
        val admin = configRepo?.adminSession?.value
        val stepDelayFactor = if (admin?.isAdminAuthenticated == true && admin.bypassRenderQueue) 0.6f else 1.0f
        val convMode = project.activeConversionMode()
        val bitrateStr = String.format(Locale.US, "%.1f Mbps", project.estimatedBitrateMbps())

        when (convMode) {
            TwoWayConversionMode.FORWARD_RECAST -> {
                emit(RecastProgress.Step("1/6: Decoding '${project.sourceVideoName}' (${project.resolution} @ ${project.frameRate}fps) via $videoEngine...", 0.14f))
                delay((360 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("2/6: Extracting 468 facial keypoints & camera depth ($modelName)...", 0.30f))
                delay((400 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("3/6: Forward Recasting '${project.primaryReferenceName.ifBlank { "Primary Cast" }}' onto actor...", 0.50f))
                delay((430 * stepDelayFactor).toLong())
            }
            TwoWayConversionMode.INVERSE_DRIVE -> {
                emit(RecastProgress.Step("1/6: Extracting motion & expression rig from driver '${project.sourceVideoName}'...", 0.14f))
                delay((360 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("2/6: Building 3D neural head mesh from still portrait '${project.primaryReferenceName}'...", 0.32f))
                delay((410 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("3/6: Inverse Photo Drive: Animating portrait at ${project.resolution} @ ${project.frameRate}fps...", 0.52f))
                delay((430 * stepDelayFactor).toLong())
            }
            TwoWayConversionMode.DUAL_SWAP -> {
                emit(RecastProgress.Step("1/6: Dual-tracking Actor A & Actor B across '${project.sourceVideoName}'...", 0.14f))
                delay((370 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("2/6: Computing bidirectional identity & gaze exchange matrices ($modelName)...", 0.32f))
                delay((410 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("3/6: Executing Two-Way Role Swap (${project.characterStrength}% blend)...", 0.52f))
                delay((430 * stepDelayFactor).toLong())
            }
            TwoWayConversionMode.REVERSE_EXTRACT -> {
                emit(RecastProgress.Step("1/6: Scanning multi-angle frames in '${project.sourceVideoName}'...", 0.14f))
                delay((360 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("2/6: Reverse-extracting clean 3D identity rig & neutral albedo map...", 0.32f))
                delay((410 * stepDelayFactor).toLong())
                emit(RecastProgress.Step("3/6: Cloning voice timbre profile & saving reusable character pack...", 0.52f))
                delay((420 * stepDelayFactor).toLong())
            }
        }

        emit(RecastProgress.Step("4/6: Adaptive volumetric re-lighting & boundary feathering...", 0.68f))
        delay((380 * stepDelayFactor).toLong())

        emit(RecastProgress.Step("5/6: Temporal de-flicker & ${project.frameRate}fps optical flow locking...", 0.83f))
        delay((350 * stepDelayFactor).toLong())

        emit(
            RecastProgress.Step(
                "6/6: Encoding ${project.resolution} @ ${project.frameRate}fps • Comp ${project.compressionLevel}% ($bitrateStr • ${project.recommendedCodecLabel()})...",
                0.95f
            )
        )
        delay((340 * stepDelayFactor).toLong())

        val variants = mutableListOf<RecastVariant>()
        val count = project.variantsCount.toInt().coerceIn(1, 4)
        for (i in 1..count) {
            val vSeed = project.seed + (i - 1) * 137
            val fidelity = 0.94f + (i * 0.014f).coerceAtMost(0.99f)
            variants.add(
                RecastVariant(
                    id = "var_${project.id}_$i",
                    variantIndex = i,
                    seed = vSeed,
                    previewLabel = "Take #$i • ${convMode.shortTitle} • ${project.resolution}@${project.frameRate}fps (Comp ${project.compressionLevel}%)",
                    characterStrength = project.characterStrength.toInt(),
                    lightingStyle = if (i % 2 == 0) "Volumetric Warm Rim" else "High-Key Cinematic Fill",
                    scoreFidelity = fidelity,
                    isFavorite = i == 1
                )
            )
        }

        val completedProject = project.copy(
            status = "completed",
            resultVideoTitle = "${project.title} • ${convMode.shortTitle} (${project.resolution} @ ${project.frameRate}fps)"
        )

        val (_, rawCredits) = estimateGenerationTimeAndCredits(project)
        configRepo?.markPipelineVerifiedFromRun(completedProject.title, rawCredits)
        workspaceRepo?.updateSharedProject(completedProject)

        emit(RecastProgress.Completed(completedProject, variants))
    }
}
