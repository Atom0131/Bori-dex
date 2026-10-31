package com.example.pokedex.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.PokedexDatabase
import com.example.pokedex.data.PokemonEntity
import com.example.pokedex.data.getCanonicalId
import com.example.pokedex.tts.OfflineTTS
import com.example.pokedex.tts.VoiceProfile
import com.example.pokedex.tts.VoiceItem
import com.example.pokedex.tts.ScanFlowPreset
import com.example.pokedex.tts.PokemonSoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.example.pokedex.camera.PokemonFormDetector
import com.example.pokedex.camera.TFLiteClassifier
import com.example.pokedex.data.PokemonNameMatcher
import com.example.pokedex.data.PokemonRegion
import com.example.pokedex.data.RegionMapEntry
import com.example.pokedex.data.PokemonGameGuideRepository
import com.example.pokedex.data.GuideQueryResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

sealed class PokedexState {
    object Idle : PokedexState()
    object LoadingModel : PokedexState()
    object Scanning : PokedexState()
    data class WhosThat(val capturedImage: Bitmap) : PokedexState()
    data class Reveal(val pokemon: PokemonEntity, val capturedImage: Bitmap, val isShiny: Boolean = false) : PokedexState()
    data class Result(val pokemon: PokemonEntity, val isShiny: Boolean = false) : PokedexState()
    data class Error(val message: String) : PokedexState()
}

class PokedexViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle? = null
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("pokedex_prefs", Context.MODE_PRIVATE)
    private val pokemonDao = PokedexDatabase.getDatabase(application).pokemonDao()
    private val tts = OfflineTTS(application)
    private val classifier = TFLiteClassifier(application)
    private val nameMatcher = PokemonNameMatcher()

    private val historyStack = mutableListOf<PokemonEntity>()
    
    private val _uiState = MutableStateFlow<PokedexState>(PokedexState.Idle)
    val uiState: StateFlow<PokedexState> = _uiState.asStateFlow()

    private val _pokemonList = MutableStateFlow<List<PokemonEntity>>(emptyList())
    val pokemonList: StateFlow<List<PokemonEntity>> = _pokemonList.asStateFlow()

    private val _voiceProfile = MutableStateFlow(
        VoiceProfile.fromId(prefs.getString("voice_profile", VoiceProfile.ORIGINAL_90S_MALE.id) ?: VoiceProfile.ORIGINAL_90S_MALE.id)
    )
    val voiceProfile: StateFlow<VoiceProfile> = _voiceProfile.asStateFlow()

    private val _customVoiceName = MutableStateFlow(prefs.getString("custom_voice_name", null))
    val customVoiceName: StateFlow<String?> = _customVoiceName.asStateFlow()

    private val _dspEnabled = MutableStateFlow(prefs.getBoolean("dsp_enabled", true))
    val dspEnabled: StateFlow<Boolean> = _dspEnabled.asStateFlow()

    private val savedPitch = prefs.getFloat("voice_pitch", -1f)
    private val initialPitch = if (savedPitch <= 0f || savedPitch == 0.90f || savedPitch == 1.18f) _voiceProfile.value.defaultPitch else savedPitch
    private val _voicePitch = MutableStateFlow(initialPitch)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val savedSpeed = prefs.getFloat("voice_speed", -1f)
    private val initialSpeed = if (savedSpeed <= 0f || savedSpeed == 1.0f) _voiceProfile.value.defaultSpeed else savedSpeed
    private val _voiceSpeed = MutableStateFlow(initialSpeed)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceItem>>(emptyList())
    val availableVoices: StateFlow<List<VoiceItem>> = _availableVoices.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _scanStatusText = MutableStateFlow("SCANNING...")
    val scanStatusText: StateFlow<String> = _scanStatusText.asStateFlow()

    private val _scanStreak = MutableStateFlow(0)
    val scanStreak: StateFlow<Int> = _scanStreak.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scannerTelemetry = MutableStateFlow("")
    val scannerTelemetry: StateFlow<String> = _scannerTelemetry.asStateFlow()

    private val _candidatePokemon = MutableStateFlow<PokemonEntity?>(null)
    val candidatePokemon: StateFlow<PokemonEntity?> = _candidatePokemon.asStateFlow()

    private val _candidateForms = MutableStateFlow<List<PokemonEntity>>(emptyList())
    val candidateForms: StateFlow<List<PokemonEntity>> = _candidateForms.asStateFlow()

    private val _isCandidateShiny = MutableStateFlow(false)
    val isCandidateShiny: StateFlow<Boolean> = _isCandidateShiny.asStateFlow()

    private val _detectedFormReason = MutableStateFlow<String?>(null)
    val detectedFormReason: StateFlow<String?> = _detectedFormReason.asStateFlow()

    fun selectCandidateForm(formEntity: PokemonEntity) {
        _candidatePokemon.value = formEntity
        _scanStatusText.value = "ACQUIRING: ${PokemonFormDetector.formatFormDisplayName(formEntity.name).uppercase()}..."
    }

    fun toggleCandidateShiny() {
        _isCandidateShiny.value = !_isCandidateShiny.value
    }

    private val _selectedRegion = MutableStateFlow<PokemonRegion?>(null)
    val selectedRegion: StateFlow<PokemonRegion?> = _selectedRegion.asStateFlow()

    private val _selectedMapEntryId = MutableStateFlow<String>("all")
    val selectedMapEntryId: StateFlow<String> = _selectedMapEntryId.asStateFlow()

    fun selectRegion(region: PokemonRegion?) {
        _selectedRegion.value = region
        if (region == null) {
            _selectedMapEntryId.value = "all"
            setPreferredGameEdition("Default", syncRegionFilter = false)
        } else {
            val matchingEntry = com.example.pokedex.data.ALL_REGION_MAP_ENTRIES.firstOrNull { it.region == region }
            if (matchingEntry != null) {
                _selectedMapEntryId.value = matchingEntry.id
            }
            val defaultEdition = com.example.pokedex.data.PokemonMapHabitatRepository.getDefaultCartridgeIdForRegion(region.displayName)
            setPreferredGameEdition(defaultEdition, syncRegionFilter = false)
        }
        prefs.edit()
            .putString("last_selected_region", region?.name)
            .putString("last_selected_map_entry_id", _selectedMapEntryId.value)
            .apply()
        savedStateHandle?.set("last_selected_region", region?.name)
        savedStateHandle?.set("last_selected_map_entry_id", _selectedMapEntryId.value)
    }

    fun selectMapEntry(entry: RegionMapEntry) {
        _selectedMapEntryId.value = entry.id
        _selectedRegion.value = entry.region
        if (!entry.preferredEditionId.isNullOrBlank()) {
            setPreferredGameEdition(entry.preferredEditionId, syncRegionFilter = false)
        } else {
            setPreferredGameEdition("Default", syncRegionFilter = false)
        }
        prefs.edit()
            .putString("last_selected_region", entry.region?.name)
            .putString("last_selected_map_entry_id", entry.id)
            .apply()
        savedStateHandle?.set("last_selected_region", entry.region?.name)
        savedStateHandle?.set("last_selected_map_entry_id", entry.id)
    }

    fun saveListScrollPosition(index: Int, offset: Int) {
        prefs.edit()
            .putInt("last_list_index", index)
            .putInt("last_list_offset", offset)
            .apply()
    }

    fun getSavedListIndex(): Int = prefs.getInt("last_list_index", 0)
    fun getSavedListOffset(): Int = prefs.getInt("last_list_offset", 0)

    private val _scanFlowPreset = MutableStateFlow(
        ScanFlowPreset.fromId(prefs.getString("scan_flow_preset", ScanFlowPreset.NOSTALGIC.id) ?: ScanFlowPreset.NOSTALGIC.id)
    )
    val scanFlowPreset: StateFlow<ScanFlowPreset> = _scanFlowPreset.asStateFlow()

    private val _playFanfare = MutableStateFlow(prefs.getBoolean("scan_play_fanfare", true))
    val playFanfare: StateFlow<Boolean> = _playFanfare.asStateFlow()

    private val _playVoiceReveal = MutableStateFlow(prefs.getBoolean("scan_voice_reveal", true))
    val playVoiceReveal: StateFlow<Boolean> = _playVoiceReveal.asStateFlow()

    private val _playCryOnReveal = MutableStateFlow(prefs.getBoolean("scan_cry_on_reveal", false))
    val playCryOnReveal: StateFlow<Boolean> = _playCryOnReveal.asStateFlow()

    private val _autoPlayCryEntry = MutableStateFlow(prefs.getBoolean("scan_auto_cry_entry", false))
    val autoPlayCryEntry: StateFlow<Boolean> = _autoPlayCryEntry.asStateFlow()

    private val _autoReadDexEntry = MutableStateFlow(prefs.getBoolean("auto_read_dex_entry", true))
    val autoReadDexEntry: StateFlow<Boolean> = _autoReadDexEntry.asStateFlow()

    // --- ULTIMATE POKÉMON GAME GUIDE & QUEST NAVIGATOR ---
    private val _isGuideModalOpen = MutableStateFlow(false)
    val isGuideModalOpen: StateFlow<Boolean> = _isGuideModalOpen.asStateFlow()

    private val _guideQuery = MutableStateFlow("")
    val guideQuery: StateFlow<String> = _guideQuery.asStateFlow()

    private val _guideResult = MutableStateFlow<GuideQueryResult?>(null)
    val guideResult: StateFlow<GuideQueryResult?> = _guideResult.asStateFlow()

    private val _isGuideSearching = MutableStateFlow(false)
    val isGuideSearching: StateFlow<Boolean> = _isGuideSearching.asStateFlow()

    fun getEffectiveGameEdition(): String {
        val pref = _preferredGameEdition.value
        if (pref.isNotBlank() && !pref.equals("Default", ignoreCase = true)) {
            return pref
        }
        val mapId = _selectedMapEntryId.value
        if (!mapId.isNullOrBlank()) {
            val entry = com.example.pokedex.data.ALL_REGION_MAP_ENTRIES.firstOrNull { it.id == mapId }
            val editionId = entry?.preferredEditionId
            if (!editionId.isNullOrBlank() && !editionId.equals("Default", ignoreCase = true)) {
                return editionId
            }
        }
        return "Emerald"
    }

    fun openGuideModal() {
        _isGuideModalOpen.value = true
        scanningJob?.cancel()
        val ed = getEffectiveGameEdition()
        if (_guideResult.value == null) {
            searchGuide(
                query = if (_guideQuery.value.isNotBlank()) _guideQuery.value else "help",
                editionId = ed
            )
        }
    }

    fun closeGuideModal() {
        _isGuideModalOpen.value = false
    }

    private var guideSearchJob: Job? = null

    fun updateGuideQuery(q: String) {
        _guideQuery.value = q
        guideSearchJob?.cancel()
        if (q.isBlank()) {
            _guideResult.value = null
            return
        }
        guideSearchJob = viewModelScope.launch {
            delay(300)
            searchGuide(q)
        }
    }

    fun selectGuideTopic(topicName: String, cartridgeId: String? = null) {
        _guideQuery.value = topicName
        val ed = cartridgeId ?: getEffectiveGameEdition()
        searchGuide(topicName, ed)
    }

    fun searchGuide(query: String? = null, editionId: String? = null) {
        val rawQ = query ?: _guideQuery.value
        val ed = editionId ?: getEffectiveGameEdition()
        val q = if (rawQ.isBlank()) "help" else rawQ

        _isGuideSearching.value = true
        val offlineResult = PokemonGameGuideRepository.queryGuide(q, ed, getApplication())
        _guideResult.value = offlineResult
        _isGuideSearching.value = false
    }

    fun readGuideAdvice(result: GuideQueryResult) {
        speakCustomText(result.spokenAdvice)
    }

    private val _preferredGameEdition = MutableStateFlow(prefs.getString("preferred_game_edition", "Default") ?: "Default")
    val preferredGameEdition: StateFlow<String> = _preferredGameEdition.asStateFlow()

    fun setPreferredGameEdition(edition: String, syncRegionFilter: Boolean = true) {
        val canonicalEdition = if (edition.equals("Paldea", ignoreCase = true)) "Scarlet" else edition
        _preferredGameEdition.value = canonicalEdition
        prefs.edit().putString("preferred_game_edition", canonicalEdition).apply()
        if (syncRegionFilter) {
            val mapEntry = com.example.pokedex.data.getRegionMapEntryForEdition(canonicalEdition)
            _selectedRegion.value = mapEntry.region
            _selectedMapEntryId.value = mapEntry.id
            prefs.edit()
                .putString("last_selected_region", mapEntry.region?.name)
                .putString("last_selected_map_entry_id", mapEntry.id)
                .apply()
            savedStateHandle?.set("last_selected_region", mapEntry.region?.name)
            savedStateHandle?.set("last_selected_map_entry_id", mapEntry.id)
        }
        if (_isGuideModalOpen.value) {
            val q = if (_guideQuery.value.isBlank()) "help" else _guideQuery.value
            searchGuide(q, canonicalEdition)
        }
    }

    fun getEffectiveFlavorText(pokemon: PokemonEntity, cartridgeId: String? = null): String {
        val targetCartridge = cartridgeId ?: _preferredGameEdition.value
        return com.example.pokedex.data.PokemonMapHabitatRepository.resolvePokedexEntryForCartridge(
            pokemon,
            targetCartridge
        ).text
    }

    fun setScanFlowPreset(preset: ScanFlowPreset) {
        _scanFlowPreset.value = preset
        prefs.edit().putString("scan_flow_preset", preset.id).apply()
        if (preset != ScanFlowPreset.CUSTOM) {
            setPlayFanfare(preset.defaultFanfare)
            setPlayVoiceReveal(preset.defaultVoiceReveal)
            setPlayCryOnReveal(preset.defaultCryOnReveal)
            setAutoPlayCryEntry(preset.defaultAutoCryEntry)
        }
    }

    fun setAutoReadDexEntry(enabled: Boolean) {
        _autoReadDexEntry.value = enabled
        prefs.edit().putBoolean("auto_read_dex_entry", enabled).apply()
    }

    fun setPlayFanfare(enabled: Boolean) {
        _playFanfare.value = enabled
        prefs.edit().putBoolean("scan_play_fanfare", enabled).apply()
    }

    fun setPlayVoiceReveal(enabled: Boolean) {
        _playVoiceReveal.value = enabled
        prefs.edit().putBoolean("scan_voice_reveal", enabled).apply()
    }

    fun setPlayCryOnReveal(enabled: Boolean) {
        _playCryOnReveal.value = enabled
        prefs.edit().putBoolean("scan_cry_on_reveal", enabled).apply()
    }

    fun setAutoPlayCryEntry(enabled: Boolean) {
        _autoPlayCryEntry.value = enabled
        prefs.edit().putBoolean("scan_auto_cry_entry", enabled).apply()
    }

    fun playPokemonCry(pokemonId: Int) {
        viewModelScope.launch {
            PokemonSoundPlayer.playPokemonCry(getApplication(), pokemonId)
        }
    }

    private fun normalizeSearchString(text: String): String {
        return java.text.Normalizer.normalize(text.lowercase().trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(".", "")
            .replace("'", "")
            .replace("’", "")
            .replace("-", " ")
            .replace("♀", " f")
            .replace("♂", " m")
    }

    val filteredPokemonList: StateFlow<List<PokemonEntity>> = kotlinx.coroutines.flow.combine(
        _pokemonList,
        _searchQuery,
        _selectedRegion
    ) { list, query, region ->
        val nationalList = list.filter { it.id <= 1025 }
        var result = nationalList

        // 1. Regional filter from Chip / Settings Game Edition
        if (region != null) {
            result = result.filter { region.matches(it) }
        }

        // 2. Search query filter
        if (query.isNotBlank()) {
            val rawQ = query.trim().lowercase()
            val q = when (rawQ) {
                "mudberry", "mudbery", "mule", "donkey" -> "mudbray"
                "draft horse", "mudhorse" -> "mudsdale"
                else -> rawQ
            }
            val normQ = normalizeSearchString(q)
            val numId = rawQ.removePrefix("#").trimStart('0').toIntOrNull()

            val queryRegion = PokemonRegion.fromQuery(q)
            val isDirectPokemonMatch = nationalList.any {
                val nName = normalizeSearchString(it.name)
                nName == normQ || nName.contains(normQ) || (numId != null && it.id == numId)
            }

            if (queryRegion != null && region == null && !isDirectPokemonMatch) {
                result = result.filter { queryRegion.matches(it) }
            } else {
                val matchesQuery: (PokemonEntity) -> Boolean = { pokemon ->
                    val normName = normalizeSearchString(pokemon.name)
                    val normType = normalizeSearchString(pokemon.type)
                    val normForms = normalizeSearchString(pokemon.forms)
                    val normFlavor = if (normQ.length >= 4) normalizeSearchString(pokemon.flavorText) else ""

                    (numId != null && pokemon.id == numId) ||
                    pokemon.id.toString() == rawQ.removePrefix("#") ||
                    normName.contains(normQ) ||
                    normType.contains(normQ) ||
                    normForms.contains(normQ) ||
                    (normFlavor.isNotEmpty() && normFlavor.contains(normQ))
                }
                val regionalMatches = result.filter(matchesQuery)
                result = if (regionalMatches.isNotEmpty() || region == null) {
                    regionalMatches
                } else {
                    // Fallback to National Dex if the user explicitly searches a Pokémon name/number not in the current region filter
                    nationalList.filter(matchesQuery)
                }
            }
        }
        result
    }.stateIn(
        viewModelScope, 
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 
        emptyList()
    )

    init {
        // 1. Persistent State Restoration: Search Query, Region & Map Entry synchronized with Settings Game Version
        val savedSearch = savedStateHandle?.get<String>("last_search_query")
            ?: prefs.getString("last_search_query", "") ?: ""
        if (savedSearch.isNotBlank()) {
            _searchQuery.value = savedSearch
        }

        val preferredEd = _preferredGameEdition.value
        val savedRegionName = savedStateHandle?.get<String>("last_selected_region")
            ?: prefs.getString("last_selected_region", null)
        val savedEntryId = savedStateHandle?.get<String>("last_selected_map_entry_id")
            ?: prefs.getString("last_selected_map_entry_id", "all") ?: "all"

        if (!preferredEd.equals("Default", ignoreCase = true) && preferredEd.isNotBlank()) {
            val editionEntry = com.example.pokedex.data.getRegionMapEntryForEdition(preferredEd)
            // Keep Costumes if user explicitly picked Costumes (which also uses GO edition), otherwise sync to the Settings game version
            if (savedEntryId == "costumes" && preferredEd.equals("GO", ignoreCase = true)) {
                _selectedRegion.value = PokemonRegion.COSTUMES
                _selectedMapEntryId.value = "costumes"
            } else {
                _selectedRegion.value = editionEntry.region
                _selectedMapEntryId.value = editionEntry.id
                prefs.edit()
                    .putString("last_selected_region", editionEntry.region?.name)
                    .putString("last_selected_map_entry_id", editionEntry.id)
                    .apply()
            }
        } else {
            if (savedRegionName != null) {
                _selectedRegion.value = runCatching { PokemonRegion.valueOf(savedRegionName) }.getOrNull()
            }
            _selectedMapEntryId.value = savedEntryId
        }

        // 2. Persistent State Restoration: Active Pokemon Result Card
        val savedActiveState = savedStateHandle?.get<String>("last_active_state")
            ?: prefs.getString("last_active_state", "IDLE")
        val savedPokemonId = savedStateHandle?.get<Int>("last_viewed_pokemon_id")
            ?: prefs.getInt("last_viewed_pokemon_id", -1)
        val savedIsShiny = savedStateHandle?.get<Boolean>("last_viewed_is_shiny")
            ?: prefs.getBoolean("last_viewed_is_shiny", false)

        if (savedActiveState == "RESULT" && savedPokemonId > 0) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                var restored = pokemonDao.getPokemonById(savedPokemonId)
                if (restored == null) {
                    delay(350)
                    restored = pokemonDao.getPokemonById(savedPokemonId)
                }
                if (restored != null && _uiState.value is PokedexState.Idle) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        selectPokemon(
                            pokemon = restored,
                            isShiny = savedIsShiny,
                            autoSpeak = false, // Do not auto-play voice narration on cold restore
                            recordHistory = false
                        )
                    }
                }
            }
        }

        tts.onSpeakingStateChanged = { speaking ->
            _isSpeaking.value = speaking
        }

        tts.onVoicesLoaded = { voices ->
            _availableVoices.value = voices
        }

        syncTtsSettings()

        val assetVersion = prefs.getInt("pokemon_data_asset_version", 0)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val sampleBulbasaur = pokemonDao.getPokemonById(1)
            val hasGoLore = sampleBulbasaur?.pokedexEntries?.contains("\"GO\"") == true
            if (assetVersion < 12 || !hasGoLore) {
                PokedexDatabase.syncAssetData(getApplication(), pokemonDao)
                prefs.edit().putInt("pokemon_data_asset_version", 12).apply()
            }
        }
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // SMOKE TEST
            val testFile = java.io.File("/sdcard/Download/test_image.jpg")
            if (testFile.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(testFile.absolutePath)
                if (bitmap != null) {
                    onImageAnalyzed(bitmap)
                }
            }
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            pokemonDao.getAllPokemon().collect {
                _pokemonList.value = it
            }
        }
    }

    private fun syncTtsSettings() {
        tts.configure(
            profile = _voiceProfile.value,
            customVoice = _customVoiceName.value,
            dsp = _dspEnabled.value,
            pitch = _voicePitch.value,
            speed = _voiceSpeed.value
        )
    }

    fun setVoiceProfile(profile: VoiceProfile) {
        _voiceProfile.value = profile
        _dspEnabled.value = profile.defaultDsp
        _voicePitch.value = profile.defaultPitch
        _voiceSpeed.value = profile.defaultSpeed
        prefs.edit()
            .putString("voice_profile", profile.id)
            .putBoolean("dsp_enabled", profile.defaultDsp)
            .putFloat("voice_pitch", profile.defaultPitch)
            .putFloat("voice_speed", profile.defaultSpeed)
            .apply()
        syncTtsSettings()
    }

    fun setCustomVoiceName(voiceName: String?) {
        _customVoiceName.value = voiceName
        prefs.edit().putString("custom_voice_name", voiceName).apply()
        syncTtsSettings()
    }

    fun setDspEnabled(enabled: Boolean) {
        _dspEnabled.value = enabled
        prefs.edit().putBoolean("dsp_enabled", enabled).apply()
        syncTtsSettings()
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        prefs.edit().putFloat("voice_pitch", pitch).apply()
        syncTtsSettings()
    }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
        prefs.edit().putFloat("voice_speed", speed).apply()
        syncTtsSettings()
    }

    fun testVoice() {
        tts.stop()
        _isSpeaking.value = false
        tts.speak("Dexter, Pokédex entry. All systems operational.")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        prefs.edit().putString("last_search_query", query).apply()
        savedStateHandle?.set("last_search_query", query)
    }

    suspend fun getPreEvolution(name: String): PokemonEntity? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            pokemonDao.getPreEvolution(name)
        }
    }

    suspend fun getFullEvolutionChain(pokemon: PokemonEntity): List<PokemonEntity> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // 1. Walk up to the root (base form)
            var root = pokemon
            val visitedPre = mutableSetOf(pokemon.id)
            while (true) {
                val pre = pokemonDao.getPreEvolution(root.name) ?: break
                if (pre.id in visitedPre) break
                visitedPre.add(pre.id)
                root = pre
            }

            // 2. Walk down from root to build the full chain
            val chain = mutableListOf<PokemonEntity>()
            chain.add(root)
            var current = root
            val visitedNext = mutableSetOf(root.id)
            while (current.evolvesTo.isNotBlank()) {
                val nextNames = current.evolvesTo.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                if (nextNames.isEmpty()) break

                // If one of the next evolutions is the target pokemon or leads to it, pick that path
                val chosenName = if (nextNames.contains(pokemon.name.lowercase())) {
                    pokemon.name.lowercase()
                } else {
                    var foundName = nextNames.first()
                    for (candidate in nextNames) {
                        val candPokemon = pokemonDao.getPokemonByName(candidate)
                        if (candPokemon != null && candPokemon.evolvesTo.lowercase().contains(pokemon.name.lowercase())) {
                            foundName = candidate
                            break
                        }
                    }
                    foundName
                }

                val nextPokemon = pokemonDao.getPokemonByName(chosenName) ?: break
                if (nextPokemon.id in visitedNext) break
                visitedNext.add(nextPokemon.id)
                chain.add(nextPokemon)
                current = nextPokemon
            }
            chain
        }
    }

    fun selectPokemonByName(name: String, evolvedFrom: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pokemon = pokemonDao.getPokemonByName(name)
            if (pokemon != null) {
                selectPokemon(pokemon, evolvedFrom)
            }
        }
    }

    fun selectPokemon(
        pokemon: PokemonEntity,
        evolvedFrom: String? = null,
        isBackNavigation: Boolean = false,
        isShiny: Boolean = false,
        autoSpeak: Boolean = true,
        recordHistory: Boolean = true
    ) {
        consensusJob?.cancel()
        consensusJob = null
        scanningJob?.cancel()
        if (!isBackNavigation && recordHistory) {
            val currentState = _uiState.value
            if (currentState is PokedexState.Result) {
                historyStack.add(currentState.pokemon)
            }
        }

        tts.stop()
        PokemonSoundPlayer.stop()
        _isSpeaking.value = false
        _uiState.value = PokedexState.Result(pokemon, isShiny = isShiny)

        // Persist active view state
        prefs.edit()
            .putString("last_active_state", "RESULT")
            .putInt("last_viewed_pokemon_id", pokemon.id)
            .putBoolean("last_viewed_is_shiny", isShiny)
            .apply()
        savedStateHandle?.set("last_active_state", "RESULT")
        savedStateHandle?.set("last_viewed_pokemon_id", pokemon.id)
        savedStateHandle?.set("last_viewed_is_shiny", isShiny)

        val baseFormName = PokemonFormDetector.formatFormDisplayName(pokemon.name)
        val capitalizedName = if (isShiny) "Shiny $baseFormName" else baseFormName

        val capitalizedEvo = evolvedFrom?.replace("-", " ")
            ?.split(" ")
            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        val evoText = if (capitalizedEvo != null) {
            "${capitalizedName}, the evolved form of ${capitalizedEvo}. "
        } else {
            "${capitalizedName}. "
        }

        val typeText = "It is a ${pokemon.type.replace("/", " and ")} type Pokemon. "
        val rawFlavor = getEffectiveFlavorText(pokemon)
        val cleanedFlavor = rawFlavor.replace("\n", " ").replace("\u000c", " ").replace("\\f", " ").replace(Regex("\\s+"), " ").trim()

        if (autoSpeak && _autoReadDexEntry.value) {
            tts.speak("${evoText}${typeText}${cleanedFlavor}")
        }

        if (autoSpeak && _autoPlayCryEntry.value) {
            playPokemonCry(pokemon.id)
        }
    }

    fun readFlavorText(pokemon: PokemonEntity, flavorText: String, isShiny: Boolean = false) {
        tts.stop()
        _isSpeaking.value = false
        val baseFormName = PokemonFormDetector.formatFormDisplayName(pokemon.name)
        val capitalizedName = if (isShiny) "Shiny $baseFormName" else baseFormName



        val typeText = "It is a ${pokemon.type.replace("/", " and ")} type Pokemon. "
        val cleaned = flavorText
            .replace("[", "")
            .replace("]", ":")
            .replace("\n", " ")
            .replace("\u000c", " ")
            .replace("\\f", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        tts.speak("${capitalizedName}. ${typeText}${cleaned}")
    }

    fun speakCustomText(text: String) {
        tts.stop()
        _isSpeaking.value = false
        tts.speak(text)
    }

    fun selectPokemonById(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pokemon = pokemonDao.getPokemonById(id)
            if (pokemon != null) {
                selectPokemon(pokemon)
            }
        }
    }

    fun goBack() {
        if (historyStack.isNotEmpty()) {
            val previousPokemon = historyStack.removeAt(historyStack.size - 1)
            selectPokemon(previousPokemon, isBackNavigation = true)
        } else {
            resetState()
        }
    }

    fun selectPreviousPokemon() {
        val current = (_uiState.value as? PokedexState.Result)?.pokemon ?: return
        val canonicalId = current.getCanonicalId(_pokemonList.value)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val prev = pokemonDao.getPreviousPokemon(canonicalId) ?: pokemonDao.getLastPokemon()
            if (prev != null) selectPokemon(prev, isBackNavigation = false)
        }
    }

    fun selectNextPokemon() {
        val current = (_uiState.value as? PokedexState.Result)?.pokemon ?: return
        val canonicalId = current.getCanonicalId(_pokemonList.value)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val next = pokemonDao.getNextPokemon(canonicalId) ?: pokemonDao.getFirstPokemon()
            if (next != null) selectPokemon(next, isBackNavigation = false)
        }
    }

    private data class FrameResult(
        val topIndex: Int,
        val topVal: Float,
        val secondIndex: Int,
        val secondVal: Float,
        val probabilities: FloatArray,
        val bitmap: Bitmap
    )

    private val rollingFrames = ArrayDeque<FrameResult>()
    private val MAX_ROLLING_FRAMES = 12
    val GLARE_ATTRACTORS = setOf("volbeat", "illumise")
    private var scanningJob: Job? = null
    private var consensusJob: Job? = null
    private var scanSettlingUntil = 0L
    private var isAnalyzing = false
    private var lockStreakCandidate = -1
    private var lockStreakFrames = 0
    private var sharpestBitmap: Bitmap? = null
    private var highestStreakConfidence = 0f

    fun startScanning() {
        consensusJob?.cancel()
        consensusJob = null
        scanningJob?.cancel()
        tts.stop()
        PokemonSoundPlayer.stop()
        _isSpeaking.value = false
        _uiState.value = PokedexState.Scanning
        _scanStatusText.value = "FOCUSING..."
        synchronized(rollingFrames) {
            rollingFrames.clear()
        }
        lockStreakCandidate = -1
        lockStreakFrames = 0
        _scanStreak.value = 0
        _scanProgress.value = 0f
        _scannerTelemetry.value = ""
        _candidateForms.value = emptyList()
        _isCandidateShiny.value = false
        _detectedFormReason.value = null
        sharpestBitmap = null
        highestStreakConfidence = 0f
        // Quick 400ms settle for camera auto-focus/exposure
        scanSettlingUntil = System.currentTimeMillis() + 800L

        scanningJob = viewModelScope.launch {
            delay(60_000) // 60-second inactivity timeout
            if (_uiState.value is PokedexState.Scanning) {
                tts.stop()
                _isSpeaking.value = false
                tts.speak("No Pokemon detected.")
                _uiState.value = PokedexState.Error("NO POKEMON DETECTED")
                delay(2500)
                _uiState.value = PokedexState.Idle
            }
        }
    }

    private fun logScannerTelemetry(msg: String) {
        try {
            val app = getApplication<android.app.Application>()
            val logFile = java.io.File(app.filesDir, "scanner_telemetry.log")
            if (logFile.length() > 2_000_000) {
                val backup = java.io.File(app.filesDir, "scanner_telemetry.old.log")
                logFile.renameTo(backup)
            }
            val ts = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
            logFile.appendText("$ts $msg\n")
        } catch (_: Exception) {}
    }

    private fun lockConsensus(pokemon: PokemonEntity, bitmap: Bitmap?, isShiny: Boolean = _isCandidateShiny.value) {
        consensusJob?.cancel()
        consensusJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            if (_uiState.value !is PokedexState.Scanning) return@launch

            android.util.Log.d("PokedexViewModel", "Consensus LOCKED on: ${pokemon.name}!")
            logScannerTelemetry("Consensus LOCKED: ${pokemon.name} (#${pokemon.id})")
            val formTitle = PokemonFormDetector.formatFormDisplayName(pokemon.name)
            val displayTitle = if (isShiny) "✨ SHINY ${formTitle.uppercase()}" else formTitle.uppercase()
            _scanStatusText.value = "LOCKED: $displayTitle!"
            scanningJob?.cancel()
            val finalBitmap = bitmap ?: sharpestBitmap

            synchronized(rollingFrames) { rollingFrames.clear() }
            lockStreakCandidate = -1
            lockStreakFrames = 0
            _scanStreak.value = 0
            _scanProgress.value = 0f
            _candidatePokemon.value = null
            tts.stop()
            _isSpeaking.value = false

            if (_playFanfare.value && finalBitmap != null) {
                _uiState.value = PokedexState.WhosThat(finalBitmap)
                PokemonSoundPlayer.playWhosThatPokemon(getApplication())
                delay(3400)
            }

            if (!isActive) return@launch

            if (_playCryOnReveal.value) {
                PokemonSoundPlayer.playPokemonCry(getApplication(), pokemon.id)
            }

            if (_playVoiceReveal.value && finalBitmap != null) {
                _uiState.value = PokedexState.Reveal(pokemon, finalBitmap, isShiny = isShiny)
                val announcement = if (isShiny) "It's a Shiny $formTitle!" else "It's $formTitle!"
                tts.speak(announcement)
                delay(1600)
            }

            if (!isActive) return@launch

            selectPokemon(pokemon, isShiny = isShiny)
        }
    }

    fun triggerManualLock() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val labels = classifier.getLabels()
            val candidateId = lockStreakCandidate
            val candidateLabel = if (candidateId != -1) labels.getOrNull(candidateId) else null
            val rawCandName = candidateLabel?.let { nameMatcher.extractName(it) }
            var pokemon = _candidatePokemon.value
                ?: rawCandName?.let { pokemonDao.getPokemonByName(it) }

            // Fallback: If lockStreakCandidate was -1, get the highest candidate from rolling frames
            if (pokemon == null) {
                val snapshot = synchronized(rollingFrames) { rollingFrames.toList() }
                if (snapshot.isNotEmpty()) {
                    val lastFrame = snapshot.last()
                    val fallbackLabel = labels.getOrNull(lastFrame.topIndex)
                    val fallbackName = fallbackLabel?.let { nameMatcher.extractName(it) }
                    pokemon = fallbackName?.let { pokemonDao.getPokemonByName(it) }
                }
            }

            if (pokemon != null && _uiState.value is PokedexState.Scanning) {
                logScannerTelemetry("Manual tap-to-lock triggered on: ${pokemon.name}")
                lockConsensus(pokemon, sharpestBitmap, isShiny = _isCandidateShiny.value)
            } else {
                logScannerTelemetry("Manual tap-to-lock: No candidate available")
            }
        }
    }

    fun onImageAnalyzed(bitmap: Bitmap) {
        if (_uiState.value !is PokedexState.Scanning) {
            synchronized(rollingFrames) { rollingFrames.clear() }
            lockStreakCandidate = -1
            lockStreakFrames = 0
            _scanProgress.value = 0f
            _scannerTelemetry.value = ""
        _candidateForms.value = emptyList()
        _isCandidateShiny.value = false
        _detectedFormReason.value = null
            return
        }
        
        // Skip frames during the quick initial settling period
        if (System.currentTimeMillis() < scanSettlingUntil) {
            _scanStatusText.value = "FOCUSING..."
            return
        }

        if (isAnalyzing) return
        isAnalyzing = true

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val labels = classifier.getLabels()
                val probs = classifier.getProbabilities(bitmap)

                // Find top 2 indices for this frame
                var top1Idx = 0
                var top1Val = -1f
                var top2Idx = 0
                var top2Val = -1f

                for (i in probs.indices) {
                    val p = probs[i]
                    if (p > top1Val) {
                        top2Val = top1Val
                        top2Idx = top1Idx
                        top1Val = p
                        top1Idx = i
                    } else if (p > top2Val) {
                        top2Val = p
                        top2Idx = i
                    }
                }

                val rawTop1Label = labels.getOrNull(top1Idx)
                val rawTop1Name = rawTop1Label?.let { nameMatcher.extractName(it)?.lowercase() }
                if (rawTop1Name in GLARE_ATTRACTORS && top1Val < 0.60f) {
                    // Specular Glare Attenuation Filter: Suppress glare false-positive in raw distribution
                    probs[top1Idx] *= 0.25f

                    // Re-calculate top 1 & top 2 for this frame
                    top1Idx = 0
                    top1Val = -1f
                    top2Idx = 0
                    top2Val = -1f
                    for (i in probs.indices) {
                        val p = probs[i]
                        if (p > top1Val) {
                            top2Val = top1Val
                            top2Idx = top1Idx
                            top1Val = p
                            top1Idx = i
                        } else if (p > top2Val) {
                            top2Val = p
                            top2Idx = i
                        }
                    }
                }

                val frameResult = FrameResult(
                    topIndex = top1Idx,
                    topVal = top1Val,
                    secondIndex = top2Idx,
                    secondVal = top2Val,
                    probabilities = probs,
                    bitmap = bitmap
                )

                val framesSnapshot: List<FrameResult>
                synchronized(rollingFrames) {
                    rollingFrames.addLast(frameResult)
                    if (rollingFrames.size > MAX_ROLLING_FRAMES) {
                        rollingFrames.removeFirst()
                    }
                    framesSnapshot = rollingFrames.toList()
                }

                if (framesSnapshot.isEmpty()) return@launch

                // 1. Recency-Decayed Multi-Frame Softmax Fusion with Outlier Soft-Clipping
                val fusedProbs = FloatArray(labels.size)
                var totalWeight = 0f
                val nFrames = framesSnapshot.size

                for (i in framesSnapshot.indices) {
                    val f = framesSnapshot[i]
                    // Soft-clip raw frame confidence at 0.60f so one glitched frame cannot hijack the window
                    val clippedConf = minOf(f.topVal, 0.75f).coerceAtLeast(0.02f)
                    // Exponential recency decay: newest frame has weight 1.0, oldest in 8-frame buffer ~0.32
                    val recencyWeight = Math.pow(0.85, (nFrames - 1 - i).toDouble()).toFloat()
                    val weight = clippedConf * recencyWeight
                    totalWeight += weight
                    for (j in f.probabilities.indices) {
                        fusedProbs[j] += f.probabilities[j] * weight
                    }
                }

                if (totalWeight > 0f) {
                    for (i in fusedProbs.indices) {
                        fusedProbs[i] /= totalWeight
                    }
                }

                // Get top 2 fused candidates across the multi-frame window
                var fusedTop1Idx = 0
                var fusedTop1Val = -1f
                var fusedTop2Idx = 0
                var fusedTop2Val = -1f
                for (i in fusedProbs.indices) {
                    val p = fusedProbs[i]
                    if (p > fusedTop1Val) {
                        fusedTop2Val = fusedTop1Val
                        fusedTop2Idx = fusedTop1Idx
                        fusedTop1Val = p
                        fusedTop1Idx = i
                    } else if (p > fusedTop2Val) {
                        fusedTop2Val = p
                        fusedTop2Idx = i
                    }
                }

                val fusedTop1Label = labels.getOrNull(fusedTop1Idx) ?: "UNKNOWN"
                val fusedTop2Label = labels.getOrNull(fusedTop2Idx) ?: "UNKNOWN"
                val top1DisplayName = nameMatcher.extractName(fusedTop1Label)?.uppercase() ?: "POKEMON"
                val top2DisplayName = nameMatcher.extractName(fusedTop2Label)?.uppercase() ?: "--"

                // 2. Multi-Tier Background & Ambient Clutter Rejection
                // Must have at least 15% confidence and beat #2 by 2.0% or 1.20x ratio
                val MIN_ACQUISITION_CONFIDENCE = 0.15f
                val MIN_PEAK_MARGIN = 0.025f
                val MIN_PEAK_RATIO = 1.20f

                val leadMargin = fusedTop1Val - fusedTop2Val
                val leadRatio = if (fusedTop2Val > 0f) fusedTop1Val / fusedTop2Val else 10f
                val isRecognizedTarget = (fusedTop1Val >= MIN_ACQUISITION_CONFIDENCE) &&
                        (leadMargin >= MIN_PEAK_MARGIN || leadRatio >= MIN_PEAK_RATIO)

                if (!isRecognizedTarget) {
                    // Non-Pokémon object or target lost: decay streak rapidly by 3 frames per iteration
                    lockStreakFrames = maxOf(0, lockStreakFrames - 2)
                    if (lockStreakFrames == 0) {
                        lockStreakCandidate = -1
                        highestStreakConfidence = 0f
                        _candidatePokemon.value = null
                        _scanProgress.value = 0f
                        _scanStreak.value = 0
                        _scanStatusText.value = "SEARCHING FOR POKÉMON..."
                    } else {
                        _scanStreak.value = lockStreakFrames
                        _scanProgress.value = (lockStreakFrames.toFloat() / 20f).coerceIn(0f, 1f)
                    }
                    val rejectionTelemetry = if (fusedTop1Val >= 0.05f) {
                        "#1: $top1DisplayName (${(fusedTop1Val * 100).toInt()}%) | #2: $top2DisplayName (${(fusedTop2Val * 100).toInt()}%) [REJECTED]"
                    } else {
                        "SEARCHING... (NO TARGET DETECTED)"
                    }
                    _scannerTelemetry.value = rejectionTelemetry
                    return@launch
                }

                // 3. Responsive Candidate Selection & Usurpation (Eliminates the 1.35x Sticky Lockout Trap)
                val activeCand = lockStreakCandidate
                val effectiveLeaderIdx: Int
                val effectiveLeaderVal: Float

                if (activeCand != -1 && lockStreakFrames > 0) {
                    val activeVal = fusedProbs.getOrElse(activeCand) { 0f }
                    if (fusedTop1Idx == activeCand) {
                        effectiveLeaderIdx = activeCand
                        effectiveLeaderVal = fusedTop1Val
                    } else if (activeVal < MIN_ACQUISITION_CONFIDENCE || activeVal < 0.12f) {
                        // Active candidate collapsed: immediately switch to new leader!
                        effectiveLeaderIdx = fusedTop1Idx
                        effectiveLeaderVal = fusedTop1Val
                    } else if (fusedTop1Val >= activeVal + 0.03f) {
                        // Challenger has a clean lead (+3% margin): switch candidate responsively
                        effectiveLeaderIdx = fusedTop1Idx
                        effectiveLeaderVal = fusedTop1Val
                    } else {
                        // Minor statistical tie/jitter: preserve active candidate for stability
                        effectiveLeaderIdx = activeCand
                        effectiveLeaderVal = activeVal
                    }
                } else {
                    effectiveLeaderIdx = fusedTop1Idx
                    effectiveLeaderVal = fusedTop1Val
                }

                val effectiveLabel = labels.getOrNull(effectiveLeaderIdx) ?: "UNKNOWN"
                val rawCandName = nameMatcher.extractName(effectiveLabel)
                val candName = rawCandName?.uppercase() ?: "POKEMON"

                // 3B. Resilient Candidate Selection & Soft Streak Decay
                if (effectiveLeaderIdx == lockStreakCandidate) {
                    lockStreakFrames++
                    if (effectiveLeaderVal > highestStreakConfidence) {
                        highestStreakConfidence = effectiveLeaderVal
                        sharpestBitmap = bitmap
                    }
                } else if (lockStreakFrames >= 5) {
                    // Soft decay: a momentary single-frame glare/jitter only decays the streak by 3
                    // rather than wiping out accumulated progress!
                    lockStreakFrames = maxOf(1, lockStreakFrames - 3)
                } else {
                    // Incumbent is exhausted or had low momentum: switch to new candidate
                    lockStreakCandidate = effectiveLeaderIdx
                    lockStreakFrames = 1
                    highestStreakConfidence = effectiveLeaderVal
                    sharpestBitmap = bitmap
                }

                // 4. Graduated Multi-Tier Auto-Locking (Human-Crafted Temporal Runway)
                // Tier 1: Ultra-High Confidence (>= 75%) -> Lock in 16 frames (~1.1s)
                // Tier 2: High Confidence (>= 50%) -> Lock in 22 frames (~1.5s)
                // Tier 3: Moderate Confidence (>= 35%) & Lead Ratio >= 1.40x -> Lock in 28 frames (~1.9s)
                // Tier 4: Borderline / Complex (< 35%) -> Advisory Mode (999 frames), NEVER auto-locks
                val currentLeadRatio = fusedTop1Val / maxOf(fusedTop2Val, 0.01f)
                val currentMargin = fusedTop1Val - fusedTop2Val
                val effectiveTargetConf = maxOf(effectiveLeaderVal, highestStreakConfidence * 0.88f)
                val isGlareAttractor = rawCandName?.lowercase() in GLARE_ATTRACTORS
                val targetLockFrames = when {
                    isGlareAttractor && effectiveTargetConf < 0.60f -> 999 // Strictly forbid auto-locking glare
                    effectiveTargetConf >= 0.65f -> 8
                    effectiveTargetConf >= 0.40f -> 10
                    effectiveTargetConf >= 0.26f && (currentLeadRatio >= 1.40f || currentMargin >= 0.07f) -> 12
                    effectiveTargetConf >= 0.22f && currentLeadRatio >= 1.80f -> 14
                    effectiveTargetConf >= 0.17f && currentLeadRatio >= 2.50f -> 16
                    else -> 999 // Advisory mode: never auto-locks on weak guesses, user can tap 1-Tap LOCK FAB
                }

                val maxDisplayFrames = if (targetLockFrames <= 30) targetLockFrames else 999
                val progressPct = if (targetLockFrames <= 30) (lockStreakFrames.toFloat() / targetLockFrames.toFloat()).coerceIn(0f, 1f) else 0f
                _scanProgress.value = progressPct
                _scanStreak.value = lockStreakFrames

                if (lockStreakFrames > 0 && rawCandName != null) {
                    val currentCand = _candidatePokemon.value
                    val currentCanonicalName = currentCand?.let { 
                        val baseId = it.getCanonicalId(_pokemonList.value)
                        _pokemonList.value.firstOrNull { b -> b.id == baseId }?.name ?: it.name
                    }

                    if (currentCand == null || !currentCanonicalName.equals(rawCandName, ignoreCase = true)) {
                        val baseEntity = pokemonDao.getPokemonByName(rawCandName)
                        if (baseEntity != null) {
                            val formList = if (baseEntity.forms.isNotBlank()) {
                                val names = baseEntity.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                pokemonDao.getPokemonByNames(names)
                            } else {
                                emptyList()
                            }
                            _candidateForms.value = formList

                            val sampleBitmap = sharpestBitmap ?: bitmap
                            val disambiguation = PokemonFormDetector.disambiguate(
                                bitmap = sampleBitmap,
                                basePokemon = baseEntity,
                                availableForms = formList
                            )
                            _isCandidateShiny.value = disambiguation.isShiny
                            _detectedFormReason.value = disambiguation.reason

                            val resolvedEntity = if (disambiguation.resolvedFormName.equals(baseEntity.name, ignoreCase = true)) {
                                baseEntity
                            } else {
                                formList.firstOrNull { it.name.equals(disambiguation.resolvedFormName, ignoreCase = true) } ?: baseEntity
                            }
                            _candidatePokemon.value = resolvedEntity
                        }
                    } else {
                        // Periodic re-evaluation as clearer / sharper frames arrive
                        val forms = _candidateForms.value
                        if (forms.isNotEmpty() && lockStreakFrames % 3 == 0) {
                            val baseEntity = pokemonDao.getPokemonByName(rawCandName)
                            if (baseEntity != null) {
                                val sampleBitmap = sharpestBitmap ?: bitmap
                                val disambiguation = PokemonFormDetector.disambiguate(
                                    bitmap = sampleBitmap,
                                    basePokemon = baseEntity,
                                    availableForms = forms
                                )
                                if (disambiguation.confidence >= 0.80f) {
                                    _isCandidateShiny.value = disambiguation.isShiny
                                    _detectedFormReason.value = disambiguation.reason
                                    val resolvedEntity = if (disambiguation.resolvedFormName.equals(baseEntity.name, ignoreCase = true)) {
                                        baseEntity
                                    } else {
                                        forms.firstOrNull { it.name.equals(disambiguation.resolvedFormName, ignoreCase = true) } ?: baseEntity
                                    }
                                    _candidatePokemon.value = resolvedEntity
                                }
                            }
                        }
                    }
                } else if (lockStreakFrames == 0) {
                    _candidatePokemon.value = null
                    _candidateForms.value = emptyList()
                    _isCandidateShiny.value = false
                    _detectedFormReason.value = null
                }

                val candidateEntity = _candidatePokemon.value
                val activeFormTitle = if (candidateEntity != null) PokemonFormDetector.formatFormDisplayName(candidateEntity.name).uppercase() else candName
                val displayCandTitle = if (_isCandidateShiny.value) "✨ SHINY $activeFormTitle" else activeFormTitle

                when {
                    lockStreakFrames == 0 -> _scanStatusText.value = "SEARCHING FOR POKÉMON..."
                    targetLockFrames == 999 -> _scanStatusText.value = "ACQUIRING: $displayCandTitle (TAP LOCK)"
                    progressPct < 0.25f -> _scanStatusText.value = "ACQUIRING: $displayCandTitle..."
                    progressPct < 0.50f -> _scanStatusText.value = "LOCKING IN 3..."
                    progressPct < 0.75f -> _scanStatusText.value = "LOCKING IN 2..."
                    progressPct < 1.0f -> _scanStatusText.value = "LOCKING IN 1..."
                    else -> _scanStatusText.value = "LOCKED: $displayCandTitle!"
                }

                val telemetryStr = "#1: $top1DisplayName (${(fusedTop1Val * 100).toInt()}%) | #2: $top2DisplayName (${(fusedTop2Val * 100).toInt()}%)"
                _scannerTelemetry.value = telemetryStr

                val logLine = "Frame: lead=$displayCandTitle(${"%.4f".format(effectiveLeaderVal)}), top1=$fusedTop1Label(${"%.4f".format(fusedTop1Val)}), top2=$fusedTop2Label(${"%.4f".format(fusedTop2Val)}), streak=$lockStreakFrames/$targetLockFrames"
                android.util.Log.d("PokedexViewModel", logLine)
                logScannerTelemetry(logLine)

                // 5. Consensus Locked: Reached targetLockFrames AND meets minimum confidence
                if (lockStreakFrames >= targetLockFrames && targetLockFrames <= 30 && highestStreakConfidence >= 0.17f && effectiveLeaderVal >= 0.15f) {
                    if (rawCandName?.lowercase() in GLARE_ATTRACTORS && highestStreakConfidence < 0.60f) {
                        return@launch
                    }
                    val resolvedPokemon = _candidatePokemon.value ?: rawCandName?.let { pokemonDao.getPokemonByName(it) }
                    if (resolvedPokemon != null && _uiState.value is PokedexState.Scanning) {
                        lockConsensus(resolvedPokemon, sharpestBitmap ?: bitmap, isShiny = _isCandidateShiny.value)
                        return@launch
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PokedexViewModel", "Error in onImageAnalyzed", e)
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun analyzeGalleryImage(rawBitmap: Bitmap) {
        scanningJob?.cancel()
        tts.stop()
        _isSpeaking.value = false

        // 1. Standard 100% 1:1 Center Crop for display & primary inference
        val minDim = minOf(rawBitmap.width, rawBitmap.height).coerceAtLeast(1)
        val cropX = (rawBitmap.width - minDim) / 2
        val cropY = (rawBitmap.height - minDim) / 2
        val bitmap = Bitmap.createBitmap(rawBitmap, cropX, cropY, minDim, minDim)

        _uiState.value = PokedexState.WhosThat(bitmap)

        consensusJob?.cancel()
        consensusJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val labels = classifier.getLabels()
                val crops = mutableListOf<Bitmap>(bitmap)

                // 2. Tighter 75% Center Crop (catches Pokémon inside phone screenshots, cards, or wider photos)
                val innerDim = (minDim * 0.75f).toInt().coerceAtLeast(1)
                if (innerDim < minDim) {
                    val innerX = (rawBitmap.width - innerDim) / 2
                    val innerY = (rawBitmap.height - innerDim) / 2
                    crops.add(Bitmap.createBitmap(rawBitmap, innerX, innerY, innerDim, innerDim))
                }

                // 3. Full-Image Square Letterbox Fit (preserves full silhouette on tall/wide photos)
                if (rawBitmap.width != rawBitmap.height) {
                    val maxDim = maxOf(rawBitmap.width, rawBitmap.height).coerceAtLeast(1)
                    val fitted = Bitmap.createBitmap(maxDim, maxDim, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(fitted)
                    canvas.drawColor(android.graphics.Color.rgb(245, 245, 245))
                    val left = (maxDim - rawBitmap.width) / 2f
                    val top = (maxDim - rawBitmap.height) / 2f
                    canvas.drawBitmap(rawBitmap, left, top, null)
                    crops.add(fitted)
                }

                // Fuse multi-crop probabilities weighted by each crop's peak confidence
                val fusedProbs = FloatArray(labels.size)
                var totalWeight = 0f
                for (cropBmp in crops) {
                    val cropProbs = classifier.getProbabilities(cropBmp)
                    var cropMaxIdx = 0
                    var cropMaxVal = -1f
                    for (i in cropProbs.indices) {
                        if (cropProbs[i] > cropMaxVal) {
                            cropMaxVal = cropProbs[i]
                            cropMaxIdx = i
                        }
                    }
                    val rawCropLabel = labels.getOrNull(cropMaxIdx)
                    val rawCropName = rawCropLabel?.let { nameMatcher.extractName(it)?.lowercase() }
                    if (rawCropName in GLARE_ATTRACTORS && cropMaxVal < 0.60f) {
                        cropProbs[cropMaxIdx] *= 0.25f
                        cropMaxVal = cropProbs.maxOrNull() ?: 0.01f
                    }
                    val w = cropMaxVal.coerceAtLeast(0.05f)
                    totalWeight += w
                    for (i in cropProbs.indices) {
                        fusedProbs[i] += cropProbs[i] * w
                    }
                }
                if (totalWeight > 0f) {
                    for (i in fusedProbs.indices) {
                        fusedProbs[i] /= totalWeight
                    }
                }

                val topCandidates = fusedProbs.withIndex().sortedByDescending { it.value }.take(5)
                var logStr = "Gallery Multi-Crop Top 3: "
                for (p in topCandidates.take(3)) {
                    logStr += "${labels.getOrNull(p.index)} (${"%.4f".format(p.value)}), "
                }
                android.util.Log.d("PokedexViewModel", logStr)
                logScannerTelemetry(logStr)

                // Always pass through: resolve the top matched Pokémon from the ranked candidates
                var basePokemon: PokemonEntity? = null
                for (cand in topCandidates) {
                    val label = labels.getOrNull(cand.index) ?: continue
                    val extracted = nameMatcher.extractName(label) ?: label.lowercase().trim()
                    basePokemon = pokemonDao.getPokemonByName(extracted)
                        ?: pokemonDao.getPokemonByName(label.lowercase().trim())
                        ?: pokemonDao.getPokemonByName(extracted.substringBefore("-"))
                    if (basePokemon != null) break
                }
                if (basePokemon == null) {
                    basePokemon = _pokemonList.value.firstOrNull() ?: pokemonDao.getFirstPokemon() ?: pokemonDao.getPokemonById(1)
                }

                if (basePokemon != null) {
                    val formList = if (basePokemon.forms.isNotBlank()) {
                        val names = basePokemon.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        pokemonDao.getPokemonByNames(names)
                    } else {
                        emptyList()
                    }
                    val disambiguation = PokemonFormDetector.disambiguate(bitmap, basePokemon, formList)
                    val pokemon = if (disambiguation.resolvedFormName.equals(basePokemon.name, ignoreCase = true)) {
                        basePokemon
                    } else {
                        formList.firstOrNull { it.name.equals(disambiguation.resolvedFormName, ignoreCase = true) } ?: basePokemon
                    }
                    val isShiny = disambiguation.isShiny
                    val formTitle = PokemonFormDetector.formatFormDisplayName(pokemon.name)

                    tts.stop()
                    _isSpeaking.value = false

                    if (_playFanfare.value) {
                        _uiState.value = PokedexState.WhosThat(bitmap)
                        PokemonSoundPlayer.playWhosThatPokemon(getApplication())
                        delay(3400)
                    }

                    if (!isActive) return@launch

                    if (_playCryOnReveal.value) {
                        PokemonSoundPlayer.playPokemonCry(getApplication(), pokemon.id)
                    }

                    if (_playVoiceReveal.value) {
                        _uiState.value = PokedexState.Reveal(pokemon, bitmap, isShiny = isShiny)
                        val announcement = if (isShiny) "It's a Shiny $formTitle!" else "It's $formTitle!"
                        tts.speak(announcement)
                        delay(1600)
                    }

                    if (!isActive) return@launch

                    selectPokemon(pokemon, isShiny = isShiny)
                    return@launch
                }

                _uiState.value = PokedexState.Idle
            } catch (e: Exception) {
                android.util.Log.e("PokedexViewModel", "Error analyzing gallery image", e)
                val fallback = _pokemonList.value.firstOrNull()
                if (fallback != null && isActive) {
                    selectPokemon(fallback)
                } else if (isActive) {
                    _uiState.value = PokedexState.Idle
                }
            }
        }
    }

    fun resetState() {
        consensusJob?.cancel()
        consensusJob = null
        scanningJob?.cancel()
        scanningJob = null

        tts.stop()
        PokemonSoundPlayer.stop()
        _isSpeaking.value = false

        historyStack.clear()
        synchronized(rollingFrames) { rollingFrames.clear() }

        lockStreakCandidate = -1
        lockStreakFrames = 0
        highestStreakConfidence = 0f
        sharpestBitmap = null
        isAnalyzing = false

        _scanStreak.value = 0
        _scanProgress.value = 0f
        _candidatePokemon.value = null
        _scanStatusText.value = "SEARCHING FOR POKÉMON..."
        _scannerTelemetry.value = ""
        _candidateForms.value = emptyList()
        _isCandidateShiny.value = false
        _detectedFormReason.value = null

        _uiState.value = PokedexState.Idle
        prefs.edit()
            .putString("last_active_state", "IDLE")
            .putInt("last_viewed_pokemon_id", -1)
            .apply()
        savedStateHandle?.set("last_active_state", "IDLE")
        savedStateHandle?.set("last_viewed_pokemon_id", -1)
    }

    override fun onCleared() {
        super.onCleared()
        consensusJob?.cancel()
        scanningJob?.cancel()
        tts.shutdown()
        PokemonSoundPlayer.stop()
        _isSpeaking.value = false
        classifier.release()
    }
}