package com.example.pokedex.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Comprehensive test suite verifying that all 13 Strategy Guide categories
 * and all 84+ topics map to authentic, existing Pokémon pixel sprite assets.
 */
class PokemonGameGuideSpriteComprehensiveTest {

    private fun resolveAssetFile(relativePath: String): File {
        val candidates = listOf(
            File("src/main/assets/$relativePath"),
            File("app/src/main/assets/$relativePath"),
            File("Pokedex/PokedexApp/app/src/main/assets/$relativePath")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate $relativePath from working dir ${File(".").absolutePath}")
    }

    private fun resolveItemsDir(): File {
        val candidates = listOf(
            File("src/main/assets/items"),
            File("app/src/main/assets/items"),
            File("Pokedex/PokedexApp/app/src/main/assets/items")
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("Could not locate items directory from working dir ${File(".").absolutePath}")
    }

    @Test
    fun `test all 13 categories have valid non-null pixel sprite assets in assets items`() {
        val itemsDir = resolveItemsDir()
        val expectedCategories = listOf(
            "healing_items",
            "key_items",
            "pokeballs",
            "money_items",
            "event_items",
            "evolution_items",
            "battle_items",
            "missions_roadblocks",
            "hidden_items",
            "secret_messages_braille",
            "legendaries",
            "event_pokemon",
            "puzzles_dungeons"
        )

        assertEquals("Must have exactly 13 categories", 13, expectedCategories.size)
        assertEquals("healing_items must map to potion.png", "potion.png", PokemonGameGuideRepository.CATEGORY_ICONS["healing_items"])
        assertEquals("key_items must map to key-items.png", "key-items.png", PokemonGameGuideRepository.CATEGORY_ICONS["key_items"])
        assertEquals("pokeballs must map to master-ball.png", "master-ball.png", PokemonGameGuideRepository.CATEGORY_ICONS["pokeballs"])
        assertEquals("battle_items must map to exp-share.png", "exp-share.png", PokemonGameGuideRepository.CATEGORY_ICONS["battle_items"])
        assertEquals("legendaries must map to weather-trio-wall.png", "weather-trio-wall.png", PokemonGameGuideRepository.CATEGORY_ICONS["legendaries"])

        for (catId in expectedCategories) {
            val spriteName = PokemonGameGuideRepository.CATEGORY_ICONS[catId]
            assertNotNull("Category '$catId' must have an icon defined in CATEGORY_ICONS", spriteName)
            assertTrue("Sprite name must end in .png for '$catId'", spriteName!!.endsWith(".png"))

            val spriteFile = File(itemsDir, spriteName)
            assertTrue("Sprite asset '$spriteName' for category '$catId' must exist at ${spriteFile.path}", spriteFile.exists())
            assertTrue("Sprite asset '$spriteName' must not be empty", spriteFile.length() > 0)
        }
    }

    @Test
    fun `test all 84 topics declared in game_guides_index_json have valid existing pixel sprite assets`() {
        val guideJsonFile = resolveAssetFile("game_guides_index.json")
        val itemsDir = resolveItemsDir()
        val json = JSONObject(guideJsonFile.readText(Charsets.UTF_8))
        val topicsObj = json.getJSONObject("topics")

        val topicKeys = topicsObj.keys()
        val topicIdList = mutableListOf<String>()
        while (topicKeys.hasNext()) {
            topicIdList.add(topicKeys.next())
        }

        assertTrue("Must have at least 80 topics declared (found ${topicIdList.size})", topicIdList.size >= 80)

        for (topicId in topicIdList) {
            val topicData = topicsObj.getJSONObject(topicId)
            val topicName = topicData.getString("name")

            val resolvedAsset = PokemonGameGuideRepository.resolveItemIconAsset(topicName, topicId = topicId)
            assertNotNull("Topic '$topicId' ('$topicName') must resolve to a valid sprite asset", resolvedAsset)
            assertTrue("Resolved asset for topic '$topicId' must end with .png", resolvedAsset!!.endsWith(".png"))

            val assetFile = File(itemsDir, resolvedAsset)
            assertTrue(
                "Resolved sprite asset '$resolvedAsset' for topic '$topicId' ('$topicName') must exist at ${assetFile.path}",
                assetFile.exists()
            )
            assertTrue("Sprite asset '$resolvedAsset' must have positive size", assetFile.length() > 0)
        }
    }

    @Test
    fun `test GuideCategory data class properly assigns category iconAsset`() {
        val itemsDir = resolveItemsDir()
        val guideJsonFile = resolveAssetFile("game_guides_index.json")
        val json = JSONObject(guideJsonFile.readText(Charsets.UTF_8))
        val catArray = json.getJSONArray("categories")

        for (i in 0 until catArray.length()) {
            val c = catArray.getJSONObject(i)
            val catId = c.getString("id")
            val cat = GuideCategory(
                id = catId,
                title = c.getString("title"),
                emoji = c.optString("emoji", "📚"),
                subtitle = c.optString("subtitle", ""),
                topicIds = emptyList()
            )

            assertNotNull("GuideCategory.iconAsset must not be null for '$catId'", cat.iconAsset)
            assertTrue("GuideCategory.iconAsset must end with .png", cat.iconAsset.endsWith(".png"))
            assertTrue("Physical asset for GuideCategory must exist: ${cat.iconAsset}", File(itemsDir, cat.iconAsset).exists())
        }
    }
}
