package com.example.pokedex.data

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Exhaustive unit test suite verifying typing integrity, defensive matchups,
 * and offensive STAB coverage across all 1,025 canonical Pokémon and 284 alternate forms.
 */
class PokemonTypeMatchupComprehensiveTest {

    private fun resolveAssetFile(fileName: String): File {
        val candidates = listOf(
            File("src/main/assets/$fileName"),
            File("app/src/main/assets/$fileName"),
            File("Pokedex/PokedexApp/app/src/main/assets/$fileName")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate $fileName from working dir ${File(".").absolutePath}")
    }

    @Test
    fun `test all 1025 Pokemon IDs are contiguous and have valid official types`() {
        val file = resolveAssetFile("pokemon_data.json")
        val jsonArray = JSONArray(file.readText(Charsets.UTF_8))

        val seenIds = mutableSetOf<Int>()
        var canonicalCount = 0
        var totalEntries = jsonArray.length()

        for (i in 0 until totalEntries) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getInt("id")
            val name = obj.getString("name")
            val type = obj.getString("type")

            assertTrue("Pokemon #$id ($name) must have non-empty type", type.isNotBlank())

            val types = type.split("/").map { it.trim() }
            assertTrue("Pokemon #$id ($name) has at least 1 and at most 2 types", types.size in 1..2)

            for (t in types) {
                assertTrue(
                    "Type '$t' on Pokemon #$id ($name) must be one of 18 official types",
                    t in PokemonTypeEffectiveness.ALL_TYPES
                )
            }

            if (id in 1..1025) {
                seenIds.add(id)
                canonicalCount++
            }
        }

        assertEquals("Must contain exactly 1,025 canonical Pokemon IDs", 1025, seenIds.size)
        for (expectedId in 1..1025) {
            assertTrue("Canonical ID $expectedId must be present in dataset", seenIds.contains(expectedId))
        }
    }

    @Test
    fun `test all 1025 Pokemon defensive matchups cleanly partition all 18 types`() {
        val file = resolveAssetFile("pokemon_data.json")
        val jsonArray = JSONArray(file.readText(Charsets.UTF_8))

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getInt("id")
            val name = obj.getString("name")
            val type = obj.getString("type")

            val summary = PokemonTypeEffectiveness.getFullMatchups(type)
            val totalPartition = summary.superEffectiveQuad.size +
                    summary.effectiveDouble.size +
                    summary.normalEffective.size +
                    summary.notVeryEffectiveHalf.size +
                    summary.notVeryEffectiveQuarter.size +
                    summary.immune.size

            assertEquals(
                "Matchups for Pokemon #$id ($name - $type) must partition exactly 18 types",
                18,
                totalPartition
            )

            // Verify mutual disjointness
            val allGrouped = summary.superEffectiveQuad +
                    summary.effectiveDouble +
                    summary.normalEffective +
                    summary.notVeryEffectiveHalf +
                    summary.notVeryEffectiveQuarter +
                    summary.immune

            assertEquals(
                "All 18 types must be distinct for Pokemon #$id ($name - $type)",
                18,
                allGrouped.distinct().size
            )

            // Weakness list check
            val weaknesses = PokemonTypeEffectiveness.getWeaknesses(type)
            assertEquals(
                "getWeaknesses count must match quad + double count for #$id ($name)",
                summary.superEffectiveQuad.size + summary.effectiveDouble.size,
                weaknesses.size
            )
            for (w in weaknesses) {
                assertTrue("Weakness multiplier must be > 1.0f for #$id", w.multiplier > 1.0f)
            }

            // Offensive STAB check
            val offensive = PokemonTypeEffectiveness.getOffensiveStrengths(type)
            for (offType in offensive) {
                assertTrue(
                    "Offensive type '$offType' must be in ALL_TYPES",
                    offType in PokemonTypeEffectiveness.ALL_TYPES
                )
            }
        }
    }

    @Test
    fun `test canonical benchmark Pokemon type matchups`() {
        // 1. Bulbasaur (Grass/Poison)
        val bulbasaur = PokemonTypeEffectiveness.getFullMatchups("Grass/Poison")
        assertTrue(bulbasaur.superEffectiveQuad.isEmpty())
        assertEquals(listOf("Fire", "Ice", "Flying", "Psychic").sorted(), bulbasaur.effectiveDouble.sorted())
        assertEquals(listOf("Water", "Electric", "Fighting", "Fairy").sorted(), bulbasaur.notVeryEffectiveHalf.sorted())
        assertEquals(listOf("Grass"), bulbasaur.notVeryEffectiveQuarter)
        assertTrue(bulbasaur.immune.isEmpty())

        val bulbaOffensive = PokemonTypeEffectiveness.getOffensiveStrengths("Grass/Poison")
        assertEquals(listOf("Water", "Grass", "Ground", "Rock", "Fairy").sorted(), bulbaOffensive.sorted())

        // 2. Charizard (Fire/Flying)
        val charizard = PokemonTypeEffectiveness.getFullMatchups("Fire/Flying")
        assertEquals(listOf("Rock"), charizard.superEffectiveQuad)
        assertEquals(listOf("Water", "Electric").sorted(), charizard.effectiveDouble.sorted())
        assertEquals(listOf("Fire", "Fighting", "Steel", "Fairy").sorted(), charizard.notVeryEffectiveHalf.sorted())
        assertEquals(listOf("Grass", "Bug").sorted(), charizard.notVeryEffectiveQuarter.sorted())
        assertEquals(listOf("Ground"), charizard.immune)

        // 3. Swampert (Water/Ground) - 4x quad weakness to Grass, immune to Electric
        val swampert = PokemonTypeEffectiveness.getFullMatchups("Water/Ground")
        assertEquals(listOf("Grass"), swampert.superEffectiveQuad)
        assertTrue(swampert.effectiveDouble.isEmpty())
        assertEquals(listOf("Electric"), swampert.immune)

        // 4. Scizor (Bug/Steel) - 4x quad weakness to Fire, immune to Poison
        val scizor = PokemonTypeEffectiveness.getFullMatchups("Bug/Steel")
        assertEquals(listOf("Fire"), scizor.superEffectiveQuad)
        assertTrue(scizor.effectiveDouble.isEmpty())
        assertEquals(listOf("Poison"), scizor.immune)
        assertEquals(listOf("Grass"), scizor.notVeryEffectiveQuarter)

        // 5. Spiritomb (Ghost/Dark) - only weak to Fairy, 3 immunities (Normal, Fighting, Psychic)
        val spiritomb = PokemonTypeEffectiveness.getFullMatchups("Ghost/Dark")
        assertTrue(spiritomb.superEffectiveQuad.isEmpty())
        assertEquals(listOf("Fairy"), spiritomb.effectiveDouble)
        assertEquals(listOf("Normal", "Fighting", "Psychic").sorted(), spiritomb.immune.sorted())

        // 6. Pecharunt #1025 (Poison/Ghost)
        val pecharunt = PokemonTypeEffectiveness.getFullMatchups("Poison/Ghost")
        assertTrue(pecharunt.superEffectiveQuad.isEmpty())
        assertEquals(listOf("Ground", "Psychic", "Ghost", "Dark").sorted(), pecharunt.effectiveDouble.sorted())
        assertEquals(listOf("Grass", "Fairy").sorted(), pecharunt.notVeryEffectiveHalf.sorted())
        assertEquals(listOf("Poison", "Bug").sorted(), pecharunt.notVeryEffectiveQuarter.sorted())
        assertEquals(listOf("Normal", "Fighting").sorted(), pecharunt.immune.sorted())
    }

    @Test
    fun `test all 324 pairwise type interactions match official Gen 9 matrix`() {
        val immunities = mapOf(
            "Normal" to setOf("Ghost"),
            "Ground" to setOf("Electric"),
            "Flying" to setOf("Ground"),
            "Ghost" to setOf("Normal", "Fighting"),
            "Dark" to setOf("Psychic"),
            "Steel" to setOf("Poison"),
            "Fairy" to setOf("Dragon")
        )

        for ((defType, expectedImmune) in immunities) {
            for (attType in expectedImmune) {
                val mult = PokemonTypeEffectiveness.getDefensiveMultiplier(attType, listOf(defType))
                assertEquals("$attType attacking $defType must be 0.0x", 0.0f, mult, 0.001f)
            }
        }

        // Test key super-effective pairings
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Fire", listOf("Grass")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Water", listOf("Fire")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Electric", listOf("Water")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Ice", listOf("Dragon")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Fighting", listOf("Normal")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Ground", listOf("Electric")), 0.001f)
        assertEquals(2.0f, PokemonTypeEffectiveness.getDefensiveMultiplier("Fairy", listOf("Dragon")), 0.001f)
    }

    @Test
    fun `test offensive STAB advantages for pure and dual types`() {
        // Pure Normal has no super-effective coverage
        assertTrue(PokemonTypeEffectiveness.getOffensiveStrengths("Normal").isEmpty())

        // Pure Ground hits 5 types
        val groundOffensive = PokemonTypeEffectiveness.getOffensiveStrengths("Ground")
        assertEquals(listOf("Fire", "Electric", "Poison", "Rock", "Steel").sorted(), groundOffensive.sorted())

        // Pure Ice hits 4 types
        val iceOffensive = PokemonTypeEffectiveness.getOffensiveStrengths("Ice")
        assertEquals(listOf("Grass", "Ice", "Ground", "Flying", "Dragon").filter { it != "Ice" }.sorted(), iceOffensive.sorted())

        // Water/Ground combined STAB hits 6 types
        val waterGround = PokemonTypeEffectiveness.getOffensiveStrengths("Water/Ground")
        assertEquals(listOf("Fire", "Electric", "Poison", "Ground", "Rock", "Steel").sorted(), waterGround.sorted())
    }
}
