package com.ahmetbozkan.fartmap.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale and layout conventions (DesignSpecification.md §5). */
@Immutable
object Spacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 48.dp
    val max: Dp = 64.dp

    val screenEdge: Dp = lg
    val cardPadding: Dp = lg
    val listItemGap: Dp = md
    val sheetPadding: Dp = xxl
    val minTouchTarget: Dp = 48.dp
    val topBarHeight: Dp = 56.dp
}

/** Elevation levels (DesignSpecification.md §7). */
@Immutable
object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 2.dp
    val level3: Dp = 4.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
}

/** Shape scale (DesignSpecification.md §6). Primary buttons use [PillShape]. */
internal val FartMapShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

val PillShape = RoundedCornerShape(percent = 50)
