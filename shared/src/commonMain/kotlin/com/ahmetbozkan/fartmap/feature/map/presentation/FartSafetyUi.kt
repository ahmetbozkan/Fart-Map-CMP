package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.safety_block_body
import com.ahmetbozkan.fartmap.resources.safety_block_cancel
import com.ahmetbozkan.fartmap.resources.safety_block_confirm
import com.ahmetbozkan.fartmap.resources.safety_block_title
import com.ahmetbozkan.fartmap.resources.safety_reason_harassment
import com.ahmetbozkan.fartmap.resources.safety_reason_illegal
import com.ahmetbozkan.fartmap.resources.safety_reason_minor_safety
import com.ahmetbozkan.fartmap.resources.safety_reason_other
import com.ahmetbozkan.fartmap.resources.safety_reason_private_info
import com.ahmetbozkan.fartmap.resources.safety_reason_sexual
import com.ahmetbozkan.fartmap.resources.safety_reason_spam
import com.ahmetbozkan.fartmap.resources.safety_report_body
import com.ahmetbozkan.fartmap.resources.safety_report_done
import com.ahmetbozkan.fartmap.resources.safety_report_submit
import com.ahmetbozkan.fartmap.resources.safety_report_thanks_body
import com.ahmetbozkan.fartmap.resources.safety_report_thanks_title
import com.ahmetbozkan.fartmap.resources.safety_report_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val reasonLabels: List<Pair<ReportReason, StringResource>> = listOf(
    ReportReason.HARASSMENT to Res.string.safety_reason_harassment,
    ReportReason.PRIVATE_INFO to Res.string.safety_reason_private_info,
    ReportReason.MINOR_SAFETY to Res.string.safety_reason_minor_safety,
    ReportReason.SEXUAL to Res.string.safety_reason_sexual,
    ReportReason.ILLEGAL to Res.string.safety_reason_illegal,
    ReportReason.SPAM to Res.string.safety_reason_spam,
    ReportReason.OTHER to Res.string.safety_reason_other,
)

/**
 * The report flow over the detail sheet (MOD-01): pick a reason, send, then a private thank-you. Shown for
 * [SafetyStep.REPORT_PICK] and [SafetyStep.REPORT_SENT]; no purchase prompt ever appears here (Monetization.md §6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportReasonSheet(state: FartSafetyState, onAction: (FartSafetyAction) -> Unit, modifier: Modifier = Modifier) {
    ModalBottomSheet(onDismissRequest = { onAction(FartSafetyAction.Dismiss) }, modifier = modifier) {
        ReportReasonContent(state = state, onAction = onAction)
    }
}

@Composable
private fun ReportReasonContent(state: FartSafetyState, onAction: (FartSafetyAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenEdge).padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (state.step == SafetyStep.REPORT_SENT) {
            Text(
                text = stringResource(Res.string.safety_report_thanks_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(Res.string.safety_report_thanks_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = { onAction(FartSafetyAction.Dismiss) },
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(stringResource(Res.string.safety_report_done))
            }
        } else {
            Text(
                text = stringResource(Res.string.safety_report_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(Res.string.safety_report_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.selectableGroup()) {
                reasonLabels.forEach { (reason, label) ->
                    ReasonRow(
                        label = stringResource(label),
                        selected = state.reason == reason,
                        onSelect = { onAction(FartSafetyAction.PickReason(reason)) },
                    )
                }
            }
            Button(
                onClick = { onAction(FartSafetyAction.SubmitReport) },
                enabled = state.reason != null,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(stringResource(Res.string.safety_report_submit))
            }
        }
    }
}

@Composable
private fun ReasonRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // The row carries the click and the selected state; the radio is decoration for it.
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** The block confirmation (PRIV-01). Says plainly that unblocking waits for Settings (EPIC-01-07). */
@Composable
fun BlockUserDialog(onAction: (FartSafetyAction) -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = { onAction(FartSafetyAction.Dismiss) },
        modifier = modifier,
        title = { Text(stringResource(Res.string.safety_block_title)) },
        text = { Text(stringResource(Res.string.safety_block_body)) },
        confirmButton = {
            TextButton(
                onClick = { onAction(FartSafetyAction.ConfirmBlock) },
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(stringResource(Res.string.safety_block_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(FartSafetyAction.Dismiss) },
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(stringResource(Res.string.safety_block_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun ReportReasonPickPreview() = FartMapTheme {
    ReportReasonContent(
        state = FartSafetyState(step = SafetyStep.REPORT_PICK, fartId = "f", authorUid = "a"),
        onAction = {},
    )
}

@Preview
@Composable
private fun ReportReasonPickedPreview() = FartMapTheme {
    ReportReasonContent(
        state = FartSafetyState(
            step = SafetyStep.REPORT_PICK,
            fartId = "f",
            authorUid = "a",
            reason = ReportReason.SPAM,
        ),
        onAction = {},
    )
}

@Preview
@Composable
private fun ReportReasonSentPreview() = FartMapTheme {
    ReportReasonContent(
        state = FartSafetyState(step = SafetyStep.REPORT_SENT, fartId = "f", authorUid = "a"),
        onAction = {},
    )
}

@Preview
@Composable
private fun BlockUserDialogPreview() = FartMapTheme {
    BlockUserDialog(onAction = {})
}
