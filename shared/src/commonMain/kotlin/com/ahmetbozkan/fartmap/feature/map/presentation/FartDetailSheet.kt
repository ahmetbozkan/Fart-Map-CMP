package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.log_visibility_followers
import com.ahmetbozkan.fartmap.resources.log_visibility_private
import com.ahmetbozkan.fartmap.resources.log_visibility_public
import com.ahmetbozkan.fartmap.resources.map_detail_area
import com.ahmetbozkan.fartmap.resources.map_detail_delete
import com.ahmetbozkan.fartmap.resources.map_detail_intensity
import com.ahmetbozkan.fartmap.resources.map_detail_no_words
import com.ahmetbozkan.fartmap.resources.map_detail_title
import com.ahmetbozkan.fartmap.resources.map_detail_title_own
import com.ahmetbozkan.fartmap.resources.map_detail_when
import com.ahmetbozkan.fartmap.resources.map_detail_when_recent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The marker's detail (`FartDetailSheet.md`, Phase 1 variant): flavor, intensity, the words, when, and the coarse
 * area line. An own fart adds its visibility and a Delete action that asks for confirmation (the dialog is the
 * route's, STORY-01-04-05). Other people's farts get no action yet; Report and Block arrive in STORY-01-05-03.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FartDetailSheet(
    content: FartDetailContent,
    onRequestDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    own: ReactionType? = null,
    reactionBreakdown: Map<ReactionType, Int> = emptyMap(),
    onReaction: (ReactionType) -> Unit = {},
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenEdge).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(
                    if (content.isOwn) Res.string.map_detail_title_own else Res.string.map_detail_title,
                    content.flavorLevel,
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = whenText(content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.map_detail_intensity, content.intensityPercent),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = content.description ?: stringResource(Res.string.map_detail_no_words),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(Res.string.map_detail_area),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ReactionBar(own = own, breakdown = reactionBreakdown, onToggle = onReaction)
            if (content.isOwn) {
                Text(
                    text = stringResource(visibilityLabel(content.visibility)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onRequestDelete,
                    modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
                ) {
                    Text(stringResource(Res.string.map_detail_delete))
                }
            }
        }
    }
}

@Composable
private fun whenText(content: FartDetailContent): String =
    if (content.ageUnderHour) {
        stringResource(Res.string.map_detail_when_recent)
    } else {
        stringResource(Res.string.map_detail_when, content.age.agoText())
    }

private fun visibilityLabel(mode: VisibilityMode): StringResource = when (mode) {
    VisibilityMode.PUBLIC -> Res.string.log_visibility_public
    VisibilityMode.FOLLOWERS -> Res.string.log_visibility_followers
    VisibilityMode.PRIVATE -> Res.string.log_visibility_private
}

@Preview
@Composable
private fun FartDetailSheetOtherPreview() = FartMapTheme {
    FartDetailSheetPreviewContent(
        FartDetailContent(
            flavorLevel = 4,
            intensityPercent = 70,
            description = "Bean soup, again.",
            age = FartAge(AgeUnit.HOURS, 2),
            ageUnderHour = false,
            isOwn = false,
            visibility = VisibilityMode.PUBLIC,
        ),
    )
}

@Preview
@Composable
private fun FartDetailSheetOwnNoWordsPreview() = FartMapTheme {
    FartDetailSheetPreviewContent(
        FartDetailContent(
            flavorLevel = 2,
            intensityPercent = 20,
            description = null,
            age = FartAge(AgeUnit.JUST_NOW, 0),
            ageUnderHour = false,
            isOwn = true,
            visibility = VisibilityMode.FOLLOWERS,
        ),
    )
}

@Preview
@Composable
private fun FartDetailSheetRecentOtherPreview() = FartMapTheme {
    FartDetailSheetPreviewContent(
        FartDetailContent(
            flavorLevel = 5,
            intensityPercent = 100,
            description = null,
            age = FartAge(AgeUnit.MINUTES, count = PREVIEW_MINUTES),
            ageUnderHour = true,
            isOwn = false,
            visibility = VisibilityMode.PUBLIC,
        ),
    )
}

private const val PREVIEW_MINUTES = 12

/** Previews render the sheet body without the modal window, which previews can't host. */
@Composable
private fun FartDetailSheetPreviewContent(content: FartDetailContent) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenEdge),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = stringResource(
                if (content.isOwn) Res.string.map_detail_title_own else Res.string.map_detail_title,
                content.flavorLevel,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(text = whenText(content), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = content.description ?: stringResource(Res.string.map_detail_no_words),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
