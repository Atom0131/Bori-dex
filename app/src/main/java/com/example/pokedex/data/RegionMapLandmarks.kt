package com.example.pokedex.data

import androidx.compose.ui.graphics.Color

enum class MapLandmarkType(
    val displayName: String,
    val emoji: String,
    val itemSprite: String,
    val badgeColor: Color,
    val borderAlpha: Float
) {
    TOWN("Towns", "🏠", "poke-ball.png", Color(0xFFFFCC00), 0.90f),
    CITY("Cities", "🏛️", "amulet-coin.png", Color(0xFF00FFCC), 0.95f),
    ROUTE("Routes", "🛣️", "running-shoes.png", Color(0xFF4FC3F7), 0.75f),
    POI("Points of Interest", "📍", "old-sea-map.png", Color(0xFFFF7043), 0.85f),
    ENTRANCE("Entrances & Gates", "🚪", "escape-rope.png", Color(0xFFA78BFA), 0.85f),
    SECRET("Secrets & Ruins", "🔍", "dowsing-machine.png", Color(0xFFF472B6), 0.90f),
    PUZZLE("Puzzles & Trials", "🧩", "silph-scope.png", Color(0xFF34D399), 0.85f);

    companion object {
        val LANDMARK: MapLandmarkType get() = POI
    }
}

data class MapLandmark(
    val id: String,
    val name: String,
    val shortName: String,
    val xNorm: Float,
    val yNorm: Float,
    val type: MapLandmarkType,
    val isMajor: Boolean = true,
    val tagline: String? = null,
    val description: String? = null,
    val keyLocations: List<String> = emptyList(),
    val connectedAreas: List<String> = emptyList(),
    val guideAdvice: String? = null,
    val gymInfo: String? = null
)

data class LandmarkGuideDetails(
    val tagline: String,
    val description: String,
    val keyLocations: List<String> = emptyList(),
    val connectedAreas: List<String> = emptyList(),
    val guideAdvice: String? = null,
    val gymInfo: String? = null
)

object RegionMapLandmarksRepository {

    fun getLandmarksForMap(mapAssetOrEdition: String): List<MapLandmark> {
        val clean = mapAssetOrEdition.trim().lowercase().substringAfterLast("/").removeSuffix(".png")
        return when {
            clean.startsWith("kanto_rgby") || clean == "red" || clean == "blue" || clean == "yellow" -> KANTO_GB_LANDMARKS
            clean.startsWith("kanto_frlg") || clean.contains("firered") || clean.contains("leafgreen") -> KANTO_GBA_LANDMARKS
            clean.startsWith("johto") || clean.contains("gold") || clean.contains("silver") || clean.contains("crystal") || clean.contains("heartgold") || clean.contains("soulsilver") -> JOHTO_LANDMARKS
            clean.startsWith("hoenn") || clean.contains("ruby") || clean.contains("sapphire") || clean.contains("emerald") || clean.contains("omega") || clean.contains("alpha") -> HOENN_LANDMARKS
            clean.startsWith("sinnoh") || clean.contains("diamond") || clean.contains("pearl") || clean.contains("platinum") || clean.contains("brilliant") || clean.contains("shining") -> SINNOH_LANDMARKS
            clean.startsWith("unova") || clean.contains("black") || clean.contains("white") -> UNOVA_LANDMARKS
            clean.startsWith("kalos") || clean.contains("kalos") || clean == "x" || clean == "y" -> KALOS_LANDMARKS
            clean.startsWith("alola") || clean.contains("sun") || clean.contains("moon") -> ALOLA_LANDMARKS
            clean.startsWith("galar") || clean.contains("sword") || clean.contains("shield") -> GALAR_LANDMARKS
            clean.startsWith("hisui") || clean.contains("legends") || clean.contains("arceus") -> HISUI_LANDMARKS
            clean.startsWith("paldea") || clean.contains("scarlet") || clean.contains("violet") -> PALDEA_LANDMARKS
            clean.contains("kanto") -> KANTO_GB_LANDMARKS
            else -> KANTO_GB_LANDMARKS
        }
    }

    fun findLandmarkById(id: String, mapAssetOrEdition: String): MapLandmark? {
        val landmarks = getLandmarksForMap(mapAssetOrEdition)
        return landmarks.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    fun findLandmarkByName(query: String, mapAssetOrEdition: String): MapLandmark? {
        val landmarks = getLandmarksForMap(mapAssetOrEdition)
        val qNorm = query.lowercase().trim()
        return landmarks.firstOrNull {
            it.id.equals(qNorm, ignoreCase = true) ||
            it.name.lowercase().contains(qNorm) ||
            it.shortName.lowercase().equals(qNorm, ignoreCase = true)
        }
    }

    fun getAllLandmarks(): List<MapLandmark> {
        return KANTO_GB_LANDMARKS + JOHTO_LANDMARKS + HOENN_LANDMARKS + SINNOH_LANDMARKS +
               UNOVA_LANDMARKS + KALOS_LANDMARKS + ALOLA_LANDMARKS + GALAR_LANDMARKS +
               HISUI_LANDMARKS + PALDEA_LANDMARKS
    }

    private fun kantoLandmark(
        id: String,
        name: String,
        shortName: String,
        xNorm: Float,
        yNorm: Float,
        type: MapLandmarkType,
        isMajor: Boolean = true
    ): MapLandmark {
        val details = KANTO_LANDMARK_DETAILS[id]
        return MapLandmark(
            id = id,
            name = name,
            shortName = shortName,
            xNorm = xNorm,
            yNorm = yNorm,
            type = type,
            isMajor = isMajor,
            tagline = details?.tagline,
            description = details?.description,
            keyLocations = details?.keyLocations ?: emptyList(),
            connectedAreas = details?.connectedAreas ?: emptyList(),
            guideAdvice = details?.guideAdvice,
            gymInfo = details?.gymInfo
        )
    }

    private fun johtoLandmark(
        id: String,
        name: String,
        shortName: String,
        xNorm: Float,
        yNorm: Float,
        type: MapLandmarkType,
        isMajor: Boolean = true
    ): MapLandmark {
        val details = JOHTO_LANDMARK_DETAILS[id]
        return MapLandmark(
            id = id,
            name = name,
            shortName = shortName,
            xNorm = xNorm,
            yNorm = yNorm,
            type = type,
            isMajor = isMajor,
            tagline = details?.tagline,
            description = details?.description,
            keyLocations = details?.keyLocations ?: emptyList(),
            connectedAreas = details?.connectedAreas ?: emptyList(),
            guideAdvice = details?.guideAdvice,
            gymInfo = details?.gymInfo
        )
    }

    private fun hoennLandmark(
        id: String,
        name: String,
        shortName: String,
        xNorm: Float,
        yNorm: Float,
        type: MapLandmarkType,
        isMajor: Boolean = true
    ): MapLandmark {
        val details = HOENN_LANDMARK_DETAILS[id]
        return MapLandmark(
            id = id,
            name = name,
            shortName = shortName,
            xNorm = xNorm,
            yNorm = yNorm,
            type = type,
            isMajor = isMajor,
            tagline = details?.tagline,
            description = details?.description,
            keyLocations = details?.keyLocations ?: emptyList(),
            connectedAreas = details?.connectedAreas ?: emptyList(),
            guideAdvice = details?.guideAdvice,
            gymInfo = details?.gymInfo
        )
    }

    private fun paldeaLandmark(
        id: String,
        name: String,
        shortName: String,
        xNorm: Float,
        yNorm: Float,
        type: MapLandmarkType,
        isMajor: Boolean = true
    ): MapLandmark {
        val details = PALDEA_LANDMARK_DETAILS[id]
        return MapLandmark(
            id = id,
            name = name,
            shortName = shortName,
            xNorm = xNorm,
            yNorm = yNorm,
            type = type,
            isMajor = isMajor,
            tagline = details?.tagline,
            description = details?.description,
            keyLocations = details?.keyLocations ?: emptyList(),
            connectedAreas = details?.connectedAreas ?: emptyList(),
            guideAdvice = details?.guideAdvice,
            gymInfo = details?.gymInfo
        )
    }

    // -------------------------------------------------------------
    // KANTO COMPREHENSIVE STRATEGY GUIDE DETAILS
    // -------------------------------------------------------------
    val KANTO_LANDMARK_DETAILS: Map<String, LandmarkGuideDetails> = mapOf(
        "pallet" to LandmarkGuideDetails(
            tagline = "Shades of your journey await!",
            description = "A quiet, tranquil town nestled along the southwestern coast of Kanto. Known for its gentle sea breezes and as the birthplace of legendary Pokémon Trainers.",
            keyLocations = listOf(
                "🔬 Prof. Oak's Research Lab: Starter Pokémon selection (Bulbasaur, Charmander, Squirtle) & National Pokédex",
                "🏠 Player's Home: Starting point with PC item storage",
                "🏡 Rival's Home: Daisy Oak awards the Town Map after delivering Oak's Parcel",
                "🌊 Southern Waterway: Shoreline embarking onto Sea Route 21"
            ),
            connectedAreas = listOf("Route 1 (North)", "Route 21 (South via Surf)"),
            guideAdvice = "Deliver Oak's Parcel from Viridian Mart back to Professor Oak to receive your Pokédex, then talk to Daisy in your rival's house to receive the Town Map."
        ),
        "viridian" to LandmarkGuideDetails(
            tagline = "The City of Evergreen!",
            description = "A bustling gateway city wrapped in dense green woods. The first major urban hub a trainer visits, featuring a mysterious Gym whose leader is often absent.",
            gymInfo = "Leader: Giovanni • Specialty: Ground • Earth Badge (Lv. 42-50)",
            keyLocations = listOf(
                "🏟️ Viridian Gym: Ground-type Gym led by Team Rocket Boss Giovanni (Earth Badge)",
                "🏪 Viridian Poké Mart: Collect Oak's Parcel for Prof. Oak; stocks early Poké Balls & Potions",
                "🏥 Pokémon Center: Free party healing & PC networking",
                "👴 Old Man Catching Tutorial: Teaches wild encounter capture mechanics",
                "🏡 Trainer House: Battle simulated trainer teams"
            ),
            connectedAreas = listOf("Route 1 (South)", "Route 2 (North)", "Route 22 (West toward Pokémon League)"),
            guideAdvice = "The Viridian Gym is locked until you collect all other 7 Kanto Badges! Retrieve Oak's Parcel from the Mart first to progress your quest."
        ),
        "pewter" to LandmarkGuideDetails(
            tagline = "A Stone-Gray City Nestled at the Foot of Mt. Moon!",
            description = "An ancient quarry city carved out of rugged limestone. Famous for its prehistoric Museum of Science and its Rock-type Gym.",
            gymInfo = "Leader: Brock • Specialty: Rock • Boulder Badge (Lv. 12-14)",
            keyLocations = listOf(
                "🏟️ Pewter Gym: Rock-type Gym led by Brock (Boulder Badge & TM34 Bide/Rock Tomb)",
                "🏛️ Pewter Museum of Science: Displays Aerodactyl and Kabutops fossils and Space Shuttle (50 Poké dollars entry)",
                "🪨 Old Amber Laboratory: Revive Aerodactyl from Old Amber via back entrance using Cut"
            ),
            connectedAreas = listOf("Route 2 (South)", "Route 3 (East toward Mt. Moon)"),
            guideAdvice = "Water and Grass moves deal quad 4x damage to Brock's Geodude and Onix. If you chose Charmander, use Metal Claw (FRLG), Butterfree with Confusion, or catch a Mankey on Route 22."
        ),
        "cerulean" to LandmarkGuideDetails(
            tagline = "A Mysterious City Surrounded by Flowing Waters!",
            description = "A vibrant city bounded by scenic rivers and coastal cliffs. Known for its seaside atmosphere, the Water-type Gym, and the notorious Bicycle Shop.",
            gymInfo = "Leader: Misty • Specialty: Water • Cascade Badge (Lv. 18-21)",
            keyLocations = listOf(
                "🏟️ Cerulean Gym: Water-type Gym led by Misty (Cascade Badge & TM11 BubbleBeam/Water Pulse)",
                "🚲 Miracle Cycle Shop: Sells the Bicycle for 1,000,000 Poké dollars (redeemable via Bike Voucher)",
                "🕵️ Robbed House: Team Rocket Grunt escapes through back hole dropping TM28 Dig",
                "🌊 Cerulean Cave Entrance: Northwest cavern housing Mewtwo (post-game)"
            ),
            connectedAreas = listOf("Route 4 (West)", "Route 24 (North via Nugget Bridge)", "Route 5 (South)", "Route 9 (East)"),
            guideAdvice = "Misty's Starmie is dangerously fast and hits hard with BubbleBeam and Water Pulse. Bring an Electric-type like Pikachu from Viridian Forest or Grass-type like Bellsprout/Oddish from Route 24."
        ),
        "vermilion" to LandmarkGuideDetails(
            tagline = "The Port City of Exquisite Sunsets!",
            description = "A warm southern port city basking in ocean breezes. Known for its bustling harbor, Electric-type Gym, and the international Pokémon Fan Club.",
            gymInfo = "Leader: Lt. Surge • Specialty: Electric • Thunder Badge (Lv. 18-24)",
            keyLocations = listOf(
                "🏟️ Vermilion Gym: Electric Gym led by Lt. Surge with hidden twin switches in trash cans",
                "🚢 Vermilion Harbor: Docking port for the luxury liner S.S. Anne",
                "🎀 Pokémon Fan Club: Listen to the Chairman's rapid chatter to receive the free Bike Voucher",
                "👴 Old Man's Machop: Ground preparation for building site"
            ),
            connectedAreas = listOf("Route 6 (North)", "Route 11 (East toward Diglett's Cave)", "S.S. Anne Pier (South Harbor)"),
            guideAdvice = "Catch a Diglett or Dugtrio in Diglett's Cave right next to Vermilion City. Ground-type moves are completely immune to Electric and deal super effective damage to Lt. Surge's Raichu!"
        ),
        "ss_anne" to LandmarkGuideDetails(
            tagline = "The World-Famous Luxury Cruise Liner!",
            description = "A majestic ocean liner docked in Vermilion Harbor for an annual convention of international Pokémon Trainers. Filled with wealthy gentlemen, sailors, and culinary delights.",
            keyLocations = listOf(
                "🚢 Port Pier Gate: Requires S.S. Ticket from Bill at Sea Cottage (Route 25)",
                "⚔️ Rival Blue Encounter: Staging showdown on 2F before the Captain's cabin",
                "👨‍✈️ Captain's Cabin: Relieve the Captain's seasickness to receive HM01 Cut",
                "🍳 Grand Kitchen: Watch master chefs prepare gourmet trainer meals",
                "🛏️ Passenger Cabins: Battle trainers for high prize money and obtain Rest TM"
            ),
            connectedAreas = listOf("Vermilion City (North Dock)"),
            guideAdvice = "Rub the Captain's back in his cabin to receive HM01 Cut. Warning: The S.S. Anne sails away permanently once you disembark after receiving Cut, so battle every trainer aboard first!"
        ),
        "lavender" to LandmarkGuideDetails(
            tagline = "The Noble Purple Town of Spirits!",
            description = "A solemn, atmospheric town shrouded in mist and mystery. Home to the towering Pokémon Tower where departed Pokémon are laid to rest.",
            keyLocations = listOf(
                "👻 Pokémon Tower: 7-floor resting place filled with Channelers, Ghost Pokémon, and the spirit of Marowak",
                "📻 Lavender Radio Tower: Modern broadcasting tower replacing Pokémon Tower in Gen 2/4",
                "🏠 Mr. Fuji's Volunteer House: Caring home for abandoned Pokémon; awards the Poké Flute",
                "🏷️ Name Rater: Evaluates and renames your Pokémon's nicknames"
            ),
            connectedAreas = listOf("Route 10 (North)", "Route 8 (West)", "Route 12 (South)"),
            guideAdvice = "You cannot identify or damage ghosts in Pokémon Tower without the Silph Scope from the Rocket Hideout in Celadon Game Corner! Calm the Lv. 30 Marowak ghost on 6F to rescue Mr. Fuji."
        ),
        "celadon" to LandmarkGuideDetails(
            tagline = "The City of Rainbow Dreams!",
            description = "Kanto's lavish commercial metropolis, featuring sparkling fountains, luxury shopping, gambling arcades, and a lush botanical Gym.",
            gymInfo = "Leader: Erika • Specialty: Grass • Rainbow Badge (Lv. 24-29)",
            keyLocations = listOf(
                "🏬 Celadon Department Store: 5 floors of evolution stones, vitamins, TMs, and rooftop drinks",
                "🎰 Rocket Game Corner: Slot machines concealing the secret underground Team Rocket Hideout",
                "🏟️ Celadon Gym: Grass-type tree greenhouse led by Erika (Rainbow Badge & TM21 Giga Drain)",
                "🏢 Celadon Mansion: Game Freak developers studio; receive Eevee on the rooftop",
                "🏨 Celadon Hotel: Relaxing hotel with invisible PC"
            ),
            connectedAreas = listOf("Route 7 (East)", "Route 16 (West to Cycling Road)"),
            guideAdvice = "Pull the poster switch in the Game Corner to unlock Giovanni's underground lair and claim the Silph Scope and Lift Key. On the Dept. Store roof, give fresh drinks to the thirsty girl for ice/rock TMs!"
        ),
        "saffron" to LandmarkGuideDetails(
            tagline = "The Shining Golden Heart of Kanto!",
            description = "The sprawling central hub of Kanto, crossed by grand boulevards and high-tech corporate architecture. Dominated by the soaring Silph Co. headquarters.",
            gymInfo = "Leader: Sabrina • Specialty: Psychic • Marsh Badge (Lv. 38-43)",
            keyLocations = listOf(
                "🏢 Silph Co. HQ: 11-floor warp tile skyscraper invaded by Giovanni and Team Rocket",
                "🏟️ Saffron Gym: Psychic warp pad Gym led by Sabrina (Marsh Badge & TM46 Psywave/Calm Mind)",
                "🥋 Fighting Dojo: Defeat Black Belt Karate Master to choose Hitmonlee or Hitmonchan",
                "🎭 Copycat's House: Mimic girl awards TM31 Mimic when returned her lost Poké Doll",
                "🚄 Magnet Train Station: High-speed rail connecting Kanto and Johto (Goldenrod City)"
            ),
            connectedAreas = listOf("Route 5 (North)", "Route 6 (South)", "Route 7 (West)", "Route 8 (East)"),
            guideAdvice = "Give Fresh Water or Tea from Celadon Mansion to any gate guard to enter Saffron City. Complete the Silph Co. warp maze to rescue the President and claim the Master Ball!"
        ),
        "fuchsia" to LandmarkGuideDetails(
            tagline = "Behold! It's Passion Pink!",
            description = "A historic, ninja-themed southern preserve renowned for its sprawling Safari Zone and venomous Gym.",
            gymInfo = "Leader: Koga • Specialty: Poison • Soul Badge (Lv. 37-43)",
            keyLocations = listOf(
                "🏟️ Fuchsia Gym: Poison Gym led by Ninja Master Koga featuring invisible glass maze walls",
                "🦏 Safari Zone: Wild reserve with 500-step time limit; search for Surf HM03 and Gold Teeth",
                "🦁 Warden's House: Return the Gold Teeth from Safari Zone Area 4 to receive HM04 Strength",
                "🎣 Fishing Guru Brother: Receives the Good Rod or Super Rod"
            ),
            connectedAreas = listOf("Route 15 (East)", "Route 18 (West)", "Route 19 (South Sea Route)"),
            guideAdvice = "Find the Secret House in Safari Zone Area 4 to get HM03 Surf, and pick up the Warden's Gold Teeth nearby to earn HM04 Strength. Psychic and Ground moves demolish Koga's Poison squad."
        ),
        "cinnabar" to LandmarkGuideDetails(
            tagline = "The Fiery Island Town of Rebirth!",
            description = "A remote volcanic island rising from the southern ocean. Famed for its geothermal hot springs, scientific cloning laboratories, and the scorched Pokémon Mansion.",
            gymInfo = "Leader: Blaine • Specialty: Fire • Volcano Badge (Lv. 40-47)",
            keyLocations = listOf(
                "🏟️ Cinnabar Gym: Fire Gym led by Quiz Master Blaine; unlock doors by answering trivia",
                "🏚️ Pokémon Mansion: Scorched ruins containing diary entries on Mew and Mewtwo, and the Secret Key",
                "🔬 Cinnabar Pokémon Lab: Fossil resurrection room reviving Helix, Dome, and Amber fossils"
            ),
            connectedAreas = listOf("Route 20 (East to Seafoam Islands)", "Route 21 (North to Pallet Town)"),
            guideAdvice = "The Gym door is locked! Explore the Pokémon Mansion to flip the Mewtwo statue switches and grab the Secret Key in the basement. Water and Ground Pokémon sweep Blaine easily."
        ),
        "indigo" to LandmarkGuideDetails(
            tagline = "The Pinnacle of Pokémon Trainers — The Pokémon League!",
            description = "The legendary mountain sanctuary atop Kanto where only trainers who possess all eight Gym Badges may enter to challenge the Elite Four and the Champion.",
            keyLocations = listOf(
                "🏆 Elite Four Chambers: Lorelei (Ice), Bruno (Fighting/Rock), Agatha (Ghost/Poison), Lance (Dragon)",
                "👑 Champion's Arena: The final showdown against Rival Blue",
                "🏥 Pokémon League Center: High-grade Mart stocking Full Restores, Max Potions, and Ultra Balls"
            ),
            connectedAreas = listOf("Victory Road (South Gate)"),
            guideAdvice = "Stock up on Revives and Full Restores! Once you step into Lorelei's room, you cannot leave until you conquer all four Elite Four masters and the Champion."
        ),
        "r1" to LandmarkGuideDetails(
            tagline = "A Quiet, Rolling Country Road",
            description = "Connecting Pallet Town to Viridian City, lined with soft grass and ledge shortcuts.",
            keyLocations = listOf("Route 1 Free Potion Mart Clerk", "Pidgey & Rattata wild encounters"),
            connectedAreas = listOf("Pallet Town (South)", "Viridian City (North)")
        ),
        "r2" to LandmarkGuideDetails(
            tagline = "A Forest Border Pathway",
            description = "Path split into eastern and western corridors by Viridian Forest.",
            keyLocations = listOf("Diglett's Cave Exit", "Oak's Aide (HM05 Flash / 10 caught)", "Pidgey, Rattata, Nidoran"),
            connectedAreas = listOf("Viridian City (South)", "Pewter City (North)")
        ),
        "r3" to LandmarkGuideDetails(
            tagline = "The Rocky Foothills of Mt. Moon",
            description = "Winding rocky trail frequented by early trainers leading toward the Mt. Moon entrance.",
            keyLocations = listOf("Magikarp Salesman (500 Poké dollars at Pokémon Center)", "Jigglypuff, Spearow, Mankey"),
            connectedAreas = listOf("Pewter City (West)", "Mt. Moon (East)")
        ),
        "r4" to LandmarkGuideDetails(
            tagline = "The Sunny Foot of Mt. Moon",
            description = "Eastern plateau descending from Mt. Moon to Cerulean City.",
            keyLocations = listOf("Mega Punch & Mega Kick Move Tutors (FRLG)", "Ekans, Sandshrew, Mankey"),
            connectedAreas = listOf("Mt. Moon (West)", "Cerulean City (East)")
        ),
        "r5" to LandmarkGuideDetails(
            tagline = "The North-South Hill Path",
            description = "Gentle downhill path connecting Cerulean and Saffron.",
            keyLocations = listOf("Pokémon Day Care (Gen 1 single Pokémon level-up)", "Underground Path to Route 6"),
            connectedAreas = listOf("Cerulean City (North)", "Saffron City (South)")
        ),
        "r6" to LandmarkGuideDetails(
            tagline = "The Vermilion Harbor Road",
            description = "Direct northern route connecting Vermilion City and Saffron.",
            keyLocations = listOf("Underground Path Exit", "Water ponds with Psyduck & Poliwag"),
            connectedAreas = listOf("Saffron City (North)", "Vermilion City (South)")
        ),
        "r7" to LandmarkGuideDetails(
            tagline = "The Short Western Meadow",
            description = "Short east-west meadow link between Celadon City and Saffron City.",
            keyLocations = listOf("Underground Path to Route 8", "Growlithe/Vulpix & Abra in grass"),
            connectedAreas = listOf("Celadon City (West)", "Saffron City (East)")
        ),
        "r8" to LandmarkGuideDetails(
            tagline = "The Lavender-Saffron Plain",
            description = "Long grass corridor lined with Gamblers and Super Nerds.",
            keyLocations = listOf("Underground Path to Route 7", "Growlithe / Vulpix wild encounters"),
            connectedAreas = listOf("Saffron City (West)", "Lavender Town (East)")
        ),
        "r9" to LandmarkGuideDetails(
            tagline = "The Rocky Ridge Path",
            description = "Rugged ledge path descending from Cerulean toward Rock Tunnel.",
            keyLocations = listOf("Machop, Spearow, and TM30 Teleport ledge"),
            connectedAreas = listOf("Cerulean City (West)", "Route 10 / Rock Tunnel (East)")
        ),
        "r10" to LandmarkGuideDetails(
            tagline = "The Power Plant River Corridor",
            description = "Waterway connecting Rock Tunnel to Lavender Town and the abandoned Power Plant.",
            keyLocations = listOf("Kanto Power Plant Entrance (Zapdos)", "Oak's Aide (Everstone)", "Voltorb, Magnemite"),
            connectedAreas = listOf("Route 9 (West)", "Rock Tunnel (Center)", "Lavender Town (South)")
        ),
        "r11" to LandmarkGuideDetails(
            tagline = "The Eastern Seaside Path",
            description = "Coastal path connecting Vermilion City to the silence of Route 12.",
            keyLocations = listOf("Diglett's Cave Entrance", "Sleeping Snorlax Roadblock (requires Poké Flute)", "Dowsing Machine Aide"),
            connectedAreas = listOf("Vermilion City (West)", "Route 12 (East)")
        ),
        "r12" to LandmarkGuideDetails(
            tagline = "The Silence Bridge",
            description = "Scenic seaside boardwalk known for excellent fishing and peaceful ocean views.",
            keyLocations = listOf("Super Rod Fishing Guru House", "Sleeping Snorlax Roadblock", "TM16 Pay Day / TM27 Return"),
            connectedAreas = listOf("Lavender Town (North)", "Route 11 (West)", "Route 13 (South)")
        ),
        "r13" to LandmarkGuideDetails(
            tagline = "The Maze of Wooden Fences",
            description = "Winding coastal path bordered by wooden fencing and maze-like trainer gauntlets.",
            keyLocations = listOf("Farfetch'd & Ditto in grass", "Hidden PP Up"),
            connectedAreas = listOf("Route 12 (North)", "Route 14 (South)")
        ),
        "r14" to LandmarkGuideDetails(
            tagline = "The Coastal Grassy Strand",
            description = "Grassy meadow bordering the southern ocean.",
            keyLocations = listOf("Bikers and Bird Keepers", "Venonat & Bellsprout/Oddish"),
            connectedAreas = listOf("Route 13 (North)", "Route 15 (West)")
        ),
        "r15" to LandmarkGuideDetails(
            tagline = "The Eastern Fuchsia Gate",
            description = "Direct eastern approach to Fuchsia City.",
            keyLocations = listOf("East Gatehouse 2F: Oak's Aide awards Exp. Share (requires 50 caught)"),
            connectedAreas = listOf("Route 14 (East)", "Fuchsia City (West)")
        ),
        "r16" to LandmarkGuideDetails(
            tagline = "The Cycling Road Entrance",
            description = "Western gate out of Celadon City onto Cycling Road.",
            keyLocations = listOf("Sleeping Snorlax Roadblock", "Secret House awarding HM02 Fly", "Cycling Road Gatehouse"),
            connectedAreas = listOf("Celadon City (East)", "Route 17 Cycling Road (West)")
        ),
        "r17" to LandmarkGuideDetails(
            tagline = "The Famous Seaside Cycling Road",
            description = "Long downhill slope over the ocean where trainers automatically glide on Bicycles.",
            keyLocations = listOf("Biker and Cue Ball gangs", "Doduo, Ponyta, and Grimer wild encounters"),
            connectedAreas = listOf("Route 16 (North)", "Route 18 (South)")
        ),
        "r18" to LandmarkGuideDetails(
            tagline = "The Fuchsia Coastal Gate",
            description = "Short coastal path connecting the bottom of Cycling Road to Fuchsia City.",
            keyLocations = listOf("Bird Keepers", "Gatehouse with Lickitung in-game trade"),
            connectedAreas = listOf("Route 17 Cycling Road (North)", "Fuchsia City (East)")
        ),
        "r19" to LandmarkGuideDetails(
            tagline = "The Southern Sea Route",
            description = "Expansive ocean channel leading from Fuchsia City down to Seafoam Islands.",
            keyLocations = listOf("Swimmers and water trainers", "Tentacool, Tentacruel, and Corsola"),
            connectedAreas = listOf("Fuchsia City (North)", "Route 20 Seafoam Islands (West)")
        ),
        "r20" to LandmarkGuideDetails(
            tagline = "The Seafoam Channel",
            description = "Ocean strait encompassing the frigid caverns of Seafoam Islands.",
            keyLocations = listOf("Seafoam Islands Entrance (Articuno)", "Swimmers and deep water currents"),
            connectedAreas = listOf("Route 19 (East)", "Cinnabar Island (West)")
        ),
        "r21" to LandmarkGuideDetails(
            tagline = "The Pallet-Cinnabar Channel",
            description = "Quiet ocean waterway connecting Pallet Town directly to Cinnabar Island.",
            keyLocations = listOf("Tangela in the grassy patches of Pallet shoreline", "Swimmers and Fishermen"),
            connectedAreas = listOf("Pallet Town (North)", "Cinnabar Island (South)")
        ),
        "r22" to LandmarkGuideDetails(
            tagline = "The League Approach",
            description = "Path leading west from Viridian City to the front gates of the Pokémon League.",
            keyLocations = listOf("Rival Blue Encounter 2", "Mankey, Nidoran (M/F), Spearow in grass"),
            connectedAreas = listOf("Viridian City (East)", "Route 23 / League Front Gate (West)")
        ),
        "r23" to LandmarkGuideDetails(
            tagline = "The Badge Verification Road",
            description = "Grand formal highway testing trainers on all 8 Gym Badges.",
            keyLocations = listOf("Eight Badge Checkpoint Guards", "Water channels with Kingler & Seaking"),
            connectedAreas = listOf("Route 22 (South)", "Victory Road Entrance (North)")
        ),
        "r24" to LandmarkGuideDetails(
            tagline = "The Legendary Nugget Bridge",
            description = "Golden bridge spanning north from Cerulean City over the river.",
            keyLocations = listOf("Nugget Bridge 5-Trainer Gauntlet", "Rocket Grunt awarding Gold Nugget", "Charmander gift trainer (Yellow)"),
            connectedAreas = listOf("Cerulean City (South)", "Route 25 (East)")
        ),
        "r25" to LandmarkGuideDetails(
            tagline = "The Promontory Sea Cottage",
            description = "Scenic seaside promontory overlooking Cerulean Bay.",
            keyLocations = listOf("Bill's Sea Cottage (transform Bill back into human to receive S.S. Ticket)", "TM19 Seismic Toss / TM43 Secret Power"),
            connectedAreas = listOf("Route 24 (West)", "Cerulean Bay (South)")
        ),
        "viridian_forest" to LandmarkGuideDetails(
            tagline = "The Natural Wooden Labyrinth",
            description = "Dense, tangled woodland populated by Bug Catchers and wild Bug/Electric Pokémon.",
            keyLocations = listOf("Pikachu (rare 5% wild encounter)", "Weedle, Caterpie, Potion & Antidote item balls"),
            connectedAreas = listOf("Route 2 South Gate", "Route 2 North Gate")
        ),
        "mt_moon" to LandmarkGuideDetails(
            tagline = "The Cavern of the Fallen Stars",
            description = "Mysterious cavern famed for frequent meteor showers and Moon Stones.",
            keyLocations = listOf("Super Nerd fossil choice (Dome Fossil / Kabuto or Helix Fossil / Omanyte)", "Clefairy & Moon Stones, Team Rocket scouts"),
            connectedAreas = listOf("Route 3 (West Entrance)", "Route 4 (East Exit)")
        ),
        "rock_tunnel" to LandmarkGuideDetails(
            tagline = "The Pitch-Black Subterranean Pass",
            description = "Unlit two-floor cavern carved through the mountains (requires HM05 Flash).",
            keyLocations = listOf("Wild Onix, Geodude, Machop", "Hikers and Pokemaniacs"),
            connectedAreas = listOf("Route 10 North (Entrance)", "Route 10 South (Exit to Lavender)")
        ),
        "power_plant" to LandmarkGuideDetails(
            tagline = "The Abandoned Industrial Generator",
            description = "A decommissioned power facility overrun with wild Electric Pokémon and Voltorb traps.",
            keyLocations = listOf("Legendary Bird Zapdos (Lv. 50)", "Electrode item ball traps", "Magneton & Electabuzz"),
            connectedAreas = listOf("Route 10 Waterway (Surf south from Route 9)")
        ),
        "seafoam" to LandmarkGuideDetails(
            tagline = "The Frigid Cavern of Glacial Rapids",
            description = "Chilly twin-island cave featuring roaring subterranean rapids and ice puzzles.",
            keyLocations = listOf("Legendary Bird Articuno (Lv. 50)", "Boulder Strength puzzle to stem currents", "Seel & Dewgong"),
            connectedAreas = listOf("Route 20 East", "Route 20 West")
        ),
        "victory_road" to LandmarkGuideDetails(
            tagline = "The Final Crucible of Champions",
            description = "Complex multi-level cavern testing boulder pushing puzzles and high-level wild Pokémon.",
            keyLocations = listOf("Legendary Bird Moltres (Lv. 50 in RGBY)", "TM05 Mega Kick / TM26 Earthquake", "Elite trainers"),
            connectedAreas = listOf("Route 23 (South Entrance)", "Indigo Plateau (North Exit)")
        ),
        "cerulean_cave" to LandmarkGuideDetails(
            tagline = "The Uncharted Cavern of the Ultimate Clone",
            description = "Endgame high-level labyrinth accessible only after entering the Hall of Fame.",
            keyLocations = listOf("Mewtwo (Lv. 70)", "Lv. 50-60 wild Pokémon", "Master Ball item spot"),
            connectedAreas = listOf("Cerulean City Northwest Riverway")
        )
    )

    // -------------------------------------------------------------
    // 1. KANTO (Game Boy RGBY)
    // -------------------------------------------------------------
    val KANTO_GB_LANDMARKS: List<MapLandmark> = listOf(
        // Towns & Cities (Anchored strictly to building icons)
        kantoLandmark("pallet", "Pallet Town", "Pallet", 0.220f, 0.690f, MapLandmarkType.TOWN),
        kantoLandmark("viridian", "Viridian City", "Viridian", 0.220f, 0.485f, MapLandmarkType.CITY),
        kantoLandmark("pewter", "Pewter City", "Pewter", 0.220f, 0.200f, MapLandmarkType.CITY),
        kantoLandmark("cerulean", "Cerulean City", "Cerulean", 0.625f, 0.140f, MapLandmarkType.CITY),
        kantoLandmark("vermilion", "Vermilion City", "Vermilion", 0.625f, 0.550f, MapLandmarkType.CITY),
        kantoLandmark("ss_anne", "S.S. Anne (Vermilion Harbor)", "S.S. Anne", 0.625f, 0.605f, MapLandmarkType.POI),
        kantoLandmark("lavender", "Lavender Town", "Lavender", 0.825f, 0.320f, MapLandmarkType.TOWN),
        kantoLandmark("celadon", "Celadon City", "Celadon", 0.470f, 0.320f, MapLandmarkType.CITY),
        kantoLandmark("saffron", "Saffron City", "Saffron", 0.625f, 0.320f, MapLandmarkType.CITY),
        kantoLandmark("fuchsia", "Fuchsia City", "Fuchsia", 0.525f, 0.780f, MapLandmarkType.CITY),
        kantoLandmark("cinnabar", "Cinnabar Island", "Cinnabar", 0.220f, 0.900f, MapLandmarkType.TOWN),
        kantoLandmark("indigo", "Indigo Plateau", "Indigo", 0.125f, 0.138f, MapLandmarkType.POI),

        // Routes (Corridor midpoints between towns/cities - never on buildings)
        kantoLandmark("r1", "Route 1", "R1", 0.220f, 0.588f, MapLandmarkType.ROUTE),
        kantoLandmark("r2", "Route 2", "R2", 0.220f, 0.365f, MapLandmarkType.ROUTE),
        kantoLandmark("r3", "Route 3", "R3", 0.320f, 0.200f, MapLandmarkType.ROUTE),
        kantoLandmark("r4", "Route 4", "R4", 0.520f, 0.140f, MapLandmarkType.ROUTE),
        kantoLandmark("r5", "Route 5", "R5", 0.625f, 0.230f, MapLandmarkType.ROUTE),
        kantoLandmark("r6", "Route 6", "R6", 0.625f, 0.435f, MapLandmarkType.ROUTE),
        kantoLandmark("r7", "Route 7", "R7", 0.545f, 0.320f, MapLandmarkType.ROUTE),
        kantoLandmark("r8", "Route 8", "R8", 0.725f, 0.320f, MapLandmarkType.ROUTE),
        kantoLandmark("r9", "Route 9", "R9", 0.725f, 0.140f, MapLandmarkType.ROUTE),
        kantoLandmark("r10", "Route 10", "R10", 0.825f, 0.230f, MapLandmarkType.ROUTE),
        kantoLandmark("r11", "Route 11", "R11", 0.725f, 0.550f, MapLandmarkType.ROUTE),
        kantoLandmark("r12", "Route 12", "R12", 0.825f, 0.440f, MapLandmarkType.ROUTE),
        kantoLandmark("r13", "Route 13", "R13", 0.750f, 0.640f, MapLandmarkType.ROUTE),
        kantoLandmark("r14", "Route 14", "R14", 0.680f, 0.720f, MapLandmarkType.ROUTE),
        kantoLandmark("r15", "Route 15", "R15", 0.600f, 0.780f, MapLandmarkType.ROUTE),
        kantoLandmark("r16", "Route 16", "R16", 0.380f, 0.320f, MapLandmarkType.ROUTE),
        kantoLandmark("r17", "Route 17 (Cycling Road)", "R17", 0.380f, 0.550f, MapLandmarkType.ROUTE),
        kantoLandmark("r18", "Route 18", "R18", 0.450f, 0.780f, MapLandmarkType.ROUTE),
        kantoLandmark("r19", "Route 19", "R19", 0.525f, 0.860f, MapLandmarkType.ROUTE),
        kantoLandmark("r20", "Route 20", "R20", 0.370f, 0.900f, MapLandmarkType.ROUTE),
        kantoLandmark("r21", "Route 21", "R21", 0.220f, 0.800f, MapLandmarkType.ROUTE),
        kantoLandmark("r22", "Route 22", "R22", 0.170f, 0.485f, MapLandmarkType.ROUTE),
        kantoLandmark("r23", "Route 23", "R23", 0.125f, 0.320f, MapLandmarkType.ROUTE),
        kantoLandmark("r24", "Route 24 (Nugget Bridge)", "R24", 0.625f, 0.080f, MapLandmarkType.ROUTE),
        kantoLandmark("r25", "Route 25 (Sea Cottage)", "R25", 0.720f, 0.080f, MapLandmarkType.ROUTE),

        // Landmarks & Special Categories (POIs, Entrances, Secrets, Puzzles)
        kantoLandmark("viridian_forest", "Viridian Forest", "V. Forest", 0.220f, 0.260f, MapLandmarkType.PUZZLE),
        kantoLandmark("mt_moon", "Mt. Moon", "Mt. Moon", 0.420f, 0.140f, MapLandmarkType.POI),
        kantoLandmark("rock_tunnel", "Rock Tunnel Entrance", "Rock Tunnel", 0.825f, 0.190f, MapLandmarkType.ENTRANCE),
        kantoLandmark("power_plant", "Power Plant (Zapdos)", "Power Plant", 0.850f, 0.260f, MapLandmarkType.SECRET),
        kantoLandmark("seafoam", "Seafoam Islands", "Seafoam", 0.400f, 0.900f, MapLandmarkType.POI),
        kantoLandmark("victory_road", "Victory Road Gate", "Victory Rd", 0.125f, 0.260f, MapLandmarkType.ENTRANCE),
        kantoLandmark("cerulean_cave", "Cerulean Cave (Mewtwo)", "Cerulean Cave", 0.570f, 0.100f, MapLandmarkType.SECRET)
    )

    // -------------------------------------------------------------
    // 2. KANTO (GBA FireRed & LeafGreen)
    // -------------------------------------------------------------
    val KANTO_GBA_LANDMARKS: List<MapLandmark> = listOf(
        kantoLandmark("pallet", "Pallet Town", "Pallet", 0.280f, 0.770f, MapLandmarkType.TOWN),
        kantoLandmark("viridian", "Viridian City", "Viridian", 0.280f, 0.620f, MapLandmarkType.CITY),
        kantoLandmark("pewter", "Pewter City", "Pewter", 0.280f, 0.420f, MapLandmarkType.CITY),
        kantoLandmark("cerulean", "Cerulean City", "Cerulean", 0.620f, 0.380f, MapLandmarkType.CITY),
        kantoLandmark("vermilion", "Vermilion City", "Vermilion", 0.620f, 0.670f, MapLandmarkType.CITY),
        kantoLandmark("ss_anne", "S.S. Anne (Vermilion Harbor)", "S.S. Anne", 0.620f, 0.720f, MapLandmarkType.POI),
        kantoLandmark("lavender", "Lavender Town", "Lavender", 0.750f, 0.520f, MapLandmarkType.TOWN),
        kantoLandmark("celadon", "Celadon City", "Celadon", 0.520f, 0.520f, MapLandmarkType.CITY),
        kantoLandmark("saffron", "Saffron City", "Saffron", 0.620f, 0.520f, MapLandmarkType.CITY),
        kantoLandmark("fuchsia", "Fuchsia City", "Fuchsia", 0.550f, 0.830f, MapLandmarkType.CITY),
        kantoLandmark("cinnabar", "Cinnabar Island", "Cinnabar", 0.280f, 0.920f, MapLandmarkType.TOWN),
        kantoLandmark("indigo", "Indigo Plateau", "Indigo", 0.210f, 0.380f, MapLandmarkType.POI),

        kantoLandmark("r1", "Route 1", "R1", 0.280f, 0.695f, MapLandmarkType.ROUTE),
        kantoLandmark("r2", "Route 2", "R2", 0.280f, 0.570f, MapLandmarkType.ROUTE),
        kantoLandmark("viridian_forest", "Viridian Forest", "V. Forest", 0.280f, 0.520f, MapLandmarkType.PUZZLE),
        kantoLandmark("r3", "Route 3", "R3", 0.350f, 0.420f, MapLandmarkType.ROUTE),
        kantoLandmark("mt_moon", "Mt. Moon", "Mt. Moon", 0.420f, 0.380f, MapLandmarkType.POI),
        kantoLandmark("r4", "Route 4", "R4", 0.520f, 0.380f, MapLandmarkType.ROUTE),
        kantoLandmark("r5", "Route 5", "R5", 0.620f, 0.450f, MapLandmarkType.ROUTE),
        kantoLandmark("r6", "Route 6", "R6", 0.620f, 0.595f, MapLandmarkType.ROUTE),
        kantoLandmark("r7", "Route 7", "R7", 0.570f, 0.520f, MapLandmarkType.ROUTE),
        kantoLandmark("r8", "Route 8", "R8", 0.685f, 0.520f, MapLandmarkType.ROUTE),
        kantoLandmark("r9", "Route 9", "R9", 0.685f, 0.380f, MapLandmarkType.ROUTE),
        kantoLandmark("r10", "Route 10", "R10", 0.750f, 0.450f, MapLandmarkType.ROUTE),
        kantoLandmark("rock_tunnel", "Rock Tunnel Entrance", "Rock Tunnel", 0.750f, 0.420f, MapLandmarkType.ENTRANCE),
        kantoLandmark("power_plant", "Power Plant (Zapdos)", "Power Plant", 0.780f, 0.450f, MapLandmarkType.SECRET),
        kantoLandmark("r11", "Route 11", "R11", 0.685f, 0.670f, MapLandmarkType.ROUTE),
        kantoLandmark("r12", "Route 12", "R12", 0.750f, 0.620f, MapLandmarkType.ROUTE),
        kantoLandmark("r13", "Route 13", "R13", 0.720f, 0.730f, MapLandmarkType.ROUTE),
        kantoLandmark("r14", "Route 14", "R14", 0.650f, 0.780f, MapLandmarkType.ROUTE),
        kantoLandmark("r15", "Route 15", "R15", 0.600f, 0.830f, MapLandmarkType.ROUTE),
        kantoLandmark("r16", "Route 16", "R16", 0.460f, 0.520f, MapLandmarkType.ROUTE),
        kantoLandmark("r17", "Route 17 (Cycling Road)", "R17", 0.460f, 0.680f, MapLandmarkType.ROUTE),
        kantoLandmark("r18", "Route 18", "R18", 0.500f, 0.830f, MapLandmarkType.ROUTE),
        kantoLandmark("r19", "Route 19", "R19", 0.550f, 0.890f, MapLandmarkType.ROUTE),
        kantoLandmark("r20", "Route 20", "R20", 0.420f, 0.920f, MapLandmarkType.ROUTE),
        kantoLandmark("seafoam", "Seafoam Islands", "Seafoam", 0.420f, 0.920f, MapLandmarkType.POI),
        kantoLandmark("r21", "Route 21", "R21", 0.280f, 0.845f, MapLandmarkType.ROUTE),
        kantoLandmark("r22", "Route 22", "R22", 0.245f, 0.620f, MapLandmarkType.ROUTE),
        kantoLandmark("r23", "Route 23", "R23", 0.210f, 0.500f, MapLandmarkType.ROUTE),
        kantoLandmark("r24", "Route 24 (Nugget Bridge)", "R24", 0.620f, 0.300f, MapLandmarkType.ROUTE),
        kantoLandmark("r25", "Route 25 (Sea Cottage)", "R25", 0.700f, 0.300f, MapLandmarkType.ROUTE),

        // Sevii Islands Highlights
        MapLandmark("one_island", "One Island (Mt. Ember)", "One Island", 0.35f, 0.20f, MapLandmarkType.TOWN),
        MapLandmark("two_island", "Two Island (Cape Brink)", "Two Island", 0.40f, 0.35f, MapLandmarkType.TOWN),
        MapLandmark("three_island", "Three Island (Berry Forest)", "Three Island", 0.45f, 0.55f, MapLandmarkType.TOWN),
        MapLandmark("four_island", "Four Island (Icefall Cave)", "Four Island", 0.25f, 0.75f, MapLandmarkType.TOWN),
        MapLandmark("seven_island", "Seven Island (Tanoby Ruins)", "Seven Island", 0.48f, 0.88f, MapLandmarkType.TOWN),
        MapLandmark("tanoby_ruins", "Tanoby Ruins (Unown)", "Tanoby", 0.48f, 0.94f, MapLandmarkType.SECRET),
        MapLandmark("icefall_cave", "Icefall Cave Entrance", "Icefall", 0.25f, 0.70f, MapLandmarkType.ENTRANCE)
    )

    // -------------------------------------------------------------
    // JOHTO COMPREHENSIVE STRATEGY GUIDE DETAILS
    // -------------------------------------------------------------
    val JOHTO_LANDMARK_DETAILS: Map<String, LandmarkGuideDetails> = mapOf(
        "new_bark" to LandmarkGuideDetails(
            tagline = "The Town Where the Winds of a New Beginning Blow",
            description = "A quiet, rural town at Johto's eastern edge where aspiring trainers begin their journey.",
            keyLocations = listOf(
                "🔬 Prof. Elm's Laboratory: Starter Pokémon selection (Chikorita, Cyndaquil, Totodile) & Pokédex upgrade",
                "🏠 Player's Home: Starting point with PC item storage; Mother saves earnings for rare items",
                "🚪 Route 29 Gate: Exit west to Cherrygrove and Violet City"
            ),
            connectedAreas = listOf("Route 29 (West)", "Route 27 / Tohjo Falls (East via Surf)"),
            guideAdvice = "Visit Prof. Elm to choose your starter, then head west along Route 29 to Cherrygrove and Mr. Pokémon's House."
        ),
        "cherrygrove" to LandmarkGuideDetails(
            tagline = "The City of Fragrant Flowers",
            description = "A picturesque coastal city adorned with blooming cherry blossoms and gentle sea vistas.",
            keyLocations = listOf(
                "👴 Guide Gent: Guided town tour rewarding the Pokégear Map Card and Running Shoes",
                "🏥 Pokémon Center & Mart: Basic healing and travel supplies",
                "🌊 Cherrygrove Bay: Coastal waters leading to Route 30"
            ),
            connectedAreas = listOf("Route 29 (East)", "Route 30 (North toward Violet City)"),
            guideAdvice = "Follow the Guide Gent on his quick town tour to earn the Map Card for your Pokégear!"
        ),
        "violet" to LandmarkGuideDetails(
            tagline = "The City of Nostalgic Scents",
            description = "An ancient city steeped in tradition, home to the towering Sprout Tower and Johto's first Pokémon Gym.",
            gymInfo = "Leader: Falkner • Specialty: Flying • Zephyr Badge (Lv. 7-9 / HGSS Lv. 9-13)",
            keyLocations = listOf(
                "🏟️ Violet Gym: Flying-type Gym led by Falkner (Zephyr Badge & TM31 Mud-Slap/Roost)",
                "🏯 Sprout Tower: Ancient multi-tiered pagoda where Sages train Bellsprout; Elder gives HM05 Flash",
                "🎓 Earl's Pokémon Academy: Classroom teaching status conditions and battle basics",
                "🥚 Mystery Egg Handover: Prof. Elm's assistant delivers the Togepi egg at the PokéMart"
            ),
            connectedAreas = listOf("Route 31 (East)", "Route 32 (South)", "Route 36 (West toward Sudowoodo)"),
            guideAdvice = "Clear Sprout Tower first to get HM05 Flash. Rock and Electric-types like Geodude from Dark Cave or Mareep on Route 32 counter Falkner's Pidgeotto easily."
        ),
        "azalea" to LandmarkGuideDetails(
            tagline = "Where People and Pokémon Live in Happy Harmony",
            description = "A cozy mountain village celebrated for its hand-crafted Apricorn Poké Balls and charcoal kiln.",
            gymInfo = "Leader: Bugsy • Specialty: Bug • Hive Badge (Lv. 14-16 / HGSS Lv. 15-17)",
            keyLocations = listOf(
                "🏟️ Azalea Gym: Bug-type Gym led by Bugsy (Hive Badge & TM49 Fury Cutter/U-turn)",
                "🔨 Kurt's House: Master craftsman turns Apricorns into specialty Poké Balls (Fast, Heavy, Lure, etc.)",
                "💧 Slowpoke Well: Cavern where Team Rocket cuts off Slowpoke tails for black market profit",
                "🔥 Charcoal Kiln: Master and apprentice reward HM01 Cut after herding Farfetch'd in Ilex Forest"
            ),
            connectedAreas = listOf("Route 33 (East)", "Ilex Forest (West)"),
            guideAdvice = "Rescue the Slowpoke in Slowpoke Well from Team Rocket to unlock Kurt's Apricorn crafting and gain entry into Bugsy's Gym. Fire, Rock, and Flying moves easily counter Bugsy's Scyther."
        ),
        "goldenrod" to LandmarkGuideDetails(
            tagline = "The Festive City of Opulent Charm",
            description = "The bustling cultural and commercial metropolis of Johto, boasting the largest Department Store, Radio Tower, and high-speed Magnet Train.",
            gymInfo = "Leader: Whitney • Specialty: Normal • Plain Badge (Lv. 18-20 / HGSS Lv. 17-19)",
            keyLocations = listOf(
                "🏟️ Goldenrod Gym: Normal Gym led by Whitney shaped like a giant Clefairy (Plain Badge & Attract)",
                "📻 Radio Tower: Johto broadcast station (Radio Card quiz, Blue Card, and Team Rocket occupation)",
                "🏬 Department Store: 6 floors stocking TMs, evolution stones, vitamins, and Pokémon trades",
                "🎰 Game Corner: Coin games rewarding rare Pokémon (Abra, Dratini) and elemental TMs",
                "🚲 Bike Shop: Free Bicycle loan for advertising across Johto",
                "🚇 Magnet Train Station: High-speed rail line connecting Johto directly to Saffron City in Kanto",
                "🕳️ Goldenrod Underground: Underground path with Haircut Brothers, Herb Shop, and Coin Case"
            ),
            connectedAreas = listOf("Route 34 (South)", "Route 35 (North)"),
            guideAdvice = "Whitney's Miltank is infamous for Rollout and Attract. Trade for Machop ('Muscle') on 5F of Goldenrod Dept Store or use female fighting/ground Pokémon to neutralize Attract."
        ),
        "ecruteak" to LandmarkGuideDetails(
            tagline = "A Historical City Where the Past Meets the Present",
            description = "An ancient capital famous for its centuries-old pagodas, traditional Kimono Dance Theater, and the myth of Ho-Oh and the Legendary Beasts.",
            gymInfo = "Leader: Morty • Specialty: Ghost • Fog Badge (Lv. 21-25 / HGSS Lv. 21-25)",
            keyLocations = listOf(
                "🏟️ Ecruteak Gym: Ghost-type Gym led by Morty with an invisible pathway over an abyss (Fog Badge & Shadow Ball)",
                "🔥 Burned Tower: Charred ruins of the Brass Tower where Raikou, Entei, and Suicune awaken and scatter",
                "🏯 Bell Tower (Tin Tower): Sacred 10-story golden pagoda where Ho-Oh descends to the Rainbow Wing bearer",
                "👘 Kimono Dance Theater: Defeat all 5 Kimono Girls to receive HM03 Surf and witness the ritual dance"
            ),
            connectedAreas = listOf("Route 37 (South)", "Route 38 (West)", "Route 42 (East)"),
            guideAdvice = "Defeat the Kimono Girls at the Dance Theater to get HM03 Surf. Normal-type Pokémon are completely immune to Ghost attacks; use Dark or Ground moves to counter Gengar."
        ),
        "olivine" to LandmarkGuideDetails(
            tagline = "The Port with Sea Breeze and Scent of the Tide",
            description = "A major maritime port city known for its historic shining Lighthouse and the luxury passenger liner S.S. Aqua.",
            gymInfo = "Leader: Jasmine • Specialty: Steel • Mineral Badge (Lv. 30-35 / HGSS Lv. 30-35)",
            keyLocations = listOf(
                "🏟️ Olivine Gym: Steel-type Gym led by Jasmine (Mineral Badge & TM23 Iron Tail)",
                "💡 Glitter Lighthouse: Multi-level beacon where Jasmine nurses the sick Ampharos ('Amphy')",
                "🚢 Olivine Harbor: Port docking the S.S. Aqua liner sailing to Vermilion City in Kanto",
                "🎣 Good Rod Fisherman: Grants the Good Rod for catching Poliwag and Magikarp"
            ),
            connectedAreas = listOf("Route 39 (North via Moomoo Farm)", "Route 40 (South/West Sea Route)"),
            guideAdvice = "Jasmine won't battle until you surf to Cianwood City to fetch the SecretPotion for Amphy in the Lighthouse. Fire, Fighting, and Ground moves shatter Steelix."
        ),
        "cianwood" to LandmarkGuideDetails(
            tagline = "A Port Surrounded by Rough Seas",
            description = "A remote, wind-swept coastal town nestled on a rugged island across the open sea from Olivine.",
            gymInfo = "Leader: Chuck • Specialty: Fighting • Storm Badge (Lv. 27-30 / HGSS Lv. 29-31)",
            keyLocations = listOf(
                "🏟️ Cianwood Gym: Fighting Gym led by Chuck under a rushing waterfall (Storm Badge & DynamicPunch)",
                "💊 500-Year Pharmacy: Dispenses the SecretPotion required to heal Amphy at Olivine Lighthouse",
                "💧 Suicune Beach Spot: Meet Eusine and witness Suicune sprinting across the waves",
                "👩 Chuck's Wife: Awards HM02 Fly outside the gym after defeating Chuck"
            ),
            connectedAreas = listOf("Route 41 (East Sea Route)", "Cliff Edge Gate / Safari Zone (West in HGSS)"),
            guideAdvice = "Grab the SecretPotion from the Pharmacy for Jasmine, defeat Chuck to earn HM02 Fly from his wife outside the gym. Flying and Psychic moves easily topple Poliwrath."
        ),
        "mahogany" to LandmarkGuideDetails(
            tagline = "The Town of Hidden Secrets and Mystery",
            description = "A quiet mountain town concealing a clandestine underground Team Rocket headquarters broadcasting radio signals to the Lake of Rage.",
            gymInfo = "Leader: Pryce • Specialty: Ice • Glacier Badge (Lv. 27-31 / HGSS Lv. 30-34)",
            keyLocations = listOf(
                "🏟️ Mahogany Gym: Ice-type Gym led by Pryce featuring frictionless ice sliding floor mazes",
                "🚀 Team Rocket Headquarters: Secret bunker beneath the Souvenir Shop guarded by password generators",
                "🍬 RageCandyBar Vendor: Old woman selling local specialty candy blocking the eastern path to Ice Path"
            ),
            connectedAreas = listOf("Route 42 (West)", "Route 43 (North toward Lake of Rage)", "Route 44 (East toward Ice Path)"),
            guideAdvice = "Investigate Lake of Rage with Champion Lance to infiltrate the Rocket Hideout beneath the souvenir shop. Defeat the Executives and shut down the radio transmitter to unlock Pryce's Gym."
        ),
        "blackthorn" to LandmarkGuideDetails(
            tagline = "A Quiet Mountain Retreat",
            description = "A mountain valley sanctuary secluded in the highlands, renowned as the clan ancestral home of dragon tamers.",
            gymInfo = "Leader: Clair • Specialty: Dragon • Rising Badge (Lv. 37-40 / HGSS Lv. 37-41)",
            keyLocations = listOf(
                "🏟️ Blackthorn Gym: Dragon Gym led by Clair featuring moving lava platform bridges (Rising Badge & DragonBreath)",
                "🐉 Dragon's Den: Sacred subterranean cave holding the Dragon Shrine and Master Dratini with ExtremeSpeed",
                "✂️ Move Deleter & Move Reminder: Forget HM moves and relearn past level-up attacks with Heart Scales"
            ),
            connectedAreas = listOf("Ice Path (North Entrance)", "Route 45 (South Mountain Ledges)"),
            guideAdvice = "Clair's Kingdra has only one weakness in Gen 2/4: Dragon-type moves! Ice moves defeat her Dragonair. After winning, pass the Elder's test in Dragon's Den to receive the Rising Badge."
        ),
        "mt_silver" to LandmarkGuideDetails(
            tagline = "The Sacred Peak of Legends",
            description = "The perilous, snow-capped mountain bordering Johto and Kanto, where only the greatest Pokémon Champion resides.",
            keyLocations = listOf(
                "🏔️ Mt. Silver Cavern: Dark, high-level labyrinth requiring Flash, Surf, and Waterfall to navigate",
                "👑 Summit Shrine: The ultimate showdown against Pokémon Champion Red and his level 75-88 team",
                "🦖 Rare Wild Spawns: Larvitar, Pupitar, Steelix, Misdreavus, and Ursaring"
            ),
            connectedAreas = listOf("Route 28 (East Entrance from Pokémon League Reception Gate)"),
            guideAdvice = "Requires all 16 Badges from Kanto and Johto! Pack high-level Pokémon (Lv. 75-88) and items to face Red's legendary team: Pikachu, Charizard, Blastoise, Venusaur, Snorlax, and Lapras."
        ),
        "r29" to LandmarkGuideDetails(
            tagline = "The Breezy Route to Cherrygrove",
            description = "First wild Pokémon route leading west from New Bark Town into Cherrygrove City.",
            keyLocations = listOf("Tuscany of Tuesday (Pink Bow)", "Sentret, Pidgey, Hoothoot, Rattata"),
            connectedAreas = listOf("New Bark Town (East)", "Cherrygrove City (West)")
        ),
        "r30" to LandmarkGuideDetails(
            tagline = "The Path of Friendly Neighbors",
            description = "Traverses farmland leading north to Mr. Pokémon's House.",
            keyLocations = listOf("Mr. Pokémon's House (receives Mystery Egg)", "Prof. Oak (receives Pokédex)", "First Trainer Battle"),
            connectedAreas = listOf("Cherrygrove City (South)", "Route 31 (North)")
        ),
        "r31" to LandmarkGuideDetails(
            tagline = "The Gateway to Dark Cave",
            description = "Wooded corridor leading west into Violet City and Dark Cave.",
            keyLocations = listOf("Dark Cave South Entrance", "Arthur of Thursday (Hard Stone)"),
            connectedAreas = listOf("Route 30 (East)", "Violet City (West)")
        ),
        "r32" to LandmarkGuideDetails(
            tagline = "The Long Coastline Road",
            description = "Stretching south toward Union Cave past the fishing waters.",
            keyLocations = listOf("Roar TM05 / Miracle Seed Guy", "Old Rod Fisherman", "Union Cave North Entrance", "Frieda of Friday (Poison Barb)"),
            connectedAreas = listOf("Violet City (North)", "Route 33 / Union Cave (South)")
        ),
        "r33" to LandmarkGuideDetails(
            tagline = "The Stormy Mountainside",
            description = "Short, rain-soaked trail skirting Union Cave and the Azalea mountains.",
            keyLocations = listOf("Union Cave South Exit", "Hiker Anthony", "Apricorn tree"),
            connectedAreas = listOf("Route 32 (East)", "Azalea Town (West)")
        ),
        "r34" to LandmarkGuideDetails(
            tagline = "The Coastline to Goldenrod",
            description = "Scenic route connecting the Ilex Forest exit to Goldenrod City.",
            keyLocations = listOf("Pokémon Day Care (Breeding & Egg hatching)", "Officer Keith (night patrol)"),
            connectedAreas = listOf("Ilex Forest (South)", "Goldenrod City (North)")
        ),
        "r35" to LandmarkGuideDetails(
            tagline = "The Busy Suburban Parkway",
            description = "Grass corridor linking Goldenrod City north to the National Park.",
            keyLocations = listOf("National Park South Gate", "Mail delivery quest (Spearow 'Kenya' with HP Up)"),
            connectedAreas = listOf("Goldenrod City (South)", "National Park (North)")
        ),
        "r36" to LandmarkGuideDetails(
            tagline = "The Crossroad of Strange Trees",
            description = "Crucial crossroads where a peculiar tree blocks the intersection.",
            keyLocations = listOf("Sudowoodo block (requires SquirtBottle)", "Rock Smash Guy (receives TM08/HM06)", "Ruins of Alph North Gate"),
            connectedAreas = listOf("Violet City (East)", "National Park (West)", "Route 37 (North)")
        ),
        "r37" to LandmarkGuideDetails(
            tagline = "The Apricorn Grove Road",
            description = "Short hillside route heading into Ecruteak City lined with Apricorn trees.",
            keyLocations = listOf("Apricorn Grove (Red, Blue, Black Apricorns)", "Sunny of Sunday (Magnet)"),
            connectedAreas = listOf("Route 36 (South)", "Ecruteak City (North)")
        ),
        "r38" to LandmarkGuideDetails(
            tagline = "The Rolling Farmland Passage",
            description = "East-west corridor lined with fences, trainers, and wild Miltank.",
            keyLocations = listOf("Sailor Harry", "Magnemite, Raticate, Tauros, Miltank encounters"),
            connectedAreas = listOf("Ecruteak City (East)", "Route 39 (West)")
        ),
        "r39" to LandmarkGuideDetails(
            tagline = "The Moomoo Farm Valley",
            description = "Gentle highway leading south into Olivine City.",
            keyLocations = listOf("Moomoo Farm (Heal sick Miltank with Oran/Berry to buy Moomoo Milk)", "TM13 Snore"),
            connectedAreas = listOf("Route 38 (North)", "Olivine City (South)")
        ),
        "r40" to LandmarkGuideDetails(
            tagline = "The Sandy Sea Highway",
            description = "Coastal waterway embarking south toward Whirl Islands and Cianwood.",
            keyLocations = listOf("Monica of Monday (Sharp Beak)", "Battle Frontier Gate (HGSS)"),
            connectedAreas = listOf("Olivine City (North)", "Route 41 (South)")
        ),
        "r41" to LandmarkGuideDetails(
            tagline = "The Treacherous Whirlpool Archipelago",
            description = "Open sea surrounding the mysterious Whirl Islands.",
            keyLocations = listOf("Four entrances to Whirl Islands (requires HM06/05 Whirlpool)", "Mantine / Tentacruel wild encounters"),
            connectedAreas = listOf("Route 40 (North)", "Cianwood City (West)")
        ),
        "r42" to LandmarkGuideDetails(
            tagline = "The Mountain Foot Trail",
            description = "Rugged route threading past Mt. Mortar's entrances.",
            keyLocations = listOf("Mt. Mortar 3 Entrances (Water Cave for Tyrogue)", "Mareep & Flaaffy fields"),
            connectedAreas = listOf("Ecruteak City (West)", "Mahogany Town (East)")
        ),
        "r43" to LandmarkGuideDetails(
            tagline = "The Rain-Soaked Toll Road",
            description = "Pathway north through the forest toward Lake of Rage.",
            keyLocations = listOf("Team Rocket Toll Gate (charges 1,000 Poké dollars)", "Santos of Saturday (Soft Sand)"),
            connectedAreas = listOf("Mahogany Town (South)", "Lake of Rage (North)")
        ),
        "r44" to LandmarkGuideDetails(
            tagline = "The Frozen Foothills",
            description = "Lake-dotted pathway leading east toward Ice Path.",
            keyLocations = listOf("Swinub & Remoraid waters", "Ice Path West Entrance"),
            connectedAreas = listOf("Mahogany Town (West)", "Ice Path (East)")
        ),
        "r45" to LandmarkGuideDetails(
            tagline = "The Mountain Ledge Trail",
            description = "Southward one-way mountain ledge descent down from Blackthorn City.",
            keyLocations = listOf("Gligar / Skarmory / Phanpy / Teddiursa depending on version", "Dark Cave South Exit"),
            connectedAreas = listOf("Blackthorn City (North)", "Route 46 (South)")
        ),
        "sprout_tower" to LandmarkGuideDetails(
            tagline = "The Swaying Sacred Pagoda",
            description = "An ancient multi-story wooden pagoda built around a giant central swaying pillar.",
            keyLocations = listOf("Swaying Pillar", "Elder Li (HM05 Flash)", "Wild Bellsprout & Gastly (night)"),
            connectedAreas = listOf("Violet City (South Entrance)")
        ),
        "ruins_alph" to LandmarkGuideDetails(
            tagline = "The Ancient Mysteries of Unown",
            description = "Mysterious archaeological ruins inscribed with ancient Braille and Unown chambers.",
            keyLocations = listOf("4 Stone Slider Puzzles (Kabuto, Aerodactyl, Omanyte, Ho-Oh)", "Unown Chambers (A-Z, ?, !)", "Secret Radio broadcast (13.7 MHz)"),
            connectedAreas = listOf("Route 32 (East)", "Route 36 (North)")
        ),
        "ilex_forest" to LandmarkGuideDetails(
            tagline = "The Timeless Forest Sanctuary",
            description = "A dense, sun-filtering canopy forest dedicated to Celebi, the Guardian of the Forest.",
            keyLocations = listOf("Farfetch'd Herding Puzzle", "Charcoal Apprentice (HM01 Cut)", "Headbutt Move Tutor", "Celebi Shrine"),
            connectedAreas = listOf("Azalea Town (East)", "Route 34 (North)")
        ),
        "national_park" to LandmarkGuideDetails(
            tagline = "The Expansive Green Reserve",
            description = "A magnificent landscaped park home to the prestigious Bug-Catching Contest.",
            keyLocations = listOf("Bug-Catching Contest (Tues/Thurs/Sat: Scyther, Pinsir -> Sun Stone)", "Soothe Bell / Quick Claw NPC"),
            connectedAreas = listOf("Route 35 (South Gate)", "Route 36 (East Gate)")
        ),
        "lake_of_rage" to LandmarkGuideDetails(
            tagline = "The Vast Craggy Lake",
            description = "A majestic inland lake plagued by Team Rocket's radio waves inducing forced evolution.",
            keyLocations = listOf("Red Gyarados (Shiny Lv. 30 battle)", "Red Scale trade (Exp. Share from Mr. Pokémon)", "Champion Lance Investigation"),
            connectedAreas = listOf("Route 43 (South)")
        ),
        "ice_path" to LandmarkGuideDetails(
            tagline = "The Glacial Mountain Pass",
            description = "A treacherous subterranean ice cavern filled with frictionless slide puzzles.",
            keyLocations = listOf("HM07 Waterfall item ball", "Boulder-pushing ice pit puzzles", "Delibird, Jynx, Swinub encounters"),
            connectedAreas = listOf("Route 44 (West Entrance)", "Blackthorn City (South Exit)")
        ),
        "whirl_islands" to LandmarkGuideDetails(
            tagline = "The Stormy Deep Sea Labyrinth",
            description = "A secluded four-island labyrinth guarded by whirlpools and deep caverns.",
            keyLocations = listOf("Silver Wing & Tidal Bell shrine", "Legendary Guardian Lugia (Lv. 40 in Silver / Lv. 70 in HGSS)", "Hidden Water Stones & Ultra Balls"),
            connectedAreas = listOf("Route 41 (Ocean Approaches)")
        )
    )

    // -------------------------------------------------------------
    // 3. JOHTO (Gold, Silver, Crystal & HeartGold, SoulSilver)
    // -------------------------------------------------------------
    val JOHTO_LANDMARKS: List<MapLandmark> = listOf(
        johtoLandmark("new_bark", "New Bark Town", "New Bark", 0.85f, 0.70f, MapLandmarkType.TOWN),
        johtoLandmark("cherrygrove", "Cherrygrove City", "Cherrygrove", 0.70f, 0.70f, MapLandmarkType.CITY),
        johtoLandmark("violet", "Violet City", "Violet", 0.55f, 0.55f, MapLandmarkType.CITY),
        johtoLandmark("azalea", "Azalea Town", "Azalea", 0.35f, 0.75f, MapLandmarkType.TOWN),
        johtoLandmark("goldenrod", "Goldenrod City", "Goldenrod", 0.25f, 0.55f, MapLandmarkType.CITY),
        johtoLandmark("ecruteak", "Ecruteak City", "Ecruteak", 0.38f, 0.38f, MapLandmarkType.CITY),
        johtoLandmark("olivine", "Olivine City", "Olivine", 0.18f, 0.45f, MapLandmarkType.CITY),
        johtoLandmark("cianwood", "Cianwood City", "Cianwood", 0.08f, 0.65f, MapLandmarkType.CITY),
        johtoLandmark("mahogany", "Mahogany Town", "Mahogany", 0.60f, 0.38f, MapLandmarkType.TOWN),
        johtoLandmark("blackthorn", "Blackthorn City", "Blackthorn", 0.78f, 0.45f, MapLandmarkType.CITY),
        johtoLandmark("mt_silver", "Mt. Silver Peak", "Mt. Silver", 0.88f, 0.55f, MapLandmarkType.POI),

        johtoLandmark("r29", "Route 29", "R29", 0.78f, 0.70f, MapLandmarkType.ROUTE),
        johtoLandmark("r30", "Route 30 (Mr. Pokémon)", "R30", 0.70f, 0.60f, MapLandmarkType.ROUTE),
        johtoLandmark("r31", "Route 31", "R31", 0.63f, 0.55f, MapLandmarkType.ROUTE),
        johtoLandmark("r32", "Route 32", "R32", 0.55f, 0.65f, MapLandmarkType.ROUTE),
        johtoLandmark("r33", "Route 33 (Union Cave)", "R33", 0.45f, 0.75f, MapLandmarkType.ROUTE),
        johtoLandmark("r34", "Route 34 (Day Care)", "R34", 0.25f, 0.65f, MapLandmarkType.ROUTE),
        johtoLandmark("r35", "Route 35", "R35", 0.25f, 0.48f, MapLandmarkType.ROUTE),
        johtoLandmark("r36", "Route 36 (Sudowoodo)", "R36", 0.45f, 0.55f, MapLandmarkType.ROUTE),
        johtoLandmark("r37", "Route 37", "R37", 0.38f, 0.46f, MapLandmarkType.ROUTE),
        johtoLandmark("r38", "Route 38", "R38", 0.28f, 0.38f, MapLandmarkType.ROUTE),
        johtoLandmark("r39", "Route 39 (Moomoo Farm)", "R39", 0.20f, 0.48f, MapLandmarkType.ROUTE),
        johtoLandmark("r40", "Route 40", "R40", 0.14f, 0.52f, MapLandmarkType.ROUTE),
        johtoLandmark("r41", "Route 41 (Whirl Islands)", "R41", 0.12f, 0.58f, MapLandmarkType.ROUTE),
        johtoLandmark("r42", "Route 42", "R42", 0.50f, 0.38f, MapLandmarkType.ROUTE),
        johtoLandmark("r43", "Route 43", "R43", 0.60f, 0.30f, MapLandmarkType.ROUTE),
        johtoLandmark("r44", "Route 44", "R44", 0.68f, 0.38f, MapLandmarkType.ROUTE),
        johtoLandmark("r45", "Route 45", "R45", 0.78f, 0.55f, MapLandmarkType.ROUTE),

        johtoLandmark("sprout_tower", "Sprout Tower Swaying Pillars", "Sprout", 0.55f, 0.50f, MapLandmarkType.PUZZLE),
        johtoLandmark("ruins_alph", "Ruins of Alph (Braille)", "R. of Alph", 0.45f, 0.55f, MapLandmarkType.SECRET),
        johtoLandmark("ilex_forest", "Ilex Forest Shrine", "Ilex Forest", 0.35f, 0.80f, MapLandmarkType.POI),
        johtoLandmark("national_park", "National Park Bug Contest", "Nat. Park", 0.35f, 0.50f, MapLandmarkType.PUZZLE),
        johtoLandmark("lake_of_rage", "Lake of Rage (Red Gyarados)", "Lake Rage", 0.60f, 0.25f, MapLandmarkType.POI),
        johtoLandmark("ice_path", "Ice Path Entrance", "Ice Path", 0.78f, 0.38f, MapLandmarkType.ENTRANCE),
        johtoLandmark("whirl_islands", "Whirl Islands (Lugia)", "Whirl Is.", 0.12f, 0.58f, MapLandmarkType.SECRET)
    )

    // -------------------------------------------------------------
    // HOENN COMPREHENSIVE STRATEGY GUIDE DETAILS
    // -------------------------------------------------------------
    val HOENN_LANDMARK_DETAILS: Map<String, LandmarkGuideDetails> = mapOf(
        "littleroot" to LandmarkGuideDetails(
            tagline = "A Town That Can't Be Shaded Any Hue",
            description = "A peaceful woodland hamlet in southwestern Hoenn where new Pokémon trainers begin their journeys.",
            keyLocations = listOf(
                "🔬 Prof. Birch's Pokémon Lab: Starter Pokémon selection (Treecko, Torchic, Mudkip) & National Pokédex",
                "🏠 Player's House & Rival's House: Room clock calibration and mother's running shoes handover",
                "🌲 Northern Trail: Leading to Route 101 where Birch is attacked by a wild Pokémon"
            ),
            connectedAreas = listOf("Route 101 (North)"),
            guideAdvice = "Set your clock inside your bedroom, rescue Prof. Birch on Route 101 to pick your starter, and battle your rival on Route 103 before receiving your Pokédex."
        ),
        "oldale" to LandmarkGuideDetails(
            tagline = "Where Things Start Off Scarce",
            description = "A small crossroads town connecting Littleroot to the coastal routes.",
            keyLocations = listOf(
                "🏥 Pokémon Center & Poké Mart: Basic supplies; clerk provides a free sample Potion",
                "🔍 Ancient Footprints Sketcher: Quirky researcher studying Pokémon footprints on Route 102 path"
            ),
            connectedAreas = listOf("Route 101 (South)", "Route 102 (West)", "Route 103 (North)"),
            guideAdvice = "Head north onto Route 103 to find your rival and trigger your first battle. Win to return to Birch's lab for the Pokédex and Poké Balls."
        ),
        "petalburg" to LandmarkGuideDetails(
            tagline = "Where People Mingle with Nature",
            description = "A picturesque city bordered by blooming floral ponds, home to your father Norman's Gym.",
            gymInfo = "Leader: Norman • Specialty: Normal • Balance Badge (Lv. 28-31)",
            keyLocations = listOf(
                "🏟️ Petalburg Gym: Normal-type Gym led by your father Norman (Balance Badge & Facade); unlocked after 4 badges",
                "🏡 Wally's House: Assist Wally in catching his Ralts on Route 102; his parents award HM03 Surf later",
                "🌊 Western Pathway: Leads to Route 104 and Mr. Briney's seaside cottage"
            ),
            connectedAreas = listOf("Route 102 (East)", "Route 104 (West)"),
            guideAdvice = "Norman will not accept your Gym challenge until you have earned 4 Badges across Hoenn. After earning the Balance Badge, Wally's father presents you with HM03 Surf!"
        ),
        "rustboro" to LandmarkGuideDetails(
            tagline = "The City Probing the Integration of Nature and Science",
            description = "A modern industrial and educational center housing the Devon Corporation headquarters.",
            gymInfo = "Leader: Roxanne • Specialty: Rock • Stone Badge (Lv. 14-15)",
            keyLocations = listOf(
                "🏟️ Rustboro Gym: Rock-type Gym led by Roxanne (Stone Badge & TM39 Rock Tomb)",
                "🏢 Devon Corporation: President Mr. Stone awards the PokéNav and commissions Devon Goods recovery",
                "🎓 Pokémon Trainer's School: Quick Claw item from teacher",
                "✂️ Cutter's House: Cutter awards HM01 Cut once the Stone Badge is earned"
            ),
            connectedAreas = listOf("Route 104 (South)", "Route 115 (North)", "Route 116 (East toward Rusturf Tunnel)"),
            guideAdvice = "Water and Grass starters effortlessly defeat Roxanne's Geodude and Nosepass. If you chose Torchic, evolve it into Combusken for Double Kick or catch a Wingull / Shroomish on Route 104."
        ),
        "dewford" to LandmarkGuideDetails(
            tagline = "A Tiny Island in the Blue Sea",
            description = "An isolated southern island community famed for its surfing beaches, trendy phrases, and Granite Cave.",
            gymInfo = "Leader: Brawly • Specialty: Fighting • Knuckle Badge (Lv. 16-18)",
            keyLocations = listOf(
                "🏟️ Dewford Gym: Fighting-type Gym led by Brawly with a pitch-black labyrinth floor (Knuckle Badge & Bulk Up)",
                "⛏️ Granite Cave: Multi-floor dark cavern where Steven Stone receives Mr. Stone's letter; awards TM47 Steel Wing",
                "🗣️ Dewford Hall: Trendy phrase fad community altering Feebas fishing tile seeds"
            ),
            connectedAreas = listOf("Route 106 (North via water)", "Route 107 (East via water)"),
            guideAdvice = "Pick up HM05 Flash in Granite Cave to illuminate Brawly's dark gym. Flying and Psychic-types like Taillow or Ralts cleanly counter Brawly's Makuhita."
        ),
        "slateport" to LandmarkGuideDetails(
            tagline = "The Port Where People and Pokémon Cross Paths",
            description = "A vibrant southern seaport famous for its bustling open-air market, shipyard, and Oceanographic Museum.",
            keyLocations = listOf(
                "🚢 Stern's Shipyard & Oceanic Museum: Captain Stern receives Devon Goods; Team Aqua/Magma encounter",
                "🛍️ Slateport Market: Outdoor bazaar selling vitamins, Secret Base furniture, dolls, and Incenses",
                "🏷️ Name Rater's House: Rename traded and caught Pokémon",
                "🎪 Contest Hall / Battle Tent: Hyper Contest venue and Battle Tent tournament",
                "⚓ Slateport Harbor: Ferry terminal for the S.S. Tidal to Lilycove and Battle Frontier"
            ),
            connectedAreas = listOf("Route 109 (South Beach)", "Route 110 (North Cycling Road)"),
            guideAdvice = "Deliver the Devon Goods to Capt. Stern at the Oceanic Museum (entry fee 50 Poké dollars) and fend off the evil team grunts to unlock the northern road to Mauville."
        ),
        "mauville" to LandmarkGuideDetails(
            tagline = "The Bright and Shiny City of Fun",
            description = "A major inland crossroads and high-tech hub featuring the Electric Gym, Game Corner, and Rydel's Cycles.",
            gymInfo = "Leader: Wattson • Specialty: Electric • Dynamo Badge (Lv. 22-24)",
            keyLocations = listOf(
                "🏟️ Mauville Gym: Electric-type Gym led by Wattson with energized zap-door switches (Dynamo Badge & Shock Wave)",
                "🚲 Rydel's Cycles: Free bicycle loan — choose between the Mach Bike (speed) and Acro Bike (bunny hop stunts)",
                "🎰 Game Corner: Coin slot and roulette games for TMs and items (requires Coin Case from Harbor Mail trade)",
                "⚡ New Mauville Quest: Post-Surf generator shutdown quest awarded by Wattson (rewards TM24 Thunderbolt)"
            ),
            connectedAreas = listOf("Route 110 (South)", "Route 117 (West toward Verdanturf)", "Route 111 (North toward Desert)", "Route 118 (East via Surf)"),
            guideAdvice = "A Ground-type Pokémon like Geodude or Marshtomp makes Wattson's Gym effortless since his Voltorb, Magneton, and Manectric cannot touch Ground with Electric moves."
        ),
        "verdanturf" to LandmarkGuideDetails(
            tagline = "The Windswept Highlands with the Sweet Scent of Grass",
            description = "A tranquil resort town sheltered by mountains, renowned for its exceptionally clean air and Rusturf Tunnel.",
            keyLocations = listOf(
                "⛏️ Rusturf Tunnel: Break the cracked rocks using Rock Smash to reunite Wanda and her boyfriend; awards HM04 Strength",
                "🎪 Contest Hall / Battle Tent: Battle Tent knockout matches",
                "🏡 Wanda's House: Resting place for the reunited tunnel couple"
            ),
            connectedAreas = listOf("Route 117 (East to Mauville)", "Rusturf Tunnel (Northwest to Rustboro)"),
            guideAdvice = "Smash the central rocks in Rusturf Tunnel with Rock Smash to link Rustboro and Verdanturf permanently. Wanda's boyfriend rewards you with HM04 Strength!"
        ),
        "fallarbor" to LandmarkGuideDetails(
            tagline = "A Farm Community with Small Gardens",
            description = "A cozy agricultural village dusting with volcanic ash drifting down from Mt. Chimney.",
            keyLocations = listOf(
                "🔬 Prof. Cozmo's House: Astronomer investigating meteorites; kidnapped to Meteor Falls by Magma/Aqua",
                "❤️ Move Reminder's House: Relearn past level-up attacks in exchange for Heart Scales",
                "🎪 Battle Tent: Fallarbor set-battle challenge tournament"
            ),
            connectedAreas = listOf("Route 113 (East from glassblower)", "Route 114 (South toward Meteor Falls)"),
            guideAdvice = "Collect Heart Scales by smashing rocks or fishing Luvdisc, then bring them to the Move Reminder here to teach your team powerful forgotten moves."
        ),
        "lavaridge" to LandmarkGuideDetails(
            tagline = "Pokémon Center Hot Springs — An Excellent Place for Relaxing!",
            description = "A serene hot spring mountain village nestled on the southern slope of the active volcano Mt. Chimney.",
            gymInfo = "Leader: Flannery • Specialty: Fire • Heat Badge (Lv. 26-28)",
            keyLocations = listOf(
                "🏟️ Lavaridge Gym: Fire-type Gym led by Flannery featuring steam trapdoor geyser pits (Heat Badge & Overheat)",
                "♨️ Hot Springs: Therapeutic bath behind the Pokémon Center; old woman gifts a Wynaut Egg",
                "🌿 Herb Shop: Sells low-cost traditional bitter medicine (EnergyPowder, Revival Herb)"
            ),
            connectedAreas = listOf("Route 112 (East / South ledges)", "Jagged Pass (Cable Car to Mt. Chimney)"),
            guideAdvice = "Flannery's Torkoal uses Sunny Day paired with lethal Overheat and White Herb. Use Water, Rock, or Ground moves, and watch out for Attract if your Pokémon is male."
        ),
        "fortree" to LandmarkGuideDetails(
            tagline = "The Feathered City Among the Trees",
            description = "An extraordinary treetop canopy city where residents live in wooden treehouses connected by rope bridges.",
            gymInfo = "Leader: Winona • Specialty: Flying • Feather Badge (Lv. 31-33)",
            keyLocations = listOf(
                "🏟️ Fortree Gym: Flying Gym led by Winona with turnstile revolving doors (Feather Badge & Aerial Ace)",
                "🔍 Invisible Blockade: An invisible Kecleon blocks the Gym entrance until Steven gifts the Devon Scope on Route 120",
                "🪵 Secret Base Guild: Furniture and decoration shops for secret bases"
            ),
            connectedAreas = listOf("Route 119 (West)", "Route 120 (East)"),
            guideAdvice = "Follow Steven past Route 120's bridge to receive the Devon Scope, which reveals the invisible Kecleon blocking the Gym door. Ice and Rock moves bring down Winona's Altaria."
        ),
        "lilycove" to LandmarkGuideDetails(
            tagline = "Where the Land Ends and the Sea Begins",
            description = "The grand cultural capital of eastern Hoenn featuring the massive Department Store, Art Museum, and Team Aqua/Magma Hideout.",
            keyLocations = listOf(
                "🏬 Lilycove Department Store: 5-story shopping center with TMs, dolls, and rooftop rooftop clearance sales",
                "🎨 Lilycove Museum: Art gallery displaying Master Rank contest victory portraits painted by the resident artist",
                "🌊 Team Hideout: Oceanic cove base holding the Master Ball and submarine stolen by Archie/Maxie",
                "✂️ Move Deleter's House: Freely forgets any HM move"
            ),
            connectedAreas = listOf("Route 121 (West)", "Route 124 (East Sea Route)"),
            guideAdvice = "Before the villainous submarine departs, raid their hideout in the eastern cove to find Hoenn's single Master Ball hidden inside a room filled with 4 Electrodes."
        ),
        "mossdeep" to LandmarkGuideDetails(
            tagline = "Our Slogan: Cherish the Pokémon!",
            description = "An ocean island city surrounded by calm waters, home to the famous Mossdeep Space Center and psychic twin Gym Leaders.",
            gymInfo = "Leaders: Tate & Liza • Specialty: Psychic (Double Battle) • Mind Badge (Lv. 42)",
            keyLocations = listOf(
                "🏟️ Mossdeep Gym: Psychic Gym led by Tate & Liza featuring Double Battle format and teleporter pads (Mind Badge & Calm Mind)",
                "🚀 Mossdeep Space Center: Rocket launch facility raided by Team Magma in Emerald; partner with Steven in a tag battle",
                "🏠 Steven's House: Steven gifts HM08 Dive and leaves a Poké Ball containing Beldum post-game",
                "🎣 Super Rod Fisherman: House near the cliff granting the Super Rod"
            ),
            connectedAreas = listOf("Route 124 (West)", "Route 125 (North to Shoal Cave)", "Route 127 (South)"),
            guideAdvice = "Tate & Liza battle as a pair with Lunatone and Solrock. Dark, Ghost, and Water moves (like Surf) deal massive damage, but watch out for their Levitate and Light Screen."
        ),
        "sootopolis" to LandmarkGuideDetails(
            tagline = "The City of Risen Mysteries",
            description = "A mystical city built inside the crater of an extinct undersea volcanic caldera, accessible only via diving or flight.",
            gymInfo = "Leader: Wallace (RS) / Juan (Emerald) • Specialty: Water • Rain Badge (Lv. 41-43)",
            keyLocations = listOf(
                "🏟️ Sootopolis Gym: Water-type Gym with fragile cracking ice tile floor puzzles (Rain Badge & Water Pulse)",
                "🌋 Cave of Origin: Sacred subterranean chamber where Groudon and Kyogre clash; awakens Rayquaza",
                "🌊 Submerged Trench: Deep-sea trench on Route 126 requiring HM08 Dive to enter the caldera"
            ),
            connectedAreas = listOf("Route 126 (Underwater crater entrance)"),
            guideAdvice = "Step on every ice tile once to turn them light blue and open the stairs to the Gym Leader. In Emerald, Juan leads the Gym with Kingdra; use Dragon moves to defeat it."
        ),
        "pacifidlog" to LandmarkGuideDetails(
            tagline = "Where the Morning Sun Smiles Upon the Waters",
            description = "A tranquil floating settlement constructed atop logs and wooden rafts drifting over deep oceanic currents.",
            keyLocations = listOf(
                "🏝️ Mirage Island Watcher: Old man watching the horizon for the elusive Mirage Island (Liechi Berry & Wynaut)",
                "📜 TM Frustration / Return Brother: Awards TM21 or TM27 depending on Pokémon friendship",
                "🌊 Fast Rapids: Western exit shoots players through high-speed water currents to Route 133 and Route 134"
            ),
            connectedAreas = listOf("Route 131 (East toward Sky Pillar)", "Route 132 (West currents to Route 133/134)"),
            guideAdvice = "Pacifidlog is the launching pad for Sky Pillar to the east and the fast rapids leading west to the Sealed Chamber."
        ),
        "ever_grande" to LandmarkGuideDetails(
            tagline = "The Grand City of Flowers and the Sea",
            description = "A floral paradise perched at the base and summit of a colossal waterfall, housing Hoenn's Victory Road and Pokémon League.",
            keyLocations = listOf(
                "🌊 Ever Grande Waterfall: Massive aquatic barrier requiring HM07 Waterfall to scale",
                "🔦 Hoenn Victory Road: Complex cavern requiring Flash, Strength, Rock Smash, and Waterfall; final Wally showdown",
                "🏆 Hoenn Pokémon League: Elite Four (Sidney, Phoebe, Glacia, Drake) & Champion (Steven in RS / Wallace in Emerald)"
            ),
            connectedAreas = listOf("Route 128 (South Sea Route)", "Victory Road (Passage to League)"),
            guideAdvice = "Teach your team a diverse movepool: Fighting for Sidney's Darks, Ghost/Dark for Phoebe's Ghosts, Fire/Fighting for Glacia's Ice types, and Dragon/Ice for Drake's Dragons."
        ),
        "trick_house" to LandmarkGuideDetails(
            tagline = "The Labyrinth of the Trick Master",
            description = "A roadside puzzle funhouse located on Route 110 where the eccentric Trick Master challenges trainers with 8 evolving scroll puzzles.",
            keyLocations = listOf(
                "📜 8 Sequential Trick Rooms: Require Cut, Flash, Rock Smash, Strength, and mechanical button mazes",
                "🎁 Trick Master Rewards: Rare Candy, Hard Stone, Smoke Ball, Magnet, and decorative Tents"
            ),
            connectedAreas = listOf("Route 110 (North of Slateport)"),
            guideAdvice = "Return to the Trick House after each Gym Badge you earn to tackle the next puzzle room and collect valuable items!"
        ),
        "meteor_falls" to LandmarkGuideDetails(
            tagline = "The Celestial Waterfalls Cavern",
            description = "A glowing subterranean cavern carved by ancient meteor strikes, housing dragon masters.",
            keyLocations = listOf(
                "🌊 Rushing Waterfalls: Requires HM07 Waterfall to climb to the inner sanctum",
                "🐉 Bagon Room: Small isolated water room containing wild Bagon and TM02 Dragon Claw",
                "⚔️ Team Magma vs Team Aqua: Showdown over Prof. Cozmo's meteorite"
            ),
            connectedAreas = listOf("Route 114 (East Entrance)", "Route 115 (West Exit)")
        ),
        "mt_chimney" to LandmarkGuideDetails(
            tagline = "The Raging Volcanic Summit",
            description = "The active volcano towering over central Hoenn, accessible via the Route 112 Cable Car.",
            keyLocations = listOf(
                "🚠 Cable Car Summit Station: Scenic tram connecting Route 112 to the volcanic rim",
                "🍪 Lava Cookie Lady: Sells Lava Cookies that heal all status conditions for 200 Poké dollars",
                "🌋 Volcanic Crater: Magma/Aqua meteorite machine battle to save or erupt the volcano",
                "🚵 Jagged Pass: Rocky downhill bike trail leading south into Lavaridge Town"
            ),
            connectedAreas = listOf("Route 112 (Cable Car Base)", "Jagged Pass (South Descent)")
        ),
        "mt_pyre" to LandmarkGuideDetails(
            tagline = "The Sacred Mountain of Spirits",
            description = "Hoenn's sacred memorial mountain rising from the waters of Route 122, enshrining ancestral Pokémon.",
            keyLocations = listOf(
                "🔴 Blue & Red Orb Shrines: Summit altar guarding the ancient orbs that awaken Kyogre and Groudon",
                "👻 Interior Tower: Multi-floor memorial crypt holding Cleanse Tag and TM30 Shadow Ball",
                "🌫️ Exterior Mist Slopes: Foggy mountain paths harboring wild Chimecho, Duskull, and Shuppet"
            ),
            connectedAreas = listOf("Route 122 (Lake surrounding Mt. Pyre)")
        ),
        "sky_pillar" to LandmarkGuideDetails(
            tagline = "The Cloud-Piercing Tower of the Sky",
            description = "An ancient crumbling spire soaring into the clouds above Route 131, home to the ozone dragon Rayquaza.",
            keyLocations = listOf(
                "🚲 Cracked Floor Tiles: Frictionless crumbling pitfalls that require top-speed Mach Bike riding to cross",
                "🐉 Summit Sanctuary: Legendary Dragon/Flying Pokémon Rayquaza (Lv. 70 in RS/Emerald)",
                "⚡ Awakening Rayquaza: In Emerald, awaken Rayquaza here to descend onto Sootopolis and quell the storm"
            ),
            connectedAreas = listOf("Route 131 (North Ocean Portal)"),
            guideAdvice = "Equip the Mach Bike at Rydel's Cycles before ascending! You must build up top speed and turn without hitting walls to ride across the cracked floor tiles."
        ),
        "shoal_cave" to LandmarkGuideDetails(
            tagline = "The Cavern of Shifting Tides",
            description = "A coastal cavern north of Mossdeep affected by high and low tide cycles every 6 hours.",
            keyLocations = listOf(
                "🌊 High Tide (9am-3pm / 9pm-3am): Flooded channels allow surfing to collect Shoal Shells",
                "🧊 Low Tide (3am-9am / 3pm-9pm): Receding waters reveal the frozen Ice Room with NeverMeltIce & TM07 Hail",
                "🐚 Shell Bell Craftsman: Craftsman trades 4 Shoal Salts and 4 Shoal Shells for a regenerative Shell Bell"
            ),
            connectedAreas = listOf("Route 125 (North of Mossdeep)")
        ),
        "battle_frontier" to LandmarkGuideDetails(
            tagline = "The Supreme Proving Ground",
            description = "The ultimate post-game facility in Hoenn (Emerald) featuring 7 Battle Facilities led by Frontier Brains.",
            keyLocations = listOf(
                "🏛️ 7 Battle Facilities: Battle Tower, Dome, Palace, Arena, Factory, Pike, and Pyramid",
                "👑 Frontier Brains: Defeat leaders to earn Silver and Gold Frontier Symbols",
                "🛍️ BP Exchange Service Corner: Trade Battle Points for rare vitamins, stat items, and moves",
                "🎨 Smeargle Artisan Cave: Cavern filled with wild Smeargle"
            ),
            connectedAreas = listOf("Slateport Ferry Port", "Lilycove Ferry Port")
        ),
        "faraway_island" to LandmarkGuideDetails(
            tagline = "The Uncharted Emerald Mirage",
            description = "A secluded, misty tropical island hidden far out to sea where the mythical Pokémon Mew plays.",
            keyLocations = listOf(
                "🌴 Misty Island Forest: Dense tropical grove surrounded by ocean breezes",
                "✨ Playful Mew: Play hide-and-seek through tall grass to engage the Mythical Pokémon Mew (Lv. 30)",
                "🎫 Old Sea Map: Requires the elusive promotional event ticket to board the S.S. Tidal"
            ),
            connectedAreas = listOf("Lilycove Ferry Port (Event Route)")
        ),
        "r101" to LandmarkGuideDetails(
            tagline = "The Woodland Beginning",
            description = "Short path from Littleroot Town to Oldale Town where Birch is attacked by a wild Pokémon.",
            keyLocations = listOf("Birch starter rescue event", "Wurmple, Poochyena, Zigzagoon"),
            connectedAreas = listOf("Littleroot Town (South)", "Oldale Town (North)")
        ),
        "r102" to LandmarkGuideDetails(
            tagline = "The First Steps Path",
            description = "Gentle route running west from Oldale to Petalburg City.",
            keyLocations = listOf("Youngster Calvin", "Ralts, Seedot, Lotad wild encounters"),
            connectedAreas = listOf("Oldale Town (East)", "Petalburg City (West)")
        ),
        "r103" to LandmarkGuideDetails(
            tagline = "The Cross-Bay Overlook",
            description = "Lakeside route where your rival challenges you to your first Pokémon battle.",
            keyLocations = listOf("Rival First Battle", "Waterway crossing to Route 110 (Surf)"),
            connectedAreas = listOf("Oldale Town (South)")
        ),
        "r104" to LandmarkGuideDetails(
            tagline = "The Coastal Flower Highway",
            description = "Scenic shoreline route split by Petalburg Woods.",
            keyLocations = listOf("Pretty Petal Flower Shop (Wailmer Pail)", "Mr. Briney's Cottage (Peeko the Wingull)", "Petalburg Woods entrances"),
            connectedAreas = listOf("Petalburg City (East)", "Rustboro City (North)")
        ),
        "r110" to LandmarkGuideDetails(
            tagline = "The Seaside Cycling Expressway",
            description = "Two-tier highway connecting Slateport City and Mauville City.",
            keyLocations = listOf("Seaside Cycling Road (upper tier)", "Trick House (lower tier)", "Rival Battle (awards Itemfinder)"),
            connectedAreas = listOf("Slateport City (South)", "Mauville City (North)")
        ),
        "r111" to LandmarkGuideDetails(
            tagline = "The Arid Desert Highway",
            description = "Long corridor spanning from Mauville north into the raging desert sands.",
            keyLocations = listOf("Winstrate Family Challenge", "Desert Ruins (Regirock)", "Mirage Tower (Root / Claw Fossil, Emerald)"),
            connectedAreas = listOf("Mauville City (South)", "Route 112 (West)", "Route 113 (North)")
        ),
        "r112" to LandmarkGuideDetails(
            tagline = "The Slopes of Mt. Chimney",
            description = "Foothill path curving around Mt. Chimney's base toward the Cable Car station.",
            keyLocations = listOf("Mt. Chimney Cable Car Station", "Fiery Path Cavern", "Jagged Pass exit"),
            connectedAreas = listOf("Route 111 (East)", "Lavaridge Town (West)")
        ),
        "r116" to LandmarkGuideDetails(
            tagline = "The Rocky Path to Rusturf",
            description = "Eastbound road from Rustboro City terminating at Rusturf Tunnel.",
            keyLocations = listOf("Tunnelers Rest House", "Devon Researcher (Devon Goods recovery quest)", "Skitty encounters"),
            connectedAreas = listOf("Rustboro City (West)", "Rusturf Tunnel (East)")
        ),
        "r119" to LandmarkGuideDetails(
            tagline = "The Tropical Rainforest Highway",
            description = "Extensive, rain-drenched route flanked by tall grass, waterfalls, and the Weather Institute.",
            keyLocations = listOf("Weather Institute (Castform & Team Aqua/Magma)", "Rival Battle (awards HM02 Fly)", "Feebas fishing spots (6 changing tiles)"),
            connectedAreas = listOf("Route 118 (South)", "Fortree City (North)")
        ),
        "r120" to LandmarkGuideDetails(
            tagline = "The Misty Ancient Ruins Path",
            description = "Wetland plateau draped in tall grass and ancient stone monuments.",
            keyLocations = listOf("Ancient Tomb (Registeel)", "Invisible Kecleon on bridge (Steven gives Devon Scope)", "Nest Ball Girl"),
            connectedAreas = listOf("Fortree City (West)", "Route 121 (East)")
        ),
        "r128" to LandmarkGuideDetails(
            tagline = "The Deep Oceanic Trench",
            description = "Vast blue sea route situated directly above the deep undersea trench.",
            keyLocations = listOf("Seafloor Cavern (Dive trench)", "Team Aqua/Magma Submarine showdown", "Awakening Kyogre / Groudon"),
            connectedAreas = listOf("Route 127 (North)", "Ever Grande City (East)")
        )
    )

    // -------------------------------------------------------------
    // 4. HOENN (Ruby, Sapphire, Emerald & ORAS)
    // -------------------------------------------------------------
    val HOENN_LANDMARKS: List<MapLandmark> = listOf(
        // Towns & Cities (Anchored strictly to PokéNav red/blue rectangular & square icons)
        hoennLandmark("littleroot", "Littleroot Town", "Littleroot", 0.180f, 0.680f, MapLandmarkType.TOWN),
        hoennLandmark("oldale", "Oldale Town", "Oldale", 0.180f, 0.580f, MapLandmarkType.TOWN),
        hoennLandmark("petalburg", "Petalburg City", "Petalburg", 0.080f, 0.580f, MapLandmarkType.CITY),
        hoennLandmark("rustboro", "Rustboro City", "Rustboro", 0.050f, 0.400f, MapLandmarkType.CITY),
        hoennLandmark("dewford", "Dewford Town", "Dewford", 0.120f, 0.820f, MapLandmarkType.TOWN),
        hoennLandmark("slateport", "Slateport City", "Slateport", 0.320f, 0.650f, MapLandmarkType.CITY),
        hoennLandmark("mauville", "Mauville City", "Mauville", 0.330f, 0.420f, MapLandmarkType.CITY),
        hoennLandmark("verdanturf", "Verdanturf Town", "Verdanturf", 0.180f, 0.420f, MapLandmarkType.TOWN),
        hoennLandmark("fallarbor", "Fallarbor Town", "Fallarbor", 0.150f, 0.120f, MapLandmarkType.TOWN),
        hoennLandmark("lavaridge", "Lavaridge Town", "Lavaridge", 0.220f, 0.280f, MapLandmarkType.TOWN),
        hoennLandmark("fortree", "Fortree City", "Fortree", 0.450f, 0.120f, MapLandmarkType.CITY),
        hoennLandmark("lilycove", "Lilycove City", "Lilycove", 0.660f, 0.270f, MapLandmarkType.CITY),
        hoennLandmark("mossdeep", "Mossdeep City", "Mossdeep", 0.860f, 0.360f, MapLandmarkType.CITY),
        hoennLandmark("sootopolis", "Sootopolis City", "Sootopolis", 0.750f, 0.470f, MapLandmarkType.CITY),
        hoennLandmark("pacifidlog", "Pacifidlog Town", "Pacifidlog", 0.620f, 0.620f, MapLandmarkType.TOWN),
        hoennLandmark("ever_grande", "Ever Grande City", "Ever Grande", 0.940f, 0.560f, MapLandmarkType.CITY),

        // Routes (Corridor midpoints between towns/cities)
        hoennLandmark("r101", "Route 101", "R101", 0.180f, 0.630f, MapLandmarkType.ROUTE),
        hoennLandmark("r102", "Route 102", "R102", 0.130f, 0.580f, MapLandmarkType.ROUTE),
        hoennLandmark("r103", "Route 103", "R103", 0.180f, 0.500f, MapLandmarkType.ROUTE),
        hoennLandmark("r104", "Route 104 (Flower Shop)", "R104", 0.060f, 0.490f, MapLandmarkType.ROUTE),
        hoennLandmark("r105", "Route 105", "R105", 0.050f, 0.650f, MapLandmarkType.ROUTE),
        hoennLandmark("r106", "Route 106", "R106", 0.080f, 0.750f, MapLandmarkType.ROUTE),
        hoennLandmark("r107", "Route 107", "R107", 0.180f, 0.820f, MapLandmarkType.ROUTE),
        hoennLandmark("r108", "Route 108", "R108", 0.240f, 0.820f, MapLandmarkType.ROUTE),
        hoennLandmark("r109", "Route 109", "R109", 0.320f, 0.740f, MapLandmarkType.ROUTE),
        hoennLandmark("r110", "Route 110 (Cycling Road)", "R110", 0.325f, 0.535f, MapLandmarkType.ROUTE),
        hoennLandmark("r111", "Route 111 (Desert)", "R111", 0.330f, 0.320f, MapLandmarkType.ROUTE),
        hoennLandmark("r112", "Route 112 (Mt. Chimney)", "R112", 0.275f, 0.280f, MapLandmarkType.ROUTE),
        hoennLandmark("r113", "Route 113", "R113", 0.185f, 0.120f, MapLandmarkType.ROUTE),
        hoennLandmark("r114", "Route 114", "R114", 0.100f, 0.180f, MapLandmarkType.ROUTE),
        hoennLandmark("r115", "Route 115", "R115", 0.050f, 0.280f, MapLandmarkType.ROUTE),
        hoennLandmark("r116", "Route 116 (Rusturf)", "R116", 0.115f, 0.400f, MapLandmarkType.ROUTE),
        hoennLandmark("r117", "Route 117 (Day Care)", "R117", 0.255f, 0.420f, MapLandmarkType.ROUTE),
        hoennLandmark("r118", "Route 118", "R118", 0.400f, 0.420f, MapLandmarkType.ROUTE),
        hoennLandmark("r119", "Route 119 (Weather Inst.)", "R119", 0.400f, 0.270f, MapLandmarkType.ROUTE),
        hoennLandmark("r120", "Route 120 (Bridge)", "R120", 0.530f, 0.195f, MapLandmarkType.ROUTE),
        hoennLandmark("r121", "Route 121 (Safari Zone)", "R121", 0.580f, 0.270f, MapLandmarkType.ROUTE),
        hoennLandmark("r123", "Route 123 (Berry Master)", "R123", 0.460f, 0.420f, MapLandmarkType.ROUTE),
        hoennLandmark("r124", "Route 124", "R124", 0.760f, 0.360f, MapLandmarkType.ROUTE),
        hoennLandmark("r125", "Route 125", "R125", 0.860f, 0.270f, MapLandmarkType.ROUTE),
        hoennLandmark("r126", "Route 126", "R126", 0.750f, 0.400f, MapLandmarkType.ROUTE),
        hoennLandmark("r127", "Route 127", "R127", 0.860f, 0.470f, MapLandmarkType.ROUTE),
        hoennLandmark("r128", "Route 128 (Seafloor Cavern)", "R128", 0.860f, 0.560f, MapLandmarkType.ROUTE),
        hoennLandmark("r129", "Route 129", "R129", 0.780f, 0.620f, MapLandmarkType.ROUTE),
        hoennLandmark("r130", "Route 130", "R130", 0.700f, 0.620f, MapLandmarkType.ROUTE),
        hoennLandmark("r131", "Route 131", "R131", 0.660f, 0.620f, MapLandmarkType.ROUTE),
        hoennLandmark("r132", "Route 132", "R132", 0.560f, 0.620f, MapLandmarkType.ROUTE),
        hoennLandmark("r133", "Route 133", "R133", 0.500f, 0.620f, MapLandmarkType.ROUTE),
        hoennLandmark("r134", "Route 134 (Sealed Chamber)", "R134", 0.440f, 0.620f, MapLandmarkType.SECRET),

        // Landmarks & Dungeons (POIs, Entrances, Secrets, Puzzles)
        hoennLandmark("trick_house", "Trick House Puzzle Mazes", "Trick House", 0.325f, 0.550f, MapLandmarkType.PUZZLE),
        hoennLandmark("meteor_falls", "Meteor Falls Cavern Entrance", "Meteor Falls", 0.080f, 0.250f, MapLandmarkType.ENTRANCE),
        hoennLandmark("mt_chimney", "Mt. Chimney Volcano", "Mt Chimney", 0.260f, 0.240f, MapLandmarkType.POI),
        hoennLandmark("mt_pyre", "Mt. Pyre Mountain", "Mt. Pyre", 0.520f, 0.380f, MapLandmarkType.POI),
        hoennLandmark("sky_pillar", "Sky Pillar (Rayquaza Ascent)", "Sky Pillar", 0.680f, 0.600f, MapLandmarkType.SECRET),
        hoennLandmark("shoal_cave", "Shoal Cave Tide Entrance", "Shoal Cave", 0.860f, 0.250f, MapLandmarkType.ENTRANCE),
        hoennLandmark("battle_frontier", "Battle Frontier Facilities", "Frontier", 0.850f, 0.750f, MapLandmarkType.PUZZLE),
        hoennLandmark("faraway_island", "Faraway Island (Mew)", "Faraway Is.", 0.850f, 0.850f, MapLandmarkType.SECRET)
    )

    // -------------------------------------------------------------
    // 5. SINNOH (Diamond, Pearl, Platinum)
    // -------------------------------------------------------------
    val SINNOH_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("twinleaf", "Twinleaf Town", "Twinleaf", 0.20f, 0.75f, MapLandmarkType.TOWN),
        MapLandmark("sandgem", "Sandgem Town", "Sandgem", 0.40f, 0.75f, MapLandmarkType.TOWN),
        MapLandmark("jubilife", "Jubilife City", "Jubilife", 0.30f, 0.65f, MapLandmarkType.CITY),
        MapLandmark("oreburgh", "Oreburgh City", "Oreburgh", 0.45f, 0.68f, MapLandmarkType.CITY),
        MapLandmark("floaroma", "Floaroma Town", "Floaroma", 0.30f, 0.55f, MapLandmarkType.TOWN),
        MapLandmark("eterna", "Eterna City", "Eterna", 0.42f, 0.40f, MapLandmarkType.CITY),
        MapLandmark("hearthome", "Hearthome City", "Hearthome", 0.55f, 0.55f, MapLandmarkType.CITY),
        MapLandmark("solaceon", "Solaceon Town", "Solaceon", 0.62f, 0.55f, MapLandmarkType.TOWN),
        MapLandmark("veilstone", "Veilstone City", "Veilstone", 0.75f, 0.45f, MapLandmarkType.CITY),
        MapLandmark("pastoria", "Pastoria City", "Pastoria", 0.62f, 0.75f, MapLandmarkType.CITY),
        MapLandmark("celestic", "Celestic Town", "Celestic", 0.52f, 0.45f, MapLandmarkType.TOWN),
        MapLandmark("canalave", "Canalave City", "Canalave", 0.15f, 0.60f, MapLandmarkType.CITY),
        MapLandmark("snowpoint", "Snowpoint City", "Snowpoint", 0.50f, 0.15f, MapLandmarkType.CITY),
        MapLandmark("sunyshore", "Sunyshore City", "Sunyshore", 0.90f, 0.60f, MapLandmarkType.CITY),
        MapLandmark("league", "Pokémon League", "League", 0.92f, 0.45f, MapLandmarkType.POI),

        MapLandmark("r201", "Route 201", "R201", 0.30f, 0.75f, MapLandmarkType.ROUTE),
        MapLandmark("r202", "Route 202", "R202", 0.35f, 0.70f, MapLandmarkType.ROUTE),
        MapLandmark("r203", "Route 203", "R203", 0.38f, 0.68f, MapLandmarkType.ROUTE),
        MapLandmark("r204", "Route 204", "R204", 0.30f, 0.60f, MapLandmarkType.ROUTE),
        MapLandmark("r205", "Route 205", "R205", 0.35f, 0.48f, MapLandmarkType.ROUTE),
        MapLandmark("r206", "Route 206 (Cycling Road)", "R206", 0.42f, 0.50f, MapLandmarkType.ROUTE),
        MapLandmark("r209", "Route 209 (Hallowed Tower)", "R209", 0.58f, 0.55f, MapLandmarkType.ROUTE),
        MapLandmark("r210", "Route 210 (Psyduck Blockade)", "R210", 0.60f, 0.42f, MapLandmarkType.ROUTE),
        MapLandmark("r212", "Route 212 (Pokémon Mansion)", "R212", 0.55f, 0.65f, MapLandmarkType.ROUTE),
        MapLandmark("r214", "Route 214", "R214", 0.75f, 0.60f, MapLandmarkType.ROUTE),
        MapLandmark("r215", "Route 215", "R215", 0.68f, 0.45f, MapLandmarkType.ROUTE),
        MapLandmark("r222", "Route 222", "R222", 0.82f, 0.60f, MapLandmarkType.ROUTE),

        MapLandmark("spear_pillar", "Spear Pillar (Dialga / Palkia / Arceus)", "Spear Pillar", 0.50f, 0.35f, MapLandmarkType.SECRET),
        MapLandmark("lake_verity", "Lake Verity", "L. Verity", 0.20f, 0.70f, MapLandmarkType.POI),
        MapLandmark("lake_valor", "Lake Valor", "L. Valor", 0.70f, 0.60f, MapLandmarkType.POI),
        MapLandmark("lake_acuity", "Lake Acuity", "L. Acuity", 0.45f, 0.15f, MapLandmarkType.POI),
        MapLandmark("newmoon", "Newmoon Island (Darkrai)", "Newmoon Is.", 0.20f, 0.20f, MapLandmarkType.SECRET),
        MapLandmark("flower_paradise", "Flower Paradise (Shaymin)", "Flower Paradise", 0.88f, 0.15f, MapLandmarkType.SECRET),
        MapLandmark("mt_coronet_ent", "Mt. Coronet Cavern Entrance", "Mt Coronet", 0.50f, 0.50f, MapLandmarkType.ENTRANCE),
        MapLandmark("solaceon_ruins", "Solaceon Ruins (Unown Labyrinth)", "Solaceon R.", 0.65f, 0.52f, MapLandmarkType.PUZZLE)
    )

    // -------------------------------------------------------------
    // 6. UNOVA (Black & White, Black 2 & White 2)
    // -------------------------------------------------------------
    val UNOVA_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("nuvema", "Nuvema Town", "Nuvema", 0.65f, 0.88f, MapLandmarkType.TOWN),
        MapLandmark("accumula", "Accumula Town", "Accumula", 0.65f, 0.75f, MapLandmarkType.TOWN),
        MapLandmark("striaton", "Striaton City", "Striaton", 0.65f, 0.62f, MapLandmarkType.CITY),
        MapLandmark("nacrene", "Nacrene City", "Nacrene", 0.52f, 0.65f, MapLandmarkType.CITY),
        MapLandmark("castelia", "Castelia City", "Castelia", 0.50f, 0.85f, MapLandmarkType.CITY),
        MapLandmark("nimbasa", "Nimbasa City", "Nimbasa", 0.50f, 0.55f, MapLandmarkType.CITY),
        MapLandmark("driftveil", "Driftveil City", "Driftveil", 0.35f, 0.55f, MapLandmarkType.CITY),
        MapLandmark("mistralton", "Mistralton City", "Mistralton", 0.25f, 0.38f, MapLandmarkType.CITY),
        MapLandmark("icirrus", "Icirrus City", "Icirrus", 0.38f, 0.25f, MapLandmarkType.CITY),
        MapLandmark("opelucid", "Opelucid City", "Opelucid", 0.62f, 0.25f, MapLandmarkType.CITY),
        MapLandmark("undella", "Undella Town", "Undella", 0.85f, 0.42f, MapLandmarkType.TOWN),
        MapLandmark("lacunosa", "Lacunosa Town", "Lacunosa", 0.78f, 0.28f, MapLandmarkType.TOWN),
        MapLandmark("aspertia", "Aspertia City", "Aspertia", 0.20f, 0.78f, MapLandmarkType.CITY),
        MapLandmark("virbank", "Virbank City", "Virbank", 0.35f, 0.82f, MapLandmarkType.CITY),

        MapLandmark("r1", "Route 1", "R1", 0.65f, 0.82f, MapLandmarkType.ROUTE),
        MapLandmark("r4", "Route 4 (Desert Resort)", "R4", 0.50f, 0.72f, MapLandmarkType.ROUTE),
        MapLandmark("r5", "Route 5", "R5", 0.42f, 0.55f, MapLandmarkType.ROUTE),
        MapLandmark("r9", "Route 9 (Shopping Mall Nine)", "R9", 0.70f, 0.25f, MapLandmarkType.ROUTE),
        MapLandmark("liberty_garden", "Liberty Garden (Victini)", "Liberty Garden", 0.40f, 0.95f, MapLandmarkType.SECRET),
        MapLandmark("giant_chasm", "Giant Chasm (Kyurem)", "Giant Chasm", 0.82f, 0.30f, MapLandmarkType.SECRET),
        MapLandmark("chargestone", "Chargestone Cave Entrance", "Chargestone", 0.30f, 0.45f, MapLandmarkType.ENTRANCE),
        MapLandmark("relic_castle", "Relic Castle Quicksand Maze", "Relic Castle", 0.48f, 0.68f, MapLandmarkType.PUZZLE)
    )

    // -------------------------------------------------------------
    // 7. KALOS (X & Y)
    // -------------------------------------------------------------
    val KALOS_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("vaniville", "Vaniville Town", "Vaniville", 0.58f, 0.88f, MapLandmarkType.TOWN),
        MapLandmark("santalune", "Santalune City", "Santalune", 0.58f, 0.70f, MapLandmarkType.CITY),
        MapLandmark("lumiose", "Lumiose City (Prism Tower)", "Lumiose", 0.50f, 0.45f, MapLandmarkType.CITY),
        MapLandmark("camphrier", "Camphrier Town", "Camphrier", 0.62f, 0.55f, MapLandmarkType.TOWN),
        MapLandmark("cyllage", "Cyllage City", "Cyllage", 0.25f, 0.68f, MapLandmarkType.CITY),
        MapLandmark("geosenge", "Geosenge Town", "Geosenge", 0.22f, 0.50f, MapLandmarkType.TOWN),
        MapLandmark("shalour", "Shalour City (Tower of Mastery)", "Shalour", 0.25f, 0.35f, MapLandmarkType.CITY),
        MapLandmark("coumarine", "Coumarine City", "Coumarine", 0.45f, 0.20f, MapLandmarkType.CITY),
        MapLandmark("laverre", "Laverre City", "Laverre", 0.55f, 0.25f, MapLandmarkType.CITY),
        MapLandmark("dendemille", "Dendemille Town", "Dendemille", 0.75f, 0.35f, MapLandmarkType.TOWN),
        MapLandmark("anistar", "Anistar City (Sundial)", "Anistar", 0.82f, 0.45f, MapLandmarkType.CITY),
        MapLandmark("snowbelle", "Snowbelle City", "Snowbelle", 0.68f, 0.75f, MapLandmarkType.CITY),

        MapLandmark("r1", "Route 1 (Vaniville Pathway)", "R1", 0.58f, 0.80f, MapLandmarkType.ROUTE),
        MapLandmark("r4", "Route 4 (Parterre Way)", "R4", 0.54f, 0.58f, MapLandmarkType.ROUTE),
        MapLandmark("r7", "Route 7 (Rivière Walk)", "R7", 0.35f, 0.55f, MapLandmarkType.ROUTE),
        MapLandmark("r13", "Route 13 (Lumiose Badlands)", "R13", 0.42f, 0.38f, MapLandmarkType.ROUTE),
        MapLandmark("r14", "Route 14 (Laverre Nature Trail)", "R14", 0.52f, 0.35f, MapLandmarkType.ROUTE),
        MapLandmark("glittering_cave", "Glittering Cave Entrance", "Glittering", 0.28f, 0.60f, MapLandmarkType.ENTRANCE),
        MapLandmark("terminus_cave", "Terminus Cave (Zygarde)", "Terminus", 0.80f, 0.38f, MapLandmarkType.SECRET),
        MapLandmark("tower_mastery", "Tower of Mastery (Trial)", "Tower Mast.", 0.22f, 0.32f, MapLandmarkType.PUZZLE)
    )

    // -------------------------------------------------------------
    // 8. ALOLA (Sun, Moon & Ultra Sun, Ultra Moon)
    // -------------------------------------------------------------
    val ALOLA_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("hauoli", "Hau'oli City", "Hau'oli", 0.15f, 0.48f, MapLandmarkType.CITY),
        MapLandmark("iki_town", "Iki Town", "Iki", 0.20f, 0.38f, MapLandmarkType.TOWN),
        MapLandmark("heahea", "Heahea City", "Heahea", 0.45f, 0.35f, MapLandmarkType.CITY),
        MapLandmark("paniola", "Paniola Town", "Paniola", 0.48f, 0.42f, MapLandmarkType.TOWN),
        MapLandmark("konikoni", "Konikoni City", "Konikoni", 0.45f, 0.55f, MapLandmarkType.CITY),
        MapLandmark("malie", "Malie City", "Malie", 0.75f, 0.35f, MapLandmarkType.CITY),
        MapLandmark("po_town", "Po Town", "Po Town", 0.68f, 0.35f, MapLandmarkType.TOWN),
        MapLandmark("seafolk", "Seafolk Village", "Seafolk", 0.20f, 0.75f, MapLandmarkType.TOWN),
        MapLandmark("aether", "Aether Paradise", "Aether", 0.50f, 0.50f, MapLandmarkType.POI),
        MapLandmark("lanakila", "Mount Lanakila (League)", "Mt Lanakila", 0.70f, 0.25f, MapLandmarkType.POI),

        MapLandmark("r1", "Route 1 (Melemele)", "R1", 0.18f, 0.44f, MapLandmarkType.ROUTE),
        MapLandmark("r2", "Route 2", "R2", 0.18f, 0.32f, MapLandmarkType.ROUTE),
        MapLandmark("r4", "Route 4 (Akala)", "R4", 0.46f, 0.38f, MapLandmarkType.ROUTE),
        MapLandmark("r8", "Route 8 (Akala Coast)", "R8", 0.42f, 0.28f, MapLandmarkType.ROUTE),
        MapLandmark("r10", "Route 10 (Mount Hokulani)", "R10", 0.72f, 0.30f, MapLandmarkType.ROUTE),
        MapLandmark("digletts_tunnel", "Diglett's Tunnel Entrance", "Diglett Tun.", 0.42f, 0.52f, MapLandmarkType.ENTRANCE),
        MapLandmark("resolution_cave", "Resolution Cave (Guzzlord)", "Resolution", 0.25f, 0.80f, MapLandmarkType.SECRET),
        MapLandmark("wela_volcano", "Wela Volcano (Totem Trial)", "Wela Park", 0.48f, 0.32f, MapLandmarkType.PUZZLE)
    )

    // -------------------------------------------------------------
    // 9. GALAR (Sword & Shield)
    // -------------------------------------------------------------
    val GALAR_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("postwick", "Postwick", "Postwick", 0.50f, 0.90f, MapLandmarkType.TOWN),
        MapLandmark("wedgehurst", "Wedgehurst", "Wedgehurst", 0.50f, 0.82f, MapLandmarkType.TOWN),
        MapLandmark("motostoke", "Motostoke", "Motostoke", 0.50f, 0.65f, MapLandmarkType.CITY),
        MapLandmark("turffield", "Turffield", "Turffield", 0.35f, 0.55f, MapLandmarkType.TOWN),
        MapLandmark("hulbury", "Hulbury", "Hulbury", 0.65f, 0.55f, MapLandmarkType.CITY),
        MapLandmark("hammerlocke", "Hammerlocke", "Hammerlocke", 0.50f, 0.40f, MapLandmarkType.CITY),
        MapLandmark("stow_on_side", "Stow-on-Side", "Stow-on-Side", 0.35f, 0.38f, MapLandmarkType.TOWN),
        MapLandmark("ballonlea", "Ballonlea", "Ballonlea", 0.30f, 0.30f, MapLandmarkType.TOWN),
        MapLandmark("circhester", "Circhester", "Circhester", 0.65f, 0.30f, MapLandmarkType.CITY),
        MapLandmark("spikemuth", "Spikemuth", "Spikemuth", 0.72f, 0.35f, MapLandmarkType.TOWN),
        MapLandmark("wyndon", "Wyndon (Champion Cup)", "Wyndon", 0.50f, 0.15f, MapLandmarkType.CITY),

        MapLandmark("r1", "Route 1", "R1", 0.50f, 0.86f, MapLandmarkType.ROUTE),
        MapLandmark("r2", "Route 2", "R2", 0.50f, 0.76f, MapLandmarkType.ROUTE),
        MapLandmark("r3", "Route 3", "R3", 0.45f, 0.70f, MapLandmarkType.ROUTE),
        MapLandmark("wild_area", "Wild Area (Meetup Spot)", "Wild Area", 0.50f, 0.60f, MapLandmarkType.POI),
        MapLandmark("r6", "Route 6", "R6", 0.40f, 0.42f, MapLandmarkType.ROUTE),
        MapLandmark("r10", "Route 10", "R10", 0.50f, 0.22f, MapLandmarkType.ROUTE),
        MapLandmark("galar_mine", "Galar Mine Entrance", "Galar Mine", 0.48f, 0.60f, MapLandmarkType.ENTRANCE),
        MapLandmark("slumbering_weald", "Slumbering Weald (Shrine)", "S. Weald", 0.45f, 0.92f, MapLandmarkType.SECRET),
        MapLandmark("glimwood_tangle", "Glimwood Tangle Maze", "Glimwood", 0.32f, 0.33f, MapLandmarkType.PUZZLE)
    )

    // -------------------------------------------------------------
    // 10. HISUI (Legends: Arceus)
    // -------------------------------------------------------------
    val HISUI_LANDMARKS: List<MapLandmark> = listOf(
        MapLandmark("jubilife_village", "Jubilife Village", "Jubilife Vil.", 0.35f, 0.60f, MapLandmarkType.CITY),
        MapLandmark("obsidian", "Obsidian Fieldlands", "Fieldlands", 0.38f, 0.75f, MapLandmarkType.POI),
        MapLandmark("crimson", "Crimson Mirelands", "Mirelands", 0.65f, 0.68f, MapLandmarkType.POI),
        MapLandmark("cobalt", "Cobalt Coastlands", "Coastlands", 0.85f, 0.45f, MapLandmarkType.POI),
        MapLandmark("coronet", "Coronet Highlands", "Highlands", 0.50f, 0.45f, MapLandmarkType.POI),
        MapLandmark("alabaster", "Alabaster Icelands", "Icelands", 0.45f, 0.18f, MapLandmarkType.POI),
        MapLandmark("grandtree", "Grandtree Arena (Kleavor)", "Grandtree", 0.42f, 0.82f, MapLandmarkType.PUZZLE),
        MapLandmark("brava_arena", "Brava Arena (Lilligant)", "Brava Arena", 0.68f, 0.75f, MapLandmarkType.PUZZLE),
        MapLandmark("molten_arena", "Molten Arena (Arcanine)", "Molten Arena", 0.88f, 0.38f, MapLandmarkType.PUZZLE),
        MapLandmark("temple_sinnoh", "Temple of Sinnoh (Mt Coronet)", "Temple Sinnoh", 0.50f, 0.42f, MapLandmarkType.SECRET),
        MapLandmark("wayward_cave", "Wayward Cave Entrance", "Wayward Cave", 0.52f, 0.48f, MapLandmarkType.ENTRANCE)
    )

    // -------------------------------------------------------------
    // 11. PALDEA (Scarlet & Violet)
    // -------------------------------------------------------------
    val PALDEA_LANDMARK_DETAILS: Map<String, LandmarkGuideDetails> = mapOf(
        "cabo_poco" to LandmarkGuideDetails(
            tagline = "Where the path to treasure begins!",
            description = "A peaceful coastal hamlet situated on the southern tip of the Paldea region, home to the player character and Nemona.",
            keyLocations = listOf(
                "🏠 Player's House: Starting point of the journey with Mom",
                "🏡 Nemona's Mansion: Sprawling beachside estate with a private battlefield",
                "🌊 Cabo Poco Beach: Scenic shore meeting the South Paldean Sea"
            ),
            connectedAreas = listOf("Poco Path (North toward Mesagoza)"),
            guideAdvice = "Choose your starter Pokémon (Sprigatito, Fuecoco, or Quaxly) and battle Nemona on the private beach before setting out on Poco Path."
        ),
        "los_platos" to LandmarkGuideDetails(
            tagline = "The town of abundant plates and pottery!",
            description = "A quaint pastoral town known for its ceramic artisans, windmill, and bustling market stalls.",
            keyLocations = listOf(
                "🏥 Los Platos Pokémon Center: First outdoor Pokémon Center with TM Machine and Union Circle terminal",
                "🏪 Market Vendors: Sells basic sandwich ingredients and cooking supplies",
                "💨 Hillside Windmill: Scenic landmark overlooking the lower valley"
            ),
            connectedAreas = listOf("Poco Path (South)", "South Province Area One (North toward Mesagoza)"),
            guideAdvice = "Learn how to use the TM Machine to craft powerful moves using wild Pokémon drops, and prepare sandwiches to boost capture power."
        ),
        "mesagoza" to LandmarkGuideDetails(
            tagline = "The Splendid City of Tradition and Learning!",
            description = "The vibrant academic capital of Paldea, centered around the grand clock tower and the renowned Naranja Academy (Scarlet) or Uva Academy (Violet).",
            keyLocations = listOf(
                "🎓 Naranja / Uva Academy: Clavell's office, biology lab, battle studies, and student dormitories",
                "🛍️ Central Plaza: Boutiques, Delibird Presents, and street food stalls",
                "🏥 North & South Pokémon Centers: Battle items and supply replenishment"
            ),
            connectedAreas = listOf("South Province (Area One)", "West Province (Area One)", "East Province (Area One)"),
            guideAdvice = "Receive your Rotom Phone, Pokédex, and ride Legendary from Director Clavell and Nemona. Stock up on Poké Balls before embarking on your Treasure Hunt."
        ),
        "cortondo" to LandmarkGuideDetails(
            tagline = "The olive-scented town of green groves!",
            description = "A charming agricultural town surrounded by olive orchards, home to the Bug-type Gym led by the master baker Katy.",
            gymInfo = "Leader: Katy • Specialty: Bug • Bug Badge (Lv. 14-15)",
            keyLocations = listOf(
                "🏟️ Cortondo Gym: Bug-type Gym led by Katy (Bug Badge)",
                "🫒 Olive Roll Field: Large grassy arena for the Gym Test",
                "🧁 Pâtisserie Soapberry: Famous bakery known for sweet confections"
            ),
            connectedAreas = listOf("South Province (Area Two)", "West Province (Area One)"),
            guideAdvice = "Complete the Olive Roll Gym Test by guiding a giant olive into the goal. Use Fire, Flying, or Rock moves against Katy's Teddiursa with Bug Tera Type."
        ),
        "artazon" to LandmarkGuideDetails(
            tagline = "The town of brass flowers and sunlit art!",
            description = "An artistic oasis filled with sculptures and flowering gardens, featuring the Grass-type Gym led by Brassius.",
            gymInfo = "Leader: Brassius • Specialty: Grass • Grass Badge (Lv. 16-17)",
            keyLocations = listOf(
                "🏟️ Artazon Gym: Grass-type Gym led by Brassius (Grass Badge)",
                "🌻 Sunflora Plaza: Playground where wild Sunflora gather for the Gym Test",
                "🎨 Brassius Sculpture Garden: Famous works including 'Surrendering Sunflora'"
            ),
            connectedAreas = listOf("South Province (Area Three)", "East Province (Area One)"),
            guideAdvice = "Gather 10 Sunflora scattered across Artazon to unlock the battle with Brassius. Watch out for his Sudowoodo Terastallizing into Grass type with Trailblaze."
        ),
        "levincia" to LandmarkGuideDetails(
            tagline = "The shining city of lights, streams, and streams!",
            description = "A hyper-modern coastal metropolis illuminated by holographic billboards, home to the Electric-type Gym and celebrity streamer Iono.",
            gymInfo = "Leader: Iono • Specialty: Electric • Electric Badge (Lv. 23-24)",
            keyLocations = listOf(
                "🏟️ Levincia Gym: Electric-type Gym led by Iono (Electric Badge)",
                "📺 Iono Zone Streaming Stage: Broadcast studio where Clavell participates in the hide-and-seek test",
                "🏙️ Levincia Central Skyscraper: Modern corporate headquarters"
            ),
            connectedAreas = listOf("East Province (Area Two)", "East Province (Area Three)"),
            guideAdvice = "Find Director Clavell in Iono's live stream hide-and-seek test. Beware of Iono's Mismagius with Levitate and Electric Tera Type, which has zero type weaknesses!"
        ),
        "cascarrafa" to LandmarkGuideDetails(
            tagline = "The Oasis City of tiered waterfalls!",
            description = "A stunning desert oasis built on multi-tiered water elevators, featuring the Water-type Gym led by master chef Kofu.",
            gymInfo = "Leader: Kofu • Specialty: Water • Water Badge (Lv. 29-30)",
            keyLocations = listOf(
                "🏟️ Cascarrafa Gym: Water-type Gym led by Kofu (Water Badge)",
                "🌊 Tiered Water Elevators: Moving water lifts connecting the three city terraces",
                "🍽️ Restaurant Kofu: Premier dining establishment"
            ),
            connectedAreas = listOf("Asado Desert", "West Province (Area One)"),
            guideAdvice = "Retrieve Kofu's forgotten wallet in Porto Marinada and win the seaweed auction. Electric and Grass attacks counter his Crabominable's Water Tera Type."
        ),
        "porto_marinada" to LandmarkGuideDetails(
            tagline = "The port of bustling market auctions!",
            description = "A lively seaside harbor famous for its daily open-air auctions where rare evolution items, berries, and Rotom catalog forms are sold.",
            keyLocations = listOf(
                "🏷️ Porto Marinada Market: Daily auction stalls selling rare items and apricorn balls",
                "⚓ Harbor Lighthouse: Overlooks the West Paldean Sea",
                "🏥 Pokémon Center: Healing station for desert travelers"
            ),
            connectedAreas = listOf("Asado Desert (East)", "West Province (Area Two)"),
            guideAdvice = "Check the auction daily with large sums of Poké Dollars to obtain Apricorn Balls, Golden Bottle Caps, and form-changing items for Rotom and Legendaries."
        ),
        "medali" to LandmarkGuideDetails(
            tagline = "The culinary town of hardworking diners!",
            description = "A bustling commercial town famous for hearty gastronomy and the Normal-type Gym led by the salaryman Larry.",
            gymInfo = "Leader: Larry • Specialty: Normal • Normal Badge (Lv. 35-36)",
            keyLocations = listOf(
                "🏟️ Medali Gym: Normal-type Gym led by Larry (Normal Badge)",
                "🍜 Treasure Eatery: Famous restaurant where the secret menu Gym Test is ordered",
                "🎁 League Representative: Awards the Amulet Coin after defeating 5 trainers"
            ),
            connectedAreas = listOf("West Province (Area Three)", "Dalizapa Passage"),
            guideAdvice = "Order 'Grilled rice balls, Medium serving, Extra crispy over Fire blast style, with Lemon' at Treasure Eatery to challenge Larry. Use Fighting moves against his Staraptor."
        ),
        "montenevera" to LandmarkGuideDetails(
            tagline = "The snowbound city of spectral rap!",
            description = "A frigid mountain retreat situated on the northern heights of Glaseado Mountain, home to the Ghost-type Gym and rapper Ryme.",
            gymInfo = "Leader: Ryme • Specialty: Ghost • Ghost Badge (Lv. 41-42)",
            keyLocations = listOf(
                "🏟️ Montenevera Gym: Ghost-type Gym featuring Double Battles led by Ryme (Ghost Badge)",
                "🎤 Concert Stage: Open-air arena with audience hype mechanics",
                "🏔️ Glaseado Slopes: Snowy cliffs leading to the Glaseado Mountain peak"
            ),
            connectedAreas = listOf("Glaseado Mountain", "North Province (Area One)"),
            guideAdvice = "Montenevera Gym matches are Double Battles! Boost your crowd hype by scoring KOs to gain stat buffs. Dark and Ghost types counter Ryme's Toxtricity."
        ),
        "alfornada" to LandmarkGuideDetails(
            tagline = "The town of starry observatories and pottery!",
            description = "A picturesque mountain city perched atop southwest cliffs, featuring an ancient observatory and the Psychic-type Gym led by makeup artist Tulip.",
            gymInfo = "Leader: Tulip • Specialty: Psychic • Psychic Badge (Lv. 44-45)",
            keyLocations = listOf(
                "🏟️ Alfornada Gym: Psychic-type Gym led by Tulip (Psychic Badge)",
                "🔭 Alfornada Observatory: Ancient stone observatory overlooking the southern ocean",
                "🏃 Dena's Emotional Calisthenics: Gym Test rhythm exercise"
            ),
            connectedAreas = listOf("Alfornada Cavern", "South Province (Area Six)"),
            guideAdvice = "Complete the Emotional Calisthenics button-matching minigame with Dena. Use Bug, Ghost, or Dark types against Tulip's Florges with Psychic Tera Type."
        ),
        "zapapico" to LandmarkGuideDetails(
            tagline = "The mining settlement of forge and armor!",
            description = "A rugged mining settlement nestled in a crater valley between East Province and the Great Crater, home to the armor artisan who crafts Auspicious Armor and Malicious Armor.",
            keyLocations = listOf(
                "🛡️ Armor Artisan: Trades 10 Bronzor Fragments for Auspicious Armor (Armarouge in Scarlet) or 10 Sinistea Chips for Malicious Armor (Ceruledge in Violet)",
                "⛏️ Zapapico Mining Quarries: Rich mineral extraction sites",
                "🏥 East & West Pokémon Centers: Healing hubs for mountain routes"
            ),
            connectedAreas = listOf("East Province (Area Three)", "Tagtree Thicket"),
            guideAdvice = "Collect Bronzor Fragments (Scarlet) or Sinistea Chips (Violet) from wild Pokémon to trade for the special armor needed to evolve Charcadet into Armarouge or Ceruledge."
        ),
        "area_zero" to LandmarkGuideDetails(
            tagline = "The Unfathomable Depths of Paldea's Great Crater",
            description = "A colossal, cloud-veiled crater formed over 1,000,000 years ago by an ancient meteorite impact. At its bottom lies Area Zero and the Zero Lab, radiating immense Terastal crystal energy and harboring enigmatic Paradox Pokémon from distant epochs.",
            keyLocations = listOf(
                "🔬 Zero Gate: Modern rim facility controlling access to the descent transport lift",
                "🏠 Research Stations 1 to 4: Outposts constructed along the descent with security lock override consoles",
                "🌊 Subterranean Waterfalls: Mid-tier waterways and secret caves harboring Roaring Moon and Iron Valiant",
                "💎 Crystal Depths: Lower caverns coated in giant Terastal crystals and high-level wild Pokémon",
                "🏛️ The Zero Lab: Colossal subterranean research laboratory housing the Time Machine and AI Professor"
            ),
            connectedAreas = listOf("Paldea Surface Rim (Zero Gate)", "The Great Crater Rim"),
            guideAdvice = "To enter Area Zero, complete Victory Road, Path of Legends, and ★ Starfall Street. Meet Nemona, Arven, and Penny at the Zero Gate. Override the terminal locks at all 4 Research Stations to access the Zero Lab and halt the Time Machine."
        ),
        "poco_path" to LandmarkGuideDetails(
            tagline = "The coastal trail of fledgling trainers!",
            description = "The winding dirt pathway extending from Cabo Poco to the inlet lighthouse, where wild Pokémon roam among olive trees and seaside bluffs.",
            keyLocations = listOf(
                "🗼 Poco Path Lighthouse: Observation tower with Professor Sada/Turo's old laboratory",
                "🌊 Coastal Bluffs: Sea view where the injured Legendary Pokémon is first encountered",
                "🌿 Starter Route Grass: Home to Lechonk, Tarountula, and Fletchling"
            ),
            connectedAreas = listOf("Cabo Poco (South)", "Inlet Grotto (Base of Lighthouse)", "South Province Area One (North)"),
            guideAdvice = "Investigate the mysterious cry at the bluff to discover your version's Legendary Pokémon (Koraidon or Miraidon) and nurse it back to health with a sandwich."
        ),
        "south_prov1" to LandmarkGuideDetails(
            tagline = "The sweeping gateway to Central Paldea!",
            description = "Expansive open meadows and rocky hills stretching between Los Platos and the towering south gates of Mesagoza.",
            keyLocations = listOf(
                "🏥 South Province Pokémon Center: Hub for early-game exploration",
                "🌉 Mesagoza South Gate: Grand drawbridge leading into the academic capital",
                "🪨 Rocky Bluffs: Roaming grounds for Pawmi, Rookidee, and Smoliv"
            ),
            connectedAreas = listOf("Los Platos (South)", "Mesagoza (North)", "South Province Area Two (West)"),
            guideAdvice = "Defeat at least 4 trainers in South Province Area One to receive TM082 (Thunder Wave) from the Pokémon League Representative at the Pokémon Center."
        ),
        "east_prov1" to LandmarkGuideDetails(
            tagline = "The rugged badlands of wind and stone!",
            description = "Vast scrublands and rocky canyons connecting Artazon to the eastern coastline, home to wild Diglett, Orthworm, and Tandemaus.",
            keyLocations = listOf(
                "🏥 East Province Area One Pokémon Center: Rest stop for trainers travelling east",
                "🏜️ Eastern Canyons: Rocky outcroppings harboring wild Fighting and Rock Pokémon",
                "⛰️ Watchtower: Elevated observation post for Tera Raid crystals"
            ),
            connectedAreas = listOf("Artazon (West)", "East Province Area Two (North toward Levincia)"),
            guideAdvice = "Battle trainers in East Province Area One to earn the Loaded Dice battle item from the League Representative, which guarantees multi-strike moves hit maximum times."
        ),
        "west_prov1" to LandmarkGuideDetails(
            tagline = "The rolling hills of wind and windmills!",
            description = "Breezy green plateaus and rolling hills situated west of Mesagoza, dotted with towering wind turbines and wild Pokémon.",
            keyLocations = listOf(
                "🏥 West Province Area One Pokémon Center: Hub between Mesagoza and Cortondo",
                "💨 Wind Turbine Fields: Tall turbines generating clean energy for the region",
                "🌉 Cascarrafa Canyon Bridge: Overpass connecting to the Asado Desert"
            ),
            connectedAreas = listOf("Mesagoza (East)", "Cortondo (South)", "Cascarrafa (North)"),
            guideAdvice = "Inspect the windmill ledges to find hidden TM070 (Sleep Talk) and TM027 (Aerial Ace). Clear trainers to receive the Clear Amulet from the League Rep."
        ),
        "inlet_grotto" to LandmarkGuideDetails(
            tagline = "The shadowy sea cavern beneath the lighthouse!",
            description = "A dark sea cave carved into the cliffs beneath Poco Path Lighthouse, where the player first journeys alongside their weakened Legendary Pokémon.",
            keyLocations = listOf(
                "🌊 Subterranean Tidal Cavern: Damp tunnels inhabited by wild Diglett and Houndour",
                "🐺 Houndoom's Lair: Deep cavern chamber where an aggressive Houndoom pack confronts the player",
                "🪜 Lighthouse Escape Cave: Ascending path leading directly to Arven's lighthouse lab"
            ),
            connectedAreas = listOf("Poco Path Bluff (Entrance)", "Poco Path Lighthouse (Exit)"),
            guideAdvice = "Follow your companion Legendary Pokémon through the cave. The Legendary will smash the blocking boulders and leap up the cliff face to rescue you from the Houndoom pack."
        ),
        "cortondo_trial" to LandmarkGuideDetails(
            tagline = "The rolling fun of the Olive Harvest!",
            description = "The official Cortondo Gym Test arena where challengers must maneuver an oversized bouncing olive through a maze of obstacles into the goal.",
            keyLocations = listOf(
                "🫒 Giant Olive Arena: Sloped grass field with wooden hurdles and barriers",
                "🧑‍🌾 Cortondo Test Stewards: Gym trainers positioned along the track",
                "🥅 Harvest Goal: Final basket where scoring unlocks the battle with Gym Leader Katy"
            ),
            connectedAreas = listOf("Cortondo Town"),
            guideAdvice = "You can bump the olive with your ride Pokémon or on foot. Defeat the optional Gym Trainer shortcuts along the track to open a direct lane to the goal!"
        )
    )

    val PALDEA_LANDMARKS: List<MapLandmark> = listOf(
        paldeaLandmark("cabo_poco", "Cabo Poco", "Cabo Poco", 0.38f, 0.92f, MapLandmarkType.TOWN),
        paldeaLandmark("los_platos", "Los Platos", "Los Platos", 0.42f, 0.85f, MapLandmarkType.TOWN),
        paldeaLandmark("mesagoza", "Mesagoza (Naranja/Uva)", "Mesagoza", 0.50f, 0.70f, MapLandmarkType.CITY),
        paldeaLandmark("cortondo", "Cortondo", "Cortondo", 0.35f, 0.75f, MapLandmarkType.TOWN),
        paldeaLandmark("artazon", "Artazon", "Artazon", 0.65f, 0.70f, MapLandmarkType.TOWN),
        paldeaLandmark("levincia", "Levincia", "Levincia", 0.85f, 0.60f, MapLandmarkType.CITY),
        paldeaLandmark("cascarrafa", "Cascarrafa", "Cascarrafa", 0.28f, 0.55f, MapLandmarkType.CITY),
        paldeaLandmark("porto_marinada", "Porto Marinada", "P. Marinada", 0.15f, 0.48f, MapLandmarkType.TOWN),
        paldeaLandmark("medali", "Medali", "Medali", 0.38f, 0.42f, MapLandmarkType.TOWN),
        paldeaLandmark("montenevera", "Montenevera", "Montenevera", 0.45f, 0.25f, MapLandmarkType.TOWN),
        paldeaLandmark("alfornada", "Alfornada", "Alfornada", 0.18f, 0.88f, MapLandmarkType.TOWN),
        paldeaLandmark("zapapico", "Zapapico", "Zapapico", 0.68f, 0.52f, MapLandmarkType.TOWN),
        paldeaLandmark("area_zero", "Great Crater of Paldea (Area Zero)", "Area Zero", 0.50f, 0.50f, MapLandmarkType.SECRET),

        paldeaLandmark("poco_path", "Poco Path", "Poco Path", 0.40f, 0.89f, MapLandmarkType.ROUTE),
        paldeaLandmark("south_prov1", "South Province (Area One)", "S. Prov 1", 0.48f, 0.82f, MapLandmarkType.ROUTE),
        paldeaLandmark("east_prov1", "East Province (Area One)", "E. Prov 1", 0.72f, 0.66f, MapLandmarkType.ROUTE),
        paldeaLandmark("west_prov1", "West Province (Area One)", "W. Prov 1", 0.30f, 0.65f, MapLandmarkType.ROUTE),
        paldeaLandmark("inlet_grotto", "Inlet Grotto Entrance", "Inlet Grotto", 0.40f, 0.90f, MapLandmarkType.ENTRANCE),
        paldeaLandmark("cortondo_trial", "Olive Roll Trial", "Olive Roll", 0.33f, 0.72f, MapLandmarkType.PUZZLE)
    )
}
