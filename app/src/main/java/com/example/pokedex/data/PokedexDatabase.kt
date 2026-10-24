package com.example.pokedex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStreamReader

@Database(entities = [PokemonEntity::class], version = 10, exportSchema = false)
abstract class PokedexDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var INSTANCE: PokedexDatabase? = null
        private lateinit var appContext: Context

        fun getDatabase(context: Context): PokedexDatabase {
            appContext = context.applicationContext
            PokemonMapHabitatRepository.init(context.applicationContext)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PokedexDatabase::class.java,
                    "pokedex_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(PokedexDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun syncAssetDataIfNeeded(context: Context, dao: PokemonDao) {
            val prefs = context.getSharedPreferences("pokedex_prefs", Context.MODE_PRIVATE)
            val currentSyncVersion = prefs.getInt("pokemon_data_asset_version", 0)
            if (currentSyncVersion < 11) {
                syncAssetData(context, dao)
            }
        }

        fun syncAssetData(context: Context, dao: PokemonDao) {
            try {
                val inputStream = context.applicationContext.assets.open("pokemon_data.json")
                val reader = InputStreamReader(inputStream)
                val jsonString = reader.readText()
                reader.close()
                
                val jsonArray = JSONArray(jsonString)
                val pokemonList = mutableListOf<PokemonEntity>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val evolvesArray = obj.optJSONArray("evolvesTo")
                    val evolvesTo = if (evolvesArray != null) {
                        (0 until evolvesArray.length()).joinToString(",") { evolvesArray.getString(it) }
                    } else ""

                    val formsArray = obj.optJSONArray("forms")
                    val forms = if (formsArray != null) {
                        (0 until formsArray.length()).joinToString(",") { formsArray.getString(it) }
                    } else ""
                    
                    val pokedexEntriesObj = obj.optJSONObject("pokedexEntries")
                    val pokedexEntries = pokedexEntriesObj?.toString() ?: "{}"

                    pokemonList.add(
                        PokemonEntity(
                            id = obj.optInt("id", i),
                            name = obj.optString("name", "Unknown"),
                            type = obj.optString("type", "Unknown"),
                            height = obj.optString("height", "?? m"),
                            weight = obj.optString("weight", "?? kg"),
                            flavorText = obj.optString("flavorText", "No data available."),
                            spriteUrl = obj.optString("spriteUrl", ""),
                            shinySpriteUrl = if (obj.has("shinySpriteUrl") && !obj.isNull("shinySpriteUrl")) obj.optString("shinySpriteUrl", "") else null,
                            evolvesTo = evolvesTo,
                            forms = forms,
                            pokedexEntries = pokedexEntries
                        )
                    )
                }
                dao.insertAll(pokemonList)
                context.getSharedPreferences("pokedex_prefs", Context.MODE_PRIVATE).edit().putInt("pokemon_data_asset_version", 11).apply()
                android.util.Log.d("PokedexDatabase", "Successfully synced ${pokemonList.size} Pokemon entries to database.")
            } catch (e: Exception) {
                android.util.Log.e("PokedexDatabase", "Error syncing asset data to database", e)
            }
        }
    }

    private class PokedexDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    syncAssetData(appContext, database.pokemonDao())
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    syncAssetData(appContext, database.pokemonDao())
                }
            }
        }
    }
}
