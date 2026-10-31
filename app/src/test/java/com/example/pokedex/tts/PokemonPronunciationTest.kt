package com.example.pokedex.tts

import com.example.pokedex.camera.PokemonFormDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

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

    private fun loadProductionPronunciationAssets() {
        val candidates = listOf(
            File("src/main/assets/pokemon_pronunciations.json"),
            File("app/src/main/assets/pokemon_pronunciations.json")
        )
        val jsonFile = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate pokemon_pronunciations.json from working dir ${File(".").absolutePath}")
        val map = mutableMapOf<String, String>()
        val entryRegex = Regex(""""([^"]+)"\s*:\s*"([^"]+)"""")
        for (match in entryRegex.findAll(jsonFile.readText(Charsets.UTF_8))) {
            val k = match.groupValues[1]
            val v = match.groupValues[2]
            map[k.lowercase()] = v
        }
        PokemonPronunciation.initForTesting(map)
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

    @Test
    fun testAllSixRegisAnimePronunciationFromProductionJson() {
        loadProductionPronunciationAssets()

        assertEquals("Rejirock", PokemonPronunciation.formatForSpeech("Regirock"))
        assertEquals("Rejice", PokemonPronunciation.formatForSpeech("Regice"))
        assertEquals("Rejisteel", PokemonPronunciation.formatForSpeech("Registeel"))
        assertEquals("Rejigigas", PokemonPronunciation.formatForSpeech("Regigigas"))
        assertEquals("Rejieleki", PokemonPronunciation.formatForSpeech("Regieleki"))
        assertEquals("Rejidrago", PokemonPronunciation.formatForSpeech("Regidrago"))

        val sentence = "Regigigas towed the continents and created Regirock, Regice, Registeel, Regieleki, and Regidrago."
        val expected = "Rejigigas towed the continents and created Rejirock, Rejice, Rejisteel, Rejieleki, and Rejidrago."
        assertEquals(expected, PokemonPronunciation.formatForSpeech(sentence))
    }

    @Test
    fun testHisuiNativeAndHisuianRegionalFormsFromProductionJson() {
        loadProductionPronunciationAssets()

        // Region & adjective
        assertEquals("Heesooee", PokemonPronunciation.formatForSpeech("Hisui"))
        assertEquals("Heesooeean", PokemonPronunciation.formatForSpeech("Hisuian"))
        assertEquals("Heesooee region", PokemonPronunciation.formatForSpeech("Hisui-region"))

        // All 7 Hisui-native species (#899-#905)
        assertEquals("Weerdeer", PokemonPronunciation.formatForSpeech("Wyrdeer"))
        assertEquals("Kleevor", PokemonPronunciation.formatForSpeech("Kleavor"))
        assertEquals("Ursaloona", PokemonPronunciation.formatForSpeech("Ursaluna"))
        assertEquals("Baskyuleejun", PokemonPronunciation.formatForSpeech("Basculegion"))
        assertEquals("Sneezler", PokemonPronunciation.formatForSpeech("Sneasler"))
        assertEquals("Overkwil", PokemonPronunciation.formatForSpeech("Overqwil"))
        assertEquals("Enamorus", PokemonPronunciation.formatForSpeech("Enamorus"))

        // Hisuian regional forms via PokemonFormDetector + PokemonPronunciation
        val hisuianForms = mapOf(
            "growlithe-hisui" to "Heesooeean Growlithe",
            "arcanine-hisui" to "Heesooeean Arcanine",
            "voltorb-hisui" to "Heesooeean Voltorb",
            "electrode-hisui" to "Heesooeean Electrode",
            "typhlosion-hisui" to "Heesooeean Typhlosion",
            "qwilfish-hisui" to "Heesooeean Qwilfish",
            "sneasel-hisui" to "Heesooeean Sneasel",
            "samurott-hisui" to "Heesooeean Samurott",
            "lilligant-hisui" to "Heesooeean Lilligant",
            "zorua-hisui" to "Heesooeean Zorua",
            "zoroark-hisui" to "Heesooeean Zoroark",
            "braviary-hisui" to "Heesooeean Braviary",
            "sliggoo-hisui" to "Heesooeean Sliggoo",
            "goodra-hisui" to "Heesooeean Goodra",
            "avalugg-hisui" to "Heesooeean Avalugg",
            "decidueye-hisui" to "Heesooeean Decidueye"
        )
        for ((formId, expectedSpeech) in hisuianForms) {
            val displayName = PokemonFormDetector.formatFormDisplayName(formId)
            assertEquals("Failed for $formId (display=$displayName)", expectedSpeech, PokemonPronunciation.formatForSpeech(displayName))
        }
    }

    @Test
    fun testFormDetectorDefaultSuffixSuppressionAndSpecialForms() {
        loadProductionPronunciationAssets()

        assertEquals("Basculegion", PokemonFormDetector.formatFormDisplayName("basculegion-male"))
        assertEquals("Baskyuleejun", PokemonPronunciation.formatForSpeech(PokemonFormDetector.formatFormDisplayName("basculegion-male")))

        assertEquals("Enamorus", PokemonFormDetector.formatFormDisplayName("enamorus-incarnate"))
        assertEquals("Enamorus", PokemonPronunciation.formatForSpeech(PokemonFormDetector.formatFormDisplayName("enamorus-incarnate")))

        assertEquals("Therian Forme Enamorus", PokemonFormDetector.formatFormDisplayName("enamorus-therian"))
        assertEquals("Therian Forme Enamorus", PokemonPronunciation.formatForSpeech(PokemonFormDetector.formatFormDisplayName("enamorus-therian")))

        assertEquals("Giratina", PokemonFormDetector.formatFormDisplayName("giratina-altered"))
        assertEquals("Geerateena", PokemonPronunciation.formatForSpeech(PokemonFormDetector.formatFormDisplayName("giratina-altered")))

        assertEquals("Bloodmoon Ursaluna", PokemonFormDetector.formatFormDisplayName("ursaluna-bloodmoon"))
        assertEquals("Bloodmoon Ursaloona", PokemonPronunciation.formatForSpeech(PokemonFormDetector.formatFormDisplayName("ursaluna-bloodmoon")))
    }

    @Test
    fun testNaturalSpeechGen1ToGen9AnimePronunciationsFromProductionJson() {
        loadProductionPronunciationAssets()

        // Critical user requirements: Lapras and Eevee must be 100% natural, never chopped into "Lap-rus" or "Ee-vee"
        assertEquals("Lapras", PokemonPronunciation.formatForSpeech("Lapras"))
        assertEquals("Eevee", PokemonPronunciation.formatForSpeech("Eevee"))
        assertEquals("Pikachu", PokemonPronunciation.formatForSpeech("Pikachu"))
        assertEquals("Charizard", PokemonPronunciation.formatForSpeech("Charizard"))
        assertEquals("Blastoise", PokemonPronunciation.formatForSpeech("Blastoise"))
        assertEquals("Venusaur", PokemonPronunciation.formatForSpeech("Venusaur"))
        assertEquals("Gairados", PokemonPronunciation.formatForSpeech("Gyarados"))
        assertEquals("Eggsecute", PokemonPronunciation.formatForSpeech("Exeggcute"))
        assertEquals("Eggsecutor", PokemonPronunciation.formatForSpeech("Exeggutor"))

        // Gen 2
        assertEquals("Feraligatr", PokemonPronunciation.formatForSpeech("Feraligatr"))
        assertEquals("Typhlosion", PokemonPronunciation.formatForSpeech("Typhlosion"))
        assertEquals("Sweekoon", PokemonPronunciation.formatForSpeech("Suicune"))
        assertEquals("Raikou", PokemonPronunciation.formatForSpeech("Raikou"))
        assertEquals("Entei", PokemonPronunciation.formatForSpeech("Entei"))

        // Gen 3
        assertEquals("Seviper", PokemonPronunciation.formatForSpeech("Seviper"))
        assertEquals("Milotic", PokemonPronunciation.formatForSpeech("Milotic"))
        assertEquals("Gardevoir", PokemonPronunciation.formatForSpeech("Gardevoir"))
        assertEquals("Raykwayza", PokemonPronunciation.formatForSpeech("Rayquaza"))
        assertEquals("Kyogur", PokemonPronunciation.formatForSpeech("Kyogre"))
        assertEquals("Growdon", PokemonPronunciation.formatForSpeech("Groudon"))

        // Gen 4
        assertEquals("Lucario", PokemonPronunciation.formatForSpeech("Lucario"))
        assertEquals("Garchomp", PokemonPronunciation.formatForSpeech("Garchomp"))
        assertEquals("Arkayus", PokemonPronunciation.formatForSpeech("Arceus"))

        // Gen 5
        assertEquals("Hydreigon", PokemonPronunciation.formatForSpeech("Hydreigon"))
        assertEquals("Zoroark", PokemonPronunciation.formatForSpeech("Zoroark"))

        // Gen 6
        assertEquals("Greninja", PokemonPronunciation.formatForSpeech("Greninja"))
        assertEquals("Zerneas", PokemonPronunciation.formatForSpeech("Xerneas"))
        assertEquals("Eevaltal", PokemonPronunciation.formatForSpeech("Yveltal"))

        // Gen 7
        assertEquals("Decidueye", PokemonPronunciation.formatForSpeech("Decidueye"))
        assertEquals("Nyeheeleego", PokemonPronunciation.formatForSpeech("Nihilego"))
        assertEquals("Solgalayo", PokemonPronunciation.formatForSpeech("Solgaleo"))
        assertEquals("Lunala", PokemonPronunciation.formatForSpeech("Lunala"))

        // Gen 8
        assertEquals("Zashian", PokemonPronunciation.formatForSpeech("Zacian"))
        assertEquals("Zamazenta", PokemonPronunciation.formatForSpeech("Zamazenta"))

        // Gen 9
        assertEquals("Woh Chyen", PokemonPronunciation.formatForSpeech("Wo-Chien"))
        assertEquals("Chyen Pow", PokemonPronunciation.formatForSpeech("Chien-Pao"))
        assertEquals("Ting Loo", PokemonPronunciation.formatForSpeech("Ting-Lu"))
        assertEquals("Chee Yoo", PokemonPronunciation.formatForSpeech("Chi-Yu"))
        assertEquals("Goldengo", PokemonPronunciation.formatForSpeech("Gholdengo"))
        assertEquals("Ogerpon", PokemonPronunciation.formatForSpeech("Ogerpon"))

        // Gender & Punctuation
        assertTrue(PokemonPronunciation.formatForSpeech("Nidoran♀").contains("Female"))
        assertTrue(PokemonPronunciation.formatForSpeech("Nidoran♂").contains("Male"))
        assertEquals("Mister Mime", PokemonPronunciation.formatForSpeech("Mr. Mime"))
        assertEquals("Mime Junior", PokemonPronunciation.formatForSpeech("Mime Jr."))
        assertEquals("Mister Rime", PokemonPronunciation.formatForSpeech("Mr. Rime"))
        assertEquals("Type Null", PokemonPronunciation.formatForSpeech("Type: Null"))
        assertEquals("Porygon Two", PokemonPronunciation.formatForSpeech("Porygon2"))
        assertEquals("Porygon Z", PokemonPronunciation.formatForSpeech("Porygon-Z"))
        assertEquals("Ho-Oh", PokemonPronunciation.formatForSpeech("Ho-Oh"))
    }
}
