package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class PokemonGameGuideTest {

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
    fun `Exp Share resolves accurately with game-specific locations across generations`() {
        // Ruby: Devon Corp in Rustboro
        val rubyRes = PokemonGameGuideRepository.queryGuide("Where can I find the Exp. Share?", "Ruby")
        assertTrue("Ruby Exp. Share must mention Rustboro / Devon Corp", rubyRes.location.contains("Rustboro", ignoreCase = true))
        assertTrue("Ruby Exp. Share must mention Mr. Stone / letter", rubyRes.steps.any { it.contains("Mr. Stone", ignoreCase = true) || it.contains("Steven", ignoreCase = true) })

        // Gold: Mr. Pokémon on Route 30
        val goldRes = PokemonGameGuideRepository.queryGuide("exp share", "Gold")
        assertTrue("Gold Exp. Share must be at Mr. Pokémon's House", goldRes.location.contains("Route 30", ignoreCase = true) || goldRes.location.contains("Mr. Pokémon", ignoreCase = true))
        assertTrue("Gold Exp. Share requires Red Scale", goldRes.prerequisites?.contains("Red Scale", ignoreCase = true) == true)

        // Red: Route 15 Aide (50 Pokémon)
        val redRes = PokemonGameGuideRepository.queryGuide("Where is Exp Share?", "Red")
        assertTrue("Red Exp. Share is on Route 15", redRes.location.contains("Route 15", ignoreCase = true))
        assertTrue("Red Exp. Share requires 50 Pokémon", redRes.prerequisites?.contains("50", ignoreCase = true) == true)
    }

    @Test
    fun `Roadblock solutions for Sudowoodo, Snorlax, and Devon Scope are guide-accurate`() {
        // Sudowoodo on Route 36 in Gold/HeartGold
        val sudoRes = PokemonGameGuideRepository.queryGuide("How do I get past the weird tree Sudowoodo?", "Gold")
        assertTrue("Sudowoodo is on Route 36", sudoRes.location.contains("Route 36", ignoreCase = true))
        assertTrue("Solution mentions SquirtBottle from Goldenrod Flower Shop", sudoRes.steps.any { it.contains("SquirtBottle", ignoreCase = true) })
        assertTrue("Solution mentions Plain Badge from Whitney", sudoRes.prerequisites?.contains("Whitney", ignoreCase = true) == true)

        // Snorlax in Red/FireRed
        val snorlaxRes = PokemonGameGuideRepository.queryGuide("How to wake up sleeping Snorlax?", "Red")
        assertTrue("Snorlax is on Route 12 or 16", snorlaxRes.location.contains("Route 12", ignoreCase = true) || snorlaxRes.location.contains("Route 16", ignoreCase = true))
        assertTrue("Solution mentions Poké Flute from Mr. Fuji", snorlaxRes.steps.any { it.contains("Poké Flute", ignoreCase = true) })

        // Devon Scope in Ruby/Emerald
        val devonRes = PokemonGameGuideRepository.queryGuide("invisible obstacle Kecleon Devon Scope", "Ruby")
        assertTrue("Devon Scope is on Route 120", devonRes.location.contains("Route 120", ignoreCase = true))
        assertTrue("Solution mentions Steven Stone on the bridge", devonRes.steps.any { it.contains("Steven", ignoreCase = true) })
    }

    @Test
    fun `Facility lookups for Move Relearner, Move Deleter, and Day Care are precise`() {
        // Move Relearner in Ruby/Emerald (Fallarbor Town, Heart Scale)
        val relearnerRuby = PokemonGameGuideRepository.queryGuide("Where is the Move Relearner?", "Ruby")
        assertTrue("Hoenn Move Relearner is in Fallarbor Town", relearnerRuby.location.contains("Fallarbor", ignoreCase = true))
        assertTrue("Hoenn Move Relearner costs 1 Heart Scale", relearnerRuby.prerequisites?.contains("Heart Scale", ignoreCase = true) == true)

        // Move Relearner in FireRed (Two Island, Mushrooms)
        val relearnerFireRed = PokemonGameGuideRepository.queryGuide("Move Relearner", "FireRed")
        assertTrue("FireRed Move Relearner is on Two Island", relearnerFireRed.location.contains("Two Island", ignoreCase = true))
        assertTrue("FireRed Move Relearner costs mushrooms", relearnerFireRed.prerequisites?.contains("Mushroom", ignoreCase = true) == true)

        // Move Deleter in Emerald (Lilycove City)
        val deleterEmerald = PokemonGameGuideRepository.queryGuide("Where to delete HM moves?", "Emerald")
        assertTrue("Move Deleter in Emerald is in Lilycove City", deleterEmerald.location.contains("Lilycove", ignoreCase = true))

        // Day Care in Hoenn (Route 117)
        val daycareRuby = PokemonGameGuideRepository.queryGuide("Where is the Day Care for breeding eggs?", "Ruby")
        assertTrue("Hoenn Day Care is on Route 117", daycareRuby.location.contains("Route 117", ignoreCase = true))
    }

    @Test
    fun `Pokemon builds provide optimal held items, movesets, and strategy guide advice`() {
        // Swampert
        val swampertBuild = PokemonGameGuideRepository.queryGuide("What are the best items and moves for Swampert?", "Ruby")
        assertEquals("POKÉMON MASTER BUILD", swampertBuild.category)
        assertTrue("Swampert best items include Leftovers / Mystic Water", swampertBuild.recommendedItems.any { it.contains("Leftovers", ignoreCase = true) || it.contains("Mystic Water", ignoreCase = true) })
        assertTrue("Swampert moveset includes Earthquake", swampertBuild.steps.any { it.contains("Earthquake", ignoreCase = true) })
        assertTrue("Swampert moveset includes Surf", swampertBuild.steps.any { it.contains("Surf", ignoreCase = true) })
        assertTrue("Swampert strategy mentions single 4x Grass weakness", swampertBuild.steps.any { it.contains("Grass", ignoreCase = true) })

        // Charizard
        val charizardBuild = PokemonGameGuideRepository.queryGuide("Charizard build", "FireRed")
        assertEquals("POKÉMON MASTER BUILD", charizardBuild.category)
        assertTrue("Charizard moveset includes Flamethrower", charizardBuild.steps.any { it.contains("Flamethrower", ignoreCase = true) })
        assertTrue("Charizard strategy mentions 4x Rock weakness", charizardBuild.steps.any { it.contains("Rock", ignoreCase = true) })
    }

    @Test
    fun `Gym Leader queries provide team rosters, level caps, and counter-tactics`() {
        // Roxanne
        val roxanneRes = PokemonGameGuideRepository.queryGuide("How to beat Roxanne in Rustboro Gym?", "Ruby")
        assertEquals("GYM BATTLE STRATEGY", roxanneRes.category)
        assertTrue("Roxanne is in Rustboro", roxanneRes.location.contains("Rustboro", ignoreCase = true))
        assertTrue("Roxanne strategy recommends Water Gun / Absorb or Double Kick", roxanneRes.steps.any { it.contains("Water", ignoreCase = true) || it.contains("Absorb", ignoreCase = true) || it.contains("Double Kick", ignoreCase = true) })

        // Wattson
        val wattsonRes = PokemonGameGuideRepository.queryGuide("Wattson Mauville Gym strategy", "Ruby")
        assertEquals("GYM BATTLE STRATEGY", wattsonRes.category)
        assertTrue("Wattson strategy recommends Ground-types / Marshtomp / Geodude", wattsonRes.steps.any { it.contains("Ground", ignoreCase = true) || it.contains("Marshtomp", ignoreCase = true) })
    }

    @Test
    fun `Dual-Era Kanto coordinates differentiate correctly between Game Boy and GBA layouts`() {
        // Game Boy Red Pewter City: (0.220, 0.200)
        val gbPewter = PokemonGameGuideRepository.resolveMapCoordinates("Red", "Pewter City")
        assertNotNull("GB Pewter must resolve", gbPewter)
        assertEquals(0.220f, gbPewter!!.first, 0.01f)
        assertEquals(0.200f, gbPewter.second, 0.01f)
        assertEquals("Pewter City", gbPewter.third)

        // GBA FireRed Pewter City: (0.280, 0.420)
        val gbaPewter = PokemonGameGuideRepository.resolveMapCoordinates("FireRed", "Pewter City")
        assertNotNull("GBA Pewter must resolve", gbaPewter)
        assertEquals(0.280f, gbaPewter!!.first, 0.01f)
        assertEquals(0.420f, gbaPewter.second, 0.01f)
        assertEquals("Pewter City", gbaPewter.third)

        // Cerulean City
        val gbCerulean = PokemonGameGuideRepository.resolveMapCoordinates("Red", "Cerulean City")
        val gbaCerulean = PokemonGameGuideRepository.resolveMapCoordinates("FireRed", "Cerulean City")
        assertNotNull(gbCerulean)
        assertNotNull(gbaCerulean)
        assertEquals(0.625f, gbCerulean!!.first, 0.01f)
        assertEquals(0.140f, gbCerulean.second, 0.01f)
        assertEquals(0.620f, gbaCerulean!!.first, 0.01f)
        assertEquals(0.380f, gbaCerulean.second, 0.01f)

        // Viridian City Mart First Encounter
        val gbMart = PokemonGameGuideRepository.resolveMapCoordinates("Red", "all poké mart")
        val gbaMart = PokemonGameGuideRepository.resolveMapCoordinates("FireRed", "all poké mart")
        assertNotNull(gbMart)
        assertNotNull(gbaMart)
        assertEquals(0.485f, gbMart!!.second, 0.01f)
        assertEquals(0.620f, gbaMart!!.second, 0.01f)
    }

    @Test
    fun `Hoenn and Johto coordinates resolve to authentic in-game grid locations`() {
        // Hoenn: Mauville City (0.330, 0.420)
        val mauville = PokemonGameGuideRepository.resolveMapCoordinates("Emerald", "Mauville City")
        assertNotNull(mauville)
        assertEquals(0.330f, mauville!!.first, 0.01f)
        assertEquals(0.420f, mauville.second, 0.01f)

        // Hoenn: Fortree City (0.450, 0.120)
        val fortree = PokemonGameGuideRepository.resolveMapCoordinates("Emerald", "Fortree City")
        assertNotNull(fortree)
        assertEquals(0.450f, fortree!!.first, 0.01f)
        assertEquals(0.120f, fortree.second, 0.01f)

        // Johto: Goldenrod City
        val goldenrod = PokemonGameGuideRepository.resolveMapCoordinates("Crystal", "Goldenrod City")
        assertNotNull(goldenrod)
        assertEquals(0.25f, goldenrod!!.first, 0.01f)
        assertEquals(0.55f, goldenrod.second, 0.01f)

        // Johto: Route 36 Sudowoodo
        val sudowoodo = PokemonGameGuideRepository.resolveMapCoordinates("Gold", "Route 36 Junction")
        assertNotNull(sudowoodo)
        assertEquals(0.45f, sudowoodo!!.first, 0.01f)
        assertEquals(0.55f, sudowoodo.second, 0.01f)
    }

    @Test
    fun `Gym Leader queries on native cartridges resolve pin coordinates accurately`() {
        // Brock on Red
        val brockRed = PokemonGameGuideRepository.queryGuide("Brock", "Red")
        assertNotNull("Brock on Red should have pinX", brockRed.pinX)
        assertNotNull("Brock on Red should have pinY", brockRed.pinY)
        assertEquals(0.220f, brockRed.pinX!!, 0.01f)
        assertEquals(0.200f, brockRed.pinY!!, 0.01f)

        // Brock on FireRed
        val brockFireRed = PokemonGameGuideRepository.queryGuide("Brock", "FireRed")
        assertNotNull("Brock on FireRed should have pinX", brockFireRed.pinX)
        assertNotNull("Brock on FireRed should have pinY", brockFireRed.pinY)
        assertEquals(0.280f, brockFireRed.pinX!!, 0.01f)
        assertEquals(0.420f, brockFireRed.pinY!!, 0.01f)

        // Wattson on Emerald
        val wattsonEmerald = PokemonGameGuideRepository.queryGuide("Wattson", "Emerald")
        assertNotNull("Wattson on Emerald should have pinX", wattsonEmerald.pinX)
        assertEquals(0.330f, wattsonEmerald.pinX!!, 0.01f)
        assertEquals(0.420f, wattsonEmerald.pinY!!, 0.01f)

        // Whitney on Gold
        val whitneyGold = PokemonGameGuideRepository.queryGuide("Whitney", "Gold")
        assertNotNull("Whitney on Gold should have pinX", whitneyGold.pinX)
        assertEquals(0.25f, whitneyGold.pinX!!, 0.01f)
        assertEquals(0.55f, whitneyGold.pinY!!, 0.01f)
    }

    @Test
    fun `Event Pokemon and Mythical Quests resolve with pin coordinates and take precedence over builds`() {
        // Mew on Emerald: should resolve to Old Sea Map / Faraway Island with pin (0.85, 0.85)
        val mewEmerald = PokemonGameGuideRepository.queryGuide("Mew", "Emerald")
        assertNotNull("Mew on Emerald must have pinX", mewEmerald.pinX)
        assertNotNull("Mew on Emerald must have pinY", mewEmerald.pinY)
        assertEquals(0.85f, mewEmerald.pinX!!, 0.01f)
        assertEquals(0.85f, mewEmerald.pinY!!, 0.01f)
        assertTrue("Mew should point to Faraway Island", mewEmerald.location.contains("Faraway Island", ignoreCase = true))

        // Old Sea Map explicitly
        val osm = PokemonGameGuideRepository.queryGuide("Old Sea Map", "Emerald")
        assertNotNull("Old Sea Map must have pinX", osm.pinX)
        assertEquals(0.85f, osm.pinX!!, 0.01f)
        assertEquals(0.85f, osm.pinY!!, 0.01f)

        // Deoxys on FireRed: should resolve to Birth Island with pin (0.50, 0.50)
        val deoxysFireRed = PokemonGameGuideRepository.queryGuide("Deoxys", "FireRed")
        assertNotNull("Deoxys on FireRed must have pinX", deoxysFireRed.pinX)
        assertEquals(0.50f, deoxysFireRed.pinX!!, 0.01f)
        assertEquals(0.50f, deoxysFireRed.pinY!!, 0.01f)
        assertTrue("Deoxys on FireRed must point to Birth Island", deoxysFireRed.location.contains("Birth Island", ignoreCase = true))

        // Darkrai on Platinum: should resolve to Newmoon Island with pin (0.20, 0.20)
        val darkraiPt = PokemonGameGuideRepository.queryGuide("Darkrai", "Platinum")
        assertNotNull("Darkrai on Platinum must have pinX", darkraiPt.pinX)
        assertEquals(0.20f, darkraiPt.pinX!!, 0.01f)
        assertEquals(0.20f, darkraiPt.pinY!!, 0.01f)
        assertTrue("Darkrai must point to Newmoon Island", darkraiPt.location.contains("Newmoon Island", ignoreCase = true))

        // Shaymin on Platinum: should resolve to Flower Paradise with pin (0.88, 0.15)
        val shayminPt = PokemonGameGuideRepository.queryGuide("Shaymin", "Platinum")
        assertNotNull("Shaymin on Platinum must have pinX", shayminPt.pinX)
        assertEquals(0.88f, shayminPt.pinX!!, 0.01f)
        assertEquals(0.15f, shayminPt.pinY!!, 0.01f)
        assertTrue("Shaymin must point to Flower Paradise", shayminPt.location.contains("Flower Paradise", ignoreCase = true))

        // Arceus on Diamond: should resolve to Spear Pillar with pin (0.50, 0.35)
        val arceusDia = PokemonGameGuideRepository.queryGuide("Arceus", "Diamond")
        assertNotNull("Arceus on Diamond must have pinX", arceusDia.pinX)
        assertEquals(0.50f, arceusDia.pinX!!, 0.01f)
        assertEquals(0.35f, arceusDia.pinY!!, 0.01f)
        assertTrue("Arceus must point to Spear Pillar", arceusDia.location.contains("Spear Pillar", ignoreCase = true))

        // Celebi on Crystal: should resolve to Ilex Forest Shrine with pin (0.35, 0.80)
        val celebiCrystal = PokemonGameGuideRepository.queryGuide("Celebi", "Crystal")
        assertNotNull("Celebi on Crystal must have pinX", celebiCrystal.pinX)
        assertEquals(0.35f, celebiCrystal.pinX!!, 0.01f)
        assertEquals(0.80f, celebiCrystal.pinY!!, 0.01f)

        // Jirachi on Emerald: should resolve to Mossdeep Space Center with pin (0.860, 0.360)
        val jirachiEmerald = PokemonGameGuideRepository.queryGuide("Jirachi", "Emerald")
        assertNotNull("Jirachi on Emerald must have pinX", jirachiEmerald.pinX)
        assertEquals(0.860f, jirachiEmerald.pinX!!, 0.01f)
        assertEquals(0.360f, jirachiEmerald.pinY!!, 0.01f)
    }

    @Test
    fun `Legendary encounters and items resolve with pins`() {
        // Mewtwo on Red: Cerulean Cave B1F (0.570, 0.100)
        val mewtwoRed = PokemonGameGuideRepository.queryGuide("Mewtwo", "Red")
        assertNotNull("Mewtwo on Red must have pinX", mewtwoRed.pinX)
        assertEquals(0.570f, mewtwoRed.pinX!!, 0.01f)
        assertEquals(0.100f, mewtwoRed.pinY!!, 0.01f)

        // Rayquaza on Emerald: Sky Pillar (0.680, 0.600)
        val rayquazaEmerald = PokemonGameGuideRepository.queryGuide("Rayquaza", "Emerald")
        assertNotNull("Rayquaza on Emerald must have pinX", rayquazaEmerald.pinX)
        assertEquals(0.680f, rayquazaEmerald.pinX!!, 0.01f)
        assertEquals(0.600f, rayquazaEmerald.pinY!!, 0.01f)

        // Articuno on Red: Seafoam Islands (0.400, 0.900)
        val articunoRed = PokemonGameGuideRepository.queryGuide("Articuno", "Red")
        assertNotNull("Articuno on Red must have pinX", articunoRed.pinX)
        assertEquals(0.400f, articunoRed.pinX!!, 0.01f)
        assertEquals(0.900f, articunoRed.pinY!!, 0.01f)

        // Exp. Share on Red: Route 15 (0.600, 0.780)
        val expShareRed = PokemonGameGuideRepository.queryGuide("Exp. Share", "Red")
        assertNotNull("Exp. Share on Red must have pinX", expShareRed.pinX)
        assertEquals(0.600f, expShareRed.pinX!!, 0.01f)
        assertEquals(0.780f, expShareRed.pinY!!, 0.01f)

        // Bicycle on Emerald: Mauville City (0.330, 0.420)
        val bicycleEmerald = PokemonGameGuideRepository.queryGuide("Bicycle", "Emerald")
        assertNotNull("Bicycle on Emerald must have pinX", bicycleEmerald.pinX)
        assertEquals(0.330f, bicycleEmerald.pinX!!, 0.01f)
        assertEquals(0.420f, bicycleEmerald.pinY!!, 0.01f)
    }

    @Test
    fun `All 13 strategy categories resolve 100 percent of their topicIds with zero missing entries`() {
        val categories = PokemonGameGuideRepository.getCategories()
        assertEquals("Must have 13 strategy domains", 13, categories.size)

        for (cat in categories) {
            val topics = PokemonGameGuideRepository.getTopicsForCategory(cat.id)
            assertTrue("Category ${cat.id} must not be empty", topics.isNotEmpty())
            assertEquals(
                "Category ${cat.id} topic count must match declared topicIds",
                cat.topicIds.size,
                topics.size
            )
        }
    }

    @Test
    fun `Roadblocks and puzzles resolve across multiple generations with authentic pins`() {
        // Snorlax on Gold: Route 11 outside Vermilion City
        val snorlaxGold = PokemonGameGuideRepository.queryGuide("Snorlax", "Gold")
        assertNotNull("Snorlax on Gold must have pinX", snorlaxGold.pinX)
        assertEquals(0.68f, snorlaxGold.pinX!!, 0.01f)
        assertEquals(0.58f, snorlaxGold.pinY!!, 0.01f)
        assertTrue("Snorlax on Gold must mention Route 11 / Vermilion", snorlaxGold.location.contains("Route 11", ignoreCase = true))
        assertTrue("Snorlax on Gold must mention EXPN Card or Poké Flute radio", snorlaxGold.steps.any { it.contains("EXPN", ignoreCase = true) || it.contains("Radio", ignoreCase = true) })

        // Psyduck on Platinum: Route 210
        val psyduckPt = PokemonGameGuideRepository.queryGuide("Psyduck", "Platinum")
        assertNotNull("Psyduck on Platinum must have pinX", psyduckPt.pinX)
        assertEquals(0.60f, psyduckPt.pinX!!, 0.01f)
        assertEquals(0.42f, psyduckPt.pinY!!, 0.01f)
        assertTrue("Psyduck on Platinum must mention Route 210", psyduckPt.location.contains("Route 210", ignoreCase = true))

        // Lt. Surge trash cans on Red: Vermilion Gym (0.625, 0.550)
        val ltSurgeRed = PokemonGameGuideRepository.queryGuide("Lt Surge", "Red")
        assertNotNull("Lt. Surge on Red must have pinX", ltSurgeRed.pinX)
        assertEquals(0.625f, ltSurgeRed.pinX!!, 0.01f)
        assertEquals(0.550f, ltSurgeRed.pinY!!, 0.01f)

        // Liberty Pass on Black: Liberty Garden
        val libertyBlack = PokemonGameGuideRepository.queryGuide("Liberty Pass", "Black")
        assertNotNull("Liberty Pass on Black must have pinX", libertyBlack.pinX)
        assertEquals(0.40f, libertyBlack.pinX!!, 0.01f)
        assertEquals(0.95f, libertyBlack.pinY!!, 0.01f)

        // Devon Scope on Emerald: Route 120 (0.530, 0.195)
        val devonEmerald = PokemonGameGuideRepository.queryGuide("Devon Scope", "Emerald")
        assertNotNull("Devon Scope on Emerald must have pinX", devonEmerald.pinX)
        assertEquals(0.530f, devonEmerald.pinX!!, 0.01f)
        assertEquals(0.195f, devonEmerald.pinY!!, 0.01f)
    }

    @Test
    fun `Case-insensitive and spaced edition IDs resolve version keys accurately`() {
        val resLower = PokemonGameGuideRepository.queryGuide("Mewtwo", "firered")
        assertNotNull("Lowercase 'firered' must resolve Mewtwo pin", resLower.pinX)
        assertEquals(0.58f, resLower.pinX!!, 0.01f)

        val resSpaced = PokemonGameGuideRepository.queryGuide("Mewtwo", "Fire Red")
        assertNotNull("Spaced 'Fire Red' must resolve Mewtwo pin", resSpaced.pinX)
        assertEquals(0.58f, resSpaced.pinX!!, 0.01f)
    }
}
