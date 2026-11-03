package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun testAll28PokeballTopicsExistWithIconsAndValidGuides() {
        val topics = PokemonGameGuideRepository.getTopicsForCategory("pokeballs")
        assertEquals("pokeballs category must have 28 topics (27 balls + master_ball_targets)", 28, topics.size)

        val expectedIds = setOf(
            "master_ball", "master_ball_targets", "ultra_ball", "great_ball", "poke_ball",
            "premier_ball", "quick_ball", "dusk_ball", "timer_ball", "net_ball",
            "dive_ball", "nest_ball", "repeat_ball", "luxury_ball", "heal_ball",
            "fast_ball", "heavy_ball", "level_ball", "lure_ball", "love_ball",
            "friend_ball", "moon_ball", "safari_ball", "sport_ball", "dream_ball",
            "beast_ball", "cherish_ball", "park_ball"
        )
        val actualIds = topics.map { it.id }.toSet()
        assertEquals("All 28 expected Poké Ball topic IDs must be present", expectedIds, actualIds)

        for (topic in topics) {
            assertNotNull("Icon asset must not be null for ${topic.id}", topic.iconAsset)
            val guide = PokemonGameGuideRepository.queryGuide(topic.id, editionId = "Scarlet")
            assertNotNull("Guide result must not be null for ${topic.id}", guide)
            assertTrue("Steps must not be empty for ${topic.id}", guide!!.steps.isNotEmpty())
            assertNotNull("Pin X must be resolved for ${topic.id}", guide.pinX)
            assertNotNull("Pin Y must be resolved for ${topic.id}", guide.pinY)
        }
    }

    @Test
    fun testMasterBallTargetsAcrossEveryGenerationAndNeverSoldAtMarts() {
        val editionExpectations = listOf(
            Triple("Red", "kanto_rgby.png", "Mewtwo"),
            Triple("Yellow", "kanto_rgby.png", "Mewtwo"),
            Triple("Gold", "johto_gsc.png", "Raikou"),
            Triple("Crystal", "johto_gsc.png", "Raikou"),
            Triple("Ruby", "hoenn_rse.png", "Latios"),
            Triple("Emerald", "hoenn_rse.png", "Latios"),
            Triple("FireRed", "kanto_frlg.png", "Entei"),
            Triple("Diamond", "sinnoh_pt.png", "Mesprit"),
            Triple("Platinum", "sinnoh_pt.png", "Mesprit"),
            Triple("HeartGold", "johto_hgss.png", "Raikou"),
            Triple("Black", "unova_bw.png", "Tornadus"),
            Triple("Black 2", "unova_b2w2.png", "Kyurem"),
            Triple("X", "kalos_xy.png", "Mewtwo"),
            Triple("Omega Ruby", "hoenn_oras.png", "Deoxys"),
            Triple("Sun", "alola_sm.png", "Tapu"),
            Triple("Ultra Sun", "alola_usum.png", "Necrozma"),
            Triple("Let's Go Pikachu", "kanto_frlg.png", "Mewtwo"),
            Triple("Sword", "galar_swsh.png", "Calyrex"),
            Triple("Brilliant Diamond", "sinnoh_pt.png", "Mesprit"),
            Triple("Legends: Arceus", "hisui_pla.png", "Origin Ball"),
            Triple("Scarlet", "paldea_sv.png", "Koraidon"),
            Triple("Violet", "paldea_sv.png", "Miraidon")
        )

        for ((edition, expectedMap, expectedTargetKeyword) in editionExpectations) {
            val masterBallGuide = PokemonGameGuideRepository.queryGuide("master_ball", editionId = edition)
            assertNotNull("master_ball guide must exist for $edition", masterBallGuide)
            assertEquals("Map asset mismatch for master_ball in $edition", expectedMap, masterBallGuide!!.mapAsset)
            assertNull("Master Ball must never have subsequentAvailability Mart stock in $edition", masterBallGuide.subsequentAvailability)
            val combinedText = masterBallGuide.steps.joinToString(" ") + " " + masterBallGuide.spokenAdvice
            assertTrue(
                "master_ball in $edition must recommend $expectedTargetKeyword (got: $combinedText)",
                combinedText.contains(expectedTargetKeyword, ignoreCase = true)
            )

            val targetGuide = PokemonGameGuideRepository.queryGuide("master_ball_targets", editionId = edition)
            assertNotNull("master_ball_targets guide must exist for $edition", targetGuide)
            assertEquals("Map asset mismatch for master_ball_targets in $edition", expectedMap, targetGuide!!.mapAsset)
            assertNull("master_ball_targets must never have subsequentAvailability Mart stock in $edition", targetGuide.subsequentAvailability)
            val targetCombined = targetGuide.steps.joinToString(" ") + " " + targetGuide.spokenAdvice
            assertTrue(
                "master_ball_targets in $edition must recommend $expectedTargetKeyword (got: $targetCombined)",
                targetCombined.contains(expectedTargetKeyword, ignoreCase = true)
            )
        }
    }

    @Test
    fun testAll10RegionalEncyclopediaGuidesLoadAndAlignMaps() {
        val queries = listOf(
            Triple("Great Crater of Paldea", "Violet", "paldea_sv.png"),
            Triple("Cerulean Cave", "FireRed", "kanto_frlg.png"),
            Triple("Mt. Silver", "HeartGold", "johto_hgss.png"),
            Triple("Sky Pillar", "Emerald", "hoenn_rse.png"),
            Triple("Spear Pillar", "Platinum", "sinnoh_pt.png"),
            Triple("Giant Chasm", "Black 2", "unova_b2w2.png"),
            Triple("Prism Tower", "X", "kalos_xy.png"),
            Triple("Aether Paradise", "Ultra Sun", "alola_usum.png"),
            Triple("Slumbering Weald", "Sword", "galar_swsh.png"),
            Triple("Temple of Sinnoh", "Legends: Arceus", "hisui_pla.png")
        )

        for ((query, edition, expectedMap) in queries) {
            val guideJsonFile = resolveAssetFile("game_guides/${
                when (query) {
                    "Great Crater of Paldea" -> "paldea_great_crater_area_zero.json"
                    "Cerulean Cave" -> "kanto_cerulean_cave_mewtwo.json"
                    "Mt. Silver" -> "johto_mt_silver_red.json"
                    "Sky Pillar" -> "hoenn_sky_pillar_rayquaza.json"
                    "Spear Pillar" -> "sinnoh_spear_pillar_coronet.json"
                    "Giant Chasm" -> "unova_giant_chasm_kyurem.json"
                    "Prism Tower" -> "kalos_lumiose_prism_tower.json"
                    "Aether Paradise" -> "alola_aether_paradise.json"
                    "Slumbering Weald" -> "galar_slumbering_weald.json"
                    "Temple of Sinnoh" -> "hisui_temple_of_sinnoh.json"
                    else -> error("Unexpected query $query")
                }
            }")
            assertTrue("Encyclopedia JSON file must exist: ${guideJsonFile.path}", guideJsonFile.exists())

            val result = PokemonGameGuideRepository.queryGuide(query, editionId = edition)
            assertNotNull("Encyclopedia query '$query' ($edition) must resolve", result)
            assertEquals("Encyclopedia query '$query' ($edition) mapAsset mismatch", expectedMap, result!!.mapAsset)
            assertTrue("Encyclopedia query '$query' must have rich steps", result.steps.size >= 4)
            assertTrue("Encyclopedia query '$query' must have rich spokenAdvice", result.spokenAdvice.length > 100)
        }
    }

    @Test
    fun testAll10RegionsHaveEnrichedLandmarks() {
        val allRegions = listOf(
            "kanto_gen1.png" to RegionMapLandmarksRepository.KANTO_GB_LANDMARKS,
            "kanto_frlg.png" to RegionMapLandmarksRepository.KANTO_GBA_LANDMARKS,
            "johto_gen2.png" to RegionMapLandmarksRepository.JOHTO_LANDMARKS,
            "hoenn_rse.png" to RegionMapLandmarksRepository.HOENN_LANDMARKS,
            "sinnoh_dppt.png" to RegionMapLandmarksRepository.SINNOH_LANDMARKS,
            "unova_bw.png" to RegionMapLandmarksRepository.UNOVA_LANDMARKS,
            "kalos_xy.png" to RegionMapLandmarksRepository.KALOS_LANDMARKS,
            "alola_sm.png" to RegionMapLandmarksRepository.ALOLA_LANDMARKS,
            "galar_swsh.png" to RegionMapLandmarksRepository.GALAR_LANDMARKS,
            "hisui_pla.png" to RegionMapLandmarksRepository.HISUI_LANDMARKS,
            "paldea_sv.png" to RegionMapLandmarksRepository.PALDEA_LANDMARKS
        )

        for ((regionMap, landmarks) in allRegions) {
            assertTrue("Region $regionMap must have landmarks", landmarks.isNotEmpty())
            for (lm in landmarks) {
                assertTrue("Landmark ${lm.id} in $regionMap must have a tagline", !lm.tagline.isNullOrBlank())
                assertTrue("Landmark ${lm.id} in $regionMap must have a description", !lm.description.isNullOrBlank())
                assertTrue("Landmark ${lm.id} in $regionMap must have guideAdvice", !lm.guideAdvice.isNullOrBlank())
                assertTrue("Landmark ${lm.id} in $regionMap must have keyLocations", lm.keyLocations.isNotEmpty())
            }
        }
    }

    @Test
    fun testHelpWelcomeGuideReturnsCleanOverviewWith13Domains() {
        val welcome = PokemonGameGuideRepository.queryGuide("help", editionId = "Emerald")
        assertNotNull(welcome)
        assertEquals("Dexter's Strategy Guide (EMERALD)", welcome.title)
        assertEquals("master-ball.png", welcome.itemIconAsset)
        assertTrue(welcome.steps.any { it.contains("13 Strategy Domains", ignoreCase = true) })
        assertTrue(welcome.steps.any { it.contains("Master Ball", ignoreCase = true) })
    }
}
