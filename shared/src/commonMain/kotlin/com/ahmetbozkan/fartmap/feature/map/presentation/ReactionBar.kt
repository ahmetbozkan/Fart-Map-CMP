package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.reaction_chip
import com.ahmetbozkan.fartmap.resources.reaction_chip_description
import com.ahmetbozkan.fartmap.resources.reaction_dead
import com.ahmetbozkan.fartmap.resources.reaction_legend
import com.ahmetbozkan.fartmap.resources.reaction_not_selected
import com.ahmetbozkan.fartmap.resources.reaction_rip
import com.ahmetbozkan.fartmap.resources.reaction_selected
import com.ahmetbozkan.fartmap.resources.reaction_smelled
import com.ahmetbozkan.fartmap.resources.reaction_toxic
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The five reactions of a fart (SOCIAL-01). Tapping the active one removes it, tapping another replaces it. The
 * selected chip is announced as "your reaction" and drawn selected, so no meaning rests on color alone. Counts are the
 * parent doc's per-type numbers ([breakdown]), never computed here. Stateless: the route owns [own] and [onToggle].
 */
@Composable
fun ReactionBar(
    own: ReactionType?,
    breakdown: Map<ReactionType, Int>,
    onToggle: (ReactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ReactionType.entries.forEach { type ->
            val label = stringResource(type.labelRes())
            val count = breakdown[type] ?: 0
            val selected = type == own
            val selectedRes = if (selected) Res.string.reaction_selected else Res.string.reaction_not_selected
            val description = stringResource(
                Res.string.reaction_chip_description,
                label,
                count,
                stringResource(selectedRes),
            )
            FilterChip(
                selected = selected,
                onClick = { onToggle(type) },
                label = { Text(stringResource(Res.string.reaction_chip, label, count)) },
                modifier = Modifier
                    .heightIn(min = Spacing.minTouchTarget)
                    .semantics { contentDescription = description },
            )
        }
    }
}

private fun ReactionType.labelRes(): StringResource = when (this) {
    ReactionType.DEAD -> Res.string.reaction_dead
    ReactionType.TOXIC -> Res.string.reaction_toxic
    ReactionType.RIP -> Res.string.reaction_rip
    ReactionType.SMELLED -> Res.string.reaction_smelled
    ReactionType.LEGEND -> Res.string.reaction_legend
}

@Preview
@Composable
private fun ReactionBarPreview() = FartMapTheme {
    ReactionBar(
        own = ReactionType.TOXIC,
        breakdown = mapOf(
            ReactionType.DEAD to PREVIEW_DEAD,
            ReactionType.TOXIC to PREVIEW_TOXIC,
            ReactionType.LEGEND to PREVIEW_LEGEND,
        ),
        onToggle = {},
    )
}

private const val PREVIEW_DEAD = 3
private const val PREVIEW_TOXIC = 1
private const val PREVIEW_LEGEND = 7
