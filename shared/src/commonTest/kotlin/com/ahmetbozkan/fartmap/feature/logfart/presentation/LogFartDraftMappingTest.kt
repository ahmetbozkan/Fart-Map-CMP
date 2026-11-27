package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticOption
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The pure draft functions of STORY-01-04-04 (acceptance 3, 4, 10). */
class LogFartDraftMappingTest {
    private val openedAt = 1_764_000_000_000L
    private val base = LogFartState(
        timestampEpochMs = openedAt,
        visibility = VisibilityMode.PUBLIC,
        allowedVisibilities = VisibilityMode.entries.toList(),
    )
    private val pinned = LocationStep.Resolved(DeviceLocation(41.05, 29.02), LocationSource.PIN)
    private val king = CosmeticOption("avatar_paid_king", "Gas King", isFree = false, isLocked = false)

    private val draft = LogDraft(
        ownerUid = "me",
        timestampEpochMs = 1_763_000_000_000L,
        avatarId = "avatar_free_cloud",
        soundId = "sound_free_thunder",
        flavor = 5,
        intensity = 0.9,
        description = "Round two",
        visibility = VisibilityMode.FOLLOWERS,
    )

    // toDraftOrNull

    @Test
    fun anUntouchedFormHasNothingToSave() {
        assertNull(base.copy(location = pinned).toDraftOrNull("me"))
    }

    @Test
    fun aDirtyFormIsSavedWhileEditingOrFailedButNotWhileUploadingOrDone() {
        val dirty = base.copy(isDirty = true)

        assertNotNull(dirty.toDraftOrNull("me"))
        assertNotNull(dirty.copy(status = LogStatus.FAILED).toDraftOrNull("me"))
        assertNull(dirty.copy(status = LogStatus.UPLOADING).toDraftOrNull("me"))
        assertNull(dirty.copy(status = LogStatus.SUCCESS).toDraftOrNull("me"))
        assertNull(dirty.copy(status = LogStatus.OFFLINE).toDraftOrNull("me"))
    }

    @Test
    fun theDraftCarriesEveryFieldButNoLocation() {
        val state = base.copy(
            location = pinned,
            timestampEpochMs = 1_763_000_000_000L,
            timePicked = true,
            avatarId = "avatar_free_cloud",
            soundId = "sound_free_thunder",
            flavor = 5,
            intensity = 0.9,
            description = "Round two",
            visibility = VisibilityMode.FOLLOWERS,
            isDirty = true,
        )

        assertEquals(draft, state.toDraftOrNull("me"))
    }

    @Test
    fun anUnpickedTimeIsNotStored() {
        assertNull(base.copy(isDirty = true).toDraftOrNull("me")?.timestampEpochMs)
    }

    @Test
    fun aPendingPickIsStoredInsteadOfTheTemporarySelection() {
        val state = base.copy(isDirty = true, pendingAvatarId = "avatar_paid_king", pendingSoundId = "sound_paid_tuba")

        val saved = state.toDraftOrNull("me")

        assertEquals("avatar_paid_king", saved?.avatarId)
        assertEquals("sound_paid_tuba", saved?.soundId)
    }

    // restore

    @Test
    fun restoreFillsEveryStoredField() {
        val restored = base.restore(draft, VisibilityMode.PUBLIC)

        assertEquals(1_763_000_000_000L, restored.timestampEpochMs)
        assertTrue(restored.timePicked)
        assertEquals("avatar_free_cloud", restored.avatarId)
        assertEquals("sound_free_thunder", restored.soundId)
        assertEquals(5, restored.flavor)
        assertEquals(0.9, restored.intensity)
        assertEquals("Round two", restored.description)
        assertEquals(VisibilityMode.FOLLOWERS, restored.visibility)
        assertTrue(restored.isDirty)
        assertTrue(restored.restoredDraft)
        assertNull(restored.pendingAvatarId)
        assertNull(restored.pendingSoundId)
        assertEquals(base.location, restored.location, "the location step is untouched")
    }

    @Test
    fun restoreKeepsTheOpenTimeWhenNoneWasPicked() {
        val restored = base.restore(draft.copy(timestampEpochMs = null), VisibilityMode.PUBLIC)

        assertEquals(openedAt, restored.timestampEpochMs)
        assertFalse(restored.timePicked)
    }

    @Test
    fun restoreChecksEveryValueLikeInput() {
        val restored = base.restore(
            draft.copy(flavor = 9, intensity = 3.0, description = "x".repeat(300)),
            VisibilityMode.PUBLIC,
        )

        assertEquals(DEFAULT_FLAVOR, restored.flavor)
        assertEquals(1.0, restored.intensity)
        assertEquals(DESCRIPTION_SOFT_MAX, restored.description.length)
    }

    @Test
    fun aNanIntensityIsIgnored() {
        assertEquals(DEFAULT_INTENSITY, base.restore(draft.copy(intensity = Double.NaN), VisibilityMode.PUBLIC).intensity)
    }

    @Test
    fun aVisibilityNotAllowedFallsBack() {
        val teen = base.copy(
            visibility = VisibilityMode.FOLLOWERS,
            allowedVisibilities = listOf(VisibilityMode.FOLLOWERS, VisibilityMode.PRIVATE),
        )

        val restored = teen.restore(draft.copy(visibility = VisibilityMode.PUBLIC), VisibilityMode.FOLLOWERS)

        assertEquals(VisibilityMode.FOLLOWERS, restored.visibility)
        assertEquals(
            VisibilityMode.FOLLOWERS,
            teen.restore(draft.copy(visibility = null), VisibilityMode.FOLLOWERS).visibility,
        )
    }

    @Test
    fun anUnusablePickWaitsAsPending() {
        val restored = base.restore(
            draft.copy(avatarId = "avatar_paid_king", soundId = "sound_paid_tuba"),
            VisibilityMode.PUBLIC,
        )

        assertEquals(DEFAULT_AVATAR, restored.avatarId)
        assertEquals("avatar_paid_king", restored.pendingAvatarId)
        assertNull(restored.soundId)
        assertEquals("sound_paid_tuba", restored.pendingSoundId)
    }

    @Test
    fun aBlankAvatarIsNotPending() {
        assertNull(base.restore(draft.copy(avatarId = ""), VisibilityMode.PUBLIC).pendingAvatarId)
    }

    // applyPending

    @Test
    fun aPendingPickBecomesTheSelectionOnceUsable() {
        val pending = base.copy(pendingAvatarId = "avatar_paid_king")

        val applied = pending.copy(avatars = pending.avatars + king).applyPending(optionsLoaded = false)

        assertEquals("avatar_paid_king", applied.avatarId)
        assertNull(applied.pendingAvatarId)
    }

    @Test
    fun aPendingPickWaitsUntilTheOptionsLoadThenIsDropped() {
        val pending = base.copy(pendingAvatarId = "avatar_paid_king", pendingSoundId = "sound_paid_tuba")

        assertEquals(pending, pending.applyPending(optionsLoaded = false))

        val dropped = pending.applyPending(optionsLoaded = true)
        assertNull(dropped.pendingAvatarId)
        assertNull(dropped.pendingSoundId)
        assertEquals(DEFAULT_AVATAR, dropped.avatarId)
    }

    // abandonStep

    @Test
    fun theStepReachedFollowsTheForm() {
        assertEquals(AnalyticsEvent.LogStep.LOCATION, base.abandonStep())
        assertEquals(AnalyticsEvent.LogStep.DETAILS, base.copy(location = pinned).abandonStep())
        assertEquals(
            AnalyticsEvent.LogStep.FAILED,
            base.copy(location = pinned, status = LogStatus.FAILED).abandonStep(),
        )
    }
}
