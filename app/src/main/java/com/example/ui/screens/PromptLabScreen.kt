package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.gemini.GeminiService
import com.example.data.model.PromptExperiment
import com.example.data.repository.PromptLabRepository
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.AiStudioRainbowButton
import com.example.ui.components.AiStudioThinkingLoader
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.animatedRainbowBorder
import com.example.ui.theme.GeminiAmber
import com.example.data.model.RecastProject
import com.example.data.repository.StudioConfigRepository
import com.example.data.repository.WorkspaceHandoffRepository
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.SystemIntegrationHubBanner
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiViolet
import kotlinx.coroutines.launch

@Composable
fun PromptLabScreen(
    geminiService: GeminiService,
    promptLabRepository: PromptLabRepository,
    currentProject: RecastProject? = null,
    onProjectChange: ((RecastProject) -> Unit)? = null,
    configRepo: StudioConfigRepository? = null,
    workspaceRepo: WorkspaceHandoffRepository? = null,
    onOpenConfigCenter: ((ControlCenterTab) -> Unit)? = null,
    onNavigateToStudio: (() -> Unit)? = null,
    onNavigateToVeo: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var systemInstruction by remember {
        mutableStateOf("You are a cinematic prompt engineer optimizing character swap tokens and camera vector prompts for neural recast.")
    }
    var promptText by remember {
        mutableStateOf("Generate a camera tracking shot with volumetric lighting at sunset, replacing original actor with an android investigator while preserving rim shadows and lens distortion.")
    }

    var modelA by remember { mutableStateOf("gemini-3.8-flash") }
    var modelB by remember { mutableStateOf("gemini-4-argon") }

    var temperature by remember { mutableDoubleStateOf(0.7) }
    var topP by remember { mutableDoubleStateOf(0.95) }
    var thinkingLevel by remember { mutableStateOf("high") }

    var responseA by remember { mutableStateOf("") }
    var responseB by remember { mutableStateOf("") }

    var isRunning by remember { mutableStateOf(false) }
    var iterationVersion by remember { mutableIntStateOf(1) }

    // Version history list for rollback
    val historyList = remember {
        mutableStateListOf(
            Triple(1, "Sunset android tracking shot v1", "Initial baseline recast prompt")
        )
    }

    // Export code modal / tab state
    var selectedExportLang by remember { mutableStateOf("Kotlin (Android)") }
    var showExportSnippet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Studio Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiSparkle(size = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PROMPT ENGINEERING",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = WarmIvory
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LAB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "A/B Model Comparison & High-Thinking Prompt Optimizer",
                            fontSize = 11.sp,
                            color = MutedIvory
                        )
                    }
                }

                AiStudioBadge(text = "AI STUDIO LAB")
            }
        }

        if (currentProject != null && configRepo != null && workspaceRepo != null) {
            item {
                SystemIntegrationHubBanner(
                    moduleName = "Prompt Lab",
                    project = currentProject,
                    configRepo = configRepo,
                    workspaceRepo = workspaceRepo,
                    onOpenConfigCenter = onOpenConfigCenter,
                    primaryBridgeLabel = "Load Project Telemetry",
                    onPrimaryBridgeClick = {
                        promptText = "Optimize character recast for '${currentProject.title}': replace actor in '${currentProject.sourceVideoName}' (${currentProject.sourceVideoResolution}) with '${currentProject.primaryReferenceName}' at ${currentProject.characterStrength}% strength, ${currentProject.qualityPreset.uppercase()} quality, preserving volumetric rim lighting and voice '${currentProject.voiceName}'."
                        configRepo.recordSystemEvent("Prompt Lab", "Injected telemetry from '${currentProject.title}' into Prompt Lab")
                        Toast.makeText(context, "Loaded '${currentProject.title}' parameters into Prompt Lab!", Toast.LENGTH_SHORT).show()
                    },
                    secondaryBridgeLabel = "Push to Veo",
                    onSecondaryBridgeClick = {
                        configRepo.recordOptimizedPrompt(promptText)
                        Toast.makeText(context, "Published prompt to Veo 3 & Studio Bus!", Toast.LENGTH_SHORT).show()
                        onNavigateToVeo?.invoke()
                    }
                )
            }
        }

        // Version Control Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iteration Rev #$iterationVersion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = WarmIvory
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rollback",
                        fontSize = 11.sp,
                        color = NeonOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (iterationVersion > 1) {
                                    iterationVersion--
                                    Toast.makeText(context, "Rolled back to Rev #$iterationVersion", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Already at earliest revision", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(4.dp)
                    )
                    Text(
                        text = "Export Snippet",
                        fontSize = 11.sp,
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showExportSnippet = !showExportSnippet }
                            .padding(4.dp)
                    )
                }
            }
        }

        // System Instruction Editor
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text("SYSTEM INSTRUCTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedIvory)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = systemInstruction,
                    onValueChange = { systemInstruction = it },
                    modifier = Modifier.fillMaxWidth().testTag("system_instruction_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonOrange,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory
                    ),
                    maxLines = 3,
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // User Prompt Editor
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TEST PROMPT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedIvory)

                    // Search Grounding Action
                    Row(
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    Toast.makeText(context, "Searching styles with gemini-3.8-flash...", Toast.LENGTH_SHORT).show()
                                    val grounded = geminiService.searchCinematicStyles(promptText)
                                    promptText = "$promptText\n\n[Grounded Cinematic Reference: $grounded]"
                                }
                            }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Search Grounding", fontSize = 10.sp, color = NeonOrange)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.fillMaxWidth().testTag("user_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonOrange,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory
                    ),
                    maxLines = 4,
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // Parameter Tuning Sliders: Temperature, Top-P, Thinking
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text("MODEL HYPERPARAMETERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedIvory)
                Spacer(modifier = Modifier.height(8.dp))

                // Temperature
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Temperature", fontSize = 11.sp, color = WarmIvory)
                    Text(String.format("%.2f", temperature), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = NeonOrange)
                }
                Slider(
                    value = temperature.toFloat(),
                    onValueChange = { temperature = it.toDouble() },
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange),
                    modifier = Modifier.fillMaxWidth()
                )

                // TopP
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top-P Sampling", fontSize = 11.sp, color = WarmIvory)
                    Text(String.format("%.2f", topP), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = NeonOrange)
                }
                Slider(
                    value = topP.toFloat(),
                    onValueChange = { topP = it.toDouble() },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange),
                    modifier = Modifier.fillMaxWidth()
                )

                // Thinking Mode Toggle for Argon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thinking Mode (Argon)", fontSize = 11.sp, color = WarmIvory)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (thinkingLevel == "high") NeonOrange else CharcoalElevated)
                            .clickable {
                                thinkingLevel = if (thinkingLevel == "high") "low" else "high"
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Thinking: ${thinkingLevel.uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (thinkingLevel == "high") ObsidianBlack else WarmIvory
                        )
                    }
                }
            }
        }

        // Model Comparison Trigger
        item {
            Button(
                onClick = {
                    isRunning = true
                    iterationVersion++
                    scope.launch {
                        // Call Model A
                        responseA = geminiService.evaluatePrompt(
                            modelName = modelA,
                            systemInstruction = systemInstruction,
                            prompt = promptText,
                            temperature = temperature,
                            topP = topP
                        )
                        // Call Model B
                        responseB = geminiService.evaluatePrompt(
                            modelName = modelB,
                            systemInstruction = systemInstruction,
                            prompt = promptText,
                            temperature = temperature,
                            topP = topP,
                            thinkingLevel = thinkingLevel
                        )

                        // Save experiment to Firestore
                        try {
                            promptLabRepository.saveExperiment(
                                PromptExperiment(
                                    title = "Comparison Rev #$iterationVersion",
                                    systemInstruction = systemInstruction,
                                    promptText = promptText,
                                    modelA = modelA,
                                    modelB = modelB,
                                    temperature = temperature,
                                    topP = topP,
                                    thinkingLevel = thinkingLevel,
                                    version = iterationVersion.toLong(),
                                    responseA = responseA,
                                    responseB = responseB
                                )
                            )
                        } catch (e: Exception) {
                            // Non fatal offline fallback
                        }

                        configRepo?.recordOptimizedPrompt(promptText)
                        if (currentProject != null) {
                            workspaceRepo?.updateSharedProject(currentProject)
                        }

                        isRunning = false
                    }
                },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonOrange,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("run_comparison_button")
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianBlack, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EVALUATING RESPONSES...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RUN SIDE-BY-SIDE EVALUATION", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Side-by-side Response Comparison View
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("SIDE-BY-SIDE MODEL EVALUATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedIvory)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Response Panel A
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = modelA,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = responseA.ifBlank { "Awaiting execution..." },
                            fontSize = 10.sp,
                            color = WarmIvory,
                            lineHeight = 14.sp
                        )
                    }

                    // Response Panel B
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "$modelB [HIGH THINKING]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = responseB.ifBlank { "Awaiting execution..." },
                            fontSize = 10.sp,
                            color = WarmIvory,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (configRepo != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                configRepo.recordOptimizedPrompt(promptText)
                                if (currentProject != null && onProjectChange != null) {
                                    val updated = currentProject.copy(
                                        characterStrength = 92L,
                                        qualityPreset = "high"
                                    )
                                    onProjectChange(updated)
                                    workspaceRepo?.updateSharedProject(updated)
                                }
                                Toast.makeText(context, "Applied optimized prompt to Studio Recast & Room DB!", Toast.LENGTH_SHORT).show()
                                onNavigateToStudio?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeminiCyan,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("apply_prompt_to_studio_button")
                        ) {
                            Text("APPLY TO STUDIO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                configRepo.recordOptimizedPrompt(promptText)
                                Toast.makeText(context, "Sent prompt to Veo 3 & Nano 2.1 Studio!", Toast.LENGTH_SHORT).show()
                                onNavigateToVeo?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("send_prompt_to_veo_button")
                        ) {
                            Text("SEND TO VEO 3 / NANO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Deployment-ready Code Snippet Export
        if (showExportSnippet) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141416))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEPLOYMENT-READY CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                        }

                        IconButton(
                            onClick = {
                                val codeSnippet = generateSnippet(selectedExportLang, promptText, systemInstruction, temperature, topP)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("code", codeSnippet))
                                Toast.makeText(context, "Copied code snippet to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonOrange, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Kotlin (Android)", "Python", "cURL", "Node.js").forEach { lang ->
                            val sel = selectedExportLang == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (sel) NeonOrange else CharcoalElevated)
                                    .clickable { selectedExportLang = lang }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) ObsidianBlack else WarmIvory
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = generateSnippet(selectedExportLang, promptText, systemInstruction, temperature, topP),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WarmIvory,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

private fun generateSnippet(
    lang: String,
    prompt: String,
    systemInstruction: String,
    temperature: Double,
    topP: Double
): String {
    return when (lang) {
        "Python" -> """
import google.generativeai as genai
genai.configure(api_key=os.environ["GEMINI_API_KEY"])
model = genai.GenerativeModel(
    model_name="gemini-4-argon",
    system_instruction="${systemInstruction.take(60)}..."
)
response = model.generate_content(
    "${prompt.take(60)}...",
    generation_config={"temperature": $temperature, "top_p": $topP}
)
print(response.text)
        """.trimIndent()
        "cURL" -> """
curl https://generativelanguage.googleapis.com/v1beta/models/gemini-4-argon:generateContent?key=${'$'}GEMINI_API_KEY \
  -H 'Content-Type: application/json' \
  -d '{"contents": [{"parts": [{"text": "${prompt.take(40)}..."}]}], "generationConfig": {"temperature": $temperature}}'
        """.trimIndent()
        "Node.js" -> """
import { GoogleGenerativeAI } from "@google/generative-ai";
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-4-argon" });
const result = await model.generateContent("${prompt.take(50)}...");
console.log(result.response.text());
        """.trimIndent()
        else -> """
// Android Kotlin Retrofit Implementation
val request = GenerateContentRequest(
    contents = listOf(Content(parts = listOf(Part(text = "${prompt.take(40)}...")))),
    generationConfig = GenerationConfig(temperature = ${temperature}f, topP = ${topP}f)
)
val response = RetrofitClient.service.generateContent(apiKey, request)
        """.trimIndent()
    }
}
