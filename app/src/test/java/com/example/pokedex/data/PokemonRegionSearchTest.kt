package com.example.pokedex.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PokemonRegionSearchTest {

    @Test
    fun `PokemonRegion fromQuery does not hijack pokemon names containing 'go'`() {
        // Words containing 'go' should NOT match POKEMON_GO
        assertNull("golem must not match POKEMON_GO", PokemonRegion.fromQuery("golem"))
        assertNull("golduck must not match POKEMON_GO", PokemonRegion.fromQuery("golduck"))
        assertNull("golbat must not match POKEMON_GO", PokemonRegion.fromQuery("golbat"))
        assertNull("goodra must not match POKEMON_GO", PokemonRegion.fromQuery("goodra"))
        assertNull("goomy must not match POKEMON_GO", PokemonRegion.fromQuery("goomy"))
        assertNull("porygon must not match POKEMON_GO", PokemonRegion.fromQuery("porygon"))

        // Exact matches or standalone word 'go' SHOULD match
        assertEquals(PokemonRegion.POKEMON_GO, PokemonRegion.fromQuery("go"))
        assertEquals(PokemonRegion.POKEMON_GO, PokemonRegion.fromQuery("pokemon go"))
        assertEquals(PokemonRegion.POKEMON_GO, PokemonRegion.fromQuery("pogo"))
        assertEquals(PokemonRegion.KANTO, PokemonRegion.fromQuery("kanto"))
        assertEquals(PokemonRegion.HOENN, PokemonRegion.fromQuery("hoenn"))
        assertEquals(PokemonRegion.PALDEA, PokemonRegion.fromQuery("paldea"))
    }
}
