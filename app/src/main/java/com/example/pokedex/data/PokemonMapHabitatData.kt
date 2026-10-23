package com.example.pokedex.data

import androidx.compose.ui.graphics.Color

/**
 * Normalized 2D coordinates on a Town Map image.
 * [xNorm] and [yNorm] are between 0.0f (top-left) and 1.0f (bottom-right).
 */
data class MapCoordinate(
    val xNorm: Float,
    val yNorm: Float
)

enum class MapPinType(val emoji: String, val badgeColor: Color, val label: String) {
    STARTER_GIFT("🎁", Color(0xFFFFD700), "STARTER GIFT"),
    SPECIAL_GIFT("🤝", Color(0xFF00FFCC), "GIFT / EVENT"),
    WILD_ROUTE("📍", Color(0xFFFF3366), "WILD ROUTE"),
    CAVE_DUNGEON("⛰️", Color(0xFFFFA726), "CAVE / DUNGEON"),
    WATER_SURF("🌊", Color(0xFF29B6F6), "WATER / SURF"),
    FISHING_SPOT("🎣", Color(0xFF00E5FF), "FISHING"),
    LEGENDARY_STATIC("⚡", Color(0xFFFFEB3B), "LEGENDARY / STATIC"),
    RAID_DEN("💎", Color(0xFFE040FB), "RAID / DEN"),
    POKEMON_GO_BIOME("📱", Color(0xFF0075BE), "GO SPAWN")
}

data class MapLocationPin(
    val id: String,
    val areaName: String,
    val coordinate: MapCoordinate,
    val type: MapPinType,
    val encounterMethod: String,
    val rateOrLevel: String,
    val notes: String? = null
)

data class PokemonHabitatMap(
    val pokemonId: Int,
    val pokemonName: String,
    val editionId: String,
    val editionDisplayName: String,
    val regionName: String,
    val mapAsset: String,
    val isAreaUnknown: Boolean,
    val unknownReason: String? = null,
    val pins: List<MapLocationPin> = emptyList(),
    val narrationText: String
)
