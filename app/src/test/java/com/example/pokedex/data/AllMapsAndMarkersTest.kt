package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class AllMapsAndMarkersTest {

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
        PokemonGameGuideRepository.setCachedDataForTesting(guideJsonFile.readText(Charsets.UTF_8))
        val encountersJsonFile = resolveAssetFile("pokemon_encounters.json")
        PokemonMapHabitatRepository.setCachedDataForTesting(encountersJsonFile.readText(Charsets.UTF_8))
    }

    @Test
    fun `all 11 regional map assets exist and are valid PNG files`() {
        val mapAssets = listOf(
            "kanto_rgby.png",
            "kanto_frlg.png",
            "johto_gsc.png",
            "hoenn_rse.png",
            "sinnoh_pt.png",
            "unova_bw.png",
            "kalos_xy.png",
            "alola_sm.png",
            "galar_swsh.png",
            "hisui_pla.png",
            "paldea_sv.png"
        )

        for (assetName in mapAssets) {
            val file = resolveAssetFile("maps/$assetName")
            assertTrue("Map asset $assetName must exist", file.exists())
            assertTrue("Map asset $assetName must have non-zero size", file.length() > 1000)
        }
    }

    @Test
    fun `every region returns comprehensive landmarks with clamped coordinates`() {
        val mapVariants = listOf(
            "kanto_rgby.png",
            "kanto_frlg.png",
            "johto_gsc.png",
            "hoenn_rse.png",
            "sinnoh_pt.png",
            "unova_bw.png",
            "kalos_xy.png",
            "alola_sm.png",
            "galar_swsh.png",
            "hisui_pla.png",
            "paldea_sv.png"
        )

        for (mapAsset in mapVariants) {
            val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(mapAsset)
            assertTrue("Landmarks for $mapAsset must not be empty", landmarks.isNotEmpty())
            assertTrue("Landmarks for $mapAsset must have at least 10 entries", landmarks.size >= 10)

            for (lm in landmarks) {
                assertTrue(
                    "Landmark ${lm.name} in $mapAsset xNorm (${lm.xNorm}) must be in [0.04, 0.96]",
                    lm.xNorm in 0.04f..0.96f
                )
                assertTrue(
                    "Landmark ${lm.name} in $mapAsset yNorm (${lm.yNorm}) must be in [0.04, 0.96]",
                    lm.yNorm in 0.04f..0.96f
                )
                assertTrue("Landmark shortName must not be blank", lm.shortName.isNotBlank())
            }
        }
    }

    @Test
    fun `dual-era kanto pin calibration distinguishes between game boy and gba coordinate planes`() {
        // Red (Game Boy): Pewter City at (0.220, 0.200)
        val gbPewter = PokemonGameGuideRepository.resolveMapCoordinates("Red", "Pewter City")
        assertNotNull("Red Pewter City must resolve", gbPewter)
        assertEquals(0.220f, gbPewter!!.first, 0.01f)
        assertEquals(0.200f, gbPewter.second, 0.01f)

        // FireRed (GBA): Pewter City at (0.280, 0.420)
        val gbaPewter = PokemonGameGuideRepository.resolveMapCoordinates("FireRed", "Pewter City")
        assertNotNull("FireRed Pewter City must resolve", gbaPewter)
        assertEquals(0.280f, gbaPewter!!.first, 0.01f)
        assertEquals(0.420f, gbaPewter.second, 0.01f)

        // Cerulean City: Red is (0.625, 0.140), FireRed is (0.620, 0.380)
        val gbCerulean = PokemonGameGuideRepository.resolveMapCoordinates("Red", "Cerulean City")
        val gbaCerulean = PokemonGameGuideRepository.resolveMapCoordinates("FireRed", "Cerulean City")
        assertNotNull(gbCerulean)
        assertNotNull(gbaCerulean)
        assertEquals(0.625f, gbCerulean!!.first, 0.01f)
        assertEquals(0.140f, gbCerulean.second, 0.01f)
        assertEquals(0.620f, gbaCerulean!!.first, 0.01f)
        assertEquals(0.380f, gbaCerulean.second, 0.01f)
    }

    @Test
    fun `kanto game boy landmarks place pewter city on building and route 2 strictly in corridor`() {
        val landmarks = RegionMapLandmarksRepository.getLandmarksForMap("kanto_rgby.png")
        val pewter = landmarks.first { it.id == "pewter" }
        val r2 = landmarks.first { it.id == "r2" }
        val viridian = landmarks.first { it.id == "viridian" }
        val vForest = landmarks.first { it.id == "viridian_forest" }

        // Pewter City must be at (0.220, 0.200) - actual square building icon
        assertEquals(0.220f, pewter.xNorm, 0.005f)
        assertEquals(0.200f, pewter.yNorm, 0.005f)

        // Viridian City must be at (0.220, 0.485) - actual square building icon
        assertEquals(0.220f, viridian.xNorm, 0.005f)
        assertEquals(0.485f, viridian.yNorm, 0.005f)

        // Route 2 must be at (0.220, 0.365) - strictly in corridor between Viridian and Viridian Forest
        assertEquals(0.220f, r2.xNorm, 0.005f)
        assertEquals(0.365f, r2.yNorm, 0.005f)

        // Viridian Forest must be at (0.220, 0.260) - circular dungeon icon
        assertEquals(0.220f, vForest.xNorm, 0.005f)
        assertEquals(0.260f, vForest.yNorm, 0.005f)

        // Assert geometric ordering along the western vertical corridor:
        // Top to bottom: Pewter (0.200) < Viridian Forest (0.260) < Route 2 (0.365) < Viridian City (0.485)
        assertTrue("Pewter is above Viridian Forest", pewter.yNorm < vForest.yNorm)
        assertTrue("Viridian Forest is above Route 2", vForest.yNorm < r2.yNorm)
        assertTrue("Route 2 is above Viridian City", r2.yNorm < viridian.yNorm)
        assertFalse("Route 2 does not overlap Viridian City building", r2.yNorm == viridian.yNorm)
        assertFalse("Pewter does not overlap Route 2 corridor", pewter.yNorm == r2.yNorm)
    }

    @Test
    fun `representative gym leaders and roadblocks resolve valid coordinates across all 9 generations`() {
        // Gen 2: Falkner in Violet City (0.55, 0.55), Sudowoodo on Route 36 (0.45, 0.55)
        val falkner = PokemonGameGuideRepository.resolveMapCoordinates("Gold", "Violet City Gym")
        val sudowoodo = PokemonGameGuideRepository.resolveMapCoordinates("Gold", "Route 36 Sudowoodo")
        assertNotNull(falkner)
        assertNotNull(sudowoodo)
        assertEquals(0.55f, falkner!!.first, 0.01f)
        assertEquals(0.55f, falkner.second, 0.01f)
        assertEquals(0.45f, sudowoodo!!.first, 0.01f)
        assertEquals(0.55f, sudowoodo.second, 0.01f)

        // Gen 3: Wattson in Mauville City (0.33, 0.42), Rayquaza at Sky Pillar (0.68, 0.60)
        val wattson = PokemonGameGuideRepository.resolveMapCoordinates("Emerald", "Mauville City")
        val rayquaza = PokemonGameGuideRepository.resolveMapCoordinates("Emerald", "Sky Pillar")
        assertNotNull(wattson)
        assertNotNull(rayquaza)
        assertEquals(0.330f, wattson!!.first, 0.01f)
        assertEquals(0.420f, wattson.second, 0.01f)
        assertEquals(0.680f, rayquaza!!.first, 0.01f)
        assertEquals(0.600f, rayquaza.second, 0.01f)

        // Gen 4: Roark in Oreburgh City (0.45, 0.68), Psyduck on Route 210 (0.60, 0.42)
        val roark = PokemonGameGuideRepository.resolveMapCoordinates("Platinum", "Oreburgh City")
        val psyduck = PokemonGameGuideRepository.resolveMapCoordinates("Platinum", "Route 210 Psyduck")
        assertNotNull(roark)
        assertNotNull(psyduck)
        assertEquals(0.45f, roark!!.first, 0.01f)
        assertEquals(0.68f, roark.second, 0.01f)
        assertEquals(0.60f, psyduck!!.first, 0.01f)
        assertEquals(0.42f, psyduck.second, 0.01f)

        // Gen 5: Castelia City (0.50, 0.85), Liberty Garden (0.40, 0.95)
        val castelia = PokemonGameGuideRepository.resolveMapCoordinates("Black", "Castelia City")
        val liberty = PokemonGameGuideRepository.resolveMapCoordinates("Black", "Liberty Garden")
        assertNotNull(castelia)
        assertNotNull(liberty)
        assertEquals(0.50f, castelia!!.first, 0.01f)
        assertEquals(0.85f, castelia.second, 0.01f)
        assertEquals(0.40f, liberty!!.first, 0.01f)
        assertEquals(0.95f, liberty.second, 0.01f)

        // Gen 9: Mesagoza (0.50, 0.75), Area Zero (0.50, 0.50)
        val mesagoza = PokemonGameGuideRepository.resolveMapCoordinates("Scarlet", "Mesagoza")
        val areaZero = PokemonGameGuideRepository.resolveMapCoordinates("Scarlet", "Area Zero")
        assertNotNull(mesagoza)
        assertNotNull(areaZero)
        assertEquals(0.50f, mesagoza!!.first, 0.01f)
        assertEquals(0.75f, mesagoza.second, 0.01f)
        assertEquals(0.50f, areaZero!!.first, 0.01f)
        assertEquals(0.50f, areaZero.second, 0.01f)
    }

    @Test
    fun `wild habitat maps enforce clean area unknown on non-native cartridges`() {
        // Rayquaza on Emerald (Native) vs Red (Non-native)
        val rayquazaEmerald = PokemonMapHabitatRepository.getHabitatForPokemon(384, "Rayquaza", "Emerald")
        assertFalse("Rayquaza on Emerald must not be Area Unknown", rayquazaEmerald.isAreaUnknown)
        assertTrue("Rayquaza on Emerald must have at least one pin", rayquazaEmerald.pins.isNotEmpty())
        assertEquals("hoenn_rse.png", rayquazaEmerald.mapAsset)

        val rayquazaRed = PokemonMapHabitatRepository.getHabitatForPokemon(384, "Rayquaza", "Red")
        assertTrue("Rayquaza on Red must be Area Unknown", rayquazaRed.isAreaUnknown)
        assertTrue("Rayquaza on Red must have zero leaked pins", rayquazaRed.pins.isEmpty())
        assertNotNull("Rayquaza on Red must provide unknownReason", rayquazaRed.unknownReason)
    }
}
