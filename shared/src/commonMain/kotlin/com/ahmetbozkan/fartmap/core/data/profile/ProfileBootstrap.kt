package com.ahmetbozkan.fartmap.core.data.profile

import com.ahmetbozkan.fartmap.core.domain.profile.DisplayNameGenerator
import com.ahmetbozkan.fartmap.core.domain.profile.NewProfile
import com.ahmetbozkan.fartmap.core.domain.profile.ProfileRepository
import com.ahmetbozkan.fartmap.core.domain.profile.StarterAvatars
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

/**
 * Gives every Active user a public profile (ADR-0025, STORY-01-02-02): when the server confirms `users/{uid}` is
 * missing, it creates it once with a generated display name and a free starter avatar. This covers new users after
 * the age gate and older accounts that never got one.
 *
 * The create is fire-and-forget; a server rejection (e.g. an unseeded catalog) is reported by PendingWriteMonitor.
 * Each uid is tried at most once per process, so a rejection can't loop; the next launch tries again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileBootstrap(
    private val session: SessionRepository,
    private val profiles: ProfileRepository,
    private val logger: Logger,
    private val random: Random = Random.Default,
) {
    private val attempted = mutableSetOf<String>()

    fun start(scope: CoroutineScope): Job = session.session
        .map { (it as? SessionState.Active)?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid -> if (uid == null) emptyFlow() else missingProfile(uid) }
        .onEach(::createOnce)
        .launchIn(scope)

    /** Emits [uid] each time the server confirms its profile doc is missing. */
    private fun missingProfile(uid: String): Flow<String> =
        profiles.observeProfileExists(uid).filter { exists -> !exists }.map { uid }

    private fun createOnce(uid: String) {
        if (!attempted.add(uid)) return
        val profile = NewProfile(
            displayName = DisplayNameGenerator.generate(random),
            avatarId = StarterAvatars.pick(random),
        )
        val result = profiles.createProfile(uid, profile)
        if (result is AppResult.Failure) {
            logger.error(
                feature = "profile",
                operation = "create",
                event = "profile_create_failed",
                error = result.error,
            )
        }
    }
}
