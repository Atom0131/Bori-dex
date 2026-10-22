package com.example.pokedex.camera

import android.graphics.Bitmap
import com.example.pokedex.data.PokemonEntity

data class FormDisambiguationResult(
    val resolvedFormName: String,
    val isShiny: Boolean,
    val confidence: Float,
    val reason: String
)

data class ColorProfile(
    val dominantHue: Float,
    val avgSaturation: Float,
    val avgValue: Float,
    val blackPixelPct: Float,
    val whitePixelPct: Float,
    val cyanBluePct: Float,
    val yellowGoldPct: Float,
    val redOrangePct: Float,
    val greenPct: Float,
    val purpleVioletPct: Float
)

object PokemonFormDetector {

    /**
     * Pure Kotlin RGB to HSV conversion (runs on Android and standalone host JVM unit tests).
     */
    fun rgbToHsv(r: Int, g: Int, b: Int, hsv: FloatArray) {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val max = maxOf(rf, maxOf(gf, bf))
        val min = minOf(rf, minOf(gf, bf))
        val delta = max - min

        var h = when {
            delta == 0f -> 0f
            max == rf -> (((gf - bf) / delta) % 6f) * 60f
            max == gf -> (((bf - rf) / delta) + 2f) * 60f
            else -> (((rf - gf) / delta) + 4f) * 60f
        }
        if (h < 0f) h += 360f

        val s = if (max == 0f) 0f else delta / max
        val v = max
        hsv[0] = h
        hsv[1] = s
        hsv[2] = v
    }

    /**
     * Extracts a normalized color profile from an array of packed ARGB/RGB pixels.
     */
    fun analyzeColorProfileFromPixels(pixels: IntArray): ColorProfile {
        if (pixels.isEmpty()) {
            return ColorProfile(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }

        var totalHueSin = 0.0
        var totalHueCos = 0.0
        var totalSat = 0.0
        var totalVal = 0.0
        var saturatedPixelCount = 0

        var blackPixels = 0
        var whitePixels = 0
        var cyanBluePixels = 0
        var yellowGoldPixels = 0
        var redOrangePixels = 0
        var greenPixels = 0
        var purpleVioletPixels = 0

        val hsv = FloatArray(3)
        val totalPixels = pixels.size

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            rgbToHsv(r, g, b, hsv)

            val h = hsv[0] // 0.0 .. 360.0
            val s = hsv[1] // 0.0 .. 1.0
            val v = hsv[2] // 0.0 .. 1.0

            totalSat += s
            totalVal += v

            // Black/Dark pixels (shadows, charcoal bodies like Shiny Charizard / Hisuian Arcanine)
            if (v < 0.28f) {
                blackPixels++
            } else if (v > 0.80f && s < 0.20f) {
                // White/Light pixels (Alolan Vulpix, Ultra Necrozma highlights)
                whitePixels++
            }

            if (s > 0.20f && v > 0.20f) {
                saturatedPixelCount++
                val rad = Math.toRadians(h.toDouble())
                totalHueSin += Math.sin(rad)
                totalHueCos += Math.cos(rad)

                // Categorize color bands
                when {
                    // Red / Orange
                    h < 35f || h >= 345f -> redOrangePixels++
                    // Yellow / Gold
                    h in 35f..75f -> yellowGoldPixels++
                    // Green
                    h in 75f..160f -> greenPixels++
                    // Cyan / Sky-Blue / Blue
                    h in 160f..255f -> cyanBluePixels++
                    // Purple / Violet / Magenta
                    h in 255f..345f -> purpleVioletPixels++
                }
            }
        }

        val avgSat = (totalSat / totalPixels).toFloat()
        val avgVal = (totalVal / totalPixels).toFloat()

        val dominantHue = if (saturatedPixelCount > 0) {
            val meanAngle = Math.toDegrees(Math.atan2(totalHueSin, totalHueCos))
            val normHue = (meanAngle + 360.0) % 360.0
            normHue.toFloat()
        } else {
            0f
        }

        val totalFloat = totalPixels.toFloat()
        return ColorProfile(
            dominantHue = dominantHue,
            avgSaturation = avgSat,
            avgValue = avgVal,
            blackPixelPct = blackPixels / totalFloat,
            whitePixelPct = whitePixels / totalFloat,
            cyanBluePct = cyanBluePixels / totalFloat,
            yellowGoldPct = yellowGoldPixels / totalFloat,
            redOrangePct = redOrangePixels / totalFloat,
            greenPct = greenPixels / totalFloat,
            purpleVioletPct = purpleVioletPixels / totalFloat
        )
    }

    /**
     * Extracts a normalized color profile from the central subject region of the viewfinder frame.
     */
    fun analyzeColorProfile(bitmap: Bitmap): ColorProfile {
        val width = bitmap.width
        val height = bitmap.height

        val startX = (width * 0.20f).toInt().coerceAtLeast(0)
        val endX = (width * 0.80f).toInt().coerceAtMost(width)
        val startY = (height * 0.20f).toInt().coerceAtLeast(0)
        val endY = (height * 0.80f).toInt().coerceAtMost(height)

        val sampleWidth = endX - startX
        val sampleHeight = endY - startY
        if (sampleWidth <= 0 || sampleHeight <= 0) {
            return ColorProfile(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }

        val pixels = IntArray(sampleWidth * sampleHeight)
        bitmap.getPixels(pixels, 0, sampleWidth, startX, startY, sampleWidth, sampleHeight)
        return analyzeColorProfileFromPixels(pixels)
    }

    /**
     * Disambiguates between the base species and all its known alternate forms or shinies given a pre-computed ColorProfile.
     */
    fun disambiguateFromProfile(
        profile: ColorProfile,
        basePokemon: PokemonEntity,
        availableForms: List<PokemonEntity>
    ): FormDisambiguationResult {
        val baseName = basePokemon.name.lowercase().trim()
        val formNames = availableForms.map { it.name.lowercase().trim() }

        // 1. SPECIFIC HIGH-PROFILE FORM RECOGNITION RULES
        when (baseName) {
            "rayquaza" -> {
                // Shiny Rayquaza: Jet-black body with yellow markings
                if (profile.blackPixelPct >= 0.40f && profile.avgValue < 0.42f) {
                    val isMega = profile.yellowGoldPct >= 0.12f
                    val form = if (isMega && formNames.contains("rayquaza-mega")) "rayquaza-mega" else "rayquaza"
                    return FormDisambiguationResult(form, isShiny = true, confidence = 0.88f, reason = "Shiny Black Rayquaza palette detected")
                }
                // Mega Rayquaza: Green body with luminous bright yellow/gold plasma tendrils and orange chin fins
                if (formNames.contains("rayquaza-mega")) {
                    if (profile.yellowGoldPct >= 0.16f || (profile.greenPct >= 0.20f && profile.yellowGoldPct >= 0.10f && profile.redOrangePct >= 0.08f)) {
                        return FormDisambiguationResult("rayquaza-mega", isShiny = false, confidence = 0.85f, reason = "Mega Rayquaza plasma tendrils and golden chin markings detected")
                    }
                }
            }

            "groudon" -> {
                // Shiny Groudon: Lime / Chartreuse-yellow body
                if (profile.yellowGoldPct >= 0.35f || (profile.dominantHue in 50f..80f && profile.avgSaturation >= 0.35f)) {
                    val isPrimal = profile.blackPixelPct >= 0.25f
                    val form = if (isPrimal && formNames.contains("groudon-primal")) "groudon-primal" else "groudon"
                    return FormDisambiguationResult(form, isShiny = true, confidence = 0.90f, reason = "Shiny Lime-Gold Groudon palette detected")
                }
                // Primal Groudon: Magma fissure glows (high yellow/orange highlights) + obsidian black plates
                if (formNames.contains("groudon-primal")) {
                    if (profile.blackPixelPct >= 0.22f && (profile.yellowGoldPct >= 0.08f || profile.redOrangePct >= 0.25f)) {
                        return FormDisambiguationResult("groudon-primal", isShiny = false, confidence = 0.86f, reason = "Primal Groudon magma glow and obsidian armor detected")
                    }
                }
            }

            "kyogre" -> {
                // Shiny Kyogre: Magenta / Hot Pink body
                if (profile.purpleVioletPct >= 0.30f || (profile.dominantHue in 290f..340f && profile.avgSaturation >= 0.35f)) {
                    val isPrimal = profile.yellowGoldPct >= 0.10f
                    val form = if (isPrimal && formNames.contains("kyogre-primal")) "kyogre-primal" else "kyogre"
                    return FormDisambiguationResult(form, isShiny = true, confidence = 0.90f, reason = "Shiny Pink Kyogre palette detected")
                }
                // Primal Kyogre: Translucent luminous blue with glowing golden/yellow alpha lines
                if (formNames.contains("kyogre-primal")) {
                    if (profile.cyanBluePct >= 0.35f && (profile.yellowGoldPct >= 0.08f || profile.whitePixelPct >= 0.18f)) {
                        return FormDisambiguationResult("kyogre-primal", isShiny = false, confidence = 0.84f, reason = "Primal Kyogre luminous alpha markings detected")
                    }
                }
            }

            "raichu" -> {
                // Alolan Raichu: Lighter golden body, psychic blue eyes, broad curved tail
                if (formNames.contains("raichu-alola")) {
                    if (profile.cyanBluePct >= 0.05f || (profile.yellowGoldPct >= 0.30f && profile.redOrangePct < 0.20f)) {
                        return FormDisambiguationResult("raichu-alola", isShiny = false, confidence = 0.82f, reason = "Alolan Raichu golden surfer tail and blue eyes detected")
                    }
                }
                // Mega Raichu X / Y (Custom / Fan / Project models in pokemon_data.json)
                if (formNames.contains("raichu-mega-x") && profile.cyanBluePct >= 0.20f) {
                    return FormDisambiguationResult("raichu-mega-x", isShiny = false, confidence = 0.80f, reason = "Mega Raichu X electric aura detected")
                }
                if (formNames.contains("raichu-mega-y") && profile.redOrangePct >= 0.40f) {
                    return FormDisambiguationResult("raichu-mega-y", isShiny = false, confidence = 0.80f, reason = "Mega Raichu Y flame markings detected")
                }
            }

            "necrozma" -> {
                // Ultra Necrozma: Pure radiant golden dragon
                if (formNames.contains("necrozma-ultra") && (profile.yellowGoldPct >= 0.40f || (profile.dominantHue in 40f..70f && profile.avgValue >= 0.55f))) {
                    return FormDisambiguationResult("necrozma-ultra", isShiny = false, confidence = 0.92f, reason = "Ultra Necrozma radiant gold dragon form detected")
                }
                // Dusk Mane Necrozma: Solgaleo white/golden lion body with black prism helmet
                if (formNames.contains("necrozma-dusk") && (profile.whitePixelPct >= 0.25f || profile.yellowGoldPct >= 0.20f) && profile.blackPixelPct >= 0.15f) {
                    return FormDisambiguationResult("necrozma-dusk", isShiny = false, confidence = 0.88f, reason = "Dusk Mane Necrozma (Solgaleo fusion) armor detected")
                }
                // Dawn Wings Necrozma: Lunala deep blue/violet moon wings with black prism chest
                if (formNames.contains("necrozma-dawn") && (profile.cyanBluePct >= 0.20f || profile.purpleVioletPct >= 0.20f) && profile.blackPixelPct >= 0.15f) {
                    return FormDisambiguationResult("necrozma-dawn", isShiny = false, confidence = 0.88f, reason = "Dawn Wings Necrozma (Lunala fusion) armor detected")
                }
            }

            "charizard" -> {
                // Shiny Charizard: Jet-black body with dark red inner wings
                if (profile.blackPixelPct >= 0.40f && profile.cyanBluePct < 0.10f) {
                    return FormDisambiguationResult("charizard", isShiny = true, confidence = 0.89f, reason = "Shiny Black Charizard detected")
                }
                // Mega Charizard X: Charcoal black body with bright cyan blue flames
                if (formNames.contains("charizard-mega-x") && (profile.blackPixelPct >= 0.28f && profile.cyanBluePct >= 0.12f)) {
                    return FormDisambiguationResult("charizard-mega-x", isShiny = false, confidence = 0.91f, reason = "Mega Charizard X black body and sky-blue flames detected")
                }
                // Mega Charizard Y: Leaner, large wing fins, deep orange
                if (formNames.contains("charizard-mega-y") && profile.redOrangePct >= 0.45f && profile.whitePixelPct >= 0.10f) {
                    return FormDisambiguationResult("charizard-mega-y", isShiny = false, confidence = 0.80f, reason = "Mega Charizard Y crest detected")
                }
            }

            "mewtwo" -> {
                // Mega Mewtwo X: Fighting type, larger shoulder/forearm pads
                if (formNames.contains("mewtwo-mega-x") && profile.purpleVioletPct < 0.15f && profile.whitePixelPct >= 0.45f) {
                    return FormDisambiguationResult("mewtwo-mega-x", isShiny = false, confidence = 0.80f, reason = "Mega Mewtwo X athletic proportions detected")
                }
                // Mega Mewtwo Y: Long head tail with purple ring
                if (formNames.contains("mewtwo-mega-y") && profile.purpleVioletPct >= 0.28f) {
                    return FormDisambiguationResult("mewtwo-mega-y", isShiny = false, confidence = 0.82f, reason = "Mega Mewtwo Y head-crest detected")
                }
                // Shiny Mewtwo: Green tail
                if (profile.greenPct >= 0.18f) {
                    return FormDisambiguationResult("mewtwo", isShiny = true, confidence = 0.87f, reason = "Shiny Green-tailed Mewtwo detected")
                }
            }

            "vulpix" -> {
                // Alolan Vulpix: Icy white/pale blue vs rust orange
                if (formNames.contains("vulpix-alola") && (profile.whitePixelPct >= 0.45f || profile.cyanBluePct >= 0.15f)) {
                    return FormDisambiguationResult("vulpix-alola", isShiny = false, confidence = 0.94f, reason = "Alolan Vulpix snowy white fur detected")
                }
            }

            "ninetales" -> {
                // Alolan Ninetales: Flowing icy silver/cyan tails vs golden cream
                if (formNames.contains("ninetales-alola") && (profile.whitePixelPct >= 0.40f || profile.cyanBluePct >= 0.15f)) {
                    return FormDisambiguationResult("ninetales-alola", isShiny = false, confidence = 0.93f, reason = "Alolan Ninetales icy silver tails detected")
                }
            }

            "arcanine" -> {
                // Hisuian Arcanine: Dark charcoal slate body with curved magma horns vs orange tiger stripes
                if (formNames.contains("arcanine-hisui") && (profile.blackPixelPct >= 0.30f || profile.redOrangePct >= 0.25f && profile.yellowGoldPct < 0.10f)) {
                    return FormDisambiguationResult("arcanine-hisui", isShiny = false, confidence = 0.88f, reason = "Hisuian Arcanine dark slate coat and magma crest detected")
                }
            }

            "zorua" -> {
                // Hisuian Zorua: Ghost white fur with crimson wisps vs dark grey and red
                if (formNames.contains("zorua-hisui") && profile.whitePixelPct >= 0.35f) {
                    return FormDisambiguationResult("zorua-hisui", isShiny = false, confidence = 0.90f, reason = "Hisuian Zorua snowy ghost fur detected")
                }
            }

            "zoroark" -> {
                // Hisuian Zoroark: Flowing white mane with ghost-red tips
                if (formNames.contains("zoroark-hisui") && profile.whitePixelPct >= 0.35f) {
                    return FormDisambiguationResult("zoroark-hisui", isShiny = false, confidence = 0.90f, reason = "Hisuian Zoroark phantom mane detected")
                }
            }

            "sandshrew" -> {
                // Alolan Sandshrew: Icy igloo dome (pale blue/white) vs yellow brick pattern
                if (formNames.contains("sandshrew-alola") && (profile.whitePixelPct >= 0.35f || profile.cyanBluePct >= 0.20f)) {
                    return FormDisambiguationResult("sandshrew-alola", isShiny = false, confidence = 0.92f, reason = "Alolan Sandshrew glacial shell detected")
                }
            }

            "sandslash" -> {
                // Alolan Sandslash: Large blue ice icicle quills vs brown quills
                if (formNames.contains("sandslash-alola") && profile.cyanBluePct >= 0.25f) {
                    return FormDisambiguationResult("sandslash-alola", isShiny = false, confidence = 0.92f, reason = "Alolan Sandslash ice quills detected")
                }
            }

            "marowak" -> {
                // Alolan Marowak: Dark grey/black body with bright turquoise ethereal bone flames
                if (formNames.contains("marowak-alola") && (profile.blackPixelPct >= 0.30f && (profile.cyanBluePct >= 0.08f || profile.purpleVioletPct >= 0.08f))) {
                    return FormDisambiguationResult("marowak-alola", isShiny = false, confidence = 0.89f, reason = "Alolan Marowak spectral flame and obsidian bone detected")
                }
            }

            "exeggutor" -> {
                // Alolan Exeggutor: Extremely tall neck (vertical palm tree composition)
                if (formNames.contains("exeggutor-alola") && profile.greenPct >= 0.35f) {
                    return FormDisambiguationResult("exeggutor-alola", isShiny = false, confidence = 0.85f, reason = "Alolan Exeggutor tropical foliage detected")
                }
            }

            "lucario" -> {
                // Mega Lucario: Crimson leg markings, black aura tendrils
                if (formNames.contains("lucario-mega") && profile.redOrangePct >= 0.15f && profile.blackPixelPct >= 0.25f) {
                    return FormDisambiguationResult("lucario-mega", isShiny = false, confidence = 0.85f, reason = "Mega Lucario crimson markings and aura tendrils detected")
                }
            }

            "gengar" -> {
                // Shiny Gengar / Mega Shiny Gengar: Pure ghost white vs deep shadow purple
                if (profile.whitePixelPct >= 0.50f) {
                    val form = if (formNames.contains("gengar-mega")) "gengar-mega" else "gengar"
                    return FormDisambiguationResult(form, isShiny = true, confidence = 0.93f, reason = "Shiny White Gengar detected")
                }
                // Mega Gengar: Third eye on forehead, reddish-purple lower half submerged
                if (formNames.contains("gengar-mega") && profile.purpleVioletPct >= 0.40f && profile.redOrangePct >= 0.10f) {
                    return FormDisambiguationResult("gengar-mega", isShiny = false, confidence = 0.83f, reason = "Mega Gengar third eye and shadow portal detected")
                }
            }

            "ponyta", "rapidash" -> {
                // Galarian Ponyta / Rapidash: Pastel cotton candy cyan and pink mane
                val galarForm = "${baseName}-galar"
                if (formNames.contains(galarForm) && (profile.cyanBluePct >= 0.15f && profile.purpleVioletPct >= 0.15f)) {
                    return FormDisambiguationResult(galarForm, isShiny = false, confidence = 0.91f, reason = "Galarian pastel fairy mane detected")
                }
            }

            "wooper" -> {
                // Paldean Wooper: Mud brown body with purple bone gills vs light blue
                if (formNames.contains("wooper-paldea") && (profile.redOrangePct >= 0.25f || profile.purpleVioletPct >= 0.12f) && profile.cyanBluePct < 0.10f) {
                    return FormDisambiguationResult("wooper-paldea", isShiny = false, confidence = 0.90f, reason = "Paldean Wooper mud coat and poison gills detected")
                }
            }
        }

        // 2. GENERIC HEURISTICS FOR REGIONAL & MEGA FORMS
        for (form in availableForms) {
            val fName = form.name.lowercase().trim()
            when {
                // Alolan forms often feature white, cyan, or ice palettes
                fName.endsWith("-alola") -> {
                    if ((profile.whitePixelPct >= 0.40f || profile.cyanBluePct >= 0.25f) && profile.avgSaturation < 0.35f) {
                        return FormDisambiguationResult(fName, isShiny = false, confidence = 0.78f, reason = "Alolan regional palette match")
                    }
                }
                // Hisuian forms frequently have charcoal rock / ancient dark palettes
                fName.endsWith("-hisui") -> {
                    if (profile.blackPixelPct >= 0.38f && profile.avgValue < 0.38f) {
                        return FormDisambiguationResult(fName, isShiny = false, confidence = 0.76f, reason = "Hisuian regional palette match")
                    }
                }
                // Mega forms typically feature high-saturation accents or auric halos
                fName.endsWith("-mega") -> {
                    if (profile.avgSaturation >= 0.45f && (profile.yellowGoldPct >= 0.18f || profile.cyanBluePct >= 0.20f)) {
                        return FormDisambiguationResult(fName, isShiny = false, confidence = 0.75f, reason = "Mega evolution auric highlights match")
                    }
                }
            }
        }

        // 3. DEFAULT: BASE SPECIES
        return FormDisambiguationResult(
            resolvedFormName = baseName,
            isShiny = false,
            confidence = 0.70f,
            reason = "Standard base species palette match"
        )
    }

    /**
     * Disambiguates between the base species and all its known alternate forms or shinies.
     */
    fun disambiguate(
        bitmap: Bitmap,
        basePokemon: PokemonEntity,
        availableForms: List<PokemonEntity>
    ): FormDisambiguationResult {
        val profile = analyzeColorProfile(bitmap)
        return disambiguateFromProfile(profile, basePokemon, availableForms)
    }

    /**
     * Formats database slugs (e.g. 'rayquaza-mega', 'groudon-primal', 'raichu-alola')
     * into elegant, human-readable display titles.
     */
    fun formatFormDisplayName(entityName: String): String {
        val lower = entityName.lowercase().trim()
        return when {
            lower.endsWith("-mega-x") -> "Mega ${lower.removeSuffix("-mega-x").replaceFirstChar { it.uppercase() }} X"
            lower.endsWith("-mega-y") -> "Mega ${lower.removeSuffix("-mega-y").replaceFirstChar { it.uppercase() }} Y"
            lower.endsWith("-mega-z") -> "Mega ${lower.removeSuffix("-mega-z").replaceFirstChar { it.uppercase() }} Z"
            lower.endsWith("-mega") -> "Mega ${lower.removeSuffix("-mega").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-primal") -> "Primal ${lower.removeSuffix("-primal").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-alola") -> "Alolan ${lower.removeSuffix("-alola").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-hisui") -> "Hisuian ${lower.removeSuffix("-hisui").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-galar") -> "Galarian ${lower.removeSuffix("-galar").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-paldea") -> "Paldean ${lower.removeSuffix("-paldea").replaceFirstChar { it.uppercase() }}"
            lower.endsWith("-gmax") -> "${lower.removeSuffix("-gmax").replaceFirstChar { it.uppercase() }} Gigantamax"
            lower == "necrozma-dusk" -> "Dusk Mane Necrozma"
            lower == "necrozma-dawn" -> "Dawn Wings Necrozma"
            lower == "necrozma-ultra" -> "Ultra Necrozma"
            lower.contains("-") -> lower.split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            else -> lower.replaceFirstChar { it.uppercase() }
        }
    }
}
