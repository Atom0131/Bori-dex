package com.example.pokedex.data

import androidx.compose.ui.graphics.Color

data class RegionMapEntry(
    val id: String,
    val region: PokemonRegion?,
    val title: String,
    val eraSubtitle: String,
    val generationText: String,
    val rangeText: String,
    val mapAsset: String,
    val preferredEditionId: String? = null,
    val cardColor: Color = Color(0xFF263238),
    val accentColor: Color = Color(0xFF00FFCC)
)

val ALL_REGION_MAP_ENTRIES = listOf(
    RegionMapEntry(
        id = "all",
        region = null,
        title = "ALL REGIONS",
        eraSubtitle = "National Pokédex",
        generationText = "All Generations",
        rangeText = "#001 - #1025",
        mapAsset = "all_world.png",
        preferredEditionId = "Default",
        cardColor = Color(0xFF1E272C),
        accentColor = Color(0xFF00FFCC)
    ),
    RegionMapEntry(
        id = "kanto_rgby",
        region = PokemonRegion.KANTO,
        title = "KANTO",
        eraSubtitle = "Red, Blue & Yellow",
        generationText = "Gen 1 (1996)",
        rangeText = "#001 - #151",
        mapAsset = "kanto_rgby.png",
        preferredEditionId = "Red",
        cardColor = Color(0xFF8B1E1E),
        accentColor = Color(0xFFFF5252)
    ),
    RegionMapEntry(
        id = "kanto_frlg",
        region = PokemonRegion.KANTO,
        title = "KANTO",
        eraSubtitle = "FireRed & LeafGreen",
        generationText = "Gen 1 Remake (2004)",
        rangeText = "#001 - #151",
        mapAsset = "kanto_frlg.png",
        preferredEditionId = "FireRed",
        cardColor = Color(0xFFC0392B),
        accentColor = Color(0xFFFF7043)
    ),
    RegionMapEntry(
        id = "johto_gsc",
        region = PokemonRegion.JOHTO,
        title = "JOHTO",
        eraSubtitle = "Gold, Silver & Crystal",
        generationText = "Gen 2 (1999)",
        rangeText = "#152 - #251",
        mapAsset = "johto_gsc.png",
        preferredEditionId = "Gold",
        cardColor = Color(0xFF7F6000),
        accentColor = Color(0xFFFFD54F)
    ),
    RegionMapEntry(
        id = "johto_hgss",
        region = PokemonRegion.JOHTO,
        title = "JOHTO",
        eraSubtitle = "HeartGold & SoulSilver",
        generationText = "Gen 2 Remake (2009)",
        rangeText = "#152 - #251",
        mapAsset = "johto_hgss.png",
        preferredEditionId = "HeartGold",
        cardColor = Color(0xFF996515),
        accentColor = Color(0xFFFFCA28)
    ),
    RegionMapEntry(
        id = "hoenn_rse",
        region = PokemonRegion.HOENN,
        title = "HOENN",
        eraSubtitle = "Ruby, Sapphire & Emerald",
        generationText = "Gen 3 (2002)",
        rangeText = "#252 - #386",
        mapAsset = "hoenn_rse.png",
        preferredEditionId = "Ruby",
        cardColor = Color(0xFF880E4F),
        accentColor = Color(0xFFFF4081)
    ),
    RegionMapEntry(
        id = "hoenn_oras",
        region = PokemonRegion.HOENN,
        title = "HOENN",
        eraSubtitle = "Omega Ruby & Alpha Sapphire",
        generationText = "Gen 3 Remake (2014)",
        rangeText = "#252 - #386",
        mapAsset = "hoenn_oras.png",
        preferredEditionId = "Omega Ruby",
        cardColor = Color(0xFF0D47A1),
        accentColor = Color(0xFF40C4FF)
    ),
    RegionMapEntry(
        id = "sinnoh_pt",
        region = PokemonRegion.SINNOH,
        title = "SINNOH",
        eraSubtitle = "Diamond, Pearl & Platinum",
        generationText = "Gen 4 (2006)",
        rangeText = "#387 - #493",
        mapAsset = "sinnoh_pt.png",
        preferredEditionId = "Platinum",
        cardColor = Color(0xFF2E4053),
        accentColor = Color(0xFF85C1E9)
    ),
    RegionMapEntry(
        id = "unova_bw",
        region = PokemonRegion.UNOVA,
        title = "UNOVA",
        eraSubtitle = "Black & White",
        generationText = "Gen 5 (2010)",
        rangeText = "#494 - #649",
        mapAsset = "unova_bw.png",
        preferredEditionId = "Black",
        cardColor = Color(0xFF263238),
        accentColor = Color(0xFF80DEEA)
    ),
    RegionMapEntry(
        id = "unova_b2w2",
        region = PokemonRegion.UNOVA,
        title = "UNOVA",
        eraSubtitle = "Black 2 & White 2",
        generationText = "Gen 5 Sequel (2012)",
        rangeText = "#494 - #649",
        mapAsset = "unova_b2w2.png",
        preferredEditionId = "Black 2",
        cardColor = Color(0xFF1E293B),
        accentColor = Color(0xFF4DD0E1)
    ),
    RegionMapEntry(
        id = "kalos_xy",
        region = PokemonRegion.KALOS,
        title = "KALOS",
        eraSubtitle = "X & Y",
        generationText = "Gen 6 (2013)",
        rangeText = "#650 - #721",
        mapAsset = "kalos_xy.png",
        preferredEditionId = "X",
        cardColor = Color(0xFF1B4F72),
        accentColor = Color(0xFF5DADE2)
    ),
    RegionMapEntry(
        id = "alola_sm",
        region = PokemonRegion.ALOLA,
        title = "ALOLA",
        eraSubtitle = "Sun & Moon",
        generationText = "Gen 7 (2016)",
        rangeText = "#722 - #809",
        mapAsset = "alola_sm.png",
        preferredEditionId = "Sun",
        cardColor = Color(0xFFD35400),
        accentColor = Color(0xFFFF9800)
    ),
    RegionMapEntry(
        id = "alola_usum",
        region = PokemonRegion.ALOLA,
        title = "ALOLA",
        eraSubtitle = "Ultra Sun & Ultra Moon",
        generationText = "Gen 7 Enhanced (2017)",
        rangeText = "#722 - #809",
        mapAsset = "alola_usum.png",
        preferredEditionId = "Ultra Sun",
        cardColor = Color(0xFF4A235A),
        accentColor = Color(0xFFBA68C8)
    ),
    RegionMapEntry(
        id = "galar_swsh",
        region = PokemonRegion.GALAR,
        title = "GALAR",
        eraSubtitle = "Sword & Shield",
        generationText = "Gen 8 (2019)",
        rangeText = "#810 - #898",
        mapAsset = "galar_swsh.png",
        preferredEditionId = "Sword",
        cardColor = Color(0xFF0E6251),
        accentColor = Color(0xFF1ABC9C)
    ),
    RegionMapEntry(
        id = "hisui_pla",
        region = PokemonRegion.HISUI,
        title = "HISUI",
        eraSubtitle = "Legends: Arceus",
        generationText = "Ancient Sinnoh",
        rangeText = "#899 - #905",
        mapAsset = "hisui_pla.png",
        preferredEditionId = "Legends: Arceus",
        cardColor = Color(0xFF196F3D),
        accentColor = Color(0xFF52BE80)
    ),
    RegionMapEntry(
        id = "paldea_sv",
        region = PokemonRegion.PALDEA,
        title = "PALDEA",
        eraSubtitle = "Scarlet & Violet",
        generationText = "Gen 9 (2022)",
        rangeText = "#906 - #1025",
        mapAsset = "paldea_sv.png",
        preferredEditionId = "Scarlet",
        cardColor = Color(0xFF5B2C6F),
        accentColor = Color(0xFFAF7AC5)
    ),
    RegionMapEntry(
        id = "pogo",
        region = PokemonRegion.POKEMON_GO,
        title = "POKÉMON GO",
        eraSubtitle = "Mobile World Radar",
        generationText = "Worldwide AR",
        rangeText = "Available Dex",
        mapAsset = "pokemongo_map.png",
        preferredEditionId = "GO",
        cardColor = Color(0xFF0075BE),
        accentColor = Color(0xFF4FC3F7)
    ),
    RegionMapEntry(
        id = "costumes",
        region = PokemonRegion.COSTUMES,
        title = "COSTUMES",
        eraSubtitle = "Event Costumes & Hats",
        generationText = "Special Events",
        rangeText = "72 Forms",
        mapAsset = "costumes_map.png",
        preferredEditionId = "GO",
        cardColor = Color(0xFFB7950B),
        accentColor = Color(0xFFFFD700)
    )
)
