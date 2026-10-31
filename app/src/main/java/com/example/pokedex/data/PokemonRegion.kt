package com.example.pokedex.data

enum class PokemonRegion(
    val displayName: String,
    val startId: Int,
    val endId: Int,
    val aliases: List<String>
) {
    KANTO("Kanto", 1, 151, listOf("kanto", "gen 1", "gen1")),
    JOHTO("Johto", 152, 251, listOf("johto", "gen 2", "gen2")),
    HOENN("Hoenn", 252, 386, listOf("hoenn", "hoen", "gen 3", "gen3")),
    SINNOH("Sinnoh", 387, 493, listOf("sinnoh", "gen 4", "gen4")),
    UNOVA("Unova", 494, 649, listOf("unova", "gen 5", "gen5")),
    KALOS("Kalos", 650, 721, listOf("kalos", "gen 6", "gen6")),
    ALOLA("Alola", 722, 809, listOf("alola", "gen 7", "gen7")),
    GALAR("Galar", 810, 898, listOf("galar", "gen 8", "gen8")),
    HISUI("Hisui", 899, 905, listOf("hisui", "hiusi", "hisuian", "legends arceus")),
    PALDEA("Paldea", 906, 1025, listOf("paldea", "gen 9", "gen9")),
    POKEMON_GO("Pokémon GO", 1, 1025, listOf("go", "pogo", "pokemon go", "pokemongo")),
    COSTUMES("Costumes", 1, 1025, listOf("costume", "costumes", "event", "hat", "glasses", "shades"));

    fun matches(pokemon: PokemonEntity): Boolean {
        if (this == POKEMON_GO) {
            return pokemon.pokedexEntries.contains("\"GO\"", ignoreCase = true) ||
                   PokemonGoCostumeRepository.getCostumesForPokemon(pokemon.id).isNotEmpty()
        }
        if (this == COSTUMES) {
            return PokemonGoCostumeRepository.getCostumesForPokemon(pokemon.id).isNotEmpty()
        }
        if (this == HISUI) {
            if ((pokemon.id in startId..endId) ||
                pokemon.name.contains("hisui", ignoreCase = true) ||
                pokemon.forms.contains("hisui", ignoreCase = true)
            ) {
                return true
            }
        }
        if (pokemon.id in startId..endId) return true

        // Also match returning species that natively inhabit this region's Pokédex
        if (PokemonMapHabitatRepository.getNativeRegionsForPokemon(pokemon.id).any {
                it.equals(this.displayName, ignoreCase = true)
            }
        ) {
            return true
        }

        return hasNativeRegionalEntry(pokemon.pokedexEntries)
    }

    private fun hasNativeRegionalEntry(pokedexEntriesJson: String): Boolean {
        if (pokedexEntriesJson.isBlank()) return false
        val editionKeys = when (this) {
            KANTO -> listOf("\"Red\"", "\"Blue\"", "\"Yellow\"", "\"FireRed\"", "\"LeafGreen\"", "\"Let's Go Pikachu\"")
            JOHTO -> listOf("\"Gold\"", "\"Silver\"", "\"Crystal\"", "\"HeartGold\"", "\"SoulSilver\"")
            HOENN -> listOf("\"Ruby\"", "\"Sapphire\"", "\"Emerald\"", "\"Omega Ruby\"", "\"Alpha Sapphire\"")
            SINNOH -> listOf("\"Diamond\"", "\"Pearl\"", "\"Platinum\"", "\"Brilliant Diamond\"", "\"Shining Pearl\"")
            UNOVA -> listOf("\"Black\"", "\"White\"", "\"Black 2\"", "\"White 2\"")
            KALOS -> listOf("\"X\"", "\"Y\"")
            ALOLA -> listOf("\"Sun\"", "\"Moon\"", "\"Ultra Sun\"", "\"Ultra Moon\"")
            GALAR -> listOf("\"Sword\"", "\"Shield\"")
            HISUI -> listOf("\"Legends: Arceus\"")
            PALDEA -> listOf("\"Scarlet\"", "\"Violet\"")
            else -> emptyList()
        }
        val notInhabitMarker = "does not naturally inhabit the ${this.displayName} region"
        for (key in editionKeys) {
            val idx = pokedexEntriesJson.indexOf(key, ignoreCase = true)
            if (idx >= 0) {
                val snippetEnd = minOf(pokedexEntriesJson.length, idx + 220)
                val snippet = pokedexEntriesJson.substring(idx, snippetEnd)
                if (!snippet.contains(notInhabitMarker, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        fun fromQuery(query: String): PokemonRegion? {
            val q = query.trim().lowercase()
            if (q.isBlank()) return null
            return entries.firstOrNull { r ->
                r.displayName.lowercase() == q ||
                r.aliases.any { alias ->
                    if (alias.length <= 2) {
                        q == alias || q.split(Regex("\\s+")).contains(alias)
                    } else {
                        q == alias || q.contains(alias)
                    }
                }
            }
        }
    }
}
