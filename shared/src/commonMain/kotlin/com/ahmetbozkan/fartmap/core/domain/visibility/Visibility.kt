package com.ahmetbozkan.fartmap.core.domain.visibility

/** Who can see a fart (DataContracts.md, LocalData.md §6). Unknown wire values decode to [PRIVATE]. */
enum class VisibilityMode(val wireName: String) {
    PUBLIC("PUBLIC"),
    FOLLOWERS("FOLLOWERS"),
    PRIVATE("PRIVATE");

    companion object {
        fun fromWire(value: String?): VisibilityMode = entries.firstOrNull { it.wireName == value } ?: PRIVATE
    }
}

/** Self-declared age band (13+ only; never a date of birth). UNKNOWN until EPIC-01-02 captures it. */
enum class AgeBand { ADULT, TEEN, UNKNOWN }

/**
 * The visibilities a user may choose (ADR-0023 amendment, 2026-11-23): PUBLIC is for the 18+ band only, and an
 * unknown band is treated like a minor (fail closed). Firestore rules enforce the same; this drives the UI.
 */
fun allowedVisibilities(ageBand: AgeBand): List<VisibilityMode> =
    if (ageBand == AgeBand.ADULT) {
        VisibilityMode.entries.toList()
    } else {
        listOf(
            VisibilityMode.FOLLOWERS,
            VisibilityMode.PRIVATE
        )
    }

/**
 * The visibility preselected in Log Fart (ADR-0023, LocalData.md §5). The user's last choice wins when it is still
 * allowed; before that, Phase 1 defaults to PUBLIC for adults and FOLLOWERS for everyone else.
 */
fun defaultVisibility(lastUsed: VisibilityMode?, ageBand: AgeBand): VisibilityMode = when {
    lastUsed != null && lastUsed in allowedVisibilities(ageBand) -> lastUsed
    ageBand == AgeBand.ADULT -> VisibilityMode.PUBLIC
    else -> VisibilityMode.FOLLOWERS
}
