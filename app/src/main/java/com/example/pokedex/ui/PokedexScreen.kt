package com.example.pokedex.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.border


import androidx.compose.foundation.Image
import android.graphics.Bitmap


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import coil.compose.AsyncImage
import com.example.pokedex.data.PokemonEntity
import com.example.pokedex.data.PokemonRegion
import com.example.pokedex.tts.VoiceProfile
import com.example.pokedex.viewmodel.PokedexState
import com.example.pokedex.viewmodel.PokedexViewModel

fun getColorForType(type: String): Color {
    val typeStr = type.lowercase()
    return when {
        typeStr.contains("fire") -> Color(0xFFF08030)
        typeStr.contains("water") -> Color(0xFF6890F0)
        typeStr.contains("grass") -> Color(0xFF78C850)
        typeStr.contains("electric") -> Color(0xFFF8D030)
        typeStr.contains("psychic") -> Color(0xFFF85888)
        typeStr.contains("ice") -> Color(0xFF98D8D8)
        typeStr.contains("dragon") -> Color(0xFF7038F8)
        typeStr.contains("dark") -> Color(0xFF705848)
        typeStr.contains("fairy") -> Color(0xFFEE99AC)
        typeStr.contains("poison") -> Color(0xFFA040A0)
        typeStr.contains("bug") -> Color(0xFFA8B820)
        typeStr.contains("rock") -> Color(0xFFB8A038)
        typeStr.contains("ground") -> Color(0xFFE0C068)
        typeStr.contains("ghost") -> Color(0xFF705898)
        typeStr.contains("steel") -> Color(0xFFB8B8D0)
        typeStr.contains("fighting") -> Color(0xFFC03028)
        typeStr.contains("flying") -> Color(0xFFA890F0)
        else -> Color(0xFFA8A878) // Normal
    }
}

@Composable
fun PokedexHeader(viewModel: PokedexViewModel, onOpenSettings: () -> Unit, onGalleryClick: () -> Unit) {
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main Lens / Camera Button
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White, shape = CircleShape)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF00BFFF), shape = CircleShape)
                    .clickable { viewModel.startScanning() }
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Status Lights
        Row(modifier = Modifier.padding(top = 8.dp)) {
            val redAlpha = if (isSpeaking) alpha else 1.0f
            val yellowAlpha = if (isSpeaking) (1.3f - alpha).coerceIn(0.3f, 1.0f) else 1.0f
            val greenAlpha = if (isSpeaking) alpha else 1.0f

            Box(modifier = Modifier.size(24.dp).background(Color.Red.copy(alpha = redAlpha), shape = CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp).background(Color.Yellow.copy(alpha = yellowAlpha), shape = CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp).background(Color.Green.copy(alpha = greenAlpha), shape = CircleShape).clickable { onGalleryClick() })
        }
        Spacer(modifier = Modifier.weight(1f))
        
        // Settings Button
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("SETTINGS", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PokedexControls(uiState: PokedexState, viewModel: PokedexViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.Black, shape = CircleShape)
                .clickable { 
                    viewModel.resetState()
                },
            contentAlignment = Alignment.Center
        ) {
            Text("RESET", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexList(
    pokemonList: List<PokemonEntity>,
    searchQuery: String,
    viewModel: PokedexViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
) {
    val selectedRegion by viewModel.selectedRegion.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "BORI-DEX INDEX",
            color = Color(0xFF00FFCC),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("SEARCH BY NAME, TYPE, OR NUMBER...", color = Color.Gray, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00FFCC),
                unfocusedBorderColor = Color.DarkGray
            ),
            singleLine = true
        )

        // Region Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isAllSelected = selectedRegion == null
            FilterChip(
                selected = isAllSelected,
                onClick = { viewModel.selectRegion(null) },
                label = { Text("ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00FFCC),
                    selectedLabelColor = Color.Black,
                    containerColor = Color.DarkGray,
                    labelColor = Color.White
                )
            )

            PokemonRegion.entries.forEach { region ->
                val isSelected = selectedRegion == region
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) viewModel.selectRegion(null)
                        else viewModel.selectRegion(region)
                    },
                    label = { Text(region.displayName, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00FFCC),
                        selectedLabelColor = Color.Black,
                        containerColor = Color.DarkGray,
                        labelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(pokemonList, key = { it.id }) { pokemon ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPokemon(pokemon) }
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = getColorForType(pokemon.type)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        AsyncImage(
                            model = pokemon.spriteUrl,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "NO. ${pokemon.id.toString().padStart(3, '0')}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val displayName = pokemon.name.replace("-", " ")
                                .split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokedexDisplay(
    uiState: PokedexState,
    pokemonList: List<PokemonEntity>,
    searchQuery: String,
    viewModel: PokedexViewModel,
    onGalleryClick: () -> Unit,
    isListOnly: Boolean = false,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanStatusText by viewModel.scanStatusText.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isListOnly) {
            PokedexList(pokemonList, searchQuery, viewModel, listState)
        } else {
            when (val state = uiState) {
                is PokedexState.Idle -> {
                    PokedexList(pokemonList, searchQuery, viewModel, listState)
                }
                is PokedexState.LoadingModel -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "MODEL LOADING... PLEASE WAIT",
                            color = Color(0xFF00FFCC),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color(0xFF00FFCC))
                    }
                }
                is PokedexState.Scanning -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.example.pokedex.camera.CameraPreview(
                            onFrame = { bitmap -> viewModel.onImageAnalyzed(bitmap) },
                            modifier = Modifier.fillMaxSize()
                        )
                        // Reticle
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .align(Alignment.Center)
                                .background(Color(0x1500FFCC), shape = RoundedCornerShape(16.dp))
                                .border(3.dp, Color(0xFF00FFCC), RoundedCornerShape(16.dp))
                        )
                        Text(
                            text = scanStatusText,
                            color = Color(0xFF00FFCC),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                        )
                        FloatingActionButton(
                            onClick = onGalleryClick,
                            containerColor = Color(0xFF00FFCC),
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                        ) {
                            Text("GALLERY", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                    }
                }


                is PokedexState.WhosThat -> {
                    var randomId by remember { mutableStateOf((1..151).random()) }
                    val assetUrl = "file:///android_asset/sprites/${randomId}.png"

                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bounce"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "WHO'S THAT POKEMON?",
                            color = Color(0xFFFFCC00),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(
                                    color = Color.Blue,
                                    offset = Offset(4f, 4f),
                                    blurRadius = 8f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val silhouetteMatrix = ColorMatrix(
                            floatArrayOf(
                                0f, 0f, 0f, 0f, 0f,
                                0f, 0f, 0f, 0f, 0f,
                                0f, 0f, 0f, 0f, 0f,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )

                        AsyncImage(
                            model = assetUrl,
                            contentDescription = "WHO'S THAT POKEMON?",
                            modifier = Modifier.size(150.dp).graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                            colorFilter = ColorFilter.colorMatrix(silhouetteMatrix)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "ANALYZING...",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is PokedexState.Reveal -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "IT'S ${state.pokemon.name.uppercase()}!",
                            color = Color(0xFFFFCC00),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(
                                    color = Color.Blue,
                                    offset = Offset(4f, 4f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            Image(
                                bitmap = state.capturedImage.asImageBitmap(),
                                contentDescription = "Captured",
                                modifier = Modifier.size(150.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            AsyncImage(
                                model = state.pokemon.spriteUrl,
                                contentDescription = state.pokemon.name,
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }
                }

                is PokedexState.Result -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = getColorForType(state.pokemon.type)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            var showShiny by remember { mutableStateOf(false) }
                            val currentSprite = if (showShiny && !state.pokemon.shinySpriteUrl.isNullOrEmpty()) {
                                state.pokemon.shinySpriteUrl
                            } else {
                                state.pokemon.spriteUrl
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = { viewModel.selectPreviousPokemon() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Text("◀", color = Color(0xFF00FFCC), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                AsyncImage(
                                    model = currentSprite,
                                    contentDescription = state.pokemon.name,
                                    modifier = Modifier.size(120.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                IconButton(
                                    onClick = { viewModel.selectNextPokemon() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Text("▶", color = Color(0xFF00FFCC), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            // Normal / Shiny Toggle
                            if (!state.pokemon.shinySpriteUrl.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.Center) {
                                    Button(
                                        onClick = { showShiny = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (!showShiny) Color.White else Color.DarkGray)
                                    ) {
                                        Text("NORMAL", color = if (!showShiny) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { showShiny = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (showShiny) Color.White else Color.DarkGray)
                                    ) {
                                        Text("SHINY", color = if (showShiny) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            // Alternate Forms Horizontal Row
                            if (state.pokemon.forms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val formsList = state.pokemon.forms.split(",")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    formsList.forEach { formName ->
                                        Button(
                                            onClick = { viewModel.selectPokemonByName(formName) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(formName.uppercase(), color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NO. ${state.pokemon.id.toString().padStart(3, '0')}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val displayName = state.pokemon.name.replace("-", " ")
                                .split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TYPE: ${state.pokemon.type.uppercase()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Parse JSON Entries
                            var entriesMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
                            LaunchedEffect(state.pokemon.pokedexEntries) {
                                try {
                                    val obj = org.json.JSONObject(state.pokemon.pokedexEntries)
                                    val map = mutableMapOf<String, String>()
                                    val keys = obj.keys()
                                    while (keys.hasNext()) {
                                        val key = keys.next()
                                        map[key] = obj.getString(key)
                                    }
                                    entriesMap = map
                                } catch (e: Exception) {
                                    entriesMap = emptyMap()
                                }
                            }
                            
                            var selectedVersion by remember { mutableStateOf("Default") }
                            var expanded by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(entriesMap) {
                                if (entriesMap.isNotEmpty() && !entriesMap.containsKey(selectedVersion)) {
                                    selectedVersion = entriesMap.keys.first()
                                }
                            }
                            
                            val displayFlavorText = entriesMap[selectedVersion] ?: state.pokemon.flavorText
                            
                            if (entriesMap.size > 1) {
                                Box {
                                    Button(
                                        onClick = { expanded = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                    ) {
                                        Text("DEX VERSION: ${selectedVersion.uppercase()} ▼", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color(0xFF222222))
                                    ) {
                                        entriesMap.keys.forEach { version ->
                                            DropdownMenuItem(
                                                text = { Text(version.uppercase(), color = Color.White, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedVersion = version
                                                    expanded = false
                                                    viewModel.readFlavorText(state.pokemon, entriesMap[version] ?: state.pokemon.flavorText)
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Text(
                                text = displayFlavorText,
                                color = Color.White,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 22.sp
                            )
                            
                            if (state.pokemon.evolvesTo.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                val nextEvo = state.pokemon.evolvesTo.split(",").first()
                                val formattedEvo = nextEvo.replace("-", " ")
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                Button(
                                    onClick = { viewModel.selectPokemonByName(nextEvo, evolvedFrom = state.pokemon.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                                ) {
                                    Text("Evolves to: $formattedEvo", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is PokedexState.Error -> {
                    Text(
                        text = state.message.uppercase(),
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

enum class VoiceCategory(val displayName: String) {
    RETRO_90S("Retro 90's Anime"),
    MODERN_HD("Modern High-End (HD)"),
    CUSTOM("Custom Voices & Accents")
}

@Composable
fun SettingsDialog(viewModel: PokedexViewModel, onDismiss: () -> Unit) {
    val currentProfile by viewModel.voiceProfile.collectAsState()
    val customVoiceName by viewModel.customVoiceName.collectAsState()
    val dspEnabled by viewModel.dspEnabled.collectAsState()
    val currentPitch by viewModel.voicePitch.collectAsState()
    val currentSpeed by viewModel.voiceSpeed.collectAsState()
    val availableVoices by viewModel.availableVoices.collectAsState()

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showVoiceDropdown by remember { mutableStateOf(false) }

    val currentCategory = when (currentProfile) {
        VoiceProfile.ORIGINAL_90S_MALE, VoiceProfile.RETRO_FEMALE_90S -> VoiceCategory.RETRO_90S
        VoiceProfile.HIGH_END_MALE, VoiceProfile.HIGH_END_FEMALE -> VoiceCategory.MODERN_HD
        VoiceProfile.CUSTOM -> VoiceCategory.CUSTOM
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VOICE SETTINGS",
                    color = Color(0xFF00FFCC),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Voice Category Dropdown
                Text(
                    text = "VOICE CATEGORY",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showCategoryDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentCategory.displayName, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("▼", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .background(Color(0xFF222222))
                    ) {
                        VoiceCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        category.displayName, 
                                        fontWeight = if (category == currentCategory) FontWeight.Bold else FontWeight.Normal,
                                        color = if (category == currentCategory) Color(0xFF00FFCC) else Color.White
                                    ) 
                                },
                                onClick = {
                                    showCategoryDropdown = false
                                    when (category) {
                                        VoiceCategory.RETRO_90S -> viewModel.setVoiceProfile(VoiceProfile.ORIGINAL_90S_MALE)
                                        VoiceCategory.MODERN_HD -> viewModel.setVoiceProfile(VoiceProfile.HIGH_END_MALE)
                                        VoiceCategory.CUSTOM -> viewModel.setVoiceProfile(VoiceProfile.CUSTOM)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Specific Voice / Accent Selection
                Text(
                    text = if (currentCategory == VoiceCategory.CUSTOM) "ACCENT & SYSTEM VOICE" else "VOICE SELECTION",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    val currentVoiceLabel = when (currentCategory) {
                        VoiceCategory.RETRO_90S -> {
                            if (currentProfile == VoiceProfile.ORIGINAL_90S_MALE) "Male (Original 90s Dexter)" else "Female (Retro 90s Anime)"
                        }
                        VoiceCategory.MODERN_HD -> {
                            if (currentProfile == VoiceProfile.HIGH_END_MALE) "Male (Modern HD Male)" else "Female (Modern HD Female)"
                        }
                        VoiceCategory.CUSTOM -> {
                            availableVoices.firstOrNull { it.name == customVoiceName }?.displayName ?: "Select Installed Voice & Accent"
                        }
                    }

                    Button(
                        onClick = { showVoiceDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentVoiceLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text("▼", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(max = 320.dp)
                            .background(Color(0xFF222222))
                    ) {
                        when (currentCategory) {
                            VoiceCategory.RETRO_90S -> {
                                DropdownMenuItem(
                                    text = { Text("Male (Original 90s Dexter)", color = if (currentProfile == VoiceProfile.ORIGINAL_90S_MALE) Color(0xFF00FFCC) else Color.White) },
                                    onClick = {
                                        viewModel.setVoiceProfile(VoiceProfile.ORIGINAL_90S_MALE)
                                        showVoiceDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Female (Retro 90s Anime)", color = if (currentProfile == VoiceProfile.RETRO_FEMALE_90S) Color(0xFF00FFCC) else Color.White) },
                                    onClick = {
                                        viewModel.setVoiceProfile(VoiceProfile.RETRO_FEMALE_90S)
                                        showVoiceDropdown = false
                                    }
                                )
                            }
                            VoiceCategory.MODERN_HD -> {
                                DropdownMenuItem(
                                    text = { Text("Male (Modern HD Male)", color = if (currentProfile == VoiceProfile.HIGH_END_MALE) Color(0xFF00FFCC) else Color.White) },
                                    onClick = {
                                        viewModel.setVoiceProfile(VoiceProfile.HIGH_END_MALE)
                                        showVoiceDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Female (Modern HD Female)", color = if (currentProfile == VoiceProfile.HIGH_END_FEMALE) Color(0xFF00FFCC) else Color.White) },
                                    onClick = {
                                        viewModel.setVoiceProfile(VoiceProfile.HIGH_END_FEMALE)
                                        showVoiceDropdown = false
                                    }
                                )
                            }
                            VoiceCategory.CUSTOM -> {
                                if (availableVoices.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No system voices found", color = Color.Gray) },
                                        onClick = { showVoiceDropdown = false }
                                    )
                                } else {
                                    availableVoices.forEach { voiceItem ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    voiceItem.displayName,
                                                    fontSize = 12.sp,
                                                    color = if (voiceItem.name == customVoiceName) Color(0xFF00FFCC) else Color.White
                                                )
                                            },
                                            onClick = {
                                                viewModel.setVoiceProfile(VoiceProfile.CUSTOM)
                                                viewModel.setCustomVoiceName(voiceItem.name)
                                                showVoiceDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(14.dp))

                // 3. 90s Hardware DSP Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setDspEnabled(!dspEnabled) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "90s HARDWARE DSP FILTER",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Metallic dual-tap comb echo & 2kHz horn mid-EQ",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = dspEnabled,
                        onCheckedChange = { viewModel.setDspEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00FFCC),
                            checkedTrackColor = Color(0xFF005544),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(14.dp))

                // 4. Voice Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VOICE PITCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val pitchLabel = if (currentPitch in 1.13f..1.17f) {
                        "${String.format("%.2f", currentPitch)}X (90s DEXTER)"
                    } else if (currentPitch in 0.98f..1.02f) {
                        "${String.format("%.2f", currentPitch)}X (NATURAL)"
                    } else {
                        "${String.format("%.2f", currentPitch)}X"
                    }
                    Text(pitchLabel, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Slider(
                    value = currentPitch,
                    onValueChange = { viewModel.setVoicePitch(it) },
                    valueRange = 0.70f..1.30f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00FFCC),
                        activeTrackColor = Color(0xFF00FFCC),
                        inactiveTrackColor = Color.Gray
                    )
                )

                // 5. Speech Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SPEECH SPEED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${String.format("%.1f", currentSpeed)}X", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Slider(
                    value = currentSpeed,
                    onValueChange = { viewModel.setVoiceSpeed(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00FFCC),
                        activeTrackColor = Color(0xFF00FFCC),
                        inactiveTrackColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: TEST VOICE & CLOSE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.testVoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("TEST VOICE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PokedexScreen(viewModel: PokedexViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredPokemonList by viewModel.filteredPokemonList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var lastViewedPokemonId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is PokedexState.Result) {
            lastViewedPokemonId = (uiState as PokedexState.Result).pokemon.id
        } else if (uiState is PokedexState.Idle && lastViewedPokemonId != null) {
            val targetIdx = filteredPokemonList.indexOfFirst { it.id == lastViewedPokemonId }
            if (targetIdx >= 0) {
                listState.animateScrollToItem(targetIdx)
            }
        }
    }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val softwareBitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    bmp.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                }
                viewModel.analyzeGalleryImage(softwareBitmap)
            } catch (e: Exception) {
                android.util.Log.e("PokedexScreen", "Error loading gallery image", e)
            }
        }
    }
    val launchGallery = { galleryLauncher.launch("image/*") }

    BackHandler(enabled = uiState !is PokedexState.Idle) {
        viewModel.goBack()
    }

    if (showSettings) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettings = false })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCC0000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (maxWidth > 600.dp) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    PokedexHeader(viewModel, onOpenSettings = { showSettings = true }, onGalleryClick = launchGallery)
                    Spacer(modifier = Modifier.height(16.dp))
                    PokedexDisplay(uiState, filteredPokemonList, searchQuery, viewModel, onGalleryClick = launchGallery, isListOnly = true, listState = listState, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (uiState is PokedexState.Idle) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SELECT A POKÉMON OR TAP THE BLUE SCANNER LENS.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        PokedexDisplay(uiState, emptyList(), "", viewModel, onGalleryClick = launchGallery, isListOnly = false, listState = listState, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        PokedexControls(uiState, viewModel)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                PokedexHeader(viewModel, onOpenSettings = { showSettings = true }, onGalleryClick = launchGallery)
                Spacer(modifier = Modifier.height(16.dp))
                PokedexDisplay(uiState, filteredPokemonList, searchQuery, viewModel, onGalleryClick = launchGallery, isListOnly = false, listState = listState, modifier = Modifier.weight(1f))
                if (uiState !is PokedexState.Idle) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PokedexControls(uiState, viewModel)
                }
            }
        }
    }
}



