package com.example.pokedex.tts

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PokemonPronunciationTest {

    @Before
    fun setUp() {
        val testDictionary = mapOf(
            "pikachu" to "Pikachu",
            "charizard" to "Charizard",
            "solgaleo" to "Solgalayo",
            "lunala" to "Lunala",
            "rayquaza" to "Raykwayza",
            "arceus" to "Arkayus",
            "suicune" to "Sweekoon",
            "kyogre" to "Kyogur",
            "groudon" to "Growdon",
            "tapu koko" to "Tahpoo Koko",
            "iron treads" to "Iron Treads",
            "mr mime" to "Mister Mime"
        )
        PokemonPronunciation.initForTesting(testDictionary)
    }

    @Test
    fun testPikachuNaturalFluidPronunciation() {
        val input = "Pikachu is an Electric type Pokemon."
        val expected = "Pikachu is an Electric type Pokemon."
        val actual = PokemonPronunciation.formatForSpeech(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testSolgaleoAndLunalaPronunciation() {
        val input = "Solgaleo and Lunala are the legendary guardians of the Alola region."
        val expected = "Solgalayo and Lunala are the legendary guardians of the Alola region."
        val actual = PokemonPronunciation.formatForSpeech(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testPossessiveFormResolution() {
        val input = "Pikachu's cheeks spark with electricity."
        val expected = "Pikachu's cheeks spark with electricity."
        val actual = PokemonPronunciation.formatForSpeech(input)
        assertEquals(expected, actual)

        val input2 = "Solgaleo's radiant body emits an immense burst of light."
        val expected2 = "Solgalayo's radiant body emits an immense burst of light."
        val actual2 = PokemonPronunciation.formatForSpeech(input2)
        assertEquals(expected2, actual2)
    }

    @Test
    fun testLegendaryAndAnimePhonetics() {
        assertEquals("Raykwayza", PokemonPronunciation.formatForSpeech("Rayquaza"))
        assertEquals("Arkayus", PokemonPronunciation.formatForSpeech("Arceus"))
        assertEquals("Sweekoon", PokemonPronunciation.formatForSpeech("Suicune"))
        assertEquals("Kyogur", PokemonPronunciation.formatForSpeech("Kyogre"))
        assertEquals("Growdon", PokemonPronunciation.formatForSpeech("Groudon"))
    }

    @Test
    fun testMultiWordSpeciesPronunciation() {
        val input = "Tapu Koko is the guardian deity of Melemele Island."
        val expected = "Tahpoo Koko is the guardian deity of Melemele Island."
        val actual = PokemonPronunciation.formatForSpeech(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testPokemonFlavorTextNormalization() {
        val input = "POKéMON like Solgaleo were referred to in ancient literature as beasts that devour the sun."
        val expected = "Pokemon like Solgalayo were referred to in ancient literature as beasts that devour the sun."
        val actual = PokemonPronunciation.formatForSpeech(input)
        assertEquals(expected, actual)
    }
}
