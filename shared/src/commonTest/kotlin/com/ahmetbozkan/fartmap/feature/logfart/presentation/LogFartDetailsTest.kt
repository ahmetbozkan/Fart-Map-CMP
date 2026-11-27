package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.StarterCosmetics
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeCosmeticRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeLocationProvider
import com.ahmetbozkan.fartmap.core.fakes.FakeNetworkStatus
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Detailed fields (STORY-01-04-03): acceptance 1–5, 7–9 through the ViewModel, plus the pure description helpers. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class LogFartDetailsTest {
    private val now = 1_764_000_003_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(now)
    }
    private val here = DeviceLocation(41.05, 29.02)

    private val sessions = InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT))
    private val location =
        FakeLocationProvider(status = LocationPermission.GRANTED, lookup = LocationLookup.Found(here))
    private val farts = FakeFartRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()

    private val king = CosmeticItem("avatar_paid_king", CosmeticType.AVATAR, "Gas King", false, true, 30)
    private val tuba = CosmeticItem("sound_paid_tuba", CosmeticType.SOUND, "Tuba Solo", false, true, 40)
    private val cosmetics = FakeCosmeticRepository(catalog = StarterCosmetics.items + king + tuba)

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LogFartViewModel(
        startCamera = MapCamera(41.0, 29.0, 12f),
        sessions = sessions,
        preferences = FakePreferencesRepository(),
        location = location,
        network = FakeNetworkStatus(),
        farts = farts,
        cosmetics = cosmetics,
        analytics = AnalyticsTracker { tracked += it },
        logger = Logger(sinks = emptyList()),
        clock = clock,
    )

    private fun TestScope.events(vm: LogFartViewModel): List<LogFartEvent> {
        val events = mutableListOf<LogFartEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    // Acceptance 1: defaults

    @Test
    fun aFreshScreenHasEveryDefault() = runTest {
        val state = viewModel().state.value

        assertEquals(now, state.timestampEpochMs)
        assertEquals("avatar_free_bean", state.avatarId)
        assertNull(state.soundId)
        assertEquals(3, state.flavor)
        assertEquals(0.5, state.intensity)
        assertEquals("", state.description)
        assertTrue(state.placeholderIndex in 0 until PLACEHOLDER_COUNT)
    }

    // Acceptance 2 (through the ViewModel): options follow the catalog and the owned items

    @Test
    fun optionsComeFromTheCatalogWithPaidItemsLocked() = runTest {
        val state = viewModel().state.value

        assertEquals(listOf("avatar_free_bean", "avatar_free_cloud", "avatar_paid_king"), state.avatars.map { it.id })
        assertTrue(state.avatars.single { it.id == "avatar_paid_king" }.isLocked)
        assertTrue(state.sounds.single { it.id == "sound_paid_tuba" }.isLocked)
        assertEquals(listOf("me"), cosmetics.ownedFor)
    }

    @Test
    fun beforeTheCatalogArrivesTheStarterItemsAreOffered() = runTest {
        cosmetics.catalog.value = emptyList()

        val state = viewModel().state.value

        assertEquals(listOf("avatar_free_bean", "avatar_free_cloud"), state.avatars.map { it.id })
        assertEquals(3, state.sounds.size)
        assertTrue((state.avatars + state.sounds).none { it.isLocked })
    }

    // Acceptance 3: selection

    @Test
    fun aUsableAvatarAndSoundCanBeSelectedAndNoSoundClearsIt() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.AvatarSelected("avatar_free_cloud"))
        vm.onAction(LogFartAction.SoundSelected("sound_free_squeak"))
        assertEquals("avatar_free_cloud", vm.state.value.avatarId)
        assertEquals("sound_free_squeak", vm.state.value.soundId)

        vm.onAction(LogFartAction.SoundSelected(null))
        assertNull(vm.state.value.soundId)
    }

    @Test
    fun aLockedOrUnknownItemIsNeverSelected() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.AvatarSelected("avatar_paid_king"))
        vm.onAction(LogFartAction.AvatarSelected("avatar_made_up"))
        vm.onAction(LogFartAction.SoundSelected("sound_paid_tuba"))
        vm.onAction(LogFartAction.SoundSelected("sound_made_up"))

        assertEquals("avatar_free_bean", vm.state.value.avatarId)
        assertNull(vm.state.value.soundId)
    }

    @Test
    fun anOwnedItemBecomesSelectable() = runTest {
        val vm = viewModel()

        cosmetics.owned.value = setOf("avatar_paid_king")
        vm.onAction(LogFartAction.AvatarSelected("avatar_paid_king"))

        assertEquals("avatar_paid_king", vm.state.value.avatarId)
    }

    @Test
    fun losingAnOwnedItemFallsBackToTheDefaults() = runTest {
        cosmetics.owned.value = setOf("avatar_paid_king", "sound_paid_tuba")
        val vm = viewModel()
        vm.onAction(LogFartAction.AvatarSelected("avatar_paid_king"))
        vm.onAction(LogFartAction.SoundSelected("sound_paid_tuba"))

        // What a failing entitlements read emits (fail closed).
        cosmetics.owned.value = emptySet()

        assertEquals("avatar_free_bean", vm.state.value.avatarId)
        assertNull(vm.state.value.soundId)
    }

    @Test
    fun aRemovedSelectionFallsBackToTheFirstUsableWhenTheDefaultIsGone() = runTest {
        val cloud = StarterCosmetics.items.single { it.id == "avatar_free_cloud" }
        val squeak = StarterCosmetics.items.single { it.id == "sound_free_squeak" }
        val vm = viewModel()
        vm.onAction(LogFartAction.SoundSelected("sound_free_classic"))

        cosmetics.catalog.value = listOf(cloud, squeak, tuba)

        assertEquals("avatar_free_cloud", vm.state.value.avatarId)
        assertNull(vm.state.value.soundId)
    }

    @Test
    fun aStillUsableSelectionSurvivesACatalogUpdate() = runTest {
        val vm = viewModel()
        vm.onAction(LogFartAction.AvatarSelected("avatar_free_cloud"))
        vm.onAction(LogFartAction.SoundSelected("sound_free_thunder"))

        cosmetics.catalog.value = StarterCosmetics.items + king

        assertEquals("avatar_free_cloud", vm.state.value.avatarId)
        assertEquals("sound_free_thunder", vm.state.value.soundId)
    }

    // Acceptance 4: flavor and intensity

    @Test
    fun flavorOutsideOneToFiveIsIgnored() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.FlavorSelected(5))
        assertEquals(5, vm.state.value.flavor)
        vm.onAction(LogFartAction.FlavorSelected(0))
        vm.onAction(LogFartAction.FlavorSelected(6))
        assertEquals(5, vm.state.value.flavor)
        vm.onAction(LogFartAction.FlavorSelected(1))
        assertEquals(1, vm.state.value.flavor)
    }

    @Test
    fun intensityIsClampedAndNanIgnored() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.IntensityChanged(0.25f))
        assertEquals(0.25, vm.state.value.intensity)
        vm.onAction(LogFartAction.IntensityChanged(Float.NaN))
        assertEquals(0.25, vm.state.value.intensity)
        vm.onAction(LogFartAction.IntensityChanged(1.5f))
        assertEquals(1.0, vm.state.value.intensity)
        vm.onAction(LogFartAction.IntensityChanged(-0.5f))
        assertEquals(0.0, vm.state.value.intensity)
    }

    // Acceptance 5: description

    @Test
    fun aCaptionUpTo280IsKeptAndLongerIsCut() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.DescriptionChanged("a".repeat(280)))
        assertEquals(280, vm.state.value.description.length)
        vm.onAction(LogFartAction.DescriptionChanged("b".repeat(300)))
        assertEquals("b".repeat(280), vm.state.value.description)
    }

    @Test
    fun theCutNeverSplitsAnEmoji() {
        // 279 letters + an emoji (2 UTF-16 units) is 281 units: the whole emoji goes, not half of it.
        val cut = limitDescription("a".repeat(279) + "💨")

        assertEquals("a".repeat(279), cut)
        assertEquals("a".repeat(278) + "💨", limitDescription("a".repeat(278) + "💨" + "zz"))
    }

    @Test
    fun aBlankCaptionIsNullAndOthersAreTrimmed() {
        assertNull(descriptionOrNull(""))
        assertNull(descriptionOrNull("  \n\t "))
        assertEquals("Any casualties? 💨", descriptionOrNull("  Any casualties? 💨 \n"))
    }

    // Acceptance 7: every field round-trips into the hand-off

    @Test
    fun everyChangedFieldReachesTheRepositoryAndAnalytics() = runTest {
        cosmetics.owned.value = setOf("avatar_paid_king")
        val picked = 1_800_000_000_000L
        val vm = viewModel()

        vm.onAction(LogFartAction.DateTimePicked(picked))
        vm.onAction(LogFartAction.AvatarSelected("avatar_paid_king"))
        vm.onAction(LogFartAction.FlavorSelected(5))
        vm.onAction(LogFartAction.IntensityChanged(0.9f))
        vm.onAction(LogFartAction.DescriptionChanged(" Silent but deadly 💨 "))
        vm.onAction(LogFartAction.SoundSelected("sound_free_thunder"))
        vm.onAction(LogFartAction.VisibilitySelected(VisibilityMode.FOLLOWERS))
        vm.onAction(LogFartAction.Submit)

        assertEquals(
            listOf(
                "me" to NewFart(
                    lat = here.lat,
                    lng = here.lng,
                    timestampEpochMs = picked,
                    avatarId = "avatar_paid_king",
                    flavorLevel = 5,
                    intensityValue = 0.9f.toDouble(),
                    descriptionText = "Silent but deadly 💨",
                    soundId = "sound_free_thunder",
                    visibility = VisibilityMode.FOLLOWERS,
                ),
            ),
            farts.created,
        )
        assertEquals(
            AnalyticsEvent.LogSubmitted(
                visibility = VisibilityMode.FOLLOWERS,
                hasDescription = true,
                hasSound = true,
                avatarIsFree = false,
                offline = false,
            ),
            tracked.filterIsInstance<AnalyticsEvent.LogSubmitted>().single(),
        )
    }

    @Test
    fun aWhitespaceCaptionIsSentAsNoDescription() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.DescriptionChanged("   "))
        vm.onAction(LogFartAction.Submit)

        assertNull(farts.created.single().second.descriptionText)
        assertEquals(false, tracked.filterIsInstance<AnalyticsEvent.LogSubmitted>().single().hasDescription)
    }

    // Acceptance 8: uploading freezes edits

    @Test
    fun everyFieldIsFrozenWhileUploading() = runTest {
        val vm = viewModel()
        lateinit var during: LogFartState
        farts.onCreate = {
            vm.onAction(LogFartAction.DateTimePicked(1L))
            vm.onAction(LogFartAction.AvatarSelected("avatar_free_cloud"))
            vm.onAction(LogFartAction.SoundSelected("sound_free_squeak"))
            vm.onAction(LogFartAction.FlavorSelected(1))
            vm.onAction(LogFartAction.IntensityChanged(1f))
            vm.onAction(LogFartAction.DescriptionChanged("late"))
            during = vm.state.value
        }

        vm.onAction(LogFartAction.Submit)

        assertEquals(LogStatus.UPLOADING, during.status)
        assertEquals(now, during.timestampEpochMs)
        assertEquals("avatar_free_bean", during.avatarId)
        assertNull(during.soundId)
        assertEquals(3, during.flavor)
        assertEquals(0.5, during.intensity)
        assertEquals("", during.description)
    }

    // Acceptance 9: the locked voice row

    @Test
    fun theVoiceRowOpensTheInfoSheetOnce() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.VoiceRowTapped)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.ShowVoiceInfo), events)
    }
}
