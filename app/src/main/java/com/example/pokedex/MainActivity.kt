package com.example.pokedex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokedex.data.PokemonMapHabitatRepository
import com.example.pokedex.ui.PokedexScreen
import com.example.pokedex.viewmodel.PokedexViewModel

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    
    private val darkColors = darkColorScheme(
        surface = Color(0xFF222222),
        onSurface = Color.White,
        surfaceContainer = Color(0xFF222222),
        surfaceContainerHigh = Color(0xFF2C2C2C),
        surfaceContainerHighest = Color(0xFF333333)
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setContent {
                MaterialTheme(colorScheme = darkColors) {
                    val viewModel: PokedexViewModel = viewModel()
                    PokedexScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PokemonMapHabitatRepository.init(applicationContext)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setContent {
                MaterialTheme(colorScheme = darkColors) {
                    val viewModel: PokedexViewModel = viewModel()
                    PokedexScreen(viewModel = viewModel)
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
