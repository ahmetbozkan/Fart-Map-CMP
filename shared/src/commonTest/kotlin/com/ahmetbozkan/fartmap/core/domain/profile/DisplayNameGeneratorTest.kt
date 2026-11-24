package com.ahmetbozkan.fartmap.core.domain.profile

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisplayNameGeneratorTest {
    private val shape = Regex("^([A-Z][a-z]+) ([A-Z][a-z]+) ([1-9][0-9])$")

    @Test
    fun everyNameIsAdjectiveNounNumberFromTheCuratedListsWithinTheRulesLimit() {
        val random = Random(42)
        repeat(1_000) {
            val name = DisplayNameGenerator.generate(random)
            val match = assertNotNullMatch(name)
            assertTrue(match.groupValues[1] in DisplayNameGenerator.adjectives, name)
            assertTrue(match.groupValues[2] in DisplayNameGenerator.nouns, name)
            assertTrue(name.length in 1..MAX_DISPLAY_NAME_LENGTH, name)
        }
    }

    @Test
    fun theSameSeedGivesTheSameName() {
        assertEquals(DisplayNameGenerator.generate(Random(1)), DisplayNameGenerator.generate(Random(1)))
    }

    @Test
    fun wordListsAreNonEmptyAndDistinct() {
        listOf(DisplayNameGenerator.adjectives, DisplayNameGenerator.nouns).forEach { words ->
            assertTrue(words.isNotEmpty())
            assertEquals(words.size, words.toSet().size)
        }
    }

    @Test
    fun theStarterAvatarPickStaysInTheList() {
        val random = Random(3)
        repeat(100) { assertTrue(StarterAvatars.pick(random) in StarterAvatars.ids) }
    }

    private fun assertNotNullMatch(name: String): MatchResult {
        val match = shape.matchEntire(name)
        assertTrue(match != null, "unexpected shape: $name")
        return match
    }
}
