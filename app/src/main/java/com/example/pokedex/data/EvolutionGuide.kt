package com.example.pokedex.data

import android.content.Context
import org.json.JSONObject

data class EvolutionTarget(
    val targetId: Int,
    val targetName: String,
    val defaultMethod: String,
    val defaultItem: String?,
    val versionNotes: Map<String, EvolutionVersionOverride>
)

data class EvolutionVersionOverride(
    val method: String,
    val item: String?,
    val note: String?
)

object EvolutionGuideRepository {
    private var guideMap: Map<Int, List<EvolutionTarget>>? = null

    fun getEvolutionsForPokemon(context: Context, pokemonId: Int): List<EvolutionTarget> {
        if (guideMap == null) {
            loadGuide(context)
        }
        return guideMap?.get(pokemonId) ?: emptyList()
    }

    @Synchronized
    private fun loadGuide(context: Context) {
        if (guideMap != null) return
        try {
            val jsonString = context.assets.open("evolution_guide.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val map = mutableMapOf<Int, List<EvolutionTarget>>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val id = key.toIntOrNull() ?: continue
                val pObj = root.getJSONObject(key)
                val evosArray = pObj.getJSONArray("evolutions")
                val evoList = mutableListOf<EvolutionTarget>()
                for (i in 0 until evosArray.length()) {
                    val evoObj = evosArray.getJSONObject(i)
                    val targetId = evoObj.getInt("targetId")
                    val targetName = evoObj.getString("targetName")
                    val defaultMethod = evoObj.getString("defaultMethod")
                    val defaultItem = if (evoObj.has("defaultItem") && !evoObj.isNull("defaultItem")) evoObj.getString("defaultItem") else null
                    
                    val versionNotes = mutableMapOf<String, EvolutionVersionOverride>()
                    if (evoObj.has("versionNotes")) {
                        val vnObj = evoObj.getJSONObject("versionNotes")
                        val vKeys = vnObj.keys()
                        while (vKeys.hasNext()) {
                            val vKey = vKeys.next()
                            val vItemObj = vnObj.getJSONObject(vKey)
                            val method = vItemObj.optString("method", defaultMethod)
                            val item = if (vItemObj.has("item") && !vItemObj.isNull("item")) vItemObj.getString("item") else null
                            val note = if (vItemObj.has("note") && !vItemObj.isNull("note")) vItemObj.getString("note") else null
                            versionNotes[vKey] = EvolutionVersionOverride(method, item, note)
                        }
                    }
                    evoList.add(EvolutionTarget(targetId, targetName, defaultMethod, defaultItem, versionNotes))
                }
                map[id] = evoList
            }
            guideMap = map
        } catch (e: Exception) {
            android.util.Log.e("EvolutionGuide", "Failed to load evolution_guide.json", e)
            guideMap = emptyMap()
        }
    }
}
