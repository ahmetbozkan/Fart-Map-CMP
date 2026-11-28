package com.ahmetbozkan.fartmap.core.domain.safety

import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Why a user reports content (MOD-01; codes in DataContracts §3.12). [wireName] is the stored `reason_code` that
 * moderation reads; derived from the policy lines in `ContentSafety.md` §5–§6.
 */
enum class ReportReason(val wireName: String) {
    /** Harassment, threats or hate aimed at a person. */
    HARASSMENT("HARASSMENT"),

    /** Doxxing: someone's private information or location. */
    PRIVATE_INFO("PRIVATE_INFO"),

    /** Puts a minor at risk; reviewed first. */
    MINOR_SAFETY("MINOR_SAFETY"),

    SEXUAL("SEXUAL"),
    ILLEGAL("ILLEGAL"),
    SPAM("SPAM"),
    OTHER("OTHER"),
}

/**
 * Report and block (MOD-01, PRIV-01). Writes go through the app write scope and are never awaited, so both work
 * offline; a server refusal (a repeated report, for one) is logged by the write scope, never shown.
 */
interface SafetyRepository {
    /** Files the caller's report of [fartId]; one per user per fart (the rules refuse a second). */
    fun reportFart(uid: String, fartId: String, reason: ReportReason): AppResult<Unit>

    /** Blocks [blockedUid] for the caller. The blocked user is never told. */
    fun blockUser(uid: String, blockedUid: String): AppResult<Unit>

    /**
     * The users [uid] has blocked, live (from the local cache first). A failed read is logged and ends the flow, so
     * callers keep the last set they saw.
     */
    fun observeBlockedUids(uid: String): Flow<Set<String>>
}
