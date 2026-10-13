import os
import urllib.request
import json

base_dir = r'C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets\sprites'
os.makedirs(base_dir, exist_ok=True)

pokemon_list = []

for i in range(1, 152):
    print(f'Fetching {i}...')
    # Get basic data
    req = urllib.request.Request(f'https://pokeapi.co/api/v2/pokemon/{i}', headers={'User-Agent': 'Mozilla/5.0'})
    r = json.loads(urllib.request.urlopen(req).read())
    name = r['name']
    types = '/'.join([t['type']['name'].capitalize() for t in r['types']])
    height = f"{r['height'] / 10.0} m"
    weight = f"{r['weight'] / 10.0} kg"
    
    # Download sprite
    sprite_url = r['sprites']['front_default']
    sprite_path = f'sprites/{i}.png'
    local_sprite_path = os.path.join(base_dir, f'{i}.png')
    
    sprite_req = urllib.request.Request(sprite_url, headers={'User-Agent': 'Mozilla/5.0'})
    img_data = urllib.request.urlopen(sprite_req).read()
    with open(local_sprite_path, 'wb') as f:
        f.write(img_data)
        
    # Get flavor text
    species_req = urllib.request.Request(r['species']['url'], headers={'User-Agent': 'Mozilla/5.0'})
    species_r = json.loads(urllib.request.urlopen(species_req).read())
    flavor_text = 'No description.'
    for entry in species_r['flavor_text_entries']:
        if entry['language']['name'] == 'en':
            flavor_text = entry['flavor_text'].replace('\n', ' ').replace('\f', ' ').replace('"', '\\"')
            break
            
    pokemon_list.append(f'PokemonEntity({i}, "{name}", "{types}", "{height}", "{weight}", "{flavor_text}", "{sprite_path}")')

# Write PokedexDatabase.kt
db_content = f'''package com.example.pokedex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PokemonEntity::class], version = 2, exportSchema = false)
abstract class PokedexDatabase : RoomDatabase() {{

    abstract fun pokemonDao(): PokemonDao

    companion object {{
        @Volatile
        private var INSTANCE: PokedexDatabase? = null

        fun getDatabase(context: Context): PokedexDatabase {{
            return INSTANCE ?: synchronized(this) {{
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
            }}
        }}
    }}

    private class PokedexDatabaseCallback : RoomDatabase.Callback() {{
        override fun onCreate(db: SupportSQLiteDatabase) {{
            super.onCreate(db)
            INSTANCE?.let {{ database ->
                CoroutineScope(Dispatchers.IO).launch {{
                    populateDatabase(database.pokemonDao())
                }}
            }}
        }}

        suspend fun populateDatabase(dao: PokemonDao) {{
            val gen1Pokemon = listOf(
                {',\\n                '.join(pokemon_list)}
            )
            dao.insertAll(gen1Pokemon)
        }}
    }}
}}
'''

with open(r'C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\java\com\example\pokedex\data\PokedexDatabase.kt', 'w', encoding='utf-8') as f:
    f.write(db_content)

print('Done!')
