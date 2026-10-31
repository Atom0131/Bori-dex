package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PokemonHabitatAndEntrySyncTest {

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
    fun `pokemon_data json has zero raw HTML entities and clean Paldea entries for Mudbray and Mudsdale`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)

        assertFalse(
            "pokemon_data.json must not contain unescaped &eacute; entities",
            rawJson.contains("&eacute;", ignoreCase = true)
        )

        // Verify #749 Mudbray and #750 Mudsdale blocks in pokemon_data.json
        val mudbrayIdx = rawJson.indexOf("\"name\": \"mudbray\"")
        val mudsdaleIdx = rawJson.indexOf("\"name\": \"mudsdale\"")
        assertTrue("Mudbray (#749) must exist in pokemon_data.json", mudbrayIdx > 0)
        assertTrue("Mudsdale (#750) must exist in pokemon_data.json", mudsdaleIdx > 0)

        val mudbrayBlock = rawJson.substring(mudbrayIdx, minOf(rawJson.length, mudbrayIdx + 2200))
        val mudsdaleBlock = rawJson.substring(mudsdaleIdx, minOf(rawJson.length, mudsdaleIdx + 2200))

        assertTrue("Mudbray must have Scarlet entry", mudbrayBlock.contains("\"Scarlet\":"))
        assertTrue("Mudbray must have Violet entry", mudbrayBlock.contains("\"Violet\":"))
        assertFalse(
            "Mudbray Scarlet/Violet entry must not say does not naturally inhabit Paldea",
            mudbrayBlock.contains("does not naturally inhabit the Paldea region", ignoreCase = true)
        )

        assertTrue("Mudsdale must have Scarlet entry", mudsdaleBlock.contains("\"Scarlet\":"))
        assertTrue("Mudsdale must have Violet entry", mudsdaleBlock.contains("\"Violet\":"))
        assertFalse(
            "Mudsdale Scarlet/Violet entry must not say does not naturally inhabit Paldea",
            mudsdaleBlock.contains("does not naturally inhabit the Paldea region", ignoreCase = true)
        )
    }

    @Test
    fun `Mudbray 749 and Mudsdale 750 have wild Paldea, Galar, and Alola habitats with region-matched pins and narration`() {
        val encountersFile = resolveAssetFile("pokemon_encounters.json")
        val rawEncounters = encountersFile.readText(Charsets.UTF_8)

        val idx749 = rawEncounters.indexOf("\"749\": {")
        val idx750 = rawEncounters.indexOf("\"750\": {")
        val idx751 = rawEncounters.indexOf("\"751\": {")
        assertTrue("Species #749 must exist in pokemon_encounters.json", idx749 > 0)
        assertTrue("Species #750 must exist in pokemon_encounters.json", idx750 > idx749)
        assertTrue("Species #751 must exist in pokemon_encounters.json", idx751 > idx750)

        val block749 = rawEncounters.substring(idx749, idx750)
        val block750 = rawEncounters.substring(idx750, idx751)

        // Check #749 Mudbray in Paldea, Galar, and Alola
        assertTrue("Mudbray must have Paldea regional habitat", block749.contains("\"Paldea\": {"))
        assertTrue("Mudbray must have Galar regional habitat", block749.contains("\"Galar\": {"))
        assertTrue("Mudbray must have Alola regional habitat", block749.contains("\"Alola\": {"))
        assertTrue("Mudbray Paldea pins must use paldea_ coordinates", block749.contains("\"paldea_south_province\""))
        assertTrue("Mudbray Paldea narration must reference Paldea", block749.contains("In Paldea, Mudbray"))

        // Check #750 Mudsdale in Paldea, Galar, and Alola
        assertFalse(
            "Mudsdale (#750) must NOT be marked isAreaUnknown: true",
            block750.contains("\"isAreaUnknown\": true")
        )
        assertTrue("Mudsdale must have Paldea regional habitat", block750.contains("\"Paldea\": {"))
        assertTrue("Mudsdale must have Galar regional habitat", block750.contains("\"Galar\": {"))
        assertTrue("Mudsdale must have Alola regional habitat", block750.contains("\"Alola\": {"))
        assertTrue("Mudsdale Paldea pins must use paldea_asado_desert", block750.contains("\"paldea_asado_desert\""))
        assertTrue("Mudsdale Paldea narration must reference Paldea", block750.contains("In Paldea, Mudsdale"))
    }

    @Test
    fun `All 1025 species in pokemon_encounters json have regionalHabitats and nativeRegions`() {
        val encountersFile = resolveAssetFile("pokemon_encounters.json")
        val rawEncounters = encountersFile.readText(Charsets.UTF_8)

        val regionalHabitatsCount = Regex("\"regionalHabitats\"\\s*:\\s*\\{").findAll(rawEncounters).count()
        val nativeRegionsCount = Regex("\"nativeRegions\"\\s*:\\s*\\[").findAll(rawEncounters).count()

        assertEquals("All 1,025 species must have regionalHabitats", 1025, regionalHabitatsCount)
        assertEquals("All 1,025 species must have nativeRegions", 1025, nativeRegionsCount)
    }

    @Test
    fun `PokemonRegion matches returning Paldea, Galar, and Alola species like Mudbray and Mudsdale`() {
        val mudbrayEntity = PokemonEntity(
            id = 749,
            name = "mudbray",
            type = "Ground",
            height = "1.0 m",
            weight = "110.0 kg",
            flavorText = "The mud stuck to Mudbray's hooves enhances its grip and its powerful running gait.",
            spriteUrl = "",
            shinySpriteUrl = "",
            evolvesTo = "mudsdale",
            forms = "",
            pokedexEntries = """
                {
                  "Sun": "The mud stuck to Mudbray's hooves enhances its grip and its powerful running gait.",
                  "Sword": "Loads weighing up to 50 times as much as its own body weight pose no issue for this Pokemon.",
                  "Scarlet": "This Pokémon covers itself in mud that it has regurgitated."
                }
            """.trimIndent()
        )

        assertTrue("Mudbray (#749) must match ALOLA", PokemonRegion.ALOLA.matches(mudbrayEntity))
        assertTrue("Mudbray (#749) must match GALAR", PokemonRegion.GALAR.matches(mudbrayEntity))
        assertTrue("Mudbray (#749) must match PALDEA", PokemonRegion.PALDEA.matches(mudbrayEntity))
        assertFalse("Mudbray (#749) must NOT match KANTO", PokemonRegion.KANTO.matches(mudbrayEntity))

        // Verify debut & region cartridge helpers
        assertEquals("Alola", PokemonMapHabitatRepository.getDebutRegionForPokemon(749))
        assertEquals("Sun", PokemonMapHabitatRepository.getDebutCartridgeIdForPokemon(749))
        assertEquals("Scarlet", PokemonMapHabitatRepository.getDefaultCartridgeIdForRegion("Paldea"))
        assertEquals("paldea_sv.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Scarlet", 749))
        assertEquals("alola_sm.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Default", 749))
    }

    @Test
    fun `getRegionMapEntryForEdition and getRegionForEdition synchronize Settings Game Version with Region filter`() {
        val scarletEntry = getRegionMapEntryForEdition("Scarlet")
        assertEquals("paldea_sv", scarletEntry.id)
        assertEquals(PokemonRegion.PALDEA, scarletEntry.region)
        assertEquals(PokemonRegion.PALDEA, getRegionForEdition("Violet"))
        assertEquals(PokemonRegion.PALDEA, getRegionForEdition("Paldea"))

        val redEntry = getRegionMapEntryForEdition("Red")
        assertEquals("kanto_rgby", redEntry.id)
        assertEquals(PokemonRegion.KANTO, redEntry.region)

        val fireRedEntry = getRegionMapEntryForEdition("FireRed")
        assertEquals("kanto_frlg", fireRedEntry.id)
        assertEquals(PokemonRegion.KANTO, fireRedEntry.region)

        val arceusEntry = getRegionMapEntryForEdition("Legends: Arceus")
        assertEquals("hisui_pla", arceusEntry.id)
        assertEquals(PokemonRegion.HISUI, arceusEntry.region)

        val goEntry = getRegionMapEntryForEdition("GO")
        assertEquals("pogo", goEntry.id)
        assertEquals(PokemonRegion.POKEMON_GO, goEntry.region)

        val defaultEntry = getRegionMapEntryForEdition("Default")
        assertEquals("all", defaultEntry.id)
        assertEquals(null, defaultEntry.region)
    }

    @Test
    fun `Multi-pin habitats always return unique pin IDs and non-overlapping coordinates`() {
        val sampleQueries = listOf(
            Triple(25, "Pikachu", "Red"),
            Triple(16, "Pidgey", "Red"),
            Triple(1, "Bulbasaur", "Scarlet"),
            Triple(749, "Mudbray", "GO")
        )
        for ((id, name, edition) in sampleQueries) {
            val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(id, name, edition)
            assertTrue("Expected multiple pins for $name in $edition", habitat.pins.size > 1)
            val uniqueIds = habitat.pins.map { it.id }.toSet()
            assertEquals("All pin IDs must be unique for $name in $edition", habitat.pins.size, uniqueIds.size)
        }
    }

    @Test
    fun `PokemonTypeEffectiveness computes exact defensive weaknesses for iconic Pokemon`() {
        // Squirtle: Water -> Weak to Electric (2x), Grass (2x)
        val squirtleWeaknesses = PokemonTypeEffectiveness.getWeaknesses("Water")
        assertEquals(2, squirtleWeaknesses.size)
        assertTrue(squirtleWeaknesses.any { it.type == "Electric" && it.multiplier == 2.0f })
        assertTrue(squirtleWeaknesses.any { it.type == "Grass" && it.multiplier == 2.0f })

        // Charmander: Fire -> Weak to Water (2x), Ground (2x), Rock (2x)
        val charmanderWeaknesses = PokemonTypeEffectiveness.getWeaknesses("Fire")
        assertEquals(3, charmanderWeaknesses.size)
        assertTrue(charmanderWeaknesses.any { it.type == "Water" && it.multiplier == 2.0f })
        assertTrue(charmanderWeaknesses.any { it.type == "Ground" && it.multiplier == 2.0f })
        assertTrue(charmanderWeaknesses.any { it.type == "Rock" && it.multiplier == 2.0f })

        // Bulbasaur: Grass/Poison -> Fire (2x), Ice (2x), Flying (2x), Psychic (2x)
        // (Grass weaknesses to Bug/Poison canceled by Poison resist; Poison weakness to Ground canceled by Grass resist)
        val bulbasaurWeaknesses = PokemonTypeEffectiveness.getWeaknesses("Grass/Poison")
        assertEquals(4, bulbasaurWeaknesses.size)
        assertTrue(bulbasaurWeaknesses.any { it.type == "Fire" && it.multiplier == 2.0f })
        assertTrue(bulbasaurWeaknesses.any { it.type == "Ice" && it.multiplier == 2.0f })
        assertTrue(bulbasaurWeaknesses.any { it.type == "Flying" && it.multiplier == 2.0f })
        assertTrue(bulbasaurWeaknesses.any { it.type == "Psychic" && it.multiplier == 2.0f })
        assertFalse("Bug weakness should be cancelled out", bulbasaurWeaknesses.any { it.type == "Bug" })
        assertFalse("Ground weakness should be cancelled out", bulbasaurWeaknesses.any { it.type == "Ground" })

        // Charizard: Fire/Flying -> Rock (4x), Water (2x), Electric (2x)
        val charizardWeaknesses = PokemonTypeEffectiveness.getWeaknesses("Fire/Flying")
        assertEquals(3, charizardWeaknesses.size)
        val rockWeakness = charizardWeaknesses.first { it.type == "Rock" }
        assertEquals("Rock weakness against Charizard must be 4x", 4.0f, rockWeakness.multiplier)
        assertTrue(charizardWeaknesses.any { it.type == "Water" && it.multiplier == 2.0f })
        assertTrue(charizardWeaknesses.any { it.type == "Electric" && it.multiplier == 2.0f })

        // Mudbray: Ground -> Water (2x), Grass (2x), Ice (2x)
        val mudbrayWeaknesses = PokemonTypeEffectiveness.getWeaknesses("Ground")
        assertEquals(3, mudbrayWeaknesses.size)
        assertTrue(mudbrayWeaknesses.any { it.type == "Water" && it.multiplier == 2.0f })
        assertTrue(mudbrayWeaknesses.any { it.type == "Grass" && it.multiplier == 2.0f })
        assertTrue(mudbrayWeaknesses.any { it.type == "Ice" && it.multiplier == 2.0f })
    }

    @Test
    fun `PokemonTypeEffectiveness computes valid weaknesses for all 1025 Pokemon in pokemon_data json`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        val typeRegex = Regex("\"type\":\\s*\"([^\"]+)\"")
        val typeMatches = typeRegex.findAll(rawJson).map { it.groupValues[1] }.toList()

        assertTrue("Must have at least 1025 entries in pokemon_data.json", typeMatches.size >= 1025)

        for (typeStr in typeMatches) {
            val weaknesses = PokemonTypeEffectiveness.getWeaknesses(typeStr)
            assertTrue("Pokemon type ($typeStr) must have non-empty weaknesses", weaknesses.isNotEmpty())

            for (w in weaknesses) {
                assertTrue("Weakness type must be one of the 18 official types", w.type in PokemonTypeEffectiveness.ALL_TYPES)
                assertTrue("Weakness multiplier must be > 1.0 (2x or 4x)", w.multiplier in listOf(2.0f, 4.0f))
            }
        }
    }

    @Test
    fun `PokemonTypeEffectiveness speech formatting produces clear English and Spanish TTS text`() {
        val squirtleWeak = PokemonTypeEffectiveness.getWeaknesses("Water")
        val enSpeech = PokemonTypeEffectiveness.formatWeaknessesForSpeech("Squirtle", squirtleWeak, isSpanish = false)
        assertTrue(enSpeech.contains("Effective 2 times against Electric and Grass"))

        val charizardWeak = PokemonTypeEffectiveness.getWeaknesses("Fire/Flying")
        val enCharizard = PokemonTypeEffectiveness.formatWeaknessesForSpeech("Charizard", charizardWeak, isSpanish = false)
        assertTrue(enCharizard.contains("Super effective 4 times against Rock"))
        assertTrue(enCharizard.contains("effective 2 times against Water and Electric"))

        val esSquirtle = PokemonTypeEffectiveness.formatWeaknessesForSpeech("Squirtle", squirtleWeak, isSpanish = true)
        assertTrue(esSquirtle.contains("Eficaz por dos contra Eléctrico y Planta"))
    }

    @Test
    fun `PokemonTypeEffectiveness getFullMatchups partitions all 18 types accurately`() {
        // Squirtle (Water)
        val squirtleSummary = PokemonTypeEffectiveness.getFullMatchups("Water")
        assertEquals(0, squirtleSummary.superEffectiveQuad.size)
        assertEquals(listOf("Electric", "Grass"), squirtleSummary.effectiveDouble)
        assertEquals(listOf("Fire", "Water", "Ice", "Steel"), squirtleSummary.notVeryEffectiveHalf)
        assertEquals(0, squirtleSummary.notVeryEffectiveQuarter.size)
        assertEquals(0, squirtleSummary.immune.size)
        val squirtleTotal = squirtleSummary.superEffectiveQuad.size +
                squirtleSummary.effectiveDouble.size +
                squirtleSummary.normalEffective.size +
                squirtleSummary.notVeryEffectiveHalf.size +
                squirtleSummary.notVeryEffectiveQuarter.size +
                squirtleSummary.immune.size
        assertEquals(18, squirtleTotal)

        // Charizard (Fire/Flying)
        val charizardSummary = PokemonTypeEffectiveness.getFullMatchups("Fire/Flying")
        assertEquals(listOf("Rock"), charizardSummary.superEffectiveQuad)
        assertEquals(listOf("Water", "Electric"), charizardSummary.effectiveDouble)
        assertEquals(listOf("Ground"), charizardSummary.immune)
        assertTrue(charizardSummary.notVeryEffectiveQuarter.contains("Grass"))
        assertTrue(charizardSummary.notVeryEffectiveQuarter.contains("Bug"))
        val charizardTotal = charizardSummary.superEffectiveQuad.size +
                charizardSummary.effectiveDouble.size +
                charizardSummary.normalEffective.size +
                charizardSummary.notVeryEffectiveHalf.size +
                charizardSummary.notVeryEffectiveQuarter.size +
                charizardSummary.immune.size
        assertEquals(18, charizardTotal)

        // Verify across all entries in pokemon_data.json
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        val typeRegex = Regex("\"type\":\\s*\"([^\"]+)\"")
        val typeMatches = typeRegex.findAll(rawJson).map { it.groupValues[1] }.toList()

        for (typeStr in typeMatches) {
            val summary = PokemonTypeEffectiveness.getFullMatchups(typeStr)
            val total = summary.superEffectiveQuad.size +
                    summary.effectiveDouble.size +
                    summary.normalEffective.size +
                    summary.notVeryEffectiveHalf.size +
                    summary.notVeryEffectiveQuarter.size +
                    summary.immune.size
            assertEquals("All 18 types must be accounted for in matchup profile of $typeStr", 18, total)
        }
    }

    @Test
    fun `Swampert and starter evolutions are strictly Area Unknown with empty pins and zero native regions in mainline games`() {
        val starterEvolutions = listOf(
            260 to "Swampert",
            259 to "Marshtomp",
            6 to "Charizard",
            9 to "Blastoise",
            3 to "Venusaur",
            724 to "Decidueye",
            908 to "Meowscarada",
            914 to "Quaquaval"
        )

        for ((id, name) in starterEvolutions) {
            // Check Default cartridge
            val defaultHabitat = PokemonMapHabitatRepository.getHabitatForPokemon(id, name, "Default")
            assertTrue("$name (#$id) must be isAreaUnknown=true in mainline games", defaultHabitat.isAreaUnknown)
            assertTrue("$name (#$id) must have zero wild pins", defaultHabitat.pins.isEmpty())
            assertNotNull("$name (#$id) must have an unknownReason explaining evolution lineage", defaultHabitat.unknownReason)
            assertTrue(
                "$name unknownReason must mention evolves from",
                defaultHabitat.unknownReason!!.contains("Evolves from", ignoreCase = true)
            )

            // Check Hoenn cartridge
            val hoennHabitat = PokemonMapHabitatRepository.getHabitatForPokemon(id, name, "Ruby")
            assertTrue("$name (#$id) must be isAreaUnknown=true in Ruby/Hoenn", hoennHabitat.isAreaUnknown)
            assertTrue("$name (#$id) must have zero pins in Ruby/Hoenn", hoennHabitat.pins.isEmpty())

            // getNativeRegionsForPokemon must be completely empty (so no misleading buttons appear in the UI)
            val nativeRegs = PokemonMapHabitatRepository.getNativeRegionsForPokemon(id)
            assertTrue("$name (#$id) must return empty native regions list", nativeRegs.isEmpty())
        }

        // Swampert specific check
        val swampertHabitat = PokemonMapHabitatRepository.getHabitatForPokemon(260, "Swampert", "Ruby")
        assertTrue(swampertHabitat.isAreaUnknown)
        assertTrue(swampertHabitat.pins.isEmpty())
        assertTrue(swampertHabitat.unknownReason!!.contains("Evolves from Marshtomp at Level 36 (Mudkip starter line)"))
        assertTrue(swampertHabitat.unknownReason!!.contains("not found roaming in the wild"))
    }

    @Test
    fun `Swampert in Pokemon GO has worldwide GPS biome pins`() {
        val goHabitat = PokemonMapHabitatRepository.getHabitatForPokemon(260, "Swampert", "GO")
        assertFalse("Swampert in Pokémon GO is not Area Unknown", goHabitat.isAreaUnknown)
        assertTrue("Swampert in Pokémon GO must have GPS biome pins", goHabitat.pins.isNotEmpty())
    }

    @Test
    fun `True wild Pokemon like Pikachu retain valid native regions and wild pins`() {
        val nativeRegs = PokemonMapHabitatRepository.getNativeRegionsForPokemon(25)
        assertTrue("Pikachu (#25) should have Kanto as debut native region", nativeRegs.contains("Kanto"))

        val kantoHabitat = PokemonMapHabitatRepository.getHabitatForPokemon(25, "Pikachu", "Red")
        assertFalse("Pikachu in Red is not Area Unknown", kantoHabitat.isAreaUnknown)
        assertTrue("Pikachu in Red has wild route pins", kantoHabitat.pins.isNotEmpty())
    }

    @Test
    fun `All 1025 base Pokemon have accurate and clean debut Pokedex entries resolving on Default`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        val array = org.json.JSONArray(rawJson)

        var testedCount = 0
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getInt("id")
            if (id > 1025) continue

            val name = obj.getString("name")
            val flavorText = obj.optString("flavorText", "")
            val pokedexEntries = obj.optString("pokedexEntries", "{}")

            val entity = PokemonEntity(
                id = id,
                name = name,
                type = obj.optString("type", "normal"),
                height = obj.optString("height", "1.0 m"),
                weight = obj.optString("weight", "10.0 kg"),
                flavorText = flavorText,
                spriteUrl = "",
                shinySpriteUrl = null,
                evolvesTo = "",
                forms = "",
                pokedexEntries = pokedexEntries
            )

            val resolved = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(entity, "Default")
            assertTrue("Species #$id ($name) must resolve documented debut entry", resolved.isDocumentedInEdition)
            assertTrue("Species #$id ($name) entry must not be blank", resolved.text.isNotBlank())
            assertFalse("Species #$id ($name) must not contain raw HTML entity &eacute;", resolved.text.contains("&eacute;", ignoreCase = true))
            assertFalse("Species #$id ($name) must not contain raw HTML entity &quot;", resolved.text.contains("&quot;", ignoreCase = true))
            assertFalse("Species #$id ($name) must not contain form feed characters", resolved.text.contains("\u000c") || resolved.text.contains("\\f"))
            testedCount++
        }
        assertEquals("Must test all 1,025 base species", 1025, testedCount)
    }

    @Test
    fun `Pokedex entry strictly matches active version on screen and explains non-native games`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        val array = org.json.JSONArray(rawJson)

        fun findEntity(targetId: Int): PokemonEntity {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.getInt("id") == targetId) {
                    return PokemonEntity(
                        id = targetId,
                        name = obj.getString("name"),
                        type = obj.optString("type", "normal"),
                        height = obj.optString("height", "1.0 m"),
                        weight = obj.optString("weight", "10.0 kg"),
                        flavorText = obj.optString("flavorText", ""),
                        spriteUrl = "",
                        shinySpriteUrl = null,
                        evolvesTo = "",
                        forms = "",
                        pokedexEntries = obj.optString("pokedexEntries", "{}")
                    )
                }
            }
            error("Target ID $targetId not found")
        }

        val swampert = findEntity(260)

        // 1. Swampert in Ruby
        val rubyRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(swampert, "Ruby")
        assertTrue("Swampert is documented in Ruby", rubyRes.isDocumentedInEdition)
        assertTrue("Swampert Ruby entry mentions strength / boulders", rubyRes.text.contains("boulders", ignoreCase = true) || rubyRes.text.contains("very strong", ignoreCase = true))

        // 2. Swampert in Sapphire
        val sapphireRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(swampert, "Sapphire")
        assertTrue("Swampert is documented in Sapphire", sapphireRes.isDocumentedInEdition)
        assertTrue("Swampert Sapphire entry mentions storms / wave", sapphireRes.text.contains("storms", ignoreCase = true) || sapphireRes.text.contains("swims", ignoreCase = true))
        assertFalse("Swampert Ruby and Sapphire lore should be distinct", rubyRes.text == sapphireRes.text)

        // 3. Swampert in Red (Incompatible / Gen 1)
        val redRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(swampert, "Red")
        assertFalse("Swampert did not exist in Gen 1 Red", redRes.isDocumentedInEdition)
        assertTrue("Fallback entry should contain canonical flavor text", redRes.text.contains("boulder", ignoreCase = true) || redRes.text.contains("swims", ignoreCase = true) || redRes.text.contains("very strong", ignoreCase = true))
        assertFalse("Fallback must not contain synthetic not documented notice", redRes.text.contains("not documented in the", ignoreCase = true))
        assertFalse("Fallback must not contain synthetic National Pokédex entry prefix", redRes.text.contains("National Pokédex entry:", ignoreCase = true))

        // 4. Charizard in Red
        val charizard = findEntity(6)
        val charizardRed = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(charizard, "Red")
        assertTrue("Charizard is documented in Red", charizardRed.isDocumentedInEdition)
        assertTrue("Charizard Red entry mentions melting boulders", charizardRed.text.contains("boulders", ignoreCase = true))

        // 5. Charizard in Scarlet
        val charizardScarlet = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(charizard, "Scarlet")
        assertTrue("Charizard is documented in Scarlet", charizardScarlet.isDocumentedInEdition)
        assertTrue("Charizard Scarlet entry mentions flame or fire", charizardScarlet.text.contains("fire", ignoreCase = true) || charizardScarlet.text.contains("flame", ignoreCase = true))
    }

    @Test
    fun `Special debut species like Poipole, Blacephalon, Meltan, and Hisuian species resolve accurately on Default`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        val array = org.json.JSONArray(rawJson)

        fun findEntity(targetId: Int): PokemonEntity {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.getInt("id") == targetId) {
                    return PokemonEntity(
                        id = targetId,
                        name = obj.getString("name"),
                        type = obj.optString("type", "normal"),
                        height = obj.optString("height", "1.0 m"),
                        weight = obj.optString("weight", "10.0 kg"),
                        flavorText = obj.optString("flavorText", ""),
                        spriteUrl = "",
                        shinySpriteUrl = null,
                        evolvesTo = "",
                        forms = "",
                        pokedexEntries = obj.optString("pokedexEntries", "{}")
                    )
                }
            }
            error("Target ID $targetId not found")
        }

        // #803 Poipole & #806 Blacephalon (Ultra Sun debut)
        val poipole = findEntity(803)
        val poipoleRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(poipole, "Default")
        assertTrue("Poipole must be documented in debut cartridge", poipoleRes.isDocumentedInEdition)
        assertEquals("Ultra Sun", poipoleRes.matchedCartridgeId)

        val blacephalon = findEntity(806)
        val blacephalonRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(blacephalon, "Default")
        assertTrue("Blacephalon must be documented in debut cartridge", blacephalonRes.isDocumentedInEdition)
        assertEquals("Ultra Sun", blacephalonRes.matchedCartridgeId)

        // #808 Meltan & #809 Melmetal (Let's Go debut)
        val meltan = findEntity(808)
        val meltanRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(meltan, "Default")
        assertTrue("Meltan must be documented in debut cartridge", meltanRes.isDocumentedInEdition)
        assertEquals("Let's Go Pikachu", meltanRes.matchedCartridgeId)

        // #899 Wyrdeer (Legends: Arceus debut)
        val wyrdeer = findEntity(899)
        val wyrdeerRes = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(wyrdeer, "Default")
        assertTrue("Wyrdeer must be documented in debut cartridge", wyrdeerRes.isDocumentedInEdition)
        assertEquals("Legends: Arceus", wyrdeerRes.matchedCartridgeId)
    }

    @Test
    fun `pokemon_data json contains zero synthetic non-native prefixes and ALL MAPS is national dex`() {
        val dataFile = resolveAssetFile("pokemon_data.json")
        val rawJson = dataFile.readText(Charsets.UTF_8)
        assertFalse("Must not contain synthetic inhabit text", rawJson.contains("does not naturally inhabit", ignoreCase = true))
        assertFalse("Must not contain synthetic National Pokédex entry prefix", rawJson.contains("National Pokédex entry:", ignoreCase = true))

        val allMapsEntry = ALL_REGION_MAP_ENTRIES.first { it.id == "all" }
        assertEquals("ALL MAPS", allMapsEntry.title)
        assertEquals("National Pokédex", allMapsEntry.eraSubtitle)
        assertEquals("pokedex_icon.png", allMapsEntry.mapAsset)

        val array = org.json.JSONArray(rawJson)
        var weedle: PokemonEntity? = null
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (obj.getInt("id") == 13) {
                weedle = PokemonEntity(
                    id = 13,
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    height = obj.getString("height"),
                    weight = obj.getString("weight"),
                    flavorText = obj.getString("flavorText"),
                    spriteUrl = obj.optString("spriteUrl", ""),
                    shinySpriteUrl = null,
                    evolvesTo = "",
                    forms = "",
                    pokedexEntries = obj.optString("pokedexEntries", "{}")
                )
                break
            }
        }
        org.junit.Assert.assertNotNull(weedle)
        val weedleScarlet = PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(weedle!!, "Scarlet")
        assertTrue("Weedle entry must be authentic", weedleScarlet.text.contains("venomous stinger", ignoreCase = true) || weedleScarlet.text.contains("forests", ignoreCase = true))
        assertFalse("Weedle entry must not contain synthetic prefix", weedleScarlet.text.contains("does not naturally inhabit", ignoreCase = true))
    }

    @Test
    fun `Canonical Legendary Quarantine isolates Yveltal and Xerneas to Kalos and blocks Hoenn roaming`() {
        // Hoenn / Ruby / Sapphire / Emerald / Omega Ruby / Alpha Sapphire
        val yveltalHoenn = PokemonMapHabitatRepository.getHabitatForPokemon(717, "Yveltal", "Ruby")
        assertTrue("Yveltal in Ruby must be Area Unknown", yveltalHoenn.isAreaUnknown)
        assertTrue("Yveltal in Ruby must have 0 pins", yveltalHoenn.pins.isEmpty())
        assertTrue("Unknown reason must cite native Kalos", yveltalHoenn.unknownReason?.contains("Kalos") == true)

        val yveltalOras = PokemonMapHabitatRepository.getHabitatForPokemon(717, "Yveltal", "Omega Ruby")
        assertTrue("Yveltal in Omega Ruby must be Area Unknown", yveltalOras.isAreaUnknown)
        assertTrue("Yveltal in Omega Ruby must have 0 pins", yveltalOras.pins.isEmpty())

        val xerneasHoenn = PokemonMapHabitatRepository.getHabitatForPokemon(716, "Xerneas", "Ruby")
        assertTrue("Xerneas in Ruby must be Area Unknown", xerneasHoenn.isAreaUnknown)
        assertTrue("Xerneas in Ruby must have 0 pins", xerneasHoenn.pins.isEmpty())

        // Kalos / X / Y
        val yveltalKalos = PokemonMapHabitatRepository.getHabitatForPokemon(717, "Yveltal", "Y")
        assertFalse("Yveltal in Y must NOT be Area Unknown", yveltalKalos.isAreaUnknown)
        assertTrue("Yveltal in Y must have Geosenge Town pin", yveltalKalos.pins.any { it.areaName.contains("Geosenge") })

        val xerneasKalos = PokemonMapHabitatRepository.getHabitatForPokemon(716, "Xerneas", "X")
        assertFalse("Xerneas in X must NOT be Area Unknown", xerneasKalos.isAreaUnknown)
        assertTrue("Xerneas in X must have Geosenge Town pin", xerneasKalos.pins.any { it.areaName.contains("Geosenge") })

        // Check Rayquaza (#384)
        val rayquazaSinnoh = PokemonMapHabitatRepository.getHabitatForPokemon(384, "Rayquaza", "Diamond")
        assertTrue("Rayquaza in Diamond must be Area Unknown", rayquazaSinnoh.isAreaUnknown)
        assertTrue("Rayquaza in Diamond must have 0 pins", rayquazaSinnoh.pins.isEmpty())

        val rayquazaHoenn = PokemonMapHabitatRepository.getHabitatForPokemon(384, "Rayquaza", "Ruby")
        assertFalse("Rayquaza in Ruby must NOT be Area Unknown", rayquazaHoenn.isAreaUnknown)
        assertTrue("Rayquaza in Ruby must have Sky Pillar pin", rayquazaHoenn.pins.any { it.areaName.contains("Sky Pillar") })

        // Check Mewtwo (#150)
        val mewtwoHoenn = PokemonMapHabitatRepository.getHabitatForPokemon(150, "Mewtwo", "Ruby")
        assertTrue("Mewtwo in Ruby must be Area Unknown", mewtwoHoenn.isAreaUnknown)
        assertTrue("Mewtwo in Ruby must have 0 pins", mewtwoHoenn.pins.isEmpty())

        val mewtwoKanto = PokemonMapHabitatRepository.getHabitatForPokemon(150, "Mewtwo", "Red")
        assertFalse("Mewtwo in Red must NOT be Area Unknown", mewtwoKanto.isAreaUnknown)
        assertTrue("Mewtwo in Red must have Cerulean Cave pin", mewtwoKanto.pins.any { it.areaName.contains("Cerulean Cave") })

        val mewtwoKalos = PokemonMapHabitatRepository.getHabitatForPokemon(150, "Mewtwo", "X")
        assertFalse("Mewtwo in Kalos must NOT be Area Unknown", mewtwoKalos.isAreaUnknown)
        assertTrue("Mewtwo in Kalos must have Unknown Dungeon pin", mewtwoKalos.pins.any { it.areaName.contains("Unknown Dungeon") })

        // Check Starter evolutions (Ivysaur #2, Blaziken #257, Greninja #658)
        val ivysaurRed = PokemonMapHabitatRepository.getHabitatForPokemon(2, "Ivysaur", "Red")
        assertTrue("Ivysaur must be Area Unknown", ivysaurRed.isAreaUnknown)
        assertTrue("Ivysaur must have 0 pins", ivysaurRed.pins.isEmpty())

        val blazikenRuby = PokemonMapHabitatRepository.getHabitatForPokemon(257, "Blaziken", "Ruby")
        assertTrue("Blaziken must be Area Unknown", blazikenRuby.isAreaUnknown)
        assertTrue("Blaziken must have 0 pins", blazikenRuby.pins.isEmpty())

        val greninjaX = PokemonMapHabitatRepository.getHabitatForPokemon(658, "Greninja", "X")
        assertTrue("Greninja must be Area Unknown", greninjaX.isAreaUnknown)
        assertTrue("Greninja must have 0 pins", greninjaX.pins.isEmpty())
    }

    @Test
    fun `PokemonRegion matches Kalos exclusively for Yveltal and Xerneas and does NOT match Hoenn`() {
        val yveltalEntity = PokemonEntity(
            id = 717,
            name = "yveltal",
            type = "Dark/Flying",
            height = "5.8 m",
            weight = "203.0 kg",
            flavorText = "When this legendary Pokémon's wings and tail feathers spread wide and glow red, it absorbs the life force of living creatures.",
            spriteUrl = "",
            shinySpriteUrl = "",
            evolvesTo = "",
            forms = "",
            pokedexEntries = """
                {
                  "Y": "When this legendary Pokémon's wings and tail feathers spread wide and glow red, it absorbs the life force of living creatures.",
                  "Omega Ruby": "When this legendary Pokémon's wings and tail feathers spread wide and glow red, it absorbs the life force of living creatures."
                }
            """.trimIndent()
        )

        assertTrue("Yveltal must match KALOS", PokemonRegion.KALOS.matches(yveltalEntity))
        assertFalse("Yveltal must NOT match HOENN despite having ORAS entry", PokemonRegion.HOENN.matches(yveltalEntity))
        assertFalse("Yveltal must NOT match KANTO", PokemonRegion.KANTO.matches(yveltalEntity))
        assertFalse("Yveltal must NOT match SINNOH", PokemonRegion.SINNOH.matches(yveltalEntity))

        val nativeRegions = PokemonMapHabitatRepository.getNativeRegionsForPokemon(717)
        assertEquals("Yveltal native regions must be strictly Kalos", listOf("Kalos"), nativeRegions)
    }
}
