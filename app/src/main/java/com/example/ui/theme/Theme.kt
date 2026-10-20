package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val FrameShiftDarkColorScheme = darkColorScheme(
    primary = NeonOrange,
    onPrimary = ObsidianBlack,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OnOrangeContainer,
    secondary = AmberAccent,
    onSecondary = ObsidianBlack,
    secondaryContainer = CharcoalElevated,
    onSecondaryContainer = WarmIvory,
    tertiary = ChampagneGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = WarmIvory,
    surface = CharcoalCard,
    onSurface = WarmIvory,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = MutedIvory,
    outline = BorderSubtle,
    error = ErrorRed,
    onError = ObsidianBlack
)

@Composable
fun FrameShiftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrameShiftDarkColorScheme,
        typography = Typography
    ) {
        ProvideTextStyle(
            value = TextStyle(fontFamily = InterFontFamily),
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FrameShiftTheme(content = content)
}
