package com.fakesteps.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF90CAF9),
    secondary = androidx.compose.ui.graphics.Color(0xFFCE93D8),
    tertiary = androidx.compose.ui.graphics.Color(0xFF80CBC4),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1A237E),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFBBDEFB)
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1565C0),
    secondary = androidx.compose.ui.graphics.Color(0xFF7B1FA2),
    tertiary = androidx.compose.ui.graphics.Color(0xFF00897B),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFBBDEFB),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF0D47A1)
)

@Composable
fun FakeStepsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        typography = Typography(),
        content = content
    )
}
