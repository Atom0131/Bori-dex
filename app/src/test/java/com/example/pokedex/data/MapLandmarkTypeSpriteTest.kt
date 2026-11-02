package com.example.pokedex.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MapLandmarkTypeSpriteTest {
    private val assetsDir = listOf(
        File("src/main/assets/items"),
        File("app/src/main/assets/items"),
        File("Pokedex/PokedexApp/app/src/main/assets/items"),
        File("C:\\Users\\pagan\\OneDrive\\Documents\\Code\\Pokedex\\PokedexApp\\app\\src\\main\\assets\\items")
    ).firstOrNull { it.exists() } ?: File("src/main/assets/items")

    @Test
    fun testAllItemSpritesExist() {
        for (type in MapLandmarkType.values()) {
            val spriteFile = File(assetsDir, type.itemSprite)
            assertTrue("Sprite file for ${type.name} not found: ${type.itemSprite}", spriteFile.exists())
        }
    }
}
