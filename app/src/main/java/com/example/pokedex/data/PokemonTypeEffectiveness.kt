package com.example.pokedex.data

import com.example.pokedex.R
import java.util.Locale

data class TypeWeakness(
    val type: String,
    val multiplier: Float
)

data class TypeMatchupSummary(
    val superEffectiveQuad: List<String>,      // 4.0x
    val effectiveDouble: List<String>,          // 2.0x
    val normalEffective: List<String>,          // 1.0x
    val notVeryEffectiveHalf: List<String>,     // 0.5x
    val notVeryEffectiveQuarter: List<String>,  // 0.25x
    val immune: List<String>                    // 0.0x
)

/**
 * Computes exact defensive type weaknesses (4x and 2x) for all 1,025 Pokémon
 * and all regional/Mega/Gigantamax forms based on the official 18-type matrix.
 */
object PokemonTypeEffectiveness {

    val ALL_TYPES: List<String> = listOf(
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
        "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
        "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    )

    private data class DefensiveProfile(
        val weakTo: Set<String>,
        val resists: Set<String> = emptySet(),
        val immuneTo: Set<String> = emptySet()
    )

    private val DEFENSIVE_CHART: Map<String, DefensiveProfile> = mapOf(
        "Normal" to DefensiveProfile(
            weakTo = setOf("Fighting"),
            immuneTo = setOf("Ghost")
        ),
        "Fire" to DefensiveProfile(
            weakTo = setOf("Water", "Ground", "Rock"),
            resists = setOf("Fire", "Grass", "Ice", "Bug", "Steel", "Fairy")
        ),
        "Water" to DefensiveProfile(
            weakTo = setOf("Electric", "Grass"),
            resists = setOf("Fire", "Water", "Ice", "Steel")
        ),
        "Electric" to DefensiveProfile(
            weakTo = setOf("Ground"),
            resists = setOf("Electric", "Flying", "Steel")
        ),
        "Grass" to DefensiveProfile(
            weakTo = setOf("Fire", "Ice", "Poison", "Flying", "Bug"),
            resists = setOf("Water", "Electric", "Grass", "Ground")
        ),
        "Ice" to DefensiveProfile(
            weakTo = setOf("Fire", "Fighting", "Rock", "Steel"),
            resists = setOf("Ice")
        ),
        "Fighting" to DefensiveProfile(
            weakTo = setOf("Flying", "Psychic", "Fairy"),
            resists = setOf("Bug", "Rock", "Dark")
        ),
        "Poison" to DefensiveProfile(
            weakTo = setOf("Ground", "Psychic"),
            resists = setOf("Grass", "Fighting", "Poison", "Bug", "Fairy")
        ),
        "Ground" to DefensiveProfile(
            weakTo = setOf("Water", "Grass", "Ice"),
            resists = setOf("Poison", "Rock"),
            immuneTo = setOf("Electric")
        ),
        "Flying" to DefensiveProfile(
            weakTo = setOf("Electric", "Ice", "Rock"),
            resists = setOf("Grass", "Fighting", "Bug"),
            immuneTo = setOf("Ground")
        ),
        "Psychic" to DefensiveProfile(
            weakTo = setOf("Bug", "Ghost", "Dark"),
            resists = setOf("Fighting", "Psychic")
        ),
        "Bug" to DefensiveProfile(
            weakTo = setOf("Fire", "Flying", "Rock"),
            resists = setOf("Grass", "Fighting", "Ground")
        ),
        "Rock" to DefensiveProfile(
            weakTo = setOf("Water", "Grass", "Fighting", "Ground", "Steel"),
            resists = setOf("Normal", "Fire", "Poison", "Flying")
        ),
        "Ghost" to DefensiveProfile(
            weakTo = setOf("Ghost", "Dark"),
            resists = setOf("Poison", "Bug"),
            immuneTo = setOf("Normal", "Fighting")
        ),
        "Dragon" to DefensiveProfile(
            weakTo = setOf("Ice", "Dragon", "Fairy"),
            resists = setOf("Fire", "Water", "Electric", "Grass")
        ),
        "Dark" to DefensiveProfile(
            weakTo = setOf("Fighting", "Bug", "Fairy"),
            resists = setOf("Ghost", "Dark"),
            immuneTo = setOf("Psychic")
        ),
        "Steel" to DefensiveProfile(
            weakTo = setOf("Fire", "Fighting", "Ground"),
            resists = setOf("Normal", "Grass", "Ice", "Flying", "Psychic", "Bug", "Rock", "Dragon", "Steel", "Fairy"),
            immuneTo = setOf("Poison")
        ),
        "Fairy" to DefensiveProfile(
            weakTo = setOf("Poison", "Steel"),
            resists = setOf("Fighting", "Bug", "Dark"),
            immuneTo = setOf("Dragon")
        )
    )

    private val OFFENSIVE_CHART: Map<String, Set<String>> = mapOf(
        "Normal" to emptySet(),
        "Fire" to setOf("Grass", "Ice", "Bug", "Steel"),
        "Water" to setOf("Fire", "Ground", "Rock"),
        "Electric" to setOf("Water", "Flying"),
        "Grass" to setOf("Water", "Ground", "Rock"),
        "Ice" to setOf("Grass", "Ground", "Flying", "Dragon"),
        "Fighting" to setOf("Normal", "Ice", "Rock", "Dark", "Steel"),
        "Poison" to setOf("Grass", "Fairy"),
        "Ground" to setOf("Fire", "Electric", "Poison", "Rock", "Steel"),
        "Flying" to setOf("Grass", "Fighting", "Bug"),
        "Psychic" to setOf("Fighting", "Poison"),
        "Bug" to setOf("Grass", "Psychic", "Dark"),
        "Rock" to setOf("Fire", "Ice", "Flying", "Bug"),
        "Ghost" to setOf("Psychic", "Ghost"),
        "Dragon" to setOf("Dragon"),
        "Dark" to setOf("Psychic", "Ghost"),
        "Steel" to setOf("Ice", "Rock", "Fairy"),
        "Fairy" to setOf("Fighting", "Dragon", "Dark")
    )

    private val SPANISH_TO_ENGLISH_TYPE: Map<String, String> = mapOf(
        "normal" to "Normal",
        "fuego" to "Fire",
        "agua" to "Water",
        "eléctrico" to "Electric",
        "electrico" to "Electric",
        "planta" to "Grass",
        "hielo" to "Ice",
        "lucha" to "Fighting",
        "veneno" to "Poison",
        "tierra" to "Ground",
        "volador" to "Flying",
        "psíquico" to "Psychic",
        "psiquico" to "Psychic",
        "bicho" to "Bug",
        "roca" to "Rock",
        "fantasma" to "Ghost",
        "dragón" to "Dragon",
        "dragon" to "Dragon",
        "siniestro" to "Dark",
        "acero" to "Steel",
        "hada" to "Fairy"
    )

    fun normalizeTypeName(raw: String): String {
        val cleaned = raw.trim()
        if (cleaned.isEmpty()) return ""
        val canonical = ALL_TYPES.firstOrNull { it.equals(cleaned, ignoreCase = true) }
        if (canonical != null) return canonical
        return SPANISH_TO_ENGLISH_TYPE[cleaned.lowercase(Locale.ROOT)] ?: cleaned
    }

    fun getDefensiveMultiplier(attackingType: String, defendingTypes: List<String>): Float {
        val canonicalAttack = normalizeTypeName(attackingType)
        var multiplier = 1.0f
        for (defType in defendingTypes) {
            val profile = DEFENSIVE_CHART[normalizeTypeName(defType)] ?: continue
            multiplier *= when {
                canonicalAttack in profile.immuneTo -> 0.0f
                canonicalAttack in profile.weakTo -> 2.0f
                canonicalAttack in profile.resists -> 0.5f
                else -> 1.0f
            }
        }
        return multiplier
    }

    /**
     * Returns all defensive weaknesses (multiplier > 1.0f, i.e. 4x and 2x) for a slash- or comma-separated
     * type string such as "Water", "Fire", "Fire/Flying", or "Grass/Poison".
     * Sorted with 4x double weaknesses first, followed by 2x weaknesses in canonical type order.
     */
    fun getWeaknesses(typeString: String): List<TypeWeakness> {
        val defendingTypes = typeString
            .split("/", ",", " ")
            .map { normalizeTypeName(it) }
            .filter { it in DEFENSIVE_CHART }
            .distinct()

        if (defendingTypes.isEmpty()) return emptyList()

        return ALL_TYPES.mapNotNull { attackType ->
            val mult = getDefensiveMultiplier(attackType, defendingTypes)
            if (mult > 1.0f) TypeWeakness(type = attackType, multiplier = mult) else null
        }.sortedByDescending { it.multiplier }
    }

    /**
     * Returns a complete breakdown of all 18 types categorized by defensive multiplier:
     * 4x Super Effective, 2x Effective, 1x Just Effective / Neutral, 0.5x & 0.25x Not Very Effective, and 0x Immune.
     */
    fun getFullMatchups(typeString: String): TypeMatchupSummary {
        val defendingTypes = typeString
            .split("/", ",", " ")
            .map { normalizeTypeName(it) }
            .filter { it in DEFENSIVE_CHART }
            .distinct()

        if (defendingTypes.isEmpty()) {
            return TypeMatchupSummary(
                superEffectiveQuad = emptyList(),
                effectiveDouble = emptyList(),
                normalEffective = ALL_TYPES,
                notVeryEffectiveHalf = emptyList(),
                notVeryEffectiveQuarter = emptyList(),
                immune = emptyList()
            )
        }

        val quad = mutableListOf<String>()
        val dbl = mutableListOf<String>()
        val normal = mutableListOf<String>()
        val half = mutableListOf<String>()
        val quarter = mutableListOf<String>()
        val immune = mutableListOf<String>()

        for (attackType in ALL_TYPES) {
            val mult = getDefensiveMultiplier(attackType, defendingTypes)
            when {
                mult == 0.0f -> immune.add(attackType)
                mult == 0.25f -> quarter.add(attackType)
                mult == 0.5f -> half.add(attackType)
                mult == 1.0f -> normal.add(attackType)
                mult == 2.0f -> dbl.add(attackType)
                mult >= 4.0f -> quad.add(attackType)
                else -> normal.add(attackType)
            }
        }

        return TypeMatchupSummary(
            superEffectiveQuad = quad,
            effectiveDouble = dbl,
            normalEffective = normal,
            notVeryEffectiveHalf = half,
            notVeryEffectiveQuarter = quarter,
            immune = immune
        )
    }

    /**
     * Returns all types that this Pokémon deals super-effective (2×) damage against
     * using its natural Same-Type Attack Bonus (STAB) moves.
     * For dual-type Pokémon, this is the combined distinct union of both types' offensive coverage.
     */
    fun getOffensiveStrengths(typeString: String): List<String> {
        val attackingTypes = typeString
            .split("/", ",", " ")
            .map { normalizeTypeName(it) }
            .filter { it in OFFENSIVE_CHART }
            .distinct()

        if (attackingTypes.isEmpty()) return emptyList()

        val superEffectiveTargets = mutableSetOf<String>()
        for (attType in attackingTypes) {
            val targets = OFFENSIVE_CHART[attType] ?: emptySet()
            superEffectiveTargets.addAll(targets)
        }

        // Return in canonical type order
        return ALL_TYPES.filter { it in superEffectiveTargets }
    }

    /**
     * Official-style high-contrast badge background color (ARGB Long) for each Pokémon type.
     */
    fun getTypeColor(type: String): Long {
        return when (normalizeTypeName(type)) {
            "Normal" -> 0xFFA8A77AL
            "Fire" -> 0xFFEE8130L
            "Water" -> 0xFF6390F0L
            "Electric" -> 0xFFF7D02CL
            "Grass" -> 0xFF7AC74FL
            "Ice" -> 0xFF96D9D6L
            "Fighting" -> 0xFFC22E28L
            "Poison" -> 0xFFA33EA1L
            "Ground" -> 0xFFE2BF65L
            "Flying" -> 0xFFA98FF3L
            "Psychic" -> 0xFFF95587L
            "Bug" -> 0xFFA6B91AL
            "Rock" -> 0xFFB6A136L
            "Ghost" -> 0xFF735797L
            "Dragon" -> 0xFF6F35FCL
            "Dark" -> 0xFF705746L
            "Steel" -> 0xFFB7B7CEL
            "Fairy" -> 0xFFD685ADL
            else -> 0xFF546E7AL
        }
    }

    /**
     * High-contrast text color (ARGB Long) for a type pill background.
     */
    fun getTypeTextColor(type: String): Long {
        return when (normalizeTypeName(type)) {
            "Electric", "Ice", "Ground", "Steel", "Normal" -> 0xFF1B1B1BL
            else -> 0xFFFFFFFFL
        }
    }

    /**
     * Localized display label for a single type (e.g., "Electric" -> "ELÉCTRICO" in Spanish or "ELECTRIC" in English).
     */
    fun translateType(type: String, isSpanish: Boolean): String {
        val canonical = normalizeTypeName(type)
        if (!isSpanish) return canonical
        return when (canonical) {
            "Normal" -> "Normal"
            "Fire" -> "Fuego"
            "Water" -> "Agua"
            "Electric" -> "Eléctrico"
            "Grass" -> "Planta"
            "Ice" -> "Hielo"
            "Fighting" -> "Lucha"
            "Poison" -> "Veneno"
            "Ground" -> "Tierra"
            "Flying" -> "Volador"
            "Psychic" -> "Psíquico"
            "Bug" -> "Bicho"
            "Rock" -> "Roca"
            "Ghost" -> "Fantasma"
            "Dragon" -> "Dragón"
            "Dark" -> "Siniestro"
            "Steel" -> "Acero"
            "Fairy" -> "Hada"
            else -> canonical
        }
    }

    /**
     * Official compact abbreviation (3 to 5 characters) for type weakness badges.
     * Prevents horizontal scrolling across all single- and dual-type Pokémon.
     */
    fun getAbbreviatedType(type: String, isSpanish: Boolean = false): String {
        val canonical = normalizeTypeName(type)
        return if (isSpanish) {
            when (canonical) {
                "Normal" -> "NORM"
                "Fire" -> "FUEGO"
                "Water" -> "AGUA"
                "Electric" -> "ELEC"
                "Grass" -> "PLAN"
                "Ice" -> "HIELO"
                "Fighting" -> "LUCHA"
                "Poison" -> "VEN"
                "Ground" -> "TIER"
                "Flying" -> "VOL"
                "Psychic" -> "PSI"
                "Bug" -> "BICHO"
                "Rock" -> "ROCA"
                "Ghost" -> "FANT"
                "Dragon" -> "DRAG"
                "Dark" -> "SIN"
                "Steel" -> "ACER"
                "Fairy" -> "HADA"
                else -> canonical.take(4).uppercase()
            }
        } else {
            when (canonical) {
                "Normal" -> "NORM"
                "Fire" -> "FIRE"
                "Water" -> "WATER"
                "Electric" -> "ELEC"
                "Grass" -> "GRASS"
                "Ice" -> "ICE"
                "Fighting" -> "FIGHT"
                "Poison" -> "POIS"
                "Ground" -> "GRND"
                "Flying" -> "FLY"
                "Psychic" -> "PSY"
                "Bug" -> "BUG"
                "Rock" -> "ROCK"
                "Ghost" -> "GHOST"
                "Dragon" -> "DRAG"
                "Dark" -> "DARK"
                "Steel" -> "STEEL"
                "Fairy" -> "FAIRY"
                else -> canonical.take(4).uppercase()
            }
        }
    }

    /**
     * Returns the 24x24 vector drawable resource ID for a given Pokémon type.
     */
    fun getTypeIconRes(type: String): Int {
        return when (normalizeTypeName(type)) {
            "Normal" -> R.drawable.ic_type_normal
            "Fire" -> R.drawable.ic_type_fire
            "Water" -> R.drawable.ic_type_water
            "Electric" -> R.drawable.ic_type_electric
            "Grass" -> R.drawable.ic_type_grass
            "Ice" -> R.drawable.ic_type_ice
            "Fighting" -> R.drawable.ic_type_fighting
            "Poison" -> R.drawable.ic_type_poison
            "Ground" -> R.drawable.ic_type_ground
            "Flying" -> R.drawable.ic_type_flying
            "Psychic" -> R.drawable.ic_type_psychic
            "Bug" -> R.drawable.ic_type_bug
            "Rock" -> R.drawable.ic_type_rock
            "Ghost" -> R.drawable.ic_type_ghost
            "Dragon" -> R.drawable.ic_type_dragon
            "Dark" -> R.drawable.ic_type_dark
            "Steel" -> R.drawable.ic_type_steel
            "Fairy" -> R.drawable.ic_type_fairy
            else -> R.drawable.ic_type_normal
        }
    }

    /**
     * Formats a natural spoken summary of a Pokémon's weaknesses for Dexter TTS.
     */
    fun formatWeaknessesForSpeech(
        pokemonName: String,
        weaknesses: List<TypeWeakness>,
        isSpanish: Boolean
    ): String {
        if (weaknesses.isEmpty()) {
            return if (isSpanish) {
                "$pokemonName no tiene debilidades de tipo."
            } else {
                "$pokemonName has no type weaknesses."
            }
        }
        val quad = weaknesses.filter { it.multiplier >= 4.0f }.map { translateType(it.type, isSpanish) }
        val double = weaknesses.filter { it.multiplier < 4.0f }.map { translateType(it.type, isSpanish) }

        fun joinTypes(items: List<String>, andWord: String): String = when (items.size) {
            0 -> ""
            1 -> items[0]
            2 -> "${items[0]} $andWord ${items[1]}"
            else -> items.dropLast(1).joinToString(", ") + " $andWord ${items.last()}"
        }

        return if (isSpanish) {
            when {
                quad.isNotEmpty() && double.isNotEmpty() ->
                    "$pokemonName: Súper eficaz por cuatro contra ${joinTypes(quad, "y")}, y eficaz por dos contra ${joinTypes(double, "y")}."
                quad.isNotEmpty() ->
                    "$pokemonName: Súper eficaz por cuatro contra ${joinTypes(quad, "y")}."
                else ->
                    "$pokemonName: Eficaz por dos contra ${joinTypes(double, "y")}."
            }
        } else {
            when {
                quad.isNotEmpty() && double.isNotEmpty() ->
                    "$pokemonName: Super effective 4 times against ${joinTypes(quad, "and")}, and effective 2 times against ${joinTypes(double, "and")}."
                quad.isNotEmpty() ->
                    "$pokemonName: Super effective 4 times against ${joinTypes(quad, "and")}."
                else ->
                    "$pokemonName: Effective 2 times against ${joinTypes(double, "and")}."
            }
        }
    }

    /**
     * Formats the complete type matchup sheet (4x, 2x, 1x, resists, immunities) for speech.
     */
    fun formatFullMatchupsForSpeech(
        pokemonName: String,
        summary: TypeMatchupSummary,
        isSpanish: Boolean,
        offensiveStrengths: List<String> = emptyList()
    ): String {
        fun join(items: List<String>, andWord: String): String = when (items.size) {
            0 -> ""
            1 -> translateType(items[0], isSpanish)
            2 -> "${translateType(items[0], isSpanish)} $andWord ${translateType(items[1], isSpanish)}"
            else -> items.dropLast(1).joinToString(", ") { translateType(it, isSpanish) } + " $andWord ${translateType(items.last(), isSpanish)}"
        }

        val parts = mutableListOf<String>()
        if (isSpanish) {
            if (offensiveStrengths.isNotEmpty()) {
                parts.add("Ventajas ofensivas propias contra: ${join(offensiveStrengths, "y")}")
            }
            if (summary.superEffectiveQuad.isNotEmpty()) {
                parts.add("Súper eficaz por 4: ${join(summary.superEffectiveQuad, "y")}")
            }
            if (summary.effectiveDouble.isNotEmpty()) {
                parts.add("Eficaz por 2: ${join(summary.effectiveDouble, "y")}")
            }
            if (summary.normalEffective.isNotEmpty()) {
                parts.add("Eficaz normal por 1: ${join(summary.normalEffective, "y")}")
            }
            val resists = summary.notVeryEffectiveHalf + summary.notVeryEffectiveQuarter
            if (resists.isNotEmpty()) {
                parts.add("Resistencias: ${join(resists, "y")}")
            }
            if (summary.immune.isNotEmpty()) {
                parts.add("Inmunidades: ${join(summary.immune, "y")}")
            }
            return "$pokemonName. Relación de tipos: ${parts.joinToString(". ")}."
        } else {
            if (offensiveStrengths.isNotEmpty()) {
                parts.add("Offensive STAB advantages against ${join(offensiveStrengths, "and")}")
            }
            if (summary.superEffectiveQuad.isNotEmpty()) {
                parts.add("Super effective 4x against ${join(summary.superEffectiveQuad, "and")}")
            }
            if (summary.effectiveDouble.isNotEmpty()) {
                parts.add("Effective 2x against ${join(summary.effectiveDouble, "and")}")
            }
            if (summary.normalEffective.isNotEmpty()) {
                parts.add("Regular effective 1x against ${join(summary.normalEffective, "and")}")
            }
            val resists = summary.notVeryEffectiveHalf + summary.notVeryEffectiveQuarter
            if (resists.isNotEmpty()) {
                parts.add("Resists ${join(resists, "and")}")
            }
            if (summary.immune.isNotEmpty()) {
                parts.add("Immune to ${join(summary.immune, "and")}")
            }
            return "$pokemonName type matchups: ${parts.joinToString(". ")}."
        }
    }
}
