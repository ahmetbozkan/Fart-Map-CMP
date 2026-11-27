package com.ahmetbozkan.fartmap.feature.logfart.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.log_draft_restored
import com.ahmetbozkan.fartmap.resources.log_leave_body
import com.ahmetbozkan.fartmap.resources.log_leave_discard
import com.ahmetbozkan.fartmap.resources.log_leave_keep
import com.ahmetbozkan.fartmap.resources.log_leave_title
import org.jetbrains.compose.resources.stringResource

/** Shown at the top of a form restored from a draft (STORY-01-04-04); back → Discard starts over. */
@Composable
internal fun RestoredNote() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.log_draft_restored),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(Spacing.cardPadding),
        )
    }
}

/**
 * Back on a dirty form: keep the draft or discard it. Tapping outside or back dismisses and stays on the form; Discard
 * is an explicit button because it throws the user's input away.
 */
@Composable
internal fun LeaveDraftDialog(onKeep: () -> Unit, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.log_leave_title)) },
        text = { Text(stringResource(Res.string.log_leave_body)) },
        confirmButton = {
            TextButton(onClick = onKeep, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(Res.string.log_leave_keep))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(Res.string.log_leave_discard), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}
