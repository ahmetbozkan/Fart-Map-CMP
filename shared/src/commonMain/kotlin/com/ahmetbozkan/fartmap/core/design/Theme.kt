package com.ahmetbozkan.fartmap.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

/** The app theme: Material 3 wired to the Fart Map tokens, light and dark (DesignSpecification.md §3–§7). */
@Composable
fun FartMapTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSemanticColors provides if (darkTheme) DarkSemanticColors else LightSemanticColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) FartMapDarkColors else FartMapLightColors,
            typography = FartMapTypography,
            shapes = FartMapShapes,
            content = content,
        )
    }
}

/** Token access beyond Material: `FartMap.semantic.success`. */
object FartMap {
    val semantic: SemanticColors
        @Composable @ReadOnlyComposable
        get() = LocalSemanticColors.current
}
