package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class PokemonGameGuideRepositoryTest {

    @Before
    fun setUp() {
        val candidates = listOf(
            File("src/main/assets/game_guides_index.json"),
            File("app/src/main/assets/game_guides_index.json"),
            File("Pokedex/PokedexApp/app/src/main/assets/game_guides_index.json")
        )
        val assetFile = candidates.firstOrNull { it.exists() }
        assertNotNull("game_guides_index.json must exist in project assets", assetFile)
        PokemonGameGuideRepository.setCachedDataForTesting(assetFile!!.readText())
    }

    @Test
    fun testCategoryLoading() {
        val categories = PokemonGameGuideRepository.getCategories(context = null)
        assertTrue("Expected at least 12 categories, found ${categories.size}", categories.size >= 12)
        val catIds = categories.map { it.id }
        assertTrue("Categories must include key_items", catIds.contains("key_items"))
        assertTrue("Categories must include battle_items", catIds.contains("battle_items"))
        assertTrue("Categories must include missions_roadblocks", catIds.contains("missions_roadblocks"))
        assertTrue("Categories must include legendaries", catIds.contains("legendaries"))
        assertTrue("Categories must include puzzles_dungeons", catIds.contains("puzzles_dungeons"))
    }

    @Test
    fun testSSAnneHarborPinInClassicAndRemake() {
        // 1. Classic Game Boy Red
        val redAnne = PokemonGameGuideRepository.queryGuide("S.S. Anne", editionId = "Red")
        assertNotNull(redAnne)
        assertEquals("Red", redAnne!!.editionName)
        assertEquals(0.625f, redAnne.pinX ?: 0f, 0.001f)
        assertEquals(0.605f, redAnne.pinY ?: 0f, 0.001f)
        assertTrue(redAnne.steps.any { it.contains("Official Slogan") || it.contains("Captain") || it.contains("Luxury Cruise Liner") })

        // 2. GBA FireRed
        val frAnne = PokemonGameGuideRepository.queryGuide("S.S. Anne", editionId = "FireRed")
        assertNotNull(frAnne)
        assertEquals("FireRed", frAnne!!.editionName)
        assertEquals(0.620f, frAnne.pinX ?: 0f, 0.001f)
        assertEquals(0.720f, frAnne.pinY ?: 0f, 0.001f)
        assertTrue(frAnne.steps.any { it.contains("Official Slogan") || it.contains("Captain") || it.contains("Luxury Cruise Liner") })
    }

    @Test
    fun testRemakeCartridgeInheritance() {
        // FireRed should inherit Red's Bicycle guide without cross-generation penalty
        val frBike = PokemonGameGuideRepository.queryGuide("Bicycle", editionId = "FireRed")
        assertNotNull(frBike)
        assertFalse("FireRed should inherit Bicycle natively", frBike!!.category.contains("CROSS-GENERATION"))
        assertTrue(frBike.steps.any { it.contains("Bike Voucher") || it.contains("Miracle Cycle") || it.contains("Vermilion City") })

        // HeartGold should inherit Gold's SquirtBottle guide
        val hgSquirt = PokemonGameGuideRepository.queryGuide("Sudowoodo", editionId = "HeartGold")
        assertNotNull(hgSquirt)
        System.err.println("--- DEBUG HG SUDOWOODO: title=${hgSquirt?.title}, category=${hgSquirt?.category}, steps=${hgSquirt?.steps}")
        assertFalse("HeartGold should resolve Sudowoodo natively", hgSquirt!!.category.contains("CROSS-GENERATION"))
        assertTrue(hgSquirt.steps.any { it.contains("Whitney") || it.contains("SquirtBottle") || it.contains("Route 36") })

        // Omega Ruby should inherit Hoenn base guide data
        val orasAcro = PokemonGameGuideRepository.queryGuide("Acro Bike", editionId = "Omega Ruby")
        assertNotNull(orasAcro)
        assertFalse("Omega Ruby should inherit Acro Bike natively", orasAcro!!.category.contains("CROSS-GENERATION"))
        assertTrue(orasAcro.steps.any { it.contains("Mauville") || it.contains("Rydel") })
    }

    @Test
    fun testMultiRegionLandmarkProfiles() {
        // Kanto - Pallet Town
        val pallet = PokemonGameGuideRepository.queryGuide("Pallet Town", editionId = "Red")
        assertNotNull(pallet)
        assertTrue(pallet!!.steps.any { it.contains("Shades of your journey await!") })
        assertTrue(pallet.steps.any { it.contains("Prof. Oak's Research Lab") })

        // Johto - Goldenrod City
        val goldenrod = PokemonGameGuideRepository.queryGuide("Goldenrod City", editionId = "HeartGold")
        assertNotNull(goldenrod)
        System.err.println("--- DEBUG GOLDENROD: title=${goldenrod?.title}, category=${goldenrod?.category}, steps=${goldenrod?.steps}")
        assertTrue(goldenrod!!.steps.any { it.contains("The Festive City of Opulent Charm") })
        assertTrue(goldenrod.steps.any { it.contains("Whitney") })
        assertTrue(goldenrod.steps.any { it.contains("Radio Tower") })

        // Hoenn - Slateport City
        val slateport = PokemonGameGuideRepository.queryGuide("Slateport City", editionId = "Emerald")
        assertNotNull(slateport)
        assertTrue(slateport!!.steps.any { it.contains("The Port Where People and Pokémon Cross Paths") })
        assertTrue(slateport.steps.any { it.contains("Stern's Shipyard") })
    }

    @Test
    fun testRouteAliasesAndLandmarkSearch() {
        // Short route alias r1
        val r1 = PokemonGameGuideRepository.queryGuide("r1", editionId = "Red")
        assertNotNull(r1)
        assertTrue(r1!!.title.contains("Route 1"))

        // Short route alias r104
        val r104 = PokemonGameGuideRepository.queryGuide("r104", editionId = "Emerald")
        assertNotNull(r104)
        assertTrue(r104!!.title.contains("Route 104"))

        // Sky Pillar
        val skyPillar = PokemonGameGuideRepository.queryGuide("Sky Pillar", editionId = "Emerald")
        assertNotNull(skyPillar)
        assertTrue(skyPillar!!.title.contains("Sky Pillar"))
        assertTrue(skyPillar.steps.any { it.contains("Rayquaza") })
    }
}
