package com.example.pokedex.data

enum class CostumeCategory(val label: String, val badgeColor: Long) {
    SQUIRTLE_SQUAD("SQUIRTLE SQUAD", 0xFF00BFFF),
    HALLOWEEN("HALLOWEEN", 0xFFFF7518),
    HOLIDAY("HOLIDAY / XMAS", 0xFFE53935),
    NEW_YEAR("NEW YEAR'S", 0xFFFFD700),
    SPECIAL_EVENT("SPECIAL EVENT", 0xFF9C27B0)
}

data class PokemonCostume(
    val id: String,
    val pokemonId: Int,
    val name: String,
    val category: CostumeCategory,
    val eventTag: String,
    val description: String,
    val assetPath: String,
    val shinyAssetPath: String? = null
) {
    val fullAssetUrl: String get() = "file:///android_asset/costumes/$assetPath"
    val fullShinyAssetUrl: String? get() = shinyAssetPath?.let { "file:///android_asset/costumes/$it" }
}
