package com.example.pokedex.tts

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.regex.Pattern

object PokemonPronunciation {

    private val pronunciationMap = mutableMapOf<String, String>()
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
            while (keys.hasNext()) {
                val key = keys.next().lowercase().trim()
                val value = jsonObject.getString(key).trim()
                pronunciationMap[key] = value
            }
            isLoaded = true
            android.util.Log.d("PokemonPronunciation", "Loaded ${pronunciationMap.size} Pokemon pronunciations successfully.")
        } catch (e: Exception) {
            android.util.Log.e("PokemonPronunciation", "Error loading pokemon_pronunciations.json", e)
        }
    }

    /**
     * Returns the phonetic respelling of a Pokemon's name, or null if unknown
     */
    fun getPhonetic(name: String): String? {
        val key = name.lowercase().trim()
        return pronunciationMap[key] ?: pronunciationMap[key.replace(" ", "-")] ?: pronunciationMap[key.split("-")[0]]
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

        // 2. Normalize smart quotes, hyphens, and non-ASCII character artifacts
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

        // 4. Fast phonetic replacement using matcher buffer (with possessive resolution)
        if (pronunciationMap.isNotEmpty()) {
            val matcher = wordPattern.matcher(text)
            val sb = StringBuffer()
            while (matcher.find()) {
                val word = matcher.group()
                val lower = word.lowercase()
                val isPossessive = lower.endsWith("'s")
                val isTrailingApos = lower.endsWith("s'")
                val base = when {
                    isPossessive -> lower.substring(0, lower.length - 2)
                    isTrailingApos -> lower.substring(0, lower.length - 1)
                    else -> lower
                }

                val phonetic = pronunciationMap[lower]
                    ?: pronunciationMap[lower.replace(" ", "-")]
                    ?: pronunciationMap[lower.replace("'", "")]
                    ?: pronunciationMap[base]?.let { if (isPossessive) "$it's" else "$it'" }
                    ?: pronunciationMap[base.replace(" ", "-")]?.let { if (isPossessive) "$it's" else "$it'" }

                if (phonetic != null) {
                    matcher.appendReplacement(sb, MatcherQuote(phonetic))
                } else {
                    matcher.appendReplacement(sb, MatcherQuote(word))
                }
            }
            matcher.appendTail(sb)
            text = sb.toString()
        }

        // 5. Ensure natural pauses around punctuation and eliminate excess spaces
        text = text.replace("\\s+".toRegex(), " ").trim()

        return text
    }

    private fun MatcherQuote(s: String): String {
        return java.util.regex.Matcher.quoteReplacement(s)
    }
}
