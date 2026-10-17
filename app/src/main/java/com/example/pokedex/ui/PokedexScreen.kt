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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.FilterQuality
import com.example.pokedex.data.EvolutionGuideRepository
import com.example.pokedex.data.EvolutionTarget
import com.example.pokedex.data.PokemonEntity
import com.example.pokedex.data.getCanonicalId
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
    GameCartridge("Legends: Z-A", "LEGENDS: Z-A", listOf("Legends: Z-A", "Legends Z-A", "Legends ZA", "ZA", "Lumiose"), Color(0xFF00E676), Color.Black, "Gen 9", "legendsza.png")
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(min = 200.dp, max = 540.dp)
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
                    // SECTION 1: ALTERNATE FORMS & MEGAS
                    if (pokemon.forms.isNotEmpty()) {
                        val formsList = pokemon.forms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val currentName = pokemon.name.lowercase()
                        val baseCandidate = formsList.firstOrNull { !it.contains("-") } ?: if (!currentName.contains("-")) currentName else currentName.substringBefore("-")
                        
                        val orderedForms = mutableListOf<String>()
                        if (currentName.equals(baseCandidate, ignoreCase = true) || formsList.contains(baseCandidate)) {
                            orderedForms.add(baseCandidate)
                        }
                        val otherForms = (formsList + listOf(currentName))
                            .filter { !it.equals(baseCandidate, ignoreCase = true) }
                            .distinct()
                            .sorted()
                        orderedForms.addAll(otherForms)

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

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    orderedForms.forEach { formName ->
                                        val isCurrent = formName.equals(currentName, ignoreCase = true)
                                        val label = when {
                                            formName.equals(baseCandidate, ignoreCase = true) -> "BASE FORM"
                                            formName.endsWith("-mega-x", ignoreCase = true) -> "MEGA ${baseCandidate.uppercase()} X"
                                            formName.endsWith("-mega-y", ignoreCase = true) -> "MEGA ${baseCandidate.uppercase()} Y"
                                            formName.endsWith("-mega-z", ignoreCase = true) -> "MEGA ${baseCandidate.uppercase()} Z"
                                            formName.endsWith("-mega", ignoreCase = true) -> "MEGA ${baseCandidate.uppercase()}"
                                            formName.endsWith("-gmax", ignoreCase = true) -> "${baseCandidate.uppercase()} G-MAX"
                                            formName.endsWith("-alola", ignoreCase = true) -> "ALOLAN ${baseCandidate.uppercase()}"
                                            formName.endsWith("-galar", ignoreCase = true) -> "GALARIAN ${baseCandidate.uppercase()}"
                                            formName.endsWith("-hisui", ignoreCase = true) -> "HISUIAN ${baseCandidate.uppercase()}"
                                            formName.endsWith("-paldea", ignoreCase = true) -> "PALDEAN ${baseCandidate.uppercase()}"
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
                                                .fillMaxWidth()
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
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (stoneItem != null) {
                                                    PixelItemIcon(
                                                        itemName = stoneItem,
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .padding(end = 8.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = label,
                                                        color = if (isCurrent) Color(0xFF00FFCC) else Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    val formEntity = allList.firstOrNull { it.name.equals(formName, ignoreCase = true) }
                                                    if (formEntity != null && formEntity.type.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            formEntity.type.split("/").forEach { typeName ->
                                                                val trimmed = typeName.trim()
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(getColorForType(trimmed), RoundedCornerShape(3.dp))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text(
                                                                        text = trimmed.uppercase(),
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 8.sp
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
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(
                                                    text = if (isCurrent) "ACTIVE" else "SWAP",
                                                    color = if (isCurrent) Color.Black else Color.White,
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

                    // SECTION 2: FAMILY EVOLUTION CHAIN
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

                    // SECTION 3: HOW TO EVOLVE (Methods & Pixel Items)
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

                        if (evolutions.isEmpty()) {
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
                                evolutions.forEach { evo ->
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
                                                Text(
                                                    text = "★ $noteText",
                                                    color = Color(0xFFFFDD00),
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
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
    val scanStreak by viewModel.scanStreak.collectAsState()
    val candidatePokemon by viewModel.candidatePokemon.collectAsState()

    val isResultState = !isListOnly && uiState is PokedexState.Result

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(if (isResultState) 6.dp else 16.dp),
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
                        ActiveScannerHud(
                            scanStatusText = scanStatusText,
                            scanStreak = scanStreak,
                            candidatePokemon = candidatePokemon,
                            onGalleryClick = onGalleryClick,
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
                            var showShiny by remember(state.pokemon.id) { mutableStateOf(false) }
                            var showEvolutionModal by remember { mutableStateOf(false) }
                            var showEditionModal by remember { mutableStateOf(false) }
                            val currentSprite = if (showShiny && !state.pokemon.shinySpriteUrl.isNullOrEmpty()) {
                                state.pokemon.shinySpriteUrl
                            } else {
                                state.pokemon.spriteUrl
                            }

                            // 1. Top Header: Number, Name, Type Badge
                            val canonicalId = state.pokemon.getCanonicalId(pokemonList)
                            val displayName = state.pokemon.name.replace("-", " ")
                                .split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

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
                                    fontWeight = FontWeight.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = state.pokemon.type.uppercase(),
                                        color = Color(0xFF00FFCC),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 2. Centered Monitor Sprite Stage with Page-Turn Edge Arrows
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.selectPreviousPokemon() },
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Text("◀", color = Color(0xFF00FFCC), fontSize = 26.sp, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(125.dp)
                                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                        .border(2.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = currentSprite,
                                        contentDescription = state.pokemon.name,
                                        modifier = Modifier.size(115.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )

                                    if (!state.pokemon.shinySpriteUrl.isNullOrEmpty()) {
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

                                IconButton(
                                    onClick = { viewModel.selectNextPokemon() },
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Text("▶", color = Color(0xFF00FFCC), fontSize = 26.sp, fontWeight = FontWeight.Black)
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

                                val rawFlavor = viewModel.getEffectiveFlavorText(state.pokemon)
                                val currentCleanedFlavor = rawFlavor
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

                                if (!state.pokemon.shinySpriteUrl.isNullOrEmpty()) {
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

                            Spacer(modifier = Modifier.height(3.dp))

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

                            val preferredEdition by viewModel.preferredGameEdition.collectAsState()

                            val availableCartridges = remember(entriesMap) {
                                ALL_GAME_CARTRIDGES.filter { cartridge ->
                                    cartridge.id == "Default" || entriesMap.keys.any { key ->
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

                            val cleanedFlavorText = displayFlavorText
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
                                        IconButton(
                                            onClick = { viewModel.readFlavorText(state.pokemon, displayFlavorText) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("🔊", fontSize = 15.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { showEvolutionModal = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.heightIn(min = 30.dp)
                                        ) {
                                            Text("⚡ FORMS & EVOS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Button(
                                            onClick = { showEditionModal = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = activeCartridge.cartridgeColor),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 30.dp)
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

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = cleanedFlavorText,
                                        color = Color.White,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (showEvolutionModal) {
                                EvolutionAndFormsModal(
                                    pokemon = state.pokemon,
                                    activeEditionKey = activeCartridge.id,
                                    availableCartridges = availableCartridges,
                                    evolutionChain = evolutionChain,
                                    viewModel = viewModel,
                                    onCartridgeSelected = { newCartridgeId ->
                                        selectedCartridgeId = newCartridgeId
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



