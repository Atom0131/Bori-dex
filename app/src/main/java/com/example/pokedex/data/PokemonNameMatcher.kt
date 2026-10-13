package com.example.pokedex.data

class PokemonNameMatcher {

    fun extractName(rawText: String): String? {
        val cleaned = rawText.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
            
        val manualOverrides = mapOf(
            "mr mime" to "mr-mime",
            "mime jr" to "mime-jr",
            "nidoran f" to "nidoran-f",
            "nidoran female" to "nidoran-f",
            "nidoran m" to "nidoran-m",
            "nidoran male" to "nidoran-m",
            "ho oh" to "ho-oh",
            "porygon z" to "porygon-z",
            "type null" to "type-null",
            "jangmo o" to "jangmo-o",
            "hakamo o" to "hakamo-o",
            "kommo o" to "kommo-o",
            "tapu koko" to "tapu-koko",
            "tapu lele" to "tapu-lele",
            "tapu bulu" to "tapu-bulu",
            "tapu fini" to "tapu-fini",
            "farfetchd" to "farfetchd",
            "sirfetchd" to "sirfetchd"
        )
        
        for ((key, value) in manualOverrides) {
            if (cleaned.contains(key)) {
                return value
            }
        }
        
        val words = cleaned.split("\\s+".toRegex())
        
        val stopWords = setOf(
            "i", "think", "this", "is", "a", "an", "photo", "of", "pokemon", "the", "it", 
            "looks", "like", "figure", "statue", "toy", "artwork", "its", "which", "answer", 
            "with", "only", "english", "name", "that", "there", "probably", "possibly", 
            "maybe", "definitely", "surely", "could", "be", "based", "on", "yes", "no",
            "can", "see", "picture", "shows", "image", "depicts", "features", "im"
        )
        
        val possibleNames = words.filter { it.isNotBlank() && it !in stopWords }
        
        if (possibleNames.isNotEmpty()) {
            // Join with hyphens in case there are multiple name parts (though overrides should catch most)
            return possibleNames.joinToString("-")
        }
        
        return null
    }
}
