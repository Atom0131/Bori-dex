package com.example.pokedex.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionMapLandmarksTest {

    private val allMapAssets = listOf(
        "kanto_rgby_pocket.png",
        "kanto_frlg.png",
        "johto_gsc.png",
        "johto_hgss.png",
        "hoenn_rse.png",
        "hoenn_oras.png",
        "sinnoh_pt.png",
        "unova_bw.png",
        "unova_b2w2.png",
        "kalos_xy.png",
        "alola_sm.png",
        "alola_usum.png",
        "galar_swsh.png",
        "hisui_pla.png",
        "paldea_sv.png"
    )

    @Test
    fun testEveryRegionMapHasLandmarks() {
        allMapAssets.forEach { asset ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(asset)
            assertNotNull("Landmarks should not be null for $asset", landmarks)
            assertTrue("Expected at least 10 landmarks for $asset, found ${landmarks.size}", landmarks.size >= 10)
        }
    }

    @Test
    fun testEveryRegionMapHasTownsAndRoutes() {
        allMapAssets.forEach { asset ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(asset)
            val hasTownOrCity = landmarks.any { it.type == MapLandmarkType.TOWN || it.type == MapLandmarkType.CITY }
            assertTrue("Map $asset must include at least one TOWN or CITY", hasTownOrCity)

            // Standard mainline regions have designated routes; Hisui has wild fieldland arenas
            if (!asset.startsWith("hisui")) {
                val hasRoute = landmarks.any { it.type == MapLandmarkType.ROUTE }
                assertTrue("Map $asset must include at least one connecting ROUTE", hasRoute)
            } else {
                val hasWildLandmarks = landmarks.any { it.type == MapLandmarkType.LANDMARK }
                assertTrue("Map $asset must include wild fieldland arenas", hasWildLandmarks)
            }
        }
    }

    @Test
    fun testLandmarkCoordinatesAreNormalized() {
        allMapAssets.forEach { asset ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(asset)
            landmarks.forEach { landmark ->
                assertTrue(
                    "Landmark ${landmark.id} on $asset xNorm (${landmark.xNorm}) must be in range [0.02, 0.98]",
                    landmark.xNorm in 0.02f..0.98f
                )
                assertTrue(
                    "Landmark ${landmark.id} on $asset yNorm (${landmark.yNorm}) must be in range [0.02, 0.98]",
                    landmark.yNorm in 0.02f..0.98f
                )
            }
        }
    }

    @Test
    fun testLandmarkNamesAndIdsAreValid() {
        allMapAssets.forEach { asset ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(asset)
            val seenIds = mutableSetOf<String>()
            landmarks.forEach { landmark ->
                assertFalse("Landmark id cannot be blank on $asset", landmark.id.isBlank())
                assertFalse("Landmark name cannot be blank on $asset", landmark.name.isBlank())
                assertFalse("Landmark shortName cannot be blank on $asset", landmark.shortName.isBlank())
                assertTrue(
                    "Duplicate landmark id '${landmark.id}' detected on $asset",
                    seenIds.add(landmark.id)
                )
            }
        }
    }

    @Test
    fun testLandmarksResolutionFromEditionNames() {
        val testEditions = listOf(
            "Red" to "Pallet",
            "FireRed" to "Pallet",
            "Gold" to "New Bark",
            "HeartGold" to "New Bark",
            "Emerald" to "Littleroot",
            "Platinum" to "Twinleaf",
            "Black" to "Nuvema",
            "X" to "Vaniville",
            "Sun" to "Iki Town",
            "Sword" to "Postwick",
            "Legends: Arceus" to "Jubilife",
            "Scarlet" to "Cabo Poco"
        )

        testEditions.forEach { (edition, expectedTownSnippet) ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(edition)
            assertTrue(
                "Edition $edition should return landmarks containing '$expectedTownSnippet'",
                landmarks.any { it.name.contains(expectedTownSnippet, ignoreCase = true) || it.shortName.contains(expectedTownSnippet, ignoreCase = true) }
            )
        }
    }

    @Test
    fun testKantoLandmarksIncludeKeyRoutesAndTowns() {
        val kantoGb = RegionMapLandmarksRepository.KANTO_GB_LANDMARKS
        assertTrue(kantoGb.any { it.id == "pallet" && it.type == MapLandmarkType.TOWN })
        assertTrue(kantoGb.any { it.id == "cerulean" && it.type == MapLandmarkType.CITY })
        assertTrue(kantoGb.any { it.id == "saffron" && it.type == MapLandmarkType.CITY })
        assertTrue(kantoGb.any { it.id == "r1" && it.type == MapLandmarkType.ROUTE })
        assertTrue(kantoGb.any { it.id == "r2" && it.type == MapLandmarkType.ROUTE })

        val kantoGba = RegionMapLandmarksRepository.KANTO_GBA_LANDMARKS
        assertTrue(kantoGba.any { it.id == "one_island" && it.type == MapLandmarkType.TOWN })
        assertTrue(kantoGba.any { it.id == "r21" && it.type == MapLandmarkType.ROUTE })
    }

    @Test
    fun testHoennLandmarksIncludeKeyRoutesAndTowns() {
        val hoenn = RegionMapLandmarksRepository.HOENN_LANDMARKS
        assertTrue(hoenn.any { it.id == "littleroot" && it.type == MapLandmarkType.TOWN })
        assertTrue(hoenn.any { it.id == "rustboro" && it.type == MapLandmarkType.CITY })
        assertTrue(hoenn.any { it.id == "sootopolis" && it.type == MapLandmarkType.CITY })
        assertTrue(hoenn.any { it.id == "r104" && it.type == MapLandmarkType.ROUTE })
        assertTrue(hoenn.any { it.id == "r119" && it.type == MapLandmarkType.ROUTE })
    }

    @Test
    fun testSinnohLandmarksIncludeKeyRoutesAndTowns() {
        val sinnoh = RegionMapLandmarksRepository.SINNOH_LANDMARKS
        assertTrue(sinnoh.any { it.id == "twinleaf" && it.type == MapLandmarkType.TOWN })
        assertTrue(sinnoh.any { it.id == "jubilife" && it.type == MapLandmarkType.CITY })
        assertTrue(sinnoh.any { it.id == "sunyshore" && it.type == MapLandmarkType.CITY })
        assertTrue(sinnoh.any { it.id == "r201" && it.type == MapLandmarkType.ROUTE })
        assertTrue(sinnoh.any { it.id == "r210" && it.type == MapLandmarkType.ROUTE })
    }

    @Test
    fun testBackwardCompatibilityAlias() {
        org.junit.Assert.assertEquals(MapLandmarkType.POI, MapLandmarkType.LANDMARK)
    }

    @Test
    fun testAllSevenLandmarkTypesExistAcrossRegions() {
        val allTypes = MapLandmarkType.values().toSet()
        val foundTypes = mutableSetOf<MapLandmarkType>()
        allMapAssets.forEach { asset ->
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(asset)
            landmarks.forEach { foundTypes.add(it.type) }
        }
        allTypes.forEach { type ->
            assertTrue("Expected landmark type $type to be present in regional datasets", foundTypes.contains(type))
        }
    }

    @Test
    fun testFilteringByLandmarkTypes() {
        val hoenn = RegionMapLandmarksRepository.HOENN_LANDMARKS
        val townsOnly = hoenn.filter { it.type in setOf(MapLandmarkType.TOWN, MapLandmarkType.CITY) }
        assertTrue(townsOnly.all { it.type == MapLandmarkType.TOWN || it.type == MapLandmarkType.CITY })
        assertTrue(townsOnly.any { it.id == "littleroot" })
        assertFalse(townsOnly.any { it.type == MapLandmarkType.ROUTE })

        val secretsOnly = hoenn.filter { it.type == MapLandmarkType.SECRET }
        assertTrue(secretsOnly.any { it.id == "sky_pillar" })
        assertTrue(secretsOnly.any { it.id == "faraway_island" })
        assertTrue(secretsOnly.any { it.id == "r134" })

        val puzzlesOnly = hoenn.filter { it.type == MapLandmarkType.PUZZLE }
        assertTrue(puzzlesOnly.any { it.id == "trick_house" })
        assertTrue(puzzlesOnly.any { it.id == "battle_frontier" })
    }
}
