package com.ahmetbozkan.fartmap.core.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// planning/design/DesignSpecification.md §3. Roles the spec doesn't define keep the Material 3 defaults.

internal val FartMapLightColors = lightColorScheme(
    primary = Color(0xFFFF8C00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4E2600),
    secondary = Color(0xFF00796B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00332D),
    tertiary = Color(0xFFD97777),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0EBE3),
    onSurfaceVariant = Color(0xFF6B6258),
    outline = Color(0xFFD4CDBF),
    outlineVariant = Color(0xFFE8E2D6),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFCDD2),
    inverseSurface = Color(0xFF2C2C2C),
    inverseOnSurface = Color(0xFFFFFFFF),
)

internal val FartMapDarkColors = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF5C3400),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFEF9A9A),
    onTertiary = Color(0xFF1A1A1A),
    background = Color(0xFF121218),
    onBackground = Color(0xFFF0EBE3),
    surface = Color(0xFF1E1E26),
    onSurface = Color(0xFFF0EBE3),
    surfaceVariant = Color(0xFF2A2A33),
    onSurfaceVariant = Color(0xFF999489),
    outline = Color(0xFF44424A),
    outlineVariant = Color(0xFF33323A),
    error = Color(0xFFEF5350),
    onError = Color(0xFF1A1A1A),
    errorContainer = Color(0xFF5C1010),
)

/** Semantic colors outside the Material scheme (DesignSpecification.md §3 "Semantic Colors"). */
@Immutable
data class SemanticColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val neutral: Color,
    val onNeutral: Color,
)

internal val LightSemanticColors = SemanticColors(
    success = Color(0xFF2E7D32),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF57C00),
    onWarning = Color(0xFFFFFFFF),
    info = Color(0xFF1565C0),
    onInfo = Color(0xFFFFFFFF),
    neutral = Color(0xFF9E9E9E),
    onNeutral = Color(0xFFFFFFFF),
)

internal val DarkSemanticColors = SemanticColors(
    success = Color(0xFF66BB6A),
    onSuccess = Color(0xFF1A1A1A),
    warning = Color(0xFFFFA726),
    onWarning = Color(0xFF1A1A1A),
    info = Color(0xFF42A5F5),
    onInfo = Color(0xFF1A1A1A),
    neutral = Color(0xFF757575),
    onNeutral = Color(0xFFFFFFFF),
)
