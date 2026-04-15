package com.example.hiroad_aws.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build

private val AwsLightColorScheme = lightColorScheme(
    primary = AwsOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF4E5),
    onPrimaryContainer = AwsSquidInk,
    secondary = AwsLinkBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F0FA),
    onSecondaryContainer = Color(0xFF0A4A6A),
    tertiary = AwsSquidInk,
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = AwsPageBackground,
    onBackground = AwsTextPrimary,
    surface = AwsPageBackground,
    onSurface = AwsTextPrimary,
    surfaceVariant = Color(0xFFE0E3E8),
    onSurfaceVariant = AwsTextSecondary,
    outline = AwsOutline,
    outlineVariant = Color(0xFFC5C9CE),
    surfaceContainerLowest = AwsSurface,
    surfaceContainerLow = Color(0xFFF7F8F8),
    surfaceContainer = Color(0xFFF0F1F2),
    surfaceContainerHigh = Color(0xFFE8E9EB),
    surfaceContainerHighest = AwsSurface,
    inverseSurface = AwsSquidInk,
    inverseOnSurface = Color.White,
    inversePrimary = AwsOrangePressed,
)

private val AwsDarkColorScheme = darkColorScheme(
    primary = AwsOrange,
    onPrimary = AwsSquidInk,
    primaryContainer = Color(0xFF4A3300),
    onPrimaryContainer = Color(0xFFFFE4B8),
    secondary = Color(0xFF6BC9FF),
    onSecondary = Color(0xFF003547),
    secondaryContainer = Color(0xFF004D6B),
    onSecondaryContainer = Color(0xFFC5E8FF),
    tertiary = Color(0xFFB8C7D9),
    onTertiary = AwsSquidInk,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF12161C),
    onBackground = Color(0xFFE0E3E8),
    surface = Color(0xFF12161C),
    onSurface = Color(0xFFE0E3E8),
    surfaceVariant = Color(0xFF3E4750),
    onSurfaceVariant = Color(0xFFB8C0C9),
    outline = Color(0xFF8C959E),
    outlineVariant = Color(0xFF3E4750),
    surfaceContainerLowest = Color(0xFF0D1116),
    surfaceContainerLow = Color(0xFF1A1F26),
    surfaceContainer = Color(0xFF1E242C),
    surfaceContainerHigh = Color(0xFF282E36),
    surfaceContainerHighest = Color(0xFF333A43),
    inverseSurface = AwsSquidInk,
    inverseOnSurface = Color.White,
    inversePrimary = AwsOrange,
)

@Composable
fun HiRoad_AWSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        darkTheme -> AwsDarkColorScheme
        else -> AwsLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
