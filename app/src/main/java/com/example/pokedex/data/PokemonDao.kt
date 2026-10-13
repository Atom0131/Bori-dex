package com.example.pokedex.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon WHERE name = :name LIMIT 1")
    fun getPokemonByName(name: String): PokemonEntity?

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun getAllPokemon(): kotlinx.coroutines.flow.Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon WHERE id = :id LIMIT 1")
    fun getPokemonById(id: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon WHERE id < :currentId ORDER BY id DESC LIMIT 1")
    fun getPreviousPokemon(currentId: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon WHERE id > :currentId ORDER BY id ASC LIMIT 1")
    fun getNextPokemon(currentId: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon ORDER BY id DESC LIMIT 1")
    fun getLastPokemon(): PokemonEntity?

    @Query("SELECT * FROM pokemon ORDER BY id ASC LIMIT 1")
    fun getFirstPokemon(): PokemonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(pokemon: List<PokemonEntity>)
}
