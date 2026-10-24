package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonMapHabitatRepositoryTest {

    @Test
    fun testBulbasaurInHeartGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "HeartGold")
        assertEquals("Kanto (Post-Game)", habitat.regionName)
        assertEquals("kanto_frlg.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertEquals(1, habitat.pins.size)
        val pin = habitat.pins.first()
        assertEquals("Pallet Town (Prof. Oak's Lab)", pin.areaName)
        assertTrue(pin.encounterMethod.contains("Professor Oak"))
        assertTrue(habitat.narrationText.contains("Professor Oak"))
        assertFalse(habitat.narrationText.contains("not found natively in the Kanto region"))
    }

    @Test
    fun testBulbasaurInGoldSilver() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "Gold")
        assertEquals("Johto", habitat.regionName)
        assertEquals("johto_gsc.png", habitat.mapAsset)
        assertTrue(habitat.isAreaUnknown)
        assertNotNull(habitat.unknownReason)
        assertTrue(habitat.unknownReason!!.contains("Time Capsule"))
        assertFalse(habitat.unknownReason!!.contains("not found natively in the Kanto region"))
    }

    @Test
    fun testChikoritaInHeartGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(152, "Chikorita", "HeartGold")
        assertEquals("Johto", habitat.regionName)
        assertEquals("johto_hgss.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertEquals(1, habitat.pins.size)
        val pin = habitat.pins.first()
        assertEquals("New Bark Town (Pokémon Lab)", pin.areaName)
        assertTrue(pin.encounterMethod.contains("Professor Elm"))
    }

    @Test
    fun testChikoritaInGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(152, "Chikorita", "Gold")
        assertEquals("Johto", habitat.regionName)
        assertEquals("johto_gsc.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertEquals(1, habitat.pins.size)
        val pin = habitat.pins.first()
        assertEquals("New Bark Town (Pokémon Lab)", pin.areaName)
        assertTrue(pin.encounterMethod.contains("Professor Elm"))
    }

    @Test
    fun testTreeckoInHeartGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(252, "Treecko", "HeartGold")
        assertEquals("Kanto (Post-Game)", habitat.regionName)
        assertEquals("kanto_frlg.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertEquals(1, habitat.pins.size)
        val pin = habitat.pins.first()
        assertEquals("Silph Co. (Saffron City)", pin.areaName)
        assertTrue(pin.encounterMethod.contains("Steven Stone"))
    }

    @Test
    fun testIvysaurInHeartGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(2, "Ivysaur", "HeartGold")
        assertEquals("Johto", habitat.regionName)
        assertTrue(habitat.isAreaUnknown)
        assertNotNull(habitat.unknownReason)
        assertFalse(habitat.unknownReason!!.contains("in Kanto"))
        assertFalse(habitat.unknownReason!!.contains("in the Kanto region"))
        assertTrue(habitat.unknownReason!!.contains("Johto"))
    }

    @Test
    fun testTreeckoInGold() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(252, "Treecko", "Gold")
        assertEquals("Johto", habitat.regionName)
        assertEquals("johto_gsc.png", habitat.mapAsset)
        assertTrue(habitat.isAreaUnknown)
        assertNotNull(habitat.unknownReason)
        assertFalse(habitat.unknownReason!!.contains("in Kanto"))
        assertTrue(habitat.unknownReason!!.contains("Johto"))
        assertTrue(habitat.unknownReason!!.contains("Hoenn"))
    }

    @Test
    fun testBulbasaurInLegendsZA() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "Legends: Z-A")
        assertEquals("Lumiose City / Kalos", habitat.regionName)
        assertEquals("kalos_xy.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertEquals(1, habitat.pins.size)
        val pin = habitat.pins.first()
        assertEquals("Prism Tower (Wild Sector)", pin.areaName)
        assertEquals("Wild Urban Overworld Capture", pin.encounterMethod)
        assertTrue(habitat.narrationText.contains("Prism Tower"))
    }

    @Test
    fun testBulbasaurInPokemonGO() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "POKÉMON GO")
        assertEquals("Pokémon GO", habitat.regionName)
        assertEquals("pokemongo_map.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertTrue(habitat.pins.isNotEmpty())
        val pinAreas = habitat.pins.map { it.areaName }
        assertTrue(pinAreas.any { it.contains("Worldwide GPS Biome") })
        assertTrue(pinAreas.any { it.contains("Egg Hatches") })
    }

    @Test
    fun testBulbasaurInScarletAndViolet() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "Scarlet")
        assertEquals("Paldea", habitat.regionName)
        assertEquals("paldea_sv.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        assertTrue(habitat.pins.any { it.areaName.contains("Blueberry Academy Terarium") })
        assertTrue(habitat.pins.any { it.areaName.contains("Link Trades") })
    }

    @Test
    fun testBulbasaurInPokemonX() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "X")
        assertEquals("Kalos", habitat.regionName)
        assertEquals("kalos_xy.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        val pin = habitat.pins.first()
        assertEquals("Lumiose City (Sycamore Pokémon Lab 2F)", pin.areaName)
        assertEquals("Gift from Professor Sycamore", pin.encounterMethod)
    }

    @Test
    fun testBulbasaurInClassicKantoRed() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "Red")
        assertEquals("Kanto", habitat.regionName)
        assertEquals("kanto_rgby.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        val pin = habitat.pins.first()
        assertEquals("Pallet Town (Pokémon Lab)", pin.areaName)
        assertEquals("Starter gift from Professor Oak", pin.encounterMethod)
    }

    @Test
    fun testBulbasaurInYellow() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(1, "Bulbasaur", "Yellow")
        assertEquals("Kanto", habitat.regionName)
        assertFalse(habitat.isAreaUnknown)
        val pin = habitat.pins.first()
        assertEquals("Cerulean City", pin.areaName)
        assertEquals("Gift from Melanie inside house", pin.encounterMethod)
    }

    @Test
    fun testSprigatitoInScarlet() {
        val habitat = PokemonMapHabitatRepository.getHabitatForPokemon(906, "Sprigatito", "Scarlet")
        assertEquals("Paldea", habitat.regionName)
        assertEquals("paldea_sv.png", habitat.mapAsset)
        assertFalse(habitat.isAreaUnknown)
        val pin = habitat.pins.first()
        assertEquals("Cabo Poco (Director Clavell's House)", pin.areaName)
        assertEquals("Starter gift from Director Clavell", pin.encounterMethod)
    }

    @Test
    fun testMapAssetResolutionForCartridges() {
        assertEquals("kalos_xy.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Legends: Z-A", 1))
        assertEquals("pokemongo_map.png", PokemonMapHabitatRepository.getMapAssetForCartridge("GO", 1))
        assertEquals("paldea_sv.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Scarlet", 1))
        assertEquals("kalos_xy.png", PokemonMapHabitatRepository.getMapAssetForCartridge("X", 1))
        assertEquals("kanto_rgby.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Red", 1))
        assertEquals("hisui_pla.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Legends: Arceus", 1))
        assertEquals("johto_hgss.png", PokemonMapHabitatRepository.getMapAssetForCartridge("HeartGold", 152))
        assertEquals("johto_gsc.png", PokemonMapHabitatRepository.getMapAssetForCartridge("Gold", 152))
    }
}
