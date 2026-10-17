package com.example.pokedex.data

object PokemonGoCostumeRepository {

    private val allCostumes: List<PokemonCostume> = listOf(
        // --- SQUIRTLE SQUAD ---
        PokemonCostume(
            id = "squirtle_squad_shades",
            pokemonId = 7,
            name = "Squirtle Squad (Leader Shades)",
            category = CostumeCategory.SQUIRTLE_SQUAD,
            eventTag = "SQUIRTLE SQUAD",
            description = "The legendary leader of the Squirtle Squad wearing sharp black anime sunglasses.",
            assetPath = "squirtle_squad_shades.png",
            shinyAssetPath = "squirtle_squad_shades_shiny.png"
        ),
        PokemonCostume(
            id = "wartortle_squad_shades",
            pokemonId = 8,
            name = "Wartortle (Squirtle Squad Shades)",
            category = CostumeCategory.SQUIRTLE_SQUAD,
            eventTag = "SQUIRTLE SQUAD",
            description = "Evolved Squirtle Squad member equipped with matching sunglasses.",
            assetPath = "wartortle_squad_shades.png",
            shinyAssetPath = "wartortle_squad_shades_shiny.png"
        ),
        PokemonCostume(
            id = "blastoise_squad_shades",
            pokemonId = 9,
            name = "Blastoise (Squirtle Squad Shades)",
            category = CostumeCategory.SQUIRTLE_SQUAD,
            eventTag = "SQUIRTLE SQUAD",
            description = "The ultimate evolved powerhouse of the Squirtle Squad wearing heavy sunglasses.",
            assetPath = "blastoise_squad_shades.png",
            shinyAssetPath = "blastoise_squad_shades_shiny.png"
        ),

        // --- PIKACHU (ALL ICONIC COSTUMES) ---
        PokemonCostume(
            id = "pikachu_holiday_santa",
            pokemonId = 25,
            name = "Santa Hat Pikachu",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY 2016",
            description = "The legendary first-ever event costume in Pokémon GO history (December 2016).",
            assetPath = "pikachu_holiday_santa.png",
            shinyAssetPath = "pikachu_holiday_santa_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_holiday_beanie",
            pokemonId = 25,
            name = "Winter Beanie Pikachu",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Cozy knitted winter beanie Pikachu with a festive pom-pom.",
            assetPath = "pikachu_holiday_beanie.png",
            shinyAssetPath = "pikachu_holiday_beanie_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_halloween_witch",
            pokemonId = 25,
            name = "Witch Hat Pikachu",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Spooky pointed witch hat adorned with a purple festival ribbon.",
            assetPath = "pikachu_halloween_witch.png",
            shinyAssetPath = "pikachu_halloween_witch_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_halloween_mimikyu",
            pokemonId = 25,
            name = "Mimikyu Costume Pikachu",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Dressed up in a handmade Mimikyu cloth costume for Halloween Mischief.",
            assetPath = "pikachu_halloween_mimikyu.png",
            shinyAssetPath = "pikachu_halloween_mimikyu_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_new_year_top_hat",
            pokemonId = 25,
            name = "New Year's Top Hat Pikachu",
            category = CostumeCategory.NEW_YEAR,
            eventTag = "NEW YEAR'S",
            description = "Ready to ring in the countdown in an elegant golden New Year top hat.",
            assetPath = "pikachu_new_year_top_hat.png",
            shinyAssetPath = "pikachu_new_year_top_hat_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_ash_cap",
            pokemonId = 25,
            name = "Ash's Original Cap Pikachu",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "SPECIAL EVENT",
            description = "Wearing Ash Ketchum's signature red-and-white Pokémon League Expo cap.",
            assetPath = "pikachu_ash_cap.png",
            shinyAssetPath = "pikachu_ash_cap_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_detective",
            pokemonId = 25,
            name = "Detective Pikachu",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "SPECIAL EVENT",
            description = "Equipped with a brown deerstalker cap for coffee-fueled mysteries.",
            assetPath = "pikachu_detective.png",
            shinyAssetPath = "pikachu_detective_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_straw_hat",
            pokemonId = 25,
            name = "Straw Hat Pikachu",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "ONE PIECE EVENT",
            description = "Wearing Monkey D. Luffy's red-ribboned straw hat from the Kumamoto charity event.",
            assetPath = "pikachu_straw_hat.png",
            shinyAssetPath = "pikachu_straw_hat_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_flower_crown",
            pokemonId = 25,
            name = "Flower Crown Pikachu",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "SPRING EVENT",
            description = "Adorned with a delicate handmade crown of blooming pastel blossoms.",
            assetPath = "pikachu_flower_crown.png",
            shinyAssetPath = "pikachu_flower_crown_shiny.png"
        ),
        PokemonCostume(
            id = "pikachu_party_hat",
            pokemonId = 25,
            name = "Red Party Hat Pikachu",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Celebratory red and white striped party hat for Pokémon Day.",
            assetPath = "pikachu_party_hat.png",
            shinyAssetPath = "pikachu_party_hat_shiny.png"
        ),

        // --- RAICHU ---
        PokemonCostume(
            id = "raichu_holiday_santa",
            pokemonId = 26,
            name = "Santa Hat Raichu",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Evolved festive Raichu keeping its Santa Hat on its head.",
            assetPath = "raichu_holiday_santa.png",
            shinyAssetPath = "raichu_holiday_santa_shiny.png"
        ),
        PokemonCostume(
            id = "raichu_holiday_beanie",
            pokemonId = 26,
            name = "Winter Beanie Raichu",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Warm winter beanie Raichu ready for sub-zero weather.",
            assetPath = "raichu_holiday_beanie.png",
            shinyAssetPath = "raichu_holiday_beanie_shiny.png"
        ),

        // --- EEVEE ---
        PokemonCostume(
            id = "eevee_holiday_hat",
            pokemonId = 133,
            name = "Holiday Hat Eevee",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Festive winter hat with holiday trim and holly leaves.",
            assetPath = "eevee_holiday_hat.png",
            shinyAssetPath = "eevee_holiday_hat_shiny.png"
        ),
        PokemonCostume(
            id = "eevee_flower_crown",
            pokemonId = 133,
            name = "Flower Crown Eevee",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "SPRING EVENT",
            description = "Adorned with delicate blooming spring pastel blossoms.",
            assetPath = "eevee_flower_crown.png",
            shinyAssetPath = "eevee_flower_crown_shiny.png"
        ),
        PokemonCostume(
            id = "eevee_party_hat",
            pokemonId = 133,
            name = "Party Hat Eevee",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Festive celebratory party hat for Pokémon Day.",
            assetPath = "eevee_party_hat.png",
            shinyAssetPath = "eevee_party_hat_shiny.png"
        ),

        // --- GLACEON ---
        PokemonCostume(
            id = "glaceon_holiday_cape",
            pokemonId = 471,
            name = "Holiday Cape Glaceon",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Adorned in an icy blue undersea holiday cape with frosted white fur trim.",
            assetPath = "glaceon_holiday_cape.png",
            shinyAssetPath = "glaceon_holiday_cape_shiny.png"
        ),

        // --- SPHEAL ---
        PokemonCostume(
            id = "spheal_holiday_scarf",
            pokemonId = 363,
            name = "Holiday Scarf Spheal",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Cozy knitted winter scarf wrapped around the spherical clapping sea lion.",
            assetPath = "spheal_holiday_scarf.png",
            shinyAssetPath = "spheal_holiday_scarf_shiny.png"
        ),

        // --- STANTLER ---
        PokemonCostume(
            id = "stantler_holiday_bells",
            pokemonId = 234,
            name = "Holiday Bells Stantler",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Decked out with festive jingling brass holiday bells on its reindeer harness.",
            assetPath = "stantler_holiday_bells.png",
            shinyAssetPath = "stantler_holiday_bells_shiny.png"
        ),

        // --- PSYDUCK ---
        PokemonCostume(
            id = "psyduck_holiday_attire",
            pokemonId = 54,
            name = "Holiday Attire Psyduck",
            category = CostumeCategory.HOLIDAY,
            eventTag = "HOLIDAY",
            description = "Cozy festive knit sweater and winter scarf helping soothe its perpetual headache.",
            assetPath = "psyduck_holiday_attire.png",
            shinyAssetPath = "psyduck_holiday_attire_shiny.png"
        ),

        // --- SLOWPOKE ---
        PokemonCostume(
            id = "slowpoke_2020_glasses",
            pokemonId = 79,
            name = "2020 Glasses Slowpoke",
            category = CostumeCategory.NEW_YEAR,
            eventTag = "NEW YEAR'S",
            description = "Sporting the iconic novelty 2020 New Year celebration glasses.",
            assetPath = "slowpoke_2020_glasses.png",
            shinyAssetPath = "slowpoke_2020_glasses_shiny.png"
        ),

        // --- HOOTHOOT ---
        PokemonCostume(
            id = "hoothoot_new_year_top_hat",
            pokemonId = 163,
            name = "New Year's Top Hat Hoothoot",
            category = CostumeCategory.NEW_YEAR,
            eventTag = "NEW YEAR'S",
            description = "Classic midnight countdown clock owl wearing a golden ribboned top hat.",
            assetPath = "hoothoot_new_year_top_hat.png",
            shinyAssetPath = "hoothoot_new_year_top_hat_shiny.png"
        ),

        // --- GENGAR ---
        PokemonCostume(
            id = "gengar_halloween_spooky",
            pokemonId = 94,
            name = "Halloween Spooky Gengar",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Spooky Festival costume complete with a grinning chef hat and lantern.",
            assetPath = "gengar_halloween_spooky.png",
            shinyAssetPath = "gengar_halloween_spooky_shiny.png"
        ),

        // --- PUMPKABOO & GOURGEIST ---
        PokemonCostume(
            id = "pumpkaboo_halloween_spooky",
            pokemonId = 710,
            name = "Spooky Festival Pumpkaboo",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Adorned with glowing autumn harvest festival ribbons.",
            assetPath = "pumpkaboo_halloween_spooky.png",
            shinyAssetPath = "pumpkaboo_halloween_spooky_shiny.png"
        ),
        PokemonCostume(
            id = "gourgeist_halloween_spooky",
            pokemonId = 711,
            name = "Spooky Festival Gourgeist",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Evolved festival pumpkin gourd dressed in autumn holiday finery.",
            assetPath = "gourgeist_halloween_spooky.png",
            shinyAssetPath = "gourgeist_halloween_spooky_shiny.png"
        ),

        // --- PIPLUP ---
        PokemonCostume(
            id = "piplup_halloween_pumpkin",
            pokemonId = 393,
            name = "Pumpkin Hat Piplup",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Waddling in an adorable orange jack-o-lantern pumpkin hat.",
            assetPath = "piplup_halloween_pumpkin.png",
            shinyAssetPath = "piplup_halloween_pumpkin_shiny.png"
        ),

        // --- DRIFBLIM ---
        PokemonCostume(
            id = "drifblim_halloween_mischief",
            pokemonId = 426,
            name = "Halloween Mischief Drifblim",
            category = CostumeCategory.HALLOWEEN,
            eventTag = "HALLOWEEN",
            description = "Eerie Halloween Mischief festival costume with festive purple and orange tassels.",
            assetPath = "drifblim_halloween_mischief.png",
            shinyAssetPath = "drifblim_halloween_mischief_shiny.png"
        ),

        // --- SNORLAX ---
        PokemonCostume(
            id = "snorlax_nightcap",
            pokemonId = 143,
            name = "Nightcap Snorlax (Pokémon Sleep)",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON SLEEP",
            description = "Signature blue-and-white striped nightcap from the Pokémon Sleep crossover.",
            assetPath = "snorlax_nightcap.png",
            shinyAssetPath = "snorlax_nightcap_shiny.png"
        ),
        PokemonCostume(
            id = "snorlax_cowboy",
            pokemonId = 143,
            name = "Cowboy Hat Snorlax",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "GO FEST",
            description = "Wild West brown felt cowboy hat from the Seattle and Taipei GO Fest.",
            assetPath = "snorlax_cowboy.png",
            shinyAssetPath = "snorlax_cowboy_shiny.png"
        ),

        // --- WURMPLE ---
        PokemonCostume(
            id = "wurmple_party_hat",
            pokemonId = 265,
            name = "Party Hat Wurmple",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "NEW YEAR'S",
            description = "Fan-favorite tiny red celebratory party hat perched on its forehead spike.",
            assetPath = "wurmple_party_hat.png",
            shinyAssetPath = "wurmple_party_hat_shiny.png"
        ),

        // --- BULBASAUR & CHARMANDER ---
        PokemonCostume(
            id = "bulbasaur_party_hat",
            pokemonId = 1,
            name = "Party Hat Bulbasaur",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Celebratory Pokémon Day party hat Bulbasaur.",
            assetPath = "bulbasaur_party_hat.png",
            shinyAssetPath = "bulbasaur_party_hat_shiny.png"
        ),
        PokemonCostume(
            id = "charmander_party_hat",
            pokemonId = 4,
            name = "Party Hat Charmander",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Celebratory Pokémon Day party hat Charmander.",
            assetPath = "charmander_party_hat.png",
            shinyAssetPath = "charmander_party_hat_shiny.png"
        ),

        // --- RATICATE & WOBBUFFET ---
        PokemonCostume(
            id = "raticate_party_hat",
            pokemonId = 20,
            name = "Party Hat Raticate",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Celebratory Pokémon Day party hat Raticate.",
            assetPath = "raticate_party_hat.png",
            shinyAssetPath = "raticate_party_hat_shiny.png"
        ),
        PokemonCostume(
            id = "wobbuffet_party_hat",
            pokemonId = 202,
            name = "Party Hat Wobbuffet",
            category = CostumeCategory.SPECIAL_EVENT,
            eventTag = "POKÉMON DAY",
            description = "Celebratory Pokémon Day party hat Wobbuffet.",
            assetPath = "wobbuffet_party_hat.png",
            shinyAssetPath = "wobbuffet_party_hat_shiny.png"
        )
    )

    fun getCostumesForPokemon(pokemonId: Int): List<PokemonCostume> {
        return allCostumes.filter { it.pokemonId == pokemonId }
    }

    fun getAllCostumes(): List<PokemonCostume> = allCostumes
}
