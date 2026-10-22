package com.example.pokedex.camera

import com.example.pokedex.data.PokemonEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonFormDetectorTest {

    private fun createDummyPokemon(id: Int, name: String, forms: String = ""): PokemonEntity {
        return PokemonEntity(
            id = id,
            name = name,
            type = "Unknown",
            height = "1.0 m",
            weight = "10.0 kg",
            flavorText = "Test flavor",
            spriteUrl = "http://example.com/$id.png",
            shinySpriteUrl = "http://example.com/shiny/$id.png",
            evolvesTo = "",
            forms = forms,
            pokedexEntries = "{}"
        )
    }

    @Test
    fun testFormatFormDisplayName() {
        assertEquals("Mega Rayquaza", PokemonFormDetector.formatFormDisplayName("rayquaza-mega"))
        assertEquals("Primal Groudon", PokemonFormDetector.formatFormDisplayName("groudon-primal"))
        assertEquals("Alolan Raichu", PokemonFormDetector.formatFormDisplayName("raichu-alola"))
        assertEquals("Hisuian Arcanine", PokemonFormDetector.formatFormDisplayName("arcanine-hisui"))
        assertEquals("Galarian Ponyta", PokemonFormDetector.formatFormDisplayName("ponyta-galar"))
        assertEquals("Paldean Wooper", PokemonFormDetector.formatFormDisplayName("wooper-paldea"))
        assertEquals("Mega Charizard X", PokemonFormDetector.formatFormDisplayName("charizard-mega-x"))
        assertEquals("Mega Charizard Y", PokemonFormDetector.formatFormDisplayName("charizard-mega-y"))
        assertEquals("Charizard Gigantamax", PokemonFormDetector.formatFormDisplayName("charizard-gmax"))
        assertEquals("Dusk Mane Necrozma", PokemonFormDetector.formatFormDisplayName("necrozma-dusk"))
        assertEquals("Dawn Wings Necrozma", PokemonFormDetector.formatFormDisplayName("necrozma-dawn"))
        assertEquals("Ultra Necrozma", PokemonFormDetector.formatFormDisplayName("necrozma-ultra"))
    }

    @Test
    fun testRgbToHsv() {
        val hsv = FloatArray(3)

        // Pure Red
        PokemonFormDetector.rgbToHsv(255, 0, 0, hsv)
        assertEquals(0f, hsv[0], 0.01f)
        assertEquals(1.0f, hsv[1], 0.01f)
        assertEquals(1.0f, hsv[2], 0.01f)

        // Pure Green
        PokemonFormDetector.rgbToHsv(0, 255, 0, hsv)
        assertEquals(120f, hsv[0], 0.01f)

        // Pure Blue
        PokemonFormDetector.rgbToHsv(0, 0, 255, hsv)
        assertEquals(240f, hsv[0], 0.01f)

        // Cyan (Sky Blue)
        PokemonFormDetector.rgbToHsv(0, 255, 255, hsv)
        assertEquals(180f, hsv[0], 0.01f)

        // Dark Charcoal
        PokemonFormDetector.rgbToHsv(20, 20, 20, hsv)
        assertTrue(hsv[2] < 0.28f)
    }

    @Test
    fun testMegaRayquazaDisambiguation() {
        val baseRay = createDummyPokemon(384, "rayquaza", "rayquaza-mega")
        val megaRay = createDummyPokemon(10079, "rayquaza-mega", "rayquaza")
        val forms = listOf(megaRay)

        // Profile with emerald green + radiant golden plasma tendrils
        val profile = ColorProfile(
            dominantHue = 120f,
            avgSaturation = 0.55f,
            avgValue = 0.60f,
            blackPixelPct = 0.05f,
            whitePixelPct = 0.02f,
            cyanBluePct = 0.01f,
            yellowGoldPct = 0.18f,
            redOrangePct = 0.08f,
            greenPct = 0.35f,
            purpleVioletPct = 0f
        )

        val result = PokemonFormDetector.disambiguateFromProfile(profile, baseRay, forms)
        assertEquals("rayquaza-mega", result.resolvedFormName)
        assertFalse(result.isShiny)
        assertTrue(result.confidence >= 0.85f)
    }

    @Test
    fun testShinyRayquazaDisambiguation() {
        val baseRay = createDummyPokemon(384, "rayquaza", "rayquaza-mega")
        val megaRay = createDummyPokemon(10079, "rayquaza-mega", "rayquaza")
        val forms = listOf(megaRay)

        // Jet-black body with yellow markings
        val profile = ColorProfile(
            dominantHue = 50f,
            avgSaturation = 0.20f,
            avgValue = 0.30f,
            blackPixelPct = 0.55f,
            whitePixelPct = 0.02f,
            cyanBluePct = 0.01f,
            yellowGoldPct = 0.05f,
            redOrangePct = 0.02f,
            greenPct = 0.05f,
            purpleVioletPct = 0f
        )

        val result = PokemonFormDetector.disambiguateFromProfile(profile, baseRay, forms)
        assertEquals("rayquaza", result.resolvedFormName)
        assertTrue(result.isShiny)
    }

    @Test
    fun testAlolanVulpixDisambiguation() {
        val baseVulpix = createDummyPokemon(37, "vulpix", "vulpix-alola")
        val alolanVulpix = createDummyPokemon(10103, "vulpix-alola", "vulpix")
        val forms = listOf(alolanVulpix)

        // Snow-white coat with icy cyan curls
        val profile = ColorProfile(
            dominantHue = 190f,
            avgSaturation = 0.15f,
            avgValue = 0.90f,
            blackPixelPct = 0.02f,
            whitePixelPct = 0.60f,
            cyanBluePct = 0.18f,
            yellowGoldPct = 0f,
            redOrangePct = 0f,
            greenPct = 0f,
            purpleVioletPct = 0f
        )

        val result = PokemonFormDetector.disambiguateFromProfile(profile, baseVulpix, forms)
        assertEquals("vulpix-alola", result.resolvedFormName)
        assertFalse(result.isShiny)
        assertTrue(result.confidence >= 0.90f)
    }

    @Test
    fun testUltraNecrozmaDisambiguation() {
        val baseNec = createDummyPokemon(800, "necrozma", "necrozma-dusk,necrozma-dawn,necrozma-ultra")
        val ultraNec = createDummyPokemon(10157, "necrozma-ultra", "necrozma")
        val forms = listOf(ultraNec)

        // Pure radiant golden dragon
        val profile = ColorProfile(
            dominantHue = 50f,
            avgSaturation = 0.65f,
            avgValue = 0.85f,
            blackPixelPct = 0.02f,
            whitePixelPct = 0.05f,
            cyanBluePct = 0f,
            yellowGoldPct = 0.65f,
            redOrangePct = 0.10f,
            greenPct = 0f,
            purpleVioletPct = 0f
        )

        val result = PokemonFormDetector.disambiguateFromProfile(profile, baseNec, forms)
        assertEquals("necrozma-ultra", result.resolvedFormName)
        assertFalse(result.isShiny)
        assertTrue(result.confidence >= 0.90f)
    }

    @Test
    fun testMegaCharizardXDisambiguation() {
        val baseChar = createDummyPokemon(6, "charizard", "charizard-mega-x,charizard-mega-y,charizard-gmax")
        val megaX = createDummyPokemon(10034, "charizard-mega-x", "charizard")
        val forms = listOf(megaX)

        // Charcoal black body with cyan blue flames
        val profile = ColorProfile(
            dominantHue = 195f,
            avgSaturation = 0.40f,
            avgValue = 0.35f,
            blackPixelPct = 0.45f,
            whitePixelPct = 0.05f,
            cyanBluePct = 0.18f,
            yellowGoldPct = 0f,
            redOrangePct = 0.05f,
            greenPct = 0f,
            purpleVioletPct = 0f
        )

        val result = PokemonFormDetector.disambiguateFromProfile(profile, baseChar, forms)
        assertEquals("charizard-mega-x", result.resolvedFormName)
        assertFalse(result.isShiny)
    }
}
