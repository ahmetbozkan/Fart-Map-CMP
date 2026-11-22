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
 * The visibility preselected in Log Fart (ADR-0023, LocalData.md §5). The user's last choice wins; before that,
 * Phase 1 defaults to PUBLIC for adults only. Teens and an unknown band get FOLLOWERS (fail safe for minors), and a
 * remembered PUBLIC is never preselected for them either — they can still pick PUBLIC explicitly each time.
 */
fun defaultVisibility(lastUsed: VisibilityMode?, ageBand: AgeBand): VisibilityMode {
    val isAdult = ageBand == AgeBand.ADULT
    return when {
        lastUsed == null -> if (isAdult) VisibilityMode.PUBLIC else VisibilityMode.FOLLOWERS
        lastUsed == VisibilityMode.PUBLIC && !isAdult -> VisibilityMode.FOLLOWERS
        else -> lastUsed
    }
}
