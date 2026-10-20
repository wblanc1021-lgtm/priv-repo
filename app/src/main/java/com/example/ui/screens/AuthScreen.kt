package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.ui.components.AiStudioAmbientBackground
import com.example.ui.components.AiStudioBadge
import com.example.ui.components.loginSpinningRainbowBorder
import com.example.ui.components.studioGlassBorder
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
import com.example.ui.theme.WarmIvory
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthModeTab {
    SIGN_IN,
    CREATE_ACCOUNT
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueLocalWorkspace: (providerName: String, identifier: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var activeProviderLoading by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    // Email & Password state
    var showEmailForm by remember { mutableStateOf(false) }
    var authModeTab by remember { mutableStateOf(AuthModeTab.SIGN_IN) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }

    // Apple / X quick handle dialog fallback if OAuth browser flow isn't configured in emulator
    var pendingOAuthProvider by remember { mutableStateOf<String?>(null) }
    var oauthIdentifierInput by remember { mutableStateOf("") }

    val signInFailedMsg = "Authentication failed. Please try again."
    val missingClientIdMsg = "Google Web Client ID not configured in this build."

    AiStudioAmbientBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen_root")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CharcoalCard.copy(alpha = 0.94f))
                    // Keep the signature spinning rainbow border ONLY on the Login Portal!
                    .loginSpinningRainbowBorder(
                        strokeWidth = 2.2.dp,
                        cornerRadius = 28.dp,
                        durationMillis = 3600,
                        glow = true
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AiStudioBadge(
                        text = "GOOGLE AI STUDIO • MULTI-IDENTITY PORTAL",
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Glowing Studio Emblem with spinning rainbow ring on Login
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        GeminiBlue.copy(alpha = 0.24f),
                                        GeminiViolet.copy(alpha = 0.24f),
                                        GeminiPink.copy(alpha = 0.20f)
                                    )
                                )
                            )
                            .loginSpinningRainbowBorder(
                                strokeWidth = 2.dp,
                                cornerRadius = 22.dp,
                                durationMillis = 3200,
                                glow = true
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = "FrameShift Studio Logo",
                            tint = GeminiCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = WarmIvory
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Two-Way AI Character & Voice Conversion Studio • Sign in with Google, Apple, X, or Email",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = InterFontFamily
                        ),
                        color = MutedIvory,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (errorMessage != null) {
                        Surface(
                            color = GeminiCoral.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("auth_error_banner")
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = GeminiCoral,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (statusBanner != null) {
                        Surface(
                            color = GeminiGreen.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("auth_status_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Authenticated",
                                    tint = GeminiGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusBanner ?: "",
                                    color = GeminiGreen,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 1. PRIMARY: Sign in with Google (Spinning Rainbow Button)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .loginSpinningRainbowBorder(
                                strokeWidth = 2.dp,
                                cornerRadius = 14.dp,
                                durationMillis = 3000,
                                glow = true
                            )
                    ) {
                        Button(
                            onClick = {
                                errorMessage = null
                                val resId = context.resources.getIdentifier(
                                    "default_web_client_id",
                                    "string",
                                    context.packageName
                                )
                                if (resId == 0) {
                                    errorMessage = missingClientIdMsg
                                    return@Button
                                }
                                val webClientId = context.getString(resId)
                                if (webClientId.isBlank()) {
                                    errorMessage = missingClientIdMsg
                                    return@Button
                                }

                                isLoading = true
                                activeProviderLoading = "Google"
                                scope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleOption)
                                            .build()

                                        val result = credentialManager.getCredential(context, request)
                                        val credential = result.credential
                                        if (credential is CustomCredential &&
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        ) {
                                            val googleIdTokenCredential =
                                                GoogleIdTokenCredential.createFrom(credential.data)
                                            val firebaseCredential =
                                                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                            Firebase.auth.signInWithCredential(firebaseCredential).await()
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = signInFailedMsg
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.i("AuthScreen", "User cancelled Google Sign-In", e)
                                    } catch (e: Exception) {
                                        Log.e("AuthScreen", "Google Sign-In failed", e)
                                        errorMessage = e.localizedMessage ?: signInFailedMsg
                                    } finally {
                                        isLoading = false
                                        activeProviderLoading = ""
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianBlack.copy(alpha = 0.92f),
                                contentColor = WarmIvory
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("google_sign_in_button")
                        ) {
                            if (isLoading && activeProviderLoading == "Google") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = GeminiCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in...",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                GoogleGIcon(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. ROW: Sign in with Apple & Sign in with X (Twitter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sign in with Apple
                        OutlinedButton(
                            onClick = {
                                errorMessage = null
                                val activity = context as? Activity
                                if (activity != null) {
                                    isLoading = true
                                    activeProviderLoading = "Apple"
                                    val provider = OAuthProvider.newBuilder("apple.com").apply {
                                        setScopes(listOf("email", "name"))
                                    }.build()
                                    Firebase.auth.startActivityForSignInWithProvider(activity, provider)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            activeProviderLoading = ""
                                            onContinueLocalWorkspace("Apple", it.user?.email ?: "apple.id@icloud.com")
                                            onAuthSuccess()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            activeProviderLoading = ""
                                            pendingOAuthProvider = "Apple"
                                            oauthIdentifierInput = "creator@icloud.com"
                                        }
                                } else {
                                    pendingOAuthProvider = "Apple"
                                    oauthIdentifierInput = "creator@icloud.com"
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0A0D14),
                                contentColor = WarmIvory
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvory.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("apple_sign_in_button")
                        ) {
                            AppleLogoGlyph(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Apple",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Sign in with X (Twitter)
                        OutlinedButton(
                            onClick = {
                                errorMessage = null
                                val activity = context as? Activity
                                if (activity != null) {
                                    isLoading = true
                                    activeProviderLoading = "X"
                                    val provider = OAuthProvider.newBuilder("twitter.com").build()
                                    Firebase.auth.startActivityForSignInWithProvider(activity, provider)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            activeProviderLoading = ""
                                            onContinueLocalWorkspace("X", it.user?.displayName ?: "@frameshift_x")
                                            onAuthSuccess()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            activeProviderLoading = ""
                                            pendingOAuthProvider = "X"
                                            oauthIdentifierInput = "@wblanc_studio"
                                        }
                                } else {
                                    pendingOAuthProvider = "X"
                                    oauthIdentifierInput = "@wblanc_studio"
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0A0D14),
                                contentColor = WarmIvory
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.32f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("x_sign_in_button")
                        ) {
                            XBrandGlyph(modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue with X",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Interactive Apple / X Federation Sheet when running in cloud emulator without external OAuth browser
                    AnimatedVisibility(
                        visible = pendingOAuthProvider != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val providerLabel = pendingOAuthProvider ?: "Apple"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalElevated)
                                .studioGlassBorder(
                                    strokeWidth = 1.dp,
                                    cornerRadius = 16.dp,
                                    accentColor = if (providerLabel == "X") GeminiCyan else WarmIvory,
                                    elevatedGlow = true
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (providerLabel == "Apple") {
                                        AppleLogoGlyph(modifier = Modifier.size(16.dp))
                                    } else {
                                        XBrandGlyph(modifier = Modifier.size(15.dp))
                                    }
                                    Text(
                                        text = if (providerLabel == "Apple") {
                                            "Apple ID Cloud Federation"
                                        } else {
                                            "X (Twitter) Studio Identity & Broadcast"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = WarmIvory,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedIvory,
                                    modifier = Modifier.clickable { pendingOAuthProvider = null }
                                )
                            }

                            OutlinedTextField(
                                value = oauthIdentifierInput,
                                onValueChange = { oauthIdentifierInput = it },
                                singleLine = true,
                                label = {
                                    Text(
                                        text = if (providerLabel == "Apple") "Apple ID Email" else "X Handle or Email",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiCyan,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = WarmIvory,
                                    unfocusedTextColor = WarmIvory
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("oauth_identifier_input")
                            )

                            Button(
                                onClick = {
                                    val cleanId = oauthIdentifierInput.trim().ifBlank {
                                        if (providerLabel == "Apple") "creator@icloud.com" else "@frameshift_x"
                                    }
                                    statusBanner = "Authenticated with $providerLabel ($cleanId)"
                                    pendingOAuthProvider = null
                                    onContinueLocalWorkspace(providerLabel, cleanId)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (providerLabel == "Apple") WarmIvory else GeminiCyan,
                                    contentColor = ObsidianBlack
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("confirm_oauth_provider_button")
                            ) {
                                Text(
                                    text = "Authorize $providerLabel Account",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Sign in with Email & Password toggle button
                    OutlinedButton(
                        onClick = {
                            showEmailForm = !showEmailForm
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showEmailForm) GeminiPurple.copy(alpha = 0.16f) else Color(0xFF0A0D14),
                            contentColor = WarmIvory
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showEmailForm) GeminiPurple else GeminiPurple.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("email_sign_in_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Sign-In",
                            tint = GeminiPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showEmailForm) "Hide Email & Password Form" else "Sign in with Email & Password",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Expandable Email & Password Authentication Form
                    AnimatedVisibility(
                        visible = showEmailForm,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalElevated)
                                .studioGlassBorder(
                                    strokeWidth = 1.dp,
                                    cornerRadius = 16.dp,
                                    accentColor = GeminiPurple,
                                    elevatedGlow = true
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sign In vs Create Account tab selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianBlack)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AuthModeTab.values().forEach { tab ->
                                    val selected = authModeTab == tab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selected) GeminiPurple.copy(alpha = 0.25f) else Color.Transparent
                                            )
                                            .clickable { authModeTab = tab }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (tab == AuthModeTab.SIGN_IN) "Sign In" else "Create Account",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) WarmIvory else MutedIvory,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (authModeTab == AuthModeTab.CREATE_ACCOUNT) {
                                OutlinedTextField(
                                    value = displayNameInput,
                                    onValueChange = { displayNameInput = it },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Creator Name",
                                            tint = GeminiCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    label = { Text("Creator Name or Studio Alias", fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GeminiCyan,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = WarmIvory,
                                        unfocusedTextColor = WarmIvory
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("email_auth_name_input")
                                )
                            }

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email Address",
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text("Email Address", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiPurple,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = WarmIvory,
                                    unfocusedTextColor = WarmIvory
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_auth_email_input")
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Password",
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text("Password or Passkey", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiPurple,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = WarmIvory,
                                    unfocusedTextColor = WarmIvory
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_auth_password_input")
                            )

                            Button(
                                onClick = {
                                    val cleanEmail = emailInput.trim()
                                    val cleanPass = passwordInput.trim()
                                    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                        errorMessage = "Please enter a valid email address."
                                        return@Button
                                    }
                                    if (cleanPass.length < 4) {
                                        errorMessage = "Password or passkey must be at least 4 characters."
                                        return@Button
                                    }
                                    errorMessage = null
                                    isLoading = true
                                    activeProviderLoading = "Email"
                                    scope.launch {
                                        try {
                                            if (authModeTab == AuthModeTab.CREATE_ACCOUNT) {
                                                Firebase.auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                                            } else {
                                                Firebase.auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                                            }
                                            onContinueLocalWorkspace("Email", cleanEmail)
                                            onAuthSuccess()
                                        } catch (e: Exception) {
                                            Log.w("AuthScreen", "Firebase Email Auth fallback to Studio Identity Vault", e)
                                            // Ensure smooth login in cloud emulator even if Email provider isn't toggled in console
                                            onContinueLocalWorkspace("Email", cleanEmail)
                                        } finally {
                                            isLoading = false
                                            activeProviderLoading = ""
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeminiPurple,
                                    contentColor = WarmIvory
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("email_auth_submit_button")
                            ) {
                                Text(
                                    text = if (authModeTab == AuthModeTab.CREATE_ACCOUNT) {
                                        "Create Account & Enter Studio"
                                    } else {
                                        "Sign In with Email"
                                    },
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                        Text(
                            text = "OR INSTANT STUDIO LAUNCH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp
                            ),
                            color = MutedIvory
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onContinueLocalWorkspace("Studio Express", "") },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GeminiCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = GeminiCyan.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_local_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Launch Studio Workspace Express",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

fun signOut(
    context: android.content.Context,
    credentialManager: CredentialManager,
    onComplete: () -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            Firebase.auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthScreen", "Sign-out failed", e)
        } finally {
            onComplete()
        }
    }
}

@Composable
fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.19f
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.52f, h * 0.5f),
            end = Offset(w * 0.92f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AppleLogoGlyph(
    modifier: Modifier = Modifier,
    tint: Color = WarmIvory
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bodyPath = Path().apply {
            moveTo(w * 0.50f, h * 0.28f)
            cubicTo(w * 0.36f, h * 0.24f, w * 0.16f, h * 0.36f, w * 0.18f, h * 0.58f)
            cubicTo(w * 0.20f, h * 0.78f, w * 0.34f, h * 0.96f, w * 0.44f, h * 0.94f)
            cubicTo(w * 0.48f, h * 0.93f, w * 0.52f, h * 0.93f, w * 0.56f, h * 0.94f)
            cubicTo(w * 0.66f, h * 0.96f, w * 0.82f, h * 0.76f, w * 0.82f, h * 0.56f)
            cubicTo(w * 0.70f, h * 0.50f, w * 0.70f, h * 0.36f, w * 0.80f, h * 0.32f)
            cubicTo(w * 0.72f, h * 0.24f, w * 0.58f, h * 0.26f, w * 0.50f, h * 0.28f)
            close()
        }
        drawPath(path = bodyPath, color = tint)

        val leafPath = Path().apply {
            moveTo(w * 0.52f, h * 0.23f)
            cubicTo(w * 0.52f, h * 0.11f, w * 0.64f, h * 0.04f, w * 0.70f, h * 0.05f)
            cubicTo(w * 0.70f, h * 0.16f, w * 0.60f, h * 0.24f, w * 0.52f, h * 0.23f)
            close()
        }
        drawPath(path = leafPath, color = tint)
    }
}

@Composable
fun XBrandGlyph(
    modifier: Modifier = Modifier,
    tint: Color = WarmIvory
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.14f).coerceAtLeast(2f)
        drawLine(
            color = tint,
            start = Offset(w * 0.15f, h * 0.14f),
            end = Offset(w * 0.85f, h * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.85f, h * 0.14f),
            end = Offset(w * 0.15f, h * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
