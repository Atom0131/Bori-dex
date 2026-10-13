package com.example.pokedex.tts

enum class VoiceProfile(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultPitch: Float,
    val defaultDsp: Boolean,
    val defaultSpeed: Float = 1.0f
) {
    ORIGINAL_90S_MALE(
        id = "original_90s_male",
        displayName = "Original 90s Dexter (Male)",
        description = "Authentic 1997 anime Pokédex voice with metallic echo & bandpass DSP",
        defaultPitch = 1.15f,
        defaultDsp = true,
        defaultSpeed = 1.08f
    ),
    HIGH_END_MALE(
        id = "high_end_male",
        displayName = "Modern High-End (Male)",
        description = "Crystal-clear HD modern male voice without retro distortion",
        defaultPitch = 1.00f,
        defaultDsp = false,
        defaultSpeed = 1.0f
    ),
    HIGH_END_FEMALE(
        id = "high_end_female",
        displayName = "Modern High-End (Female)",
        description = "Crystal-clear HD modern female voice",
        defaultPitch = 1.00f,
        defaultDsp = false,
        defaultSpeed = 1.0f
    ),
    RETRO_FEMALE_90S(
        id = "retro_female_90s",
        displayName = "Retro Anime (Female)",
        description = "Female voice with 1990s metallic echo & bandpass filter",
        defaultPitch = 1.05f,
        defaultDsp = true,
        defaultSpeed = 1.05f
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom System Voice",
        description = "Select any installed device voice from a dropdown",
        defaultPitch = 1.00f,
        defaultDsp = false,
        defaultSpeed = 1.0f
    );

    companion object {
        fun fromId(id: String): VoiceProfile = entries.firstOrNull { it.id == id } ?: ORIGINAL_90S_MALE
    }
}
