package com.ahmetbozkan.fartmap.feature.logfart.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticOption
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_lock
import com.ahmetbozkan.fartmap.resources.ic_waveform
import com.ahmetbozkan.fartmap.resources.log_avatar_title
import com.ahmetbozkan.fartmap.resources.log_description_counter
import com.ahmetbozkan.fartmap.resources.log_description_placeholder_1
import com.ahmetbozkan.fartmap.resources.log_description_placeholder_2
import com.ahmetbozkan.fartmap.resources.log_description_placeholder_3
import com.ahmetbozkan.fartmap.resources.log_description_placeholder_4
import com.ahmetbozkan.fartmap.resources.log_description_title
import com.ahmetbozkan.fartmap.resources.log_flavor_1
import com.ahmetbozkan.fartmap.resources.log_flavor_2
import com.ahmetbozkan.fartmap.resources.log_flavor_3
import com.ahmetbozkan.fartmap.resources.log_flavor_4
import com.ahmetbozkan.fartmap.resources.log_flavor_5
import com.ahmetbozkan.fartmap.resources.log_flavor_title
import com.ahmetbozkan.fartmap.resources.log_intensity_max
import com.ahmetbozkan.fartmap.resources.log_intensity_min
import com.ahmetbozkan.fartmap.resources.log_intensity_title
import com.ahmetbozkan.fartmap.resources.log_intensity_value
import com.ahmetbozkan.fartmap.resources.log_locked
import com.ahmetbozkan.fartmap.resources.log_sound_none
import com.ahmetbozkan.fartmap.resources.log_sound_title
import com.ahmetbozkan.fartmap.resources.log_voice_locked
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_body
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_ok
import com.ahmetbozkan.fartmap.resources.log_voice_sheet_title
import com.ahmetbozkan.fartmap.resources.log_voice_title
import com.ahmetbozkan.fartmap.resources.log_when_cancel
import com.ahmetbozkan.fartmap.resources.log_when_change
import com.ahmetbozkan.fartmap.resources.log_when_change_description
import com.ahmetbozkan.fartmap.resources.log_when_confirm
import com.ahmetbozkan.fartmap.resources.log_when_time_title
import com.ahmetbozkan.fartmap.resources.log_when_title
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// The detailed-field cards of Log Fart (STORY-01-04-03). Stateless apart from which picker dialog is open.

@Composable
internal fun DetailCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private enum class WhenDialog { NONE, DATE, TIME }

/** Step 2: the shown time, and "Change" → date picker → time picker. No max date (create.md step 2). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WhenCard(timestampEpochMs: Long, enabled: Boolean, onPick: (Long) -> Unit) {
    val zone = TimeZone.currentSystemDefault()
    val shown = formatLogTime(timestampEpochMs, zone)
    var dialog by rememberSaveable { mutableStateOf(WhenDialog.NONE) }
    var pickedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    val changeDescription = stringResource(Res.string.log_when_change_description, shown)
    DetailCard(title = stringResource(Res.string.log_when_title)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(text = shown, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { dialog = WhenDialog.DATE },
                enabled = enabled,
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget)
                    .semantics { contentDescription = changeDescription },
            ) { Text(stringResource(Res.string.log_when_change)) }
        }
    }
    val parts = pickerParts(timestampEpochMs, zone)
    when (dialog) {
        WhenDialog.NONE -> Unit
        WhenDialog.DATE -> {
            val dateState = rememberDatePickerState(initialSelectedDateMillis = parts.dateUtcMidnightMs)
            DatePickerDialog(
                onDismissRequest = { dialog = WhenDialog.NONE },
                confirmButton = {
                    TextButton(onClick = {
                        pickedDate = dateState.selectedDateMillis ?: parts.dateUtcMidnightMs
                        dialog = WhenDialog.TIME
                    }) { Text(stringResource(Res.string.log_when_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = WhenDialog.NONE }) { Text(stringResource(Res.string.log_when_cancel)) }
                },
            ) { DatePicker(state = dateState) }
        }
        WhenDialog.TIME -> {
            val timeState = rememberTimePickerState(initialHour = parts.hour, initialMinute = parts.minute)
            AlertDialog(
                onDismissRequest = { dialog = WhenDialog.NONE },
                title = { Text(stringResource(Res.string.log_when_time_title)) },
                text = { TimePicker(state = timeState) },
                confirmButton = {
                    TextButton(onClick = {
                        val date = pickedDate ?: parts.dateUtcMidnightMs
                        onPick(combineDateAndTime(date, timeState.hour, timeState.minute, zone))
                        dialog = WhenDialog.NONE
                    }) { Text(stringResource(Res.string.log_when_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = WhenDialog.NONE }) { Text(stringResource(Res.string.log_when_cancel)) }
                },
            )
        }
    }
}

/**
 * Steps 3 and 7: one chip per option, announced as radio buttons. A locked option keeps its chip with a lock (it stays
 * visible); tapping it selects nothing until the unlock sheet exists (EPIC-01-06). [noneLabel] adds a "none" chip.
 */
@Composable
internal fun CosmeticCard(
    title: String,
    options: List<CosmeticOption>,
    selectedId: String?,
    enabled: Boolean,
    onSelect: (String?) -> Unit,
    noneLabel: String? = null,
) {
    DetailCard(title = title) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup()
                .semantics { contentDescription = title },
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (noneLabel != null) {
                OptionChip(label = noneLabel, selected = selectedId == null, enabled = enabled, onClick = { onSelect(null) })
            }
            options.forEach { option ->
                OptionChip(
                    label = option.name,
                    selected = option.id == selectedId,
                    enabled = enabled,
                    locked = option.isLocked,
                    onClick = { onSelect(option.id) },
                )
            }
        }
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit, locked: Boolean = false) {
    val lockedDescription = stringResource(Res.string.log_locked, label)
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = if (locked) {
            { Icon(painterResource(Res.drawable.ic_lock), contentDescription = null, modifier = Modifier.size(Spacing.lg)) }
        } else {
            null
        },
        modifier = Modifier.heightIn(min = Spacing.minTouchTarget).semantics {
            role = Role.RadioButton
            if (locked) contentDescription = lockedDescription
        },
    )
}

@Composable
internal fun AvatarCard(state: LogFartState, onAction: (LogFartAction) -> Unit) = CosmeticCard(
    title = stringResource(Res.string.log_avatar_title),
    options = state.avatars,
    selectedId = state.avatarId,
    enabled = state.isEditable,
    onSelect = { id -> id?.let { onAction(LogFartAction.AvatarSelected(it)) } },
)

@Composable
internal fun SoundCard(state: LogFartState, onAction: (LogFartAction) -> Unit) = CosmeticCard(
    title = stringResource(Res.string.log_sound_title),
    options = state.sounds,
    selectedId = state.soundId,
    enabled = state.isEditable,
    onSelect = { onAction(LogFartAction.SoundSelected(it)) },
    noneLabel = stringResource(Res.string.log_sound_none),
)

private val flavorLabels: List<StringResource> = listOf(
    Res.string.log_flavor_1,
    Res.string.log_flavor_2,
    Res.string.log_flavor_3,
    Res.string.log_flavor_4,
    Res.string.log_flavor_5,
)

/** Step 4: five labelled steps, so the level is in the text, never in colour alone. */
@Composable
internal fun FlavorCard(flavor: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    DetailCard(title = stringResource(Res.string.log_flavor_title)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            flavorLabels.forEachIndexed { index, label ->
                val level = index + 1
                SegmentedButton(
                    selected = flavor == level,
                    onClick = { onSelect(level) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = flavorLabels.size),
                    icon = {},
                    modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
                ) { Text(stringResource(label), maxLines = 1, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

/** Step 5: Whisper (0.0) to Thunderclap (1.0). */
@Composable
internal fun IntensityCard(intensity: Double, enabled: Boolean, onChange: (Float) -> Unit) {
    val percent = (intensity * PERCENT).roundToInt()
    val valueDescription = stringResource(Res.string.log_intensity_value, percent)
    DetailCard(title = stringResource(Res.string.log_intensity_title)) {
        Slider(
            value = intensity.toFloat(),
            onValueChange = onChange,
            enabled = enabled,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth().semantics { stateDescription = valueDescription },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.log_intensity_min), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(Res.string.log_intensity_max), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val placeholders: List<StringResource> = listOf(
    Res.string.log_description_placeholder_1,
    Res.string.log_description_placeholder_2,
    Res.string.log_description_placeholder_3,
    Res.string.log_description_placeholder_4,
)

/** Step 6: optional, emoji welcome, stops at [DESCRIPTION_SOFT_MAX] (the ViewModel cuts) with a visible counter. */
@Composable
internal fun DescriptionCard(state: LogFartState, onChange: (String) -> Unit) {
    val title = stringResource(Res.string.log_description_title)
    DetailCard(title = title) {
        OutlinedTextField(
            value = state.description,
            onValueChange = onChange,
            enabled = state.isEditable,
            placeholder = { Text(stringResource(placeholders[state.placeholderIndex.mod(placeholders.size)])) },
            supportingText = {
                Text(stringResource(Res.string.log_description_counter, state.description.length, DESCRIPTION_SOFT_MAX))
            },
            minLines = 2,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = title },
        )
    }
}

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
                Text(locked, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

private const val PERCENT = 100
