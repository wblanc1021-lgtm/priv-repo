package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.repository.StudioConfigRepository
import com.example.ui.components.AdminAndConfigCenterModal
import com.example.ui.components.AiStudioAmbientBackground
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.ControlCenterTab
import com.example.ui.components.GeminiSparkle
import com.example.ui.components.loginSpinningRainbowBorder
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.WarmIvory
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val configRepo = remember { StudioConfigRepository(context) }
    val adminSession by configRepo.adminSession.collectAsState()
    val subscriptionState by configRepo.subscriptionState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfigModal by remember { mutableStateOf(false) }
    var initialModalTab by remember { mutableStateOf(ControlCenterTab.SUBSCRIPTION) }
    var showOwnerVerifyDialog by remember { mutableStateOf(false) }
    var ownerEmailInput by remember { mutableStateOf("") }
    var ownerPasskeyInput by remember { mutableStateOf("") }

    val isOwnerVerified = adminSession.isAdminAuthenticated

    // Attempt silent auto sign-in on launch
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                configRepo.syncWithFirebaseUser(Firebase.auth.currentUser?.email)
                onAuthSuccess()
            },
            onUnauthenticated = {},
            scope = scope
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "authOrb")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRot"
    )

    AiStudioAmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Release Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CharcoalElevated.copy(alpha = 0.88f))
                    .border(1.dp, GeminiCyan.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GeminiSparkle(size = 14.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FRAMESHIFT OS 4.0 • TWO-WAY AI CONVERSION STUDIO",
                    fontSize = 10.sp,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = WarmIvory
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Animated Holographic Core Emblem
            Box(
                modifier = Modifier.size(102.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GeminiCyan, GeminiPurple, GeminiAmber, GeminiGreen, GeminiCyan)
                        ),
                        radius = size.minDimension / 2f - 4.dp.toPx(),
                        center = c,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = GeminiPurple.copy(alpha = 0.22f),
                        radius = size.minDimension / 2.4f + (ringRotation % 10f),
                        center = c
                    )
                }

                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF141B2D), Color(0xFF1E1433))
                            )
                        )
                        .border(1.5.dp, if (isOwnerVerified) GeminiCyan else NeonOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOwnerVerified) Icons.Default.AdminPanelSettings else Icons.Default.SwapHoriz,
                        contentDescription = "FrameShift Two-Way AI Conversion Core",
                        tint = if (isOwnerVerified) GeminiCyan else NeonOrange,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FRAMESHIFT STUDIO",
                fontSize = 26.sp,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.4.sp,
                color = WarmIvory
            )

            Text(
                text = if (isOwnerVerified) {
                    "ROOT ADMIN & FOUNDER PORTAL • ${StudioConfigRepository.ROOT_ADMIN_EMAIL.uppercase()}"
                } else {
                    "BIDIRECTIONAL VIDEO ⇄ PHOTO • ACTOR A ⇄ B • VOICE ⇄ LIP-SYNC"
                },
                fontSize = 10.sp,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isOwnerVerified) GeminiCyan else NeonOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main Login Card with Signature Spinning Rainbow Border (Retained Exclusively on Login!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CharcoalCard)
                    .loginSpinningRainbowBorder(
                        strokeWidth = 1.8.dp,
                        cornerRadius = 18.dp,
                        glow = true
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isOwnerVerified) Icons.Default.VerifiedUser else Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = if (isOwnerVerified) GeminiCyan else NeonOrange,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isOwnerVerified) {
                                        "Founder Admin Unlocked (${StudioConfigRepository.ROOT_ADMIN_EMAIL})"
                                    } else {
                                        "Two-Way AI Conversion & Multi-Device Sync"
                                    },
                                    fontSize = 13.sp,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory
                                )
                                Text(
                                    text = if (isOwnerVerified) {
                                        "Owner Omni-Access • Unlimited Credits • Full Cluster Override"
                                    } else {
                                        "Forward Video Recast ⇄ Inverse Photo Drive • 4K 120fps Mastering"
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = InterFontFamily,
                                    color = MutedIvory
                                )
                            }
                        }

                        AiStudioBadge(
                            text = if (isOwnerVerified) "OWNER ADMIN" else "2-WAY AI"
                        )
                    }

                    // Two-Way Conversion Capabilities Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalElevated)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Video + Photo ➔ Recast  |  Photo + Motion ➔ Video",
                                fontSize = 10.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = GeminiCyan
                            )
                            Text(
                                text = "Plan: ${subscriptionState.stagedTier.title} (${if (subscriptionState.isBillingImplemented) "Active" else "Staged / Unbilled"}) • QUIC Sync",
                                fontSize = 9.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = GeminiGreen
                            )
                        }
                        Text(
                            text = "Configure ➔",
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GeminiAmber,
                            modifier = Modifier.clickable {
                                initialModalTab = if (isOwnerVerified) ControlCenterTab.ADMIN else ControlCenterTab.SUBSCRIPTION
                                showConfigModal = true
                            }
                        )
                    }

                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2D1215))
                                .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontFamily = InterFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Primary Interactive Google Sign-In Button (Mandatory CredentialManager Flow)
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    configRepo.syncWithFirebaseUser(Firebase.auth.currentUser?.email)
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOwnerVerified) GeminiCyan else NeonOrange,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isOwnerVerified) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOwnerVerified) {
                                    "Sign in with Google (Owner Admin)"
                                } else {
                                    "Sign in with Google"
                                },
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    // Pre-Flight Settings Row (Admin button is ONLY shown if owner is verified!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isOwnerVerified) {
                            OutlinedButton(
                                onClick = {
                                    initialModalTab = ControlCenterTab.ADMIN
                                    showConfigModal = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("open_admin_console_from_auth")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = GeminiCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Owner Admin",
                                    fontSize = 11.sp,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmIvory
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                initialModalTab = ControlCenterTab.SUBSCRIPTION
                                showConfigModal = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("open_staged_plans_from_auth")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = GeminiAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Plans, API & Connection",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmIvory
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Discreet Owner-Only Key Unlock (No email or admin details shown to regular users)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        if (isOwnerVerified) {
                            initialModalTab = ControlCenterTab.ADMIN
                            showConfigModal = true
                        } else {
                            showOwnerVerifyDialog = true
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("founder_key_unlock_trigger"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isOwnerVerified) Icons.Default.VerifiedUser else Icons.Default.Key,
                    contentDescription = "Owner Key",
                    tint = if (isOwnerVerified) GeminiCyan else MutedIvory.copy(alpha = 0.55f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isOwnerVerified) {
                        "Owner Session Verified • Tap for Admin Console"
                    } else {
                        "Founder Passkey"
                    },
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily,
                    color = if (isOwnerVerified) GeminiCyan else MutedIvory.copy(alpha = 0.55f)
                )
            }
        }

        if (showOwnerVerifyDialog) {
            AlertDialog(
                onDismissRequest = { showOwnerVerifyDialog = false },
                containerColor = CharcoalCard,
                titleContentColor = WarmIvory,
                textContentColor = MutedIvory,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Owner Identity Verification",
                            fontSize = 16.sp,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Admin settings are private and only visible to the verified studio owner.",
                            fontSize = 12.sp,
                            fontFamily = InterFontFamily,
                            color = MutedIvory
                        )
                        OutlinedTextField(
                            value = ownerEmailInput,
                            onValueChange = { ownerEmailInput = it },
                            label = { Text("Owner Email", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_email_input")
                        )
                        OutlinedTextField(
                            value = ownerPasskeyInput,
                            onValueChange = { ownerPasskeyInput = it },
                            label = { Text("Owner Passkey / PIN", fontSize = 11.sp) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_passkey_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val ok = configRepo.loginAsAdmin(ownerEmailInput, ownerPasskeyInput)
                            if (ok) {
                                showOwnerVerifyDialog = false
                                ownerPasskeyInput = ""
                                Toast.makeText(context, "Owner Admin Verified", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Unauthorized: Owner credentials required", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeminiCyan,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Text(
                            text = "Verify Owner",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOwnerVerifyDialog = false }) {
                        Text("Cancel", color = MutedIvory)
                    }
                }
            )
        }

        AdminAndConfigCenterModal(
            isOpen = showConfigModal,
            initialTab = initialModalTab,
            onDismiss = { showConfigModal = false },
            configRepo = configRepo
        )
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
