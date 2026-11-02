package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class PokemonGameGuideExhaustiveTest {

    private fun resolveAssetFile(fileName: String): File {
        val candidates = listOf(
            File("src/main/assets/$fileName"),
            File("app/src/main/assets/$fileName"),
            File("Pokedex/PokedexApp/app/src/main/assets/$fileName")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate $fileName from working dir ${File(".").absolutePath}")
    }

    @Before
    fun setUp() {
        val guideJsonFile = resolveAssetFile("game_guides_index.json")
        val rawJson = guideJsonFile.readText(Charsets.UTF_8)
        PokemonGameGuideRepository.setCachedDataForTesting(rawJson)
    }

    @Test
    fun testGreatCraterOfPaldeaReturnsExhaustiveMasterGuide() {
        val result = PokemonGameGuideRepository.queryGuide("Great Crater of Paldea", editionId = "Violet")
        assertNotNull("Guide result must not be null", result)
        assertEquals("Great Crater of Paldea (Area Zero)", result.title)
        assertTrue("Category must reflect Area Zero encyclopedia", result.category.contains("AREA ZERO ENCYCLOPEDIA"))
        assertEquals(0.50f, result.pinX ?: 0f, 0.001f)
        assertEquals(0.50f, result.pinY ?: 0f, 0.001f)
        assertEquals("paldea_sv.png", result.mapAsset)

        // Check extensive narrative lore & history
        assertTrue("Must include meteorite impact origin", result.steps.any { it.contains("1,000,000 BC", ignoreCase = true) || it.contains("meteorite impact", ignoreCase = true) })
        assertTrue("Must include Heath expedition lore", result.steps.any { it.contains("Heath", ignoreCase = true) })
        assertTrue("Must include Violet Book documentation", result.steps.any { it.contains("Violet Book", ignoreCase = true) || it.contains("Scarlet", ignoreCase = true) })
        assertTrue("Must include Professor Turo/Sada research", result.steps.any { it.contains("Professor Turo", ignoreCase = true) || it.contains("Professor Sada", ignoreCase = true) })
        assertTrue("Must include Time Machine and Paradise Protection Protocol", result.steps.any { it.contains("Time Machine", ignoreCase = true) })
        assertTrue("Must include Terastal and Terapagos connection", result.steps.any { it.contains("Terastal", ignoreCase = true) || it.contains("Terapagos", ignoreCase = true) })

        // Check complete Paradox Pokémon tables
        assertTrue("Must list Iron Valiant for Violet", result.steps.any { it.contains("Iron Valiant", ignoreCase = true) })
        assertTrue("Must list Roaring Moon for Scarlet", result.steps.any { it.contains("Roaring Moon", ignoreCase = true) })
        assertTrue("Must list Iron Hands", result.steps.any { it.contains("Iron Hands", ignoreCase = true) })
        assertTrue("Must list Great Tusk", result.steps.any { it.contains("Great Tusk", ignoreCase = true) })

        // Check key items and unlocking walkthrough
        assertTrue("Must include Booster Energy", result.steps.any { it.contains("Booster Energy", ignoreCase = true) })
        assertTrue("Must include TM171 Tera Blast", result.steps.any { it.contains("Tera Blast", ignoreCase = true) })
        assertTrue("Must detail Research Station 1", result.steps.any { it.contains("Research Station No. 1", ignoreCase = true) })
        assertTrue("Must detail Research Station 4", result.steps.any { it.contains("Research Station No. 4", ignoreCase = true) })
    }

    @Test
    fun testAreaZeroQueryResolvesExhaustiveData() {
        val result = PokemonGameGuideRepository.queryGuide("Area Zero", editionId = "Scarlet")
        assertNotNull("Area Zero query must resolve", result)
        assertEquals("Great Crater of Paldea (Area Zero)", result.title)
        assertTrue("Must have multiple detailed walkthrough steps", result.steps.size >= 15)

        // Verify multi-paragraph spoken narration
        assertTrue("Spoken advice must be comprehensive and exceed 200 characters", result.spokenAdvice.length > 200)
        assertTrue("Spoken advice must mention meteorite impact or Heath", result.spokenAdvice.contains("crater", ignoreCase = true) && (result.spokenAdvice.contains("Heath", ignoreCase = true) || result.spokenAdvice.contains("million", ignoreCase = true)))
    }

    @Test
    fun testProfessorTuroQueryResolvesAreaZeroGuide() {
        val result = PokemonGameGuideRepository.queryGuide("Professor Turo", editionId = "Violet")
        assertNotNull("Professor Turo search must return Area Zero guide", result)
        assertEquals("Great Crater of Paldea (Area Zero)", result.title)
        assertTrue(result.steps.any { it.contains("Professor Turo", ignoreCase = true) })
        assertTrue(result.steps.any { it.contains("Zero Lab", ignoreCase = true) })
    }

    @Test
    fun testParadoxPokemonQueryResolvesAreaZeroGuide() {
        val result = PokemonGameGuideRepository.queryGuide("Paradox Pokemon", editionId = "Scarlet")
        assertNotNull("Paradox Pokemon search must return Area Zero guide", result)
        assertEquals("Great Crater of Paldea (Area Zero)", result.title)
        assertTrue(result.steps.any { it.contains("Great Tusk", ignoreCase = true) })
        assertTrue(result.steps.any { it.contains("Iron Treads", ignoreCase = true) })
    }

    @Test
    fun testPaldeaLandmarksAreEnrichedWithDescriptions() {
        val paldea = RegionMapLandmarksRepository.PALDEA_LANDMARKS
        val areaZero = paldea.firstOrNull { it.id == "area_zero" }
        assertNotNull("Area zero landmark must be present in Paldea", areaZero)
        assertEquals("Great Crater of Paldea (Area Zero)", areaZero!!.name)
        assertTrue("Area zero must have non-null tagline", !areaZero.tagline.isNullOrBlank())
        assertTrue("Area zero must have non-null description", !areaZero.description.isNullOrBlank())
        assertTrue("Area zero must have key locations", areaZero.keyLocations.isNotEmpty())
        assertTrue("Area zero must have guide advice", !areaZero.guideAdvice.isNullOrBlank())

        // Verify other Paldea landmarks are also enriched
        val mesagoza = paldea.firstOrNull { it.id == "mesagoza" }
        assertNotNull("Mesagoza must be present", mesagoza)
        assertTrue("Mesagoza must have non-null description", !mesagoza!!.description.isNullOrBlank())
        assertTrue("Mesagoza must have key locations", mesagoza.keyLocations.isNotEmpty())
    }
}
