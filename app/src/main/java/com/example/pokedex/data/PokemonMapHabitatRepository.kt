package com.example.pokedex.data

import java.util.Locale

object PokemonMapHabitatRepository {

    fun getHabitatForPokemon(
        pokemonId: Int,
        pokemonName: String,
        cartridgeId: String?
    ): PokemonHabitatMap {
        val cid = cartridgeId?.trim() ?: "Default"
        val normName = pokemonName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        // Determine Region and Map Asset based on cartridge
        val (regionName, mapAsset, editionDisplayName) = resolveRegionAndMap(cid)

        // 1. Check Starter Partner Gifts & Cross-Generation Regional Gifts
        val starterResult = resolveStarterGift(pokemonId, normName, cid, regionName, mapAsset, editionDisplayName)
        if (starterResult != null) return starterResult

        // 2. Check Special Static / Legendary Encounters & In-Game NPC Gifts
        val legendaryResult = resolveLegendaryOrStatic(pokemonId, normName, cid, regionName, mapAsset, editionDisplayName)
        if (legendaryResult != null) return legendaryResult

        // 3. Check Known Wild Encounters
        val wildResult = resolveWildEncounters(pokemonId, normName, cid, regionName, mapAsset, editionDisplayName)
        if (wildResult != null) return wildResult

        // 4. Evolution / Non-Wild Fallback (Area Unknown)
        return resolveEvolutionOrAreaUnknown(pokemonId, normName, cid, regionName, mapAsset, editionDisplayName)
    }

    fun getMapAssetForCartridge(cartridgeId: String?, pokemonId: Int = 1): String {
        val cid = cartridgeId?.trim() ?: "Default"
        if (cid.equals("Default", ignoreCase = true)) {
            return getDebutMapAssetForPokemon(pokemonId)
        }
        return resolveRegionAndMap(cid).second
    }

    fun getDebutMapAssetForPokemon(pokemonId: Int): String {
        return when (pokemonId) {
            in 1..151 -> "kanto_rgby.png"
            in 152..251 -> "johto_gsc.png"
            in 252..386 -> "hoenn_rse.png"
            in 387..493 -> "sinnoh_pt.png"
            in 494..649 -> "unova_bw.png"
            in 650..721 -> "kalos_xy.png"
            in 722..809 -> "alola_sm.png"
            in 810..898 -> "galar_swsh.png"
            in 899..905 -> "hisui_pla.png"
            in 906..1025 -> "paldea_sv.png"
            else -> "kanto_rgby.png"
        }
    }

    private fun resolveRegionAndMap(cartridgeId: String): Triple<String, String, String> {
        val upper = cartridgeId.uppercase(Locale.ROOT)
        return when {
            upper in listOf("RED", "BLUE", "YELLOW") -> Triple("Kanto", "kanto_rgby.png", cartridgeId.uppercase())
            upper in listOf("FIRERED", "LEAFGREEN", "FIRE RED", "LEAF GREEN") -> Triple("Kanto", "kanto_frlg.png", cartridgeId.uppercase())
            upper in listOf("LET'S GO PIKACHU", "LET'S GO EEVEE", "LETS GO PIKACHU", "LETS GO EEVEE") -> Triple("Kanto", "kanto_frlg.png", cartridgeId.uppercase())
            upper in listOf("GOLD", "SILVER", "CRYSTAL") -> Triple("Johto", "johto_gsc.png", cartridgeId.uppercase())
            upper in listOf("HEARTGOLD", "SOULSILVER", "HEART GOLD", "SOUL SILVER") -> Triple("Johto", "johto_hgss.png", cartridgeId.uppercase())
            upper in listOf("RUBY", "SAPPHIRE", "EMERALD") -> Triple("Hoenn", "hoenn_rse.png", cartridgeId.uppercase())
            upper in listOf("OMEGA RUBY", "ALPHA SAPPHIRE", "OMEGARUBY", "ALPHASAPPHIRE") -> Triple("Hoenn", "hoenn_oras.png", cartridgeId.uppercase())
            upper in listOf("DIAMOND", "PEARL", "PLATINUM", "BDSP") -> Triple("Sinnoh", "sinnoh_pt.png", cartridgeId.uppercase())
            upper in listOf("BLACK", "WHITE") -> Triple("Unova", "unova_bw.png", cartridgeId.uppercase())
            upper in listOf("BLACK 2", "WHITE 2", "BLACK2", "WHITE2") -> Triple("Unova", "unova_b2w2.png", cartridgeId.uppercase())
            upper in listOf("X", "Y") -> Triple("Kalos", "kalos_xy.png", cartridgeId.uppercase())
            upper in listOf("SUN", "MOON") -> Triple("Alola", "alola_sm.png", cartridgeId.uppercase())
            upper in listOf("ULTRA SUN", "ULTRA MOON", "ULTRASUN", "ULTRAMOON") -> Triple("Alola", "alola_usum.png", cartridgeId.uppercase())
            upper in listOf("SWORD", "SHIELD") -> Triple("Galar", "galar_swsh.png", cartridgeId.uppercase())
            upper in listOf("LEGENDS: ARCEUS", "LEGENDS ARCEUS", "HISUI") -> Triple("Hisui", "hisui_pla.png", "LEGENDS: ARCEUS")
            upper in listOf("SCARLET", "VIOLET") -> Triple("Paldea", "paldea_sv.png", cartridgeId.uppercase())
            upper in listOf("GO", "POKEMON GO", "POKÉMON GO") -> Triple("Pokémon GO", "pokemongo_map.png", "POKÉMON GO")
            else -> Triple("Kanto", "kanto_rgby.png", "CANONICAL")
        }
    }

    /**
     * Resolves Starter gifts and Cross-Generation Gifts across ALL games:
     * - Gen 1: Red/Blue/Yellow/FRLG/Let's Go, HGSS (Prof. Oak post-Red), XY (Prof. Sycamore Lumiose), SwSh (Leon/Honey)
     * - Gen 2: GSC/HGSS, Emerald (Birch), ORAS (Birch), PLA (Laventon)
     * - Gen 3: RSE/ORAS, HGSS (Steven Stone Silph Co.)
     * - Gen 4: DPPt/BDSP, ORAS (Birch), PLA (Wild Sinnoh starters)
     * - Gen 5: BW (Juniper), B2W2 (Bianca Aspertia), ORAS (Birch), PLA (Laventon)
     * - Gen 6: XY (Shauna/Tierno Aquacorde)
     * - Gen 7: SM/USUM (Hala/Kukui Iki Town), PLA (Laventon)
     * - Gen 8: SwSh (Leon Postwick)
     * - Gen 9: SV (Clavell Cabo Poco)
     */
    private fun resolveStarterGift(
        pokemonId: Int,
        pokemonName: String,
        cartridgeId: String,
        regionName: String,
        mapAsset: String,
        editionDisplayName: String
    ): PokemonHabitatMap? {
        val cid = cartridgeId.uppercase(Locale.ROOT)

        // ==========================================
        // 1. GEN 1 STARTERS: Bulbasaur (1), Charmander (4), Squirtle (7)
        // ==========================================
        if (pokemonId in listOf(1, 4, 7)) {
            // A. Pokemon Yellow
            if (cid.contains("YELLOW")) {
                val pin = when (pokemonId) {
                    1 -> MapLocationPin("cerulean_yellow", "Cerulean City", MapCoordinate(0.640f, 0.380f), MapPinType.SPECIAL_GIFT, "Gift from Melanie inside house", "Lv. 10 (High Pikachu friendship)", "Melanie entrusts Bulbasaur to you after caring for injured Pokémon.")
                    4 -> MapLocationPin("route24_yellow", "Route 24 (Damian's Rock)", MapCoordinate(0.640f, 0.250f), MapPinType.SPECIAL_GIFT, "Gift from Trainer Damian", "Lv. 10", "Damian abandoned Charmander in the rain and gives it to you.")
                    else -> MapLocationPin("vermilion_yellow", "Vermilion City", MapCoordinate(0.640f, 0.610f), MapPinType.SPECIAL_GIFT, "Gift from Officer Jenny", "Lv. 10 (Thunder Badge required)", "Officer Jenny rewards you with Squirtle after earning the Thunder Badge from Lt. Surge.")
                }
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = "kanto_rgby.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon Yellow, $pokemonName is received in ${pin.areaName} as a special in-game gift: ${pin.encounterMethod}."
                )
            }

            // B. Let's Go Pikachu / Let's Go Eevee
            if (cid.contains("LET'S GO") || cid.contains("LETS GO")) {
                val pin = when (pokemonId) {
                    1 -> MapLocationPin("cerulean_letsgo", "Cerulean City (Melanie's House)", MapCoordinate(0.640f, 0.380f), MapPinType.SPECIAL_GIFT, "Gift from Melanie (30+ caught)", "Lv. 12", "Also spawns rarely in tall grass throughout Viridian Forest.")
                    4 -> MapLocationPin("route24_letsgo", "Route 24 (North of Nugget Bridge)", MapCoordinate(0.640f, 0.250f), MapPinType.SPECIAL_GIFT, "Gift from Damian (50+ caught)", "Lv. 14", "Also spawns rarely on Route 3, Route 4, and inside Rock Tunnel.")
                    else -> MapLocationPin("vermilion_letsgo", "Vermilion City", MapCoordinate(0.640f, 0.610f), MapPinType.SPECIAL_GIFT, "Gift from Officer Jenny (60+ caught)", "Lv. 16", "Also spawns rarely in waters along Route 24 and Route 25.")
                }
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In $editionDisplayName, $pokemonName can be obtained as a gift in ${pin.areaName} once you have captured enough Pokémon."
                )
            }

            // C. Pokemon X / Y (Kalos - Sycamore Gift with Mega Stones!)
            if (regionName == "Kalos" || cid.contains("X") || cid.contains("Y")) {
                val stoneName = when (pokemonId) {
                    1 -> "Venusaurite"
                    4 -> if (cid.contains("X")) "Charizardite X" else "Charizardite Y"
                    else -> "Blastoisinite"
                }
                val pin = MapLocationPin(
                    id = "lumiose_sycamore_lab",
                    areaName = "Lumiose City (Sycamore Pokémon Lab 2F)",
                    coordinate = MapCoordinate(0.500f, 0.500f),
                    type = MapPinType.SPECIAL_GIFT,
                    encounterMethod = "Gift from Professor Sycamore",
                    rateOrLevel = "Lv. 10 (Holding $stoneName)",
                    notes = "Professor Sycamore battles you in Lumiose City and awards your choice of a classic Kanto starter holding its Mega Stone!"
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Kalos", mapAsset = "kalos_xy.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is gifted by Professor Sycamore on floor 2F of the Pokémon Lab in Lumiose City, holding its Mega Stone!"
                )
            }

            // D. Pokemon Sword / Shield (Galar - Leon / Honey Gifts)
            if (regionName == "Galar" || cid.contains("SWORD") || cid.contains("SHIELD")) {
                if (pokemonId == 4) {
                    val pin = MapLocationPin(
                        id = "postwick_leon_charmander",
                        areaName = "Postwick (Hop's Bedroom)",
                        coordinate = MapCoordinate(0.500f, 0.920f),
                        type = MapPinType.SPECIAL_GIFT,
                        encounterMethod = "Post-Game Gift from Champion Leon",
                        rateOrLevel = "Lv. 5 (Gigantamax Factor)",
                        notes = "After becoming Champion, Champion Leon leaves a Poké Ball containing Charmander in Hop's bedroom. It is capable of Gigantamaxing when fully evolved into Charizard!"
                    )
                    return PokemonHabitatMap(
                        pokemonId = 4, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Galar", mapAsset = "galar_swsh.png",
                        isAreaUnknown = false, pins = listOf(pin),
                        narrationText = "In Pokémon $editionDisplayName, Charmander is gifted by Champion Leon in Hop's bedroom in Postwick after you enter the Hall of Fame, possessing the Gigantamax factor!"
                    )
                } else {
                    val pin = MapLocationPin(
                        id = "master_dojo_honey",
                        areaName = "Master Dojo (Isle of Armor)",
                        coordinate = MapCoordinate(0.880f, 0.650f),
                        type = MapPinType.SPECIAL_GIFT,
                        encounterMethod = "Gift from Honey (Dojo Trial 1)",
                        rateOrLevel = "Lv. 5 (Gigantamax Factor)",
                        notes = "Rewarded by Honey after completing the first trial at the Master Dojo. Evolves into a Gigantamax-capable partner!"
                    )
                    return PokemonHabitatMap(
                        pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Galar", mapAsset = "galar_swsh.png",
                        isAreaUnknown = false, pins = listOf(pin),
                        narrationText = "In Pokémon $editionDisplayName, $pokemonName is gifted by Honey at the Master Dojo on the Isle of Armor with the Gigantamax factor."
                    )
                }
            }

            // E. HeartGold / SoulSilver (Kanto Post-Game Gift from Prof. Oak)
            if (cid.contains("HEART") || cid.contains("SOUL")) {
                val pin = MapLocationPin(
                    id = "pallet_oak_hgss",
                    areaName = "Pallet Town (Prof. Oak's Lab)",
                    coordinate = MapCoordinate(0.224f, 0.800f),
                    type = MapPinType.SPECIAL_GIFT,
                    encounterMethod = "Gift from Professor Oak (Post-Red)",
                    rateOrLevel = "Lv. 5",
                    notes = "After earning all 16 Badges and defeating Champion Red atop Mt. Silver, visit Professor Oak in Pallet Town to choose one Kanto starter!"
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Johto / Kanto", mapAsset = "kanto_rgby.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is awarded by Professor Oak in Pallet Town after you defeat Champion Red atop Mt. Silver!"
                )
            }

            // F. Classic Kanto (Red, Blue, FireRed, LeafGreen, Default)
            val isFRLG = cid.contains("FIRE") || cid.contains("LEAF")
            val coord = if (isFRLG) MapCoordinate(0.150f, 0.750f) else MapCoordinate(0.224f, 0.800f)
            val pin = MapLocationPin(
                id = "pallet_oak",
                areaName = "Pallet Town (Pokémon Lab)",
                coordinate = coord,
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Professor Oak",
                rateOrLevel = "Lv. 5",
                notes = "Chosen as your first partner Pokémon in the Pallet Town Pokémon Lab from Professor Oak."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is chosen as your first partner Pokémon in Pallet Town, received as a starter gift from Professor Oak in the Pokémon Lab."
            )
        }

        // ==========================================
        // 2. PIKACHU (#25) & EEVEE (#133) STARTERS
        // ==========================================
        if (pokemonId == 25 && (cid.contains("YELLOW") || cid.contains("LET'S GO PIKACHU") || cid.contains("LETS GO PIKACHU"))) {
            val pin = MapLocationPin(
                id = "pallet_oak_pikachu",
                areaName = "Pallet Town (Pokémon Lab)",
                coordinate = MapCoordinate(0.224f, 0.800f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter partner from Professor Oak",
                rateOrLevel = "Lv. 5",
                notes = "Caught by Professor Oak on Route 1 and given to you as your faithful partner Pokémon."
            )
            return PokemonHabitatMap(
                pokemonId = 25, pokemonName = "Pikachu", editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, Pikachu is your faithful starter Pokémon, given to you by Professor Oak in Pallet Town."
            )
        }

        if (pokemonId == 133 && (cid.contains("LET'S GO EEVEE") || cid.contains("LETS GO EEVEE"))) {
            val pin = MapLocationPin(
                id = "pallet_oak_eevee",
                areaName = "Pallet Town (Pokémon Lab)",
                coordinate = MapCoordinate(0.224f, 0.800f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter partner from Professor Oak",
                rateOrLevel = "Lv. 5",
                notes = "Encountered on Route 1 and given to you by Professor Oak as your special Partner Eevee."
            )
            return PokemonHabitatMap(
                pokemonId = 133, pokemonName = "Eevee", editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In Pokémon Let's Go Eevee, Eevee is your faithful starter partner Pokémon, received from Professor Oak in Pallet Town."
            )
        }

        // ==========================================
        // 3. GEN 2 STARTERS: Chikorita (152), Cyndaquil (155), Totodile (158)
        // ==========================================
        if (pokemonId in listOf(152, 155, 158)) {
            // A. Legends: Arceus (Cyndaquil)
            if (pokemonId == 155 && (regionName == "Hisui" || cid.contains("ARCEUS") || cid.contains("PLA"))) {
                val pin = MapLocationPin(
                    id = "prelude_laventon_cyndaquil",
                    areaName = "Prelude Beach / Jubilife Village Galaxy Hall",
                    coordinate = MapCoordinate(0.250f, 0.450f),
                    type = MapPinType.STARTER_GIFT,
                    encounterMethod = "Starter gift from Professor Laventon",
                    rateOrLevel = "Lv. 5",
                    notes = "Chosen as your partner Pokémon in Hisui. Also spawns in Space-Time Distortions in Crimson Mirelands post-game."
                )
                return PokemonHabitatMap(
                    pokemonId = 155, pokemonName = "Cyndaquil", editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hisui", mapAsset = "hisui_pla.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon Legends: Arceus, Cyndaquil is chosen as your starter Pokémon from Professor Laventon in Jubilife Village."
                )
            }

            // B. Emerald / ORAS (Hoenn Route 101 Birch Gifts)
            if (regionName == "Hoenn" || cid.contains("RUBY") || cid.contains("SAPPHIRE") || cid.contains("EMERALD")) {
                val isORAS = cid.contains("OMEGA") || cid.contains("ALPHA")
                val condition = if (isORAS) "Gift from Prof. Birch after clearing Delta Episode" else "Gift from Prof. Birch after completing Hoenn Pokédex"
                val pin = MapLocationPin(
                    id = "route101_birch_johto",
                    areaName = "Route 101 / Littleroot Town",
                    coordinate = MapCoordinate(0.230f, 0.690f),
                    type = MapPinType.SPECIAL_GIFT,
                    encounterMethod = condition,
                    rateOrLevel = "Lv. 5",
                    notes = "Professor Birch rewards your dedication by letting you pick one Johto starter from his bag on Route 101."
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hoenn", mapAsset = mapAsset,
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is gifted by Professor Birch on Route 101: $condition."
                )
            }

            // C. Native Johto (Gold, Silver, Crystal, HeartGold, SoulSilver)
            val pin = MapLocationPin(
                id = "new_bark_elm",
                areaName = "New Bark Town (Pokémon Lab)",
                coordinate = MapCoordinate(0.860f, 0.620f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Professor Elm",
                rateOrLevel = "Lv. 5",
                notes = "Selected as your starter partner Pokémon from Professor Elm in New Bark Town before visiting Mr. Pokémon."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Johto", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is received in New Bark Town as a starter gift from Professor Elm in the Pokémon Lab."
            )
        }

        // ==========================================
        // 4. GEN 3 STARTERS: Treecko (252), Torchic (255), Mudkip (258)
        // ==========================================
        if (pokemonId in listOf(252, 255, 258)) {
            // A. HeartGold / SoulSilver (Steven Stone Silph Co. Gift)
            if (cid.contains("HEART") || cid.contains("SOUL")) {
                val pin = MapLocationPin(
                    id = "silph_co_steven_stone",
                    areaName = "Silph Co. (Saffron City)",
                    coordinate = MapCoordinate(0.640f, 0.500f),
                    type = MapPinType.SPECIAL_GIFT,
                    encounterMethod = "Gift from Hoenn Champion Steven Stone",
                    rateOrLevel = "Lv. 5",
                    notes = "After defeating Champion Red atop Mt. Silver, Steven Stone visits Silph Co. in Saffron City and gives you your choice of a Hoenn starter!"
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Johto / Kanto", mapAsset = "kanto_rgby.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is gifted by Steven Stone inside Silph Co. headquarters in Saffron City!"
                )
            }

            // B. Native Hoenn (Ruby, Sapphire, Emerald, Omega Ruby, Alpha Sapphire)
            val pin = MapLocationPin(
                id = "route101_birch",
                areaName = "Route 101 / Littleroot Town",
                coordinate = MapCoordinate(0.230f, 0.690f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Professor Birch",
                rateOrLevel = "Lv. 5",
                notes = "Chosen from Professor Birch's briefcase on Route 101 to rescue him from an attacking wild Poochyena / Zigzagoon."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Hoenn", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is chosen on Route 101 from Professor Birch's bag to rescue him from a wild Pokémon."
            )
        }

        // ==========================================
        // 5. GEN 4 STARTERS: Turtwig (387), Chimchar (390), Piplup (393)
        // ==========================================
        if (pokemonId in listOf(387, 390, 393)) {
            // A. Legends: Arceus (Wild Encounters in Hisui!)
            if (regionName == "Hisui" || cid.contains("ARCEUS") || cid.contains("PLA")) {
                val pin = when (pokemonId) {
                    387 -> MapLocationPin("droning_meadow_turtwig", "Droning Meadow (Crimson Mirelands)", MapCoordinate(0.600f, 0.620f), MapPinType.WILD_ROUTE, "Wild Overworld Spawn", "Lv. 16-18", "Found roaming near the small pond in Droning Meadow.")
                    390 -> MapLocationPin("deertrack_chimchar", "Deertrack Heights / Ramanas Island", MapCoordinate(0.420f, 0.600f), MapPinType.WILD_ROUTE, "Wild Overworld Spawn", "Lv. 12-15", "Found near the cliffs of Deertrack Heights and around Ramanas Island in Obsidian Fieldlands.")
                    else -> MapLocationPin("spring_path_piplup", "Spring Path (Cobalt Coastlands)", MapCoordinate(0.780f, 0.420f), MapPinType.WILD_ROUTE, "Wild Overworld Spawn", "Lv. 15-17", "Found waddling around the lake on Spring Path north of Islespy Shore.")
                }
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hisui", mapAsset = "hisui_pla.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon Legends: Arceus, $pokemonName can be caught in the wild at ${pin.areaName}."
                )
            }

            // B. Omega Ruby / Alpha Sapphire (Route 101 Birch Post-E4 Round 2)
            if (cid.contains("OMEGA") || cid.contains("ALPHA")) {
                val pin = MapLocationPin(
                    id = "route101_birch_sinnoh",
                    areaName = "Route 101 / Littleroot Town",
                    coordinate = MapCoordinate(0.230f, 0.690f),
                    type = MapPinType.SPECIAL_GIFT,
                    encounterMethod = "Gift from Prof. Birch after entering Hall of Fame twice",
                    rateOrLevel = "Lv. 5",
                    notes = "Professor Birch gifts you your choice of a Sinnoh starter after defeating the Pokémon League a second time."
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hoenn", mapAsset = mapAsset,
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is received as a gift from Professor Birch on Route 101 after your second Hall of Fame induction."
                )
            }

            // C. Native Sinnoh (Diamond, Pearl, Platinum, BDSP)
            val pin = MapLocationPin(
                id = "lake_verity_rowan",
                areaName = "Lake Verity / Route 201",
                coordinate = MapCoordinate(0.220f, 0.720f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Professor Rowan's Briefcase",
                rateOrLevel = "Lv. 5",
                notes = "Chosen from Professor Rowan's briefcase left at Lake Verity to fend off attacking wild Starly."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Sinnoh", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is chosen at Lake Verity from Professor Rowan's briefcase to defend against wild Pokémon."
            )
        }

        // ==========================================
        // 6. GEN 5 STARTERS: Snivy (495), Tepig (498), Oshawott (501)
        // ==========================================
        if (pokemonId in listOf(495, 498, 501)) {
            // A. Legends: Arceus (Oshawott)
            if (pokemonId == 501 && (regionName == "Hisui" || cid.contains("ARCEUS") || cid.contains("PLA"))) {
                val pin = MapLocationPin(
                    id = "prelude_laventon_oshawott",
                    areaName = "Prelude Beach / Jubilife Village Galaxy Hall",
                    coordinate = MapCoordinate(0.250f, 0.450f),
                    type = MapPinType.STARTER_GIFT,
                    encounterMethod = "Starter gift from Professor Laventon",
                    rateOrLevel = "Lv. 5",
                    notes = "Chosen as your partner Pokémon in Hisui. Also spawns in Space-Time Distortions in Alabaster Icelands post-game."
                )
                return PokemonHabitatMap(
                    pokemonId = 501, pokemonName = "Oshawott", editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hisui", mapAsset = "hisui_pla.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon Legends: Arceus, Oshawott is chosen as your starter partner from Professor Laventon in Jubilife Village."
                )
            }

            // B. Black 2 / White 2 (Aspertia City Bianca Gift)
            if (cid.contains("2") || cid.contains("BLACK 2") || cid.contains("WHITE 2")) {
                val pin = MapLocationPin(
                    id = "aspertia_bianca",
                    areaName = "Aspertia City (City Lookout)",
                    coordinate = MapCoordinate(0.180f, 0.780f),
                    type = MapPinType.STARTER_GIFT,
                    encounterMethod = "Starter gift from Bianca",
                    rateOrLevel = "Lv. 5",
                    notes = "Delivered by Bianca at the scenic Aspertia City Lookout on behalf of Professor Juniper."
                )
                return PokemonHabitatMap(
                    pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Unova", mapAsset = "unova_b2w2.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon $editionDisplayName, $pokemonName is received at the Aspertia City Lookout as a starter gift from Bianca."
                )
            }

            // C. Native Black / White (Nuvema Town Juniper Gift Parcel)
            val pin = MapLocationPin(
                id = "nuvema_juniper",
                areaName = "Nuvema Town (Player's House)",
                coordinate = MapCoordinate(0.750f, 0.840f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Gift parcel from Professor Juniper",
                rateOrLevel = "Lv. 5",
                notes = "Delivered in a gift box to the Player's bedroom in Nuvema Town by Professor Juniper."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Unova", mapAsset = "unova_bw.png",
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is received in Nuvema Town inside a gift parcel from Professor Juniper."
            )
        }

        // ==========================================
        // 7. GEN 6 STARTERS: Chespin (650), Fennekin (653), Froakie (656)
        // ==========================================
        if (pokemonId in listOf(650, 653, 656) && (regionName == "Kalos" || cid.contains("X") || cid.contains("Y"))) {
            val pin = MapLocationPin(
                id = "aquacorde_sycamore",
                areaName = "Aquacorde Town",
                coordinate = MapCoordinate(0.500f, 0.870f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Gift from Tierno & Shauna",
                rateOrLevel = "Lv. 5",
                notes = "Given by your friends Tierno and Shauna on behalf of Professor Sycamore."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Kalos", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is received in Aquacorde Town as your starter partner on behalf of Professor Sycamore."
            )
        }

        // ==========================================
        // 8. GEN 7 STARTERS: Rowlet (722), Litten (725), Popplio (728)
        // ==========================================
        if (pokemonId in listOf(722, 725, 728)) {
            // A. Legends: Arceus (Rowlet)
            if (pokemonId == 722 && (regionName == "Hisui" || cid.contains("ARCEUS") || cid.contains("PLA"))) {
                val pin = MapLocationPin(
                    id = "prelude_laventon_rowlet",
                    areaName = "Prelude Beach / Jubilife Village Galaxy Hall",
                    coordinate = MapCoordinate(0.250f, 0.450f),
                    type = MapPinType.STARTER_GIFT,
                    encounterMethod = "Starter gift from Professor Laventon",
                    rateOrLevel = "Lv. 5",
                    notes = "Chosen as your partner Pokémon in Hisui. Also spawns in Space-Time Distortions in Coronet Highlands post-game."
                )
                return PokemonHabitatMap(
                    pokemonId = 722, pokemonName = "Rowlet", editionId = cartridgeId,
                    editionDisplayName = editionDisplayName, regionName = "Hisui", mapAsset = "hisui_pla.png",
                    isAreaUnknown = false, pins = listOf(pin),
                    narrationText = "In Pokémon Legends: Arceus, Rowlet is chosen as your starter partner from Professor Laventon in Jubilife Village."
                )
            }

            // B. Native Alola (Sun, Moon, Ultra Sun, Ultra Moon)
            val isUltra = cid.contains("ULTRA")
            val pin = MapLocationPin(
                id = "iki_town_hala",
                areaName = if (isUltra) "Route 1 (Mahalo Approach)" else "Iki Town",
                coordinate = MapCoordinate(0.300f, 0.650f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Kahuna Hala & Prof. Kukui",
                rateOrLevel = "Lv. 5",
                notes = "Presented by Island Kahuna Hala and Professor Kukui as your partner Pokémon."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Alola", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is chosen at ${pin.areaName} as a gift from Kahuna Hala and Professor Kukui."
            )
        }

        // ==========================================
        // 9. GEN 8 STARTERS: Grookey (810), Scorbunny (813), Sobble (816)
        // ==========================================
        if (pokemonId in listOf(810, 813, 816) && (regionName == "Galar" || cid.contains("SWORD") || cid.contains("SHIELD"))) {
            val pin = MapLocationPin(
                id = "postwick_leon",
                areaName = "Postwick (Hop's House)",
                coordinate = MapCoordinate(0.500f, 0.920f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Champion Leon",
                rateOrLevel = "Lv. 5",
                notes = "Given by Champion Leon in Postwick to you and Hop at the start of your journey."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Galar", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is received in Postwick as a starter partner from Champion Leon."
            )
        }

        // ==========================================
        // 10. GEN 9 STARTERS: Sprigatito (906), Fuecoco (909), Quaxly (912)
        // ==========================================
        if (pokemonId in listOf(906, 909, 912) && (regionName == "Paldea" || cid.contains("SCARLET") || cid.contains("VIOLET"))) {
            val pin = MapLocationPin(
                id = "cabo_poco_clavell",
                areaName = "Cabo Poco (Director's House)",
                coordinate = MapCoordinate(0.420f, 0.900f),
                type = MapPinType.STARTER_GIFT,
                encounterMethod = "Starter gift from Director Clavell",
                rateOrLevel = "Lv. 5",
                notes = "Presented by Academy Director Clavell at his private residence in Cabo Poco."
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Paldea", mapAsset = mapAsset,
                isAreaUnknown = false, pins = listOf(pin),
                narrationText = "In $editionDisplayName, $pokemonName is chosen in Cabo Poco as a starter gift from Director Clavell."
            )
        }

        return null
    }

    /**
     * Resolves iconic legendaries and static encounters (Mewtwo, Articuno, Zapdos, Moltres, Snorlax, Lapras, Eevee, Lucario, Beldum, etc.)
     */
    private fun resolveLegendaryOrStatic(
        pokemonId: Int,
        pokemonName: String,
        cartridgeId: String,
        regionName: String,
        mapAsset: String,
        editionDisplayName: String
    ): PokemonHabitatMap? {
        val cid = cartridgeId.uppercase(Locale.ROOT)

        // Eevee (#133) Gifts across games
        if (pokemonId == 133) {
            when {
                regionName == "Kanto" || cid.contains("RED") || cid.contains("BLUE") || cid.contains("YELLOW") || cid.contains("FIRE") || cid.contains("LEAF") -> {
                    val pin = MapLocationPin("eevee_celadon", "Celadon Mansion (Roof Room)", MapCoordinate(0.480f, 0.500f), MapPinType.SPECIAL_GIFT, "Poké Ball on table in rooftop room", "Lv. 25", "Enter through the back entrance of Celadon Mansion to reach the roof.")
                    return PokemonHabitatMap(133, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Eevee is found inside Celadon Mansion, left in a Poké Ball on the rooftop table.")
                }
                regionName == "Johto" || cid.contains("GOLD") || cid.contains("SILVER") || cid.contains("CRYSTAL") || cid.contains("HEART") || cid.contains("SOUL") -> {
                    val pin = MapLocationPin("eevee_goldenrod", "Goldenrod City (Bill's House)", MapCoordinate(0.320f, 0.620f), MapPinType.SPECIAL_GIFT, "Gift from Pokémaniac Bill", "Lv. 20 (GSC) / Lv. 5 (HGSS)", "Visit Bill's house in Goldenrod City after meeting him in Ecruteak City.")
                    return PokemonHabitatMap(133, pokemonName, cartridgeId, editionDisplayName, "Johto", mapAsset, false, null, listOf(pin), "Eevee is gifted by Bill at his family home in Goldenrod City.")
                }
                regionName == "Sinnoh" || cid.contains("DIAMOND") || cid.contains("PEARL") || cid.contains("PLATINUM") || cid.contains("BDSP") -> {
                    val pin = MapLocationPin("eevee_hearthome", "Hearthome City (Bebe's House)", MapCoordinate(0.520f, 0.520f), MapPinType.SPECIAL_GIFT, "Gift from Bebe", "Lv. 20", "Gifted by PC storage creator Bebe in Hearthome City.")
                    return PokemonHabitatMap(133, pokemonName, cartridgeId, editionDisplayName, "Sinnoh", mapAsset, false, null, listOf(pin), "Eevee is gifted by Bebe in Hearthome City next to the Pokémon Center.")
                }
            }
        }

        // Lapras (#131) Gifts & Statics
        if (pokemonId == 131) {
            when {
                regionName == "Kanto" || cid.contains("RED") || cid.contains("BLUE") || cid.contains("FIRE") || cid.contains("LEAF") -> {
                    val pin = MapLocationPin("lapras_silph", "Silph Co. (7F)", MapCoordinate(0.640f, 0.500f), MapPinType.SPECIAL_GIFT, "Gift from Silph Co. employee", "Lv. 15 (Gen 1) / Lv. 25 (FRLG)", "Given by an employee fleeing Team Rocket on the 7th floor of Silph Co.")
                    return PokemonHabitatMap(131, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Lapras is received as a gift inside Silph Co. headquarters in Saffron City.")
                }
                regionName == "Johto" || cid.contains("GOLD") || cid.contains("SILVER") || cid.contains("HEART") || cid.contains("SOUL") -> {
                    val pin = MapLocationPin("lapras_union_cave", "Union Cave (B2F Deep Water)", MapCoordinate(0.480f, 0.720f), MapPinType.LEGENDARY_STATIC, "Friday Static Encounter (Surf)", "Lv. 20", "Appears every Friday in the subterranean waters of Union Cave B2F.")
                    return PokemonHabitatMap(131, pokemonName, cartridgeId, editionDisplayName, "Johto", mapAsset, false, null, listOf(pin), "Lapras appears as a static encounter in the deepest waters of Union Cave every Friday.")
                }
                regionName == "Kalos" || cid.contains("X") || cid.contains("Y") -> {
                    val pin = MapLocationPin("lapras_kalos_r12", "Route 12 (Fourrage Road)", MapCoordinate(0.580f, 0.380f), MapPinType.SPECIAL_GIFT, "Gift from Pokémon Breeder", "Lv. 30", "Gifted by a Pokémon Breeder near the beach entrance of Route 12.")
                    return PokemonHabitatMap(131, pokemonName, cartridgeId, editionDisplayName, "Kalos", mapAsset, false, null, listOf(pin), "Lapras is received as a gift from a Pokémon Breeder on Route 12 in Kalos.")
                }
            }
        }

        // Snorlax (#143)
        if (pokemonId == 143) {
            when {
                regionName == "Kanto" || cid.contains("RED") || cid.contains("BLUE") || cid.contains("YELLOW") || cid.contains("FIRE") || cid.contains("LEAF") -> {
                    val pins = listOf(
                        MapLocationPin("snorlax_route12", "Route 12 (Silence Bridge)", MapCoordinate(0.810f, 0.650f), MapPinType.SPECIAL_GIFT, "Sleeping obstacle (Poké Flute)", "Lv. 30", "Awakened by playing the Poké Flute."),
                        MapLocationPin("snorlax_route16", "Route 16 (Cycling Road Gate)", MapCoordinate(0.360f, 0.500f), MapPinType.SPECIAL_GIFT, "Sleeping obstacle (Poké Flute)", "Lv. 30", "Blocks western passage out of Celadon City.")
                    )
                    return PokemonHabitatMap(143, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, pins, "Snorlax is found asleep on Route 12 and Route 16, requiring a Poké Flute to awaken and battle.")
                }
                regionName == "Kalos" || cid.contains("X") || cid.contains("Y") -> {
                    val pin = MapLocationPin("snorlax_kalos_r7", "Route 7 (Rivière Walk Bridge)", MapCoordinate(0.420f, 0.600f), MapPinType.SPECIAL_GIFT, "Sleeping obstacle (Poké Flute)", "Lv. 15", "Blocks the river bridge on Route 7 until awakened by the Poké Flute from Parfum Palace.")
                    return PokemonHabitatMap(143, pokemonName, cartridgeId, editionDisplayName, "Kalos", mapAsset, false, null, listOf(pin), "Snorlax is asleep on the Route 7 bridge, requiring the Poké Flute to awaken and battle.")
                }
            }
        }

        // Lucario (#448) & Riolu (#447)
        if (pokemonId in listOf(447, 448)) {
            if (regionName == "Kalos" || cid.contains("X") || cid.contains("Y")) {
                val pin = MapLocationPin("lucario_shalour", "Tower of Mastery (Shalour City)", MapCoordinate(0.280f, 0.380f), MapPinType.SPECIAL_GIFT, "Gift from Successor Korrina", "Lv. 32 (Holding Lucarionite)", "Joins your team after your Mega Evolution battle against Korrina at the summit of the Tower of Mastery.")
                return PokemonHabitatMap(pokemonId, pokemonName, cartridgeId, editionDisplayName, "Kalos", mapAsset, false, null, listOf(pin), "Lucario is gifted by Korrina atop the Tower of Mastery in Shalour City, holding Lucarionite!")
            }
            if (regionName == "Sinnoh" || cid.contains("DIAMOND") || cid.contains("PEARL") || cid.contains("PLATINUM") || cid.contains("BDSP")) {
                val pin = MapLocationPin("riolu_iron_island", "Iron Island Caverns", MapCoordinate(0.300f, 0.220f), MapPinType.SPECIAL_GIFT, "Egg Gift from Riley", "Egg (Hatches into Riolu Lv. 1)", "Rewarded by aura guardian Riley after teaming up to clear Team Galactic from Iron Island.")
                return PokemonHabitatMap(pokemonId, pokemonName, cartridgeId, editionDisplayName, "Sinnoh", mapAsset, false, null, listOf(pin), "Riolu is received as an egg from Riley deep inside the caverns of Iron Island.")
            }
        }

        // Beldum (#374) Hoenn Gift
        if (pokemonId == 374 && (regionName == "Hoenn" || cid.contains("RUBY") || cid.contains("SAPPHIRE") || cid.contains("EMERALD"))) {
            val pin = MapLocationPin("beldum_mossdeep", "Mossdeep City (Steven's House)", MapCoordinate(0.850f, 0.450f), MapPinType.SPECIAL_GIFT, "Poké Ball left on table", "Lv. 5", "Left on the table inside Steven's house with a letter after you enter the Hall of Fame.")
            return PokemonHabitatMap(374, pokemonName, cartridgeId, editionDisplayName, "Hoenn", mapAsset, false, null, listOf(pin), "Beldum is found inside Steven's house in Mossdeep City, left as a post-game gift on the table.")
        }

        // Kanto Legendaries
        if (regionName == "Kanto") {
            when (pokemonId) {
                150 -> {
                    val pin = MapLocationPin("cerulean_cave", "Cerulean Cave (B1F)", MapCoordinate(0.590f, 0.340f), MapPinType.LEGENDARY_STATIC, "Static Legendary encounter", "Lv. 70", "Guarded in the deepest recesses of Cerulean Cave. Accessible only after defeating the Pokémon League.")
                    return PokemonHabitatMap(150, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Mewtwo is located in the deepest chamber of Cerulean Cave, encounterable at Level 70 after entering the Hall of Fame.")
                }
                145 -> {
                    val pin = MapLocationPin("power_plant_zapdos", "Kanto Power Plant", MapCoordinate(0.748f, 0.400f), MapPinType.LEGENDARY_STATIC, "Static Legendary encounter", "Lv. 50", "Perched at the exit of the abandoned Kanto Power Plant on Route 10.")
                    return PokemonHabitatMap(145, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Zapdos is perched at the abandoned Power Plant on Route 10, awaiting a static encounter at Level 50.")
                }
                144 -> {
                    val pin = MapLocationPin("seafoam_articuno", "Seafoam Islands (B4F)", MapCoordinate(0.460f, 0.880f), MapPinType.LEGENDARY_STATIC, "Static Legendary encounter", "Lv. 50", "Requires solving the boulder-pushing current puzzles to reach the bottom cavern.")
                    return PokemonHabitatMap(144, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Articuno resides deep inside Seafoam Islands on floor B4F, encounterable at Level 50.")
                }
                146 -> {
                    val isFRLG = cid.contains("FIRE") || cid.contains("LEAF")
                    val pin = if (isFRLG) {
                        MapLocationPin("mt_ember_moltres", "Mt. Ember Summit (One Island)", MapCoordinate(0.880f, 0.880f), MapPinType.LEGENDARY_STATIC, "Static Legendary encounter", "Lv. 50", "Located atop the peak of Mt. Ember in the Sevii Islands.")
                    } else {
                        MapLocationPin("victory_road_moltres", "Victory Road (2F)", MapCoordinate(0.150f, 0.380f), MapPinType.LEGENDARY_STATIC, "Static Legendary encounter", "Lv. 50", "Nestled in the caverns of Victory Road before the Indigo Plateau.")
                    }
                    return PokemonHabitatMap(146, pokemonName, cartridgeId, editionDisplayName, "Kanto", mapAsset, false, null, listOf(pin), "Moltres is encountered as a static Legendary at Level 50 in ${pin.areaName}.")
                }
            }
        }

        return null
    }

    /**
     * Resolves wild encounter routes for common & iconic species across regions.
     */
    private fun resolveWildEncounters(
        pokemonId: Int,
        pokemonName: String,
        cartridgeId: String,
        regionName: String,
        mapAsset: String,
        editionDisplayName: String
    ): PokemonHabitatMap? {
        // Pokémon GO Biomes
        if (regionName == "Pokémon GO") {
            val pins = listOf(
                MapLocationPin("go_wild", "Wild Biome Spawns", MapCoordinate(0.500f, 0.500f), MapPinType.POKEMON_GO_BIOME, "Wild World Map", "Standard", "Spawns influenced by local weather & active seasonal events."),
                MapLocationPin("go_eggs", "Egg Hatches", MapCoordinate(0.250f, 0.350f), MapPinType.POKEMON_GO_BIOME, "2km / 5km / 10km Eggs", "Egg Hatch", "Obtained from Pokéstops and Gyms."),
                MapLocationPin("go_research", "Field Research & Raids", MapCoordinate(0.750f, 0.650f), MapPinType.POKEMON_GO_BIOME, "Field Tasks & Raid Battles", "Event Encounter", "Special Research task encounter reward.")
            )
            return PokemonHabitatMap(
                pokemonId = pokemonId, pokemonName = pokemonName, editionId = cartridgeId,
                editionDisplayName = editionDisplayName, regionName = "Pokémon GO", mapAsset = mapAsset,
                isAreaUnknown = false, pins = pins,
                narrationText = "In Pokémon GO, $pokemonName is encountered in the wild across biomes, through egg hatches, and in Field Research tasks."
            )
        }

        // Kanto Wild Encounters
        if (regionName == "Kanto") {
            when (pokemonId) {
                // Pikachu (#25) in Red/Blue/FireRed
                25 -> {
                    val isFRLG = cartridgeId.uppercase().contains("FIRE") || cartridgeId.uppercase().contains("LEAF")
                    val vForestCoord = if (isFRLG) MapCoordinate(0.150f, 0.500f) else MapCoordinate(0.224f, 0.489f)
                    val powerPlantCoord = if (isFRLG) MapCoordinate(0.800f, 0.400f) else MapCoordinate(0.748f, 0.400f)

                    val pins = listOf(
                        MapLocationPin("viridian_forest_pika", "Viridian Forest", vForestCoord, MapPinType.WILD_ROUTE, "Tall Grass", "Rare (5% spawn · Lv. 3-5)"),
                        MapLocationPin("power_plant_pika", "Power Plant", powerPlantCoord, MapPinType.WILD_ROUTE, "Wild Building Area", "Uncommon (20-25% · Lv. 20-24)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 25, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "In Kanto, Pikachu is commonly found in Viridian Forest at low levels, and inside the abandoned Power Plant."
                    )
                }
                // Pidgey (#16)
                16 -> {
                    val pins = listOf(
                        MapLocationPin("pidgey_r1", "Route 1", MapCoordinate(0.250f, 0.690f), MapPinType.WILD_ROUTE, "Tall Grass", "Very Common (50% · Lv. 2-5)"),
                        MapLocationPin("pidgey_r2", "Route 2", MapCoordinate(0.250f, 0.490f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (40% · Lv. 3-5)"),
                        MapLocationPin("pidgey_r3", "Route 3", MapCoordinate(0.360f, 0.380f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (30% · Lv. 6-8)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 16, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Pidgey is abundantly found in tall grass across Routes 1, 2, and 3 throughout Kanto."
                    )
                }
                // Rattata (#19)
                19 -> {
                    val pins = listOf(
                        MapLocationPin("rat_r1", "Route 1", MapCoordinate(0.250f, 0.690f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (50% · Lv. 2-4)"),
                        MapLocationPin("rat_r2", "Route 2", MapCoordinate(0.250f, 0.490f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (45% · Lv. 3-5)"),
                        MapLocationPin("rat_r22", "Route 22", MapCoordinate(0.150f, 0.600f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (50% · Lv. 3-5)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 19, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Rattata is found commonly in tall grass across Route 1, Route 2, and Route 22."
                    )
                }
                // Caterpie (#10)
                10 -> {
                    val pins = listOf(
                        MapLocationPin("cat_r2", "Route 2", MapCoordinate(0.250f, 0.490f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (15-45% · Lv. 3-5)"),
                        MapLocationPin("cat_vf", "Viridian Forest", MapCoordinate(0.224f, 0.489f), MapPinType.WILD_ROUTE, "Tall Grass", "Abundant (50% · Lv. 3-6)"),
                        MapLocationPin("cat_r24", "Route 24 & 25", MapCoordinate(0.640f, 0.250f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (20% · Lv. 7-10)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 10, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Caterpie is found abundantly in Viridian Forest and along Route 2."
                    )
                }
                // Weedle (#13)
                13 -> {
                    val pins = listOf(
                        MapLocationPin("weed_r2", "Route 2", MapCoordinate(0.250f, 0.490f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (15-45% · Lv. 3-5)"),
                        MapLocationPin("weed_vf", "Viridian Forest", MapCoordinate(0.224f, 0.489f), MapPinType.WILD_ROUTE, "Tall Grass", "Abundant (50% · Lv. 3-6)"),
                        MapLocationPin("weed_r24", "Route 24 & 25", MapCoordinate(0.640f, 0.250f), MapPinType.WILD_ROUTE, "Tall Grass", "Common (20% · Lv. 7-10)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 13, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Weedle is found in tall grass across Viridian Forest and Route 2."
                    )
                }
                // Zubat (#41)
                41 -> {
                    val pins = listOf(
                        MapLocationPin("zubat_mt_moon", "Mt. Moon", MapCoordinate(0.470f, 0.380f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Abundant (79% · Lv. 7-12)"),
                        MapLocationPin("zubat_rock_tunnel", "Rock Tunnel", MapCoordinate(0.810f, 0.380f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Common (50% · Lv. 15-18)"),
                        MapLocationPin("zubat_seafoam", "Seafoam Islands", MapCoordinate(0.460f, 0.880f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Common (40% · Lv. 26-30)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 41, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Zubat is ubiquitous in every dark cavern in Kanto, including Mt. Moon and Rock Tunnel."
                    )
                }
                // Geodude (#74)
                74 -> {
                    val pins = listOf(
                        MapLocationPin("geo_mt_moon", "Mt. Moon", MapCoordinate(0.470f, 0.380f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Common (20-30% · Lv. 8-10)"),
                        MapLocationPin("geo_rock_tunnel", "Rock Tunnel", MapCoordinate(0.810f, 0.380f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Abundant (35% · Lv. 16-18)"),
                        MapLocationPin("geo_victory", "Victory Road", MapCoordinate(0.150f, 0.380f), MapPinType.CAVE_DUNGEON, "Cave Encounter", "Common (20% · Lv. 36-40)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 74, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Geodude is commonly encountered in mountainous caverns including Mt. Moon and Rock Tunnel."
                    )
                }
                // Magikarp (#129)
                129 -> {
                    val pins = listOf(
                        MapLocationPin("karp_pallet", "Pallet Town Waters", MapCoordinate(0.250f, 0.770f), MapPinType.FISHING_SPOT, "Old Rod / Good Rod", "100% with Old Rod"),
                        MapLocationPin("karp_vermilion", "Vermilion City Port", MapCoordinate(0.640f, 0.610f), MapPinType.WATER_SURF, "Surfing / Fishing", "Common (70%)"),
                        MapLocationPin("karp_safari", "Safari Zone Waters", MapCoordinate(0.580f, 0.760f), MapPinType.FISHING_SPOT, "Super Rod", "Common (50%)")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 129, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Magikarp can be reeled in from virtually any body of water across Kanto using an Old Rod."
                    )
                }
                // Abra (#63)
                63 -> {
                    val pins = listOf(
                        MapLocationPin("abra_r24", "Route 24 & 25", MapCoordinate(0.640f, 0.250f), MapPinType.WILD_ROUTE, "Tall Grass", "Uncommon (15% · Lv. 8-12)"),
                        MapLocationPin("abra_game_corner", "Rocket Game Corner (Celadon)", MapCoordinate(0.480f, 0.500f), MapPinType.SPECIAL_GIFT, "Prize exchange (180 Coins)", "Lv. 9", "Teleports immediately if encountered in wild.")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 63, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Abra is found on Route 24 and 25, or purchased as a prize at the Rocket Game Corner in Celadon City."
                    )
                }
                // Gastly (#92)
                92 -> {
                    val pin = MapLocationPin(
                        id = "gastly_tower",
                        areaName = "Pokémon Tower (Lavender Town)",
                        coordinate = MapCoordinate(0.810f, 0.500f),
                        type = MapPinType.CAVE_DUNGEON,
                        encounterMethod = "Haunted Tower (3F - 7F)",
                        rateOrLevel = "Abundant (80-90% · Lv. 18-24)",
                        notes = "Requires the Silph Scope to unmask spirits and capture."
                    )
                    return PokemonHabitatMap(
                        pokemonId = 92, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = listOf(pin),
                        narrationText = "Gastly haunts the Pokémon Tower in Lavender Town, requiring a Silph Scope to battle."
                    )
                }
                // Onix (#95)
                95 -> {
                    val pin = MapLocationPin(
                        id = "onix_rock_tunnel",
                        areaName = "Rock Tunnel (B1F)",
                        coordinate = MapCoordinate(0.810f, 0.380f),
                        type = MapPinType.CAVE_DUNGEON,
                        encounterMethod = "Cave Encounter",
                        rateOrLevel = "Uncommon (10-15% · Lv. 15-17)",
                        notes = "Also wielded by Brock at the Pewter City Gym."
                    )
                    return PokemonHabitatMap(
                        pokemonId = 95, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = listOf(pin),
                        narrationText = "Onix is encountered in the lower level of Rock Tunnel and on Victory Road."
                    )
                }
                // Dratini (#147)
                147 -> {
                    val pins = listOf(
                        MapLocationPin("dratini_safari", "Safari Zone (Area 1-3)", MapCoordinate(0.580f, 0.760f), MapPinType.FISHING_SPOT, "Super Rod fishing", "Rare (15-25% · Lv. 15)"),
                        MapLocationPin("dratini_game_corner", "Rocket Game Corner", MapCoordinate(0.480f, 0.500f), MapPinType.SPECIAL_GIFT, "Prize exchange (2800 / 4600 Coins)", "Lv. 18-24")
                    )
                    return PokemonHabitatMap(
                        pokemonId = 147, pokemonName = pokemonName, editionId = cartridgeId,
                        editionDisplayName = editionDisplayName, regionName = "Kanto", mapAsset = mapAsset,
                        isAreaUnknown = false, pins = pins,
                        narrationText = "Dratini can be reeled in with a Super Rod in the Safari Zone, or obtained at the Rocket Game Corner."
                    )
                }
            }
        }

        return null
    }

    /**
     * Resolves Evolutions, Formes, and Non-Native species (Classic Pokédex "Area Unknown" state)
     */
    private fun resolveEvolutionOrAreaUnknown(
        pokemonId: Int,
        pokemonName: String,
        cartridgeId: String,
        regionName: String,
        mapAsset: String,
        editionDisplayName: String
    ): PokemonHabitatMap {
        val (reason, audio) = when {
            // Stage 1 & 2 Starters
            pokemonId in listOf(2, 3) -> Pair("Evolves from Bulbasaur (Lv. 16 / Lv. 32). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Bulbasaur and is not found in the wild in $regionName.")
            pokemonId in listOf(5, 6) -> Pair("Evolves from Charmander (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Charmander and is not found in the wild in $regionName.")
            pokemonId in listOf(8, 9) -> Pair("Evolves from Squirtle (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Squirtle and is not found in the wild in $regionName.")
            pokemonId in listOf(153, 154) -> Pair("Evolves from Chikorita (Lv. 16 / Lv. 32). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Chikorita and is not found in the wild in $regionName.")
            pokemonId in listOf(156, 157) -> Pair("Evolves from Cyndaquil (Lv. 14 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Cyndaquil and is not found in the wild in $regionName.")
            pokemonId in listOf(159, 160) -> Pair("Evolves from Totodile (Lv. 18 / Lv. 30). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Totodile and is not found in the wild in $regionName.")
            pokemonId in listOf(253, 254) -> Pair("Evolves from Treecko (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Treecko and is not found in the wild in $regionName.")
            pokemonId in listOf(256, 257) -> Pair("Evolves from Torchic (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Torchic and is not found in the wild in $regionName.")
            pokemonId in listOf(259, 260) -> Pair("Evolves from Mudkip (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Mudkip and is not found in the wild in $regionName.")
            pokemonId in listOf(388, 389) -> Pair("Evolves from Turtwig (Lv. 18 / Lv. 32). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Turtwig and is not found in the wild in $regionName.")
            pokemonId in listOf(391, 392) -> Pair("Evolves from Chimchar (Lv. 14 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Chimchar and is not found in the wild in $regionName.")
            pokemonId in listOf(394, 395) -> Pair("Evolves from Piplup (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Piplup and is not found in the wild in $regionName.")
            pokemonId in listOf(496, 497) -> Pair("Evolves from Snivy (Lv. 17 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Snivy and is not found in the wild in $regionName.")
            pokemonId in listOf(499, 500) -> Pair("Evolves from Tepig (Lv. 17 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Tepig and is not found in the wild in $regionName.")
            pokemonId in listOf(502, 503) -> Pair("Evolves from Oshawott (Lv. 17 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Oshawott and is not found in the wild in $regionName.")
            pokemonId in listOf(651, 652) -> Pair("Evolves from Chespin (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Chespin and is not found in the wild in $regionName.")
            pokemonId in listOf(654, 655) -> Pair("Evolves from Fennekin (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Fennekin and is not found in the wild in $regionName.")
            pokemonId in listOf(657, 658) -> Pair("Evolves from Froakie (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Froakie and is not found in the wild in $regionName.")
            pokemonId in listOf(811, 812) -> Pair("Evolves from Grookey (Lv. 16 / Lv. 35). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Grookey and is not found in the wild in $regionName.")
            pokemonId in listOf(814, 815) -> Pair("Evolves from Scorbunny (Lv. 16 / Lv. 35). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Scorbunny and is not found in the wild in $regionName.")
            pokemonId in listOf(817, 818) -> Pair("Evolves from Sobble (Lv. 16 / Lv. 35). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Sobble and is not found in the wild in $regionName.")
            pokemonId in listOf(907, 908) -> Pair("Evolves from Sprigatito (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Sprigatito and is not found in the wild in $regionName.")
            pokemonId in listOf(910, 911) -> Pair("Evolves from Fuecoco (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Fuecoco and is not found in the wild in $regionName.")
            pokemonId in listOf(913, 914) -> Pair("Evolves from Quaxly (Lv. 16 / Lv. 36). Starter evolution line not found in the wild in $regionName.", "$pokemonName evolves from Quaxly and is not found in the wild in $regionName.")

            // Iconic Evolutions
            pokemonId == 26 -> Pair("Evolves from Pikachu with a Thunder Stone. Raichu is not found wild in $regionName in $editionDisplayName.", "Raichu evolves from Pikachu with a Thunder Stone and is not found wild in $regionName.")
            pokemonId == 130 -> Pair("Evolves from Magikarp at Level 20. Rarely encountered wild via Super Rod.", "Gyarados evolves from Magikarp at Level 20.")
            pokemonId in listOf(64, 65) -> Pair("Evolves from Abra (Lv. 16) -> Kadabra evolves via Trade to Alakazam. Not found wild in Kanto.", "$pokemonName evolves from Abra and is not found wild in Kanto.")
            pokemonId in listOf(93, 94) -> Pair("Evolves from Gastly (Lv. 25) -> Haunter evolves via Trade to Gengar.", "$pokemonName evolves from Gastly and Haunter.")
            pokemonId in listOf(148, 149) -> Pair("Evolves from Dratini (Lv. 30 -> Lv. 55). Dragonite line not found wild in Kanto.", "$pokemonName evolves from Dratini and is not found wild in Kanto.")

            else -> Pair(
                "Area Unknown in $regionName. Not encountered natively in the wild in $editionDisplayName. Must be traded or transferred.",
                "Area Unknown in $regionName. $pokemonName is not found natively in the wild in $editionDisplayName."
            )
        }

        return PokemonHabitatMap(
            pokemonId = pokemonId,
            pokemonName = pokemonName,
            editionId = cartridgeId,
            editionDisplayName = editionDisplayName,
            regionName = regionName,
            mapAsset = mapAsset,
            isAreaUnknown = true,
            unknownReason = reason,
            pins = emptyList(),
            narrationText = audio
        )
    }
}
