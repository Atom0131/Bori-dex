package com.example.pokedex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val height: String,
    val weight: String,
    val flavorText: String,
    val spriteUrl: String,
    val shinySpriteUrl: String?,
    val evolvesTo: String,
    val forms: String,
    val pokedexEntries: String
)

fun PokemonEntity.getCanonicalId(allList: List<PokemonEntity>? = null): Int {
    if (this.id <= 1025) return this.id
    val cName = this.name.lowercase()
    if (allList != null) {
        val match = allList.firstOrNull { base ->
            base.id <= 1025 && (
                base.forms.split(",").map { it.trim().lowercase() }.contains(cName) ||
                (base.name.lowercase().split("-")[0] == cName.split("-")[0])
            )
        }
        if (match != null) return match.id
    }
    return this.id
}
