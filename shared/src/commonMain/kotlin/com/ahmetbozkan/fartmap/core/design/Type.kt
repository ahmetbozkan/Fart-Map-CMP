package com.ahmetbozkan.fartmap.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// planning/design/DesignSpecification.md §4. Inter / JetBrains Mono are not bundled yet (owner action in STATE):
// the platform default family carries the spec's sizes, weights, line heights and letter spacing until they are.
private val Primary = FontFamily.Default

private fun style(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: TextUnit = 0.sp) = TextStyle(
    fontFamily = Primary,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing,
)

internal val FartMapTypography = Typography(
    displayLarge = style(FontWeight.Bold, 57, 64, (-0.25).sp),
    displayMedium = style(FontWeight.Bold, 45, 52),
    displaySmall = style(FontWeight.SemiBold, 36, 44),
    headlineLarge = style(FontWeight.SemiBold, 32, 40),
    headlineMedium = style(FontWeight.SemiBold, 28, 36),
    headlineSmall = style(FontWeight.Medium, 24, 32),
    titleLarge = style(FontWeight.Medium, 22, 28),
    titleMedium = style(FontWeight.Medium, 16, 24, 0.15.sp),
    titleSmall = style(FontWeight.Medium, 14, 20, 0.1.sp),
    bodyLarge = style(FontWeight.Normal, 16, 24, 0.5.sp),
    bodyMedium = style(FontWeight.Normal, 14, 20, 0.25.sp),
    bodySmall = style(FontWeight.Normal, 12, 16, 0.4.sp),
    labelLarge = style(FontWeight.Medium, 14, 20, 0.1.sp),
    labelMedium = style(FontWeight.Medium, 12, 16, 0.5.sp),
    labelSmall = style(FontWeight.Medium, 11, 16, 0.5.sp),
)

/** Numeric data (streaks, XP, ranks): monospaced digits so numbers don't shift as they change (§4). */
val NumericFontFamily: FontFamily = FontFamily.Monospace
