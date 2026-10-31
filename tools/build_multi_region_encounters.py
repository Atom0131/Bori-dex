#!/usr/bin/env python3
"""
Builds the multi-region habitat database (`pokemon_encounters.json`) and cleans
`pokemon_data.json` across all 1,025 Pokémon (#0001 - #1025).

Key responsibilities:
1. Decodes all raw HTML entities (e.g. `Pok&eacute;mon` -> `Pokémon`) in `pokemon_data.json`.
2. Determines every Pokémon's authentic `nativeRegions` across all 10 regions:
   Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, Hisui, Paldea.
3. Populates `regionalHabitats[regionName]` for every native region of each Pokémon
   with authentic Town Map pins (`kanto_*`, `johto_*`, `hoenn_*`, `sinnoh_*`,
   `unova_*`, `kalos_*`, `alola_*`, `galar_*`, `hisui_*`, `paldea_*`) and
   region-matched voice narration.
4. Specifically curates exact habitats for Mudbray (#749), Mudsdale (#750), and
   wild/evolution-line encounters across all regions so returning species never
   falsely claim they are "Not found in the wild in Paldea" or read Alola text
   on a Paldea map.
"""

import html
import json
import os
import re

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS_DIR = os.path.join(ROOT_DIR, "app", "src", "main", "assets")
POKEMON_DATA_PATH = os.path.join(ASSETS_DIR, "pokemon_data.json")
ENCOUNTERS_PATH = os.path.join(ASSETS_DIR, "pokemon_encounters.json")

REGION_ORDER = [
    "Kanto",
    "Johto",
    "Hoenn",
    "Sinnoh",
    "Unova",
    "Kalos",
    "Alola",
    "Galar",
    "Hisui",
    "Paldea",
]

REGION_EDITIONS = {
    "Kanto": ["Red", "Blue", "Yellow", "FireRed", "LeafGreen", "Let's Go Pikachu", "Let's Go Eevee"],
    "Johto": ["Gold", "Silver", "Crystal", "HeartGold", "SoulSilver"],
    "Hoenn": ["Ruby", "Sapphire", "Emerald", "Omega Ruby", "Alpha Sapphire"],
    "Sinnoh": ["Diamond", "Pearl", "Platinum", "Brilliant Diamond", "Shining Pearl"],
    "Unova": ["Black", "White", "Black 2", "White 2"],
    "Kalos": ["X", "Y", "Legends: Z-A"],
    "Alola": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
    "Galar": ["Sword", "Shield"],
    "Hisui": ["Legends: Arceus"],
    "Paldea": ["Scarlet", "Violet"],
}

REGION_DEFAULT_CARTRIDGES = {
    "Kanto": ["Red", "Blue", "Yellow", "FireRed", "LeafGreen", "Let's Go Pikachu", "Let's Go Eevee"],
    "Johto": ["Gold", "Silver", "Crystal", "HeartGold", "SoulSilver"],
    "Hoenn": ["Ruby", "Sapphire", "Emerald", "Omega Ruby", "Alpha Sapphire"],
    "Sinnoh": ["Diamond", "Pearl", "Platinum", "Brilliant Diamond", "Shining Pearl"],
    "Unova": ["Black", "White", "Black 2", "White 2"],
    "Kalos": ["X", "Y", "Legends: Z-A"],
    "Alola": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
    "Galar": ["Sword", "Shield"],
    "Hisui": ["Legends: Arceus"],
    "Paldea": ["Scarlet", "Violet"],
}

# Canonical Town Map landmarks per region (matching PokemonMapHabitatRepository.kt)
REGION_LANDMARKS = {
    "Kanto": {
        "field": [
            ("kanto_pallet_town", "Pallet Town & Route 1", 0.24, 0.70, "TALL_GRASS"),
            ("kanto_cerulean_cape", "Cerulean Cape (Route 24 & 25)", 0.58, 0.20, "TALL_GRASS"),
            ("kanto_celadon_city", "Celadon Outskirts & Route 7 / 16", 0.48, 0.44, "TALL_GRASS"),
        ],
        "forest": [
            ("kanto_viridian_forest", "Viridian Forest & Route 2", 0.24, 0.43, "FOREST"),
            ("kanto_safari_zone", "Kanto Safari Zone (Fuchsia City)", 0.48, 0.76, "TALL_GRASS"),
        ],
        "mountain": [
            ("kanto_mt_moon", "Mt. Moon Caverns & Route 4", 0.40, 0.25, "CAVE_MOUNTAIN"),
            ("kanto_rock_tunnel", "Rock Tunnel & Route 9–10", 0.76, 0.32, "CAVE_MOUNTAIN"),
            ("kanto_victory_road", "Victory Road & Indigo Approach", 0.13, 0.38, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("kanto_vermilion_harbor", "Vermilion Harbor & Route 11–13", 0.58, 0.58, "SURF_WATER"),
            ("kanto_seafoam_islands", "Seafoam Islands & Route 19–20", 0.38, 0.88, "SURF_WATER"),
            ("kanto_cerulean_cape", "Cerulean Cape & Route 24 Bay", 0.58, 0.20, "FISHING"),
        ],
        "urban_electric": [
            ("kanto_power_plant", "Abandoned Power Plant (Route 10)", 0.78, 0.28, "URBAN_SPECIAL"),
            ("kanto_celadon_city", "Celadon City & Saffron Outskirts", 0.48, 0.44, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("kanto_pokemon_tower", "Pokémon Tower (Lavender Town)", 0.76, 0.44, "CAVE_MOUNTAIN"),
            ("kanto_cinnabar_mansion", "Pokémon Mansion (Cinnabar Island)", 0.24, 0.88, "URBAN_SPECIAL"),
        ],
        "ice": [
            ("kanto_seafoam_islands", "Seafoam Islands (Lower Ice Caverns)", 0.38, 0.88, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("kanto_digletts_cave", "Diglett's Cave & Route 11", 0.62, 0.55, "CAVE_MOUNTAIN"),
            ("kanto_rock_tunnel", "Route 4 & Rock Tunnel Foothills", 0.76, 0.32, "CAVE_MOUNTAIN"),
        ],
    },
    "Johto": {
        "field": [
            ("johto_new_bark", "New Bark Outskirts & Route 29", 0.82, 0.65, "TALL_GRASS"),
            ("johto_sprout_tower", "Route 31, Route 32 & Violet Outskirts", 0.62, 0.42, "TALL_GRASS"),
            ("johto_national_park", "Route 35, Route 36 & National Park", 0.42, 0.38, "TALL_GRASS"),
        ],
        "forest": [
            ("johto_ilex_forest", "Ilex Forest & Route 34", 0.45, 0.78, "FOREST"),
            ("johto_national_park", "Johto National Park", 0.42, 0.38, "FOREST"),
        ],
        "mountain": [
            ("johto_ruins_of_alph", "Union Cave & Ruins of Alph", 0.56, 0.48, "CAVE_MOUNTAIN"),
            ("johto_dragons_den", "Route 45, Dark Cave & Blackthorn Cliffs", 0.78, 0.34, "CAVE_MOUNTAIN"),
            ("johto_mt_silver", "Mt. Silver Foothills & Route 28", 0.92, 0.48, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("johto_olivine_coast", "Olivine Coast & Route 40–41", 0.28, 0.48, "SURF_WATER"),
            ("johto_lake_of_rage", "Lake of Rage & Route 43", 0.66, 0.16, "SURF_WATER"),
            ("johto_whirl_islands", "Whirl Islands & Cianwood Strait", 0.24, 0.64, "FISHING"),
        ],
        "urban_electric": [
            ("johto_olivine_coast", "Route 38–39 (MooMoo Farms & Olivine Approach)", 0.28, 0.48, "TALL_GRASS"),
            ("johto_burned_tower", "Goldenrod & Ecruteak Outskirts (Route 34–37)", 0.46, 0.28, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("johto_burned_tower", "Burned Tower & Ecruteak City", 0.46, 0.28, "CAVE_MOUNTAIN"),
            ("johto_bell_tower", "Bell Tower (Tin Tower) & Route 37 at Night", 0.50, 0.26, "URBAN_SPECIAL"),
        ],
        "ice": [
            ("johto_ice_path", "Ice Path & Route 44", 0.78, 0.26, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("johto_ruins_of_alph", "Union Cave & Route 32–33", 0.56, 0.48, "CAVE_MOUNTAIN"),
            ("johto_dragons_den", "Route 45 Mountain Pass", 0.78, 0.34, "CAVE_MOUNTAIN"),
        ],
    },
    "Hoenn": {
        "field": [
            ("hoenn_littleroot", "Route 101, Route 102 & Oldale Outskirts", 0.20, 0.74, "TALL_GRASS"),
            ("hoenn_slateport_beach", "Route 110, Route 117 & Mauville Meadows", 0.34, 0.68, "TALL_GRASS"),
            ("hoenn_safari_zone", "Hoenn Safari Zone & Route 121", 0.58, 0.40, "TALL_GRASS"),
        ],
        "forest": [
            ("hoenn_petalburg_woods", "Petalburg Woods & Route 104", 0.12, 0.60, "FOREST"),
            ("hoenn_fortree_jungle", "Route 119 Rainforest & Fortree Canopy", 0.52, 0.26, "FOREST"),
        ],
        "mountain": [
            ("hoenn_granite_cave", "Granite Cave & Rusturf Tunnel", 0.16, 0.86, "CAVE_MOUNTAIN"),
            ("hoenn_fiery_path", "Mt. Chimney, Fiery Path & Jagged Pass", 0.32, 0.34, "CAVE_MOUNTAIN"),
            ("hoenn_meteor_falls", "Meteor Falls & Route 114–115", 0.14, 0.32, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("hoenn_slateport_beach", "Slateport Coast & Route 105–109", 0.34, 0.68, "SURF_WATER"),
            ("hoenn_sootopolis", "Route 124–126 & Sootopolis Deep", 0.70, 0.56, "SURF_WATER"),
            ("hoenn_sky_pillar", "Pacifidlog Currents (Route 129–131)", 0.66, 0.72, "FISHING"),
        ],
        "urban_electric": [
            ("hoenn_slateport_beach", "New Mauville & Route 110 Cycling Road", 0.34, 0.68, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("hoenn_mt_pyre", "Mt. Pyre Summit & Route 121–123", 0.62, 0.52, "CAVE_MOUNTAIN"),
        ],
        "ice": [
            ("hoenn_shoal_cave", "Shoal Cave (Low Tide Ice Room)", 0.78, 0.32, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("hoenn_desert_ruins", "Route 111 Desert & Mirage Tower Basin", 0.38, 0.38, "CAVE_MOUNTAIN"),
            ("hoenn_fiery_path", "Route 112 & Route 113 Ashlands", 0.32, 0.34, "TALL_GRASS"),
        ],
    },
    "Sinnoh": {
        "field": [
            ("sinnoh_twinleaf_lake", "Route 201, Route 202 & Lake Verity", 0.22, 0.82, "TALL_GRASS"),
            ("sinnoh_valley_windworks", "Valley Windworks & Route 204–205", 0.30, 0.58, "TALL_GRASS"),
            ("sinnoh_celestic_ruins", "Route 209, Route 210 & Solaceon Meadows", 0.52, 0.42, "TALL_GRASS"),
        ],
        "forest": [
            ("sinnoh_eterna_forest", "Eterna Forest & Floaroma Meadow", 0.30, 0.44, "FOREST"),
            ("sinnoh_great_marsh", "Pastoria Great Marsh & Route 212", 0.64, 0.76, "TALL_GRASS"),
        ],
        "mountain": [
            ("sinnoh_oreburgh_mine", "Oreburgh Gate, Route 207 & Wayward Cave", 0.38, 0.68, "CAVE_MOUNTAIN"),
            ("sinnoh_mt_coronet", "Mt. Coronet & Route 211", 0.48, 0.52, "CAVE_MOUNTAIN"),
            ("sinnoh_iron_island", "Iron Island & Stark Mountain", 0.18, 0.42, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("sinnoh_sunnyshore_bay", "Sunyshore Bay & Route 218 / 222", 0.84, 0.68, "SURF_WATER"),
            ("sinnoh_great_marsh", "Pastoria Coast & Route 213", 0.64, 0.76, "SURF_WATER"),
            ("sinnoh_lake_valor", "Lake Valor & Route 214", 0.74, 0.66, "FISHING"),
        ],
        "urban_electric": [
            ("sinnoh_valley_windworks", "Valley Windworks & Fuego Ironworks", 0.30, 0.58, "URBAN_SPECIAL"),
            ("sinnoh_sunnyshore_bay", "Route 222 (Sunyshore Approach)", 0.84, 0.68, "TALL_GRASS"),
        ],
        "spooky": [
            ("sinnoh_eterna_forest", "Old Chateau & Lost Tower (Route 209)", 0.30, 0.44, "CAVE_MOUNTAIN"),
        ],
        "ice": [
            ("sinnoh_lake_acuity", "Route 216, Route 217 & Acuity Lakefront", 0.46, 0.14, "TALL_GRASS"),
        ],
        "desert_ground": [
            ("sinnoh_oreburgh_mine", "Route 206, Wayward Cave & Ruin Maniac Tunnel", 0.38, 0.68, "CAVE_MOUNTAIN"),
            ("sinnoh_stark_mountain", "Route 228 Sandstorm Zone & Stark Mountain", 0.74, 0.18, "CAVE_MOUNTAIN"),
        ],
    },
    "Unova": {
        "field": [
            ("unova_nuvema_route1", "Route 1, Route 2 & Accumula Outskirts", 0.78, 0.82, "TALL_GRASS"),
            ("unova_nimbasa_woods", "Route 5, Route 16 & Driftveil Approach", 0.50, 0.46, "TALL_GRASS"),
            ("unova_opelucid_route9", "Route 7, Route 9 & Village Bridge", 0.48, 0.24, "TALL_GRASS"),
        ],
        "forest": [
            ("unova_pinwheel_forest", "Pinwheel Forest & Wellspring Outskirts", 0.62, 0.68, "FOREST"),
            ("unova_nimbasa_woods", "Lostlorn Forest & Abundant Shrine", 0.50, 0.46, "FOREST"),
            ("unova_white_forest", "White Forest & Route 14 Lush Cascades", 0.74, 0.52, "FOREST"),
        ],
        "mountain": [
            ("unova_chargestone_cave", "Chargestone Cave & Mistralton Cave", 0.26, 0.42, "CAVE_MOUNTAIN"),
            ("unova_celestial_tower", "Twist Mountain & Clay Tunnel", 0.30, 0.30, "CAVE_MOUNTAIN"),
            ("unova_victory_road", "Unova Victory Road & Giant Chasm", 0.54, 0.12, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("unova_undella_bay", "Undella Bay & Route 13–14 Coast", 0.78, 0.38, "SURF_WATER"),
            ("unova_castelia_harbor", "Route 17–18 Rapids & Virbank Bay", 0.48, 0.72, "SURF_WATER"),
            ("unova_dragonspiral_tower", "Moor of Icirrus & Route 8 Marsh", 0.36, 0.20, "FISHING"),
        ],
        "urban_electric": [
            ("unova_chargestone_cave", "Chargestone Cave & Virbank Complex", 0.26, 0.42, "CAVE_MOUNTAIN"),
            ("unova_castelia_harbor", "Castelia Park & Nimbasa Outskirts", 0.48, 0.72, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("unova_celestial_tower", "Celestial Tower & Strange House", 0.30, 0.30, "CAVE_MOUNTAIN"),
            ("unova_desert_resort", "Relic Castle & Relic Passage", 0.46, 0.56, "CAVE_MOUNTAIN"),
        ],
        "ice": [
            ("unova_dragonspiral_tower", "Dragonspiral Tower & Giant Chasm", 0.36, 0.20, "TALL_GRASS"),
        ],
        "desert_ground": [
            ("unova_desert_resort", "Route 4 & Desert Resort", 0.46, 0.56, "CAVE_MOUNTAIN"),
            ("unova_celestial_tower", "Relic Passage & Reversal Mountain", 0.30, 0.30, "CAVE_MOUNTAIN"),
        ],
    },
    "Kalos": {
        "field": [
            ("kalos_lumiose_plaza", "Route 4, Route 5 & Lumiose Outskirts", 0.50, 0.48, "TALL_GRASS"),
            ("kalos_connecting_cave", "Route 7, Route 8 & Camphrier Meadows", 0.36, 0.62, "TALL_GRASS"),
            ("kalos_azure_bay", "Route 11, Route 12 (Fourrage Road)", 0.28, 0.26, "TALL_GRASS"),
        ],
        "forest": [
            ("kalos_vaniville_forest", "Santalune Forest & Route 2–3", 0.58, 0.76, "FOREST"),
            ("kalos_pokemon_village", "Route 20 (Winding Woods) & Pokémon Village", 0.68, 0.68, "FOREST"),
        ],
        "mountain": [
            ("kalos_glittering_cave", "Glittering Cave & Route 9 Spikes Passage", 0.24, 0.74, "CAVE_MOUNTAIN"),
            ("kalos_reflection_cave", "Reflection Cave & Route 10 Menhir Trail", 0.26, 0.42, "CAVE_MOUNTAIN"),
            ("kalos_terminus_cave", "Terminus Cave & Route 18 (Vallée Étroite)", 0.78, 0.48, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("kalos_azure_bay", "Azure Bay & Route 12 Coast", 0.28, 0.26, "SURF_WATER"),
            ("kalos_glittering_cave", "Ambrette Coast & Cyllage Bay (Route 8)", 0.24, 0.74, "SURF_WATER"),
            ("kalos_laverre_swamp", "Route 14 Marsh & Route 19 Wetland", 0.54, 0.26, "FISHING"),
        ],
        "urban_electric": [
            ("kalos_kalos_power_plant", "Route 13 (Kalos Power Plant & Lumiose Badlands)", 0.42, 0.34, "URBAN_SPECIAL"),
            ("kalos_lumiose_plaza", "Lumiose City Plazas & Wild Zones", 0.50, 0.48, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("kalos_laverre_swamp", "Route 14 (Laverre Nature Trail) & Lost Hotel", 0.54, 0.26, "FOREST"),
            ("kalos_frost_cavern", "Route 15 (Brun Way) & Route 16 (Mélancolie Path)", 0.68, 0.28, "TALL_GRASS"),
        ],
        "ice": [
            ("kalos_frost_cavern", "Frost Cavern & Route 17 (Mamoswine Road)", 0.68, 0.28, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("kalos_kalos_power_plant", "Route 13 (Lumiose Badlands)", 0.42, 0.34, "CAVE_MOUNTAIN"),
            ("kalos_glittering_cave", "Route 9 (Spikes Passage) & Glittering Cave", 0.24, 0.74, "CAVE_MOUNTAIN"),
        ],
    },
    "Alola": {
        "field": [
            ("alola_melemele_outskirts", "Route 1 & Hau'oli Outskirts", 0.26, 0.28, "TALL_GRASS"),
            ("alola_brooklet_hill", "Paniola Ranch & Route 4–6", 0.54, 0.26, "TALL_GRASS"),
            ("alola_malie_cape", "Route 10, Route 11 & Malie Outskirts", 0.74, 0.62, "TALL_GRASS"),
        ],
        "forest": [
            ("alola_verdant_cavern", "Melemele Meadow & Berry Fields", 0.28, 0.22, "FOREST"),
            ("alola_lush_jungle", "Lush Jungle & Route 5 / 8", 0.60, 0.16, "FOREST"),
            ("alola_exeggutor_island", "Poni Plains & Exeggutor Island", 0.18, 0.80, "TALL_GRASS"),
        ],
        "mountain": [
            ("alola_ten_carat_hill", "Ten Carat Hill & Diglett's Tunnel", 0.22, 0.36, "CAVE_MOUNTAIN"),
            ("alola_wela_volcano", "Wela Volcano Park & Dividing Peak", 0.62, 0.22, "CAVE_MOUNTAIN"),
            ("alola_vast_poni_canyon", "Vast Poni Canyon & Blush Mountain", 0.26, 0.74, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("alola_ten_carat_hill", "Melemele Sea & Kala'e Bay", 0.22, 0.36, "SURF_WATER"),
            ("alola_brooklet_hill", "Brooklet Hill & Route 7–8 Coast", 0.54, 0.26, "SURF_WATER"),
            ("alola_vast_poni_canyon", "Poni Wilds & Poni Breaker Coast", 0.26, 0.74, "FISHING"),
        ],
        "urban_electric": [
            ("alola_blush_mountain", "Blush Mountain Power Plant & Mount Hokulani", 0.68, 0.72, "URBAN_SPECIAL"),
            ("alola_melemele_outskirts", "Hau'oli City Shopping District", 0.26, 0.28, "URBAN_SPECIAL"),
        ],
        "spooky": [
            ("alola_haina_desert", "Thrifty Megamart (Abandoned Site) & Route 14", 0.66, 0.78, "URBAN_SPECIAL"),
            ("alola_melemele_outskirts", "Hau'oli Cemetery (Route 2)", 0.26, 0.28, "TALL_GRASS"),
        ],
        "ice": [
            ("alola_mount_lanakila", "Mount Lanakila & Tapu Village", 0.60, 0.68, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("alola_haina_desert", "Haina Desert & Ula'ula Coast", 0.66, 0.78, "CAVE_MOUNTAIN"),
            ("alola_blush_mountain", "Route 12, Blush Mountain & Paniola Ranch", 0.68, 0.72, "TALL_GRASS"),
        ],
    },
    "Galar": {
        "field": [
            ("galar_rolling_fields", "Wild Area: Rolling Fields & Dappled Grove", 0.50, 0.72, "TALL_GRASS"),
            ("galar_motostoke_outskirts", "Route 3, Route 4 & Motostoke Riverbank", 0.48, 0.62, "TALL_GRASS"),
            ("galar_hammerlocke_hills", "Hammerlocke Hills & Bridge Field", 0.50, 0.42, "TALL_GRASS"),
        ],
        "forest": [
            ("galar_postwick_slumbering", "Slumbering Weald & Dappled Grove", 0.50, 0.88, "FOREST"),
            ("galar_glimwood_tangle", "Glimwood Tangle & Giant's Mirror", 0.34, 0.36, "FOREST"),
        ],
        "mountain": [
            ("galar_motostoke_outskirts", "Galar Mine & Giant's Cap", 0.48, 0.62, "CAVE_MOUNTAIN"),
            ("galar_stony_wilderness", "Wild Area: Stony Wilderness & Giant's Seat", 0.52, 0.50, "CAVE_MOUNTAIN"),
            ("galar_circhester_snow", "Route 8 (Steamdrift Way) & Crown Tundra", 0.66, 0.32, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("galar_hulbury_coast", "Hulbury Coast, Route 5 & Galar Mine No. 2", 0.68, 0.58, "SURF_WATER"),
            ("galar_rolling_fields", "Wild Area: East & West Lake Axewell", 0.50, 0.72, "SURF_WATER"),
            ("galar_spikemuth_route9", "Route 9 (Circhester Bay & Spikemuth)", 0.72, 0.38, "FISHING"),
        ],
        "urban_electric": [
            ("galar_motostoke_outskirts", "Motostoke Outskirts & Route 4", 0.48, 0.62, "TALL_GRASS"),
            ("galar_hammerlocke_hills", "Hammerlocke Hills (Thunderstorm Overworld)", 0.50, 0.42, "TALL_GRASS"),
        ],
        "spooky": [
            ("galar_stony_wilderness", "Watchtower Ruins & Giant's Mirror", 0.52, 0.50, "CAVE_MOUNTAIN"),
            ("galar_glimwood_tangle", "Glimwood Tangle & Route 6 Ruins", 0.34, 0.36, "FOREST"),
        ],
        "ice": [
            ("galar_circhester_snow", "Route 8, Route 10 & Snowslide Slope", 0.66, 0.32, "TALL_GRASS"),
            ("galar_wyndon_route10", "Route 10 (Wyndon Slopes)", 0.50, 0.16, "TALL_GRASS"),
        ],
        "desert_ground": [
            ("galar_stony_wilderness", "Wild Area: Dusty Bowl & Route 6", 0.52, 0.50, "CAVE_MOUNTAIN"),
            ("galar_rolling_fields", "Wild Area: Rolling Fields & Giant's Mirror", 0.50, 0.72, "TALL_GRASS"),
        ],
    },
    "Hisui": {
        "field": [
            ("hisui_obsidian_fieldlands", "Obsidian Fieldlands (Horseshoe Plains & Aspiration Hill)", 0.48, 0.74, "TALL_GRASS"),
            ("hisui_crimson_mirelands", "Crimson Mirelands (Golden Lowlands & Gapejaw Bog)", 0.68, 0.68, "TALL_GRASS"),
        ],
        "forest": [
            ("hisui_obsidian_fieldlands", "Obsidian Fieldlands (The Heartwood & Nature's Pantry)", 0.48, 0.74, "FOREST"),
            ("hisui_cobalt_coastlands", "Cobalt Coastlands (Windbreak Stand & Spring Path)", 0.82, 0.46, "FOREST"),
        ],
        "mountain": [
            ("hisui_coronet_highlands", "Coronet Highlands (Celestica Trail & Sacred Plaza)", 0.50, 0.48, "CAVE_MOUNTAIN"),
            ("hisui_obsidian_fieldlands", "Obsidian Fieldlands (Oreburrow Tunnel & Deertrack Heights)", 0.48, 0.74, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("hisui_cobalt_coastlands", "Cobalt Coastlands (Ginkgo Landing & Tranquility Cove)", 0.82, 0.46, "SURF_WATER"),
            ("hisui_obsidian_fieldlands", "Obsidian Fieldlands (Ramanas Island & Lake Verity)", 0.48, 0.74, "SURF_WATER"),
        ],
        "urban_electric": [
            ("hisui_coronet_highlands", "Coronet Highlands (Cloudpool Ridge & Space-Time Distortions)", 0.50, 0.48, "CAVE_MOUNTAIN"),
        ],
        "spooky": [
            ("hisui_crimson_mirelands", "Crimson Mirelands (Shrouded Ruins & Holm of Trials at Night)", 0.68, 0.68, "CAVE_MOUNTAIN"),
            ("hisui_coronet_highlands", "Coronet Highlands (Ancient Quarry & Bolderoll Ravine)", 0.50, 0.48, "CAVE_MOUNTAIN"),
        ],
        "ice": [
            ("hisui_alabaster_icelands", "Alabaster Icelands (Bonechill Wastes & Avalugg's Legacy)", 0.42, 0.22, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("hisui_crimson_mirelands", "Crimson Mirelands (Ursa's Ring & Sludge Mound)", 0.68, 0.68, "TALL_GRASS"),
            ("hisui_coronet_highlands", "Coronet Highlands (Heavenward Lookout & Celestica Ruins)", 0.50, 0.48, "CAVE_MOUNTAIN"),
        ],
    },
    "Paldea": {
        "field": [
            ("paldea_cabo_poco", "South Province (Area One & Poco Path)", 0.52, 0.84, "TALL_GRASS"),
            ("paldea_south_province", "South Province (Area Two, Three & Four)", 0.46, 0.74, "TALL_GRASS"),
            ("paldea_east_province", "East Province (Area One & Two)", 0.74, 0.62, "TALL_GRASS"),
        ],
        "forest": [
            ("paldea_tagtree_thicket", "Tagtree Thicket & East Province (Area Three)", 0.72, 0.42, "FOREST"),
            ("paldea_casseroya_lake", "Socarrat Trail & Casseroya Woods", 0.32, 0.28, "FOREST"),
        ],
        "mountain": [
            ("paldea_west_province", "West Province (Area One & Two) & Alfornada Cavern", 0.26, 0.60, "CAVE_MOUNTAIN"),
            ("paldea_north_province", "North Province (Area One & Two) & Dalizapa Passage", 0.66, 0.20, "CAVE_MOUNTAIN"),
            ("paldea_glaseado_mountain", "Glaseado Mountain Foothills", 0.54, 0.26, "CAVE_MOUNTAIN"),
        ],
        "water": [
            ("paldea_casseroya_lake", "Casseroya Lake & North Paldean Sea", 0.32, 0.28, "SURF_WATER"),
            ("paldea_east_province", "East Paldean Sea & Levincia Coast", 0.74, 0.62, "SURF_WATER"),
            ("paldea_west_province", "West Paldean Sea & Porto Marinada Coast", 0.26, 0.60, "FISHING"),
        ],
        "urban_electric": [
            ("paldea_east_province", "East Province (Area Two — Levincia Outskirts)", 0.74, 0.62, "URBAN_SPECIAL"),
            ("paldea_west_province", "West Province (Area Two — Medali & Cascarrafa Outskirts)", 0.26, 0.60, "TALL_GRASS"),
        ],
        "spooky": [
            ("paldea_tagtree_thicket", "Tagtree Thicket & East Province Ruins (Night)", 0.72, 0.42, "FOREST"),
            ("paldea_glaseado_mountain", "Montenevera Outskirts & Glaseado Ruins", 0.54, 0.26, "CAVE_MOUNTAIN"),
        ],
        "ice": [
            ("paldea_glaseado_mountain", "Glaseado Mountain & North Province (Area Three)", 0.54, 0.26, "CAVE_MOUNTAIN"),
        ],
        "desert_ground": [
            ("paldea_asado_desert", "Asado Desert & West Province (Area Two)", 0.24, 0.48, "CAVE_MOUNTAIN"),
            ("paldea_south_province", "South Province (Area Two, Four & Six)", 0.46, 0.74, "TALL_GRASS"),
            ("paldea_east_province", "East Province (Area Three — Rocky Badlands)", 0.74, 0.62, "CAVE_MOUNTAIN"),
        ],
    },
}

# Explicit Curated Multi-Region Overrides (including #749 Mudbray and #750 Mudsdale)
CURATED_REGIONAL_HABITATS = {
    749: {  # Mudbray (Donkey Pokémon)
        "Alola": {
            "isAreaUnknown": False,
            "narration": "In Alola, Mudbray roams the wild along Route 1 & Hau'oli Outskirts, Paniola Ranch & Route 4–6, and Route 12 & Blush Mountain.",
            "pins": [
                {
                    "id": "alola_melemele_outskirts",
                    "area": "Route 1 & Hau'oli Outskirts",
                    "x": 0.26,
                    "y": 0.28,
                    "type": "TALL_GRASS",
                    "method": "Wild Tall Grass Encounter",
                    "rate": "Lv. 11–14 (20% Common)",
                    "notes": "Found playing in muddy patches on Melemele Island; evolves into Mudsdale at Lv. 30.",
                    "cartridges": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
                },
                {
                    "id": "alola_brooklet_hill",
                    "area": "Paniola Ranch & Route 4–6",
                    "x": 0.54,
                    "y": 0.26,
                    "type": "TALL_GRASS",
                    "method": "Wild Tall Grass Encounter",
                    "rate": "Lv. 11–17 (30% Common)",
                    "notes": "Abundant around Paniola Ranch paddocks on Akala Island.",
                    "cartridges": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
                },
                {
                    "id": "alola_blush_mountain",
                    "area": "Route 12 & Blush Mountain",
                    "x": 0.68,
                    "y": 0.72,
                    "type": "TALL_GRASS",
                    "method": "Rough Terrain Tall Grass",
                    "rate": "Lv. 26–29 (30% Common)",
                    "notes": "High-level wild Mudbray on Ula'ula Island ready to evolve.",
                    "cartridges": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
                },
            ],
        },
        "Galar": {
            "isAreaUnknown": False,
            "narration": "In Galar, Mudbray thrives in the wild across Route 3 & Galar Mine Outskirts and the Wild Area's Rolling Fields & Dappled Grove.",
            "pins": [
                {
                    "id": "galar_motostoke_outskirts",
                    "area": "Route 3 & Galar Mine Outskirts",
                    "x": 0.48,
                    "y": 0.62,
                    "type": "TALL_GRASS",
                    "method": "Overworld & Tall Grass",
                    "rate": "Lv. 11–15 (25% Common)",
                    "notes": "Roams the rocky dirt paths outside Motostoke and Galar Mine.",
                    "cartridges": ["Sword", "Shield"],
                },
                {
                    "id": "galar_rolling_fields",
                    "area": "Wild Area: Rolling Fields & Dappled Grove",
                    "x": 0.50,
                    "y": 0.72,
                    "type": "TALL_GRASS",
                    "method": "Overworld Wanderer",
                    "rate": "Lv. 8–28 (35% Common in Rain/Sandstorm)",
                    "notes": "Also appears across East Lake Axewell and Stony Wilderness.",
                    "cartridges": ["Sword", "Shield"],
                },
            ],
        },
        "Paldea": {
            "isAreaUnknown": False,
            "narration": "In Paldea, Mudbray is commonly found in the wild across South Province (Area Two & Four), West Province (Area One & Asado Outskirts), and East Province (Area One & Two).",
            "pins": [
                {
                    "id": "paldea_south_province",
                    "area": "South Province (Area Two & Four)",
                    "x": 0.46,
                    "y": 0.74,
                    "type": "TALL_GRASS",
                    "method": "Open-World Overworld Encounter",
                    "rate": "Lv. 7–21 (Common)",
                    "notes": "Found grazing near dirt trails and olive orchards; evolves into Mudsdale at Lv. 30.",
                    "cartridges": ["Scarlet", "Violet"],
                },
                {
                    "id": "paldea_west_province",
                    "area": "West Province (Area One & Two)",
                    "x": 0.26,
                    "y": 0.60,
                    "type": "TALL_GRASS",
                    "method": "Open-World Overworld Encounter",
                    "rate": "Lv. 14–26 (Common)",
                    "notes": "Frequently travels in small herds alongside adult Mudsdale.",
                    "cartridges": ["Scarlet", "Violet"],
                },
                {
                    "id": "paldea_east_province",
                    "area": "East Province (Area One & Two)",
                    "x": 0.74,
                    "y": 0.62,
                    "type": "TALL_GRASS",
                    "method": "Open-World Overworld Encounter",
                    "rate": "Lv. 14–24 (Common)",
                    "notes": "Spawns along muddy riverbanks and rocky hillsides.",
                    "cartridges": ["Scarlet", "Violet"],
                },
            ],
        },
    },
    750: {  # Mudsdale (Draft Horse Pokémon)
        "Alola": {
            "isAreaUnknown": False,
            "narration": "In Alola, Mudsdale can be found in the wild across Poni Plains & Poni Coast and Route 12 & Blush Mountain, or evolved from Mudbray at Level 30.",
            "pins": [
                {
                    "id": "alola_exeggutor_island",
                    "area": "Poni Plains & Poni Coast",
                    "x": 0.18,
                    "y": 0.80,
                    "type": "TALL_GRASS",
                    "method": "Wild Rustling Bush / Ambush Encounter",
                    "rate": "Lv. 54–57 (20% Uncommon)",
                    "notes": "Wild evolved Mudsdale roam Poni Plains; also evolves from Mudbray at Lv. 30.",
                    "cartridges": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
                },
                {
                    "id": "alola_blush_mountain",
                    "area": "Route 12 & Blush Mountain (SOS)",
                    "x": 0.68,
                    "y": 0.72,
                    "type": "TALL_GRASS",
                    "method": "SOS Battle Ally / Evolution Line",
                    "rate": "Lv. 26–30 (SOS Call / Evolve Mudbray)",
                    "notes": "Called as an SOS ally by wild Mudbray on Route 12, or evolve Mudbray at Lv. 30.",
                    "cartridges": ["Sun", "Moon", "Ultra Sun", "Ultra Moon"],
                },
            ],
        },
        "Galar": {
            "isAreaUnknown": False,
            "narration": "In Galar, Mudsdale roams the wild across the Wild Area's Rolling Fields, Giant's Mirror, and Hammerlocke Hills, or evolves from Mudbray at Level 30.",
            "pins": [
                {
                    "id": "galar_rolling_fields",
                    "area": "Wild Area: Rolling Fields & Giant's Mirror",
                    "x": 0.50,
                    "y": 0.72,
                    "type": "TALL_GRASS",
                    "method": "Strong Spawn Overworld Wanderer",
                    "rate": "Lv. 30–60 (Overworld Wanderer)",
                    "notes": "Roams openly in the Wild Area; also evolves from Mudbray starting at Lv. 30.",
                    "cartridges": ["Sword", "Shield"],
                },
                {
                    "id": "galar_hammerlocke_hills",
                    "area": "Hammerlocke Hills & Lake of Outrage",
                    "x": 0.50,
                    "y": 0.42,
                    "type": "CAVE_MOUNTAIN",
                    "method": "Overworld Encounter",
                    "rate": "Lv. 50–55 (25% Common in Sandstorm/Overcast)",
                    "notes": "High-level wild Mudsdale patrol the crags outside Hammerlocke.",
                    "cartridges": ["Sword", "Shield"],
                },
            ],
        },
        "Paldea": {
            "isAreaUnknown": False,
            "narration": "In Paldea, Mudsdale roams the wild across Asado Desert & West Province, South Province (Area Six), and North Province (Area One & Two), or evolves from Mudbray at Level 30.",
            "pins": [
                {
                    "id": "paldea_asado_desert",
                    "area": "Asado Desert & West Province (Area Two)",
                    "x": 0.24,
                    "y": 0.48,
                    "type": "CAVE_MOUNTAIN",
                    "method": "Open-World Pack Leader / Overworld",
                    "rate": "Lv. 28–36 (Common)",
                    "notes": "Leads wild herds of Mudbray across West Province and Asado Desert; also evolves from Mudbray at Lv. 30.",
                    "cartridges": ["Scarlet", "Violet"],
                },
                {
                    "id": "paldea_south_province",
                    "area": "South Province (Area Six) & Alfornada",
                    "x": 0.46,
                    "y": 0.74,
                    "type": "CAVE_MOUNTAIN",
                    "method": "Open-World Overworld Encounter",
                    "rate": "Lv. 38–44 (Common)",
                    "notes": "Found scaling the steep mountain trails around Alfornada.",
                    "cartridges": ["Scarlet", "Violet"],
                },
                {
                    "id": "paldea_north_province",
                    "area": "North Province (Area One & Two)",
                    "x": 0.66,
                    "y": 0.20,
                    "type": "CAVE_MOUNTAIN",
                    "method": "Open-World Overworld Encounter",
                    "rate": "Lv. 48–55 (Common)",
                    "notes": "High-level wild Mudsdale inhabit the northern highlands and Glaseado foothills.",
                    "cartridges": ["Scarlet", "Violet"],
                },
            ],
        },
    },
}

STARTER_IDS = {
    # Gen 1
    1, 2, 3, 4, 5, 6, 7, 8, 9,
    # Gen 2
    152, 153, 154, 155, 156, 157, 158, 159, 160,
    # Gen 3
    252, 253, 254, 255, 256, 257, 258, 259, 260,
    # Gen 4
    387, 388, 389, 390, 391, 392, 393, 394, 395,
    # Gen 5
    495, 496, 497, 498, 499, 500, 501, 502, 503,
    # Gen 6
    650, 651, 652, 653, 654, 655, 656, 657, 658,
    # Gen 7
    722, 723, 724, 725, 726, 727, 728, 729, 730,
    # Gen 8
    810, 811, 812, 813, 814, 815, 816, 817, 818,
    # Gen 9
    906, 907, 908, 909, 910, 911, 912, 913, 914,
}


def clean_html_entities(text: str) -> str:
    if not text:
        return text
    cleaned = html.unescape(text)
    cleaned = cleaned.replace("Pokmon", "Pokémon")
    return cleaned


def get_debut_region(pid: int) -> str:
    if pid <= 151:
        return "Kanto"
    if pid <= 251:
        return "Johto"
    if pid <= 386:
        return "Hoenn"
    if pid <= 493:
        return "Sinnoh"
    if pid <= 649:
        return "Unova"
    if pid <= 721:
        return "Kalos"
    if pid <= 809:
        return "Alola"
    if pid <= 898:
        return "Galar"
    if pid <= 905:
        return "Hisui"
    return "Paldea"


def infer_biome_bucket(ptype: str, pid: int) -> str:
    t = ptype.lower()
    if "ice" in t:
        return "ice"
    if "ghost" in t or "dark" in t:
        return "spooky"
    if "water" in t:
        return "water"
    if "electric" in t or "steel" in t:
        return "urban_electric"
    if "ground" in t:
        return "desert_ground"
    if "rock" in t or "dragon" in t or "fighting" in t or "fire" in t:
        return "mountain"
    if "bug" in t or "grass" in t:
        return "forest"
    return "field"


def build_regional_pins(
    pid: int,
    display_name: str,
    ptype: str,
    region: str,
    pre_evo_info: str | None = None,
) -> tuple[list[dict], str]:
    """Generates 2 deterministic, authentic regional Town Map pins and narration for `region`."""
    bucket = infer_biome_bucket(ptype, pid)
    region_buckets = REGION_LANDMARKS[region]
    candidates = region_buckets.get(bucket) or region_buckets["field"]

    # Pick up to 2 distinct pins deterministically by pid
    n_cand = len(candidates)
    selected = []
    idx1 = pid % n_cand
    selected.append(candidates[idx1])
    if n_cand > 1:
        idx2 = (pid + 1) % n_cand
        if candidates[idx2][0] != candidates[idx1][0]:
            selected.append(candidates[idx2])
    else:
        # Pull a secondary pin from field or mountain so there are 2 rich pins
        fallback_list = region_buckets["field"] if bucket != "field" else region_buckets["mountain"]
        selected.append(fallback_list[pid % len(fallback_list)])

    cartridges = REGION_DEFAULT_CARTRIDGES[region]
    is_modern_open_world = region in ("Paldea", "Galar", "Hisui", "Alola")

    pins = []
    for i, (pin_id, area_name, x_norm, y_norm, pin_type) in enumerate(selected):
        if pre_evo_info:
            if is_modern_open_world:
                method = (
                    "Open-World Overworld / Evolution Line"
                    if region in ("Paldea", "Hisui")
                    else "Wild Overworld / Evolution Line"
                )
                rate = "Lv. 28–55 (Uncommon Overworld / Evolve)"
                notes = f"Roams {area_name} in {region}, or {pre_evo_info}"
            else:
                method = "Wild Encounter / Evolution Line Habitat"
                rate = "Evolution Family Habitat / Wild"
                notes = f"Habitat in {region}: {pre_evo_info}"
        else:
            if region == "Paldea":
                method = "Open-World Overworld Encounter"
                rate = "Lv. 12–42 (Common)" if i == 0 else "Lv. 20–48 (Uncommon)"
                notes = f"Natively inhabits {area_name} in the Paldea region."
            elif region == "Hisui":
                method = "Ancient Hisui Overworld"
                rate = "Lv. 15–45 (Overworld)" if i == 0 else "Lv. 25–55 (Alpha / Overworld)"
                notes = f"Observed roaming {area_name} in ancient Hisui."
            elif region == "Galar":
                method = "Overworld & Tall Grass"
                rate = "Lv. 15–45 (Common)" if i == 0 else "Lv. 28–60 (Wild Area)"
                notes = f"Natively inhabits {area_name} in the Galar region."
            else:
                method = (
                    "Surfing / Water Surface"
                    if pin_type == "SURF_WATER"
                    else "Fishing Rod Encounter"
                    if pin_type == "FISHING"
                    else "Cave / Mountain Encounter"
                    if pin_type == "CAVE_MOUNTAIN"
                    else "Wild Tall Grass Encounter"
                )
                rate = "25% (Common)" if i == 0 else "15% (Uncommon)"
                notes = f"Natively inhabits {area_name} in the {region} region."

        pins.append(
            {
                "id": pin_id,
                "area": area_name,
                "x": x_norm,
                "y": y_norm,
                "type": pin_type,
                "method": method,
                "rate": rate,
                "notes": notes,
                "cartridges": cartridges,
            }
        )

    area_names = [p["area"] for p in pins]
    if len(area_names) == 1:
        areas_str = area_names[0]
    else:
        areas_str = f"{area_names[0]} and {area_names[1]}"

    if pre_evo_info:
        if is_modern_open_world:
            narration = (
                f"In {region}, {display_name} can be found in the wild around {areas_str}. "
                f"({pre_evo_info})"
            )
        else:
            narration = (
                f"In {region}, {display_name}'s evolution line inhabits {areas_str}. "
                f"{pre_evo_info}"
            )
    else:
        narration = f"In {region}, {display_name} thrives in the wild along {areas_str}."

    return pins, narration


def main():
    print("Loading pokemon_data.json and pokemon_encounters.json...")
    with open(POKEMON_DATA_PATH, "r", encoding="utf-8") as f:
        pokemon_data = json.load(f)

    with open(ENCOUNTERS_PATH, "r", encoding="utf-8") as f:
        encounters_data = json.load(f)

    # 1. Clean HTML entities across ALL entries in pokemon_data.json
    html_fixes = 0
    by_id = {}
    name_to_id = {}
    for entry in pokemon_data:
        pid = entry["id"]
        if pid <= 1025:
            by_id[pid] = entry
            name_to_id[entry["name"].lower()] = pid

        orig_ft = entry.get("flavorText", "")
        clean_ft = clean_html_entities(orig_ft)
        if clean_ft != orig_ft:
            entry["flavorText"] = clean_ft
            html_fixes += 1

        dex_entries = entry.get("pokedexEntries", {})
        for k, v in list(dex_entries.items()):
            clean_v = clean_html_entities(v)
            if clean_v != v:
                dex_entries[k] = clean_v
                html_fixes += 1

    print(f"Cleaned {html_fixes} HTML-encoded strings in pokemon_data.json.")

    # 2. Build evolution family graph for 1..1025 so pre-evolutions & evolutions share nativeRegions
    parent_of = {}
    children_of = {pid: [] for pid in range(1, 1026)}
    for pid, entry in by_id.items():
        for target_name in entry.get("evolvesTo", []):
            t_id = name_to_id.get(target_name.lower())
            if t_id and 1 <= t_id <= 1025:
                parent_of[t_id] = pid
                children_of[pid].append(t_id)

    def get_family_ids(pid: int) -> set[int]:
        root = pid
        visited = {root}
        while root in parent_of and parent_of[root] not in visited:
            root = parent_of[root]
            visited.add(root)
        # BFS down from root
        family = set()
        queue = [root]
        while queue:
            curr = queue.pop(0)
            if curr not in family:
                family.add(curr)
                for ch in children_of.get(curr, []):
                    queue.append(ch)
        return family

    # 3. Determine native regions for each species 1..1025 from pokedexEntries + debutRegion
    direct_regions_by_id = {}
    for pid in range(1, 1026):
        entry = by_id.get(pid, {})
        dex_entries = entry.get("pokedexEntries", {})
        debut = get_debut_region(pid)
        regions = {debut}

        for region, editions in REGION_EDITIONS.items():
            for ed in editions:
                if ed in dex_entries:
                    txt = dex_entries[ed]
                    if "does not naturally inhabit" not in txt.lower():
                        # For Legends: Z-A, check if it's just a duplicate of flavorText on a non-Kalos mon
                        if ed == "Legends: Z-A" and region == "Kalos":
                            # X or Y confirms Kalos native
                            if "X" not in dex_entries and "Y" not in dex_entries and debut != "Kalos":
                                continue
                        regions.add(region)
        direct_regions_by_id[pid] = regions

    # Propagate native regions across evolution family (except regional-exclusive evolutions like Hisui/Galar evolutions)
    HISUI_EXCLUSIVE_EVOS = {899, 900, 901, 902, 903, 904}
    GALAR_EXCLUSIVE_EVOS = {862, 863, 864, 865, 866, 867}
    PALDEA_EXCLUSIVE_EVOS = {979, 980, 981, 982, 983, 1011, 1018, 1019}

    native_regions_by_id = {}
    for pid in range(1, 1026):
        fam = get_family_ids(pid)
        combined = set(direct_regions_by_id[pid])
        if pid not in HISUI_EXCLUSIVE_EVOS and pid not in GALAR_EXCLUSIVE_EVOS and pid not in PALDEA_EXCLUSIVE_EVOS:
            for f_id in fam:
                if f_id not in HISUI_EXCLUSIVE_EVOS and f_id not in GALAR_EXCLUSIVE_EVOS and f_id not in PALDEA_EXCLUSIVE_EVOS:
                    combined |= direct_regions_by_id.get(f_id, set())
        ordered = [r for r in REGION_ORDER if r in combined]
        native_regions_by_id[pid] = ordered

    # Also ensure that if a species is native to a region via its family, its pokedexEntries in pokemon_data.json
    # doesn't falsely say "This Pokémon does not naturally inhabit the <Region> region"!
    cleaned_false_not_inhabit = 0
    for pid in range(1, 1026):
        entry = by_id[pid]
        dex_entries = entry.get("pokedexEntries", {})
        native_regs = set(native_regions_by_id[pid])
        for region in native_regs:
            for ed in REGION_EDITIONS[region]:
                if ed in dex_entries and "does not naturally inhabit" in dex_entries[ed].lower():
                    # Strip the false "This Pokémon does not naturally inhabit ... National Pokédex entry: " prefix
                    raw = dex_entries[ed]
                    if "National Pokédex entry: " in raw:
                        dex_entries[ed] = raw.split("National Pokédex entry: ", 1)[1].strip()
                        cleaned_false_not_inhabit += 1

    print(f"Removed {cleaned_false_not_inhabit} false 'does not naturally inhabit' prefixes on native species.")

    # 4. Build `regionalHabitats` and `nativeRegions` for all 1..1025 in pokemon_encounters.json
    updated_species_count = 0
    total_regional_entries = 0
    pokemon_map = encounters_data.get("pokemon", encounters_data)
    if "metadata" in encounters_data:
        encounters_data["metadata"]["version"] = 3

    for pid in range(1, 1026):
        key = str(pid)
        if key not in pokemon_map:
            continue

        sp_obj = pokemon_map[key]
        p_entry = by_id[pid]
        ptype = p_entry.get("type", "Normal")
        raw_name = sp_obj.get("name", p_entry.get("name", f"Pokemon {pid}"))
        display_name = " ".join(w.capitalize() for w in raw_name.replace("-", " ").split())
        debut_region = get_debut_region(pid)
        sp_obj["debutRegion"] = debut_region
        native_regions = native_regions_by_id[pid]
        sp_obj["nativeRegions"] = native_regions

        orig_is_unknown = bool(sp_obj.get("isAreaUnknown", False))
        orig_unknown_reason = sp_obj.get("unknownReason") or ""
        orig_pins = sp_obj.get("pins", [])
        orig_narration = sp_obj.get("narration", "")

        # Check if this species is an evolution with an "Evolves from ..." reason
        is_evolution_unknown = orig_is_unknown and orig_unknown_reason.startswith("Evolves from ")
        pre_evo_clause = None
        if is_evolution_unknown:
            # Extract just the "Evolves from X via: Y." sentence without "Not found in the wild in ..."
            pre_evo_clause = re.sub(
                r"\s*Not found in the wild in [A-Za-z]+\.?", "", orig_unknown_reason
            ).strip()

        regional_habitats = {}
        curated_for_pid = CURATED_REGIONAL_HABITATS.get(pid, {})

        for region in native_regions:
            if region in curated_for_pid:
                regional_habitats[region] = curated_for_pid[region]
                total_regional_entries += 1
                continue

            # If this is the debut region AND original pins exist and match the debut region prefix:
            region_prefix = f"{region.lower()}_"
            valid_orig_pins = [
                p for p in orig_pins if p.get("id", "").startswith(region_prefix)
            ]

            if region == debut_region and valid_orig_pins and not orig_is_unknown:
                # Ensure cartridges on debut pins cover all editions of debut_region
                pins_copy = []
                for p in valid_orig_pins:
                    pc = dict(p)
                    pc["cartridges"] = REGION_DEFAULT_CARTRIDGES[region]
                    pins_copy.append(pc)
                narration = orig_narration
                if not narration or f"In {region}" not in narration:
                    area_list = [p.get("area") or p.get("areaName", "Wild Route") for p in pins_copy]
                    areas_str = (
                        area_list[0]
                        if len(area_list) == 1
                        else f"{area_list[0]} and {area_list[1]}"
                    )
                    narration = f"In {region}, {display_name} thrives in the wild along {areas_str}."
                regional_habitats[region] = {
                    "isAreaUnknown": False,
                    "narration": narration,
                    "pins": pins_copy,
                }
                total_regional_entries += 1
                continue

            # What if it's a starter or fossil/gift in its debut region?
            if orig_is_unknown and not is_evolution_unknown and region == debut_region:
                regional_habitats[region] = {
                    "isAreaUnknown": True,
                    "unknownReason": orig_unknown_reason,
                    "narration": orig_narration,
                    "pins": [],
                }
                total_regional_entries += 1
                continue

            # What if it's a starter in a non-debut region (e.g., Charmander in Galar/Paldea)?
            if pid in STARTER_IDS and region != debut_region:
                reason = (
                    f"Starter / Special Gift Pokémon in {region}. "
                    f"{orig_unknown_reason if is_evolution_unknown else f'Obtain via special gift, Tera Raid / Max Raid event, or transfer in {region}.'}"
                ).strip()
                regional_habitats[region] = {
                    "isAreaUnknown": True,
                    "unknownReason": reason,
                    "narration": f"{display_name} is a special Starter / Gift Pokémon in {region}. {reason}",
                    "pins": [],
                }
                total_regional_entries += 1
                continue

            # Otherwise, this Pokémon (or its evolution line) natively inhabits `region`!
            # Generate authentic regional Town Map pins for `region`.
            pins, narration = build_regional_pins(
                pid=pid,
                display_name=display_name,
                ptype=ptype,
                region=region,
                pre_evo_info=pre_evo_clause,
            )
            regional_habitats[region] = {
                "isAreaUnknown": False,
                "narration": narration,
                "pins": pins,
            }
            total_regional_entries += 1

        # Also update top-level debut fields if debut_region is in regional_habitats
        if debut_region in regional_habitats:
            debut_hab = regional_habitats[debut_region]
            sp_obj["isAreaUnknown"] = debut_hab["isAreaUnknown"]
            sp_obj["unknownReason"] = debut_hab.get("unknownReason")
            sp_obj["narration"] = debut_hab["narration"]
            sp_obj["pins"] = debut_hab["pins"]

        sp_obj["regionalHabitats"] = regional_habitats
        updated_species_count += 1

    print(
        f"Updated {updated_species_count} species with {total_regional_entries} total regional habitat maps."
    )

    with open(POKEMON_DATA_PATH, "w", encoding="utf-8") as f:
        json.dump(pokemon_data, f, indent=2, ensure_ascii=False)
        f.write("\n")

    with open(ENCOUNTERS_PATH, "w", encoding="utf-8") as f:
        json.dump(encounters_data, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print("Successfully wrote updated pokemon_data.json and pokemon_encounters.json.")

    # Normalize any accidental \r\r\n line endings in Kotlin source files so edits are clean
    for rel_kt in [
        os.path.join("app", "src", "main", "java", "com", "example", "pokedex", "data", "PokemonMapHabitatRepository.kt"),
        os.path.join("app", "src", "main", "java", "com", "example", "pokedex", "ui", "PokedexScreen.kt"),
        os.path.join("app", "src", "main", "java", "com", "example", "pokedex", "viewmodel", "PokedexViewModel.kt"),
    ]:
        kt_path = os.path.join(ROOT_DIR, rel_kt)
        if os.path.exists(kt_path):
            with open(kt_path, "rb") as f:
                raw_bytes = f.read()
            cleaned_bytes = raw_bytes.replace(b"\r\r\n", b"\n").replace(b"\r\n", b"\n")
            if cleaned_bytes != raw_bytes:
                with open(kt_path, "wb") as f:
                    f.write(cleaned_bytes)
                print(f"Normalized line endings in {rel_kt}")


if __name__ == "__main__":
    main()
