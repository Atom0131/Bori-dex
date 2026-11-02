package com.example.pokedex.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.pokedex.camera.CameraXController
import com.example.pokedex.camera.PokemonFormDetector
import com.example.pokedex.data.*
import com.example.pokedex.tts.VoiceProfile
import com.example.pokedex.viewmodel.PokedexState
import com.example.pokedex.viewmodel.PokedexViewModel
import kotlinx.coroutines.flow.collect

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



data class GroupedEditionEntry(

    val groupLabel: String,

    val flavorText: String

)



data class GameCartridge(

    val id: String,

    val displayName: String,

    val candidateKeys: List<String>,

    val cartridgeColor: Color,

    val textColor: Color = Color.White,

    val generation: String = "",

    val mascotAsset: String = "default.png"

)



val ALL_GAME_CARTRIDGES = listOf(

    // Default / Debut

    GameCartridge("Default", "AUTO / DEBUT", listOf("Default"), Color(0xFF37474F), Color(0xFF00FFCC), "Default", "default.png"),

    

    // Gen 1 Kanto

    GameCartridge("Red", "RED", listOf("Red"), Color(0xFFD32F2F), Color.White, "Gen 1", "red.png"),

    GameCartridge("Blue", "BLUE", listOf("Blue"), Color(0xFF1976D2), Color.White, "Gen 1", "blue.png"),

    GameCartridge("Yellow", "YELLOW", listOf("Yellow"), Color(0xFFFBC02D), Color.Black, "Gen 1", "yellow.png"),

    

    // Gen 2 Johto

    GameCartridge("Gold", "GOLD", listOf("Gold"), Color(0xFFC5A059), Color.Black, "Gen 2", "gold.png"),

    GameCartridge("Silver", "SILVER", listOf("Silver"), Color(0xFF9E9E9E), Color.Black, "Gen 2", "silver.png"),

    GameCartridge("Crystal", "CRYSTAL", listOf("Crystal"), Color(0xFF00BCD4), Color.Black, "Gen 2", "crystal.png"),

    

    // Gen 3 Hoenn & Remakes

    GameCartridge("Ruby", "RUBY", listOf("Ruby"), Color(0xFFC2185B), Color.White, "Gen 3", "ruby.png"),

    GameCartridge("Sapphire", "SAPPHIRE", listOf("Sapphire"), Color(0xFF0D47A1), Color.White, "Gen 3", "sapphire.png"),

    GameCartridge("Emerald", "EMERALD", listOf("Emerald"), Color(0xFF00897B), Color.White, "Gen 3", "emerald.png"),

    GameCartridge("FireRed", "FIRERED", listOf("FireRed", "Firered"), Color(0xFFFF5722), Color.White, "Gen 1 Remake", "firered.png"),

    GameCartridge("LeafGreen", "LEAFGREEN", listOf("LeafGreen", "Leafgreen"), Color(0xFF43A047), Color.White, "Gen 1 Remake", "leafgreen.png"),

    

    // Gen 4 Sinnoh & Remakes

    GameCartridge("Diamond", "DIAMOND", listOf("Diamond"), Color(0xFF0288D1), Color.White, "Gen 4", "diamond.png"),

    GameCartridge("Pearl", "PEARL", listOf("Pearl"), Color(0xFFD81B60), Color.White, "Gen 4", "pearl.png"),

    GameCartridge("Platinum", "PLATINUM", listOf("Platinum"), Color(0xFF78909C), Color.White, "Gen 4", "platinum.png"),

    GameCartridge("HeartGold", "HEARTGOLD", listOf("HeartGold", "Heartgold"), Color(0xFFFFA000), Color.Black, "Gen 2 Remake", "heartgold.png"),

    GameCartridge("SoulSilver", "SOULSILVER", listOf("SoulSilver", "Soulsilver"), Color(0xFF546E7A), Color.White, "Gen 2 Remake", "soulsilver.png"),

    

    // Gen 5 Unova

    GameCartridge("Black", "BLACK", listOf("Black"), Color(0xFF263238), Color.White, "Gen 5", "black.png"),

    GameCartridge("White", "WHITE", listOf("White"), Color(0xFFECEFF1), Color(0xFF212121), "Gen 5", "white.png"),

    GameCartridge("Black 2", "BLACK 2", listOf("Black 2", "Black2"), Color(0xFF212121), Color(0xFF80D8FF), "Gen 5", "black2.png"),

    GameCartridge("White 2", "WHITE 2", listOf("White 2", "White2"), Color(0xFFEEEEEE), Color(0xFFD84315), "Gen 5", "white2.png"),

    

    // Gen 6 Kalos & Remakes

    GameCartridge("X", "X", listOf("X"), Color(0xFF1565C0), Color.White, "Gen 6", "x.png"),

    GameCartridge("Y", "Y", listOf("Y"), Color(0xFFC62828), Color.White, "Gen 6", "y.png"),

    GameCartridge("Omega Ruby", "OMEGA RUBY", listOf("Omega Ruby", "Omegaruby"), Color(0xFFBF360C), Color.White, "Gen 3 Remake", "omegaruby.png"),

    GameCartridge("Alpha Sapphire", "ALPHA SAPPHIRE", listOf("Alpha Sapphire", "Alphasapphire"), Color(0xFF01579B), Color.White, "Gen 3 Remake", "alphasapphire.png"),

    

    // Gen 7 Alola & Kanto

    GameCartridge("Sun", "SUN", listOf("Sun"), Color(0xFFE65100), Color.White, "Gen 7", "sun.png"),

    GameCartridge("Moon", "MOON", listOf("Moon"), Color(0xFF4A148C), Color.White, "Gen 7", "moon.png"),

    GameCartridge("Ultra Sun", "ULTRA SUN", listOf("Ultra Sun", "Ultrasun"), Color(0xFFFF6F00), Color.White, "Gen 7", "ultrasun.png"),

    GameCartridge("Ultra Moon", "ULTRA MOON", listOf("Ultra Moon", "Ultramoon"), Color(0xFF311B92), Color.White, "Gen 7", "ultramoon.png"),

    GameCartridge("Let's Go Pikachu", "LET'S GO PIKACHU", listOf("Let's Go Pikachu", "Lets Go Pikachu", "Lets-go-pikachu"), Color(0xFFFDD835), Color.Black, "Gen 7", "letsgopikachu.png"),

    GameCartridge("Let's Go Eevee", "LET'S GO EEVEE", listOf("Let's Go Eevee", "Lets Go Eevee", "Lets-go-eevee"), Color(0xFF8D6E63), Color.White, "Gen 7", "letsgoeevee.png"),

    

    // Gen 8 Galar & Hisui

    GameCartridge("Sword", "SWORD", listOf("Sword"), Color(0xFF0097A7), Color.White, "Gen 8", "sword.png"),

    GameCartridge("Shield", "SHIELD", listOf("Shield"), Color(0xFFAD1457), Color.White, "Gen 8", "shield.png"),

    GameCartridge("Legends: Arceus", "LEGENDS: ARCEUS", listOf("Legends: Arceus", "Legends Arceus", "Hisui"), Color(0xFF1B5E20), Color.White, "Gen 8", "legendsarceus.png"),

    

    // Gen 9 Paldea & Lumiose

    GameCartridge("Scarlet", "SCARLET", listOf("Scarlet"), Color(0xFFD32F2F), Color.White, "Gen 9", "scarlet.png"),

    GameCartridge("Violet", "VIOLET", listOf("Violet"), Color(0xFF6A1B9A), Color.White, "Gen 9", "violet.png"),

    GameCartridge("Legends: Z-A", "LEGENDS: Z-A", listOf("Legends: Z-A", "Legends Z-A", "Legends ZA", "ZA", "Lumiose"), Color(0xFF00E676), Color.Black, "Gen 9", "legendsza.png"),



    // Mobile / Pokémon GO

    GameCartridge("GO", "POKÉMON GO", listOf("GO", "Pokemon GO", "PokemonGo"), Color(0xFF0075BE), Color.White, "Mobile / GO", "pokemongo.png")

)



@Composable

fun PixelCartridgeIcon(assetName: String, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    val bitmap = remember(assetName) {

        try {

            context.assets.open("cartridges/$assetName").use { inputStream ->

                android.graphics.BitmapFactory.decodeStream(inputStream)

            }

        } catch (_: Exception) {

            null

        }

    }

    if (bitmap != null) {

        Image(

            bitmap = bitmap.asImageBitmap(),

            contentDescription = assetName,

            filterQuality = FilterQuality.None,

            modifier = modifier

        )

    }

}



@Composable

fun PixelCostumeIcon(

    assetPath: String,

    modifier: Modifier = Modifier,

    contentDescription: String? = null

) {

    val context = LocalContext.current

    val bitmap = remember(assetPath) {

        try {

            context.assets.open("costumes/$assetPath").use { inputStream ->

                android.graphics.BitmapFactory.decodeStream(inputStream)

            }

        } catch (_: Exception) {

            null

        }

    }

    if (bitmap != null) {

        Image(

            bitmap = bitmap.asImageBitmap(),

            contentDescription = contentDescription,

            filterQuality = FilterQuality.None,

            modifier = modifier

        )

    }

}



@Composable

fun PixelMapIcon(

    assetName: String,

    modifier: Modifier = Modifier,

    contentDescription: String? = null

) {

    val context = LocalContext.current

    val bitmap = remember(assetName) {

        try {

            context.assets.open("maps/$assetName").use { inputStream ->

                android.graphics.BitmapFactory.decodeStream(inputStream)

            }

        } catch (_: Exception) {

            null

        }

    }

    if (bitmap != null) {

        Image(

            bitmap = bitmap.asImageBitmap(),

            contentDescription = contentDescription ?: assetName,

            contentScale = ContentScale.Crop,

            modifier = modifier

        )

    } else {

        Box(

            modifier = modifier.background(Color(0xFF222222)),

            contentAlignment = Alignment.Center

        ) {

            Text("🗺️", fontSize = 16.sp)

        }

    }

}



@Composable

fun RegionSelectionModal(

    title: String = "SELECT REGION & TOWN MAP",

    subtitle: String = "Choose a region to view its authentic Town Map & canonical Pokédex:",

    entries: List<RegionMapEntry> = ALL_REGION_MAP_ENTRIES,

    selectedEntryId: String?,

    onSelectEntry: (RegionMapEntry) -> Unit,

    onDismiss: () -> Unit

) {

    Dialog(onDismissRequest = onDismiss) {

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .fillMaxHeight(0.85f)

                .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))

                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(16.dp))

                .padding(14.dp)

        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // Header

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(

                            text = title,

                            color = Color(0xFF00FFCC),

                            fontWeight = FontWeight.Black,

                            fontSize = 15.sp

                        )

                        Text(

                            text = subtitle,

                            color = Color.LightGray,

                            fontSize = 11.sp

                        )

                    }

                    IconButton(

                        onClick = onDismiss,

                        modifier = Modifier

                            .size(32.dp)

                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)

                    ) {

                        Text("✖", color = Color.White, fontSize = 14.sp)

                    }

                }



                Spacer(modifier = Modifier.height(10.dp))



                // 2-Column Grid of Regions & Town Maps

                LazyVerticalGrid(

                    columns = GridCells.Fixed(2),

                    horizontalArrangement = Arrangement.spacedBy(8.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp),

                    modifier = Modifier

                        .fillMaxWidth()

                        .weight(1f)

                ) {

                    items(entries, key = { it.id }) { entry ->

                        val isSelected = entry.id.equals(selectedEntryId, ignoreCase = true)



                        Box(

                            modifier = Modifier

                                .fillMaxWidth()

                                .height(80.dp)

                                .background(entry.cardColor, RoundedCornerShape(10.dp))

                                .border(

                                    width = if (isSelected) 2.5.dp else 1.5.dp,

                                    color = if (isSelected) Color(0xFF00FFCC) else Color.Black.copy(alpha = 0.5f),

                                    shape = RoundedCornerShape(10.dp)

                                )

                                .clickable {

                                    onSelectEntry(entry)

                                }

                                .padding(4.dp)

                        ) {

                            // Inner Card Sticker Label

                            Box(

                                modifier = Modifier

                                    .fillMaxSize()

                                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))

                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))

                                    .padding(horizontal = 6.dp, vertical = 4.dp)

                            ) {

                                Row(

                                    modifier = Modifier.fillMaxSize(),

                                    horizontalArrangement = Arrangement.SpaceBetween,

                                    verticalAlignment = Alignment.CenterVertically

                                ) {

                                    // Text Column

                                    Column(

                                        modifier = Modifier.weight(1f),

                                        verticalArrangement = Arrangement.Center

                                    ) {

                                        Text(

                                            text = entry.title,

                                            color = Color.White,

                                            fontWeight = FontWeight.Black,

                                            fontSize = 11.sp,

                                            maxLines = 1

                                        )

                                        Text(

                                            text = entry.eraSubtitle,

                                            color = Color.White.copy(alpha = 0.85f),

                                            fontWeight = FontWeight.SemiBold,

                                            fontSize = 9.sp,

                                            maxLines = 2,

                                            lineHeight = 11.sp

                                        )

                                        Spacer(modifier = Modifier.height(1.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Text(

                                                text = entry.rangeText,

                                                color = Color.White.copy(alpha = 0.6f),

                                                fontWeight = FontWeight.Bold,

                                                fontSize = 9.sp

                                            )

                                            if (isSelected) {

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Text(

                                                    text = "★ ACTIVE",

                                                    color = Color(0xFF00FFCC),

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 8.sp

                                                )

                                            }

                                        }

                                    }



                                    Spacer(modifier = Modifier.width(4.dp))



                                    // Canonical In-Game Town Map Artwork Box

                                    Box(

                                        modifier = Modifier

                                            .size(52.dp)

                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))

                                            .border(

                                                1.dp,

                                                if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.25f),

                                                RoundedCornerShape(6.dp)

                                            )

                                            .clip(RoundedCornerShape(6.dp)),

                                        contentAlignment = Alignment.Center

                                    ) {

                                        PixelMapIcon(

                                            assetName = entry.mapAsset,

                                            modifier = Modifier.fillMaxSize()

                                        )

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

fun GameEditionModal(

    title: String = "SELECT POKÉDEX VERSION",

    subtitle: String = "Choose a game edition to load its canonical lore & narration:",

    cartridges: List<GameCartridge>,

    selectedCartridgeId: String?,

    onSelectCartridge: (GameCartridge) -> Unit,

    onDismiss: () -> Unit

) {

    Dialog(onDismissRequest = onDismiss) {

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .fillMaxHeight(0.80f)

                .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))

                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(16.dp))

                .padding(16.dp)

        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // Header

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(

                            text = title,

                            color = Color(0xFF00FFCC),

                            fontWeight = FontWeight.Black,

                            fontSize = 15.sp

                        )

                        Text(

                            text = subtitle,

                            color = Color.LightGray,

                            fontSize = 11.sp

                        )

                    }

                    IconButton(

                        onClick = onDismiss,

                        modifier = Modifier

                            .size(32.dp)

                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)

                    ) {

                        Text("✖", color = Color.White, fontSize = 14.sp)

                    }

                }



                Spacer(modifier = Modifier.height(12.dp))



                // 2-Column Grid of Cartridges

                LazyVerticalGrid(

                    columns = GridCells.Fixed(2),

                    horizontalArrangement = Arrangement.spacedBy(8.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp),

                    modifier = Modifier

                        .fillMaxWidth()

                        .weight(1f)

                ) {

                    items(cartridges) { cartridge ->

                        val isSelected = cartridge.id.equals(selectedCartridgeId, ignoreCase = true) ||

                                (selectedCartridgeId != null && cartridge.candidateKeys.any { it.equals(selectedCartridgeId, ignoreCase = true) })



                        Box(

                            modifier = Modifier

                                .fillMaxWidth()

                                .height(68.dp)

                                .background(cartridge.cartridgeColor, RoundedCornerShape(10.dp))

                                .border(

                                    width = if (isSelected) 2.5.dp else 1.5.dp,

                                    color = if (isSelected) Color(0xFF00FFCC) else Color.Black.copy(alpha = 0.5f),

                                    shape = RoundedCornerShape(10.dp)

                                )

                                .clickable {

                                    onSelectCartridge(cartridge)

                                }

                                .padding(4.dp)

                        ) {

                            // Inner Cartridge Label Sticker

                            Box(

                                modifier = Modifier

                                    .fillMaxSize()

                                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))

                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))

                                    .padding(horizontal = 8.dp, vertical = 4.dp)

                            ) {

                                Row(

                                    modifier = Modifier.fillMaxSize(),

                                    horizontalArrangement = Arrangement.SpaceBetween,

                                    verticalAlignment = Alignment.CenterVertically

                                ) {

                                    Column(

                                        modifier = Modifier.weight(1f),

                                        verticalArrangement = Arrangement.Center

                                    ) {

                                        Text(

                                            text = cartridge.displayName,

                                            color = Color.White,

                                            fontWeight = FontWeight.Black,

                                            fontSize = 12.sp,

                                            maxLines = 1

                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Text(

                                                text = cartridge.generation,

                                                color = Color.White.copy(alpha = 0.7f),

                                                fontWeight = FontWeight.Bold,

                                                fontSize = 10.sp

                                            )

                                            if (isSelected) {

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(

                                                    text = "★ ACTIVE",

                                                    color = Color(0xFF00FFCC),

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 9.sp

                                                )

                                            }

                                        }

                                    }



                                    // Authentic Mascot Pixel Artwork

                                    Box(

                                        modifier = Modifier

                                            .size(44.dp)

                                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(6.dp))

                                            .border(1.dp, if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),

                                        contentAlignment = Alignment.Center

                                    ) {

                                        PixelCartridgeIcon(

                                            assetName = cartridge.mascotAsset,

                                            modifier = Modifier.size(40.dp)

                                        )

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

fun EvolutionAndFormsModal(

    pokemon: PokemonEntity,

    activeEditionKey: String,

    availableCartridges: List<GameCartridge> = emptyList(),

    evolutionChain: List<PokemonEntity>,

    viewModel: PokedexViewModel,

    selectedCostume: PokemonCostume? = null,

    onSelectCostume: ((PokemonCostume?) -> Unit)? = null,

    onCartridgeSelected: ((String) -> Unit)? = null,

    onDismiss: () -> Unit

) {

    var currentEditionKey by remember(activeEditionKey) { mutableStateOf(activeEditionKey) }

    var showCartridgeModal by remember { mutableStateOf(false) }



    val context = LocalContext.current

    val evolutions = remember(pokemon.id) {

        EvolutionGuideRepository.getEvolutionsForPokemon(context, pokemon.id)

    }



    Dialog(onDismissRequest = onDismiss) {

        val allList by viewModel.pokemonList.collectAsState()

        val canonicalId = pokemon.getCanonicalId(allList)

        val costumes = remember(canonicalId) {

            PokemonGoCostumeRepository.getCostumesForPokemon(canonicalId)

        }

        var isCostumesExpanded by remember(currentEditionKey) {

            mutableStateOf(currentEditionKey.equals("GO", ignoreCase = true))

        }



        val formsList = remember(pokemon.forms) {

            pokemon.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        }

        val currentName = pokemon.name.lowercase()

        val baseCandidate = formsList.firstOrNull { !it.contains("-") }

            ?: if (!currentName.contains("-")) currentName else currentName.substringBefore("-")



        val orderedForms = remember(formsList, currentName, baseCandidate) {

            val list = mutableListOf<String>()

            if (currentName.equals(baseCandidate, ignoreCase = true) || formsList.contains(baseCandidate)) {

                list.add(baseCandidate)

            }

            val otherForms = (formsList + listOf(currentName))

                .filter { !it.equals(baseCandidate, ignoreCase = true) }

                .distinct()

                .sorted()

            list.addAll(otherForms)

            list

        }



        Box(

            modifier = Modifier

                .fillMaxWidth()

                .wrapContentHeight()

                .heightIn(min = 200.dp, max = 640.dp)

                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))

                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(16.dp))

                .padding(16.dp)

        ) {

            Column(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {

                // Header

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(

                            text = "⚡ FORMS & EVOLUTIONS",

                            color = Color(0xFF00FFCC),

                            fontWeight = FontWeight.Black,

                            fontSize = 16.sp

                        )

                        Text(

                            text = "${pokemon.name.replace("-", " ").uppercase()} • #${canonicalId.toString().padStart(3, '0')}",

                            color = Color.White.copy(alpha = 0.7f),

                            fontWeight = FontWeight.Bold,

                            fontSize = 11.sp

                        )

                    }

                    IconButton(

                        onClick = onDismiss,

                        modifier = Modifier

                            .size(32.dp)

                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)

                    ) {

                        Text("✖", color = Color.White, fontSize = 14.sp)

                    }

                }



                Spacer(modifier = Modifier.height(12.dp))



                // Scrollable Body

                Column(

                    modifier = Modifier

                        .fillMaxWidth()

                        .weight(1f, fill = false)

                        .verticalScroll(rememberScrollState()),

                    verticalArrangement = Arrangement.spacedBy(14.dp)

                ) {

                    // SECTION 1: FAMILY EVOLUTION CHAIN (Prioritized at the top!)

                    if (evolutionChain.size > 1) {

                        Column(

                            modifier = Modifier

                                .fillMaxWidth()

                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))

                                .padding(12.dp)

                        ) {

                            Text(

                                text = "FAMILY EVOLUTION CHAIN",

                                color = Color(0xFF00FFCC),

                                fontWeight = FontWeight.Bold,

                                fontSize = 12.sp

                            )

                            Spacer(modifier = Modifier.height(8.dp))



                            Row(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .horizontalScroll(rememberScrollState()),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                evolutionChain.forEachIndexed { index, evoPokemon ->

                                    val isCurrent = evoPokemon.id == pokemon.id

                                    val formattedName = evoPokemon.name.replace("-", " ")

                                        .split(" ")

                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }



                                    Box(

                                        modifier = Modifier

                                            .background(

                                                if (isCurrent) Color(0xFF00FFCC) else Color.Black.copy(alpha = 0.5f),

                                                RoundedCornerShape(6.dp)

                                            )

                                            .border(

                                                if (isCurrent) 1.5.dp else 1.dp,

                                                if (isCurrent) Color.White else Color.Black.copy(alpha = 0.3f),

                                                RoundedCornerShape(6.dp)

                                            )

                                            .clickable(enabled = !isCurrent) {

                                                viewModel.selectPokemon(evoPokemon)

                                                onDismiss()

                                            }

                                            .padding(horizontal = 10.dp, vertical = 6.dp)

                                    ) {

                                        Text(

                                            text = formattedName.uppercase(),

                                            color = if (isCurrent) Color.Black else Color.White,

                                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,

                                            fontSize = 11.sp

                                        )

                                    }



                                    if (index < evolutionChain.size - 1) {

                                        Text(

                                            text = " ➔ ",

                                            color = Color(0xFF00FFCC),

                                            fontWeight = FontWeight.Black,

                                            fontSize = 12.sp,

                                            modifier = Modifier.padding(horizontal = 4.dp)

                                        )

                                    }

                                }

                            }

                        }

                    }



                    // SECTION 2: HOW TO EVOLVE (Version-Aware, immediately below family chain)

                    val hasMegasInForms = orderedForms.any { form ->

                        form.endsWith("-mega-x", true) || form.endsWith("-mega-y", true) || form.endsWith("-mega", true)

                    }

                    val filteredEvolutions = evolutions.filter { evo ->

                        // Deduplicate Megas: if Megas exist in Alternate Forms section, don't duplicate them here

                        !hasMegasInForms || !evo.targetName.contains("Mega", ignoreCase = true)

                    }



                    Column(

                        modifier = Modifier

                            .fillMaxWidth()

                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))

                            .padding(12.dp)

                    ) {

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween,

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Text(

                                text = "HOW TO EVOLVE",

                                color = Color(0xFF00FFCC),

                                fontWeight = FontWeight.Bold,

                                fontSize = 12.sp

                            )

                            Row(

                                modifier = Modifier

                                    .background(Color(0xFF00FFCC).copy(alpha = 0.15f), RoundedCornerShape(6.dp))

                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(6.dp))

                                    .clickable { showCartridgeModal = true }

                                    .padding(horizontal = 8.dp, vertical = 3.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                val activeCart = ALL_GAME_CARTRIDGES.firstOrNull { it.id.equals(currentEditionKey, ignoreCase = true) }

                                if (activeCart != null && activeCart.mascotAsset.isNotEmpty()) {

                                    PixelCartridgeIcon(

                                        assetName = activeCart.mascotAsset,

                                        modifier = Modifier.size(14.dp)

                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                }

                                Text(

                                    text = "VER: ${currentEditionKey.uppercase()} ▾",

                                    color = Color(0xFF00FFCC),

                                    fontSize = 10.sp,

                                    fontWeight = FontWeight.Black

                                )

                            }

                        }



                        Spacer(modifier = Modifier.height(8.dp))



                        if (filteredEvolutions.isEmpty()) {

                            Box(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))

                                    .padding(12.dp),

                                contentAlignment = Alignment.Center

                            ) {

                                Text(

                                    text = "FINAL STAGE / DOES NOT EVOLVE FURTHER",

                                    color = Color.White.copy(alpha = 0.7f),

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 11.sp

                                )

                            }

                        } else {

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                filteredEvolutions.forEach { evo ->

                                    val candidateList = ALL_GAME_CARTRIDGES.firstOrNull { it.id.equals(currentEditionKey, ignoreCase = true) }?.candidateKeys

                                        ?: listOf(currentEditionKey)



                                    val versionOverride = if (currentEditionKey.equals("Default", ignoreCase = true)) {

                                        null

                                    } else {

                                        evo.versionNotes.entries.firstOrNull { (k, _) ->

                                            val cleanK = k.lowercase().trim()

                                            candidateList.any { cand ->

                                                val cleanCand = cand.lowercase().trim()

                                                cleanK == cleanCand ||

                                                cleanK.split("/").any { it.trim() == cleanCand } ||

                                                cleanCand.split("/").any { it.trim() == cleanK } ||

                                                cleanK.replace(" ", "") == cleanCand.replace(" ", "") ||

                                                cleanK.replace(":", "").replace("-", " ") == cleanCand.replace(":", "").replace("-", " ") ||

                                                (cleanCand.length > 3 && cleanK.contains(cleanCand)) ||

                                                (cleanK.length > 3 && cleanCand.contains(cleanK))

                                            }

                                        }?.value

                                    }



                                    val methodText = versionOverride?.method ?: evo.defaultMethod

                                    val itemSprite = versionOverride?.item ?: evo.defaultItem

                                    val noteText = versionOverride?.note



                                    Row(

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))

                                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.35f), RoundedCornerShape(8.dp))

                                            .clickable {

                                                viewModel.selectPokemonById(evo.targetId)

                                                onDismiss()

                                            }

                                            .padding(10.dp),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        Box(

                                            modifier = Modifier

                                                .size(54.dp)

                                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))

                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),

                                            contentAlignment = Alignment.Center

                                        ) {

                                            AsyncImage(

                                                model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${evo.targetId}.png",

                                                contentDescription = evo.targetName,

                                                modifier = Modifier.size(48.dp)

                                            )

                                        }



                                        Spacer(modifier = Modifier.width(10.dp))



                                        Column(modifier = Modifier.weight(1f)) {

                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                Text(

                                                    text = "➔ ${evo.targetName.uppercase()}",

                                                    color = Color(0xFF00FFCC),

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 13.sp

                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(

                                                    text = "#${evo.targetId.toString().padStart(3, '0')}",

                                                    color = Color.White.copy(alpha = 0.6f),

                                                    fontWeight = FontWeight.Bold,

                                                    fontSize = 10.sp

                                                )



                                                val targetEntity = allList.firstOrNull { it.id == evo.targetId }

                                                if (targetEntity != null && targetEntity.type.isNotBlank()) {

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                                                        targetEntity.type.split("/").forEach { typeName ->

                                                            val trimmed = typeName.trim()

                                                            Box(

                                                                modifier = Modifier

                                                                    .background(getColorForType(trimmed), RoundedCornerShape(4.dp))

                                                                    .padding(horizontal = 5.dp, vertical = 1.dp)

                                                            ) {

                                                                Text(

                                                                    text = trimmed.uppercase(),

                                                                    color = Color.White,

                                                                    fontWeight = FontWeight.Bold,

                                                                    fontSize = 8.5.sp

                                                                )

                                                            }

                                                        }

                                                    }

                                                }

                                            }



                                            Spacer(modifier = Modifier.height(4.dp))



                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                if (itemSprite != null) {

                                                    PixelItemIcon(

                                                        itemName = itemSprite,

                                                        modifier = Modifier

                                                            .size(28.dp)

                                                            .padding(end = 6.dp)

                                                    )

                                                }

                                                Text(

                                                    text = methodText,

                                                    color = Color.White,

                                                    fontWeight = FontWeight.Normal,

                                                    fontSize = 12.sp,

                                                    lineHeight = 16.sp

                                                )

                                            }



                                            if (!noteText.isNullOrBlank()) {

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Box(

                                                    modifier = Modifier

                                                        .fillMaxWidth()

                                                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(4.dp))

                                                        .padding(horizontal = 6.dp, vertical = 3.dp)

                                                ) {

                                                    Text(

                                                        text = "★ $noteText",

                                                        color = Color(0xFFFFDD00),

                                                        fontWeight = FontWeight.Medium,

                                                        fontSize = 9.5.sp,

                                                        lineHeight = 13.5.sp

                                                    )

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }



                    // SECTION 3: ALTERNATE FORMS & MEGAS (Compact 2-Column Grid - No Infinite Scrolling!)

                    if (orderedForms.size > 1) {

                        Column(

                            modifier = Modifier

                                .fillMaxWidth()

                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))

                                .padding(12.dp)

                        ) {

                            Row(

                                modifier = Modifier.fillMaxWidth(),

                                horizontalArrangement = Arrangement.SpaceBetween,

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(

                                    text = "ALTERNATE FORMS & MEGAS",

                                    color = Color(0xFF00FFCC),

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 12.sp

                                )

                                Text(

                                    text = "${orderedForms.size} AVAILABLE",

                                    color = Color.LightGray,

                                    fontSize = 10.sp,

                                    fontWeight = FontWeight.Bold

                                )

                            }



                            Spacer(modifier = Modifier.height(8.dp))



                            // 2-column compact layout

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                                orderedForms.chunked(2).forEach { pair ->

                                    Row(

                                        modifier = Modifier.fillMaxWidth(),

                                        horizontalArrangement = Arrangement.spacedBy(8.dp)

                                    ) {

                                        pair.forEach { formName ->

                                            val isCurrent = formName.equals(currentName, ignoreCase = true)

                                            val label = when {

                                                formName.equals(baseCandidate, ignoreCase = true) -> "BASE FORM"

                                                formName.endsWith("-mega-x", ignoreCase = true) -> "MEGA X"

                                                formName.endsWith("-mega-y", ignoreCase = true) -> "MEGA Y"

                                                formName.endsWith("-mega-z", ignoreCase = true) -> "MEGA Z"

                                                formName.endsWith("-mega", ignoreCase = true) -> "MEGA"

                                                formName.endsWith("-gmax", ignoreCase = true) -> "G-MAX"

                                                formName.endsWith("-alola", ignoreCase = true) -> "ALOLAN"

                                                formName.endsWith("-galar", ignoreCase = true) -> "GALARIAN"

                                                formName.endsWith("-hisui", ignoreCase = true) -> "HISUIAN"

                                                formName.endsWith("-paldea", ignoreCase = true) -> "PALDEAN"

                                                else -> formName.replace(baseCandidate, "").replace("-", " ").trim().uppercase().ifEmpty { formName.uppercase() }

                                            }



                                            val stoneItem = when {

                                                formName.endsWith("-mega-x", ignoreCase = true) -> "${baseCandidate}ite-x.png"

                                                formName.endsWith("-mega-y", ignoreCase = true) -> "${baseCandidate}ite-y.png"

                                                formName.endsWith("-mega-z", ignoreCase = true) -> "${baseCandidate}ite-z.png"

                                                formName.endsWith("-mega", ignoreCase = true) -> "${baseCandidate}ite.png"

                                                formName.endsWith("-gmax", ignoreCase = true) -> "dynamax-band.png"

                                                else -> null

                                            }



                                            Row(

                                                modifier = Modifier

                                                    .weight(1f)

                                                    .background(

                                                        if (isCurrent) Color(0xFF00FFCC).copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.25f),

                                                        RoundedCornerShape(8.dp)

                                                    )

                                                    .border(

                                                        width = if (isCurrent) 1.5.dp else 1.dp,

                                                        color = if (isCurrent) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f),

                                                        shape = RoundedCornerShape(8.dp)

                                                    )

                                                    .clickable {

                                                        if (!isCurrent) {

                                                            viewModel.selectPokemonByName(formName)

                                                            onDismiss()

                                                        }

                                                    }

                                                    .padding(horizontal = 8.dp, vertical = 6.dp),

                                                verticalAlignment = Alignment.CenterVertically,

                                                horizontalArrangement = Arrangement.SpaceBetween

                                            ) {

                                                Row(

                                                    verticalAlignment = Alignment.CenterVertically,

                                                    modifier = Modifier.weight(1f)

                                                ) {

                                                    if (stoneItem != null) {

                                                        PixelItemIcon(

                                                            itemName = stoneItem,

                                                            modifier = Modifier

                                                                .size(20.dp)

                                                                .padding(end = 4.dp)

                                                        )

                                                    }

                                                    Column {

                                                        Text(

                                                            text = label,

                                                            color = if (isCurrent) Color(0xFF00FFCC) else Color.White,

                                                            fontWeight = FontWeight.Bold,

                                                            fontSize = 10.sp,

                                                            maxLines = 1

                                                        )

                                                        val formEntity = allList.firstOrNull { it.name.equals(formName, ignoreCase = true) }

                                                        if (formEntity != null && formEntity.type.isNotBlank()) {

                                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {

                                                                formEntity.type.split("/").forEach { typeName ->

                                                                    val trimmed = typeName.trim()

                                                                    Box(

                                                                        modifier = Modifier

                                                                            .background(getColorForType(trimmed), RoundedCornerShape(2.dp))

                                                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)

                                                                    ) {

                                                                        Text(

                                                                            text = trimmed.uppercase(),

                                                                            color = Color.White,

                                                                            fontWeight = FontWeight.Bold,

                                                                            fontSize = 7.sp,

                                                                            maxLines = 1,

                                                                            softWrap = false

                                                                        )

                                                                    }

                                                                }

                                                            }

                                                        }

                                                    }

                                                }



                                                Button(

                                                    onClick = {

                                                        if (!isCurrent) {

                                                            viewModel.selectPokemonByName(formName)

                                                            onDismiss()

                                                        }

                                                    },

                                                    colors = ButtonDefaults.buttonColors(

                                                        containerColor = if (isCurrent) Color(0xFF00FFCC) else Color.DarkGray

                                                    ),

                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),

                                                    shape = RoundedCornerShape(4.dp),

                                                    modifier = Modifier.height(22.dp)

                                                ) {

                                                    Text(

                                                        text = if (isCurrent) "ACTIVE" else "SWAP",

                                                        color = if (isCurrent) Color.Black else Color.White,

                                                        fontWeight = FontWeight.Black,

                                                        fontSize = 8.sp,

                                                        maxLines = 1,

                                                        softWrap = false

                                                    )

                                                }

                                            }

                                        }

                                        if (pair.size == 1) {

                                            Spacer(modifier = Modifier.weight(1f))

                                        }

                                    }

                                }

                            }

                        }

                    }



                    // SECTION 4: POKÉMON GO EVENT COSTUMES (Positioned at bottom, collapsible!)

                    if (costumes.isNotEmpty()) {

                        Column(

                            modifier = Modifier

                                .fillMaxWidth()

                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))

                                .padding(12.dp)

                        ) {

                            Row(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .clickable { isCostumesExpanded = !isCostumesExpanded },

                                horizontalArrangement = Arrangement.SpaceBetween,

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    PixelCartridgeIcon(

                                        assetName = "pokemongo.png",

                                        modifier = Modifier.size(16.dp)

                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(

                                        text = "POKÉMON GO EVENT COSTUMES",

                                        color = Color(0xFF00FFCC),

                                        fontWeight = FontWeight.Bold,

                                        fontSize = 12.sp

                                    )

                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Text(

                                        text = "${costumes.size} AVAILABLE",

                                        color = Color.LightGray,

                                        fontSize = 10.sp,

                                        fontWeight = FontWeight.Bold

                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(

                                        text = if (isCostumesExpanded) "▲" else "▼",

                                        color = Color(0xFF00FFCC),

                                        fontSize = 12.sp,

                                        fontWeight = FontWeight.Bold

                                    )

                                }

                            }



                            if (isCostumesExpanded) {

                                Spacer(modifier = Modifier.height(8.dp))



                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                    costumes.forEach { costume ->

                                        val isEquipped = selectedCostume?.id == costume.id



                                        Row(

                                            modifier = Modifier

                                                .fillMaxWidth()

                                                .background(

                                                    if (isEquipped) Color(0xFF0075BE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f),

                                                    RoundedCornerShape(8.dp)

                                                )

                                                .border(

                                                    width = if (isEquipped) 1.5.dp else 1.dp,

                                                    color = if (isEquipped) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f),

                                                    shape = RoundedCornerShape(8.dp)

                                                )

                                                .padding(8.dp),

                                            verticalAlignment = Alignment.CenterVertically,

                                            horizontalArrangement = Arrangement.SpaceBetween

                                        ) {

                                            Row(

                                                verticalAlignment = Alignment.CenterVertically,

                                                modifier = Modifier.weight(1f)

                                            ) {

                                                Box(

                                                    modifier = Modifier

                                                        .size(48.dp)

                                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))

                                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),

                                                    contentAlignment = Alignment.Center

                                                ) {

                                                    PixelCostumeIcon(

                                                        assetPath = costume.assetPath,

                                                        contentDescription = costume.name,

                                                        modifier = Modifier.size(44.dp)

                                                    )

                                                }



                                                Spacer(modifier = Modifier.width(10.dp))



                                                Column(modifier = Modifier.weight(1f)) {

                                                    Box(

                                                        modifier = Modifier

                                                            .background(Color(costume.category.badgeColor), RoundedCornerShape(3.dp))

                                                            .padding(horizontal = 4.dp, vertical = 1.dp)

                                                    ) {

                                                        Text(

                                                            text = costume.category.label,

                                                            color = Color.Black,

                                                            fontWeight = FontWeight.Black,

                                                            fontSize = 7.5.sp

                                                        )

                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(

                                                        text = costume.name.uppercase(),

                                                        color = if (isEquipped) Color(0xFF00FFCC) else Color.White,

                                                        fontWeight = FontWeight.Bold,

                                                        fontSize = 11.sp,

                                                        maxLines = 1

                                                    )

                                                    Text(

                                                        text = costume.description,

                                                        color = Color.White.copy(alpha = 0.7f),

                                                        fontSize = 9.sp,

                                                        lineHeight = 12.sp,

                                                        maxLines = 2

                                                    )

                                                }

                                            }



                                            Spacer(modifier = Modifier.width(8.dp))



                                            Button(

                                                onClick = {

                                                    if (isEquipped) {

                                                        onSelectCostume?.invoke(null)

                                                    } else {

                                                        onSelectCostume?.invoke(costume)

                                                    }

                                                },

                                                colors = ButtonDefaults.buttonColors(

                                                    containerColor = if (isEquipped) Color(0xFF00FFCC) else Color(0xFF0075BE)

                                                ),

                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),

                                                shape = RoundedCornerShape(6.dp),

                                                modifier = Modifier.height(28.dp)

                                            ) {

                                                Text(

                                                    text = if (isEquipped) "EQUIPPED" else "EQUIP",

                                                    color = if (isEquipped) Color.Black else Color.White,

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 10.sp

                                                )

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



        if (showCartridgeModal) {

            GameEditionModal(

                title = "SELECT GAME VERSION",

                subtitle = "Switch game cartridge to view version-specific evolution rules:",

                cartridges = if (availableCartridges.isNotEmpty()) availableCartridges else ALL_GAME_CARTRIDGES,

                selectedCartridgeId = currentEditionKey,

                onSelectCartridge = { cartridge ->

                    currentEditionKey = cartridge.id

                    showCartridgeModal = false

                    onCartridgeSelected?.invoke(cartridge.id)

                },

                onDismiss = { showCartridgeModal = false }

            )

        }

    }

}

@Composable

fun PixelItemIcon(itemName: String?, modifier: Modifier = Modifier) {

    if (itemName == null) return

    val context = LocalContext.current

    val bitmap = remember(itemName) {

        try {

            context.assets.open("items/$itemName").use { inputStream ->

                android.graphics.BitmapFactory.decodeStream(inputStream)

            }

        } catch (_: Exception) {

            null

        }

    }

    if (bitmap != null) {

        Image(

            bitmap = bitmap.asImageBitmap(),

            contentDescription = itemName,

            modifier = modifier,

            filterQuality = FilterQuality.None

        )

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

        

        // Strategy Guide Button
        Button(
            onClick = { viewModel.openGuideModal() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("🧭 GUIDE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Settings Button
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(17.dp))
                .clickable { viewModel.resetState() }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("◀ INDEX / RESET", color = Color(0xFF00FFCC), fontWeight = FontWeight.Black, fontSize = 11.5.sp)
        }
    }
}



@Composable
fun PokedexFoldHinge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(18.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Dark red recessed hinge groove
        Box(
            modifier = Modifier
                .width(14.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF660000),
                            Color(0xFF440000),
                            Color(0xFF220000),
                            Color(0xFF440000),
                            Color(0xFF660000)
                        )
                    ),
                    shape = RoundedCornerShape(7.dp)
                )
                .border(1.dp, Color(0xFF2A0000), RoundedCornerShape(7.dp))
        ) {
            // Cylindrical metallic hinge knuckles
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(32.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF555555),
                                        Color(0xFF888888),
                                        Color(0xFFCCCCCC),
                                        Color(0xFF888888),
                                        Color(0xFF444444)
                                    )
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    )
                }
            }
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

    val selectedMapEntryId by viewModel.selectedMapEntryId.collectAsState()

    var showRegionModal by remember { mutableStateOf(false) }



    val activeMapEntry = remember(selectedMapEntryId, selectedRegion) {

        val byId = ALL_REGION_MAP_ENTRIES.firstOrNull { it.id == selectedMapEntryId }

        if (byId != null) {

            byId

        } else if (selectedRegion != null) {

            ALL_REGION_MAP_ENTRIES.firstOrNull { it.region == selectedRegion } ?: ALL_REGION_MAP_ENTRIES.first()

        } else {

            ALL_REGION_MAP_ENTRIES.first()

        }

    }



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



        // Tactile Active Region Bar

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .padding(bottom = 8.dp)

                .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))

                .border(

                    width = if (selectedRegion != null) 2.dp else 1.dp,

                    color = if (selectedRegion != null) Color(0xFF00FFCC) else Color(0xFF333333),

                    shape = RoundedCornerShape(10.dp)

                )

                .clickable { showRegionModal = true }

                .padding(horizontal = 8.dp, vertical = 6.dp)

        ) {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    modifier = Modifier.weight(1f)

                ) {

                    // Active Town Map Thumbnail

                    Box(

                        modifier = Modifier

                            .size(42.dp)

                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))

                            .border(

                                1.dp,

                                if (selectedRegion != null) Color(0xFF00FFCC).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),

                                RoundedCornerShape(6.dp)

                            )

                            .clip(RoundedCornerShape(6.dp)),

                        contentAlignment = Alignment.Center

                    ) {

                        PixelMapIcon(

                            assetName = activeMapEntry.mapAsset,

                            modifier = Modifier.fillMaxSize()

                        )

                    }



                    Spacer(modifier = Modifier.width(10.dp))



                    Column(modifier = Modifier.weight(1f)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(

                                text = if (activeMapEntry.id == "all") "ALL MAPS" else "REGION: ${activeMapEntry.title}",

                                color = if (selectedRegion != null || activeMapEntry.id == "all") Color(0xFF00FFCC) else Color.White,

                                fontWeight = FontWeight.Black,

                                fontSize = 12.sp,

                                maxLines = 1

                            )

                            if (activeMapEntry.id == "all") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• National Dex",
                                    color = Color(0xFF00FFCC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            } else if (selectedRegion != null) {

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(

                                    text = "• ${activeMapEntry.eraSubtitle}",

                                    color = Color.LightGray,

                                    fontSize = 10.sp,

                                    maxLines = 1

                                )

                            }

                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(

                            text = "${activeMapEntry.generationText} • ${activeMapEntry.rangeText}",

                            color = Color.Gray,

                            fontSize = 10.sp,

                            fontWeight = FontWeight.Bold,

                            maxLines = 1

                        )

                    }

                }



                Spacer(modifier = Modifier.width(6.dp))



                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (selectedRegion != null) {

                        IconButton(

                            onClick = {

                                viewModel.selectRegion(null)

                            },

                            modifier = Modifier

                                .size(26.dp)

                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)

                        ) {

                            Text("✕", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        }

                        Spacer(modifier = Modifier.width(6.dp))

                    }



                    Box(

                        modifier = Modifier

                            .background(Color(0xFF2C2C2C), RoundedCornerShape(6.dp))

                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), RoundedCornerShape(6.dp))

                            .clickable { showRegionModal = true }

                            .padding(horizontal = 8.dp, vertical = 5.dp)

                    ) {

                        Text(

                            text = "MAP 🗺️ ▼",

                            color = Color(0xFF00FFCC),

                            fontSize = 10.sp,

                            fontWeight = FontWeight.Black

                        )

                    }

                }

            }

        }



        if (showRegionModal) {

            RegionSelectionModal(

                selectedEntryId = selectedMapEntryId,

                onSelectEntry = { entry ->

                    viewModel.selectMapEntry(entry)

                    showRegionModal = false

                },

                onDismiss = { showRegionModal = false }

            )

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



fun formatPokemonHeight(rawHeight: String?): String {
    if (rawHeight.isNullOrBlank() || rawHeight.contains("?")) return "📏 HT: --"
    val numStr = rawHeight.replace(Regex("[^0-9.]"), "")
    val meters = numStr.toDoubleOrNull() ?: return "📏 HT: $rawHeight"
    val totalInches = Math.round(meters * 39.3701).toInt()
    val feet = totalInches / 12
    val inches = totalInches % 12
    return "📏 HT: ${meters}m • ${feet}'%02d\"".format(inches)
}

fun formatPokemonWeight(rawWeight: String?): String {
    if (rawWeight.isNullOrBlank() || rawWeight.contains("?")) return "⚖️ WT: --"
    val numStr = rawWeight.replace(Regex("[^0-9.]"), "")
    val kg = numStr.toDoubleOrNull() ?: return "⚖️ WT: $rawWeight"
    val lbs = kg * 2.20462
    return "⚖️ WT: ${String.format(java.util.Locale.US, "%.1f", kg)}kg • ${String.format(java.util.Locale.US, "%.1f", lbs)}lbs"
}

fun formatPokemonSizeCategory(rawHeight: String?): String {
    if (rawHeight.isNullOrBlank() || rawHeight.contains("?")) return "🏷️ SIZE: STANDARD"
    val numStr = rawHeight.replace(Regex("[^0-9.]"), "")
    val meters = numStr.toDoubleOrNull() ?: return "🏷️ SIZE: STANDARD"
    val sizeLabel = when {
        meters < 0.5 -> "TINY"
        meters <= 1.2 -> "SMALL"
        meters <= 2.2 -> "MEDIUM"
        meters <= 3.5 -> "LARGE"
        else -> "GIGANTIC"
    }
    return "🏷️ SIZE: $sizeLabel"
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

    val scanStreak by viewModel.scanStreak.collectAsState()

    val scanProgress by viewModel.scanProgress.collectAsState()

    val scannerTelemetry by viewModel.scannerTelemetry.collectAsState()

    val candidatePokemon by viewModel.candidatePokemon.collectAsState()

    val candidateForms by viewModel.candidateForms.collectAsState()

    val isCandidateShiny by viewModel.isCandidateShiny.collectAsState()



    val isCompactBezel = !isListOnly && (uiState is PokedexState.Result || uiState is PokedexState.Scanning)



    Box(

        modifier = modifier

            .fillMaxWidth()

            .background(Color.Black, shape = RoundedCornerShape(8.dp))

            .padding(if (isCompactBezel) 6.dp else 16.dp),

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

                    val cameraController = remember { CameraXController() }

                    DisposableEffect(Unit) {

                        onDispose {

                            cameraController.setTorch(false)

                        }

                    }



                    Box(

                        modifier = Modifier

                            .fillMaxSize()

                            .clip(RoundedCornerShape(8.dp))

                    ) {

                        com.example.pokedex.camera.CameraPreview(

                            onFrame = { bitmap -> viewModel.onImageAnalyzed(bitmap) },

                            controller = cameraController,

                            modifier = Modifier

                                .fillMaxSize()

                                .clip(RoundedCornerShape(8.dp))

                        )

                        ActiveScannerHud(

                            scanStatusText = scanStatusText,

                            scanStreak = scanStreak,

                            scanProgress = scanProgress,

                            scannerTelemetry = scannerTelemetry,

                            candidatePokemon = candidatePokemon,

                            candidateForms = candidateForms,

                            isCandidateShiny = isCandidateShiny,

                            onSelectForm = { form -> viewModel.selectCandidateForm(form) },

                            onToggleShiny = { viewModel.toggleCandidateShiny() },

                            onGalleryClick = onGalleryClick,

                            onManualLock = {

                                cameraController.setTorch(false)

                                viewModel.triggerManualLock()

                            },

                            controller = cameraController,

                            modifier = Modifier.fillMaxSize()

                        )

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

                    val formTitle = PokemonFormDetector.formatFormDisplayName(state.pokemon.name)

                    val revealTitle = if (state.isShiny) "IT'S A SHINY ${formTitle.uppercase()}!" else "IT'S ${formTitle.uppercase()}!"

                    val revealSprite = if (state.isShiny && !state.pokemon.shinySpriteUrl.isNullOrEmpty()) {

                        state.pokemon.shinySpriteUrl

                    } else {

                        state.pokemon.spriteUrl

                    }

                    Column(

                        horizontalAlignment = Alignment.CenterHorizontally,

                        verticalArrangement = Arrangement.Center,

                        modifier = Modifier.fillMaxSize()

                    ) {

                        Text(

                            text = revealTitle,

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

                                model = revealSprite,

                                contentDescription = state.pokemon.name,

                                modifier = Modifier.size(150.dp)

                            )

                        }

                    }

                }



                is PokedexState.Result -> {

                    var evolutionChain by remember { mutableStateOf<List<com.example.pokedex.data.PokemonEntity>>(emptyList()) }

                    LaunchedEffect(state.pokemon.id) {

                        evolutionChain = viewModel.getFullEvolutionChain(state.pokemon)

                    }



                    Card(

                        modifier = Modifier

                            .fillMaxSize(),

                        colors = CardDefaults.cardColors(containerColor = getColorForType(state.pokemon.type)),

                        shape = RoundedCornerShape(8.dp)

                    ) {

                        Column(

                            horizontalAlignment = Alignment.CenterHorizontally,

                            modifier = Modifier

                                .fillMaxSize()

                                .padding(horizontal = 10.dp, vertical = 8.dp)

                        ) {

                            var showShiny by remember(state.pokemon.id, state.isShiny) { mutableStateOf(state.isShiny) }

                            var selectedCostume by remember(state.pokemon.id) { mutableStateOf<PokemonCostume?>(null) }

                            var showEvolutionModal by remember { mutableStateOf(false) }

                            var showEditionModal by remember { mutableStateOf(false) }

                            var showPokemonMapModal by remember { mutableStateOf(false) }

                            var showTypeGuide by remember(state.pokemon.id) { mutableStateOf(false) }

                            val preferredEdition by viewModel.preferredGameEdition.collectAsState()

                            val currentSprite = if (selectedCostume != null) {

                                if (showShiny && !selectedCostume?.shinyAssetPath.isNullOrEmpty()) {

                                    selectedCostume!!.fullShinyAssetUrl

                                } else {

                                    selectedCostume!!.fullAssetUrl

                                }

                            } else if (showShiny && !state.pokemon.shinySpriteUrl.isNullOrEmpty()) {

                                state.pokemon.shinySpriteUrl

                            } else {

                                state.pokemon.spriteUrl

                            }



                            // 1. Top Header: Number, Name, Type Badge

                            val canonicalId = state.pokemon.getCanonicalId(pokemonList)

                            val displayName = PokemonFormDetector.formatFormDisplayName(state.pokemon.name)



                            Row(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .padding(horizontal = 4.dp, vertical = 2.dp),

                                horizontalArrangement = Arrangement.SpaceBetween,

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(

                                    text = "NO. ${canonicalId.toString().padStart(3, '0')}",

                                    color = Color.White,

                                    fontSize = 16.sp,

                                    fontWeight = FontWeight.Black

                                )

                                Text(

                                    text = displayName,

                                    color = Color.White,

                                    fontSize = 22.sp,

                                    fontWeight = FontWeight.Black,

                                    maxLines = 1,

                                    overflow = TextOverflow.Ellipsis,

                                    modifier = Modifier

                                        .weight(1f, fill = false)

                                        .padding(horizontal = 6.dp)

                                )

                                PokemonTypeBadge(type = state.pokemon.type)

                            }

                            val formattedHeight = remember(state.pokemon.height) { formatPokemonHeight(state.pokemon.height) }
                            val formattedWeight = remember(state.pokemon.weight) { formatPokemonWeight(state.pokemon.weight) }
                            val formattedSize = remember(state.pokemon.height) { formatPokemonSizeCategory(state.pokemon.height) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Height Chip
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(5.dp))
                                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.70f), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = formattedHeight,
                                        color = Color(0xFF00FFCC),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Weight Chip
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(5.dp))
                                        .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.70f), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = formattedWeight,
                                        color = Color(0xFFFFCC00),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Size Classification Chip
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(5.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = formattedSize,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))



                            // 2. Centered Monitor Sprite Stage with Page-Turn Edge Arrows
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), CircleShape)
                                        .clickable { viewModel.selectPreviousPokemon() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("◀", color = Color(0xFF00FFCC), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(152.dp)

                                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))

                                        .border(2.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),

                                    contentAlignment = Alignment.Center

                                ) {

                                    if (selectedCostume != null) {

                                        val costumeFile = if (showShiny && selectedCostume?.shinyAssetPath != null) {

                                            selectedCostume!!.shinyAssetPath!!

                                        } else {

                                            selectedCostume!!.assetPath

                                        }

                                        PixelCostumeIcon(

                                            assetPath = costumeFile,

                                            contentDescription = state.pokemon.name,

                                            modifier = Modifier.size(136.dp)

                                        )



                                        Box(

                                            modifier = Modifier

                                                .align(Alignment.TopStart)

                                                .padding(4.dp)

                                                .background(Color(0xFF0075BE).copy(alpha = 0.9f), RoundedCornerShape(4.dp))

                                                .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(4.dp))

                                                .clickable { selectedCostume = null }

                                                .padding(horizontal = 4.dp, vertical = 2.dp)

                                        ) {

                                            Text(

                                                text = "GO EVENT ✖",

                                                color = Color.White,

                                                fontSize = 8.sp,

                                                fontWeight = FontWeight.Black

                                            )

                                        }

                                    } else {

                                        AsyncImage(

                                            model = currentSprite,

                                            contentDescription = state.pokemon.name,

                                            modifier = Modifier.size(136.dp),

                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit

                                        )

                                    }



                                    if (!state.pokemon.shinySpriteUrl.isNullOrEmpty() || selectedCostume?.shinyAssetPath != null) {

                                        Box(

                                            modifier = Modifier

                                                .align(Alignment.BottomEnd)

                                                .padding(6.dp)

                                                .size(28.dp)

                                                .background(

                                                    Color.Black.copy(alpha = 0.55f),

                                                    CircleShape

                                                )

                                                .border(

                                                    1.dp,

                                                    Color.White.copy(alpha = 0.35f),

                                                    CircleShape

                                                )

                                                .clickable { showShiny = !showShiny },

                                            contentAlignment = Alignment.Center

                                        ) {

                                            Text(

                                                text = "✨",

                                                fontSize = 13.sp

                                            )

                                        }

                                    }

                                }



                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), CircleShape)
                                        .clickable { viewModel.selectNextPokemon() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▶", color = Color(0xFF00FFCC), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }



                            if (selectedCostume != null) {

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(

                                    modifier = Modifier

                                        .background(Color(0xFF0075BE).copy(alpha = 0.45f), RoundedCornerShape(6.dp))

                                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(6.dp))

                                        .clickable { selectedCostume = null }

                                        .padding(horizontal = 8.dp, vertical = 3.dp),

                                    verticalAlignment = Alignment.CenterVertically

                                ) {

                                    Text(

                                        text = "EQUIPPED: ${selectedCostume?.name?.uppercase()}",

                                        color = Color(0xFF00FFCC),

                                        fontSize = 9.5.sp,

                                        fontWeight = FontWeight.Black

                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(

                                        text = "✖",

                                        color = Color.White,

                                        fontSize = 10.sp,

                                        fontWeight = FontWeight.Bold

                                    )

                                }

                            }



                            // Action Buttons (Cry, Speak, Shiny)

                            Row(

                                horizontalArrangement = Arrangement.Center,

                                verticalAlignment = Alignment.CenterVertically,

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .horizontalScroll(rememberScrollState())

                                    .padding(vertical = 4.dp)

                            ) {

                                Button(

                                    onClick = { viewModel.playPokemonCry(state.pokemon.id) },

                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),

                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),

                                    shape = RoundedCornerShape(6.dp),

                                    modifier = Modifier.heightIn(min = 34.dp)

                                ) {

                                    Text("🔊 CRY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)

                                }



                                Spacer(modifier = Modifier.width(6.dp))



                                val rawFlavor = remember(state.pokemon, preferredEdition) {

                                    viewModel.getEffectiveFlavorText(state.pokemon)

                                }

                                val currentCleanedFlavor = com.example.pokedex.data.PokemonMapHabitatRepository.cleanFlavorText(rawFlavor)

                                    .replace("\n", " ")

                                    .replace("\u000c", " ")

                                    .replace("\\f", " ")

                                    .replace(Regex("\\s+"), " ")

                                    .trim()



                                Button(

                                    onClick = { viewModel.readFlavorText(state.pokemon, currentCleanedFlavor) },

                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),

                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),

                                    shape = RoundedCornerShape(6.dp),

                                    modifier = Modifier.heightIn(min = 34.dp)

                                ) {

                                    Text("🗣 SPEAK", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)

                                }



                                if (!state.pokemon.shinySpriteUrl.isNullOrEmpty() || selectedCostume?.shinyAssetPath != null) {

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Button(

                                        onClick = { showShiny = !showShiny },

                                        colors = ButtonDefaults.buttonColors(

                                            containerColor = if (showShiny) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.5f)

                                        ),

                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),

                                        shape = RoundedCornerShape(6.dp),

                                        modifier = Modifier.heightIn(min = 34.dp)

                                    ) {

                                        Text(

                                            text = if (showShiny) "★ SHINY" else "NORMAL",

                                            color = if (showShiny) Color.Black else Color.White,

                                            fontWeight = if (showShiny) FontWeight.Black else FontWeight.Bold,

                                            fontSize = 11.sp

                                        )

                                    }

                                }

                            }



                            Spacer(modifier = Modifier.height(6.dp))

                            // 3. Ultra-Slim Text-Only Evolution Line

                            if (evolutionChain.size > 1) {

                                Row(

                                    modifier = Modifier

                                        .fillMaxWidth()

                                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(6.dp))

                                        .padding(horizontal = 8.dp, vertical = 5.dp),

                                    horizontalArrangement = Arrangement.Center,

                                    verticalAlignment = Alignment.CenterVertically

                                ) {

                                    Text(

                                        text = "EVOLUTION: ",

                                        color = Color(0xFF00FFCC),

                                        fontSize = 11.sp,

                                        fontWeight = FontWeight.Bold,

                                        modifier = Modifier.padding(end = 4.dp)

                                    )

                                    Row(

                                        modifier = Modifier.horizontalScroll(rememberScrollState()),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        evolutionChain.forEachIndexed { index, evoPokemon ->

                                            val isCurrent = evoPokemon.id == state.pokemon.id

                                            val formattedName = evoPokemon.name.replace("-", " ")

                                                .split(" ")

                                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }



                                            Box(

                                                modifier = Modifier

                                                    .background(

                                                        if (isCurrent) Color(0xFF00FFCC) else Color.Black.copy(alpha = 0.5f),

                                                        RoundedCornerShape(6.dp)

                                                    )

                                                    .border(

                                                        if (isCurrent) 1.5.dp else 1.dp,

                                                        if (isCurrent) Color.White else Color.Black.copy(alpha = 0.3f),

                                                        RoundedCornerShape(6.dp)

                                                    )

                                                    .clickable(enabled = !isCurrent) {

                                                        viewModel.selectPokemon(evoPokemon)

                                                    }

                                                    .padding(horizontal = 10.dp, vertical = 5.dp)

                                            ) {

                                                Text(

                                                    text = formattedName.uppercase(),

                                                    color = if (isCurrent) Color.Black else Color.White,

                                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,

                                                    fontSize = 12.sp

                                                )

                                            }



                                            if (index < evolutionChain.size - 1) {

                                                Text(

                                                    text = " ➔ ",

                                                    color = Color(0xFF00FFCC),

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 12.sp,

                                                    modifier = Modifier.padding(horizontal = 2.dp)

                                                )

                                            }

                                        }

                                    }

                                }

                                Spacer(modifier = Modifier.height(4.dp))

                            }



                            // 4. Unified Pokédex Data Panel with Paired Edition Grouping

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



                            val availableCartridges = remember(entriesMap) {

                                ALL_GAME_CARTRIDGES.filter { cartridge ->

                                    cartridge.id == "Default" || cartridge.id == "GO" || entriesMap.keys.any { key ->

                                        cartridge.candidateKeys.any { it.equals(key, ignoreCase = true) }

                                    }

                                }

                            }



                            var selectedCartridgeId by remember { mutableStateOf<String?>(null) }



                            LaunchedEffect(state.pokemon.id, availableCartridges, preferredEdition) {

                                if (availableCartridges.isNotEmpty()) {

                                    val preferredMatch = availableCartridges.firstOrNull {

                                        it.id.equals(preferredEdition, ignoreCase = true) ||

                                        it.candidateKeys.any { k -> k.equals(preferredEdition, ignoreCase = true) }

                                    }

                                    selectedCartridgeId = preferredMatch?.id ?: availableCartridges.first().id

                                } else {

                                    selectedCartridgeId = "Default"

                                }

                            }



                            val activeCartridge = availableCartridges.firstOrNull { it.id == selectedCartridgeId }

                                ?: availableCartridges.firstOrNull()

                                ?: ALL_GAME_CARTRIDGES.first()



                            val displayFlavorText = remember(activeCartridge, state.pokemon, entriesMap) {

                                if (activeCartridge.id == "Default") {

                                    state.pokemon.flavorText

                                } else {

                                    var found: String? = null

                                    for (cand in activeCartridge.candidateKeys) {

                                        val match = entriesMap.entries.firstOrNull { it.key.equals(cand, ignoreCase = true) }

                                        if (match != null) {

                                            found = match.value

                                            break

                                        }

                                    }

                                    found ?: state.pokemon.flavorText

                                }

                            }



                            val cleanedFlavorText = com.example.pokedex.data.PokemonMapHabitatRepository.cleanFlavorText(displayFlavorText)

                                .replace("\n", " ")

                                .replace("\u000c", " ")

                                .replace("\\f", " ")

                                .replace(Regex("\\s+"), " ")

                                .trim()



                            Column(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .weight(1f)

                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))

                                    .padding(horizontal = 12.dp, vertical = 10.dp)

                            ) {

                                // Row 1: Section Title & Audio on Left, Edition Selector on Right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "POKÉDEX DATA",
                                            color = Color(0xFF00FFCC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.5f))
                                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                .clickable { viewModel.readFlavorText(state.pokemon, displayFlavorText) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🔊",
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (activeCartridge.id.equals("GO", ignoreCase = true)) {
                                            Box(
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .background(Color(0xFF0075BE), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        selectedCartridgeId = "Default"
                                                        viewModel.setPreferredGameEdition("Default")
                                                    }
                                                    .padding(horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    PixelCartridgeIcon(
                                                        assetName = "pokemongo.png",
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "EXIT GO",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Button(
                                            onClick = { showEditionModal = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = activeCartridge.cartridgeColor),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 28.dp)
                                                .border(1.dp, activeCartridge.textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        ) {
                                            Text(
                                                text = "VER: ${activeCartridge.displayName} ▼",
                                                color = activeCartridge.textColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Row 2: Habitat Town Map Pill on Left, Forms & Evos Button on Right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val activeMapAsset = remember(activeCartridge.id, state.pokemon.id) {
                                        PokemonMapHabitatRepository.getMapAssetForCartridge(activeCartridge.id, state.pokemon.id)
                                    }

                                    // Town Map Launcher Pill
                                    Box(
                                        modifier = Modifier
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                            .clickable { showPokemonMapModal = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            ) {
                                                PixelMapIcon(
                                                    assetName = activeMapAsset,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentDescription = "Town Map"
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "TOWN MAP 🗺️",
                                                color = Color(0xFF00FFCC),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    // Forms & Evos Button
                                    Button(
                                        onClick = { showEvolutionModal = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00FFCC)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "⚡ FORMS & EVOS",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val (entryFontSize, entryLineHeight) = when {
                                     cleanedFlavorText.length <= 95 -> Pair(15.sp, 21.sp)
                                     cleanedFlavorText.length <= 155 -> Pair(13.5.sp, 18.5.sp)
                                     cleanedFlavorText.length <= 220 -> Pair(12.sp, 16.5.sp)
                                     cleanedFlavorText.length <= 300 -> Pair(11.sp, 15.sp)
                                     else -> Pair(10.5.sp, 14.sp)
                                 }

                                 val isSpanishLocale = remember { java.util.Locale.getDefault().language == "es" }
                                 val offensiveStrengths = remember(state.pokemon.type) {
                                     PokemonTypeEffectiveness.getOffensiveStrengths(state.pokemon.type)
                                 }
                                 val typeWeaknesses = remember(state.pokemon.type) {
                                     PokemonTypeEffectiveness.getWeaknesses(state.pokemon.type)
                                 }

                                 Column(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .weight(1f)
                                         .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                         .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                         .padding(8.dp)
                                 ) {
                                     // Row 1 inside dark box: STRONG TO (Offensive STAB Coverage 2×) + Ultimate Type Guide Launcher
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .padding(vertical = 1.5.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Text(
                                             text = if (isSpanishLocale) "⚡ FUERTE C/" else "⚡ STRONG TO",
                                             color = Color(0xFF00FFCC),
                                             fontSize = 9.sp,
                                             fontWeight = FontWeight.Black,
                                             modifier = Modifier.padding(end = 5.dp)
                                         )
                                         Row(
                                             modifier = Modifier
                                                 .weight(1f)
                                                 .horizontalScroll(rememberScrollState()),
                                             horizontalArrangement = Arrangement.spacedBy(3.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             if (offensiveStrengths.isEmpty()) {
                                                 Box(
                                                     modifier = Modifier
                                                         .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                         .padding(horizontal = 5.dp, vertical = 2.dp)
                                                 ) {
                                                     Text(
                                                         text = if (isSpanishLocale) "NINGUNA (1×)" else "NONE (1×)",
                                                         color = Color.White.copy(alpha = 0.6f),
                                                         fontSize = 8.sp,
                                                         fontWeight = FontWeight.Bold
                                                     )
                                                 }
                                             } else {
                                                 offensiveStrengths.forEach { t ->
                                                     TypePillBadge(
                                                         type = t,
                                                         multiplierLabel = "2×",
                                                         isQuad = false,
                                                         isSpanish = isSpanishLocale
                                                     )
                                                 }
                                             }
                                         }
                                         Spacer(modifier = Modifier.width(6.dp))
                                         Box(
                                             modifier = Modifier
                                                 .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                             .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                             .clickable { showTypeGuide = true }
                                             .padding(horizontal = 7.dp, vertical = 4.dp),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             Text(
                                                 text = "TYPE GUIDE ℹ️",
                                                 color = Color(0xFF00FFCC),
                                                 fontSize = 9.sp,
                                                 fontWeight = FontWeight.Black
                                             )
                                         }
                                     }

                                     Spacer(modifier = Modifier.height(3.dp))

                                     // Row 2 inside dark box: WEAK TO (Defensive Vulnerabilities 2× / 4×)
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .padding(vertical = 1.5.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Text(
                                             text = if (isSpanishLocale) "⚠️ DÉBIL A" else "⚠️ WEAK TO",
                                             color = Color(0xFFFF7777),
                                             fontSize = 9.sp,
                                             fontWeight = FontWeight.Black,
                                             modifier = Modifier.padding(end = 5.dp)
                                         )
                                         Row(
                                             modifier = Modifier
                                                 .weight(1f)
                                                 .horizontalScroll(rememberScrollState()),
                                             horizontalArrangement = Arrangement.spacedBy(3.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             if (typeWeaknesses.isEmpty()) {
                                                 Box(
                                                     modifier = Modifier
                                                         .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                         .padding(horizontal = 5.dp, vertical = 2.dp)
                                                 ) {
                                                     Text(
                                                         text = if (isSpanishLocale) "NINGUNA" else "NONE",
                                                         color = Color.White.copy(alpha = 0.6f),
                                                         fontSize = 8.sp,
                                                         fontWeight = FontWeight.Bold
                                                     )
                                                 }
                                             } else {
                                                 typeWeaknesses.forEach { w ->
                                                     TypePillBadge(
                                                         type = w.type,
                                                         multiplierLabel = if (w.multiplier >= 4.0f) "4×" else "2×",
                                                         isQuad = w.multiplier >= 4.0f,
                                                         isSpanish = isSpanishLocale
                                                     )
                                                 }
                                             }
                                         }
                                     }

                                     if (showTypeGuide) {
                                         TypeMatchupsDialog(
                                             pokemonName = displayName,
                                             typeString = state.pokemon.type,
                                             onReadMatchups = {
                                                 viewModel.speakCustomText(
                                                     PokemonTypeEffectiveness.formatFullMatchupsForSpeech(
                                                         displayName,
                                                         PokemonTypeEffectiveness.getFullMatchups(state.pokemon.type),
                                                         isSpanishLocale,
                                                         PokemonTypeEffectiveness.getOffensiveStrengths(state.pokemon.type)
                                                     )
                                                 )
                                             },
                                             onDismiss = { showTypeGuide = false }
                                         )
                                     }

                                     Spacer(modifier = Modifier.height(6.dp))

                                     // Subtle Terminal Divider
                                     Box(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .height(1.dp)
                                             .background(
                                                 Brush.horizontalGradient(
                                                     listOf(
                                                         Color(0xFF00FFCC).copy(alpha = 0.6f),
                                                         Color.White.copy(alpha = 0.2f),
                                                         Color.Transparent
                                                     )
                                                 )
                                             )
                                     )

                                     Spacer(modifier = Modifier.height(6.dp))

                                     // Narrative Lore Box with Scroll
                                     Box(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .weight(1f)
                                             .verticalScroll(rememberScrollState()),
                                         contentAlignment = Alignment.TopStart
                                     ) {
                                         Text(
                                             text = cleanedFlavorText,
                                             color = Color.White,
                                             fontWeight = FontWeight.Normal,
                                             fontSize = entryFontSize,
                                             lineHeight = entryLineHeight,
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                     }
                                 }

                            }



                            if (showEvolutionModal) {

                                EvolutionAndFormsModal(

                                    pokemon = state.pokemon,

                                    activeEditionKey = activeCartridge.id,

                                    availableCartridges = availableCartridges,

                                    evolutionChain = evolutionChain,

                                    viewModel = viewModel,

                                    selectedCostume = selectedCostume,

                                    onSelectCostume = { newCostume ->

                                        selectedCostume = newCostume

                                    },

                                    onCartridgeSelected = { newCartridgeId ->

                                        selectedCartridgeId = newCartridgeId

                                        viewModel.setPreferredGameEdition(newCartridgeId)

                                    },

                                    onDismiss = { showEvolutionModal = false }

                                )

                            }



                            if (showEditionModal) {

                                GameEditionModal(

                                    title = "SELECT POKÉDEX VERSION",

                                    subtitle = "Choose an edition to load its authentic lore & narration:",

                                    cartridges = availableCartridges,

                                    selectedCartridgeId = activeCartridge.id,

                                    onSelectCartridge = { cartridge ->

                                        selectedCartridgeId = cartridge.id

                                        viewModel.setPreferredGameEdition(cartridge.id)

                                        showEditionModal = false

                                        val newText = if (cartridge.id == "Default") {

                                            state.pokemon.flavorText

                                        } else {

                                            var matchText: String? = null

                                            for (cand in cartridge.candidateKeys) {

                                                val m = entriesMap.entries.firstOrNull { cand.equals(it.key, ignoreCase = true) }

                                                if (m != null) {

                                                    matchText = m.value

                                                    break

                                                }

                                            }

                                            matchText ?: state.pokemon.flavorText

                                        }

                                        viewModel.readFlavorText(state.pokemon, newText)

                                    },

                                    onDismiss = { showEditionModal = false }

                                )

                            }



                            if (showPokemonMapModal) {

                                PokemonMapHabitatModal(

                                    pokemon = state.pokemon,

                                    cartridge = activeCartridge,

                                    availableCartridges = availableCartridges,

                                    selectedCartridgeId = activeCartridge.id,

                                    onSelectCartridge = { /* Decoupled: Map viewing does not affect main screen lore */ },

                                    viewModel = viewModel,

                                    onDismiss = { showPokemonMapModal = false }

                                )

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



    val currentScanPreset by viewModel.scanFlowPreset.collectAsState()

    val playFanfare by viewModel.playFanfare.collectAsState()

    val playVoiceReveal by viewModel.playVoiceReveal.collectAsState()

    val playCryOnReveal by viewModel.playCryOnReveal.collectAsState()

    val autoPlayCryEntry by viewModel.autoPlayCryEntry.collectAsState()

    val autoReadDexEntry by viewModel.autoReadDexEntry.collectAsState()

    val preferredEdition by viewModel.preferredGameEdition.collectAsState()



    var showCategoryDropdown by remember { mutableStateOf(false) }

    var showVoiceDropdown by remember { mutableStateOf(false) }

    var showScanPresetDropdown by remember { mutableStateOf(false) }

    var showGameEditionModal by remember { mutableStateOf(false) }



    val currentCategory = when (currentProfile) {

        VoiceProfile.ORIGINAL_90S_MALE, VoiceProfile.RETRO_FEMALE_90S -> VoiceCategory.RETRO_90S

        VoiceProfile.HIGH_END_MALE, VoiceProfile.HIGH_END_FEMALE -> VoiceCategory.MODERN_HD

        VoiceProfile.CUSTOM -> VoiceCategory.CUSTOM

    }



    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("VOICE", "SOUND & SCAN", "GAME VERSION")



    Dialog(onDismissRequest = onDismiss) {

        Card(

            modifier = Modifier

                .fillMaxWidth()

                .padding(vertical = 8.dp),

            shape = RoundedCornerShape(14.dp),

            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))

        ) {

            Column(

                modifier = Modifier

                    .padding(14.dp),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // Header with fast X close

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Text(

                        text = "SETTINGS",

                        color = Color(0xFF00FFCC),

                        fontSize = 17.sp,

                        fontWeight = FontWeight.Bold

                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {

                        Text("✕", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    }

                }



                Spacer(modifier = Modifier.height(8.dp))



                // Compact Tab Navigation

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .background(Color(0xFF121212), RoundedCornerShape(8.dp))

                        .padding(3.dp),

                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {

                    tabs.forEachIndexed { index, tabTitle ->

                        val isTabSelected = selectedTab == index

                        Box(

                            modifier = Modifier

                                .weight(1f)

                                .background(if (isTabSelected) Color(0xFF00FFCC) else Color.Transparent, RoundedCornerShape(6.dp))

                                .clickable { selectedTab = index }

                                .padding(vertical = 7.dp),

                            contentAlignment = Alignment.Center

                        ) {

                            Text(

                                text = tabTitle,

                                color = if (isTabSelected) Color.Black else Color.Gray,

                                fontWeight = FontWeight.Bold,

                                fontSize = 11.sp,

                                maxLines = 1

                            )

                        }

                    }

                }



                Spacer(modifier = Modifier.height(10.dp))



                // Tab Content (Fixed compact height, no massive vertical scrolling)

                Box(

                    modifier = Modifier

                        .fillMaxWidth()

                        .height(310.dp)

                ) {

                    when (selectedTab) {

                        0 -> {

                            // TAB 0: VOICE & HARDWARE DSP

                            Column(

                                modifier = Modifier

                                    .fillMaxSize()

                                    .verticalScroll(rememberScrollState()),

                                verticalArrangement = Arrangement.spacedBy(8.dp)

                            ) {

                                // Category

                                Text("VOICE CATEGORY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                Box(modifier = Modifier.fillMaxWidth()) {

                                    Button(

                                        onClick = { showCategoryDropdown = true },

                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),

                                        shape = RoundedCornerShape(6.dp),

                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),

                                        modifier = Modifier.fillMaxWidth().height(36.dp)

                                    ) {

                                        Row(

                                            modifier = Modifier.fillMaxWidth(),

                                            horizontalArrangement = Arrangement.SpaceBetween,

                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Text(currentCategory.displayName, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                            Text("▼", color = Color.Gray, fontSize = 11.sp)

                                        }

                                    }

                                    DropdownMenu(

                                        expanded = showCategoryDropdown,

                                        onDismissRequest = { showCategoryDropdown = false },

                                        modifier = Modifier.fillMaxWidth(0.75f).background(Color(0xFF222222))

                                    ) {

                                        VoiceCategory.entries.forEach { category ->

                                            DropdownMenuItem(

                                                text = { 

                                                    Text(

                                                        category.displayName, 

                                                        fontWeight = if (category == currentCategory) FontWeight.Bold else FontWeight.Normal,

                                                        color = if (category == currentCategory) Color(0xFF00FFCC) else Color.White,

                                                        fontSize = 12.sp

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



                                // Voice Profile

                                Text(if (currentCategory == VoiceCategory.CUSTOM) "ACCENT & SYSTEM VOICE" else "VOICE PROFILE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

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

                                        shape = RoundedCornerShape(6.dp),

                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),

                                        modifier = Modifier.fillMaxWidth().height(36.dp)

                                    ) {

                                        Row(

                                            modifier = Modifier.fillMaxWidth(),

                                            horizontalArrangement = Arrangement.SpaceBetween,

                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Text(currentVoiceLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)

                                            Text("▼", color = Color.Gray, fontSize = 11.sp)

                                        }

                                    }



                                    DropdownMenu(

                                        expanded = showVoiceDropdown,

                                        onDismissRequest = { showVoiceDropdown = false },

                                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 260.dp).background(Color(0xFF222222))

                                    ) {

                                        when (currentCategory) {

                                            VoiceCategory.RETRO_90S -> {

                                                DropdownMenuItem(

                                                    text = { Text("Male (Original 90s Dexter)", color = if (currentProfile == VoiceProfile.ORIGINAL_90S_MALE) Color(0xFF00FFCC) else Color.White, fontSize = 12.sp) },

                                                    onClick = {

                                                        viewModel.setVoiceProfile(VoiceProfile.ORIGINAL_90S_MALE)

                                                        showVoiceDropdown = false

                                                    }

                                                )

                                                DropdownMenuItem(

                                                    text = { Text("Female (Retro 90s Anime)", color = if (currentProfile == VoiceProfile.RETRO_FEMALE_90S) Color(0xFF00FFCC) else Color.White, fontSize = 12.sp) },

                                                    onClick = {

                                                        viewModel.setVoiceProfile(VoiceProfile.RETRO_FEMALE_90S)

                                                        showVoiceDropdown = false

                                                    }

                                                )

                                            }

                                            VoiceCategory.MODERN_HD -> {

                                                DropdownMenuItem(

                                                    text = { Text("Male (Modern HD Male)", color = if (currentProfile == VoiceProfile.HIGH_END_MALE) Color(0xFF00FFCC) else Color.White, fontSize = 12.sp) },

                                                    onClick = {

                                                        viewModel.setVoiceProfile(VoiceProfile.HIGH_END_MALE)

                                                        showVoiceDropdown = false

                                                    }

                                                )

                                                DropdownMenuItem(

                                                    text = { Text("Female (Modern HD Female)", color = if (currentProfile == VoiceProfile.HIGH_END_FEMALE) Color(0xFF00FFCC) else Color.White, fontSize = 12.sp) },

                                                    onClick = {

                                                        viewModel.setVoiceProfile(VoiceProfile.HIGH_END_FEMALE)

                                                        showVoiceDropdown = false

                                                    }

                                                )

                                            }

                                            VoiceCategory.CUSTOM -> {

                                                if (availableVoices.isEmpty()) {

                                                    DropdownMenuItem(

                                                        text = { Text("No system voices found", color = Color.Gray, fontSize = 12.sp) },

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



                                // 90s Hardware DSP Toggle

                                Row(

                                    modifier = Modifier

                                        .fillMaxWidth()

                                        .clickable { viewModel.setDspEnabled(!dspEnabled) },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("90s Hardware DSP Filter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                        Text("Metallic dual-tap comb echo & horn mid-EQ", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = dspEnabled,

                                        onCheckedChange = { viewModel.setDspEnabled(it) },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }



                                // Voice Pitch Slider

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Text("VOICE PITCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                    val pitchLabel = if (currentPitch in 1.13f..1.17f) {

                                        "${String.format("%.2f", currentPitch)}X (90s DEXTER)"

                                    } else if (currentPitch in 0.98f..1.02f) {

                                        "${String.format("%.2f", currentPitch)}X (NATURAL)"

                                    } else {

                                        "${String.format("%.2f", currentPitch)}X"

                                    }

                                    Text(pitchLabel, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 11.sp)

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



                                // Speech Speed Slider

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Text("SPEECH SPEED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                    Text("${String.format("%.1f", currentSpeed)}X", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 11.sp)

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

                            }

                        }

                        1 -> {

                            // TAB 1: SCAN & SOUND BEHAVIOR

                            Column(

                                modifier = Modifier

                                    .fillMaxSize()

                                    .verticalScroll(rememberScrollState()),

                                verticalArrangement = Arrangement.spacedBy(8.dp)

                            ) {

                                Text("SCANNING FLOW PRESET", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                Box(modifier = Modifier.fillMaxWidth()) {

                                    Button(

                                        onClick = { showScanPresetDropdown = true },

                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),

                                        shape = RoundedCornerShape(6.dp),

                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),

                                        modifier = Modifier.fillMaxWidth().height(36.dp)

                                    ) {

                                        Row(

                                            modifier = Modifier.fillMaxWidth(),

                                            horizontalArrangement = Arrangement.SpaceBetween,

                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Text(currentScanPreset.displayName, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                            Text("▼", color = Color.Gray, fontSize = 11.sp)

                                        }

                                    }

                                    DropdownMenu(

                                        expanded = showScanPresetDropdown,

                                        onDismissRequest = { showScanPresetDropdown = false },

                                        modifier = Modifier.fillMaxWidth(0.85f).background(Color(0xFF222222))

                                    ) {

                                        com.example.pokedex.tts.ScanFlowPreset.entries.forEach { preset ->

                                            DropdownMenuItem(

                                                text = {

                                                    Column {

                                                        Text(

                                                            preset.displayName, 

                                                            fontWeight = FontWeight.Bold, 

                                                            color = if (preset == currentScanPreset) Color(0xFF00FFCC) else Color.White,

                                                            fontSize = 12.sp

                                                        )

                                                        Text(preset.description, fontSize = 10.sp, color = Color.Gray)

                                                    }

                                                },

                                                onClick = {

                                                    showScanPresetDropdown = false

                                                    viewModel.setScanFlowPreset(preset)

                                                }

                                            )

                                        }

                                    }

                                }



                                // 1. Fanfare

                                Row(

                                    modifier = Modifier.fillMaxWidth().clickable {

                                        viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                        viewModel.setPlayFanfare(!playFanfare)

                                    },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("Anime Fanfare", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Text("Commercial break fanfare & silhouette", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = playFanfare,

                                        onCheckedChange = {

                                            viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                            viewModel.setPlayFanfare(it)

                                        },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }



                                // 2. Voice Callout

                                Row(

                                    modifier = Modifier.fillMaxWidth().clickable {

                                        viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                        viewModel.setPlayVoiceReveal(!playVoiceReveal)

                                    },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("Voice Callout ('It's Pikachu!')", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Text("Dexter announces Pokémon name", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = playVoiceReveal,

                                        onCheckedChange = {

                                            viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                            viewModel.setPlayVoiceReveal(it)

                                        },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }



                                // 3. Cry on Reveal

                                Row(

                                    modifier = Modifier.fillMaxWidth().clickable {

                                        viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                        viewModel.setPlayCryOnReveal(!playCryOnReveal)

                                    },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("Cry on Scan Reveal", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Text("Animal cry plays with reveal card", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = playCryOnReveal,

                                        onCheckedChange = {

                                            viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                            viewModel.setPlayCryOnReveal(it)

                                        },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }



                                // 4. Auto-Play Cry on Dex Entry

                                Row(

                                    modifier = Modifier.fillMaxWidth().clickable {

                                        viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                        viewModel.setAutoPlayCryEntry(!autoPlayCryEntry)

                                    },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("Auto-Play Cry in Dex Entry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Text("Plays cry on Pokémon screen", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = autoPlayCryEntry,

                                        onCheckedChange = {

                                            viewModel.setScanFlowPreset(com.example.pokedex.tts.ScanFlowPreset.CUSTOM)

                                            viewModel.setAutoPlayCryEntry(it)

                                        },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }



                                // 5. Auto-Read Dex Entry

                                Row(

                                    modifier = Modifier.fillMaxWidth().clickable {

                                        viewModel.setAutoReadDexEntry(!autoReadDexEntry)

                                    },

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text("Auto-Read (Forward Only)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Text("Dexter speaks on next/open, silent on back", color = Color.Gray, fontSize = 10.sp)

                                    }

                                    Switch(

                                        checked = autoReadDexEntry,

                                        onCheckedChange = {

                                            viewModel.setAutoReadDexEntry(it)

                                        },

                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FFCC), checkedTrackColor = Color(0xFF005544))

                                    )

                                }

                            }

                        }

                        2 -> {

                            // TAB 2: GAME VERSION SELECTION

                            Column(

                                modifier = Modifier

                                    .fillMaxSize(),

                                verticalArrangement = Arrangement.spacedBy(10.dp)

                            ) {

                                Text(

                                    text = "PREFERRED GAME VERSION",

                                    color = Color(0xFF00FFCC),

                                    fontSize = 14.sp,

                                    fontWeight = FontWeight.Bold

                                )

                                Text(

                                    text = "Pokédex entries and Dexter's voice narration will prioritize this release generation.",

                                    color = Color.LightGray,

                                    fontSize = 11.sp

                                )



                                val selectedCartridge = ALL_GAME_CARTRIDGES.firstOrNull {

                                    it.id.equals(preferredEdition, ignoreCase = true) ||

                                    it.candidateKeys.any { k -> k.equals(preferredEdition, ignoreCase = true) }

                                } ?: ALL_GAME_CARTRIDGES.first()



                                Box(modifier = Modifier.fillMaxWidth()) {

                                    Button(

                                        onClick = { showGameEditionModal = true },

                                        colors = ButtonDefaults.buttonColors(containerColor = selectedCartridge.cartridgeColor),

                                        shape = RoundedCornerShape(8.dp),

                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),

                                        modifier = Modifier.fillMaxWidth().height(48.dp)

                                    ) {

                                        Row(

                                            modifier = Modifier.fillMaxWidth(),

                                            horizontalArrangement = Arrangement.SpaceBetween,

                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                Box(

                                                    modifier = Modifier

                                                        .size(12.dp)

                                                        .background(selectedCartridge.textColor, CircleShape)

                                                )

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Text(

                                                    text = "${selectedCartridge.displayName} (${selectedCartridge.generation})",

                                                    color = selectedCartridge.textColor,

                                                    fontWeight = FontWeight.Black,

                                                    fontSize = 13.sp

                                                )

                                            }

                                            Text("CHANGE ▼", color = selectedCartridge.textColor.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                        }

                                    }

                                }



                                if (showGameEditionModal) {

                                    GameEditionModal(

                                        title = "STANDARD GAME VERSION",

                                        subtitle = "Select your preferred version for Pokédex data & narration:",

                                        cartridges = ALL_GAME_CARTRIDGES,

                                        selectedCartridgeId = selectedCartridge.id,

                                        onSelectCartridge = { cartridge ->

                                            viewModel.setPreferredGameEdition(cartridge.id)

                                            showGameEditionModal = false

                                        },

                                        onDismiss = { showGameEditionModal = false }

                                    )

                                }

                            }

                        }

                    }

                }



                Spacer(modifier = Modifier.height(10.dp))



                // Bottom Action Buttons: TEST VOICE & CLOSE

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(10.dp)

                ) {

                    Button(

                        onClick = { viewModel.testVoice() },

                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),

                        shape = RoundedCornerShape(6.dp),

                        modifier = Modifier.weight(1f).height(38.dp)

                    ) {

                        Text("TEST VOICE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    }



                    Button(

                        onClick = onDismiss,

                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),

                        shape = RoundedCornerShape(6.dp),

                        modifier = Modifier.weight(1f).height(38.dp)

                    ) {

                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

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

    val initialListIndex = remember { viewModel.getSavedListIndex() }
    val initialListOffset = remember { viewModel.getSavedListOffset() }
    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState(
            firstVisibleItemIndex = initialListIndex,
            firstVisibleItemScrollOffset = initialListOffset
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (idx, offset) ->
                viewModel.saveListScrollPosition(idx, offset)
            }
    }

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
                    bmp.copy(Bitmap.Config.ARGB_8888, true)
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

    val isGuideModalOpen by viewModel.isGuideModalOpen.collectAsState()
    if (isGuideModalOpen) {
        GameGuideModal(viewModel = viewModel, onDismiss = { viewModel.closeGuideModal() })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCC0000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val isWide = maxWidth > 600.dp

        LaunchedEffect(isWide, filteredPokemonList, searchQuery) {
            if (isWide && filteredPokemonList.isNotEmpty()) {
                val currentPokemon = (uiState as? PokedexState.Result)?.pokemon
                val currentInList = currentPokemon != null && filteredPokemonList.any { it.id == currentPokemon.id }
                if (!currentInList) {
                    viewModel.selectPokemon(
                        pokemon = filteredPokemonList.first(),
                        autoSpeak = false,
                        recordHistory = false
                    )
                }
            }
        }

        if (isWide) {
            Column(modifier = Modifier.fillMaxSize()) {
                PokedexHeader(
                    viewModel = viewModel,
                    onOpenSettings = { showSettings = true },
                    onGalleryClick = launchGallery
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        PokedexDisplay(
                            uiState = uiState,
                            pokemonList = filteredPokemonList,
                            searchQuery = searchQuery,
                            viewModel = viewModel,
                            onGalleryClick = launchGallery,
                            isListOnly = true,
                            listState = listState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    PokedexFoldHinge(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (uiState is PokedexState.Idle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black, shape = RoundedCornerShape(8.dp)),
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
                            PokedexDisplay(
                                uiState = uiState,
                                pokemonList = emptyList(),
                                searchQuery = "",
                                viewModel = viewModel,
                                onGalleryClick = launchGallery,
                                isListOnly = false,
                                listState = listState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                PokedexHeader(viewModel, onOpenSettings = { showSettings = true }, onGalleryClick = launchGallery)
                Spacer(modifier = Modifier.height(16.dp))
                PokedexDisplay(uiState, filteredPokemonList, searchQuery, viewModel, onGalleryClick = launchGallery, isListOnly = false, listState = listState, modifier = Modifier.weight(1f))
                if (uiState !is PokedexState.Idle) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PokedexControls(uiState, viewModel)
                }
            }
        }
    }
}



val LandmarkTypeSetSaver: androidx.compose.runtime.saveable.Saver<Set<MapLandmarkType>, ArrayList<String>> =
    androidx.compose.runtime.saveable.Saver(
        save = { set -> ArrayList(set.map { it.name }) },
        restore = { list -> list.map { MapLandmarkType.valueOf(it) }.toSet() }
    )

@Composable
fun RegionMapLandmarksLayer(
    landmarks: List<MapLandmark>,
    drawnWidthDp: androidx.compose.ui.unit.Dp,
    drawnHeightDp: androidx.compose.ui.unit.Dp,
    enabledTypes: Set<MapLandmarkType> = MapLandmarkType.values().toSet(),
    selectedLandmark: MapLandmark? = null,
    onSelectLandmark: ((MapLandmark) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(drawnWidthDp, drawnHeightDp)
    ) {
        landmarks.filter { it.type in enabledTypes }.forEach { landmark ->
            val isSelected = selectedLandmark?.id == landmark.id
            val xDp = drawnWidthDp * landmark.xNorm.coerceIn(0.02f, 0.98f)
            val yDp = drawnHeightDp * landmark.yNorm.coerceIn(0.02f, 0.98f)

            val (badgeBg, borderCol, textCol) = when (landmark.type) {
                MapLandmarkType.CITY -> Triple(
                    Color(0xFF0F1A1E).copy(alpha = 0.92f),
                    Color(0xFF00FFCC).copy(alpha = 0.90f),
                    Color(0xFF00FFCC)
                )
                MapLandmarkType.TOWN -> Triple(
                    Color(0xFF1E1A0F).copy(alpha = 0.92f),
                    Color(0xFFFFCC00).copy(alpha = 0.90f),
                    Color(0xFFFFCC00)
                )
                MapLandmarkType.ROUTE -> Triple(
                    Color(0xFF151D24).copy(alpha = 0.88f),
                    Color(0xFF4FC3F7).copy(alpha = 0.75f),
                    Color(0xFFE0F7FA)
                )
                MapLandmarkType.POI -> Triple(
                    Color(0xFF221712).copy(alpha = 0.90f),
                    Color(0xFFFF7043).copy(alpha = 0.85f),
                    Color(0xFFFFAB91)
                )
                MapLandmarkType.ENTRANCE -> Triple(
                    Color(0xFF1B1526).copy(alpha = 0.92f),
                    Color(0xFFA78BFA).copy(alpha = 0.85f),
                    Color(0xFFEDE9FE)
                )
                MapLandmarkType.SECRET -> Triple(
                    Color(0xFF24141E).copy(alpha = 0.92f),
                    Color(0xFFF472B6).copy(alpha = 0.90f),
                    Color(0xFFFCE7F3)
                )
                MapLandmarkType.PUZZLE -> Triple(
                    Color(0xFF102018).copy(alpha = 0.92f),
                    Color(0xFF34D399).copy(alpha = 0.88f),
                    Color(0xFFD1FAE5)
                )
            }

            val effectiveBorderCol = if (isSelected) Color(0xFF00FFCC) else borderCol
            val effectiveBorderWidth = if (isSelected) 1.8.dp else 0.8.dp
            val effectiveBadgeBg = if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.35f) else badgeBg

            val tagEstimatedWidth = if (landmark.type == MapLandmarkType.ROUTE) 26.dp else 48.dp
            val tagOffsetX = (xDp - (tagEstimatedWidth / 2)).coerceIn(2.dp, (drawnWidthDp - tagEstimatedWidth - 4.dp).coerceAtLeast(2.dp))
            val tagOffsetY = (yDp - 8.dp).coerceIn(2.dp, (drawnHeightDp - 18.dp).coerceAtLeast(2.dp))

            Box(
                modifier = Modifier
                    .offset(x = tagOffsetX, y = tagOffsetY)
                    .clip(RoundedCornerShape(4.dp))
                    .background(effectiveBadgeBg, RoundedCornerShape(4.dp))
                    .border(effectiveBorderWidth, effectiveBorderCol, RoundedCornerShape(4.dp))
                    .then(
                        if (onSelectLandmark != null) Modifier.clickable { onSelectLandmark(landmark) }
                        else Modifier
                    )
                    .padding(horizontal = if (isSelected) 4.5.dp else 3.dp, vertical = if (isSelected) 1.8.dp else 1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (isSelected) {
                        Text(
                            text = "📍",
                            fontSize = 7.sp,
                            lineHeight = 8.sp
                        )
                    } else if (landmark.type != MapLandmarkType.ROUTE) {
                        Text(
                            text = landmark.type.emoji,
                            fontSize = 6.5.sp,
                            lineHeight = 7.5.sp
                        )
                    }
                    Text(
                        text = landmark.shortName,
                        color = if (isSelected) Color.White else textCol,
                        fontSize = if (landmark.type == MapLandmarkType.ROUTE) 7.sp else 7.5.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 8.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun MapLandmarkInspectorCard(
    landmark: MapLandmark,
    onClose: () -> Unit,
    onSelectConnectedArea: (String) -> Unit = {},
    onReadAdvice: () -> Unit = {},
    onOpenInGuide: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14191E).copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, landmark.type.badgeColor.copy(alpha = 0.85f)),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: Type emoji badge, Landmark Name, Read Audio, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(landmark.type.badgeColor.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                            .border(1.dp, landmark.type.badgeColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${landmark.type.emoji} ${landmark.type.displayName.uppercase()}",
                            color = landmark.type.badgeColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = landmark.name,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // TTS Read Button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FFCC).copy(alpha = 0.20f))
                            .border(1.dp, Color(0xFF00FFCC), CircleShape)
                            .clickable { onReadAdvice() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔊", fontSize = 11.sp)
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tagline / Slogan
            if (!landmark.tagline.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "“" + landmark.tagline + "”",
                    color = Color(0xFFFFCC00),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Overview / Description
            if (!landmark.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = landmark.description,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp
                )
            }

            // Gym Information
            if (!landmark.gymInfo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00FFCC).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🏟️ ${landmark.gymInfo}",
                        color = Color(0xFF00FFCC),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Key Facilities & Landmarks
            if (landmark.keyLocations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "KEY FACILITIES & LANDMARKS:",
                    color = Color(0xFF00FFCC),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                landmark.keyLocations.forEach { loc ->
                    Text(
                        text = loc,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            // Connected Routes & Areas
            if (landmark.connectedAreas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "CONNECTED ROUTES & AREAS (TAP TO JUMP):",
                    color = Color(0xFF4FC3F7),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    landmark.connectedAreas.forEach { area ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4FC3F7).copy(alpha = 0.15f))
                                .border(0.8.dp, Color(0xFF4FC3F7).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .clickable { onSelectConnectedArea(area) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🛣️ $area",
                                color = Color(0xFFE0F7FA),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Dexter's Strategy Advice
            if (!landmark.guideAdvice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .border(0.8.dp, Color(0xFFFFCC00).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 DEXTER'S TRAINER ADVICE",
                            color = Color(0xFFFFCC00),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = landmark.guideAdvice,
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Open in Game Guide Action
            if (onOpenInGuide != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onOpenInGuide,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                ) {
                    Text(
                        text = "🧭 SEARCH IN STRATEGY GUIDE ▶",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MapQuickLayerBar(
    enabledTypes: Set<MapLandmarkType>,
    onToggleType: (MapLandmarkType) -> Unit,
    onOpenFilterModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Open Modal Button
        Box(
            modifier = Modifier
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF00FFCC).copy(alpha = 0.22f))
                .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(6.dp))
                .clickable { onOpenFilterModal() }
                .padding(horizontal = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏷️ LAYERS (${enabledTypes.size}/7) ▼",
                color = Color(0xFF00FFCC),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Quick Toggle Chips for all 7 types
        MapLandmarkType.values().forEach { type ->
            val isEnabled = enabledTypes.contains(type)
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isEnabled) type.badgeColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.5f))
                    .border(
                        if (isEnabled) 1.2.dp else 0.8.dp,
                        if (isEnabled) type.badgeColor else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleType(type) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(type.emoji, fontSize = 8.5.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isEnabled) "${type.displayName} ✓" else type.displayName,
                        color = if (isEnabled) Color.White else Color.Gray,
                        fontSize = 8.5.sp,
                        fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun MapLayersFilterModal(
    regionName: String,
    landmarks: List<MapLandmark>,
    enabledTypes: Set<MapLandmarkType>,
    onToggleType: (MapLandmarkType) -> Unit,
    onSetAll: (Set<MapLandmarkType>) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF12161A),
            border = BorderStroke(2.dp, Color(0xFF00FFCC).copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🗺️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MAP LAYER CONTROLS",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Toggle landmark visibility on the ${regionName.uppercase()} map:",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Bar
                Text(
                    text = "QUICK PRESETS:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allSet = MapLandmarkType.values().toSet()
                    val civSet = setOf(MapLandmarkType.TOWN, MapLandmarkType.CITY)
                    val wildSet = setOf(MapLandmarkType.POI, MapLandmarkType.ENTRANCE, MapLandmarkType.SECRET, MapLandmarkType.PUZZLE)

                    // ALL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (enabledTypes.size == allSet.size) Color(0xFF00FFCC).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (enabledTypes.size == allSet.size) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onSetAll(allSet) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ALL (7)", color = if (enabledTypes.size == allSet.size) Color(0xFF00FFCC) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    // NONE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (enabledTypes.isEmpty()) Color(0xFFFF5252).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (enabledTypes.isEmpty()) Color(0xFFFF5252) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onSetAll(emptySet()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("NONE", color = if (enabledTypes.isEmpty()) Color(0xFFFF5252) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    // CIVILIZATION
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (enabledTypes == civSet) Color(0xFFFFCC00).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (enabledTypes == civSet) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onSetAll(civSet) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠 TOWNS ONLY", color = if (enabledTypes == civSet) Color(0xFFFFCC00) else Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }

                    // WILD & SECRETS
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (enabledTypes == wildSet) Color(0xFF34D399).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (enabledTypes == wildSet) Color(0xFF34D399) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onSetAll(wildSet) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔍 WILD & SECRETS", color = if (enabledTypes == wildSet) Color(0xFF34D399) else Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Granular Category Rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MapLandmarkType.values().forEach { type ->
                        val isEnabled = enabledTypes.contains(type)
                        val count = landmarks.count { it.type == type }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEnabled) type.badgeColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                                .border(
                                    1.dp,
                                    if (isEnabled) type.badgeColor.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onToggleType(type) }
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(type.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = type.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "$count located in this region",
                                            color = type.badgeColor,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isEnabled) type.badgeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, if (isEnabled) type.badgeColor else Color.Gray, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isEnabled) "ACTIVE ✓" else "OFF",
                                        color = if (isEnabled) type.badgeColor else Color.Gray,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "APPLY & VIEW MAP ▶",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}


@Composable

fun PokemonMapHabitatModal(

    pokemon: PokemonEntity,

    cartridge: GameCartridge,

    availableCartridges: List<GameCartridge>,

    selectedCartridgeId: String,

    onSelectCartridge: (GameCartridge) -> Unit,

    viewModel: PokedexViewModel,

    onDismiss: () -> Unit

) {

    val context = LocalContext.current

    var currentCartridgeId by remember(selectedCartridgeId) { mutableStateOf(selectedCartridgeId) }

    val activeCartridge = ALL_GAME_CARTRIDGES.firstOrNull { it.id == currentCartridgeId }
        ?: availableCartridges.firstOrNull { it.id == currentCartridgeId }
        ?: cartridge

    var showVersionPopout by remember { mutableStateOf(false) }
    var showRegionPopout by remember { mutableStateOf(false) }



    val habitat = remember(pokemon.id, activeCartridge.id) {

        PokemonMapHabitatRepository.getHabitatForPokemon(pokemon.id, pokemon.name, activeCartridge.id, context)

    }



    var selectedGbPalette by remember(activeCartridge.id) {
        val initial = when {
            activeCartridge.id.equals("Red", ignoreCase = true) -> GameBoyPalette.POCKET
            activeCartridge.id.equals("Blue", ignoreCase = true) -> GameBoyPalette.POCKET
            activeCartridge.id.equals("Yellow", ignoreCase = true) -> GameBoyPalette.SGB
            else -> GameBoyPalette.POCKET
        }
        mutableStateOf(initial)
    }

    val effectiveMapAsset = remember(habitat.mapAsset, selectedGbPalette) {
        if (habitat.mapAsset.startsWith("kanto_rgby")) selectedGbPalette.mapAsset else habitat.mapAsset
    }

    val isPixelMap = remember(effectiveMapAsset) {
        isPixelArtMap(effectiveMapAsset)
    }

    var enabledLandmarkTypes by rememberSaveable(stateSaver = LandmarkTypeSetSaver) {
        mutableStateOf(MapLandmarkType.values().toSet())
    }
    var showLayerFilterModal by remember { mutableStateOf(false) }

    val landmarks = remember(effectiveMapAsset) {
        RegionMapLandmarksRepository.getLandmarksForMap(effectiveMapAsset)
    }

    val mapBitmap = remember(effectiveMapAsset) {
        try {
            context.assets.open("maps/$effectiveMapAsset").use { inputStream ->
                val options = android.graphics.BitmapFactory.Options().apply {
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                    inScaled = false
                }
                android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    var selectedLandmark by remember { mutableStateOf<MapLandmark?>(null) }



    var selectedPin by remember(habitat) { mutableStateOf<MapLocationPin?>(habitat.pins.firstOrNull()) }



    // Pulsing radar animation for pins

    val infiniteTransition = rememberInfiniteTransition(label = "pinPulse")

    val pulseScale by infiniteTransition.animateFloat(

        initialValue = 0.85f,

        targetValue = 1.35f,

        animationSpec = infiniteRepeatable(

            animation = tween(900, easing = FastOutSlowInEasing),

            repeatMode = RepeatMode.Reverse

        ),

        label = "pulseScale"

    )

    val pulseAlpha by infiniteTransition.animateFloat(

        initialValue = 0.65f,

        targetValue = 0.2f,

        animationSpec = infiniteRepeatable(

            animation = tween(900, easing = FastOutSlowInEasing),

            repeatMode = RepeatMode.Reverse

        ),

        label = "pulseAlpha"

    )



    Dialog(

        onDismissRequest = onDismiss,

        properties = DialogProperties(usePlatformDefaultWidth = false)

    ) {

        Box(

            modifier = Modifier

                .fillMaxWidth(0.95f)

                .fillMaxHeight(0.90f)

                .background(Color(0xFF14181B), RoundedCornerShape(16.dp))

                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.8f), RoundedCornerShape(16.dp))

                .padding(14.dp)

        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗺️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "HABITAT & TOWN MAP",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${habitat.regionName.uppercase()} REGION · ${activeCartridge.displayName}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Speaker narration button: dedicated 34dp pill, guaranteed non-overlapping
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FFCC).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFF00FFCC), CircleShape)
                                .clickable { viewModel.readFlavorText(pokemon, habitat.narrationText) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔊", fontSize = 15.sp)
                        }

                        // Close Button: dedicated 34dp circle, crisp, guaranteed non-overlapping
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }



                Spacer(modifier = Modifier.height(8.dp))

                // Symmetrical Dual Popout Control Bar: Region Popout & Version Popout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Region Popout Button (opens RegionSelectionModal)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.2.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { showRegionPopout = true }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🗺️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "REGION: ${habitat.regionName.uppercase()}",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("▾", color = Color(0xFF00FFCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Version Popout Button (opens GameEditionModal)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeCartridge.cartridgeColor.copy(alpha = 0.85f))
                            .border(1.2.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { showVersionPopout = true }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            PixelCartridgeIcon(
                                assetName = activeCartridge.mascotAsset,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "VER: ${activeCartridge.displayName}",
                                color = activeCartridge.textColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("▾", color = activeCartridge.textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Map Layers Control Bar
                MapQuickLayerBar(
                    enabledTypes = enabledLandmarkTypes,
                    onToggleType = { toggled ->
                        enabledLandmarkTypes = if (enabledLandmarkTypes.contains(toggled)) {
                            enabledLandmarkTypes - toggled
                        } else {
                            enabledLandmarkTypes + toggled
                        }
                    },
                    onOpenFilterModal = { showLayerFilterModal = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Town Map Canvas with responsive coordinate overlay

                Box(

                    modifier = Modifier

                        .fillMaxWidth()

                        .weight(1f)

                        .background(Color.Black, RoundedCornerShape(12.dp))

                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f), RoundedCornerShape(12.dp))

                        .clip(RoundedCornerShape(12.dp)),

                    contentAlignment = Alignment.Center

                ) {

                    if (mapBitmap != null) {

                        BoxWithConstraints(

                            modifier = Modifier.fillMaxSize(),

                            contentAlignment = Alignment.Center

                        ) {

                            val containerWidth = maxWidth

                            val containerHeight = maxHeight

                            val density = LocalDensity.current



                            val containerWidthPx = with(density) { containerWidth.toPx() }

                            val containerHeightPx = with(density) { containerHeight.toPx() }



                            val bitmapWidth = mapBitmap.width.toFloat()

                            val bitmapHeight = mapBitmap.height.toFloat()

                            val bitmapRatio = bitmapWidth / bitmapHeight

                            val containerRatio = containerWidthPx / containerHeightPx



                            val (drawnWidthPx, drawnHeightPx) = if (containerRatio > bitmapRatio) {

                                val h = containerHeightPx

                                val w = h * bitmapRatio

                                Pair(w, h)

                            } else {

                                val w = containerWidthPx

                                val h = w / bitmapRatio

                                Pair(w, h)

                            }



                            val drawnWidthDp = with(density) { drawnWidthPx.toDp() }

                            val drawnHeightDp = with(density) { drawnHeightPx.toDp() }



                            // The Map Artwork
                            Image(
                                bitmap = mapBitmap.asImageBitmap(),
                                contentDescription = "${habitat.regionName} Town Map",
                                contentScale = ContentScale.Fit,
                                filterQuality = if (isPixelMap) FilterQuality.None else FilterQuality.High,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Granular landmark layers overlay with click inspection
                            RegionMapLandmarksLayer(
                                landmarks = landmarks,
                                drawnWidthDp = drawnWidthDp,
                                drawnHeightDp = drawnHeightDp,
                                enabledTypes = enabledLandmarkTypes,
                                selectedLandmark = selectedLandmark,
                                onSelectLandmark = { lm -> selectedLandmark = lm }
                            )



                            // Render Glowing Location Pins directly on top of the map image!

                            Box(

                                modifier = Modifier

                                    .size(drawnWidthDp, drawnHeightDp)

                                    .align(Alignment.Center)

                            ) {

                                habitat.pins.forEach { pin ->

                                    val pinXDp = drawnWidthDp * pin.coordinate.xNorm

                                    val pinYDp = drawnHeightDp * pin.coordinate.yNorm



                                    val isPinSelected = selectedPin?.id == pin.id



                                    Box(

                                        modifier = Modifier

                                            .offset(x = pinXDp - 15.dp, y = pinYDp - 15.dp)

                                            .size(30.dp)

                                            .clickable { selectedPin = pin },

                                        contentAlignment = Alignment.Center

                                    ) {

                                        // Pulse Halo Ring

                                        Box(

                                            modifier = Modifier

                                                .size((if (isPinSelected) 28.dp else 22.dp) * pulseScale)

                                                .background(

                                                    pin.type.badgeColor.copy(alpha = if (isPinSelected) pulseAlpha else pulseAlpha * 0.7f),

                                                    CircleShape

                                                )

                                                .border(

                                                    width = if (isPinSelected) 2.dp else 1.dp,

                                                    color = pin.type.badgeColor,

                                                    shape = CircleShape

                                                )

                                        )



                                        // Pin Core Badge

                                        Box(

                                            modifier = Modifier

                                                .size(18.dp)

                                                .background(Color.Black.copy(alpha = 0.8f), CircleShape)

                                                .border(1.dp, pin.type.badgeColor, CircleShape),

                                            contentAlignment = Alignment.Center

                                        ) {

                                            Text(

                                                text = pin.type.emoji,

                                                fontSize = 9.sp

                                            )

                                        }

                                    }

                                }

                            }



                            // Retro Area Unknown Banner if not native / evolution

                            if (habitat.isAreaUnknown) {

                                Box(

                                    modifier = Modifier

                                        .fillMaxSize()

                                        .background(Color.Black.copy(alpha = 0.55f)),

                                    contentAlignment = Alignment.Center

                                ) {

                                    Column(

                                        horizontalAlignment = Alignment.CenterHorizontally,

                                        modifier = Modifier

                                            .fillMaxWidth(0.85f)

                                            .background(Color(0xFF212121).copy(alpha = 0.95f), RoundedCornerShape(12.dp))

                                            .border(2.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))

                                            .padding(16.dp)

                                    ) {

                                        Text(

                                            text = "⚠️ AREA UNKNOWN",

                                            color = Color(0xFFFF5252),

                                            fontWeight = FontWeight.Black,

                                            fontSize = 15.sp

                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(

                                            text = "NOT FOUND IN THE WILD IN ${habitat.regionName.uppercase()}",

                                            color = Color.White,

                                            fontWeight = FontWeight.Bold,

                                            fontSize = 10.5.sp,

                                            textAlign = TextAlign.Center

                                        )

                                        if (habitat.unknownReason != null) {

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(

                                                text = habitat.unknownReason,

                                                color = Color(0xFF00FFCC),

                                                fontSize = 11.5.sp,

                                                lineHeight = 16.sp,

                                                textAlign = TextAlign.Center

                                            )

                                        }

                                    }

                                }

                            }

                        }

                    } else {

                        Text(

                            text = "MAP NOT AVAILABLE",

                            color = Color.Gray,

                            fontSize = 12.sp

                        )

                    }

                }



                Spacer(modifier = Modifier.height(10.dp))



                // Detail Encounter / Landmark Inspector Drawer below the map
                if (selectedLandmark != null) {
                    MapLandmarkInspectorCard(
                        landmark = selectedLandmark!!,
                        onClose = { selectedLandmark = null },
                        onSelectConnectedArea = { areaName ->
                            val matched = landmarks.firstOrNull { lm ->
                                areaName.contains(lm.name, ignoreCase = true) ||
                                areaName.contains(lm.shortName, ignoreCase = true) ||
                                (lm.id.startsWith("r") && areaName.contains(lm.name.substringBefore(" ("), ignoreCase = true))
                            }
                            if (matched != null) selectedLandmark = matched
                        },
                        onReadAdvice = {
                            val lm = selectedLandmark!!
                            val spoken = "${lm.name}. ${lm.tagline ?: ""} ${lm.description ?: ""} ${lm.guideAdvice ?: ""}".trim()
                            viewModel.readFlavorText(pokemon, spoken)
                        },
                        onOpenInGuide = {
                            viewModel.openGuideModal()
                            viewModel.searchGuide(selectedLandmark!!.name, activeCartridge.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 150.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {

                    if (habitat.pins.isNotEmpty()) {

                        val displayPin = selectedPin ?: habitat.pins.first()

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween,

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Text(displayPin.type.emoji, fontSize = 14.sp)

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(

                                    text = displayPin.areaName,

                                    color = Color(0xFF00FFCC),

                                    fontWeight = FontWeight.Black,

                                    fontSize = 13.sp

                                )

                            }



                            Box(

                                modifier = Modifier

                                    .background(displayPin.type.badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))

                                    .border(1.dp, displayPin.type.badgeColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))

                                    .padding(horizontal = 6.dp, vertical = 2.dp)

                            ) {

                                Text(

                                    text = displayPin.type.label,

                                    color = displayPin.type.badgeColor,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 9.sp

                                )

                            }

                        }



                        Spacer(modifier = Modifier.height(4.dp))



                        Text(

                            text = "• Method: ${displayPin.encounterMethod}",

                            color = Color.White,

                            fontSize = 12.sp

                        )



                        Text(

                            text = "• Rate / Level: ${displayPin.rateOrLevel}",

                            color = Color.LightGray,

                            fontSize = 11.5.sp

                        )



                        if (!displayPin.notes.isNullOrEmpty()) {

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(

                                text = "• Note: ${displayPin.notes}",

                                color = Color(0xFFFFD54F),

                                fontSize = 11.sp

                            )

                        }



                        if (habitat.pins.size > 1) {

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(

                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),

                                horizontalArrangement = Arrangement.spacedBy(6.dp)

                            ) {

                                habitat.pins.forEach { p ->

                                    val isSelected = p.id == displayPin.id

                                    Box(

                                        modifier = Modifier

                                            .background(

                                                if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),

                                                RoundedCornerShape(6.dp)

                                            )

                                            .border(

                                                1.dp,

                                                if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.2f),

                                                RoundedCornerShape(6.dp)

                                            )

                                            .clickable { selectedPin = p }

                                            .padding(horizontal = 8.dp, vertical = 4.dp)

                                    ) {

                                        Text(

                                            text = "${p.type.emoji} ${p.areaName}",

                                            color = if (isSelected) Color(0xFF00FFCC) else Color.White,

                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,

                                            fontSize = 10.sp

                                        )

                                    }

                                }

                            }

                        }

                    } else {

                        // Area unknown acquisition instructions

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text("ℹ️", fontSize = 14.sp)

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(

                                text = "ACQUISITION & EVOLUTION NOTES",

                                color = Color(0xFF00FFCC),

                                fontWeight = FontWeight.Bold,

                                fontSize = 12.sp

                            )

                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(

                            text = habitat.unknownReason ?: "Not found in the wild in this region. Obtain via trade, breeding, or transferring from another version.",

                            color = Color.White,

                            fontSize = 12.sp,

                            lineHeight = 17.sp

                        )

                    }

                }

            }

            }

            if (showRegionPopout) {
                RegionSelectionModal(
                    title = "SELECT REGION MAP",
                    subtitle = "View ${pokemon.name}'s habitat across different regions:",
                    selectedEntryId = null,
                    onSelectEntry = { entry ->
                        showRegionPopout = false
                        val targetEditionId = entry.preferredEditionId ?: "Red"
                        val matched = ALL_GAME_CARTRIDGES.firstOrNull {
                            it.id.equals(targetEditionId, ignoreCase = true) || it.displayName.equals(targetEditionId, ignoreCase = true)
                        }
                        if (matched != null) {
                            currentCartridgeId = matched.id
                        }
                    },
                    onDismiss = { showRegionPopout = false }
                )
            }

            if (showVersionPopout) {
                GameEditionModal(
                    title = "SELECT GAME VERSION",
                    subtitle = "Inspect ${pokemon.name}'s habitat in a specific game edition:",
                    cartridges = ALL_GAME_CARTRIDGES,
                    selectedCartridgeId = activeCartridge.id,
                    onSelectCartridge = { selectedCart ->
                        showVersionPopout = false
                        currentCartridgeId = selectedCart.id
                    },
                    onDismiss = { showVersionPopout = false }
                )
            }
        }
    }
}


@Composable
fun PokemonTypeBadge(
    type: String,
    isSpanish: Boolean = false,
    modifier: Modifier = Modifier
) {
    val types = type.split("/").map { it.trim() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { t ->
            val typeColor = Color(PokemonTypeEffectiveness.getTypeColor(t))
            val textColor = Color(PokemonTypeEffectiveness.getTypeTextColor(t))
            val typeName = PokemonTypeEffectiveness.translateType(t, isSpanish).uppercase()
            Box(
                modifier = Modifier
                    .background(typeColor, RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = PokemonTypeEffectiveness.getTypeIconRes(t)),
                        contentDescription = typeName,
                        tint = textColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = typeName,
                        color = textColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun TypePillBadge(
    type: String,
    multiplierLabel: String,
    isQuad: Boolean,
    isSpanish: Boolean,
    showText: Boolean = false,
    useFullName: Boolean = false
) {
    val typeColor = Color(PokemonTypeEffectiveness.getTypeColor(type))
    val textColor = Color(PokemonTypeEffectiveness.getTypeTextColor(type))
    val typeName = if (useFullName) {
        PokemonTypeEffectiveness.translateType(type, isSpanish).uppercase()
    } else {
        PokemonTypeEffectiveness.getAbbreviatedType(type, isSpanish).uppercase()
    }

    Box(
        modifier = Modifier
            .background(typeColor, RoundedCornerShape(4.dp))
            .border(
                width = if (isQuad) 1.2.dp else 0.5.dp,
                color = if (isQuad) Color(0xFFFF5252) else Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = if (showText) 5.dp else 3.5.dp, vertical = 1.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            Icon(
                painter = painterResource(id = PokemonTypeEffectiveness.getTypeIconRes(type)),
                contentDescription = typeName,
                tint = textColor,
                modifier = Modifier.size(11.dp)
            )
            if (showText) {
                Text(
                    text = typeName,
                    color = textColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Box(
                modifier = Modifier
                    .background(if (isQuad) Color(0xFFD32F2F) else Color.Black.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 2.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = multiplierLabel,
                    color = if (isQuad) Color.White else textColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun TypeMatchupsDialog(
    pokemonName: String,
    typeString: String,
    onReadMatchups: () -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWide = configuration.screenWidthDp > 600
    val isSpanish = java.util.Locale.getDefault().language == "es"
    val summary = remember(typeString) { PokemonTypeEffectiveness.getFullMatchups(typeString) }
    val offensive = remember(typeString) { PokemonTypeEffectiveness.getOffensiveStrengths(typeString) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(if (isWide) 0.82f else 0.94f)
                .wrapContentHeight()
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E2124),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSpanish) "GUÍA MAESTRA DE TIPOS" else "ULTIMATE TYPE GUIDE",
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isSpanish) "$pokemonName • Tipo: ${typeString.uppercase()}"
                                   else "$pokemonName • Type: ${typeString.uppercase()}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Text("✖", color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable categories: 2-Column Battle Matrix for wide/foldable screens, 1-Column for portrait
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Column: Tactical Advantages (STAB 2x, Resists 1/2x & 1/4x, Immunities 0x)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isSpanish) "VENTAJAS Y DEFENSAS" else "STRENGTHS & DEFENSES",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )

                            // 0. Offensive STAB Advantages (2x)
                            if (offensive.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "⚡ VENTAJAS OFENSIVAS (STAB 2×)" else "⚡ OFFENSIVE ADVANTAGES (STAB 2×)",
                                    titleColor = Color(0xFF00FFCC),
                                    types = offensive,
                                    multiplierLabel = "2×",
                                    isQuad = false,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }

                            // 4. Not Very Effective (0.5x & 0.25x)
                            val resists = summary.notVeryEffectiveHalf + summary.notVeryEffectiveQuarter
                            if (resists.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "🛡️ RESISTENCIAS (½× / ¼×)" else "🛡️ RESISTANCES (½× / ¼×)",
                                    titleColor = Color(0xFF64B5F6),
                                    types = summary.notVeryEffectiveHalf,
                                    quarterTypes = summary.notVeryEffectiveQuarter,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }

                            // 5. Immune (0x)
                            if (summary.immune.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "✨ INMUNIDADES (0×)" else "✨ IMMUNITIES (0×)",
                                    titleColor = Color(0xFFB0BEC5),
                                    types = summary.immune,
                                    multiplierLabel = "0×",
                                    isQuad = false,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }
                        }

                        // Right Column: Vulnerabilities & Neutral (4x, 2x, 1x)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isSpanish) "VULNERABILIDADES Y DAÑO" else "VULNERABILITIES & NEUTRAL",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )

                            // 1. Super Effective (4x)
                            if (summary.superEffectiveQuad.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "💥 SÚPER EFICAZ (4×)" else "💥 SUPER EFFECTIVE (4×)",
                                    titleColor = Color(0xFFFF5252),
                                    types = summary.superEffectiveQuad,
                                    multiplierLabel = "4×",
                                    isQuad = true,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }

                            // 2. Effective (2x)
                            if (summary.effectiveDouble.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "⚠️ EFICAZ / DEBILIDAD (2×)" else "⚠️ WEAKNESSES (2×)",
                                    titleColor = Color(0xFFFFCC00),
                                    types = summary.effectiveDouble,
                                    multiplierLabel = "2×",
                                    isQuad = false,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }

                            // 3. Just Effective / Neutral (1x)
                            if (summary.normalEffective.isNotEmpty()) {
                                MatchupGridSection(
                                    title = if (isSpanish) "⚖️ EFICAZ NORMAL (1×)" else "⚖️ NEUTRAL DAMAGE (1×)",
                                    titleColor = Color(0xFFE0E0E0),
                                    types = summary.normalEffective,
                                    multiplierLabel = "1×",
                                    isQuad = false,
                                    isSpanish = isSpanish,
                                    isWide = true
                                )
                            }
                        }
                    }
                } else {
                    // Mobile Portrait: Single scrollable vertical column
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 0. Offensive STAB Advantages (2x)
                        if (offensive.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "⚡ VENTAJAS OFENSIVAS (STAB 2×)" else "⚡ OFFENSIVE ADVANTAGES (STAB 2×)",
                                titleColor = Color(0xFF00FFCC),
                                types = offensive,
                                multiplierLabel = "2×",
                                isQuad = false,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }

                        // 1. Super Effective (4x)
                        if (summary.superEffectiveQuad.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "💥 SÚPER EFICAZ (4×)" else "💥 SUPER EFFECTIVE (4×)",
                                titleColor = Color(0xFFFF5252),
                                types = summary.superEffectiveQuad,
                                multiplierLabel = "4×",
                                isQuad = true,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }

                        // 2. Effective (2x)
                        if (summary.effectiveDouble.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "⚠️ EFICAZ / DEBILIDAD (2×)" else "⚠️ WEAKNESSES (2×)",
                                titleColor = Color(0xFFFFCC00),
                                types = summary.effectiveDouble,
                                multiplierLabel = "2×",
                                isQuad = false,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }

                        // 3. Just Effective / Neutral (1x)
                        if (summary.normalEffective.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "⚖️ EFICAZ NORMAL (1×)" else "⚖️ NEUTRAL DAMAGE (1×)",
                                titleColor = Color(0xFFE0E0E0),
                                types = summary.normalEffective,
                                multiplierLabel = "1×",
                                isQuad = false,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }

                        // 4. Not Very Effective (0.5x & 0.25x)
                        val resists = summary.notVeryEffectiveHalf + summary.notVeryEffectiveQuarter
                        if (resists.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "🛡️ RESISTENCIAS (½× / ¼×)" else "🛡️ RESISTANCES (½× / ¼×)",
                                titleColor = Color(0xFF64B5F6),
                                types = summary.notVeryEffectiveHalf,
                                quarterTypes = summary.notVeryEffectiveQuarter,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }

                        // 5. Immune (0x)
                        if (summary.immune.isNotEmpty()) {
                            MatchupGridSection(
                                title = if (isSpanish) "✨ INMUNIDADES (0×)" else "✨ IMMUNITIES (0×)",
                                titleColor = Color(0xFFB0BEC5),
                                types = summary.immune,
                                multiplierLabel = "0×",
                                isQuad = false,
                                isSpanish = isSpanish,
                                isWide = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onReadMatchups,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isSpanish) "🔊 ESCUCHAR GUÍA" else "🔊 READ TYPE GUIDE",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSpanish) "CERRAR" else "CLOSE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchupGridSection(
    title: String,
    titleColor: Color,
    types: List<String>,
    multiplierLabel: String = "",
    quarterTypes: List<String> = emptyList(),
    isQuad: Boolean = false,
    isSpanish: Boolean = false,
    isWide: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .border(1.dp, titleColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            color = titleColor,
            fontWeight = FontWeight.Black,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        val allItems = types.map { it to (if (multiplierLabel.isNotEmpty()) multiplierLabel else "½×") } +
                quarterTypes.map { it to "¼×" }

        val chunkSize = if (isWide) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            allItems.chunked(chunkSize).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEach { (type, mult) ->
                        TypePillBadge(
                            type = type,
                            multiplierLabel = mult,
                            isQuad = isQuad,
                            isSpanish = isSpanish,
                            showText = true,
                            useFullName = true
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GuideCategoryPopoutModal(
    categories: List<com.example.pokedex.data.GuideCategory>,
    selectedCategoryId: String?,
    onSelectTopic: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeCategory by remember(selectedCategoryId) {
        mutableStateOf(categories.firstOrNull { it.id == selectedCategoryId })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF16191D), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeCategory != null) {
                            Button(
                                onClick = { activeCategory = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242B33)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            ) {
                                Text("◀ ALL DOMAINS", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (activeCategory != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                PixelItemIcon(itemName = activeCategory!!.iconAsset, modifier = Modifier.size(20.dp))
                            }
                        } else {
                            Text("🧭", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = activeCategory?.title?.uppercase() ?: "STRATEGY GUIDE DOMAINS",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (activeCategory == null) "Tap a category to explore items, puzzles & walkthroughs:" else activeCategory!!.subtitle,
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body: View A (Category Adaptive Grid) vs View B (Topic Adaptive Grid)
                if (activeCategory == null) {
                    // View A: Adaptive Grid of Categories (2 columns on folded, 3-4 on unfolded)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E242B), RoundedCornerShape(10.dp))
                                    .border(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .clickable { activeCategory = cat }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PixelItemIcon(
                                                itemName = cat.iconAsset,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.subtitle,
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // View B: Adaptive Grid of Topics (1 column on folded, 2 on unfolded)
                    val currentCat = activeCategory!!
                    val topics = remember(currentCat.id) {
                        com.example.pokedex.data.PokemonGameGuideRepository.getTopicsForCategory(currentCat.id, context)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(topics, key = { it.name }) { topic ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E242B), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSelectTopic(topic.name)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (topic.iconAsset != null) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 10.dp)
                                                .size(32.dp)
                                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PixelItemIcon(
                                                itemName = topic.iconAsset,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (topic.aliases.isNotEmpty()) {
                                            Text(
                                                text = "Keywords: " + topic.aliases.joinToString(", "),
                                                color = Color.Gray,
                                                fontSize = 9.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("VIEW ▶", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 10.sp)
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
fun GuideMapPinCanvas(
    mapAsset: String,
    pinX: Float?,
    pinY: Float?,
    pinLabel: String?,
    editionName: String,
    firstEncounter: String? = null,
    subsequentAvailability: String? = null,
    modifier: Modifier = Modifier,
    canvasHeight: androidx.compose.ui.unit.Dp = 180.dp,
    fillMaxHeight: Boolean = false
) {
    val context = LocalContext.current
    var selectedGbPalette by rememberSaveable { mutableStateOf(GameBoyPalette.POCKET) }
    val effectiveMapAsset = remember(mapAsset, selectedGbPalette) {
        if (mapAsset.startsWith("kanto_rgby")) selectedGbPalette.mapAsset else mapAsset
    }
    val isPixel = remember(effectiveMapAsset) { isPixelArtMap(effectiveMapAsset) }
    var enabledLandmarkTypes by rememberSaveable(stateSaver = LandmarkTypeSetSaver) {
        mutableStateOf(MapLandmarkType.values().toSet())
    }
    var showLayerFilterModal by remember { mutableStateOf(false) }
    var selectedLandmark by remember { mutableStateOf<MapLandmark?>(null) }
    val landmarks = remember(effectiveMapAsset) {
        RegionMapLandmarksRepository.getLandmarksForMap(effectiveMapAsset)
    }
    val mapBitmap = remember(effectiveMapAsset) {
        try {
            context.assets.open("maps/$effectiveMapAsset").use { inputStream ->
                val options = android.graphics.BitmapFactory.Options().apply {
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                    inScaled = false
                }
                android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "guidePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "guidePulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "guidePulseAlpha"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (mapAsset.startsWith("kanto_rgby")) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PALETTE:", color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                GameBoyPalette.values().forEach { pal ->
                    val isSelected = selectedGbPalette == pal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(pal.chipColor.copy(alpha = if (isSelected) 1.0f else 0.45f))
                            .border(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedGbPalette = pal },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pal.displayName,
                            color = if (isSelected) Color.White else pal.textColor.copy(alpha = 0.85f),
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        val canvasModifier = if (fillMaxHeight) {
            Modifier
                .fillMaxWidth()
                .weight(1f)
        } else {
            Modifier
                .fillMaxWidth()
                .height(canvasHeight)
        }

        Box(
            modifier = canvasModifier
                .background(Color.Black, RoundedCornerShape(10.dp))
                .border(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = mapBitmap != null) { isExpanded = true },
            contentAlignment = Alignment.Center
        ) {
        if (mapBitmap != null) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val containerWidth = maxWidth
                val containerHeight = maxHeight
                val density = LocalDensity.current

                val containerWidthPx = with(density) { containerWidth.toPx() }
                val containerHeightPx = with(density) { containerHeight.toPx() }

                val bitmapWidth = mapBitmap.width.toFloat()
                val bitmapHeight = mapBitmap.height.toFloat()
                val bitmapRatio = bitmapWidth / bitmapHeight
                val containerRatio = containerWidthPx / containerHeightPx

                val (drawnWidthPx, drawnHeightPx) = if (containerRatio > bitmapRatio) {
                    val h = containerHeightPx
                    val w = h * bitmapRatio
                    Pair(w, h)
                } else {
                    val w = containerWidthPx
                    val h = w / bitmapRatio
                    Pair(w, h)
                }

                val drawnWidthDp = with(density) { drawnWidthPx.toDp() }
                val drawnHeightDp = with(density) { drawnHeightPx.toDp() }

                // The Map Artwork
                Image(
                    bitmap = mapBitmap.asImageBitmap(),
                    contentDescription = "$editionName Region Map",
                    contentScale = ContentScale.Fit,
                    filterQuality = if (isPixel) FilterQuality.None else FilterQuality.High,
                    modifier = Modifier.fillMaxSize()
                )

                // Town and Road Landmark Labels (granular layer filtering + click inspection)
                RegionMapLandmarksLayer(
                    landmarks = landmarks,
                    drawnWidthDp = drawnWidthDp,
                    drawnHeightDp = drawnHeightDp,
                    enabledTypes = enabledLandmarkTypes,
                    selectedLandmark = selectedLandmark,
                    onSelectLandmark = { lm -> selectedLandmark = lm }
                )

                // Render Glowing Pinpoint directly on the map if coordinates exist!
                if (pinX != null && pinY != null) {
                    Box(
                        modifier = Modifier
                            .size(drawnWidthDp, drawnHeightDp)
                            .align(Alignment.Center)
                    ) {
                        val pinXDp = drawnWidthDp * pinX.coerceIn(0.04f, 0.96f)
                        val pinYDp = drawnHeightDp * pinY.coerceIn(0.04f, 0.96f)

                        Box(
                            modifier = Modifier
                                .offset(x = pinXDp - 18.dp, y = pinYDp - 18.dp)
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing Radar Beacon
                            Box(
                                modifier = Modifier
                                    .size(34.dp * pulseScale)
                                    .background(Color(0xFF00FFCC).copy(alpha = pulseAlpha), CircleShape)
                                    .border(2.dp, Color(0xFF00FFCC), CircleShape)
                            )

                            // Pin Core Disc
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.92f), CircleShape)
                                    .border(2.dp, Color(0xFFFFCC00), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📍", fontSize = 12.sp)
                            }
                        }

                        // Floating Location Name Tag
                        if (!pinLabel.isNullOrBlank()) {
                            val tagYOffset = if (pinY > 0.35f) pinYDp - 36.dp else pinYDp + 20.dp
                            val tagXOffset = (pinXDp - 50.dp).coerceIn(4.dp, (drawnWidthDp - 100.dp).coerceAtLeast(4.dp))
                            Box(
                                modifier = Modifier
                                    .offset(x = tagXOffset, y = tagYOffset)
                                    .background(Color(0xFF16191D).copy(alpha = 0.95f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFFFFCC00), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = pinLabel,
                                    color = Color(0xFFFFCC00),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Top Header Overlay Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🗺️", fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (pinX != null) "TARGET LOCATION PINPOINT (${editionName.uppercase()})" else "REGION MAP (${editionName.uppercase()})",
                        color = Color(0xFF00FFCC),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top End Expand Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⛶", color = Color(0xFF00FFCC), fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TAP TO EXPAND",
                        color = Color(0xFF00FFCC),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bottom Start Layers Filter Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(0.8.dp, if (enabledLandmarkTypes.isNotEmpty()) Color(0xFF00FFCC) else Color.Gray, RoundedCornerShape(4.dp))
                    .clickable { showLayerFilterModal = true }
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = "🏷️ LAYERS (${enabledLandmarkTypes.size}/7) ▼",
                    color = if (enabledLandmarkTypes.isNotEmpty()) Color(0xFF00FFCC) else Color.LightGray,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        if (selectedLandmark != null) {
            Spacer(modifier = Modifier.height(6.dp))
            MapLandmarkInspectorCard(
                landmark = selectedLandmark!!,
                onClose = { selectedLandmark = null },
                onSelectConnectedArea = { areaName ->
                    val matched = landmarks.firstOrNull { lm ->
                        areaName.contains(lm.name, ignoreCase = true) ||
                        areaName.contains(lm.shortName, ignoreCase = true) ||
                        (lm.id.startsWith("r") && areaName.contains(lm.name.substringBefore(" ("), ignoreCase = true))
                    }
                    if (matched != null) selectedLandmark = matched
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

            if (showLayerFilterModal) {
                MapLayersFilterModal(
                    regionName = editionName,
                    landmarks = landmarks,
                    enabledTypes = enabledLandmarkTypes,
                    onToggleType = { toggled ->
                        enabledLandmarkTypes = if (enabledLandmarkTypes.contains(toggled)) {
                            enabledLandmarkTypes - toggled
                        } else {
                            enabledLandmarkTypes + toggled
                        }
                    },
                    onSetAll = { newSet -> enabledLandmarkTypes = newSet },
                    onDismiss = { showLayerFilterModal = false }
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🗺️", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("MAP NOT AVAILABLE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    }

    if (isExpanded && mapBitmap != null) {
        ExpandedGuideMapModal(
            mapAsset = effectiveMapAsset,
            pinX = pinX,
            pinY = pinY,
            pinLabel = pinLabel,
            editionName = editionName,
            firstEncounter = firstEncounter,
            subsequentAvailability = subsequentAvailability,
            onDismiss = { isExpanded = false }
        )
    }
}

@Composable


fun ExpandedGuideMapModal(
    mapAsset: String,
    pinX: Float?,
    pinY: Float?,
    pinLabel: String?,
    editionName: String,
    firstEncounter: String? = null,
    subsequentAvailability: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var enabledLandmarkTypes by rememberSaveable(stateSaver = LandmarkTypeSetSaver) {
        mutableStateOf(MapLandmarkType.values().toSet())
    }
    var showLayerFilterModal by remember { mutableStateOf(false) }
    var selectedLandmark by remember { mutableStateOf<MapLandmark?>(null) }
    val landmarks = remember(mapAsset) {
        RegionMapLandmarksRepository.getLandmarksForMap(mapAsset)
    }
    val mapBitmap = remember(mapAsset) {
        try {
            context.assets.open("maps/$mapAsset").use { inputStream ->
                val options = android.graphics.BitmapFactory.Options().apply {
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                    inScaled = false
                }
                android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "expandedGuidePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "expandedPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "expandedPulseAlpha"
    )

    androidx.activity.compose.BackHandler(enabled = true) {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF101418),
            border = BorderStroke(2.dp, Color(0xFF00FFCC).copy(alpha = 0.85f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗺️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "EXPANDED REGION MAP",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${editionName.uppercase()} EDITION • ${pinLabel ?: "REGIONAL MAP"}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Layers Filter Button
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (enabledLandmarkTypes.isNotEmpty()) Color(0xFF00FFCC).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                                .border(1.dp, if (enabledLandmarkTypes.isNotEmpty()) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { showLayerFilterModal = true }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏷️ LAYERS (${enabledLandmarkTypes.size}/7) ▼",
                                color = if (enabledLandmarkTypes.isNotEmpty()) Color(0xFF00FFCC) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        // Tactile Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                // 2. Interactive Pinch-to-Zoom & Pan Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1.0f, 4.0f)
                                scale = newScale
                                if (newScale > 1.0f) {
                                    val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (mapBitmap != null) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val containerWidth = maxWidth
                            val containerHeight = maxHeight
                            val density = LocalDensity.current

                            val containerWidthPx = with(density) { containerWidth.toPx() }
                            val containerHeightPx = with(density) { containerHeight.toPx() }

                            val bitmapWidth = mapBitmap.width.toFloat()
                            val bitmapHeight = mapBitmap.height.toFloat()
                            val bitmapRatio = bitmapWidth / bitmapHeight
                            val containerRatio = containerWidthPx / containerHeightPx

                            val (drawnWidthPx, drawnHeightPx) = if (containerRatio > bitmapRatio) {
                                val h = containerHeightPx
                                val w = h * bitmapRatio
                                Pair(w, h)
                            } else {
                                val w = containerWidthPx
                                val h = w / bitmapRatio
                                Pair(w, h)
                            }

                            val drawnWidthDp = with(density) { drawnWidthPx.toDp() }
                            val drawnHeightDp = with(density) { drawnHeightPx.toDp() }

                            // Transformed content container
                            Box(
                                modifier = Modifier
                                    .size(drawnWidthDp, drawnHeightDp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    }
                            ) {
                                Image(
                                    bitmap = mapBitmap.asImageBitmap(),
                                    contentDescription = "$editionName Region Map Expanded",
                                    contentScale = ContentScale.Fit,
                                    filterQuality = if (isPixelArtMap(mapAsset)) FilterQuality.None else FilterQuality.High,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Town and Road Landmark Labels (granular layer filtering + click inspection)
                                RegionMapLandmarksLayer(
                                    landmarks = landmarks,
                                    drawnWidthDp = drawnWidthDp,
                                    drawnHeightDp = drawnHeightDp,
                                    enabledTypes = enabledLandmarkTypes,
                                    selectedLandmark = selectedLandmark,
                                    onSelectLandmark = { lm -> selectedLandmark = lm }
                                )

                                if (pinX != null && pinY != null) {
                                    val pinXDp = drawnWidthDp * pinX.coerceIn(0.04f, 0.96f)
                                    val pinYDp = drawnHeightDp * pinY.coerceIn(0.04f, 0.96f)

                                    Box(
                                        modifier = Modifier
                                            .offset(x = pinXDp - 22.dp, y = pinYDp - 22.dp)
                                            .size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Outer pulsing beacon
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp * pulseScale)
                                                .background(Color(0xFF00FFCC).copy(alpha = pulseAlpha), CircleShape)
                                                .border(2.dp, Color(0xFF00FFCC), CircleShape)
                                        )

                                        // Pin Core Disc
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color.Black.copy(alpha = 0.95f), CircleShape)
                                                .border(2.dp, Color(0xFFFFCC00), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("📍", fontSize = 14.sp)
                                        }
                                    }

                                    // Floating Pin Label
                                    if (!pinLabel.isNullOrBlank()) {
                                        val tagYOffset = if (pinY > 0.35f) pinYDp - 40.dp else pinYDp + 24.dp
                                        val tagXOffset = (pinXDp - 60.dp).coerceIn(6.dp, (drawnWidthDp - 120.dp).coerceAtLeast(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .offset(x = tagXOffset, y = tagYOffset)
                                                .background(Color(0xFF16191D).copy(alpha = 0.95f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFFFFCC00), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = pinLabel,
                                                color = Color(0xFFFFCC00),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tactile Zoom HUD Overlay (Bottom End)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Zoom Out
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222830))
                                .clickable {
                                    val newScale = (scale - 0.5f).coerceAtLeast(1.0f)
                                    scale = newScale
                                    if (newScale == 1.0f) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➖", fontSize = 11.sp)
                        }

                        // Reset Zoom Scale Indicator
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF181E24))
                                .clickable {
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", scale)}x ↺",
                                color = Color(0xFF00FFCC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Zoom In
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222830))
                                .clickable {
                                    scale = (scale + 0.5f).coerceAtMost(4.0f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", fontSize = 11.sp)
                        }
                    }
                }

                // 3. Bottom Information & Progression Banner
                if (!firstEncounter.isNullOrBlank() || !subsequentAvailability.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161A1E))
                            .padding(12.dp)
                    ) {
                        if (!firstEncounter.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "FIRST ENCOUNTER & UNLOCK PIN",
                                        color = Color(0xFF00FFCC),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = firstEncounter.replace("First Encounter / Earliest Unlock: ", "").replace("First Encounter: ", ""),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (!subsequentAvailability.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "REGION-WIDE MART PROGRESSION",
                                        color = Color(0xFF00BFFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = subsequentAvailability.replace("Region-Wide Availability: ", "").replace("Subsequent Availability: ", ""),
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        if (selectedLandmark != null) {
            Spacer(modifier = Modifier.height(6.dp))
            MapLandmarkInspectorCard(
                landmark = selectedLandmark!!,
                onClose = { selectedLandmark = null },
                onSelectConnectedArea = { areaName ->
                    val matched = landmarks.firstOrNull { lm ->
                        areaName.contains(lm.name, ignoreCase = true) ||
                        areaName.contains(lm.shortName, ignoreCase = true) ||
                        (lm.id.startsWith("r") && areaName.contains(lm.name.substringBefore(" ("), ignoreCase = true))
                    }
                    if (matched != null) selectedLandmark = matched
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

            if (showLayerFilterModal) {
                MapLayersFilterModal(
                    regionName = editionName,
                    landmarks = landmarks,
                    enabledTypes = enabledLandmarkTypes,
                    onToggleType = { toggled ->
                        enabledLandmarkTypes = if (enabledLandmarkTypes.contains(toggled)) {
                            enabledLandmarkTypes - toggled
                        } else {
                            enabledLandmarkTypes + toggled
                        }
                    },
                    onSetAll = { newSet -> enabledLandmarkTypes = newSet },
                    onDismiss = { showLayerFilterModal = false }
                )
            }
}


@Composable
fun GameGuideModal(
    viewModel: PokedexViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val preferredEdition by viewModel.preferredGameEdition.collectAsState()
    val guideQuery by viewModel.guideQuery.collectAsState()
    val guideResult by viewModel.guideResult.collectAsState()
    val isSearching by viewModel.isGuideSearching.collectAsState()

    var showEditionPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val activeCartridge = remember(preferredEdition) {
        val effectiveId = if (preferredEdition.isNotBlank() && !preferredEdition.equals("Default", ignoreCase = true)) {
            preferredEdition
        } else {
            viewModel.getEffectiveGameEdition()
        }
        ALL_GAME_CARTRIDGES.firstOrNull {
            it.id.equals(effectiveId, ignoreCase = true) ||
            it.candidateKeys.any { k -> k.equals(effectiveId, ignoreCase = true) }
        } ?: ALL_GAME_CARTRIDGES.firstOrNull { it.id == "Emerald" || it.id == "Red" } ?: ALL_GAME_CARTRIDGES.first()
    }

    androidx.activity.compose.BackHandler(enabled = (showEditionPicker || showCategoryPicker)) {
        if (showEditionPicker) {
            showEditionPicker = false
        } else if (showCategoryPicker) {
            showCategoryPicker = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF14181B),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFCC).copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // 1. Header with Cartridge Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧭", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ULTIMATE GAME GUIDE",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "AI Quest Navigator & Strategy Guide",
                                color = Color.Gray,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    // Symmetrical Popout Control Bar: Cartridge & Category Popouts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 1. Version Popout Button
                        Button(
                            onClick = { showEditionPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = activeCartridge.cartridgeColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .border(1.dp, activeCartridge.textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "${activeCartridge.displayName} ▼",
                                color = activeCartridge.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 2. Topics & Categories Popout Button
                        Button(
                            onClick = { showCategoryPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3238)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "📚 TOPICS ▼",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Search & Ask Input Bar
                OutlinedTextField(
                    value = guideQuery,
                    onValueChange = { viewModel.updateGuideQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Ask anything (e.g. 'Exp. Share', 'Sudowoodo', 'Move Relearner', 'Swampert build')...",
                            color = Color.Gray,
                            fontSize = 11.5.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00FFCC),
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.searchGuide(guideQuery, activeCartridge.id)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (guideQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    viewModel.updateGuideQuery("")
                                    viewModel.searchGuide("help", activeCartridge.id)
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }) {
                                    Text("✕", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = {
                                viewModel.searchGuide(guideQuery, activeCartridge.id)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }) {
                                Text("🔍", fontSize = 16.sp)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Tactile Strategy Domain Launcher Bar (Zero Side-Scrolling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showCategoryPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📚", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BROWSE 13 STRATEGY DOMAINS ▶",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (guideQuery.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.updateGuideQuery("")
                                viewModel.searchGuide("help", activeCartridge.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("CLEAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Results Card & Walkthrough Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    if (isSearching) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00FFCC))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("CONSULTING DEXTER'S STRATEGY DATABASE...", color = Color(0xFF00FFCC), fontSize = 11.sp)
                        }
                    } else if (guideResult != null) {
                        val res = guideResult!!
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val isFoldableWide = maxWidth > 650.dp && res.mapAsset != null

                            if (isFoldableWide) {
                                // 🌟 FOLDABLE TWO-PANE ADAPTIVE LAYOUT (Pixel Fold unfolded 2076x2152)
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // LEFT PANE: Full Walkthrough, Encounter Unlocks & Strategy Instructions
                                    Column(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .fillMaxHeight()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        // Title & Category Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (res.itemIconAsset != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(end = 10.dp)
                                                            .size(40.dp)
                                                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                                            .border(1.5.dp, Color(0xFFFFCC00).copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        PixelItemIcon(
                                                            itemName = res.itemIconAsset,
                                                            modifier = Modifier.size(30.dp)
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = res.title,
                                                        color = Color(0xFFFFCC00),
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${res.category} • ${res.editionName.uppercase()} EDITION",
                                                        color = Color(0xFF00FFCC),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            // Spoken Audio Button
                                            Button(
                                                onClick = { viewModel.readGuideAdvice(res) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("🗣 READ GUIDE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Location & Progression Banners
                                        if (res.firstEncounter != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF00FFCC).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("📍", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(text = "FIRST ENCOUNTER & UNLOCK PIN", color = Color(0xFF00FFCC), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Text(text = res.firstEncounter.replace("First Encounter / Earliest Unlock: ", "").replace("First Encounter: ", ""), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            if (res.subsequentAvailability != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFF00BFFF).copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                                                        .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("🌐", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(text = "REGION-WIDE MART PROGRESSION", color = Color(0xFF00BFFF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                            Text(text = res.subsequentAvailability.replace("Region-Wide Availability: ", "").replace("Subsequent Availability: ", ""), color = Color.White.copy(alpha = 0.95f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("📍", fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(text = "LOCATION / CONTEXT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = res.location, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        if (!res.prerequisites.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "🔑 Prerequisite / Cost: ${res.prerequisites}",
                                                color = Color(0xFFFF9900),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Step-by-Step Instructions
                                        Text(
                                            text = "WALKTHROUGH & STRATEGY:",
                                            color = Color(0xFF00FFCC),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        res.steps.forEach { step ->
                                            Text(
                                                text = step,
                                                color = Color.White.copy(alpha = 0.95f),
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // RIGHT PANE: Full-Height Interactive Region Map Pinpoint
                                    Column(
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .fillMaxHeight()
                                    ) {
                                        GuideMapPinCanvas(
                                            mapAsset = res.mapAsset!!,
                                            pinX = res.pinX,
                                            pinY = res.pinY,
                                            pinLabel = res.pinLocationName ?: res.location,
                                            editionName = res.editionName,
                                            firstEncounter = res.firstEncounter,
                                            subsequentAvailability = res.subsequentAvailability,
                                            fillMaxHeight = true,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            } else {
                                // COMPACT PHONE / SINGLE-PANE LAYOUT
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    // Title & Category Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (res.itemIconAsset != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 10.dp)
                                                        .size(40.dp)
                                                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                                        .border(1.5.dp, Color(0xFFFFCC00).copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    PixelItemIcon(
                                                        itemName = res.itemIconAsset,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = res.title,
                                                    color = Color(0xFFFFCC00),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 15.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${res.category} • ${res.editionName.uppercase()} EDITION",
                                                    color = Color(0xFF00FFCC),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Spoken Audio Button
                                        Button(
                                            onClick = { viewModel.readGuideAdvice(res) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("🗣 READ GUIDE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Location & Progression Banners
                                    if (res.firstEncounter != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF00FFCC).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📍", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(text = "FIRST ENCOUNTER & UNLOCK PIN", color = Color(0xFF00FFCC), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                    Text(text = res.firstEncounter.replace("First Encounter / Earliest Unlock: ", "").replace("First Encounter: ", ""), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (res.subsequentAvailability != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF00BFFF).copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🌐", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(text = "REGION-WIDE MART PROGRESSION", color = Color(0xFF00BFFF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                        Text(text = res.subsequentAvailability.replace("Region-Wide Availability: ", "").replace("Subsequent Availability: ", ""), color = Color.White.copy(alpha = 0.95f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📍", fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(text = "LOCATION / CONTEXT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = res.location, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    if (!res.prerequisites.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "🔑 Prerequisite / Cost: ${res.prerequisites}",
                                            color = Color(0xFFFF9900),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (res.mapAsset != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        GuideMapPinCanvas(
                                            mapAsset = res.mapAsset,
                                            pinX = res.pinX,
                                            pinY = res.pinY,
                                            pinLabel = res.pinLocationName ?: res.location,
                                            editionName = res.editionName,
                                            firstEncounter = res.firstEncounter,
                                            subsequentAvailability = res.subsequentAvailability
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Step-by-Step Instructions
                                    Text(
                                        text = "WALKTHROUGH & STRATEGY:",
                                        color = Color(0xFF00FFCC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    res.steps.forEach { step ->
                                        Text(
                                            text = step,
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditionPicker) {
            GameEditionModal(
                title = "SELECT GAME VERSION",
                subtitle = "Choose an edition to ask quest, item, and map questions for:",
                cartridges = ALL_GAME_CARTRIDGES.filter { it.id != "Default" },
                selectedCartridgeId = activeCartridge.id,
                onSelectCartridge = { selectedCart ->
                    showEditionPicker = false
                    viewModel.setPreferredGameEdition(selectedCart.id)
                    val q = if (guideQuery.isBlank()) "help" else guideQuery
                    viewModel.searchGuide(q, selectedCart.id)
                },
                onDismiss = { showEditionPicker = false }
            )
        }

        if (showCategoryPicker) {
            val categories = remember { com.example.pokedex.data.PokemonGameGuideRepository.getCategories(context) }
            GuideCategoryPopoutModal(
                categories = categories,
                selectedCategoryId = null,
                onSelectTopic = { topicName ->
                    showCategoryPicker = false
                    viewModel.selectGuideTopic(topicName, activeCartridge.id)
                },
                onDismiss = { showCategoryPicker = false }
            )
        }
    }
}









