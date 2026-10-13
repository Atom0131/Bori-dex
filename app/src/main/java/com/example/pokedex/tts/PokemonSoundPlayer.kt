package com.example.pokedex.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.pokedex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object PokemonSoundPlayer {
    private const val TAG = "PokemonSoundPlayer"
    private var activePlayer: MediaPlayer? = null

    /**
     * Plays the authentic 1997 4Kids anime "Who's That Pokémon?!" fanfare and kids' shout.
     */
    fun playWhosThatPokemon(context: Context, onCompletion: (() -> Unit)? = null) {
        stop()
        try {
            val player = MediaPlayer.create(context, R.raw.whos_that_pokemon) ?: return
            activePlayer = player
            player.setOnCompletionListener {
                player.release()
                if (activePlayer == player) activePlayer = null
                onCompletion?.invoke()
            }
            player.setOnErrorListener { _, _, _ ->
                player.release()
                if (activePlayer == player) activePlayer = null
                onCompletion?.invoke()
                true
            }
            player.start()
            Log.d(TAG, "Playing authentic Who's That Pokemon fanfare SFX")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing whos_that_pokemon: ${e.message}", e)
            onCompletion?.invoke()
        }
    }

    /**
     * Plays the official Pokémon cry (Gen 1-9) cached from the PokéAPI repository.
     */
    suspend fun playPokemonCry(context: Context, pokemonId: Int, onCompletion: (() -> Unit)? = null) {
        withContext(Dispatchers.IO) {
            val criesDir = File(context.cacheDir, "cries")
            if (!criesDir.exists()) criesDir.mkdirs()
            val cryFile = File(criesDir, "$pokemonId.ogg")

            // Download and cache if missing or empty
            if (!cryFile.exists() || cryFile.length() < 100) {
                try {
                    val cryUrl = "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/$pokemonId.ogg"
                    val url = URL(cryUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.instanceFollowRedirects = true
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        conn.inputStream.use { input ->
                            FileOutputStream(cryFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.d(TAG, "Downloaded cry for #$pokemonId (${cryFile.length()} bytes)")
                    } else {
                        Log.w(TAG, "Failed to download cry for #$pokemonId: HTTP ${conn.responseCode}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error caching cry for #$pokemonId: ${e.message}")
                }
            }

            if (cryFile.exists() && cryFile.length() > 100) {
                withContext(Dispatchers.Main) {
                    stop()
                    try {
                        val player = MediaPlayer().apply {
                            setDataSource(cryFile.absolutePath)
                            setOnPreparedListener { start() }
                            setOnCompletionListener {
                                release()
                                if (activePlayer == this) activePlayer = null
                                onCompletion?.invoke()
                            }
                            setOnErrorListener { _, _, _ ->
                                release()
                                if (activePlayer == this) activePlayer = null
                                onCompletion?.invoke()
                                true
                            }
                            prepareAsync()
                        }
                        activePlayer = player
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing cry file: ${e.message}", e)
                        onCompletion?.invoke()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onCompletion?.invoke()
                }
            }
        }
    }

    fun stop() {
        try {
            activePlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sound player: ${e.message}", e)
        } finally {
            activePlayer = null
        }
    }
}
