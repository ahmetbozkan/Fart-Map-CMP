package com.ahmetbozkan.fartmap.core.navigation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/** The main shell holds no screen state of its own; it only surfaces app-level effects. */
data object MainShellState

/** No user actions reach the shell's ViewModel (tab navigation is pure UI). */
sealed interface MainShellAction

sealed interface MainShellEvent {
    /** The server refused a fart write after the hand-off (rules, validation): "We couldn't post that one." */
    data object FartRejected : MainShellEvent
}

/**
 * The app-level collector of [PendingWriteMonitor] (LocalData.md §3: one collector owns surfacing). Fart writes are
 * surfaced to the user (create.md step 10); every other rejection is already logged as `write_rejected`.
 */
class MainShellViewModel(
    writes: PendingWriteMonitor,
) : StateViewModel<MainShellState, MainShellAction, MainShellEvent>(MainShellState) {

    init {
        viewModelScope.launch {
            writes.rejections.collect { rejected ->
                if (rejected.op.startsWith(FART_OP_PREFIX)) sendEvent(MainShellEvent.FartRejected)
            }
        }
    }

    override fun onAction(action: MainShellAction) = Unit

    private companion object {
        const val FART_OP_PREFIX = "fart."
    }
}
