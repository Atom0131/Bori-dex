package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class PokemonGameGuideCategoryTest {

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
    fun `getCategories returns all 13 canonical strategy guide categories`() {
        val categories = PokemonGameGuideRepository.getCategories()
        assertEquals("Must return 13 categories", 13, categories.size)

        val expectedIds = setOf(
            "healing_items", "money_items", "key_items", "pokeballs", "event_items",
            "evolution_items", "battle_items", "missions_roadblocks", "hidden_items",
            "secret_messages_braille", "legendaries", "event_pokemon", "puzzles_dungeons"
        )
        val actualIds = categories.map { it.id }.toSet()
        assertEquals("All expected category IDs must be present", expectedIds, actualIds)
    }

    @Test
    fun `getTopicsForCategory retrieves topics correctly for legendaries and secret messages`() {
        val legends = PokemonGameGuideRepository.getTopicsForCategory("legendaries")
        assertTrue("Legendaries category must not be empty", legends.isNotEmpty())
        assertTrue("Mewtwo must be in legendaries", legends.any { it.id == "mewtwo_encounter" })
        assertTrue("Treasures of Ruin must be in legendaries", legends.any { it.id == "treasures_of_ruin_stakes" })

        val secrets = PokemonGameGuideRepository.getTopicsForCategory("secret_messages_braille")
        assertTrue("Braille category must not be empty", secrets.isNotEmpty())
        assertTrue("Sealed Chamber must be in Braille category", secrets.any { it.id == "sealed_chamber_braille" })
    }

    @Test
    fun `Sealed Chamber Braille puzzle resolves party differences between Ruby and Emerald`() {
        val rubyRes = PokemonGameGuideRepository.queryGuide("Sealed Chamber Braille", "Ruby")
        assertTrue("Ruby instructions mention Relicanth first and Wailord 6th", rubyRes.steps.any { it.contains("Relicanth in the 1st", ignoreCase = true) })

        val emeraldRes = PokemonGameGuideRepository.queryGuide("Sealed Chamber Braille", "Emerald")
        assertTrue("Emerald instructions mention Wailord first and Relicanth 6th", emeraldRes.steps.any { it.contains("WAILORD in the 1st", ignoreCase = true) })
    }

    @Test
    fun `Amulet Coin resolves version-specifically across Emerald, FireRed, and Violet`() {
        val emRes = PokemonGameGuideRepository.queryGuide("Amulet Coin", "Emerald")
        assertTrue("Emerald Amulet Coin is given by Mom in Littleroot", emRes.steps.any { it.contains("Mom", ignoreCase = true) && it.contains("Littleroot", ignoreCase = true) })

        val frRes = PokemonGameGuideRepository.queryGuide("Amulet Coin", "FireRed")
        assertTrue("FireRed Amulet Coin is given by Oak's Aide on Route 16", frRes.location.contains("Route 16", ignoreCase = true))

        val viRes = PokemonGameGuideRepository.queryGuide("Amulet Coin", "Violet")
        assertTrue("Violet Amulet Coin is at Medali League Rep", viRes.location.contains("Medali", ignoreCase = true))
    }

    @Test
    fun `Mewtwo Cerulean Cave guide distinguishes Red from FireRed Sevii requirements`() {
        val redRes = PokemonGameGuideRepository.queryGuide("Mewtwo", "Red")
        assertTrue("Red Mewtwo requires Elite Four / Champion", redRes.prerequisites?.contains("Elite Four", ignoreCase = true) == true)

        val frRes = PokemonGameGuideRepository.queryGuide("Mewtwo", "FireRed")
        assertTrue("FireRed Mewtwo requires Sevii Islands Ruby & Sapphire", frRes.steps.any { it.contains("Ruby", ignoreCase = true) && it.contains("Sapphire", ignoreCase = true) })
    }

    @Test
    fun `Trick House puzzle guide details challenges across 8 rooms`() {
        val trickRes = PokemonGameGuideRepository.queryGuide("Trick House", "Emerald")
        assertTrue("Trick House is on Route 110", trickRes.location.contains("Route 110", ignoreCase = true))
        assertTrue("Trick House details Cut room", trickRes.steps.any { it.contains("Room 1 (Cut)", ignoreCase = true) })
        assertTrue("Trick House details Mecha Quizzes", trickRes.steps.any { it.contains("Room 5 (Mecha Quizzes)", ignoreCase = true) })
    }

    @Test
    fun `Treasures of Ruin in Violet details all 4 shrines and 32 stakes`() {
        val ruinRes = PokemonGameGuideRepository.queryGuide("Treasures of Ruin 32 stakes", "Violet")
        assertTrue("Treasures of Ruin mentions 4 shrines", ruinRes.location.contains("4 Shrines", ignoreCase = true))
        assertTrue("Mentions Wo-Chien purple stakes", ruinRes.steps.any { it.contains("Wo-Chien", ignoreCase = true) && it.contains("Purple", ignoreCase = true) })
        assertTrue("Mentions Chien-Pao yellow stakes", ruinRes.steps.any { it.contains("Chien-Pao", ignoreCase = true) && it.contains("Yellow", ignoreCase = true) })
        assertTrue("Mentions Ting-Lu green stakes", ruinRes.steps.any { it.contains("Ting-Lu", ignoreCase = true) && it.contains("Green", ignoreCase = true) })
        assertTrue("Mentions Chi-Yu blue stakes", ruinRes.steps.any { it.contains("Chi-Yu", ignoreCase = true) && it.contains("Blue", ignoreCase = true) })
    }

    @Test
    fun `healing_items category contains complete potions, revives, sacred ash, and berries`() {
        val healingTopics = PokemonGameGuideRepository.getTopicsForCategory("healing_items")
        assertTrue("Healing category must contain at least 10 items", healingTopics.size >= 10)

        val topicIds = healingTopics.map { it.id }.toSet()
        assertTrue("Must include potion", topicIds.contains("potion"))
        assertTrue("Must include hyper_max_potion", topicIds.contains("hyper_max_potion"))
        assertTrue("Must include full_restore", topicIds.contains("full_restore"))
        assertTrue("Must include revive_items", topicIds.contains("revive_items"))
        assertTrue("Must include sacred_ash", topicIds.contains("sacred_ash"))
        assertTrue("Must include sitrus_berry", topicIds.contains("sitrus_berry"))
        assertTrue("Must include lum_berry", topicIds.contains("lum_berry"))
        assertTrue("Must include leppa_berry", topicIds.contains("leppa_berry"))
        assertTrue("Must include oran_berry", topicIds.contains("oran_berry"))
        assertTrue("Must include natural_drinks", topicIds.contains("natural_drinks"))
    }

    @Test
    fun `healing items and berries resolve with accurate map pinpoint coordinates`() {
        // Full Restore in Ruby
        val fullRestoreRes = PokemonGameGuideRepository.queryGuide("Full Restore", "Ruby")
        assertNotNull("Full Restore must have pinX", fullRestoreRes.pinX)
        assertNotNull("Full Restore must have pinY", fullRestoreRes.pinY)
        assertTrue("Full Restore location mentions Lilycove / Ever Grande", fullRestoreRes.location.contains("Lilycove", ignoreCase = true) || fullRestoreRes.location.contains("Ever Grande", ignoreCase = true))

        // Sacred Ash in Gold
        val sacredAshRes = PokemonGameGuideRepository.queryGuide("Sacred Ash", "Gold")
        assertNotNull("Sacred Ash must have pinX", sacredAshRes.pinX)
        assertNotNull("Sacred Ash must have pinY", sacredAshRes.pinY)
        assertTrue("Sacred Ash mentions Tin Tower", sacredAshRes.location.contains("Tin Tower", ignoreCase = true))

        // Sitrus Berry in Emerald
        val sitrusRes = PokemonGameGuideRepository.queryGuide("Sitrus Berry", "Emerald")
        assertNotNull("Sitrus Berry must have pinX", sitrusRes.pinX)
        assertNotNull("Sitrus Berry must have pinY", sitrusRes.pinY)
        assertTrue("Sitrus Berry mentions Berry Master / Route 123", sitrusRes.location.contains("Route 123", ignoreCase = true) || sitrusRes.location.contains("Route 119", ignoreCase = true))

        // Lum Berry in Ruby
        val lumRes = PokemonGameGuideRepository.queryGuide("Lum Berry", "Ruby")
        assertNotNull("Lum Berry must have pinX", lumRes.pinX)
        assertNotNull("Lum Berry must have pinY", lumRes.pinY)

        // Leppa Berry in Emerald
        val leppaRes = PokemonGameGuideRepository.queryGuide("Leppa Berry", "Emerald")
        assertNotNull("Leppa Berry must have pinX", leppaRes.pinX)
        assertNotNull("Leppa Berry must have pinY", leppaRes.pinY)
    }

    @Test
    fun `items and roadblocks pinpoint map locations with pulsing coordinates`() {
        // Exp. Share in Ruby -> Rustboro City (Devon Corp)
        val rubyExp = PokemonGameGuideRepository.queryGuide("Exp. Share", "Ruby")
        assertNotNull("Exp Share in Ruby must have pinX", rubyExp.pinX)
        assertNotNull("Exp Share in Ruby must have pinY", rubyExp.pinY)
        assertTrue("Exp Share pin label mentions Rustboro", rubyExp.pinLocationName?.contains("Rustboro", ignoreCase = true) == true)

        // Mewtwo in Red -> Cerulean Cave B1F
        val redMewtwo = PokemonGameGuideRepository.queryGuide("Mewtwo", "Red")
        assertNotNull("Mewtwo in Red must have pinX", redMewtwo.pinX)
        assertNotNull("Mewtwo in Red must have pinY", redMewtwo.pinY)
        assertTrue("Mewtwo pin label mentions Cerulean Cave", redMewtwo.pinLocationName?.contains("Cerulean Cave", ignoreCase = true) == true)
    }

    @Test
    fun `Potion resolves with First Encounter Pin and Region-Wide Mart Progression across Paldea and Hoenn`() {
        // Violet / Paldea Potion
        val viPotion = PokemonGameGuideRepository.queryGuide("Potion", "Violet")
        println("TEST DEBUG viPotion: subAvail='${viPotion.subsequentAvailability}', spoken='${viPotion.spokenAdvice}'")
        assertEquals("Must load Paldea map asset", "paldea_sv.png", viPotion.mapAsset)
        assertNotNull("Must have pinX", viPotion.pinX)
        assertNotNull("Must have pinY", viPotion.pinY)
        assertTrue("Pin must target Mesagoza first unlock", viPotion.pinLocationName?.contains("Mesagoza", ignoreCase = true) == true)
        assertNotNull("firstEncounter must be populated", viPotion.firstEncounter)
        assertTrue("First encounter mentions Mesagoza", viPotion.firstEncounter!!.contains("Mesagoza", ignoreCase = true))
        assertNotNull("subsequentAvailability must be populated", viPotion.subsequentAvailability)
        assertTrue("Subsequent availability mentions every Poké Mart in Paldea", viPotion.subsequentAvailability!!.contains("every single", ignoreCase = true) && viPotion.subsequentAvailability!!.contains("Paldea", ignoreCase = true))
        assertTrue("Spoken advice explains first encounter and region-wide mart stock", viPotion.spokenAdvice.contains("first encountered", ignoreCase = true) && viPotion.spokenAdvice.contains("every single Poké Mart", ignoreCase = true))

        // Ruby / Hoenn Potion
        val rubyPotion = PokemonGameGuideRepository.queryGuide("Potion", "Ruby")
        assertEquals("Must load Hoenn map asset", "hoenn_rse.png", rubyPotion.mapAsset)
        assertNotNull("Must have pinX", rubyPotion.pinX)
        assertNotNull("Must have pinY", rubyPotion.pinY)
        assertTrue("Pin must target Oldale Town first unlock", rubyPotion.pinLocationName?.contains("Oldale", ignoreCase = true) == true)
        assertNotNull("firstEncounter must be populated", rubyPotion.firstEncounter)
        assertTrue("First encounter mentions Oldale", rubyPotion.firstEncounter!!.contains("Oldale", ignoreCase = true))
        assertNotNull("subsequentAvailability must be populated", rubyPotion.subsequentAvailability)
        assertTrue("Subsequent availability mentions Hoenn", rubyPotion.subsequentAvailability!!.contains("HOENN", ignoreCase = true))
    }

    @Test
    fun `Ultra Ball in Emerald has single First Encounter Pin at Fortree and explains progression`() {
        val ultraBall = PokemonGameGuideRepository.queryGuide("Ultra Ball", "Emerald")
        assertEquals("Must load Hoenn map asset", "hoenn_rse.png", ultraBall.mapAsset)
        assertNotNull("Must have pinX", ultraBall.pinX)
        assertNotNull("Must have pinY", ultraBall.pinY)
        assertTrue("First encounter pin mentions Fortree", ultraBall.firstEncounter?.contains("Fortree", ignoreCase = true) == true || ultraBall.pinLocationName?.contains("Fortree", ignoreCase = true) == true)
        assertTrue("Progression mentions all Poké Marts", ultraBall.subsequentAvailability?.contains("Poké Mart", ignoreCase = true) == true)
    }

    @Test
    fun `Item icon assets resolve accurately for healing, money, battle, and travel items`() {
        // Query results icons
        val potionResult = PokemonGameGuideRepository.queryGuide("Potion", "FireRed")
        assertEquals("potion.png", potionResult.itemIconAsset)

        val reviveResult = PokemonGameGuideRepository.queryGuide("Revive", "Emerald")
        assertEquals("revive.png", reviveResult.itemIconAsset)

        val ultraBallResult = PokemonGameGuideRepository.queryGuide("Ultra Ball", "Emerald")
        assertEquals("ultra-ball.png", ultraBallResult.itemIconAsset)

        val expShareResult = PokemonGameGuideRepository.queryGuide("Exp. Share", "Emerald")
        assertEquals("exp-share.png", expShareResult.itemIconAsset)

        // Strategy Category Topics icons
        val healingTopics = PokemonGameGuideRepository.getTopicsForCategory("healing_items")
        val potionTopic = healingTopics.firstOrNull { it.id == "potion" }
        assertNotNull("Potion topic must exist", potionTopic)
        assertEquals("potion.png", potionTopic?.iconAsset)

        val reviveTopic = healingTopics.firstOrNull { it.id == "revive_items" }
        assertNotNull("Revive topic must exist", reviveTopic)
        assertEquals("revive.png", reviveTopic?.iconAsset)

        val moneyTopics = PokemonGameGuideRepository.getTopicsForCategory("money_items")
        val nuggetTopic = moneyTopics.firstOrNull { it.id == "nugget" }
        assertNotNull("Nugget topic must exist", nuggetTopic)
        assertEquals("nugget.png", nuggetTopic?.iconAsset)

        val amuletCoinTopic = moneyTopics.firstOrNull { it.id == "amulet_coin" }
        assertNotNull("Amulet coin topic must exist", amuletCoinTopic)
        assertEquals("amulet-coin.png", amuletCoinTopic?.iconAsset)
    }

    @Test
    fun `Bidirectional flexible search resolves partial keywords without falling back to help`() {
        val relearner = PokemonGameGuideRepository.queryGuide("relearner", "Emerald")
        assertTrue("Relearner query matches Move Relearner", relearner.title.contains("Move Relearner", ignoreCase = true))

        val snorlax = PokemonGameGuideRepository.queryGuide("snorlax", "FireRed")
        assertTrue("Snorlax query matches Sleeping Giant Snorlax", snorlax.title.contains("Snorlax", ignoreCase = true))

        val devon = PokemonGameGuideRepository.queryGuide("devon goods", "Emerald")
        assertTrue("Devon Goods matches rescue mission", devon.title.contains("Devon Goods", ignoreCase = true))

        val quickBall = PokemonGameGuideRepository.queryGuide("quick ball", "Scarlet")
        assertTrue("Quick Ball matches topic", quickBall.title.contains("Quick Ball", ignoreCase = true))
        assertEquals("quick-ball.png", quickBall.itemIconAsset)

        val masterBall = PokemonGameGuideRepository.queryGuide("master ball", "FireRed")
        assertTrue("Master Ball matches guaranteed catch topic", masterBall.title.contains("Master Ball", ignoreCase = true))
        assertEquals("master-ball.png", masterBall.itemIconAsset)

        val rayquaza = PokemonGameGuideRepository.queryGuide("rayquaza", "Emerald")
        assertTrue("Rayquaza query matches legendary capture", rayquaza.title.contains("Rayquaza", ignoreCase = true))

        val ltSurge = PokemonGameGuideRepository.queryGuide("lt surge", "FireRed")
        assertTrue("Lt. Surge query matches Vermilion Gym", ltSurge.title.contains("Lt. Surge", ignoreCase = true) || ltSurge.title.contains("Vermilion", ignoreCase = true))

        val misty = PokemonGameGuideRepository.queryGuide("misty", "FireRed")
        assertTrue("Misty query matches Cerulean Gym", misty.title.contains("Misty", ignoreCase = true))
    }
}
