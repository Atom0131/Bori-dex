package com.example.pokedex.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import com.example.pokedex.camera.PokemonFormDetector
import com.example.pokedex.camera.TFLiteClassifier
import com.example.pokedex.data.PokedexDatabase
import com.example.pokedex.data.PokemonEntity
import com.example.pokedex.data.PokemonNameMatcher
import com.example.pokedex.data.PokemonTypeEffectiveness
import com.example.pokedex.tts.OfflineTTS
import com.example.pokedex.tts.VoiceProfile
import com.example.pokedex.ui.getColorForType
import com.example.pokedex.ui.getPokemonSpriteModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

enum class OverlayMode {
    MINI_LENS,
    SCANNER_RETICLE,
    BATTLE_GUIDE
}

data class CropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

data class TacticalBattleProfile(
    val pokemonName: String,
    val types: List<String>,
    val quadWeaknesses: List<String>,
    val doubleWeaknesses: List<String>,
    val resistances: List<String>,
    val immunities: List<String>,
    val stabStrengths: List<String>,
    val spokenBriefing: String
)

object BattleOverlayCropHelper {

    fun computeReticleCropBounds(
        screenWidth: Int,
        screenHeight: Int,
        reticleCenterX: Int,
        reticleCenterY: Int,
        reticleSizePx: Int,
        scaleFactor: Float = 1.0f
    ): CropRect {
        val safeW = screenWidth.coerceAtLeast(64)
        val safeH = screenHeight.coerceAtLeast(64)
        val minScreenDim = minOf(safeW, safeH)
        val targetSize = (reticleSizePx * scaleFactor).roundToInt().coerceIn(48, minScreenDim)
        val left = (reticleCenterX - targetSize / 2).coerceIn(0, (safeW - targetSize).coerceAtLeast(0))
        val top = (reticleCenterY - targetSize / 2).coerceIn(0, (safeH - targetSize).coerceAtLeast(0))
        return CropRect(left = left, top = top, width = targetSize, height = targetSize)
    }

    fun computeTacticalBattleProfile(
        pokemonName: String,
        typeString: String,
        isSpanish: Boolean = false
    ): TacticalBattleProfile {
        val cleanTypes = typeString
            .split("/", ",", " ")
            .map { PokemonTypeEffectiveness.normalizeTypeName(it) }
            .filter { it.isNotBlank() }
            .distinct()

        val matchups = PokemonTypeEffectiveness.getFullMatchups(typeString)
        val stab = PokemonTypeEffectiveness.getOffensiveStrengths(typeString)
        val resists = (matchups.notVeryEffectiveHalf + matchups.notVeryEffectiveQuarter).distinct()

        val spokenParts = mutableListOf<String>()
        val typeSpoken = if (cleanTypes.isNotEmpty()) cleanTypes.joinToString(" and ") else "Normal"
        spokenParts.add("Battle Guide: $pokemonName. $typeSpoken type.")

        if (matchups.superEffectiveQuad.isNotEmpty()) {
            spokenParts.add("Super effective 4x weak to ${matchups.superEffectiveQuad.joinToString(", ")}.")
        }
        if (matchups.effectiveDouble.isNotEmpty()) {
            spokenParts.add("Weak 2x to ${matchups.effectiveDouble.joinToString(", ")}.")
        }
        if (matchups.immune.isNotEmpty()) {
            spokenParts.add("Immune to ${matchups.immune.joinToString(", ")}.")
        }
        if (resists.isNotEmpty()) {
            spokenParts.add("Resists ${resists.joinToString(", ")}.")
        }
        if (stab.isNotEmpty()) {
            spokenParts.add("Its STAB attacks hit ${stab.joinToString(", ")} for 2x damage.")
        }

        return TacticalBattleProfile(
            pokemonName = pokemonName,
            types = cleanTypes,
            quadWeaknesses = matchups.superEffectiveQuad,
            doubleWeaknesses = matchups.effectiveDouble,
            resistances = resists,
            immunities = matchups.immune,
            stabStrengths = stab,
            spokenBriefing = spokenParts.joinToString(" ")
        )
    }

    fun resolveCandidatesWithHint(
        visualCandidates: List<PokemonEntity>,
        hintPokemon: PokemonEntity?
    ): List<PokemonEntity> {
        val resolved = visualCandidates.toMutableList()
        if (hintPokemon != null) {
            val existingIndex = resolved.indexOfFirst { it.id == hintPokemon.id }
            if (existingIndex > 0) {
                // Hint matches an alternate candidate already in the visual top list -> promote it
                val match = resolved.removeAt(existingIndex)
                resolved.add(0, match)
            } else if (resolved.isEmpty()) {
                // No visual candidates found -> fallback to window hint
                resolved.add(hintPokemon)
            } else if (resolved.none { it.id == hintPokemon.id }) {
                // Keep visual candidates first; add window hint as alternate candidate chip
                resolved.add(hintPokemon)
            }
        }
        return resolved
    }
}

private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class PokedexOverlayAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_OVERLAY_CONTROL = "com.example.pokedex.ACTION_OVERLAY_CONTROL"

        @Volatile
        private var instance: PokedexOverlayAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _isOverlayVisible = MutableStateFlow(false)
        val isOverlayVisible: StateFlow<Boolean> = _isOverlayVisible.asStateFlow()

        private val _overlayMode = MutableStateFlow(OverlayMode.SCANNER_RETICLE)
        val overlayMode: StateFlow<OverlayMode> = _overlayMode.asStateFlow()

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (instance != null && _isServiceConnected.value) return true
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(context.packageName, ignoreCase = true) &&
                enabledServices.contains("PokedexOverlayAccessibilityService", ignoreCase = true)
        }

        fun showOverlay(mode: OverlayMode = OverlayMode.SCANNER_RETICLE): Boolean {
            val svc = instance ?: return false
            svc.mainHandler.post {
                svc.ensureOverlayAttached()
                _overlayMode.value = mode
                _isOverlayVisible.value = true
                svc.updateOverlayVisibility(true)
            }
            return true
        }

        fun hideOverlay() {
            val svc = instance ?: return
            svc.mainHandler.post {
                _isOverlayVisible.value = false
                svc.updateOverlayVisibility(false)
            }
        }

        fun triggerScan() {
            val svc = instance ?: return
            svc.mainHandler.post {
                svc.ensureOverlayAttached()
                _isOverlayVisible.value = true
                svc.updateOverlayVisibility(true)
                svc.executeOverlayScan()
            }
        }

        fun selectPokemonInOverlay(pokemonId: Int) {
            val svc = instance ?: return
            svc.serviceScope.launch(Dispatchers.IO) {
                val dao = PokedexDatabase.getDatabase(svc.applicationContext).pokemonDao()
                val entity = dao.getPokemonById(pokemonId) ?: dao.getPokemonById(6)
                if (entity != null) {
                    withContext(Dispatchers.Main) {
                        svc.ensureOverlayAttached()
                        _isOverlayVisible.value = true
                        svc.updateOverlayVisibility(true)
                        svc.applyLockedPokemon(entity, listOf(entity), isShiny = false)
                    }
                }
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private var classifier: TFLiteClassifier? = null
    private var tts: OfflineTTS? = null
    private val nameMatcher = PokemonNameMatcher()

    // Reactive UI State inside the Overlay
    private val _isScanning = MutableStateFlow(false)
    private val _scanStatusText = MutableStateFlow("DRAG OVER POKÉMON & TAP SCAN")
    private val _scanProgress = MutableStateFlow(0f)
    private val _scanStreak = MutableStateFlow(0)
    private val _lockedPokemon = MutableStateFlow<PokemonEntity?>(null)
    private val _candidateList = MutableStateFlow<List<PokemonEntity>>(emptyList())
    private val _availableForms = MutableStateFlow<List<PokemonEntity>>(emptyList())
    private val _allPokemon = MutableStateFlow<List<PokemonEntity>>(emptyList())
    private val _isShiny = MutableStateFlow(false)
    private val _showQuickPicker = MutableStateFlow(false)

    // Reticle center on screen (updated by Compose layout coordinates)
    @Volatile
    private var reticleCenterScreenX: Int = 540
    @Volatile
    private var reticleCenterScreenY: Int = 700
    @Volatile
    private var reticleBoxSizePx: Int = 520

    @Volatile
    private var targetOverlayX: Int = 80
    @Volatile
    private var targetOverlayY: Int = 180
    @Volatile
    private var forcedReticleX: Int? = null
    @Volatile
    private var forcedReticleY: Int? = null

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_OVERLAY_CONTROL) return
            when (intent.getStringExtra("cmd")?.lowercase()) {
                "show", "reticle" -> {
                    val posX = intent.getIntExtra("x", -1)
                    val posY = intent.getIntExtra("y", -1)
                    mainHandler.post {
                        if (posX >= 0 && posY >= 0) {
                            targetOverlayX = posX
                            targetOverlayY = posY
                        }
                        showOverlay(OverlayMode.SCANNER_RETICLE)
                        if (posX >= 0 && posY >= 0) {
                            setOverlayPosition(posX, posY)
                        }
                    }
                }
                "mini" -> showOverlay(OverlayMode.MINI_LENS)
                "hide" -> hideOverlay()
                "scan" -> {
                    val rx = intent.getIntExtra("reticle_x", -1)
                    val ry = intent.getIntExtra("reticle_y", -1)
                    if (rx >= 0 && ry >= 0) {
                        forcedReticleX = rx
                        forcedReticleY = ry
                    }
                    triggerScan()
                }
                "pokemon", "guide" -> {
                    val id = intent.getIntExtra("pokemon_id", intent.getIntExtra("id", 6))
                    selectPokemonInOverlay(id)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true

        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = info.flags or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.DEFAULT
            serviceInfo = info
        } catch (_: Exception) {}

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter(ACTION_OVERLAY_CONTROL)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(controlReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(controlReceiver, filter)
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val db = PokedexDatabase.getDatabase(applicationContext)
                val dao = db.pokemonDao()
                PokedexDatabase.syncAssetDataIfNeeded(applicationContext, dao)
                classifier = TFLiteClassifier(applicationContext)
                withContext(Dispatchers.Main) {
                    val offlineTts = OfflineTTS(applicationContext)
                    val prefs = applicationContext.getSharedPreferences("pokedex_prefs", Context.MODE_PRIVATE)
                    val profileId = prefs.getString("voice_profile", VoiceProfile.ORIGINAL_90S_MALE.id) ?: VoiceProfile.ORIGINAL_90S_MALE.id
                    val profile = VoiceProfile.fromId(profileId)
                    val dsp = prefs.getBoolean("dsp_enabled", true)
                    val pitch = prefs.getFloat("voice_pitch", profile.defaultPitch).let { if (it <= 0f) profile.defaultPitch else it }
                    val speed = prefs.getFloat("voice_speed", profile.defaultSpeed).let { if (it <= 0f) profile.defaultSpeed else it }
                    offlineTts.configure(profile, prefs.getString("custom_voice_name", null), dsp, pitch, speed)
                    tts = offlineTts
                }
                dao.getAllPokemon().collect { list ->
                    _allPokemon.value = list.filter { it.id <= 1025 }
                    if (_lockedPokemon.value == null && list.isNotEmpty()) {
                        _lockedPokemon.value = list.firstOrNull { it.id == 6 } ?: list.first()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PokedexOverlayService", "Init error", e)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive listener — scanning is user-triggered or broadcast-triggered.
    }

    override fun onInterrupt() {
        tts?.stop()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(controlReceiver)
        } catch (_: Exception) {}
        removeOverlayWindow()
        tts?.shutdown()
        classifier?.release()
        serviceScope.cancel()
        _isServiceConnected.value = false
        _isOverlayVisible.value = false
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    private fun ensureOverlayAttached() {
        if (composeView != null) return
        val wm = windowManager ?: (getSystemService(Context.WINDOW_SERVICE) as WindowManager).also { windowManager = it }

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        lifecycleOwner = owner

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = targetOverlayX
            y = targetOverlayY
        }
        layoutParams = params

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                val darkColors = darkColorScheme(
                    surface = Color(0xFF141A1F),
                    onSurface = Color.White
                )
                MaterialTheme(colorScheme = darkColors) {
                    val visible by _isOverlayVisible.collectAsState()
                    val mode by _overlayMode.collectAsState()
                    val isScanning by _isScanning.collectAsState()
                    val statusText by _scanStatusText.collectAsState()
                    val progress by _scanProgress.collectAsState()
                    val streak by _scanStreak.collectAsState()
                    val pokemon by _lockedPokemon.collectAsState()
                    val candidates by _candidateList.collectAsState()
                    val forms by _availableForms.collectAsState()
                    val allList by _allPokemon.collectAsState()
                    val isShiny by _isShiny.collectAsState()
                    val showPicker by _showQuickPicker.collectAsState()

                    if (visible) {
                        OverlayScannerAndBattleHud(
                            mode = mode,
                            isScanning = isScanning,
                            scanStatusText = statusText,
                            scanProgress = progress,
                            scanStreak = streak,
                            lockedPokemon = pokemon,
                            candidateList = candidates,
                            availableForms = forms,
                            allPokemon = allList,
                            isShiny = isShiny,
                            showQuickPicker = showPicker,
                            onDragOverlay = { dx, dy ->
                                moveOverlayBy(dx.roundToInt(), dy.roundToInt())
                            },
                            onReticleBoundsUpdated = { cx, cy, sizePx ->
                                reticleCenterScreenX = cx
                                reticleCenterScreenY = cy
                                reticleBoxSizePx = sizePx
                            },
                            onChangeMode = { newMode ->
                                _showQuickPicker.value = false
                                _overlayMode.value = newMode
                            },
                            onTriggerScan = {
                                executeOverlayScan()
                            },
                            onSelectCandidate = { selected ->
                                applyLockedPokemon(selected, _candidateList.value, isShiny = _isShiny.value)
                            },
                            onToggleShiny = {
                                _isShiny.value = !_isShiny.value
                            },
                            onToggleQuickPicker = {
                                _showQuickPicker.value = !_showQuickPicker.value
                            },
                            onSpeakBattleBriefing = { briefing ->
                                tts?.stop()
                                tts?.speak(briefing)
                            },
                            onCloseOverlay = {
                                tts?.stop()
                                hideOverlay()
                            }
                        )
                    }
                }
            }
        }
        composeView = view
        try {
            wm.addView(view, params)
        } catch (e: Exception) {
            android.util.Log.e("PokedexOverlayService", "Failed to add accessibility overlay view", e)
        }
    }

    private fun updateOverlayVisibility(visible: Boolean) {
        composeView?.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun moveOverlayBy(dx: Int, dy: Int) {
        val wm = windowManager ?: return
        val view = composeView ?: return
        val lp = layoutParams ?: return
        val metrics = resources.displayMetrics
        lp.x = (lp.x + dx).coerceIn(0, (metrics.widthPixels - 140).coerceAtLeast(0))
        lp.y = (lp.y + dy).coerceIn(0, (metrics.heightPixels - 140).coerceAtLeast(0))
        try {
            wm.updateViewLayout(view, lp)
        } catch (_: Exception) {}
    }

    private fun setOverlayPosition(x: Int, y: Int) {
        val wm = windowManager ?: return
        val view = composeView ?: return
        val lp = layoutParams ?: return
        val metrics = resources.displayMetrics
        lp.x = x.coerceIn(0, (metrics.widthPixels - 140).coerceAtLeast(0))
        lp.y = y.coerceIn(0, (metrics.heightPixels - 140).coerceAtLeast(0))
        try {
            wm.updateViewLayout(view, lp)
        } catch (_: Exception) {}
    }

    private fun removeOverlayWindow() {
        val wm = windowManager
        val view = composeView
        if (wm != null && view != null) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {}
        }
        composeView = null
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }

    internal fun applyLockedPokemon(
        pokemon: PokemonEntity,
        candidates: List<PokemonEntity>,
        isShiny: Boolean
    ) {
        _lockedPokemon.value = pokemon
        _candidateList.value = if (candidates.isNotEmpty()) candidates else listOf(pokemon)
        _isShiny.value = isShiny
        _showQuickPicker.value = false
        _overlayMode.value = OverlayMode.BATTLE_GUIDE

        serviceScope.launch(Dispatchers.IO) {
            val dao = PokedexDatabase.getDatabase(applicationContext).pokemonDao()
            val baseId = if (pokemon.id <= 1025) pokemon.id else pokemon.id
            val baseEntity = dao.getPokemonById(baseId) ?: pokemon
            val formNames = baseEntity.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val forms = if (formNames.isNotEmpty()) {
                (listOf(baseEntity) + dao.getPokemonByNames(formNames)).distinctBy { it.id }
            } else {
                listOf(pokemon)
            }
            _availableForms.value = forms
        }

        val displayTitle = PokemonFormDetector.formatFormDisplayName(pokemon.name)
        val profile = BattleOverlayCropHelper.computeTacticalBattleProfile(displayTitle, pokemon.type)
        tts?.stop()
        tts?.speak(profile.spokenBriefing)
    }

    private fun extractWindowPokemonHint(allPokemon: List<PokemonEntity>): PokemonEntity? {
        if (allPokemon.isEmpty()) return null
        val collectedTexts = mutableListOf<String>()
        fun traverse(node: AccessibilityNodeInfo?) {
            if (node == null) return
            val pkg = node.packageName?.toString() ?: ""
            // Ignore our own overlay node texts
            if (!pkg.contains("com.example.pokedex")) {
                node.text?.toString()?.takeIf { it.isNotBlank() }?.let { collectedTexts.add(it) }
                node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { collectedTexts.add(it) }
            }
            for (i in 0 until node.childCount) {
                traverse(node.getChild(i))
            }
        }
        try {
            traverse(rootInActiveWindow)
        } catch (_: Exception) {}

        val combined = collectedTexts.joinToString(" ").lowercase()
        if (combined.isBlank()) return null
        return allPokemon.firstOrNull { p ->
            val clean = p.name.lowercase()
            clean.length >= 4 && Regex("\\b${Regex.escape(clean)}\\b").containsMatchIn(combined)
        }
    }

    internal fun executeOverlayScan() {
        if (_isScanning.value) return
        _overlayMode.value = OverlayMode.SCANNER_RETICLE
        _isScanning.value = true
        _scanStatusText.value = "ACQUIRING FREQUENCY..."
        _scanStreak.value = 6
        _scanProgress.value = 0.35f

        serviceScope.launch {
            delay(220)
            _scanStatusText.value = "LOCKING TARGET..."
            _scanStreak.value = 14
            _scanProgress.value = 0.78f
            delay(220)

            // Briefly make overlay transparent so screenshot captures clean game pixels underneath
            composeView?.alpha = 0f
            delay(45)

            val capturedBitmap = captureScreenBitmapSuspend()
            composeView?.alpha = 1f

            _scanStatusText.value = "ANALYZING BATTLE DATA..."
            _scanStreak.value = 18
            _scanProgress.value = 1.0f

            withContext(Dispatchers.IO) {
                val dao = PokedexDatabase.getDatabase(applicationContext).pokemonDao()
                val clf = classifier ?: TFLiteClassifier(applicationContext).also { classifier = it }
                val labels = clf.getLabels()

                val resolvedCandidates = mutableListOf<PokemonEntity>()
                var isShinyDetected = false

                if (capturedBitmap != null) {
                    val sw = capturedBitmap.width
                    val sh = capturedBitmap.height
                    val cx = (forcedReticleX ?: reticleCenterScreenX).coerceIn(0, sw)
                    val cy = (forcedReticleY ?: reticleCenterScreenY).coerceIn(0, sh)
                    forcedReticleX = null
                    forcedReticleY = null
                    val boxPx = reticleBoxSizePx.coerceIn(120, minOf(sw, sh))

                    val r100 = BattleOverlayCropHelper.computeReticleCropBounds(sw, sh, cx, cy, boxPx, 1.0f)
                    val r075 = BattleOverlayCropHelper.computeReticleCropBounds(sw, sh, cx, cy, boxPx, 0.75f)
                    val r125 = BattleOverlayCropHelper.computeReticleCropBounds(sw, sh, cx, cy, boxPx, 1.25f)

                    val crops = listOf(r100, r075, r125).mapNotNull { rect ->
                        runCatching {
                            Bitmap.createBitmap(capturedBitmap, rect.left, rect.top, rect.width, rect.height)
                        }.getOrNull()
                    }

                    val fusedProbs = FloatArray(labels.size)
                    var totalWeight = 0f
                    for (cropBmp in crops) {
                        val probs = clf.getProbabilities(cropBmp)
                        val peak = (probs.maxOrNull() ?: 0.01f).coerceAtLeast(0.05f)
                        totalWeight += peak
                        for (i in probs.indices) {
                            fusedProbs[i] += probs[i] * peak
                        }
                    }
                    if (totalWeight > 0f) {
                        for (i in fusedProbs.indices) {
                            fusedProbs[i] /= totalWeight
                        }
                    }

                    val topIndices = fusedProbs.withIndex()
                        .sortedByDescending { it.value }
                        .take(6)

                    for (cand in topIndices) {
                        val label = labels.getOrNull(cand.index) ?: continue
                        val extracted = nameMatcher.extractName(label) ?: label.lowercase().trim()
                        val entity = dao.getPokemonByName(extracted)
                            ?: dao.getPokemonByName(label.lowercase().trim())
                            ?: dao.getPokemonByName(extracted.substringBefore("-"))
                        if (entity != null && resolvedCandidates.none { it.id == entity.id }) {
                            resolvedCandidates.add(entity)
                        }
                        if (resolvedCandidates.size >= 3) break
                    }

                    val primaryCrop = crops.firstOrNull()
                    val topEntity = resolvedCandidates.firstOrNull()
                    if (primaryCrop != null && topEntity != null) {
                        val formNames = topEntity.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val formEntities = if (formNames.isNotEmpty()) dao.getPokemonByNames(formNames) else emptyList()
                        val disambig = PokemonFormDetector.disambiguate(primaryCrop, topEntity, formEntities)
                        isShinyDetected = disambig.isShiny
                        if (!disambig.resolvedFormName.equals(topEntity.name, ignoreCase = true)) {
                            val matchedForm = formEntities.firstOrNull { it.name.equals(disambig.resolvedFormName, ignoreCase = true) }
                            if (matchedForm != null) {
                                resolvedCandidates[0] = matchedForm
                            }
                        }
                    }
                }

                // Resolve candidates with ambient window hint (visual is PRIMARY)
                val hintPokemon = extractWindowPokemonHint(_allPokemon.value)
                val finalCandidates = BattleOverlayCropHelper.resolveCandidatesWithHint(resolvedCandidates, hintPokemon).toMutableList()

                if (finalCandidates.isEmpty()) {
                    val fallback = _lockedPokemon.value
                        ?: dao.getPokemonById(6)
                        ?: dao.getFirstPokemon()
                    if (fallback != null) {
                        finalCandidates.add(fallback)
                    }
                }

                withContext(Dispatchers.Main) {
                    _isScanning.value = false
                    _scanStreak.value = 0
                    _scanProgress.value = 0f
                    _scanStatusText.value = "DRAG OVER POKÉMON & TAP SCAN"
                    val winner = finalCandidates.firstOrNull()
                    if (winner != null) {
                        applyLockedPokemon(winner, finalCandidates.take(3), isShiny = isShinyDetected)
                    }
                }
            }
        }
    }

    private suspend fun captureScreenBitmapSuspend(): Bitmap? = suspendCancellableCoroutine { cont ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            cont.resume(null) {}
            return@suspendCancellableCoroutine
        }
        try {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        try {
                            val hwBuffer = screenshot.hardwareBuffer
                            val colorSpace = screenshot.colorSpace ?: ColorSpace.get(ColorSpace.Named.SRGB)
                            val hwBitmap = Bitmap.wrapHardwareBuffer(hwBuffer, colorSpace)
                            val swBitmap = hwBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                            hwBitmap?.recycle()
                            hwBuffer.close()
                            if (cont.isActive) cont.resume(swBitmap) {}
                        } catch (e: Exception) {
                            android.util.Log.e("PokedexOverlayService", "Failed wrapping screenshot buffer", e)
                            if (cont.isActive) cont.resume(null) {}
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        android.util.Log.w("PokedexOverlayService", "takeScreenshot failed with code=$errorCode")
                        if (cont.isActive) cont.resume(null) {}
                    }
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("PokedexOverlayService", "Exception calling takeScreenshot", e)
            if (cont.isActive) cont.resume(null) {}
        }
    }
}

@Composable
fun OverlayScannerAndBattleHud(
    mode: OverlayMode,
    isScanning: Boolean,
    scanStatusText: String,
    scanProgress: Float,
    scanStreak: Int,
    lockedPokemon: PokemonEntity?,
    candidateList: List<PokemonEntity>,
    availableForms: List<PokemonEntity>,
    allPokemon: List<PokemonEntity>,
    isShiny: Boolean,
    showQuickPicker: Boolean,
    onDragOverlay: (Float, Float) -> Unit,
    onReticleBoundsUpdated: (Int, Int, Int) -> Unit,
    onChangeMode: (OverlayMode) -> Unit,
    onTriggerScan: () -> Unit,
    onSelectCandidate: (PokemonEntity) -> Unit,
    onToggleShiny: () -> Unit,
    onToggleQuickPicker: () -> Unit,
    onSpeakBattleBriefing: (String) -> Unit,
    onCloseOverlay: () -> Unit
) {
    when (mode) {
        OverlayMode.MINI_LENS -> {
            val infiniteTransition = rememberInfiniteTransition(label = "mini_lens_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lens_scale"
            )
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDragOverlay(dragAmount.x, dragAmount.y)
                        }
                    }
                    .clickable { onChangeMode(OverlayMode.SCANNER_RETICLE) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(Color(0xFFCC0000).copy(alpha = 0.92f), CircleShape)
                        .border(2.5.dp, Color.White, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF80EAFF), Color(0xFF00BFFF), Color(0xFF005588))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFF00FFCC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEX",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        OverlayMode.SCANNER_RETICLE -> {
            val reticleColor = when {
                scanProgress >= 1.0f -> Color(0xFF00FF66)
                isScanning -> Color(0xFFFF9900)
                else -> Color(0xFF00FFCC)
            }

            val infiniteTransition = rememberInfiniteTransition(label = "overlay_scanner_anim")
            val laserPos by infiniteTransition.animateFloat(
                initialValue = 0.06f,
                targetValue = 0.94f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = if (isScanning) 750 else 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "overlay_laser"
            )
            val baseRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = if (isScanning) 1400 else 5200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "overlay_radar_rot"
            )
            val innerRingPulse by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 0.86f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = if (isScanning) 240 else 750, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "overlay_inner_pulse"
            )
            val corePulse by infiniteTransition.animateFloat(
                initialValue = 0.82f,
                targetValue = 1.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = if (isScanning) 200 else 850, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "overlay_core_pulse"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(248.dp)
            ) {
                // Top Drag & Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10161B).copy(alpha = 0.92f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .border(1.5.dp, reticleColor.copy(alpha = 0.8f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onDragOverlay(dragAmount.x, dragAmount.y)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✥ BORI-DEX OVERLAY",
                        color = reticleColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (lockedPokemon != null) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00FFCC).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(4.dp))
                                    .clickable { onChangeMode(OverlayMode.BATTLE_GUIDE) }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("⚔️ GUIDE", color = Color(0xFF00FFCC), fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                .clickable { onChangeMode(OverlayMode.MINI_LENS) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("—", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color(0xFFB71C1C).copy(alpha = 0.85f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable { onCloseOverlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Center Transparent Animated Scanner Reticle (Same animation as ActiveScannerHud)
                Box(
                    modifier = Modifier
                        .size(224.dp)
                        .background(Color.Black.copy(alpha = 0.14f))
                        .border(1.5.dp, reticleColor.copy(alpha = 0.55f))
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionOnScreen()
                            val w = coords.size.width
                            val h = coords.size.height
                            onReticleBoundsUpdated(
                                (pos.x + w / 2f).roundToInt(),
                                (pos.y + h / 2f).roundToInt(),
                                minOf(w, h)
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onDragOverlay(dragAmount.x, dragAmount.y)
                            }
                        }
                        .clickable { onTriggerScan() }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w / 2f, h / 2f)
                        val outerRadius = w / 2f * 0.82f
                        val pokeRadius = w / 2f * 0.70f
                        val buttonRadius = 15.dp.toPx()
                        val innerButtonRadius = 8.5.dp.toPx()

                        val inset = 6.dp.toPx()
                        val bracketLen = 22.dp.toPx()
                        val bracketStroke = 2.5.dp.toPx()
                        val bracketColor = reticleColor.copy(alpha = 0.90f)
                        val cornerRadius = 8.dp.toPx()
                        val bracketStyle = Stroke(width = bracketStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

                        // 4 Rounded Sci-Fi Corner Brackets
                        val pathTL = Path().apply {
                            moveTo(inset, inset + bracketLen)
                            lineTo(inset, inset + cornerRadius)
                            arcTo(Rect(inset, inset, inset + 2 * cornerRadius, inset + 2 * cornerRadius), 180f, 90f, false)
                            lineTo(inset + bracketLen, inset)
                        }
                        drawPath(pathTL, bracketColor, style = bracketStyle)

                        val pathTR = Path().apply {
                            moveTo(w - inset - bracketLen, inset)
                            lineTo(w - inset - cornerRadius, inset)
                            arcTo(Rect(w - inset - 2 * cornerRadius, inset, w - inset, inset + 2 * cornerRadius), 270f, 90f, false)
                            lineTo(w - inset, inset + bracketLen)
                        }
                        drawPath(pathTR, bracketColor, style = bracketStyle)

                        val pathBR = Path().apply {
                            moveTo(w - inset, h - inset - bracketLen)
                            lineTo(w - inset, h - inset - cornerRadius)
                            arcTo(Rect(w - inset - 2 * cornerRadius, h - inset - 2 * cornerRadius, w - inset, h - inset), 0f, 90f, false)
                            lineTo(w - inset - bracketLen, h - inset)
                        }
                        drawPath(pathBR, bracketColor, style = bracketStyle)

                        val pathBL = Path().apply {
                            moveTo(inset + bracketLen, h - inset)
                            lineTo(inset + cornerRadius, h - inset)
                            arcTo(Rect(inset, h - inset - 2 * cornerRadius, inset + 2 * cornerRadius, h - inset), 90f, 90f, false)
                            lineTo(inset, h - inset - bracketLen)
                        }
                        drawPath(pathBL, bracketColor, style = bracketStyle)

                        // Invizimals 8-Segment Outer Rotating Radar Ring
                        rotate(degrees = baseRotation, pivot = center) {
                            val segments = 8
                            val sweepAngle = 24f
                            for (i in 0 until segments) {
                                drawArc(
                                    color = reticleColor.copy(alpha = 0.55f),
                                    startAngle = i * (360f / segments),
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                                    size = Size(outerRadius * 2, outerRadius * 2),
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Contracting Dashed Inner Ring
                        val dynamicInnerRadius = pokeRadius * innerRingPulse
                        rotate(degrees = -baseRotation * 1.5f, pivot = center) {
                            drawCircle(
                                color = reticleColor.copy(alpha = 0.40f),
                                radius = dynamicInnerRadius,
                                center = center,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        }

                        // Poké Ball Outer Circular Frame & Equator
                        drawCircle(color = reticleColor, radius = pokeRadius, center = center, style = Stroke(width = 2.2.dp.toPx()))
                        val gap = buttonRadius * 1.25f
                        drawLine(reticleColor, Offset(center.x - pokeRadius, center.y), Offset(center.x - gap, center.y), strokeWidth = 2.2.dp.toPx())
                        drawLine(reticleColor, Offset(center.x + gap, center.y), Offset(center.x + pokeRadius, center.y), strokeWidth = 2.2.dp.toPx())

                        // Center Core Button with Dynamic Pulse
                        drawCircle(color = reticleColor, radius = buttonRadius, center = center, style = Stroke(width = 2.dp.toPx()))
                        val animatedCore = innerButtonRadius * corePulse
                        drawCircle(color = reticleColor.copy(alpha = 0.28f), radius = animatedCore * 1.35f, center = center)
                        drawCircle(color = reticleColor, radius = animatedCore, center = center)
                        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = animatedCore * 0.45f, center = center)

                        // Chaotic Laser Scanline Sweep
                        val laserY = h * laserPos
                        val scanlineGradient = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                reticleColor.copy(alpha = 0.55f),
                                reticleColor,
                                reticleColor.copy(alpha = 0.55f),
                                Color.Transparent
                            ),
                            startY = laserY - 12.dp.toPx(),
                            endY = laserY + 12.dp.toPx()
                        )
                        val clipCircle = Path().apply {
                            addOval(Rect(center.x - pokeRadius, center.y - pokeRadius, center.x + pokeRadius, center.y + pokeRadius))
                        }
                        clipPath(clipCircle) {
                            drawRect(
                                brush = scanlineGradient,
                                topLeft = Offset(0f, laserY - 10.dp.toPx()),
                                size = Size(w, 20.dp.toPx())
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.90f),
                                start = Offset(center.x - pokeRadius, laserY),
                                end = Offset(center.x + pokeRadius, laserY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }

                    // Corner Telemetry Readouts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SYS: OVERLAY 894MHz",
                            color = reticleColor,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isScanning) "LOCK: [$scanStreak/18]" else "RADAR: READY",
                            color = reticleColor,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = scanStatusText,
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom Action Bar: SCAN POKÉMON + PICK FROM DEX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10161B).copy(alpha = 0.95f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .border(1.5.dp, reticleColor.copy(alpha = 0.8f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTriggerScan,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isScanning) "⚡ SCANNING..." else "🎯 SCAN POKÉMON",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            onChangeMode(OverlayMode.BATTLE_GUIDE)
                            onToggleQuickPicker()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "🔍 DEX",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
        }

        OverlayMode.BATTLE_GUIDE -> {
            val activePokemon = lockedPokemon ?: allPokemon.firstOrNull()
            val displayName = activePokemon?.let { PokemonFormDetector.formatFormDisplayName(it.name) } ?: "Charizard"
            val typeStr = activePokemon?.type ?: "Fire/Flying"
            val battleProfile = remember(displayName, typeStr) {
                BattleOverlayCropHelper.computeTacticalBattleProfile(displayName, typeStr)
            }

            Surface(
                modifier = Modifier.width(330.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF10161B).copy(alpha = 0.96f),
                border = BorderStroke(2.dp, Color(0xFF00FFCC).copy(alpha = 0.90f)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // 1. Draggable Top Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDragOverlay(dragAmount.x, dragAmount.y)
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚔️ BORI-DEX BATTLE GUIDE",
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00FFCC), RoundedCornerShape(6.dp))
                                    .clickable { onChangeMode(OverlayMode.SCANNER_RETICLE) }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text("🎯 RESCAN", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .background(if (showQuickPicker) Color(0xFFFFCC00) else Color(0xFF263238), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFFFCC00), RoundedCornerShape(6.dp))
                                    .clickable { onToggleQuickPicker() }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🔍 PICK",
                                    color = if (showQuickPicker) Color.Black else Color(0xFFFFCC00),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    .clickable { onChangeMode(OverlayMode.MINI_LENS) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("—", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFFB71C1C).copy(alpha = 0.85f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onCloseOverlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Optional In-Overlay Quick Pokémon Picker (by Generation / Type)
                    if (showQuickPicker && allPokemon.isNotEmpty()) {
                        var selectedGenRange by remember { mutableStateOf(1..151) }
                        val genRanges = listOf(
                            "G1" to (1..151),
                            "G2" to (152..251),
                            "G3" to (252..386),
                            "G4" to (387..493),
                            "G5" to (494..649),
                            "G6-9" to (650..1025)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                genRanges.forEach { (label, range) ->
                                    val active = selectedGenRange == range
                                    Box(
                                        modifier = Modifier
                                            .background(if (active) Color(0xFFFFCC00) else Color(0xFF222222), RoundedCornerShape(5.dp))
                                            .clickable { selectedGenRange = range }
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (active) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val filtered = remember(allPokemon, selectedGenRange) {
                                allPokemon.filter { it.id in selectedGenRange }
                            }
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filtered, key = { it.id }) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectCandidate(item) }
                                            .padding(vertical = 3.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${item.id.toString().padStart(3, '0')} ${PokemonFormDetector.formatFormDisplayName(item.name)}",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.type.uppercase(),
                                            color = Color(0xFF00FFCC),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // 2. Scanned Pokémon Identity Banner + Type Badges + Audio Briefing
                    if (activePokemon != null) {
                        val spriteModel = getPokemonSpriteModel(activePokemon, isShiny)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(getColorForType(activePokemon.type).copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                                .border(1.dp, getColorForType(activePokemon.type).copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .clickable { onToggleShiny() },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = spriteModel,
                                    contentDescription = displayName,
                                    modifier = Modifier.size(48.dp)
                                )
                                if (isShiny) {
                                    Text(
                                        text = "✨",
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${activePokemon.id.toString().padStart(3, '0')}",
                                        color = Color(0xFF00FFCC),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = displayName.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Official Elemental Type Badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    battleProfile.types.forEach { tName ->
                                        OverlayTypeChip(type = tName, multiplier = null, isQuad = false)
                                    }
                                }
                            }

                            // Dexter TTS Battle Briefing Button
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00FFCC), RoundedCornerShape(8.dp))
                                    .clickable { onSpeakBattleBriefing(battleProfile.spokenBriefing) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔊 READ",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }

                    // 3. Top-3 Vision Candidates / Regional & Mega Forms Switcher
                    val chipTargets = remember(candidateList, availableForms) {
                        (candidateList + availableForms).distinctBy { it.id }
                    }
                    if (chipTargets.size > 1) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            chipTargets.forEach { cand ->
                                val selected = cand.id == activePokemon?.id
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selected) Color(0xFF00FFCC).copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.6f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.25f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onSelectCandidate(cand) }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = PokemonFormDetector.formatFormDisplayName(cand.name).uppercase(),
                                        color = if (selected) Color(0xFF00FFCC) else Color.LightGray,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Tactical Combat Breakdown (Scrollable if needed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 210.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // A. HIT IT WITH (4x and 2x Weaknesses)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "🎯 HIT IT WITH (WEAK TO 4× / 2×):",
                                color = Color(0xFFFF7777),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (battleProfile.quadWeaknesses.isEmpty() && battleProfile.doubleWeaknesses.isEmpty()) {
                                    Text("NONE (1× NEUTRAL)", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    battleProfile.quadWeaknesses.forEach { t ->
                                        OverlayTypeChip(type = t, multiplier = "4×", isQuad = true)
                                    }
                                    battleProfile.doubleWeaknesses.forEach { t ->
                                        OverlayTypeChip(type = t, multiplier = "2×", isQuad = false)
                                    }
                                }
                            }
                        }

                        // B. WATCH OUT FOR (Enemy STAB Offensive Coverage 2x)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "⚡ STRONG TO (ITS STAB HITS 2×):",
                                color = Color(0xFF00FFCC),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (battleProfile.stabStrengths.isEmpty()) {
                                    Text("NONE (NORMAL 1× STAB)", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    battleProfile.stabStrengths.forEach { t ->
                                        OverlayTypeChip(type = t, multiplier = "2×", isQuad = false)
                                    }
                                }
                            }
                        }

                        // C. AVOID USING (Immunities 0x & Resistances 0.5x/0.25x)
                        if (battleProfile.immunities.isNotEmpty() || battleProfile.resistances.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "🛡️ AVOID USING (IMMUNE 0× / RESISTS ½×):",
                                    color = Color(0xFFFFCC00),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    battleProfile.immunities.forEach { t ->
                                        OverlayTypeChip(type = t, multiplier = "0×", isQuad = false)
                                    }
                                    battleProfile.resistances.forEach { t ->
                                        OverlayTypeChip(type = t, multiplier = "½×", isQuad = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayTypeChip(
    type: String,
    multiplier: String?,
    isQuad: Boolean
) {
    val bgColor = Color(PokemonTypeEffectiveness.getTypeColor(type))
    val txtColor = Color(PokemonTypeEffectiveness.getTypeTextColor(type))
    val borderCol = if (isQuad) Color(0xFFFFEA00) else Color.White.copy(alpha = 0.45f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(5.dp))
            .border(if (isQuad) 1.5.dp else 0.8.dp, borderCol, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = type.uppercase(),
            color = txtColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
        if (!multiplier.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(3.dp))
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = multiplier,
                    color = if (isQuad) Color(0xFFFFEA00) else Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
