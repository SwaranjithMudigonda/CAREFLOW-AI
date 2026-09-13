package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ClinicalTealPrimary,
    onPrimary = ClinicalTealOnPrimary,
    primaryContainer = ClinicalTealContainer,
    onPrimaryContainer = ClinicalTealOnContainer,
    secondary = ClinicalSlateSecondary,
    onSecondary = ClinicalSlateOnSecondary,
    secondaryContainer = ClinicalSlateContainer,
    onSecondaryContainer = ClinicalSlateOnContainer,
    tertiary = ClinicalWarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = ClinicalWarningAmberContainer,
    onTertiaryContainer = ClinicalWarningAmberOnContainer,
    background = ClinicalBackground,
    onBackground = ClinicalOnBackground,
    surface = ClinicalSurface,
    onSurface = ClinicalOnSurface,
    surfaceVariant = ClinicalSurfaceVariant,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant,
    error = MedicalErrorRed,
    onError = Color.White,
    errorContainer = MedicalErrorContainer,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkClinicalTealPrimary,
    onPrimary = DarkClinicalTealOnPrimary,
    primaryContainer = DarkClinicalTealContainer,
    onPrimaryContainer = ClinicalTealContainer,
    secondary = ClinicalSlateContainer,
    onSecondary = ClinicalSlateOnContainer,
    background = DarkClinicalBackground,
    onBackground = DarkClinicalOnSurface,
    surface = DarkClinicalSurface,
    onSurface = DarkClinicalOnSurface,
    surfaceVariant = DarkClinicalSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0C8CC),
    outline = Color(0xFF4E585D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Healthcare apps demand brand color consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun CareFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

