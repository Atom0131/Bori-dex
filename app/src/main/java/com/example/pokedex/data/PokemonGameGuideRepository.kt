package com.example.pokedex.data

import android.content.Context
import org.json.JSONObject

data class GuideQueryResult(
    val title: String,
    val category: String,
    val editionName: String,
    val location: String,
    val summary: String,
    val steps: List<String>,
    val prerequisites: String?,
    val mapAsset: String?,
    val recommendedItems: List<String> = emptyList(),
    val spokenAdvice: String,
    val pinX: Float? = null,
    val pinY: Float? = null,
    val pinLocationName: String? = null,
    val firstEncounter: String? = null,
    val subsequentAvailability: String? = null,
    val itemIconAsset: String? = null
)

data class GuideCategory(
    val id: String,
    val title: String,
    val emoji: String,
    val subtitle: String,
    val topicIds: List<String>,
    val iconAsset: String = PokemonGameGuideRepository.CATEGORY_ICONS[id] ?: "poke-ball.png"
)

data class GuideTopicItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val aliases: List<String>,
    val iconAsset: String? = null
)

object PokemonGameGuideRepository {

    private var cachedData: JSONObject? = null

    val CATEGORY_ICONS: Map<String, String> = mapOf(
        "healing_items" to "potion.png",
        "key_items" to "key-items.png",
        "money_items" to "amulet-coin.png",
        "event_items" to "eon-ticket.png",
        "evolution_items" to "fire-stone.png",
        "battle_items" to "exp-share.png",
        "pokeballs" to "master-ball.png",
        "missions_roadblocks" to "poke-flute.png",
        "hidden_items" to "dowsing-machine.png",
        "secret_messages_braille" to "relic-copper.png",
        "legendaries" to "weather-trio-wall.png",
        "event_pokemon" to "cherish-ball.png",
        "puzzles_dungeons" to "escape-rope.png"
    )

    val TOPIC_ITEM_ICONS: Map<String, String> = mapOf(
        "potion" to "potion.png",
        "hyper_max_potion" to "hyper-potion.png",
        "full_restore" to "full-restore.png",
        "revive_items" to "revive.png",
        "max_revive" to "max-revive.png",
        "sacred_ash" to "sacred-ash.png",
        "status_heals" to "full-heal.png",
        "natural_drinks" to "moomoo-milk.png",
        "oran_berry" to "oran-berry.png",
        "sitrus_berry" to "sitrus-berry.png",
        "lum_berry" to "lum-berry.png",
        "leppa_berry" to "leppa-berry.png",
        "status_berries" to "cheri-berry.png",
        "pokeball_types" to "poke-ball.png",
        "ultra_ball" to "ultra-ball.png",
        "master_ball" to "master-ball.png",
        "quick_ball" to "quick-ball.png",
        "dusk_ball" to "dusk-ball.png",
        "timer_ball" to "timer-ball.png",
        "amulet_coin" to "amulet-coin.png",
        "nugget" to "nugget.png",
        "big_pearl" to "big-pearl.png",
        "comet_shard" to "comet-shard.png",
        "relic_gold" to "relic-gold.png",
        "bicycle" to "bicycle.png",
        "fishing_rods" to "super-rod.png",
        "poke_flute" to "poke-flute.png",
        "silph_scope" to "silph-scope.png",
        "devon_scope" to "scope-lens.png",
        "running_shoes" to "running-shoes.png",
        "dowsing_machine" to "dowsing-machine.png",
        "squirt_bottle" to "squirt-bottle.png",
        "sprayduck" to "sprayduck.png",
        "secret_potion" to "potion.png",
        "leftovers" to "leftovers.png",
        "choice_items" to "choice-band.png",
        "focus_sash" to "focus-sash.png",
        "life_orb" to "life-orb.png",
        "eviolite" to "eviolite.png",
        "heavy_duty_boots" to "heavy-duty-boots.png",
        "assault_vest" to "assault-vest.png",
        "rocky_helmet" to "rocky-helmet.png",
        "soothe_bell" to "soothe-bell.png",
        "cleanse_tag" to "cleanse-tag.png",
        "smoke_ball" to "smoke-ball.png",
        "rare_candy" to "rare-candy.png",
        "vitamins" to "protein.png",
        "pp_up" to "pp-up.png",
        "ability_capsule" to "ability-capsule.png",
        "ability_patch" to "ability-patch.png",
        "repel" to "repel.png",
        "super_repel" to "super-repel.png",
        "max_repel" to "max-repel.png",
        "escape_rope" to "escape-rope.png",
        "poke_doll" to "poke-doll.png",
        "fluffy_tail" to "fluffy-tail.png",
        "honey" to "honey.png",
        "evolution_stones" to "fire-stone.png",
        "metal_coat" to "metal-coat.png",
        "kings_rock" to "kings-rock.png",
        "dragon_scale" to "dragon-scale.png",
        "upgrade_dubious" to "upgrade.png",
        "protector" to "protector.png",
        "link_cable" to "link-cable.png",
        "old_sea_map" to "old-sea-map.png",
        "eon_ticket" to "eon-ticket.png",
        "aurora_ticket" to "eon-ticket.png",
        "mystic_ticket" to "eon-ticket.png",
        "member_card" to "member-card.png",
        "oaks_letter" to "oaks-letter.png",
        "liberty_pass" to "member-card.png",
        "azure_flute" to "azure-flute.png",
        "exp_share" to "exp-share.png",
        "sudowoodo" to "squirt-bottle.png",
        "snorlax" to "poke-flute.png",
        "psyduck_headache" to "potion.png",
        "route_110_blockade" to "bicycle.png",
        "devon_goods_quest" to "silph-scope.png",
        "silph_co_invasion" to "silph-scope.png",
        "power_plant_part" to "metal-coat.png",
        "dowsing_radar_spots" to "dowsing-machine.png",
        "hidden_rare_candies" to "rare-candy.png",
        "buried_fossils" to "peat-block.png",
        "daily_berry_trees" to "sitrus-berry.png",
        "sealed_chamber_braille" to "old-sea-map.png",
        "ruin_of_alph_puzzles" to "relic-copper.png",
        "tanoby_ruins_cipher" to "relic-silver.png",
        "abyssal_ruins_alphabet" to "relic-gold.png",
        "mewtwo_encounter" to "master-ball.png",
        "kanto_birds" to "ultra-ball.png",
        "rayquaza_encounter" to "timer-ball.png",
        "weather_trio" to "weather-trio-wall.png",
        "regi_trio" to "heavy-ball.png",
        "treasures_of_ruin_stakes" to "dusk-ball.png",
        "celebi_ilex_shrine" to "friend-ball.png",
        "jirachi_wishmaker" to "star-piece.png",
        "deoxys_birth_island" to "eon-ticket.png",
        "trick_house_all_rooms" to "escape-rope.png",
        "ice_path_sliding_puzzles" to "ice-heal.png",
        "lt_surge_trash_can_switches" to "thunder-stone.png",
        "silph_co_warp_pads" to "link-cable.png",
        "sabrina_gym_warp_maze" to "cleanse-tag.png",
        "sootopolis_gym_ice_tiles" to "water-stone.png"
    )

    val KNOWN_ITEM_ASSETS: Set<String> = setOf(
        "ability-capsule.png", "ability-patch.png", "aguav-berry.png", "amulet-coin.png", "antidote.png", "apicot-berry.png",
        "aspear-berry.png", "assault-vest.png", "auspicious-armor.png", "awakening.png", "azure-flute.png", "beast-ball.png",
        "berry-juice.png", "bicycle.png", "big-malasada.png", "big-nugget.png", "big-pearl.png", "black-augurite.png",
        "black-sludge.png", "blastoisinite.png", "bright-powder.png", "burn-heal.png", "calcium.png", "carbos.png",
        "casteliacone.png", "charizardite-x.png", "charizardite-y.png", "cheri-berry.png", "cherish-ball.png", "chesto-berry.png",
        "chipped-pot.png", "choice-band.png", "choice-scarf.png", "choice-specs.png", "cleanse-tag.png", "comet-shard.png",
        "cracked-pot.png", "dawn-stone.png", "deep-sea-scale.png", "deep-sea-tooth.png", "dire-hit.png", "dive-ball.png",
        "dowsing-machine.png", "dragon-scale.png", "dream-ball.png", "dubious-disc.png", "dusk-ball.png", "dusk-stone.png",
        "electirizer.png", "energy-powder.png", "energy-root.png", "enigma-berry.png", "eon-ticket.png", "escape-rope.png",
        "everstone.png", "eviolite.png", "exp-share.png", "expert-belt.png", "fast-ball.png", "figy-berry.png",
        "fire-stone.png", "fluffy-tail.png", "focus-band.png", "focus-sash.png", "fresh-water.png", "friend-ball.png",
        "full-heal.png", "full-restore.png", "galladite.png", "ganlon-berry.png", "garchompite.png", "gardevoirite.png",
        "gengarite.png", "gimmighoul-coin.png", "glacial-lure.png", "go-candy-xl.png", "go-candy.png", "good-rod.png",
        "great-ball.png", "guard-spec.png", "heal-ball.png", "heal-powder.png", "heavy-ball.png", "heavy-duty-boots.png",
        "honey.png", "hp-up.png", "hyper-potion.png", "iapapa-berry.png", "ice-heal.png", "ice-stone.png",
        "iron.png", "key-items.png", "kings-rock.png", "lansat-berry.png", "lava-cookie.png", "leaders-crest.png", "leaf-stone.png",
        "leftovers.png", "lemonade.png", "leppa-berry.png", "level-ball.png", "liechi-berry.png", "life-orb.png",
        "link-cable.png", "linking-cord.png", "love-ball.png", "lucarionite.png", "lum-berry.png", "lunar-fusion-energy.png",
        "lure-ball.png", "luxury-ball.png", "magmarizer.png", "magnetic-lure.png", "mago-berry.png", "malicious-armor.png",
        "master-ball.png", "masterpiece-teacup.png", "max-potion.png", "max-repel.png", "max-revive.png", "mega-bracelet.png",
        "mega-energy.png", "mega-ring.png", "member-card.png", "metagrossite.png", "metal-alloy.png", "metal-coat.png",
        "metronome.png", "mewtwonite-x.png", "mewtwonite-y.png", "moomoo-milk.png", "moon-ball.png", "moon-stone.png",
        "mossy-lure.png", "muscle-band.png", "nest-ball.png", "net-ball.png", "nugget.png", "oaks-letter.png",
        "old-gateau.png", "old-rod.png", "old-sea-map.png", "oran-berry.png", "oval-stone.png", "paralyze-heal.png",
        "park-ball.png", "pearl.png", "peat-block.png", "pecha-berry.png", "petaya-berry.png", "poffin.png",
        "poke-ball.png", "poke-doll.png", "poke-flute.png", "pokeblock.png", "potion.png", "pp-max.png",
        "pp-up.png", "premier-ball.png", "primal-energy-omega.png", "primal-energy.png", "prism-scale.png", "protector.png",
        "protein.png", "quick-ball.png", "quick-claw.png", "rainy-lure.png", "rare-candy.png", "rawst-berry.png",
        "razor-claw.png", "razor-fang.png", "reaper-cloth.png", "relic-copper.png", "relic-gold.png", "relic-silver.png",
        "repeat-ball.png", "repel.png", "revival-herb.png", "revive.png", "rocky-helmet.png", "running-shoes.png", "sachet.png",
        "sacred-ash.png", "safari-ball.png", "salac-berry.png", "salamencite.png", "scope-lens.png", "scroll-of-darkness.png",
        "scroll-of-waters.png", "shiny-stone.png", "silph-scope.png", "sinnoh-stone.png", "sitrus-berry.png", "smoke-ball.png",
        "soda-pop.png", "solar-fusion-energy.png", "soothe-bell.png", "sport-ball.png", "sprayduck.png", "squirt-bottle.png",
        "star-piece.png", "stardust.png", "starf-berry.png", "sun-stone.png", "super-potion.png", "super-repel.png",
        "super-rod.png", "sweet-apple.png", "syrupy-apple.png", "tart-apple.png", "thunder-stone.png", "timer-ball.png",
        "ultra-ball.png", "unova-stone.png", "unremarkable-teacup.png", "upgrade.png", "venusaurite.png", "water-stone.png",
        "weather-trio-wall.png", "whipped-dream.png", "wide-lens.png", "wiki-berry.png", "wise-glasses.png", "x-accuracy.png", "x-attack.png",
        "x-defense.png", "x-sp-atk.png", "x-sp-def.png", "x-speed.png", "zinc.png", "zoom-lens.png"
    )

    fun resolveItemIconAsset(queryOrName: String, topicId: String? = null, context: Context? = null): String? {
        if (topicId != null && TOPIC_ITEM_ICONS.containsKey(topicId)) {
            return TOPIC_ITEM_ICONS[topicId]
        }

        val slug = queryOrName.lowercase().trim()
            .replace("é", "e")
            .replace("’", "")
            .replace("'", "")
            .replace(".", "")
            .replace(" ", "-")
            .replace("_", "-")

        if (TOPIC_ITEM_ICONS.containsKey(slug)) {
            return TOPIC_ITEM_ICONS[slug]
        }

        if (KNOWN_ITEM_ASSETS.contains("$slug.png")) {
            return "$slug.png"
        }

        val clean = queryOrName.lowercase().replace(Regex("[^a-z0-9]"), "-")
        for ((k, v) in TOPIC_ITEM_ICONS) {
            val keySlug = k.replace("_", "-")
            if (clean.contains(keySlug) || keySlug.contains(clean)) {
                return v
            }
        }

        // Substring match sorted by length descending so longer names take precedence
        val sorted = KNOWN_ITEM_ASSETS.sortedByDescending { it.length }
        for (asset in sorted) {
            val base = asset.removeSuffix(".png")
            if (base.length >= 4 && clean.contains(base)) {
                return asset
            }
        }

        return null
    }

    fun normalizeForSearch(text: String): String {
        return java.text.Normalizer.normalize(text.lowercase().trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(".", "")
            .replace("'", "")
            .replace("’", "")
            .replace(":", "")
            .replace("/", " ")
            .replace("&", " and ")
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    val GUIDE_STOP_WORDS: Set<String> = setOf(
        "the", "and", "for", "with", "from", "are", "what", "where", "how", "best",
        "item", "items", "move", "moves", "strategy", "guide", "battle", "battles",
        "city", "gym", "route", "badge", "pokemon", "past", "obstacle", "get", "beat",
        "find", "obtain", "location", "locations", "build", "builds", "recommended",
        "does", "can", "help", "need", "give", "take", "walk", "look", "into", "over",
        "through", "under", "about", "which", "when", "there", "this", "that"
    )

    enum class GuideSearchMode {
        EXACT,
        PHRASE,
        TOKEN
    }

    fun matchesSearch(
        query: String,
        target: String,
        aliases: List<String> = emptyList(),
        exactOnly: Boolean = false,
        mode: GuideSearchMode = if (exactOnly) GuideSearchMode.EXACT else GuideSearchMode.PHRASE
    ): Boolean {
        val qNorm = normalizeForSearch(query)
        val tNorm = normalizeForSearch(target)
        if (qNorm.isBlank()) return false

        when (mode) {
            GuideSearchMode.EXACT -> {
                if (qNorm == tNorm) return true
                for (alias in aliases) {
                    if (qNorm == normalizeForSearch(alias)) return true
                }
                return false
            }
            GuideSearchMode.PHRASE -> {
                if (qNorm == tNorm) return true
                if (tNorm.contains(qNorm) && (qNorm.length >= 3 && !GUIDE_STOP_WORDS.contains(qNorm))) return true
                if (tNorm.length >= 4 && !GUIDE_STOP_WORDS.contains(tNorm) && qNorm.contains(tNorm)) return true

                for (alias in aliases) {
                    val aNorm = normalizeForSearch(alias)
                    if (aNorm.isNotBlank()) {
                        if (qNorm == aNorm) return true
                        if (aNorm.contains(qNorm) && (qNorm.length >= 3 && !GUIDE_STOP_WORDS.contains(qNorm))) return true
                        if (aNorm.length >= 4 && !GUIDE_STOP_WORDS.contains(aNorm) && qNorm.contains(aNorm)) return true
                    }
                }
                return false
            }
            GuideSearchMode.TOKEN -> {
                val qTokens = qNorm.split(" ").filter { it.length >= 4 && !GUIDE_STOP_WORDS.contains(it) }
                if (qTokens.isEmpty()) return false

                val tTokens = tNorm.split(" ").filter { it.length >= 4 && !GUIDE_STOP_WORDS.contains(it) }
                if (qTokens.any { tTokens.contains(it) }) return true

                for (alias in aliases) {
                    val aTokens = normalizeForSearch(alias).split(" ").filter { it.length >= 4 && !GUIDE_STOP_WORDS.contains(it) }
                    if (qTokens.any { aTokens.contains(it) }) return true
                }
                return false
            }
        }
    }

    private fun loadData(context: Context?): JSONObject? {
        if (cachedData != null) return cachedData
        if (context == null) return null
        return try {
            context.assets.open("game_guides_index.json").use { inputStream ->
                val text = inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                cachedData = obj
                obj
            }
        } catch (_: Exception) {
            null
        }
    }

    fun setCachedDataForTesting(jsonText: String) {
        cachedData = JSONObject(jsonText)
    }

    fun getCategories(context: Context? = null): List<GuideCategory> {
        val data = loadData(context) ?: return emptyList()
        val catArray = data.optJSONArray("categories") ?: return emptyList()
        val list = mutableListOf<GuideCategory>()
        for (i in 0 until catArray.length()) {
            val c = catArray.getJSONObject(i)
            val tArray = c.optJSONArray("topicIds")
            val tList = mutableListOf<String>()
            if (tArray != null) {
                for (j in 0 until tArray.length()) tList.add(tArray.getString(j))
            }
            val catId = c.getString("id")
            list.add(
                GuideCategory(
                    id = catId,
                    title = c.getString("title"),
                    emoji = c.optString("emoji", "📚"),
                    subtitle = c.optString("subtitle", ""),
                    topicIds = tList,
                    iconAsset = CATEGORY_ICONS[catId] ?: "poke-ball.png"
                )
            )
        }
        return list
    }

    fun getTopicsForCategory(categoryId: String, context: Context? = null): List<GuideTopicItem> {
        val data = loadData(context) ?: return emptyList()
        val list = mutableListOf<GuideTopicItem>()
        val seenIds = mutableSetOf<String>()

        // 1. First check category's explicit topicIds in canonical order
        val catArray = data.optJSONArray("categories")
        val targetCat = if (catArray != null) {
            (0 until catArray.length()).map { catArray.getJSONObject(it) }
                .firstOrNull { it.optString("id").equals(categoryId, ignoreCase = true) }
        } else null

        val topicsObj = data.optJSONObject("topics")
        if (targetCat != null && topicsObj != null) {
            val tArray = targetCat.optJSONArray("topicIds")
            if (tArray != null) {
                for (i in 0 until tArray.length()) {
                    val tid = tArray.getString(i)
                    if (topicsObj.has(tid) && seenIds.add(tid)) {
                        val t = topicsObj.getJSONObject(tid)
                        val aArray = t.optJSONArray("aliases")
                        val aList = mutableListOf<String>()
                        if (aArray != null) {
                            for (j in 0 until aArray.length()) aList.add(aArray.getString(j))
                        }
                        val topicName = t.getString("name")
                        list.add(
                            GuideTopicItem(
                                id = t.getString("id"),
                                name = topicName,
                                categoryId = categoryId,
                                aliases = aList,
                                iconAsset = resolveItemIconAsset(topicName, topicId = tid, context = context)
                            )
                        )
                    }
                }
            }
        }

        // 2. Scan remaining topicsObj matching categoryId
        if (topicsObj != null) {
            val keys = topicsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val t = topicsObj.getJSONObject(k)
                if (t.optString("categoryId").equals(categoryId, ignoreCase = true) && seenIds.add(k)) {
                    val aArray = t.optJSONArray("aliases")
                    val aList = mutableListOf<String>()
                    if (aArray != null) {
                        for (j in 0 until aArray.length()) aList.add(aArray.getString(j))
                    }
                    val topicName = t.getString("name")
                    list.add(
                        GuideTopicItem(
                            id = t.getString("id"),
                            name = topicName,
                            categoryId = categoryId,
                            aliases = aList,
                            iconAsset = resolveItemIconAsset(topicName, topicId = k, context = context)
                        )
                    )
                }
            }
        }

        // 3. Scan items array matching category
        val itemsArray = data.optJSONArray("items")
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val cat = item.optString("category", "").lowercase()
                val matchesCat = when (categoryId.lowercase()) {
                    "battle_items" -> cat.contains("held") || cat.contains("battle")
                    "key_items" -> cat.contains("key")
                    "healing_items" -> cat.contains("heal")
                    "money_items" -> cat.contains("money")
                    else -> false
                }
                val itemId = item.getString("id")
                if (matchesCat && seenIds.add(itemId)) {
                    val aArray = item.optJSONArray("aliases")
                    val aList = mutableListOf<String>()
                    if (aArray != null) {
                        for (j in 0 until aArray.length()) aList.add(aArray.getString(j))
                    }
                    val itemName = item.getString("name")
                    list.add(
                        GuideTopicItem(
                            id = itemId,
                            name = itemName,
                            categoryId = categoryId,
                            aliases = aList,
                            iconAsset = resolveItemIconAsset(itemName, topicId = itemId, context = context)
                        )
                    )
                }
            }
        }

        return list
    }

    fun resolveMapCoordinates(editionOrRegion: String, locationText: String): Triple<Float, Float, String>? {
        val loc = locationText.lowercase()
        val cleanEd = editionOrRegion.replace(" ", "")
        val isGbaKanto = cleanEd.contains("FireRed", ignoreCase = true) ||
                         cleanEd.contains("LeafGreen", ignoreCase = true) ||
                         cleanEd.contains("kanto_frlg", ignoreCase = true) ||
                         cleanEd.contains("FRLG", ignoreCase = true)

        if (isGbaKanto) {
            val gbaKantoCoord = when {
                loc.contains("pallet") -> Triple(0.280f, 0.770f, "Pallet Town")
                loc.contains("viridian") -> Triple(0.280f, 0.620f, "Viridian City")
                loc.contains("pewter") -> Triple(0.280f, 0.420f, "Pewter City")
                loc.contains("cerulean cave") || loc.contains("unknown dungeon") -> Triple(0.580f, 0.340f, "Cerulean Cave B1F")
                loc.contains("nugget bridge") || loc.contains("route 24") -> Triple(0.620f, 0.300f, "Route 24 (Nugget Bridge)")
                loc.contains("machine part") || (loc.contains("cerulean") && loc.contains("gym")) -> Triple(0.620f, 0.380f, "Cerulean City Gym (Machine Part)")
                loc.contains("cerulean") -> Triple(0.620f, 0.380f, "Cerulean City")
                loc.contains("s.s. anne") || loc.contains("ss anne") -> Triple(0.620f, 0.720f, "S.S. Anne (Vermilion Harbor)")
                loc.contains("vermilion") -> Triple(0.620f, 0.670f, "Vermilion City")
                loc.contains("celadon") -> Triple(0.520f, 0.520f, "Celadon City")
                loc.contains("silph co") || loc.contains("card key") -> Triple(0.620f, 0.520f, "Silph Co. HQ (Saffron City)")
                loc.contains("saffron") || loc.contains("silph") -> Triple(0.620f, 0.520f, "Saffron City")
                loc.contains("lavender") || loc.contains("pokemon tower") -> Triple(0.750f, 0.520f, "Lavender Town")
                loc.contains("fuchsia") || loc.contains("safari zone") -> Triple(0.550f, 0.830f, "Fuchsia City")
                loc.contains("cinnabar") -> Triple(0.280f, 0.920f, "Cinnabar Island")
                loc.contains("indigo plateau") || loc.contains("victory road") -> Triple(0.210f, 0.380f, "Indigo Plateau")
                loc.contains("power plant") -> Triple(0.780f, 0.450f, "Kanto Power Plant")
                loc.contains("rock tunnel") -> Triple(0.750f, 0.420f, "Rock Tunnel")
                loc.contains("route 15") -> Triple(0.600f, 0.830f, "Route 15 Gatehouse")
                loc.contains("route 16") -> Triple(0.460f, 0.520f, "Route 16 Gatehouse")
                loc.contains("route 11") -> Triple(0.685f, 0.670f, "Route 11")
                loc.contains("route 12") -> Triple(0.750f, 0.620f, "Route 12")
                loc.contains("mt. moon") -> Triple(0.420f, 0.380f, "Mt. Moon")
                loc.contains("berry forest") || loc.contains("three island") -> Triple(0.45f, 0.55f, "Berry Forest (Sevii)")
                loc.contains("tanoby") || loc.contains("monean") || loc.contains("dotted hole") -> Triple(0.48f, 0.88f, "Tanoby Ruins (Sevii Seven)")
                loc.contains("navel rock") -> Triple(0.50f, 0.88f, "Navel Rock")
                loc.contains("seafoam") -> Triple(0.420f, 0.920f, "Seafoam Islands (Articuno)")
                loc.contains("bill") || loc.contains("sea cottage") || loc.contains("route 25") -> Triple(0.700f, 0.300f, "Bill's Sea Cottage (Route 25)")
                loc.contains("viridian forest") -> Triple(0.280f, 0.520f, "Viridian Forest")
                loc.contains("diglett") -> Triple(0.280f, 0.620f, "Diglett's Cave")
                loc.contains("mt. ember") || loc.contains("one island") -> Triple(0.35f, 0.20f, "Mt. Ember (Sevii)")
                loc.contains("birth island") -> Triple(0.50f, 0.50f, "Birth Island (Deoxys)")
                loc.contains("faraway island") -> Triple(0.85f, 0.85f, "Faraway Island (Mew)")
                else -> null
            }
            if (gbaKantoCoord != null) return gbaKantoCoord
        }

        return when {
            // KANTO (Game Boy RGBY)
            loc.contains("pallet") -> Triple(0.220f, 0.690f, "Pallet Town")
            loc.contains("viridian") -> Triple(0.220f, 0.485f, "Viridian City")
            loc.contains("pewter") -> Triple(0.220f, 0.200f, "Pewter City")
            loc.contains("cerulean cave") || loc.contains("unknown dungeon") -> Triple(0.570f, 0.100f, "Cerulean Cave B1F")
            loc.contains("nugget bridge") || loc.contains("route 24") -> Triple(0.625f, 0.080f, "Route 24 (Nugget Bridge)")
            loc.contains("machine part") || (loc.contains("cerulean") && loc.contains("gym")) -> Triple(0.625f, 0.140f, "Cerulean City Gym (Machine Part)")
            loc.contains("cerulean") -> Triple(0.625f, 0.140f, "Cerulean City")
            loc.contains("s.s. anne") || loc.contains("ss anne") -> Triple(0.625f, 0.605f, "S.S. Anne (Vermilion Harbor)")
            loc.contains("vermilion") -> Triple(0.625f, 0.550f, "Vermilion City")
            loc.contains("celadon") -> Triple(0.470f, 0.320f, "Celadon City")
            loc.contains("silph co") || loc.contains("card key") -> Triple(0.625f, 0.320f, "Silph Co. HQ (Saffron City)")
            loc.contains("saffron") || loc.contains("silph") -> Triple(0.625f, 0.320f, "Saffron City")
            loc.contains("lavender") || loc.contains("pokemon tower") -> Triple(0.825f, 0.320f, "Lavender Town")
            loc.contains("fuchsia") || loc.contains("safari zone") -> Triple(0.525f, 0.780f, "Fuchsia City")
            loc.contains("cinnabar") -> Triple(0.220f, 0.900f, "Cinnabar Island")
            loc.contains("indigo plateau") -> Triple(0.125f, 0.138f, "Indigo Plateau")
            loc.contains("victory road") -> Triple(0.125f, 0.260f, "Victory Road")
            loc.contains("power plant") -> Triple(0.850f, 0.260f, "Kanto Power Plant")
            loc.contains("rock tunnel") -> Triple(0.825f, 0.190f, "Rock Tunnel")
            loc.contains("route 15") -> Triple(0.600f, 0.780f, "Route 15 Gatehouse")
            loc.contains("route 16") -> Triple(0.380f, 0.320f, "Route 16 Gatehouse")
            loc.contains("route 11") -> Triple(0.725f, 0.550f, "Route 11")
            loc.contains("route 12") -> Triple(0.825f, 0.440f, "Route 12")
            loc.contains("mt. moon") -> Triple(0.420f, 0.140f, "Mt. Moon")
            loc.contains("berry forest") || loc.contains("three island") -> Triple(0.35f, 0.60f, "Berry Forest (Sevii)")
            loc.contains("navel rock") -> Triple(0.48f, 0.88f, "Navel Rock")
            loc.contains("seafoam") -> Triple(0.400f, 0.900f, "Seafoam Islands (Articuno)")
            loc.contains("bill") || loc.contains("sea cottage") || loc.contains("route 25") -> Triple(0.720f, 0.080f, "Bill's Sea Cottage (Route 25)")
            loc.contains("viridian forest") -> Triple(0.220f, 0.260f, "Viridian Forest")
            loc.contains("diglett") -> Triple(0.220f, 0.485f, "Diglett's Cave")

            // JOHTO
            loc.contains("new bark") -> Triple(0.85f, 0.70f, "New Bark Town")
            loc.contains("cherrygrove") -> Triple(0.70f, 0.70f, "Cherrygrove City")
            loc.contains("sprout tower") -> Triple(0.55f, 0.50f, "Sprout Tower")
            loc.contains("violet") -> Triple(0.55f, 0.55f, "Violet City")
            loc.contains("ruins of alph") || loc.contains("ruin of alph") -> Triple(0.45f, 0.55f, "Ruins of Alph")
            loc.contains("azalea") || loc.contains("slowpoke well") -> Triple(0.35f, 0.75f, "Azalea Town")
            loc.contains("ilex forest") -> Triple(0.35f, 0.80f, "Ilex Forest")
            loc.contains("goldenrod") || loc.contains("radio tower") -> Triple(0.25f, 0.55f, "Goldenrod City")
            loc.contains("route 36") || loc.contains("sudowoodo") -> Triple(0.45f, 0.55f, "Route 36 Junction")
            loc.contains("route 39") || loc.contains("moomoo") -> Triple(0.20f, 0.48f, "Route 39 Moomoo Farm")
            loc.contains("tin tower") || loc.contains("bell tower") || loc.contains("burned tower") || loc.contains("ecruteak") -> Triple(0.38f, 0.38f, "Ecruteak City")
            loc.contains("olivine") || loc.contains("lighthouse") -> Triple(0.18f, 0.45f, "Olivine City")
            loc.contains("cianwood") || loc.contains("pharmacy") -> Triple(0.08f, 0.65f, "Cianwood City")
            loc.contains("lake of rage") || loc.contains("mahogany") -> Triple(0.60f, 0.25f, "Lake of Rage")
            loc.contains("blackthorn") || loc.contains("dragon's den") -> Triple(0.78f, 0.45f, "Blackthorn City")
            loc.contains("ice path") -> Triple(0.78f, 0.38f, "Ice Path")
            loc.contains("mt. silver") -> Triple(0.88f, 0.55f, "Mt. Silver")
            loc.contains("route 30") -> Triple(0.70f, 0.60f, "Route 30 (Mr. Pokémon)")
            loc.contains("route 34") -> Triple(0.25f, 0.65f, "Route 34 Day Care")
            loc.contains("whirl islands") || loc.contains("whirl island") -> Triple(0.12f, 0.58f, "Whirl Islands (Lugia)")
            loc.contains("ilex shrine") || (loc.contains("ilex") && loc.contains("shrine")) -> Triple(0.35f, 0.80f, "Ilex Forest Shrine (Celebi)")
            loc.contains("dark cave") -> Triple(0.65f, 0.55f, "Dark Cave")
            loc.contains("national park") -> Triple(0.35f, 0.50f, "National Park (Bug Catching Contest)")

            // HOENN
            loc.contains("littleroot") -> Triple(0.180f, 0.680f, "Littleroot Town")
            loc.contains("oldale") -> Triple(0.180f, 0.580f, "Oldale Town")
            loc.contains("petalburg woods") -> Triple(0.060f, 0.530f, "Petalburg Woods")
            loc.contains("petalburg") -> Triple(0.080f, 0.580f, "Petalburg City")
            loc.contains("flower shop") || loc.contains("route 104") -> Triple(0.060f, 0.490f, "Route 104 Flower Shop")
            loc.contains("rustboro") || loc.contains("devon") -> Triple(0.050f, 0.400f, "Rustboro City (Devon Corp)")
            loc.contains("rusturf") || loc.contains("peeko") -> Triple(0.115f, 0.400f, "Rusturf Tunnel")
            loc.contains("dewford") || loc.contains("granite cave") -> Triple(0.120f, 0.820f, "Dewford Town")
            loc.contains("slateport") -> Triple(0.320f, 0.650f, "Slateport City")
            loc.contains("trick house") || loc.contains("route 110") -> Triple(0.325f, 0.535f, "Route 110 (Trick House)")
            loc.contains("mauville") -> Triple(0.330f, 0.420f, "Mauville City")
            loc.contains("verdanturf") -> Triple(0.180f, 0.420f, "Verdanturf Town")
            loc.contains("fallarbor") -> Triple(0.150f, 0.120f, "Fallarbor Town")
            loc.contains("mt. chimney") || loc.contains("chimney") || loc.contains("lavaridge") -> Triple(0.220f, 0.280f, "Mt. Chimney / Lavaridge")
            loc.contains("route 118") -> Triple(0.400f, 0.420f, "Route 118")
            loc.contains("weather institute") || loc.contains("route 119") -> Triple(0.400f, 0.270f, "Route 119")
            loc.contains("fortree") -> Triple(0.450f, 0.120f, "Fortree City")
            loc.contains("route 120") -> Triple(0.530f, 0.195f, "Route 120")
            loc.contains("berry master") || loc.contains("route 123") -> Triple(0.460f, 0.420f, "Route 123 (Berry Master)")
            loc.contains("lilycove") -> Triple(0.660f, 0.270f, "Lilycove City")
            loc.contains("mossdeep") || loc.contains("space center") -> Triple(0.860f, 0.360f, "Mossdeep City")
            loc.contains("cave of origin") -> Triple(0.750f, 0.470f, "Cave of Origin")
            loc.contains("sootopolis") -> Triple(0.750f, 0.470f, "Sootopolis City")
            loc.contains("pacifidlog") -> Triple(0.620f, 0.620f, "Pacifidlog Town")
            loc.contains("sealed chamber") || loc.contains("route 134") -> Triple(0.440f, 0.620f, "Route 134 (Sealed Chamber)")
            loc.contains("sky pillar") -> Triple(0.680f, 0.600f, "Sky Pillar")
            loc.contains("ever grande") -> Triple(0.940f, 0.560f, "Ever Grande City")
            loc.contains("battle frontier") || loc.contains("battle tower") -> Triple(0.850f, 0.750f, "Battle Frontier")
            loc.contains("faraway island") -> Triple(0.850f, 0.850f, "Faraway Island")
            loc.contains("southern island") -> Triple(0.600f, 0.850f, "Southern Island")
            loc.contains("birth island") -> Triple(0.500f, 0.500f, "Birth Island")
            loc.contains("shoal cave") -> Triple(0.860f, 0.250f, "Shoal Cave")
            loc.contains("desert ruins") || loc.contains("regirock") -> Triple(0.330f, 0.320f, "Desert Ruins (Regirock)")
            loc.contains("island cave") || loc.contains("regice") -> Triple(0.050f, 0.650f, "Island Cave (Regice)")
            loc.contains("ancient tomb") || loc.contains("registeel") -> Triple(0.530f, 0.195f, "Ancient Tomb (Registeel)")
            loc.contains("marine cave") -> Triple(0.760f, 0.360f, "Marine Cave (Kyogre)")
            loc.contains("terra cave") -> Triple(0.275f, 0.280f, "Terra Cave (Groudon)")
            loc.contains("abandoned ship") || loc.contains("sea mauville") -> Triple(0.240f, 0.820f, "Abandoned Ship")
            loc.contains("new mauville") -> Triple(0.330f, 0.470f, "New Mauville")
            loc.contains("meteor falls") -> Triple(0.080f, 0.250f, "Meteor Falls")

            // SINNOH
            loc.contains("twinleaf") -> Triple(0.20f, 0.75f, "Twinleaf Town")
            loc.contains("sandgem") -> Triple(0.40f, 0.75f, "Sandgem Town")
            loc.contains("jubilife") -> Triple(0.30f, 0.65f, "Jubilife City")
            loc.contains("oreburgh") -> Triple(0.45f, 0.68f, "Oreburgh City")
            loc.contains("floaroma") -> Triple(0.30f, 0.55f, "Floaroma Town")
            loc.contains("eterna") || loc.contains("old chateau") -> Triple(0.42f, 0.40f, "Eterna City")
            loc.contains("hearthome") || loc.contains("amity square") -> Triple(0.55f, 0.55f, "Hearthome City")
            loc.contains("solaceon") -> Triple(0.62f, 0.55f, "Solaceon Town")
            loc.contains("veilstone") -> Triple(0.75f, 0.45f, "Veilstone City")
            loc.contains("pastoria") || loc.contains("great marsh") -> Triple(0.62f, 0.75f, "Pastoria City")
            loc.contains("celestic") -> Triple(0.52f, 0.45f, "Celestic Town")
            loc.contains("canalave") -> Triple(0.15f, 0.60f, "Canalave City")
            loc.contains("snowpoint") -> Triple(0.50f, 0.15f, "Snowpoint City")
            loc.contains("sunyshore") -> Triple(0.90f, 0.60f, "Sunyshore City")
            loc.contains("spear pillar") || loc.contains("coronet") -> Triple(0.50f, 0.35f, "Spear Pillar")
            loc.contains("newmoon") -> Triple(0.20f, 0.20f, "Newmoon Island")
            loc.contains("flower paradise") || loc.contains("route 224") -> Triple(0.88f, 0.15f, "Flower Paradise")
            loc.contains("fullmoon") -> Triple(0.20f, 0.15f, "Fullmoon Island (Cresselia)")
            loc.contains("snowpoint temple") || loc.contains("regigigas") -> Triple(0.50f, 0.15f, "Snowpoint Temple (Regigigas)")
            loc.contains("lake verity") || loc.contains("mesprit") -> Triple(0.20f, 0.70f, "Lake Verity")
            loc.contains("lake valor") || loc.contains("azelf") -> Triple(0.70f, 0.60f, "Lake Valor")
            loc.contains("lake acuity") || loc.contains("uxie") -> Triple(0.45f, 0.15f, "Lake Acuity")
            loc.contains("turnback cave") || loc.contains("giratina") -> Triple(0.80f, 0.55f, "Turnback Cave (Giratina)")
            loc.contains("fuego ironworks") -> Triple(0.30f, 0.48f, "Fuego Ironworks")
            loc.contains("route 210") || (loc.contains("psyduck") && loc.contains("headache")) -> Triple(0.60f, 0.42f, "Route 210 (Psyduck Blockade)")

            // UNOVA
            loc.contains("liberty") || loc.contains("victini") -> Triple(0.40f, 0.95f, "Liberty Garden (Castelia Pier)")
            loc.contains("castelia") -> Triple(0.50f, 0.85f, "Castelia City")
            loc.contains("nimbasa") -> Triple(0.50f, 0.55f, "Nimbasa City")
            loc.contains("driftveil") -> Triple(0.35f, 0.55f, "Driftveil City")
            loc.contains("undella") || loc.contains("abyssal") -> Triple(0.90f, 0.50f, "Undella Bay")
            loc.contains("giant chasm") -> Triple(0.85f, 0.30f, "Giant Chasm")
            loc.contains("shopping mall nine") || loc.contains("route 9") -> Triple(0.75f, 0.45f, "Shopping Mall Nine")

            // PALDEA
            loc.contains("mesagoza") -> Triple(0.50f, 0.75f, "Mesagoza Poké Mart")
            loc.contains("los platos") -> Triple(0.50f, 0.85f, "Los Platos Pokémon Center")
            loc.contains("cabo poco") -> Triple(0.48f, 0.92f, "Cabo Poco")
            loc.contains("cortondo") -> Triple(0.42f, 0.80f, "Cortondo")
            loc.contains("artazon") -> Triple(0.68f, 0.78f, "Artazon")
            loc.contains("cascarrafa") -> Triple(0.35f, 0.55f, "Cascarrafa")
            loc.contains("medali") -> Triple(0.45f, 0.45f, "Medali")
            loc.contains("porto marinada") -> Triple(0.25f, 0.45f, "Porto Marinada")
            loc.contains("levincia") -> Triple(0.80f, 0.55f, "Levincia")
            loc.contains("alfornada") -> Triple(0.20f, 0.88f, "Alfornada")
            loc.contains("montenevera") -> Triple(0.50f, 0.25f, "Montenevera")
            loc.contains("area zero") -> Triple(0.50f, 0.50f, "Area Zero")

            // GENERIC ALL MARTS / FIRST ENCOUNTER RESOLUTION BY REGION
            loc.contains("all poké mart") || loc.contains("all poke mart") || loc.contains("all pokémon center") ||
            loc.contains("all pokemon center") || loc.contains("multiple location") || loc.contains("standard potion") -> {
                val regionTitle = try {
                    com.example.pokedex.data.getRegionMapEntryForEdition(editionOrRegion).title.uppercase()
                } catch (_: Exception) {
                    editionOrRegion.uppercase()
                }
                when {
                    regionTitle.contains("PALDEA") -> Triple(0.50f, 0.75f, "Mesagoza Pokémon Center Mart (First Encounter)")
                    regionTitle.contains("HOENN") -> Triple(0.180f, 0.580f, "Oldale Town Mart (First Encounter)")
                    regionTitle.contains("JOHTO") -> Triple(0.70f, 0.70f, "Cherrygrove City Mart (First Encounter)")
                    regionTitle.contains("SINNOH") -> Triple(0.40f, 0.75f, "Sandgem Town Mart (First Encounter)")
                    regionTitle.contains("UNOVA") -> Triple(0.50f, 0.85f, "Castelia City / Accumula (First Encounter)")
                    regionTitle.contains("KALOS") -> Triple(0.42f, 0.70f, "Aquacorde Town (First Encounter)")
                    regionTitle.contains("ALOLA") -> Triple(0.35f, 0.65f, "Hau'oli City Mart (First Encounter)")
                    regionTitle.contains("GALAR") -> Triple(0.50f, 0.85f, "Wedgehurst Mart (First Encounter)")
                    else -> {
                        if (isGbaKanto) Triple(0.280f, 0.620f, "Viridian City Mart (First Encounter)")
                        else Triple(0.220f, 0.485f, "Viridian City Mart (First Encounter)")
                    }
                }
            }

            else -> null
        }
    }

    fun queryGuide(
        rawQuery: String,
        editionId: String,
        context: Context? = null
    ): GuideQueryResult {
        val q = rawQuery.trim().lowercase()
        val data = loadData(context)
        val edition = editionId.trim()

        // 1. Exact topic match (e.g. topic ID or exact name clicked from list)
        val exactTopicMatch = queryTopic(q, data, edition, mode = GuideSearchMode.EXACT)
        if (exactTopicMatch != null) return finalizeResult(exactTopicMatch, rawQuery, context)

        val isBuildQuery = q.contains("build") || q.contains("moveset") || q.contains("nature") || q.contains(" ev") || q.endsWith("ev")

        // 2. Pass 1: Phrase / Substring match (High Precision)
        // Check specific entity domains first. Unless the user explicitly asks for a build/moveset,
        // prioritize landmarks, quests, legendaries, items, and roadblocks over text-only competitive builds.
        val phraseMatch = if (isBuildQuery) {
            queryPokemonBuild(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryGym(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryLandmark(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryTopic(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryItem(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryRoadblock(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryFacility(q, data, edition, mode = GuideSearchMode.PHRASE)
        } else if (q.contains("gym") || q.contains("badge") || q.contains("leader")) {
            queryGym(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryLandmark(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryTopic(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryItem(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryRoadblock(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryFacility(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryPokemonBuild(q, data, edition, mode = GuideSearchMode.PHRASE)
        } else {
            queryLandmark(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryGym(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryTopic(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryItem(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryRoadblock(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryFacility(q, data, edition, mode = GuideSearchMode.PHRASE)
                ?: queryPokemonBuild(q, data, edition, mode = GuideSearchMode.PHRASE)
        }

        if (phraseMatch != null) return finalizeResult(phraseMatch, rawQuery, context)

        // 3. Pass 2: Keyword Token Overlap (Fuzzy Fallback)
        val tokenMatch = if (isBuildQuery) {
            queryPokemonBuild(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryGym(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryLandmark(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryTopic(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryItem(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryRoadblock(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryFacility(q, data, edition, mode = GuideSearchMode.TOKEN)
        } else {
            queryGym(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryLandmark(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryTopic(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryItem(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryRoadblock(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryFacility(q, data, edition, mode = GuideSearchMode.TOKEN)
                ?: queryPokemonBuild(q, data, edition, mode = GuideSearchMode.TOKEN)
        }

        if (tokenMatch != null) return finalizeResult(tokenMatch, rawQuery, context)

        // 4. Helpful General Fallback
        val fallbackIcon = resolveItemIconAsset(rawQuery, context = context)
        return GuideQueryResult(
            title = "Pokémon Guide: \"$rawQuery\"",
            category = "GENERAL STRATEGY",
            editionName = edition,
            location = "Pokémon World (${edition.uppercase()})",
            summary = "Dexter can help you find items, solve puzzles, catch legendaries, or navigate roadblocks for $edition.",
            steps = listOf(
                "• Browse Topics: Tap '📚 TOPICS & ITEMS' in the header to explore all 12 strategy domains.",
                "• Search an item: 'Exp. Share', 'Bicycle', 'Running Shoes', 'Amulet Coin', 'Leftovers'",
                "• Search a roadblock: 'Sudowoodo', 'Snorlax', 'Psyduck', 'Devon Goods'",
                "• Search a puzzle: 'Trick House', 'Lt. Surge', 'Ice Path', 'Silph Co. warp pads'",
                "• Search a legendary: 'Mewtwo', 'Rayquaza', 'Wo-Chien 32 stakes', 'Sealed Chamber Braille'"
            ),
            prerequisites = null,
            mapAsset = PokemonMapHabitatRepository.getMapAssetForCartridge(edition),
            recommendedItems = emptyList(),
            spokenAdvice = "In Pokémon $edition, you can browse all strategy domains or search for any item, obstacle, dungeon puzzle, or legendary encounter.",
            itemIconAsset = fallbackIcon
        )
    }

    private fun finalizeResult(matched: GuideQueryResult, rawQuery: String, context: Context?): GuideQueryResult {
        return if (matched.itemIconAsset == null) {
            val icon = resolveItemIconAsset(matched.title, context = context)
                ?: resolveItemIconAsset(rawQuery, context = context)
            if (icon != null) matched.copy(itemIconAsset = icon) else matched
        } else {
            matched
        }
    }

    private fun getLandmarkAliases(lm: MapLandmark): List<String> {
        val cleanName = lm.name.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        val base = mutableListOf(
            lm.shortName,
            lm.id,
            cleanName,
            cleanName.replace(" City", ""),
            cleanName.replace(" Town", ""),
            cleanName.replace(" Island", ""),
            cleanName.replace(" Peak", "")
        )
        if (lm.name.startsWith("Route ", ignoreCase = true)) {
            val num = lm.name.substring(6).split(" ").firstOrNull()?.trim() ?: ""
            if (num.isNotBlank()) {
                base.add("r$num")
                base.add("rt$num")
                base.add("rt $num")
                base.add("route $num")
            }
        }
        return base.filter { it.isNotBlank() }.distinct()
    }

    private fun queryLandmark(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val landmarks = RegionMapLandmarksRepository.getLandmarksForMap(edition)
        val mapAsset = PokemonMapHabitatRepository.getMapAssetForCartridge(edition)

        // Find matching landmark in current edition first, or across all landmarks
        val matched = landmarks.firstOrNull { lm ->
            matchesSearch(q, lm.name, getLandmarkAliases(lm), mode = mode)
        } ?: run {
            RegionMapLandmarksRepository.getAllLandmarks().firstOrNull { lm ->
                matchesSearch(q, lm.name, getLandmarkAliases(lm), mode = mode)
            }
        } ?: return null

        val steps = mutableListOf<String>()
        if (!matched.tagline.isNullOrBlank()) {
            steps.add("🏷️ Official Slogan: \"${matched.tagline}\"")
        }
        if (!matched.description.isNullOrBlank()) {
            steps.add("📖 Overview: ${matched.description}")
        }
        if (!matched.gymInfo.isNullOrBlank()) {
            steps.add("🏟️ Gym: ${matched.gymInfo}")
        }
        if (matched.keyLocations.isNotEmpty()) {
            steps.add("🏛️ Key Facilities & Points of Interest:\n• " + matched.keyLocations.joinToString("\n• "))
        }
        if (matched.connectedAreas.isNotEmpty()) {
            steps.add("🛣️ Connecting Routes & Paths:\n• " + matched.connectedAreas.joinToString("\n• "))
        }
        if (!matched.guideAdvice.isNullOrBlank()) {
            steps.add("💡 Dexter's Strategy Advice:\n${matched.guideAdvice}")
        }

        val icon = if (matched.type == MapLandmarkType.POI && matched.id.contains("ss_anne")) {
            "old-sea-map.png"
        } else {
            matched.type.itemSprite
        }

        val spoken = "${matched.name}. ${matched.tagline ?: ""} ${matched.description ?: ""} ${matched.guideAdvice ?: ""}".trim()

        return GuideQueryResult(
            title = "${matched.name} Guide",
            category = "${matched.type.displayName.uppercase()} & LOCATION GUIDE • ${edition.uppercase()}",
            editionName = edition,
            location = matched.name,
            summary = matched.tagline ?: matched.description ?: "Explore ${matched.name} on the ${edition.uppercase()} map.",
            steps = steps,
            prerequisites = matched.gymInfo,
            mapAsset = mapAsset,
            recommendedItems = emptyList(),
            spokenAdvice = spoken,
            pinX = matched.xNorm,
            pinY = matched.yNorm,
            pinLocationName = matched.name,
            itemIconAsset = icon
        )
    }

    private fun queryTopic(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val topicsObj = data?.optJSONObject("topics") ?: return null
        val keys = topicsObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val topic = topicsObj.getJSONObject(key)
            val name = topic.getString("name")
            val aliases = topic.optJSONArray("aliases")
            val catId = topic.optString("categoryId", "GENERAL")

            val aliasList = mutableListOf(key, key.replace("_", " "))
            if (aliases != null) {
                for (a in 0 until aliases.length()) {
                    aliasList.add(aliases.getString(a))
                }
            }

            if (matchesSearch(q, name, aliasList, mode = mode)) {
                val versions = topic.optJSONObject("versions")
                val verKey = if (versions != null) resolveVersionKey(versions, edition) else null
                val verInfo = if (verKey != null && versions != null) versions.optJSONObject(verKey) else null

                val location = verInfo?.optString("location")
                    ?: topic.optString("generalLocation", "Multiple Locations")
                val instructions = verInfo?.optString("instructions")
                    ?: topic.optString("generalInstructions", topic.optString("description", ""))
                val prereq = if (verInfo != null && verInfo.has("prerequisites")) verInfo.getString("prerequisites")
                else if (topic.has("generalPrerequisites") && topic.getString("generalPrerequisites").isNotBlank()) topic.getString("generalPrerequisites")
                else null

                val mapAsset = verInfo?.optString("mapAsset")
                    ?: PokemonMapHabitatRepository.getMapAssetForCartridge(edition)

                val stepList = instructions.split("\n").filter { it.isNotBlank() }
                val catFormatted = catId.replace("_", " ").uppercase()

                val pinObj = verInfo?.optJSONObject("pin") ?: topic.optJSONObject("pin")
                val explicitX = pinObj?.optDouble("x", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitY = pinObj?.optDouble("y", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitArea = pinObj?.optString("area")

                val resolvedCoords = if (explicitX != null && explicitY != null) {
                    Triple(explicitX, explicitY, explicitArea ?: location)
                } else {
                    resolveMapCoordinates(verKey ?: edition, location)
                }

                val (firstEnc, subAvail) = resolveMartProgression(
                    name = name,
                    location = location,
                    pinArea = resolvedCoords?.third ?: explicitArea,
                    edition = verKey ?: edition,
                    instructions = instructions,
                    explicitFirstEncounter = verInfo?.optString("firstEncounter")?.takeIf { it.isNotBlank() } ?: topic.optString("firstEncounter").takeIf { it.isNotBlank() },
                    explicitSubsequentAvailability = verInfo?.optString("subsequentAvailability")?.takeIf { it.isNotBlank() } ?: topic.optString("subsequentAvailability").takeIf { it.isNotBlank() }
                )

                val combinedSteps = mutableListOf<String>()
                if (firstEnc != null) combinedSteps.add("📍 $firstEnc")
                if (subAvail != null) combinedSteps.add("🌐 $subAvail")
                combinedSteps.addAll(stepList.ifEmpty { listOf(instructions) })

                val spokenText = if (subAvail != null && firstEnc != null) {
                    val cleanFirst = firstEnc.replace("First Encounter / Earliest Unlock: ", "").replace("First Encounter: ", "")
                    "In Pokémon ${verKey ?: edition}, $name is first encountered at $cleanFirst. After this point, it is available at every single Poké Mart across the region."
                } else {
                    "In Pokémon ${verKey ?: edition}, $name is at $location. $instructions"
                }

                return GuideQueryResult(
                    title = name,
                    category = "$catFormatted • ${(verKey ?: edition).uppercase()} EDITION",
                    editionName = verKey ?: edition,
                    location = location,
                    summary = if (subAvail != null) "In Pokémon ${verKey ?: edition}, $name is first unlocked at ${resolvedCoords?.third ?: location}, then stocked at all regional Poké Marts." else "In Pokémon ${verKey ?: edition}, $name ($location).",
                    steps = combinedSteps,
                    prerequisites = prereq,
                    mapAsset = mapAsset,
                    recommendedItems = listOf(name),
                    spokenAdvice = spokenText,
                    pinX = resolvedCoords?.first,
                    pinY = resolvedCoords?.second,
                    pinLocationName = resolvedCoords?.third ?: location,
                    firstEncounter = firstEnc,
                    subsequentAvailability = subAvail,
                    itemIconAsset = resolveItemIconAsset(name, topicId = key, context = null)
                )
            }
        }
        return null
    }

    private fun queryItem(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val itemsArray = data?.optJSONArray("items") ?: return null
        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            val name = item.getString("name")
            val aliases = item.optJSONArray("aliases")
            val aliasList = mutableListOf<String>()
            if (aliases != null) {
                for (a in 0 until aliases.length()) {
                    aliasList.add(aliases.getString(a))
                }
            }

            if (matchesSearch(q, name, aliasList, mode = mode)) {
                val versions = item.getJSONObject("versions")
                val verKey = resolveVersionKey(versions, edition)
                val isNative = verKey != null
                val fallbackKey = if (versions.keys().hasNext()) versions.keys().next() else edition
                val verInfo = if (isNative) versions.getJSONObject(verKey!!) else versions.getJSONObject(fallbackKey)
                val location = verInfo.optString("location", "Unknown Location")
                val instructions = verInfo.optString("instructions", "")
                val prereq = if (verInfo.has("prerequisites")) verInfo.getString("prerequisites") else null
                val mapAsset = if (isNative) verInfo.optString("mapAsset", PokemonMapHabitatRepository.getMapAssetForCartridge(edition))
                               else PokemonMapHabitatRepository.getMapAssetForCartridge(edition)

                val stepList = instructions.split("\n").filter { it.isNotBlank() }

                val pinObj = if (isNative) (verInfo.optJSONObject("pin") ?: item.optJSONObject("pin")) else null
                val explicitX = pinObj?.optDouble("x", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitY = pinObj?.optDouble("y", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitArea = pinObj?.optString("area")

                val resolvedCoords = if (isNative) {
                    if (explicitX != null && explicitY != null) {
                        Triple(explicitX, explicitY, explicitArea ?: location)
                    } else {
                        resolveMapCoordinates(verKey ?: edition, location)
                    }
                } else null

                val (firstEnc, subAvail) = if (isNative) {
                    resolveMartProgression(
                        name = name,
                        location = location,
                        pinArea = resolvedCoords?.third ?: explicitArea,
                        edition = verKey ?: edition,
                        instructions = instructions,
                        explicitFirstEncounter = verInfo.optString("firstEncounter").takeIf { it.isNotBlank() } ?: item.optString("firstEncounter").takeIf { it.isNotBlank() },
                        explicitSubsequentAvailability = verInfo.optString("subsequentAvailability").takeIf { it.isNotBlank() } ?: item.optString("subsequentAvailability").takeIf { it.isNotBlank() }
                    )
                } else Pair(null, null)

                val combinedSteps = mutableListOf<String>()
                if (!isNative) {
                    combinedSteps.add("ℹ️ Note: $name originates in Pokémon $fallbackKey. In Pokémon $edition, this item is not present or unobtainable.")
                }
                if (firstEnc != null) combinedSteps.add("📍 $firstEnc")
                if (subAvail != null) combinedSteps.add("🌐 $subAvail")
                combinedSteps.addAll(stepList.ifEmpty { listOf(instructions) })

                val spokenText = if (!isNative) {
                    "In Pokémon $edition, $name is not available. It is found in Pokémon $fallbackKey at $location."
                } else if (subAvail != null && firstEnc != null) {
                    val cleanFirst = firstEnc.replace("First Encounter / Earliest Unlock: ", "").replace("First Encounter: ", "")
                    "In Pokémon $edition, $name is first encountered at $cleanFirst. After this point, it is available at every single Poké Mart across the region."
                } else {
                    "In Pokémon $edition, $name is found at $location. $instructions"
                }

                return GuideQueryResult(
                    title = "$name Location Guide",
                    category = if (isNative) "ITEM & TM QUEST" else "ITEM & TM QUEST (CROSS-GENERATION)",
                    editionName = edition,
                    location = if (isNative) location else "$location ($fallbackKey)",
                    summary = if (!isNative) "$name is from Pokémon $fallbackKey ($location) and does not appear in Pokémon $edition."
                              else if (subAvail != null) "In Pokémon $edition, $name is first unlocked at ${resolvedCoords?.third ?: location}, then stocked at all regional Poké Marts."
                              else "In Pokémon $edition, $name is found at $location.",
                    steps = combinedSteps,
                    prerequisites = prereq,
                    mapAsset = mapAsset,
                    recommendedItems = listOf(name),
                    spokenAdvice = spokenText,
                    pinX = resolvedCoords?.first,
                    pinY = resolvedCoords?.second,
                    pinLocationName = resolvedCoords?.third ?: (if (isNative) location else null),
                    firstEncounter = firstEnc,
                    subsequentAvailability = subAvail,
                    itemIconAsset = resolveItemIconAsset(name, topicId = item.optString("id"), context = null)
                )
            }
        }
        return null
    }

    private fun queryRoadblock(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val rbArray = data?.optJSONArray("routes_and_roadblocks") ?: data?.optJSONArray("roadblocks") ?: return null
        for (i in 0 until rbArray.length()) {
            val rb = rbArray.getJSONObject(i)
            val name = rb.getString("name")
            val aliases = rb.optJSONArray("aliases")
            val aliasList = mutableListOf<String>()
            if (aliases != null) {
                for (a in 0 until aliases.length()) {
                    aliasList.add(aliases.getString(a))
                }
            }

            if (matchesSearch(q, name, aliasList, mode = mode)) {
                val versions = rb.getJSONObject("versions")
                val verKey = resolveVersionKey(versions, edition)
                val isNative = verKey != null
                val fallbackKey = if (versions.keys().hasNext()) versions.keys().next() else edition
                val verInfo = if (isNative) versions.getJSONObject(verKey!!) else versions.getJSONObject(fallbackKey)
                val location = verInfo.optString("location", "Route Obstacle")
                val solution = verInfo.optString("solution", verInfo.optString("instructions", ""))
                val prereq = if (verInfo.has("prerequisites")) verInfo.getString("prerequisites") else null
                val mapAsset = if (isNative) verInfo.optString("mapAsset", PokemonMapHabitatRepository.getMapAssetForCartridge(edition))
                               else PokemonMapHabitatRepository.getMapAssetForCartridge(edition)

                val stepList = solution.split("\n").filter { it.isNotBlank() }

                val pinObj = if (isNative) (verInfo.optJSONObject("pin") ?: rb.optJSONObject("pin")) else null
                val explicitX = pinObj?.optDouble("x", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitY = pinObj?.optDouble("y", -1.0)?.toFloat()?.takeIf { it >= 0 }
                val explicitArea = pinObj?.optString("area")

                val resolvedCoords = if (isNative) {
                    if (explicitX != null && explicitY != null) {
                        Triple(explicitX, explicitY, explicitArea ?: location)
                    } else {
                        resolveMapCoordinates(verKey ?: edition, location)
                    }
                } else null

                val steps = mutableListOf<String>()
                if (!isNative) {
                    steps.add("ℹ️ Note: $name is an obstacle in Pokémon $fallbackKey. In Pokémon $edition, this roadblock is not present.")
                }
                steps.addAll(if (stepList.isNotEmpty()) stepList else listOf(solution))

                return GuideQueryResult(
                    title = "Roadblock Solution: $name",
                    category = if (isNative) "ROUTE NAVIGATION" else "ROUTE NAVIGATION (CROSS-GENERATION)",
                    editionName = edition,
                    location = if (isNative) location else "$location ($fallbackKey)",
                    summary = if (!isNative) "$name is from Pokémon $fallbackKey ($location) and does not exist in Pokémon $edition."
                              else "Clearing $name in Pokémon $edition ($location).",
                    steps = steps,
                    prerequisites = prereq,
                    mapAsset = mapAsset,
                    recommendedItems = emptyList(),
                    spokenAdvice = if (!isNative) "$name is a roadblock from Pokémon $fallbackKey, not Pokémon $edition."
                                   else "To clear $name in Pokémon $edition at $location: $solution",
                    pinX = resolvedCoords?.first,
                    pinY = resolvedCoords?.second,
                    pinLocationName = resolvedCoords?.third ?: (if (isNative) location else null)
                )
            }
        }
        return null
    }

    private fun queryFacility(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val facArray = data?.optJSONArray("facilities") ?: return null
        for (i in 0 until facArray.length()) {
            val fac = facArray.getJSONObject(i)
            val name = fac.getString("name")
            val aliases = fac.optJSONArray("aliases")
            val aliasList = mutableListOf<String>()
            if (aliases != null) {
                for (a in 0 until aliases.length()) {
                    aliasList.add(aliases.getString(a))
                }
            }

            if (matchesSearch(q, name, aliasList, mode = mode)) {
                val versions = fac.getJSONObject("versions")
                val verKey = resolveVersionKey(versions, edition)
                val isNative = verKey != null
                val fallbackKey = if (versions.keys().hasNext()) versions.keys().next() else edition
                val verInfo = if (isNative) versions.getJSONObject(verKey!!) else versions.getJSONObject(fallbackKey)
                val city = verInfo.optString("city", verInfo.optString("location", "Key Facility"))
                val instructions = verInfo.optString("instructions", "")
                val cost = verInfo.optString("cost", "Free")
                val prereq = if (verInfo.has("prerequisites")) verInfo.getString("prerequisites") else "Cost: $cost"
                val mapAsset = if (isNative) verInfo.optString("mapAsset", PokemonMapHabitatRepository.getMapAssetForCartridge(edition))
                               else PokemonMapHabitatRepository.getMapAssetForCartridge(edition)
                val resolvedCoords = if (isNative) resolveMapCoordinates(verKey ?: edition, city) else null

                val steps = mutableListOf<String>()
                if (!isNative) {
                    steps.add("ℹ️ Note: $name is a facility in Pokémon $fallbackKey. In Pokémon $edition, this facility is not present.")
                }
                steps.addAll(listOf("Location: $city", "Cost / Requirement: $cost", "Instructions: $instructions"))

                return GuideQueryResult(
                    title = "$name Facility Guide",
                    category = if (isNative) "KEY FACILITY" else "KEY FACILITY (CROSS-GENERATION)",
                    editionName = edition,
                    location = if (isNative) city else "$city ($fallbackKey)",
                    summary = if (!isNative) "$name is from Pokémon $fallbackKey ($city) and does not exist in Pokémon $edition."
                              else "$name in Pokémon $edition is located at $city.",
                    steps = steps,
                    prerequisites = prereq,
                    mapAsset = mapAsset,
                    recommendedItems = emptyList(),
                    spokenAdvice = if (!isNative) "$name is a facility in Pokémon $fallbackKey, not Pokémon $edition."
                                   else "In Pokémon $edition, the $name is located at $city. $instructions",
                    pinX = resolvedCoords?.first,
                    pinY = resolvedCoords?.second,
                    pinLocationName = resolvedCoords?.third ?: (if (isNative) city else null)
                )
            }
        }
        return null
    }

    private fun queryGym(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val gymsArray = data?.optJSONArray("gyms") ?: return null
        for (i in 0 until gymsArray.length()) {
            val gym = gymsArray.getJSONObject(i)
            val leader = gym.optString("leader", gym.optString("name", "Gym Leader"))
            val city = gym.optString("city", "Gym")
            val badge = gym.optString("badge", "Badge")
            val gymType = gym.optString("type", gym.optString("specialty", "Pokémon"))
            val aliases = gym.optJSONArray("aliases")
            val aliasList = mutableListOf(city, badge, "$city gym", "$leader gym")
            if (aliases != null) {
                for (a in 0 until aliases.length()) {
                    aliasList.add(aliases.getString(a))
                }
            }

            if (matchesSearch(q, leader, aliasList, mode = mode)) {
                val versions = gym.getJSONObject("versions")
                val verKey = resolveVersionKey(versions, edition)
                val isNative = verKey != null
                val fallbackKey = if (versions.keys().hasNext()) versions.keys().next() else edition
                val verInfo = if (isNative) versions.getJSONObject(verKey!!) else versions.getJSONObject(fallbackKey)
                val strategy = verInfo.optString("strategy", "")

                val roster = if (verInfo.has("roster")) {
                    verInfo.getString("roster")
                } else if (verInfo.has("team")) {
                    val teamArr = verInfo.getJSONArray("team")
                    val teamMembers = mutableListOf<String>()
                    for (t in 0 until teamArr.length()) {
                        val m = teamArr.getJSONObject(t)
                        val mName = m.optString("name", "Pokémon")
                        val mLvl = m.optInt("level", 0)
                        teamMembers.add("$mName (Lv. $mLvl)")
                    }
                    teamMembers.joinToString(", ")
                } else {
                    ""
                }

                val levelCap = verInfo.optInt("leaderLevel", verInfo.optInt("levelCap", gym.optInt("levelCap", 15)))

                val counters = gym.optJSONArray("recommendedCounters")
                val counterList = mutableListOf<String>()
                if (counters != null) {
                    for (c in 0 until counters.length()) counterList.add(counters.getString(c))
                }

                val steps = mutableListOf<String>()
                if (!isNative) {
                    steps.add("ℹ️ Note: $leader ($badge) is a Gym Leader in Pokémon $fallbackKey. In Pokémon $edition, this Gym Leader is not present.")
                }
                steps.add("City: $city | Badge Awarded: $badge")
                steps.add("Leader Specialty: $gymType Type | Highest Level: Lv. $levelCap")
                if (roster.isNotBlank()) {
                    steps.add("Roster: $roster")
                }
                if (counterList.isNotEmpty()) {
                    steps.add("Recommended Counters:\n• " + counterList.joinToString("\n• "))
                }
                steps.add("Battle Strategy:\n$strategy")

                val mapAsset = if (isNative) verInfo.optString("mapAsset", PokemonMapHabitatRepository.getMapAssetForCartridge(edition))
                               else PokemonMapHabitatRepository.getMapAssetForCartridge(edition)
                val resolvedCoords = if (isNative) resolveMapCoordinates(verKey ?: edition, city) else null

                return GuideQueryResult(
                    title = "Gym Guide: $leader ($badge)",
                    category = if (isNative) "GYM BATTLE STRATEGY" else "GYM BATTLE STRATEGY (CROSS-GENERATION)",
                    editionName = edition,
                    location = if (isNative) "$city Gym" else "$city Gym ($fallbackKey)",
                    summary = if (!isNative) "$leader ($badge) is a Gym Leader in Pokémon $fallbackKey and does not appear in Pokémon $edition."
                              else "Tactical strategy for $leader at $city Gym in Pokémon $edition.",
                    steps = steps,
                    prerequisites = "Recommended Level Cap: Lv. $levelCap",
                    mapAsset = mapAsset,
                    recommendedItems = emptyList(),
                    spokenAdvice = if (!isNative) "Gym Leader $leader is from Pokémon $fallbackKey at $city Gym, and does not appear in Pokémon $edition."
                                   else "Gym Leader $leader uses ${gymType}-type Pokémon with a level cap around level $levelCap. $strategy",
                    pinX = resolvedCoords?.first,
                    pinY = resolvedCoords?.second,
                    pinLocationName = resolvedCoords?.third ?: (if (isNative) "$city Gym" else null)
                )
            }
        }
        return null
    }

    private fun queryPokemonBuild(q: String, data: JSONObject?, edition: String, mode: GuideSearchMode = GuideSearchMode.PHRASE): GuideQueryResult? {
        val buildsArray = data?.optJSONArray("pokemon_builds") ?: return null
        for (i in 0 until buildsArray.length()) {
            val build = buildsArray.getJSONObject(i)
            val name = build.getString("name")
            if (matchesSearch(q, name, mode = mode)) {
                val inGame = build.optJSONObject("inGameBuild")
                val role = inGame?.optString("role") ?: build.optString("role", "Battler")
                val heldItem = inGame?.optString("heldItem") ?: ""
                val tactical = inGame?.optString("tacticalAdvice") ?: build.optString("strategy", "")

                val movesList = mutableListOf<String>()
                if (inGame != null && inGame.has("moveset")) {
                    val movesArr = inGame.getJSONArray("moveset")
                    for (m in 0 until movesArr.length()) {
                        val mv = movesArr.getJSONObject(m)
                        movesList.add("• ${mv.getString("move")} (${mv.getString("type")} - Pwr ${mv.getInt("power")}): ${mv.getString("source")}")
                    }
                } else {
                    val optimalMoveset = build.optJSONArray("optimalMoveset")
                    if (optimalMoveset != null) {
                        for (m in 0 until optimalMoveset.length()) movesList.add("• " + optimalMoveset.getString(m))
                    }
                }

                val itemsList = mutableListOf<String>()
                val bestItems = build.optJSONArray("bestItems")
                if (bestItems != null) {
                    for (b in 0 until bestItems.length()) itemsList.add(bestItems.getString(b))
                } else {
                    val recommendedHoldItems = build.optJSONArray("recommendedHoldItems")
                    if (recommendedHoldItems != null) {
                        for (h in 0 until recommendedHoldItems.length()) itemsList.add(recommendedHoldItems.getString(h))
                    }
                }
                if (heldItem.isNotBlank() && !itemsList.contains(heldItem)) {
                    itemsList.add(heldItem)
                }

                val evSpread = build.optString("evSpread", "")

                val steps = mutableListOf<String>()
                steps.add("Role: $role")
                if (itemsList.isNotEmpty()) steps.add("Recommended Held Items: " + itemsList.joinToString(", "))
                if (evSpread.isNotBlank()) steps.add("Optimal EV Spread: $evSpread")
                if (movesList.isNotEmpty()) {
                    steps.add("Recommended Moveset:")
                    steps.addAll(movesList)
                }
                if (tactical.isNotBlank()) steps.add("Tactical Strategy:\n$tactical")

                return GuideQueryResult(
                    title = "$name Best Build & Recommended Items",
                    category = "POKÉMON MASTER BUILD",
                    editionName = edition,
                    location = name,
                    summary = "Optimal in-game and competitive build for $name in Pokémon $edition.",
                    steps = steps,
                    prerequisites = "Recommended Items: " + itemsList.joinToString(", "),
                    mapAsset = PokemonMapHabitatRepository.getMapAssetForCartridge(edition),
                    recommendedItems = itemsList,
                    spokenAdvice = "For $name, the recommended build is $role. Recommended items include " + itemsList.joinToString(", ") + ". $tactical"
                )
            }
        }
        return null
    }

    private fun resolveVersionKey(versionsObj: JSONObject, edition: String): String? {
        val keys = versionsObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k.equals(edition, ignoreCase = true)) return k
        }
        val cleanEdition = edition.trim()
        val candidates = PokemonMapHabitatRepository.CARTRIDGE_CANDIDATE_KEYS.entries
            .firstOrNull { it.key.equals(cleanEdition, ignoreCase = true) || it.key.replace(" ", "").equals(cleanEdition.replace(" ", ""), ignoreCase = true) }?.value
            ?: PokemonMapHabitatRepository.CARTRIDGE_CANDIDATE_KEYS[cleanEdition]
            ?: listOf(cleanEdition)
        for (cand in candidates) {
            val kIter = versionsObj.keys()
            while (kIter.hasNext()) {
                val k = kIter.next()
                if (k.equals(cand, ignoreCase = true)) return k
            }
        }
        return null
    }

    private fun resolveMartProgression(
        name: String,
        location: String,
        pinArea: String?,
        edition: String,
        instructions: String,
        explicitFirstEncounter: String?,
        explicitSubsequentAvailability: String?
    ): Pair<String?, String?> {
        if (!explicitFirstEncounter.isNullOrBlank() || !explicitSubsequentAvailability.isNullOrBlank()) {
            return Pair(explicitFirstEncounter, explicitSubsequentAvailability)
        }
        val lowerName = name.lowercase()
        val lowerLoc = location.lowercase()
        val lowerInst = instructions.lowercase()
        val isMartOrProgressiveItem = lowerName.contains("potion") ||
            lowerName.contains("ball") ||
            lowerName.contains("repel") ||
            lowerName.contains("revive") ||
            lowerName.contains("antidote") ||
            lowerName.contains("heal") ||
            lowerName.contains("awakening") ||
            lowerName.contains("escape rope") ||
            lowerLoc.contains("mart") ||
            lowerInst.contains("mart") ||
            lowerInst.contains("pokémon center") ||
            lowerInst.contains("pokemon center")

        if (!isMartOrProgressiveItem) return Pair(null, null)

        val firstTown = pinArea ?: location.split("&").first().trim()
        val regionName = try {
            com.example.pokedex.data.getRegionMapEntryForEdition(edition).title
        } catch (_: Exception) {
            "Regional"
        }

        val firstEnc = "First Encounter / Earliest Unlock: $firstTown"
        val subAvail = when {
            lowerName.contains("ultra ball") ->
                "Region-Wide Availability: Once unlocked at $firstTown (Gym 5+), stocked and available for purchase at every single Poké Mart across $regionName!"
            lowerName.contains("great ball") ->
                "Region-Wide Availability: Once unlocked at $firstTown (Gym 1-3), stocked and available for purchase at every single Poké Mart across $regionName!"
            lowerName.contains("hyper potion") ->
                "Region-Wide Availability: Once unlocked at $firstTown (Gym 5+), stocked and available for purchase at every single Poké Mart across $regionName!"
            lowerName.contains("max potion") || lowerName.contains("full restore") ->
                "Region-Wide Availability: Once unlocked at $firstTown (Gym 7-8), stocked and available for purchase at every single Poké Mart across $regionName!"
            lowerName.contains("super potion") || lowerName.contains("potion") || lowerName.contains("poké ball") || lowerName.contains("poke ball") || lowerName.contains("antidote") ->
                "Region-Wide Availability: After unlocking here at $firstTown, this item is standard stock at every single Poké Mart / Pokémon Center across $regionName!"
            else ->
                "Region-Wide Availability: After unlocking here at $firstTown, this item is stocked and available at every single Poké Mart in $regionName!"
        }
        return Pair(firstEnc, subAvail)
    }
}
