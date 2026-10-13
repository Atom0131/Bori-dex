package com.example.pokedex.tts

enum class ScanFlowPreset(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultFanfare: Boolean,
    val defaultVoiceReveal: Boolean,
    val defaultCryOnReveal: Boolean,
    val defaultAutoCryEntry: Boolean
) {
    NOSTALGIC(
        id = "nostalgic",
        displayName = "Nostalgic (Anime TV Show)",
        description = "Authentic fanfare & silhouette intermission, Dexter 'It's Pikachu!', cry in Dex entry",
        defaultFanfare = true,
        defaultVoiceReveal = true,
        defaultCryOnReveal = false,
        defaultAutoCryEntry = false
    ),
    FAST(
        id = "fast",
        displayName = "Speed Scanner (Fast)",
        description = "Skips fanfare intermission, jumps straight to entry and reads info immediately",
        defaultFanfare = false,
        defaultVoiceReveal = true,
        defaultCryOnReveal = false,
        defaultAutoCryEntry = false
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom Scan Flow",
        description = "Independently customize fanfare, voice callout, and cry behavior",
        defaultFanfare = true,
        defaultVoiceReveal = true,
        defaultCryOnReveal = false,
        defaultAutoCryEntry = false
    );

    companion object {
        fun fromId(id: String): ScanFlowPreset = entries.firstOrNull { it.id == id } ?: NOSTALGIC
    }
}
