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
