package com.example.pokedex.tts

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

data class VoiceItem(
    val name: String,
    val displayName: String,
    val accentName: String,
    val isMale: Boolean,
    val locale: String
)

class OfflineTTS(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var currentSpeed: Float = 1.0f
    private var currentPitch: Float = 0.90f
    private var currentProfile: VoiceProfile = VoiceProfile.ORIGINAL_90S_MALE
    private var customVoiceName: String? = null
    private var dspEnabled: Boolean = true

    private var mediaPlayer: MediaPlayer? = null
    private var activeUtteranceId: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dspExecutor = Executors.newSingleThreadExecutor()

    private val rawWavFile = File(context.cacheDir, "dexter_raw.wav")
    private val processedWavFile = File(context.cacheDir, "dexter_processed.wav")

    var onSpeakingStateChanged: ((Boolean) -> Unit)? = null
    var onVoicesLoaded: ((List<VoiceItem>) -> Unit)? = null

    private val _availableVoices = mutableListOf<VoiceItem>()
    val availableVoices: List<VoiceItem> get() = _availableVoices

    init {
        PokemonPronunciation.init(context)
        val googleEngine = "com.google.android.tts"
        tts = try {
            TextToSpeech(context, this, googleEngine)
        } catch (e: Exception) {
            TextToSpeech(context, this)
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId != null && !utteranceId.startsWith("DEXTER_FILE_")) {
                    mainHandler.post { onSpeakingStateChanged?.invoke(true) }
                }
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId != null && utteranceId.startsWith("DEXTER_FILE_")) {
                    if (utteranceId != activeUtteranceId) return

                    val currentRawFile = File(context.cacheDir, "${utteranceId}_raw.wav")
                    val currentProcessedFile = File(context.cacheDir, "${utteranceId}_proc.wav")

                    dspExecutor.execute {
                        val dspSuccess = if (dspEnabled && currentRawFile.exists()) {
                            DexterAudioDsp.processWav(currentRawFile, currentProcessedFile)
                        } else {
                            false
                        }

                        val fileToPlay = if (dspSuccess && currentProcessedFile.exists() && currentProcessedFile.length() > 44) {
                            currentProcessedFile
                        } else {
                            currentRawFile
                        }

                        mainHandler.post {
                            if (utteranceId == activeUtteranceId && fileToPlay.exists()) {
                                playWavFile(fileToPlay)
                            } else {
                                currentRawFile.delete()
                                currentProcessedFile.delete()
                            }
                        }
                    }
                } else {
                    mainHandler.post { onSpeakingStateChanged?.invoke(false) }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post { onSpeakingStateChanged?.invoke(false) }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            loadAvailableVoices()
            applyConfiguration()
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        }
    }

    private fun isVoiceMale(voice: Voice): Boolean {
        val name = voice.name.lowercase()
        val features = voice.features?.map { it.lowercase() } ?: emptyList()
        if (features.any { it.contains("gender=male") || it == "male" }) return true
        if (features.any { it.contains("gender=female") || it == "female" }) return false
        // Common male voice markers in Google & Samsung TTS engines
        if (name.contains("male") || name.contains("guy") || name.contains("man") || name.contains("#m")) return true
        if (name.contains("tpd") || name.contains("iom") || name.contains("rjs") || name.contains("cfl")) return true
        return false
    }

    private fun isVoiceFemale(voice: Voice): Boolean {
        val name = voice.name.lowercase()
        val features = voice.features?.map { it.lowercase() } ?: emptyList()
        if (features.any { it.contains("gender=female") || it == "female" }) return true
        if (features.any { it.contains("gender=male") || it == "male" }) return false
        if (name.contains("female") || name.contains("girl") || name.contains("woman") || name.contains("#f")) return true
        if (name.contains("sfg") || name.contains("iol") || name.contains("gba")) return true
        return false
    }

    private fun loadAvailableVoices() {
        val allVoices = tts?.voices ?: emptySet()
        _availableVoices.clear()
        
        // Filter English or primary language voices for better UX
        val englishVoices = allVoices.filter { it.locale.language == "en" }
            .sortedWith(compareBy({ it.locale.displayCountry.ifBlank { it.locale.country } }, { it.name }))

        englishVoices.forEach { voice ->
            val male = isVoiceMale(voice)
            val female = isVoiceFemale(voice)
            val genderTag = when {
                male -> "Male"
                female -> "Female"
                else -> "Neutral"
            }
            val country = voice.locale.displayCountry.takeIf { it.isNotBlank() } ?: voice.locale.country
            val accentTag = when (voice.locale.country.uppercase()) {
                "US" -> "American (US)"
                "GB" -> "British (UK)"
                "AU" -> "Australian (AU)"
                "IN" -> "Indian (IN)"
                "CA" -> "Canadian (CA)"
                "IE" -> "Irish (IE)"
                "ZA" -> "South African (ZA)"
                "NZ" -> "New Zealand (NZ)"
                else -> if (country.isNotBlank()) "$country (${voice.locale.country})" else "English"
            }
            val cleanName = voice.name.replace("en-", "").replace("-local", "").replace("-network", "")
            _availableVoices.add(
                VoiceItem(
                    name = voice.name,
                    displayName = "$accentTag: $cleanName ($genderTag)",
                    accentName = accentTag,
                    isMale = male,
                    locale = voice.locale.toString()
                )
            )
        }
        mainHandler.post { onVoicesLoaded?.invoke(_availableVoices) }
    }

    fun configure(
        profile: VoiceProfile,
        customVoice: String?,
        dsp: Boolean,
        pitch: Float,
        speed: Float
    ) {
        currentProfile = profile
        customVoiceName = customVoice
        dspEnabled = dsp
        currentPitch = pitch
        currentSpeed = speed

        if (isInitialized) {
            applyConfiguration()
        }
    }

    private fun applyConfiguration() {
        tts?.setSpeechRate(currentSpeed)
        tts?.setPitch(currentPitch)

        val voices = tts?.voices ?: emptySet()
        val targetVoice: Voice? = when (currentProfile) {
            VoiceProfile.ORIGINAL_90S_MALE -> {
                // Priority 1: US English tenor/crisp male voice (iom or cfl), falling back to tpd
                val usMales = voices.filter { it.locale.language == "en" && it.locale.country == "US" && isVoiceMale(it) }
                usMales.firstOrNull { it.name.contains("iom") }
                    ?: usMales.firstOrNull { it.name.contains("cfl") }
                    ?: usMales.firstOrNull { it.name.contains("tpd") }
                    ?: usMales.firstOrNull()
                    ?: voices.firstOrNull { it.locale.language == "en" && it.locale.country == "US" && !isVoiceFemale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && isVoiceMale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && !isVoiceFemale(it) }
            }
            VoiceProfile.HIGH_END_MALE -> {
                voices.firstOrNull { it.locale.language == "en" && it.locale.country == "US" && isVoiceMale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && isVoiceMale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && !isVoiceFemale(it) }
            }
            VoiceProfile.HIGH_END_FEMALE, VoiceProfile.RETRO_FEMALE_90S -> {
                voices.firstOrNull { it.locale.language == "en" && it.locale.country == "US" && isVoiceFemale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && isVoiceFemale(it) }
                    ?: voices.firstOrNull { it.locale.language == "en" && !isVoiceMale(it) }
            }
            VoiceProfile.CUSTOM -> {
                if (!customVoiceName.isNullOrEmpty()) {
                    voices.firstOrNull { it.name == customVoiceName }
                } else null
            }
        }

        if (targetVoice != null) {
            tts?.voice = targetVoice
            android.util.Log.d("OfflineTTS", "Configured voice: ${targetVoice.name} (profile: ${currentProfile.name})")
        } else {
            tts?.setLanguage(Locale.US)
            android.util.Log.d("OfflineTTS", "Configured fallback Locale.US (profile: ${currentProfile.name})")
        }
    }

    fun speak(text: String) {
        val formattedText = PokemonPronunciation.formatForSpeech(text)
        android.util.Log.d("OfflineTTS", "Speaking -> raw: '$text' | formatted: '$formattedText'")

        stop()

        if (!isInitialized) {
            pendingText = formattedText
            return
        }

        val utteranceId = "DEXTER_FILE_${System.currentTimeMillis()}"
        activeUtteranceId = utteranceId

        if (dspEnabled) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            val utteranceRawFile = File(context.cacheDir, "${utteranceId}_raw.wav")
            val synthResult = tts?.synthesizeToFile(formattedText, params, utteranceRawFile, utteranceId)
            if (synthResult != TextToSpeech.SUCCESS) {
                android.util.Log.w("OfflineTTS", "synthesizeToFile returned $synthResult, falling back to direct tts.speak")
                val fallbackId = "DEXTER_FALLBACK_${System.currentTimeMillis()}"
                activeUtteranceId = fallbackId
                tts?.speak(formattedText, TextToSpeech.QUEUE_FLUSH, null, fallbackId)
            }
        } else {
            // Direct playback for high-end voices without DSP overhead
            val directId = "DEXTER_DIRECT_${System.currentTimeMillis()}"
            activeUtteranceId = directId
            tts?.speak(formattedText, TextToSpeech.QUEUE_FLUSH, null, directId)
        }
    }

    private fun playWavFile(file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    onSpeakingStateChanged?.invoke(true)
                    start()
                }
                setOnCompletionListener {
                    onSpeakingStateChanged?.invoke(false)
                    try { file.delete() } catch (_: Exception) {}
                }
                setOnErrorListener { _, _, _ ->
                    onSpeakingStateChanged?.invoke(false)
                    try { file.delete() } catch (_: Exception) {}
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("OfflineTTS", "MediaPlayer playback error: ${e.message}", e)
            onSpeakingStateChanged?.invoke(false)
        }
    }

    fun stop() {
        activeUtteranceId = null
        try {
            tts?.stop()
        } catch (e: Exception) {}

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {}

        onSpeakingStateChanged?.invoke(false)
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (e: Exception) {}
        dspExecutor.shutdown()
    }
}
