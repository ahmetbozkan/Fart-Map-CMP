package com.ahmetbozkan.fartmap.core.domain.profile

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.StarterCosmetics
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

/** What the client sets when it creates `users/{uid}` (DataContracts.md §3.1, ADR-0025); counters are server-side. */
data class NewProfile(val displayName: String, val avatarId: String)

/** The public profile doc `users/{uid}` (implemented in `firebaseData`). */
interface ProfileRepository {
    /**
     * True when the doc exists (a pending local write counts); false only when the server confirms it doesn't.
     * A miss served from the local cache emits nothing, so a new device never re-creates an existing profile.
     */
    fun observeProfileExists(uid: String): Flow<Boolean>

    /** Creates the doc once, fire-and-forget through AppWriteScope; a rejection surfaces via PendingWriteMonitor. */
    fun createProfile(uid: String, profile: NewProfile): AppResult<Unit>
}

/**
 * The free starter avatars every new profile picks from: the AVATAR items of [StarterCosmetics] (which mirrors
 * `backend/catalog/starter-catalog.json`, checked by StarterCatalogParityTest); the rules reject any other id
 * unless the user is entitled to it.
 */
object StarterAvatars {
    val ids: List<String> = StarterCosmetics.items.filter { it.type == CosmeticType.AVATAR }.map { it.id }

    fun pick(random: Random): String = ids[random.nextInt(ids.size)]
}

/** The rules' limit on `display_name` (backend/firestore.rules, users). */
const val MAX_DISPLAY_NAME_LENGTH = 50

/**
 * Generated funny display names: "<Adjective> <Noun> <10–99>" from curated lists inside the humor boundary
 * (ContentSafety.md: absurd, never aimed at a person or a body). Not unique; identity is the uid. Nothing in the name
 * comes from the user or the uid.
 */
object DisplayNameGenerator {
    internal val adjectives: List<String> = listOf(
        "Sneaky", "Mighty", "Silent", "Breezy", "Rumbling", "Noble", "Cosmic", "Fizzy", "Gusty", "Humble",
        "Jolly", "Legendary", "Mysterious", "Polite", "Rapid", "Sassy", "Thunderous", "Turbo", "Whispering",
        "Zesty", "Bubbly", "Dramatic", "Fearless", "Majestic",
    )

    internal val nouns: List<String> = listOf(
        "Bean", "Breeze", "Cloud", "Trumpet", "Tornado", "Whistle", "Bubble", "Captain", "Comet", "Duke",
        "Gust", "Hurricane", "Legend", "Maestro", "Ninja", "Rocket", "Squeaker", "Storm", "Tuba", "Wizard",
        "Zephyr", "Baron", "Balloon", "Thunder",
    )

    private const val MIN_SUFFIX = 10
    private const val MAX_SUFFIX_EXCLUSIVE = 100

    fun generate(random: Random): String {
        val adjective = adjectives[random.nextInt(adjectives.size)]
        val noun = nouns[random.nextInt(nouns.size)]
        val suffix = random.nextInt(MIN_SUFFIX, MAX_SUFFIX_EXCLUSIVE)
        return "$adjective $noun $suffix"
    }
}
