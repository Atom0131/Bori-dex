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
    PALDEA("Paldea", 906, 1025, listOf("paldea", "gen 9", "gen9"));

    fun matches(pokemon: PokemonEntity): Boolean {
        if (this == HISUI) {
            return (pokemon.id in startId..endId) ||
                    pokemon.name.contains("hisui", ignoreCase = true) ||
                    pokemon.forms.contains("hisui", ignoreCase = true)
        }
        return pokemon.id in startId..endId
    }

    companion object {
        fun fromQuery(query: String): PokemonRegion? {
            val q = query.trim().lowercase()
            return entries.firstOrNull { r ->
                r.aliases.any { alias -> q.contains(alias) } || r.displayName.lowercase() == q
            }
        }
    }
}
