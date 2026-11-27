package com.ahmetbozkan.fartmap.feature.logfart.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_lock
import com.ahmetbozkan.fartmap.resources.ic_waveform
import com.ahmetbozkan.fartmap.resources.log_voice_locked
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_body
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_ok
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_title
import com.ahmetbozkan.fartmap.resources.log_voice_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Step 8: always shown, always locked until voice notes ship (CREATE-03.2 rule 2); a tap explains, never sells. */
@Composable
internal fun VoiceCard(onTap: () -> Unit) {
    val title = stringResource(Res.string.log_voice_title)
    val locked = stringResource(Res.string.log_voice_locked)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget)
                .clickable(role = Role.Button, onClick = onTap)
                .padding(Spacing.cardPadding)
                .semantics { contentDescription = "$title, $locked" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                painterResource(Res.drawable.ic_waveform),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = locked,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(painterResource(Res.drawable.ic_lock), contentDescription = null)
        }
    }
}

/** The informational voice sheet: one "Got it", no purchase, no ad (CREATE-03.2 rules 2–3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceInfoSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) { VoiceInfoContent(onDismiss) }
}

@Composable
internal fun VoiceInfoContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(Spacing.sheetPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(stringResource(Res.string.log_voice_sheet_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.log_voice_sheet_body), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget)) {
            Text(stringResource(Res.string.log_voice_sheet_ok))
        }
    }
}
