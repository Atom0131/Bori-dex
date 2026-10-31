package com.example.pokedex.tts

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.regex.Pattern

object PokemonPronunciation {

    private val pronunciationMap = mutableMapOf<String, String>()
    private val multiWordPhrases = mutableListOf<Pair<Pattern, String>>()
    private var isLoaded = false

    // Precompiled regex for rapid word boundary matching
    private val wordPattern = Pattern.compile("(?i)\\b[a-z0-9'-]+\\b")

    /**
     * Initializes the comprehensive 1,025+ pronunciation dictionary from assets
     */
    fun init(context: Context) {
        if (isLoaded) return
        try {
            val inputStream = context.assets.open("pokemon_pronunciations.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val jsonString = reader.readText()
            reader.close()

            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            val multiWordTemp = mutableListOf<Pair<String, String>>()

            while (keys.hasNext()) {
                val key = keys.next().lowercase().trim()
                val value = jsonObject.getString(key).trim()
                pronunciationMap[key] = value

                if (key.contains(" ")) {
                    multiWordTemp.add(Pair(key, value))
                }
            }

            // Sort multi-word phrases by length descending to match longest phrases first
            multiWordPhrases.clear()
            multiWordTemp.sortByDescending { it.first.length }
            for ((phrase, phonetic) in multiWordTemp) {
                val startBoundary = if (phrase.first().isLetterOrDigit()) "\\b" else ""
                val endBoundary = if (phrase.last().isLetterOrDigit()) "\\b" else ""
                val pattern = Pattern.compile("(?i)" + startBoundary + Pattern.quote(phrase) + endBoundary)
                multiWordPhrases.add(Pair(pattern, phonetic))
            }

            isLoaded = true
            android.util.Log.d("PokemonPronunciation", "Loaded ${pronunciationMap.size} Pokemon pronunciations (${multiWordPhrases.size} multi-word phrases) successfully.")
        } catch (e: Exception) {
            android.util.Log.e("PokemonPronunciation", "Error loading pokemon_pronunciations.json", e)
        }
    }

    /**
     * Test initialization method for JVM unit tests
     */
    fun initForTesting(testMap: Map<String, String>) {
        pronunciationMap.clear()
        multiWordPhrases.clear()

        val multiWordTemp = mutableListOf<Pair<String, String>>()
        for ((k, v) in testMap) {
            val key = k.lowercase().trim()
            val value = v.trim()
            pronunciationMap[key] = value
            if (key.contains(" ")) {
                multiWordTemp.add(Pair(key, value))
            }
        }

        multiWordTemp.sortByDescending { it.first.length }
        for ((phrase, phonetic) in multiWordTemp) {
            val startBoundary = if (phrase.first().isLetterOrDigit()) "\\b" else ""
            val endBoundary = if (phrase.last().isLetterOrDigit()) "\\b" else ""
            val pattern = Pattern.compile("(?i)" + startBoundary + Pattern.quote(phrase) + endBoundary)
            multiWordPhrases.add(Pair(pattern, phonetic))
        }
        isLoaded = true
    }

    /**
     * Returns the phonetic respelling of a Pokemon's name, or null if unknown
     */
    fun getPhonetic(name: String): String? {
        val key = name.lowercase().trim()
        return pronunciationMap[key] ?: pronunciationMap[key.replace(" ", "-")] ?: pronunciationMap[key.split("-")[0]]
    }

    private fun lookupWordOrPossessive(word: String): String? {
        val lower = word.lowercase()
        val isPossessive = lower.endsWith("'s")
        val isTrailingApos = lower.endsWith("s'")
        val base = when {
            isPossessive -> lower.substring(0, lower.length - 2)
            isTrailingApos -> lower.substring(0, lower.length - 1)
            else -> lower
        }

        val direct = pronunciationMap[lower]
            ?: pronunciationMap[lower.replace(" ", "-")]
            ?: pronunciationMap[lower.replace("'", "")]
            ?: pronunciationMap[base]?.let { if (isPossessive) "$it's" else if (isTrailingApos) "$it'" else it }
            ?: pronunciationMap[base.replace(" ", "-")]?.let { if (isPossessive) "$it's" else if (isTrailingApos) "$it'" else it }

        if (direct != null) {
            if (direct.equals(word, ignoreCase = true)) {
                return word
            }
            return direct
        }

        // If the token is hyphenated (e.g. "Hisui-region", "Grass-type") and not a single dictionary key,
        // resolve each hyphen-separated component individually and join smoothly.
        if (word.contains("-")) {
            val parts = word.split("-")
            var anyChanged = false
            val resolvedParts = parts.map { part ->
                if (part.isEmpty()) {
                    part
                } else {
                    val subPhonetic = lookupSingleToken(part)
                    if (subPhonetic != null) {
                        anyChanged = true
                        subPhonetic
                    } else {
                        part
                    }
                }
            }
            if (anyChanged) {
                // If the word was a canonical compound like Ho-Oh, keep hyphen; otherwise use space for fluid speech
                return if (word.equals("Ho-Oh", ignoreCase = true)) "Ho-Oh" else resolvedParts.joinToString(" ")
            }
        }

        return null
    }

    private fun lookupSingleToken(token: String): String? {
        val lower = token.lowercase()
        val isPossessive = lower.endsWith("'s")
        val isTrailingApos = lower.endsWith("s'")
        val base = when {
            isPossessive -> lower.substring(0, lower.length - 2)
            isTrailingApos -> lower.substring(0, lower.length - 1)
            else -> lower
        }
        val direct = pronunciationMap[lower]
            ?: pronunciationMap[lower.replace("'", "")]
            ?: pronunciationMap[base]?.let { if (isPossessive) "$it's" else if (isTrailingApos) "$it'" else it }

        if (direct != null && direct.equals(token, ignoreCase = true)) {
            return token
        }
        return direct
    }

    /**
     * Formats raw text so that the offline speech engine pronounces Pokemon names,
     * punctuation, and flavor text naturally and accurately.
     */
    fun formatForSpeech(rawText: String): String {
        var text = rawText

        // 1. Clean legacy line wraps and formatting inherited from Game Boy entries
        text = text.replace("-\n", "").replace("-\r\n", "")
        text = text.replace("\n", " ").replace("\r", " ")

        // 2. Normalize smart quotes, gender symbols, and non-ASCII character artifacts
        text = text.replace("♀", " Female")
        text = text.replace("♂", " Male")
        text = text.replace('\u2019', '\'') // right single quotation mark
        text = text.replace('\u2018', '\'') // left single quotation mark
        text = text.replace('\u201C', '"')  // left double quotation mark
        text = text.replace('\u201D', '"')  // right double quotation mark
        text = text.replace('\u00E9', 'e')  // é
        text = text.replace('\u00C9', 'E')  // É
        text = text.replace('\u00AD', ' ')  // soft hyphen

        // 3. Clean Pokédex terms
        text = text.replace("(?i)\\bPOKéMON\\b".toRegex(), "Pokemon")
        text = text.replace("(?i)\\bPOKEMON\\b".toRegex(), "Pokemon")

        // 3.5. Multi-word phrase replacement protected by sentinel tokens so Step 4 never re-mutates them
        val multiWordPlaceholders = mutableListOf<String>()
        if (multiWordPhrases.isNotEmpty()) {
            for ((pattern, phonetic) in multiWordPhrases) {
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    val sb = StringBuffer()
                    matcher.reset()
                    while (matcher.find()) {
                        val idx = multiWordPlaceholders.size
                        multiWordPlaceholders.add(phonetic)
                        matcher.appendReplacement(sb, "\u0001$idx\u0002")
                    }
                    matcher.appendTail(sb)
                    text = sb.toString()
                }
            }
        }

        // 4. Single-token & hyphenated-token phonetic replacement (with possessive resolution)
        if (pronunciationMap.isNotEmpty()) {
            val matcher = wordPattern.matcher(text)
            val sb = StringBuffer()
            while (matcher.find()) {
                val word = matcher.group()
                val phonetic = lookupWordOrPossessive(word)
                if (phonetic != null) {
                    matcher.appendReplacement(sb, MatcherQuote(phonetic))
                } else {
                    matcher.appendReplacement(sb, MatcherQuote(word))
                }
            }
            matcher.appendTail(sb)
            text = sb.toString()
        }

        // 4.5. Restore protected multi-word replacements
        for (i in multiWordPlaceholders.indices) {
            text = text.replace("\u0001$i\u0002", multiWordPlaceholders[i])
        }

        // 5. Ensure natural pauses around punctuation and eliminate excess spaces
        text = text.replace("\\s+".toRegex(), " ").trim()

        return text
    }

    private fun MatcherQuote(s: String): String {
        return java.util.regex.Matcher.quoteReplacement(s)
    }
}
